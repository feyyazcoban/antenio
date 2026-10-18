package dev.iptvplayer.tv

import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.iptvplayer.tv.ui.guideDate
import dev.iptvplayer.tv.ui.guideDayTime
import dev.iptvplayer.tv.ui.guideUpdatedTime
import dev.iptvplayer.tv.ui.updatedTime
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

/**
 * Dates with Android's own patterns (S13): each language and region in its order, which the JVM tests cannot see, and the
 * TV's clock by its language when it has no 12/24-hour setting of its own. Changes nothing on the device.
 */
@RunWith(AndroidJUnit4::class)
class LocaleFormatsTest {
    private val istanbul = ZoneId.of("Europe/Istanbul")
    /** Sunday 18 October 2026, 20:30 in Istanbul. */
    private val evening = Instant.parse("2026-10-18T17:30:00Z").toEpochMilli()
    private val hour = 3_600_000L
    private fun shown(tag: String, hour24: Boolean = true) = LocaleController.shownLocale(Locale.forLanguageTag(tag), hour24)

    @Test fun datesFollowTheLanguageAndItsRegion() {
        assertEquals("18 Eki Pazar", guideDate(evening, istanbul, shown("tr-TR")))
        assertEquals("18 Eki, 20:30", updatedTime(evening, istanbul, shown("tr-TR")))
        assertEquals("19 Eki 01:30", guideDayTime(evening + 5 * hour, istanbul, shown("tr-TR")))
        assertEquals("Sunday 18 Oct", guideDate(evening, istanbul, shown("en-GB")))
        assertEquals("18 Oct, 20:30", updatedTime(evening, istanbul, shown("en-GB")))
        assertEquals("Sunday, Oct 18", guideDate(evening, istanbul, shown("en-US", hour24 = false)))
        assertEquals("Sunday, Oct 18, 8:30 PM", guideUpdatedTime(evening, istanbul, shown("en-US", hour24 = false)))
        assertEquals("Oct 18, 8:30 PM", updatedTime(evening, istanbul, shown("en-US", hour24 = false)))
        assertEquals("Oct 19 1:30 AM", guideDayTime(evening + 5 * hour, istanbul, shown("en-US", hour24 = false)))
    }

    @Test fun withoutASettingTheClockFollowsTheTvsLanguage() {
        assertTrue(LocaleController.hour24(null, Locale.forLanguageTag("tr-TR")))
        assertTrue(LocaleController.hour24(null, Locale.UK))
        assertFalse(LocaleController.hour24(null, Locale.US))
        assertFalse(LocaleController.hour24("12", Locale.forLanguageTag("tr-TR")))
    }
}
