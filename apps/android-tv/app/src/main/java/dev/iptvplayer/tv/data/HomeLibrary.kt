package dev.iptvplayer.tv.data

import org.json.JSONArray
import org.json.JSONObject

/** Only opaque identifiers and playback state; never persists media URLs or credentials. */
data class WatchRecord(val id: String, val kind: MediaKind, val parentId: String? = null,
    val season: Int = 1, val episode: Int = 1, val positionMs: Long = 0,
    val durationMs: Long = 0, val updatedAt: Long = 0, val sourceId: String = "") {
    val progress get() = PlaybackMark(positionMs, durationMs, updatedAt)
}

object WatchHistory {
    const val LIMIT = 50
    fun upsert(records: List<WatchRecord>, record: WatchRecord): List<WatchRecord> =
        (listOf(record) + records.filterNot { it.id == record.id }).take(LIMIT)
    fun encode(records: List<WatchRecord>): String = JSONArray().apply {
        records.take(LIMIT).forEach { r ->
            require(r.sourceId.isEmpty()) { "Store only a source's local history" }
            put(JSONObject().put("id", r.id).put("kind", r.kind.name)
            .put("parent", r.parentId).put("season", r.season).put("episode", r.episode)
            .put("position", r.positionMs).put("duration", r.durationMs).put("time", r.updatedAt)) }
    }.toString()
    fun decode(value: String?): List<WatchRecord> = try {
        val array = JSONArray(value ?: "[]")
        (0 until minOf(array.length(), LIMIT)).mapNotNull { i ->
            try {
                val r = array.getJSONObject(i)
                WatchRecord(r.getString("id"), MediaKind.valueOf(r.getString("kind")),
                    r.optString("parent").takeUnless { it.isBlank() || it == "null" },
                    r.optInt("season", 1), r.optInt("episode", 1), r.optLong("position").coerceAtLeast(0),
                    r.optLong("duration").coerceAtLeast(0), r.optLong("time"))
            } catch (_: Exception) { null }
        }.distinctBy { it.id }
    } catch (_: Exception) { emptyList() }
}

/** [record] is the latest one for the title. A completed episode means its series goes on with the next episode. */
data class ContinueItem(val entry: MediaEntry, val record: WatchRecord)

/**
 * [newest] is the hero carousel: the most recently added films and series the provider dates. [featured] is the
 * single hero for a catalog without dates (M3U lists). A locked title is never promoted: it stays out of the hero and
 * the automatic shelves, and keeps its place in what the viewer chose (continue, recent channels, favourites). While
 * the locks cannot be read every title counts as locked (ParentalLocks.unknown).
 */
data class HomeLibrary(val continueWatching: List<ContinueItem>, val recentChannels: List<MediaEntry>,
    val favorites: List<MediaEntry>, val channels: List<MediaEntry>, val movies: List<MediaEntry>,
    val series: List<MediaEntry>, val featured: MediaEntry?, val newest: List<MediaEntry> = emptyList())

fun homeLibrary(catalog: Catalog, history: List<WatchRecord>, favorites: Set<String>, locks: ParentalLocks = ParentalLocks()): HomeLibrary {
    val visible = catalog.items.filterNot { it.adult }
    val byId = visible.associateBy { it.id }
    // A finished film leaves the row; a series whose latest episode was finished stays for its next one.
    val continuing = history.asSequence().filter { it.kind != MediaKind.LIVE }
        .distinctBy { it.parentId ?: it.id }.filter { it.progress.resumable || it.kind == MediaKind.EPISODE && it.progress.completed }
        .mapNotNull { record -> byId[record.parentId ?: record.id]?.let { ContinueItem(it, record) } }
        .distinctBy { it.entry.id }.take(12).toList()
    // Automatic discovery does not promote provider headings or locked titles. Explicit favorites/history still work.
    val automatic = visible.filterNot { it.isProviderDivider() || locks.locked(it.id) }
    val movies = automatic.filter { it.kind == MediaKind.MOVIE }
    val series = automatic.filter { it.kind == MediaKind.SERIES }
    return HomeLibrary(continuing,
        history.filter { it.kind == MediaKind.LIVE }.mapNotNull { byId[it.id] }.take(10),
        visible.filter { it.id in favorites }.take(16),
        homeShelf(automatic.filter { it.kind == MediaKind.LIVE }),
        homeShelf(movies), homeShelf(series), featuredEntry(movies, series, favorites), newestEntries(movies + series))
}
