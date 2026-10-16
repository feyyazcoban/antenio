package dev.iptvplayer.tv

import android.os.SystemClock
import android.view.KeyEvent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.iptvplayer.tv.data.*
import kotlinx.coroutines.*
import okhttp3.mockwebserver.*
import okio.Buffer
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/** Destructive synthetic fixtures: never run on a user's TV. */
@RunWith(AndroidJUnit4::class)
class SourceOperationFlowTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()
    private lateinit var model: TvViewModel
    private lateinit var server: MockWebServer
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val releases = mutableListOf<CompletableDeferred<Unit>>()
    private val writers = mutableListOf<Deferred<Unit>>()
    private val extraStores = mutableListOf<ViewModelStore>()
    private val slowEntered = CountDownLatch(1)
    private val slowRelease = CountDownLatch(1)
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()

    @Before fun setup() {
        Assume.assumeTrue(android.os.Build.HARDWARE.contains("ranchu") || android.os.Build.FINGERPRINT.contains("generic"))
        val bytes = instrumentation.context.assets.open("playback-fixture.mp4").use { it.readBytes() }
        server = MockWebServer(); server.start()
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val path = request.requestUrl!!.encodedPath
                if (path.endsWith(".mp4")) return MockResponse().addHeader("Content-Type", "video/mp4").setBody(Buffer().write(bytes))
                if (path == "/slow.m3u") { slowEntered.countDown(); slowRelease.await(20, TimeUnit.SECONDS) }
                return MockResponse().setBody(list(if (path == "/one.m3u") "Birinci" else if (path == "/two.m3u") "İkinci" else "Gecikmiş"))
            }
        }
        model = resetFixtureSources(ui)
        ui.waitUntil(10_000) { model.state.value.route == Route.SETUP }
        connect("one"); home("Birinci")
    }

    @After fun cleanup() {
        slowRelease.countDown(); releases.forEach { it.complete(Unit) }
        runBlocking { withTimeout(15_000) { writers.forEach { runCatching { it.await() } } } }
        ui.activity.filesDir.resolve("catalogs").setWritable(true)
        ui.runOnUiThread { extraStores.forEach { it.clear() }; ui.activity.viewModelStore.clear() }
        runBlocking { SourceTransactions.shared.write {
            DeviceStore(ui.activity).forgetSource(); CatalogStore(ui.activity).clear()
        } }
        scope.cancel()
        if (::server.isInitialized) server.shutdown()
    }
    private fun list(title: String) = "#EXTM3U\n#EXTINF:-1 group-title=\"Filmler\",$title film\n${server.url("/movie/$title.mp4")}\n"
    private fun connect(name: String) {
        val address = server.url("/$name.m3u").toString()
        ui.runOnUiThread { model.draft(SourceDraft(name, SourceType.M3U, address)); model.connectDraft() }
    }
    private fun home(title: String) = ui.waitUntil(10_000) {
        model.state.value.route == Route.HOME && model.state.value.catalog.items.singleOrNull()?.title == "$title film"
    }
    private fun back() { instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK); ui.waitForIdle() }
    private fun recreate() {
        // Configuration recreation keeps the old ViewModel unless explicitly cleared.
        ui.runOnUiThread { ui.activity.viewModelStore.clear() }
        ui.activityRule.scenario.recreate(); ui.waitForIdle()
        ui.runOnUiThread { model = ViewModelProvider(ui.activity)[TvViewModel::class.java] }
    }
    private fun holdWrite(afterRelease: suspend () -> Unit = {}): CompletableDeferred<Unit> {
        val entered = CompletableDeferred<Unit>(); val release = CompletableDeferred<Unit>()
        releases.add(release)
        writers += scope.async { SourceTransactions.shared.write { entered.complete(Unit); release.await(); afterRelease() } }
        runBlocking { withTimeout(5_000) { entered.await() } }
        return release
    }

    @Test fun cancellingDownloadPreservesThePreviousSourceAfterRecreation() {
        val old = model.state.value.source!!
        ui.runOnUiThread { model.editSource() }; connect("slow")
        assertTrue(slowEntered.await(5, TimeUnit.SECONDS))
        assertEquals(SourceTask.IMPORTING, model.state.value.sourceTask)
        ui.onNodeWithText("İptal").assertIsFocused()
        back(); assertEquals(Route.SETUP, model.state.value.route)
        slowRelease.countDown(); SystemClock.sleep(300)
        assertEquals(old.id, model.store.loadSources().sources.singleOrNull()!!.id)
        back(); home("Birinci"); recreate(); home("Birinci")
    }

    @Test fun lateCancelledResponseCannotReplaceANewerConnection() {
        ui.runOnUiThread { model.editSource() }; connect("slow")
        assertTrue(slowEntered.await(5, TimeUnit.SECONDS))
        ui.onNodeWithText("İptal").assertIsFocused()
        back(); assertEquals(Route.SETUP, model.state.value.route)
        connect("two"); home("İkinci")
        val accepted = model.state.value.source!!.id
        slowRelease.countDown(); SystemClock.sleep(300)
        assertEquals(accepted, model.store.loadSources().sources.singleOrNull()!!.id)
        home("İkinci"); recreate(); home("İkinci")
    }

    @Test fun removalWaitsForOldCommitAndBackCannotStartANewSource() {
        val old = model.state.value.source!!
        val catalog = model.state.value.catalog
        val store = DeviceStore(ui.activity); val snapshots = CatalogStore(ui.activity)
        val release = holdWrite { store.saveSources(store.loadSources().upsert(old)); snapshots.write(old, CatalogSnapshot(catalog.fixtureRaw(), System.currentTimeMillis())) }
        ui.runOnUiThread { model.removeSource() }
        ui.onNodeWithText("Kaynağın kaldırılıyor").assertExists()
        ui.onAllNodesWithText("İptal").assertCountEquals(0)
        back(); connect("two")
        assertEquals(SourceTask.REMOVING, model.state.value.sourceTask)
        assertNull(model.state.value.source)
        release.complete(Unit)
        ui.waitUntil(10_000) { model.state.value.route == Route.SETUP }
        assertNull(store.loadSources().sources.singleOrNull())
        assertTrue(ui.activity.filesDir.resolve("catalogs").listFiles().orEmpty().isEmpty())
        recreate(); ui.waitUntil(10_000) { model.state.value.route == Route.SETUP }
        assertNull(model.state.value.source)
    }

    @Test fun savingFinishesBeforeTheNewCatalogIsShown() {
        val old = model.state.value.source!!
        val release = holdWrite()
        ui.runOnUiThread { model.editSource() }; connect("two")
        ui.waitUntil(10_000) { model.state.value.sourceTask == SourceTask.SAVING }
        ui.onNodeWithText("Kütüphanen kaydediliyor").assertExists()
        ui.onAllNodesWithText("İptal").assertCountEquals(0)
        back(); assertEquals(SourceTask.SAVING, model.state.value.sourceTask)
        assertEquals(old.id, model.store.loadSources().sources.singleOrNull()!!.id)
        release.complete(Unit); home("İkinci")
        val current = model.state.value.source!!
        assertEquals(current.id, model.store.loadSources().sources.singleOrNull()!!.id)
        assertEquals("İkinci film", CatalogStore(ui.activity).read(current)!!.catalog.items.single().title)
        recreate(); home("İkinci")
    }

    @Test fun playerDisposalCannotResurrectProgressAfterRemoval() {
        ui.runOnUiThread { model.play(model.state.value.catalog.items.single()) }
        ui.waitUntil(10_000) { ui.onAllNodesWithText("Duraklat").fetchSemanticsNodes().isNotEmpty() }
        ui.runOnUiThread { model.saveProgress(model.state.value.playing!!.entry, 20_000, 120_000) }
        assertTrue(model.state.value.history.isNotEmpty())
        ui.runOnUiThread { model.removeSource() }
        ui.waitUntil(10_000) { model.state.value.route == Route.SETUP }
        ui.waitForIdle(); SystemClock.sleep(300)
        val keys = ui.activity.getSharedPreferences("device_state", 0).all.keys
        assertFalse(keys.any { it == "source" || it.startsWith("p:") || it.startsWith("p2:") || it.startsWith("f:") || it.startsWith("history:") })
        assertNull(model.state.value.source)
        assertTrue(model.store.loadSources().sources.isEmpty())
    }

    @Test fun failedRemovalShowsAnErrorAndCanBeRetried() {
        val directory = ui.activity.filesDir.resolve("catalogs")
        assertTrue(directory.listFiles().orEmpty().isNotEmpty())
        assertTrue(directory.setWritable(false))
        try {
            ui.runOnUiThread { model.removeSource() }
            ui.waitUntil(10_000) { model.state.value.sourceTask == SourceTask.IDLE }
            assertEquals(Route.HOME, model.state.value.route)
            assertEquals(CatalogNotice("Kaynak kaldırılamadı. Tekrar dene.", CatalogOperation.SOURCE_REMOVAL, needsAttention = true), model.state.value.catalogNotice)
            assertNotNull(model.store.loadSources().sources.singleOrNull())
        } finally { assertTrue(directory.setWritable(true)) }
        ui.runOnUiThread { model.removeSource() }
        ui.waitUntil(10_000) { model.state.value.route == Route.SETUP }
        assertNull(model.store.loadSources().sources.singleOrNull())
    }

    @Test fun anotherViewModelWaitsForTheWholeSourceAndCacheWrite() {
        val next = SourceConfig(name = "two", type = SourceType.M3U, address = server.url("/two.m3u").toString()).validated()
        val parsed = M3uParser.parse(list("İkinci").reader(), next.address)
        val store = DeviceStore(ui.activity); val snapshots = CatalogStore(ui.activity)
        val release = holdWrite {
            store.saveSources(SourceRegistry(listOf(next)))
            snapshots.write(next, CatalogSnapshot(parsed, System.currentTimeMillis()))
        }
        lateinit var other: TvViewModel
        ui.runOnUiThread {
            val owner = ViewModelStore().also { extraStores.add(it) }
            other = ViewModelProvider(owner, ViewModelProvider.AndroidViewModelFactory(ui.activity.application))[TvViewModel::class.java]
        }
        ui.waitForIdle(); assertEquals(SourceTask.RESTORING, other.state.value.sourceTask)
        assertNull(other.state.value.source)
        release.complete(Unit)
        ui.waitUntil(10_000) { other.state.value.route == Route.HOME }
        assertEquals(next.id, other.state.value.source!!.id)
        assertEquals("İkinci film", other.state.value.catalog.items.single().title)
    }
}
