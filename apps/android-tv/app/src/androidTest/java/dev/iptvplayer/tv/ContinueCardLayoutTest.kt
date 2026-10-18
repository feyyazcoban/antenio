package dev.iptvplayer.tv

import android.graphics.Bitmap
import android.view.KeyEvent
import androidx.lifecycle.ViewModelProvider
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import dev.iptvplayer.tv.data.*
import dev.iptvplayer.tv.ui.*
import org.junit.*
import org.junit.Assert.*
import kotlinx.coroutines.runBlocking

/** Emulator-only encrypted fixtures, restored through the actual app and removed after the test. */
class ContinueCardLayoutTest {
    @get:Rule(order = 0) val turkish = TurkishLocaleRule()
    @get:Rule(order = 1) val ui = createAndroidComposeRule<MainActivity>()
    @After fun cleanup() { resetFixtureSources(ui) }
    @Test fun longEpisodeAndSourceKeepResumeTimeReadableAcrossShelf() {
        var model = resetFixtureSources(ui)
        val sources = (1..2).map { SourceConfig("layout-$it", "Aile arşivi ve belgesel koleksiyonu $it", SourceType.M3U, "http://127.0.0.1:1/list.m3u") }
        val entries = (1..8).map { MediaEntry("series:$it", "$it", MediaKind.SERIES,
            "Uzun Bir Yolculuğun Ardından: Dünyanın Bilinmeyen Köşeleri $it", "Belgeseller", sourceId = sources[(it-1)%2].id) }
        val records = entries.map { WatchRecord("episode:${it.providerId}", MediaKind.EPISODE, parentId = it.id,
            season = 120, episode = 350, positionMs = 4_956_000, durationMs = 9_000_000, sourceId = it.sourceId) }
        ui.runOnUiThread { ui.activity.viewModelStore.clear() }
        runBlocking { SourceTransactions.shared.write {
            val store = DeviceStore(ui.activity)
            sources.forEach { source ->
                CatalogStore(ui.activity).write(source, CatalogSnapshot(Catalog(entries.filter { it.sourceId == source.id }.map { it.copy(sourceId = "") }), System.currentTimeMillis()))
                store.history(source.id, records.filter { it.sourceId == source.id }.map { it.copy(sourceId = "") })
            }
            store.saveSources(SourceRegistry(sources))
        } }
        ui.activityRule.scenario.recreate()
        ui.runOnUiThread { model = ViewModelProvider(ui.activity)[TvViewModel::class.java] }
        ui.waitUntil(10_000) { model.state.value.route == Route.HOME && model.state.value.sourceTask == SourceTask.IDLE }
        ui.waitForIdle()
        val restored = homeLibrary(model.state.value.catalog, model.state.value.history, emptySet()).continueWatching.map { it.entry }
        assertEquals(8, restored.size)
        // Home opens on the hero when there is one (D070); Down reaches the continue shelf.
        if (ui.onAllNodesWithTag("hero-watch").fetchSemanticsNodes().isNotEmpty()) {
            ui.onNodeWithTag("hero-watch").assertIsFocused()
            val heroBefore = ui.onNodeWithTag("home-hero").getUnclippedBoundsInRoot()
            val viewportBefore = ui.onNodeWithTag("home-content").getUnclippedBoundsInRoot()
            InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN); ui.waitForIdle()
            val heroAfter = ui.onNodeWithTag("home-hero").getUnclippedBoundsInRoot()
            // A full-screen hero leaves the first shelf uncomposed until D-pad beyond-bounds search reaches it.
            val firstAfter = ui.onNodeWithTag("continue:${restored.first().id}").assertIsFocused().getUnclippedBoundsInRoot()
            assertTrue("Down scrolls away from the full-screen hero", heroAfter.top < heroBefore.top)
            assertEquals("Only reveal the continue card with its 27dp margin, never pin the shelf",
                viewportBefore.bottom.value - 27f, firstAfter.bottom.value, 4f)
            val heading = ui.onNodeWithTag("shelf-heading:continue").getUnclippedBoundsInRoot()
            assertTrue("Continue heading stays visible", heading.top >= viewportBefore.top)
        }
        repeat(restored.size) { index ->
            val card = ui.onNodeWithTag("continue:${restored[index].id}").assertIsFocused()
            val bounds = card.getUnclippedBoundsInRoot()
            val viewport = ui.onNodeWithTag("home-content").getUnclippedBoundsInRoot()
            assertTrue("Whole focused card must fit", bounds.top >= viewport.top && bounds.bottom <= viewport.bottom && bounds.left.value >= 7 && bounds.right.value <= viewport.right.value - 7)
            // The card shows what is left (U18, D065): 9 000 s − 4 956 s is 67 min 24 s, rounded up to whole minutes.
            val time = ui.onNode(hasText("1 sa 8 dk kaldı") and hasAnyAncestor(hasTestTag("continue:${restored[index].id}")), useUnmergedTree = true)
            val layouts = mutableListOf<TextLayoutResult>()
            time.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
            assertTrue(layouts.isNotEmpty())
            assertFalse("Remaining time must not be ellipsized", layouts.single().isLineEllipsized(0))
            if (index == 0 || index == entries.lastIndex) {
                Thread.sleep(1200); ui.waitForIdle()
                val dir=ui.activity.filesDir.resolve("continue-layout").apply { mkdirs() }
                InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot().let { bitmap ->
                    dir.resolve("card-$index.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) }; bitmap.recycle()
                }
            }
            if(index < entries.lastIndex) {
                InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT)
                ui.waitForIdle()
            }
        }
    }
}
