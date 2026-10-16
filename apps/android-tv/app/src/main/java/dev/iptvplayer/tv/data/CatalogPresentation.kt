package dev.iptvplayer.tv.data

import java.text.Collator
import java.util.Locale

/** A presentation hint, never a claim that the entry cannot play. Raw entries remain intact. */
fun MediaEntry.isProviderDivider(): Boolean {
    if (epgId.isNotBlank()) return false
    val name = title.trim()
    val edge = name.firstOrNull() ?: return false
    if (edge !in "*=_~─━-") return false
    val leading = name.takeWhile { it == edge }.length
    val trailing = name.takeLastWhile { it == edge }.length
    return leading >= 3 && trailing >= 3
}

/** Stable partition: preserve source order inside both groups and keep every identity reachable. */
fun catalogDisplayOrder(entries: List<MediaEntry>): List<MediaEntry> {
    val (dividers, content) = entries.partition { it.isProviderDivider() }
    return if (dividers.isEmpty()) entries else content + dividers
}

internal val MediaEntry.hasBackdrop: Boolean
    get() = !backdrop.isNullOrBlank() && backdrop != artwork

internal val MediaEntry.hasArtwork: Boolean
    get() = !artwork.isNullOrBlank() || hasBackdrop

/** Prefer available artwork for the bounded automatic shelf, without changing the full library. */
internal fun homeShelf(entries: List<MediaEntry>, limit: Int = 12): List<MediaEntry> {
    val illustrated = ArrayList<MediaEntry>(limit)
    val fallback = ArrayList<MediaEntry>(limit)
    for (entry in entries) {
        val target = if (entry.hasArtwork) illustrated else fallback
        if (target.size < limit) target += entry
        if (illustrated.size == limit) break
    }
    return (illustrated + fallback).take(limit)
}

internal fun featuredEntry(movies: List<MediaEntry>, series: List<MediaEntry>, favorites: Set<String>): MediaEntry? {
    val candidates = movies.asSequence() + series.asSequence()
    return candidates.firstOrNull { it.id in favorites && it.hasBackdrop }
        ?: candidates.firstOrNull { it.hasBackdrop }
        ?: candidates.firstOrNull { it.id in favorites && it.hasArtwork }
        ?: candidates.firstOrNull { it.hasArtwork }
        ?: candidates.firstOrNull()
}

/**
 * The [limit] most recently added entries with artwork, newest first; equal dates keep the provider's order. Entries
 * without a provider date (every M3U entry) never qualify. One pass, so a large catalog is not sorted.
 */
internal fun newestEntries(entries: List<MediaEntry>, limit: Int = 6): List<MediaEntry> {
    val top = ArrayList<MediaEntry>(limit + 1)
    for (entry in entries) {
        if (entry.addedAt <= 0 || !entry.hasArtwork) continue
        if (top.size == limit && entry.addedAt <= top.last().addedAt) continue
        val at = top.indexOfFirst { entry.addedAt > it.addedAt }.let { if (it < 0) top.size else it }
        top.add(at, entry)
        if (top.size > limit) top.removeAt(limit)
    }
    return top
}

/** Categories of [entries] in the provider's order; names it did not list follow in first-appearance order. */
fun providerCategories(entries: List<MediaEntry>, order: List<String>): List<String> {
    val present = LinkedHashSet<String>()
    entries.forEach { present += it.category }
    val listed = order.filter { it in present }
    val listedSet = listed.toHashSet()
    return listed + present.filter { it !in listedSet }
}

/** How the film and series lists are ordered; the label is what the Sırala button shows. */
enum class LibrarySort(val label: String) {
    DEFAULT("Varsayılan"), ADDED("Son eklenen"), TITLE("Ad (A–Z)"), YEAR("Yıl"), RATING("Puan")
}

private val turkish: Locale = Locale.forLanguageTag("tr")

/**
 * Orders a list for the library. Newest, highest and A–Z come first; titles without a date, year or rating go
 * last. Every order is stable, so equal titles keep the provider's order. Provider dividers are not titles: they
 * stay after the content in their own order, as [catalogDisplayOrder] puts them.
 */
fun librarySorted(entries: List<MediaEntry>, sort: LibrarySort): List<MediaEntry> {
    if (sort == LibrarySort.DEFAULT) return entries
    val (dividers, content) = entries.partition { it.isProviderDivider() }
    // Each key is computed once per title, not on every comparison.
    fun <K : Comparable<K>> by(descending: Boolean, key: (MediaEntry) -> K) = content.map { it to key(it) }
        .sortedWith { a, b -> if (descending) b.second.compareTo(a.second) else a.second.compareTo(b.second) }.map { it.first }
    val sorted = when (sort) {
        LibrarySort.DEFAULT -> content
        LibrarySort.ADDED -> by(true) { it.addedAt }
        LibrarySort.YEAR -> by(true) { it.year.take(4).toIntOrNull() ?: 0 }
        LibrarySort.RATING -> by(true) { it.rating }
        LibrarySort.TITLE -> Collator.getInstance(turkish).apply { strength = Collator.SECONDARY }
            .let { collator -> by(false) { collator.getCollationKey(it.title.trim()) } }
    }
    return sorted + dividers
}

/**
 * A section's sorted list, kept between Library visits. Library leaves composition for Detail and the player,
 * and sorting tens of thousands of titles again on every return would hold up its first frame.
 */
class LibraryOrderCache {
    private class Kept(val catalog: Catalog, val sort: LibrarySort, val items: List<MediaEntry>)
    private val kept = HashMap<MediaKind, Kept>()

    /** The section's titles as Library lists them (no adult titles, dividers last), in [sort] order. */
    fun sorted(catalog: Catalog, kind: MediaKind, sort: LibrarySort): List<MediaEntry> {
        synchronized(this) { kept[kind]?.takeIf { it.catalog === catalog && it.sort == sort }?.let { return it.items } }
        // Sorted outside the lock, so a read on the main thread never waits for a sort running elsewhere.
        val items = librarySorted(catalogDisplayOrder(catalog.items.filter { it.kind == kind && !it.adult }), sort)
        synchronized(this) { kept[kind] = Kept(catalog, sort, items) }
        return items
    }
}

/** "7,5" for a known rating, null otherwise. */
val MediaEntry.ratingText: String? get() = rating.takeIf { it > 0f }?.let { String.format(turkish, "%.1f", it) }
