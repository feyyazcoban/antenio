package dev.iptvplayer.tv.data

import org.junit.Assert.*
import org.junit.Test

class EpisodeSequenceTest {
    private val source = SourceConfig(id = "source", name = "Example", type = SourceType.XTREAM,
        address = "https://source.example/", username = "synthetic", password = "synthetic")
    private fun episode(id: String, season: Int, number: Int) = MediaEntry(id, id, MediaKind.EPISODE, "Bölüm $number",
        extension = "mp4", season = season, episode = number, episodeOrderKnown = true)
    private val first = episode("1", 1, 1)

    @Test fun sortsProviderRowsAndAdvancesWithinSeason() {
        val next = episode("2", 1, 2)
        val choice = EpisodeSequence.next(source, first, listOf(next, first))!!
        assertEquals(next, choice.entry); assertTrue(choice.automaticAllowed)
    }
    @Test fun crossesOnlyAnAdjacentSeasonStartingAtOne() {
        val last = episode("3", 1, 12); val next = episode("4", 2, 1)
        assertTrue(EpisodeSequence.next(source, last, listOf(last, next))!!.automaticAllowed)
        assertFalse(EpisodeSequence.next(source, last, listOf(last, next.copy(season = 3)))!!.automaticAllowed)
        assertFalse(EpisodeSequence.next(source, last, listOf(last, next.copy(episode = 2)))!!.automaticAllowed)
    }
    @Test fun gapsRemainManualWithoutInventingAnEpisode() {
        val available = episode("3", 1, 3)
        val choice = EpisodeSequence.next(source, first, listOf(first, available))!!
        assertEquals(available, choice.entry); assertFalse(choice.automaticAllowed)
        assertTrue(choice.notice!!.contains("eksik"))
    }
    @Test fun unknownDuplicateOrInvalidNumbersRemainManual() {
        val second = episode("2", 1, 2)
        assertFalse(EpisodeSequence.next(source, first, listOf(first.copy(episodeOrderKnown = false), second))!!.automaticAllowed)
        assertFalse(EpisodeSequence.next(source, first, listOf(first, first.copy(id = "alternate"), second))!!.automaticAllowed)
        val unknown = first.copy(episode = 0)
        assertFalse(EpisodeSequence.next(source, unknown, listOf(unknown, second.copy(episode = 1)))!!.automaticAllowed)
    }
    @Test fun inaccessibleTargetDoesNotSkipAhead() {
        val second = episode("2", 1, 2); val third = episode("3", 1, 3)
        assertNull(EpisodeSequence.next(source, first, listOf(first, second.copy(adult = true), third)))
        assertNull(EpisodeSequence.next(source, first, listOf(first, second.copy(extension = ""), third)))
        assertNull(EpisodeSequence.next(source, first, listOf(first, second.copy(uri = "file:///private/video"), third)))
    }
    @Test fun missingCurrentFinalEpisodeAndOtherKindsHaveNoNext() {
        val second = episode("2", 1, 2)
        assertNull(EpisodeSequence.next(source, first, listOf(second)))
        assertNull(EpisodeSequence.next(source, second, listOf(first, second)))
        assertNull(EpisodeSequence.next(source, first.copy(kind = MediaKind.MOVIE), listOf(first, second)))
        assertNull(EpisodeSequence.next(source, first.copy(adult = true), listOf(first, second)))
    }
    @Test fun m3uNumberingIsInferredAndSurvivesCache() {
        val parsed = M3uParser.parse("#EXTM3U\n#EXTINF:-1,Test S01E01\nhttps://media.example/series/one.mp4\n#EXTINF:-1,Test S01E02\nhttps://media.example/series/two.mp4\n".reader(), "https://media.example/list.m3u")
        val restored = CatalogSnapshotCodec.decode(CatalogSnapshotCodec.encode(CatalogSnapshot(parsed, 1))).catalog
        val episodes = restored.m3uEpisodes.values.single()
        val m3u = source.copy(type = SourceType.M3U)
        val choice = EpisodeSequence.next(m3u, episodes.first(), episodes)!!
        assertTrue(choice.automaticAllowed); assertTrue(choice.notice!!.contains("başlık"))
    }
    @Test fun oldCacheWithoutNumberingEvidenceStillLoadsButDoesNotAutoplay() {
        val items = CatalogSnapshotCodec.decode(legacyCatalogFixture()).catalog.m3uEpisodes["show"]!!
        assertEquals(2, items.size); assertFalse(items.first().episodeOrderKnown)
        assertFalse(EpisodeSequence.next(source, items.first(), items)!!.automaticAllowed)
    }
    @Test fun xtreamMissingNumbersDoNotAcquireEvidenceFromFallbackIndices() {
        val show = first.copy(kind = MediaKind.SERIES)
        val (_, episodes) = CatalogParser.episodes("""{"episodes":{"1":[{"id":"1","container_extension":"mp4"},{"id":"2","episode_num":2,"container_extension":"mp4"}]}}""", show)
        assertFalse(episodes.first().episodeOrderKnown); assertTrue(episodes.last().episodeOrderKnown)
        assertFalse(EpisodeSequence.next(source, episodes.first(), episodes)!!.automaticAllowed)
    }
}
