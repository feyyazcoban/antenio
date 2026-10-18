@file:androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)

package dev.iptvplayer.tv.ui

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.ForwardingAudioSink
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.CaptionStyleCompat
import androidx.media3.ui.SubtitleView
import dev.iptvplayer.tv.PlaybackMode
import dev.iptvplayer.tv.PlayingMedia
import dev.iptvplayer.tv.data.AudioOutput
import dev.iptvplayer.tv.data.BufferPreset
import dev.iptvplayer.tv.data.DecoderMode
import dev.iptvplayer.tv.data.DeviceStore
import dev.iptvplayer.tv.data.FrameRateMatching
import dev.iptvplayer.tv.data.MediaEntry
import dev.iptvplayer.tv.data.PlaybackPreferences
import dev.iptvplayer.tv.data.SourceHttp
import dev.iptvplayer.tv.data.SubtitleBackground
import dev.iptvplayer.tv.data.SubtitleColor
import dev.iptvplayer.tv.data.SubtitleSource
import dev.iptvplayer.tv.data.SubtitleStyle

/**
 * One stream to open (gap plan T4): its address, the headers and User-Agent it is asked with, where it starts and how
 * the player treats it. The address can hold the account, so it never reaches a log.
 */
internal data class PlaybackRequest(val entry: MediaEntry, val url: String, val headers: Map<String, String>,
    val userAgent: String, val startMs: Long, val mode: PlaybackMode) {
    override fun toString() = "PlaybackRequest(id=${entry.id}, mode=$mode, url=REDACTED)"
}

/** Builds the player and what it opens; PlayerScreen keeps the order of the calls (one stream at a time). */
internal object PlayerFactory {
    /** [media] as the player opens it, with the headers and User-Agent [SourceHttp] gives a stream. */
    fun request(media: PlayingMedia) = PlaybackRequest(media.entry, media.url, SourceHttp.streamHeaders(media.entry),
        SourceHttp.streamAgent, media.startMs, media.mode)

    /**
     * The player for Settings → Oynatıcı's [preferences]: their buffer, decoders, audio output, tunnelling and frame rate,
     * with audio focus, a pause when headphones go, and the remembered track languages. A decoder that cannot open a
     * stream gives way to another on the device.
     */
    fun create(context: Context, store: DeviceStore, preferences: PlaybackPreferences): ExoPlayer {
        val renderers = object : DefaultRenderersFactory(context) {
            override fun buildAudioSink(context: Context, enableFloatOutput: Boolean, enableAudioTrackPlaybackParams: Boolean): AudioSink? =
                super.buildAudioSink(context, enableFloatOutput, enableAudioTrackPlaybackParams)
                    ?.let { if (preferences.audioOutput == AudioOutput.PCM) PcmOnlyAudioSink(it) else it }
        }.setEnableDecoderFallback(true)
        if (preferences.decoder == DecoderMode.SOFTWARE_FIRST) renderers.setMediaCodecSelector(MediaCodecSelector.PREFER_SOFTWARE)
        // Tunnelling stays in the selector's own parameters: the track languages set below and in TracksDialog are
        // merged into them (DefaultTrackSelector.setParameters), never put in their place.
        val selector = DefaultTrackSelector(context).apply { setParameters(buildUponParameters().setTunnelingEnabled(preferences.tunneling)) }
        return ExoPlayer.Builder(context, renderers).setLoadControl(loadControl(preferences.buffer)).setTrackSelector(selector).build().apply {
            setAudioAttributes(AudioAttributes.DEFAULT, true)
            setHandleAudioBecomingNoisy(true)
            setVideoChangeFrameRateStrategy(frameRateStrategy(preferences.frameRate))
            trackSelectionParameters = rememberedTracks(trackSelectionParameters, store)
        }
    }

    /** How far ahead a stream loads; Media3 checks that the durations agree. */
    fun loadControl(buffer: BufferPreset): DefaultLoadControl = DefaultLoadControl.Builder()
        .setBufferDurationsMsForStreaming(buffer.minMs, buffer.maxMs, buffer.playMs, buffer.rebufferMs).build()

    /** Media3 has no strategy that forces a mode change; Kapalı and Yalnız kesintisiz are its two. */
    fun frameRateStrategy(matching: FrameRateMatching): Int = when (matching) {
        FrameRateMatching.OFF -> C.VIDEO_CHANGE_FRAME_RATE_STRATEGY_OFF
        FrameRateMatching.SEAMLESS -> C.VIDEO_CHANGE_FRAME_RATE_STRATEGY_ONLY_IF_SEAMLESS
    }

    /**
     * [style] on the player's subtitles: Android's caption settings and the stream's own styling, as PlayerView starts
     * with, or NEXTV's size, background and colour in place of both.
     */
    fun subtitles(view: SubtitleView, style: SubtitleStyle) {
        if (style.source == SubtitleSource.SYSTEM) {
            view.setUserDefaultStyle(); view.setUserDefaultTextSize(); view.setApplyEmbeddedStyles(true)
        } else {
            view.setStyle(captionStyle(style))
            view.setFractionalTextSize(SubtitleView.DEFAULT_TEXT_SIZE_FRACTION * style.size.factor)
            view.setApplyEmbeddedStyles(false)
        }
    }

    /** White or yellow letters on no background (with a drop shadow), 60 % black or black. */
    fun captionStyle(style: SubtitleStyle) = CaptionStyleCompat(
        if (style.color == SubtitleColor.YELLOW) 0xFFFFEB3B.toInt() else 0xFFFFFFFF.toInt(),
        when (style.background) { SubtitleBackground.NONE -> 0; SubtitleBackground.SEMI -> 0x99000000.toInt(); SubtitleBackground.SOLID -> 0xFF000000.toInt() },
        0, if (style.background == SubtitleBackground.NONE) CaptionStyleCompat.EDGE_TYPE_DROP_SHADOW else CaptionStyleCompat.EDGE_TYPE_NONE,
        0xFF000000.toInt(), null)

    /** The audio and subtitle languages the viewer last chose (TracksDialog saves them), without the last stream's picks. */
    fun rememberedTracks(parameters: TrackSelectionParameters, store: DeviceStore): TrackSelectionParameters = parameters.buildUpon().clearOverrides()
        .setPreferredAudioLanguage(store.audioLanguage)
        .setPreferredTextLanguage(store.subtitleLanguage)
        .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, store.subtitlesOff).build()

    /** [request]'s stream with 8 s connect and read timeouts; a single attempt per load, as PlaybackRecovery owns the retries. */
    fun mediaSource(context: Context, request: PlaybackRequest): MediaSource {
        val http = DefaultHttpDataSource.Factory().setUserAgent(request.userAgent)
            .setConnectTimeoutMs(8_000).setReadTimeoutMs(8_000)
            .setDefaultRequestProperties(request.headers)
        return DefaultMediaSourceFactory(DefaultDataSource.Factory(context, http))
            .setLoadErrorHandlingPolicy(SingleAttemptLoadPolicy())
            .createMediaSource(MediaItem.fromUri(request.url))
    }
}

/**
 * "Cihazda çöz": the sink takes decoded audio only, so the audio renderer decodes Dolby and DTS with the device's own
 * decoders instead of passing them through; a format it cannot decode then reads as unsupported in Ses / Altyazı.
 */
internal class PcmOnlyAudioSink(sink: AudioSink) : ForwardingAudioSink(sink) {
    override fun supportsFormat(format: Format) = format.sampleMimeType == MimeTypes.AUDIO_RAW && super.supportsFormat(format)
    override fun getFormatSupport(format: Format) =
        if (format.sampleMimeType == MimeTypes.AUDIO_RAW) super.getFormatSupport(format) else AudioSink.SINK_FORMAT_UNSUPPORTED
}
