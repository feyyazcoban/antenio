package dev.iptvplayer.tv.data

import android.content.Context
import android.util.AtomicFile
import java.io.File

/** App-private encrypted snapshots. Call on IO; authenticated decoding finishes before anything is displayed. */
class CatalogStore(context: Context) {
    private val directory = File(context.filesDir, "catalogs")
    private val secrets = DeviceStore(context)
    private fun binding(source: SourceConfig) = stableId(listOf(source.id, source.type.name, source.address,
        source.username, source.password).joinToString("\u0000"))
    private fun ownerPrefix(source: SourceConfig) = stableId(source.id) + "-"
    internal fun file(source: SourceConfig) = File(directory, ownerPrefix(source) + binding(source) + ".bin")
    private fun legacyFile(source: SourceConfig) = File(directory, binding(source) + ".bin")

    private fun compressed(source: SourceConfig): ByteArray {
        val current = file(source)
        val candidate = if (current.exists() || File(current.path + ".bak").exists()) current else legacyFile(source)
        val encrypted = AtomicFile(candidate).openRead().use { input ->
            val output = java.io.ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            while (true) {
                val n = input.read(buffer)
                if (n < 0) break
                check(output.size() + n <= 64 * 1024 * 1024)
                output.write(buffer, 0, n)
            }
            output.toByteArray()
        }
        return secrets.unseal(encrypted, "catalog-v1:${binding(source)}")
    }

    @Synchronized fun read(source: SourceConfig): CatalogSnapshot? = try {
        val bytes = compressed(source)
        try { CatalogSnapshotCodec.decode(bytes) } finally { bytes.fill(0) }
    } catch (_: Exception) { null }

    /** Read only the authenticated header; guide publication must not decode 60k entries a second time. */
    @Synchronized fun readEpgFeeds(source: SourceConfig): List<String>? = try {
        val bytes = compressed(source)
        try {
            java.util.zip.GZIPInputStream(bytes.inputStream()).bufferedReader().use { reader ->
                val header = org.json.JSONObject(reader.readLine() ?: error("Missing header"))
                CatalogSnapshotCodec.headerVersion(header)
                val feeds = header.optJSONArray("epgUrls") ?: org.json.JSONArray()
                (0 until feeds.length()).map { feeds.getString(it) }
            }
        } finally { bytes.fill(0) }
    } catch (_: Exception) { null }

    @Synchronized fun write(source: SourceConfig, snapshot: CatalogSnapshot) {
        check(directory.isDirectory || directory.mkdirs())
        val compressed = CatalogSnapshotCodec.encode(snapshot)
        val encrypted = try { secrets.seal(compressed, "catalog-v1:${binding(source)}") } finally { compressed.fill(0) }
        check(encrypted.size <= 64 * 1024 * 1024)
        val atomic = AtomicFile(file(source))
        val output = atomic.startWrite()
        try { output.write(encrypted); atomic.finishWrite(output) }
        catch (e: Exception) { atomic.failWrite(output); throw e }
        // A verified replacement is available; discard this account's superseded legacy name.
        listOf(legacyFile(source), File(legacyFile(source).path + ".bak"), File(legacyFile(source).path + ".new"))
            .forEach { check(!it.exists() || it.delete()) }
    }

    @Synchronized fun clear() {
        directory.listFiles()?.filter { it.isFile }?.forEach { check(it.delete() || !it.exists()) }
    }

    /** Call only before the first registry commit, while the device still has one legacy owner.
     * Preserve its readable snapshot; unowned caches from older account changes are disposable.
     * Prefixed files belonging to any new source are never touched. Idempotent after interruption.
     */
    @Synchronized fun migrateLegacy(source: SourceConfig?) {
        if (source != null) read(source)?.let { write(source, it) }
        val legacyName = Regex("[a-f0-9]{64}\\.bin(?:\\.bak|\\.new)?")
        directory.listFiles().orEmpty().filter { it.isFile && legacyName.matches(it.name) }.forEach {
            check(it.delete() || !it.exists())
        }
    }

    @Synchronized fun remove(source: SourceConfig) {
        val legacy = legacyFile(source)
        val owned = directory.listFiles().orEmpty().filter { it.isFile && it.name.startsWith(ownerPrefix(source)) }
        (owned + listOf(legacy, File(legacy.path + ".bak"), File(legacy.path + ".new"))).forEach {
            check(!it.exists() || it.delete())
        }
    }
}
