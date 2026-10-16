package dev.iptvplayer.tv

import android.graphics.Bitmap
import android.view.KeyEvent
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import dev.iptvplayer.tv.data.*
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import java.io.File

class SetupLayoutTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()
    private lateinit var model: TvViewModel
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    @Before fun setup() { model = resetFixtureSources(ui); model.store.startupSound = false }
    @After fun cleanup() {
        ui.runOnUiThread { ui.activity.viewModelStore.clear() }
        runBlocking { SourceTransactions.shared.write {
            model.store.forgetSource(); CatalogStore(ui.activity).clear(); model.store.startupSound = true
        } }
    }
    private fun shot(name: String) {
        ui.waitForIdle(); Thread.sleep(1200); ui.waitForIdle()
        val file = File(ui.activity.filesDir, "setup-preview/$name.png").apply { parentFile!!.mkdirs() }
        file.outputStream().use { instrumentation.uiAutomation.takeScreenshot().compress(Bitmap.CompressFormat.PNG,100,it) }
    }
    private fun visible(tag: String) {
        val rect = ui.onNodeWithTag(tag).getUnclippedBoundsInRoot()
        val root = ui.onRoot().getUnclippedBoundsInRoot()
        assertTrue("$tag must fit completely", rect.top >= root.top && rect.bottom <= root.bottom && rect.left >= root.left && rect.right <= root.right)
    }
    @Test fun sourceTypesAndValidationKeepPrimaryActionVisible() {
        ui.waitUntil(10000) { model.state.value.route == Route.SETUP }
        ui.onNodeWithTag("setup-xtream").assertIsFocused()
        ui.onNodeWithTag("setup-xtream").assertIsSelected()
        ui.onNodeWithTag("setup-m3u").assertIsNotSelected()
        visible("setup-connect"); shot("xtream")
        repeat(5) {
            ui.runOnUiThread {
                val ime = ui.activity.getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                ime.hideSoftInputFromWindow(ui.activity.window.decorView.windowToken, 0)
            }
            Thread.sleep(400)
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN); ui.waitForIdle()
            ui.onNode(isFocused()).assertIsDisplayed()
            val bounds = ui.onNode(isFocused()).getUnclippedBoundsInRoot()
            val root = ui.onNodeWithTag("setup-fields").getUnclippedBoundsInRoot()
            assertTrue("Focused entry must remain fully visible", bounds.top >= root.top && bounds.bottom <= root.bottom)
        }
        ui.runOnUiThread {
            val ime = ui.activity.getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            ime.hideSoftInputFromWindow(ui.activity.window.decorView.windowToken, 0)
        }
        Thread.sleep(400)
        shot("last-field")
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN); ui.waitForIdle()
        ui.onNodeWithTag("setup-connect").assertIsFocused()
        ui.onNodeWithTag("setup-xtream").performSemanticsAction(SemanticsActions.RequestFocus)
        ui.waitForIdle()
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT); ui.waitForIdle()
        ui.onNodeWithTag("setup-m3u").assertIsFocused()
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER); ui.waitForIdle()
        assertEquals(SourceType.M3U, model.state.value.draft.type)
        ui.onNodeWithTag("setup-m3u").assertIsSelected()
        ui.onNodeWithTag("setup-xtream").assertIsNotSelected()
        shot("m3u")
        ui.onNodeWithTag("setup-connect").performSemanticsAction(SemanticsActions.RequestFocus)
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.waitUntil(5000) { model.state.value.error != null }
        visible("setup-error"); visible("setup-connect")
        ui.onNodeWithTag("setup-connect").assertIsFocused(); shot("validation")
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_UP); ui.waitForIdle()
        ui.onNode(isFocused()).assertIsDisplayed()
    }
}
