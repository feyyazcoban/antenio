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
    private fun key(code: Int) { instrumentation.sendKeyDownUpSync(code); ui.waitForIdle() }

    /** The new flow (SETUP_FLOW, D117): welcome, the remote way one question at a time, and the phone way's QR screen. */
    @Test fun setupFlowStepsKeepFieldAndActionsVisible() {
        ui.waitUntil(10000) { model.state.value.route == Route.SETUP }
        ui.waitUntil(5000) { runCatching { ui.onNodeWithTag("setup-phone").assertIsFocused() }.isSuccess }
        visible("setup-remote"); shot("welcome")
        key(KeyEvent.KEYCODE_DPAD_DOWN)
        ui.onNodeWithTag("setup-remote").assertIsFocused()
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.waitUntil(5000) { runCatching { ui.onNodeWithTag("setup-m3u").assertIsFocused() }.isSuccess }
        shot("type")
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        assertEquals(SourceType.M3U, model.state.value.draft.type)
        ui.waitUntil(5000) { runCatching { ui.onNodeWithTag("setup-field").assertIsFocused() }.isSuccess }
        visible("setup-field"); visible("setup-next"); shot("address")
        // An empty link keeps the step and says so under the field.
        ui.onNodeWithTag("setup-next").performSemanticsAction(SemanticsActions.RequestFocus)
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.onNodeWithTag("setup-error").assertIsDisplayed(); visible("setup-error"); shot("validation")
        // Back returns to the type, then to the welcome.
        key(KeyEvent.KEYCODE_BACK)
        ui.waitUntil(5000) { runCatching { ui.onNodeWithTag("setup-m3u").assertIsDisplayed() }.isSuccess }
        key(KeyEvent.KEYCODE_BACK)
        ui.waitUntil(5000) { runCatching { ui.onNodeWithTag("setup-phone").assertIsFocused() }.isSuccess }
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.waitUntil(10000) { runCatching { ui.onNodeWithTag("setup-qr").assertIsDisplayed() }.isSuccess ||
            runCatching { ui.onNodeWithTag("setup-phone-offline").assertIsDisplayed() }.isSuccess }
        visible("setup-new-code"); shot("phone")
        key(KeyEvent.KEYCODE_BACK)
        ui.waitUntil(5000) { runCatching { ui.onNodeWithTag("setup-phone").assertIsFocused() }.isSuccess }
    }
}
