package dev.iptvplayer.tv

import android.view.KeyEvent.*
import org.junit.Assert.*
import org.junit.Test

class OpeningInputTest {
    @Test fun launchersReleaseDoesNotSkipButANewPressDoes() {
        val input = OpeningInput()
        var opening = true
        assertFalse(input.consume(KEYCODE_DPAD_CENTER, ACTION_UP, opening) { opening = false })
        assertTrue(opening)
        assertTrue(input.consume(KEYCODE_DPAD_CENTER, ACTION_DOWN, opening) { opening = false })
        assertFalse(opening)
        // Holding OK through Home's first layout cannot start whatever receives initial focus.
        repeat(3) { assertTrue(input.consume(KEYCODE_DPAD_CENTER, ACTION_DOWN, opening) { fail() }) }
        assertTrue(input.consume(KEYCODE_DPAD_CENTER, ACTION_UP, opening) { fail() })
        assertFalse(input.consume(KEYCODE_DPAD_CENTER, ACTION_DOWN, opening) { fail() })
        assertFalse(input.consume(KEYCODE_DPAD_CENTER, ACTION_UP, opening) { fail() })
    }

    @Test fun volumeWorksDuringTheCoverAndDoesNotLoseAPendingSkipRelease() {
        val input = OpeningInput()
        var skipped = 0
        assertFalse(input.consume(KEYCODE_VOLUME_UP, ACTION_DOWN, true) { skipped++ })
        assertEquals(1, skipped)
        assertFalse(input.consume(KEYCODE_VOLUME_UP, ACTION_UP, false) { fail() })
        assertTrue(input.consume(KEYCODE_BACK, ACTION_DOWN, true) { skipped++ })
        assertFalse(input.consume(KEYCODE_VOLUME_DOWN, ACTION_DOWN, false) { fail() })
        assertTrue(input.consume(KEYCODE_BACK, ACTION_UP, false) { fail() })
        assertFalse(input.consume(KEYCODE_BACK, ACTION_DOWN, false) { fail() })
    }

    @Test fun leavingTheActivityDiscardsAnUnreleasedSkipPress() {
        val input = OpeningInput()
        assertTrue(input.consume(KEYCODE_DPAD_DOWN, ACTION_DOWN, true) {})
        input.clear()
        assertFalse(input.consume(KEYCODE_DPAD_DOWN, ACTION_DOWN, false) { fail() })
    }
}
