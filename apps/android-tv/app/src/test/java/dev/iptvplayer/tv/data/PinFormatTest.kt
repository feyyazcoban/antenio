package dev.iptvplayer.tv.data

import org.junit.Assert.*
import org.junit.Test

/** Synthetic PINs only. */
class PinFormatTest {
    private fun pin(digits: String) = digits.toCharArray()

    @Test fun thePadTakesFourAsciiDigits() {
        assertTrue(PinFormat.isValid(pin("2580")))
        for (wrong in listOf("258", "25801", "25a0", "٢٥٨٠", "")) assertFalse(wrong, PinFormat.isValid(pin(wrong)))
    }

    @Test fun oneDigitFourTimesAndRunsUpOrDownAreTooEasy() {
        val easy = (0..9).map { "$it".repeat(4) } + (0..6).map { start -> (start..start + 3).joinToString("") } +
            (9 downTo 3).map { start -> (start downTo start - 3).joinToString("") }
        assertEquals(24, easy.size)
        for (digits in easy) assertTrue(digits, PinFormat.isEasy(pin(digits)))
        // Not a run of single steps, a run that wraps, or a run broken in the middle.
        for (digits in listOf("2580", "1357", "8901", "1243", "1123", "0246", "9753")) assertFalse(digits, PinFormat.isEasy(pin(digits)))
        // What the pad cannot send is not judged here.
        assertFalse(PinFormat.isEasy(pin("111")))
    }
}
