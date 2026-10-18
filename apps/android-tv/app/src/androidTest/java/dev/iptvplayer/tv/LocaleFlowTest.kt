package dev.iptvplayer.tv

import android.content.res.Resources
import android.view.KeyEvent
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.iptvplayer.tv.data.*
import dev.iptvplayer.tv.ui.updatedTime
import okhttp3.mockwebserver.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

/**
 * The app's language (S13) on the English test emulator: English by default; Turkish, English and the TV's language
 * again from Settings → Dil / Language. A new language recreates the activity: the screen behind and Settings come back
 * in it, Settings on the language row, and dates follow. Isolated synthetic catalog; never run state-clearing tests on a
 * user's television.
 */
@RunWith(AndroidJUnit4::class)
class LocaleFlowTest {
    @get:Rule(order = 0) val language = AppLanguageRule(AppLanguage.SYSTEM)
    @get:Rule(order = 1) val ui = createAndroidComposeRule<MainActivity>()
    private lateinit var model: TvViewModel
    private lateinit var server: MockWebServer
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()

    @Before fun setup() {
        // The default texts are English; on a TV in Turkish the app would open in Turkish instead.
        Assume.assumeTrue(Resources.getSystem().configuration.locales[0].language == "en")
        server = MockWebServer()
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val body = when (request.requestUrl!!.queryParameter("action")) {
                    null -> """{"user_info":{"auth":1,"status":"Active"}}"""
                    "get_live_categories", "get_vod_categories", "get_series_categories" -> """[{"category_id":"1","category_name":"General"}]"""
                    "get_live_streams" -> """[{"stream_id":1,"name":"Sample channel","category_id":"1"}]"""
                    "get_vod_streams" -> """[{"stream_id":2,"name":"Sample film","category_id":"1","container_extension":"mp4"}]"""
                    "get_series" -> """[{"series_id":4,"name":"Sample series","category_id":"1"}]"""
                    else -> "[]"
                }
                return MockResponse().addHeader("Content-Type", "application/json").setBody(body)
            }
        }
        server.start()
        model = resetFixtureSources(ui)
        val address = server.url("/").toString()
        ui.runOnUiThread {
            model.draft(SourceDraft("Synthetic library", SourceType.XTREAM, address, "synthetic", "synthetic"))
            model.connectDraft()
        }
        ui.waitUntil(20_000) { model.state.value.route == Route.HOME }
    }

    @After fun cleanup() {
        if (::model.isInitialized) ui.runOnUiThread { model.removeSource() }
        if (::server.isInitialized) server.shutdown()
    }

    private fun key(key: Int) { instrumentation.sendKeyDownUpSync(key); ui.waitForIdle() }
    private fun row(label: String) = ui.onNode(hasText(label) and hasClickAction() and hasAnyAncestor(hasTestTag("settings-panel")))
    private fun waitForFocus(label: String) = ui.waitUntil(10_000) {
        ui.onAllNodes(hasText(label) and hasClickAction() and isFocused()).fetchSemanticsNodes().isNotEmpty()
    }

    private fun openSettings(nav: String, first: String) {
        ui.onNodeWithTag("nav:$nav").performSemanticsAction(SemanticsActions.RequestFocus)
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        // A platform Dialog acquires window focus independently of Compose's idle clock.
        waitForFocus(first)
    }

    /** Picks [option] in Dil / Language from its row; Settings comes back on the row in the language [tag]. */
    private fun choose(option: AppLanguage, tag: String) {
        row("Dil / Language").assertIsFocused()
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.onNodeWithTag("settings-title").assertTextEquals("Dil / Language")
        listOf("Sistem dili / System language", "Türkçe", "English").forEachIndexed { index, label ->
            ui.onNodeWithTag("settings-option:$index").assertTextContains(label)
        }
        // The options follow AppLanguage's order, and the chosen one has focus first.
        val from = LocaleController.choice(ui.activity).ordinal
        ui.onNodeWithTag("settings-option:$from").assertIsFocused()
        repeat(Math.abs(option.ordinal - from)) { key(if (option.ordinal > from) KeyEvent.KEYCODE_DPAD_DOWN else KeyEvent.KEYCODE_DPAD_UP) }
        ui.onNodeWithTag("settings-option:${option.ordinal}").assertIsFocused()
        val before = ui.activity
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        // Android recreates the activity in the new language; a language already on screen may not need it.
        runCatching { ui.waitUntil(5_000) { runCatching { ui.activity !== before }.getOrDefault(false) } }
        ui.waitUntil(15_000) {
            runCatching { LocaleController.choice(ui.activity) == option && LocaleController.locale.language == tag }.getOrDefault(false)
        }
        waitForFocus("Dil / Language")
        assertEquals(Route.HOME, model.state.value.route)
    }

    @Test fun englishByDefaultThenTurkishAndBackFromSettingsWithTheScreenBehind() {
        assertEquals(AppLanguage.SYSTEM, LocaleController.choice(ui.activity))
        assertEquals("en", LocaleController.locale.language)
        ui.onNodeWithTag("nav:Home").assertExists()
        ui.onNodeWithTag("nav:Live TV").assertExists()
        openSettings("Settings", "Reduce motion")
        ui.onNodeWithTag("settings-title").assertTextEquals("Settings")
        key(KeyEvent.KEYCODE_DPAD_DOWN)
        row("Dil / Language").assertIsFocused().assert(hasText("Sistem dili / System language"))
        ui.onNodeWithTag("settings-section").assertTextEquals("Appearance and sound")
        val updated = model.state.value.catalogUpdatedAt
        ui.onNodeWithText("Last updated: ${updatedTime(updated)}").assertExists()

        choose(AppLanguage.TURKISH, "tr")
        assertEquals(AppLanguage.TURKISH, LocaleController.choice(ui.activity))
        ui.onNodeWithTag("settings-title").assertTextEquals("Ayarlar")
        row("Dil / Language").assertIsFocused().assert(hasText("Türkçe"))
        ui.onNodeWithTag("settings-section").assertTextEquals("Görünüm ve ses")
        // Dates follow the app's language, not the TV's.
        ui.onNodeWithText("Son güncelleme: ${updatedTime(updated)}").assertExists()
        assertTrue(updatedTime(updated), updatedTime(updated).contains(Regex("Eki|Kas|Ara|Oca|Şub|Mar|Nis|May|Haz|Tem|Ağu|Eyl")))
        // The screen behind came back in Turkish with the same state.
        key(KeyEvent.KEYCODE_BACK)
        ui.waitUntil(5_000) { ui.onAllNodesWithTag("settings-panel").fetchSemanticsNodes().isEmpty() }
        ui.onNodeWithTag("nav:Ana Sayfa").assertExists()
        ui.onNodeWithTag("nav:Canlı TV").assertExists()
        assertEquals(1, model.state.value.sources.size)

        openSettings("Ayarlar", "Hareketi azalt")
        key(KeyEvent.KEYCODE_DPAD_DOWN)
        choose(AppLanguage.ENGLISH, "en")
        assertEquals(AppLanguage.ENGLISH, LocaleController.choice(ui.activity))
        ui.onNodeWithTag("settings-title").assertTextEquals("Settings")
        row("Dil / Language").assertIsFocused().assert(hasText("English"))

        choose(AppLanguage.SYSTEM, "en")
        assertEquals(AppLanguage.SYSTEM, LocaleController.choice(ui.activity))
        ui.waitUntil(5_000) { ui.onAllNodes(hasText("Sistem dili / System language") and hasClickAction()).fetchSemanticsNodes().isNotEmpty() }
        ui.onNodeWithTag("settings-title").assertTextEquals("Settings")
        key(KeyEvent.KEYCODE_BACK)
        ui.waitUntil(5_000) { ui.onAllNodesWithTag("settings-panel").fetchSemanticsNodes().isEmpty() }
        ui.onNodeWithTag("nav:Home").assertExists()
    }
}
