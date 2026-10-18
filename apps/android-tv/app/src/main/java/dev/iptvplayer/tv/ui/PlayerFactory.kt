@file:androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)

package dev.iptvplayer.tv.ui

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import dev.iptvplayer.tv.PlaybackMode
import dev.iptvplayer.tv.PlayingMedia
import dev.iptvplayer.tv.data.DeviceStore
import dev.iptvplayer.tv.data.MediaEntry
import dev.iptvplayer.tv.data.SourceHttp

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

    /** Media3's default renderers, with audio focus, a pause when headphones go, and the remembered track languages. */
    fun create(context: Context, store: DeviceStore): ExoPlayer = ExoPlayer.Builder(context).build().apply {
        setAudioAttributes(AudioAttributes.DEFAULT, true)
        setHandleAudioBecomingNoisy(true)
        trackSelectionParameters = rememberedTracks(trackSelectionParameters, store)
    }

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
