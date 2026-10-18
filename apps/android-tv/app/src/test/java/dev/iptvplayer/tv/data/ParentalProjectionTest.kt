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
    private fun content(entry: MediaEntry) = AccessScope.Content(ContentRef(entry.sourceId, entry.localId!!))
    private fun project(catalog: Catalog, vararg rules: AccessRule) = ParentalProjection.project(sources, catalog, AccessPolicy(rules.toList()))

    @Test fun withoutRulesTheSameLibraryGoesOut() {
        val catalog = Catalog(listOf(news, sport, movie))
        for (policy in listOf(AccessPolicy(emptyList()), AccessPolicy(listOf(AccessRule(sportsId))))) {
            val view = ParentalProjection.project(sources, catalog, policy)
            assertSame(catalog, view.catalog)
            assertEquals(ParentalLocks(), view.locks)
            assertTrue(view.missing.isEmpty())
        }
    }

    @Test fun anUnreadPolicyLocksEveryTitleAndKeepsWhatTheRowsRestedOn() {
        val catalog = Catalog(listOf(news, sport, movie))
        val rows = mapOf(CategoryVisibility.key(MediaKind.LIVE, "Spor") to setOf(sportsId))
        val view = ParentalProjection.project(sources, catalog, null, rows)
        // The gate asks for every title (D049); the screens promote and fetch none of them.
        assertSame(catalog, view.catalog)
        assertTrue(view.locks.unknown)
        assertTrue(listOf(news, sport, movie).all { view.locks.locked(it.id) })
        assertTrue(view.missing.isEmpty())
        assertEquals(rows, view.rows)
        assertFalse(ParentalLocks().locked(news.id))
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
        // They lock before the record holds them, and while it refuses them.
        assertEquals(setOf(sport.id, sport2.id, listSport.id), view.locks.ids)
        // Once added nothing is missing, and every "Spor" still asks.
        val added = project(Catalog(listOf(news, sport, sport2, listSport)), lock(sportsId), *view.missing.toTypedArray())
        assertTrue(added.missing.isEmpty())
        assertEquals(setOf(sport.id, sport2.id, listSport.id), added.locks.ids)
        // An identity with a rule of its own (hidden, say) gets no second one.
        assertTrue(project(Catalog(listOf(sport, listSport)), lock(sportsId),
            AccessRule(AccessScope.Category("b", MediaKind.LIVE, AccessCategory.M3uName("Spor")), hidden = true)).missing.isEmpty())
    }

    @Test fun aLockedRowTheProviderRenumbersKeepsItsLock() {
        val first = project(Catalog(listOf(news, sport)), lock(sportsId))
        val row = CategoryVisibility.key(MediaKind.LIVE, "Spor")
        assertEquals(mapOf(row to setOf(sportsId)), first.rows)
        // A publish without the row (hidden from Ayarlar for a while) keeps what it rested on, but the screens do not list
        // the row as locked: it reads as hidden alone, as it does once the app restarts without that memory.
        val policy = AccessPolicy(listOf(lock(sportsId)))
        val hidden = ParentalProjection.project(sources, Catalog(listOf(news)), policy, first.rows)
        assertEquals(first.rows, hidden.rows)
        assertTrue(hidden.locks.categories.isEmpty())
        assertTrue(ParentalProjection.project(sources, Catalog(listOf(news)), policy).locks.categories.isEmpty())
        // A refresh: "Spor" is ID 12 now, and ID 10 is gone.
        val renumbered = Catalog(listOf(news, channel("2", "Spor", "12")))
        val view = ParentalProjection.project(sources, renumbered, policy, first.rows)
        val twelve = AccessScope.Category("a", MediaKind.LIVE, AccessCategory.ProviderId("12"))
        assertEquals(listOf(lock(twelve)), view.missing)
        assertEquals(setOf(sport.id), view.locks.ids)
        assertEquals(setOf(row), view.locks.categories)
        // Once its rule is saved the row rests on it as well.
        val saved = ParentalProjection.project(sources, renumbered, AccessPolicy(listOf(lock(sportsId), lock(twelve))), view.rows)
        assertTrue(saved.missing.isEmpty())
        assertEquals(setOf(sport.id), saved.locks.ids)
        assertEquals(mapOf(row to setOf(twelve, sportsId)), saved.rows)
        // Without the rows of the publish before, the name has no lock to carry over.
        assertTrue(project(renumbered, lock(sportsId)).locks.ids.isEmpty())
        // A row unlocked in Ayarlar loses every rule it rested on, and nothing brings its lock back.
        val unlocked = ParentalProjection.project(sources, renumbered, AccessPolicy(listOf(lock(content(movie)))), saved.rows)
        assertTrue(unlocked.missing.isEmpty() && unlocked.rows.isEmpty())
        assertTrue(unlocked.locks.ids.isEmpty())
    }

    @Test fun aTitleTheProviderFiledUnderNoCategoryIsNeitherLockedNorLeftOut() {
        // A list read now: the provider sent no category ID, so the title is under "Diğer".
        val loose = channel("7", CategoryFilter.OTHER)
        val view = project(Catalog(listOf(news, sport, loose)), lock(sportsId))
        assertEquals(listOf(news, sport, loose), view.catalog.items)
        assertEquals(setOf(sport.id), view.locks.ids)
        assertEquals(0, view.locks.unresolved)
        // Its own lock still asks.
        assertEquals(setOf(sport.id, loose.id), project(Catalog(listOf(sport, loose)), lock(sportsId),
            lock(AccessScope.Content(ContentRef("a", "live:7")))).locks.ids)
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
        // The list's "Spor" group locks with the row while its rule is being added.
        assertEquals(setOf(sport.id, listSport.id), view.locks.ids)
        // A source without category rules keeps an old record as it is.
        assertEquals(0, project(Catalog(listOf(listSport.copy(category = ""))), lock(sportsId)).locks.unresolved)
    }

    @Test fun eachLockedTitleIsGroupedByWhatOpensItAndAPinOpensOnlyWhatItCovers() {
        val weather = channel("8", "Haber", "20")
        val catalog = Catalog(listOf(news, sport, movie, listSport, listNews, weather))
        val listGroup = AccessScope.Category("b", MediaKind.LIVE, AccessCategory.M3uName("Spor"))
        val view = project(catalog, lock(sportsId), lock(content(movie)), lock(content(news)), lock(AccessScope.Source("b")))
        // The list's "Spor" asks for its source's lock and for the row's lock, which its new rule carries.
        assertEquals(mapOf(setOf(sportsId) to setOf(sport.id), setOf(content(movie)) to setOf(movie.id),
            setOf(content(news)) to setOf(news.id), setOf(AccessScope.Source("b"), listGroup) to setOf(listSport.id),
            setOf(AccessScope.Source("b")) to setOf(listNews.id)), view.groups)
        // The titles locked on their own, by identity, for the quick menu and Kilitli içerikler.
        assertEquals(setOf(movie.id, news.id), view.locks.contents)
        // A PIN opens what covers every lock of a title, as the gate decides it.
        assertEquals(setOf(sport.id), ParentalProjection.opened(view.groups, setOf(sportsId)))
        assertEquals(setOf(listNews.id), ParentalProjection.opened(view.groups, setOf(AccessScope.Source("b"))))
        assertEquals(setOf(listSport.id, listNews.id), ParentalProjection.opened(view.groups, setOf(AccessScope.Source("b"), listGroup)))
        assertTrue(ParentalProjection.opened(view.groups, emptySet()).isEmpty())
        // What a PIN opened reads as unlocked on screen and is still locked for the hero and the shelves.
        val locks = view.locks.copy(open = ParentalProjection.opened(view.groups, setOf(sportsId)))
        assertFalse(locks.asksPin(sport.id)); assertTrue(locks.locked(sport.id))
        assertTrue(locks.asksPin(movie.id) && locks.asksPin(listSport.id))
        assertFalse(locks.asksPin(weather.id) || locks.locked(weather.id))
        // Unread locks ask for everything, whatever a PIN opened before.
        assertTrue(ParentalLocks(unknown = true, open = setOf(sport.id)).asksPin(sport.id))
    }

    @Test fun adultTitlesAreLeftAsTheyAre() {
        val adult = channel("9", "Spor", "10").copy(adult = true)
        val view = project(Catalog(listOf(sport, adult)), lock(sportsId))
        // The screens leave adult titles out on their own; the projection neither locks nor drops them.
        assertEquals(listOf(sport, adult), view.catalog.items)
        assertEquals(setOf(sport.id), view.locks.ids)
    }
}
