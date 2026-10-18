package dev.iptvplayer.tv.data

import org.junit.Assert.*
import org.junit.Test

class AccessTargetsTest {
    private val home = SourceConfig("a", "Ev", SourceType.XTREAM, "http://127.0.0.1/", "kullanici", "sifre")
    private val list = SourceConfig("b", "Liste", SourceType.M3U, "http://127.0.0.1/list.m3u")
    private fun channel(n: Int) = MediaEntry("live:$n", "$n", MediaKind.LIVE, "Kanal $n", "Genel", extension = "ts").inSource("a")
    private val channels = (1..4).map(::channel)
    private val movie = MediaEntry("movie:5", "5", MediaKind.MOVIE, "Film", "Aile", categoryId = "30", extension = "mp4").inSource("a")
    private val show = MediaEntry("series:6", "6", MediaKind.SERIES, "Dizi", "Aile", categoryId = "40").inSource("a")
    private val episode = MediaEntry("episode:7", "7", MediaKind.EPISODE, "1. bölüm", extension = "mp4", episode = 1,
        episodeOrderKnown = true, parentLocalId = "series:6").inSource("a")
    private val listShow = MediaEntry("m3u-show:8", "m3u-show:8", MediaKind.SERIES, "Liste dizisi", "Diziler").inSource("b")
    private val listEpisode = MediaEntry("m3u:9", "m3u:9", MediaKind.EPISODE, "Liste dizisi S01E01", "Diziler",
        uri = "http://127.0.0.1/series/1.mp4", parentLocalId = "m3u-show:8").inSource("b")
    private val catalog = Catalog(channels + listOf(movie, show, listShow), mapOf(listShow.id to listOf(listEpisode)))
    private val snapshot = GateSnapshot(listOf(home, list), catalog)
    private fun resolve(target: AccessTarget, at: GateSnapshot = snapshot) = AccessTargets.resolve(target, at)

    @Test fun onlyTheLibraryAsPublishedResolves() {
        assertEquals(ResolvedTarget(movie), resolve(AccessTarget.Open(movie.id)))
        // A screen's copy is never what opens.
        assertEquals(ResolvedTarget(movie), resolve(AccessTarget.Watch(movie.copy(title = "Eski ad").id)))
        // Dropped by a refresh, hidden from Ayarlar, adult: nothing to open.
        assertNull(resolve(AccessTarget.Open(movie.id), snapshot.copy(allCatalog = catalog.copy(items = catalog.items - movie))))
        val hidden = CategoryVisibility.apply(catalog, setOf(CategoryVisibility.key(MediaKind.MOVIE, "Aile")), hideAdult = false)
        assertNull(resolve(AccessTarget.Open(movie.id), snapshot.copy(allCatalog = hidden)))
        assertNull(resolve(AccessTarget.Open(movie.id), snapshot.copy(allCatalog = catalog.copy(items = catalog.items - movie + movie.copy(adult = true)))))
        // A channel target names a channel only.
        assertNull(resolve(AccessTarget.ChannelNumber(1), snapshot.copy(channels = listOf(movie))))
        assertEquals(ResolvedTarget(channels[1]), resolve(AccessTarget.ChannelNumber(2), snapshot.copy(channels = channels)))
    }

    @Test fun anOpenDetailPlaysButTheLibrarysEntryIsJudged() {
        // A film's detail can name the stream's real container and lack the list's category.
        val detail = movie.copy(extension = "mkv", categoryId = "")
        assertEquals(ResolvedTarget(detail, judged = movie), resolve(AccessTarget.Play(movie.id), snapshot.copy(selected = detail)))
        assertEquals(ResolvedTarget(movie), resolve(AccessTarget.Play(movie.id)))
    }

    @Test fun anEpisodeComesWithTheSeriesItIsReachedThrough() {
        // The detail's copy of the series opened it; the library's series is the one its locks are read on.
        val detail = show.copy(categoryId = "", description = "Ayrıntı")
        val open = snapshot.copy(selected = detail, episodes = listOf(episode))
        assertEquals(ResolvedTarget(episode, AccessPolicy.SeriesContext(show, listOf(episode))), resolve(AccessTarget.Play(episode.id), open))
        // Without its series' detail an Xtream episode is not reached.
        assertNull(resolve(AccessTarget.Play(episode.id), snapshot.copy(episodes = listOf(episode))))
        assertNull(resolve(AccessTarget.Play(episode.id), open.copy(allCatalog = catalog.copy(items = catalog.items - show))))
        // An M3U episode comes with its list's series.
        assertEquals(ResolvedTarget(listEpisode, AccessPolicy.SeriesContext(listShow, listOf(listEpisode))),
            resolve(AccessTarget.Play(listEpisode.id)))
        assertNull(resolve(AccessTarget.Play(listEpisode.id), snapshot.copy(allCatalog = catalog.copy(items = catalog.items - listShow))))
    }

    @Test fun nextZapStepsOverWhatMayNotPlayAndWrapsAround() {
        val (one, two, three, four) = channels
        val locked = setOf(two.id, three.id)
        fun zap(from: String?, direction: Int, allowed: (MediaEntry) -> Boolean = { it.id !in locked }) =
            AccessTargets.nextZap(channels, from, direction, allowed)
        assertEquals(four, zap(one.id, 1))
        assertEquals(one, zap(four.id, 1))
        assertEquals(four, zap(one.id, -1))
        assertEquals(one, zap(four.id, -1))
        // From a title outside the list CH+ starts at its first channel that may play.
        assertEquals(one, zap(null, 1))
        assertEquals(four, zap("movie", 1) { it.id !in locked && it.id != one.id })
        // Nothing else may play: the player stays where it is.
        assertNull(zap(one.id, 1) { it.id == one.id })
        assertNull(zap(one.id, 1) { false })
        assertNull(AccessTargets.nextZap(emptyList(), one.id, 1) { true })
        assertNull(zap(one.id, 0))
        // A single channel does not open again.
        assertNull(AccessTargets.nextZap(listOf(one), one.id, 1) { true })
    }

    @Test fun channelStepsCountInTheirListAndSkipWhatWouldAsk() {
        val (one, two, three) = channels
        val zapping = snapshot.copy(channels = channels, playing = one)
        assertEquals(ResolvedTarget(three), AccessTargets.resolve(AccessTarget.ChannelStep(1), zapping) { it.entry.id != two.id })
        assertEquals(ResolvedTarget(two), AccessTargets.resolve(AccessTarget.ChannelStep(1), zapping))
        // A channel the library no longer has is passed, as is one CH± may not play.
        val stale = zapping.copy(allCatalog = catalog.copy(items = catalog.items - two))
        assertEquals(ResolvedTarget(three), AccessTargets.resolve(AccessTarget.ChannelStep(1), stale))
        assertNull(AccessTargets.resolve(AccessTarget.ChannelStep(1), zapping) { false })
        assertNull(AccessTargets.resolve(AccessTarget.ChannelStep(1), snapshot.copy(playing = one)))
    }
}
