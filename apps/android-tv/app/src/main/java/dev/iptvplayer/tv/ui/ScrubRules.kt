package dev.iptvplayer.tv.ui

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal object ScrubRules {
    const val COMMIT_MS = 1_000L
    fun nudge(position: Long, pending: Long?, delta: Long, duration: Long): Long {
        val end = duration.coerceAtLeast(0)
        val from = (pending ?: position).coerceIn(0, end)
        return if (delta >= 0) from + delta.coerceAtMost(end - from)
            else from + delta.coerceAtLeast(-from)
    }
}

/** A single cancellable timer; seeking never opens another media source. All calls are on the UI thread. */
@Stable
internal class ScrubHolder(private val scope: CoroutineScope, private val clock: PlaybackClock,
    private val canSeek: () -> Boolean, private val seek: (Long) -> Unit) {
    var target by mutableStateOf<Long?>(null); private set
    var origin by mutableLongStateOf(0L); private set
    var announcement by mutableStateOf<Long?>(null); private set
    private var timer: Job? = null

    fun nudge(delta: Long) {
        if (!canSeek() || !clock.seekable) return
        if (target == null) origin = clock.position
        target = ScrubRules.nudge(clock.position, target, delta, clock.duration)
        timer?.cancel()
        timer = scope.launch { delay(ScrubRules.COMMIT_MS); commit() }
    }
    fun commit(): Boolean {
        val next = target
        cancel()
        if (next == null || !canSeek() || !clock.seekable) return false
        val value = next.coerceIn(0, clock.duration)
        seek(value); clock.moved(value); announcement = value
        return true
    }
    fun moveTo(value: Long): Boolean {
        cancel()
        if (!canSeek() || !clock.seekable) return false
        target = value.coerceIn(0, clock.duration)
        return commit()
    }
    fun cancel() { timer?.cancel(); timer = null; target = null }
    fun reset() { cancel(); announcement = null }
}
