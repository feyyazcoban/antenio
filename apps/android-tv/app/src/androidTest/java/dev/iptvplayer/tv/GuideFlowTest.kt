package dev.iptvplayer.tv

import android.graphics.Bitmap
import android.view.KeyEvent
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.iptvplayer.tv.data.*
import dev.iptvplayer.tv.ui.Action
import dev.iptvplayer.tv.ui.PilotTheme
import dev.iptvplayer.tv.ui.RemoteTextReader
import dev.iptvplayer.tv.ui.guideStatusText
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.*
import okio.Buffer
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@RunWith(AndroidJUnit4::class)
class GuideFlowTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()
    private lateinit var model: TvViewModel
    private lateinit var server: MockWebServer
    private lateinit var source: SourceConfig
    private lateinit var raw: Catalog
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val streams = AtomicInteger()
    private val requests = AtomicInteger()
    @Volatile private var failGuide = false
    @Volatile private var slowGuide = false
    private var base = 0L
    private val names = listOf("TRT Belgesel", "TRT 1", "TRT Müzik", "TRT Spor", "TRT Haber", "CNN Türk")
    private val currentTitle = listOf("Doğanın izinde", "Günün hikâyesi", "Müzik zamanı", "Spor gündemi", "Güne bakış", "Dünyadan haberler")
    private fun id(index: Int) = ContentRef(source.id, "live:$index").key
    private fun xmlTime(time: Long) = Instant.ofEpochMilli(time).atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("uuuuMMddHHmmss xx", Locale.ROOT))
    private fun xml(alternate: Boolean = false): String = buildString {
        append("<tv>")
        repeat(18) { n ->
            append("<channel id='epg.$n'><display-name>${names[n % 6]}</display-name></channel>")
        }
        repeat(18) { n ->
            val stop = base + if (n == 1) 10 * 60_000 else 40 * 60_000
            append("<programme channel='epg.$n' start='${xmlTime(base - 60 * 60_000)}' stop='${xmlTime(stop)}'><title>${if (alternate) "Yeni rehber" else currentTitle[n % 6]}</title><desc>Program açıklaması. Türkçe karakterler: İstanbul, yağmur, ışık.</desc></programme>")
            append("<programme channel='epg.$n' start='${xmlTime(stop)}' stop='${xmlTime(stop + 80 * 60_000)}'><title>Sıradaki program $n</title><desc>Bir sonraki programın ayrıntıları.</desc></programme>")
        }
        append("</tv>")
    }
    @Before fun setup() {
        model = resetFixtureSources(ui)
        model.store.guideZoneId = ""
        val assets = instrumentation.context.assets
        val video = assets.open("playback-fixture.mp4").use { it.readBytes() }
        base = System.currentTimeMillis() / 60_000 * 60_000
        server = MockWebServer()
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val path = request.requestUrl!!.encodedPath
                if (path.startsWith("/art/")) return MockResponse().setHeader("Content-Type", "image/png")
                    .setBody(Buffer().write(assets.open("visual/${path.substringAfterLast('/')}").use { it.readBytes() }))
                if (path.startsWith("/live/")) { streams.incrementAndGet(); return MockResponse().setHeader("Content-Type", "video/mp4").setBody(Buffer().write(video)) }
                if (path.endsWith(".xml")) {
                    requests.incrementAndGet()
                    if (failGuide) return MockResponse().setResponseCode(503)
                    val response = MockResponse().setHeader("Content-Type", "application/xml").setBody(xml(path == "/alternate.xml"))
                    return if (slowGuide) response.throttleBody(1, 3, TimeUnit.SECONDS) else response
                }
                return MockResponse().setResponseCode(404)
            }
        }
        server.start()
        source = SourceConfig("guide-fixture", "Görsel deneme", SourceType.M3U, server.url("/list.m3u").toString())
        raw = Catalog((0..17).map { n -> MediaEntry("live:$n", "$n", MediaKind.LIVE,
            if (n == 12) "İstanbul'dan dünyaya yayın yapan çok uzun kanal başlığı" else names[n % 6], "Canlı",
            artwork = server.url("/art/channel-${n % 6}.png").toString(), uri = server.url("/live/$n").toString(), epgId = if (n == 2) "missing" else "epg.$n") },
            epgUrls = listOf(server.url("/guide.xml").toString()))
        runBlocking { SourceTransactions.shared.write {
            DeviceStore(ui.activity).saveSources(SourceRegistry(listOf(source)))
            CatalogStore(ui.activity).write(source, CatalogSnapshot(raw, System.currentTimeMillis()))
            DeviceStore(ui.activity).history(source.id, listOf(WatchRecord("live:0", MediaKind.LIVE, updatedAt = base)))
        } }
        ui.runOnUiThread { model.restore() }
        ui.waitUntil(15_000) { model.state.value.route == Route.HOME }
    }
    @After fun cleanup() {
        if (::model.isInitialized) {
            ui.runOnUiThread { model.guideZone("") }
            for (source in model.state.value.sources.toList()) {
                ui.runOnUiThread { model.removeSource(source.id) }
                ui.waitUntil(10_000) { model.state.value.sourceTask == SourceTask.IDLE }
            }
        }
        if (::server.isInitialized) server.shutdown()
    }
    private fun key(code: Int) { instrumentation.sendKeyDownUpSync(code); ui.waitForIdle() }
    private fun click(tag: String) {
        if (tag == "open-guide") ui.onNodeWithTag("library-grid").performScrollToIndex(0)
        ui.onNodeWithTag(tag).performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.OnClick) { it() }
    }
    private fun guide() {
        click("nav:Canlı TV"); click("open-guide")
        try { ui.waitUntil(10_000) { model.state.value.guideStatus[source.id]?.info != null && model.state.value.guideStatus[source.id]?.refreshing == false } }
        catch (e: Throwable) {
            shot("failure-ready")
            throw AssertionError("Guide readiness: route=${model.state.value.route}, status=${model.state.value.guideStatus[source.id]}, requests=${requests.get()}", e)
        }
        ui.waitUntil(5_000) { ui.onAllNodesWithText(currentTitle[0]).fetchSemanticsNodes().isNotEmpty() }
        ui.onNodeWithTag("guide-grid").assertIsFocused()
    }
    private fun shot(name: String) {
        ui.waitForIdle(); Thread.sleep(350)
        val directory = ui.activity.filesDir.resolve("guide-preview").apply { mkdirs() }
        instrumentation.uiAutomation.takeScreenshot().let { bitmap ->
            directory.resolve("$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }; bitmap.recycle()
        }
    }
    private fun inside(child: SemanticsNodeInteraction, parent: SemanticsNodeInteraction) {
        val bounds = child.getUnclippedBoundsInRoot()
        val viewport = parent.getUnclippedBoundsInRoot()
        assertTrue("Focused content must fit: $bounds inside $viewport", bounds.top >= viewport.top && bounds.bottom <= viewport.bottom &&
            bounds.left >= viewport.left && bounds.right <= viewport.right)
    }
    private fun insideDialog(tag: String) = inside(ui.onNodeWithTag(tag), ui.onAllNodesWithTag("source-dialog").onLast())
    private fun readToEnd(tag: String) {
        ui.onNodeWithTag(tag).assertIsFocused()
        repeat(30) {
            val state = ui.onNodeWithTag(tag).fetchSemanticsNode().config[SemanticsProperties.StateDescription]
            if (state == "Metnin sonu" || state == "Metnin tamamı görünüyor") return
            key(KeyEvent.KEYCODE_DPAD_DOWN)
            ui.onNodeWithTag(tag).assertIsFocused()
        }
        fail("Complete programme/status text must be reachable")
    }
    @Test fun remotePreservesTimeAcrossUnequalProgrammesAndReturnsFromDetailsAndPlayer() {
        guide()
        assertEquals(id(0), model.state.value.guideChannelId)
        val anchor = model.state.value.guideAnchor
        key(KeyEvent.KEYCODE_DPAD_DOWN)
        assertEquals(id(1), model.state.value.guideChannelId); assertEquals(anchor, model.state.value.guideAnchor)
        key(KeyEvent.KEYCODE_DPAD_RIGHT)
        assertEquals(base + 10 * 60_000, model.state.value.guideAnchor)
        ui.onNodeWithTag("guide-grid").assertContentDescriptionContains("Sıradaki program 1", substring = true)
        assertEquals(0, streams.get())
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.onNodeWithTag("guide-detail-reader").assertIsDisplayed(); assertEquals(0, streams.get())
        key(KeyEvent.KEYCODE_BACK)
        ui.onNodeWithTag("guide-grid").assertIsFocused()
        click("guide-now"); key(KeyEvent.KEYCODE_DPAD_CENTER)
        try { ui.waitUntil(10_000) { streams.get() > 0 && ui.onAllNodesWithText("Duraklat").fetchSemanticsNodes().isNotEmpty() } }
        catch (e: Throwable) {
            shot("failure-player")
            throw AssertionError("Player readiness: route=${model.state.value.route}, streams=${streams.get()}, guideAnchor=${model.state.value.guideAnchor}", e)
        }
        ui.onNodeWithTag("player-current-programme").assertTextContains(currentTitle[1], substring = true)
        key(KeyEvent.KEYCODE_BACK); assertEquals(Route.PLAYER, model.state.value.route)
        key(KeyEvent.KEYCODE_BACK); assertEquals(Route.GUIDE, model.state.value.route)
        ui.onNodeWithTag("guide-grid").assertIsFocused(); assertEquals(id(1), model.state.value.guideChannelId)
        // Back from the grid: the day and time controls, then the menu, then Ana Sayfa.
        key(KeyEvent.KEYCODE_BACK); ui.onNodeWithTag("guide-now").assertIsFocused()
        key(KeyEvent.KEYCODE_BACK); ui.onNodeWithTag("nav:Canlı TV").assertIsFocused()
        key(KeyEvent.KEYCODE_BACK); assertEquals(Route.HOME, model.state.value.route)
    }
    /** A quick double Back: the second one arrives in the same main-thread turn, before the screen leaves composition. */
    private fun doubleBack() = ui.runOnUiThread { ui.activity.onBackPressedDispatcher.run { onBackPressed(); onBackPressed() } }
    private fun exitAsked() = ui.waitUntil(5_000) { ui.onAllNodes(hasTestTag("exit-cancel") and isFocused()).fetchSemanticsNodes().isNotEmpty() }
    @Test fun guideBackFollowsTheModelRouteBeforeTheScreenLeaves() {
        // The guide's own Back handler stays enabled until the next frame removes the screen: from its side menu the
        // second Back belongs to Home and asks, instead of going Home a second time unseen.
        guide()
        key(KeyEvent.KEYCODE_BACK); ui.onNodeWithTag("guide-now").assertIsFocused()
        key(KeyEvent.KEYCODE_BACK); ui.onNodeWithTag("nav:Canlı TV").assertIsFocused()
        doubleBack(); exitAsked()
        assertEquals(Route.HOME, model.state.value.route)
        assertFalse(ui.activity.isFinishing)
    }
    @Test fun playerBackFollowsTheModelRouteBeforeTheScreenLeaves() {
        val channel = model.state.value.catalog.items.first { it.id == id(0) }
        ui.runOnUiThread { model.open(channel) }
        ui.waitUntil(10_000) { streams.get() > 0 && ui.onAllNodesWithText("Duraklat").fetchSemanticsNodes().isNotEmpty() }
        key(KeyEvent.KEYCODE_DPAD_CENTER) // Pause the short fixture so it cannot end and start a recovery.
        // Back hides the controls, the next Back leaves for Home, and one right after it asks there.
        key(KeyEvent.KEYCODE_BACK); assertEquals(Route.PLAYER, model.state.value.route)
        doubleBack(); exitAsked()
        assertEquals(Route.HOME, model.state.value.route)
        assertFalse(ui.activity.isFinishing)
    }
    @Test fun missingGuideAndEmptyFutureDayNeverBlockLivePlayback() {
        guide()
        key(KeyEvent.KEYCODE_DPAD_DOWN); key(KeyEvent.KEYCODE_DPAD_DOWN)
        ui.onNodeWithTag("guide-grid").assertContentDescriptionContains("Rehberde kanal kimliği bulunamadı", substring = true)
        click("guide-day-next")
        ui.onNodeWithTag("guide-grid").performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.RequestFocus) { it() }
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.onNodeWithTag("guide-play-live").assertIsDisplayed()
        click("guide-play-live")
        ui.waitUntil(10_000) { streams.get() > 0 }
        assertEquals(Route.PLAYER, model.state.value.route)
    }
    @Test fun guideOverrideIsEncryptedAndPersistsWithoutReloadingTheCatalog() {
        guide()
        ui.runOnUiThread { model.manageSources() }
        click("info-source:${source.id}"); click("source-guide-settings"); click("edit-guide-url")
        val address = server.url("/alternate.xml?token=fixture-secret").toString()
        ui.onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("guide-url-field"))).performTextReplacement(address)
        click("save-guide-url")
        ui.waitUntil(10_000) { model.state.value.guideSaveRevision > 0 && model.state.value.sourceTask == SourceTask.IDLE }
        assertEquals(address, DeviceStore(ui.activity).loadSources().sources.single().epgUrl)
        assertEquals(raw, CatalogStore(ui.activity).read(source)!!.catalog)
        val prefs = ui.activity.getSharedPreferences("device_state", 0).all.values.joinToString()
        assertFalse(prefs.contains("fixture-secret"))
        ui.runOnUiThread { model.restore() }
        ui.waitUntil(10_000) { model.state.value.route == Route.HOME }
        click("nav:Canlı TV"); click("open-guide")
        ui.waitUntil(10_000) { ui.onAllNodesWithText("Yeni rehber").fetchSemanticsNodes().isNotEmpty() }
        assertEquals(address, model.state.value.sources.single().epgUrl)
        assertEquals(0, streams.get())
    }
    @Test fun timezoneSelectionKeepsInstantAndPersistsAcrossRestoration() {
        guide(); val anchor = model.state.value.guideAnchor
        click("guide-zone"); click("guide-zone:Europe/Berlin")
        assertEquals("Europe/Berlin", model.state.value.guideZoneId); assertEquals(anchor, model.state.value.guideAnchor)
        assertEquals("Europe/Berlin", DeviceStore(ui.activity).guideZoneId)
        ui.runOnUiThread { model.restore() }
        ui.waitUntil(10_000) { model.state.value.route == Route.HOME }
        assertEquals("Europe/Berlin", model.state.value.guideZoneId)
    }
    @Test fun refreshFailureKeepsOldProgrammesAndIsVisibleInSourceDetails() {
        guide()
        val updated = model.state.value.guideStatus[source.id]!!.info!!.updatedAt
        failGuide = true
        ui.runOnUiThread { model.refreshGuide(source.id) }
        ui.waitUntil(10_000) { model.state.value.guideStatus[source.id]?.error != null && model.state.value.guideStatus[source.id]?.refreshing == false }
        assertEquals(updated, model.state.value.guideStatus[source.id]!!.info!!.updatedAt)
        ui.onNodeWithTag("guide-grid").assertContentDescriptionContains(currentTitle[0], substring = true)
        ui.runOnUiThread { model.manageSources() }
        click("info-source:${source.id}")
        ui.onNodeWithText("Yayın rehberi şu anda alınamıyor.", substring = true).assertExists()
        assertEquals(2, requests.get())
    }
    @Test fun removalCancelsGuideBodyAndCannotRecreateCacheLater() {
        guide(); val count = requests.get(); slowGuide = true
        ui.runOnUiThread { model.refreshGuide(source.id) }
        ui.waitUntil(5_000) { requests.get() > count }
        ui.runOnUiThread { model.removeSource(source.id) }
        ui.waitUntil(10_000) { model.state.value.route == Route.SETUP && model.state.value.sourceTask == SourceTask.IDLE }
        ui.waitUntil(5_000) { ui.activity.cacheDir.resolve("guide-staging").listFiles().orEmpty().isEmpty() }
        assertNull(EpgStore(ui.activity).info(EpgPlan.create(source, raw)!!))
        assertTrue(DeviceStore(ui.activity).loadSources().sources.isEmpty())
    }
    @Test fun cancellingFirstGuideDoesNotRestartFromRecompositionAndManualRetryWorks() {
        slowGuide = true; click("nav:Canlı TV"); click("open-guide")
        ui.waitUntil(5_000) { requests.get() == 1 }
        ui.runOnUiThread { model.cancelGuide(source.id); repeat(5) { model.ensureGuide(listOf(source.id)) } }
        ui.waitUntil(5_000) { model.state.value.guideStatus[source.id]?.let { !it.refreshing && it.error == UiText.Res(R.string.guide_refresh_cancelled) } == true }
        ui.runOnUiThread { repeat(5) { model.ensureGuide(listOf(source.id)) } }
        assertEquals(1, requests.get())
        assertTrue(ui.activity.cacheDir.resolve("guide-staging").listFiles().orEmpty().isEmpty())
        slowGuide = false
        ui.runOnUiThread { model.refreshGuide(source.id) }
        ui.waitUntil(5_000) { model.state.value.guideStatus[source.id]?.let { !it.refreshing && it.info != null && it.error == null } == true }
        assertEquals(2, requests.get()); assertEquals(0, streams.get())
    }
    @Test fun captureCurrentFontAndReachLongNamesAndAllGuideControls() {
        guide()
        val scale = ui.activity.resources.configuration.fontScale.toString().replace('.', '_')
        shot("guide-$scale")
        repeat(12) { key(KeyEvent.KEYCODE_DPAD_DOWN) }
        assertEquals(id(12), model.state.value.guideChannelId)
        ui.onNodeWithTag("guide-row:${id(12)}").assertIsDisplayed()
        inside(ui.onNodeWithTag("guide-row:${id(12)}"), ui.onNodeWithTag("guide-grid"))
        shot("guide-long-$scale")
        key(KeyEvent.KEYCODE_DPAD_RIGHT); key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.onNodeWithTag("guide-detail-reader").assertIsDisplayed()
        insideDialog("guide-play-live"); insideDialog("guide-close-detail")
        shot("guide-detail-$scale")
        readToEnd("guide-detail-reader")
        key(KeyEvent.KEYCODE_DPAD_DOWN); ui.onNodeWithTag("guide-play-live").assertIsFocused()
        key(KeyEvent.KEYCODE_BACK)
        click("guide-zone"); ui.onNodeWithTag("guide-zone-system").assertIsFocused()
        insideDialog("guide-zone-system"); insideDialog("close-guide-zone")
        shot("guide-zone-$scale")
        key(KeyEvent.KEYCODE_BACK)
        ui.onNodeWithTag("guide-zone").assertIsFocused()
        ui.runOnUiThread { model.manageSources() }
        click("info-source:${source.id}"); click("source-guide-settings")
        ui.onNodeWithTag("guide-status-reader").assertExists()
        insideDialog("refresh-guide"); insideDialog("edit-guide-url"); insideDialog("close-guide-settings")
        shot("guide-settings-$scale")
        readToEnd("guide-status-reader")
        key(KeyEvent.KEYCODE_DPAD_DOWN); ui.onNodeWithTag("refresh-guide").assertIsFocused()
        key(KeyEvent.KEYCODE_DPAD_DOWN); ui.onNodeWithTag("edit-guide-url").assertIsFocused()
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.onNodeWithTag("guide-url-field").assertExists()
        // The editor puts focus on its field without the keyboard; OK opens it, Back closes only the keyboard.
        fun imeShown() = android.os.ParcelFileDescriptor.AutoCloseInputStream(
            InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand("dumpsys input_method")
        ).bufferedReader().use { it.readText().contains("mInputShown=true") }
        ui.waitForIdle(); Thread.sleep(800)
        assertFalse("The keyboard must not open on focus", imeShown())
        key(KeyEvent.KEYCODE_DPAD_CENTER); ui.waitUntil(5_000) { imeShown() }
        key(KeyEvent.KEYCODE_BACK); ui.waitUntil(5_000) { !imeShown() }
        insideDialog("save-guide-url"); insideDialog("cancel-guide-url")
        shot("guide-editor-$scale")
        click("cancel-guide-url")
        ui.onNodeWithTag("guide-status-reader").assertIsFocused()
    }
    @Test fun liveCardsAndPlayerKeepProgrammeAndRemoteControlsVisible() {
        guide(); click("nav:Canlı TV")
        val scale = ui.activity.resources.configuration.fontScale.toString().replace('.', '_')
        ui.waitUntil(5_000) { ui.onAllNodesWithText(currentTitle[0], useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty() }
        inside(ui.onNodeWithTag("tile:library:${id(0)}"), ui.onNodeWithTag("library-grid"))
        shot("guide-live-cards-$scale")
        // D060: entering Canlı TV from the side menu focuses the selected "Tüm kanallar"; Right enters the first row.
        ui.onNodeWithTag("category:${CategoryFilter.ALL}").assertIsFocused().assertIsSelected(); key(KeyEvent.KEYCODE_DPAD_RIGHT)
        ui.onNode(isFocused() and hasAnyAncestor(hasTestTag("tile:library:${id(0)}"))).assertExists()
        repeat(12) { key(KeyEvent.KEYCODE_DPAD_DOWN) } // One numbered row per channel: the thirteenth is twelve rows down.
        ui.onNodeWithContentDescription(raw.items[12].title).assertIsFocused()
        inside(ui.onNodeWithTag("tile:library:${id(12)}"), ui.onNodeWithTag("library-grid"))
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.waitUntil(10_000) { ui.onAllNodesWithText("Duraklat").fetchSemanticsNodes().isNotEmpty() }
        key(KeyEvent.KEYCODE_DPAD_CENTER) // Pause the short fixture so the controls stay inspectable.
        ui.onNodeWithTag("player-current-programme").assertTextContains(currentTitle[0], substring = true)
        inside(ui.onNodeWithTag("player-panel"), ui.onRoot())
        inside(ui.onNodeWithTag("player-title"), ui.onNodeWithTag("player-panel"))
        inside(ui.onNodeWithTag("player-current-programme"), ui.onNodeWithTag("player-panel"))
        inside(ui.onNodeWithTag("player-controls"), ui.onNodeWithTag("player-panel"))
        shot("guide-player-$scale")
        repeat(5) { key(KeyEvent.KEYCODE_DPAD_RIGHT) }
        ui.onNodeWithText("Kapat").assertIsFocused()
        inside(ui.onNodeWithText("Kapat"), ui.onNodeWithTag("player-controls"))
        shot("guide-player-controls-$scale")
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        assertEquals(Route.LIBRARY, model.state.value.route)
        ui.onNodeWithContentDescription(raw.items[12].title).assertIsFocused()
    }
    @Test fun cachedGuideSurvivesRestorationWithoutAnotherDownload() {
        guide(); val downloads = requests.get(); val updated = model.state.value.guideStatus[source.id]!!.info!!.updatedAt
        failGuide = true
        ui.runOnUiThread { model.restore() }
        ui.waitUntil(10_000) { model.state.value.route == Route.HOME }
        guide()
        assertEquals(downloads, requests.get())
        assertEquals(updated, model.state.value.guideStatus[source.id]!!.info!!.updatedAt)
        assertEquals(0, streams.get())
    }
    @Test fun emptyGuideHasReachableNavigationAndNoUnattachedFocusTarget() {
        runBlocking { SourceTransactions.shared.write { CatalogStore(ui.activity).write(source, CatalogSnapshot(Catalog(emptyList()), System.currentTimeMillis())) } }
        ui.runOnUiThread { model.restore() }
        ui.waitUntil(10_000) { model.state.value.route == Route.HOME }
        click("nav:Canlı TV"); click("open-guide")
        ui.onNodeWithTag("nav:Canlı TV").assertIsFocused()
        key(KeyEvent.KEYCODE_DPAD_RIGHT); ui.onNodeWithTag("guide-now").assertIsFocused()
        key(KeyEvent.KEYCODE_DPAD_CENTER)
        ui.onNodeWithText("Canlı kanal bulunamadı").assertIsDisplayed()
        key(KeyEvent.KEYCODE_DPAD_DOWN)
        // Back from the day and time controls goes to the menu, then to Ana Sayfa (D067).
        key(KeyEvent.KEYCODE_BACK); ui.onNodeWithTag("nav:Canlı TV").assertIsFocused()
        key(KeyEvent.KEYCODE_BACK)
        assertEquals(Route.HOME, model.state.value.route)
    }
    /**
     * The source info dialog's text follows every guide status. Its sentences once emitted a different number of
     * composition groups per status straight into the dialog's own group, which duplicated the dialog's actions or
     * crashed it (ClassCastException in Action's remember) when the status changed under the open dialog.
     */
    @Test fun guideStatusChangesKeepTheSourceInfoDialogIntact() {
        val status = mutableStateOf<GuideStatus?>(null)
        val loaded = GuideStatus(configured = true, initialized = true,
            info = EpgSnapshotInfo(System.currentTimeMillis(), GuideWindow(0, 1), channels = 3, programmes = 10, invalid = 0, outsideWindow = 0))
        // The dialog's shape: the status text in a reader, then its two actions.
        ui.runOnUiThread { ui.activity.setContent { PilotTheme { Column {
            RemoteTextReader("Görsel deneme\n\n" + guideStatusText(status.value, ZoneOffset.UTC), Modifier, "status-reader")
            Action("Rehber ayarları", Modifier.testTag("status-settings")) { }
            Action("Kapat", Modifier.testTag("status-close")) { }
        } } } }
        for (next in listOf(loaded, GuideStatus(configured = true), loaded, null,
            loaded.copy(refreshing = true, error = UiText.Res(R.string.guide_refresh_cancelled)), GuideStatus(configured = true), GuideStatus())) {
            ui.runOnUiThread { status.value = next }
            ui.waitForIdle()
            ui.onNodeWithTag("status-settings").assertExists()
            ui.onNodeWithTag("status-close").assertExists()
        }
    }
}
