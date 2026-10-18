package dev.iptvplayer.tv

import org.junit.Assert.*
import org.junit.Test
import java.util.Locale

class LocaleControllerTest {
    private fun shown(tag: String?, hour24: Boolean = true) =
        LocaleController.shownLocale(tag?.let(Locale::forLanguageTag), hour24).toLanguageTag()

    @Test fun theAppSpeaksTurkishOrEnglishAndEnglishForAnyOtherLanguage() {
        assertEquals("tr-TR-u-hc-h23", shown("tr-TR"))
        assertEquals("en-GB-u-hc-h23", shown("en-GB"))
        assertEquals("en-US-u-hc-h12", shown("en-US", hour24 = false))
        // German, French or Arabic TVs read the default English texts, so dates and numbers are English too.
        listOf("de-DE", "fr-FR", "ar-EG", null).forEach { assertEquals(it, "en-u-hc-h23", shown(it)) }
        // The pseudo-locales: en-XA is English, ar-XB shows the English texts mirrored.
        assertEquals("en-XA-u-hc-h23", shown("en-XA"))
        assertEquals("en-u-hc-h23", shown("ar-XB"))
    }

    @Test fun aTvsOwnDigitsAndCalendarNeverReachTheAppsNumbers() {
        assertEquals("tr-TR-u-hc-h23", shown("tr-TR-u-nu-arab-ca-islamic"))
    }

    @Test fun guideTitlesFollowTheAppsLanguageAndThenTheTvs() {
        val english = LocaleController.shownLocale(Locale.ENGLISH, hour24 = true)
        assertEquals(listOf("en", "de"), LocaleController.guideLanguages(english, Locale.GERMANY))
        assertEquals(listOf("en"), LocaleController.guideLanguages(english, Locale.US))
        assertEquals(listOf("en"), LocaleController.guideLanguages(english, null))
        assertEquals(listOf("tr", "en"), LocaleController.guideLanguages(LocaleController.shownLocale(Locale.forLanguageTag("tr"), true), Locale.US))
    }

    @Test fun onlyTheAppsLanguagesAreKeptAsAChoice() {
        assertEquals(AppLanguage.TURKISH, AppLanguage.of("tr"))
        assertEquals(AppLanguage.ENGLISH, AppLanguage.of("en"))
        listOf(null, "", "de", "TR", "tr-TR").forEach { assertEquals(it, AppLanguage.SYSTEM, AppLanguage.of(it)) }
    }
}
