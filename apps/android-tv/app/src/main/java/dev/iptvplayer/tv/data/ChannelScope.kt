package dev.iptvplayer.tv.data

/** A live list's source/category selection; filtering never renumbers its channels. */
data class ChannelScope(val sourceId: String? = null, val category: String? = null, val favoritesOnly: Boolean = false) {
    companion object { const val FAVORITES = "@favorites" }
}

enum class PickOrigin { LIST, ZAP, NUMBER, PREVIOUS, AUTOSTART, CONTROLS }

internal data class ScopeChoice(val scope: ChannelScope, val count: Int)

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
    private val categoryOrder = providerCategories(all, catalog.categoryOrder[MediaKind.LIVE].orEmpty())
    private var favoriteIds: Set<String>? = null
    private var favoriteLists: LiveLists? = null

    fun prepareFavorites(ids: Set<String>) {
        if (favoriteIds == ids) return
        favoriteIds = ids
        // Iterate the small favorite set, rather than the entire catalogue on panel opening.
        favoriteLists = LiveLists(Catalog(ids.mapNotNull(byId::get).sortedBy { numbers[it.id] }, categoryOrder = catalogOrder()))
    }

    private fun catalogOrder() = mapOf(MediaKind.LIVE to categoryOrder)

    fun choices(favorites: Set<String>, source: String? = null): List<ScopeChoice> {
        prepareFavorites(favorites)
        val groups = if (source == null) categories else sourceCategories[source].orEmpty()
        val favoriteCount = if (source == null) favoriteLists!!.all.size else favoriteLists!!.sources[source].orEmpty().size
        return listOf(ScopeChoice(ChannelScope(source, favoritesOnly = true), favoriteCount),
            ScopeChoice(ChannelScope(source), if (source == null) all.size else sources[source].orEmpty().size)) +
            categoryOrder.mapNotNull { name -> groups[name]?.let { ScopeChoice(ChannelScope(source, name), it.size) } }
    }

    fun select(scope: ChannelScope, favorites: Set<String>, numberedSource: String? = null): LiveIndex {
        if (scope.favoritesOnly) {
            prepareFavorites(favorites)
            return favoriteLists!!.select(scope.copy(favoritesOnly = false), emptySet()).copy(
                numbered = if (numberedSource == null) all else sources[numberedSource].orEmpty(),
                numbers = if (numberedSource == null) numbers else sourceNumbers[numberedSource].orEmpty())
        }
        val pool = when {
            scope.sourceId != null && scope.category != null -> sourceCategories[scope.sourceId]?.get(scope.category).orEmpty()
            scope.sourceId != null -> sources[scope.sourceId].orEmpty()
            scope.category != null -> categories[scope.category].orEmpty()
            else -> all
        }
        return LiveIndex(if (numberedSource == null) all else sources[numberedSource].orEmpty(), pool,
            if (numberedSource == null) numbers else sourceNumbers[numberedSource].orEmpty())
    }

    private fun numbering(entries: List<MediaEntry>) = entries.mapIndexed { i, channel -> channel.id to i + 1 }.toMap()
}
