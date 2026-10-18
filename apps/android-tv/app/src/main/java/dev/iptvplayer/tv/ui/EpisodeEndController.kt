package dev.iptvplayer.tv.ui

import dev.iptvplayer.tv.R
import dev.iptvplayer.tv.data.MediaEntry
import dev.iptvplayer.tv.data.MediaKind
import dev.iptvplayer.tv.data.NextEpisodeChoice
import dev.iptvplayer.tv.data.UiText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal data class EpisodeEndState(
    val sourceId: String,
    val current: MediaEntry,
    val next: NextEpisodeChoice?,
    val seconds: Int? = null,
    val notice: UiText? = null
)

/** What asking for the next episode did: it plays, it waits for the PIN of its locked series, or it cannot open. */
enum class NextResult { STARTED, NEEDS_PIN, UNAVAILABLE }

/**
 * The end of media, not an estimated credits offset, starts the cancellable countdown. A next episode whose locked
 * series this session has not opened (pin.md §3.7) stays offered without a countdown; the viewer starts it, and the PIN
 * pad asks.
 */
internal class EpisodeEndController(
    private val scope: CoroutineScope,
    private val playNext: (sourceId: String, currentId: String, nextId: String, automatic: Boolean) -> NextResult
) {
    private val mutableState = MutableStateFlow<EpisodeEndState?>(null)
    val state = mutableState.asStateFlow()
    private var timer: Job? = null
    private var handled = false
    private var foreground = true
    private var closed = false

    fun newMedia() { timer?.cancel(); mutableState.value = null; handled = false }

    fun ended(sourceId: String, current: MediaEntry, next: NextEpisodeChoice?, autoEnabled: Boolean) {
        if (closed || handled || current.kind != MediaKind.EPISODE) return
        handled = true
        val automatic = next?.automaticAllowed == true && autoEnabled && foreground
        mutableState.value = EpisodeEndState(sourceId, current, next, if (automatic) COUNTDOWN_SECONDS else null,
            when { !foreground -> UiText.Res(R.string.episode_auto_paused); !autoEnabled && next != null -> UiText.Res(R.string.episode_auto_off); else -> null })
        if (automatic) timer = scope.launch {
            for (seconds in COUNTDOWN_SECONDS - 1 downTo 0) {
                delay(1000)
                val currentState = mutableState.value ?: return@launch
                if (seconds == 0) play(automatic = true)
                else mutableState.value = currentState.copy(seconds = seconds)
            }
        }
    }

    /** Şimdi oynat; null when there was nothing to start. NEEDS_PIN: the PIN pad opened, which the card shows. */
    fun playNow(): NextResult? = play(automatic = false)

    private fun play(automatic: Boolean): NextResult? {
        timer?.cancel()
        val s = mutableState.value ?: return null
        val next = s.next ?: return null
        if (!foreground || closed) return null
        // The model validates the current source, episode and latest target again before opening media.
        val result = playNext(s.sourceId, s.current.id, next.entry.id, automatic)
        mutableState.value = when (result) {
            NextResult.STARTED -> null
            NextResult.NEEDS_PIN -> s.copy(seconds = null, notice = UiText.Res(R.string.episode_next_needs_pin))
            NextResult.UNAVAILABLE -> s.copy(next = null, seconds = null, notice = UiText.Res(R.string.episode_next_unavailable))
        }
        return result
    }

    fun dismiss() { timer?.cancel(); mutableState.value = null }

    fun preferenceChanged(enabled: Boolean) {
        if (!enabled && mutableState.value?.seconds != null) {
            timer?.cancel()
            mutableState.value = mutableState.value?.copy(seconds = null, notice = UiText.Res(R.string.episode_auto_off))
        }
    }

    fun availabilityChanged(next: NextEpisodeChoice?) {
        val s = mutableState.value ?: return
        val previous = s.next ?: return
        if (next?.entry?.id != previous.entry.id || (s.seconds != null && !next.automaticAllowed)) {
            timer?.cancel()
            mutableState.value = s.copy(next = null, seconds = null,
                notice = UiText.Res(R.string.episode_list_changed))
        }
    }

    fun background() {
        foreground = false; timer?.cancel()
        mutableState.value = mutableState.value?.copy(seconds = null, notice = UiText.Res(R.string.episode_auto_paused))
    }
    fun foreground() { foreground = true }
    fun close() { closed = true; timer?.cancel(); mutableState.value = null }

    companion object { const val COUNTDOWN_SECONDS = 10 }
}
