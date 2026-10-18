package dev.iptvplayer.tv

import android.view.KeyEvent
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.iptvplayer.tv.data.*
import dev.iptvplayer.tv.ui.NextResult
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.*
import okio.Buffer
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

/** Two independent local providers intentionally reuse IDs and titles. Emulator only. */
@RunWith(AndroidJUnit4::class)
class MultiSourceFlowTest {
    @get:Rule(order = 0) val turkish = TurkishLocaleRule()
    @get:Rule(order = 1) val ui = createAndroidComposeRule<MainActivity>()
    private lateinit var model: TvViewModel
    private lateinit var a: MockWebServer
    private lateinit var b: MockWebServer
    @Volatile private var failA = false
    @Volatile private var partialA = false
    /** Both providers' calls, with film and series details on their own: Home fetches its slides' once per session (D067). */
    private val catalogCalls = java.util.concurrent.atomic.AtomicInteger()
    private val detailCalls = java.util.concurrent.atomic.AtomicInteger()
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()

    @Before fun setup() {
        Assume.assumeTrue(android.os.Build.HARDWARE.contains("ranchu") || android.os.Build.FINGERPRINT.contains("generic"))
        ui.runOnUiThread { ui.activity.viewModelStore.clear() }
        runBlocking { SourceTransactions.shared.write {
            DeviceStore(ui.activity).forgetSource(); CatalogStore(ui.activity).clear()
        } }
        val bytes = instrumentation.context.assets.open("playback-fixture.mp4").use { it.readBytes() }
        fun server(first: Boolean) = MockWebServer().apply {
            dispatcher = object : Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse {
                    if (request.requestUrl!!.queryParameter("action") in setOf("get_vod_info", "get_series_info")) detailCalls.incrementAndGet()
                    else catalogCalls.incrementAndGet()
                    if (first && failA) return MockResponse().setResponseCode(401)
                    if (first && partialA && request.requestUrl!!.queryParameter("action") == "get_series")
                        return MockResponse().setResponseCode(503)
                    if (request.requestUrl!!.encodedPath == "/mixed.m3u") return MockResponse().setBody(
                        "#EXTM3U\n#EXTINF:-1 group-title=\"Diziler\",Ortak Dizi S01E01\n" +
                            request.requestUrl!!.resolve("/series/1.mp4") + "\n#EXTINF:-1 group-title=\"Diziler\",Ortak Dizi S01E02\n" +
                            request.requestUrl!!.resolve("/series/2.mp4") + "\n")
                    if (request.path.orEmpty().substringBefore('?').endsWith(".mp4"))
                        return MockResponse().addHeader("Content-Type", "video/mp4").setBody(Buffer().write(bytes))
                    val body = when (request.requestUrl!!.queryParameter("action")) {
                        null -> """{"user_info":{"auth":1,"status":"Active"}}"""
                        "get_live_categories", "get_vod_categories", "get_series_categories" -> """[{"category_id":"1","category_name":"Ortak"}]"""
                        "get_live_streams" -> """[{"stream_id":1,"name":"Aynı kanal","category_id":"1","container_extension":"mp4"}]"""
                        "get_vod_streams" -> """[{"stream_id":3,"name":"Aynı film","category_id":"1","container_extension":"mp4"}]"""
                        "get_series" -> """[{"series_id":4,"name":"Aynı dizi","category_id":"1"}]"""
                        "get_vod_info" -> """{"info":{"plot":"İki bağımsız hesaptaki aynı başlık"},"movie_data":{"container_extension":"mp4"}}"""
                        "get_series_info" -> """{"info":{},"episodes":{"1":[{"id":5,"title":"Birinci bölüm","episode_num":1,"container_extension":"mp4"},{"id":6,"title":"İkinci bölüm","episode_num":2,"container_extension":"mp4"}]}}"""
                        else -> "[]"
                    }
                    return MockResponse().addHeader("Content-Type", "application/json").setBody(body)
                }
            }
            start()
        }
        a = server(true); b = server(false)
        reopen(Route.SETUP)
    }
    @After fun cleanup() {
        if (::model.isInitialized) ui.runOnUiThread { ui.activity.viewModelStore.clear() }
        runBlocking { SourceTransactions.shared.write {
            DeviceStore(ui.activity).forgetSource(); CatalogStore(ui.activity).clear()
        } }
        if (::a.isInitialized) a.shutdown()
        if (::b.isInitialized) b.shutdown()
    }
    private fun reopen(route: Route = Route.HOME) {
        ui.runOnUiThread { ui.activity.viewModelStore.clear() }
        ui.activityRule.scenario.recreate(); ui.waitForIdle()
        ui.runOnUiThread { model = ViewModelProvider(ui.activity)[TvViewModel::class.java] }
        ui.waitUntil(10_000) { model.state.value.route == route && model.state.value.sourceTask == SourceTask.IDLE }
    }
    private fun click(text: String) = ui.onNode((hasText(text) or hasContentDescription(text)) and hasClickAction()).performSemanticsAction(SemanticsActions.OnClick) { it() }
    private fun tag(tag: String) = ui.onNodeWithTag(tag).performSemanticsAction(SemanticsActions.OnClick) { it() }
    private fun back() { instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK); ui.waitForIdle() }
    private fun fill(name: String, server: MockWebServer) {
        val fields = ui.onAllNodes(hasSetTextAction())
        fields[0].performTextReplacement(name)
        fields[1].performTextReplacement(server.url("/").toString())
        fields[2].performTextReplacement("fixture")
        fields[3].performTextReplacement("fixture-secret")
        click("Kütüphaneyi aç")
    }
    private fun two(): Pair<SourceConfig, SourceConfig> {
        fill("Salon", a)
        ui.waitUntil(15_000) { model.state.value.route == Route.HOME }
        // Sources are managed from Settings (Kaynakları yönet); the screens have no source selector.
        ui.runOnUiThread { model.manageSources() }; ui.waitForIdle()
        tag("add-source")
        fill("Arşiv", b)
        ui.waitUntil(15_000) { model.state.value.route == Route.SOURCES && model.state.value.sources.size == 2 }
        return model.state.value.sources[0] to model.state.value.sources[1]
    }
    private fun entry(source: SourceConfig, kind: MediaKind) = model.state.value.allCatalog.items.single { it.sourceId == source.id && it.kind == kind }

    @Test fun collidingIdsHaveIndependentFavoritesHistoryAndPersistentFilters() {
        val (one, two) = two()
        val filmA = entry(one, MediaKind.MOVIE); val filmB = entry(two, MediaKind.MOVIE)
        assertNotEquals(filmA.id, filmB.id); assertEquals(filmA.localId, filmB.localId)
        ui.runOnUiThread {
            model.toggleFavorite(filmA); model.recordViewed(filmA); model.saveProgress(filmA, 12000, 120000)
            model.recordViewed(filmB); model.saveProgress(filmB, 34000, 120000)
        }
        assertEquals(setOf(filmA.id), model.state.value.favoriteIds)
        assertEquals(2, model.state.value.history.size)
        assertEquals(12000L, model.progress(filmA)!!.positionMs)
        assertEquals(34000L, model.progress(filmB)!!.positionMs)
        back(); ui.runOnUiThread { model.sourceFilter(two.id) }; ui.waitForIdle()
        assertEquals(setOf(two.id), model.state.value.catalog.items.map { it.sourceId }.toSet())
        click("Ara")
        ui.onNode(hasSetTextAction()).performTextReplacement("aynı film")
        ui.waitUntil(10_000) { !model.state.value.searching && model.state.value.searchResults.size == 2 }
        // Done moves to the results; the search source is set through the model (no selector on screen).
        ui.onNode(hasSetTextAction()).performImeAction()
        ui.runOnUiThread { model.searchSource(one.id) }
        ui.waitUntil(10_000) { !model.state.value.searching && model.state.value.searchResults.size == 1 }
        tag("search:${filmA.id}")
        ui.waitUntil(10_000) { !model.state.value.detailLoading }
        back()
        assertEquals("aynı film", model.state.value.searchQuery)
        assertEquals(one.id, model.state.value.searchSourceId)
        ui.onNodeWithTag("search:${filmA.id}").assertIsFocused()
        val requests = catalogCalls.get(); val details = detailCalls.get()
        reopen()
        assertEquals(requests, catalogCalls.get())
        // Home's only slide is the featured film (no entry is dated): the new session fetches its details once (D067,
        // at most 6 requests), and a restored Home does not ask again.
        ui.waitUntil(10_000) { detailCalls.get() > details }
        ui.activityRule.scenario.recreate(); ui.waitForIdle()
        ui.waitUntil(10_000) { ui.onAllNodesWithTag("hero-watch").fetchSemanticsNodes().isNotEmpty() }
        android.os.SystemClock.sleep(1_000)
        assertEquals("Slide details are fetched once per session", details + 1, detailCalls.get())
        // No screen offers a source selector, so opening the app shows every source again (D060).
        assertNull(model.state.value.sourceFilterId)
        assertEquals(setOf(one.id, two.id), model.state.value.catalog.items.map { it.sourceId }.toSet())
        assertEquals(2, model.state.value.history.size)
        assertEquals(setOf(filmA.id), model.state.value.favoriteIds)
        assertEquals(12000L, model.progress(filmA)!!.positionMs)
        assertEquals(34000L, model.progress(filmB)!!.positionMs)
    }

    @Test fun failedEditPreservesAccountAndRemovalKeepsOtherOwner() {
        val (one, two) = two()
        val filmA = entry(one, MediaKind.MOVIE); val filmB = entry(two, MediaKind.MOVIE)
        ui.runOnUiThread { model.toggleFavorite(filmB); model.recordViewed(filmB); model.saveProgress(filmB, 45000, 120000) }
        tag("edit-source:${one.id}")
        failA = true; fill("Başarısız değişiklik", a)
        ui.waitUntil(10_000) { model.state.value.route == Route.SETUP && model.state.value.error != null }
        assertEquals(one, model.store.loadSources().sources.first())
        assertEquals(6, model.state.value.allCatalog.items.size)
        failA = false; fill("Salon güncel", a)
        ui.waitUntil(15_000) { model.state.value.route == Route.SOURCES }
        assertEquals(one.id, model.state.value.sources.first().id)
        assertEquals("Salon güncel", model.state.value.sources.first().name)
        ui.onNodeWithTag("edit-source:${one.id}").assertIsFocused()
        ui.waitForIdle()
        instrumentation.uiAutomation.takeScreenshot().let { image ->
            val folder = ui.activity.filesDir.resolve("source-navigation-preview").apply { mkdirs() }
            folder.resolve("manager.png").outputStream().use { image.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
            image.recycle()
        }
        ui.runOnUiThread { model.sourceFilter(one.id); model.searchSource(one.id) }
        tag("remove-source:${one.id}")
        ui.onNodeWithTag("cancel-source-removal").assertIsFocused()
        back(); assertEquals(2, model.state.value.sources.size)
        tag("remove-source:${one.id}"); tag("confirm-source-removal")
        ui.waitUntil(10_000) { model.state.value.route == Route.SOURCES && model.state.value.sources.size == 1 }
        assertNull(model.state.value.sourceFilterId); assertNull(model.state.value.searchSourceId)
        ui.onNodeWithTag("remove-source:${two.id}").assertIsFocused()
        assertEquals(setOf(two.id), model.state.value.allCatalog.items.map { it.sourceId }.toSet())
        assertEquals(setOf(filmB.id), model.state.value.favoriteIds)
        assertEquals(45000L, model.progress(filmB)!!.positionMs)
        assertNull(model.progress(filmA))
        ui.runOnUiThread { model.saveProgress(filmA, 999, 1000) }
        assertNull(model.store.progress(one.id, filmA.localId!!))
        assertNull(CatalogStore(ui.activity).read(one))
        reopen()
        assertEquals(listOf(two), model.state.value.sources)
        assertEquals(45000L, model.progress(filmB)!!.positionMs)
    }

    @Test fun unavailableSourceOnStartupKeepsBothCachesAndOtherSourcePlayable() {
        val (one, two) = two()
        runBlocking { SourceTransactions.shared.write {
            val cache = CatalogStore(ui.activity)
            cache.write(one, cache.read(one)!!.copy(updatedAt = 1))
        } }
        failA = true
        reopen()
        ui.waitUntil(10_000) { !model.state.value.refreshing && model.state.value.sourceStatus[one.id]?.failed == true }
        assertEquals(6, model.state.value.allCatalog.items.size)
        assertEquals(3, model.state.value.sourceStatus[two.id]!!.count)
        runBlocking { SourceTransactions.shared.write { CatalogStore(ui.activity).remove(one) } }
        reopen()
        ui.waitUntil(10_000) { !model.state.value.refreshing && model.state.value.sourceStatus[one.id]?.failed == true }
        assertEquals(3, model.state.value.allCatalog.items.size)
        assertEquals(setOf(two.id), model.state.value.allCatalog.items.map { it.sourceId }.toSet())
        val film = entry(two, MediaKind.MOVIE)
        ui.runOnUiThread { model.play(film) }
        ui.waitUntil(20_000) { ui.onAllNodesWithContentDescription("Duraklat").fetchSemanticsNodes().isNotEmpty() }
        assertTrue(model.state.value.playing!!.url.startsWith(b.url("/").toString()))
        back()
        assertEquals(2, model.store.loadSources().sources.size)
    }

    @Test fun secondSourcesEpisodesResolveAndFavoriteTheirOwnParent() {
        val (one, two) = two()
        val show = entry(two, MediaKind.SERIES)
        ui.runOnUiThread { model.open(show) }
        ui.waitUntil(10_000) { !model.state.value.detailLoading && model.state.value.episodes.size == 2 }
        val episode = model.state.value.episodes.first()
        assertEquals(two.id, episode.sourceId)
        ui.runOnUiThread { model.toggleFavorite(episode); model.play(episode) }
        assertEquals(setOf(show.id), model.state.value.favoriteIds)
        assertTrue(model.store.favorites(one.id).isEmpty())
        assertEquals(setOf("series:4"), model.store.favorites(two.id))
        assertEquals(two.id, model.nextEpisode()!!.sourceId)
        assertEquals(NextResult.UNAVAILABLE, model.playNextEpisode(one.id, episode.id, model.nextEpisode()!!.id))
        ui.runOnUiThread { assertEquals(NextResult.STARTED, model.playNextEpisode(two.id, episode.id, model.nextEpisode()!!.id)) }
        assertTrue(model.state.value.playing!!.url.startsWith(b.url("/").toString()))
        back()
    }

    @Test fun partialRefreshKeepsFullOldCatalogAndDoesNotDisturbOtherSourceFilter() {
        val (one, two) = two()
        val catalog = model.state.value.allCatalog
        ui.runOnUiThread { model.sourceFilter(two.id); model.searchSource(one.id); model.searchQuery("dizi") }
        ui.waitUntil(10_000) { !model.state.value.searching }
        partialA = true
        ui.runOnUiThread { model.refresh(one.id) }
        ui.waitUntil(15_000) { !model.state.value.refreshing && model.state.value.sourceStatus[one.id]?.failed == true }
        assertEquals(catalog, model.state.value.allCatalog)
        assertEquals(two.id, model.state.value.sourceFilterId)
        assertEquals(one.id, model.state.value.searchResults.single().sourceId)
        assertEquals(3, CatalogStore(ui.activity).read(one)!!.catalog.items.size)
        partialA = false
        ui.runOnUiThread { model.refresh(one.id) }
        ui.waitUntil(15_000) { !model.state.value.refreshing && !model.state.value.searching }
        assertFalse(model.state.value.sourceStatus[one.id]!!.failed)
        assertEquals(two.id, model.state.value.sourceFilterId)
        assertEquals(one.id, model.state.value.searchResults.single().sourceId)
    }

    @Test fun m3uSeriesDetailsUseEachOwnersRawEpisodeMapAfterReopening() {
        fun add(name: String, server: MockWebServer) {
            val address = server.url("/mixed.m3u").toString()
            ui.runOnUiThread {
                model.draft(SourceDraft(name, SourceType.M3U, address))
                model.connectDraft()
            }
        }
        add("Liste", a)
        ui.waitUntil(10_000) { model.state.value.route == Route.HOME }
        ui.runOnUiThread { model.addSource() }; add("Liste", b)
        ui.waitUntil(10_000) { model.state.value.route == Route.HOME && model.state.value.sources.size == 2 }
        val second = model.state.value.sources[1]
        assertEquals("Liste · 2", model.state.value.sourceName(second.id))
        reopen()
        val show = entry(second, MediaKind.SERIES)
        ui.runOnUiThread { model.open(show) }
        ui.waitUntil(10_000) { !model.state.value.detailLoading }
        assertEquals(2, model.state.value.episodes.size)
        val episode = model.state.value.episodes.first()
        assertTrue(episode.uri!!.startsWith(b.url("/").toString()))
        ui.runOnUiThread { model.toggleFavorite(episode); model.recordViewed(episode); model.saveProgress(episode, 11000, 120000) }
        assertEquals(setOf(show.id), model.state.value.favoriteIds)
        assertEquals(show.withoutSource().id, model.store.history(second.id).single().parentId)
        reopen()
        val resume = model.state.value.history.single()
        ui.runOnUiThread { model.open(model.state.value.allCatalog.items.single { it.id == show.id }, resume) }
        ui.waitUntil(10_000) { model.state.value.route == Route.PLAYER }
        assertEquals(episode.id, model.state.value.playing!!.entry.id)
        assertEquals(11000L, model.state.value.playing!!.startMs)
        back()
    }

    @Test fun duplicateConnectionIsRejectedWithoutChangingEitherSource() {
        val (one, two) = two()
        tag("add-source"); fill("Yanlışlıkla tekrar", a)
        ui.waitUntil(5_000) { model.state.value.error != null }
        assertEquals(Route.SETUP, model.state.value.route)
        assertEquals(listOf(one, two), model.store.loadSources().sources)
        assertEquals("Yanlışlıkla tekrar", model.state.value.draft.name)
        // Validation leaves the form and keyboard available for correction. Back dismisses IME first.
        fun keyboardVisible() = android.os.ParcelFileDescriptor.AutoCloseInputStream(
            instrumentation.uiAutomation.executeShellCommand("dumpsys input_method")
        ).bufferedReader().use { it.readText().contains("mInputShown=true") }
        if (keyboardVisible()) {
            back(); ui.waitUntil(5_000) { !keyboardVisible() }
            assertEquals(Route.SETUP, model.state.value.route)
        }
        back(); assertEquals(Route.SOURCES, model.state.value.route)
    }
}
