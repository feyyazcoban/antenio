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
import dev.iptvplayer.tv.ui.LoadingScreen
import dev.iptvplayer.tv.ui.PilotTheme
import org.junit.*
import org.junit.Assert.*

class LoadingLayoutTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()
    @Test fun everySourceOperationFitsAndImportCanBeCancelled() {
        val state = mutableStateOf(TvState())
        var cancelled = false
        ui.runOnUiThread { ui.activity.setContent { PilotTheme { LoadingScreen(state.value) { cancelled = true } } } }
        for (task in listOf(SourceTask.RESTORING, SourceTask.IMPORTING, SourceTask.SAVING, SourceTask.REMOVING, SourceTask.RESETTING)) {
            ui.runOnUiThread { state.value = TvState(sourceTask = task, progressLabel = when(task) {
                SourceTask.IMPORTING -> "Filmler ve diziler işleniyor · 60.000 kayıt"
                SourceTask.RESTORING -> "Kayıtlı kütüphanen kontrol ediliyor…"
                SourceTask.SAVING -> "Kütüphanen cihazına kaydediliyor…"
                else -> "Bu cihazdaki kaynak kayıtları temizleniyor…"
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
