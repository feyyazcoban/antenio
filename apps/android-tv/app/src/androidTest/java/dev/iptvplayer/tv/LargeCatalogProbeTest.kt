package dev.iptvplayer.tv

import android.os.SystemClock
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.iptvplayer.tv.data.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.util.concurrent.atomic.AtomicLong
import org.json.JSONArray
import org.json.JSONObject
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.io.File

/** Diagnostic debug/emulator timings, not a release-device Macrobenchmark or a PRD performance gate. */
@RunWith(AndroidJUnit4::class)
class LargeCatalogProbeTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()
    private lateinit var model: TvViewModel
    private val diagnostics = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
    @Before fun setup() { model = resetFixtureSources(ui) }
    @After fun cleanup() {
        diagnostics.cancel()
        if (!::model.isInitialized) return
        ui.runOnUiThread { ui.activity.viewModelStore.clear() }
        runBlocking { SourceTransactions.shared.write {
            (ui.activity.application as TvApplication).artworkCache.clear()
            CatalogStore(ui.activity).clear(); model.store.forgetSource()
        } }
    }
    @Test fun measureSixtyThousandEntriesThroughVisibleSearch() {
        val source = SourceConfig("large-probe", "Yerel büyük katalog", SourceType.M3U, "http://127.0.0.1:1/list.m3u")
        val labels = MediaKind.entries.associateWith { ui.activity.getString(it.label) }
        val entries = (1..60_000).map { n ->
            val kind = when { n <= 10_000 -> MediaKind.LIVE; n <= 45_000 -> MediaKind.MOVIE; else -> MediaKind.SERIES }
            MediaEntry("${kind.name}:$n", n.toString(), kind, "${labels.getValue(kind)} İstanbul $n", "Kategori ${n % 100}",
                uri = "http://127.0.0.1:1/fixture.mp4")
        }
        val result = JSONObject().put("build", BuildConfig.VERSION_NAME).put("entries", entries.size)
            .put("live", 10000).put("movies", 35000).put("series", 15000)
            .put("scope", "Debug emulator: model request through Compose idle and visible semantics. Includes test synchronization; not physical input-to-pixel or process startup.")
        var start = SystemClock.elapsedRealtimeNanos()
        runBlocking { SourceTransactions.shared.write {
            model.store.saveSources(SourceRegistry(listOf(source)))
            CatalogStore(ui.activity).write(source, CatalogSnapshot(Catalog(entries), System.currentTimeMillis()))
        } }
        result.put("encryptedWriteMs", (SystemClock.elapsedRealtimeNanos() - start) / 1_000_000.0)
        val restores = JSONArray()
        repeat(5) {
            start = SystemClock.elapsedRealtimeNanos()
            ui.runOnUiThread { model.restore() }
            ui.waitUntil(30000) { model.state.value.route == Route.HOME && model.state.value.catalog.items.size == 60_000 }
            ui.onNodeWithText("KÜTÜPHANENDEN · FİLMLER").assertIsDisplayed()
            ui.waitForIdle()
            restores.put((SystemClock.elapsedRealtimeNanos() - start) / 1_000_000.0)
        }
        result.put("restoreToHomeMs", restores)
        val indexCache = TvViewModel::class.java.getDeclaredField("searchIndex").apply { isAccessible = true }.get(model)
        val cachedCatalog = indexCache.javaClass.getDeclaredField("catalog").apply { isAccessible = true }
        val pendingIndex = indexCache.javaClass.getDeclaredField("pending").apply { isAccessible = true }
        assertTrue("Unchanged restore must prepare the instance actually retained by StateFlow",
            model.state.value.allCatalog === cachedCatalog.get(indexCache))
        val prepared = pendingIndex.get(indexCache)
        ui.runOnUiThread { model.search() }
        ui.onNode(hasSetTextAction()).assertIsFocused()
        val samples = JSONArray()
        val queries = listOf("istanbul 10001", "İSTANBUL 45001", "1000", "istanbul", "60000", "film 22222")
        repeat(4) { pass ->
            for (query in queries) {
                val readyAt = AtomicLong(0)
                // Observe publication directly: waitUntil includes Compose/test polling overhead.
                val observer = diagnostics.launch(start = CoroutineStart.UNDISPATCHED) {
                    // Query text is published before updateSearch clears previous results.
                    // Do not mistake that intermediate state for the new completed query.
                    model.state.first { it.searching && it.searchQuery == query }
                    model.state.first { !it.searching && it.searchQuery == query && it.searchResults.isNotEmpty() }
                    readyAt.set(SystemClock.elapsedRealtimeNanos())
                }
                start = SystemClock.elapsedRealtimeNanos()
                ui.runOnUiThread { model.searchQuery(query) }
                val dispatchedAt = SystemClock.elapsedRealtimeNanos()
                ui.waitUntil(30000) { !model.state.value.searching && model.state.value.searchQuery == query }
                val polledAt = SystemClock.elapsedRealtimeNanos()
                runBlocking { withTimeout(30000) { observer.join() } }
                val found = model.state.value.searchResults
                assertTrue(found.isNotEmpty())
                ui.onNodeWithTag("search:${found.first().id}").assertIsDisplayed()
                ui.waitForIdle()
                samples.put(JSONObject().put("pass", pass).put("queryClass", queries.indexOf(query))
                    .put("results", found.size).put("ms", (SystemClock.elapsedRealtimeNanos() - start) / 1_000_000.0)
                    .put("requestDispatchMs", (dispatchedAt - start) / 1_000_000.0)
                    .put("modelReadyMs", (readyAt.get() - start) / 1_000_000.0)
                    .put("testPollReadyMs", (polledAt - start) / 1_000_000.0)
                    .put("modelToIdleAndVisibleMs", (SystemClock.elapsedRealtimeNanos() - readyAt.get()) / 1_000_000.0))
            }
        }
        result.put("queryToVisibleMs", samples)
        assertSame("Queries must reuse the index prepared during restore", prepared, pendingIndex.get(indexCache))
        val runtime = Runtime.getRuntime()
        result.put("usedJavaHeapBytesAtEnd", runtime.totalMemory() - runtime.freeMemory())
        File(ui.activity.filesDir, "large-catalog-probe.json").writeText(result.toString(2))
        assertEquals(60_000, model.state.value.allCatalog.items.size)
    }
}
