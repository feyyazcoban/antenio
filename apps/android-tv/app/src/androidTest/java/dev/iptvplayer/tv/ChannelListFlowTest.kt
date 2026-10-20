@file:androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
package dev.iptvplayer.tv

import android.graphics.Bitmap
import android.os.SystemClock
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.iptvplayer.tv.data.*
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.*
import okio.Buffer
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/** Only tools/run-device-tests.sh / emulator-5558. All addresses and media are a local synthetic fixture. */
@RunWith(AndroidJUnit4::class)
class ChannelListFlowTest {
    @get:Rule(order = 0) val turkish = TurkishLocaleRule()
    @get:Rule(order = 1) val ui = createAndroidComposeRule<MainActivity>()
    private lateinit var model: TvViewModel
    private lateinit var server: MockWebServer
    private lateinit var source: SourceConfig
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val opens = ConcurrentHashMap<String, AtomicInteger>()
    private var base = 0L
    private fun id(n: Int) = ContentRef(source.id, "live:$n").key
    private fun time(t: Long) = Instant.ofEpochMilli(t).atOffset(ZoneOffset.UTC)
        .format(DateTimeFormatter.ofPattern("uuuuMMddHHmmss xx", Locale.ROOT))
    private fun xml() = buildString {
        append("<tv>")
        repeat(18) { n -> append("<channel id='c$n'><display-name>Kanal $n</display-name></channel>") }
        repeat(18) { n -> repeat(10) { i ->
            val start = base - 30 * 60_000 + i * 3_600_000
            append("<programme channel='c$n' start='${time(start)}' stop='${time(start + 3_600_000)}'><title>${if (i == 0) "Şimdiki program $n" else "Gelecek $n-$i"}</title><desc>Deneme program açıklaması $n-$i.</desc></programme>")
        } }
        append("</tv>")
    }
    @Before fun setup() {
        model = resetFixtureSources(ui)
        base = System.currentTimeMillis() / 60_000 * 60_000
        val assets = instrumentation.context.assets
        val video = assets.open("playback-fixture.mp4").use { it.readBytes() }
        server = MockWebServer()
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val path = request.requestUrl!!.encodedPath
                if (path.startsWith("/art/")) return MockResponse().setHeader("Content-Type", "image/png")
                    .setBody(Buffer().write(assets.open("visual/${path.substringAfterLast('/')}").use { it.readBytes() }))
                if (path == "/guide.xml") return MockResponse().setHeader("Content-Type", "application/xml").setBody(xml())
                if (path.startsWith("/live/")) {
                    opens.computeIfAbsent(path) { AtomicInteger() }.incrementAndGet()
                    val start = request.getHeader("Range")?.substringAfter("bytes=")?.substringBefore('-')?.toIntOrNull()?.coerceIn(0, video.size) ?: 0
                    return MockResponse().setResponseCode(if (start > 0) 206 else 200).setHeader("Content-Type", "video/mp4")
                        .setHeader("Accept-Ranges", "bytes").apply { if (start > 0) setHeader("Content-Range", "bytes $start-${video.lastIndex}/${video.size}") }
                        .setBody(Buffer().write(video, start, video.size - start))
                }
                return MockResponse().setResponseCode(404)
            }
        }
        server.start()
        source = SourceConfig("panel-fixture", "Panel denemesi", SourceType.M3U, server.url("/list.m3u").toString())
        val raw = Catalog((0..17).map { n -> MediaEntry("live:$n", "$n", MediaKind.LIVE,
            if (n == 17) "*** BİLGİ ***" else "Kanal $n", when (n) { in 0..4, 17 -> "Haber"; in 5..9 -> "Spor"; in 10..15 -> "Çocuk"; else -> "Gizli" },
            artwork = server.url("/art/channel-${n % 6}.png").toString(), uri = server.url("/live/$n.mp4").toString(), epgId = "c$n", adult = n == 15) },
            epgUrls = listOf(server.url("/guide.xml").toString()), categoryOrder = mapOf(MediaKind.LIVE to listOf("Haber", "Spor", "Çocuk", "Gizli")))
        runBlocking { SourceTransactions.shared.write {
            model.store.saveSources(SourceRegistry(listOf(source)))
            model.store.hiddenCategories = setOf(CategoryVisibility.key(MediaKind.LIVE, "Gizli"))
            model.store.hideAdultCategories = true
            CatalogStore(ui.activity).write(source, CatalogSnapshot(raw, System.currentTimeMillis()))
        } }
        ui.runOnUiThread { model.restore() }
        ui.waitUntil(15_000) { model.state.value.route == Route.HOME }
        ui.runOnUiThread { model.playback { it.copy(osdTimeout = OsdTimeout.NEVER) } }
    }
    @After fun cleanup() {
        if (::model.isInitialized) {
            ui.runOnUiThread { if (model.state.value.route == Route.PLAYER) model.back(); model.removeSource() }
            ui.waitUntil(10_000) { model.state.value.sourceTask == SourceTask.IDLE }
        }
        if (::server.isInitialized) server.shutdown()
    }
    private fun view(v: View): PlayerView? = if (v is PlayerView) v else (v as? ViewGroup)?.let { group ->
        (0 until group.childCount).firstNotNullOfOrNull { view(group.getChildAt(it)) }
    }
    private fun engine(): Player = view(ui.activity.window.decorView)!!.player!!
    private fun key(code: Int) { instrumentation.sendKeyDownUpSync(code); ui.waitForIdle() }
    private fun channelFocused(n: Int) {
        val tag = "player-channel:${id(n)}"
        try {
            ui.waitUntil(5_000) { ui.onAllNodes(hasTestTag(tag) and isFocused()).fetchSemanticsNodes().isNotEmpty() }
        } catch (error: Throwable) {
            ui.activity.filesDir.resolve("channel-panel-preview/focus-failure.txt").apply { parentFile!!.mkdirs() }
                .writeText(ui.onRoot(useUnmergedTree = true).printToString())
            shot("focus-failure")
            throw error
        }
        ui.onNodeWithTag(tag).assertIsFocused()
    }
    private fun openVideo() {
        ui.runOnUiThread { model.open(model.state.value.catalog.items.first { it.id == id(0) }) }
        ui.waitUntil(15_000) { model.store.lastLive(source.id)?.localId == "live:0" }
        ui.waitForIdle()
        key(KeyEvent.KEYCODE_BACK)
        ui.onNodeWithTag("player-canvas").assertIsFocused()
    }
    private fun panel() { openVideo(); key(KeyEvent.KEYCODE_DPAD_CENTER); ui.onNodeWithTag("player-channel:${id(0)}").assertIsFocused() }
    private fun shot(name: String) {
        ui.waitForIdle(); Thread.sleep(350)
        val file = ui.activity.filesDir.resolve("channel-panel-preview/$name.png").apply { parentFile!!.mkdirs() }
        instrumentation.uiAutomation.takeScreenshot().let { image ->
            file.outputStream().use { image.compress(Bitmap.CompressFormat.PNG, 100, it) }; image.recycle()
        }
    }
    @Test fun shortOkOpensOnReleaseAndSecondSelectionReturnsToVideoOnTheSamePlayer() {
        openVideo()
        val original = engine()
        ui.onNodeWithTag("player-canvas").assertHasClickAction()
        val down = SystemClock.uptimeMillis()
        instrumentation.sendKeySync(KeyEvent(down, down, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_CENTER, 0))
        ui.waitForIdle(); ui.onNodeWithTag("player-channel-drawer").assertDoesNotExist()
        instrumentation.sendKeySync(KeyEvent(down, down + 30, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_CENTER, 0))
        ui.waitForIdle()
        ui.onNodeWithTag("player-channel:${id(0)}").assertIsFocused()
        ui.onNodeWithTag("player-channel-drawer").assert(SemanticsMatcher.expectValue(SemanticsProperties.PaneTitle, "Kanal listesi"))
        key(KeyEvent.KEYCODE_DPAD_DOWN)
        assertEquals(id(0), model.state.value.playing?.entry?.id)
        assertNull(opens["/live/1.mp4"])
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.waitUntil(10_000) { model.store.lastLive(source.id)?.localId == "live:1" }
        ui.onNodeWithTag("player-channel:${id(1)}").assertIsFocused()
        ui.onNodeWithTag("player-channel-drawer").assertExists()
        ui.runOnIdle { assertSame(original, engine()) }
        shot("channels")
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.onNodeWithTag("player-canvas").assertIsFocused()
        assertEquals(Route.PLAYER, model.state.value.route)
    }
    @Test fun heldOkShowsControlsAndItsReleaseNeverPausesPlayback() {
        openVideo()
        val down = SystemClock.uptimeMillis()
        instrumentation.sendKeySync(KeyEvent(down, down, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_CENTER, 0))
        instrumentation.sendKeySync(KeyEvent(down, down + 600, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_CENTER, 1))
        ui.waitForIdle(); ui.onNodeWithTag("player-primary").assertIsFocused()
        instrumentation.sendKeySync(KeyEvent(down, down + 630, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_CENTER, 0))
        ui.waitForIdle()
        ui.runOnIdle { assertTrue(engine().playWhenReady) }
        ui.onNodeWithTag("player-channel-drawer").assertDoesNotExist()
    }
    @Test fun categoryBrowsingDoesNotFilterUntilOkAndThePickedScopeDrivesZap() {
        panel()
        key(KeyEvent.KEYCODE_DPAD_LEFT)
        ui.onNodeWithTag("channel-scope:@all").assertIsFocused()
        key(KeyEvent.KEYCODE_DPAD_DOWN)
        ui.onNodeWithTag("channel-scope:Haber").assertIsFocused()
        ui.onNodeWithTag("channel-panel-scope").assertTextEquals("Tüm kanallar")
        shot("categories")
        // The playing channel is outside Spor: category selection focuses its first row without playing it.
        key(KeyEvent.KEYCODE_DPAD_DOWN)
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.onNodeWithTag("channel-panel-scope").assertTextEquals("Spor")
        channelFocused(5)
        assertEquals(id(0), model.state.value.playing?.entry?.id)
        key(KeyEvent.KEYCODE_DPAD_LEFT); key(KeyEvent.KEYCODE_DPAD_UP); key(KeyEvent.KEYCODE_DPAD_CENTER)
        channelFocused(0)
        ui.onNodeWithTag("channel-panel-scope").assertTextEquals("Haber")
        assertEquals(id(0), model.state.value.playing?.entry?.id)
        key(KeyEvent.KEYCODE_DPAD_DOWN); key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.waitUntil(5_000) { model.state.value.playing?.entry?.id == id(1) }
        assertEquals("Haber", model.state.value.liveScope.category)
        key(KeyEvent.KEYCODE_BACK)
        key(KeyEvent.KEYCODE_CHANNEL_UP)
        ui.waitUntil(5_000) { model.state.value.playing?.entry?.id == id(2) }
    }
    @Test fun miniGuideDetailsReturnToTheirExactRowAndBackOnlyClosesOneColumn() {
        panel()
        ui.waitUntil(10_000) { model.state.value.guideStatus[source.id]?.info != null }
        key(KeyEvent.KEYCODE_DPAD_RIGHT)
        ui.waitUntil(10_000) { ui.onAllNodesWithTag("channel-programme:0").fetchSemanticsNodes().isNotEmpty() }
        ui.onNodeWithTag("channel-programme:0").assertIsFocused()
        key(KeyEvent.KEYCODE_DPAD_DOWN)
        ui.onNodeWithTag("channel-programme:1").assertIsFocused()
        val channelBounds = ui.onNodeWithTag("player-channel:${id(0)}").getUnclippedBoundsInRoot()
        val viewport = ui.onNodeWithTag("player-channel-list").getUnclippedBoundsInRoot()
        assertTrue("Mini guide must keep its channel completely visible", channelBounds.top >= viewport.top && channelBounds.bottom <= viewport.bottom)
        shot("programmes")
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.onNodeWithTag("channel-programme-reader").assertExists()
        key(KeyEvent.KEYCODE_BACK)
        ui.waitUntil(5_000) { ui.activity.hasWindowFocus() }; ui.waitForIdle()
        ui.onNodeWithTag("channel-programme:1").assertIsFocused()
        key(KeyEvent.KEYCODE_DPAD_LEFT)
        channelFocused(0)
        ui.onNodeWithTag("channel-mini-guide").assertDoesNotExist()
        assertEquals(id(0), model.state.value.playing?.entry?.id)
        assertEquals(setOf("/live/0.mp4"), opens.keys)
        key(KeyEvent.KEYCODE_BACK)
        ui.onNodeWithTag("player-canvas").assertIsFocused()
    }
    @Test fun channelKeysPageAndDigitsFocusGlobalNumbersOutsideTheSelectedCategory() {
        panel()
        ui.waitUntil(10_000) { model.state.value.guideStatus[source.id]?.info != null }
        key(KeyEvent.KEYCODE_CHANNEL_UP)
        ui.onAllNodes(hasTestTag("player-channel:${id(0)}") and isFocused()).assertCountEquals(0)
        assertEquals(id(0), model.state.value.playing?.entry?.id)
        key(KeyEvent.KEYCODE_PAGE_UP)
        ui.onNodeWithTag("player-channel:${id(0)}").assertIsFocused()
        key(KeyEvent.KEYCODE_DPAD_LEFT); key(KeyEvent.KEYCODE_DPAD_DOWN); key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.onNodeWithTag("channel-panel-scope").assertTextEquals("Haber")
        key(KeyEvent.KEYCODE_1); key(KeyEvent.KEYCODE_0)
        ui.waitUntil(5_000) { ui.onAllNodes(hasTestTag("player-channel:${id(9)}") and isFocused()).fetchSemanticsNodes().isNotEmpty() }
        ui.onNodeWithTag("channel-panel-scope").assertTextEquals("Tüm kanallar")
        assertEquals(id(0), model.state.value.playing?.entry?.id)
        // GUIDE belongs to Google TV's GlobalKeyReceiver on this emulator; test the real app input boundary.
        ui.runOnUiThread { for (action in intArrayOf(KeyEvent.ACTION_DOWN, KeyEvent.ACTION_UP))
            ui.activity.window.callback.dispatchKeyEvent(KeyEvent(action, KeyEvent.KEYCODE_GUIDE)) }
        ui.waitUntil(10_000) { model.state.value.route == Route.GUIDE }
        assertEquals(id(9), model.state.value.guideChannelId)
    }
    @Test fun hiddenAndAdultChannelsAreNeverListedAndQuickActionsRestoreFocus() {
        panel()
        assertEquals(16, model.liveIndex().channels.size)
        assertNull(model.liveEntry(id(15))); assertNull(model.liveEntry(id(16)))
        ui.onNodeWithTag("player-channel:${id(15)}").assertDoesNotExist()
        ui.onNodeWithTag("player-channel:${id(16)}").assertDoesNotExist()
        key(KeyEvent.KEYCODE_DPAD_LEFT)
        ui.onNodeWithTag("channel-scope:Gizli").assertDoesNotExist()
        key(KeyEvent.KEYCODE_BACK)
        ui.onNodeWithTag("player-channel:${id(0)}").performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.OnLongClick) { it() }
        ui.onNodeWithTag("quick-favorite").assertExists()
        key(KeyEvent.KEYCODE_BACK)
        ui.waitUntil(5_000) { ui.activity.hasWindowFocus() }; ui.waitForIdle()
        channelFocused(0)
    }
    @Test fun panelUsesFifteenSecondsRatherThanTheControlsTimeout() {
        panel()
        ui.runOnUiThread { model.playback { it.copy(osdTimeout = OsdTimeout.S4) } }
        Thread.sleep(5_000); ui.waitForIdle()
        ui.onNodeWithTag("player-channel-drawer").assertExists()
        ui.waitUntil(15_000) { ui.onAllNodesWithTag("player-channel-drawer").fetchSemanticsNodes().isEmpty() }
        ui.onNodeWithTag("player-canvas").assertIsFocused()
        assertEquals(Route.PLAYER, model.state.value.route)
    }

}
