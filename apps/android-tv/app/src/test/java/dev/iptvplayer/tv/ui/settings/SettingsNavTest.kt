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

    @Test fun aNewLanguageBringsTheSectionBackOnTheRowItWasLeftOn() {
        val nav = SettingsNav(main)
        val general = SettingsRoute.Page("general")
        assertEquals(listOf(SettingsNav.Entry(main)), nav.kept { true })
        // Dil / Language opens its options; picking one recreates the activity with that ChoicePage still on top.
        nav.open(general, "general")
        nav.open(SettingsRoute.Choice("language"), "language")
        val kept = nav.kept { it is SettingsRoute.Page }
        assertEquals(listOf(SettingsNav.Entry(main, "general"), SettingsNav.Entry(general, "language")), kept)
        val again = SettingsNav(main, kept)
        assertEquals(SettingsNav.Entry(general, "language"), again.top)
        assertTrue(again.back())
        assertEquals(SettingsNav.Entry(main, "general"), again.top)
        assertFalse(again.back())
    }

    @Test fun aPageThatAsksForSomethingIsNotOpenedAgainByAConfigurationChange() {
        val nav = SettingsNav(main)
        val content = SettingsRoute.Page("content")
        val parental = SettingsRoute.Page("parental")
        nav.open(content, "content"); nav.open(parental, "parental"); nav.open(SettingsRoute.Page("parental-locked"), "locked")
        // Ebeveyn denetimi asks for its PIN: it and the pages above it close, and the section returns to its row.
        val kept = nav.kept { it != parental }
        assertEquals(listOf(SettingsNav.Entry(main, "content"), SettingsNav.Entry(content, "parental")), kept)
        assertEquals(SettingsNav.Entry(content, "parental"), SettingsNav(main, kept).top)
        // What another panel kept is no start for this one.
        assertEquals(SettingsNav.Entry(main), SettingsNav(main, listOf(SettingsNav.Entry(content))).top)
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

    @Test fun everySectionHasItsPageAndEveryOtherPageASection() {
        // The main page lists the sections; each opens a page of its own (plan decision 4a-8).
        for (section in SectionId.entries) assertTrue(section.name, settingsPageList.any { it.section == section })
        assertEquals(listOf(mainPage.id), settingsPageList.filter { it.section == null }.map { it.id })
        assertEquals(PageGuard.None, settingsPages.getValue(playerPage.id).guard)
        assertEquals(SectionId.PLAYER, settingsPages.getValue("player-subtitles").section)
    }
}
