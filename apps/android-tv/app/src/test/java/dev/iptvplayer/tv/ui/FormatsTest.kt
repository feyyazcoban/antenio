package dev.iptvplayer.tv.ui

import dev.iptvplayer.tv.LocaleController
import dev.iptvplayer.tv.data.EpgProgramme
import dev.iptvplayer.tv.data.MediaEntry
import dev.iptvplayer.tv.data.MediaKind
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Locale

class FormatsTest {
    private val istanbul = ZoneId.of("Europe/Istanbul")
    /** Sunday 18 October 2026, 20:30 in Istanbul. */
    private val evening = Instant.parse("2026-10-18T17:30:00Z").toEpochMilli()
    private val hour = 3_600_000L
    private val turkish = LocaleController.shownLocale(Locale.forLanguageTag("tr-TR"), hour24 = true)
    private val english = LocaleController.shownLocale(Locale.forLanguageTag("en-GB"), hour24 = true)
    /** An English TV that keeps a 12-hour clock. */
    private val american = LocaleController.shownLocale(Locale.US, hour24 = false)

    @Test fun datesAndTimesReadInTurkishAsBefore() {
        assertEquals("20:30", guideTime(evening, istanbul, turkish))
        assertEquals("18 Eki Pazar", guideDate(evening, istanbul, turkish))
        assertEquals("18 Eki 20:30", guideDayTime(evening, istanbul, turkish))
        assertEquals("UTC+03:00", guideOffset(evening, istanbul))
        assertEquals("UTC+00:00", guideOffset(evening, ZoneOffset.UTC))
        assertEquals("18 Eki, 20:30", updatedTime(evening, istanbul, turkish))
        assertEquals("18 Eki Pazar, 20:30", guideUpdatedTime(evening, istanbul, turkish))
        assertEquals("20:30 — 21:30", programmeTime(EpgProgramme("c", "Akşam", evening, evening + hour), istanbul, unknownEnd = "?", turkish))
        assertEquals("20:30 — ?", programmeTime(EpgProgramme("c", "Akşam", evening, null), istanbul, unknownEnd = "?", turkish))
        // Across midnight both ends carry their day.
        assertEquals("18 Eki 20:30 — 19 Eki 01:30", programmeTime(EpgProgramme("c", "Gece", evening, evening + 5 * hour), istanbul,
            unknownEnd = "?", turkish))
    }

    // The JVM has no Android date patterns, so these are the fallbacks; on a TV each region writes its own order, as
    // LocaleFormatsTest checks there ("Sunday, Oct 18" in American English).
    @Test fun englishPutsTheWeekdayFirstAndFollowsTheTvsClock() {
        assertEquals("20:30", guideTime(evening, istanbul, english))
        assertEquals("Sunday 18 Oct", guideDate(evening, istanbul, english))
        assertEquals("18 Oct 20:30", guideDayTime(evening, istanbul, english))
        assertEquals("18 Oct, 20:30", updatedTime(evening, istanbul, english))
        assertEquals("Sunday 18 Oct, 20:30", guideUpdatedTime(evening, istanbul, english))
        assertEquals("8:30 PM", guideTime(evening, istanbul, american))
        assertEquals("Sunday 18 Oct, 8:30 PM", guideUpdatedTime(evening, istanbul, american))
        assertEquals("18 Oct 8:30 PM — 19 Oct 1:30 AM", programmeTime(EpgProgramme("c", "Night", evening, evening + 5 * hour), istanbul,
            unknownEnd = "?", american))
    }

    @Test fun clockRatingAndLanguageNamesKeepTheirShape() {
        assertEquals("0:00", clockTime(-5, turkish))
        assertEquals("4:07", clockTime(247_000, turkish))
        assertEquals("1:05:09", clockTime(3_909_000, english))
        assertEquals("7,5", ratingText(7.5f, turkish))
        assertEquals("7.5", ratingText(7.5f, english))
        assertNull(ratingText(0f))
        assertEquals("İngilizce", languageName("en", turkish))
        assertEquals("Almanca", languageName("de", turkish))
        assertEquals("English", languageName("en", english))
        assertEquals("German", languageName("de", american))
        // A code without a name stays as the stream sent it; no language at all leaves the fallback to the caller.
        assertEquals("qqq", languageName("qqq", turkish))
        listOf(null, "", " ", "und").forEach { assertNull(it, languageName(it)) }
    }

    @Test fun theAppsLocaleIsEnglishUntilAnActivityTellsOtherwise() {
        // Before MainActivity reads the activity's language, as on the JVM: the default texts' language.
        assertEquals("en", appLocale.language)
        assertEquals("8.0", MediaEntry("r", "r", MediaKind.MOVIE, "Film").copy(rating = 8f).ratingText)
    }

    @Test fun formatsFollowTheLocaleTheyAreGivenAndNeverTheTvs() {
        // A TV whose own language writes digits in another script would otherwise put them on the player's clock.
        val tv = Locale.getDefault()
        try {
            Locale.setDefault(Locale.forLanguageTag("ar-EG"))
            assertEquals("1:05:09", clockTime(3_909_000))
            assertEquals("7,5", ratingText(7.5f, turkish))
            assertEquals("7.5", ratingText(7.5f))
            assertEquals("18 Eki Pazar", guideDate(evening, istanbul, turkish))
            assertEquals("Sunday 18 Oct", guideDate(evening, istanbul))
            assertEquals("İngilizce", languageName("en", turkish))
        } finally {
            Locale.setDefault(tv)
        }
    }
}
