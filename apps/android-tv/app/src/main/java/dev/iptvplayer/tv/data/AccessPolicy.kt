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
 * Pure, unauthenticated decision. Not wired into the UI until persistence and PIN verification exist.
 * Callers must supply the current registry source and canonical catalog entry, not a stale UI copy.
 */
internal class AccessPolicy(rules: List<AccessRule>, val revision: Long = 0) {
    private val rulesBySource = rules.toList().filter { it.hidden || it.pinRequired }.groupBy { it.scope.sourceId }
    init { require(revision >= 0) }

    /** Series and episode membership must come from one authoritative repository/cache result. */
    data class SeriesContext(val series: MediaEntry, val episodes: List<MediaEntry>)

    fun decide(source: SourceConfig?, entry: MediaEntry, context: SeriesContext? = null): AccessDecision {
        if (source == null || source.id.isBlank() || !validEntry(entry, source.id) || entry.adult) return AccessDecision.DENIED
        val applicable = rulesBySource[source.id].orEmpty()
        val direct = applicable.filter { rule -> when (val scope = rule.scope) {
            is AccessScope.Source -> true
            is AccessScope.Content -> scope.ref.localId == entry.localId
            is AccessScope.Category -> false
        } }
        if (direct.any { it.hidden }) return AccessDecision.HIDDEN

        var categoryEntry = entry
        val matches = direct.toMutableList()
        if (entry.kind == MediaKind.EPISODE) {
            val parent = context?.series
            val validParent = parent != null && parent.kind == MediaKind.SERIES && validEntry(parent, source.id) &&
                context.episodes.any { it == entry } &&
                (entry.parentLocalId == null || entry.parentLocalId == parent.localId)
            if (!validParent) {
                // An unrelated source's rules must not affect an unprotected legacy episode.
                return if (applicable.isEmpty()) AccessDecision.ALLOW else AccessDecision.UNRESOLVED
            }
            if (parent!!.adult) return AccessDecision.DENIED
            categoryEntry = parent
            matches += applicable.filter { (it.scope as? AccessScope.Content)?.ref?.localId == parent.localId }
        }
        if (matches.any { it.hidden }) return AccessDecision.HIDDEN
        val categories = applicable.filter { (it.scope as? AccessScope.Category)?.kind == categoryEntry.kind }
        val category: AccessCategory? = when (source.type) {
            SourceType.XTREAM -> categoryEntry.categoryId.takeIf { it.isNotBlank() }?.let(AccessCategory::ProviderId)
            SourceType.M3U -> categoryEntry.category.takeIf { it.isNotBlank() }?.let(AccessCategory::M3uName)
        }
        if (categories.isNotEmpty() && category == null) return AccessDecision.UNRESOLVED
        // A policy encoded for another source type cannot silently become ineffective after an edit.
        if (categories.any { rule ->
                val key = (rule.scope as AccessScope.Category).key
                (source.type == SourceType.XTREAM) != (key is AccessCategory.ProviderId)
            }) return AccessDecision.UNRESOLVED
        matches += categories.filter { (it.scope as AccessScope.Category).key == category }
        return when {
            matches.any { it.hidden } -> AccessDecision.HIDDEN
            matches.any { it.pinRequired } -> AccessDecision.REQUIRE_PIN
            else -> AccessDecision.ALLOW
        }
    }

    private fun validEntry(entry: MediaEntry, sourceId: String): Boolean =
        entry.sourceId == sourceId && entry.localId?.takeIf { it.isNotBlank() }?.let {
            ContentRef(sourceId, it).key == entry.id
        } == true
}
