package dev.iptvplayer.tv.ui

import dev.iptvplayer.tv.BuildConfig
import dev.iptvplayer.tv.PlaybackMode
import dev.iptvplayer.tv.PlayingMedia
import dev.iptvplayer.tv.data.MediaEntry
import dev.iptvplayer.tv.data.MediaKind
import dev.iptvplayer.tv.data.inSource
import org.junit.Assert.*
import org.junit.Test

class PlayerFactoryTest {
    @Test fun aStreamIsAskedForWithItsOwnHeadersAndTheVersionedUserAgent() {
        val channel = MediaEntry("m3u:1", "m3u:1", MediaKind.LIVE, "Haber", "Haberler", uri = "http://127.0.0.1/live/kullanici/sifre/1.ts",
            headers = mapOf("Referer" to "http://127.0.0.1/")).inSource("b")
        val media = PlayingMedia(channel, channel.uri!!, 0, PlaybackMode.of(channel.kind))
        val request = PlayerFactory.request(media)
        assertEquals(PlaybackRequest(channel, media.url, mapOf("Referer" to "http://127.0.0.1/"),
            "NEXTV/${BuildConfig.VERSION_NAME} AndroidTV", 0, PlaybackMode.LIVE), request)
        val film = MediaEntry("movie:3", "3", MediaKind.MOVIE, "Film", "Aile", extension = "mp4").inSource("a")
        assertEquals(PlaybackMode.VOD, PlayerFactory.request(PlayingMedia(film, "http://127.0.0.1/movie/3.mp4", 42_000, PlaybackMode.of(film.kind))).mode)
        assertEquals(42_000L, PlayerFactory.request(PlayingMedia(film, "http://127.0.0.1/movie/3.mp4", 42_000, PlaybackMode.VOD)).startMs)
        // The address can hold the account: neither the request nor the playing media prints it.
        for (text in listOf(request.toString(), media.toString())) assertFalse(text, text.contains("sifre") || text.contains("127.0.0.1"))
    }

    @Test fun onlyChannelsPlayLive() {
        assertEquals(PlaybackMode.LIVE, PlaybackMode.of(MediaKind.LIVE))
        for (kind in listOf(MediaKind.MOVIE, MediaKind.SERIES, MediaKind.EPISODE)) assertEquals(PlaybackMode.VOD, PlaybackMode.of(kind))
    }
}
