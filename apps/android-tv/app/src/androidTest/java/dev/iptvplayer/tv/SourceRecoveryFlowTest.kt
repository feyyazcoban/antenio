package dev.iptvplayer.tv

import android.graphics.Bitmap
import android.view.KeyEvent
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.iptvplayer.tv.data.*
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class SourceRecoveryFlowTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()
    private lateinit var model: TvViewModel
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val prefs get() = ui.activity.getSharedPreferences("device_state", 0)
    private val source = SourceConfig("recovery-fixture", "Ev arşivi", SourceType.M3U, "http://127.0.0.1:1/list.m3u")
    private val movie = MediaEntry("movie:1", "1", MediaKind.MOVIE, "Örnek Film", uri = "http://127.0.0.1:1/movie.mp4")
    @Before fun setup() { model = resetFixtureSources(ui); model.store.startupSound = false }
    @After fun cleanup() {
        if (!::model.isInitialized) return
        ui.runOnUiThread { ui.activity.viewModelStore.clear() }
        runBlocking { SourceTransactions.shared.write {
            (ui.activity.application as TvApplication).artworkCache.clear()
            CatalogStore(ui.activity).clear(); model.store.forgetSource()
            model.store.reducedMotion = false; model.store.startupSound = true
        } }
    }
    private fun tag(name: String) = ui.onNodeWithTag(name).performSemanticsAction(SemanticsActions.OnClick) { it() }
    private fun back() { instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK); ui.waitForIdle() }
    private fun reopen() {
        ui.runOnUiThread { ui.activity.viewModelStore.clear() }
        ui.activityRule.scenario.recreate(); ui.waitForIdle()
        ui.runOnUiThread { model = ViewModelProvider(ui.activity)[TvViewModel::class.java] }
        ui.waitUntil(10000) { model.state.value.sourceTask == SourceTask.IDLE }
    }
    private fun records(): String {
        runBlocking { SourceTransactions.shared.write {
            model.store.saveSources(SourceRegistry(listOf(source)))
            CatalogStore(ui.activity).write(source, CatalogSnapshot(Catalog(listOf(movie)), System.currentTimeMillis()))
            model.store.favorites(source.id, setOf(movie.id)); model.store.saveProgress(source.id, movie.id, 17000, 120000)
        } }
        return prefs.getString("sources_v2", null)!!
    }
    private fun corrupt() { prefs.edit().putString("sources_v2", "unreadable-fixture").commit(); reopen(); assertEquals(Route.RECOVERY, model.state.value.route) }
    private fun shot(name: String) {
        ui.waitForIdle()
        val file = File(ui.activity.filesDir, "source-recovery-preview/$name.png").apply { parentFile!!.mkdirs() }
        file.outputStream().use { instrumentation.uiAutomation.takeScreenshot().compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    @Test fun retryAndCancelKeepRecordsAndFocusAndSuccessfulRetryRestoresLibrary() {
        val saved = records(); corrupt()
        ui.onNodeWithTag("retry-source-recovery").assertIsFocused(); shot("recovery")
        tag("retry-source-recovery")
        ui.waitUntil(10000) { model.state.value.route == Route.RECOVERY && model.state.value.sourceTask == SourceTask.IDLE }
        assertEquals("unreadable-fixture", prefs.getString("sources_v2", null))
        tag("open-source-reset")
        ui.onNodeWithTag("cancel-source-reset").assertIsFocused(); shot("confirmation")
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_UP)
        for (index in 0..3) {
            ui.onNodeWithTag("reset-detail:$index").assertIsFocused().assertIsDisplayed()
            val text = ui.onNodeWithTag("reset-detail:$index").getUnclippedBoundsInRoot()
            val viewport = ui.onNodeWithTag("reset-details-viewport").getUnclippedBoundsInRoot()
            assertTrue("Complete explanation paragraph must be readable", text.top >= viewport.top && text.bottom <= viewport.bottom)
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN)
        }
        ui.onNodeWithTag("cancel-source-reset").assertIsFocused()
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER); ui.waitForIdle()
        ui.onNodeWithTag("open-source-reset").assertIsFocused()
        assertEquals(setOf(movie.id), model.store.favorites(source.id))
        tag("open-source-reset"); ui.onNodeWithTag("cancel-source-reset").assertIsFocused(); back()
        ui.onNodeWithTag("open-source-reset").assertIsFocused()
        assertNotNull(CatalogStore(ui.activity).read(source))
        prefs.edit().putString("sources_v2", saved).commit(); tag("retry-source-recovery")
        ui.waitUntil(10000) { model.state.value.route == Route.HOME }
        assertEquals(1, model.state.value.catalog.items.size)
        assertEquals(17000L, model.store.progress(source.id, movie.id)!!.positionMs)
    }

    @Test fun confirmedResetClearsAllSourcesRotatesKeyAndPreservesDevicePreferences() {
        records()
        val sealed = model.store.seal("old-fixture".toByteArray(), "fixture")
        model.store.reducedMotion = true; model.store.startupSound = false
        model.store.audioLanguage = "tr"
        corrupt(); tag("open-source-reset")
        ui.onNodeWithTag("cancel-source-reset").assertIsFocused()
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN); ui.waitForIdle()
        ui.onNodeWithTag("confirm-source-reset").assertIsFocused().assertIsDisplayed()
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.waitUntil(10000) { model.state.value.route == Route.SETUP && model.state.value.sourceTask == SourceTask.IDLE }
        assertTrue(model.store.loadSources().sources.isEmpty()); assertNull(CatalogStore(ui.activity).read(source))
        assertTrue(model.store.favorites(source.id).isEmpty()); assertNull(model.store.progress(source.id, movie.id))
        assertTrue(model.store.reducedMotion); assertFalse(model.store.startupSound); assertEquals("tr", model.store.audioLanguage)
        assertTrue(runCatching { model.store.unseal(sealed, "fixture") }.isFailure)
        runBlocking { SourceTransactions.shared.write { model.store.saveSources(SourceRegistry(listOf(source)))
            CatalogStore(ui.activity).write(source, CatalogSnapshot(Catalog(listOf(movie)), System.currentTimeMillis())) } }
        reopen(); assertEquals(Route.HOME, model.state.value.route)
    }

    @Test fun staleConfirmationCannotClearNewlyRepairedAccounts() {
        val saved = records(); corrupt(); tag("open-source-reset")
        // Simulate another accepted writer finishing after this recovery screen was rendered.
        prefs.edit().putString("sources_v2", saved).commit()
        tag("confirm-source-reset")
        ui.waitUntil(10000) { model.state.value.route == Route.HOME }
        assertEquals(listOf(source), model.store.loadSources().sources)
        assertEquals(17000L, model.store.progress(source.id, movie.id)!!.positionMs)
        assertNotNull(CatalogStore(ui.activity).read(source))
    }

    @Test fun malformedLegacyAndWrongPreferenceTypeHaveRecoveryWithoutSilentOverwrite() {
        records(); prefs.edit().remove("sources_v2").putString("source", "bad-legacy-fixture").commit()
        reopen(); assertEquals(Route.RECOVERY, model.state.value.route); assertTrue(model.state.value.recoveryCanReset)
        assertEquals("bad-legacy-fixture", prefs.getString("source", null))
        prefs.edit().putInt("sources_v2", 17).commit()
        tag("retry-source-recovery")
        ui.waitUntil(10000) { model.state.value.route == Route.RECOVERY && model.state.value.sourceTask == SourceTask.IDLE }
        assertTrue(model.state.value.recoveryCanReset); assertEquals(17, prefs.getInt("sources_v2", 0))
        tag("open-source-reset"); tag("confirm-source-reset")
        ui.waitUntil(10000) { model.state.value.route == Route.SETUP }
        assertFalse(prefs.contains("source")); assertFalse(prefs.contains("sources_v2"))
    }
}
