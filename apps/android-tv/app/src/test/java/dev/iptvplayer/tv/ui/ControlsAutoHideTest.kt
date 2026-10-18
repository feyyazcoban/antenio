package dev.iptvplayer.tv.ui

import dev.iptvplayer.tv.data.OsdTimeout
import org.junit.Assert.*
import org.junit.Test

class ControlsAutoHideTest {
    /** [quiet] ms after the last key, with a stream playing under the controls unless said otherwise. */
    private fun hides(quiet: Long, timeout: OsdTimeout = OsdTimeout.S8, playing: Boolean = true, controlsOnTop: Boolean = true,
        failed: Boolean = false, recovering: Boolean = false, talkBack: Boolean = false) =
        ControlsAutoHide.shouldHide(100_000 + quiet, 100_000, timeout, playing, controlsOnTop, failed, recovering, talkBack)

    @Test fun theControlsGoOnceTheChosenTimeHasPassed() {
        for (timeout in OsdTimeout.entries.filter { it.ms != null }) {
            assertFalse(timeout.name, hides(timeout.ms!!, timeout))
            assertTrue(timeout.name, hides(timeout.ms!! + 1, timeout))
        }
        // Eight seconds is what the player always did.
        assertFalse(hides(8_000)); assertTrue(hides(8_001))
    }

    @Test fun neverKeepsThemUntilBack() {
        assertFalse(hides(Long.MAX_VALUE / 2, OsdTimeout.NEVER))
    }

    @Test fun theyStayWhileTheStreamIsNotPlayingOrSomethingCoversThem() {
        // Paused or opening, under the Ses / Altyazı panel or the end card.
        assertFalse(hides(60_000, playing = false))
        assertFalse(hides(60_000, controlsOnTop = false))
    }

    @Test fun theyStayWhileTheConnectionIsInTrouble() {
        assertFalse(hides(60_000, recovering = true))
        assertFalse(hides(60_000, failed = true))
    }

    @Test fun theyStayWhileTalkBackReadsTheScreen() {
        for (timeout in OsdTimeout.entries) assertFalse(timeout.name, hides(60_000, timeout, talkBack = true))
    }
    @Test fun pendingSeekKeepsControlsVisible() {
        assertFalse(ControlsAutoHide.shouldHide(60_000, 0, OsdTimeout.S8, true, true, false, false, false, scrubbing = true))
    }

}
