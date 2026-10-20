package dev.iptvplayer.tv.ui

import org.junit.Assert.*
import org.junit.Test

class ChannelPanelRulesTest {
    @Test fun pagingOverlapsOneVisibleRowAndClampsBothEnds() {
        assertEquals(6, PanelPaging.target(0, 1, 7, 18))
        assertEquals(0, PanelPaging.target(6, -1, 7, 18))
        assertEquals(17, PanelPaging.target(15, 1, 7, 18))
        assertEquals(0, PanelPaging.target(0, -1, 7, 18))
        assertEquals(0, PanelPaging.target(0, 1, 0, 0))
        assertEquals(1, PanelPaging.target(0, 1, 1, 18))
    }
    @Test fun panelOnlyClosesAtItsDeadlineAndNeverWithTalkBackOrNeverSetting() {
        assertFalse(PanelAutoClose.shouldClose(15_099, 100, 15_000, false))
        assertTrue(PanelAutoClose.shouldClose(15_100, 100, 15_000, false))
        assertFalse(PanelAutoClose.shouldClose(99_000, 100, 15_000, true))
        assertFalse(PanelAutoClose.shouldClose(99_000, 100, null, false))
        assertFalse(PanelAutoClose.shouldClose(99, 100, 15_000, false))
    }
}
