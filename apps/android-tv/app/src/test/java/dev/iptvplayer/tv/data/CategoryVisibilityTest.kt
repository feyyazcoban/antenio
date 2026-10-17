package dev.iptvplayer.tv.data

import org.junit.Assert.*
import org.junit.Test

class CategoryVisibilityTest {
    private fun entry(id: String, kind: MediaKind, category: String) = MediaEntry(id, id, kind, "Title $id", category)

    @Test fun adultNamesAreRecognisedWithoutCatchingOrdinaryWords() {
        listOf("XXX", "TR ~ ADULT", "Adults Only", "+18", "18+ Filmler", "YETİŞKİN", "Yetişkin", "EROTIK", "Porn").forEach {
            assertTrue(it, CategoryVisibility.looksAdult(it))
        }
        listOf("TURK SPOR", "Essex Dramas", "Sussex", "TR/FILM ~ YERLI 2018", "Kids 18 Ay", "Adultswim Classics").forEach {
            assertFalse(it, CategoryVisibility.looksAdult(it))
        }
    }

    @Test fun hiddenCategoriesLeaveTheCatalogAndChoicesKeepEveryCategory() {
        val catalog = Catalog(listOf(entry("1", MediaKind.LIVE, "SPOR"), entry("2", MediaKind.LIVE, "XXX"),
            entry("3", MediaKind.MOVIE, "SPOR"), entry("4", MediaKind.SERIES, "Dram")),
            mapOf("show" to listOf(entry("5", MediaKind.EPISODE, "Dram"))))
        assertSame(catalog, CategoryVisibility.apply(catalog, emptySet(), hideAdult = false))
        val adultOff = CategoryVisibility.apply(catalog, emptySet(), hideAdult = true)
        assertEquals(listOf("1", "3", "4"), adultOff.items.map { it.id })
        // A name hides only in its own section: live SPOR hidden, the film category of the same name stays.
        val hidden = CategoryVisibility.apply(catalog, setOf(CategoryVisibility.key(MediaKind.LIVE, "SPOR"),
            CategoryVisibility.key(MediaKind.SERIES, "Dram")), hideAdult = true)
        assertEquals(listOf("3"), hidden.items.map { it.id })
        assertTrue("Episodes go with their series' category", hidden.m3uEpisodes.getValue("show").isEmpty())
        val choices = CategoryVisibility.choices(catalog)
        assertEquals(listOf(CategoryChoice("SPOR", 1, false), CategoryChoice("XXX", 1, true)), choices.getValue(MediaKind.LIVE))
        assertEquals(listOf("Dram"), choices.getValue(MediaKind.SERIES).map { it.name })
    }
}
