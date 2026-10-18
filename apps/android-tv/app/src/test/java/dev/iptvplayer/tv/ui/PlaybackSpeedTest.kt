package dev.iptvplayer.tv.ui

import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import dev.iptvplayer.tv.PlaybackMode
import java.lang.reflect.Proxy
import java.util.Locale
import org.junit.Assert.*
import org.junit.Test

class PlaybackSpeedTest {
    @Test fun changingSpeedKeepsPitchAndDoesNotRestartOrSeek() {
        val calls = mutableListOf<String>()
        var parameters = PlaybackParameters.DEFAULT
        val player = Proxy.newProxyInstance(Player::class.java.classLoader, arrayOf(Player::class.java)) { _, method, args ->
            calls += method.name
            when (method.name) {
                "isCommandAvailable" -> true
                "setPlaybackParameters" -> { parameters = args!![0] as PlaybackParameters; null }
                else -> error("Unexpected player operation: ${method.name}")
            }
        } as Player
        PlaybackSpeed.choices.forEach { value ->
            assertTrue(PlaybackSpeed.apply(player, PlaybackMode.VOD, value, false))
            assertEquals(value, parameters.speed, 0f)
            assertEquals(1f, parameters.pitch, 0f)
        }
        assertTrue(calls.all { it == "isCommandAvailable" || it == "setPlaybackParameters" })
        calls.clear()
        assertFalse(PlaybackSpeed.apply(player, PlaybackMode.LIVE, 2f, false))
        assertFalse(PlaybackSpeed.apply(player, PlaybackMode.VOD, 2f, true))
        for (value in listOf(0f, -1f, Float.NaN, Float.POSITIVE_INFINITY, 3f))
            assertFalse(PlaybackSpeed.apply(player, PlaybackMode.VOD, value, false))
        assertTrue(calls.isEmpty())
    }

    @Test fun unavailableCommandDoesNotChangeThePlayer() {
        val player = Proxy.newProxyInstance(Player::class.java.classLoader, arrayOf(Player::class.java)) { _, method, _ ->
            if (method.name == "isCommandAvailable") false else error(method.name)
        } as Player
        assertFalse(PlaybackSpeed.apply(player, PlaybackMode.VOD, 2f, false))
    }

    @Test fun predictedEndUsesWallTimeAndUnknownOrEndedMediaHasNone() {
        assertEquals(30_000L, PlaybackSpeed.remainingMs(30_000, 90_000, 2f))
        assertEquals(120_000L, PlaybackSpeed.remainingMs(30_000, 90_000, .5f))
        assertEquals(90_000L, PlaybackSpeed.remainingMs(-1, 90_000, 1f))
        assertNull(PlaybackSpeed.remainingMs(0, 0, 1f))
        assertNull(PlaybackSpeed.remainingMs(90_000, 90_000, 1f))
        assertNull(PlaybackSpeed.remainingMs(100_000, 90_000, 1f))
        assertNull(PlaybackSpeed.remainingMs(0, 90_000, Float.NaN))
        assertEquals("1,25×", PlaybackSpeed.label(1.25f, Locale.forLanguageTag("tr")))
        assertEquals("1.25×", PlaybackSpeed.label(1.25f, Locale.ENGLISH))
    }

    @Test fun optionsAndSpeedBackCloseOneLevelAndArrowsDoNotSeek() {
        val ctx = KeyContext(PlaybackMode.VOD, PlayerLayer.Speed, playing = true)
        assertEquals(PlayerCommand.CloseSpeed, PlayerKeyRouter.back(ctx))
        assertEquals(PlayerCommand.CloseOptions, PlayerKeyRouter.back(ctx.copy(layer = PlayerLayer.Options)))
        for (layer in listOf(PlayerLayer.Options, PlayerLayer.Speed)) {
            for (key in listOf(PlayerKey.Left, PlayerKey.Right, PlayerKey.Up, PlayerKey.Down, PlayerKey.Center,
                PlayerKey.FastForward, PlayerKey.Rewind)) assertNull(PlayerKeyRouter.route(key, ctx.copy(layer = layer)))
            assertEquals(PlayerCommand.Pause, PlayerKeyRouter.route(PlayerKey.PlayPause, ctx.copy(layer = layer)))
        }
    }
}
