package dev.iptvplayer.tv.data

import org.junit.Assert.*
import org.junit.Test

class ChannelScopeTest {
    @Test fun publishedListsReuseNumbersAcrossCategoriesAndSources() {
        val a = channel("1"); val b = channel("2", "Sport"); val c = channel("3", source = "b")
        val lists = LiveLists(Catalog(listOf(a, b, c, channel("adult").copy(adult = true))))
        val all = lists.select(ChannelScope(), emptySet())
        val category = lists.select(ChannelScope(category = "Sport"), emptySet())
        assertSame(all.numbers, category.numbers)
        assertEquals(listOf(b), category.channels)
        assertEquals(listOf(a, c), lists.select(ChannelScope(favoritesOnly = true), setOf(a.id, c.id)).channels)
        assertEquals(listOf(c), lists.select(ChannelScope(sourceId = "b"), emptySet()).channels)
        assertEquals(mapOf(c.id to 1), lists.select(ChannelScope(sourceId = "b"), emptySet(), "b").numbers)
        assertNull(lists.byId[ContentRef("a", "adult").key])
    }
    private fun channel(id: String, category: String = "News", source: String = "a") =
        MediaEntry(id, id, MediaKind.LIVE, id, category).inSource(source)
    @Test fun filteringKeepsGlobalNumbersAndProviderOrderWithDividersLast() {
        val a = channel("1"); val b = channel("2", "Sport"); val c = channel("3", "Sport")
        val divider = channel("divider").copy(title = "*** INFORMATION ***")
        val hiddenAdult = channel("adult").copy(adult = true)
        val catalog = Catalog(listOf(divider, a, b, c, hiddenAdult, a.copy(id = "movie", kind = MediaKind.MOVIE)))
        val index = LiveIndex.build(catalog, ChannelScope(category = "Sport"), emptySet())
        assertEquals(listOf(b, c), index.channels)
        assertEquals(2, index.numbers[b.id]); assertEquals(3, index.numbers[c.id])
        assertEquals(listOf(a, b, c, divider), index.numbered)
        val favorites = LiveIndex.build(catalog, ChannelScope(favoritesOnly = true), setOf(c.id, a.id))
        assertEquals(listOf(a, c), favorites.channels)
        assertTrue(LiveIndex.build(catalog, ChannelScope(category = "Missing"), emptySet()).channels.isEmpty())
    }
    @Test fun sourceSelectionCannotLeakOtherSourcesAndFavoriteCategoryIntersects() {
        val a = channel("1", source = "a"); val b = channel("1", "Sport", "b"); val c = channel("2", "News", "b")
        val catalog = Catalog(listOf(a, b, c))
        val index = LiveIndex.build(catalog, ChannelScope("b", "Sport", true), setOf(a.id, b.id, c.id))
        assertEquals(listOf(b), index.channels); assertEquals(listOf(a, b, c), index.numbered)
        assertEquals(mapOf(a.id to 1, b.id to 2, c.id to 3), index.numbers)
        val selectedLibrary = LiveIndex.build(catalog, ChannelScope("b"), emptySet(), numberedCatalog = Catalog(listOf(a)))
        assertEquals(listOf(b, c), selectedLibrary.channels)
        assertEquals(listOf(a), selectedLibrary.numbered) // Searching another source does not change numeric remote shortcuts.
        assertNull(selectedLibrary.numbers[b.id])
    }
}
