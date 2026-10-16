@file:androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)

package dev.iptvplayer.tv.ui

import android.os.SystemClock
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.upstream.DefaultLoadErrorHandlingPolicy
import androidx.media3.exoplayer.upstream.LoadErrorHandlingPolicy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.net.ssl.SSLException

/** One retry owner: prevent Media3's per-load retries from multiplying the visible attempts. */
internal class SingleAttemptLoadPolicy : DefaultLoadErrorHandlingPolicy(0) {
    override fun getRetryDelayMsFor(loadErrorInfo: LoadErrorHandlingPolicy.LoadErrorInfo) = C.TIME_UNSET
    override fun getFallbackSelectionFor(
        fallbackOptions: LoadErrorHandlingPolicy.FallbackOptions,
        loadErrorInfo: LoadErrorHandlingPolicy.LoadErrorInfo
    ): LoadErrorHandlingPolicy.FallbackSelection? = null
}

internal data class PlaybackProblem(val message: String, val retryable: Boolean = false)

internal fun playbackProblem(error: PlaybackException): PlaybackProblem {
    val causes = generateSequence(error as Throwable) { it.cause }.take(12).toList()
    // Certificate failures need intervention, even when wrapped as a network connection failure.
    if (causes.any { it is SSLException }) return PlaybackProblem("Sunucuyla güvenli bağlantı kurulamadı. Kaynak adresini kontrol et.")
    val http = causes.filterIsInstance<HttpDataSource.InvalidResponseCodeException>().firstOrNull()?.responseCode
    if (http != null) return when (http) {
        401, 403 -> PlaybackProblem("Bu yayına erişim verilmedi. Hesap bilgilerini ve aboneliğini kontrol et.")
        404, 410 -> PlaybackProblem("Bu yayın artık bulunamıyor. Kataloğu yenileyebilir veya başka bir içerik seçebilirsin.")
        429 -> PlaybackProblem("Sağlayıcının bağlantı sınırına ulaşıldı. Biraz bekleyip tekrar dene.")
        408, 500, 502, 503, 504 -> PlaybackProblem("Sağlayıcı şu anda yanıt vermiyor. Biraz sonra tekrar dene.", true)
        else -> PlaybackProblem("Sağlayıcı yayını açmadı. Kaynak erişimini kontrol et.")
    }
    return when (error.errorCode) {
        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT -> PlaybackProblem("Yayına bağlanılamadı. İnternet bağlantısını kontrol et.", true)
        PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW -> PlaybackProblem("Canlı yayınla bağlantı koptu. Yeniden bağlanmayı dene.", true)
        PlaybackException.ERROR_CODE_DECODER_INIT_FAILED,
        PlaybackException.ERROR_CODE_DECODING_FAILED,
        PlaybackException.ERROR_CODE_DECODING_FORMAT_EXCEEDS_CAPABILITIES,
        PlaybackException.ERROR_CODE_DECODING_FORMAT_UNSUPPORTED -> PlaybackProblem("Bu cihaz yayının görüntü veya ses biçimini çözemiyor.")
        PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED,
        PlaybackException.ERROR_CODE_PARSING_MANIFEST_MALFORMED,
        PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED,
        PlaybackException.ERROR_CODE_PARSING_MANIFEST_UNSUPPORTED,
        PlaybackException.ERROR_CODE_IO_INVALID_HTTP_CONTENT_TYPE -> PlaybackProblem("Yayının biçimi okunamadı. Başka bir içerik deneyebilirsin.")
        else -> PlaybackProblem("Yayın oynatılamadı. Tekrar dene veya başka bir içerik seç.")
    }
}

internal data class RecoveryState(val failure: String? = null, val attempt: Int = 0, val seconds: Int? = null) {
    val recovering get() = attempt > 0
    val message get() = if (seconds != null) "Bağlantı kesildi · $seconds sn sonra yeniden denenecek ($attempt/2)"
        else "Yeniden bağlanılıyor… ($attempt/2)"
}

/** All calls and callbacks run on the player's main looper. URLs/errors are never displayed or logged. */
internal class PlaybackRecovery(
    private val player: Player,
    private val scope: CoroutineScope,
    private val elapsedMs: () -> Long = SystemClock::elapsedRealtime
) : Player.Listener {
    private val mutableState = MutableStateFlow(RecoveryState())
    val state = mutableState.asStateFlow()
    private var retryJob: Job? = null
    private var deadlineJob: Job? = null
    private var stableJob: Job? = null
    private var attempts = 0
    private var incidentAt: Long? = null
    private var live = false
    private var foreground = true
    private var closed = false

    init { player.addListener(this) }

    fun newMedia(isLive: Boolean) {
        cancelJobs(); attempts = 0; incidentAt = null; live = isLive
        mutableState.value = RecoveryState()
    }

    fun retryManually() {
        if (!foreground || closed) return
        cancelJobs(); attempts = 0; incidentAt = null
        mutableState.value = RecoveryState()
        prepare()
    }

    private fun prepare() {
        // prepare() retains VOD position; live recovery rejoins the current live window.
        if (live) player.seekToDefaultPosition()
        player.prepare(); player.play()
    }

    override fun onPlayerError(error: PlaybackException) {
        if (closed) return
        retryJob?.cancel(); stableJob?.cancel(); deadlineJob?.cancel()
        val problem = playbackProblem(error)
        if (!foreground || !player.playWhenReady || !problem.retryable || attempts >= 2) {
            mutableState.value = RecoveryState(failure = problem.message)
            return
        }
        val now = elapsedMs()
        val first = incidentAt ?: now.also { incidentAt = it }
        val wait = if (attempts == 0) 1_000L else 3_000L
        if (now - first + wait >= RECOVERY_LIMIT_MS) {
            stopWithMessage("Yayın bağlantısı zamanında kurulamadı. Tekrar deneyebilirsin.")
            return
        }
        val attempt = ++attempts
        mutableState.value = RecoveryState(attempt = attempt, seconds = (wait / 1000).toInt())
        armDeadline(RECOVERY_LIMIT_MS - (now - first))
        retryJob = scope.launch {
            for (seconds in (wait / 1000).toInt() downTo 1) {
                mutableState.value = RecoveryState(attempt = attempt, seconds = seconds)
                delay(1000)
            }
            if (!foreground || closed) return@launch
            mutableState.value = RecoveryState(attempt = attempt)
            prepare()
        }
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        if (closed || !foreground) return
        when (playbackState) {
            Player.STATE_BUFFERING -> {
                if (deadlineJob?.isActive != true) {
                    val remaining = incidentAt?.let { RECOVERY_LIMIT_MS - (elapsedMs() - it) } ?: OPEN_LIMIT_MS
                    armDeadline(remaining.coerceAtLeast(1))
                }
            }
            Player.STATE_READY, Player.STATE_ENDED -> {
                deadlineJob?.cancel()
                mutableState.value = RecoveryState()
            }
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        stableJob?.cancel()
        if (isPlaying && !closed && foreground) {
            deadlineJob?.cancel(); mutableState.value = RecoveryState()
            // A brief ready/playing pulse must not replenish a failed connection's retry budget.
            stableJob = scope.launch { delay(STABLE_PLAYBACK_MS); attempts = 0; incidentAt = null }
        }
    }

    private fun armDeadline(remaining: Long) {
        deadlineJob?.cancel()
        deadlineJob = scope.launch {
            delay(remaining)
            stopWithMessage("Yayın bağlantısı zamanında kurulamadı. Tekrar deneyebilirsin.")
        }
    }

    fun cancel() = stopWithMessage("Yeniden bağlanma durduruldu. Hazır olduğunda tekrar deneyebilirsin.")

    fun background() {
        foreground = false
        val connecting = state.value.recovering || player.playbackState == Player.STATE_BUFFERING
        cancelJobs()
        player.pause()
        if (connecting) stopWithMessage("Yayına bağlanma duraklatıldı. Devam etmek için tekrar dene.")
    }

    fun foreground() { foreground = true }

    private fun stopWithMessage(message: String) {
        cancelJobs()
        mutableState.value = RecoveryState(failure = message)
        player.pause(); player.stop()
    }

    private fun cancelJobs() { retryJob?.cancel(); deadlineJob?.cancel(); stableJob?.cancel() }

    fun close() { closed = true; cancelJobs(); player.removeListener(this) }

    companion object {
        const val OPEN_LIMIT_MS = 30_000L
        const val RECOVERY_LIMIT_MS = 20_000L
        const val STABLE_PLAYBACK_MS = 10_000L
    }
}
