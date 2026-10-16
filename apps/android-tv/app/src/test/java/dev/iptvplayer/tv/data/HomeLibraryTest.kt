package dev.iptvplayer.tv.data

import org.junit.Assert.*
import org.junit.Test

class HomeLibraryTest {
    private fun entry(id: String, kind: MediaKind, adult: Boolean = false) = MediaEntry(id, id, kind, "Title $id", adult = adult)
    @Test fun unfinishedMovieAndLatestEpisodeAppearButCompletedAndRemovedItemsDoNot() {
        val catalog = Catalog(listOf(entry("movie", MediaKind.MOVIE), entry("done", MediaKind.MOVIE), entry("series", MediaKind.SERIES)))
        val history = listOf(WatchRecord("ep2", MediaKind.EPISODE, "series", 2, 1, 50, 1000),
            WatchRecord("ep1", MediaKind.EPISODE, "series", 1, 1, 50, 1000),
            WatchRecord("movie", MediaKind.MOVIE, positionMs = 300, durationMs = 1000),
            WatchRecord("done", MediaKind.MOVIE, positionMs = 960, durationMs = 1000),
            WatchRecord("gone", MediaKind.MOVIE, positionMs = 500, durationMs = 1000))
        val home = homeLibrary(catalog, history, emptySet())
        assertEquals(listOf("ep2", "movie"), home.continueWatching.map { it.record.id })
    }
    @Test fun adultItemsAreExcludedFromEveryHomeSurface() {
        val catalog = Catalog(listOf(entry("adult", MediaKind.MOVIE, true), entry("live", MediaKind.LIVE, true)))
        val home = homeLibrary(catalog, listOf(WatchRecord("adult", MediaKind.MOVIE, positionMs = 100), WatchRecord("live", MediaKind.LIVE)), setOf("adult", "live"))
        assertTrue(home.continueWatching.isEmpty()); assertTrue(home.recentChannels.isEmpty())
        assertTrue(home.favorites.isEmpty()); assertNull(home.featured)
    }
    @Test fun historyIsBoundedAndRevisitingMovesItemWithoutDuplicates() {
        var records = emptyList<WatchRecord>()
        repeat(75) { records = WatchHistory.upsert(records, WatchRecord("$it", MediaKind.LIVE)) }
        records = WatchHistory.upsert(records, WatchRecord("50", MediaKind.LIVE))
        assertEquals(50, records.size); assertEquals("50", records.first().id)
        assertEquals(50, records.map { it.id }.distinct().size)
    }
    @Test fun historyRoundTripPreservesExactEpisodeWithoutMediaAddress() {
        val records = listOf(WatchRecord("episode:4", MediaKind.EPISODE, "series:2", 3, 7, 32000, 100000, 123))
        val encoded = WatchHistory.encode(records)
        assertEquals(records, WatchHistory.decode(encoded))
        assertFalse(encoded.contains("http")); assertFalse(encoded.contains("password"))
        assertTrue(WatchHistory.decode("corrupt").isEmpty())
    }
    @Test fun unknownDurationCanResumeAndShortClipsAreNotPrematurelyCompleted() {
        assertEquals(1_000L, PlaybackMark(1000, 5000, 0).resumeMs)
        assertEquals(0L, PlaybackMark(4750, 5000, 0).resumeMs)
        assertEquals(1_000L, PlaybackMark(1000, 0, 0).resumeMs)
    }
    @Test fun recentChannelsUseCurrentCatalogDataAndDropRemovedEntries() {
        val home = homeLibrary(Catalog(listOf(entry("1", MediaKind.LIVE))), listOf(WatchRecord("2", MediaKind.LIVE), WatchRecord("1", MediaKind.LIVE)), emptySet())
        assertEquals(listOf("1"), home.recentChannels.map { it.id })
    }
    @Test fun completedLatestEpisodeKeepsItsSeriesForTheNextEpisodeWithoutResurrectingAnOlderOne() {
        val home = homeLibrary(Catalog(listOf(entry("series", MediaKind.SERIES))), listOf(
            WatchRecord("new", MediaKind.EPISODE, "series", positionMs = 960, durationMs = 1000),
            WatchRecord("old", MediaKind.EPISODE, "series", positionMs = 500, durationMs = 1000)), emptySet())
        assertEquals(listOf("new"), home.continueWatching.map { it.record.id })
        assertTrue(home.continueWatching.single().record.progress.completed)
    }
    @Test fun newestHeroSlidesAreTheLatestDatedFilmsAndSeriesWithArtwork() {
        fun dated(id: String, kind: MediaKind, added: Long, art: String? = "http://img/$id.jpg") =
            MediaEntry(id, id, kind, "Title $id", artwork = art, addedAt = added)
        val catalog = Catalog(listOf(dated("m1", MediaKind.MOVIE, 100), dated("m2", MediaKind.MOVIE, 500),
            dated("bare", MediaKind.MOVIE, 900, art = null), dated("s1", MediaKind.SERIES, 700),
            dated("tie", MediaKind.MOVIE, 100), dated("live", MediaKind.LIVE, 999),
            dated("old", MediaKind.MOVIE, 0), dated("hidden", MediaKind.SERIES, 800).copy(adult = true))
            + (1..8).map { dated("x$it", MediaKind.MOVIE, 10L + it) })
        val home = homeLibrary(catalog, emptyList(), emptySet())
        // Newest first; no artwork, no date, channels and adult titles never qualify; equal dates keep list order.
        assertEquals(listOf("s1", "m2", "m1", "tie", "x8", "x7"), home.newest.map { it.id })
    }
    @Test fun aListWithoutDatesHasNoNewestSlidesAndKeepsTheFeaturedTitle() {
        val catalog = Catalog(listOf(MediaEntry("m", "m", MediaKind.MOVIE, "Movie", artwork = "http://img/m.jpg")))
        val home = homeLibrary(catalog, emptyList(), emptySet())
        assertTrue(home.newest.isEmpty())
        assertEquals("m", home.featured?.id)
    }
}
