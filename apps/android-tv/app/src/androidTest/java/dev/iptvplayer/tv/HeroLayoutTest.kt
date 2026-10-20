package dev.iptvplayer.tv

import android.graphics.Bitmap
import android.view.KeyEvent
import androidx.lifecycle.ViewModelProvider
import androidx.compose.ui.test.*
import androidx.test.platform.app.InstrumentationRegistry
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.iptvplayer.tv.data.*
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*

class HeroLayoutTest {
    @get:Rule(order = 0) val turkish = TurkishLocaleRule()
    @get:Rule(order = 1) val ui = createAndroidComposeRule<MainActivity>()
    @After fun cleanup() { DeviceStore(ui.activity).reducedMotion = false; resetFixtureSources(ui) }

    /** Home with [entries] as the only (local) source's catalog. */
    private fun home(entries: List<MediaEntry>): TvViewModel {
        var model = resetFixtureSources(ui)
        val source = SourceConfig("hero-layout", "Yerel deneme", SourceType.M3U, "http://127.0.0.1:1/list.m3u")
        ui.runOnUiThread { ui.activity.viewModelStore.clear() }
        runBlocking { SourceTransactions.shared.write {
            CatalogStore(ui.activity).write(source, CatalogSnapshot(Catalog(entries), System.currentTimeMillis()))
            DeviceStore(ui.activity).saveSources(SourceRegistry(listOf(source)))
        } }
        ui.activityRule.scenario.recreate()
        ui.runOnUiThread { model = ViewModelProvider(ui.activity)[TvViewModel::class.java] }
        ui.waitUntil(10_000) { model.state.value.route == Route.HOME && model.state.value.sourceTask == SourceTask.IDLE }
        ui.waitForIdle()
        return model
    }

    @Test fun newestTitlesTurnWithRightAndLeftAndLeftOnTheFirstReachesTheMenu() {
        // Reduced motion stops the 8 s timer, so only the remote turns the slides here.
        DeviceStore(ui.activity).reducedMotion = true
        home((1..3).map { MediaEntry("movie:$it", "$it", MediaKind.MOVIE, "Yeni film $it", "Belgeseller",
            description = if (it == 2) "Uzun açıklaması olan başka bir slayt. ".repeat(8) else "",
            artwork = "http://127.0.0.1:1/poster-$it.jpg", addedAt = 1_000L + it, uri = "http://127.0.0.1:1/movie.mp4") })
        // D079: İzle is the only action; Right/Left turn slides without moving focus.
        ui.onNodeWithTag("hero-watch").assertIsFocused()
        ui.onNodeWithTag("hero-details").assertDoesNotExist()
        ui.onNodeWithTag("hero-label", useUnmergedTree = true).assertTextEquals("YENİ EKLENEN")
        ui.onNodeWithTag("hero-dots", useUnmergedTree = true).assertExists()
        fun title() = ui.onNodeWithTag("hero-title", useUnmergedTree = true)
        title().assertTextEquals("Yeni film 3")
        val label = ui.onNodeWithTag("hero-label", useUnmergedTree = true).getUnclippedBoundsInRoot()
        val heading = title().getUnclippedBoundsInRoot()
        assertEquals("A taller hidden slide must not separate the label from the visible title",
            10f, (heading.top - label.bottom).value, 1f)
        val hero = ui.onNodeWithTag("home-hero").getUnclippedBoundsInRoot()
        val watch = ui.onNodeWithTag("hero-watch").getUnclippedBoundsInRoot()
        val keys = InstrumentationRegistry.getInstrumentation()
        keys.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT); ui.waitForIdle()
        title().assertTextEquals("Yeni film 2")
        val description = ui.onNodeWithTag("hero-description", useUnmergedTree = true).getUnclippedBoundsInRoot()
        assertTrue("Description has breathing room before the watch action",
            ui.onNodeWithTag("hero-watch").getUnclippedBoundsInRoot().top - description.bottom >= androidx.compose.ui.unit.Dp(28f))
        assertEquals("Turning to a taller slide keeps the shelf boundary stable", hero,
            ui.onNodeWithTag("home-hero").getUnclippedBoundsInRoot())
        assertEquals("Turning slides keeps the watch action stable", watch,
            ui.onNodeWithTag("hero-watch").getUnclippedBoundsInRoot())
        ui.onNodeWithTag("hero-watch").assertIsFocused()
        keys.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT); keys.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT); ui.waitForIdle()
        title().assertTextEquals("Yeni film 3")  // the last slide wraps to the first
        keys.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT); ui.waitForIdle()
        title().assertTextEquals("Yeni film 2")
        keys.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_LEFT); ui.waitForIdle()
        ui.onNodeWithTag("hero-watch").assertIsFocused()
        title().assertTextEquals("Yeni film 3")
        ui.onNodeWithTag("hero-watch").assertIsFocused()
        keys.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_LEFT); ui.waitForIdle()
        ui.onNodeWithTag("nav:Ana Sayfa").assertIsFocused()
        keys.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT); ui.waitForIdle()
        ui.onNodeWithTag("hero-watch").assertIsFocused()
        title().assertTextEquals("Yeni film 3")
    }

    @Test fun aSingleCompactShelfDoesNotCreateBlankScrollingAndKeepsItsFocus() {
        val model = home(listOf(MediaEntry("live:1", "1", MediaKind.LIVE, "Tek kanal", "Kanallar",
            uri = "http://127.0.0.1:1/live.ts")))
        val entry = model.state.value.catalog.items.single()
        ui.runOnUiThread { model.recordViewed(entry) }
        ui.waitForIdle()
        ui.activityRule.scenario.recreate()
        ui.waitForIdle()
        val tag = "recent:${entry.id}"
        ui.onNodeWithTag(tag).performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.RequestFocus) { it() }
        ui.waitForIdle()
        val range = ui.onNodeWithTag("home-content").fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsProperties.VerticalScrollAxisRange]
        assertEquals("Short content must not acquire an artificial scroll tail", 0f, range.maxValue(), .01f)
        for (key in listOf(KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT)) {
            InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(key); ui.waitForIdle()
            ui.onNodeWithTag(tag).assertIsFocused()
        }
        InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_LEFT); ui.waitForIdle()
        ui.onNodeWithTag("nav:Ana Sayfa").assertIsFocused()
        InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT); ui.waitForIdle()
        ui.onNodeWithTag(tag).assertIsFocused()
    }

    @Test fun longTitleWithoutArtworkKeepsHeaderAndSingleActionVisible() {
        home((1..6).map { MediaEntry("movie:$it", "$it", MediaKind.MOVIE,
            "Uzun Bir Yolculuğun Ardından: Dünyanın Bilinmeyen Köşeleri $it", "Belgeseller", year = "2026",
            description = "Uzak kıyılardan dağlara uzanan bir yolculuk. ".repeat(12), uri = "http://127.0.0.1:1/movie.mp4") })
        ui.onNodeWithTag("hero-watch").assertIsFocused()
        val hero = ui.onNodeWithTag("home-hero").getUnclippedBoundsInRoot()
        val viewport = ui.onNodeWithTag("home-content").getUnclippedBoundsInRoot()
        assertTrue("Full hero fits when its action is focused: $hero / $viewport", hero.top >= viewport.top && hero.bottom <= viewport.bottom)
        for(tag in listOf("hero-label","hero-title","hero-watch")) {
            val b=ui.onNodeWithTag(tag,useUnmergedTree=true).getUnclippedBoundsInRoot()
            assertTrue("$tag fits inside hero", b.left >= hero.left && b.right <= hero.right && b.top >= hero.top && b.bottom <= hero.bottom)
        }
        assertEquals("Hero reaches the left screen edge", 0f, hero.left.value, 1f)
        assertEquals("Hero fills viewport width", (viewport.right - viewport.left).value, (hero.right - hero.left).value, 1f)
        InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN); ui.waitForIdle()
        ui.onNodeWithTag("hero-watch").assertIsNotFocused()
        assertTrue("Down scrolls into a shelf", ui.onNodeWithTag("home-hero").getUnclippedBoundsInRoot().top < viewport.top)
        InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_UP); ui.waitForIdle()
        ui.onNodeWithTag("hero-watch").assertIsFocused()
        assertEquals("Returning to hero restores its top edge", viewport.top.value,
            ui.onNodeWithTag("home-hero").getUnclippedBoundsInRoot().top.value, 1f)
        val title=ui.onNodeWithTag("hero-title",useUnmergedTree=true).getUnclippedBoundsInRoot()
        val label=ui.onNodeWithTag("hero-label",useUnmergedTree=true).getUnclippedBoundsInRoot()
        assertTrue("The label keeps its own header line",label.bottom <= title.top)
        // Home has no source selector (it stays in the libraries and Ara).
        ui.onNodeWithTag("library-source-filter").assertDoesNotExist()
        // This fixture has one featured slide; Right keeps İzle focused, never a card in the first shelf.
        ui.onNodeWithTag("hero-live").assertDoesNotExist()
        ui.onNodeWithTag("hero-details").assertDoesNotExist()
        ui.onNodeWithTag("hero-dots").assertDoesNotExist()
        InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT)
        ui.waitForIdle()
        ui.onNodeWithTag("hero-watch").assertIsFocused()
        Thread.sleep(1200);ui.waitForIdle()
        val dir=ui.activity.filesDir.resolve("hero-preview").apply { mkdirs() }
        InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot().let { bitmap ->
            dir.resolve("no-art.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) };bitmap.recycle()
        }
    }
}
