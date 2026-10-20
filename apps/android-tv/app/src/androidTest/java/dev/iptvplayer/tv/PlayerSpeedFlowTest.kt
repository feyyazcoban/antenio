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
import dev.iptvplayer.tv.ui.PlaybackSpeed
import okhttp3.mockwebserver.*
import okio.Buffer
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlayerSpeedFlowTest {
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
                    "#EXTM3U\n#EXTINF:-1,Hız filmi\n${server.url("/movie/one.mp4")}\n" +
                    "#EXTINF:-1,Deneme kanalı\n${server.url("/live/one.ts")}\n")
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
            model.draft(SourceDraft("Hız denemesi", SourceType.M3U, address))
            model.connectDraft()
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
    private fun open(tunneling: Boolean = false) {
        ui.runOnUiThread {
            model.playback { it.copy(tunneling = tunneling) }
            model.play(model.state.value.catalog.items.first { it.kind == MediaKind.MOVIE })
        }
        ui.waitUntil(20_000) { ui.onAllNodesWithContentDescription("Duraklat").fetchSemanticsNodes().isNotEmpty() }
        key(KeyEvent.KEYCODE_MEDIA_PAUSE)
        ui.onNodeWithTag("player-options").performSemanticsAction(SemanticsActions.RequestFocus)
        key(KeyEvent.KEYCODE_DPAD_CENTER)
    }
    private fun waitFocus(tag: String) {
        ui.waitUntil(5_000) { ui.onAllNodes(hasTestTag(tag) and isFocused()).fetchSemanticsNodes().isNotEmpty() }
    }
    private fun picker(current: Float) {
        waitFocus("player-speed-open"); key(KeyEvent.KEYCODE_DPAD_CENTER); waitFocus("player-speed:$current")
    }
    private fun assertSpeed(value: Float) = ui.runOnIdle {
        assertEquals(value, engine().playbackParameters.speed, .001f)
        assertEquals(1f, engine().playbackParameters.pitch, 0f)
        assertFalse(engine().isPlaying)
    }
    private fun shot() {
        Thread.sleep(400)
        instrumentation.uiAutomation.takeScreenshot().let { image ->
            val scale = ui.activity.resources.configuration.fontScale
            val dir = ui.activity.filesDir.resolve("speed-preview").apply { mkdirs() }
            dir.resolve("speed-$scale.png").outputStream().use { image.compress(Bitmap.CompressFormat.PNG, 100, it) }
            image.recycle()
        }
    }
    @Test fun everySpeedAppliesWithPitchPreservedAndBackReturnsToTheOpener() {
        open(); picker(1f)
        key(KeyEvent.KEYCODE_DPAD_DOWN)
        ui.onNodeWithTag("player-speed:1.25").assertIsFocused()
        assertSpeed(1f)
        key(KeyEvent.KEYCODE_BACK); picker(1f) // Back cancels without applying the focused choice.
        var current = 1f
        for (value in PlaybackSpeed.choices) {
            val delta = PlaybackSpeed.choices.indexOf(value) - PlaybackSpeed.choices.indexOf(current)
            repeat(kotlin.math.abs(delta)) { key(if (delta > 0) KeyEvent.KEYCODE_DPAD_DOWN else KeyEvent.KEYCODE_DPAD_UP) }
            val row = ui.onNodeWithTag("player-speed:$value").assertIsFocused()
            val bounds = row.getUnclippedBoundsInRoot()
            val viewport = ui.onNodeWithTag("player-options-viewport").getUnclippedBoundsInRoot()
            assertTrue("Focused speed row and ring must be fully visible", bounds.top >= viewport.top + 8.dp && bounds.bottom <= viewport.bottom - 8.dp)
            if (value == 2f) shot()
            key(KeyEvent.KEYCODE_DPAD_CENTER); waitFocus("player-speed-open"); assertSpeed(value)
            current = value
            if (value != PlaybackSpeed.choices.last()) picker(current)
        }
        key(KeyEvent.KEYCODE_BACK); waitFocus("player-options")
        key(KeyEvent.KEYCODE_MEDIA_PLAY)
        ui.waitUntil(5_000) { ui.onAllNodesWithContentDescription("Duraklat").fetchSemanticsNodes().isNotEmpty() }
        ui.runOnIdle { assertEquals(2f, engine().playbackParameters.speed, .001f) }
        // A reported parameter alone is insufficient: verify it survives audio output startup
        // and that media time really advances at the selected rate.
        Thread.sleep(1200)
        var before = 0L
        ui.runOnIdle { before = engine().currentPosition; assertEquals(2f, engine().playbackParameters.speed, .001f) }
        val started = android.os.SystemClock.elapsedRealtime()
        Thread.sleep(2000)
        ui.runOnIdle {
            val elapsed = android.os.SystemClock.elapsedRealtime() - started
            val ratio = (engine().currentPosition - before).toDouble() / elapsed
            assertEquals(2f, engine().playbackParameters.speed, .001f)
            assertTrue("2x must advance actual media time: $ratio", ratio in 1.6..2.4)
        }
        key(KeyEvent.KEYCODE_BACK); ui.onNodeWithTag("player-panel").assertDoesNotExist()
        key(KeyEvent.KEYCODE_BACK)
        ui.waitUntil(5_000) { model.state.value.route != Route.PLAYER }
        open(); assertSpeed(1f)
    }
    @Test fun liveResetsSpeedAndOffersNoSpeedOption() {
        open(); picker(1f)
        repeat(4) { key(KeyEvent.KEYCODE_DPAD_DOWN) }; key(KeyEvent.KEYCODE_DPAD_CENTER)
        assertSpeed(2f)
        ui.runOnUiThread { model.play(model.state.value.catalog.items.first { it.kind == MediaKind.LIVE }) }
        ui.waitUntil(15_000) { ui.onAllNodesWithContentDescription("Duraklat").fetchSemanticsNodes().isNotEmpty() }
        ui.onNodeWithTag("player-options").assertDoesNotExist()
        ui.runOnIdle { assertEquals(1f, engine().playbackParameters.speed, 0f) }
    }
    @Test fun tunnelingExplainsWhySpeedIsUnavailableAndBackStillWorks() {
        open(tunneling = true)
        waitFocus("player-options-back")
        ui.onNodeWithTag("player-speed-open").assertIsNotEnabled()
        ui.onNodeWithText("Hızı değiştirmek için", substring = true).assertExists()
        key(KeyEvent.KEYCODE_BACK); waitFocus("player-options"); assertSpeed(1f)
    }
}
