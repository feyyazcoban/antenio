package dev.iptvplayer.tv

import android.app.Activity
import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import android.text.format.DateFormat
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import dev.iptvplayer.tv.data.DeviceStore
import java.util.Locale

/**
 * The language chosen in Settings → Dil / Language; [SYSTEM] follows the TV. [tag] is what DeviceStore keeps, [label]
 * the option, the same in every language.
 */
enum class AppLanguage(val tag: String, @StringRes val label: Int) {
    SYSTEM("", R.string.language_system), TURKISH("tr", R.string.language_turkish), ENGLISH("en", R.string.language_english);

    companion object {
        /** A stored or system tag; anything the app does not speak follows the TV. */
        fun of(tag: String?): AppLanguage = entries.firstOrNull { it != SYSTEM && it.tag == tag } ?: SYSTEM
    }
}

/**
 * The language the app speaks (gap plan S13): English by default, Turkish, or the TV's language when the app speaks it.
 * Android 13 and later keep the viewer's choice themselves (LocaleManager) and bring the activity back in it. Android
 * 8–12 have no language of their own for an app: DeviceStore keeps the choice, [wrap] gives MainActivity a context in
 * it and [choose] recreates the activity. MainActivity stays a ComponentActivity: AppCompat's per-app language does
 * the same on those versions, but only for an AppCompatActivity with an AppCompat theme.
 *
 * [locale] is what every date, time, number and title order follows (Formats.appLocale), read from the activity each
 * time it is created; [guideLanguages] is the order XMLTV titles are picked in.
 */
object LocaleController {
    private val spoken = setOf(AppLanguage.TURKISH.tag, AppLanguage.ENGLISH.tag)

    @Volatile var locale: Locale = shownLocale(null, hour24 = true); private set
    @Volatile var guideLanguages: List<String> = guideLanguages(locale, null); private set

    fun choice(context: Context): AppLanguage =
        if (Build.VERSION.SDK_INT >= 33) AppLanguage.of(localeManager(context).applicationLocales.first()?.language)
        else AppLanguage.of(DeviceStore(context).appLanguage)

    /** Changes the app's language; the activity comes back in it, on Android 13 and later by itself. */
    fun choose(activity: Activity, language: AppLanguage) {
        if (language == choice(activity)) return
        store(activity, language)
        if (Build.VERSION.SDK_INT < 33) activity.recreate()
    }

    /** Keeps [language] for the next activity without recreating one: a test sets the app's language before it starts. */
    fun store(context: Context, language: AppLanguage) {
        if (Build.VERSION.SDK_INT >= 33) localeManager(context).applicationLocales =
            if (language == AppLanguage.SYSTEM) LocaleList.getEmptyLocaleList() else LocaleList.forLanguageTags(language.tag)
        else DeviceStore(context).appLanguage = language.tag
    }

    /** [base] in the chosen language on Android 12 and older, for MainActivity.attachBaseContext. */
    fun wrap(base: Context): Context {
        if (Build.VERSION.SDK_INT >= 33) return base
        val language = AppLanguage.of(DeviceStore(base).appLanguage).takeIf { it != AppLanguage.SYSTEM } ?: return base
        val configuration = Configuration(base.resources.configuration).apply { setLocales(LocaleList.forLanguageTags(language.tag)) }
        return base.createConfigurationContext(configuration)
    }

    /** Reads the language the activity's texts resolved to and the TV's clock; MainActivity calls it as it is created. */
    fun update(activity: Activity) {
        locale = shownLocale(activity.resources.configuration.locales.first(), DateFormat.is24HourFormat(activity))
        val device = if (Build.VERSION.SDK_INT >= 33) localeManager(activity).systemLocales else Resources.getSystem().configuration.locales
        guideLanguages = guideLanguages(locale, device.first())
    }

    /**
     * The locale of the texts on screen when [first] leads the activity's locales (Android puts there the TV language
     * that best matches the app's, counting the default texts as English): Turkish for Turkish, English for any other
     * language. Only language and region stay, so a TV's own digits (-u-nu-) never reach the app's numbers; the hour
     * cycle (-u-hc-) carries the TV's 12- or 24-hour clock to the guide's times.
     */
    internal fun shownLocale(first: Locale?, hour24: Boolean): Locale {
        val language = first?.language?.takeIf { it in spoken } ?: AppLanguage.ENGLISH.tag
        return Locale.Builder().setLanguage(language).setRegion(if (first?.language == language) first.country else "")
            .setUnicodeLocaleKeyword("hc", if (hour24) "h23" else "h12").build()
    }

    /** XMLTV titles in the app's language first, then in the TV's own (XmltvParser). */
    internal fun guideLanguages(app: Locale, device: Locale?): List<String> =
        listOfNotNull(app.language, device?.language?.takeIf { it.isNotEmpty() }).distinct()

    private fun LocaleList.first(): Locale? = if (isEmpty) null else get(0)
    @RequiresApi(33) private fun localeManager(context: Context) = context.getSystemService(LocaleManager::class.java)
}
