package dev.iptvplayer.tv.data

import org.junit.Assert.*
import org.junit.Test

class CatalogPresentationTest {
    private fun entry(id: String, title: String = id, kind: MediaKind = MediaKind.MOVIE) = MediaEntry(id, id, kind, title)

    @Test fun librarySortsAreStableAndPutUnknownValuesLast() {
        val a = entry("a", "Dağ").copy(year = "2010", rating = 7f, addedAt = 30)
        val b = entry("b", "Çiçek").copy(year = "", rating = 0f, addedAt = 0)
        val c = entry("c", "Cam").copy(year = "2021", rating = 8.5f, addedAt = 10)
        val d = entry("d", "Ayna").copy(year = "2010", rating = 7f, addedAt = 30)
        val list = listOf(a, b, c, d)
        assertEquals(list, librarySorted(list, LibrarySort.DEFAULT))
        assertEquals(listOf(a, d, c, b), librarySorted(list, LibrarySort.ADDED))
        assertEquals(listOf(c, a, d, b), librarySorted(list, LibrarySort.YEAR))
        assertEquals(listOf(c, a, d, b), librarySorted(list, LibrarySort.RATING))
        // Turkish alphabet: C before Ç before D.
        assertEquals(listOf(d, c, b, a), librarySorted(list, LibrarySort.TITLE))
        // Dividers are not titles: every order keeps them after the content, in the provider's order.
        val star = entry("s", "*****BİLGİ*****").copy(addedAt = 99, rating = 9f, year = "2030")
        val line = entry("l", "=====4K=====")
        for (sort in LibrarySort.entries.filter { it != LibrarySort.DEFAULT })
            assertEquals(listOf(star, line), librarySorted(listOf(star, a, line, b, c, d), sort).takeLast(2))
        assertEquals("7,5", entry("r").copy(rating = 7.5f).ratingText)
        assertNull(entry("n").ratingText)
    }

    @Test fun decorativeEdgesAreHintsButInfoAndInternalPunctuationAreOrdinaryContent() {
        listOf("*****BİLGİ*****", "=== Spor ===", "  ───Filmler─── ", "*****").forEach {
            assertTrue(entry("divider", it).isProviderDivider())
        }
        listOf("TVP INFO", "SRF INFO", "News Information", "The End of the F***ing World", "Film ***(2023)***",
            "M*A*S*H", "**Yıldızlar**", "*****Özel Kanal", "").forEach {
            assertFalse(entry("content", it).isProviderDivider())
        }
        assertFalse(entry("epg", "*****Kanal*****", MediaKind.LIVE).copy(epgId = "channel.example").isProviderDivider())
    }

    @Test fun displayOrderKeepsEveryIdentityAndSourceOrderInsideEachGroup() {
        val source = listOf(entry("a", "*****BİLGİ*****"), entry("b", "TVP INFO"), entry("c", "=== Filmler ==="), entry("d"))
        val original = source.toList()
        assertEquals(listOf("b", "d", "a", "c"), catalogDisplayOrder(source).map { it.id })
        assertEquals(original, source)
        assertEquals(source.toSet(), catalogDisplayOrder(source).toSet())
    }

    @Test fun automaticShelvesSkipDividersButExplicitFavoritesAndHistoryRemainReachable() {
        val divider = entry("divider", "*****Bilgi*****")
        val content = entry("movie")
        val home = homeLibrary(Catalog(listOf(divider, content)),
            listOf(WatchRecord("divider", MediaKind.MOVIE, positionMs = 100, durationMs = 1000)), setOf("divider"))
        assertEquals(listOf(content), home.movies)
        assertEquals(content, home.featured)
        assertEquals(listOf(divider), home.favorites)
        assertEquals(divider, home.continueWatching.single().entry)
        val empty = homeLibrary(Catalog(listOf(divider)), emptyList(), emptySet())
        assertNull(empty.featured)
        assertTrue(empty.movies.isEmpty())
    }

    @Test fun heroSearchesFullLibraryAndPrefersFavoritesWithWideArtWithoutClaimingPopularity() {
        val missing = (0..20).map { entry("missing:$it") }
        val poster = entry("poster").copy(artwork = "https://art.example/poster.jpg")
        val wide = entry("wide").copy(backdrop = "https://art.example/wide.jpg")
        val favorite = entry("favorite", kind = MediaKind.SERIES).copy(backdrop = "https://art.example/favorite.jpg")
        val catalog = Catalog(missing + poster + wide + favorite)
        val home = homeLibrary(catalog, emptyList(), setOf("favorite"))
        assertEquals(favorite, home.featured)
        assertEquals(listOf(poster, wide), home.movies.take(2))
        assertEquals(12, home.movies.size)
        assertEquals(wide, homeLibrary(catalog, emptyList(), emptySet()).featured)
        assertEquals(missing, catalog.items.take(missing.size))
    }

    @Test fun posterReusedAsBackdropDoesNotBecomeWideArtAndNoArtStillHasContent() {
        val poster = entry("poster").copy(artwork = "https://art.example/same.jpg", backdrop = "https://art.example/same.jpg")
        assertFalse(poster.hasBackdrop)
        val plain = entry("plain")
        assertEquals(poster, homeLibrary(Catalog(listOf(plain, poster)), emptyList(), emptySet()).featured)
        assertEquals(plain, homeLibrary(Catalog(listOf(plain)), emptyList(), emptySet()).featured)
        assertNull(homeLibrary(Catalog(listOf(poster.copy(adult = true))), emptyList(), emptySet()).featured)
    }

    @Test fun firstInvalidBackdropDoesNotDiscardLaterValidProviderArtwork() {
        val json = """[{"series_id":1,"name":"Dizi","backdrop_path":[null,"","file:///bad","https://art.example/wide.jpg"]}]"""
        assertEquals("https://art.example/wide.jpg", CatalogParser.xtream(json, MediaKind.SERIES, emptyMap()).single().backdrop)
        val original = entry("movie").copy(backdrop = "https://art.example/original.jpg")
        assertEquals(original.backdrop, CatalogParser.movie("""{"info":{"backdrop_path":[null, "not a URL"]}}""", original).backdrop)
    }

    @Test fun categoriesFollowTheProviderOrderThenFirstAppearance() {
        fun live(id: String, category: String) = MediaEntry(id, id, MediaKind.LIVE, id, category)
        val entries = listOf(live("1", "TURK ULUSAL"), live("2", "Diğer"), live("3", "TURK SPOR"), live("4", "TURK ULUSAL"), live("5", "Yeni"))
        assertEquals(listOf("TURK SPOR", "TURK ULUSAL", "Diğer", "Yeni"),
            providerCategories(entries, listOf("TURK SPOR", "KKTC ULUSAL", "TURK ULUSAL")))
        assertEquals(listOf("TURK ULUSAL", "Diğer", "TURK SPOR", "Yeni"), providerCategories(entries, emptyList()))
    }
    @Test fun episodeCardsShowTheEpisodesOwnName() {
        assertEquals("Pilot Bölüm", episodeDisplayTitle("Breaking Bad - S01E01 - Pilot Bölüm", "Breaking Bad"))
        assertEquals("", episodeDisplayTitle("Asırlık Gece (2026) S01 E01", "Asırlık Gece (2026)"))
        assertEquals("Sırlar", episodeDisplayTitle("Dark (2017) - S01E01 - Sırlar", "Dark"))
        assertEquals("Birinci bölüm", episodeDisplayTitle("Birinci bölüm", "Test dizisi"))
        assertEquals("", episodeDisplayTitle("8x8 (2023)", "8x8 (2023)"))
        assertEquals("1. Bölüm", episodeDisplayTitle("Big Cat Country - S01E01 - 1. Bölüm", "Big Cat Country"))
    }
}
