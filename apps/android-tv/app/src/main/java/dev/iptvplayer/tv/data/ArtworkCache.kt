package dev.iptvplayer.tv.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import coil3.ImageLoader
import coil3.Extras
import coil3.disk.DiskCache
import coil3.intercept.Interceptor
import coil3.memory.MemoryCache
import coil3.network.*
import coil3.network.okhttp.asNetworkClient
import coil3.request.ImageResult
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okio.Path.Companion.toOkioPath
import okhttp3.Call
import okhttp3.OkHttpClient
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/** Only runtime data. URLs never enter the ownership index or its diagnostic representation. */
internal data class SourceArtwork(val owner: String, val url: String) {
    override fun toString() = "SourceArtwork(REDACTED)"
}

/** One process-wide loader/cache. Lifecycle mutations are called inside SourceTransactions. */
internal class ArtworkCache(private val context: Context) : Interceptor {
    private val gate = Mutex()
    private val lifecycle = Mutex()
    @Volatile private var active = emptySet<String>()
    private data class InFlight(val owner: String, val key: String)
    private val running = mutableMapOf<Job, InFlight>()
    private val maintenanceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var maintenance: Job? = null
    private var completedSinceSweep = 0
    private var sweepAfter = 0L
    private val calls = ConcurrentHashMap<Call, String>()
    private val ownerKey = Extras.Key("")
    private val client by lazy { OkHttpClient.Builder().connectTimeout(8, TimeUnit.SECONDS).readTimeout(8, TimeUnit.SECONDS)
        .callTimeout(20, TimeUnit.SECONDS).build() }
    private val network = object : NetworkClient {
        override suspend fun <T> executeRequest(request: NetworkRequest, block: suspend (NetworkResponse) -> T): T {
            val owner = checkNotNull(request.extras[ownerKey])
            val http = withContext(Dispatchers.IO) { client }
            var call: Call? = null
            val factory = Call.Factory { raw -> http.newCall(raw).also {
                call = it
                calls[it] = owner
                // Registration can race with removal. Inactive owners never start a late socket.
                if (owner !in active) it.cancel()
            } }
            try { return factory.asNetworkClient().executeRequest(request, block) }
            finally { call?.let { calls.remove(it) } }
        }
    }
    private var index: SQLiteDatabase? = null
    private val memory by lazy { MemoryCache.Builder().maxSizePercent(context, 0.12).build() }
    private val disk by lazy { DiskCache.Builder().directory(context.cacheDir.resolve("artwork").toOkioPath())
        .maxSizeBytes(80L * 1024 * 1024).build() }
    val loader: ImageLoader by lazy { ImageLoader.Builder(context).memoryCache(memory).diskCache(disk)
        .components { add(this@ArtworkCache); add(NetworkFetcher.Factory(networkClient = { network })) }.build() }

    // Called under gate, on IO. Marker follows cache clear so interrupted migration is retried.
    private fun database(): SQLiteDatabase {
        index?.let { return it }
        val path = context.getDatabasePath("artwork-owners.db")
        check(path.parentFile!!.isDirectory || path.parentFile!!.mkdirs())
        val db = SQLiteDatabase.openOrCreateDatabase(path, null)
        try {
            db.execSQL("CREATE TABLE IF NOT EXISTS owners (owner TEXT NOT NULL, cache_key TEXT NOT NULL, PRIMARY KEY(owner, cache_key))")
            db.execSQL("CREATE INDEX IF NOT EXISTS artwork_key ON owners(cache_key)")
            // Paths come from Coil snapshots, never from provider URLs or guessed cache filenames.
            db.execSQL("CREATE TABLE IF NOT EXISTS artwork_files (cache_key TEXT PRIMARY KEY NOT NULL, metadata TEXT NOT NULL, data TEXT NOT NULL)")
            if (db.version != 1) {
                // Pre-0.10 cached URLs have no trustworthy owner. One-time disposable cache migration.
                disk.clear(); memory.clear(); db.delete("owners", null, null); db.delete("artwork_files", null, null); db.version = 1
            }
            index = db
            return db
        } catch (e: Exception) { db.close(); throw e }
    }

    override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
        val artwork = chain.request.data as? SourceArtwork
            ?: throw IllegalArgumentException("Artwork requires a source owner")
        val owner = stableId(artwork.owner)
        val key = stableId(artwork.url)
        return coroutineScope {
            val job = currentCoroutineContext().job
            try {
                withContext(Dispatchers.IO) { gate.withLock {
                    if (owner !in active) throw CancellationException("Artwork source is inactive")
                    database().execSQL("INSERT OR IGNORE INTO owners(owner, cache_key) VALUES (?, ?)", arrayOf(owner, key))
                    running[job] = InFlight(owner, key)
                } }
                val request = chain.request.newBuilder().data(artwork.url)
                    .memoryCacheKey("artwork:$owner:$key").diskCacheKey(key).apply { extras[ownerKey] = owner }.build()
                chain.withRequest(request).proceed()
            } finally {
                withContext(NonCancellable + Dispatchers.IO) { gate.withLock {
                    running.remove(job)
                    // A completed write can now expose its snapshot paths. An active editor is
                    // protected by running, and an unknown path is checked in a later sweep.
                    if (owner in active) runCatching { rememberFiles(database(), key) }
                    if (++completedSinceSweep >= 16) { completedSinceSweep = 0; scheduleSweep() }
                } }
            }
        }
    }

    suspend fun synchronizeSources(ids: Collection<String>) = withContext(Dispatchers.IO) { lifecycle.withLock {
        val wanted = ids.mapTo(HashSet(), ::stableId)
        val obsolete = gate.withLock {
            val known = database().rawQuery("SELECT DISTINCT owner FROM owners", null).use { cursor ->
                buildSet { while (cursor.moveToNext()) add(cursor.getString(0)) }
            }
            (known + active) - wanted
        }
        obsolete.forEach { removeOwner(it) }
        gate.withLock { active = wanted; scheduleSweep() }
    } }

    suspend fun removeSource(id: String) = withContext(Dispatchers.IO) { lifecycle.withLock { removeOwner(stableId(id)) } }

    private suspend fun removeOwner(owner: String) {
        val jobs = gate.withLock { active = active - owner; running.filterValues { it.owner == owner }.keys.toList() }
        jobs.forEach { it.cancel() }
        // Coroutine cancellation alone cannot interrupt a blocking response-body copy.
        calls.filterValues { it == owner }.keys.forEach { it.cancel() }
        jobs.joinAll() // Wait for decoder/network snapshots to close before evicting disk entries.
        gate.withLock {
            val db = database()
            val uniqueKeys = db.rawQuery("SELECT cache_key FROM owners WHERE owner = ? AND cache_key NOT IN (SELECT cache_key FROM owners WHERE owner != ?)",
                arrayOf(owner, owner)).use { cursor -> buildList { while (cursor.moveToNext()) add(cursor.getString(0)) } }
            uniqueKeys.forEach { key ->
                disk.remove(key)
                check(disk.openSnapshot(key)?.use { false } ?: true) { "Artwork cache cleanup failed" }
            }
            memory.keys.filter { it.key.startsWith("artwork:$owner:") }.forEach(memory::remove)
            // Delete references last. If eviction fails or the process dies, cleanup is retryable.
            db.delete("owners", "owner = ?", arrayOf(owner))
            db.execSQL("DELETE FROM artwork_files WHERE cache_key NOT IN (SELECT cache_key FROM owners)")
        }
    }

    suspend fun clear() = withContext(Dispatchers.IO) { lifecycle.withLock {
        val jobs = gate.withLock { active = emptySet(); running.keys.toList() }
        jobs.forEach { it.cancel() }; calls.keys.forEach { it.cancel() }; jobs.joinAll()
        gate.withLock {
            val db = database(); disk.clear(); memory.clear(); db.delete("owners", null, null)
            db.delete("artwork_files", null, null); sweepAfter = 0; completedSinceSweep = 0
        }
    } }

    // Called under gate. Maintenance never acquires lifecycle/source locks or holds snapshots.
    private fun scheduleSweep() {
        if (maintenance?.isActive != true && active.isNotEmpty()) {
            maintenance = maintenanceScope.launch {
                try { pruneIndex() }
                catch (e: CancellationException) { throw e }
                catch (_: Exception) { /* Disposable cache maintenance retries on later activity. */ }
            }
        }
    }

    private fun rememberFiles(db: SQLiteDatabase, key: String) {
        val known = db.rawQuery("SELECT 1 FROM artwork_files WHERE cache_key = ?", arrayOf(key)).use { it.moveToFirst() }
        if (known) return
        disk.openSnapshot(key)?.use { snapshot ->
            db.execSQL("INSERT OR IGNORE INTO artwork_files(cache_key, metadata, data) VALUES (?, ?, ?)",
                arrayOf(key, snapshot.metadata.relativeTo(disk.directory).toString(), snapshot.data.relativeTo(disk.directory).toString()))
        }
    }

    /** Bounded, incremental pruning. References survive while bytes, memory or an in-flight job exist. */
    internal suspend fun pruneIndex(): Int = withContext(Dispatchers.IO) { gate.withLock {
        val db = database()
        val rows = db.rawQuery("SELECT rowid, cache_key FROM owners WHERE rowid > ? ORDER BY rowid LIMIT 64",
            arrayOf(sweepAfter.toString())).use { cursor -> buildList {
                while (cursor.moveToNext()) add(cursor.getLong(0) to cursor.getString(1))
            } }
        sweepAfter = rows.lastOrNull()?.first ?: 0L
        val protected = running.values.mapTo(HashSet()) { it.key }
        memory.keys.forEach { protected.add(it.key.substringAfterLast(':')) }
        val obsolete = rows.map { it.second }.distinct().filter { key ->
            if (key in protected) false else {
                rememberFiles(db, key)
                val paths = db.rawQuery("SELECT metadata, data FROM artwork_files WHERE cache_key = ?", arrayOf(key))
                    .use { if (it.moveToFirst()) it.getString(0) to it.getString(1) else null }
                // Checking recorded paths does not open snapshots and promote cold images in Coil's LRU.
                paths == null || !disk.fileSystem.exists(disk.directory / paths.first) || !disk.fileSystem.exists(disk.directory / paths.second)
            }
        }
        if (obsolete.isNotEmpty()) {
            db.beginTransaction()
            try {
                obsolete.forEach { key ->
                    db.delete("owners", "cache_key = ?", arrayOf(key))
                    db.delete("artwork_files", "cache_key = ?", arrayOf(key))
                }
                db.setTransactionSuccessful()
            } finally { db.endTransaction() }
        }
        rows.size
    } }

    /** Used when the owner of an isolated loader is destroyed; callers must not reuse it. */
    suspend fun close() = withContext(Dispatchers.IO) { lifecycle.withLock {
        maintenanceScope.coroutineContext.job.cancelAndJoin()
        val jobs = gate.withLock { active = emptySet(); running.keys.toList() }
        jobs.forEach { it.cancel() }; calls.keys.forEach { it.cancel() }; jobs.joinAll()
        gate.withLock { loader.shutdown(); disk.shutdown(); index?.close(); index = null }
    } }
}
