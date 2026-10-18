package dev.iptvplayer.tv.data

import org.junit.Assert.*
import org.junit.Test

class ParentalProjectionTest {
    private val home = SourceConfig("a", "Ev", SourceType.XTREAM, "http://127.0.0.1/", "kullanici", "sifre")
    private val list = SourceConfig("b", "Liste", SourceType.M3U, "http://127.0.0.1/list.m3u")
    private val sources = listOf(home, list)
    private fun channel(id: String, category: String, categoryId: String = "", source: String = "a") =
        MediaEntry("live:$id", id, MediaKind.LIVE, "Kanal $id", category, categoryId = categoryId, extension = "ts").inSource(source)
    private val news = channel("1", "Haber", "20")
    private val sport = channel("2", "Spor", "10")
    private val sport2 = channel("3", "Spor", "11")
    private val listSport = channel("4", "Spor", source = "b")
    private val listNews = channel("5", "Haber", source = "b")
    private val movie = MediaEntry("movie:6", "6", MediaKind.MOVIE, "Film", "Aile", categoryId = "30", extension = "mp4").inSource("a")
    private val sportsId = AccessScope.Category("a", MediaKind.LIVE, AccessCategory.ProviderId("10"))
    private fun lock(scope: AccessScope) = AccessRule(scope, pinRequired = true)
    private fun project(catalog: Catalog, vararg rules: AccessRule) = ParentalProjection.project(sources, catalog, AccessPolicy(rules.toList()))

    @Test fun withoutRulesTheSameLibraryGoesOut() {
        val catalog = Catalog(listOf(news, sport, movie))
        for (policy in listOf(AccessPolicy(emptyList()), AccessPolicy(listOf(AccessRule(sportsId))), null)) {
            val view = ParentalProjection.project(sources, catalog, policy)
            assertSame(catalog, view.catalog)
            assertEquals(ParentalLocks(), view.locks)
            assertTrue(view.missing.isEmpty())
        }
    }

    @Test fun categoryContentAndSourceRulesLockTheirTitles() {
        val catalog = Catalog(listOf(news, sport, movie, listSport, listNews))
        val view = project(catalog, lock(sportsId), lock(AccessScope.Content(ContentRef("a", "movie:6"))), lock(AccessScope.Source("b")))
        // Locked titles stay listed; they ask for the PIN on opening.
        assertSame(catalog, view.catalog)
        assertEquals(setOf(sport.id, movie.id, listSport.id, listNews.id), view.locks.ids)
        assertEquals(setOf(CategoryVisibility.key(MediaKind.LIVE, "Spor")), view.locks.categories)
        assertEquals(setOf("b"), view.locks.sources)
        assertEquals(0, view.locks.unresolved)
        // The row "Spor" spans the list's group of that name as well, which gets its rule.
        assertEquals(listOf(lock(AccessScope.Category("b", MediaKind.LIVE, AccessCategory.M3uName("Spor")))), view.missing)
    }

    @Test fun aNameInTwoSourcesIsTwoScopes() {
        val catalog = Catalog(listOf(news, sport, listSport, listNews, movie))
        assertEquals(setOf(sportsId, AccessScope.Category("b", MediaKind.LIVE, AccessCategory.M3uName("Spor"))),
            ParentalProjection.categoryScopes(sources, catalog, MediaKind.LIVE, "Spor"))
        assertEquals(setOf(AccessScope.Category("a", MediaKind.MOVIE, AccessCategory.ProviderId("30"))),
            ParentalProjection.categoryScopes(sources, catalog, MediaKind.MOVIE, "Aile"))
        // A section is its own row: no channel group "Aile".
        assertTrue(ParentalProjection.categoryScopes(sources, catalog, MediaKind.LIVE, "Aile").isEmpty())
    }

    @Test fun aLockedRowGainsTheNewIdentitiesOfItsName() {
        // The provider opened the same name under a new ID, and a new source has a group of that name.
        val view = project(Catalog(listOf(news, sport, sport2, listSport)), lock(sportsId))
        assertEquals(listOf(lock(AccessScope.Category("a", MediaKind.LIVE, AccessCategory.ProviderId("11"))),
            lock(AccessScope.Category("b", MediaKind.LIVE, AccessCategory.M3uName("Spor")))), view.missing)
        // Until they are added, only the rule's own identity locks.
        assertEquals(setOf(sport.id), view.locks.ids)
        // Once added nothing is missing, and every "Spor" asks.
        val added = project(Catalog(listOf(news, sport, sport2, listSport)), lock(sportsId), *view.missing.toTypedArray())
        assertTrue(added.missing.isEmpty())
        assertEquals(setOf(sport.id, sport2.id, listSport.id), added.locks.ids)
        // An identity with a rule of its own (hidden, say) gets no second one.
        assertTrue(project(Catalog(listOf(sport, listSport)), lock(sportsId),
            AccessRule(AccessScope.Category("b", MediaKind.LIVE, AccessCategory.M3uName("Spor")), hidden = true)).missing.isEmpty())
    }

    @Test fun aRenamedListGroupLosesItsLock() {
        val rule = lock(AccessScope.Category("b", MediaKind.LIVE, AccessCategory.M3uName("Spor")))
        assertEquals(setOf(listSport.id), project(Catalog(listOf(listSport, listNews)), rule).locks.ids)
        val renamed = project(Catalog(listOf(listSport.copy(category = "Sporlar"), listNews)), rule)
        assertTrue(renamed.locks.ids.isEmpty())
        assertTrue(renamed.locks.categories.isEmpty())
        assertTrue(renamed.missing.isEmpty())
    }

    @Test fun titlesOfAnOlderListAreLeftOutButHiddenCategoriesAreNotCounted() {
        val old = channel("7", "Spor")
        val raw = Catalog(listOf(news, sport, old, channel("8", "Belgesel"), listSport))
        // "Belgesel" is hidden from Ayarlar (D068): it is not in the library at all.
        val visible = CategoryVisibility.apply(raw, setOf(CategoryVisibility.key(MediaKind.LIVE, "Belgesel")), hideAdult = false)
        val view = ParentalProjection.project(sources, visible, AccessPolicy(listOf(lock(sportsId))))
        assertEquals(listOf(news, sport, listSport), view.catalog.items)
        assertEquals(1, view.locks.unresolved)
        assertEquals(setOf(sport.id), view.locks.ids)
        // A source without category rules keeps an old record as it is.
        assertEquals(0, project(Catalog(listOf(listSport.copy(category = ""))), lock(sportsId)).locks.unresolved)
    }

    @Test fun adultTitlesAreLeftAsTheyAre() {
        val adult = channel("9", "Spor", "10").copy(adult = true)
        val view = project(Catalog(listOf(sport, adult)), lock(sportsId))
        // The screens leave adult titles out on their own; the projection neither locks nor drops them.
        assertEquals(listOf(sport, adult), view.catalog.items)
        assertEquals(setOf(sport.id), view.locks.ids)
    }
}
