package dev.iptvplayer.tv.data

/**
 * What the lock rules make of the published library, for the screens (pin.md §4, §7). [ids]: the channels, films and
 * series that ask for the PIN. [categories]: the locked category rows the library shows, as CategoryVisibility.key names
 * them in Ayarlar; a hidden one is not among them. [sources]: the locked sources. [unresolved]: titles of an older list
 * left out because their category identity is missing while a category rule of their source needs it; a refresh brings
 * them back. [unknown]: the rules are not read, or cannot be, so every title asks for the PIN (D049) and none is
 * promoted or fetched before it. [contents]: the titles with a lock of their own (Kilitle in the quick menu), by
 * identity, listed in the library or not. [open]: the locked titles whose every lock a PIN opened in this session; they
 * read as unlocked until the app leaves the screen.
 */
data class ParentalLocks(val ids: Set<String> = emptySet(), val categories: Set<String> = emptySet(),
    val sources: Set<String> = emptySet(), val unresolved: Int = 0, val unknown: Boolean = false,
    val contents: Set<String> = emptySet(), val open: Set<String> = emptySet()) {
    /** Whether a lock rule locks the title [id], whatever PINs opened: it is never promoted nor fetched on its own. */
    fun locked(id: String): Boolean = unknown || id in ids

    /** Whether opening [id] asks for the PIN now: the screens mark it and keep its programmes to themselves. */
    fun asksPin(id: String): Boolean = unknown || id in ids && id !in open
}

/**
 * [catalog]: the library to publish. [missing]: the rules a locked category row needs for identities it gained; they
 * lock in [locks] already. [rows]: each locked row with the rules its lock rests on, for the next publish
 * ([ParentalProjection.project]); unlocking a row removes these as well as the rules of its identities. [groups]: the
 * locked titles by the scopes a PIN must open for each ([ParentalProjection.opened]).
 */
internal data class ParentalView(val catalog: Catalog, val locks: ParentalLocks, val missing: List<AccessRule>,
    val rows: Map<String, Set<AccessScope.Category>> = emptyMap(), val groups: Map<Set<AccessScope>, Set<String>> = emptyMap())

/**
 * The lock rules over the library (pin.md §5): which titles a rule locks, and the category rows of Ayarlar behind the
 * provider's identities. A row works by section and name across every source, as hiding does (D068); a rule names one
 * source's identity: an Xtream category ID, or an M3U group name. A locked row therefore spans every identity its name
 * has, and one the provider adds under that name (a new ID, a new source) gets its rule at the next publish; adding a
 * lock tightens, so it needs no PIN. Until the record holds such a rule it locks all the same, here and at the gate
 * (ParentalController.gatePolicy). A row the provider renumbers keeps its lock through the rows of the publish before;
 * that memory ends with the app, so a renumbering whose rule was not saved before the app closed is not followed. An
 * M3U group renamed loses its lock, as a hidden one does. Pure; run it off the main thread, on the library as published
 * (hidden categories already left out).
 */
internal object ParentalProjection {
    /** [before]: [ParentalView.rows] of the last publish. */
    fun project(sources: List<SourceConfig>, visible: Catalog, policy: AccessPolicy?,
        before: Map<String, Set<AccessScope.Category>> = emptyMap()): ParentalView {
        // An unread policy locks everything at the gate (AccessGate), which needs no list here; what the rows rested
        // on is kept for when the record reads again.
        if (policy == null) return ParentalView(visible, ParentalLocks(unknown = true), emptyList(), before)
        // Without rules nothing is computed and the same library goes out.
        if (policy.rules.none { it.hidden || it.pinRequired }) return ParentalView(visible, ParentalLocks(), emptyList())
        val pinned = policy.rules.filter { it.pinRequired && !it.hidden }
        val lockedScopes = pinned.mapNotNullTo(HashSet()) { it.scope as? AccessScope.Category }
        val rows = if (lockedScopes.isEmpty()) emptyMap() else categoryRows(sources, visible)
        // A row is locked by a rule on one of its identities, or by a rule that locked it at the last publish though the
        // provider has since replaced that identity (a category renumbered): its name carries the lock over.
        val lockedRows = LinkedHashMap<String, Set<AccessScope.Category>>()
        for (key in rows.keys + before.keys) {
            val by = (rows[key].orEmpty() + before[key].orEmpty()).filterTo(LinkedHashSet()) { it in lockedScopes }
            if (by.isNotEmpty()) lockedRows[key] = by
        }
        val ruled = policy.rules.mapTo(HashSet()) { it.scope }
        val missing = lockedRows.keys.flatMap { rows[it].orEmpty() }.filter { it !in ruled }.distinct()
            .map { AccessRule(it, pinRequired = true) }
        // The rules being added lock at once, and still do while the record refuses them (fail closed).
        val effective = if (missing.isEmpty()) policy else AccessPolicy(policy.rules + missing, policy.revision)
        val byId = sources.associateBy { it.id }
        val locked = HashSet<String>()
        // Few distinct sets: one per locked category, title or source, so a PIN's grants map onto titles quickly.
        val groups = HashMap<Set<AccessScope>, MutableSet<String>>()
        var unresolved = 0
        val items = visible.items.filter { entry ->
            // An adult title is out of every screen whatever its rules say.
            if (entry.adult) return@filter true
            val verdict = effective.evaluate(byId[entry.sourceId], entry)
            when (verdict.decision) {
                AccessDecision.REQUIRE_PIN -> { locked += entry.id; groups.getOrPut(verdict.pinScopes) { HashSet() } += entry.id; true }
                AccessDecision.UNRESOLVED -> { unresolved++; false }
                AccessDecision.HIDDEN -> false
                AccessDecision.ALLOW, AccessDecision.DENIED -> true
            }
        }
        val catalog = if (items.size == visible.items.size) visible else visible.copy(items = items)
        // The screens list a locked row while the library shows it. One hidden in Ayarlar keeps its rules and its memory
        // in [rows], but reads as hidden alone, before and after the app restarts.
        return ParentalView(catalog, ParentalLocks(locked, lockedRows.keys.filterTo(HashSet()) { it in rows },
            pinned.mapNotNullTo(HashSet()) { (it.scope as? AccessScope.Source)?.sourceId }, unresolved,
            contents = pinned.mapNotNullTo(LinkedHashSet()) { (it.scope as? AccessScope.Content)?.ref?.key }),
            missing, lockedRows, groups)
    }

    /**
     * The titles of [groups] ([ParentalView.groups]) whose every lock [grants] opened: what PINs opened in the session
     * (ParentalSession), as the gate would decide it.
     */
    fun opened(groups: Map<Set<AccessScope>, Set<String>>, grants: Set<AccessScope>): Set<String> =
        if (grants.isEmpty()) emptySet() else groups.entries.filter { grants.containsAll(it.key) }.flatMapTo(HashSet()) { it.value }

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
                // An Xtream title without its category ID (an older record, or one filed under no category) names no
                // identity; the rule cannot follow it.
                SourceType.XTREAM -> entry.categoryId.takeIf { it.isNotBlank() }?.let(AccessCategory::ProviderId)
                SourceType.M3U -> entry.category.takeIf { it.isNotBlank() }?.let(AccessCategory::M3uName)
            } ?: continue
            rows.getOrPut(CategoryVisibility.key(entry.kind, entry.category)) { LinkedHashSet() } +=
                AccessScope.Category(source.id, entry.kind, key)
        }
        return rows
    }
}
