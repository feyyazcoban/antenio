package dev.iptvplayer.tv.data

import org.junit.Assert.*
import org.junit.Test

class AccessGateTest {
    private val home = SourceConfig("a", "Ev", SourceType.XTREAM, "http://127.0.0.1/", "kullanici", "sifre")
    private val list = SourceConfig("b", "Liste", SourceType.M3U, "http://127.0.0.1/list.m3u")
    private val news = MediaEntry("live:1", "1", MediaKind.LIVE, "Haber", "Haberler", extension = "ts").inSource("a")
    private val sport = MediaEntry("live:2", "2", MediaKind.LIVE, "Spor", "Spor", extension = "ts").inSource("a")
    private val movie = MediaEntry("movie:3", "3", MediaKind.MOVIE, "Film", "Aile", extension = "mp4").inSource("a")
    private val adult = MediaEntry("movie:4", "4", MediaKind.MOVIE, "Gece", "Aile", extension = "mp4", adult = true).inSource("a")
    private val show = MediaEntry("series:5", "5", MediaKind.SERIES, "Dizi", "Aile").inSource("a")
    private fun episode(n: Int) = MediaEntry("episode:${5 + n}", "${5 + n}", MediaKind.EPISODE, "$n. bölüm", "Dizi", extension = "mp4",
        episode = n, episodeOrderKnown = true, parentLocalId = "series:5").inSource("a")
    private val first = episode(1)
    private val second = episode(2)
    private val listShow = MediaEntry("m3u-show:8", "m3u-show:8", MediaKind.SERIES, "Liste dizisi", "Diziler").inSource("b")
    private val listEpisode = MediaEntry("m3u:9", "m3u:9", MediaKind.EPISODE, "Liste dizisi S01E01", "Diziler",
        uri = "http://127.0.0.1/series/1.mp4", parentLocalId = "m3u-show:8").inSource("b")
    private val raw = Catalog(listOf(news, sport, movie, adult, show, listShow), mapOf(listShow.id to listOf(listEpisode)))
    private val snapshot = GateSnapshot(listOf(home, list), raw)
    private val inSeries = snapshot.copy(selected = show, episodes = listOf(first, second))

    private fun decide(target: AccessTarget, at: GateSnapshot = snapshot) = AccessGate.decide(target, at)
    private fun allows(entry: MediaEntry, target: AccessTarget, at: GateSnapshot = snapshot) =
        assertEquals(target.toString(), GateResult.Allow(entry), decide(target, at))
    private fun denies(target: AccessTarget, at: GateSnapshot = snapshot) = assertEquals(target.toString(), GateResult.Deny, decide(target, at))

    @Test fun publishedTitlesOpenAndNothingAsksForAPinOrPlusYet() {
        allows(movie, AccessTarget.Open(movie.id))
        allows(news, AccessTarget.Open(news.id))
        allows(show, AccessTarget.Watch(show.id))
        allows(movie, AccessTarget.Play(movie.id, restart = true))
        val targets = listOf(AccessTarget.Open(movie.id), AccessTarget.Watch(movie.id), AccessTarget.Play(first.id),
            AccessTarget.ChannelNumber(1), AccessTarget.ChannelStep(1), AccessTarget.NextEpisode("a", first.id, second.id, true))
        val playing = inSeries.copy(playing = first, playerShown = true, channels = listOf(news))
        assertTrue(targets.all { decide(it, playing) is GateResult.Allow })
    }

    @Test fun adultTitlesAreDeniedOnEveryPath() {
        for (target in listOf(AccessTarget.Open(adult.id), AccessTarget.Watch(adult.id), AccessTarget.Play(adult.id))) denies(target)
        // An adult-looking category is hidden by default (D068), so it is not in the published library at all.
        val night = MediaEntry("movie:10", "10", MediaKind.MOVIE, "Gece filmi", "XXX Gece", extension = "mp4").inSource("a")
        denies(AccessTarget.Open(night.id), snapshot.copy(allCatalog = CategoryVisibility.apply(raw.copy(items = raw.items + night), emptySet(), true)))
        denies(AccessTarget.Play(first.id), inSeries.copy(episodes = listOf(first.copy(adult = true))))
        denies(AccessTarget.Play(first.id), inSeries.copy(allCatalog = raw.copy(items = raw.items - show + show.copy(adult = true))))
    }

    @Test fun hiddenCategoriesAreDeniedEvenThroughAnOpenDetailOrChannelList() {
        val hidden = setOf(CategoryVisibility.key(MediaKind.MOVIE, "Aile"), CategoryVisibility.key(MediaKind.LIVE, "Spor"),
            CategoryVisibility.key(MediaKind.SERIES, "Aile"))
        val published = inSeries.copy(allCatalog = CategoryVisibility.apply(raw, hidden, hideAdult = false))
        denies(AccessTarget.Open(movie.id), published)
        // The film's detail was open when its category was hidden in Ayarlar.
        denies(AccessTarget.Play(movie.id), published.copy(selected = movie))
        denies(AccessTarget.Play(first.id), published)
        denies(AccessTarget.ChannelNumber(2), published.copy(channels = listOf(news, sport)))
        allows(news, AccessTarget.ChannelNumber(1), published.copy(channels = listOf(news, sport)))
    }

    @Test fun aTitleWithoutItsSourceIsDenied() {
        val removed = inSeries.copy(sources = listOf(list), playing = news, playerShown = true, channels = listOf(news))
        for (target in listOf(AccessTarget.Open(movie.id), AccessTarget.Watch(show.id), AccessTarget.Play(first.id),
            AccessTarget.ChannelNumber(1), AccessTarget.ChannelStep(1))) denies(target, removed)
        allows(listEpisode, AccessTarget.Play(listEpisode.id), removed)
    }

    @Test fun episodesAreReachedThroughTheirSeriesOnly() {
        allows(first, AccessTarget.Play(first.id), inSeries)
        denies(AccessTarget.Play(first.id), snapshot.copy(episodes = listOf(first)))
        denies(AccessTarget.Play(first.id), inSeries.copy(selected = movie))
        denies(AccessTarget.Play(first.id), inSeries.copy(selected = listShow))
        denies(AccessTarget.Play(first.id), inSeries.copy(episodes = listOf(first.copy(parentLocalId = "series:99"))))
        // An episode is never opened as a title of its own.
        denies(AccessTarget.Open(first.id), inSeries)
        // An M3U series' episodes come with the list, so they are found without its detail.
        allows(listEpisode, AccessTarget.Play(listEpisode.id))
        denies(AccessTarget.Play(listEpisode.id), snapshot.copy(allCatalog = raw.copy(items = raw.items - listShow)))
    }

    @Test fun theNextEpisodeFollowsThePlayerAndTheCountdownSetting() {
        val playing = inSeries.copy(playing = first, playerShown = true)
        allows(second, AccessTarget.NextEpisode("a", first.id, second.id, automatic = false), playing)
        allows(second, AccessTarget.NextEpisode("a", first.id, second.id, automatic = true), playing)
        allows(second, AccessTarget.NextEpisode("a", first.id, second.id, automatic = false), playing.copy(autoNextEpisode = false))
        denies(AccessTarget.NextEpisode("a", first.id, second.id, automatic = true), playing.copy(autoNextEpisode = false))
        denies(AccessTarget.NextEpisode("b", first.id, second.id, automatic = false), playing)
        denies(AccessTarget.NextEpisode("a", first.id, first.id, automatic = false), playing)
        // The countdown of an episode that is no longer on screen.
        denies(AccessTarget.NextEpisode("a", first.id, second.id, automatic = true), playing.copy(playing = second))
        denies(AccessTarget.NextEpisode("a", first.id, second.id, automatic = true), playing.copy(playerShown = false))
        // An uncertain order still starts by hand, never by itself.
        val unknown = playing.copy(episodes = listOf(first, second.copy(episodeOrderKnown = false)))
        allows(unknown.episodes[1], AccessTarget.NextEpisode("a", first.id, second.id, automatic = false), unknown)
        denies(AccessTarget.NextEpisode("a", first.id, second.id, automatic = true), unknown)
    }

    @Test fun aScreensStaleCopyIsNeverTrusted() {
        // The gate opens the library's entry, whatever the screen's copy says.
        val stale = movie.copy(title = "Eski ad", extension = "avi")
        assertEquals(GateResult.Allow(movie), decide(AccessTarget.Open(stale.id)))
        denies(AccessTarget.Open(adult.copy(adult = false).id))
        // A refresh took the title out of the list while a screen still showed it.
        denies(AccessTarget.Open(movie.id), snapshot.copy(allCatalog = raw.copy(items = raw.items - movie)))
        denies(AccessTarget.ChannelStep(1), snapshot.copy(allCatalog = raw.copy(items = raw.items - sport), playing = news, channels = listOf(news, sport)))
        // Play starts the open detail of the same title: a film's detail can name the stream's real container.
        val detail = movie.copy(extension = "mkv", description = "Ayrıntı")
        allows(detail, AccessTarget.Play(movie.id), snapshot.copy(selected = detail))
        allows(movie, AccessTarget.Open(movie.id), snapshot.copy(selected = detail))
        allows(movie, AccessTarget.Play(movie.id), snapshot.copy(selected = show))
    }

    @Test fun channelTargetsCountInTheirOwnList() {
        val channels = snapshot.copy(channels = listOf(news, sport))
        allows(news, AccessTarget.ChannelNumber(1), channels)
        allows(sport, AccessTarget.ChannelNumber(2), channels)
        denies(AccessTarget.ChannelNumber(0), channels)
        denies(AccessTarget.ChannelNumber(3), channels)
        allows(sport, AccessTarget.ChannelStep(1), channels.copy(playing = news))
        allows(news, AccessTarget.ChannelStep(1), channels.copy(playing = sport))
        allows(sport, AccessTarget.ChannelStep(-1), channels.copy(playing = news))
        // From a title outside the list CH+ starts at its first channel, as before.
        allows(news, AccessTarget.ChannelStep(1), channels.copy(playing = movie))
        denies(AccessTarget.ChannelStep(1), snapshot.copy(playing = news))
        denies(AccessTarget.ChannelNumber(1), snapshot.copy(channels = listOf(movie)))
    }

    @Test fun everyFeatureIsFreeUntilSalesOpen() {
        assertTrue(Feature.entries.all(AllFree::has))
        assertSame(AllFree, snapshot.entitlements)
    }
}
