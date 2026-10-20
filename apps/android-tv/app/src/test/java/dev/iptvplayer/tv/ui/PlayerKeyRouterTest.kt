package dev.iptvplayer.tv.ui

import android.view.KeyEvent
import dev.iptvplayer.tv.PlaybackMode
import org.junit.Assert.*
import org.junit.Test

class PlayerKeyRouterTest {
    @Test fun channelDrawerKeepsBrowsingKeysAndClosesBeforeRecovery() {
        for (ctx in listOf(KeyContext(PlaybackMode.LIVE, PlayerLayer.Channels),
            KeyContext(PlaybackMode.LIVE, PlayerLayer.Channels, recovering = true),
            KeyContext(PlaybackMode.LIVE, PlayerLayer.Channels, failed = true))) {
            for (key in listOf(PlayerKey.Up, PlayerKey.Down, PlayerKey.Left, PlayerKey.Right, PlayerKey.Center)) routes(null, key, ctx)
            for (key in listOf(PlayerKey.ChannelUp, PlayerKey.ChannelDown, PlayerKey.Guide, PlayerKey.Digit(2))) routes(PlayerCommand.Consume, key, ctx)
            assertEquals(PlayerCommand.CloseChannels, PlayerKeyRouter.back(ctx))
        }
    }
    /** The layers the viewer opens; the end card has its own test. */
    private val layers = listOf(PlayerLayer.Video, PlayerLayer.Controls, PlayerLayer.Tracks)
    private val modes = listOf(PlaybackMode.LIVE, PlaybackMode.VOD)
    private val live = KeyContext(PlaybackMode.LIVE, PlayerLayer.Video)
    private val vod = KeyContext(PlaybackMode.VOD, PlayerLayer.Video)
    private val arrows = listOf(PlayerKey.Up, PlayerKey.Down, PlayerKey.Left, PlayerKey.Right)

    /** Every layer, mode and connection state, for the keys that mean the same everywhere. */
    private fun everywhere(layers: List<PlayerLayer> = this.layers) = modes.flatMap { mode -> layers.flatMap { layer ->
        listOf(KeyContext(mode, layer), KeyContext(mode, layer, playing = true), KeyContext(mode, layer, recovering = true),
            KeyContext(mode, layer, failed = true))
    } }

    private fun route(key: PlayerKey, ctx: KeyContext) = PlayerKeyRouter.route(key, ctx)
    private fun routes(expected: PlayerCommand?, key: PlayerKey, ctx: KeyContext) = assertEquals("$key in $ctx", expected, route(key, ctx))

    @Test fun androidKeyCodesMapToThePlayersKeys() {
        val keys = mapOf(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE to PlayerKey.PlayPause, KeyEvent.KEYCODE_MEDIA_PLAY to PlayerKey.Play,
            KeyEvent.KEYCODE_MEDIA_PAUSE to PlayerKey.Pause, KeyEvent.KEYCODE_CHANNEL_UP to PlayerKey.ChannelUp,
            KeyEvent.KEYCODE_CHANNEL_DOWN to PlayerKey.ChannelDown, KeyEvent.KEYCODE_DPAD_UP to PlayerKey.Up,
            KeyEvent.KEYCODE_DPAD_DOWN to PlayerKey.Down, KeyEvent.KEYCODE_DPAD_LEFT to PlayerKey.Left,
            KeyEvent.KEYCODE_DPAD_RIGHT to PlayerKey.Right, KeyEvent.KEYCODE_DPAD_CENTER to PlayerKey.Center,
            KeyEvent.KEYCODE_ENTER to PlayerKey.Center, KeyEvent.KEYCODE_GUIDE to PlayerKey.Guide,
            KeyEvent.KEYCODE_MEDIA_FAST_FORWARD to PlayerKey.FastForward, KeyEvent.KEYCODE_MEDIA_REWIND to PlayerKey.Rewind)
        keys.forEach { (code, key) -> assertEquals("$code", key, PlayerKey.of(code)) }
        for (d in 0..9) {
            assertEquals(PlayerKey.Digit(d), PlayerKey.of(KeyEvent.KEYCODE_0 + d))
            assertEquals(PlayerKey.Digit(d), PlayerKey.of(KeyEvent.KEYCODE_NUMPAD_0 + d))
        }
        // Back belongs to BackHandler; the other keys go on to focus as before.
        for (code in listOf(KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_MENU, KeyEvent.KEYCODE_INFO, KeyEvent.KEYCODE_NUMPAD_ENTER,
            KeyEvent.KEYCODE_PAGE_UP, KeyEvent.KEYCODE_MEDIA_STOP)) assertNull("$code", PlayerKey.of(code))
    }

    @Test fun playPauseStopsAReconnectionRetriesAnErrorOrToggles() {
        for (ctx in everywhere()) {
            routes(when { ctx.recovering -> PlayerCommand.StopRecovery; ctx.failed -> PlayerCommand.Retry
                ctx.playing -> PlayerCommand.Pause; else -> PlayerCommand.Play }, PlayerKey.PlayPause, ctx)
            // Play is taken without effect while reconnecting.
            routes(when { ctx.failed -> PlayerCommand.Retry; ctx.recovering -> PlayerCommand.Consume; else -> PlayerCommand.Play }, PlayerKey.Play, ctx)
            routes(if (ctx.recovering) PlayerCommand.StopRecovery else PlayerCommand.Pause, PlayerKey.Pause, ctx)
        }
        routes(PlayerCommand.Retry, PlayerKey.Play, live.copy(recovering = true, failed = true))
    }

    @Test fun channelKeysZapLiveInEveryLayerAndStateButNotFilms() {
        for (ctx in everywhere()) {
            routes(if (ctx.live) PlayerCommand.Zap(1) else null, PlayerKey.ChannelUp, ctx)
            routes(if (ctx.live) PlayerCommand.Zap(-1) else null, PlayerKey.ChannelDown, ctx)
        }
    }

    @Test fun upAndDownZapOnlyWhileTheControlsAreHiddenAndPlaybackIsHealthy() {
        routes(PlayerCommand.Zap(1), PlayerKey.Up, live)
        routes(PlayerCommand.Zap(-1), PlayerKey.Down, live)
        routes(PlayerCommand.Zap(1), PlayerKey.Up, live.copy(playing = true, repeat = 12))
        // A broken or reconnecting stream brings the controls up instead.
        routes(PlayerCommand.ShowControls, PlayerKey.Up, live.copy(recovering = true))
        routes(PlayerCommand.ShowControls, PlayerKey.Down, live.copy(failed = true))
        // With anything open the arrows move focus.
        for (layer in layers - PlayerLayer.Video) for (key in arrows) routes(null, key, live.copy(layer = layer))
    }

    @Test fun digitsTypeALiveChannelNumberExceptOverTheTracksPanel() {
        for (d in 0..9) {
            for (layer in listOf(PlayerLayer.Video, PlayerLayer.Controls))
                routes(if (d == 0) PlayerCommand.PreviousChannel else PlayerCommand.AppendDigit(d), PlayerKey.Digit(d), live.copy(layer = layer, failed = true))
            routes(null, PlayerKey.Digit(d), live.copy(layer = PlayerLayer.Tracks))
            for (layer in layers) routes(null, PlayerKey.Digit(d), vod.copy(layer = layer))
        }
        assertEquals(1_500L, PlayerKeyRouter.DIGIT_COMMIT_MS)
        assertEquals("7", PlayerKeyRouter.appendDigit("", 7))
        assertEquals("1230", PlayerKeyRouter.appendDigit("123", 0))
        // A fifth digit is dropped; the number still commits after the pause.
        assertEquals("1234", PlayerKeyRouter.appendDigit("1234", 5))
    }

    @Test fun previousChannelIsImmediateButZeroStillCompletesANumber() {
        assertEquals(PlayerKey.LastChannel, PlayerKey.of(KeyEvent.KEYCODE_LAST_CHANNEL))
        for (layer in listOf(PlayerLayer.Video, PlayerLayer.Controls)) {
            val context = live.copy(layer = layer)
            routes(PlayerCommand.PreviousChannel, PlayerKey.Digit(0), context)
            routes(PlayerCommand.AppendDigit(0), PlayerKey.Digit(0), context.copy(pendingDigits = true))
            routes(PlayerCommand.PreviousChannel, PlayerKey.LastChannel, context.copy(pendingDigits = true))
            routes(PlayerCommand.Consume, PlayerKey.LastChannel, context.copy(repeat = 1))
            routes(PlayerCommand.Consume, PlayerKey.Digit(0), context.copy(repeat = 12))
            routes(null, PlayerKey.LastChannel, vod.copy(layer = layer))
        }
        routes(null, PlayerKey.LastChannel, live.copy(layer = PlayerLayer.Tracks))
        routes(PlayerCommand.Consume, PlayerKey.LastChannel, live.copy(layer = PlayerLayer.Channels))
    }

    @Test fun theGuideKeyOpensTheGuideFromLiveOnly() {
        for (ctx in everywhere()) routes(if (ctx.live) PlayerCommand.OpenGuide else null, PlayerKey.Guide, ctx)
    }

    @Test fun fastForwardAndRewindJumpInFilmsWhilePlaybackIsHealthy() {
        for (ctx in everywhere()) {
            val jumps = !ctx.live && ctx.healthy
            routes(if (jumps) (if (ctx.layer == PlayerLayer.Controls) PlayerCommand.ScrubNudge(30_000) else PlayerCommand.Seek(30_000)) else null, PlayerKey.FastForward, ctx)
            routes(if (jumps) (if (ctx.layer == PlayerLayer.Controls) PlayerCommand.ScrubNudge(-10_000) else PlayerCommand.Seek(-10_000)) else null, PlayerKey.Rewind, ctx)
        }
    }

    @Test fun heldLeftAndRightSeekFurtherTheLongerTheyAreHeld() {
        val steps = mapOf(0 to 10_000L, 9 to 10_000L, 10 to 30_000L, 29 to 30_000L, 30 to 60_000L, 200 to 60_000L)
        steps.forEach { (repeat, step) ->
            assertEquals(step, PlayerKeyRouter.seekStep(repeat))
            routes(PlayerCommand.Seek(step), PlayerKey.Right, vod.copy(repeat = repeat, playing = true))
            routes(PlayerCommand.Seek(-step), PlayerKey.Left, vod.copy(repeat = repeat))
        }
        // Without a healthy stream, or on a channel, they bring the controls up; with the controls up they move focus.
        routes(PlayerCommand.ShowControls, PlayerKey.Right, vod.copy(failed = true))
        routes(PlayerCommand.ShowControls, PlayerKey.Left, vod.copy(recovering = true))
        routes(PlayerCommand.ShowControls, PlayerKey.Left, live)
        routes(PlayerCommand.ShowControls, PlayerKey.Right, live.copy(repeat = 40))
        for (layer in layers - PlayerLayer.Video) {
            routes(null, PlayerKey.Left, vod.copy(layer = layer))
            routes(null, PlayerKey.Right, vod.copy(layer = layer))
        }
        routes(PlayerCommand.ShowControls, PlayerKey.Up, vod)
        routes(PlayerCommand.ShowControls, PlayerKey.Down, vod.copy(failed = true))
    }

    @Test fun okBringsHiddenControlsUpAndOtherwiseClicksTheFocusedControl() {
        for (ctx in everywhere()) routes(if (ctx.layer == PlayerLayer.Video) PlayerCommand.ShowControls else null, PlayerKey.Center, ctx)
    }

    @Test fun backClosesOneLayerAPress() {
        fun back(ctx: KeyContext) = PlayerKeyRouter.back(ctx)
        for (mode in modes) {
            val ctx = KeyContext(mode, PlayerLayer.Video)
            // The panel closes first, even while reconnecting.
            assertEquals(PlayerCommand.CloseTracks, back(ctx.copy(layer = PlayerLayer.Tracks)))
            assertEquals(PlayerCommand.CloseTracks, back(ctx.copy(layer = PlayerLayer.Tracks, recovering = true)))
            assertEquals(PlayerCommand.CloseTracks, back(ctx.copy(layer = PlayerLayer.Tracks, failed = true)))
            // A reconnection stops and the player closes, whatever else is shown.
            for (layer in layers - PlayerLayer.Tracks) assertEquals(PlayerCommand.Exit(stopRecovery = true), back(ctx.copy(layer = layer, recovering = true)))
            assertEquals(PlayerCommand.HideControls, back(ctx.copy(layer = PlayerLayer.Controls, playing = true)))
            // The error stays on screen with the controls: Back leaves.
            assertEquals(PlayerCommand.Exit(), back(ctx.copy(layer = PlayerLayer.Controls, failed = true)))
            assertEquals(PlayerCommand.Exit(), back(ctx))
            assertEquals(PlayerCommand.Exit(), back(ctx.copy(failed = true)))
        }
    }

    @Test fun theEndCardClosesOnPauseOrBackAndLeavesTheOtherKeysToItsButtons() {
        val others = listOf(PlayerKey.Play, PlayerKey.ChannelUp, PlayerKey.ChannelDown, PlayerKey.Center, PlayerKey.Guide,
            PlayerKey.FastForward, PlayerKey.Rewind, PlayerKey.Digit(1)) + arrows
        // As Bu bölümde kal, whatever the connection is doing: the card closes and the controls come up.
        for (ctx in everywhere(listOf(PlayerLayer.EpisodeEnd))) {
            routes(PlayerCommand.CloseEndCard, PlayerKey.PlayPause, ctx)
            routes(PlayerCommand.CloseEndCard, PlayerKey.Pause, ctx)
            assertEquals("Back in $ctx", PlayerCommand.CloseEndCard, PlayerKeyRouter.back(ctx))
            for (key in others) routes(null, key, ctx.copy(repeat = 12))
        }
    }
    @Test fun pendingSeekCommitsBeforeToggleAndBackCancelsBeforeHiding() {
        val pending = vod.copy(layer = PlayerLayer.Controls, scrubbing = true)
        routes(PlayerCommand.CommitScrub(true), PlayerKey.PlayPause, pending)
        assertEquals(PlayerCommand.CancelScrub, PlayerKeyRouter.back(pending))
        assertEquals(PlayerCommand.HideControls, PlayerKeyRouter.back(pending.copy(scrubbing = false)))
        assertEquals(PlayerCommand.Exit(true), PlayerKeyRouter.back(pending.copy(recovering = true)))
        assertEquals(PlayerCommand.CloseTracks, PlayerKeyRouter.back(pending.copy(layer = PlayerLayer.Tracks)))
        routes(PlayerCommand.StopRecovery, PlayerKey.PlayPause, pending.copy(recovering = true))
    }

}
