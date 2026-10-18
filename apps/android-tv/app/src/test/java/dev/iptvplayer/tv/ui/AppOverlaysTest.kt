package dev.iptvplayer.tv.ui

import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.WindowInfo
import dev.iptvplayer.tv.R
import dev.iptvplayer.tv.data.MediaEntry
import dev.iptvplayer.tv.data.MediaKind
import dev.iptvplayer.tv.data.inSource
import org.junit.Assert.*
import org.junit.Test

class AppOverlaysTest {
    private val window = object : WindowInfo { override val isWindowFocused = true }
    private val movie = MediaEntry("movie:1", "1", MediaKind.MOVIE, "Film", "Aile", extension = "mp4").inSource("a")
    private val show = MediaEntry("series:2", "2", MediaKind.SERIES, "Dizi", "Aile").inSource("a")
    private val home = Any()
    private val library = Any()
    private val card = FocusRequester()
    private fun opened(entry: MediaEntry, owner: Any = home) = AppOverlays.Opened(AppOverlay.Quick(entry), owner, window) { card }

    @Test fun oneOverlayAtATimeAndAClosedOneWaitsForItsFocusToReturn() {
        val overlays = AppOverlays()
        val first = opened(movie)
        val second = opened(show, library)
        overlays.show(first)
        overlays.show(second)
        assertSame(second, overlays.shown)
        assertNull(overlays.returning)
        overlays.close()
        assertNull(overlays.shown)
        assertSame(second, overlays.returning)
        assertSame(card, overlays.returning?.returnFocus?.invoke())
        // Only the overlay whose focus came back clears the return.
        overlays.returned(first)
        assertSame(second, overlays.returning)
        overlays.returned(second)
        assertNull(overlays.returning)
        // Nothing on screen: a second close has nothing to return to.
        overlays.close()
        assertNull(overlays.returning)
    }

    @Test fun aNewOverlayDropsTheFocusReturnOfTheOneBefore() {
        val overlays = AppOverlays()
        overlays.show(opened(movie))
        overlays.close()
        val next = opened(show)
        overlays.show(next)
        assertSame(next, overlays.shown)
        assertNull(overlays.returning)
    }

    @Test fun withAPinTheQuickActionsOfferTheTitlesOwnLock() {
        var closed = 0
        val toggled = mutableListOf<Boolean>()
        fun actions(locked: Boolean?) = quickActions(movie, favorite = false, toggleFavorite = {}, open = {}, close = { closed++ },
            locked = locked) { toggled += it }
        // Without a PIN there is no lock to offer.
        assertEquals(listOf("favorite", "open"), actions(null).map { it.key })
        val lock = actions(false).last()
        assertEquals("lock", lock.key); assertEquals(R.string.lock_add, lock.label); assertEquals("lock", lock.icon)
        lock.run()
        assertEquals(listOf(true), toggled); assertEquals(1, closed)
        // Its own lock can be removed; the PIN pad then takes the closed menu's place.
        val unlock = actions(true).last()
        assertEquals(R.string.lock_remove, unlock.label)
        unlock.run()
        assertEquals(listOf(true, false), toggled); assertEquals(2, closed)
    }

    @Test fun anOpenerThatLeavesTheScreenTakesItsOverlayAndFocusReturnAlong() {
        val overlays = AppOverlays()
        overlays.show(opened(movie, home))
        overlays.leave(library)
        assertNotNull(overlays.shown)
        overlays.leave(home)
        assertNull(overlays.shown)
        assertNull(overlays.returning)
        overlays.show(opened(movie, home))
        overlays.close()
        overlays.leave(library)
        assertNotNull(overlays.returning)
        overlays.leave(home)
        assertNull(overlays.returning)
    }
}
