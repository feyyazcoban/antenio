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
 * returns to the section whose list was open. Pure, so these rules are tested on the JVM.
 */
internal class SettingsNav(val root: SettingsRoute) {
    /** [route] on screen and the row it focuses: the opener of the page above once that closes, or its own start. */
    data class Entry(val route: SettingsRoute, val focusKey: String? = null)

    private val stack = mutableListOf(Entry(root))
    private val lastOpener = mutableMapOf<SettingsRoute, String>()

    val top: Entry get() = stack.last()

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

    /** Leaves the top page; false on [root], which the panel closes instead. */
    fun back(): Boolean {
        if (stack.size == 1) return false
        stack.removeAt(stack.lastIndex)
        return true
    }
}
