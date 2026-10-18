package dev.iptvplayer.tv.ui

import dev.iptvplayer.tv.data.*
import org.junit.Assert.*
import org.junit.Test

class PlayerControlsModelTest {
    @Test fun clockRejectsUnknownDurationAndResetsForANewMedia() {
        val clock = PlaybackClock()
        clock.update(-1, -9223372036854775807, -1, true, 100)
        assertEquals(0, clock.duration); assertEquals(0, clock.position); assertFalse(clock.seekable)
        clock.update(1000, 60_000, 50_000, true, 200); assertTrue(clock.seekable)
        clock.reset(500); assertFalse(clock.seekable); assertEquals(500, clock.position); assertEquals(0, clock.duration)
    }
    @Test fun labelDropPriorityAndLargeFontsPreserveRoomForTheCluster() {
        val chips = listOf(ControlRowPlan.Chip("previous", 180, 56, 0), ControlRowPlan.Chip("tracks", 160, 56, 1),
            ControlRowPlan.Chip("channels", 140, 56, 2), ControlRowPlan.Chip("star", 56, 56, 3))
        assertEquals(setOf("tracks", "channels"), ControlRowPlan.fit(760, 256, chips, 8, 32, emptyList(), false).labels)
        assertEquals(setOf("channels"), ControlRowPlan.fit(650, 256, chips, 8, 32, emptyList(), false).labels)
        assertEquals(emptySet<String>(), ControlRowPlan.fit(912, 256, chips, 8, 32, emptyList(), true).labels)
        assertEquals(2, ControlRowPlan.fit(300, 64, emptyList(), 0, 32, listOf(100, 80, 80), false).hints)
    }
    @Test fun episodeMetadataNeverInventsOrderOrStripsAStandaloneTitle() {
        val episode = MediaEntry("e", "e", MediaKind.EPISODE, "A complete title", sourceId = "one")
        val single = PlayerInfoModel.from(episode, null)
        assertEquals("A complete title", single.title); assertNull(single.season); assertNull(single.episodeTitle)
        val series = MediaEntry("s", "s", MediaKind.SERIES, "The Show", sourceId = "one")
        val known = PlayerInfoModel.from(episode.copy(episodeOrderKnown = true, season = 2, episode = 5), series)
        assertEquals("The Show", known.title); assertEquals(2, known.season); assertEquals(5, known.episode)
        assertEquals(single, PlayerInfoModel.from(episode, series.copy(sourceId = "another")))
    }
    @Test fun endTimeUsesActualSpeedAndIsAbsentWhenPausedOrUnknown() {
        assertEquals(120_000L, PlayerInfoModel.endMinute(60_000, 0, 120_000, 2f, true))
        assertNull(PlayerInfoModel.endMinute(60_000, 0, 0, 1f, true))
        assertNull(PlayerInfoModel.endMinute(60_000, 0, 120_000, 1f, false))
    }
}
