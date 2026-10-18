package dev.iptvplayer.tv

import android.view.KeyEvent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.iptvplayer.tv.data.*
import okhttp3.mockwebserver.*
import okio.Buffer
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

/** Synthetic accounts and a generated video only; never put real provider details in this APK. */
@RunWith(AndroidJUnit4::class)
class PilotFlowTest {
    @get:Rule(order = 0) val turkish = TurkishLocaleRule()
    @get:Rule(order = 1) val ui = createAndroidComposeRule<MainActivity>()
    private lateinit var server: MockWebServer
    private lateinit var model: TvViewModel

    @Before fun setup() {
        val bytes = InstrumentationRegistry.getInstrumentation().context.assets.open("playback-fixture.mp4").use { it.readBytes() }
        server = MockWebServer()
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val url = request.requestUrl!!
                if (url.encodedPath.endsWith(".mp4")) {
                    val start = request.getHeader("Range")?.substringAfter("bytes=")?.substringBefore('-')?.toIntOrNull() ?: 0
                    return MockResponse().setResponseCode(if (start > 0) 206 else 200)
                        .addHeader("Content-Type", "video/mp4").addHeader("Accept-Ranges", "bytes")
                        .apply { if (start > 0) addHeader("Content-Range", "bytes $start-${bytes.lastIndex}/${bytes.size}") }
                        .setBody(Buffer().write(bytes, start.coerceIn(0, bytes.size), (bytes.size - start).coerceAtLeast(0)))
                }
                val body = when (url.queryParameter("action")) {
                    null -> """{"user_info":{"auth":1,"status":"Active"}}"""
                    "get_live_categories", "get_vod_categories", "get_series_categories" -> """[{"category_id":"1","category_name":"Test"}]"""
                    "get_live_streams" -> """[{"stream_id":1,"name":"Test kanalı","category_id":"1","container_extension":"mp4"},{"stream_id":2,"name":"İkinci kanal","category_id":"1","container_extension":"mp4"}]"""
                    "get_vod_streams" -> """[{"stream_id":3,"name":"Test filmi","category_id":"1","container_extension":"mp4"}]"""
                    "get_series" -> """[{"series_id":4,"name":"Test dizisi","category_id":"1"}]"""
                    "get_vod_info" -> """{"info":{"plot":"Sentetik oynatma testi","duration_secs":120},"movie_data":{"container_extension":"mp4"}}"""
                    "get_series_info" -> """{"info":{"plot":"Sentetik dizi"},"episodes":{"1":[{"id":5,"title":"Birinci bölüm","episode_num":1,"container_extension":"mp4"},{"id":6,"title":"İkinci bölüm","episode_num":2,"container_extension":"mp4"}],"2":[{"id":7,"title":"Yeni sezon","episode_num":1,"season":2,"container_extension":"mp4"}]}}"""
                    else -> "[]"
                }
                return MockResponse().addHeader("Content-Type", "application/json").setBody(body)
            }
        }
        server.start()
        model = resetFixtureSources(ui)
        ui.waitUntil(10_000) { model.state.value.route == Route.SETUP }
    }

    @After fun cleanup() {
        if (::model.isInitialized) ui.runOnUiThread { model.back(); model.removeSource() }
        if (::server.isInitialized) server.shutdown()
    }

    private fun connect(openLive: Boolean = true) {
        val address = server.url("/").toString()
        val fields = ui.onAllNodes(hasSetTextAction())
        fields[0].performTextReplacement("Test")
        fields[1].performTextReplacement(address)
        fields[2].performTextReplacement("fixture")
        fields[3].performTextReplacement("fixture-secret")
        click("Kütüphaneyi aç")
        ui.waitUntil(30_000) { model.state.value.route == Route.HOME }
        if (openLive) click("Canlı TV")
    }
    private fun click(text: String) = ui.onNode((hasText(text) or hasContentDescription(text)) and hasClickAction()).performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.OnClick) { it() }
    /** Side menu items by tag: section names such as Favoriler also appear in the category column. */
    private fun nav(label: String) = ui.onNodeWithTag("nav:$label").performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.OnClick) { it() }
    private fun back() { InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK); ui.waitForIdle() }
    private fun ready() {
        ui.waitUntil(30_000) { ui.onAllNodesWithText("Duraklat").fetchSemanticsNodes().isNotEmpty() }
    }
    private fun reopenFromDisk() {
        // Activity recreation alone retains its ViewModel; explicitly discard it to exercise disk restoration.
        ui.runOnUiThread { ui.activity.viewModelStore.clear() }
        ui.activityRule.scenario.recreate(); ui.waitForIdle()
        ui.runOnUiThread { model = ViewModelProvider(ui.activity)[TvViewModel::class.java] }
        ui.waitUntil(10_000) { model.state.value.route == Route.HOME }
    }

    @Test fun liveMovieSeriesTracksFavoritesAndBack() {
        connect()
        // D060: entering Canlı TV from the menu lands on the selected "Tüm kanallar" row; Right reaches the first channel.
        ui.onNodeWithTag("category:${CategoryFilter.ALL}").assertIsFocused().assertIsSelected()
        InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT); ui.waitForIdle()
        ui.onNodeWithContentDescription("Test kanalı").assertIsFocused()
        InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER)
        ready()
        click("Ses / Altyazı")
        ui.waitUntil(10_000) { ui.onAllNodesWithText("Türkçe", substring = true).fetchSemanticsNodes().isNotEmpty() }
        ui.onNodeWithText("İngilizce · 2", substring = true).performScrollTo().performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.OnClick) { it() }
        ui.onNodeWithText("İngilizce · 1", substring = true).performScrollTo().performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.OnClick) { it() }
        ui.onNodeWithText("İngilizce · 1", substring = true).assertExists()
        back()
        click("Kanal ›")
        ui.waitUntil(10_000) { model.state.value.playing?.entry?.title == "İkinci kanal" }
        ready()
        back(); back()
        ui.onNodeWithContentDescription("Test kanalı").assertIsFocused()
        click("Filmler")
        ui.onNodeWithContentDescription("Test filmi").performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.OnClick) { it() }
        ui.waitUntil(10_000) { !model.state.value.detailLoading }
        click("Favoriye ekle")
        click("Oynat")
        ready()
        click("+10 sn")
        ui.waitUntil(10_000) { (model.progress(model.state.value.playing!!.entry)?.positionMs ?: 0) >= 10_000 }
        back(); back()
        ui.onNodeWithText("Devam et", substring = true).assertExists()
        back()
        ui.onNodeWithContentDescription("Test filmi").assertIsFocused()
        click("Diziler")
        ui.onNodeWithContentDescription("Test dizisi").performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.OnClick) { it() }
        ui.waitUntil(10_000) { model.state.value.episodes.size == 3 }
        ui.onNodeWithText("Birinci bölüm").performScrollTo().performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.OnClick) { it() }
        ready()
        click("Sonraki bölüm")
        ui.waitUntil(10_000) { model.state.value.playing?.entry?.title == "İkinci bölüm" }
        ready()
        back(); back()
        // Seasons are stacked shelves: back from the player on the episode just played, Down reaches the next
        // season and Up returns to the episode the first shelf remembers.
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        ui.onNode(hasText("İkinci bölüm") and hasClickAction()).assertIsFocused()
        ui.onNodeWithTag("season-position:1", useUnmergedTree = true).assertTextEquals("2 / 2")
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN); ui.waitForIdle()
        ui.onNode(hasText("Yeni sezon") and hasClickAction()).assertIsFocused()
        ui.onNodeWithText("Sezon 2").assertIsDisplayed()
        ui.onNodeWithTag("season-position:2", useUnmergedTree = true).assertTextEquals("1 / 1")
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_UP); ui.waitForIdle()
        ui.onNode(hasText("İkinci bölüm") and hasClickAction()).assertIsFocused()
        back()
        click("Filmler"); nav("Favoriler")
        ui.onNodeWithContentDescription("Test filmi").assertExists()
        val stored = ui.activity.getSharedPreferences("device_state", 0).getString("sources_v2", "").orEmpty()
        assertTrue(stored.isNotEmpty()); assertFalse(stored.contains("fixture-secret"))
        val restored = DeviceStore(ui.activity).loadSources().sources.singleOrNull()
        assertEquals("fixture-secret", restored?.password)
    }

    @Test fun m3uImportAndSafeEmptyState() {
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest) = MockResponse().setBody("#EXTM3U\n")
        }
        val address = server.url("/empty.m3u").toString()
        ui.runOnUiThread {
            model.draft(SourceDraft("Empty", SourceType.M3U, address))
            model.connectDraft()
        }
        ui.waitUntil(20_000) { model.state.value.route == Route.HOME }
        ui.onNodeWithText("Kütüphanen için yer hazır").assertExists()
        ui.onNodeWithTag("nav:Ana Sayfa").assertIsFocused()
        ui.onNodeWithTag("nav-label:Ana Sayfa", useUnmergedTree = true).assertExists()
        click("Ayarlar"); click("Kaynağı kaldır"); click("Kaynağı kaldır")
        ui.waitUntil(10_000) { model.state.value.route == Route.SETUP }
        assertNull(DeviceStore(ui.activity).loadSources().sources.singleOrNull())
    }

    @Test fun episodeFavoriteAddsItsSeriesAndPersistsAcrossReopening() {
        connect(openLive = false)
        click("Diziler")
        ui.runOnUiThread { model.open(model.state.value.catalog.items.first { it.kind == MediaKind.SERIES }) }
        ui.waitUntil(10_000) { !model.state.value.detailLoading }
        click("Oynat"); ready()
        ui.onNodeWithTag("player-favorite").assertContentDescriptionEquals("Diziyi favorilere ekle")
            .performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.OnClick) { it() }
        assertEquals(setOf(model.fixtureKey("series:4")), model.state.value.favoriteIds)
        click("Sonraki bölüm"); ready()
        ui.onNodeWithTag("player-favorite").assertContentDescriptionEquals("Diziyi favorilerden çıkar")
        back(); back(); back(); nav("Favoriler")
        ui.onNodeWithContentDescription("Test dizisi").assertExists()
        reopenFromDisk()
        assertEquals(setOf(model.fixtureKey("series:4")), model.state.value.favoriteIds)
        nav("Favoriler")
        ui.onNodeWithContentDescription("Test dizisi").performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.OnClick) { it() }
        ui.waitUntil(10_000) { !model.state.value.detailLoading }
        click("Favorilerden çıkar")
        assertTrue(model.state.value.favoriteIds.isEmpty())
    }

    @Test fun legacyEpisodeFavoriteMigratesWhenItsSeriesIsOpened() {
        connect(openLive = false)
        ui.runOnUiThread { model.store.favorites(model.state.value.source!!.id, setOf("episode:6")) }
        reopenFromDisk()
        assertEquals(setOf(model.fixtureKey("episode:6")), model.state.value.favoriteIds)
        ui.runOnUiThread { model.open(model.state.value.catalog.items.first { it.kind == MediaKind.SERIES }) }
        ui.waitUntil(10_000) { !model.state.value.detailLoading }
        assertEquals(setOf(model.fixtureKey("series:4")), model.state.value.favoriteIds)
        assertEquals(setOf("series:4"), model.store.favorites(model.state.value.source!!.id))
        click("Favorilerden çıkar"); assertTrue(model.state.value.favoriteIds.isEmpty())
    }

    @Test fun homeResumesExactEpisodeRestoresFocusAndClearsOnlyHistory() {
        connect(openLive = false)
        click("Diziler")
        ui.onNodeWithContentDescription("Test dizisi").performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.OnClick) { it() }
        ui.waitUntil(10_000) { model.state.value.episodes.size == 3 }
        click("Favoriye ekle")
        ui.onNodeWithText("İkinci bölüm").performScrollTo().performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.OnClick) { it() }
        ready()
        click("+10 sn")
        ui.waitUntil(10_000) { model.state.value.history.any { it.id == model.fixtureKey("episode:6") && it.positionMs >= 10_000 } }
        back(); back(); back()
        click("Ana Sayfa")
        // Home opens on the hero when there is one (D070); Down reaches the continue shelf.
        if (ui.onAllNodesWithTag("hero-watch").fetchSemanticsNodes().isNotEmpty()) {
            ui.onNodeWithTag("hero-watch").assertIsFocused()
            InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN); ui.waitForIdle()
        }
        val tile = ui.onNodeWithTag("continue:${model.fixtureKey("series:4")}")
        tile.assertIsFocused()
        val sourceId = model.state.value.source!!.id
        assertEquals("episode:6", DeviceStore(ui.activity).history(sourceId).first().id)
        tile.performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.OnClick) { it() }
        ui.waitUntil(20_000) { model.state.value.route == Route.PLAYER && model.state.value.playing?.entry?.id == model.fixtureKey("episode:6") }
        ready()
        assertTrue(model.state.value.playing!!.startMs >= 10_000)
        back(); back(); back()
        ui.onNodeWithTag("continue:${model.fixtureKey("series:4")}").assertIsFocused()
        click("Ayarlar")
        ui.onNodeWithText("İzleme geçmişini temizle").performScrollTo().performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.OnClick) { it() }
        click("Geçmişi temizle")
        ui.waitUntil(5_000) { model.state.value.history.isEmpty() }
        assertTrue(DeviceStore(ui.activity).history(sourceId).isEmpty())
        assertNull(model.progress(MediaEntry("episode:6", "6", MediaKind.EPISODE, "Second").inSource(sourceId)))
        assertTrue(model.fixtureKey("series:4") in model.state.value.favoriteIds)
        ui.onAllNodesWithTag("continue:${model.fixtureKey("series:4")}").assertCountEquals(0)
    }

    @Test fun cachedCatalogOpensWithoutRequestsAndFailedRefreshKeepsLibrary() {
        connect(openLive = false)
        val expected = model.state.value.catalog
        // Home fetches its slides' details once per session (D067); those are not catalogue calls.
        val catalogCalls = java.util.concurrent.atomic.AtomicInteger()
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                if (request.requestUrl!!.queryParameter("action") !in setOf("get_vod_info", "get_series_info")) catalogCalls.incrementAndGet()
                return MockResponse().setResponseCode(401).setBody("{}")
            }
        }
        lateinit var reopened: TvViewModel
        ui.runOnUiThread { reopened = TvViewModel(ui.activity.application) }
        ui.waitUntil(10_000) { reopened.state.value.route == Route.HOME }
        assertEquals(expected, reopened.state.value.catalog)
        assertEquals("Fresh cache must open without provider calls", 0, catalogCalls.get())
        ui.runOnUiThread { model.showKind(MediaKind.MOVIE); model.refresh() }
        ui.waitUntil(15_000) { !model.state.value.refreshing }
        assertEquals(Route.LIBRARY, model.state.value.route)
        assertEquals(expected, model.state.value.catalog)
        assertNotNull(model.state.value.catalogNotice)
        val source = model.state.value.source!!
        assertEquals(expected.fixtureRaw(), CatalogStore(ui.activity).read(source)?.catalog)
        val cachedFile = ui.activity.filesDir.resolve("catalogs").listFiles()!!.first { it.extension == "bin" }
        assertFalse(cachedFile.readBytes().toString(Charsets.UTF_8).contains("Test kanalı"))
        ui.runOnUiThread { model.removeSource() }
        ui.waitUntil(10_000) { model.state.value.route == Route.SETUP }
        assertNull(CatalogStore(ui.activity).read(source))
    }

    @Test fun corruptOrDifferentAccountCacheIsNotReused() {
        connect(openLive = false)
        val source = model.state.value.source!!
        val cache = CatalogStore(ui.activity)
        assertNotNull(cache.read(source))
        assertNull(cache.read(source.copy(password = "other-account-secret")))
        val cachedFile = ui.activity.filesDir.resolve("catalogs").listFiles()!!.first { it.extension == "bin" }
        val data = cachedFile.readBytes()
        data[data.lastIndex] = (data.last().toInt() xor 1).toByte()
        cachedFile.writeBytes(data)
        assertNull(cache.read(source))
        lateinit var reopened: TvViewModel
        ui.runOnUiThread { reopened = TvViewModel(ui.activity.application) }
        ui.waitUntil(30_000) { reopened.state.value.route == Route.HOME && !reopened.state.value.refreshing }
        assertNotNull(cache.read(source))
        assertEquals(model.state.value.catalog.items, reopened.state.value.catalog.items)
    }

    @Test fun devicePreferencesSurviveSourceRemovalAndActivityRecreation() {
        connect(openLive = false)
        ui.runOnUiThread {
            if (!model.state.value.reducedMotion) model.reducedMotion()
            if (model.state.value.startupSound) model.startupSound()
            if (model.state.value.autoNextEpisode) model.autoNextEpisode()
        }
        ui.activityRule.scenario.recreate()
        ui.waitForIdle()
        assertTrue(DeviceStore(ui.activity).reducedMotion)
        assertFalse(DeviceStore(ui.activity).startupSound)
        assertFalse(DeviceStore(ui.activity).autoNextEpisode)
        ui.runOnUiThread { model.removeSource() }
        ui.waitUntil(10_000) { model.state.value.route == Route.SETUP }
        assertTrue(model.state.value.reducedMotion)
        assertFalse(model.state.value.startupSound)
        assertFalse(model.state.value.autoNextEpisode)
        assertNull(DeviceStore(ui.activity).loadSources().sources.singleOrNull())
        // Leave the shared emulator at the shipping defaults for the next case.
        ui.runOnUiThread { model.reducedMotion(); model.startupSound(); model.autoNextEpisode() }
    }
}

