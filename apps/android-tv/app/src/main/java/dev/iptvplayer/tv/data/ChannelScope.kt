package dev.iptvplayer.tv.data

/** A live list's source/category selection; filtering never renumbers its channels. */
internal data class ChannelScope(val sourceId: String? = null, val category: String? = null, val favoritesOnly: Boolean = false) {
    companion object { const val FAVORITES = "@favorites" }
}

internal data class LiveIndex(val numbered: List<MediaEntry>, val channels: List<MediaEntry>, val numbers: Map<String, Int>) {
    companion object {
        fun build(catalog: Catalog, scope: ChannelScope, favorites: Set<String>, numberedCatalog: Catalog = catalog): LiveIndex {
            val all = catalogDisplayOrder(numberedCatalog.items.filter { it.kind == MediaKind.LIVE && !it.adult })
            val pool = if (catalog === numberedCatalog) all else catalogDisplayOrder(catalog.items.filter { it.kind == MediaKind.LIVE && !it.adult })
            val visible = pool.filter {
                (scope.sourceId == null || it.sourceId == scope.sourceId) &&
                    (!scope.favoritesOnly || it.id in favorites) && (scope.category == null || it.category == scope.category)
            }
            return LiveIndex(all, visible, all.mapIndexed { i, channel -> channel.id to i + 1 }.toMap())
        }
    }
}
