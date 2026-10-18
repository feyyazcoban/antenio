package dev.iptvplayer.tv.data

/** Favorites are library entries; an episode belongs to its series, never an invisible standalone ID. */
object FavoriteTargets {
    fun resolve(entry: MediaEntry, catalog: Catalog, selected: MediaEntry? = null,
        episodes: List<MediaEntry> = emptyList(), history: List<WatchRecord> = emptyList()): MediaEntry? {
        if (entry.adult) return null
        if (entry.kind != MediaKind.EPISODE)
            return catalog.items.firstOrNull { it.id == entry.id && it.kind == entry.kind && !it.adult }
        val parentId = when {
            selected?.kind == MediaKind.SERIES && !selected.adult && episodes.any { it.id == entry.id } -> selected.id
            else -> catalog.m3uEpisodes.entries.firstOrNull { (_, list) -> list.any { it.id == entry.id } }?.key
                ?: history.firstOrNull { it.id == entry.id && it.kind == MediaKind.EPISODE }?.parentId
        }
        return catalog.items.firstOrNull { it.id == parentId && it.kind == MediaKind.SERIES && !it.adult }
    }

    /** Repair older episode stars only when their parent is known; retain unresolved IDs for a later detail load. */
    fun migrate(ids: Set<String>, catalog: Catalog, selected: MediaEntry? = null,
        episodes: List<MediaEntry> = emptyList(), history: List<WatchRecord> = emptyList()): Set<String> {
        if (ids.isEmpty()) return ids
        // Ordinary movie/channel stars need no full-catalog episode-parent index on every publish.
        if (catalog.m3uEpisodes.isEmpty() && history.none { it.kind == MediaKind.EPISODE && it.parentId != null } &&
            !(selected?.kind == MediaKind.SERIES && !selected.adult && episodes.isNotEmpty())) return ids
        val parents = catalog.items.filter { it.kind == MediaKind.SERIES && !it.adult }.mapTo(HashSet()) { it.id }
        val mapping = mutableMapOf<String, String>()
        history.filter { it.kind == MediaKind.EPISODE && it.parentId in parents }
            .forEach { mapping[it.id] = it.parentId!! }
        catalog.m3uEpisodes.forEach { (parent, list) ->
            if (parent in parents) list.filterNot { it.adult }.forEach { mapping[it.id] = parent }
        }
        if (selected?.id in parents && selected?.kind == MediaKind.SERIES && !selected.adult)
            episodes.filterNot { it.adult }.forEach { mapping[it.id] = selected.id }
        return ids.mapTo(LinkedHashSet()) { mapping[it] ?: it }
    }
}
