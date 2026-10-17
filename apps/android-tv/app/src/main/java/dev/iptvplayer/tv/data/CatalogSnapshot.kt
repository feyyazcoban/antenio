package dev.iptvplayer.tv.data

import org.json.JSONObject
import org.json.JSONArray
import java.io.*
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

data class CatalogSnapshot(val catalog: Catalog, val updatedAt: Long)

/** Streaming, versioned catalogue serialization; the device store encrypts the resulting bytes. */
object CatalogSnapshotCodec {
    /** The compact format [encode] writes; [decode] reads this and every earlier one. */
    const val VERSION = 4
    private const val MAX_ITEMS = 150_000
    private const val MAX_TEXT_BYTES = 160L * 1024 * 1024
    private val compactStringFields = intArrayOf(1, 2, 3, 4, 5, 8, 9, 11, 18, 19)

    internal fun headerVersion(header: JSONObject): Int = header.getInt("version").also {
        require(it in 1..VERSION) { "Unsupported catalogue version" }
    }

    fun encode(snapshot: CatalogSnapshot): ByteArray = ByteArrayOutputStream().use { output ->
        GZIPOutputStream(output).bufferedWriter(Charsets.UTF_8).use { writer ->
            writer.appendLine(JSONObject().put("version", VERSION).put("updatedAt", snapshot.updatedAt)
                .put("warnings", JSONArray(snapshot.catalog.warnings.map { warning -> JSONObject().put("reason", warning.reason.name)
                    .put("kind", warning.kind?.name).put("count", warning.count.takeIf { it != 0 }) }))
                .put("epgUrls", JSONArray(snapshot.catalog.epgUrls))
                .put("categoryOrder", JSONObject(snapshot.catalog.categoryOrder.entries.associate { it.key.name to JSONArray(it.value) })).toString())
            fun entry(value: MediaEntry, group: String?) {
                require(value.sourceId.isEmpty() && value.localId == null) { "Store only a source's local catalog" }
                // v3 appends category/parent identity, v4 rating and added time; earlier fields are unchanged.
                writer.appendLine(JSONArray(listOf(group, value.id, value.providerId, value.kind.name,
                    value.title, value.category, value.artwork, value.backdrop, value.description,
                    value.year, value.durationSeconds, value.extension, value.uri, JSONObject(value.headers),
                    value.season, value.episode, value.adult, value.episodeOrderKnown, value.epgId, value.epgName,
                    value.categoryId, value.parentLocalId, value.rating.toDouble(), value.addedAt)).toString())
            }
            snapshot.catalog.items.forEach { entry(it, null) }
            snapshot.catalog.m3uEpisodes.forEach { (group, items) ->
                writer.appendLine(JSONArray(listOf(group)).toString())
                items.forEach { entry(it, group) }
            }
        }
        output.toByteArray()
    }

    fun decode(bytes: ByteArray): CatalogSnapshot {
        val bounded = object : FilterInputStream(GZIPInputStream(ByteArrayInputStream(bytes))) {
            var count = 0L
            override fun read(): Int = super.read().also { if (it >= 0) { count++; check(count <= MAX_TEXT_BYTES) } }
            override fun read(b: ByteArray, off: Int, len: Int): Int = `in`.read(b, off, len).also {
                if (it > 0) { count += it; check(count <= MAX_TEXT_BYTES) }
            }
        }
        return bounded.bufferedReader(Charsets.UTF_8).use { reader ->
            val header = JSONObject(reader.readLine() ?: error("Missing catalogue header"))
            val version = headerVersion(header)
            val items = mutableListOf<MediaEntry>()
            val episodes = linkedMapOf<String, MutableList<MediaEntry>>()
            var count = 0
            reader.forEachLine { line ->
                check(++count <= MAX_ITEMS)
                if (version >= 2) {
                    val record = JSONArray(line)
                    require(record.length() == 1 || record.length() == when (version) { 2 -> 20; 3 -> 22; else -> 24 }) { "Invalid catalogue row" }
                    require(record.isNull(0) || record.get(0) is String)
                    val group = if (record.isNull(0)) null else record.getString(0)
                    if (record.length() == 1) {
                        require(group != null)
                        episodes.getOrPut(group) { mutableListOf() }
                    } else {
                        compactStringFields.forEach { require(record.get(it) is String) }
                        if (version >= 3) {
                            require(record.get(20) is String)
                            require(record.isNull(21) || record.get(21) is String)
                        }
                        if (version >= 4) {
                            require(record.get(22) is Number && record.get(23) is Number)
                        }
                        fun optional(i: Int) = if (record.isNull(i)) null else record.getString(i)
                        val headers = record.getJSONObject(13)
                        val entry = MediaEntry(record.getString(1), record.getString(2), MediaKind.valueOf(record.getString(3)),
                            record.getString(4), record.getString(5), optional(6), optional(7), record.getString(8),
                            record.getString(9), record.getLong(10), record.getString(11), optional(12),
                            headers.keys().asSequence().associateWith { headers.getString(it) },
                            record.getInt(14), record.getInt(15), record.getBoolean(16), record.getBoolean(17),
                            epgId = record.getString(18), epgName = record.getString(19),
                            categoryId = if (version >= 3) record.getString(20) else "",
                            parentLocalId = if (version >= 3) optional(21) else null,
                            rating = if (version >= 4) record.getDouble(22).toFloat() else 0f,
                            addedAt = if (version >= 4) record.getLong(23) else 0L)
                        if (group == null) items.add(entry) else episodes.getOrPut(group) { mutableListOf() }.add(entry)
                    }
                } else {
                    val record = JSONObject(line)
                    val group = record.optString("group").takeUnless { record.isNull("group") }
                    if (!record.has("entry")) {
                        require(group != null)
                        episodes.getOrPut(group) { mutableListOf() }
                    } else {
                        val j = record.getJSONObject("entry")
                        fun optional(key: String) = j.optString(key).takeUnless { j.isNull(key) }
                        val headers = j.getJSONObject("headers")
                        val entry = MediaEntry(j.getString("id"), j.getString("provider"), MediaKind.valueOf(j.getString("kind")),
                            j.getString("title"), j.getString("category"), optional("artwork"), optional("backdrop"),
                            j.getString("description"), j.getString("year"), j.getLong("duration"), j.getString("extension"),
                            optional("uri"), headers.keys().asSequence().associateWith { headers.getString(it) },
                            j.getInt("season"), j.getInt("episode"), j.getBoolean("adult"), j.optBoolean("episodeOrderKnown", false),
                            epgId = j.optString("epgId", ""), epgName = j.optString("epgName", ""))
                        if (group == null) items.add(entry) else episodes.getOrPut(group) { mutableListOf() }.add(entry)
                    }
                }
            }
            val warnings = header.getJSONArray("warnings")
            val feeds = header.optJSONArray("epgUrls") ?: JSONArray()
            // Older snapshots have no order; categories then follow first appearance until the next refresh.
            val order = header.optJSONObject("categoryOrder")
            val categoryOrder = MediaKind.entries.mapNotNull { kind ->
                order?.optJSONArray(kind.name)?.let { names -> kind to (0 until names.length()).map { names.getString(it) } }
            }.toMap()
            CatalogSnapshot(Catalog(items, episodes, (0 until warnings.length()).mapNotNull { warning(warnings.get(it)) },
                (0 until feeds.length()).map { feeds.getString(it) }, categoryOrder), header.getLong("updatedAt"))
        }
    }

    /** A stored warning; one this version does not know (a newer reason) is left out rather than failing the snapshot. */
    private fun warning(value: Any): CatalogWarning? = try {
        when (value) {
            is JSONObject -> CatalogWarning(CatalogWarning.Reason.valueOf(value.getString("reason")),
                value.optString("kind").takeIf { it.isNotEmpty() }?.let(MediaKind::valueOf), value.optInt("count"))
            is String -> legacyWarning(value)
            else -> null
        }
    } catch (_: IllegalArgumentException) { null }

    /** Snapshots written before S01 kept the Turkish sentence itself; these were its only forms. */
    private fun legacyWarning(text: String): CatalogWarning? {
        val kinds = mapOf("Canlı TV" to MediaKind.LIVE, "Filmler" to MediaKind.MOVIE, "Diziler" to MediaKind.SERIES)
        Regex("(.+): kategori adları alınamadı\\.").matchEntire(text)?.let { match ->
            return kinds[match.groupValues[1]]?.let { CatalogWarning(CatalogWarning.Reason.CATEGORIES_UNAVAILABLE, it) }
        }
        Regex("(.+) yüklenemedi\\. Kaynağı yenileyerek tekrar deneyebilirsin\\.").matchEntire(text)?.let { match ->
            return kinds[match.groupValues[1]]?.let { CatalogWarning(CatalogWarning.Reason.KIND_UNAVAILABLE, it) }
        }
        Regex("(\\d+) geçersiz adres atlandı\\.").matchEntire(text)?.let { match ->
            return match.groupValues[1].toIntOrNull()?.let { CatalogWarning(CatalogWarning.Reason.INVALID_ADDRESSES, count = it) }
        }
        return CatalogWarning(CatalogWarning.Reason.FEEDS_LIMITED).takeIf { text == "Listenin ilk dört yayın rehberi adresi alındı." }
    }
}
