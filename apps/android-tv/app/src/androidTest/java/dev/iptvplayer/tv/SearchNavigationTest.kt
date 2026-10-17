package dev.iptvplayer.tv

import android.graphics.Bitmap
import android.view.KeyEvent
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.iptvplayer.tv.data.*
import okhttp3.mockwebserver.*
import org.json.JSONArray
import org.json.JSONObject
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

/** Isolated synthetic catalog. Never run state-clearing tests on a user's television. */
@RunWith(AndroidJUnit4::class)
class SearchNavigationTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()
    private lateinit var model: TvViewModel
    private lateinit var server: MockWebServer
    @Volatile private var removedId: String? = null
    @Volatile private var removeCategory = false
    @Volatile private var manyCategories = false
    @Volatile private var slowAuth = false
    @Volatile private var failAuth = false
    @Volatile private var emptyMovies = false
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()

    @Before fun setup() {
        Assume.assumeTrue(android.os.Build.HARDWARE.contains("ranchu") || android.os.Build.FINGERPRINT.contains("generic"))
        server = MockWebServer()
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                if (failAuth) return MockResponse().setResponseCode(401).setBody("{}")
                val body = when (request.requestUrl!!.queryParameter("action")) {
                    null -> """{"user_info":{"auth":1,"status":"Active"}}"""
                    "get_live_categories", "get_series_categories" -> """[{"category_id":"1","category_name":"Genel"}]"""
                    "get_vod_categories" -> if (manyCategories) JSONArray().apply {
                        for (i in 1..50) put(JSONObject().put("category_id", "$i").put("category_name", "Kategori %02d".format(i)))
                    }.toString() else if (removeCategory) """[{"category_id":"1","category_name":"Genel"}]"""
                        else """[{"category_id":"1","category_name":"Genel"},{"category_id":"2","category_name":"Koleksiyon"}]"""
                    "get_live_streams" -> """[{"stream_id":1,"name":"İstanbul Işığı TV","category_id":"1"}]"""
                    "get_series" -> """[{"series_id":2,"name":"Işığı Arayanlar","category_id":"1"}]"""
                    "get_vod_streams" -> if (emptyMovies) "[]" else JSONArray().apply {
                        for (i in 1..80) if (removedId != "movie:$i") put(JSONObject().put("stream_id", i)
                            .put("name", if (i == 1) "İstanbul Işığı" else "Film $i · Uzak Şehirlerden Uzun Bir Hikâye")
                            .put("category_id", if (manyCategories) "${i % 50 + 1}" else if (!removeCategory && i > 70) "2" else "1").put("container_extension", "mp4"))
                        put(JSONObject().put("stream_id", 99).put("name", "Işığı Gizli").put("is_adult", "1").put("category_id", "1"))
                    }.toString()
                    "get_vod_info" -> """{"info":{"plot":"Sentetik uzun başlık ve kumanda testi","duration_secs":120},"movie_data":{"container_extension":"mp4"}}"""
                    else -> "[]"
                }
                return MockResponse().addHeader("Content-Type", "application/json").setBody(body).apply {
                    if (slowAuth && request.requestUrl!!.queryParameter("action") == null)
                        setBodyDelay(700, java.util.concurrent.TimeUnit.MILLISECONDS)
                }
            }
        }
        server.start()
        model = resetFixtureSources(ui)
        ui.waitUntil(10_000) { model.state.value.route == Route.SETUP }
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
    private fun click(text: String) = ui.onNode((hasText(text) or hasContentDescription(text)) and hasClickAction()).performSemanticsAction(SemanticsActions.OnClick) { it() }
    private fun key(key: Int) { instrumentation.sendKeyDownUpSync(key); ui.waitForIdle() }
    /** assertIsFocused that also names what has focus instead, across the app's windows. */
    private fun SemanticsNodeInteraction.assertFocusedOrSay(): SemanticsNodeInteraction = try { assertIsFocused() } catch (failure: AssertionError) {
        val now = ui.onAllNodes(isFocused()).fetchSemanticsNodes().map { node -> node.config.getOrNull(SemanticsProperties.Text)?.joinToString()
            ?: node.config.getOrNull(SemanticsProperties.ContentDescription)?.joinToString() ?: node.config.getOrNull(SemanticsProperties.TestTag) ?: "?" }
        throw AssertionError("Focused instead: $now (route ${model.state.value.route})", failure)
    }
    private fun searchFinished() = ui.waitUntil(10_000) { !model.state.value.searching }
    private fun imeShown() = android.os.ParcelFileDescriptor.AutoCloseInputStream(
        instrumentation.uiAutomation.executeShellCommand("dumpsys input_method")
    ).bufferedReader().use { it.readText().contains("mInputShown=true") }
    /** Fields open the keyboard only on OK; close it when a step opened it, without spending a Back otherwise. */
    private fun hideKeyboard() {
        // The TV IME is a floating window: insets can report hidden while it is visible, so ask the system.
        ui.waitForIdle()
        if (imeShown()) { key(KeyEvent.KEYCODE_BACK); ui.waitUntil(5_000) { !imeShown() } }
    }
    private fun shot(name: String) {
        ui.waitForIdle(); Thread.sleep(1200); ui.waitForIdle()
        instrumentation.uiAutomation.takeScreenshot().let { image ->
            val directory = ui.activity.filesDir.resolve("navigation-preview").apply { mkdirs() }
            directory.resolve("$name.png").outputStream().use { image.compress(Bitmap.CompressFormat.PNG, 100, it) }
            image.recycle()
        }
    }

    @Test fun genuinelyEmptyMovieLibraryOpensSourcesAndReturns() {
        emptyMovies = true
        ui.runOnUiThread { model.refresh() }
        ui.waitUntil(15000) { !model.state.value.refreshing && model.state.value.catalog.items.none { it.kind == MediaKind.MOVIE } }
        ui.runOnUiThread { model.showKind(MediaKind.MOVIE) }; ui.waitForIdle()
        ui.onNodeWithText("Bu bölümde içerik yok").assertExists()
        // Right from the category column goes to the empty list's action.
        ui.onNodeWithTag("category:${CategoryFilter.ALL}").performSemanticsAction(SemanticsActions.RequestFocus)
        key(KeyEvent.KEYCODE_DPAD_RIGHT)
        ui.onNodeWithTag("library-empty-action").assertIsFocused()
        val panel = ui.onNodeWithTag("library-empty").getUnclippedBoundsInRoot()
        val viewport = ui.onNodeWithTag("library-grid").getUnclippedBoundsInRoot()
        assertTrue(panel.top >= viewport.top && panel.bottom <= viewport.bottom)
        shot("empty-movies")
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.waitUntil(5000) { model.state.value.route == Route.SOURCES }
        key(KeyEvent.KEYCODE_BACK)
        ui.waitUntil(5000) { model.state.value.route == Route.LIBRARY }
        ui.onNodeWithText("Bu bölümde içerik yok").assertExists()
        assertTrue(model.state.value.catalog.items.any { it.kind == MediaKind.LIVE })
    }

    @Test fun emptyLibraryExplainsFiltersAndFavoritesAndOffersRecovery() {
        ui.runOnUiThread { model.showKind(MediaKind.MOVIE) }; ui.waitForIdle()
        ui.runOnUiThread { model.query("bulunmayan-deneme-987654") }
        ui.waitUntil(10000) { !model.state.value.librarySearching }
        ui.onNodeWithText("Bu filtrelerle içerik bulunamadı").assertExists()
        // Right from the category column goes to the empty list's action.
        ui.onNodeWithTag("category:${CategoryFilter.ALL}").performSemanticsAction(SemanticsActions.RequestFocus)
        key(KeyEvent.KEYCODE_DPAD_RIGHT)
        ui.onNodeWithTag("library-empty-action").assertIsFocused()
        fun checkPanel() {
            val panel = ui.onNodeWithTag("library-empty").getUnclippedBoundsInRoot()
            val viewport = ui.onNodeWithTag("library-grid").getUnclippedBoundsInRoot()
            assertTrue("Empty panel must fit while its action is focused", panel.top >= viewport.top && panel.bottom <= viewport.bottom)
        }
        checkPanel(); shot("empty-library-filter")
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.waitUntil(10000) { !model.state.value.librarySearching && model.state.value.query.isEmpty() }
        ui.onNodeWithTag("library-empty-action").assertDoesNotExist()
        assertEquals(CategoryFilter.ALL, model.state.value.category)
        ui.runOnUiThread { model.favorites() }; ui.waitForIdle()
        ui.onNodeWithText("Favorilerin burada görünecek").assertExists()
        // Right from the category column goes to the empty list's action.
        ui.onNodeWithTag("category:${CategoryFilter.ALL}").performSemanticsAction(SemanticsActions.RequestFocus)
        key(KeyEvent.KEYCODE_DPAD_RIGHT)
        ui.onNodeWithTag("library-empty-action").assertIsFocused()
        checkPanel(); shot("empty-favorites")
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.waitUntil(5000) { model.state.value.route == Route.HOME }
    }

    @Test fun searchInACategoryOffersTheWholeSection() {
        ui.runOnUiThread { model.showKind(MediaKind.MOVIE) }; ui.waitForIdle()
        ui.runOnUiThread { model.category("Koleksiyon") }; ui.waitForIdle()
        click("Bu kategoride ara")
        ui.runOnUiThread { model.query("ISIGI") }
        ui.waitUntil(10_000) { !model.state.value.librarySearching }
        ui.onNodeWithText("Bu kategoride bulunamadı").assertExists()
        ui.onNodeWithTag("category:Koleksiyon").performSemanticsAction(SemanticsActions.RequestFocus)
        key(KeyEvent.KEYCODE_DPAD_RIGHT)
        ui.onNodeWithTag("library-empty-action").assertIsFocused().assertTextContains("Tüm filmlerde ara")
        // OK keeps the search and widens it to every film; focus lands on the result.
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.waitUntil(10_000) { model.state.value.category == CategoryFilter.ALL && !model.state.value.librarySearching }
        assertEquals("ISIGI", model.state.value.query)
        ui.waitUntil(5_000) { ui.onAllNodes(hasContentDescription("İstanbul Işığı") and isFocused()).fetchSemanticsNodes().isNotEmpty() }
        ui.onNodeWithText("Bu bölümde ara").assertExists()
    }

    @Test fun sideMenuLabelsFitAndRemoteCanReachSettingsAndReturn() {
        val order = listOf("Ara", "Ana Sayfa", "Canlı TV", "Filmler", "Diziler", "Favoriler", "Ayarlar")
        val density = ui.activity.resources.displayMetrics.density
        fun railWidth() = ui.onNodeWithTag("navigation-bar").getUnclippedBoundsInRoot().let { (it.right - it.left).value }
        // At rest the menu is an icon rail: every item is reachable by name and no label is drawn.
        assertEquals(72f, railWidth(), 1f)
        order.forEach { label ->
            ui.onNode(hasTestTag("nav:$label") and hasContentDescription(label)).assertExists()
            ui.onAllNodesWithTag("nav-label:$label", useUnmergedTree = true).assertCountEquals(0)
        }
        shot("navigation-content-focused")
        val contentItem = ui.onNode(isFocused()).fetchSemanticsNode().id
        // Left from the first content column opens the menu on the current section, over the content.
        key(KeyEvent.KEYCODE_DPAD_LEFT)
        ui.onNodeWithTag("nav:Ana Sayfa").assertIsFocused()
        ui.waitUntil(2_000) { railWidth() >= 207f }
        // 208 dp, or wider when a label needs it at large text (D029).
        assertTrue("Open menu width: ${railWidth()}", railWidth() >= 207f)
        key(KeyEvent.KEYCODE_DPAD_UP)
        val metrics = JSONArray()
        order.forEachIndexed { index, label ->
            if (index > 0) key(KeyEvent.KEYCODE_DPAD_DOWN)
            val button = ui.onNodeWithTag("nav:$label").assertIsFocused().getUnclippedBoundsInRoot()
            val node = ui.onNodeWithTag("nav-label:$label", useUnmergedTree = true)
            val bounds = node.getUnclippedBoundsInRoot()
            val layout = mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
            node.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layout) }
            val right = bounds.left.value + label.indices.maxOf { layout.single().getBoundingBox(it).right } / density
            metrics.put(JSONObject().put("label", label).put("buttonRight", button.right.value).put("paintRight", right)
                .put("overflow", layout.single().hasVisualOverflow))
            assertFalse("Menu label is clipped: $label", layout.single().hasVisualOverflow)
            assertTrue("Menu label keeps 10dp inside its pill: $label $right / $button", right <= button.right.value - 10)
            val rail = ui.onNodeWithTag("navigation-bar").getUnclippedBoundsInRoot()
            assertTrue("Menu item fits the screen height: $label", button.top >= rail.top && button.bottom <= rail.bottom)
        }
        ui.activity.filesDir.resolve("nav-open.json").writeText(metrics.toString(2))
        key(KeyEvent.KEYCODE_DPAD_DOWN)
        ui.onNodeWithTag("nav:Ayarlar").assertIsFocused()
        // The open menu covers the content instead of pushing it, so the hero header (the line that held the source
        // selector before D059) keeps its full label. D060: no screen offers a source picker.
        ui.onAllNodesWithText("Tüm kaynaklar", useUnmergedTree = true).assertCountEquals(0)
        val heroLabel = mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
        ui.onNodeWithTag("hero-label", useUnmergedTree = true)
            .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(heroLabel) }
        assertFalse("The hero header label must fit at the current font scale", heroLabel.single().hasVisualOverflow)
        shot("navigation-large")
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        // A platform Dialog acquires window focus independently of Compose's idle clock.
        try {
            ui.waitUntil(2_000) {
                ui.onAllNodes(hasText("Hareketi azalt") and hasClickAction() and isFocused()).fetchSemanticsNodes().isNotEmpty()
            }
        } catch (failure: Throwable) {
            shot("settings-focus-failure")
            println(ui.onNodeWithTag("settings-panel").printToString())
            throw failure
        }
        ui.onNode(hasText("Hareketi azalt") and hasClickAction()).assertIsFocused()
        key(KeyEvent.KEYCODE_BACK)
        ui.onNodeWithTag("nav:Ayarlar").assertIsFocused()
        // The closed dialog's window keeps the system's input focus for a moment after Compose drops it; keys sent
        // before the app's window has it again never reach the app (InputDispatcher: "no input channel").
        ui.waitUntil(5_000) { ui.onAllNodesWithTag("settings-panel").fetchSemanticsNodes().isEmpty() && ui.activity.hasWindowFocus() }
        repeat(order.size) { key(KeyEvent.KEYCODE_DPAD_UP) }
        ui.onNodeWithTag("nav:Ara").assertFocusedOrSay()
        key(KeyEvent.KEYCODE_DPAD_UP)
        ui.onNodeWithTag("nav:Ara").assertIsFocused()
        key(KeyEvent.KEYCODE_DPAD_LEFT)
        ui.onNodeWithTag("nav:Ara").assertIsFocused()
        assertEquals(Route.HOME, model.state.value.route)
        // Right returns to the content item that had focus and the menu folds back to its rail.
        key(KeyEvent.KEYCODE_DPAD_RIGHT)
        assertEquals(contentItem, ui.onNode(isFocused()).fetchSemanticsNode().id)
        ui.waitUntil(2_000) { railWidth() < 73f }
    }

    @Test fun homeBackAsksBeforeLeavingTheApp() {
        assertEquals(Route.HOME, model.state.value.route)
        // An IME left open by an earlier test would take the first Back for itself.
        if (imeShown()) { key(KeyEvent.KEYCODE_BACK); ui.waitUntil(5_000) { !imeShown() } }
        fun asked() = try {
            ui.waitUntil(5_000) { ui.onAllNodes(hasTestTag("exit-cancel") and isFocused()).fetchSemanticsNodes().isNotEmpty() }
        } catch (failure: Throwable) {
            shot("exit-failure")
            throw failure
        }
        // The closed dialog's window keeps the system's input focus for about 100 ms after Compose drops it; keys
        // sent before Home's window has focus again never reach the app (InputDispatcher: "no input channel").
        val activity = ui.activity
        fun closed() = ui.waitUntil(5_000) { ui.onAllNodesWithTag("exit-dialog").fetchSemanticsNodes().isEmpty() && activity.hasWindowFocus() }
        // A viewer presses Back on a Home they can see: wait until Home has put focus on its content.
        ui.waitUntil(5_000) { ui.onAllNodes(isFocused() and hasAnyAncestor(hasTestTag("home-content"))).fetchSemanticsNodes().isNotEmpty() }
        // Back from the content first opens the side menu on Ana Sayfa; Back there asks.
        key(KeyEvent.KEYCODE_BACK)
        ui.onNodeWithTag("nav:Ana Sayfa").assertIsFocused()
        ui.onNodeWithTag("exit-dialog").assertDoesNotExist()
        key(KeyEvent.KEYCODE_BACK); asked()
        ui.onNodeWithText("Uygulamadan çıkmak istiyor musunuz?").assertIsDisplayed()
        ui.onNodeWithTag("exit-confirm").assertIsNotFocused()
        shot("exit-dialog")
        // Back answers the question with "stay", as does Vazgeç, which has focus first.
        key(KeyEvent.KEYCODE_BACK); closed()
        assertFalse(ui.activity.isFinishing)
        key(KeyEvent.KEYCODE_BACK); asked(); key(KeyEvent.KEYCODE_DPAD_CENTER); closed()
        assertFalse(ui.activity.isFinishing)
        assertEquals(Route.HOME, model.state.value.route)
        // A quick double Back while the question is still opening never leaves the app unasked.
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK); instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
        ui.waitForIdle(); Thread.sleep(500); ui.waitForIdle()
        assertFalse(ui.activity.isFinishing)
        // The second Back either reached the opening question (closing it) or was taken by Home again.
        if (ui.onAllNodesWithTag("exit-dialog").fetchSemanticsNodes().isEmpty()) key(KeyEvent.KEYCODE_BACK)
        asked()
        key(KeyEvent.KEYCODE_DPAD_RIGHT)
        ui.onNodeWithTag("exit-confirm").assertIsFocused()
        // Leaving Home while asked drops the question; returning does not ask again.
        ui.runOnUiThread { model.showKind(MediaKind.MOVIE) }; closed()
        ui.runOnUiThread { model.home() }; ui.waitForIdle()
        ui.onNodeWithTag("exit-dialog").assertDoesNotExist()
        assertFalse(ui.activity.isFinishing)
    }

    /** Back in the same main-thread turn as [change] arrives before the screen recomposes for the new route. */
    private fun backRightAfter(change: () -> Unit) = ui.runOnUiThread { change(); ui.activity.onBackPressedDispatcher.onBackPressed() }

    @Test fun backFollowsTheModelRouteBeforeTheScreenCatchesUp() {
        // Like a quick Back while a slow first Home frame is still being built.
        ui.runOnUiThread { model.showKind(MediaKind.MOVIE) }; ui.waitForIdle()
        // Library → Home: the Back belongs to Home and asks, instead of vanishing in the library's handler.
        backRightAfter { model.home() }
        ui.waitUntil(5_000) { ui.onAllNodes(hasTestTag("exit-cancel") and isFocused()).fetchSemanticsNodes().isNotEmpty() }
        key(KeyEvent.KEYCODE_BACK)
        ui.waitUntil(5_000) { ui.onAllNodesWithTag("exit-dialog").fetchSemanticsNodes().isEmpty() }
        // Home → Library: the Back belongs to the library and returns Home without asking.
        backRightAfter { model.showKind(MediaKind.MOVIE) }; ui.waitForIdle()
        assertEquals(Route.HOME, model.state.value.route)
        ui.onNodeWithTag("exit-dialog").assertDoesNotExist()
        assertFalse(ui.activity.isFinishing)
    }

    @Test fun searchBackFollowsTheModelRouteBeforeTheScreenLeaves() {
        // Search's own Back handler stays enabled until the next frame removes the screen. From its side menu a quick
        // double Back goes Home and asks there, instead of going Home a second time unseen.
        click("Ara"); hideKeyboard()
        key(KeyEvent.KEYCODE_BACK); ui.onNodeWithTag("nav:Ara").assertIsFocused()
        ui.runOnUiThread { ui.activity.onBackPressedDispatcher.run { onBackPressed(); onBackPressed() } }
        ui.waitUntil(5_000) { ui.onAllNodes(hasTestTag("exit-cancel") and isFocused()).fetchSemanticsNodes().isNotEmpty() }
        assertEquals(Route.HOME, model.state.value.route)
        val activity = ui.activity
        key(KeyEvent.KEYCODE_BACK)
        ui.waitUntil(5_000) { ui.onAllNodesWithTag("exit-dialog").fetchSemanticsNodes().isEmpty() && activity.hasWindowFocus() }
        // A result opened in the same turn as Back: the Back closes it as Detail's own Back does.
        click("Ara"); hideKeyboard()
        val movie = model.state.value.catalog.items.first { it.kind == MediaKind.MOVIE && !it.adult }
        backRightAfter { model.open(movie) }; ui.waitForIdle()
        assertEquals(Route.SEARCH, model.state.value.route)
        assertFalse(ui.activity.isFinishing)
    }

    @Test fun sideMenuAlwaysEntersOnTheCurrentSection() {
        click("Filmler")
        // A section opens on its category column; Right goes to the first title.
        ui.onNodeWithTag("category:${CategoryFilter.ALL}").assertIsFocused(); key(KeyEvent.KEYCODE_DPAD_RIGHT)
        ui.onNodeWithContentDescription("İstanbul Işığı").assertIsFocused()
        // Left: the category column on the selected category, then the side menu on the current section.
        key(KeyEvent.KEYCODE_DPAD_LEFT); ui.onNodeWithTag("category:${CategoryFilter.ALL}").assertIsFocused().assertIsSelected()
        key(KeyEvent.KEYCODE_DPAD_LEFT); ui.onNodeWithTag("nav:Filmler").assertIsFocused().assertIsSelected()
        key(KeyEvent.KEYCODE_DPAD_DOWN); ui.onNodeWithTag("nav:Diziler").assertIsFocused()
        assertEquals(MediaKind.MOVIE, model.state.value.kind)
        // Right: back to the selected category, then the card that had focus.
        key(KeyEvent.KEYCODE_DPAD_RIGHT); ui.onNodeWithTag("category:${CategoryFilter.ALL}").assertIsFocused()
        key(KeyEvent.KEYCODE_DPAD_LEFT); ui.onNodeWithTag("nav:Filmler").assertIsFocused()
        key(KeyEvent.KEYCODE_DPAD_RIGHT); key(KeyEvent.KEYCODE_DPAD_RIGHT)
        ui.onNodeWithContentDescription("İstanbul Işığı").assertIsFocused()
        ui.onNodeWithTag("nav-label:Filmler", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test fun fieldsOpenTheKeyboardOnlyOnOk() {
        fun staysClosed() { ui.waitForIdle(); Thread.sleep(800); assertFalse("The keyboard must not open on focus", imeShown()) }
        fun okOpensAndBackCloses() {
            key(KeyEvent.KEYCODE_DPAD_CENTER); ui.waitUntil(5_000) { imeShown() }
            key(KeyEvent.KEYCODE_BACK); ui.waitUntil(5_000) { !imeShown() }
        }
        // Ara puts focus on its field; the keyboard waits for OK, and Back closes it on the same field.
        click("Ara")
        val input = ui.onNode(hasSetTextAction())
        input.assertIsFocused(); staysClosed()
        okOpensAndBackCloses()
        input.assertIsFocused(); assertEquals(Route.SEARCH, model.state.value.route)
        // Coming back to the field by remote does not open it either.
        key(KeyEvent.KEYCODE_DPAD_DOWN); input.assertIsNotFocused()
        key(KeyEvent.KEYCODE_DPAD_UP); input.assertIsFocused(); staysClosed()
        // A computer keyboard's Esc is Back: it closes an open keyboard first, then acts as Back.
        key(KeyEvent.KEYCODE_DPAD_CENTER); ui.waitUntil(5_000) { imeShown() }
        key(KeyEvent.KEYCODE_ESCAPE); ui.waitUntil(5_000) { !imeShown() }
        input.assertIsFocused()
        key(KeyEvent.KEYCODE_ESCAPE)
        ui.onNodeWithTag("nav:Ara").assertIsFocused()
        assertEquals(Route.SEARCH, model.state.value.route)
        // "Bu bölümde ara" moves focus to the section field, also without the keyboard.
        ui.runOnUiThread { model.showKind(MediaKind.MOVIE) }; ui.waitForIdle()
        click("Bu bölümde ara")
        val section = ui.onNode(hasSetTextAction())
        ui.waitUntil(5_000) { section.fetchSemanticsNode().config.getOrNull(SemanticsProperties.Focused) == true }
        staysClosed()
        ui.onNodeWithTag("library-heading").assertExists()
        okOpensAndBackCloses()
        section.assertIsFocused()
        // The source form starts on its first field without the keyboard.
        ui.runOnUiThread { model.manageSources() }
        ui.waitUntil(5_000) { model.state.value.route == Route.SOURCES }
        ui.onNodeWithTag("add-source").performSemanticsAction(SemanticsActions.OnClick) { it() }
        ui.waitUntil(5_000) { model.state.value.route == Route.SETUP }
        val fields = ui.onAllNodes(hasSetTextAction())
        fields[0].performSemanticsAction(SemanticsActions.RequestFocus) { it() }
        fields[0].assertIsFocused(); staysClosed()
        // Moving between the form's fields by remote keeps it closed too.
        key(KeyEvent.KEYCODE_DPAD_DOWN)
        fields[1].assertIsFocused(); staysClosed()
        shot("setup-keyboard-closed")
    }

    @Test fun posterGridHeadingCountsTheFocusedPlace() {
        click("Filmler")
        // A section opens on its category column; Right goes to the first title.
        ui.onNodeWithTag("category:${CategoryFilter.ALL}").assertIsFocused(); key(KeyEvent.KEYCODE_DPAD_RIGHT)
        ui.onNodeWithContentDescription("İstanbul Işığı").assertIsFocused()
        val counter = ui.onNodeWithTag("library-position", useUnmergedTree = true)
        fun place() = counter.fetchSemanticsNode().config[SemanticsProperties.Text].joinToString("")
        // Provider order: the first film of 80; the adult entry is not counted.
        assertEquals("1 / 80", place())
        key(KeyEvent.KEYCODE_DPAD_RIGHT); assertEquals("2 / 80", place())
        // Five columns while the grid has focus (D064); Down moves one row.
        key(KeyEvent.KEYCODE_DPAD_DOWN); assertEquals("7 / 80", place())
        repeat(3) { key(KeyEvent.KEYCODE_DPAD_DOWN) }
        assertEquals("22 / 80", place())
        // The heading stays above the grid, so the counter is in view while the posters scroll.
        val heading = ui.onNodeWithTag("library-heading").getUnclippedBoundsInRoot()
        val grid = ui.onNodeWithTag("library-grid").getUnclippedBoundsInRoot()
        assertTrue("Heading must rest above the scrolled grid: $heading / $grid", heading.top.value >= 0f && heading.bottom <= grid.top)
        counter.assertIsDisplayed()
        shot("library-position")
        // Back in the category column the heading shows the section's size again.
        repeat(2) { key(KeyEvent.KEYCODE_DPAD_LEFT) }
        ui.onNodeWithTag("category:${CategoryFilter.ALL}").assertIsFocused()
        ui.onNodeWithTag("library-position", useUnmergedTree = true).assertDoesNotExist()
        ui.onNodeWithText("80 içerik").assertIsDisplayed()
    }

    @Test fun categoriesFollowTheProviderWithVirtualOnesFirst() {
        click("Filmler")
        // A section opens on its category column; Right goes to the first title.
        ui.onNodeWithTag("category:${CategoryFilter.ALL}").assertIsFocused(); key(KeyEvent.KEYCODE_DPAD_RIGHT)
        ui.onNodeWithContentDescription("İstanbul Işığı").assertIsFocused()
        // A focused poster grid takes the category column's place; Left brings it back on the selected category.
        ui.onNodeWithTag("library-categories").assertDoesNotExist()
        key(KeyEvent.KEYCODE_DPAD_LEFT)
        ui.onNodeWithTag("category:${CategoryFilter.ALL}").assertIsFocused()
        // On-screen order (the focused item can sit last in the semantics tree).
        val labels = ui.onNodeWithTag("library-categories").onChildren().fetchSemanticsNodes().sortedBy { it.boundsInRoot.top }
            .mapNotNull { it.config.getOrNull(SemanticsProperties.ContentDescription)?.singleOrNull() }
        assertEquals(listOf("Favoriler", "Geçmiş", "Tüm filmler", "Genel", "Koleksiyon"), labels)
        // Moving through the categories leaves the list as it is; OK on a category shows it.
        repeat(2) { key(KeyEvent.KEYCODE_DPAD_DOWN) }
        ui.onNodeWithTag("category:Koleksiyon").assertIsFocused()
        Thread.sleep(600); ui.waitForIdle()
        assertEquals(CategoryFilter.ALL, model.state.value.category)
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.waitUntil(3_000) { model.state.value.category == "Koleksiyon" }
        key(KeyEvent.KEYCODE_DPAD_RIGHT)
        ui.onNodeWithContentDescription("Film 71 · Uzak Şehirlerden Uzun Bir Hikâye").assertIsFocused()
        shot("library-category-column")
    }

    @Test fun typingKeepsTheBestRowRightUnderTheField() {
        click("Ara")
        val input = ui.onNode(hasSetTextAction())
        input.assertIsFocused().performTextReplacement("ISIGI")
        searchFinished()
        ui.onNodeWithTag("search-kinds").assertDoesNotExist()
        ui.onNodeWithTag("search-source-filter").assertDoesNotExist()
        val results = model.state.value.searchResults
        // Rows follow relevance: the best match leads the first row.
        ui.onNodeWithTag("search-row:${results.first().kind.name}", useUnmergedTree = true).assertExists()
        val field = ui.onNodeWithTag("search-field").getUnclippedBoundsInRoot()
        val tile = ui.onNodeWithTag("tile:search:${results.first().id}", useUnmergedTree = true).getUnclippedBoundsInRoot()
        assertTrue("Best match sits under the field: $field / $tile", tile.top > field.bottom)
        if (ui.activity.resources.configuration.fontScale < 1.5f)
            assertTrue("First row ends above the TV keyboard (about 308 dp): $tile", tile.bottom.value <= 300f)
        shot("search-typing")
        hideKeyboard()
    }

    @Test fun globalTurkishSearchFiltersRestoresFocusAndCancelsOldQueries() {
        click("Ara")
        val input = ui.onNode(hasSetTextAction())
        input.assertIsFocused().performTextReplacement("ISIGI")
        searchFinished()
        assertEquals(setOf(MediaKind.LIVE, MediaKind.MOVIE, MediaKind.SERIES), model.state.value.searchResults.map { it.kind }.toSet())
        assertEquals(3, model.state.value.searchResults.size)
        ui.onAllNodesWithContentDescription("Işığı Gizli").assertCountEquals(0)
        // Type chips step aside while typing; leaving the field brings them back.
        ui.onNodeWithTag("search-kinds").assertDoesNotExist()
        hideKeyboard()
        key(KeyEvent.KEYCODE_DPAD_DOWN)
        input.assertIsNotFocused()
        ui.onNodeWithTag("search-kinds").assertExists()
        ui.onNode(hasText("Filmler") and hasClickAction() and hasAnyAncestor(hasTestTag("search-kinds"))).performSemanticsAction(SemanticsActions.OnClick) { it() }
        searchFinished()
        assertEquals(MediaKind.MOVIE, model.state.value.searchResults.single().kind)
        input.performSemanticsAction(SemanticsActions.RequestFocus) { it() }
        input.performImeAction()
        ui.onNodeWithTag("search:${model.fixtureKey("movie:1")}").assertIsFocused()
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.waitUntil(10_000) { model.state.value.route == Route.DETAIL && !model.state.value.detailLoading }
        key(KeyEvent.KEYCODE_BACK)
        assertEquals(Route.SEARCH, model.state.value.route)
        assertEquals("ISIGI", model.state.value.searchQuery)
        assertEquals(MediaKind.MOVIE, model.state.value.searchKind)
        ui.onNodeWithTag("search:${model.fixtureKey("movie:1")}").assertIsFocused()
        ui.runOnUiThread {
            model.searchQuery("film"); model.searchQuery("missing"); model.searchQuery("İSTANBUL")
        }
        searchFinished()
        assertEquals(listOf(model.fixtureKey("movie:1")), model.state.value.searchResults.map { it.id })
        input.performSemanticsAction(SemanticsActions.RequestFocus) { it() }
        input.performTextReplacement("Hiç bulunmayan başlık")
        searchFinished()
        ui.onNodeWithText("Eşleşen içerik bulunamadı").assertIsDisplayed()
        input.assertIsFocused()
        hideKeyboard()
        assertEquals(Route.SEARCH, model.state.value.route)
        shot("empty-search")
        input.performTextReplacement("isigi")
        searchFinished()
        input.performImeAction()
        ui.onNodeWithTag("search:${model.fixtureKey("movie:1")}").assertIsFocused()
        removedId = "movie:1"
        ui.runOnUiThread { model.refresh() }
        ui.waitUntil(15_000) { !model.state.value.refreshing && !model.state.value.searching }
        assertTrue(model.state.value.searchResults.isEmpty())
        input.assertIsFocused()
    }

    @Test fun dpadKeepsWholeCaptionsVisibleAndReturnsToSameCatalogCard() {
        click("Filmler")
        // A section opens on its category column; Right goes to the first title.
        ui.onNodeWithTag("category:${CategoryFilter.ALL}").assertIsFocused(); key(KeyEvent.KEYCODE_DPAD_RIGHT)
        ui.onNodeWithContentDescription("İstanbul Işığı").assertIsFocused()
        repeat(7) { key(KeyEvent.KEYCODE_DPAD_DOWN) }
        val focused = ui.onNode(isFocused()).fetchSemanticsNode().config[SemanticsProperties.ContentDescription].single()
        val entry = model.state.value.catalog.items.first { it.title == focused }
        val tile = ui.onNodeWithTag("tile:library:${entry.id}", useUnmergedTree = true).getUnclippedBoundsInRoot()
        val root = ui.onNodeWithTag("library-grid").getUnclippedBoundsInRoot()
        assertTrue("Caption must fit below image: $tile / $root", tile.bottom <= root.bottom)
        assertTrue(tile.top >= root.top)
        shot("catalog-focus")
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.waitUntil(10_000) { model.state.value.route == Route.DETAIL && !model.state.value.detailLoading }
        key(KeyEvent.KEYCODE_BACK)
        ui.onNodeWithContentDescription(focused).assertIsFocused()
        repeat(20) { instrumentation.sendKeyDownUpSync(if (it % 2 == 0) KeyEvent.KEYCODE_DPAD_RIGHT else KeyEvent.KEYCODE_DPAD_LEFT) }
        ui.waitForIdle()
        ui.onNode(isFocused()).assertExists()
        assertEquals(Route.LIBRARY, model.state.value.route)
    }

    @Test fun refreshRecoversRemovedFocusedItemAndMissingCategoryWithoutStealingInput() {
        click("Filmler")
        // A section opens on its category column; Right goes to the first title.
        ui.onNodeWithTag("category:${CategoryFilter.ALL}").assertIsFocused(); key(KeyEvent.KEYCODE_DPAD_RIGHT)
        ui.onNodeWithContentDescription("İstanbul Işığı").assertIsFocused()
        key(KeyEvent.KEYCODE_DPAD_RIGHT)
        ui.onNodeWithContentDescription("Film 2 · Uzak Şehirlerden Uzun Bir Hikâye").assertIsFocused()
        removedId = "movie:2"
        ui.runOnUiThread { model.refresh() }
        ui.waitUntil(15_000) { !model.state.value.refreshing }
        ui.onNodeWithContentDescription("Film 3 · Uzak Şehirlerden Uzun Bir Hikâye").assertIsFocused()
        // Back to the first column, then Left shows the category column again.
        repeat(3) { key(KeyEvent.KEYCODE_DPAD_LEFT) }
        click("Koleksiyon")
        assertEquals("Koleksiyon", model.state.value.category)
        removeCategory = true
        // With a category selected the search says it searches that category.
        click("Bu kategoride ara")
        val input = ui.onNode(hasSetTextAction())
        input.performSemanticsAction(SemanticsActions.RequestFocus) { it() }
        // As a viewer would: OK opens the keyboard on the field before typing.
        key(KeyEvent.KEYCODE_DPAD_CENTER); ui.waitUntil(5_000) { imeShown() }
        ui.runOnUiThread { model.refresh() }
        ui.waitUntil(15_000) { !model.state.value.refreshing }
        assertEquals(CategoryFilter.ALL, model.state.value.category)
        input.assertIsFocused()
        input.performTextReplacement("ISIGI")
        ui.waitUntil(10_000) { !model.state.value.librarySearching }
        input.assertIsFocused()
        ui.onNodeWithContentDescription("İstanbul Işığı").assertExists()
        // While typing, the title and categories step aside so results stay above the TV keyboard.
        ui.onNodeWithTag("library-heading").assertDoesNotExist()
        ui.onNodeWithTag("library-categories").assertDoesNotExist()
        hideKeyboard()
        assertEquals(Route.LIBRARY, model.state.value.route)
        // Without the keyboard open, up/down must leave a text field on a TV remote, and the full layout returns.
        key(KeyEvent.KEYCODE_DPAD_DOWN)
        input.assertIsNotFocused()
        ui.onNodeWithTag("library-heading").assertExists()
        // Down goes to the result under the field, whether or not the keyboard reported its closing (a TV keyboard
        // does, and once went to the category column instead); the focused poster grid takes the category column's
        // place (D056) and Left from its first column brings the column back.
        ui.onNodeWithContentDescription("İstanbul Işığı").assertIsFocused()
        ui.onNodeWithTag("library-categories").assertDoesNotExist()
        key(KeyEvent.KEYCODE_DPAD_LEFT)
        // Either way the column is back, on the category the refresh fell back to.
        ui.onNodeWithTag("library-categories").assertExists()
        ui.onNode(isFocused() and hasAnyAncestor(hasTestTag("library-categories"))).assertExists()
        ui.onNodeWithTag("category:${CategoryFilter.ALL}").assertIsSelected()
    }

    @Test fun longCategoryRowRemainsReachableFromSearchField() {
        manyCategories = true
        ui.runOnUiThread { model.refresh() }
        ui.waitUntil(15_000) { !model.state.value.refreshing }
        click("Filmler")
        // A section opens on its category column; Right goes to the first title.
        ui.onNodeWithTag("category:${CategoryFilter.ALL}").assertIsFocused(); key(KeyEvent.KEYCODE_DPAD_RIGHT)
        ui.onNodeWithContentDescription("İstanbul Işığı").assertIsFocused()
        key(KeyEvent.KEYCODE_DPAD_LEFT)
        ui.onNodeWithTag("library-categories").performScrollToIndex(40)
        ui.onNodeWithTag("library-grid").performScrollToIndex(0)
        click("Bu bölümde ara")
        val input = ui.onNode(hasSetTextAction())
        input.performSemanticsAction(SemanticsActions.RequestFocus) { it() }
        hideKeyboard()
        key(KeyEvent.KEYCODE_DPAD_DOWN)
        input.assertIsNotFocused()
        ui.onNode(isFocused()).assertExists()
        assertEquals(Route.LIBRARY, model.state.value.route)
    }

    @Test fun searchBackVisitsResultsAndNavigationBeforeHome() {
        click("Ara")
        val input = ui.onNode(hasSetTextAction())
        input.performTextReplacement("film")
        searchFinished()
        hideKeyboard()
        input.assertIsFocused()
        assertEquals(Route.SEARCH, model.state.value.route)
        key(KeyEvent.KEYCODE_BACK)
        ui.onNodeWithTag("search:${model.fixtureKey("movie:2")}").assertIsFocused()
        key(KeyEvent.KEYCODE_BACK)
        ui.onNodeWithTag("nav:Ara").assertIsFocused()
        assertEquals("film", model.state.value.searchQuery)
        assertEquals(Route.SEARCH, model.state.value.route)
        key(KeyEvent.KEYCODE_BACK)
        assertEquals(Route.HOME, model.state.value.route)
        ui.onNodeWithTag("exit-dialog").assertDoesNotExist()
        click("Ara")
        ui.onNode(hasSetTextAction()).assertTextContains("film").performTextReplacement("")
        searchFinished()
        hideKeyboard()
        key(KeyEvent.KEYCODE_BACK)
        ui.onNodeWithTag("nav:Ara").assertIsFocused()
        assertEquals(Route.SEARCH, model.state.value.route)
    }

    @Test fun settingsKeepsHeaderRestoresSubpageFocusAndCancelsRemoval() {
        ui.onNodeWithTag("nav:Ayarlar").performSemanticsAction(SemanticsActions.RequestFocus) { it() }
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.onNode(hasText("Hareketi azalt") and hasClickAction()).assertIsFocused()
        val original = model.state.value.reducedMotion
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        assertEquals(!original, model.state.value.reducedMotion)
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        assertEquals(original, model.state.value.reducedMotion)
        repeat(8) { key(KeyEvent.KEYCODE_DPAD_DOWN) }
        ui.onNode(hasText("Uygulama hakkında") and hasClickAction()).assertIsFocused()
        ui.onNodeWithTag("settings-title").assertIsDisplayed()
        ui.onNodeWithTag("settings-close").assertIsDisplayed()
        shot("settings-bottom")
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.onNodeWithTag("settings-close").assertIsFocused()
        key(KeyEvent.KEYCODE_DPAD_DOWN)
        ui.onNodeWithTag("settings-reading").assertIsFocused()
        repeat(10) { key(KeyEvent.KEYCODE_DPAD_DOWN) }
        ui.onNodeWithText("Sağlayıcının yetişkin", substring = true).assertIsDisplayed()
        ui.onNodeWithTag("settings-close").assertIsDisplayed()
        key(KeyEvent.KEYCODE_BACK)
        ui.onNode(hasText("Uygulama hakkında") and hasClickAction()).assertIsFocused()
        repeat(3) { key(KeyEvent.KEYCODE_DPAD_UP) }
        ui.onNode(hasText("Kaynağı kaldır") and hasClickAction()).assertIsFocused()
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.onNodeWithText("Vazgeç").assertIsFocused()
        key(KeyEvent.KEYCODE_BACK)
        ui.onNode(hasText("Kaynağı kaldır") and hasClickAction()).assertIsFocused()
        assertNotNull(model.state.value.source)
        key(KeyEvent.KEYCODE_BACK)
        ui.onAllNodesWithTag("settings-panel").assertCountEquals(0)
        ui.onNodeWithTag("nav:Ayarlar").assertIsFocused()
    }

    @Test fun refreshKeepsAlreadyFocusedCaptionInsideShrinkingViewport() {
        click("Filmler")
        // A section opens on its category column; Right goes to the first title.
        ui.onNodeWithTag("category:${CategoryFilter.ALL}").assertIsFocused(); key(KeyEvent.KEYCODE_DPAD_RIGHT)
        ui.onNodeWithContentDescription("İstanbul Işığı").assertIsFocused()
        repeat(7) { key(KeyEvent.KEYCODE_DPAD_DOWN) }
        val title = ui.onNode(isFocused()).fetchSemanticsNode().config[SemanticsProperties.ContentDescription].single()
        val entry = model.state.value.catalog.items.first { it.title == title }
        ui.runOnUiThread { model.refresh() }
        ui.waitUntil(15_000) { !model.state.value.refreshing }
        ui.onNodeWithContentDescription(title).assertIsFocused()
        val tile = ui.onNodeWithTag("tile:library:${entry.id}", useUnmergedTree = true).getUnclippedBoundsInRoot()
        val root = ui.onNodeWithTag("library-grid").getUnclippedBoundsInRoot()
        shot("refresh-caption")
        assertTrue("Refresh must keep the caption visible: $tile / $root", tile.bottom <= root.bottom)
    }

    @Test fun settingsRefreshKeepsFocusAndIgnoresRepeatedConfirmation() {
        click("Ayarlar")
        ui.onNode(hasText("Hareketi azalt") and hasClickAction()).assertIsFocused()
        repeat(3) { key(KeyEvent.KEYCODE_DPAD_DOWN) }
        val row = ui.onNode(hasText("Kataloğu yenile") and hasClickAction())
        row.assertFocusedOrSay()
        val before = server.requestCount
        slowAuth = true
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.waitUntil(5_000) { model.state.value.refreshing }
        row.assertIsFocused()
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.waitUntil(15_000) { !model.state.value.refreshing }
        row.assertIsFocused()
        assertEquals("One auth plus six catalog endpoints, without a duplicated refresh", 7, server.requestCount - before)
        ui.onNode(hasText("Kütüphanen güncellendi.") and hasAnyAncestor(hasTestTag("settings-panel"))).assertExists()
        ui.onNodeWithTag("settings-close").assertIsDisplayed()
    }

    @Test fun settingsSectionStaysFixedDuringRemoteTraversal() {
        click("Ayarlar")
        ui.waitUntil(5_000) { ui.onAllNodes(hasText("Hareketi azalt") and isFocused()).fetchSemanticsNodes().isNotEmpty() }
        val rows = listOf("Hareketi azalt", "Açılış sesi", "Kaynakları yönet", "Kataloğu yenile",
            "Kaynağı düzenle", "Kaynağı kaldır", "Yetişkin kategorilerini gizle", "Kategorileri yönet",
            "Otomatik sonraki bölüm", "İzleme geçmişini temizle", "Uygulama hakkında")
        val label = ui.onNodeWithTag("settings-section")
        val initial = label.getUnclippedBoundsInRoot()
        fun check(index: Int) {
            val expected = when (index) { 0, 1 -> "Görünüm ve ses"; in 2..5 -> "Kaynaklar"; 6, 7 -> "İçerik"; else -> "İzleme ve uygulama" }
            label.assertTextEquals(expected).assertIsDisplayed()
            val current = label.getUnclippedBoundsInRoot()
            assertEquals(initial.top, current.top)
            assertEquals(initial.bottom, current.bottom)
            val viewport = ui.onNodeWithTag("settings-viewport").getUnclippedBoundsInRoot()
            assertTrue("Section label must stay outside the scrolling viewport", current.bottom <= viewport.top)
            val row = ui.onNode(hasText(rows[index]) and hasClickAction())
            row.assertIsFocused()
            val bounds = row.getUnclippedBoundsInRoot()
            assertTrue("Focused setting must remain fully readable", bounds.top >= viewport.top && bounds.bottom <= viewport.bottom)
        }
        check(0); shot("section-appearance")
        for (index in 1..8) {
            key(KeyEvent.KEYCODE_DPAD_DOWN); check(index)
            if (index == 3) shot("section-sources")
        }
        shot("section-playback")
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.onNodeWithTag("settings-close").assertIsFocused()
        key(KeyEvent.KEYCODE_BACK); check(8)
        for (index in 7 downTo 0) { key(KeyEvent.KEYCODE_DPAD_UP); check(index) }
        key(KeyEvent.KEYCODE_BACK)
    }

    @Test fun failedRefreshRemainsReadableAndCachedContentStaysReachable() {
        val expected = model.state.value.catalog
        click("Ayarlar")
        ui.onNode(hasText("Hareketi azalt") and hasClickAction()).assertIsFocused()
        repeat(3) { key(KeyEvent.KEYCODE_DPAD_DOWN) }
        val row = ui.onNode(hasText("Kataloğu yenile") and hasClickAction())
        row.assertFocusedOrSay()
        failAuth = true
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.waitUntil(15_000) { !model.state.value.refreshing }
        val notice = model.state.value.catalogNotice!!
        val message = notice.message.resolve(InstrumentationRegistry.getInstrumentation().targetContext.resources)
        assertTrue(notice.needsAttention)
        assertEquals(expected, model.state.value.catalog)
        row.assertIsFocused()
        ui.onNode(hasText(message) and hasAnyAncestor(hasTestTag("settings-panel"))).assertIsDisplayed()
        val rowBounds = row.getUnclippedBoundsInRoot()
        val viewport = ui.onNodeWithTag("settings-viewport").getUnclippedBoundsInRoot()
        assertTrue("The full warning row must remain visible", rowBounds.top >= viewport.top && rowBounds.bottom <= viewport.bottom)
        shot("settings-warning")
        key(KeyEvent.KEYCODE_BACK)
        ui.onNodeWithText(message).assertExists()
        click("Filmler")
        // A section opens on its category column; Right goes to the first title.
        ui.onNodeWithTag("category:${CategoryFilter.ALL}").assertIsFocused(); key(KeyEvent.KEYCODE_DPAD_RIGHT)
        ui.onNodeWithContentDescription("İstanbul Işığı").assertIsFocused()
        // Exercise the remote path: an artificial scroll with card focus still active
        // is correctly settled back to the cards by the library's focus policy.
        repeat(2) { key(KeyEvent.KEYCODE_DPAD_UP) }
        ui.onNode(isFocused() and hasAnyAncestor(hasTestTag("library-heading"))).assertExists()
        ui.onNodeWithText(message).assertIsDisplayed()
        val warningBounds = ui.onNodeWithText(message).getUnclippedBoundsInRoot()
        val libraryViewport = ui.onNodeWithTag("library-grid").getUnclippedBoundsInRoot()
        // The heading, warning included, stays above the grid at normal text.
        assertTrue("The entire catalog warning must be readable from the header controls",
            warningBounds.top >= ui.onRoot().getUnclippedBoundsInRoot().top && warningBounds.bottom <= libraryViewport.top)
        shot("catalog-warning")
        repeat(2) { key(KeyEvent.KEYCODE_DPAD_DOWN) }
        ui.onNode(isFocused() and hasAnyAncestor(hasTestTag("library-grid"))).assertExists()
    }
}

