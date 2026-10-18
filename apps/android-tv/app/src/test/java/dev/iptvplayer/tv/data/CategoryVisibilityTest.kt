package dev.iptvplayer.tv.data

import dev.iptvplayer.tv.TvState
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

    @Test fun adultNamesAreRecognisedInTheLanguagesProvidersWriteThemIn() {
        listOf("DE: Für Erwachsene", "ERWACHSENE", "Filme für Erwachsene", "Erotik", "Porno DE", // German
            "ADULTOS", "Cine adulto", "Para Adultos +18", "Erótico", "Sexo", // Spanish
            "Pornô Brasil", "Filmes Adultos", "Eróticos", // Portuguese
            "Pour adultes", "FR | ADULTE", "Érotique", "Sexe", // French
            "Per adulti", "Film per Adulti", "Erotico", "Sesso", "VM18", // Italian
            "Voor volwassenen", "Yetişkinlere Özel", "YETİŞKİNLER", // Dutch, Turkish
            "Lil Kibar Faqat", "للكبار فقط", "+18 للكبار", "أفلام سكس", "إباحي", // Arabic, in Latin letters and its own script
            "Adult", "ADULTS 18+", "Hentai", "PORNOS", "XXX | VIP", "18 +").forEach {
            assertTrue(it, CategoryVisibility.looksAdult(it))
        }
    }

    @Test fun ordinaryWordsThatContainAnAdultWordStayVisible() {
        listOf("Middlesex", "Sexta-feira Santa", "Sextou", "Sexto Sentido", "Adult Swim", "ADULT SWIM", "Young Adult", "Young Adults",
            "Erwachsenenbildung", "Adulterio", "Adultère", "Pornic", "Kibar Feyzo", "Saxony", "Sachsen", "XXL Comedy",
            "Esporte", "Sport 18", "FR | Séries", "Volwassen worden", "18 Kasım", "Erotomania", "ساكسونيا", "كبار السن").forEach {
            assertFalse(it, CategoryVisibility.looksAdult(it))
        }
    }

    @Test fun aKeyGivesBackItsSectionAndName() {
        assertEquals(MediaKind.LIVE to "Spor | Canlı", CategoryVisibility.row(CategoryVisibility.key(MediaKind.LIVE, "Spor | Canlı")))
        assertEquals(MediaKind.SERIES to "Dram", CategoryVisibility.row(CategoryVisibility.key(MediaKind.EPISODE, "Dram")))
        assertNull(CategoryVisibility.row("Spor"))
        assertNull(CategoryVisibility.row("RADIO|Spor"))
    }

    @Test fun kategorileriYonetShowsARowShownLockedOrHiddenAndHidingWins() {
        val sport = CategoryVisibility.key(MediaKind.LIVE, "Spor")
        val drama = CategoryVisibility.key(MediaKind.SERIES, "Dram")
        val state = TvState(hiddenCategories = setOf(drama), locks = ParentalLocks(categories = setOf(sport, drama)))
        assertEquals(CategoryAccess.LOCKED, state.categoryAccess(MediaKind.LIVE, "Spor"))
        // A row hidden while locked keeps its lock under the hide.
        assertEquals(CategoryAccess.HIDDEN, state.categoryAccess(MediaKind.SERIES, "Dram"))
        // By section: the film category of the same name is its own row.
        assertEquals(CategoryAccess.SHOWN, state.categoryAccess(MediaKind.MOVIE, "Spor"))
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
