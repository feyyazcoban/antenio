package dev.iptvplayer.tv

import android.graphics.Bitmap
import android.view.KeyEvent
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.iptvplayer.tv.data.*
import okhttp3.mockwebserver.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

/**
 * The Settings page stack (S04): Back leaves one page and focus returns to the row that opened it, at the device's
 * font scale (run at 1.0, 1.5 and 2.0). Isolated synthetic catalog; never run state-clearing tests on a user's television.
 */
@RunWith(AndroidJUnit4::class)
class SettingsFrameworkTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()
    private lateinit var model: TvViewModel
    private lateinit var server: MockWebServer
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()

    @Before fun setup() {
        server = MockWebServer()
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val body = when (request.requestUrl!!.queryParameter("action")) {
                    null -> """{"user_info":{"auth":1,"status":"Active"}}"""
                    "get_live_categories", "get_series_categories" -> """[{"category_id":"1","category_name":"Genel"}]"""
                    "get_vod_categories" -> """[{"category_id":"1","category_name":"Genel"},{"category_id":"2","category_name":"Koleksiyon"}]"""
                    "get_live_streams" -> """[{"stream_id":1,"name":"Deneme kanalı","category_id":"1"}]"""
                    "get_vod_streams" -> """[{"stream_id":2,"name":"Deneme filmi","category_id":"1","container_extension":"mp4"},{"stream_id":3,"name":"Koleksiyon filmi","category_id":"2","container_extension":"mp4"}]"""
                    "get_series" -> """[{"series_id":4,"name":"Deneme dizisi","category_id":"1"}]"""
                    else -> "[]"
                }
                return MockResponse().addHeader("Content-Type", "application/json").setBody(body)
            }
        }
        server.start()
        model = resetFixtureSources(ui)
        ui.waitUntil(10_000) { model.state.value.route == Route.SETUP }
        ui.runOnUiThread {
            model.draft(SourceDraft("Deneme kütüphanesi", SourceType.XTREAM, server.url("/").toString(), "synthetic", "synthetic"))
            model.connectDraft()
        }
        ui.waitUntil(20_000) { model.state.value.route == Route.HOME }
    }

    @After fun cleanup() {
        if (::model.isInitialized) ui.runOnUiThread { model.removeSource() }
        if (::server.isInitialized) server.shutdown()
    }

    private fun key(key: Int) { instrumentation.sendKeyDownUpSync(key); ui.waitForIdle() }
    private fun row(label: String) = ui.onNode(hasText(label) and hasClickAction())
    private fun title(text: String) = ui.onNodeWithTag("settings-title").assertTextEquals(text)
    private val scale get() = ui.activity.resources.configuration.fontScale.toString().replace('.', '_')

    private fun openSettings() {
        ui.onNodeWithTag("nav:Ayarlar").performSemanticsAction(SemanticsActions.RequestFocus)
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        // A platform Dialog acquires window focus independently of Compose's idle clock.
        ui.waitUntil(5_000) { ui.onAllNodes(hasText("Hareketi azalt") and isFocused()).fetchSemanticsNodes().isNotEmpty() }
    }

    /** The focused row lies wholly inside the panel's viewport, whatever the font scale. */
    private fun assertFocusedRowReadable() {
        val focused = ui.onNode(isFocused() and hasAnyAncestor(hasTestTag("settings-panel"))).getUnclippedBoundsInRoot()
        val viewport = ui.onNodeWithTag("settings-viewport").getUnclippedBoundsInRoot()
        assertTrue("Focused setting must stay readable: $focused / $viewport", focused.top >= viewport.top && focused.bottom <= viewport.bottom)
    }

    private fun shot(name: String) {
        ui.waitForIdle(); Thread.sleep(1200); ui.waitForIdle()
        instrumentation.uiAutomation.takeScreenshot().let { image ->
            val directory = ui.activity.filesDir.resolve("settings-preview").apply { mkdirs() }
            directory.resolve("$name.png").outputStream().use { image.compress(Bitmap.CompressFormat.PNG, 100, it) }
            image.recycle()
        }
    }

    @Test fun backLeavesOnePageAndFocusReturnsToTheRowThatOpenedIt() {
        openSettings()
        row("Kategorileri yönet").performSemanticsAction(SemanticsActions.RequestFocus)
        ui.onNodeWithTag("settings-section").assertTextEquals("İçerik")
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        title("Kategoriler")
        ui.onNodeWithTag("category-kind:LIVE").assertIsFocused()
        key(KeyEvent.KEYCODE_DPAD_DOWN)
        ui.onNodeWithTag("category-kind:MOVIE").assertIsFocused()
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        title("Filmler")
        ui.onNodeWithTag("category-choice:0").assertIsFocused()
        assertFocusedRowReadable()
        shot("categories-movies-$scale")
        key(KeyEvent.KEYCODE_BACK)
        title("Kategoriler")
        ui.onNodeWithTag("category-kind:MOVIE").assertIsFocused()
        assertFocusedRowReadable()
        key(KeyEvent.KEYCODE_BACK)
        title("Ayarlar")
        row("Kategorileri yönet").assertIsFocused()
        ui.onNodeWithTag("settings-section").assertTextEquals("İçerik")
        assertFocusedRowReadable()
        shot("categories-return-$scale")
        // Opened again in the same visit, Kategoriler starts on the section whose list was open.
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.onNodeWithTag("category-kind:MOVIE").assertIsFocused()
        key(KeyEvent.KEYCODE_BACK)
        key(KeyEvent.KEYCODE_BACK)
        ui.onAllNodesWithTag("settings-panel").assertCountEquals(0)
        ui.onNodeWithTag("nav:Ayarlar").assertIsFocused()
    }

    @Test fun cancellingTheHistoryQuestionReturnsToItsRowAndKeepsHistory() {
        val history = model.state.value.history
        openSettings()
        row("İzleme geçmişini temizle").performSemanticsAction(SemanticsActions.RequestFocus)
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        title("Geçmişi temizle?")
        row("Vazgeç").assertIsFocused()
        shot("history-question-$scale")
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        title("Ayarlar")
        row("İzleme geçmişini temizle").assertIsFocused()
        assertFocusedRowReadable()
        assertEquals(history, model.state.value.history)
        key(KeyEvent.KEYCODE_BACK)
        ui.onAllNodesWithTag("settings-panel").assertCountEquals(0)
    }
}
