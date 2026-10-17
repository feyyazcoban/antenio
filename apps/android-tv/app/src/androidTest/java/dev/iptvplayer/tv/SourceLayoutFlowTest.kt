package dev.iptvplayer.tv

import android.graphics.Bitmap
import android.view.KeyEvent
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.iptvplayer.tv.data.*
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class SourceLayoutFlowTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()
    private lateinit var model: TvViewModel
    private lateinit var sources: List<SourceConfig>
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    @Before fun setup() {
        model = resetFixtureSources(ui)
        sources = (1..16).map { index -> SourceConfig("layout-$index",
            "Aile arşivimiz ve salon televizyonunun bütün film, dizi, belgesel ve spor kanalları · " +
                if (index < 15) "Koleksiyon $index" else "Ortak koleksiyon",
            SourceType.M3U, "http://127.0.0.1:1/source-$index.m3u") }
        runBlocking { SourceTransactions.shared.write {
            model.store.saveSources(SourceRegistry(sources))
            sources.forEach { source -> CatalogStore(ui.activity).write(source,
                CatalogSnapshot(Catalog(listOf(MediaEntry("movie:1", "1", MediaKind.MOVIE, "Örnek Film",
                    category = "Filmler", uri = "http://127.0.0.1:1/fixture.mp4"))), System.currentTimeMillis())) }
        } }
        ui.runOnUiThread { model.restore() }
        ui.waitUntil(10000) { model.state.value.route == Route.HOME && model.state.value.sources.size == 16 }
    }
    @After fun cleanup() {
        if (!::model.isInitialized) return
        ui.runOnUiThread { ui.activity.viewModelStore.clear() }
        runBlocking { SourceTransactions.shared.write {
            (ui.activity.application as TvApplication).artworkCache.clear()
            CatalogStore(ui.activity).clear(); model.store.forgetSource()
        } }
    }
    private fun key(code: Int) { instrumentation.sendKeyDownUpSync(code); ui.waitForIdle() }
    private fun open(tag: String) {
        ui.onNodeWithTag(tag).performSemanticsAction(SemanticsActions.RequestFocus) { it() }
        key(KeyEvent.KEYCODE_DPAD_CENTER)
    }
    private fun shot(name: String) {
        ui.waitForIdle()
        // Compose idle does not include Android's separate dialog-window exit animation.
        // Settle the screenshot only; focus/behavior assertions remain independent of this delay.
        android.os.SystemClock.sleep(400)
        val file = File(ui.activity.filesDir, "source-layout-preview/$name.png").apply { parentFile!!.mkdirs() }
        file.outputStream().use { instrumentation.uiAutomation.takeScreenshot().compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    @Test fun deepManagerRestoresEditAndRemovalCancelFocus() {
        val source = sources[13]
        ui.runOnUiThread { model.manageSources(); model.editSource(source.id); model.back() }
        ui.onNodeWithTag("edit-source:${source.id}").assertIsFocused().assertIsDisplayed()
        shot("manager-deep")
        key(KeyEvent.KEYCODE_DPAD_RIGHT)
        ui.onNodeWithTag("remove-source:${source.id}").assertIsFocused()
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.onNodeWithTag("cancel-source-removal").assertIsFocused()
        shot("removal-long-name")
        ui.onNodeWithTag("confirm-source-removal").assertIsDisplayed()
        fullyInsideDialog("cancel-source-removal"); fullyInsideDialog("confirm-source-removal")
        key(KeyEvent.KEYCODE_DPAD_UP)
        readToEnd("source-removal-reader")
        key(KeyEvent.KEYCODE_DPAD_DOWN)
        ui.onNodeWithTag("cancel-source-removal").assertIsFocused()
        key(KeyEvent.KEYCODE_BACK)
        ui.onNodeWithTag("remove-source:${source.id}").assertIsFocused()
        assertEquals(16, model.store.loadSources().sources.size)
        repeat(3) { key(KeyEvent.KEYCODE_DPAD_LEFT) }
        ui.onNodeWithTag("info-source:${source.id}").assertIsFocused()
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        readToEnd("source-info-reader")
        shot("source-info")
        key(KeyEvent.KEYCODE_BACK)
        ui.onNodeWithTag("info-source:${source.id}").assertIsFocused()
    }

    @Test fun veryLongNameCanBeReadBothWaysWithoutLosingCloseOrCancel() {
        val source = sources.last().copy(name = "Çocukların ve ailenin belgesel, film ve dizi arşivi. ".repeat(20) + "SON BÖLÜM")
        runBlocking { SourceTransactions.shared.write { model.store.saveSources(model.store.loadSources().upsert(source)) } }
        ui.runOnUiThread { model.restore() }
        ui.waitUntil(10000) { model.state.value.route == Route.HOME && model.state.value.sources.last().name == source.name }
        ui.runOnUiThread { model.manageSources(); model.editSource(source.id); model.back() }
        ui.onNodeWithTag("edit-source:${source.id}").assertIsFocused()
        key(KeyEvent.KEYCODE_DPAD_LEFT); key(KeyEvent.KEYCODE_DPAD_LEFT)
        ui.onNodeWithTag("info-source:${source.id}").assertIsFocused()
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        readToEnd("source-info-reader"); fullyInsideDialog("close-source-info")
        key(KeyEvent.KEYCODE_DPAD_DOWN); ui.onNodeWithTag("source-guide-settings").assertIsFocused()
        key(KeyEvent.KEYCODE_DPAD_DOWN); ui.onNodeWithTag("close-source-info").assertIsFocused()
        key(KeyEvent.KEYCODE_DPAD_UP); ui.onNodeWithTag("source-guide-settings").assertIsFocused()
        key(KeyEvent.KEYCODE_DPAD_UP); ui.onNodeWithTag("source-info-reader").assertIsFocused()
        repeat(30) {
            if (ui.onNodeWithTag("source-info-reader").fetchSemanticsNode().config[SemanticsProperties.StateDescription] != "Metnin başı")
                key(KeyEvent.KEYCODE_DPAD_UP)
        }
        ui.onNodeWithTag("source-info-reader").assertIsFocused()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Metnin başı"))
        shot("full-name-reader")
        key(KeyEvent.KEYCODE_BACK); ui.onNodeWithTag("info-source:${source.id}").assertIsFocused()
        repeat(3) { key(KeyEvent.KEYCODE_DPAD_RIGHT) }
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.onNodeWithTag("cancel-source-removal").assertIsFocused()
        fullyInsideDialog("cancel-source-removal"); fullyInsideDialog("confirm-source-removal")
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        assertEquals(16, model.store.loadSources().sources.size)
    }

    @Test fun managerKeepsHeaderAlignedAndAllRemoteActionsVisible() {
        ui.runOnUiThread { model.manageSources() }
        ui.onNodeWithTag("add-source").assertIsFocused()
        val header = ui.onNodeWithTag("sources-header").getUnclippedBoundsInRoot()
        val title = ui.onNodeWithTag("sources-title").getUnclippedBoundsInRoot()
        key(KeyEvent.KEYCODE_DPAD_DOWN)
        ui.onNodeWithTag("info-source:${sources.first().id}").assertIsFocused()
        repeat(3) { key(KeyEvent.KEYCODE_DPAD_RIGHT) }
        sources.forEachIndexed { index, source ->
            ui.onNodeWithTag("remove-source:${source.id}").assertIsFocused()
            val card = ui.onNodeWithTag("source:${source.id}").getUnclippedBoundsInRoot()
            val viewport = ui.onNodeWithTag("sources-list").getUnclippedBoundsInRoot()
            assertEquals("Card and heading share the leading edge", title.left, card.left)
            assertEquals(header, ui.onNodeWithTag("sources-header").getUnclippedBoundsInRoot())
            assertTrue("Full source card remains visible", card.top >= viewport.top && card.bottom <= viewport.bottom)
            listOf("info", "refresh", "edit", "remove").forEach { action ->
                val control = ui.onNodeWithTag("$action-source:${source.id}").getUnclippedBoundsInRoot()
                assertTrue("Action fits card: $action", control.left >= card.left && control.right <= card.right && control.bottom <= card.bottom)
            }
            if (index == 0) shot("manager-first")
            if (index < sources.lastIndex) key(KeyEvent.KEYCODE_DPAD_DOWN)
        }
        shot("manager-last")
        key(KeyEvent.KEYCODE_BACK)
        assertEquals(Route.HOME, model.state.value.route)
    }

    private fun fullyInsideDialog(tag: String) {
        val control = ui.onNodeWithTag(tag).getUnclippedBoundsInRoot()
        val dialog = ui.onNodeWithTag("source-dialog").getUnclippedBoundsInRoot()
        assertTrue("Control must fit inside dialog: $tag", control.top >= dialog.top && control.bottom <= dialog.bottom &&
            control.left >= dialog.left && control.right <= dialog.right)
    }

    @Test fun failedLongSourceKeepsIdentityAndRemoteActionsInsideTheList() {
        val source = sources[13]
        runBlocking { SourceTransactions.shared.write {
            val cache = CatalogStore(ui.activity)
            cache.write(source, cache.read(source)!!.copy(updatedAt = 0))
        } }
        ui.runOnUiThread { model.restore() }
        ui.waitUntil(15000) { model.state.value.route == Route.HOME && !model.state.value.refreshing && model.state.value.sourceStatus[source.id]?.failed == true }
        ui.runOnUiThread { model.manageSources(); model.editSource(source.id); model.back() }
        ui.onNodeWithTag("edit-source:${source.id}").assertIsFocused()
        val card = ui.onNodeWithTag("source:${source.id}").getUnclippedBoundsInRoot()
        val viewport = ui.onNodeWithTag("sources-list").getUnclippedBoundsInRoot()
        assertTrue("Focused source identity, status and actions must remain visible", card.top >= viewport.top && card.bottom <= viewport.bottom)
        shot("failed-source")
        key(KeyEvent.KEYCODE_DPAD_RIGHT); ui.onNodeWithTag("remove-source:${source.id}").assertIsFocused()
        key(KeyEvent.KEYCODE_DPAD_DOWN); ui.onNodeWithTag("remove-source:${sources[14].id}").assertIsFocused()
        key(KeyEvent.KEYCODE_DPAD_UP); ui.onNodeWithTag("remove-source:${source.id}").assertIsFocused()
        repeat(3) { key(KeyEvent.KEYCODE_DPAD_LEFT) }
        key(KeyEvent.KEYCODE_DPAD_CENTER); readToEnd("source-info-reader")
        ui.onNodeWithTag("source-info-reader").assert(hasAnyDescendant(hasText(model.state.value.sourceStatus[source.id]!!.notices.joinToString(" ") { it.resolve(ui.activity.resources) }, substring = true)))
        key(KeyEvent.KEYCODE_BACK); ui.onNodeWithTag("info-source:${source.id}").assertIsFocused()
    }

    private fun readToEnd(tag: String) {
        ui.onNodeWithTag(tag).assertIsFocused()
        repeat(30) {
            val state = ui.onNodeWithTag(tag).fetchSemanticsNode().config[SemanticsProperties.StateDescription]
            if (state == "Metnin sonu" || state == "Metnin tamamı görünüyor") return
            key(KeyEvent.KEYCODE_DPAD_DOWN)
            ui.onNodeWithTag(tag).assertIsFocused()
        }
        fail("The entire text must be reachable by remote")
    }
}
