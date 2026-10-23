package dev.iptvplayer.tv.data

import kotlinx.coroutines.*
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.TimeUnit

class SourceTest {
    private fun entry(kind: MediaKind = MediaKind.MOVIE, extension: String = "mkv") =
        MediaEntry("movie:4", "4", kind, "Film", extension = extension)

    @Test fun credentialsAreEncodedAsSingleUrlSegments() {
        val source = SourceConfig(name = "Test", type = SourceType.XTREAM, address = "https://example.com/get.php?discard=yes",
            username = "a+b@example.com", password = "x/y?z#").validated()
        assertEquals("https://example.com/", source.address)
        assertEquals("a+b@example.com", source.apiUrl().queryParameter("username"))
        assertEquals("x/y?z#", source.apiUrl().queryParameter("password"))
        assertTrue(source.streamUrl(entry()).contains("x%2Fy%3Fz%23/4.mkv"))
        assertFalse(source.toString().contains("x/y"))
    }

    @Test fun missingVodExtensionNeverInventsMp4() {
        val source = SourceConfig(name = "Test", type = SourceType.XTREAM, address = "https://example.com", username = "u", password = "p")
        assertEquals(FailureReason.STREAM_FORMAT_MISSING, assertThrows(SourceFailure::class.java) { source.streamUrl(entry(extension = "")) }.reason)
        assertTrue(source.streamUrl(entry(MediaKind.LIVE, "")).endsWith(".ts"))
    }

    @Test fun sourceRejectsNonHttpAndBasicUserInfo() {
        mapOf("file:///private" to FailureReason.ADDRESS_INVALID,
            "https://username:password@example.com" to FailureReason.ADDRESS_HAS_CREDENTIALS).forEach { (address, reason) ->
            assertEquals(reason, assertThrows(SourceFailure::class.java) { SourceConfig(name = "", type = SourceType.M3U, address = address).validated() }.reason)
        }
    }

    @Test fun quotedCommaAndRelativeUrlsKeepTitlesAndHeaders() {
        val catalog = M3uParser.parse("""
            #EXTM3U
            #EXTINF:-1 tvg-logo="https://example.com/a,b.png" group-title="News, World",A, B News
            #EXTVLCOPT:http-user-agent=Fixture Agent
            #EXTVLCOPT:http-referrer=https://example.com/
            /live/channel.ts
        """.trimIndent().reader(), "https://example.com/playlist.m3u")
        val item = catalog.items.single()
        assertEquals("A, B News", item.title)
        assertEquals("News, World", item.category)
        assertEquals("https://example.com/live/channel.ts", item.uri)
        assertEquals("Fixture Agent", item.headers["User-Agent"])
        assertEquals(MediaKind.LIVE, item.kind)
    }

    @Test fun m3uSeriesAreGroupedAndOrderedWithoutLosingMovies() {
        val catalog = M3uParser.parse("""
            #EXTM3U
            #EXTINF:-1 group-title="Shows",Example S02E01
            https://example.com/series/u/p/3.mkv
            #EXTINF:-1 group-title="Shows",Example S01E02
            https://example.com/series/u/p/2.mkv
            #EXTINF:-1 group-title="Shows",Example S01E01
            https://example.com/series/u/p/1.mkv
            #EXTINF:-1 group-title="Films",A movie
            https://example.com/movie/u/p/9.mp4
        """.trimIndent().reader(), "https://example.com/list.m3u")
        val show = catalog.items.single { it.kind == MediaKind.SERIES }
        assertEquals("Example", show.title)
        assertEquals(listOf(1 to 1, 1 to 2, 2 to 1), catalog.m3uEpisodes.getValue(show.id).map { it.season to it.episode })
        assertEquals(1, catalog.items.count { it.kind == MediaKind.MOVIE })
    }

    @Test fun hlsPlaylistAndHtmlAreRejectedAsCatalogs() {
        mapOf("<html>error</html>" to FailureReason.LIST_NOT_M3U,
            "#EXTM3U\n#EXT-X-TARGETDURATION:6\n#EXTINF:6,\na.ts" to FailureReason.LIST_SINGLE_STREAM).forEach { (text, reason) ->
            assertEquals(reason, assertThrows(SourceFailure::class.java) { M3uParser.parse(text.reader(), "https://example.com/") }.reason)
        }
    }

    @Test fun duplicateUrlsDoNotCreateDuplicateCards() {
        val text = "#EXTM3U\n#EXTINF:-1,A\nhttps://example.com/live/1.ts\n#EXTINF:-1,B\nhttps://example.com/live/1.ts"
        assertEquals(1, M3uParser.parse(text.reader(), "https://example.com/").items.size)
    }

    @Test fun xtreamRatingsAndAddedTimesAreReadAndUnknownOnesStayZero() {
        val list = CatalogParser.xtream("""[{"stream_id":1,"name":"A","rating":"7.5","added":"1700000000"},
            {"stream_id":2,"name":"B","rating":"","rating_5based":4},{"stream_id":3,"name":"C","rating":"N/A","added":""},
            {"stream_id":4,"name":"D","rating":"6,8"},{"stream_id":5,"name":"E","rating":"42"}]""", MediaKind.MOVIE, emptyMap())
        assertEquals(listOf(7.5f, 8f, 0f, 6.8f, 0f), list.map { it.rating })
        assertEquals(listOf(1_700_000_000L, 0L, 0L, 0L, 0L), list.map { it.addedAt })
        val series = CatalogParser.xtream("""[{"series_id":1,"name":"S","rating":"8.9","last_modified":"1690000000"}]""", MediaKind.SERIES, emptyMap())
        assertEquals(8.9f, series.single().rating); assertEquals(1_690_000_000L, series.single().addedAt)
        val live = CatalogParser.xtream("""[{"stream_id":1,"name":"L","rating":"9"}]""", MediaKind.LIVE, emptyMap())
        assertEquals(0f, live.single().rating)
    }

    @Test fun xtreamCatalogHandlesNullArtAndAdultFlag() {
        val list = CatalogParser.xtream("""[{"stream_id":7,"name":"Channel","stream_icon":null,"category_id":"2","is_adult":"1"},{"name":"invalid"}]""",
            MediaKind.LIVE, mapOf("2" to "Category"))
        assertEquals(1, list.size)
        assertNull(list.single().artwork)
        assertTrue(list.single().adult)
        assertEquals("Category", list.single().category)
    }

    @Test fun episodeApiReadsActualContainerSeasonAndRuntime() {
        val show = entry(MediaKind.SERIES).copy(id = "series:1", title = "Example")
        val result = CatalogParser.episodes("""{"info":{"plot":"Description"},"episodes":{"2":[{"id":"9","title":"Finale","episode_num":3,"container_extension":"mkv","info":{"duration":"00:42:13"}}],"1":[{"id":"8","episode_num":1,"container_extension":"mp4"}]}}""", show)
        assertEquals("Description", result.first.description)
        assertEquals(listOf(1, 2), result.second.map { it.season })
        assertEquals(2533L, result.second.last().durationSeconds)
        assertEquals("mkv", result.second.last().extension)
    }

    @Test fun completedVideoRestartsButIncompleteVideoResumes() {
        assertEquals(0L, PlaybackMark(119_000, 120_000, 0).resumeMs)
        assertEquals(60_000L, PlaybackMark(60_000, 120_000, 0).resumeMs)
        assertEquals(60_000L, PlaybackMark(60_000, 0, 0).resumeMs)
    }

    @Test fun repositoryKeepsSuccessfulCatalogWhenOneKindFails() = runBlocking {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody("""{"user_info":{"auth":1,"status":"Active"}}"""))
            server.enqueue(MockResponse().setBody("[]"))
            server.enqueue(MockResponse().setBody("""[{"stream_id":1,"name":"One"}]"""))
            server.enqueue(MockResponse().setBody("[]"))
            server.enqueue(MockResponse().setResponseCode(503))
            server.enqueue(MockResponse().setBody("[]"))
            server.enqueue(MockResponse().setBody("[]"))
            server.start()
            val source = SourceConfig(name = "Fixture", type = SourceType.XTREAM, address = server.url("/").toString(), username = "fixture", password = "fixture")
            val result = SourceRepository().load(source) {}
            assertEquals(1, result.items.size)
            assertEquals(listOf(CatalogWarning(CatalogWarning.Reason.KIND_UNAVAILABLE, MediaKind.MOVIE)), result.warnings)
        }
    }

    @Test fun authenticationErrorsDoNotIncludeSourceSecrets() = runBlocking {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setResponseCode(403).setBody("private-marker"))
            server.start()
            val source = SourceConfig(name = "Fixture", type = SourceType.XTREAM, address = server.url("/").toString(), username = "private-marker", password = "private-marker")
            try { SourceRepository().load(source) {}; fail("Expected source failure") }
            catch (e: SourceFailure) {
                assertEquals(FailureReason.ACCESS_DENIED, e.reason)
                assertFalse(e.message.orEmpty().contains("private-marker"))
                assertFalse(e.message.orEmpty().contains(server.hostName))
                // Coroutine stack recovery may copy a safe exception into its cause chain.
                generateSequence<Throwable>(e) { it.cause }.take(8).forEach { cause ->
                    assertTrue(cause is SourceFailure)
                    assertFalse(cause.message.orEmpty().contains("private-marker"))
                }
            }
        }
    }

    @Test fun pendingImportCanBeCancelled() = runBlocking {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setHeadersDelay(3, TimeUnit.SECONDS).setBody("#EXTM3U"))
            server.start()
            val source = SourceConfig(name = "Fixture", type = SourceType.M3U, address = server.url("/list").toString())
            val job = launch { SourceRepository().load(source) {}; fail("Cancelled import completed") }
            withContext(Dispatchers.IO) { checkNotNull(server.takeRequest(3, TimeUnit.SECONDS)) }
            withTimeout(2000) { job.cancelAndJoin() }
            assertTrue(job.isCancelled)
        }
    }

    @Test fun onlyPlainHttpAddressesAreUnencrypted() {
        assertTrue(isUnencrypted("http://example.com:8080"))
        assertTrue(isUnencrypted("  HTTP://example.com/get.php?username=a&password=b "))
        assertFalse(isUnencrypted("https://example.com"))
        assertFalse(isUnencrypted(""))
        assertFalse(isUnencrypted("example.com"))
    }

    @Test fun largeCatalogKeepsEveryUniqueEntry() {
        val text = buildString { append("#EXTM3U\n"); repeat(63_175) { append("#EXTINF:-1 group-title=\"Test\",Channel $it\nhttps://example.com/live/$it.ts\n") } }
        assertEquals(63_175, M3uParser.parse(text.reader(), "https://example.com/list").items.size)
    }
}
