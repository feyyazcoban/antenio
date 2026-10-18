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
    @Test fun aFewSecondsOfAFilmAreNotWorthResuming() {
        val film = 80 * 60_000L
        assertFalse(PlaybackMark(19_000, film, 0).resumable)
        assertTrue(PlaybackMark(2 * 60_000, film, 0).resumable)
        assertTrue(PlaybackMark(10_000, 120_000, 0).resumable)
        assertFalse(PlaybackMark(60_000, 0, 0).resumable)
        assertFalse(PlaybackMark(film * 96 / 100, film, 0).resumable)
        val home = homeLibrary(Catalog(listOf(entry("movie", MediaKind.MOVIE))),
            listOf(WatchRecord("movie", MediaKind.MOVIE, positionMs = 19_000, durationMs = film)), emptySet())
        assertTrue(home.continueWatching.isEmpty()); assertEquals(listOf("movie"), home.movies.map { it.id })
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
    @Test fun lockedTitlesStayOutOfTheHeroAndAutomaticShelvesButNotOutOfWhatTheViewerChose() {
        fun titled(id: String, kind: MediaKind, added: Long = 0) =
            MediaEntry(id, id, kind, "Title $id", artwork = "http://img/$id.jpg", backdrop = "http://img/$id-wide.jpg", addedAt = added)
        val catalog = Catalog(listOf(titled("film", MediaKind.MOVIE, 100), titled("night", MediaKind.MOVIE, 900),
            titled("show", MediaKind.SERIES, 800), titled("lockedShow", MediaKind.SERIES, 700),
            titled("news", MediaKind.LIVE), titled("sport", MediaKind.LIVE)))
        val locked = setOf("night", "lockedShow", "sport")
        val history = listOf(WatchRecord("night", MediaKind.MOVIE, positionMs = 600_000, durationMs = 5_400_000),
            WatchRecord("sport", MediaKind.LIVE))
        val home = homeLibrary(catalog, history, setOf("lockedShow"), ParentalLocks(locked))
        assertEquals(listOf("show", "film"), home.newest.map { it.id })
        assertEquals(listOf("film"), home.movies.map { it.id })
        assertEquals(listOf("show"), home.series.map { it.id })
        assertEquals(listOf("news"), home.channels.map { it.id })
        assertFalse(home.featured!!.id in locked)
        // Chosen by the viewer: they stay, and ask for the PIN when opened.
        assertEquals(listOf("night"), home.continueWatching.map { it.entry.id })
        assertEquals(listOf("sport"), home.recentChannels.map { it.id })
        assertEquals(listOf("lockedShow"), home.favorites.map { it.id })
        // Without locks the same titles are promoted.
        assertEquals(listOf("night", "show", "lockedShow", "film"), homeLibrary(catalog, history, emptySet()).newest.map { it.id })
        // The locks cannot be read (D049): every title asks for the PIN, so none is promoted; what the viewer chose stays.
        val unknown = homeLibrary(catalog, history, setOf("lockedShow"), ParentalLocks(unknown = true))
        assertTrue(unknown.newest.isEmpty()); assertNull(unknown.featured)
        assertTrue(unknown.movies.isEmpty() && unknown.series.isEmpty() && unknown.channels.isEmpty())
        assertEquals(listOf("night"), unknown.continueWatching.map { it.entry.id })
        assertEquals(listOf("sport"), unknown.recentChannels.map { it.id })
        assertEquals(listOf("lockedShow"), unknown.favorites.map { it.id })
    }

    @Test fun aTitleAPinOpenedForTheSessionIsStillNotPromoted() {
        fun titled(id: String, kind: MediaKind, added: Long = 0) =
            MediaEntry(id, id, kind, "Title $id", artwork = "http://img/$id.jpg", backdrop = "http://img/$id-wide.jpg", addedAt = added)
        val catalog = Catalog(listOf(titled("film", MediaKind.MOVIE, 100), titled("night", MediaKind.MOVIE, 900),
            titled("sport", MediaKind.LIVE)))
        // The PIN opened them until the app leaves the screen; the hero and the shelves stay as they were.
        val home = homeLibrary(catalog, emptyList(), emptySet(), ParentalLocks(setOf("night", "sport"), open = setOf("night", "sport")))
        assertEquals(listOf("film"), home.newest.map { it.id })
        assertEquals(listOf("film"), home.movies.map { it.id })
        assertTrue(home.channels.isEmpty())
    }

    @Test fun aListWithoutDatesHasNoNewestSlidesAndKeepsTheFeaturedTitle() {
        val catalog = Catalog(listOf(MediaEntry("m", "m", MediaKind.MOVIE, "Movie", artwork = "http://img/m.jpg")))
        val home = homeLibrary(catalog, emptyList(), emptySet())
        assertTrue(home.newest.isEmpty())
        assertEquals("m", home.featured?.id)
    }
}
