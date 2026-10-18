@file:androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)

package dev.iptvplayer.tv

import android.graphics.Bitmap
import android.os.SystemClock
import android.view.KeyEvent
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.media3.common.C
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.iptvplayer.tv.data.*
import dev.iptvplayer.tv.ui.PlayerFactory
import okhttp3.mockwebserver.*
import okio.Buffer
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

/**
 * Ayarlar → Oynatıcı with the remote (S14, playback.md §3.3, §8): a choice opens on the current value, says what each
 * option does and reads as radio buttons; OK keeps the pick and focus returns to its row, Back keeps the value as it was;
 * Altyazı görünümü is a page of its own with its own Back. A film then opens with every engine setting changed, and its
 * controls hide after the chosen time, or never. A synthetic list and the fixture video on a local server; never run
 * state-clearing tests on a user's television.
 */
@RunWith(AndroidJUnit4::class)
class PlayerSettingsFlowTest {
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
                if (request.requestUrl!!.encodedPath == "/list.m3u")
                    return MockResponse().setBody("#EXTM3U\n#EXTINF:-1,Ayar filmi\n${server.url("/movie/one.mp4")}\n")
                val start = request.getHeader("Range")?.substringAfter("bytes=")?.substringBefore('-')?.toIntOrNull() ?: 0
                return MockResponse().setResponseCode(if (start > 0) 206 else 200)
                    .addHeader("Content-Type", "video/mp4").addHeader("Accept-Ranges", "bytes")
                    .apply { if (start > 0) addHeader("Content-Range", "bytes $start-${bytes.lastIndex}/${bytes.size}") }
                    .setBody(Buffer().write(bytes, start.coerceIn(0, bytes.size), (bytes.size - start).coerceAtLeast(0)))
            }
        }
        server.start()
        // The settings start from their defaults (resetFixtureSources).
        model = resetFixtureSources(ui)
        val address = server.url("/list.m3u").toString()
        ui.runOnUiThread {
            model.draft(SourceDraft("Oynatıcı denemesi", SourceType.M3U, address))
            model.connectDraft()
        }
        ui.waitUntil(20_000) { model.state.value.route == Route.HOME }
    }

    @After fun cleanup() {
        if (::model.isInitialized) ui.runOnUiThread {
            if (model.state.value.route == Route.PLAYER) model.back()
            model.playback { PlaybackPreferences() }
            model.removeSource()
        }
        if (::server.isInitialized) server.shutdown()
    }

    private fun key(key: Int) { instrumentation.sendKeyDownUpSync(key); ui.waitForIdle() }
    private fun row(label: String) = ui.onNode(hasText(label) and hasClickAction() and hasAnyAncestor(hasTestTag("settings-panel")))
    private fun title(text: String) = ui.onNodeWithTag("settings-title").assertTextEquals(text)
    private fun state(text: String) = SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, text)
    private val radio = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton)
    private val prefs get() = model.state.value.playback
    private fun controlsShown() = ui.onAllNodesWithTag("player-panel").fetchSemanticsNodes().isNotEmpty()
    private fun playing() = ui.onAllNodesWithText("Duraklat").fetchSemanticsNodes().isNotEmpty()

    /** Ayarlar → Oynatıcı, focus on its first row. */
    private fun openPlayerSettings() {
        ui.onNodeWithTag("nav:Ayarlar").performSemanticsAction(SemanticsActions.RequestFocus)
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        // A platform Dialog acquires window focus independently of Compose's idle clock.
        ui.waitUntil(5_000) { ui.onAllNodes(hasText("Genel ve dil") and isFocused()).fetchSemanticsNodes().isNotEmpty() }
        row("Oynatıcı").performSemanticsAction(SemanticsActions.RequestFocus)
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        title("Oynatıcı")
        row("Otomatik sonraki bölüm").assertIsFocused()
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
            val scale = ui.activity.resources.configuration.fontScale.toString().replace('.', '_')
            val directory = ui.activity.filesDir.resolve("player-settings-preview").apply { mkdirs() }
            directory.resolve("$name-$scale.png").outputStream().use { image.compress(Bitmap.CompressFormat.PNG, 100, it) }
            image.recycle()
        }
    }

    @Test fun aChoiceOpensOnTheCurrentValueAndOkReturnsToItsRow() {
        openPlayerSettings()
        val buffer = ui.onNodeWithTag("player-setting:buffer")
        buffer.performSemanticsAction(SemanticsActions.RequestFocus)
        // The row says what the chosen one does, its name at the end.
        buffer.assertIsFocused().assert(state("Normal")).assert(hasText("Yayın en hızlı açılır."))
        assertFocusedRowReadable()
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        title("Arabellek")
        ui.onNode(hasText("Bu ayarlar bir sonraki açtığın yayında geçerli olur.") and hasAnyAncestor(hasTestTag("settings-panel"))).assertExists()
        ui.onNodeWithTag("settings-option:0").assertIsFocused().assertIsSelected().assert(radio).assert(state("Seçili"))
        ui.onNodeWithTag("settings-option:1").assert(radio).assert(state("Seçili değil"))
            .assert(hasText("Geniş")).assert(hasText("Açılış biraz uzar; kısa bağlantı dalgalanmalarına daha dayanıklıdır."))
        ui.onNodeWithTag("settings-option:2").assert(hasText("Çok geniş"))
        assertFocusedRowReadable()
        shot("buffer-choice")
        key(KeyEvent.KEYCODE_DPAD_DOWN)
        ui.onNodeWithTag("settings-option:1").assertIsFocused()
        assertFocusedRowReadable()
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        // Kept on the device; the page closes on its row, which reads the new value.
        title("Oynatıcı")
        buffer.assertIsFocused().assert(state("Geniş"))
        assertEquals(BufferPreset.LARGE, prefs.buffer)
        assertEquals(BufferPreset.LARGE, DeviceStore(ui.activity).playback.buffer)
        assertFocusedRowReadable()
        // Opened again it starts on Geniş; Back leaves without a change.
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.onNodeWithTag("settings-option:1").assertIsFocused().assertIsSelected()
        key(KeyEvent.KEYCODE_DPAD_DOWN)
        key(KeyEvent.KEYCODE_BACK)
        title("Oynatıcı")
        buffer.assertIsFocused()
        assertEquals(BufferPreset.LARGE, prefs.buffer)
        // A switch among the choices: tunnelling, experimental and off by default.
        val tunneling = ui.onNodeWithTag("player-setting:tunneling")
        tunneling.performSemanticsAction(SemanticsActions.RequestFocus)
        tunneling.assertIsFocused().assertIsOff()
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        tunneling.assertIsOn(); assertTrue(prefs.tunneling)
        assertFocusedRowReadable()
        key(KeyEvent.KEYCODE_BACK)
        title("Ayarlar")
        row("Oynatıcı").assertIsFocused()
        key(KeyEvent.KEYCODE_BACK)
        ui.onAllNodesWithTag("settings-panel").assertCountEquals(0)
    }

    @Test fun subtitlesHaveAPageOfTheirOwnAndChoosingALookUsesNextvsSetting() {
        openPlayerSettings()
        key(KeyEvent.KEYCODE_DPAD_DOWN)
        row("Altyazı görünümü").assertIsFocused().assert(hasText("Android ayarı"))
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        title("Altyazı görünümü")
        ui.onNodeWithTag("player-setting:subtitle-source").assertIsFocused().assert(hasText("Android ayarı"))
        ui.onNodeWithText("Boyut, zemin veya renk seçersen NEXTV ayarına geçilir.").assertExists()
        key(KeyEvent.KEYCODE_DPAD_DOWN)
        val size = ui.onNodeWithTag("player-setting:subtitle-size")
        size.assertIsFocused()
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        title("Boyut")
        ui.onNodeWithTag("settings-option:1").assertIsFocused().assert(hasText("Normal"))
        key(KeyEvent.KEYCODE_DPAD_DOWN); key(KeyEvent.KEYCODE_DPAD_DOWN)
        ui.onNodeWithTag("settings-option:3").assertIsFocused().assert(hasText("Çok büyük"))
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        // The size moves the subtitles to NEXTV's setting, and the note says what that leaves out.
        title("Altyazı görünümü")
        size.assertIsFocused().assert(hasText("Çok büyük"))
        assertEquals(SubtitleStyle(SubtitleSource.APP, SubtitleSize.XLARGE), prefs.subtitle)
        ui.onNodeWithTag("player-setting:subtitle-source").assert(hasText("NEXTV ayarı"))
        ui.onNodeWithText("NEXTV ayarında yayının kendi altyazı biçimi kullanılmaz.").assertExists()
        assertFocusedRowReadable()
        shot("subtitles")
        // Back: the subtitles page, then Oynatıcı on its row, which sums the look up.
        key(KeyEvent.KEYCODE_BACK)
        title("Oynatıcı")
        row("Altyazı görünümü").assertIsFocused().assert(hasText("Çok büyük · Yarı saydam · Beyaz"))
        key(KeyEvent.KEYCODE_BACK)
        row("Oynatıcı").assertIsFocused()
        key(KeyEvent.KEYCODE_BACK)
        ui.onAllNodesWithTag("settings-panel").assertCountEquals(0)
    }

    @Test fun theControlsHideAfterTheChosenTimeOrStayWithNever() {
        openPlayerSettings()
        val timeout = ui.onNodeWithTag("player-setting:osd-timeout")
        timeout.performSemanticsAction(SemanticsActions.RequestFocus)
        timeout.assertIsFocused().assert(state("8 saniye"))
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        title("Kontrollerin kapanma süresi")
        ui.onNodeWithTag("settings-option:1").assertIsFocused().assert(hasText("8 saniye"))
        ui.onNodeWithTag("settings-option:4").assert(hasText("Hiçbir zaman")).assert(hasText("Kontroller Geri'ye basana kadar kalır."))
        key(KeyEvent.KEYCODE_DPAD_UP)
        ui.onNodeWithTag("settings-option:0").assertIsFocused().assert(hasText("4 saniye"))
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        timeout.assertIsFocused().assert(state("4 saniye"))
        assertEquals(OsdTimeout.S4, prefs.osdTimeout)
        key(KeyEvent.KEYCODE_BACK); key(KeyEvent.KEYCODE_BACK)

        // Four seconds after the film opens the controls go, well before the eight they used to take.
        ui.runOnUiThread { model.play(model.state.value.catalog.items.first()) }
        ui.waitUntil(20_000) { playing() }
        val shownAt = SystemClock.elapsedRealtime()
        assertTrue(controlsShown())
        ui.waitUntil(8_000) { !controlsShown() }
        assertTrue("Hidden after ${SystemClock.elapsedRealtime() - shownAt} ms", SystemClock.elapsedRealtime() - shownAt < 6_000)
        ui.runOnUiThread { model.back() }
        // The player leaves the screen before the film opens again, as it does between two presses of the remote.
        ui.waitUntil(5_000) { model.state.value.route != Route.PLAYER && ui.onAllNodesWithTag("player-controls").fetchSemanticsNodes().isEmpty() }
        ui.waitForIdle()

        // Hiçbir zaman: still up long after any timeout would have hidden them.
        ui.runOnUiThread { model.playback { it.copy(osdTimeout = OsdTimeout.NEVER) } }
        ui.runOnUiThread { model.play(model.state.value.catalog.items.first()) }
        ui.waitUntil(20_000) { playing() }
        Thread.sleep(10_000)
        assertTrue(controlsShown())
    }

    @Test fun aFilmOpensWithEveryEngineSettingChanged() {
        val changed = PlaybackPreferences(FrameRateMatching.OFF, BufferPreset.MAX, DecoderMode.SOFTWARE_FIRST, AudioOutput.PCM,
            tunneling = true, SubtitleStyle(SubtitleSource.APP, SubtitleSize.LARGE, SubtitleBackground.NONE, SubtitleColor.YELLOW),
            OsdTimeout.S30, PanelOpacity.SHEER)
        ui.runOnUiThread { model.playback { changed } }
        ui.runOnUiThread { model.play(model.state.value.catalog.items.first()) }
        // Software decoders, audio decoded on the device and the largest buffer still play it; tunnelling the emulator
        // lacks falls back to ordinary playback.
        ui.waitUntil(30_000) { playing() }
        assertEquals(Route.PLAYER, model.state.value.route)
        ui.onAllNodesWithText("çözemiyor", substring = true).assertCountEquals(0)
        shot("player-changed")
    }

    @Test fun tunnellingSurvivesTheRememberedLanguagesAndATrackChoice() {
        ui.runOnUiThread {
            val store = DeviceStore(ui.activity).apply { audioLanguage = "tr"; subtitlesOff = true }
            val player = PlayerFactory.create(ui.activity, store, PlaybackPreferences(tunneling = true))
            try {
                val selector = player.trackSelector as DefaultTrackSelector
                assertTrue(selector.parameters.tunnelingEnabled)
                // Each new stream sets the remembered languages, and Ses / Altyazı a track type, over the same parameters.
                player.trackSelectionParameters = PlayerFactory.rememberedTracks(player.trackSelectionParameters, store)
                player.trackSelectionParameters = player.trackSelectionParameters.buildUpon().setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false).build()
                assertTrue(selector.parameters.tunnelingEnabled)
                assertEquals(listOf("tr"), selector.parameters.preferredAudioLanguages)
            } finally {
                player.release(); store.audioLanguage = null
            }
        }
    }
}
