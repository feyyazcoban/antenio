@file:androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
package dev.iptvplayer.tv

import android.graphics.Bitmap
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.iptvplayer.tv.data.*
import okhttp3.mockwebserver.*
import okio.Buffer
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicInteger

/** Synthetic source only. Run at 1.0/1.5/2.0 through tools/run-device-tests.sh on 5558. */
@RunWith(AndroidJUnit4::class)
class PlayerControlsLayoutTest {
    @get:Rule(order = 0) val turkish = TurkishLocaleRule()
    @get:Rule(order = 1) val ui = createAndroidComposeRule<MainActivity>()
    private lateinit var model: TvViewModel
    private lateinit var server: MockWebServer
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    @Before fun setup() {
        val bytes = instrumentation.context.assets.open("playback-fixture.mp4").use { it.readBytes() }
        server = MockWebServer()
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                if (request.requestUrl!!.encodedPath == "/list.m3u") return MockResponse().setBody(
                    "#EXTM3U\n#EXTINF:-1,Uzak Şehirler: İstanbul’dan dünyaya uzanan unutulmaz yolculuk ve beklenmedik karşılaşmalar\n${server.url("/movie/one.mp4")}\n" +
                    "#EXTINF:-1,Deneme Haber Kanalı\n${server.url("/live/one.ts")}\n")
                val start = request.getHeader("Range")?.substringAfter("bytes=")?.substringBefore('-')?.toIntOrNull()?.coerceIn(0, bytes.size) ?: 0
                return MockResponse().setResponseCode(if (start > 0) 206 else 200)
                    .addHeader("Content-Type", "video/mp4").addHeader("Accept-Ranges", "bytes")
                    .apply { if (start > 0) addHeader("Content-Range", "bytes $start-${bytes.lastIndex}/${bytes.size}") }
                    .setBody(Buffer().write(bytes, start, bytes.size - start))
            }
        }
        server.start()
        model = resetFixtureSources(ui)
        val address = server.url("/list.m3u").toString()
        ui.runOnUiThread {
            model.draft(SourceDraft("Kontrol denemesi", SourceType.M3U, address)); model.connectDraft()
            model.playback { it.copy(osdTimeout = OsdTimeout.NEVER) }
        }
        ui.waitUntil(20_000) { model.state.value.route == Route.HOME }
    }
    @After fun cleanup() {
        if (::model.isInitialized) ui.runOnUiThread { if (model.state.value.route == Route.PLAYER) model.back(); model.removeSource() }
        if (::server.isInitialized) server.shutdown()
    }
    private fun key(code: Int) { instrumentation.sendKeyDownUpSync(code); ui.waitForIdle() }
    private fun playerView(v: View): PlayerView? = if (v is PlayerView) v else
        (v as? ViewGroup)?.let { group -> (0 until group.childCount).firstNotNullOfOrNull { playerView(group.getChildAt(it)) } }
    private fun engine(): Player = playerView(ui.activity.window.decorView)!!.player!!
    private fun open(live: Boolean = false) {
        ui.runOnUiThread { model.play(model.state.value.catalog.items.first { it.kind == if (live) MediaKind.LIVE else MediaKind.MOVIE }) }
        ui.waitUntil(20_000) { ui.onAllNodesWithContentDescription("Duraklat").fetchSemanticsNodes().isNotEmpty() }
        key(KeyEvent.KEYCODE_MEDIA_PAUSE)
        ui.onNodeWithTag("player-primary").assertIsFocused()
        if (!live) {
            ui.runOnUiThread { engine().seekTo(20_000) }
            ui.waitUntil(5_000) { ui.onAllNodes(hasTestTag("player-scrub") and isFocusable()).fetchSemanticsNodes().isNotEmpty() }
        }
    }
    private fun focus(tag: String) { ui.onNodeWithTag(tag).performSemanticsAction(SemanticsActions.RequestFocus); ui.waitForIdle() }
    private fun bounds(tag: String) = ui.onNodeWithTag(tag, useUnmergedTree = true).getUnclippedBoundsInRoot()
    private fun inScreen(tag: String, ring: Boolean = false) {
        val box = bounds(tag); val screen = ui.onRoot().getUnclippedBoundsInRoot(); val pad = if (ring) 5.dp else 0.dp
        assertTrue("$tag must fit: $box / $screen", box.left >= screen.left + pad && box.right <= screen.right - pad &&
            box.top >= screen.top + pad && box.bottom <= screen.bottom - pad)
    }
    private fun shot(name: String) {
        // Compose can be idle before the emulator's renderer presents every overlay layer.
        ui.waitForIdle(); Thread.sleep(350); ui.waitForIdle()
        val directory = ui.activity.filesDir.resolve("controls-preview").apply { mkdirs() }
        instrumentation.uiAutomation.takeScreenshot().let { image ->
            directory.resolve("$name-${ui.activity.resources.configuration.fontScale}.png").outputStream().use { image.compress(Bitmap.CompressFormat.PNG, 100, it) }
            image.recycle()
        }
    }
    @Test fun movieFocusGeometryAndPanelsReturnToTheirOpeningControl() {
        open()
        listOf("player-panel", "player-title", "player-controls", "player-clock").forEach { inScreen(it) }
        assertTrue(bounds("player-title").bottom < bounds("player-scrub").top)
        assertTrue(bounds("player-scrub").bottom < bounds("player-controls").top)
        assertTrue(bounds("player-tracks").left - bounds("player-seek-forward").right >= 32.dp)
        val order = listOf("player-seek-back", "player-primary", "player-seek-forward", "player-tracks", "player-options", "player-favorite")
        focus(order.first())
        order.forEachIndexed { index, tag ->
            if (index > 0) key(KeyEvent.KEYCODE_DPAD_RIGHT)
            ui.onNodeWithTag(tag).assertIsFocused(); inScreen(tag, ring = true)
            if (tag == "player-seek-back" || tag == "player-favorite") { inScreen("player-focus-label"); shot(tag) }
        }
        key(KeyEvent.KEYCODE_DPAD_RIGHT); ui.onNodeWithTag("player-favorite").assertIsFocused()
        key(KeyEvent.KEYCODE_DPAD_UP); ui.onNodeWithTag("player-scrub").assertIsFocused(); inScreen("player-scrub-bubble"); shot("scrub")
        key(KeyEvent.KEYCODE_DPAD_DOWN); ui.onNodeWithTag("player-favorite").assertIsFocused()
        focus("player-tracks"); key(KeyEvent.KEYCODE_DPAD_CENTER); key(KeyEvent.KEYCODE_BACK)
        ui.onNodeWithTag("player-tracks").assertIsFocused()
        focus("player-options"); key(KeyEvent.KEYCODE_DPAD_CENTER); key(KeyEvent.KEYCODE_BACK)
        ui.onNodeWithTag("player-options").assertIsFocused(); shot("movie")
    }
    @Test fun pendingTargetCommitsOnceAndBackCancelsBeforeHidingOrLeaving() {
        open(); key(KeyEvent.KEYCODE_DPAD_UP)
        ui.onNodeWithTag("player-scrub").assertIsFocused()
        val seeks = AtomicInteger()
        ui.runOnUiThread { engine().addListener(object : Player.Listener {
            override fun onPositionDiscontinuity(oldPosition: Player.PositionInfo, newPosition: Player.PositionInfo, reason: Int) {
                if (reason == Player.DISCONTINUITY_REASON_SEEK) seeks.incrementAndGet()
            }
        }) }
        repeat(3) { instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT) }
        ui.waitUntil(5_000) { seeks.get() == 1 }
        ui.runOnIdle { assertEquals(50_000L, engine().currentPosition); assertFalse(engine().isPlaying) }
        assertEquals(1, seeks.get())
        // Seeking buffers briefly. Do not send a new target until the player and its enabled controls are ready.
        ui.waitUntil(5_000) { var ready = false; instrumentation.runOnMainSync { ready = engine().playbackState == Player.STATE_READY }; ready }
        ui.waitForIdle()
        // Keep Compose's test clock from auto-advancing the one-second timer between these two key injections.
        ui.mainClock.autoAdvance = false
        try {
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT)
            ui.mainClock.advanceTimeByFrame()
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
            ui.mainClock.advanceTimeByFrame()
        } finally { ui.mainClock.autoAdvance = true }
        ui.waitForIdle()
        ui.onNodeWithTag("player-scrub").assertIsFocused()
        Thread.sleep(1200); assertEquals(1, seeks.get())
        key(KeyEvent.KEYCODE_BACK); ui.onNodeWithTag("player-panel").assertDoesNotExist()
        assertEquals(Route.PLAYER, model.state.value.route)
        key(KeyEvent.KEYCODE_BACK); assertNotEquals(Route.PLAYER, model.state.value.route)
    }
    @Test fun mediaSeekCommitsBeforePlayAndHiddenSeekShowsOnlyThePreview() {
        open()
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_MEDIA_FAST_FORWARD)
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE); ui.waitForIdle()
        ui.runOnIdle { assertTrue(engine().currentPosition >= 50_000); assertTrue(engine().playWhenReady) }
        ui.waitUntil(5_000) { var running = false; instrumentation.runOnMainSync { running = engine().isPlaying }; running }
        ui.mainClock.autoAdvance = false
        try {
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_MEDIA_FAST_FORWARD)
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
            ui.mainClock.advanceTimeByFrame()
        } finally { ui.mainClock.autoAdvance = true }
        ui.runOnIdle { assertTrue(engine().currentPosition >= 80_000); assertFalse("Seek buffering must not turn Pause into Play", engine().playWhenReady) }
        ui.runOnUiThread { engine().play() }
        ui.waitUntil(5_000) { var running = false; instrumentation.runOnMainSync { running = engine().isPlaying }; running }
        focus("player-primary")
        ui.mainClock.autoAdvance = false
        try {
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_MEDIA_REWIND)
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER)
            ui.mainClock.advanceTimeByFrame()
        } finally { ui.mainClock.autoAdvance = true }
        ui.runOnIdle { assertTrue(engine().currentPosition < 80_000); assertFalse("The focused Pause button must also preserve the pre-seek intent", engine().playWhenReady) }
        key(KeyEvent.KEYCODE_MEDIA_PAUSE)
        key(KeyEvent.KEYCODE_BACK); key(KeyEvent.KEYCODE_DPAD_RIGHT)
        ui.onNodeWithTag("player-panel").assertDoesNotExist(); ui.onNodeWithTag("player-peek").assertExists()
        inScreen("player-scrub-bubble"); shot("peek")
        ui.waitUntil(5_000) { ui.onAllNodesWithTag("player-peek").fetchSemanticsNodes().isEmpty() }
    }
    @Test fun okDownAndAccessibleProgressApplyTargetsWithoutChangingThePausedState() {
        open()
        focus("player-seek-forward"); key(KeyEvent.KEYCODE_DPAD_UP)
        ui.mainClock.autoAdvance = false
        try {
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT)
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER)
            ui.mainClock.advanceTimeByFrame()
        } finally { ui.mainClock.autoAdvance = true }
        ui.runOnIdle { assertEquals(30_000L, engine().currentPosition); assertFalse(engine().playWhenReady) }
        ui.waitUntil(5_000) { var ready = false; instrumentation.runOnMainSync { ready = engine().playbackState == Player.STATE_READY }; ready }
        ui.waitForIdle()
        ui.mainClock.autoAdvance = false
        try {
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT)
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN)
            ui.mainClock.advanceTimeByFrame()
        } finally { ui.mainClock.autoAdvance = true }
        ui.onNodeWithTag("player-seek-forward").assertIsFocused()
        ui.runOnIdle { assertEquals(40_000L, engine().currentPosition); assertFalse(engine().playWhenReady) }
        ui.waitUntil(5_000) { var ready = false; instrumentation.runOnMainSync { ready = engine().playbackState == Player.STATE_READY }; ready }
        ui.waitForIdle()
        ui.onNodeWithTag("player-scrub").performSemanticsAction(SemanticsActions.SetProgress) { assertTrue(it(60_000f)) }
        ui.runOnIdle { assertEquals(60_000L, engine().currentPosition); assertFalse(engine().playWhenReady) }
    }
    @Test fun liveKeepsChannelActionsOutsideTheTransportClusterAndDoesNotOfferScrubbing() {
        open(live = true)
        inScreen("player-panel"); inScreen("player-live-badge"); shot("live")
        ui.onNodeWithTag("player-seek-back").assertDoesNotExist(); ui.onNodeWithTag("player-seek-forward").assertDoesNotExist()
        ui.onNodeWithTag("player-scrub").assertDoesNotExist()
        key(KeyEvent.KEYCODE_DPAD_UP); ui.onNodeWithTag("player-primary").assertIsFocused()
        assertTrue(bounds("player-channel-down").left - bounds("player-primary").right >= 32.dp)
        listOf("player-channel-down", "player-channel-up", "player-tracks", "player-favorite").forEach {
            key(KeyEvent.KEYCODE_DPAD_RIGHT); ui.onNodeWithTag(it).assertIsFocused(); inScreen(it, ring = true)
        }
    }
}
