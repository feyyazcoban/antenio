package dev.iptvplayer.tv

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.SystemClock
import dev.iptvplayer.tv.data.DeviceStore
import java.util.concurrent.atomic.AtomicBoolean

/**
 * The splash chime (D062), once per process. No volume override and no late/background playback. MediaPlayer rather
 * than SoundPool because the asset's quiet lead is skipped so its impact lands with the LaunchCover mark pop.
 */
internal class StartupSound(private val context: Context) {
    private var player: MediaPlayer? = null
    private var foreground = false
    private var prepared = false
    private var played = false
    private var startedAt = 0L

    fun prepare() {
        if (!claimed.compareAndSet(false, true) || !DeviceStore(context).startupSound) return
        if ((context.getSystemService(Context.AUDIO_SERVICE) as AudioManager).isMusicActive) return
        startedAt = SystemClock.elapsedRealtime()
        // Best effort: a TV without audio output or a decoder failure only skips the chime.
        runCatching {
            player = MediaPlayer().apply {
                setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
                context.resources.openRawResourceFd(R.raw.splash_chime).use { setDataSource(it.fileDescriptor, it.startOffset, it.length) }
                setOnPreparedListener { it.seekTo(LEAD_SKIP_MS); prepared = true; maybePlay() }
                setOnCompletionListener { stop() }
                setOnErrorListener { _, _, _ -> stop(); true }
                prepareAsync()
            }
        }.onFailure { stop() }
    }

    fun start() { foreground = true; maybePlay() }
    private fun maybePlay() {
        if (!foreground || !prepared || played || SystemClock.elapsedRealtime() - startedAt > 1800) return
        played = true
        runCatching { player?.start() }.onFailure { stop() }
    }
    fun stop() {
        foreground = false; played = true
        player?.let { runCatching { it.release() } }
        player = null
    }

    companion object {
        private val claimed = AtomicBoolean(false)
        // The asset opens with ~0.4 s of near-silence; skipping it lands the impact ~1.34 s in, LaunchCover's Impact.
        private const val LEAD_SKIP_MS = 400
    }
}
