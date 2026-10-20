package dev.iptvplayer.tv.ui

import android.view.KeyEvent

/** A quick menu owns only a fresh OK press that began in its window, never the held opener's tail. */
internal class QuickActionInput {
    private var pressed: Int? = null

    fun consume(key: Int, action: Int, repeat: Int): Boolean {
        if (key != KeyEvent.KEYCODE_DPAD_CENTER && key != KeyEvent.KEYCODE_ENTER && key != KeyEvent.KEYCODE_NUMPAD_ENTER) return false
        return when (action) {
            KeyEvent.ACTION_DOWN -> if (repeat > 0) true else { pressed = key; false }
            KeyEvent.ACTION_UP -> (pressed != key).also { if (pressed == key) pressed = null }
            else -> false
        }
    }

    fun clear() { pressed = null }
}
