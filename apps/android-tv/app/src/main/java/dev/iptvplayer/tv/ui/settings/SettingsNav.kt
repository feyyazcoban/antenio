package dev.iptvplayer.tv.ui.settings

/** A page of the Settings panel: a registered page by its [SettingsPageSpec.id], or the options of a ChoiceRow. */
internal sealed interface SettingsRoute {
    data class Page(val id: String) : SettingsRoute
    /** The options of the ChoiceRow [rowKey] on the page below. */
    data class Choice(val rowKey: String) : SettingsRoute
}

/**
 * The pages open in the Settings panel, [root] at the bottom. Back leaves one page and focus returns to the row that
 * opened it, named by the row's key: rows come and go with the state (Kaynağı kaldır needs a single source), a key
 * stays. A page opened again in the same visit starts on the row that last opened a page from it, as Kategoriler
 * returns to the section whose list was open. Pure, so these rules are tested on the JVM. [kept] are the pages a
 * configuration change (a new language) left open, as [SettingsNav.kept] said then, [root] first.
 */
internal class SettingsNav(val root: SettingsRoute, kept: List<Entry> = emptyList()) {
    /** [route] on screen and the row it focuses: the opener of the page above once that closes, or its own start. */
    data class Entry(val route: SettingsRoute, val focusKey: String? = null)

    private val stack = if (kept.firstOrNull()?.route == root) kept.toMutableList() else mutableListOf(Entry(root))
    private val lastOpener = mutableMapOf<SettingsRoute, String>()

    val top: Entry get() = stack.last()

    /**
     * The pages to open again after a configuration change, [root] first: those [keep] allows, up to the first it does
     * not. The last one returns to the row that opened the page above it, so a new language comes back on Dil / Language.
     */
    fun kept(keep: (SettingsRoute) -> Boolean): List<Entry> = listOf(stack.first()) + stack.drop(1).takeWhile { keep(it.route) }

    /**
     * Shows [route] above the current page; Back returns to the row [fromRowKey], or to the page's start without one.
     * The page already on top is not opened twice: a second OK can reach the row before the panel recomposes.
     */
    fun open(route: SettingsRoute, fromRowKey: String?) {
        if (route == top.route) return
        if (fromRowKey != null) lastOpener[top.route] = fromRowKey
        stack[stack.lastIndex] = top.copy(focusKey = fromRowKey)
        stack += Entry(route, lastOpener[route])
    }

    /**
     * Leaves the top page; false on [root], which the panel closes instead. A page's own button (Vazgeç, a ChoicePage
     * option) names its page as [from]: a second OK can reach that button before the panel recomposes, and once the
     * page has closed it leaves nothing more.
     */
    fun back(from: SettingsRoute = top.route): Boolean {
        if (from != top.route) return true
        if (stack.size == 1) return false
        stack.removeAt(stack.lastIndex)
        return true
    }

    /** Whether [route] is on screen or below it, waiting for Back. */
    fun isOpen(route: SettingsRoute): Boolean = stack.any { it.route == route }
}
