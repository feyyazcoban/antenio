package dev.iptvplayer.tv

import android.graphics.Bitmap
import android.os.Build
import android.view.KeyEvent
import android.view.WindowManager
import android.view.inspector.WindowInspector
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.*
import androidx.media3.exoplayer.ExoPlayer
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import dev.iptvplayer.tv.data.PanelOpacity
import dev.iptvplayer.tv.data.PlaybackPreferences
import dev.iptvplayer.tv.ui.PilotTheme
import dev.iptvplayer.tv.ui.TracksDialog
import org.junit.*

/** Metadata-only panel stress cases; no provider or media requests. Dedicated emulator only. */
class TracksLayoutTest {
    @get:Rule(order = 0) val turkish = TurkishLocaleRule()
    @get:Rule(order = 1) val ui = createAndroidComposeRule<MainActivity>()
    private var player: ExoPlayer? = null
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private var dismissed = false

    /** The panel over [tracks], its body as Oynatıcı panelleri says: the default unless [opacity] is given. */
    private fun open(tracks: Tracks, opacity: PanelOpacity = PlaybackPreferences().panelOpacity) {
        ui.runOnUiThread {
            val model = ViewModelProvider(ui.activity)[TvViewModel::class.java]
            val engine = ExoPlayer.Builder(ui.activity).build().also { player = it }
            ui.activity.setContent {
                // setContent keeps the last panel's state in place; each open shows a new one.
                var visible by remember(engine) { mutableStateOf(true) }
                PilotTheme { if (visible) TracksDialog(tracks, engine.trackSelectionParameters, engine, model, opacity) {
                    visible = false
                    dismissed = true
                } }
            }
        }
        ui.waitUntil(5_000) {
            ui.onAllNodesWithTag("tracks-close").fetchSemanticsNodes().any {
                it.config.getOrNull(SemanticsProperties.Focused) == true
            }
        }
    }
    @After fun cleanup() { ui.runOnUiThread { player?.release() } }
    private fun down() {
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN)
        ui.waitForIdle()
    }
    private fun shot(name: String) {
        ui.waitForIdle()
        // Platform Dialog animation is outside the Compose test clock.
        Thread.sleep(1200)
        ui.waitForIdle()
        instrumentation.uiAutomation.takeScreenshot().let { image ->
            val dir = ui.activity.filesDir.resolve("visual-preview").apply { mkdirs() }
            dir.resolve("$name.png").outputStream().use { image.compress(Bitmap.CompressFormat.PNG, 100, it) }
            image.recycle()
        }
    }
    private fun focusedInsideViewport() {
        val row = ui.onNode(isFocused()).fetchSemanticsNode().boundsInRoot
        val viewport = ui.onNodeWithTag("tracks-viewport").fetchSemanticsNode().boundsInRoot
        Assert.assertTrue("Focused row top $row outside $viewport", row.top >= viewport.top)
        Assert.assertTrue("Focused row bottom $row outside $viewport", row.bottom <= viewport.bottom)
    }
    @Test fun emptyMetadataKeepsExplanationAndCloseReachable() {
        open(Tracks.EMPTY)
        ui.onNodeWithText("Ses parçası bulunamadı; yayın henüz açılmamış olabilir.").assertExists()
        ui.onNodeWithText("Bu yayında gömülü altyazı bulunamadı.").assertExists()
        down()
        ui.onNodeWithText("Kapalı").assertIsFocused().assert(
            SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton))
        focusedInsideViewport()
        shot("tracks-empty")
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
        ui.waitUntil(5_000) { dismissed }
    }
    @Test fun manyTracksWithLongLabelsKeepRemoteSelectionReadable() {
        fun group(type: Int): Tracks.Group {
            val formats = (1..12).map { number ->
                Format.Builder().setId("$type-$number")
                    .setSampleMimeType(if (type == C.TRACK_TYPE_AUDIO) MimeTypes.AUDIO_AAC else MimeTypes.TEXT_VTT)
                    .setLanguage("tr").setLabel(if (number == 4)
                        "Ayrıntılı açıklama: özgün sinema sürümü, işitme güçlüğü yaşayan izleyiciler için konuşma ve ortam sesleri"
                        else "Seçenek $number")
                    .build()
            }.toTypedArray()
            return Tracks.Group(TrackGroup("group-$type", *formats), false,
                IntArray(12) { if (it == 1) C.FORMAT_UNSUPPORTED_TYPE else C.FORMAT_HANDLED },
                BooleanArray(12) { it == 0 })
        }
        open(Tracks(listOf(group(C.TRACK_TYPE_AUDIO), group(C.TRACK_TYPE_TEXT))))
        val header = ui.onNodeWithTag("tracks-title").fetchSemanticsNode().boundsInRoot
        ui.onAllNodes(hasText("cihaz desteklemiyor", substring = true)).assertCountEquals(2)
        ui.onAllNodes(hasText("cihaz desteklemiyor", substring = true))[0].assertIsNotEnabled()
        // Eleven supported audio choices, Off, eleven supported subtitle choices.
        repeat(23) { step ->
            android.util.Log.i("TracksMetadata", "step=$step")
            down()
            ui.onNode(isFocused()).assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton))
            val text = ui.onNode(isFocused()).fetchSemanticsNode().config.getOrNull(SemanticsProperties.Text).orEmpty().joinToString()
            Assert.assertFalse(text, text.contains("cihaz desteklemiyor"))
            focusedInsideViewport()
            Assert.assertEquals(header, ui.onNodeWithTag("tracks-title").fetchSemanticsNode().boundsInRoot)
            if (step == 2) shot("tracks-long")
        }
        ui.onNode(isFocused()).assertTextContains("Türkçe · 12 · Seçenek 12")
        shot("tracks-many-last")
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
        ui.waitUntil(5_000) { dismissed }
    }
    @Test fun oversizedDescriptionCanBeReadWithoutSelectingTrack() {
        val label = "Açıklama başlangıcı. " + "Konuşmalar, ortam sesleri ve erişilebilirlik bilgileri. ".repeat(30) + "AÇIKLAMA SONU"
        val format = Format.Builder().setId("long-audio").setSampleMimeType(MimeTypes.AUDIO_AAC)
            .setLanguage("tr").setLabel(label).build()
        open(Tracks(listOf(Tracks.Group(TrackGroup("long", format), false,
            intArrayOf(C.FORMAT_UNSUPPORTED_TYPE), booleanArrayOf(false)))))
        down()
        ui.onNodeWithText("Tam açıklama").assertIsFocused()
        shot("tracks-oversized-summary")
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.waitUntil(5_000) {
            ui.onAllNodesWithTag("track-description-reader").fetchSemanticsNodes().any {
                it.config.getOrNull(SemanticsProperties.Focused) == true
            }
        }
        shot("tracks-description-start")
        var steps = 0
        while (ui.onNodeWithTag("track-description-reader").fetchSemanticsNode().config
                .getOrNull(SemanticsProperties.StateDescription) != "Metnin sonu") {
            Assert.assertTrue("Reader must reach the end", steps++ < 80)
            down()
            ui.onNodeWithTag("track-description-reader").assertIsFocused()
        }
        shot("tracks-description-end")
        ui.runOnUiThread { Assert.assertTrue(player!!.trackSelectionParameters.overrides.isEmpty()) }
        down()
        ui.onNodeWithTag("track-description-back").assertIsFocused()
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
        ui.waitUntil(5_000) {
            ui.onAllNodesWithText("Tam açıklama").fetchSemanticsNodes().any {
                it.config.getOrNull(SemanticsProperties.Focused) == true
            }
        }
        down()
        ui.onNodeWithText("Kapalı").assertIsFocused()
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
        ui.waitUntil(5_000) { dismissed }
    }

    /**
     * Oynatıcı panelleri (S14, playback.md §3.1): while the panel lets the picture through, its window dims nothing
     * behind it, or the picture would show through as a dark grey; an opaque panel keeps the dimmed picture it had.
     */
    @SdkSuppress(minSdkVersion = Build.VERSION_CODES.Q)
    @Test fun aSeeThroughPanelLeavesThePictureBehindItUndimmed() {
        for (opacity in PanelOpacity.entries) {
            open(Tracks.EMPTY, opacity)
            ui.onNodeWithTag("tracks-panel").assertExists()
            ui.onNodeWithText("Ses parçası bulunamadı; yayın henüz açılmamış olabilir.").assertExists()
            ui.waitForIdle()
            lateinit var window: WindowManager.LayoutParams
            ui.runOnUiThread {
                // The panel's own window, above the activity's.
                window = WindowInspector.getGlobalWindowViews().mapNotNull { it.layoutParams as? WindowManager.LayoutParams }
                    .last { it.type == WindowManager.LayoutParams.TYPE_APPLICATION }
            }
            val dimmed = (window.flags and WindowManager.LayoutParams.FLAG_DIM_BEHIND) != 0 && window.dimAmount > 0f
            Assert.assertEquals("$opacity: dim ${window.dimAmount}", opacity == PanelOpacity.OPAQUE, dimmed)
            if (opacity == PlaybackPreferences().panelOpacity) shot("tracks-see-through")
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
            ui.waitUntil(5_000) { dismissed }
            dismissed = false
            ui.runOnUiThread { player?.release(); player = null }
        }
    }
}
