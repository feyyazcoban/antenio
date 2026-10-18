package dev.iptvplayer.tv.ui

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

    @Test fun datesAndTimesReadInTurkishAsBefore() {
        assertEquals("tr", appLocale.language)
        assertEquals("20:30", guideTime(evening, istanbul))
        assertEquals("18 Eki Pazar", guideDate(evening, istanbul))
        assertEquals("18 Eki 20:30", guideDayTime(evening, istanbul))
        assertEquals("UTC+03:00", guideOffset(evening, istanbul))
        assertEquals("UTC+00:00", guideOffset(evening, ZoneOffset.UTC))
        assertEquals("18 Eki, 20:30", updatedTime(evening, istanbul))
        assertEquals("20:30 — 21:30", programmeTime(EpgProgramme("c", "Akşam", evening, evening + hour), istanbul, unknownEnd = "?"))
        assertEquals("20:30 — ?", programmeTime(EpgProgramme("c", "Akşam", evening, null), istanbul, unknownEnd = "?"))
        // Across midnight both ends carry their day.
        assertEquals("18 Eki 20:30 — 19 Eki 01:30", programmeTime(EpgProgramme("c", "Gece", evening, evening + 5 * hour), istanbul, unknownEnd = "?"))
    }

    @Test fun clockRatingAndLanguageNamesKeepTheirShape() {
        assertEquals("0:00", clockTime(-5))
        assertEquals("4:07", clockTime(247_000))
        assertEquals("1:05:09", clockTime(3_909_000))
        assertEquals("7,5", ratingText(7.5f))
        assertEquals("8,0", MediaEntry("r", "r", MediaKind.MOVIE, "Film").copy(rating = 8f).ratingText)
        assertNull(ratingText(0f))
        assertEquals("İngilizce", languageName("en"))
        assertEquals("Almanca", languageName("de"))
        // A code without a name stays as the stream sent it; no language at all leaves the fallback to the caller.
        assertEquals("qqq", languageName("qqq"))
        listOf(null, "", " ", "und").forEach { assertNull(it, languageName(it)) }
    }

    @Test fun formatsFollowTheLocaleTheyAreGivenAndNeverTheTvs() {
        assertEquals("18 Oct Sunday", guideDate(evening, istanbul, Locale.ENGLISH))
        assertEquals("7.5", ratingText(7.5f, Locale.ENGLISH))
        assertEquals("English", languageName("en", Locale.ENGLISH))
        // A TV whose own language writes digits in another script would otherwise put them on the player's clock.
        val tv = Locale.getDefault()
        try {
            Locale.setDefault(Locale.forLanguageTag("ar-EG"))
            assertEquals("1:05:09", clockTime(3_909_000))
            assertEquals("7,5", ratingText(7.5f))
            assertEquals("18 Eki Pazar", guideDate(evening, istanbul))
            assertEquals("İngilizce", languageName("en"))
        } finally {
            Locale.setDefault(tv)
        }
    }
}
