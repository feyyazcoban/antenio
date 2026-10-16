package dev.iptvplayer.tv.data

import kotlinx.coroutines.*
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPOutputStream

class EpgSourceTest {
    private val source = SourceConfig("test", "Fixture", SourceType.M3U, "https://example.com/list.m3u")
    private val emptySink = object : EpgSink { override fun channel(value: EpgChannel) {}; override fun programme(value: EpgProgramme) {} }

    @Test fun playlistDiscoveryPreservesIdsNamesAndFeedsInEncryptedSnapshotPayload() {
        val catalog = M3uParser.parse("""#EXTM3U x-tvg-url="guide.xml, https://epg.example/guide.xml.gz?key=private-marker" url-tvg="guide.xml"
            #EXTINF:-1 tvg-id="c.tr" tvg-name="Örnek Kanal" group-title="Canlı",Kanal HD
            /live/1.ts
            """.trimIndent().reader(), source.address)
        assertEquals(listOf("https://example.com/guide.xml", "https://epg.example/guide.xml.gz?key=private-marker"), catalog.epgUrls)
        assertEquals("c.tr", catalog.items.single().epgId); assertEquals("Örnek Kanal", catalog.items.single().epgName)
        assertEquals(catalog, CatalogSnapshotCodec.decode(CatalogSnapshotCodec.encode(CatalogSnapshot(catalog, 1))).catalog)
        assertFalse(catalog.toString().contains("private-marker"))
        assertFalse(EpgPlan.create(source, catalog).toString().contains("private-marker"))
    }
    @Test fun emptyAndUnsafeFeedAttributesNeverResolveToThePlaylistItself() {
        val parsed = M3uParser.parse("#EXTM3U x-tvg-url=\" , file:///private, https://user:password@example.com/a\"".reader(), source.address)
        assertTrue(parsed.epgUrls.isEmpty()); assertNull(EpgPlan.create(source, parsed))
    }
    @Test fun xtreamUsesEpgChannelIdAndEncodesXmltvCredentials() {
        val xtream = source.copy(type = SourceType.XTREAM, address = "https://example.com/base/player_api.php?ignored=x", username = "a+b@example.com", password = "x/y&z")
        val url = xtream.xmltvUrl()
        assertEquals("/base/xmltv.php", url.encodedPath); assertEquals("a+b@example.com", url.queryParameter("username")); assertEquals("x/y&z", url.queryParameter("password"))
        assertNull(url.queryParameter("ignored"))
        assertEquals("e.tr", CatalogParser.xtream("""[{"stream_id":1,"name":"A","epg_channel_id":"e.tr"}]""", MediaKind.LIVE, emptyMap()).single().epgId)
        assertEquals("", CatalogParser.xtream("""[{"stream_id":1,"name":"A","epg_channel_id":null}]""", MediaKind.LIVE, emptyMap()).single().epgId)
    }
    @Test fun planBindingSeparatesSourcesCredentialsAndFeedChangesButNotDisplayName() {
        val catalog = Catalog(emptyList(), epgUrls = listOf("https://example.com/a"))
        val a = EpgPlan.create(source, catalog)!!
        assertEquals(a.binding, EpgPlan.create(source.copy(name = "New"), catalog)!!.binding)
        listOf(EpgPlan.create(source.copy(id = "other"), catalog)!!, EpgPlan.create(source.copy(password = "changed"), catalog)!!,
            EpgPlan.create(source, catalog, "https://example.com/b")!!).forEach { assertNotEquals(a.binding, it.binding) }
        assertThrows(SourceFailure::class.java) { EpgPlan.create(source, catalog, "file:///bad") }
    }
    @Test fun combinedLibrariesKeepChannelIdsButNeverPoolPrivateFeedAddresses() {
        val entry = MediaEntry("live:1", "1", MediaKind.LIVE, "A", epgId = "a.epg", epgName = "Alias")
        val a = Catalog(listOf(entry), epgUrls = listOf("https://example.invalid/a"))
        val b = a.copy(epgUrls = listOf("https://example.invalid/b"))
        val other = source.copy(id = "other")
        val combined = SourceLibrary.combine(listOf(source, other), mapOf(source.id to a, other.id to b))
        assertTrue(combined.epgUrls.isEmpty())
        assertEquals(listOf("a.epg", "a.epg"), combined.items.map { it.epgId })
        assertEquals(listOf(source.id, other.id), combined.items.map { it.sourceId })
    }
    @Test fun excessFeedLimitIsVisibleInCatalogWarnings() {
        val urls = (1..5).joinToString(",") { "https://example.invalid/$it" }
        val catalog = M3uParser.parse("#EXTM3U x-tvg-url=\"$urls\"".reader(), source.address)
        assertEquals(4, catalog.epgUrls.size); assertEquals(1, catalog.warnings.size)
    }
    @Test fun plainAndGzippedFeedsAreReadWithoutWholeBodyStrings() = runBlocking {
        MockWebServer().use { server ->
            val xml = "<tv><channel id='c'><display-name>Örnek</display-name></channel></tv>"
            val zipped = ByteArrayOutputStream().also { out -> GZIPOutputStream(out).use { it.write(xml.toByteArray()) } }.toByteArray()
            server.enqueue(MockResponse().setBody(xml))
            server.enqueue(MockResponse().setBody(Buffer().write(zipped)))
            server.enqueue(MockResponse().setHeader("Content-Encoding", "gzip").setBody(Buffer().write(zipped)))
            server.start()
            repeat(3) { assertEquals(1, EpgRepository().download(server.url("/guide").toString(), GuideWindow(0, Long.MAX_VALUE), emptySink).channels) }
        }
    }
    @Test fun cancellingWhileBodyIsStalledClosesTheActualNetworkCall() = runBlocking {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody("<tv>${" ".repeat(100)}</tv>").throttleBody(1, 3, TimeUnit.SECONDS))
            server.start()
            val bodyStarted = java.util.concurrent.CountDownLatch(1)
            val client = okhttp3.OkHttpClient.Builder().readTimeout(40, TimeUnit.SECONDS).eventListener(object : okhttp3.EventListener() {
                override fun responseBodyStart(call: okhttp3.Call) { bodyStarted.countDown() }
            }).build()
            val job = launch(Dispatchers.IO) { EpgRepository(client).download(server.url("/").toString(), GuideWindow(0, Long.MAX_VALUE), emptySink) }
            assertNotNull(server.takeRequest(5, TimeUnit.SECONDS))
            assertTrue(bodyStarted.await(5, TimeUnit.SECONDS))
            withTimeout(2_000) { job.cancelAndJoin() }
            assertEquals(0, client.dispatcher.runningCallsCount())
        }
    }
    @Test fun authenticationFailureNeverExposesRemoteBodyOrUrl() = runBlocking {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setResponseCode(403).setBody("private-marker")); server.start()
            try { EpgRepository().download(server.url("/private-marker").toString(), GuideWindow(0, 1), emptySink); fail("Expected failure") }
            catch (e: SourceFailure) {
                generateSequence<Throwable>(e) { it.cause }.forEach { assertFalse(it.message.orEmpty().contains("private-marker")) }
            }
        }
    }
}
