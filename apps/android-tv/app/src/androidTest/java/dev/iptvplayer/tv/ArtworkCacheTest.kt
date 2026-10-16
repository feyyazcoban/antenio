package dev.iptvplayer.tv

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import coil3.request.*
import dev.iptvplayer.tv.data.*
import kotlinx.coroutines.*
import okhttp3.mockwebserver.*
import okio.Buffer
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class ArtworkCacheTest {
    private lateinit var cache: ArtworkCache
    private lateinit var context: Context
    private lateinit var server: MockWebServer
    private lateinit var png: ByteArray
    @Before fun setup() {
        Assume.assumeTrue(android.os.Build.HARDWARE.contains("ranchu") || android.os.Build.FINGERPRINT.contains("generic"))
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val root = File(base.cacheDir, "artwork-test-${System.nanoTime()}").apply { mkdirs() }
        context = object : ContextWrapper(base) {
            override fun getCacheDir() = root
            override fun getDatabasePath(name: String) = File(root, name)
            override fun getApplicationContext(): Context = this
        }
        cache = ArtworkCache(context)
        png = ByteArrayOutputStream().also { output ->
            Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888).apply { eraseColor(0xff53e6bf.toInt()) }
                .compress(Bitmap.CompressFormat.PNG, 100, output)
        }.toByteArray()
        server = MockWebServer().apply { start() }
        runBlocking { cache.synchronizeSources(listOf("A", "B")) }
    }
    @After fun cleanup() { if (::cache.isInitialized) runBlocking { cache.close() }; if (::server.isInitialized) server.shutdown() }
    private fun response() = MockResponse().setHeader("Content-Type", "image/png").setHeader("Cache-Control", "max-age=3600")
        .setBody(Buffer().write(png))
    private suspend fun image(owner: String, url: String): ImageResult = cache.loader.execute(ImageRequest.Builder(context)
        .data(SourceArtwork(owner, url)).size(32).build())
    private fun present(url: String) = cache.loader.diskCache!!.openSnapshot(stableId(url))?.use { true } ?: false
    private fun ownerCount(key: String? = null): Int = android.database.sqlite.SQLiteDatabase.openDatabase(
        context.getDatabasePath("artwork-owners.db").path, null, android.database.sqlite.SQLiteDatabase.OPEN_READONLY
    ).use { db -> db.rawQuery("SELECT COUNT(*) FROM owners" + if (key == null) "" else " WHERE cache_key = ?",
        key?.let { arrayOf(it) }).use { it.moveToFirst(); it.getInt(0) } }
    private suspend fun fullSweep() { while (cache.pruneIndex() != 0) { /* bounded batches */ } }

    @Test fun repeatedFailedDownloadsTriggerAutomaticIndexMaintenance() = runBlocking {
        repeat(96) { n ->
            server.enqueue(MockResponse().setResponseCode(404))
            assertTrue(image("A", server.url("/missing-$n.png").toString()) is ErrorResult)
        }
        withTimeout(3000) { while (ownerCount() > 64) delay(20) }
        assertTrue(ownerCount() <= 64)
        assertEquals(96, server.requestCount)
        assertEquals(0L, cache.loader.diskCache!!.size)
    }

    @Test fun pruningDiscardsAbsentImagesButRetainsSharedDiskAndMemoryOnlyOwners() = runBlocking {
        val shared = server.url("/shared.png").toString(); val evicted = server.url("/evicted.png").toString()
        val memoryOnly = server.url("/memory.png").toString()
        repeat(3) { server.enqueue(response()) }
        assertTrue(image("A", shared) is SuccessResult); assertTrue(image("B", shared) is SuccessResult)
        assertTrue(image("A", evicted) is SuccessResult); assertTrue(image("B", memoryOnly) is SuccessResult)
        assertTrue(cache.loader.diskCache!!.remove(stableId(evicted)))
        cache.loader.memoryCache!!.keys.filter { it.key.endsWith(":" + stableId(evicted)) }.forEach(cache.loader.memoryCache!!::remove)
        assertTrue(cache.loader.diskCache!!.remove(stableId(memoryOnly)))
        fullSweep()
        assertEquals(0, ownerCount(stableId(evicted)))
        assertEquals(2, ownerCount(stableId(shared))); assertEquals(1, ownerCount(stableId(memoryOnly)))
        cache.removeSource("A"); assertTrue(present(shared))
        cache.close(); cache = ArtworkCache(context); cache.synchronizeSources(listOf("B")); fullSweep()
        assertEquals(0, ownerCount(stableId(memoryOnly))); assertEquals(1, ownerCount(stableId(shared)))
        assertTrue(image("B", shared) is SuccessResult); assertEquals(3, server.requestCount)
    }

    @Test fun pruningPreservesActiveEditorAndAllOwnersOfItsKey() = runBlocking {
        val url = server.url("/slow-prune.png").toString()
        server.enqueue(response().setBodyDelay(2, TimeUnit.SECONDS))
        val request = async(Dispatchers.Default) { image("A", url) }
        assertNotNull(withContext(Dispatchers.IO) { server.takeRequest(5, TimeUnit.SECONDS) })
        withTimeout(1500) {
            while (context.cacheDir.resolve("artwork").listFiles().orEmpty().none { it.name.endsWith(".tmp") && it.length() > 0 }) delay(10)
        }
        fullSweep(); assertEquals(1, ownerCount(stableId(url)))
        assertTrue(request.await() is SuccessResult)
        fullSweep(); assertTrue(present(url)); assertEquals(1, ownerCount(stableId(url)))
        cache.removeSource("A"); assertFalse(present(url))
    }

    @Test fun abandonedIndexRowsArePrunedInBoundedBatchesWithoutClearingLiveCache() = runBlocking {
        val url = server.url("/keep.png").toString(); server.enqueue(response()); image("A", url)
        android.database.sqlite.SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath("artwork-owners.db"), null).use { db ->
            db.beginTransaction()
            try {
                val statement = db.compileStatement("INSERT OR IGNORE INTO owners(owner, cache_key) VALUES (?, ?)")
                statement.use { repeat(10_000) { n ->
                    it.bindString(1, stableId("A")); it.bindString(2, stableId("abandoned-$n")); it.executeInsert()
                } }
                db.setTransactionSuccessful()
            } finally { db.endTransaction() }
        }
        assertEquals(10_001, ownerCount())
        fun databaseBytes() = context.cacheDir.listFiles().orEmpty().filter { it.name.startsWith("artwork-owners.db") }.sumOf { it.length() }
        val bytesBefore = databaseBytes()
        val started = android.os.SystemClock.elapsedRealtimeNanos()
        val first = cache.pruneIndex()
        val firstMs = (android.os.SystemClock.elapsedRealtimeNanos() - started) / 1_000_000.0
        assertTrue(first in 1..64); assertTrue(ownerCount() >= 10_001 - 64)
        fullSweep(); assertEquals(1, ownerCount()); assertTrue(present(url))
        val elapsed = (android.os.SystemClock.elapsedRealtimeNanos() - started) / 1_000_000.0
        File(InstrumentationRegistry.getInstrumentation().targetContext.filesDir, "artwork-pruning-probe.json")
            .writeText(org.json.JSONObject().put("abandonedRows", 10000).put("remainingRows", ownerCount())
                .put("sweepMs", elapsed).put("firstBatchMs", firstMs).put("batchLimit", 64)
                .put("databaseBytesBefore", bytesBefore).put("databaseBytesAfter", databaseBytes())
                .put("scope", "Debug emulator; incremental batches. Database size includes journal/WAL side files; no explicit VACUUM.").toString(2))
    }

    @Test fun sharedDiskSurvivesOneOwnerRemovalAndLastOwnerCleansBytesAndMemory() = runBlocking {
        val shared = server.url("/shared.png").toString(); val onlyA = server.url("/a.png").toString()
        server.enqueue(response()); server.enqueue(response())
        assertTrue(image("A", shared) is SuccessResult)
        assertTrue(image("B", shared) is SuccessResult)
        assertTrue(image("A", onlyA) is SuccessResult)
        assertEquals(2, server.requestCount)
        cache.removeSource("A")
        assertTrue(present(shared)); assertFalse(present(onlyA))
        assertFalse(cache.loader.memoryCache!!.keys.any { it.key.startsWith("artwork:${stableId("A")}") })
        assertTrue(cache.loader.memoryCache!!.keys.any { it.key.startsWith("artwork:${stableId("B")}") })
        assertTrue(runCatching { image("A", shared) }.exceptionOrNull() is CancellationException)
        assertEquals(2, server.requestCount)
        cache.removeSource("B")
        assertFalse(present(shared)); assertTrue(cache.loader.memoryCache!!.keys.isEmpty())
    }

    @Test fun ownershipPersistsAcrossLoaderRestartAndAbsentOwnerIsReconciled() = runBlocking {
        val shared = server.url("/shared.png").toString(); val onlyA = server.url("/a.png").toString()
        server.enqueue(response()); server.enqueue(response())
        image("A", shared); image("B", shared); image("A", onlyA)
        cache.close(); cache = ArtworkCache(context)
        // Simulate a 0.10/0.11 ownership database with no snapshot-path side table.
        android.database.sqlite.SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath("artwork-owners.db"), null)
            .use { it.execSQL("DROP TABLE IF EXISTS artwork_files") }
        cache.synchronizeSources(listOf("B"))
        assertFalse(present(onlyA)); assertTrue(present(shared))
        assertTrue(image("B", shared) is SuccessResult); assertEquals(2, server.requestCount)
        cache.removeSource("B"); assertFalse(present(shared))
    }

    @Test fun removalCancelsInFlightBodyAndRejectsLateRequests() = runBlocking {
        val url = server.url("/slow.png").toString()
        server.enqueue(response().setBodyDelay(3, TimeUnit.SECONDS))
        val request = async(Dispatchers.Default) { runCatching { image("A", url) } }
        assertNotNull(withContext(Dispatchers.IO) { server.takeRequest(5, TimeUnit.SECONDS) })
        // Wait for the metadata editor: headers arrived and Coil is copying the delayed body.
        withTimeout(2000) {
            while (context.cacheDir.resolve("artwork").listFiles().orEmpty().none { it.name.endsWith(".tmp") && it.length() > 0 }) delay(10)
        }
        withTimeout(1500) { cache.removeSource("A"); request.await() }
        assertTrue(request.await().exceptionOrNull() is CancellationException)
        assertFalse(present(url))
        assertTrue(runCatching { image("A", url) }.exceptionOrNull() is CancellationException)
        assertEquals(1, server.requestCount)
    }

    @Test fun clearCancelsAllOwnersAndRemovesEveryReference() = runBlocking {
        val url = server.url("/shared.png").toString(); server.enqueue(response())
        image("A", url); image("B", url)
        cache.clear()
        assertFalse(present(url)); assertTrue(cache.loader.memoryCache!!.keys.isEmpty())
        cache.synchronizeSources(listOf("B")); server.enqueue(response())
        assertTrue(image("B", url) is SuccessResult); assertEquals(2, server.requestCount)
    }

    @Test fun unownedLegacyCacheIsDiscardedOnceBeforeNewOwnershipIsAccepted() = runBlocking {
        val url = server.url("/legacy.png").toString()
        val disk = cache.loader.diskCache!!
        disk.openEditor(url)!!.let { editor ->
            disk.fileSystem.write(editor.metadata) { writeUtf8("legacy-fixture") }
            disk.fileSystem.write(editor.data) { write(png) }
            editor.commit()
        }
        cache.close()
        android.database.sqlite.SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath("artwork-owners.db"), null)
            .use { it.version = 0 }
        cache = ArtworkCache(context); cache.synchronizeSources(listOf("B"))
        assertNull(cache.loader.diskCache!!.openSnapshot(url))
        server.enqueue(response()); assertTrue(image("B", url) is SuccessResult)
        cache.synchronizeSources(listOf("B"))
        assertTrue(present(url)); assertEquals(1, server.requestCount)
    }
}
