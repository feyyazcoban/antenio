package dev.iptvplayer.tv

import android.view.KeyEvent

/** Keeps a skip press out of the newly revealed screen, including a held key's repeats and release. */
internal class OpeningInput {
    private var skippedKey: Int? = null

    fun consume(key: Int, action: Int, opening: Boolean, skip: () -> Unit): Boolean {
        if (key == skippedKey) {
            if (action == KeyEvent.ACTION_UP) skippedKey = null
            return true
        }
        if (opening && action == KeyEvent.ACTION_DOWN) {
            skip()
            if (key in NavigationKeys) { skippedKey = key; return true }
        }
        return false
    }

    fun clear() { skippedKey = null }
}

private val NavigationKeys = setOf(KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN,
    KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_DPAD_CENTER,
    KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER, KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_ESCAPE)
