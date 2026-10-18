package dev.iptvplayer.tv.ui

import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import dev.iptvplayer.tv.PlaybackMode
import java.text.NumberFormat
import java.util.Locale

/** Session-only VOD speed; no preference writes, media restart or second stream. */
internal object PlaybackSpeed {
    val choices = listOf(.5f, .75f, 1f, 1.25f, 1.5f, 1.75f, 2f)

    fun apply(player: Player, mode: PlaybackMode, speed: Float, tunneling: Boolean): Boolean {
        if (mode != PlaybackMode.VOD || tunneling || speed !in choices ||
            !player.isCommandAvailable(Player.COMMAND_SET_SPEED_AND_PITCH)) return false
        // Media3 PlaybackParameters(speed) time-stretches audio and keeps pitch at 1.
        player.playbackParameters = PlaybackParameters(speed)
        return true
    }

    fun label(speed: Float, locale: Locale = appLocale): String =
        NumberFormat.getNumberInstance(locale).apply { maximumFractionDigits = 2 }.format(speed) + "×"

    /** Remaining wall time, not media time; an unknown/finished timeline has no predicted end. */
    fun remainingMs(position: Long, duration: Long, speed: Float): Long? =
        if (duration <= 0 || position >= duration || !speed.isFinite() || speed <= 0) null
        else ((duration - position.coerceAtLeast(0)).toDouble() / speed).toLong()
}
