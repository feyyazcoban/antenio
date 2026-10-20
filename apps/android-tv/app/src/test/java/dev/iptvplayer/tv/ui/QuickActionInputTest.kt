package dev.iptvplayer.tv.ui

import android.view.KeyEvent.*
import org.junit.Assert.*
import org.junit.Test

class QuickActionInputTest {
    @Test fun openingPressCannotPickAnActionButTheNextPressCan() {
        for (key in listOf(KEYCODE_DPAD_CENTER, KEYCODE_ENTER, KEYCODE_NUMPAD_ENTER)) {
            val input = QuickActionInput()
            repeat(4) { assertTrue(input.consume(key, ACTION_DOWN, it + 1)) }
            assertTrue(input.consume(key, ACTION_UP, 0))
            assertFalse(input.consume(key, ACTION_DOWN, 0))
            repeat(3) { assertTrue(input.consume(key, ACTION_DOWN, it + 1)) }
            assertFalse(input.consume(key, ACTION_UP, 0))
            assertTrue(input.consume(key, ACTION_UP, 0))
        }
    }

    @Test fun accessibilityOpeningDoesNotSwallowTheFirstFreshOkOrNavigation() {
        val input = QuickActionInput()
        for (key in listOf(KEYCODE_DPAD_DOWN, KEYCODE_DPAD_UP, KEYCODE_BACK)) {
            assertFalse(input.consume(key, ACTION_DOWN, 0))
            assertFalse(input.consume(key, ACTION_UP, 0))
        }
        assertFalse(input.consume(KEYCODE_ENTER, ACTION_DOWN, 0))
        assertFalse(input.consume(KEYCODE_ENTER, ACTION_UP, 0))
    }

    @Test fun losingWindowFocusDiscardsAnUnreleasedPress() {
        val input = QuickActionInput()
        assertFalse(input.consume(KEYCODE_DPAD_CENTER, ACTION_DOWN, 0))
        input.clear()
        assertTrue(input.consume(KEYCODE_DPAD_CENTER, ACTION_UP, 0))
        assertFalse(input.consume(KEYCODE_DPAD_CENTER, ACTION_DOWN, 0))
        assertFalse(input.consume(KEYCODE_DPAD_CENTER, ACTION_UP, 0))
    }
}
