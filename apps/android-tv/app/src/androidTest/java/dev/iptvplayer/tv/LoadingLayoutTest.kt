package dev.iptvplayer.tv

import android.graphics.Bitmap
import android.view.KeyEvent
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.lifecycle.ViewModelProvider
import dev.iptvplayer.tv.data.ParentalLocks
import dev.iptvplayer.tv.data.ParentalStatus
import dev.iptvplayer.tv.data.ParentalUi
import dev.iptvplayer.tv.data.PinPrompt
import dev.iptvplayer.tv.data.PinPurpose
import dev.iptvplayer.tv.data.PinStep
import dev.iptvplayer.tv.data.SourceConfig
import dev.iptvplayer.tv.data.SourceType
import dev.iptvplayer.tv.ui.AppLockScreen
import dev.iptvplayer.tv.ui.SourcesScreen
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.sp
import dev.iptvplayer.tv.ui.LoadingScreen
import dev.iptvplayer.tv.ui.LocalReducedMotion
import dev.iptvplayer.tv.ui.PilotTheme
import dev.iptvplayer.tv.ui.RestoringCover
import org.junit.*
import org.junit.Assert.*

class LoadingLayoutTest {
    @get:Rule(order = 0) val turkish = TurkishLocaleRule()
    @get:Rule(order = 1) val ui = createAndroidComposeRule<MainActivity>()
    /** D069: the cover that opens saved sources says it is working, in a readable size, with or without motion. */
    @Test fun restoringCoverShowsProgressAndALabel() {
        val reduced = mutableStateOf(false)
        ui.runOnUiThread { ui.activity.setContent { PilotTheme { CompositionLocalProvider(LocalReducedMotion provides reduced.value) { RestoringCover() } } } }
        for (still in listOf(false, true)) {
            ui.runOnUiThread { reduced.value = still }
            ui.waitUntil(5_000) { ui.onAllNodesWithTag("launch-loading").fetchSemanticsNodes().isNotEmpty() }
            ui.onNodeWithTag("launch-loading").assert(SemanticsMatcher.expectValue(SemanticsProperties.ProgressBarRangeInfo, ProgressBarRangeInfo.Indeterminate))
            // At least 14 sp (D069), read from the laid-out text: a single line's box is trimmed to the font, so its
            // height does not tell 12 sp from 14 sp.
            val layouts = mutableListOf<TextLayoutResult>()
            ui.onNodeWithText("Hazırlanıyor...").assertExists().performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
            assertTrue("The label keeps a TV-readable size", layouts.single().layoutInput.style.fontSize >= 14.sp)
        }
    }

    // RESTORING here is the recovery screen's Yeniden dene and the reset fallback; opening the app uses the cover above.
    @Test fun everySourceOperationFitsAndImportCanBeCancelled() {
        val state = mutableStateOf(TvState())
        var cancelled = false
        ui.runOnUiThread { ui.activity.setContent { PilotTheme { LoadingScreen(state.value) { cancelled = true } } } }
        for (task in listOf(SourceTask.RESTORING, SourceTask.IMPORTING, SourceTask.SAVING, SourceTask.REMOVING, SourceTask.RESETTING)) {
            ui.runOnUiThread { state.value = TvState(sourceTask = task, progressLabel = when(task) {
                SourceTask.IMPORTING -> dev.iptvplayer.tv.data.UiText.Raw("Filmler ve diziler işleniyor · 60.000 kayıt")
                SourceTask.RESTORING -> dev.iptvplayer.tv.data.UiText.Res(R.string.progress_checking_library)
                SourceTask.SAVING -> dev.iptvplayer.tv.data.UiText.Res(R.string.progress_saving_library)
                else -> dev.iptvplayer.tv.data.UiText.Res(R.string.progress_resetting_sources)
            }) }
            ui.waitForIdle()
            val root = ui.onRoot().getUnclippedBoundsInRoot()
            for (tag in listOf("loading-title", "loading-status", "loading-explanation") + if(task == SourceTask.IMPORTING) listOf("loading-cancel") else emptyList()) {
                val bounds = ui.onNodeWithTag(tag).getUnclippedBoundsInRoot()
                assertTrue("$task $tag must fit", bounds.top >= root.top && bounds.bottom <= root.bottom && bounds.left >= root.left && bounds.right <= root.right)
            }
            ui.onNodeWithTag("loading-status").assert(SemanticsMatcher.expectValue(SemanticsProperties.ProgressBarRangeInfo, ProgressBarRangeInfo.Indeterminate))
            if(task == SourceTask.IMPORTING) {
                ui.onNodeWithTag("loading-cancel").assertIsFocused()
                InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER)
                ui.waitForIdle(); assertTrue(cancelled)
            } else ui.onNodeWithTag("loading-cancel").assertDoesNotExist()
            val dir = ui.activity.filesDir.resolve("loading-preview").apply { mkdirs() }
            InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot().let { bitmap ->
                dir.resolve("${task.name.lowercase()}.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) }; bitmap.recycle()
            }
        }
    }

    /**
     * S12 (pin.md §3.9, §4): the lock screen keeps its title, digits, keys and PIN'i unuttum inside the 960×540 screen at
     * 1.0, 1.5 and 2.0 text, focus starting on 1, and so does its notice of a record that cannot be read, with Yeniden
     * dene and Sıfırla…; TalkBack hears each named as it appears. Synthetic states; nothing is checked or reset.
     */
    @Test fun theLockScreenKeepsItsKeysAndActionsInViewAtEveryTextSize() {
        lateinit var model: TvViewModel
        ui.runOnUiThread { model = ViewModelProvider(ui.activity)[TvViewModel::class.java] }
        val pad = TvState(parental = ParentalUi(ParentalStatus.ON, prompt = PinPrompt(1, PinPurpose.Launch, PinStep.VERIFY), appLocked = true))
        val unreadable = TvState(parental = ParentalUi(ParentalStatus.UNAVAILABLE, appLocked = true))
        val state = mutableStateOf(pad)
        val scale = mutableFloatStateOf(1f)
        ui.runOnUiThread { ui.activity.setContent { PilotTheme {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, scale.floatValue)) { AppLockScreen(state.value, model) }
        } } }
        fun fits(tag: String, what: String) {
            val root = ui.onRoot().getUnclippedBoundsInRoot()
            val bounds = ui.onNodeWithTag(tag).getUnclippedBoundsInRoot()
            assertTrue("$what: $tag must fit", bounds.top >= root.top && bounds.bottom <= root.bottom && bounds.left >= root.left && bounds.right <= root.right)
        }
        fun shot(name: String) {
            ui.waitForIdle(); Thread.sleep(600); ui.waitForIdle()
            val dir = ui.activity.filesDir.resolve("loading-preview").apply { mkdirs() }
            InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot().let { bitmap ->
                dir.resolve("$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }; bitmap.recycle()
            }
        }
        for (fontScale in listOf(1f, 1.5f, 2f)) {
            val name = fontScale.toString().replace('.', '_')
            ui.runOnUiThread { scale.floatValue = fontScale; state.value = pad }
            ui.waitUntil(5_000) { ui.onAllNodes(hasTestTag("pin-key:1") and isFocused()).fetchSemanticsNodes().isNotEmpty() }
            for (tag in listOf("app-lock-title", "pin-boxes", "pin-feedback", "pin-key:1", "pin-key:0", "pin-key:delete", "pin-forgot")) fits(tag, "pad $fontScale")
            ui.onNodeWithTag("pin-boxes").assertContentDescriptionEquals("0 / 4 rakam girildi")
            ui.onNodeWithTag("app-lock").assert(SemanticsMatcher.expectValue(SemanticsProperties.PaneTitle, "NEXTV kilitli"))
            shot("app-lock-$name")
            ui.runOnUiThread { state.value = unreadable }
            ui.waitUntil(5_000) { ui.onAllNodes(hasTestTag("app-lock-retry") and isFocused()).fetchSemanticsNodes().isNotEmpty() }
            for (tag in listOf("app-lock-title", "app-lock-retry", "app-lock-reset")) fits(tag, "unreadable $fontScale")
            ui.onAllNodesWithTag("pin-key:1").assertCountEquals(0)
            ui.onNodeWithTag("app-lock").assert(SemanticsMatcher.expectValue(SemanticsProperties.PaneTitle, "Ebeveyn denetimi kaydı açılamadı"))
            shot("app-lock-unreadable-$name")
        }
    }

    /**
     * S12 (pin.md §3.8): with a PIN set, Kaynaklar' Bilgiler holds Kaynağı kilitle beside Rehber ayarları and Kapat, and
     * at 1.0, 1.5 and 2.0 text the source's details still show at least two lines while every action sits inside the
     * window. Synthetic states: nothing is locked or saved.
     */
    @Test fun bilgilerKeepsTheDetailsReadableBesideTheSourceLockAtEveryTextSize() {
        lateinit var model: TvViewModel
        ui.runOnUiThread { model = ViewModelProvider(ui.activity)[TvViewModel::class.java] }
        val source = SourceConfig("layout-lock", "Salon televizyonu", SourceType.M3U, "http://127.0.0.1:1/list.m3u")
        val open = TvState(route = Route.SOURCES, sources = listOf(source), parental = ParentalUi(ParentalStatus.ON))
        // The longer label: Kaynağın kilidini kaldır.
        val locked = open.copy(locks = ParentalLocks(sources = setOf(source.id)))
        val state = mutableStateOf(open)
        val scale = mutableFloatStateOf(1f)
        ui.runOnUiThread { ui.activity.setContent { PilotTheme {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, scale.floatValue)) { SourcesScreen(state.value, model) }
        } } }
        fun inside(tag: String, what: String) {
            val control = ui.onNodeWithTag(tag).getUnclippedBoundsInRoot()
            val dialog = ui.onNodeWithTag("source-dialog").getUnclippedBoundsInRoot()
            assertTrue("$what: $tag must fit inside the window", control.top >= dialog.top && control.bottom <= dialog.bottom &&
                control.left >= dialog.left && control.right <= dialog.right)
        }
        for (fontScale in listOf(1f, 1.5f, 2f)) for ((name, value) in listOf("open" to open, "locked" to locked)) {
            val what = "$name $fontScale"
            ui.runOnUiThread { scale.floatValue = fontScale; state.value = value }
            ui.waitForIdle()
            ui.onNodeWithTag("info-source:${source.id}").performSemanticsAction(SemanticsActions.RequestFocus)
            InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER)
            ui.waitUntil(5_000) { ui.onAllNodes(hasTestTag("source-info-reader") and isFocused()).fetchSemanticsNodes().isNotEmpty() }
            ui.onNodeWithTag("source-lock").assertTextEquals(if (value === locked) "Kaynağın kilidini kaldır" else "Kaynağı kilitle")
            for (tag in listOf("source-info-reader", "source-guide-settings", "source-lock", "close-source-info")) inside(tag, what)
            // The reader snaps to whole lines of 18 sp under 16 dp of padding.
            val reader = ui.onNodeWithTag("source-info-reader").getUnclippedBoundsInRoot()
            val lines = ((reader.bottom - reader.top).value - 16f) / (18f * fontScale)
            assertTrue("$what: the details show $lines lines", lines >= 1.99f)
            val file = "source-info-lock-$name-${fontScale.toString().replace('.', '_')}"
            ui.waitForIdle(); Thread.sleep(400)
            val dir = ui.activity.filesDir.resolve("loading-preview").apply { mkdirs() }
            InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot().let { bitmap ->
                dir.resolve("$file.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }; bitmap.recycle()
            }
            InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
            ui.waitUntil(5_000) { ui.onAllNodesWithTag("source-dialog").fetchSemanticsNodes().isEmpty() }
        }
    }
}
