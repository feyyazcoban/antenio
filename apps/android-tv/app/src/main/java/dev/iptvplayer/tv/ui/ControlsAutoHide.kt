package dev.iptvplayer.tv.ui

import dev.iptvplayer.tv.data.OsdTimeout

/**
 * Whether the player's controls go on their own (playback.md §3.1): while a stream plays and the controls are on top,
 * once [timeout] has passed since the last key. They stay while a panel or the end card covers them, while the
 * connection reconnects or has failed, and while TalkBack reads the screen, which a hiding control would cut short;
 * "Hiçbir zaman" keeps them until Back. Pure, so the rules are tested on the JVM.
 */
internal object ControlsAutoHide {
    fun shouldHide(nowMs: Long, lastInteractionMs: Long, timeout: OsdTimeout, playing: Boolean, controlsOnTop: Boolean,
        failed: Boolean, recovering: Boolean, talkBack: Boolean, scrubbing: Boolean = false): Boolean {
        val after = timeout.ms ?: return false
        return playing && controlsOnTop && !failed && !recovering && !talkBack && !scrubbing && nowMs - lastInteractionMs > after
    }
}
