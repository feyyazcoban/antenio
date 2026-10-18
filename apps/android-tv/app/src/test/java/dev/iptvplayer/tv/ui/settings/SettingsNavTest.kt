package dev.iptvplayer.tv.ui.settings

import androidx.compose.ui.focus.FocusRequester
import dev.iptvplayer.tv.data.MediaKind
import org.junit.Assert.*
import org.junit.Test

class SettingsNavTest {
    private val main = SettingsRoute.Page("main")
    private val categories = SettingsRoute.Page("categories")
    private val movies = SettingsRoute.Page("category-list:MOVIE")
    private val about = SettingsRoute.Page("about")
    private val clearHistory = SettingsRoute.Page("clear-history")

    @Test fun backOnTheFirstPageLeavesThePanelToClose() {
        val nav = SettingsNav(main)
        assertEquals(SettingsNav.Entry(main), nav.top)
        assertFalse(nav.back())
        assertEquals(SettingsNav.Entry(main), nav.top)
    }

    @Test fun backLeavesOnePageAndReturnsToTheRowThatOpenedIt() {
        val nav = SettingsNav(main)
        nav.open(categories, "manage-categories")
        assertEquals(SettingsNav.Entry(categories), nav.top)
        nav.open(movies, "category-kind:MOVIE")
        assertEquals(SettingsNav.Entry(movies), nav.top)
        assertTrue(nav.back())
        assertEquals(SettingsNav.Entry(categories, "category-kind:MOVIE"), nav.top)
        assertTrue(nav.back())
        assertEquals(SettingsNav.Entry(main, "manage-categories"), nav.top)
        assertFalse(nav.back())
    }

    @Test fun aPageOpenedAgainStartsOnTheRowThatLastOpenedAPageFromIt() {
        val nav = SettingsNav(main)
        nav.open(categories, "manage-categories")
        nav.open(movies, "category-kind:MOVIE")
        nav.back(); nav.back()
        nav.open(categories, "manage-categories")
        assertEquals(SettingsNav.Entry(categories, "category-kind:MOVIE"), nav.top)
        // A page nothing was opened from starts where it always does.
        nav.back(); nav.open(about, "about")
        assertEquals(SettingsNav.Entry(about), nav.top)
    }

    @Test fun theOpenPageIsNotStackedTwiceByARepeatedOk() {
        val nav = SettingsNav(main)
        nav.open(about, "about")
        nav.open(about, "about")
        assertTrue(nav.back())
        assertEquals(SettingsNav.Entry(main, "about"), nav.top)
        assertFalse(nav.back())
    }

    @Test fun aRepeatedOkOnAPagesOwnButtonLeavesOnlyThatPage() {
        val nav = SettingsNav(main)
        nav.open(clearHistory, "watch-history")
        assertTrue(nav.back(from = clearHistory))
        // The second OK reaches Vazgeç before the panel recomposes: the main page stays, and so does the panel.
        assertTrue(nav.back(from = clearHistory))
        assertEquals(SettingsNav.Entry(main, "watch-history"), nav.top)
        // The Back key leaves the top page whatever it is.
        assertFalse(nav.back())
    }

    @Test fun thePagesBelowTheTopStayOpenUntilBackLeavesThem() {
        val nav = SettingsNav(main)
        nav.open(categories, "manage-categories")
        nav.open(movies, "category-kind:MOVIE")
        assertTrue(nav.isOpen(main) && nav.isOpen(categories) && nav.isOpen(movies))
        nav.back()
        assertFalse(nav.isOpen(movies))
        assertTrue(nav.isOpen(categories))
        assertFalse(nav.isOpen(about))
    }

    @Test fun aPageOpenedWithoutARowReturnsToThePageStart() {
        val nav = SettingsNav(main)
        nav.open(about, "about")
        nav.back()
        nav.open(SettingsRoute.Choice("guide-offset"), null)
        nav.back()
        assertEquals(SettingsNav.Entry(main), nav.top)
    }

    @Test fun aNewLanguageBringsTheMainPageBackOnTheRowItWasLeftOn() {
        val nav = SettingsNav(main)
        assertNull(nav.rootFocus)
        // Dil / Language opens its options; picking one recreates the activity with that ChoicePage still on top.
        nav.open(SettingsRoute.Choice("language"), "language")
        assertEquals("language", nav.rootFocus)
        val again = SettingsNav(main, nav.rootFocus)
        assertEquals(SettingsNav.Entry(main, "language"), again.top)
        assertFalse(again.back())
    }

    @Test fun focusReturnsToARowStillOnThePageOrFallsBackToItsStart() {
        val rows = PageRows()
        assertNull(rows.entry(null))
        val first = FocusRequester(); val chosen = FocusRequester(); val opener = FocusRequester()
        rows.add("first", first, start = false)
        rows.add("option:2", chosen, start = false)
        assertSame(first, rows.entry(null))
        rows.add("opener", opener, start = false)
        assertSame(opener, rows.entry("opener"))
        // A row that left the page is no longer named; one asked to start the page wins over the first row.
        rows.remove("opener", opener)
        assertSame(first, rows.entry("opener"))
        rows.add("option:2", chosen, start = true)
        assertSame(chosen, rows.entry(null))
        // A row composed again under the same key keeps its place when the old composition leaves after it.
        val again = FocusRequester()
        rows.add("first", again, start = false)
        rows.remove("first", first)
        assertSame(again, rows.entry("first"))
    }

    @Test fun everyPageIsRegisteredOnceAndEachCategorySectionHasItsList() {
        val ids = settingsPageList.map { it.id }
        assertEquals(ids.distinct(), ids)
        assertTrue(mainPage.id in settingsPages)
        for (kind in listOf(MediaKind.LIVE, MediaKind.MOVIE, MediaKind.SERIES)) assertTrue(kind.name, "category-list:${kind.name}" in settingsPages)
    }
}
