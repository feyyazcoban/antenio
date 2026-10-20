package dev.iptvplayer.tv.data

/** A live list's source/category selection; filtering never renumbers its channels. */
data class ChannelScope(val sourceId: String? = null, val category: String? = null, val favoritesOnly: Boolean = false) {
    companion object { const val FAVORITES = "@favorites" }
}

enum class PickOrigin { LIST, ZAP, NUMBER, PREVIOUS, AUTOSTART, CONTROLS }

internal data class LiveIndex(val numbered: List<MediaEntry>, val channels: List<MediaEntry>, val numbers: Map<String, Int>) {
    companion object {
        fun build(catalog: Catalog, scope: ChannelScope, favorites: Set<String>, numberedCatalog: Catalog = catalog): LiveIndex {
            val pool = LiveLists(catalog)
            val numbered = if (catalog === numberedCatalog) pool else LiveLists(numberedCatalog)
            return pool.select(scope, favorites).copy(numbered = numbered.all, numbers = numbered.numbers)
        }
    }
}

/** Built on the publishing worker once. Scope changes reuse the same lists and O(1) numbering maps. */
internal class LiveLists(catalog: Catalog) {
    val all = catalogDisplayOrder(catalog.items.filter { it.kind == MediaKind.LIVE && !it.adult })
    val numbers = numbering(all)
    val byId = all.associateBy { it.id }
    private val sources = all.groupBy { it.sourceId }
    private val sourceNumbers = sources.mapValues { numbering(it.value) }
    private val categories = all.groupBy { it.category }
    private val sourceCategories = sources.mapValues { it.value.groupBy { channel -> channel.category } }

    fun select(scope: ChannelScope, favorites: Set<String>, numberedSource: String? = null): LiveIndex {
        val pool = when {
            scope.sourceId != null && scope.category != null -> sourceCategories[scope.sourceId]?.get(scope.category).orEmpty()
            scope.sourceId != null -> sources[scope.sourceId].orEmpty()
            scope.category != null -> categories[scope.category].orEmpty()
            else -> all
        }
        val visible = if (scope.favoritesOnly) pool.filter { it.id in favorites } else pool
        return LiveIndex(if (numberedSource == null) all else sources[numberedSource].orEmpty(), visible,
            if (numberedSource == null) numbers else sourceNumbers[numberedSource].orEmpty())
    }

    private fun numbering(entries: List<MediaEntry>) = entries.mapIndexed { i, channel -> channel.id to i + 1 }.toMap()
}
