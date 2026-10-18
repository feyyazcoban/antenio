package dev.iptvplayer.tv

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.rules.ExternalResource

/**
 * The app's language for one test: set before its activity starts and given back to the TV's after it
 * (LocaleController.store). Ordered outside the Compose rule, `@get:Rule(order = 0)`, so the activity starts in it.
 */
open class AppLanguageRule(private val language: AppLanguage) : ExternalResource() {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    override fun before() = LocaleController.store(context, language)
    override fun after() = LocaleController.store(context, AppLanguage.SYSTEM)
}

/** The flows that assert the Turkish texts, on a test emulator whose own language is English. */
class TurkishLocaleRule : AppLanguageRule(AppLanguage.TURKISH)
