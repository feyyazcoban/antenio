package dev.iptvplayer.tv.data

/** Length-prefixing makes arbitrary source/provider IDs unambiguous without changing provider IDs. */
data class ContentRef(val sourceId: String, val localId: String) {
    init { require(sourceId.isNotBlank() && localId.isNotBlank()) }
    val key: String get() = "${sourceId.length}:$sourceId$localId"

    companion object {
        fun parse(key: String): ContentRef? = runCatching {
            val colon = key.indexOf(':')
            require(colon > 0)
            val length = key.substring(0, colon).toInt()
            require(length > 0 && length < key.length - colon - 1)
            val end = colon + 1 + length
            ContentRef(key.substring(colon + 1, end), key.substring(end))
        }.getOrNull()
    }
}

/** Runtime identities are scoped; encrypted per-source snapshots keep the original local identities. */
fun MediaEntry.inSource(owner: String): MediaEntry {
    require(sourceId.isEmpty() || sourceId == owner)
    require((sourceId.isEmpty() && localId == null) ||
        (localId != null && id == ContentRef(sourceId, localId).key))
    val original = localId ?: id
    return copy(id = ContentRef(owner, original).key, sourceId = owner, localId = original)
}

fun MediaEntry.withoutSource(): MediaEntry = copy(id = localId ?: id, sourceId = "", localId = null)

fun WatchRecord.inSource(owner: String): WatchRecord {
    require(sourceId.isEmpty() || sourceId == owner)
    if (sourceId == owner) return this
    return copy(id = ContentRef(owner, id).key, parentId = parentId?.let { ContentRef(owner, it).key }, sourceId = owner)
}

fun WatchRecord.withoutSource(): WatchRecord {
    if (sourceId.isEmpty()) return this
    fun local(key: String): String {
        val ref = requireNotNull(ContentRef.parse(key))
        require(ref.sourceId == sourceId)
        return ref.localId
    }
    return copy(id = local(id), parentId = parentId?.let(::local), sourceId = "")
}

object SourceLibrary {
    fun combine(sources: List<SourceConfig>, catalogs: Map<String, Catalog>, filterId: String? = null): Catalog {
        SourceRegistry(sources, filterId).checked()
        val items = ArrayList<MediaEntry>()
        val episodes = LinkedHashMap<String, List<MediaEntry>>()
        val warnings = ArrayList<String>()
        val categoryOrder = LinkedHashMap<MediaKind, LinkedHashSet<String>>()
        sources.filter { filterId == null || it.id == filterId }.forEach { source ->
            val catalog = catalogs[source.id] ?: return@forEach
            items.addAll(catalog.items.map { it.inSource(source.id) })
            catalog.m3uEpisodes.forEach { (parent, list) ->
                episodes[ContentRef(source.id, parent).key] = list.map { it.inSource(source.id) }
            }
            warnings.addAll(catalog.warnings)
            catalog.categoryOrder.forEach { (kind, names) -> categoryOrder.getOrPut(kind) { LinkedHashSet() }.addAll(names) }
        }
        return Catalog(items, episodes, warnings, categoryOrder = categoryOrder.mapValues { it.value.toList() })
    }

    fun favorites(sources: List<SourceConfig>, local: (String) -> Set<String>): Set<String> =
        sources.flatMapTo(LinkedHashSet()) { source -> local(source.id).map { ContentRef(source.id, it).key } }

    fun history(sources: List<SourceConfig>, local: (String) -> List<WatchRecord>): List<WatchRecord> =
        sources.flatMap { source -> local(source.id).map { it.inSource(source.id) } }.sortedByDescending { it.updatedAt }
}
