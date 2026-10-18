package dev.iptvplayer.tv.data

/** Typed category identities keep provider IDs separate from M3U labels. */
internal sealed interface AccessCategory {
    data class ProviderId(val value: String) : AccessCategory { init { require(value.isNotBlank()) } }
    data class M3uName(val value: String) : AccessCategory { init { require(value.isNotBlank()) } }
}

internal sealed interface AccessScope {
    val sourceId: String
    data class Source(override val sourceId: String) : AccessScope { init { require(sourceId.isNotBlank()) } }
    data class Content(val ref: ContentRef) : AccessScope { override val sourceId get() = ref.sourceId }
    data class Category(override val sourceId: String, val kind: MediaKind, val key: AccessCategory) : AccessScope {
        init { require(sourceId.isNotBlank() && kind != MediaKind.EPISODE) }
    }
}

internal data class AccessRule(val scope: AccessScope, val hidden: Boolean = false, val pinRequired: Boolean = false)

internal enum class AccessDecision(val visible: Boolean, val canOpen: Boolean) {
    ALLOW(true, true), REQUIRE_PIN(true, false), HIDDEN(false, false),
    UNRESOLVED(false, false), DENIED(false, false)
}

/**
 * A [decision] and, on REQUIRE_PIN, the scopes of the rules that ask for the PIN (pin.md §6): the content, its series,
 * its category or its source. A PIN typed for the title opens exactly these for the session (ParentalSession).
 */
internal data class AccessVerdict(val decision: AccessDecision, val pinScopes: Set<AccessScope> = emptySet())

/**
 * Pure, unauthenticated decision; AccessGate asks it for every open and play, ParentalProjection for the library.
 * Callers must supply the current registry source and canonical catalog entry, not a stale UI copy.
 */
internal class AccessPolicy(rules: List<AccessRule>, val revision: Long = 0) {
    /** The rules as given, kept apart from the caller's list. */
    val rules: List<AccessRule> = java.util.Collections.unmodifiableList(rules.toList())
    private val rulesBySource = this.rules.filter { it.hidden || it.pinRequired }.groupBy { it.scope.sourceId }
    init { require(revision >= 0) }

    /** Series and episode membership must come from one authoritative repository/cache result. */
    data class SeriesContext(val series: MediaEntry, val episodes: List<MediaEntry>)

    fun decide(source: SourceConfig?, entry: MediaEntry, context: SeriesContext? = null): AccessDecision =
        evaluate(source, entry, context).decision

    fun evaluate(source: SourceConfig?, entry: MediaEntry, context: SeriesContext? = null): AccessVerdict {
        if (source == null || source.id.isBlank() || !validEntry(entry, source.id) || entry.adult) return AccessVerdict(AccessDecision.DENIED)
        val applicable = rulesBySource[source.id].orEmpty()
        val direct = applicable.filter { rule -> when (val scope = rule.scope) {
            is AccessScope.Source -> true
            is AccessScope.Content -> scope.ref.localId == entry.localId
            is AccessScope.Category -> false
        } }
        if (direct.any { it.hidden }) return AccessVerdict(AccessDecision.HIDDEN)

        var categoryEntry = entry
        val matches = direct.toMutableList()
        if (entry.kind == MediaKind.EPISODE) {
            val parent = context?.series
            val validParent = parent != null && parent.kind == MediaKind.SERIES && validEntry(parent, source.id) &&
                context.episodes.any { it == entry } &&
                (entry.parentLocalId == null || entry.parentLocalId == parent.localId)
            if (!validParent) {
                // An unrelated source's rules must not affect an unprotected legacy episode.
                return AccessVerdict(if (applicable.isEmpty()) AccessDecision.ALLOW else AccessDecision.UNRESOLVED)
            }
            if (parent!!.adult) return AccessVerdict(AccessDecision.DENIED)
            categoryEntry = parent
            matches += applicable.filter { (it.scope as? AccessScope.Content)?.ref?.localId == parent.localId }
        }
        if (matches.any { it.hidden }) return AccessVerdict(AccessDecision.HIDDEN)
        val categories = applicable.filter { (it.scope as? AccessScope.Category)?.kind == categoryEntry.kind }
        val category: AccessCategory? = when (source.type) {
            SourceType.XTREAM -> categoryEntry.categoryId.takeIf { it.isNotBlank() }?.let(AccessCategory::ProviderId)
            SourceType.M3U -> categoryEntry.category.takeIf { it.isNotBlank() }?.let(AccessCategory::M3uName)
        }
        if (categories.isNotEmpty() && category == null) return AccessVerdict(AccessDecision.UNRESOLVED)
        // A policy encoded for another source type cannot silently become ineffective after an edit.
        if (categories.any { rule ->
                val key = (rule.scope as AccessScope.Category).key
                (source.type == SourceType.XTREAM) != (key is AccessCategory.ProviderId)
            }) return AccessVerdict(AccessDecision.UNRESOLVED)
        matches += categories.filter { (it.scope as AccessScope.Category).key == category }
        return when {
            matches.any { it.hidden } -> AccessVerdict(AccessDecision.HIDDEN)
            matches.any { it.pinRequired } ->
                AccessVerdict(AccessDecision.REQUIRE_PIN, matches.filter { it.pinRequired }.mapTo(LinkedHashSet()) { it.scope })
            else -> AccessVerdict(AccessDecision.ALLOW)
        }
    }

    private fun validEntry(entry: MediaEntry, sourceId: String): Boolean =
        entry.sourceId == sourceId && entry.localId?.takeIf { it.isNotBlank() }?.let {
            ContentRef(sourceId, it).key == entry.id
        } == true
}
