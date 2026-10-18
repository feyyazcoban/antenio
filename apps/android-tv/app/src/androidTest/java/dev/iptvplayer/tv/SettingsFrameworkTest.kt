package dev.iptvplayer.tv

import dev.iptvplayer.tv.ui.settings.SectionId

import android.graphics.Bitmap
import android.view.KeyEvent
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
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
 * The Settings page stack (S04) and its two levels (S14): the main page lists the sections, each a page of its own;
 * Back leaves one page and focus returns to the row that opened it, at the device's font scale (run at 1.0, 1.5 and
 * 2.0). Isolated synthetic catalog; never run state-clearing tests on a user's television.
 */
@RunWith(AndroidJUnit4::class)
class SettingsFrameworkTest {
    @get:Rule(order = 0) val turkish = TurkishLocaleRule()
    @get:Rule(order = 1) val ui = createAndroidComposeRule<MainActivity>()
    private lateinit var model: TvViewModel
    private lateinit var server: MockWebServer
    @Volatile private var manyCategories = false
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()

    @Before fun setup() {
        server = MockWebServer()
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val body = when (request.requestUrl!!.queryParameter("action")) {
                    null -> """{"user_info":{"auth":1,"status":"Active"}}"""
                    "get_live_categories", "get_series_categories" -> """[{"category_id":"1","category_name":"Genel"}]"""
                    "get_vod_categories" -> if (manyCategories) org.json.JSONArray().apply {
                        for (i in 1..40) put(org.json.JSONObject().put("category_id", "$i").put("category_name", "Kategori %02d".format(i)))
                    }.toString() else """[{"category_id":"1","category_name":"Genel"},{"category_id":"2","category_name":"Koleksiyon"}]"""
                    "get_live_streams" -> """[{"stream_id":1,"name":"Deneme kanalı","category_id":"1"}]"""
                    "get_vod_streams" -> if (manyCategories) org.json.JSONArray().apply {
                        for (i in 1..40) put(org.json.JSONObject().put("stream_id", i).put("name", "Film $i").put("category_id", "$i").put("container_extension", "mp4"))
                    }.toString() else """[{"stream_id":2,"name":"Deneme filmi","category_id":"1","container_extension":"mp4"},{"stream_id":3,"name":"Koleksiyon filmi","category_id":"2","container_extension":"mp4"}]"""
                    "get_series" -> """[{"series_id":4,"name":"Deneme dizisi","category_id":"1"}]"""
                    else -> "[]"
                }
                return MockResponse().addHeader("Content-Type", "application/json").setBody(body)
            }
        }
        server.start()
        model = resetFixtureSources(ui)
        ui.waitUntil(10_000) { model.state.value.route == Route.SETUP }
        // MockWebServer.url looks the host name up, which the main thread may not do.
        val address = server.url("/").toString()
        ui.runOnUiThread {
            model.draft(SourceDraft("Deneme kütüphanesi", SourceType.XTREAM, address, "synthetic", "synthetic"))
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
        ui.waitUntil(5_000) { ui.onAllNodes(hasText("Genel ve dil") and isFocused()).fetchSemanticsNodes().isNotEmpty() }
    }

    /** The section [label] from the main page, as OK on its row opens it. */
    private fun openSection(label: String) {
        row(label).performSemanticsAction(SemanticsActions.RequestFocus)
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        title(label)
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

    @Test fun theMainPageListsTheSectionsAndEveryRowIsInOne() {
        openSettings()
        title("Ayarlar")
        ui.onNodeWithTag("settings-title").assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
        // Each section with the rows it took from the one list before (plan decision 4a-8).
        val sections = listOf(
            "Genel ve dil" to listOf("Hareketi azalt", "Dil / Language", "Açılış sesi"),
            "Kaynaklar" to listOf("Kaynakları yönet", "Kataloğu yenile", "Kaynağı düzenle", "Kaynağı kaldır"),
            "İçerik ve ebeveyn denetimi" to listOf("Yetişkin kategorilerini gizle", "Kategorileri yönet", "Ebeveyn denetimi"),
            "Oynatıcı" to listOf("Otomatik sonraki bölüm", "Altyazı görünümü", "Kontrollerin kapanma süresi", "Oynatıcı panelleri",
                "Kare hızı eşleştirme", "Arabellek", "Kod çözücü", "Ses çıkışı", "Tünelli oynatma (deneysel)"),
            "Kütüphane" to listOf("İzleme geçmişini temizle"))
        SectionId.entries.forEachIndexed { index, section ->
            val label = ui.activity.getString(section.label)
            if (index > 0) key(KeyEvent.KEYCODE_DPAD_DOWN)
            row(label).assertIsFocused()
            assertFocusedRowReadable()
        }
        shot("sections-$scale")
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        title("Uygulama hakkında")
        key(KeyEvent.KEYCODE_BACK)
        row("Hakkında").assertIsFocused()
        sections.forEach { (section, rows) ->
            openSection(section)
            // The section starts on its first row and walks its rows in order with the remote.
            rows.forEachIndexed { index, label ->
                if (index > 0) key(KeyEvent.KEYCODE_DPAD_DOWN)
                row(label).assertIsFocused()
                assertFocusedRowReadable()
            }
            key(KeyEvent.KEYCODE_BACK)
            title("Ayarlar")
            row(section).assertIsFocused()
        }
        key(KeyEvent.KEYCODE_BACK)
        ui.onAllNodesWithTag("settings-panel").assertCountEquals(0)
    }

    @Test fun categoryScrollbarFollowsTheListAndSupportsDragging() {
        manyCategories = true
        ui.runOnUiThread { model.refresh() }
        ui.waitUntil(20_000) { !model.state.value.refreshing && model.state.value.categoryChoices[MediaKind.MOVIE]?.size == 40 }
        openSettings(); openSection("İçerik ve ebeveyn denetimi")
        row("Kategorileri yönet").performSemanticsAction(SemanticsActions.RequestFocus)
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.onNodeWithTag("category-kind:MOVIE").performSemanticsAction(SemanticsActions.RequestFocus)
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        fun progress() = ui.onNodeWithTag("settings-scrollbar").fetchSemanticsNode().config[SemanticsProperties.ProgressBarRangeInfo].current
        assertEquals(0f, progress(), .01f)
        repeat(18) { key(KeyEvent.KEYCODE_DPAD_DOWN) }
        assertTrue("Thumb follows remote list scrolling", progress() > .2f && progress() < .8f)
        val focused = ui.onNode(isFocused() and hasAnyAncestor(hasTestTag("settings-panel"))).fetchSemanticsNode().config[SemanticsProperties.TestTag]
        val previous = progress()
        key(KeyEvent.KEYCODE_DPAD_CENTER); key(KeyEvent.KEYCODE_BACK)
        ui.onNodeWithTag(focused).assertIsFocused()
        assertEquals("Back keeps list position", previous, progress(), .02f)
        shot("category-scroll-middle")
        ui.onNodeWithTag("settings-scrollbar").performSemanticsAction(SemanticsActions.SetProgress) { it(0f) }
        ui.waitForIdle()
        ui.onNodeWithTag("settings-scrollbar").performTouchInput { swipe(center.copy(y = 8f), center.copy(y = height - 1f), 500) }
        ui.waitForIdle()
        assertTrue("Dragging the thumb reaches the lower list", progress() > .85f)
        ui.onNodeWithTag("settings-scrollbar").performSemanticsAction(SemanticsActions.SetProgress) { it(1f) }
        ui.waitForIdle()
        assertEquals(1f, progress(), .01f)
        ui.onNodeWithTag("settings-scroll-more").assertTextEquals("")
        ui.onNodeWithTag("settings-scroll-up").assertTextEquals(ui.activity.getString(R.string.settings_scroll_up))
        shot("category-scroll-bottom")
    }

    @Test fun backLeavesOnePageAndFocusReturnsToTheRowThatOpenedIt() {
        openSettings()
        openSection("İçerik ve ebeveyn denetimi")
        row("Kategorileri yönet").performSemanticsAction(SemanticsActions.RequestFocus)
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
        title("İçerik ve ebeveyn denetimi")
        row("Kategorileri yönet").assertIsFocused()
        assertFocusedRowReadable()
        shot("categories-return-$scale")
        // Opened again in the same visit, Kategoriler starts on the section whose list was open.
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.onNodeWithTag("category-kind:MOVIE").assertIsFocused()
        key(KeyEvent.KEYCODE_BACK)
        key(KeyEvent.KEYCODE_BACK)
        title("Ayarlar")
        row("İçerik ve ebeveyn denetimi").assertIsFocused()
        // The section, opened again, starts on the row it was left on.
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        row("Kategorileri yönet").assertIsFocused()
        key(KeyEvent.KEYCODE_BACK)
        key(KeyEvent.KEYCODE_BACK)
        ui.onAllNodesWithTag("settings-panel").assertCountEquals(0)
        ui.onNodeWithTag("nav:Ayarlar").assertIsFocused()
    }

    @Test fun cancellingTheHistoryQuestionReturnsToItsRowAndKeepsHistory() {
        val history = model.state.value.history
        openSettings()
        openSection("Kütüphane")
        row("İzleme geçmişini temizle").assertIsFocused()
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        title("Geçmişi temizle?")
        row("Vazgeç").assertIsFocused()
        shot("history-question-$scale")
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        title("Kütüphane")
        row("İzleme geçmişini temizle").assertIsFocused()
        assertFocusedRowReadable()
        assertEquals(history, model.state.value.history)
        key(KeyEvent.KEYCODE_BACK)
        row("Kütüphane").assertIsFocused()
        key(KeyEvent.KEYCODE_BACK)
        ui.onAllNodesWithTag("settings-panel").assertCountEquals(0)
    }
}
