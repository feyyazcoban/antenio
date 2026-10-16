package dev.iptvplayer.tv

import android.graphics.Bitmap
import android.view.KeyEvent
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import dev.iptvplayer.tv.ui.Ink
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import dev.iptvplayer.tv.ui.HomeScreen
import dev.iptvplayer.tv.ui.PilotTheme
import org.junit.*
import org.junit.Assert.*

class EmptyHomeLayoutTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()
    @Test fun emptyAndFailedHomeKeepSourcesReachable() {
        val model = resetFixtureSources(ui)
        val state = mutableStateOf(TvState(route = Route.HOME))
        ui.runOnUiThread { ui.activity.setContent { PilotTheme { Box(Modifier.fillMaxSize().background(Ink.Background)) { HomeScreen(state.value, model) {} } } } }
        for (failure in listOf(false,true)) {
            ui.runOnUiThread { state.value = TvState(route = Route.HOME, catalogNotice = if(failure)
                CatalogNotice("Kaynak yenilenemedi. Bağlantını ve hesap bilgilerini kontrol edip yeniden deneyebilirsin. Kayıtlı kaynak bilgilerin korunuyor.", CatalogOperation.REFRESH, true) else null) }
            ui.waitForIdle()
            ui.onNodeWithText(if(failure) "İçerikler gösterilemiyor" else "Kütüphanen için yer hazır").assertExists()
            // Home has no source selector; the sources are one action away.
            ui.onNodeWithTag("library-source-filter").assertDoesNotExist()
            ui.onNodeWithTag("home-empty-action").performSemanticsAction(SemanticsActions.RequestFocus)
            ui.waitForIdle(); ui.onNodeWithTag("home-empty-action").assertIsFocused()
            val bounds = ui.onNodeWithTag("home-empty-action").getUnclippedBoundsInRoot()
            val root = ui.onRoot().getUnclippedBoundsInRoot()
            assertTrue(bounds.top >= root.top && bounds.bottom <= root.bottom)
            Thread.sleep(1200); ui.waitForIdle()
            val dir = ui.activity.filesDir.resolve("empty-home-preview").apply { mkdirs() }
            InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot().let { bitmap ->
                dir.resolve(if(failure) "failed-home.png" else "empty-home.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) };bitmap.recycle()
            }
        }
        InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.waitUntil(5000) { model.state.value.route == Route.SOURCES }
    }
}
