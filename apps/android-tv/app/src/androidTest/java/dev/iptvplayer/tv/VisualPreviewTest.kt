package dev.iptvplayer.tv

import android.graphics.Bitmap
import android.view.KeyEvent
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.iptvplayer.tv.data.*
import okhttp3.mockwebserver.*
import okio.Buffer
import org.json.JSONObject
import org.json.JSONArray
import org.junit.*
import org.junit.runner.RunWith

/** Real, already-reviewed artwork over local fixture APIs. Never contacts a provider. Emulator only. */
@RunWith(AndroidJUnit4::class)
class VisualPreviewTest {
    @get:Rule(order = 0) val turkish = TurkishLocaleRule()
    @get:Rule(order = 1) val ui = createAndroidComposeRule<MainActivity>()
    private lateinit var server: MockWebServer
    private lateinit var model: TvViewModel
    @Volatile private lateinit var fixture: JSONObject
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()

    @Before fun setup() {
        val assets = instrumentation.context.assets
        server = MockWebServer()
        server.start()
        fixture = JSONObject(assets.open("visual/catalog.json").bufferedReader().use { it.readText() }
            .replace("@BASE@", server.url("/").toString()))
        val video = assets.open("playback-fixture.mp4").use { it.readBytes() }
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val url = request.requestUrl!!
                if (url.encodedPath.startsWith("/art/")) {
                    val name = url.pathSegments.last()
                    return MockResponse().addHeader("Content-Type", if (name.endsWith("png")) "image/png" else "image/jpeg")
                        .setBody(Buffer().write(assets.open("visual/$name").use { it.readBytes() }))
                }
                if (url.encodedPath.endsWith(".mp4")) return MockResponse().addHeader("Content-Type", "video/mp4").setBody(Buffer().write(video))
                val action = url.queryParameter("action")
                val key = when (action) {
                    "get_vod_info" -> "vod_${url.queryParameter("vod_id")}"
                    "get_series_info" -> "series_${url.queryParameter("series_id")}"
                    else -> action
                }
                return MockResponse().addHeader("Content-Type", "application/json").setBody(
                    if (key == null) """{"user_info":{"auth":1,"status":"Active"}}""" else fixture.opt(key)?.toString() ?: "[]")
            }
        }
        model = resetFixtureSources(ui)
        ui.waitUntil(10_000) { model.state.value.route == Route.SETUP }
        ui.runOnUiThread {
            model.draft(SourceDraft("Evim", SourceType.XTREAM, server.url("/").toString(), "visual", "fixture"))
            model.connectDraft()
        }
        ui.waitUntil(20_000) { model.state.value.route == Route.HOME }
    }
    @After fun cleanup() {
        if (::model.isInitialized) {
            val ids = model.state.value.sources.map { it.id }
            for (id in ids) {
                ui.runOnUiThread { model.removeSource(id) }
                ui.waitUntil(10_000) { model.state.value.sourceTask == SourceTask.IDLE }
            }
        }
        if (::server.isInitialized) server.shutdown()
    }
    private fun click(text: String) = ui.onNode((hasText(text) or hasContentDescription(text)) and hasClickAction()).performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.OnClick) { it() }
    private fun shot(name: String) {
        ui.waitForIdle()
        // Bound the image decode/renderer settlement; no production timing depends on this.
        Thread.sleep(1200)
        // Image completion can invalidate Compose during the wait; settle its fallback state too.
        ui.waitForIdle()
        instrumentation.uiAutomation.takeScreenshot().let { image ->
            val directory = ui.activity.filesDir.resolve("visual-preview").apply { mkdirs() }
            directory.resolve("$name.png").outputStream().use { image.compress(Bitmap.CompressFormat.PNG, 100, it) }
            image.recycle()
        }
    }
    /** Startup must not build a ready library under the moving logo on the same UI thread. */
    @Test fun launchEntranceFinishesBeforeHomeIsComposed() {
        val opening = mutableStateOf(true)
        ui.mainClock.autoAdvance = false
        try {
            ui.runOnUiThread { ui.activity.setContent { dev.iptvplayer.tv.ui.PilotTheme {
                dev.iptvplayer.tv.ui.PilotApp(model, opening.value) { opening.value = false }
            } } }
            ui.mainClock.advanceTimeBy(160)
            ui.waitForIdle()
            ui.onNodeWithTag("home-content").assertDoesNotExist()
            ui.mainClock.advanceTimeBy(2400)
            ui.waitForIdle()
            ui.onNodeWithTag("home-content").assertDoesNotExist()
            ui.mainClock.advanceTimeBy(1200)
            ui.waitForIdle()
            ui.onNodeWithTag("home-content").assertIsDisplayed()
            Assert.assertFalse("The ready Home is revealed after the entrance", opening.value)
            ui.onNodeWithTag("hero-watch").assertIsFocused()
            // A skip opens the same gate immediately, without waiting for the rest of the three-second entrance.
            ui.runOnIdle { opening.value = true }
            ui.mainClock.advanceTimeBy(400); ui.waitForIdle()
            ui.onNodeWithTag("home-content").assertDoesNotExist()
            ui.runOnIdle { opening.value = false }
            ui.mainClock.advanceTimeBy(200); ui.waitForIdle()
            ui.onNodeWithTag("home-content").assertIsDisplayed()
            ui.onNodeWithTag("hero-watch").assertIsFocused()
        } finally { ui.mainClock.autoAdvance = true }
    }
    @Test fun captureGoogleTvRevision() {
        shot("soft-home")
        ui.onAllNodes(SemanticsMatcher("live shelf card") {
            it.config.getOrNull(androidx.compose.ui.semantics.SemanticsProperties.TestTag)?.startsWith("live:") == true
        })[0].performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.RequestFocus) { it() }
        repeat(7) {
            ui.waitForIdle()
            val focused = ui.onNode(isFocused()).fetchSemanticsNode()
            val tag = focused.config[androidx.compose.ui.semantics.SemanticsProperties.TestTag]
            Assert.assertTrue("Remote stays on live shelf", tag.startsWith("live:"))
            val tile = ui.onNodeWithTag("tile:$tag", useUnmergedTree = true).getUnclippedBoundsInRoot()
            val screen = ui.onRoot().getUnclippedBoundsInRoot()
            Assert.assertTrue("Focused channel and caption stay inside screen: $tag tile=$tile screen=$screen", tile.left.value >= 7 && tile.right.value <= screen.right.value - 7 && tile.bottom <= screen.bottom)
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT)
        }
        shot("soft-home-channels")
        ui.onNodeWithTag("navigation-bar").assertIsDisplayed()
        ui.onNodeWithTag("nav:Ana Sayfa").performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.RequestFocus) { it() }
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN)
        ui.onNodeWithTag("nav:Canlı TV").assertIsFocused()
        Assert.assertEquals(Route.HOME, model.state.value.route)
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.waitUntil(5_000) { model.state.value.route == Route.LIBRARY }
        shot("soft-live")
        ui.onNodeWithTag("library-grid").performScrollToIndex(0)
        click("Rehber")
        ui.waitUntil(5_000) { model.state.value.route == Route.GUIDE }
        shot("soft-guide")
        val channels = model.state.value.catalog.items.filter { it.kind == MediaKind.LIVE && !it.adult }
        val visited = mutableSetOf<String>()
        repeat(channels.size) { index ->
            if (index > 0) { instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN); ui.waitForIdle() }
            val id = model.state.value.guideChannelId!!
            visited.add(id)
            val logo = ui.onNodeWithTag("guide-logo:$id", useUnmergedTree = true).getUnclippedBoundsInRoot()
            // D065: the channel number shares the row, so a logo keeps 60 dp of it.
            Assert.assertTrue("Wide channel logos must have enough room", (logo.right - logo.left).value >= 60f)
            val row = ui.onNodeWithTag("guide-row:$id").getUnclippedBoundsInRoot()
            val viewport = ui.onNodeWithTag("guide-grid").getUnclippedBoundsInRoot()
            Assert.assertTrue("Selected row must fit", row.top >= viewport.top && row.bottom <= viewport.bottom)
            Assert.assertTrue("Logo must fit in its channel row", logo.top >= row.top && logo.bottom <= row.bottom)
        }
        Assert.assertEquals(channels.size, visited.size)
        shot("soft-guide-last")
        click("Filmler")
        shot("soft-movies")
        val tile = ui.onNodeWithTag("tile:library:${model.fixtureKey("movie:3")}", useUnmergedTree = true).getUnclippedBoundsInRoot()
        val viewport = ui.onNodeWithTag("library-grid").getUnclippedBoundsInRoot()
        Assert.assertTrue("The whole initially focused poster and caption must fit", tile.top >= viewport.top && tile.bottom.value <= viewport.bottom.value - 26)
        val movie = ui.onNodeWithContentDescription("Inception")
        movie.performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.OnClick) { it() }
        ui.waitUntil(10_000) { !model.state.value.detailLoading }
        shot("soft-detail")
        click("Geri")
        movie.assertIsFocused()
        click("Diziler")
        shot("soft-series")
        click("Ara")
        ui.onNode(hasSetTextAction()).performTextReplacement("in")
        ui.waitUntil(10_000) { !model.state.value.searching && model.state.value.searchResults.isNotEmpty() }
        ui.onNode(hasSetTextAction()).performImeAction()
        shot("soft-search")
    }

    @Test fun searchCardsUseAvailableWidthAndStayVisibleWithRemote() {
        click("Ara")
        ui.onNode(hasSetTextAction()).performTextReplacement("in")
        ui.waitUntil(10_000) { !model.state.value.searching && model.state.value.searchResults.size == 4 }
        ui.onNode(hasSetTextAction()).performImeAction()
        fun focusedTile() {
            ui.waitForIdle()
            val id = model.state.value.searchFocusedId!!
            ui.onNodeWithTag("search:$id").assertIsFocused()
            val tile = ui.onNodeWithTag("tile:search:$id", useUnmergedTree = true).getUnclippedBoundsInRoot()
            val viewport = ui.onNodeWithTag("search-results").getUnclippedBoundsInRoot()
            Assert.assertTrue("Focused search tile fits with safe bottom space: $tile / $viewport",
                tile.top >= viewport.top && tile.bottom.value <= viewport.bottom.value - 26 &&
                    tile.left >= viewport.left && tile.right <= viewport.right)
        }
        focusedTile()
        // One row per kind, best match first: the movie row leads and its cards share a top edge.
        val results = model.state.value.searchResults
        val movies = results.filter { it.kind == MediaKind.MOVIE }
        Assert.assertEquals(results[0].id, model.state.value.searchFocusedId)
        fun tile(entry: MediaEntry) = ui.onNodeWithTag("tile:search:${entry.id}", true).getUnclippedBoundsInRoot()
        val first = tile(movies.first())
        repeat(movies.size - 1) { instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT); focusedTile() }
        Assert.assertEquals(movies.last().id, model.state.value.searchFocusedId)
        Assert.assertEquals(first.top.value, tile(movies.last()).top.value, 1f)
        shot("search-dense-first-row")
        repeat(movies.size - 1) { instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_LEFT); focusedTile() }
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN)
        focusedTile()
        val below = results.first { it.id == model.state.value.searchFocusedId }
        Assert.assertEquals(MediaKind.SERIES, below.kind)
        Assert.assertTrue("The next kind row sits below the first", tile(below).top > first.bottom)
        shot("search-dense-last-row")
    }

    @Test fun heldOkOnHomeCardDoesNotChooseFavoriteWhenReleased() {
        val movie = model.state.value.catalog.items.first { it.kind == MediaKind.MOVIE }
        ui.runOnUiThread { model.recordViewed(movie); model.saveProgress(movie, 45_000, 120_000) }
        ui.waitForIdle()
        val card = ui.onNodeWithTag("continue:${movie.id}")
        card.performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.RequestFocus) { it() }
        ui.waitForIdle()
        heldOkNeedsANewPressForFavorite(card, movie)
    }

    @Test fun heldOkOnLibraryCardDoesNotChooseFavoriteWhenReleased() {
        val movie = model.state.value.catalog.items.first { it.kind == MediaKind.MOVIE }
        ui.runOnUiThread { model.showKind(MediaKind.MOVIE) }
        ui.waitForIdle()
        ui.onNodeWithTag("library-grid").performScrollToNode(hasTestTag("tile:library:${movie.id}"))
        val card = ui.onNode(hasClickAction() and hasAnyAncestor(hasTestTag("tile:library:${movie.id}")))
        card.performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.RequestFocus) { it() }
        ui.waitForIdle()
        heldOkNeedsANewPressForFavorite(card, movie)
    }

    private fun heldOkNeedsANewPressForFavorite(card: SemanticsNodeInteraction, entry: MediaEntry) {
        Assert.assertFalse(entry.id in model.state.value.favoriteIds)
        card.assertIsFocused()
        val down = android.os.SystemClock.uptimeMillis()
        fun event(action: Int, repeat: Int, flags: Int = 0) = KeyEvent(down, android.os.SystemClock.uptimeMillis(),
            action, KeyEvent.KEYCODE_DPAD_CENTER, repeat, 0, android.view.KeyCharacterMap.VIRTUAL_KEYBOARD, 0, flags)
        instrumentation.sendKeySync(event(KeyEvent.ACTION_DOWN, 0))
        Thread.sleep(android.view.ViewConfiguration.getLongPressTimeout().toLong() + 50)
        instrumentation.sendKeySync(event(KeyEvent.ACTION_DOWN, 1, KeyEvent.FLAG_LONG_PRESS))
        ui.waitUntil(5_000) { ui.onAllNodesWithTag("quick-favorite").fetchSemanticsNodes().isNotEmpty() }
        ui.waitForIdle()
        instrumentation.sendKeySync(event(KeyEvent.ACTION_DOWN, 2))
        instrumentation.sendKeySync(event(KeyEvent.ACTION_DOWN, 3))
        instrumentation.sendKeySync(event(KeyEvent.ACTION_UP, 0))
        ui.waitForIdle()
        ui.onNodeWithTag("quick-favorite").assertIsFocused()
        Assert.assertFalse("Releasing the opener must not favorite the card", entry.id in model.state.value.favoriteIds)
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.waitForIdle()
        ui.onNodeWithTag("quick-actions").assertDoesNotExist()
        Assert.assertTrue("A fresh OK press intentionally favorites the card", entry.id in model.state.value.favoriteIds)
        card.assertIsFocused()
    }

    @Test fun continueToRecentChannelsOnlyScrollsEnoughToRevealTheCompactRow() {
        val movie = model.state.value.catalog.items.first { it.kind == MediaKind.MOVIE }
        val live = model.state.value.catalog.items.first { it.kind == MediaKind.LIVE }
        ui.runOnUiThread {
            model.recordViewed(movie); model.saveProgress(movie, 45_000, 120_000)
            model.recordViewed(live)
        }
        ui.waitForIdle()
        ui.onNodeWithTag("hero-watch").performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.RequestFocus) { it() }
        ui.waitForIdle()
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN)
        ui.waitForIdle()
        ui.onNodeWithTag("continue:${movie.id}").assertIsFocused()
        val before = ui.onNodeWithTag("continue:${movie.id}").getUnclippedBoundsInRoot()
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN)
        ui.waitForIdle()
        ui.onNodeWithTag("recent:${live.id}").assertIsFocused()
        val viewport = ui.onNodeWithTag("home-content").getUnclippedBoundsInRoot()
        val heading = ui.onNodeWithTag("shelf-heading:recent", true).getUnclippedBoundsInRoot()
        val recent = ui.onNodeWithTag("recent:${live.id}").getUnclippedBoundsInRoot()
        android.util.Log.i("RecentScrollCheck", "continueBefore=$before recentHeading=$heading recent=$recent viewport=$viewport")
        Assert.assertTrue("A compact recent row must not be pinned to the screen top", heading.top >= viewport.top + 100.dp)
        val after = ui.onNodeWithTag("continue:${movie.id}").getUnclippedBoundsInRoot()
        Assert.assertTrue("The previous continue row remains visible after this short move", after.bottom > viewport.top + 32.dp)
        Assert.assertTrue("Recent card and bottom focus margin are visible", recent.bottom <= viewport.bottom - 26.dp)
        val settled = recent.top
        ui.mainClock.advanceTimeBy(500); ui.waitForIdle()
        Assert.assertEquals("No second scroll after becoming visible", settled.value,
            ui.onNodeWithTag("recent:${live.id}").getUnclippedBoundsInRoot().top.value, 1f)
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_UP)
        ui.waitForIdle()
        ui.onNodeWithTag("continue:${movie.id}").assertIsFocused()
        shot("continue-recent-short-scroll")
    }

    @Test fun recentChannelsKeepLogoTitleAndSourceTogetherWithRemote() {
        val firstSource = model.state.value.sources.single().id
        // The second local source also exercises a long name and the missing-logo fallback.
        fixture.getJSONArray("get_live_streams").getJSONObject(0)
            .put("name", "Dünyanın Dört Bir Yanından Kültür ve Belgesel Kanalı")
        fixture.getJSONArray("get_live_streams").getJSONObject(1).put("stream_icon", "")
        ui.runOnUiThread {
            model.addSource()
            model.draft(SourceDraft("Belgesel ve müzik koleksiyonu", SourceType.XTREAM, server.url("/").toString(), "visual-two", "fixture"))
            model.connectDraft()
        }
        ui.waitUntil(20_000) { model.state.value.route == Route.HOME && model.state.value.sources.size == 2 && model.state.value.sourceTask == SourceTask.IDLE }
        val channels = model.state.value.catalog.items.filter { it.kind == MediaKind.LIVE }
            .groupBy { it.sourceId }.values.flatMap { it.take(3) }
        Assert.assertEquals(6, channels.size)
        for (entry in channels.reversed()) {
            ui.runOnUiThread { model.recordViewed(entry) }
            Thread.sleep(2) // Distinct history timestamps, independent of source merge ordering.
        }
        ui.waitForIdle()
        val recent = homeLibrary(model.state.value.catalog, model.state.value.history, emptySet()).recentChannels
        Assert.assertTrue(recent.any { it.sourceId != firstSource && it.artwork == null })
        ui.onNodeWithTag("hero-watch").performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.RequestFocus) { it() }
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN)
        repeat(recent.size) { index ->
            ui.waitForIdle()
            val entry = recent[index]
            val tag = "recent:${entry.id}"
            val card = ui.onNodeWithTag(tag).assertIsFocused()
            val tile = ui.onNodeWithTag("tile:$tag", true).getUnclippedBoundsInRoot()
            val viewport = ui.onNodeWithTag("home-content").getUnclippedBoundsInRoot()
            Assert.assertTrue("The whole recent card stays visible: $tile / $viewport",
                tile.top >= viewport.top && tile.bottom <= viewport.bottom &&
                    tile.left.value >= viewport.left.value + 7 && tile.right.value <= viewport.right.value - 7)
            val source = model.state.value.sourceName(entry.sourceId)
            Assert.assertTrue(card.fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsProperties.StateDescription].contains(source))
            ui.onNode(hasText(source) and hasAnyAncestor(hasTestTag(tag)), true).assertIsDisplayed()
            if (index == 0 || index == recent.lastIndex) shot("home-recent-$index")
            if (index < recent.lastIndex) instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT)
        }
        val lastTag = "recent:${recent.last().id}"
        ui.onNodeWithTag(lastTag).performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.OnLongClick) { it() }
        ui.onNodeWithTag("quick-actions").assertIsDisplayed()
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
        ui.waitForIdle()
        ui.onNodeWithTag(lastTag).assertIsFocused()
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.waitUntil(10_000) { model.state.value.route == Route.PLAYER }
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
        ui.waitForIdle()
        if (model.state.value.route == Route.PLAYER) instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
        ui.waitUntil(5_000) { model.state.value.route == Route.HOME }
        ui.waitForIdle()
        ui.onNodeWithTag(lastTag).assertIsFocused()
    }

    @Test fun homePostersAndContinueRemainVisibleWithRemote() {
        val backdrop = ui.onNodeWithTag("home-hero-backdrop").getUnclippedBoundsInRoot()
        val homeViewport = ui.onNodeWithTag("home-content").getUnclippedBoundsInRoot()
        Assert.assertEquals("Backdrop starts under the menu", 0f, backdrop.left.value, 1f)
        Assert.assertEquals("Backdrop reaches the right edge", homeViewport.right.value, backdrop.right.value, 1f)
        Assert.assertEquals("Hero reaches the bottom edge", homeViewport.bottom.value, backdrop.bottom.value, 1f)
        shot("home-fullscreen-backdrop")
        for ((section, title) in listOf("movies" to "Film kütüphanen", "series" to "Dizi kütüphanen")) {
            // Release the focused hero/card before programmatic scrolling; its visibility
            // requester otherwise correctly pulls the viewport back toward that focus.
            ui.onNodeWithTag("nav:Ana Sayfa").performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.RequestFocus) { it() }
            ui.onNodeWithTag("home-content").performScrollToNode(hasText(title))
            ui.waitForIdle()
            val first = ui.onAllNodes(SemanticsMatcher("shelf card") {
                it.config.getOrNull(androidx.compose.ui.semantics.SemanticsProperties.TestTag)?.startsWith("$section:") == true
            })[0]
            first.performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.RequestFocus) { it() }
            repeat(3) {
                ui.waitForIdle()
                val focused = ui.onNode(isFocused())
                val tag = focused.fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsProperties.TestTag]
                Assert.assertTrue(tag.startsWith("$section:"))
                val art = focused.getUnclippedBoundsInRoot()
                Assert.assertEquals("Poster ratio", (art.right - art.left).value * 1.5f, (art.bottom - art.top).value, 1f)
                val tile = ui.onNodeWithTag("tile:$tag", useUnmergedTree = true).getUnclippedBoundsInRoot()
                val viewport = ui.onNodeWithTag("home-content").getUnclippedBoundsInRoot()
                Assert.assertTrue("Whole poster and caption fit: $tile / $viewport", tile.top >= viewport.top && tile.bottom <= viewport.bottom)
                val heading = ui.onNodeWithTag("shelf-heading:$section", true).getUnclippedBoundsInRoot()
                Assert.assertTrue("Shelf heading stays fully visible: $heading / $viewport", heading.top >= viewport.top && heading.bottom < tile.top)
                Assert.assertTrue("Shelf starts beneath navigation without a previous-row text sliver: $heading / $viewport",
                    heading.top.value <= viewport.top.value + 9f)
                if (it < 2) instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT)
            }
            shot("home-$section-posters")
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER)
            ui.waitUntil(10_000) { model.state.value.route == Route.DETAIL && !model.state.value.detailLoading }
            click("Geri")
            ui.waitUntil(5_000) { model.state.value.route == Route.HOME }
            ui.waitForIdle()
            Assert.assertTrue(ui.onNode(isFocused()).fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsProperties.TestTag].startsWith("$section:"))
        }
        val movie = model.state.value.catalog.items.first { it.localId == "movie:3" }
        ui.runOnUiThread { model.recordViewed(movie); model.saveProgress(movie, 45_000, 120_000) }
        ui.waitForIdle()
        ui.onNodeWithTag("nav:Ana Sayfa").performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.RequestFocus) { it() }
        ui.onNodeWithTag("home-content").performScrollToIndex(0)
        ui.onNodeWithTag("hero-watch").performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.RequestFocus) { it() }
        ui.waitForIdle()
        ui.onNodeWithTag("hero-watch").assertIsFocused()
        val initialViewport = ui.onNodeWithTag("home-content").getUnclippedBoundsInRoot()
        val initialHeading = ui.onNodeWithTag("shelf-heading:continue", true).getUnclippedBoundsInRoot()
        val initialCard = ui.onNodeWithTag("continue:${movie.id}").getUnclippedBoundsInRoot()
        Assert.assertTrue("Continue heading is fully visible before scrolling: $initialHeading / $initialViewport",
            initialHeading.top >= initialViewport.top && initialHeading.bottom < initialViewport.bottom)
        Assert.assertTrue("At least 32 dp of the continue card peeks into the first screen: $initialCard / $initialViewport",
            initialCard.top <= initialViewport.bottom - 32.dp)
        val initialBackdrop = ui.onNodeWithTag("home-hero-backdrop").getUnclippedBoundsInRoot()
        Assert.assertEquals("Picture still reaches the screen bottom", initialViewport.bottom.value, initialBackdrop.bottom.value, 1f)
        shot("home-initial-continue")
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN)
        ui.waitForIdle()
        ui.onNodeWithTag("continue:${movie.id}").assertIsFocused()
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_UP)
        ui.waitForIdle()
        ui.onNodeWithTag("hero-watch").assertIsFocused()
        Assert.assertEquals("Returning restores the unchanged picture origin", initialViewport.top.value,
            ui.onNodeWithTag("home-hero-backdrop").getUnclippedBoundsInRoot().top.value, 1f)
        ui.onNodeWithTag("nav:Ana Sayfa").performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.RequestFocus) { it() }
        ui.onNodeWithTag("home-content").performScrollToNode(hasText("İzlemeye devam et"))
        val resume = ui.onNodeWithTag("continue:${movie.id}")
        resume.performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.RequestFocus) { it() }
        ui.waitForIdle()
        val card = resume.getUnclippedBoundsInRoot()
        val viewport = ui.onNodeWithTag("home-content").getUnclippedBoundsInRoot()
        Assert.assertTrue(card.top >= viewport.top && card.bottom <= viewport.bottom)
        ui.onNodeWithText("2 dk kaldı", substring = true).assertIsDisplayed()
        shot("home-continue")
        // Discovery stays available with history, directly below the series shelf.
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN)
        ui.waitForIdle()
        Assert.assertTrue(ui.onNode(isFocused()).fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsProperties.TestTag].startsWith("live:"))
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN)
        ui.waitForIdle()
        Assert.assertTrue(ui.onNode(isFocused()).fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsProperties.TestTag].startsWith("series:"))
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN)
        ui.waitForIdle()
        Assert.assertTrue(ui.onNode(isFocused()).fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsProperties.TestTag].startsWith("movies:"))
        shot("home-movies-with-history")
    }

    @Test fun remoteVerticalShelvesKeepTheirHeadingAndFullCardTogether() {
        // The full-screen Hero leaves the first shelf outside LazyColumn's composed viewport.
        ui.onNodeWithTag("hero-watch").assertIsFocused()
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN)
        ui.waitForIdle()
        fun tag(): String {
            ui.waitForIdle()
            return ui.onNode(isFocused()).fetchSemanticsNode().config
                .getOrNull(androidx.compose.ui.semantics.SemanticsProperties.TestTag).orEmpty()
        }
        fun reach(section: String, key: Int) {
            repeat(4) {
                if (!tag().startsWith("$section:")) instrumentation.sendKeyDownUpSync(key)
            }
            val focused = tag()
            Assert.assertTrue("Remote reaches $section, actual=$focused", focused.startsWith("$section:"))
            val viewport = ui.onNodeWithTag("home-content").getUnclippedBoundsInRoot()
            val heading = ui.onNodeWithTag("shelf-heading:$section", true).getUnclippedBoundsInRoot()
            val tile = ui.onNodeWithTag("tile:$focused", true).getUnclippedBoundsInRoot()
            Assert.assertTrue("Heading fully visible", heading.top >= viewport.top && heading.bottom < tile.top)
            Assert.assertTrue("Heading anchored below navigation", heading.top.value <= viewport.top.value + 9f)
            Assert.assertTrue("Whole card and captions visible", tile.top >= viewport.top && tile.bottom.value <= viewport.bottom.value - 26f)
            Assert.assertEquals(Route.HOME, model.state.value.route)
        }
        repeat(3) {
            reach("series", KeyEvent.KEYCODE_DPAD_DOWN)
            reach("movies", KeyEvent.KEYCODE_DPAD_DOWN)
            reach("series", KeyEvent.KEYCODE_DPAD_UP)
            reach("live", KeyEvent.KEYCODE_DPAD_UP)
        }
        reach("movies", KeyEvent.KEYCODE_DPAD_DOWN)
        // Start with a right-hand neighbour; a blocked Right at the last card is not reversible.
        val anchor = model.fixtureKey("movie:3")
        ui.onNodeWithTag("movies:$anchor").performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.RequestFocus) { it() }
        Assert.assertEquals("movies:$anchor", tag())
        repeat(8) {
            // No Compose-idle wait between the two opposite input events.
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT)
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_LEFT)
            Assert.assertEquals("Opposite inputs return to the same content on pair $it", "movies:$anchor", tag())
        }
        shot("home-remote-shelf")
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.waitUntil(10_000) { model.state.value.route == Route.DETAIL && !model.state.value.detailLoading }
        Assert.assertEquals("OK opens the focused item after rapid reversal", anchor, model.state.value.selected?.id)
        click("Geri")
        ui.waitUntil(5_000) { model.state.value.route == Route.HOME }
        Assert.assertEquals("Back restores that exact item", "movies:$anchor", tag())
    }

    @Test fun refreshKeepsContentInPlaceAndSettingsShowsItsResult() {
        fun bounds(tag: String) = ui.onNodeWithTag(tag, useUnmergedTree = true).getUnclippedBoundsInRoot()
        ui.waitForIdle()
        val hero = bounds("home-hero")
        val shelf = bounds("tile:live:${model.fixtureKey("live:100")}")
        ui.runOnUiThread { model.refresh() }
        ui.waitUntil(15_000) { !model.state.value.refreshing }
        ui.waitForIdle()
        Assert.assertEquals("Successful refresh must not move the hero", hero, bounds("home-hero"))
        Assert.assertEquals("Successful refresh must not push the first shelf down", shelf, bounds("tile:live:${model.fixtureKey("live:100")}"))
        ui.onAllNodesWithText("Kütüphanen güncellendi.").assertCountEquals(0)
        shot("feedback-home")
        click("Filmler")
        // A section opens on its category column; Right goes to the first title.
        ui.onNodeWithTag("category:${CategoryFilter.ALL}").assertIsFocused(); instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT); ui.waitForIdle()
        ui.onNodeWithContentDescription("Inception").assertIsFocused()
        val posterTag = "tile:library:${model.fixtureKey("movie:3") }"
        val before = bounds(posterTag)
        val viewport = bounds("library-grid")
        fun assertNoPartialHeading() {
            val nodes = ui.onAllNodesWithTag("library-heading", useUnmergedTree = true).fetchSemanticsNodes()
            if (nodes.isNotEmpty()) {
                val heading = bounds("library-heading")
                Assert.assertTrue("A settled title must be wholly visible or above the viewport: $heading / $viewport",
                    heading.top.value >= viewport.top.value - 1 || heading.bottom.value <= viewport.top.value + 1)
            }
        }
        assertNoPartialHeading()
        ui.runOnUiThread { model.refresh() }
        ui.waitUntil(15_000) { !model.state.value.refreshing }
        ui.waitForIdle()
        Assert.assertEquals("The focused poster and caption must keep their position", before, bounds(posterTag))
        ui.onNodeWithContentDescription("Inception").assertIsFocused()
        assertNoPartialHeading()
        shot("feedback-movies")
        // Repeat to catch a scroll observer that dies after its first interrupted animation.
        repeat(3) {
            repeat(2) { instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_UP); ui.waitForIdle() }
            ui.onNode(isFocused() and hasAnyAncestor(hasTestTag("library-heading"))).assertExists()
            val fullHeading = bounds("library-heading")
            // At normal text the heading stays above the grid (TiviMate-like); it must still be whole on screen.
            Assert.assertTrue("Up must restore the whole heading and its controls",
                fullHeading.top >= ui.onRoot().getUnclippedBoundsInRoot().top && fullHeading.bottom <= viewport.bottom)
            shot("feedback-movies-controls")
            repeat(2) { instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN); ui.waitForIdle() }
            // Down follows the nearest horizontal column; only Back promises the previous card.
            val focused = ui.onNode(isFocused()).fetchSemanticsNode().config
            val focusedTitle = focused.getOrNull(androidx.compose.ui.semantics.SemanticsProperties.ContentDescription)?.singleOrNull()
            val returnedCard = model.state.value.catalog.items.firstOrNull { it.kind == MediaKind.MOVIE && it.title == focusedTitle }
            Assert.assertNotNull("Down from the heading must reach a movie card: $focused", returnedCard)
            assertNoPartialHeading()
            val restored = bounds("tile:library:${returnedCard!!.id}")
            Assert.assertTrue("Down must reveal the whole poster and caption again", restored.top >= viewport.top && restored.bottom.value <= viewport.bottom.value - 26)
        }
        shot("feedback-movies-return")
        click("Ayarlar")
        ui.onNode(hasText("Genel ve dil") and hasClickAction()).assertIsFocused()
        shot("feedback-settings")
        // Kaynaklar, then Kataloğu yenile below Kaynakları yönet.
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN); ui.waitForIdle()
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER); ui.waitForIdle()
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN); ui.waitForIdle()
        val refresh = ui.onNode(hasText("Kataloğu yenile") and hasClickAction())
        refresh.assertIsFocused()
        ui.onNode(hasText("Kütüphanen güncellendi.") and hasAnyAncestor(hasTestTag("settings-panel"))).assertIsDisplayed()
        val refreshBounds = refresh.getUnclippedBoundsInRoot()
        val settingsViewport = bounds("settings-viewport")
        Assert.assertTrue("Focus ring needs breathing room below the refresh row", refreshBounds.bottom.value <= settingsViewport.bottom.value - 6)
        shot("feedback-settings-result")
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.waitUntil(15_000) { !model.state.value.refreshing }
        refresh.assertIsFocused()
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK); ui.waitForIdle()
        repeat(4) { instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN); ui.waitForIdle() }
        ui.onNode(hasText("Hakkında") and hasClickAction()).assertIsFocused()
        shot("feedback-settings-bottom")
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.onNodeWithTag("settings-close").assertIsFocused()
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN)
        repeat(12) { instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN); ui.waitForIdle() }
        ui.onNodeWithText("yayın rehberi kullanılabilir.", substring = true).assertIsDisplayed()
        shot("feedback-about")
    }

    @Test fun providerDividersRemainReachableAndPosterOnlyContentKeepsItsShape() {
        val changed = JSONObject(fixture.toString())
        val originalMovies = changed.getJSONArray("get_vod_streams")
        val movie = (0 until originalMovies.length()).map { originalMovies.getJSONObject(it) }
            .first { it.getString("stream_id") == "3" }.apply { remove("backdrop_path") }
        val divider = "*****Sağlayıcı bilgisi*****"
        changed.put("get_vod_streams", JSONArray().put(JSONObject().put("stream_id", 990).put("name", divider)
            .put("category_id", "1").put("container_extension", "mp4")).put(movie)
            .put(JSONObject().put("stream_id", 991).put("name", "Kapaksız film").put("category_id", "1").put("container_extension", "mp4")))
        changed.getJSONObject("vod_3").getJSONObject("info").remove("backdrop_path")
        changed.put("get_series", JSONArray())
        val firstChannel = changed.getJSONArray("get_live_streams").getJSONObject(0)
        changed.put("get_live_streams", JSONArray().put(JSONObject().put("stream_id", 990).put("name", "*****KANALLAR*****"))
            .put(firstChannel).put(JSONObject().put("stream_id", 991).put("name", "TVP INFO")))
        fixture = changed
        ui.runOnUiThread { model.refresh() }
        ui.waitUntil(15_000) { !model.state.value.refreshing && model.state.value.catalog.items.size == 6 }
        ui.onAllNodesWithContentDescription(divider).assertCountEquals(0)
        val heroPoster = ui.onNodeWithTag("home-hero-poster").assertIsDisplayed().getUnclippedBoundsInRoot()
        Assert.assertEquals((heroPoster.right - heroPoster.left).value * 1.5f, (heroPoster.bottom - heroPoster.top).value, 1f)
        val heroLabel = ui.onNodeWithTag("hero-label", useUnmergedTree = true).getUnclippedBoundsInRoot()
        val heroTitle = ui.onNodeWithTag("hero-title", useUnmergedTree = true).getUnclippedBoundsInRoot()
        // D067: a poster-only slide stands its poster on the right, beside the label and the title rather than below them.
        Assert.assertTrue("Poster stays clear of the hero label and title: $heroPoster / $heroLabel / $heroTitle",
            heroPoster.left.value >= maxOf(heroLabel.right.value, heroTitle.right.value) + 8)
        // The side menu sits beside the hero, never on it, and its section icons stay apart.
        val films = ui.onNodeWithTag("nav:Filmler").getUnclippedBoundsInRoot()
        val series = ui.onNodeWithTag("nav:Diziler").getUnclippedBoundsInRoot()
        Assert.assertTrue("Menu items keep their own rows: $films / $series", series.top >= films.bottom + 4.dp)
        val rail = ui.onNodeWithTag("navigation-bar").getUnclippedBoundsInRoot()
        Assert.assertTrue("Hero poster stays clear of the menu: $heroPoster / $rail", heroPoster.left >= rail.right)
        shot("presentation-home-poster")
        click("Filmler")
        // A section opens on its category column; Right goes to the first title.
        ui.onNodeWithTag("category:${CategoryFilter.ALL}").assertIsFocused(); instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT); ui.waitForIdle()
        ui.onNodeWithContentDescription("Inception").assertIsFocused()
        val normal = ui.onNodeWithContentDescription("Kapaksız film").getUnclippedBoundsInRoot()
        val dividerCard = ui.onNodeWithContentDescription(divider)
        Assert.assertTrue("Divider follows ordinary content", dividerCard.getUnclippedBoundsInRoot().left > normal.left)
        val dated = ui.onNodeWithTag("library-year:${model.fixtureKey("movie:3")}", true).getUnclippedBoundsInRoot()
        val undated = ui.onNodeWithTag("library-year:${model.fixtureKey("movie:991")}", true).getUnclippedBoundsInRoot()
        for (id in listOf(model.fixtureKey("movie:3"), model.fixtureKey("movie:991"))) {
            val titleBox = ui.onNodeWithTag("library-title:$id", true).getUnclippedBoundsInRoot()
            val yearBox = ui.onNodeWithTag("library-year:$id", true).getUnclippedBoundsInRoot()
            Assert.assertEquals("Metadata follows the actual title with 4dp gap", titleBox.bottom.value + 4, yearBox.top.value, 1f)
        }
        Assert.assertEquals("Missing year reserves the same line height", (dated.bottom - dated.top).value, (undated.bottom - undated.top).value, 1f)
        shot("presentation-movies")
        ui.onNodeWithContentDescription("Kapaksız film").performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.RequestFocus) { it() }
        val missingTile = ui.onNodeWithTag("tile:library:${model.fixtureKey("movie:991")}", true).getUnclippedBoundsInRoot()
        val missingViewport = ui.onNodeWithTag("library-grid").getUnclippedBoundsInRoot()
        Assert.assertTrue("Missing artwork still keeps the whole caption visible",
            missingTile.top >= missingViewport.top && missingTile.bottom <= missingViewport.bottom)
        shot("presentation-missing-focused")
        dividerCard.performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.RequestFocus) { it() }
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.waitUntil(10_000) { model.state.value.route == Route.DETAIL && !model.state.value.detailLoading }
        Assert.assertEquals(model.fixtureKey("movie:990"), model.state.value.selected?.id)
        click("Geri")
        dividerCard.assertIsFocused()
        ui.onNodeWithContentDescription("Inception").performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.OnClick) { it() }
        ui.waitUntil(10_000) { !model.state.value.detailLoading }
        val detailPoster = ui.onNodeWithTag("detail-poster-fallback").assertIsDisplayed().getUnclippedBoundsInRoot()
        Assert.assertEquals((detailPoster.right - detailPoster.left).value * 1.5f, (detailPoster.bottom - detailPoster.top).value, 1f)
        shot("presentation-detail-poster")
        click("Geri"); click("Canlı TV")
        // A section opens on its category column; Right goes to the first title.
        ui.onNodeWithTag("category:${CategoryFilter.ALL}").assertIsFocused(); instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT); ui.waitForIdle()
        ui.onNodeWithContentDescription(firstChannel.getString("name")).assertIsFocused()
        ui.onNodeWithContentDescription("TVP INFO").assertExists()
        shot("presentation-live")
        click("Ara")
        ui.onNode(hasSetTextAction()).performTextReplacement("Sağlayıcı")
        ui.waitUntil(10_000) { !model.state.value.searching && model.state.value.searchResults.isNotEmpty() }
        Assert.assertTrue("The raw information entry remains searchable", model.state.value.searchResults.any { it.id == model.fixtureKey("movie:990") })
        fixture = JSONObject(changed.toString()).put("get_live_streams", JSONArray())
            .put("get_vod_streams", JSONArray().put(changed.getJSONArray("get_vod_streams").getJSONObject(0)))
        ui.runOnUiThread { model.refresh() }
        ui.waitUntil(15_000) { !model.state.value.refreshing && model.state.value.catalog.items.size == 1 }
        click("Ana Sayfa")
        ui.onNodeWithText("Ana sayfada öne çıkarılacak içerik bulunamadı.", substring = true).assertIsDisplayed()
        ui.onNodeWithText("Kaynakları kontrol et").assertIsDisplayed()
        shot("presentation-divider-only")
        click("Filmler")
        // A section opens on its category column; Right goes to the first title.
        ui.onNodeWithTag("category:${CategoryFilter.ALL}").assertIsFocused(); instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT); ui.waitForIdle()
        ui.onNodeWithContentDescription(divider).assertIsFocused()
    }

    @Test fun tracksPanelKeepsHeaderAndSelectionReadable() {
        val movie = model.state.value.catalog.items.first { it.localId == "movie:3" }
        ui.runOnUiThread { model.play(movie) }
        ui.waitUntil(10_000) { ui.onAllNodesWithContentDescription("Duraklat").fetchSemanticsNodes().isNotEmpty() }
        click("Duraklat"); click("Ses / Altyazı")
        ui.waitUntil(5_000) { ui.onAllNodes(hasTestTag("tracks-close") and isFocused()).fetchSemanticsNodes().isNotEmpty() }
        ui.onNodeWithTag("tracks-close").assertIsFocused()
        val title = ui.onNodeWithTag("tracks-title").getUnclippedBoundsInRoot()
        fun checkFocusedChoice() {
            val row = ui.onNode(isFocused()).getUnclippedBoundsInRoot()
            val viewport = ui.onNodeWithTag("tracks-viewport").getUnclippedBoundsInRoot()
            Assert.assertTrue("Focused track and its outer focus ring need a safe viewport inset: $row / $viewport",
                row.top.value >= viewport.top.value + 7 && row.bottom.value <= viewport.bottom.value - 7)
            Assert.assertEquals(title, ui.onNodeWithTag("tracks-title").getUnclippedBoundsInRoot())
            ui.onNodeWithTag("tracks-close").assertIsDisplayed()
        }
        repeat(2) { instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN); ui.waitForIdle(); checkFocusedChoice() }
        ui.onNode(hasText("İngilizce · 2", substring = true) and hasClickAction()).assertIsFocused()
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.waitUntil(5_000) { model.store.audioLanguage == "eng" || model.store.audioLanguage == "en" }
        ui.onNode(hasText("İngilizce · 2", substring = true) and hasClickAction()).assertIsSelected()
        shot("tracks-audio")
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN); ui.waitForIdle(); checkFocusedChoice()
        ui.onNode(hasText("Kapalı") and hasClickAction()).assertIsFocused()
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.waitUntil(5_000) { model.store.subtitlesOff }
        ui.onNode(hasText("Kapalı") and hasClickAction()).assertIsSelected()
        shot("tracks-off")
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN); ui.waitForIdle(); checkFocusedChoice()
        ui.onNode(hasText("İngilizce · 1", substring = true) and hasClickAction()).assertIsFocused()
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.waitUntil(5_000) { !model.store.subtitlesOff }
        ui.onNode(hasText("İngilizce · 1", substring = true) and hasClickAction()).assertIsSelected()
        shot("tracks-subtitle")
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK); ui.waitForIdle()
        click("Ses / Altyazı")
        ui.waitUntil(5_000) { ui.onAllNodes(hasTestTag("tracks-close") and isFocused()).fetchSemanticsNodes().isNotEmpty() }
        ui.onNodeWithTag("tracks-close").assertIsFocused()
        ui.onNode(hasText("İngilizce · 2", substring = true) and hasClickAction()).assertIsSelected()
        ui.onNode(hasText("İngilizce · 1", substring = true) and hasClickAction()).assertIsSelected()
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
        ui.waitForIdle()
        ui.onNodeWithTag("player-tracks").assertIsFocused()
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK); ui.waitForIdle(); instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK); ui.waitForIdle()
        ui.waitUntil(10_000) { model.state.value.route == Route.HOME }
    }

    @Test fun playerPanelKeepsTitleAndRemoteActionsInsideScreen() {
        click("Diziler")
        ui.onNodeWithContentDescription("Breaking Bad").performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.OnClick) { it() }
        ui.waitUntil(10_000) { !model.state.value.detailLoading }
        click("Oynat")
        ui.waitUntil(10_000) { ui.onAllNodesWithContentDescription("Duraklat").fetchSemanticsNodes().isNotEmpty() }
        // Freeze the local fixture so the control timeout does not influence geometry checks.
        click("Duraklat")
        val panel = ui.onNodeWithTag("player-panel").getUnclippedBoundsInRoot()
        val title = ui.onNodeWithTag("player-title", useUnmergedTree = true).getUnclippedBoundsInRoot()
        val controls = ui.onNodeWithTag("player-controls").getUnclippedBoundsInRoot()
        val screen = ui.onRoot().getUnclippedBoundsInRoot()
        Assert.assertTrue("Panel respects the common TV gutter", panel.left.value >= dev.iptvplayer.tv.ui.TvLayout.Gutter.value && panel.right.value <= screen.right.value - dev.iptvplayer.tv.ui.TvLayout.Gutter.value)
        Assert.assertTrue("Panel must fit vertically", panel.top >= screen.top && panel.bottom <= screen.bottom)
        Assert.assertTrue("Title remains above transport controls", title.top >= panel.top && title.bottom < controls.top)
        Assert.assertTrue("Controls remain inside panel", controls.bottom <= panel.bottom)
        shot("player-panel")
        ui.onNodeWithTag("player-next-episode").assertExists()
        val nextButton = ui.onNodeWithTag("player-next-episode", true).getUnclippedBoundsInRoot()
        val nextSymbol = ui.onNodeWithTag("player-next-episode-symbol", true).getUnclippedBoundsInRoot()
        Assert.assertEquals("Next episode symbol stays centered in its pill", (nextButton.top + nextButton.bottom).value / 2,
            (nextSymbol.top + nextSymbol.bottom).value / 2, 1f)
        Assert.assertTrue("Episode action has clear space before the audio controls",
            ui.onNodeWithTag("player-tracks", true).getUnclippedBoundsInRoot().left - nextButton.right >= 32.dp)
        repeat(3) { instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT); ui.waitForIdle() }
        ui.onNodeWithTag("player-tracks").assertIsFocused()
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER)
        shot("player-tracks")
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
        ui.waitForIdle()
        repeat(8) {
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT)
            ui.waitForIdle()
            val focused = ui.onNode(isFocused()).getUnclippedBoundsInRoot()
            Assert.assertTrue("Every focused action and its ring must fit without scrolling",
                focused.left.value >= screen.left.value + 5 && focused.right.value <= screen.right.value - 5 && focused.bottom <= controls.bottom)
        }
        ui.onNodeWithTag("player-favorite").assertIsFocused()
        shot("player-last-action")
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK); ui.waitForIdle()
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
        ui.waitUntil(10_000) { model.state.value.route == Route.DETAIL }
    }

    @Test fun detailDescriptionAndWideEpisodesStayReadableWithRemote() {
        val changed = JSONObject(fixture.toString())
        changed.getJSONObject("vod_3").getJSONObject("info").put("plot",
            "Açıklama başlangıcı. " + "Filmdeki kişiler, olaylar ve anlatının ayrıntıları. ".repeat(35) + "AÇIKLAMA SONU")
        fixture = changed
        click("Filmler")
        ui.onNodeWithContentDescription("Inception").performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.OnClick) { it() }
        ui.waitUntil(10_000) { !model.state.value.detailLoading }
        ui.onNodeWithTag("open-description").performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.RequestFocus) { it() }
        shot("movie-detail-spacing")
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.waitUntil(5_000) {
            ui.onAllNodesWithTag("detail-description-reader").fetchSemanticsNodes().any {
                it.config.getOrNull(androidx.compose.ui.semantics.SemanticsProperties.Focused) == true
            }
        }
        shot("detail-description-start")
        var steps = 0
        while (ui.onNodeWithTag("detail-description-reader").fetchSemanticsNode().config
                .getOrNull(androidx.compose.ui.semantics.SemanticsProperties.StateDescription) != "Metnin sonu") {
            Assert.assertTrue("The entire description must be reachable", steps++ < 80)
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN)
            ui.waitForIdle()
            ui.onNodeWithTag("detail-description-reader").assertIsFocused()
        }
        shot("detail-description-end")
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN)
        ui.onNodeWithTag("detail-description-back").assertIsFocused()
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
        ui.waitUntil(5_000) {
            ui.onNodeWithTag("open-description").fetchSemanticsNode().config
                .getOrNull(androidx.compose.ui.semantics.SemanticsProperties.Focused) == true
        }
        click("Geri"); click("Diziler")
        ui.onNodeWithContentDescription("Breaking Bad").performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.OnClick) { it() }
        ui.waitUntil(10_000) { !model.state.value.detailLoading }
        val episodes = model.state.value.episodes.filter { it.season == model.state.value.season && !it.adult }.take(3)
        Assert.assertTrue(episodes.isNotEmpty())
        // TiviMate-like: focus lands on the first episode itself, under the details, not on Oynat.
        ui.waitUntil(5_000) {
            ui.onNodeWithTag("episode:${episodes.first().id}").fetchSemanticsNode().config
                .getOrNull(androidx.compose.ui.semantics.SemanticsProperties.Focused) == true
        }
        ui.onNodeWithText("Oynat").assertIsDisplayed()
        shot("series-detail")
        episodes.forEach { episode ->
            val node = ui.onNodeWithTag("episode:${episode.id}")
            node.performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.RequestFocus) { it() }
            ui.waitForIdle()
            val card = node.getUnclippedBoundsInRoot()
            val screen = ui.onRoot().getUnclippedBoundsInRoot()
            // One 16:9 shelf per season, TiviMate-like: four 196 dp episodes fit beside the side menu.
            Assert.assertEquals("Episode uses the landscape shelf width", 196f, (card.right - card.left).value, 1f)
            Assert.assertTrue("Focused episode includes safe border space", card.top.value >= screen.top.value + 7 && card.bottom.value <= screen.bottom.value - 7 && card.left.value >= screen.left.value + 7 && card.right.value <= screen.right.value - 7)
        }
        shot("series-episodes")
        // One shelf per season: the counter follows the row, Down moves to the next season, Up returns to the
        // episode the first row remembers.
        val all = model.state.value.episodes.filter { !it.adult }
        val seasons = all.map { it.season }.distinct()
        Assert.assertTrue("The fixture series has two seasons", seasons.size >= 2)
        val firstSeason = all.filter { it.season == seasons[0] }
        ui.onNodeWithTag("episode:${firstSeason[0].id}").performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.RequestFocus) { it() }
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT); ui.waitForIdle()
        ui.onNodeWithTag("episode:${firstSeason[1].id}").assertIsFocused()
        ui.onNodeWithTag("season-position:${seasons[0]}", useUnmergedTree = true).assertTextEquals("2 / ${firstSeason.size}")
        val counter = ui.onNodeWithTag("season-position:${seasons[0]}", useUnmergedTree = true).getUnclippedBoundsInRoot()
        val title = ui.onNodeWithText("Sezon ${seasons[0]}").getUnclippedBoundsInRoot()
        Assert.assertEquals("The season counter sits 10 dp right of its title", (title.right + 10.dp).value, counter.left.value, 1f)
        Assert.assertEquals("Title and counter share a bottom line", title.bottom.value, counter.bottom.value, 3f)
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN); ui.waitForIdle()
        ui.onNode(isFocused() and hasAnyAncestor(hasTestTag("season:${seasons[1]}"))).assertExists()
        ui.onNodeWithTag("season-position:${seasons[1]}", useUnmergedTree = true)
            .assertTextEquals("1 / ${all.count { it.season == seasons[1] }}")
        val heading = ui.onNodeWithText("Sezon ${seasons[1]}").getUnclippedBoundsInRoot()
        val viewport = ui.onNodeWithTag("detail-content").getUnclippedBoundsInRoot()
        Assert.assertTrue("The next season's heading comes into view with its episode", heading.top >= viewport.top)
        shot("series-next-season")
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_UP); ui.waitForIdle()
        ui.onNodeWithTag("episode:${firstSeason[1].id}").assertIsFocused()
    }

    @Test fun mixedEpisodeArtworkTitlesAndResumeKeepAlignedCaptions() {
        val changed = JSONObject(fixture.toString())
        val rows = changed.getJSONObject("series_2").getJSONObject("episodes").getJSONArray("1")
        val longTitle = "Breaking Bad - S01E01 - Beklenmedik Bir Yolculuğun Ardından Gelen Uzun ve Zorlu Kararlar"
        rows.getJSONObject(0).put("title", longTitle).getJSONObject("info")
            .put("movie_image", server.url("/missing-art").toString())
        rows.getJSONObject(1).put("title", "Yazı Tura")
        fixture = changed
        click("Diziler")
        ui.onNodeWithContentDescription("Breaking Bad").performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.OnClick) { it() }
        ui.waitUntil(10_000) { !model.state.value.detailLoading }
        val episodes = model.state.value.episodes.filter { it.season == 1 }.take(3)
        Assert.assertEquals(3, episodes.size)
        ui.runOnUiThread { model.recordViewed(episodes.last()); model.saveProgress(episodes.last(), 45_000, 120_000) }
        val first = ui.onNodeWithTag("episode:${episodes.first().id}")
        first.performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.RequestFocus) { it() }
        repeat(3) { index ->
            ui.waitForIdle()
            val episode = episodes[index]
            ui.onNodeWithTag("episode:${episode.id}").assertIsFocused()
            val card = ui.onNodeWithTag("episode:${episode.id}").getUnclippedBoundsInRoot()
            val screen = ui.onRoot().getUnclippedBoundsInRoot()
            Assert.assertTrue("Whole mixed-data episode fits with focus margin", card.top.value >= screen.top.value + 7 && card.bottom.value <= screen.bottom.value - 7)
            val metadata = episodes.map { ui.onNodeWithTag("episode-metadata:${it.id}", useUnmergedTree = true).getUnclippedBoundsInRoot() }
            Assert.assertTrue("Short/long titles and resume status keep metadata aligned: ${metadata.map { it.top }}",
                metadata.all { kotlin.math.abs(it.top.value - metadata.first().top.value) < 1f })
            if (index < 2) instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT)
        }
        ui.onNodeWithTag("episode-metadata:${episodes.last().id}", useUnmergedTree = true)
            .assertTextContains("Devam et", substring = true)
        shot("series-mixed-episodes")
        // Failed artwork must not repeat the title inside its placeholder; the card shows the episode's own name.
        ui.onAllNodesWithText(longTitle.substringAfter("S01E01 - "), useUnmergedTree = true).assertCountEquals(1)
        Assert.assertEquals(45_000L, model.progress(episodes.last())?.resumeMs)
    }

    /** Shareable gallery of the local fixture's favorites and settings, at the device's current text size. */
    @Test fun captureGalleryFavoritesAndSettings() {
        val favorites = listOf(MediaKind.MOVIE, MediaKind.SERIES, MediaKind.LIVE).map { kind ->
            model.state.value.catalog.items.first { it.kind == kind && !it.adult }
        }
        ui.runOnUiThread {
            favorites.filterNot { it.id in model.state.value.favoriteIds }.forEach(model::toggleFavorite)
            model.favorites()
        }
        ui.waitUntil(10_000) {
            model.state.value.route == Route.LIBRARY &&
                ui.onAllNodesWithTag("library-grid").fetchSemanticsNodes().isNotEmpty()
        }
        shot("gallery-favorites")

        fun title(text: String) {
            ui.waitUntil(5_000) {
                ui.onAllNodes(hasTestTag("settings-title") and hasText(text)).fetchSemanticsNodes().isNotEmpty()
            }
        }
        fun back() {
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
            ui.waitForIdle()
        }
        fun openSection(label: String) {
            ui.onNode(hasText(label) and hasClickAction() and hasAnyAncestor(hasTestTag("settings-panel")))
                .performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.OnClick) { it() }
        }

        ui.onNodeWithTag("nav:Ayarlar")
            .performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.RequestFocus) { it() }
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER)
        title("Ayarlar")
        shot("gallery-settings")
        listOf(
            "Genel ve dil" to "general",
            "Kaynaklar" to "sources",
            "İçerik ve ebeveyn denetimi" to "parental",
            "Oynatıcı" to "player",
            "Kütüphane" to "library"
        ).forEach { (label, name) ->
            openSection(label)
            title(label)
            shot("gallery-settings-$name")
            back()
            title("Ayarlar")
        }
        openSection("Hakkında")
        title("Uygulama hakkında")
        shot("gallery-about")
        back()
        title("Ayarlar")
        back()
        ui.waitUntil(5_000) { ui.onAllNodesWithTag("settings-panel").fetchSemanticsNodes().isEmpty() }
    }

    @Test fun captureNativeScreensWithRealArtwork() {
        val movie = model.state.value.catalog.items.first { it.localId == "movie:3" }
        ui.runOnUiThread { model.recordViewed(movie); model.saveProgress(movie, 1_200_000, 8_880_000) }
        click("Filmler"); click("Ana Sayfa")
        // Home opens on the hero (D070), even with a title to continue.
        ui.onNodeWithTag("hero-watch").assertIsFocused()
        // Resume focus now frames its shelf. Release it before capturing the hero;
        // the current hero renders the content type in a separate metadata line.
        ui.onNodeWithTag("hero-watch").performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.RequestFocus) { it() }
        ui.onNodeWithTag("home-content").performScrollToIndex(0)
        ui.onNodeWithTag("hero-label").assertTextEquals("KÜTÜPHANENDEN").assertIsDisplayed()
        shot("home")
        click("Filmler")
        shot("movies")
        ui.onNodeWithContentDescription("Inception").performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.OnClick) { it() }
        ui.waitUntil(10_000) { !model.state.value.detailLoading }
        shot("movie")
        click("Geri"); click("Diziler")
        ui.onNodeWithContentDescription("Breaking Bad").performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.OnClick) { it() }
        ui.waitUntil(10_000) { !model.state.value.detailLoading }
        shot("series")
        click("Oynat")
        ui.waitUntil(10_000) { ui.onAllNodesWithContentDescription("Duraklat").fetchSemanticsNodes().isNotEmpty() }
        shot("player")
        click("Ses / Altyazı")
        shot("tracks")
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
        ui.waitForIdle()
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK); ui.waitForIdle(); instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK); ui.waitForIdle()
        click("Geri"); click("Ayarlar")
        shot("settings")
        ui.onNode(hasText("Hakkında") and hasClickAction()).performScrollTo().performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.OnClick) { it() }
        shot("about")
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
        shot("settings-bottom")
        click("Kapat"); click("Ara")
        ui.onNode(hasSetTextAction()).performTextReplacement("in")
        ui.waitUntil(10_000) { !model.state.value.searching && model.state.value.searchResults.isNotEmpty() }
        ui.onNode(hasSetTextAction()).performImeAction()
        shot("search")
        ui.runOnUiThread {
            model.manageSources(); model.addSource()
            model.draft(SourceDraft("Sinema Arşivi", SourceType.XTREAM, server.url("/").toString(), "archive", "fixture"))
            model.connectDraft()
        }
        ui.waitUntil(20_000) { model.state.value.route == Route.SOURCES && model.state.value.sources.size == 2 }
        shot("sources")
        click("Geri")
        ui.runOnUiThread { model.search(); model.searchQuery("inception") }
        ui.waitUntil(10_000) { !model.state.value.searching && model.state.value.searchResults.size == 2 }
        ui.onNode(hasSetTextAction()).performImeAction()
        shot("multi-search")
        ui.onNodeWithTag("search-source-filter").assertDoesNotExist()
        ui.runOnUiThread { model.showKind(MediaKind.MOVIE) }
        shot("multi-movies")
    }
}
