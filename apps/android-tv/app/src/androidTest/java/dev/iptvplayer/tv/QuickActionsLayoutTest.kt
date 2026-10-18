package dev.iptvplayer.tv

import android.graphics.Bitmap
import android.view.KeyEvent
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.BringIntoViewSpec
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import dev.iptvplayer.tv.data.MediaEntry
import dev.iptvplayer.tv.data.MediaKind
import dev.iptvplayer.tv.data.inSource
import dev.iptvplayer.tv.ui.PilotTheme
import dev.iptvplayer.tv.ui.QuickAction
import dev.iptvplayer.tv.ui.QuickActions
import org.junit.*
import org.junit.Assert.*

/**
 * The quick actions with more rows than their window (S05; later steps add rows up to seven): the list scrolls by whole
 * rows and the focused row stays inside it, at the device's font scale (run at 1.0, 1.5 and 2.0). No source and no
 * provider requests. Dedicated emulator only.
 */
@OptIn(ExperimentalFoundationApi::class)
class QuickActionsLayoutTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val scale get() = ui.activity.resources.configuration.fontScale.toString().replace('.', '_')

    private fun key(key: Int) { instrumentation.sendKeyDownUpSync(key); ui.waitForIdle() }
    private fun bounds(tag: String) = ui.onNodeWithTag(tag).getUnclippedBoundsInRoot()
    // Rows are measured in whole pixels; a rounding pixel is not a cut row.
    private fun DpRect.inside(window: DpRect) = top >= window.top - 1.dp && bottom <= window.bottom + 1.dp

    @Test fun aLongListScrollsByWholeRowsAndKeepsTheFocusedRowInside() {
        val entry = MediaEntry("movie:1", "1", MediaKind.MOVIE, "Deneme filmi", "Genel", extension = "mp4").inSource("test")
        val tags = List(8) { "row-$it" }
        var ran: String? = null
        var dismissed = false
        ui.runOnUiThread {
            ui.activity.setContent {
                // The app's own scroll rule (PilotApp), not the platform's TV pivot.
                PilotTheme { CompositionLocalProvider(LocalBringIntoViewSpec provides object : BringIntoViewSpec {}) {
                    QuickActions(entry, tags.map { tag -> QuickAction(tag, R.string.action_details) { ran = tag } }) { dismissed = true }
                } }
            }
        }
        // A platform Dialog acquires window focus independently of Compose's idle clock.
        ui.waitUntil(5_000) { ui.onAllNodes(hasTestTag("quick-row-0") and isFocused()).fetchSemanticsNodes().isNotEmpty() }
        val window = bounds("quick-actions")
        for (tag in tags.drop(1).map { "quick-$it" } + "quick-cancel") {
            key(KeyEvent.KEYCODE_DPAD_DOWN)
            ui.onNodeWithTag(tag).assertIsFocused()
            assertTrue("Focused row $tag must stay inside the window", bounds(tag).inside(window))
        }
        // The list scrolled, and the rows that show are whole.
        assertFalse("The first row has scrolled away", bounds("quick-row-0").inside(window))
        for (tag in tags.map { "quick-$it" } + "quick-cancel") {
            val row = bounds(tag)
            if (row.bottom > window.top && row.top < window.bottom) assertTrue("Row $tag is cut by the window", row.inside(window))
        }
        ui.waitForIdle(); Thread.sleep(1200); ui.waitForIdle()
        instrumentation.uiAutomation.takeScreenshot().let { image ->
            val directory = ui.activity.filesDir.resolve("quick-preview").apply { mkdirs() }
            directory.resolve("quick-long-$scale.png").outputStream().use { image.compress(Bitmap.CompressFormat.PNG, 100, it) }
            image.recycle()
        }
        key(KeyEvent.KEYCODE_DPAD_UP)
        ui.onNodeWithTag("quick-row-7").assertIsFocused()
        assertTrue(bounds("quick-row-7").inside(window))
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        assertEquals("row-7", ran)
        key(KeyEvent.KEYCODE_BACK)
        ui.waitUntil(5_000) { dismissed }
    }
}
