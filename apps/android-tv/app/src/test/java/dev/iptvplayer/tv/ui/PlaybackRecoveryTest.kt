@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@file:androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)

package dev.iptvplayer.tv.ui

import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackGroup
import androidx.media3.common.Tracks
import dev.iptvplayer.tv.R
import dev.iptvplayer.tv.data.AudioOutput
import dev.iptvplayer.tv.data.UiText
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test
import java.lang.reflect.Proxy
import javax.net.ssl.SSLHandshakeException

class PlaybackRecoveryTest {
    private fun network() = PlaybackException("private URL must not surface", null, PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED)

    @Test fun retryDelaysAndAttemptLimitDoNotStack() = runTest {
        val f = FakePlayer(); val r = PlaybackRecovery(f.player, this) { testScheduler.currentTime }
        r.newMedia(false); r.onPlayerError(network()); runCurrent()
        assertEquals(1, r.state.value.attempt)
        advanceTimeBy(999); runCurrent(); assertEquals(0, f.prepares)
        advanceTimeBy(1); runCurrent(); assertEquals(1, f.prepares)
        r.onPlayerError(network()); runCurrent()
        advanceTimeBy(2_999); runCurrent(); assertEquals(1, f.prepares)
        advanceTimeBy(1); runCurrent(); assertEquals(2, f.prepares)
        r.onPlayerError(network()); advanceTimeBy(30_000); runCurrent()
        assertEquals(2, f.prepares); assertNotNull(r.state.value.failure)
        // A fixed sentence without arguments: nothing from the error reaches the screen.
        assertEquals(UiText.Res(R.string.playback_unreachable), r.state.value.failure); r.close()
    }

    @Test fun permanentAndCertificateFailuresNeverRetry() = runTest {
        val f = FakePlayer(); val r = PlaybackRecovery(f.player, this) { testScheduler.currentTime }
        r.onPlayerError(PlaybackException("secret", null, PlaybackException.ERROR_CODE_DECODING_FORMAT_UNSUPPORTED))
        advanceTimeBy(30_000); runCurrent(); assertEquals(0, f.prepares)
        r.onPlayerError(PlaybackException("secret", SSLHandshakeException("secret"), PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED))
        advanceTimeBy(30_000); runCurrent(); assertEquals(0, f.prepares)
        assertEquals(UiText.Res(R.string.playback_secure_failed), r.state.value.failure); r.close()
    }

    @Test fun decoderFailuresPointToThePlayerSettings() = runTest {
        // A decoder that cannot open the stream names Ayarlar > Oynatıcı.
        val hinted = listOf(PlaybackException.ERROR_CODE_DECODER_INIT_FAILED, PlaybackException.ERROR_CODE_DECODING_FAILED,
            PlaybackException.ERROR_CODE_DECODING_FORMAT_EXCEEDS_CAPABILITIES, PlaybackException.ERROR_CODE_DECODING_FORMAT_UNSUPPORTED)
        for (code in hinted) assertEquals(code.toString(), PlaybackProblem(R.string.playback_decoder_failed),
            playbackProblem(PlaybackException("http://kaynak.test/live/user/pass/1.ts", null, code)))
        // A stream that cannot be read is no matter of decoders, nor an audio track that fails, as one does while the
        // sound system is busy or its output changes: those say to try again, as before.
        assertEquals(R.string.playback_unreadable, playbackProblem(PlaybackException("secret", null,
            PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED)).message)
        for (code in listOf(PlaybackException.ERROR_CODE_AUDIO_TRACK_INIT_FAILED, PlaybackException.ERROR_CODE_AUDIO_TRACK_WRITE_FAILED))
            assertEquals(code.toString(), PlaybackProblem(R.string.playback_failed), playbackProblem(PlaybackException("secret", null, code)))
        // Changing a setting is the way out: nothing retries on its own, and nothing from the error reaches the screen.
        val f = FakePlayer(); val r = PlaybackRecovery(f.player, this) { testScheduler.currentTime }
        r.onPlayerError(PlaybackException("http://kaynak.test/live/user/pass/1.ts", null, PlaybackException.ERROR_CODE_DECODER_INIT_FAILED))
        advanceTimeBy(30_000); runCurrent(); assertEquals(0, f.prepares)
        assertEquals(UiText.Res(R.string.playback_decoder_failed), r.state.value.failure); r.close()
    }

    @Test fun onlyAudioThatCannotPlayWhileDecodedOnTheDeviceAsksForOtomatik() {
        fun group(mime: String, support: Int) =
            Tracks.Group(TrackGroup(Format.Builder().setSampleMimeType(mime).build()), false, intArrayOf(support), booleanArrayOf(false))
        val video = group(MimeTypes.VIDEO_H264, C.FORMAT_HANDLED)
        val ac3 = group(MimeTypes.AUDIO_AC3, C.FORMAT_UNSUPPORTED_SUBTYPE)
        // An AC-3 stream the device has no decoder for: silent with Cihazda çöz, sent as it is with Otomatik.
        val silent = Tracks(listOf(video, ac3))
        assertTrue(unplayableAudio(silent, AudioOutput.PCM))
        assertFalse(unplayableAudio(silent, AudioOutput.AUTO))
        // One track that plays is enough, even one beyond what the device declares; a stream without audio is not silent.
        assertFalse(unplayableAudio(Tracks(listOf(video, ac3, group(MimeTypes.AUDIO_AAC, C.FORMAT_HANDLED))), AudioOutput.PCM))
        assertFalse(unplayableAudio(Tracks(listOf(video, group(MimeTypes.AUDIO_E_AC3, C.FORMAT_EXCEEDS_CAPABILITIES))), AudioOutput.PCM))
        assertFalse(unplayableAudio(Tracks(listOf(video)), AudioOutput.PCM))
        assertFalse(unplayableAudio(Tracks.EMPTY, AudioOutput.PCM))
    }

    @Test fun manualRetryResetsBudgetPreservesVodAndLiveRejoinsWindow() = runTest {
        val f = FakePlayer(); val r = PlaybackRecovery(f.player, this) { testScheduler.currentTime }
        r.newMedia(false); r.onPlayerError(network()); runCurrent()
        r.retryManually(); advanceTimeBy(2_000); runCurrent()
        assertEquals(1, f.prepares); assertEquals(0, f.liveSeeks)
        r.onPlayerError(network()); assertEquals(1, r.state.value.attempt)
        r.newMedia(true); r.retryManually()
        assertEquals(2, f.prepares); assertEquals(1, f.liveSeeks); r.close()
    }

    @Test fun cancelStopsPendingRetryAndLeavesManualAction() = runTest {
        val f = FakePlayer(); val r = PlaybackRecovery(f.player, this) { testScheduler.currentTime }
        r.onPlayerError(network()); runCurrent(); r.cancel()
        advanceTimeBy(30_000); runCurrent()
        assertEquals(0, f.prepares); assertEquals(1, f.stops)
        assertFalse(r.state.value.recovering); assertNotNull(r.state.value.failure); r.close()
    }

    @Test fun backgroundCancelsRetryAndDoesNotAutomaticallyReopenIt() = runTest {
        val f = FakePlayer(); val r = PlaybackRecovery(f.player, this) { testScheduler.currentTime }
        r.onPlayerError(network()); runCurrent(); r.background()
        advanceTimeBy(5_000); runCurrent(); r.foreground()
        advanceTimeBy(5_000); runCurrent()
        assertEquals(0, f.prepares); assertNotNull(r.state.value.failure)
        r.retryManually(); assertEquals(1, f.prepares); r.close()
    }

    @Test fun incidentDeadlineStopsAnInFlightRecovery() = runTest {
        val f = FakePlayer(); val r = PlaybackRecovery(f.player, this) { testScheduler.currentTime }
        r.onPlayerError(network()); runCurrent(); advanceTimeBy(1_000); runCurrent()
        r.onPlaybackStateChanged(Player.STATE_BUFFERING)
        advanceTimeBy(18_999); runCurrent(); assertEquals(0, f.stops)
        advanceTimeBy(1); runCurrent(); assertEquals(1, f.stops)
        assertNotNull(r.state.value.failure); assertFalse(r.state.value.recovering); r.close()
    }

    @Test fun onlyTenSecondsOfContinuousPlaybackReplenishesBudget() = runTest {
        val f = FakePlayer(); val r = PlaybackRecovery(f.player, this) { testScheduler.currentTime }
        r.onPlayerError(network()); runCurrent(); advanceTimeBy(1_000); runCurrent()
        r.onPlaybackStateChanged(Player.STATE_READY); r.onIsPlayingChanged(true); runCurrent()
        advanceTimeBy(2_000); r.onIsPlayingChanged(false); r.onPlayerError(network()); runCurrent()
        assertEquals(2, r.state.value.attempt)
        advanceTimeBy(3_000); runCurrent(); r.onIsPlayingChanged(true); runCurrent()
        advanceTimeBy(10_000); runCurrent(); r.onIsPlayingChanged(false); r.onPlayerError(network())
        assertEquals(1, r.state.value.attempt); r.close()
    }

    @Test fun newMediaAndDisposalCancelOldWork() = runTest {
        val f = FakePlayer(); val r = PlaybackRecovery(f.player, this) { testScheduler.currentTime }
        r.onPlayerError(network()); runCurrent(); r.newMedia(false)
        advanceTimeBy(3_000); runCurrent(); assertEquals(0, f.prepares)
        r.onPlayerError(network()); runCurrent(); r.close()
        advanceTimeBy(30_000); runCurrent(); assertEquals(0, f.prepares)
        assertEquals(0, f.listeners.size)
    }

    @Test fun openingHasASeparateThirtySecondLimit() = runTest {
        val f = FakePlayer(); val r = PlaybackRecovery(f.player, this) { testScheduler.currentTime }
        r.newMedia(false); r.onPlaybackStateChanged(Player.STATE_BUFFERING); runCurrent()
        advanceTimeBy(29_999); runCurrent(); assertEquals(0, f.stops)
        advanceTimeBy(1); runCurrent(); assertEquals(1, f.stops)
        assertNotNull(r.state.value.failure); r.close()
    }

    private class FakePlayer {
        var prepares = 0; var stops = 0; var liveSeeks = 0; var playWhenReady = true
        val listeners = mutableListOf<Player.Listener>()
        val player = Proxy.newProxyInstance(Player::class.java.classLoader, arrayOf(Player::class.java)) { _, method, args ->
            when (method.name) {
                "addListener" -> { listeners.add(args!![0] as Player.Listener); null }
                "removeListener" -> { listeners.remove(args!![0] as Player.Listener); null }
                "getPlayWhenReady" -> playWhenReady
                "getPlaybackState" -> Player.STATE_IDLE
                "prepare" -> { prepares++; null }
                "play" -> { playWhenReady = true; null }
                "pause" -> { playWhenReady = false; null }
                "stop" -> { stops++; null }
                "seekToDefaultPosition" -> { liveSeeks++; null }
                else -> when (method.returnType) { Boolean::class.javaPrimitiveType -> false; Int::class.javaPrimitiveType -> 0; Long::class.javaPrimitiveType -> 0L; else -> null }
            }
        } as Player
    }
}
