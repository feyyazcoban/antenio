package dev.iptvplayer.tv

import android.graphics.Bitmap
import android.view.KeyEvent
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.dp
import dev.iptvplayer.tv.ui.LoadingScreen
import dev.iptvplayer.tv.ui.LocalReducedMotion
import dev.iptvplayer.tv.ui.PilotTheme
import dev.iptvplayer.tv.ui.RestoringCover
import org.junit.*
import org.junit.Assert.*

class LoadingLayoutTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()
    /** D069: the cover that opens saved sources says it is working, in a readable size, with or without motion. */
    @Test fun restoringCoverShowsProgressAndALabel() {
        val reduced = mutableStateOf(false)
        ui.runOnUiThread { ui.activity.setContent { PilotTheme { CompositionLocalProvider(LocalReducedMotion provides reduced.value) { RestoringCover() } } } }
        for (still in listOf(false, true)) {
            ui.runOnUiThread { reduced.value = still }
            ui.waitUntil(5_000) { ui.onAllNodesWithTag("launch-loading").fetchSemanticsNodes().isNotEmpty() }
            ui.onNodeWithTag("launch-loading").assert(SemanticsMatcher.expectValue(SemanticsProperties.ProgressBarRangeInfo, ProgressBarRangeInfo.Indeterminate))
            val label = ui.onNodeWithText("Hazırlanıyor...").assertExists().getUnclippedBoundsInRoot()
            assertTrue("The label keeps a TV-readable height", label.bottom - label.top >= 18.dp)
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
}
