package dev.iptvplayer.tv.data

import org.junit.Assert.*
import org.junit.Test

class GuideControllerTest {
    private val hour = 3_600_000L
    private val now = 1_800_000_000_000L
    private val wanted = GuideWindow(now - 2 * hour, now + 24 * hour)
    private fun saved(updatedAt: Long = now - hour, window: GuideWindow = wanted, languages: List<String> = listOf("en")) =
        EpgSnapshotInfo(updatedAt, window, channels = 1, programmes = 1, invalid = 0, outsideWindow = 0, languages = languages)

    @Test fun aFreshGuideInTheWantedLanguagesStays() {
        assertFalse(GuideController.stale(saved(), now, wanted, listOf("en")))
        assertTrue(GuideController.stale(null, now, wanted, listOf("en")))
        assertTrue(GuideController.stale(saved(updatedAt = now - 7 * hour), now, wanted, listOf("en")))
        assertTrue(GuideController.stale(saved(updatedAt = now + hour), now, wanted, listOf("en")))
        assertTrue(GuideController.stale(saved(window = GuideWindow(wanted.from, wanted.until - hour)), now, wanted, listOf("en")))
    }

    @Test fun anotherLanguageOrTheSameLanguagesInAnotherOrderDownloadTheGuideAgain() {
        // An English app on a German TV picks English titles first, a German app would pick German ones.
        assertFalse(GuideController.stale(saved(languages = listOf("en", "de")), now, wanted, listOf("en", "de")))
        assertTrue(GuideController.stale(saved(languages = listOf("en", "de")), now, wanted, listOf("de", "en")))
        assertTrue(GuideController.stale(saved(languages = listOf("en")), now, wanted, listOf("tr", "en")))
    }

    @Test fun aGuideSavedBeforeTheAppSpokeEnglishReadsAsTurkish() {
        // EpgStore reads a snapshot without a languages column as ["tr"]: still fresh for a Turkish app on a Turkish TV.
        val before = saved(languages = listOf("tr"))
        assertFalse(GuideController.stale(before, now, wanted, listOf("tr")))
        assertTrue(GuideController.stale(before, now, wanted, listOf("en")))
        assertTrue(GuideController.stale(before, now, wanted, listOf("tr", "en")))
    }
}
