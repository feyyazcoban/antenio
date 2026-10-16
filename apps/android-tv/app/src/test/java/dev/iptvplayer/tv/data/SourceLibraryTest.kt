package dev.iptvplayer.tv.data

import org.junit.Assert.*
import org.junit.Test

class SourceLibraryTest {
    private val a = SourceConfig("a:b", "Ev", SourceType.M3U, "https://example.invalid/a.m3u")
    private val b = a.copy(id = "a", name = "Ev", address = "https://example.invalid/b.m3u")
    private val movie = MediaEntry("movie:1", "1", MediaKind.MOVIE, "Aynı film", extension = "mp4")
    private val show = MediaEntry("series:2", "2", MediaKind.SERIES, "Dizi")
    private val episode = MediaEntry("episode:3", "3", MediaKind.EPISODE, "Bölüm")
    private val catalog = Catalog(listOf(movie, show), mapOf(show.id to listOf(episode)))

    @Test fun lengthPrefixedKeysCannotCollideAndRoundTripUnicode() {
        val refs = listOf(ContentRef("a:b", "c"), ContentRef("a", "b:c"), ContentRef("Ev 📺", "bölüm:1"))
        assertEquals(3, refs.map { it.key }.distinct().size)
        refs.forEach { assertEquals(it, ContentRef.parse(it.key)) }
        listOf("", "a:b", "0:abc", "-1:abc", "99:abc", "1:a", "999999999999999:x").forEach { assertNull(ContentRef.parse(it)) }
    }
    @Test fun identicalProviderIdsAndTitlesRemainTwoEntries() {
        val all = SourceLibrary.combine(listOf(a, b), mapOf(a.id to catalog, b.id to catalog))
        assertEquals(4, all.items.size)
        assertEquals(4, all.items.map { it.id }.distinct().size)
        assertEquals(listOf(a.id,a.id,b.id,b.id), all.items.map { it.sourceId })
        assertEquals(movie, all.items.first().withoutSource())
        assertEquals(movie.inSource(a.id), movie.inSource(a.id).inSource(a.id))
        assertThrows(IllegalArgumentException::class.java) { movie.inSource(a.id).inSource(b.id) }
    }
    @Test fun episodesAndTheirParentAreScopedTogether() {
        val all = SourceLibrary.combine(listOf(a, b), mapOf(a.id to catalog, b.id to catalog))
        val parent = ContentRef(b.id, show.id).key
        val target = all.m3uEpisodes.getValue(parent).single()
        assertEquals(b.id, target.sourceId)
        assertEquals(parent, FavoriteTargets.resolve(target, all)!!.id)
    }
    @Test fun filterKeepsOnlyItsOwnCatalogAndMissingCacheDoesNotEraseOthers() {
        val one = SourceLibrary.combine(listOf(a,b),mapOf(a.id to catalog,b.id to catalog),b.id)
        assertTrue(one.items.all { it.sourceId == b.id })
        assertEquals(2, SourceLibrary.combine(listOf(a,b),mapOf(a.id to catalog)).items.size)
        assertTrue(SourceLibrary.combine(listOf(a,b),mapOf(a.id to catalog),b.id).items.isEmpty())
        assertThrows(IllegalArgumentException::class.java) { SourceLibrary.combine(listOf(a,b),emptyMap(),"missing") }
    }
    @Test fun favoritesAndContinueWatchingDoNotMergeDifferentSources() {
        val all = SourceLibrary.combine(listOf(a,b),mapOf(a.id to catalog,b.id to catalog))
        val favorites = SourceLibrary.favorites(listOf(a,b)) { setOf(movie.id) }
        val records = SourceLibrary.history(listOf(a,b)) { id ->
            listOf(WatchRecord(episode.id, MediaKind.EPISODE, show.id, positionMs = 1000, durationMs = 10000,
                updatedAt = if (id == b.id) 20 else 10))
        }
        val home = homeLibrary(all, records, favorites)
        assertEquals(2, home.favorites.size)
        assertEquals(listOf(b.id,a.id), home.continueWatching.map { it.entry.sourceId })
        assertEquals(episode.id, records.first().withoutSource().id)
        assertEquals(show.id, records.first().withoutSource().parentId)
    }
    @Test fun sourceBoundaryRejectsForeignHistoryAndCacheWrites() {
        val record = WatchRecord(episode.id, MediaKind.EPISODE, show.id).inSource(a.id)
        assertEquals(record, record.inSource(a.id))
        assertThrows(IllegalArgumentException::class.java) { record.inSource(b.id) }
        assertThrows(IllegalArgumentException::class.java) { WatchHistory.encode(listOf(record)) }
        assertThrows(IllegalArgumentException::class.java) {
            CatalogSnapshotCodec.encode(CatalogSnapshot(Catalog(listOf(movie.inSource(a.id))),1))
        }
    }
}
