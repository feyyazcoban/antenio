package dev.iptvplayer.tv.data

import org.junit.Assert.*
import org.junit.Test

class FavoriteTargetsTest {
    private val show = MediaEntry("series:1", "1", MediaKind.SERIES, "Dizi")
    private val movie = MediaEntry("movie:2", "2", MediaKind.MOVIE, "Film")
    private val episode = MediaEntry("episode:3", "3", MediaKind.EPISODE, "Bölüm")
    private val catalog = Catalog(listOf(show, movie))

    @Test fun ordinaryFavoritesRequireAVisibleCatalogEntry() {
        assertEquals(movie, FavoriteTargets.resolve(movie, catalog))
        assertNull(FavoriteTargets.resolve(movie.copy(id = "missing"), catalog))
        assertNull(FavoriteTargets.resolve(movie, catalog.copy(items = listOf(movie.copy(adult = true)))))
    }
    @Test fun episodeUsesItsLoadedSeriesAndNotAnUnrelatedSelection() {
        assertEquals(show, FavoriteTargets.resolve(episode, catalog, show, listOf(episode)))
        assertNull(FavoriteTargets.resolve(episode, catalog, show, emptyList()))
        assertNull(FavoriteTargets.resolve(episode.copy(adult = true), catalog, show, listOf(episode)))
        assertNull(FavoriteTargets.resolve(episode, catalog, movie, listOf(episode)))
    }
    @Test fun m3uAndHistoryCanResolveTheParentWithoutAnOpenDetail() {
        assertEquals(show, FavoriteTargets.resolve(episode, catalog.copy(m3uEpisodes = mapOf(show.id to listOf(episode)))))
        val history = listOf(WatchRecord(episode.id, MediaKind.EPISODE, show.id))
        assertEquals(show, FavoriteTargets.resolve(episode, catalog, history = history))
        assertNull(FavoriteTargets.resolve(episode, Catalog(emptyList()), history = history))
    }
    @Test fun oldEpisodeStarsCollapseToOneSeriesAndPreserveOtherFavorites() {
        val more = episode.copy(id = "episode:4")
        val result = FavoriteTargets.migrate(setOf(episode.id, more.id, show.id, movie.id), catalog, show, listOf(episode, more))
        assertEquals(setOf(show.id, movie.id), result)
    }
    @Test fun unresolvedStarsAreRetainedUntilTheirDetailCanBeLoaded() {
        assertEquals(setOf(episode.id), FavoriteTargets.migrate(setOf(episode.id), catalog))
        val history = listOf(WatchRecord(episode.id, MediaKind.EPISODE, show.id))
        assertEquals(setOf(show.id), FavoriteTargets.migrate(setOf(episode.id), catalog, history = history))
    }
    @Test fun blockedParentsAreNotRevealedByMigrationOrResolution() {
        val blocked = catalog.copy(items = listOf(show.copy(adult = true)))
        assertNull(FavoriteTargets.resolve(episode, blocked, show, listOf(episode)))
        assertEquals(setOf(episode.id), FavoriteTargets.migrate(setOf(episode.id), blocked, show, listOf(episode)))
    }
}
