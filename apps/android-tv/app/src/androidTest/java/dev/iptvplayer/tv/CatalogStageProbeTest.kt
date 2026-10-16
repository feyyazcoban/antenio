package dev.iptvplayer.tv

import android.os.SystemClock
import androidx.test.platform.app.InstrumentationRegistry
import dev.iptvplayer.tv.data.*
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test

/** Isolated debug timings, not additive startup phases or a physical performance gate. */
class CatalogStageProbeTest {
    @Test fun measureCatalogComponentsWithoutUiSynchronization() {
        assumeTrue(android.os.Build.HARDWARE.contains("ranchu") || android.os.Build.FINGERPRINT.contains("generic"))
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val source = SourceConfig("stage-probe", "Yerel aşama testi", SourceType.M3U, "http://127.0.0.1:1/list.m3u")
        val entries = (1..60000).map { n ->
            val kind = when { n <= 10000 -> MediaKind.LIVE; n <= 45000 -> MediaKind.MOVIE; else -> MediaKind.SERIES }
            MediaEntry("${kind.name}:$n", n.toString(), kind, "${kind.label} İstanbul $n", "Kategori ${n % 100}", uri = "http://127.0.0.1:1/fixture.mp4")
        }
        val snapshot = CatalogSnapshot(Catalog(entries), System.currentTimeMillis())
        val store = CatalogStore(context)
        val samples = JSONArray()
        fun <T> measure(pass: Int, stage: String, work: () -> T): T {
            val start = SystemClock.elapsedRealtimeNanos()
            val value = work()
            samples.put(JSONObject().put("pass", pass).put("stage", stage).put("ms", (SystemClock.elapsedRealtimeNanos()-start)/1000000.0))
            return value
        }
        try {
            repeat(3) { pass ->
                val bytes = measure(pass, "gzipJsonEncode") { CatalogSnapshotCodec.encode(snapshot) }
                val decoded = measure(pass, "gzipJsonDecode") { CatalogSnapshotCodec.decode(bytes) }
                assertEquals(60000, decoded.catalog.items.size)
                measure(pass, "encryptedWrite") { store.write(source, snapshot) }
                measure(pass, "authenticatedHeaderRead") { assertNotNull(store.readEpgFeeds(source)) }
                val loaded = measure(pass, "encryptedReadAndDecode") { store.read(source)!! }
                assertEquals(60000, loaded.catalog.items.size)
                val combined = measure(pass, "sourceIdentityCombine") { SourceLibrary.combine(listOf(source), mapOf(source.id to loaded.catalog)) }
                measure(pass, "homePresentation") { homeLibrary(combined, emptyList(), emptySet()) }
                val index = measure(pass, "titleIndexBuild") { TitleIndex(combined.items) }
                measure(pass, "narrowQuery") { assertEquals(1, index.find("istanbul 10001").size) }
                measure(pass, "broadQuery") { assertEquals(60000, index.find("istanbul").size) }
                bytes.fill(0)
            }
            val result = JSONObject().put("build", BuildConfig.VERSION_NAME).put("entries", 60000)
                .put("scope", "Sequential isolated debug components; caches, JIT and allocations carry across samples. No UI synchronization. Timings are not additive startup phases.")
                .put("samples", samples)
            context.filesDir.resolve("catalog-stage-probe.json").writeText(result.toString(2))
        } finally { store.remove(source) }
    }
}
