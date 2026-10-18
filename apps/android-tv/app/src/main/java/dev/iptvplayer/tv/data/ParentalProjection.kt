package dev.iptvplayer.tv.data

/**
 * What the lock rules make of the published library, for the screens (pin.md §4, §7). [ids]: the channels, films and
 * series that ask for the PIN. [categories]: the locked category rows, as CategoryVisibility.key names them in Ayarlar.
 * [sources]: the locked sources. [unresolved]: titles of an older list left out because their category identity is
 * missing while a category rule of their source needs it; a refresh brings them back.
 */
data class ParentalLocks(val ids: Set<String> = emptySet(), val categories: Set<String> = emptySet(),
    val sources: Set<String> = emptySet(), val unresolved: Int = 0)

/** [catalog]: the library to publish. [missing]: the rules a locked category row needs for identities it gained. */
internal data class ParentalView(val catalog: Catalog, val locks: ParentalLocks, val missing: List<AccessRule>)

/**
 * The lock rules over the library (pin.md §5): which titles a rule locks, and the category rows of Ayarlar behind the
 * provider's identities. A row works by section and name across every source, as hiding does (D068); a rule names one
 * source's identity: an Xtream category ID, or an M3U group name. A locked row therefore spans every identity its name
 * has, and one the provider adds under that name (a new ID, a new source) gets its rule at the next publish; adding a
 * lock tightens, so it needs no PIN. An M3U group renamed loses its lock, as a hidden one does. Pure; run it off the
 * main thread, on the library as published (hidden categories already left out).
 */
internal object ParentalProjection {
    fun project(sources: List<SourceConfig>, visible: Catalog, policy: AccessPolicy?): ParentalView {
        // Without rules nothing is computed and the same library goes out. An unread policy locks everything at the
        // gate instead (AccessGate), which needs no list here.
        if (policy == null || policy.rules.none { it.hidden || it.pinRequired }) return ParentalView(visible, ParentalLocks(), emptyList())
        val byId = sources.associateBy { it.id }
        val locked = HashSet<String>()
        var unresolved = 0
        val items = visible.items.filter { entry ->
            // An adult title is out of every screen whatever its rules say.
            if (entry.adult) return@filter true
            when (policy.decide(byId[entry.sourceId], entry)) {
                AccessDecision.REQUIRE_PIN -> { locked += entry.id; true }
                AccessDecision.UNRESOLVED -> { unresolved++; false }
                AccessDecision.HIDDEN -> false
                AccessDecision.ALLOW, AccessDecision.DENIED -> true
            }
        }
        val catalog = if (items.size == visible.items.size) visible else visible.copy(items = items)
        val pinned = policy.rules.filter { it.pinRequired && !it.hidden }
        val lockedScopes = pinned.mapNotNullTo(HashSet()) { it.scope as? AccessScope.Category }
        val rows = if (lockedScopes.isEmpty()) emptyMap() else categoryRows(sources, visible)
        val lockedRows = rows.filterValues { scopes -> scopes.any { it in lockedScopes } }.keys
        val ruled = policy.rules.mapTo(HashSet()) { it.scope }
        val missing = lockedRows.flatMap { rows[it].orEmpty() }.filter { it !in ruled }.distinct()
            .map { AccessRule(it, pinRequired = true) }
        return ParentalView(catalog, ParentalLocks(locked, lockedRows,
            pinned.mapNotNullTo(HashSet()) { (it.scope as? AccessScope.Source)?.sourceId }, unresolved), missing)
    }

    /** The identities of the category row [name] of [kind] in every source of [catalog], for a lock set from Ayarlar. */
    fun categoryScopes(sources: List<SourceConfig>, catalog: Catalog, kind: MediaKind, name: String): Set<AccessScope.Category> =
        categoryRows(sources, catalog)[CategoryVisibility.key(kind, name)].orEmpty()

    /** Each category row (CategoryVisibility.key) with the identities its titles carry, in the library's order. */
    private fun categoryRows(sources: List<SourceConfig>, catalog: Catalog): Map<String, Set<AccessScope.Category>> {
        val byId = sources.associateBy { it.id }
        val rows = LinkedHashMap<String, LinkedHashSet<AccessScope.Category>>()
        for (entry in catalog.items) {
            if (entry.adult || entry.kind == MediaKind.EPISODE) continue
            val source = byId[entry.sourceId] ?: continue
            val key = when (source.type) {
                // An older Xtream record without its category ID names no identity; the rule cannot follow it.
                SourceType.XTREAM -> entry.categoryId.takeIf { it.isNotBlank() }?.let(AccessCategory::ProviderId)
                SourceType.M3U -> entry.category.takeIf { it.isNotBlank() }?.let(AccessCategory::M3uName)
            } ?: continue
            rows.getOrPut(CategoryVisibility.key(entry.kind, entry.category)) { LinkedHashSet() } +=
                AccessScope.Category(source.id, entry.kind, key)
        }
        return rows
    }
}
