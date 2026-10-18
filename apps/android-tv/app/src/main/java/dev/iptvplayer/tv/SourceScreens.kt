package dev.iptvplayer.tv

/**
 * Kaynaklar, the setup form and the operation card over them: the sources' screens a PIN opens (pin.md §3.8), and where
 * each way out of them goes. Pure, so the routes are tested on the JVM. TvViewModel applies them, and once the route is
 * outside [routes], whatever moved it, what the PIN opened there ends.
 */
internal object SourceScreens {
    val routes = setOf(Route.SOURCES, Route.SETUP, Route.LOADING)

    /**
     * [s] with the sources' screens left as Back would, when the PIN that opened them ended (the app left the screen):
     * Kaynaklar for its opener, and the setup form with its draft forgotten, as it holds the source's password. The first
     * setup of a TV without sources stays, as it has nothing to protect. An import or a removal under way goes on and
     * ends where [afterSave] and [afterRemoval] say.
     */
    fun left(s: TvState): TvState = when {
        s.sources.isEmpty() -> s
        s.route == Route.SOURCES -> s.copy(route = s.sourcesReturn)
        s.route == Route.SETUP -> s.copy(route = if (s.setupReturn == Route.SOURCES) s.sourcesReturn else s.setupReturn,
            draft = SourceDraft(), editingSourceId = null, error = null)
        else -> s
    }

    /**
     * Where a source saved from the form of [s] goes: back to Kaynaklar it came from while [mayManage] (the PIN that opened
     * them still holds), else to what opened Kaynaklar; a form opened from Ayarlar ends on Ana Sayfa.
     */
    fun afterSave(s: TvState, mayManage: Boolean): Route = when {
        s.setupReturn != Route.SOURCES -> Route.HOME
        mayManage -> Route.SOURCES
        else -> s.sourcesReturn
    }

    /** Where a removal begun on [previous] ends while sources are left, by the same rule as [afterSave]. */
    fun afterRemoval(previous: TvState, mayManage: Boolean): Route = when {
        previous.route != Route.SOURCES -> Route.HOME
        mayManage -> Route.SOURCES
        else -> previous.sourcesReturn
    }
}
