package dev.iptvplayer.tv

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.SystemClock
import dev.iptvplayer.tv.data.DeviceStore
import java.util.concurrent.atomic.AtomicBoolean

/** A short original cue, once per process. No volume override and no late/background playback. */
internal class StartupSound(private val context: Context) {
    private var pool: SoundPool? = null
    private var foreground = false
    private var loaded = false
    private var sample = 0
    private var played = false
    private var startedAt = 0L

    fun prepare() {
        if (!claimed.compareAndSet(false, true) || !DeviceStore(context).startupSound) return
        if ((context.getSystemService(Context.AUDIO_SERVICE) as AudioManager).isMusicActive) return
        startedAt = SystemClock.elapsedRealtime()
        pool = SoundPool.Builder().setMaxStreams(1).setAudioAttributes(
            AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
        ).build().also { sound ->
            sound.setOnLoadCompleteListener { _, _, status ->
                loaded = status == 0
                maybePlay()
            }
            sample = sound.load(context, R.raw.startup, 1)
        }
    }

    fun start() { foreground = true; maybePlay() }
    private fun maybePlay() {
        if (!foreground || !loaded || played || SystemClock.elapsedRealtime() - startedAt > 1800) return
        played = true
        pool?.play(sample, 1f, 1f, 1, 0, 1f)
    }
    fun stop() { foreground = false; pool?.release(); pool = null; played = true }

    companion object { private val claimed = AtomicBoolean(false) }
}
