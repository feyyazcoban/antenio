package dev.iptvplayer.tv

import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.iptvplayer.tv.data.*
import org.json.JSONObject
import org.junit.*
import org.junit.runner.RunWith
import java.io.File

/** Opt-in, private device-only input. Excluded from the shipping APK. See README. */
@RunWith(AndroidJUnit4::class)
class ProviderSmokeTest {
    @get:Rule(order = 0) val turkish = TurkishLocaleRule()
    @get:Rule(order = 1) val ui = createAndroidComposeRule<MainActivity>()

    @Test fun privateProviderCatalogAndSequentialPlayback() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        Assume.assumeTrue("Private provider test is opt-in", InstrumentationRegistry.getArguments().getString("privateProvider") == "true")
        val input = File(ui.activity.filesDir, "provider-test.json")
        Assume.assumeTrue("Private input is required", input.isFile)
        val json = JSONObject(input.readText())
        val model = ViewModelProvider(ui.activity)[TvViewModel::class.java]
        val result = JSONObject()
        try {
            ui.runOnUiThread {
                model.draft(SourceDraft("Özel test", SourceType.XTREAM, json.getString("base"), json.getString("username"), json.getString("password")))
                model.connectDraft()
            }
            ui.waitUntil(180_000) { model.state.value.route != Route.LOADING }
            Assert.assertEquals("Provider catalog unavailable", Route.HOME, model.state.value.route)
            result.put("catalogCount", model.state.value.catalog.items.size)
            ui.waitForIdle()
            Thread.sleep(3500)
            instrumentation.uiAutomation.takeScreenshot()?.let { bitmap ->
                File(ui.activity.filesDir, "native-home.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                bitmap.recycle()
            }
            val picks = json.getJSONArray("samples")
            for (i in 0 until picks.length()) {
                val pick = picks.getJSONObject(i)
                val kind = MediaKind.valueOf(pick.getString("kind"))
                val entry = model.state.value.catalog.items.first { it.kind == kind && it.providerId == pick.getString("id") && !it.adult }
                ui.runOnUiThread { model.showKind(kind) }
                ui.waitForIdle()
                if (kind == MediaKind.MOVIE) {
                    // Library screen has no account details or playback URLs.
                    Thread.sleep(3000)
                    instrumentation.uiAutomation.takeScreenshot()?.let { bitmap ->
                        File(ui.activity.filesDir, "native-library.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                        bitmap.recycle()
                    }
                }
                ui.runOnUiThread { model.open(entry) }
                if (kind != MediaKind.LIVE) {
                    ui.waitUntil(45_000) { !model.state.value.detailLoading }
                    ui.runOnUiThread { model.play(if (kind == MediaKind.SERIES) model.state.value.episodes.first() else model.state.value.selected!!) }
                }
                ui.waitUntil(45_000) { ui.onAllNodesWithText("Duraklat").fetchSemanticsNodes().isNotEmpty() ||
                    ui.onAllNodesWithText("Tekrar dene").fetchSemanticsNodes().isNotEmpty() }
                ui.onNodeWithText("Duraklat").assertExists()
                ui.onNodeWithText("Ses / Altyazı").performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.OnClick) { it() }
                ui.onNodeWithText("Ses dili").assertExists()
                Thread.sleep(2000)
                result.put(kind.name, "playing")
                instrumentation.sendKeyDownUpSync(android.view.KeyEvent.KEYCODE_BACK)
                ui.runOnUiThread { model.back() }
                ui.waitUntil(10_000) { model.state.value.route != Route.PLAYER }
                if (kind != MediaKind.LIVE) ui.runOnUiThread { model.back() }
            }
        } finally {
            File(ui.activity.filesDir, "provider-result.json").writeText(result.toString(2))
            ui.runOnUiThread { model.back(); model.removeSource() }
            input.delete()
        }
    }
}

