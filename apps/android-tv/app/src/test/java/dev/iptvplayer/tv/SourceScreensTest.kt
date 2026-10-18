package dev.iptvplayer.tv

import dev.iptvplayer.tv.data.*
import org.junit.Assert.*
import org.junit.Test

/**
 * S12's screen decisions (pin.md §3.8–3.9): what the lock screen covers, where leaving the sources' screens and their
 * operations ends, and the player that comes back after the lock screen. Synthetic sources only.
 */
class SourceScreensTest {
    private val source = SourceConfig("a", "Ev", SourceType.XTREAM, "http://127.0.0.1/", "kullanici", "sifre")
    private val draft = SourceDraft("Ev", SourceType.XTREAM, "http://127.0.0.1/", "kullanici", "sifre")
    private val locked = ParentalUi(ParentalStatus.ON, appLocked = true)

    @Test fun theLockScreenCoversEveryRouteButTheOperationCardAndTheRecoveryScreen() {
        for (route in Route.entries - Route.LOCKED) {
            assertEquals(route, TvState(route = route).screen)
            val covered = if (route == Route.LOADING || route == Route.RECOVERY) route else Route.LOCKED
            assertEquals(route.name, covered, TvState(route = route, parental = locked).screen)
        }
    }

    @Test fun leavingTheSourcesScreensForgetsTheFormAndKeepsAnOperationUnderWay() {
        // Kaynaklar opened from the guide; the form on a source opened from Kaynaklar, its password in the draft.
        val sources = TvState(route = Route.SOURCES, sources = listOf(source), sourcesReturn = Route.GUIDE)
        assertEquals(Route.GUIDE, SourceScreens.left(sources).route)
        val form = sources.copy(route = Route.SETUP, setupReturn = Route.SOURCES, draft = draft, editingSourceId = "a",
            error = UiText.Res(R.string.error_source_not_saved))
        val left = SourceScreens.left(form)
        assertEquals(Route.GUIDE, left.route); assertEquals(SourceDraft(), left.draft)
        assertNull(left.editingSourceId); assertNull(left.error)
        // Kaynağı düzenle in Ayarlar opened the form over Ana Sayfa.
        assertEquals(Route.HOME, SourceScreens.left(form.copy(setupReturn = Route.HOME)).route)
        // An import or a removal goes on (it ends by afterSave or afterRemoval); elsewhere nothing changes.
        val importing = form.copy(route = Route.LOADING, sourceTask = SourceTask.IMPORTING)
        assertSame(importing, SourceScreens.left(importing))
        val home = form.copy(route = Route.HOME)
        assertSame(home, SourceScreens.left(home))
        // The first setup of a TV without sources has nothing to protect.
        val first = TvState(route = Route.SETUP, draft = draft)
        assertSame(first, SourceScreens.left(first))
    }

    @Test fun anOperationEndsInKaynaklarOnlyWhileThePinThatOpenedThemHolds() {
        val fromSources = TvState(route = Route.LOADING, sources = listOf(source), setupReturn = Route.SOURCES, sourcesReturn = Route.LIBRARY)
        assertEquals(Route.SOURCES, SourceScreens.afterSave(fromSources, mayManage = true))
        assertEquals(Route.LIBRARY, SourceScreens.afterSave(fromSources, mayManage = false))
        assertEquals(Route.HOME, SourceScreens.afterSave(fromSources.copy(setupReturn = Route.HOME), mayManage = true))
        // A removal is judged on where it began.
        val removing = fromSources.copy(route = Route.SOURCES)
        assertEquals(Route.SOURCES, SourceScreens.afterRemoval(removing, mayManage = true))
        assertEquals(Route.LIBRARY, SourceScreens.afterRemoval(removing, mayManage = false))
        assertEquals(Route.HOME, SourceScreens.afterRemoval(removing.copy(route = Route.HOME), mayManage = true))
    }

    @Test fun thePlayerAfterTheLockScreenStartsWhereItWasSavedAndAsTheViewerLeftIt() {
        val film = MediaEntry("movie:3", "3", MediaKind.MOVIE, "Film", "Aile", extension = "mp4").inSource("a")
        val media = PlayingMedia(film, "http://127.0.0.1/movie/3.mp4", 600_000, PlaybackMode.VOD)
        assertEquals(PlayingMedia(film, media.url, 1_800_000, PlaybackMode.VOD), media.resumed(PlaybackMark(1_800_000, 7_200_000, 1), paused = false))
        // In its last minutes too (the credits): where it stopped, not from the start.
        assertEquals(7_000_000L, media.resumed(PlaybackMark(7_000_000, 7_200_000, 1), paused = false).startMs)
        // Paused, it comes back paused; never saved, it starts where it was started.
        assertEquals(PlayingMedia(film, media.url, 600_000, PlaybackMode.VOD, paused = true), media.resumed(null, paused = true))
        // A live channel joins its live edge.
        val channel = PlayingMedia(MediaEntry("live:1", "1", MediaKind.LIVE, "Haber", "Haberler", extension = "ts").inSource("a"),
            "http://127.0.0.1/live/1.ts", 0, PlaybackMode.LIVE)
        assertEquals(0L, channel.resumed(PlaybackMark(90_000, 0, 1), paused = false).startMs)
    }
}
