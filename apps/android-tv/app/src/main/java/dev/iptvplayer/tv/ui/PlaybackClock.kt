package dev.iptvplayer.tv.ui

import androidx.compose.runtime.*

/** Timing is read by the clock, metadata and bar, never by PlayerScreen's composition (D056/D072). */
@Stable
internal class PlaybackClock {
    var position by mutableLongStateOf(0L); private set
    var duration by mutableLongStateOf(0L); private set
    var buffered by mutableLongStateOf(0L); private set
    var now by mutableLongStateOf(System.currentTimeMillis()); private set
    var seekable by mutableStateOf(false); private set

    fun update(position: Long, duration: Long, buffered: Long, seekable: Boolean, now: Long) {
        this.duration = duration.coerceAtLeast(0)
        this.position = position.coerceAtLeast(0)
        this.buffered = buffered.coerceAtLeast(0)
        this.seekable = seekable && duration > 0
        this.now = now
    }
    fun moved(position: Long) { this.position = position.coerceAtLeast(0) }
    fun reset(position: Long = 0) = update(position, 0, 0, false, System.currentTimeMillis())
}
