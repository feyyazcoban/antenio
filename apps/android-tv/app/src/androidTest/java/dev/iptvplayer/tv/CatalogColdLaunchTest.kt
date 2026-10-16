package dev.iptvplayer.tv

import android.os.SystemClock
import androidx.test.core.app.ActivityScenario
import androidx.lifecycle.ViewModelProvider
import org.json.JSONObject
import org.junit.Assert.*
import kotlinx.coroutines.runBlocking
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.iptvplayer.tv.data.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Explicit emulator-only large-catalog fixture for an external force-stop/relaunch observation. */
@RunWith(AndroidJUnit4::class)
class CatalogColdLaunchTest {
    @Test fun prepareEncryptedCatalogForColdLaunch() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("prepareColdCatalog") == "true")
        assumeTrue(android.os.Build.HARDWARE.contains("ranchu") || android.os.Build.FINGERPRINT.contains("generic"))
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val store = DeviceStore(context)
        store.forgetSource()
        val cache = CatalogStore(context)
        cache.clear()
        val source = SourceConfig("cold-catalog-fixture", "Yerel katalog denemesi", SourceType.XTREAM,
            "http://127.0.0.1:1/", "fixture", "fixture")
        val entries = (1..63_175).map { n ->
            val kind = when (n % 5) { 0 -> MediaKind.LIVE; 1 -> MediaKind.SERIES; else -> MediaKind.MOVIE }
            MediaEntry("${kind.name.lowercase()}:$n", n.toString(), kind,
                "${kind.label} $n", "Kategori ${n % 100}", extension = "mp4")
        }
        store.saveSource(source)
        cache.write(source, CatalogSnapshot(Catalog(entries), System.currentTimeMillis()))
        store.favorites(source.id, setOf("movie:2"))
        store.saveProgress(source.id, "movie:2", 45000L, 120000L)
        store.history(source.id, listOf(WatchRecord("movie:2", MediaKind.MOVIE, positionMs = 45000L,
            durationMs = 120000L, updatedAt = System.currentTimeMillis())))
        check(cache.read(source)?.catalog?.items?.size == 63_175)
        context.filesDir.resolve("cold-seed-pid.txt").writeText(android.os.Process.myPid().toString())
    }
    @Test fun verifyRestoredAfterProcessRestart() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("verifyColdCatalog") == "true")
        assumeTrue(android.os.Build.HARDWARE.contains("ranchu") || android.os.Build.FINGERPRINT.contains("generic"))
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val seedPid = context.filesDir.resolve("cold-seed-pid.txt").readText().trim().toInt()
        assertNotEquals("Verifier must run in a different process", seedPid, android.os.Process.myPid())
        val start = SystemClock.elapsedRealtime()
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            lateinit var model: TvViewModel
            scenario.onActivity { model = ViewModelProvider(it)[TvViewModel::class.java] }
            val deadline = start + 30000
            while (model.state.value.sourceTask != SourceTask.IDLE && SystemClock.elapsedRealtime() < deadline) SystemClock.sleep(50)
            val state = model.state.value
            assertEquals(Route.HOME, state.route)
            assertEquals(63175, state.allCatalog.items.size)
            assertEquals(63175, state.catalog.items.size)
            assertEquals("cold-catalog-fixture", state.sources.single().id)
            val movie = state.catalog.items.single { it.withoutSource().id == "movie:2" }
            assertTrue(movie.id in state.favoriteIds)
            assertEquals(45000L, model.progress(movie)!!.positionMs)
            assertEquals(movie.id, state.history.single().id)
            assertEquals(45000L, state.history.single().positionMs)
            assertEquals(movie.id, homeLibrary(state.catalog, state.history, state.favoriteIds).continueWatching.single().entry.id)
            val result = JSONObject().put("build", BuildConfig.VERSION_NAME).put("entries", state.catalog.items.size)
                .put("sourceCount", state.sources.size).put("favoriteRestored", true).put("continueWatchingRestored", true).put("progressRestoredMs", 45000)
                .put("seedPid", seedPid).put("verifyPid", android.os.Process.myPid())
                .put("launchToRestoredStateMs", SystemClock.elapsedRealtime() - start)
                .put("scope", "Instrumentation starts a new process, then ActivityScenario launches MainActivity. State readiness only, not first rendered content or physical startup timing.")
            context.filesDir.resolve("cold-restore-verification.json").writeText(result.toString(2))
        }
    }

    @Test fun cleanColdCatalogFixture() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("cleanColdCatalog") == "true")
        assumeTrue(android.os.Build.HARDWARE.contains("ranchu") || android.os.Build.FINGERPRINT.contains("generic"))
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        runBlocking { SourceTransactions.shared.write {
            DeviceStore(context).forgetSource(); CatalogStore(context).clear()
        } }
    }

}
