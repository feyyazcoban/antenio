package dev.iptvplayer.tv.data

/**
 * The PIN the pad takes (pin.md §3.1): four ASCII digits, from the remote's number keys or the pad on screen.
 * PinCredential itself would take up to eight; the pad asks for four, the same on every remote.
 */
internal object PinFormat {
    const val LENGTH = 4

    /** Four ASCII digits; nothing else reaches PinCredential, which would refuse it. */
    fun isValid(pin: CharArray): Boolean = pin.size == LENGTH && pin.all { it in '0'..'9' }

    /**
     * Too easy to guess for a new PIN: one digit four times (1111), or a run up or down the digits (0123 … 6789,
     * 9876 … 3210). Read in place, so the digits never become a String.
     */
    fun isEasy(pin: CharArray): Boolean {
        if (!isValid(pin)) return false
        val step = pin[1] - pin[0]
        return step in -1..1 && (2 until LENGTH).all { pin[it] - pin[it - 1] == step }
    }
}
