@file:androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)

package dev.iptvplayer.tv.ui

import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MimeTypes
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.ui.CaptionStyleCompat
import dev.iptvplayer.tv.BuildConfig
import dev.iptvplayer.tv.PlaybackMode
import dev.iptvplayer.tv.PlayingMedia
import dev.iptvplayer.tv.data.BufferPreset
import dev.iptvplayer.tv.data.FrameRateMatching
import dev.iptvplayer.tv.data.MediaEntry
import dev.iptvplayer.tv.data.MediaKind
import dev.iptvplayer.tv.data.SubtitleBackground
import dev.iptvplayer.tv.data.SubtitleColor
import dev.iptvplayer.tv.data.SubtitleSource
import dev.iptvplayer.tv.data.SubtitleStyle
import dev.iptvplayer.tv.data.inSource
import org.junit.Assert.*
import org.junit.Test
import java.lang.reflect.Proxy

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

    @Test fun everyBufferBuildsALoadControlAndTheFrameRateKeepsMediaThreesStrategies() {
        for (preset in BufferPreset.entries) PlayerFactory.loadControl(preset)
        assertEquals(C.VIDEO_CHANGE_FRAME_RATE_STRATEGY_OFF, PlayerFactory.frameRateStrategy(FrameRateMatching.OFF))
        assertEquals(C.VIDEO_CHANGE_FRAME_RATE_STRATEGY_ONLY_IF_SEAMLESS, PlayerFactory.frameRateStrategy(FrameRateMatching.SEAMLESS))
    }

    @Test fun decodingOnTheDeviceLetsOnlyDecodedAudioThrough() {
        var asked = 0
        val sink = Proxy.newProxyInstance(AudioSink::class.java.classLoader, arrayOf(AudioSink::class.java)) { _, method, _ ->
            when (method.name) {
                "supportsFormat" -> { asked++; true }
                "getFormatSupport" -> { asked++; AudioSink.SINK_FORMAT_SUPPORTED_DIRECTLY }
                else -> null
            }
        } as AudioSink
        val pcmOnly = PcmOnlyAudioSink(sink)
        val ac3 = Format.Builder().setSampleMimeType(MimeTypes.AUDIO_AC3).setChannelCount(6).setSampleRate(48_000).build()
        val pcm = Format.Builder().setSampleMimeType(MimeTypes.AUDIO_RAW).setPcmEncoding(C.ENCODING_PCM_16BIT)
            .setChannelCount(2).setSampleRate(48_000).build()
        // Dolby is not passed through, so the renderer decodes it with the device's decoder.
        assertFalse(pcmOnly.supportsFormat(ac3))
        assertEquals(AudioSink.SINK_FORMAT_UNSUPPORTED, pcmOnly.getFormatSupport(ac3))
        assertEquals(0, asked)
        // Decoded audio goes on to the sink as before.
        assertTrue(pcmOnly.supportsFormat(pcm))
        assertEquals(AudioSink.SINK_FORMAT_SUPPORTED_DIRECTLY, pcmOnly.getFormatSupport(pcm))
        assertEquals(2, asked)
    }

    @Test fun vodDecodesAudioForSpeedWithoutChangingLiveOrTunnelingOutput() {
        val automatic = dev.iptvplayer.tv.data.PlaybackPreferences()
        assertTrue(PlayerFactory.decodeAudio(automatic, PlaybackMode.VOD))
        assertFalse(PlayerFactory.decodeAudio(automatic, PlaybackMode.LIVE))
        assertFalse(PlayerFactory.decodeAudio(automatic.copy(tunneling = true), PlaybackMode.VOD))
        assertTrue(PlayerFactory.decodeAudio(automatic.copy(audioOutput = dev.iptvplayer.tv.data.AudioOutput.PCM), PlaybackMode.LIVE))
    }

    @Test fun nextvSubtitlesTakeTheChosenColourAndBackground() {
        val semi = PlayerFactory.captionStyle(SubtitleStyle(SubtitleSource.APP, background = SubtitleBackground.SEMI))
        assertEquals(0xFFFFFFFF.toInt(), semi.foregroundColor)
        assertEquals(0x99000000.toInt(), semi.backgroundColor)
        assertEquals(CaptionStyleCompat.EDGE_TYPE_NONE, semi.edgeType)
        // Without a background the letters cast a shadow, so they stay readable on a bright picture.
        val bare = PlayerFactory.captionStyle(SubtitleStyle(SubtitleSource.APP, background = SubtitleBackground.NONE, color = SubtitleColor.YELLOW))
        assertEquals(0, bare.backgroundColor)
        assertEquals(CaptionStyleCompat.EDGE_TYPE_DROP_SHADOW, bare.edgeType)
        assertEquals(0xFFFFEB3B.toInt(), bare.foregroundColor)
        assertEquals(0xFF000000.toInt(), PlayerFactory.captionStyle(SubtitleStyle(background = SubtitleBackground.SOLID)).backgroundColor)
        // No window colour behind a whole cue block, only behind its lines.
        assertEquals(0, semi.windowColor)
    }
}
