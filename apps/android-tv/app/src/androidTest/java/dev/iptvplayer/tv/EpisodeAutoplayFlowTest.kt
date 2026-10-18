package dev.iptvplayer.tv

import android.graphics.Bitmap
import android.os.SystemClock
import android.view.KeyEvent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.iptvplayer.tv.data.*
import okhttp3.mockwebserver.*
import okio.Buffer
import org.json.JSONArray
import org.json.JSONObject
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@RunWith(AndroidJUnit4::class)
class EpisodeAutoplayFlowTest {
    @get:Rule(order = 0) val turkish = TurkishLocaleRule()
    @get:Rule(order = 1) val ui = createAndroidComposeRule<MainActivity>()
    private lateinit var model: TvViewModel
    private lateinit var server: MockWebServer
    private val requests = ConcurrentHashMap<String, AtomicInteger>()
    @Volatile private var removedNext = false
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()

    @Before fun setup() {
        Assume.assumeTrue(android.os.Build.HARDWARE.contains("ranchu") || android.os.Build.FINGERPRINT.contains("generic"))
        val assets = instrumentation.context.assets
        val short = assets.open("episode-end-fixture.mp4").use { it.readBytes() }
        val long = assets.open("playback-fixture.mp4").use { it.readBytes() }
        server = MockWebServer(); server.start()
        val base = server.url("/").toString()
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val url = request.requestUrl!!
                if (url.encodedPath == "/art.jpg") return MockResponse().addHeader("Content-Type", "image/jpeg")
                    .setBody(Buffer().write(assets.open("visual/s2-2-1.jpg").use { it.readBytes() }))
                if (url.encodedPath == "/list.m3u") return MockResponse().setBody(
                    "#EXTM3U\n#EXTINF:-1 group-title=\"Diziler\" tvg-logo=\"${base}art.jpg\",Örnek S01E01\n${base}series/10.mp4\n#EXTINF:-1 group-title=\"Diziler\" tvg-logo=\"${base}art.jpg\",Örnek S01E02\n${base}series/20.mp4\n")
                if (url.encodedPath.endsWith(".mp4")) {
                    val id = url.pathSegments.last().substringBefore('.')
                    requests.getOrPut(id) { AtomicInteger() }.incrementAndGet()
                    val bytes = if (id == "20") long else short
                    val start = request.getHeader("Range")?.substringAfter("bytes=")?.substringBefore('-')?.toIntOrNull() ?: 0
                    return MockResponse().setResponseCode(if (start > 0) 206 else 200)
                        .addHeader("Content-Type", "video/mp4").addHeader("Accept-Ranges", "bytes")
                        .apply { if (start > 0) addHeader("Content-Range", "bytes $start-${bytes.lastIndex}/${bytes.size}") }
                        .setBody(Buffer().write(bytes, start.coerceIn(0, bytes.size), (bytes.size - start).coerceAtLeast(0)))
                }
                val body = when (url.queryParameter("action")) {
                    null -> """{"user_info":{"auth":1,"status":"Active"}}"""
                    "get_live_categories", "get_vod_categories", "get_series_categories" -> "[]"
                    "get_live_streams", "get_vod_streams" -> "[]"
                    "get_series" -> """[{"series_id":1,"name":"Bölüm akışı denemesi","cover":"${base}art.jpg"}]"""
                    "get_series_info" -> JSONObject().put("info", JSONObject().put("plot", "Yerel bölüm sonu denemesi"))
                        .put("episodes", JSONObject().put("1", JSONArray().put(episode("10", "Sezon finali", 1, 1, base)))
                            .put("2", JSONArray().apply {
                                if (!removedNext) put(episode("20", "Yeni bir başlangıç · İkinci sezonun ilk bölümü", 2, 1, base))
                                put(episode("30", "Kütüphanedeki son bölüm", 2, 2, base))
                            })).toString()
                    else -> "[]"
                }
                return MockResponse().addHeader("Content-Type", "application/json").setBody(body)
            }
        }
        model = resetFixtureSources(ui)
        ui.waitUntil(10_000) { model.state.value.route == Route.SETUP }
        ui.runOnUiThread {
            if (!model.state.value.autoNextEpisode) model.autoNextEpisode()
            model.draft(SourceDraft("Bölümler", SourceType.XTREAM, base, "synthetic", "synthetic")); model.connectDraft()
        }
        ui.waitUntil(10_000) { model.state.value.route == Route.HOME }
    }
    private fun episode(id: String, title: String, season: Int, number: Int, base: String) = JSONObject()
        .put("id", id).put("title", title).put("season", season).put("episode_num", number).put("container_extension", "mp4")
        .put("info", JSONObject().put("movie_image", "${base}art.jpg"))
    @After fun cleanup() {
        if (::model.isInitialized) ui.runOnUiThread {
            model.back(); if (!model.state.value.autoNextEpisode) model.autoNextEpisode(); model.removeSource()
        }
        if (::server.isInitialized) server.shutdown()
    }
    private fun openSeries() {
        ui.runOnUiThread { model.open(model.state.value.catalog.items.first { it.kind == MediaKind.SERIES }) }
        ui.waitUntil(10_000) { !model.state.value.detailLoading && model.state.value.episodes.isNotEmpty() }
    }
    private fun start(index: Int = 0) {
        openSeries(); ui.runOnUiThread { model.play(model.state.value.episodes[index], restart = true) }
        ui.waitUntil(8_000) { exists("Duraklat") }
    }
    private fun click(text: String) = ui.onNode((hasText(text) or hasContentDescription(text)) and hasClickAction()).performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.OnClick) { it() }
    private fun exists(text: String) = ui.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
    private fun end() = ui.waitUntil(10_000) { ui.onAllNodesWithTag("episode-end").fetchSemanticsNodes().isNotEmpty() }
    private fun count(id: String) = requests[id]?.get() ?: 0
    private fun key(code: Int) { instrumentation.sendKeyDownUpSync(code); ui.waitForIdle() }
    private fun shot(name: String) {
        ui.waitForIdle()
        // The platform dialog window fade is not driven by Compose's test clock.
        SystemClock.sleep(300)
        val folder = ui.activity.filesDir.resolve("episode-preview").apply { mkdirs() }
        instrumentation.uiAutomation.takeScreenshot().let { bitmap ->
            folder.resolve("$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
        }
    }

    @Test fun countdownOpensOneNextSeasonEpisodeAndReturnsToItsCard() {
        start(); end(); val began = ui.mainClock.currentTime; val realBegan = SystemClock.elapsedRealtime()
        ui.onNodeWithText("Bu bölümde kal").assertIsFocused()
        assertTrue(model.progress(model.state.value.playing!!.entry)!!.completed)
        assertEquals(0, count("20")); shot("countdown")
        ui.waitUntil(13_000) { model.state.value.playing?.entry?.providerId == "20" && exists("Duraklat") }
        val virtualElapsed = ui.mainClock.currentTime - began
        val realElapsed = SystemClock.elapsedRealtime() - realBegan
        ui.activity.filesDir.resolve("episode-timing.json").writeText("""{"composeClockMs":$virtualElapsed,"wallClockMs":$realElapsed}""")
        assertTrue("Compose clock elapsed=$virtualElapsed; wall clock elapsed=$realElapsed", virtualElapsed >= 9_000)
        assertEquals(1, count("20")); assertEquals(0, count("30")); assertEquals(2, model.state.value.season)
        key(KeyEvent.KEYCODE_BACK); key(KeyEvent.KEYCODE_BACK)
        assertEquals(Route.DETAIL, model.state.value.route)
        ui.onNode(hasText("Yeni bir başlangıç", substring = true) and hasClickAction()).assertIsFocused()
    }

    @Test fun playingAndPausedEpisodesPreservePositionAcrossBackgroundAndExit() {
        start(index = 1) // The two-minute fixture, not the short automatic-transition fixture.
        val entry = model.state.value.playing!!.entry
        fun findPlayer(view: android.view.View): androidx.media3.common.Player? {
            if (view is androidx.media3.ui.PlayerView) return view.player
            if (view is android.view.ViewGroup) for (i in 0 until view.childCount) {
                findPlayer(view.getChildAt(i))?.let { return it }
            }
            return null
        }
        lateinit var player: androidx.media3.common.Player
        ui.runOnUiThread { player = findPlayer(ui.activity.window.decorView)!! }
        ui.waitUntil(10_000) {
            var ready = false
            ui.runOnUiThread { ready = player.isPlaying && player.currentPosition >= 1500 }
            ready
        }
        ui.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        var stoppedAt = 0L
        instrumentation.runOnMainSync {
            assertFalse("Background must stop playback", player.isPlaying)
            stoppedAt = player.currentPosition
            assertTrue("Lifecycle stop saves progress", model.progress(entry)!!.positionMs >= 1000)
        }
        val requestsAtStop = count("20")
        SystemClock.sleep(1200)
        instrumentation.runOnMainSync {
            assertTrue("Background position must not advance", kotlin.math.abs(player.currentPosition - stoppedAt) < 150)
        }
        assertEquals("Buffered fixture must not open another media request in background", requestsAtStop, count("20"))
        ui.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        ui.waitUntil(8000) {
            var resumed = false
            ui.runOnUiThread { resumed = player.isPlaying && player.currentPosition > stoppedAt + 300 }
            resumed
        }
        ui.runOnUiThread { assertSame(player, findPlayer(ui.activity.window.decorView)) }
        // Explicit pause must remain a pause after a second background/foreground cycle.
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        if (exists("Duraklat")) click("Duraklat")
        ui.runOnUiThread { assertFalse(player.playWhenReady) }
        ui.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        ui.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        ui.runOnUiThread { assertFalse("User pause survives return", player.playWhenReady) }
        ui.runOnUiThread { model.back() }
        ui.waitUntil(5000) { model.state.value.route == Route.DETAIL }
        ui.waitForIdle()
        assertTrue("Exit keeps the same episode progress", model.progress(entry)!!.positionMs >= stoppedAt)
        ui.onNode(hasText("Yeni bir başlangıç", substring = true) and hasClickAction()).assertIsFocused()
    }

    @Test fun backCancelsCountdownAndManualNextStartsFromBeginning() {
        start(); end()
        ui.runOnUiThread { model.store.saveProgress(model.state.value.source!!.id, "episode:20", 30_000, 120_000) }
        key(KeyEvent.KEYCODE_BACK)
        ui.onAllNodesWithTag("episode-end").assertCountEquals(0)
        ui.onNodeWithText("Baştan izle").assertIsFocused()
        SystemClock.sleep(10_500); assertEquals(0, count("20"))
        click("Sonraki bölüm")
        ui.waitUntil(8_000) { model.state.value.playing?.entry?.providerId == "20" && exists("Duraklat") }
        assertEquals(0L, model.state.value.playing!!.startMs)
    }

    @Test fun okOnInitialCancelPreventsTransitionAndReplayCanFinishAgain() {
        start(); end(); key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.onAllNodesWithTag("episode-end").assertCountEquals(0)
        click("Baştan izle"); end()
        assertEquals(0, count("20")); ui.onNodeWithTag("episode-countdown").assertExists()
        click("Bu bölümde kal")
    }

    @Test fun disabledAutoplayLeavesAManualEndCard() {
        ui.runOnUiThread { model.autoNextEpisode() }
        start(); end(); ui.onAllNodesWithTag("episode-countdown").assertCountEquals(0)
        SystemClock.sleep(10_500); assertEquals(0, count("20"))
        click("Şimdi oynat")
        ui.waitUntil(8_000) { model.state.value.playing?.entry?.providerId == "20" && exists("Duraklat") }
    }

    @Test fun finalEpisodeHasNoInventedNextAndCanReturnToTheEpisodeList() {
        start(2); end(); ui.onAllNodesWithTag("episode-countdown").assertCountEquals(0)
        ui.onAllNodesWithText("Şimdi oynat").assertCountEquals(0); shot("final")
        click("Bölümlere dön")
        assertEquals(Route.DETAIL, model.state.value.route)
        ui.onNode(hasText("Kütüphanedeki son bölüm") and hasClickAction()).assertIsFocused()
        assertEquals(0, count("20"))
    }

    @Test fun backgroundStopsCountdownAndReturnRequiresManualStart() {
        start(); end(); ui.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        SystemClock.sleep(10_500); assertEquals(0, count("20"))
        ui.activityRule.scenario.moveToState(Lifecycle.State.RESUMED); end()
        ui.onAllNodesWithTag("episode-countdown").assertCountEquals(0)
        ui.onNodeWithText("Otomatik geçiş duraklatıldı.").assertExists()
        click("Şimdi oynat")
        ui.waitUntil(8_000) { model.state.value.playing?.entry?.providerId == "20" && exists("Duraklat") }
    }

    @Test fun changingEpisodeListCancelsAnAlreadyVisibleCountdown() {
        start(); end(); removedNext = true
        ui.runOnUiThread { model.retryDetail() }
        ui.waitUntil(8_000) { !model.state.value.detailLoading && model.state.value.episodes.size == 2 }
        ui.waitUntil(5_000) { exists("Bölüm listesi değişti. Devam etmek için listeden bir bölüm seç.") }
        ui.onAllNodesWithTag("episode-countdown").assertCountEquals(0)
        SystemClock.sleep(10_500); assertEquals(0, count("20")); assertEquals(0, count("30"))
    }

    @Test fun tracksPanelYieldsToEndPromptWithoutLosingCancelFocus() {
        start(); click("Ses / Altyazı"); end()
        ui.onAllNodesWithText("Ses ve altyazı").assertCountEquals(0)
        ui.onNodeWithText("Bu bölümde kal").assertIsFocused()
        key(KeyEvent.KEYCODE_MEDIA_PAUSE)
        ui.onAllNodesWithTag("episode-end").assertCountEquals(0)
    }

    @Test fun m3uSequenceUsesParsedNumbersAndLabelsTheInference() {
        ui.runOnUiThread { model.removeSource() }
        ui.waitUntil(8_000) { model.state.value.route == Route.SETUP }
        val address = server.url("/list.m3u").toString()
        ui.runOnUiThread { model.draft(SourceDraft("M3U denemesi", SourceType.M3U, address)); model.connectDraft() }
        ui.waitUntil(10_000) { model.state.value.route == Route.HOME }
        start(); end()
        ui.onNodeWithText("Bölüm sırası listedeki başlıklardan eşleştirildi.").assertExists()
        ui.onNodeWithTag("episode-countdown").assertExists()
        click("Şimdi oynat"); ui.waitUntil(8_000) { count("20") > 0 && exists("Duraklat") }
    }

    @Test fun autoplaySettingTogglesWithRemoteAndSurvivesRecreation() {
        click("Ayarlar")
        // The player's section holds it.
        ui.onNode(hasText("Oynatıcı") and hasClickAction() and hasAnyAncestor(hasTestTag("settings-panel")))
            .performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.OnClick) { it() }
        ui.onNode(hasText("Otomatik sonraki bölüm") and hasClickAction()).performScrollTo()
            .performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.RequestFocus) { it() }
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.onNode(hasText("Otomatik sonraki bölüm") and hasClickAction()).assertIsFocused().assertIsOff()
        val row = ui.onNode(hasText("Otomatik sonraki bölüm") and hasClickAction()).getUnclippedBoundsInRoot()
        val viewport = ui.onNodeWithTag("settings-viewport").getUnclippedBoundsInRoot()
        assertTrue("Focused setting including its full explanation must fit: $row / $viewport",
            row.top >= viewport.top && row.bottom <= viewport.bottom)
        assertFalse(model.state.value.autoNextEpisode); shot("setting")
        key(KeyEvent.KEYCODE_DPAD_DOWN); key(KeyEvent.KEYCODE_DPAD_UP)
        ui.onNode(hasText("Otomatik sonraki bölüm") and hasClickAction()).assertIsFocused()
        ui.activityRule.scenario.recreate(); ui.waitForIdle()
        ui.runOnUiThread { model = ViewModelProvider(ui.activity)[TvViewModel::class.java] }
        ui.waitUntil(8_000) { model.state.value.route == Route.HOME }
        assertFalse(model.state.value.autoNextEpisode)
    }
}
