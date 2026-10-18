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
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicInteger

/** Emulator-only network failures. No provider, account or parallel real stream. */
@RunWith(AndroidJUnit4::class)
class PlaybackRecoveryFlowTest {
    @get:Rule(order = 0) val turkish = TurkishLocaleRule()
    @get:Rule(order = 1) val ui = createAndroidComposeRule<MainActivity>()
    private lateinit var server: MockWebServer
    private lateinit var model: TvViewModel
    private val firstRequests = AtomicInteger()
    private val secondRequests = AtomicInteger()
    private val hlsRequests = AtomicInteger()
    @Volatile private var mode = "temporary"
    @Volatile private var firstTitle = "Kurtarma filmi"
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()

    @Before fun setup() {
        Assume.assumeTrue(android.os.Build.HARDWARE.contains("ranchu") || android.os.Build.FINGERPRINT.contains("generic"))
        val bytes = instrumentation.context.assets.open("playback-fixture.mp4").use { it.readBytes() }
        val segment = instrumentation.context.assets.open("recovery-fixture.ts").use { it.readBytes() }
        server = MockWebServer(); server.start()
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val path = request.requestUrl!!.encodedPath
                if (path == "/list.m3u") return MockResponse().setBody(
                    "#EXTM3U\n#EXTINF:-1,$firstTitle\n${server.url("/movie/one.mp4")}\n#EXTINF:-1,Diğer film\n${server.url("/movie/two.mp4")}\n#EXTINF:-1,HLS denemesi\n${server.url("/movie/three.m3u8")}\n")
                if (path.endsWith("three.m3u8")) return MockResponse().addHeader("Content-Type", "application/vnd.apple.mpegurl")
                    .setBody("#EXTM3U\n#EXT-X-VERSION:3\n#EXT-X-TARGETDURATION:4\n#EXT-X-MEDIA-SEQUENCE:0\n#EXT-X-PLAYLIST-TYPE:VOD\n#EXTINF:4.0,\nsegment.ts\n#EXT-X-ENDLIST\n")
                if (path.endsWith("segment.ts")) return if (hlsRequests.incrementAndGet() == 1) MockResponse().setResponseCode(503)
                    else MockResponse().addHeader("Content-Type", "video/mp2t").setBody(Buffer().write(segment))
                val count = if (path.endsWith("one.mp4")) firstRequests.incrementAndGet() else secondRequests.incrementAndGet()
                if (path.endsWith("one.mp4")) when (mode) {
                    "temporary" -> if (count == 1) return MockResponse().setResponseCode(503)
                    "unavailable" -> return MockResponse().setResponseCode(503)
                    "auth" -> return MockResponse().setResponseCode(403)
                    "limit" -> return MockResponse().setResponseCode(429)
                    "malformed" -> return MockResponse().addHeader("Content-Type", "video/mp4").setBody("not a media container")
                    "silent" -> return if (count == 1) MockResponse().setResponseCode(503) else MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE)
                }
                val start = request.getHeader("Range")?.substringAfter("bytes=")?.substringBefore('-')?.toIntOrNull() ?: 0
                return MockResponse().setResponseCode(if (start > 0) 206 else 200)
                    .addHeader("Content-Type", "video/mp4").addHeader("Accept-Ranges", "bytes")
                    .apply { if (start > 0) addHeader("Content-Range", "bytes $start-${bytes.lastIndex}/${bytes.size}") }
                    .setBody(Buffer().write(bytes, start.coerceIn(0, bytes.size), (bytes.size - start).coerceAtLeast(0)))
            }
        }
        model = resetFixtureSources(ui)
        ui.waitUntil(10_000) { model.state.value.route == Route.SETUP }
        val address = server.url("/list.m3u").toString()
        ui.runOnUiThread {
            model.draft(SourceDraft("Bağlantı denemesi", SourceType.M3U, address))
            model.connectDraft()
        }
        ui.waitUntil(10_000) { model.state.value.route == Route.HOME }
    }

    @After fun cleanup() {
        if (::model.isInitialized) ui.runOnUiThread { model.back(); model.removeSource() }
        if (::server.isInitialized) server.shutdown()
    }

    private fun start(savedPosition: Long = 0) = ui.runOnUiThread {
        val entry = model.state.value.catalog.items.first()
        if (savedPosition > 0) model.store.saveProgress(model.state.value.source!!.id, entry.withoutSource().id, savedPosition, 120_000)
        model.play(entry)
    }
    private fun click(text: String) = ui.onNode((hasText(text) or hasContentDescription(text)) and hasClickAction()).performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.OnClick) { it() }
    private fun exists(text: String, part: Boolean = false) = ui.onAllNodes(hasText(text, substring = part) or hasContentDescription(text, substring = part)).fetchSemanticsNodes().isNotEmpty()
    private fun waiting() = ui.waitUntil(10_000) { exists("İptal et") }
    private fun ready() = ui.waitUntil(15_000) { exists("Duraklat") }
    private fun back() { instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK); ui.waitForIdle() }
    private fun shot(name: String) {
        ui.waitForIdle()
        val folder = ui.activity.filesDir.resolve("recovery-preview").apply { mkdirs() }
        instrumentation.uiAutomation.takeScreenshot().let { bitmap ->
            folder.resolve("$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
        }
    }

    @Test fun longTitleRecoveryAndFailureKeepControlsInsideScreen() {
        mode = "unavailable"
        // The long title comes from the list: the model plays the catalogue's entry, never a copy handed to it.
        firstTitle = "İstanbul’dan dünyaya uzanan çok uzun bir yolculuk: unutulmaz hikâyeler ve beklenmedik karşılaşmalar"
        ui.runOnUiThread { model.refresh() }
        ui.waitUntil(10_000) { !model.state.value.refreshing && model.state.value.catalog.items.first().title == firstTitle }
        ui.runOnUiThread { model.play(model.state.value.catalog.items.first()) }
        fun assertPanelFits() {
            val panel = ui.onNodeWithTag("player-panel").getUnclippedBoundsInRoot()
            val viewport = ui.onRoot().getUnclippedBoundsInRoot()
            val title = ui.onNodeWithTag("player-title", useUnmergedTree = true).getUnclippedBoundsInRoot()
            val controls = ui.onNodeWithTag("player-controls").getUnclippedBoundsInRoot()
            assertTrue("Long title and recovery status must not push the panel off screen: $panel / $viewport",
                panel.top >= viewport.top && panel.bottom <= viewport.bottom)
            assertTrue("Title must precede visible actions", title.top >= panel.top && title.bottom < controls.top && controls.bottom <= panel.bottom)
            ui.onNode(isFocused() and hasAnyAncestor(hasTestTag("player-controls"))).assertExists()
        }
        waiting(); assertPanelFits(); shot("long-recovering")
        ui.waitUntil(12_000) { exists("Tekrar dene") }
        ui.onNodeWithTag("player-primary").assertIsFocused()
        assertPanelFits(); shot("long-failed")
        mode = "success"; click("Tekrar dene"); ready(); click("Duraklat")
        assertPanelFits(); shot("long-ready")
        back(); back()
        assertEquals(Route.HOME, model.state.value.route)
    }

    @Test fun temporaryServerFailureRecoversAtSavedVodPosition() {
        start(savedPosition = 12_000); ready()
        assertTrue(firstRequests.get() >= 2)
        ui.waitUntil(10_000) { (model.progress(model.state.value.playing!!.entry)?.positionMs ?: 0) >= 12_000 }
        assertFalse(exists("Tekrar dene")); assertFalse(exists("İptal et"))
    }

    @Test fun repeatedServerFailureStopsAfterTwoRetriesAndKeepsRetryFocused() {
        mode = "unavailable"; start(); waiting(); shot("waiting")
        ui.waitUntil(12_000) { exists("Tekrar dene") }
        ui.onNodeWithTag("player-primary").assertIsFocused()
        assertEquals(3, firstRequests.get())
        SystemClock.sleep(1500); assertEquals(3, firstRequests.get()); shot("failed")
    }

    @Test fun deniedAccessStopsAndManualRetryCanRecover() {
        mode = "auth"; start()
        ui.waitUntil(8_000) { exists("Tekrar dene") }
        ui.onNodeWithText("Bu yayına erişim verilmedi.", substring = true).assertExists()
        SystemClock.sleep(1500); assertEquals(1, firstRequests.get())
        mode = "success"; click("Tekrar dene"); ready(); assertTrue(firstRequests.get() >= 2)
    }

    @Test fun rateLimitDoesNotAddAutomaticTraffic() {
        mode = "limit"; start()
        ui.waitUntil(8_000) { exists("Tekrar dene") }
        ui.onNodeWithText("Sağlayıcının bağlantı sınırına ulaşıldı.", substring = true).assertExists()
        SystemClock.sleep(1500); assertEquals(1, firstRequests.get())
    }

    @Test fun malformedMediaDoesNotLoop() {
        mode = "malformed"; start()
        ui.waitUntil(8_000) { exists("Tekrar dene") }
        ui.onNodeWithText("Yayının biçimi okunamadı.", substring = true).assertExists()
        SystemClock.sleep(1500); assertEquals(1, firstRequests.get())
    }

    @Test fun cancelStopsFurtherRequestsAndBackReturnsToLibrary() {
        mode = "unavailable"; start(); waiting(); click("İptal et")
        ui.onNodeWithText("Yeniden bağlanma durduruldu.", substring = true).assertExists()
        val stoppedAt = firstRequests.get()
        SystemClock.sleep(3500); assertEquals(stoppedAt, firstRequests.get())
        back(); assertEquals(Route.HOME, model.state.value.route)
    }

    @Test fun backDuringRecoveryDisposesItsPendingRequest() {
        mode = "unavailable"; start(); waiting(); back()
        assertEquals(Route.HOME, model.state.value.route)
        val stoppedAt = firstRequests.get(); SystemClock.sleep(3500)
        assertEquals(stoppedAt, firstRequests.get())
    }

    @Test fun backgroundCancelsRecoveryUntilExplicitRetry() {
        mode = "unavailable"; start(); waiting()
        ui.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        val stoppedAt = firstRequests.get(); SystemClock.sleep(3500)
        assertEquals(stoppedAt, firstRequests.get())
        ui.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        ui.waitUntil(8_000) { exists("Tekrar dene") }
        SystemClock.sleep(1500); assertEquals(stoppedAt, firstRequests.get())
        mode = "success"; click("Tekrar dene"); ready()
    }

    @Test fun changingMediaCancelsOldRecovery() {
        mode = "unavailable"; start(); waiting()
        ui.runOnUiThread { model.play(model.state.value.catalog.items[1]) }
        ready(); val stoppedAt = firstRequests.get(); SystemClock.sleep(3500)
        assertEquals(stoppedAt, firstRequests.get()); assertTrue(secondRequests.get() > 0)
        assertEquals("Diğer film", model.state.value.playing?.entry?.title)
    }

    @Test fun silentServerStopsWithinTheRecoveryDeadline() {
        mode = "silent"; start(); waiting()
        val began = SystemClock.elapsedRealtime()
        ui.waitUntil(24_000) { exists("Tekrar dene") }
        assertTrue(SystemClock.elapsedRealtime() - began < 23_000)
        assertTrue(firstRequests.get() in 2..3)
        // Socket timeout and the overall deadline may finish on either side of the same boundary.
        assertTrue(exists("Yayın bağlantısı zamanında kurulamadı.", true) || exists("Yayına bağlanılamadı.", true))
        ui.onNodeWithTag("player-primary").assertIsFocused()
    }

    @Test fun hlsSegmentFailureRecoversThroughTheSameBudget() {
        ui.runOnUiThread { model.play(model.state.value.catalog.items[2]) }
        ready(); assertEquals(2, hlsRequests.get())
        assertFalse(exists("Tekrar dene")); assertFalse(exists("İptal et"))
    }
}
