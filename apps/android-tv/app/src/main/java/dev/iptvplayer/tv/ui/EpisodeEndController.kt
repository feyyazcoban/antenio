package dev.iptvplayer.tv.ui

import dev.iptvplayer.tv.data.MediaEntry
import dev.iptvplayer.tv.data.MediaKind
import dev.iptvplayer.tv.data.NextEpisodeChoice
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
    val notice: String? = null
)

/** The end of media, not an estimated credits offset, starts the cancellable countdown. */
internal class EpisodeEndController(
    private val scope: CoroutineScope,
    private val playNext: (sourceId: String, currentId: String, nextId: String, automatic: Boolean) -> Boolean
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
            when { !foreground -> "Otomatik geçiş duraklatıldı."; !autoEnabled && next != null -> "Otomatik geçiş kapalı."; else -> null })
        if (automatic) timer = scope.launch {
            for (seconds in COUNTDOWN_SECONDS - 1 downTo 0) {
                delay(1000)
                val currentState = mutableState.value ?: return@launch
                if (seconds == 0) play(automatic = true)
                else mutableState.value = currentState.copy(seconds = seconds)
            }
        }
    }

    fun playNow() = play(automatic = false)

    private fun play(automatic: Boolean) {
        timer?.cancel()
        val s = mutableState.value ?: return
        val next = s.next ?: return
        if (!foreground || closed) return
        // The model validates the current source, episode and latest target again before opening media.
        if (playNext(s.sourceId, s.current.id, next.entry.id, automatic)) mutableState.value = null
        else mutableState.value = s.copy(next = null, seconds = null,
            notice = "Sonraki bölüm artık açılamıyor. Bölüm listesinden yeniden seçebilirsin.")
    }

    fun dismiss() { timer?.cancel(); mutableState.value = null }

    fun preferenceChanged(enabled: Boolean) {
        if (!enabled && mutableState.value?.seconds != null) {
            timer?.cancel()
            mutableState.value = mutableState.value?.copy(seconds = null, notice = "Otomatik geçiş kapalı.")
        }
    }

    fun availabilityChanged(next: NextEpisodeChoice?) {
        val s = mutableState.value ?: return
        val previous = s.next ?: return
        if (next?.entry?.id != previous.entry.id || (s.seconds != null && !next.automaticAllowed)) {
            timer?.cancel()
            mutableState.value = s.copy(next = null, seconds = null,
                notice = "Bölüm listesi değişti. Devam etmek için listeden bir bölüm seç.")
        }
    }

    fun background() {
        foreground = false; timer?.cancel()
        mutableState.value = mutableState.value?.copy(seconds = null, notice = "Otomatik geçiş duraklatıldı.")
    }
    fun foreground() { foreground = true }
    fun close() { closed = true; timer?.cancel(); mutableState.value = null }

    companion object { const val COUNTDOWN_SECONDS = 10 }
}
