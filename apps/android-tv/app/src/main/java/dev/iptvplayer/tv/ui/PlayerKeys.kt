package dev.iptvplayer.tv.ui

import android.view.KeyEvent
import dev.iptvplayer.tv.PlaybackMode

/**
 * What covers the picture (gap plan T4). The viewer opens [Controls] and [Tracks]; [EpisodeEnd] is the end card
 * EpisodeEndController keeps, over whatever was open. A new layer joins here and in [PlayerKeyRouter] (plan §2, rule 6).
 */
internal sealed interface PlayerLayer {
    /** Nothing open: the picture, with the corner notes over it. */
    data object Video : PlayerLayer
    data object Controls : PlayerLayer
    /** The Ses / Altyazı panel. */
    data object Tracks : PlayerLayer
    data object EpisodeEnd : PlayerLayer
}

/** The remote keys the player reads. [Center] is OK and Enter; a [Digit] comes from the number row or the keypad. */
internal sealed interface PlayerKey {
    data object PlayPause : PlayerKey
    data object Play : PlayerKey
    data object Pause : PlayerKey
    data object ChannelUp : PlayerKey
    data object ChannelDown : PlayerKey
    data object Up : PlayerKey
    data object Down : PlayerKey
    data object Left : PlayerKey
    data object Right : PlayerKey
    data object Center : PlayerKey
    data object Guide : PlayerKey
    data object FastForward : PlayerKey
    data object Rewind : PlayerKey
    data class Digit(val value: Int) : PlayerKey

    companion object {
        /** The key an Android [keyCode] stands for; null for the others, Back among them, which focus and BackHandler take. */
        fun of(keyCode: Int): PlayerKey? = when (keyCode) {
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> PlayPause
            KeyEvent.KEYCODE_MEDIA_PLAY -> Play
            KeyEvent.KEYCODE_MEDIA_PAUSE -> Pause
            KeyEvent.KEYCODE_CHANNEL_UP -> ChannelUp
            KeyEvent.KEYCODE_CHANNEL_DOWN -> ChannelDown
            KeyEvent.KEYCODE_DPAD_UP -> Up
            KeyEvent.KEYCODE_DPAD_DOWN -> Down
            KeyEvent.KEYCODE_DPAD_LEFT -> Left
            KeyEvent.KEYCODE_DPAD_RIGHT -> Right
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> Center
            KeyEvent.KEYCODE_GUIDE -> Guide
            KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> FastForward
            KeyEvent.KEYCODE_MEDIA_REWIND -> Rewind
            in KeyEvent.KEYCODE_0..KeyEvent.KEYCODE_9 -> Digit(keyCode - KeyEvent.KEYCODE_0)
            in KeyEvent.KEYCODE_NUMPAD_0..KeyEvent.KEYCODE_NUMPAD_9 -> Digit(keyCode - KeyEvent.KEYCODE_NUMPAD_0)
            else -> null
        }
    }
}

/**
 * The player when a key arrives: [layer] is the one on top, [recovering] and [failed] the connection's state, [playing]
 * the player's own isPlaying, and [repeat] how often a key held down has repeated (KeyEvent.repeatCount).
 */
internal data class KeyContext(val mode: PlaybackMode, val layer: PlayerLayer, val recovering: Boolean = false,
    val failed: Boolean = false, val playing: Boolean = false, val repeat: Int = 0) {
    val live get() = mode == PlaybackMode.LIVE
    /** Neither reconnecting nor stopped by an error. */
    val healthy get() = !recovering && !failed
}

/** What a key or Back asks of the player; PlayerScreen carries it out. */
internal sealed interface PlayerCommand {
    data object Play : PlayerCommand
    data object Pause : PlayerCommand
    /** Tekrar dene after an error. */
    data object Retry : PlayerCommand
    /** Stops a reconnection, as the controls' Durdur does. */
    data object StopRecovery : PlayerCommand
    /** Taken with nothing to do: Play while reconnecting. */
    data object Consume : PlayerCommand
    /** The next channel up (1) or down (−1) of the list the player was opened from. */
    data class Zap(val direction: Int) : PlayerCommand
    /** One more digit of a Canlı TV number. */
    data class AppendDigit(val digit: Int) : PlayerCommand
    data object OpenGuide : PlayerCommand
    data class Seek(val deltaMs: Long) : PlayerCommand
    data object ShowControls : PlayerCommand
    data object HideControls : PlayerCommand
    data object CloseTracks : PlayerCommand
    /** Leaves the player; [stopRecovery] stops a reconnection first. */
    data class Exit(val stopRecovery: Boolean = false) : PlayerCommand
}

/**
 * The player's remote (gap plan T4), pure so that every key is tested on the JVM. Live: CH± change the channel at any
 * time, Up/Down only with the controls hidden (Up is the next number, as CH+); digits type a Canlı TV number, played
 * [DIGIT_COMMIT_MS] after the last one; the guide key opens the programme guide. Films and episodes: FF jumps 30 s
 * ahead and RW 10 s back; with the controls hidden, Left/Right seek further the longer they are held ([seekStep]).
 * With the controls hidden, OK and the arrows left over bring them up.
 */
internal object PlayerKeyRouter {
    const val DIGIT_COMMIT_MS = 1_500L
    const val MAX_DIGITS = 4

    /** The command for [key], or null to leave the key to focus: the controls' buttons and the panels. */
    fun route(key: PlayerKey, ctx: KeyContext): PlayerCommand? = with(ctx) {
        val hidden = layer == PlayerLayer.Video
        when {
            key == PlayerKey.PlayPause -> when {
                recovering -> PlayerCommand.StopRecovery
                failed -> PlayerCommand.Retry
                playing -> PlayerCommand.Pause
                else -> PlayerCommand.Play
            }
            key == PlayerKey.Play -> when { failed -> PlayerCommand.Retry; recovering -> PlayerCommand.Consume; else -> PlayerCommand.Play }
            key == PlayerKey.Pause -> if (recovering) PlayerCommand.StopRecovery else PlayerCommand.Pause
            live && (key == PlayerKey.ChannelUp || key == PlayerKey.ChannelDown) -> PlayerCommand.Zap(if (key == PlayerKey.ChannelUp) 1 else -1)
            live && hidden && healthy && (key == PlayerKey.Up || key == PlayerKey.Down) -> PlayerCommand.Zap(if (key == PlayerKey.Up) 1 else -1)
            live && layer != PlayerLayer.Tracks && key is PlayerKey.Digit -> PlayerCommand.AppendDigit(key.value)
            live && key == PlayerKey.Guide -> PlayerCommand.OpenGuide
            !live && healthy && (key == PlayerKey.FastForward || key == PlayerKey.Rewind) ->
                PlayerCommand.Seek(if (key == PlayerKey.FastForward) 30_000 else -10_000)
            !live && hidden && healthy && (key == PlayerKey.Left || key == PlayerKey.Right) ->
                PlayerCommand.Seek(seekStep(repeat).let { if (key == PlayerKey.Right) it else -it })
            hidden && key in setOf(PlayerKey.Center, PlayerKey.Left, PlayerKey.Right, PlayerKey.Up, PlayerKey.Down) -> PlayerCommand.ShowControls
            else -> null
        }
    }

    /**
     * Back, one layer a press: the panel closes; a reconnection stops and the player closes; the controls hide; else
     * the player closes. The panel and the end card are dialogs and take their own Back first; a Back that reaches the
     * player under the end card hides the controls behind it.
     */
    fun back(ctx: KeyContext): PlayerCommand = when {
        ctx.layer == PlayerLayer.Tracks -> PlayerCommand.CloseTracks
        ctx.recovering -> PlayerCommand.Exit(stopRecovery = true)
        ctx.layer != PlayerLayer.Video && !ctx.failed -> PlayerCommand.HideControls
        else -> PlayerCommand.Exit()
    }

    /** A held Left/Right: 10 s a step, 30 s from the 10th repeat, a minute from the 30th. */
    fun seekStep(repeat: Int): Long = when { repeat >= 30 -> 60_000L; repeat >= 10 -> 30_000L; else -> 10_000L }

    /** The channel number typed so far with [digit] added; it stops growing at [MAX_DIGITS]. */
    fun appendDigit(typed: String, digit: Int): String = if (typed.length < MAX_DIGITS) typed + digit else typed
}
