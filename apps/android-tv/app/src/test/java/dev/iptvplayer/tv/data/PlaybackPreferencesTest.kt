@file:androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)

package dev.iptvplayer.tv.data

import androidx.media3.exoplayer.DefaultLoadControl
import dev.iptvplayer.tv.ui.Ink
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.pow

class PlaybackPreferencesTest {
    private val changed = PlaybackPreferences(FrameRateMatching.OFF, BufferPreset.MAX, DecoderMode.SOFTWARE_FIRST, AudioOutput.PCM,
        tunneling = true, SubtitleStyle(SubtitleSource.APP, SubtitleSize.SMALL, SubtitleBackground.SOLID, SubtitleColor.YELLOW),
        OsdTimeout.S30, PanelOpacity.OPAQUE)

    @Test fun theDefaultsAreWhatThePlayerDidBefore() {
        val p = PlaybackPreferences()
        // Media3's own strategy, load control and decoders, Android's captions, and the 8 s and 90 % the player had.
        assertEquals(FrameRateMatching.SEAMLESS, p.frameRate)
        assertEquals(listOf(DefaultLoadControl.DEFAULT_MIN_BUFFER_MS, DefaultLoadControl.DEFAULT_MAX_BUFFER_MS,
            DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS, DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS),
            p.buffer.let { listOf(it.minMs, it.maxMs, it.playMs, it.rebufferMs) })
        assertEquals(DecoderMode.AUTO, p.decoder); assertEquals(AudioOutput.AUTO, p.audioOutput); assertFalse(p.tunneling)
        assertEquals(SubtitleSource.SYSTEM, p.subtitle.source)
        assertEquals(8_000L, p.osdTimeout.ms)
        assertEquals(.9f, p.panelOpacity.alpha)
        // Nothing stored reads as the defaults.
        assertEquals(p, PlaybackPreferencesCodec.decode { null })
    }

    @Test fun everyFieldIsKeptAndReadBack() {
        assertEquals(changed, PlaybackPreferencesCodec.decode(PlaybackPreferencesCodec.encode(changed)::get))
        val memory = MemoryPreferences()
        val store = DeviceStore(memory)
        store.playback = changed
        assertEquals(changed, store.playback)
        // One key per field under player., each an enum's name or a switch: no address, no account.
        assertEquals(11, memory.values.size)
        assertTrue(memory.values.keys.all { it.startsWith(DeviceKeys.playback.name) })
        assertTrue(memory.values.values.all { it is String || it is Boolean })
        store.playback = PlaybackPreferences()
        assertEquals(PlaybackPreferences(), store.playback)
    }

    @Test fun aValueNoBuildWroteReadsAsTheDefault() {
        val stored = PlaybackPreferencesCodec.encode(changed).toMutableMap<String, Any>()
        stored["buffer"] = "HUGE"; stored["tunneling"] = "yes"; stored["osdTimeout"] = 4; stored["subtitle.size"] = "large"
        val read = PlaybackPreferencesCodec.decode(stored::get)
        assertEquals(BufferPreset.NORMAL, read.buffer); assertFalse(read.tunneling)
        assertEquals(OsdTimeout.S8, read.osdTimeout); assertEquals(SubtitleSize.NORMAL, read.subtitle.size)
        // The rest stays as written.
        assertEquals(changed.decoder, read.decoder); assertEquals(changed.subtitle.color, read.subtitle.color)
    }

    @Test fun everyBufferSatisfiesMediaThreesRules() {
        for (preset in BufferPreset.entries) {
            assertTrue(preset.name, preset.minMs >= preset.playMs && preset.minMs >= preset.rebufferMs && preset.maxMs >= preset.minMs)
            // DefaultLoadControl refuses durations that disagree.
            DefaultLoadControl.Builder().setBufferDurationsMsForStreaming(preset.minMs, preset.maxMs, preset.playMs, preset.rebufferMs).build()
        }
        // Each one loads further ahead than the one before.
        assertEquals(BufferPreset.entries, BufferPreset.entries.sortedBy { it.maxMs })
    }

    @Test fun onlyNeverKeepsTheControlsUp() {
        assertEquals(listOf(4_000L, 8_000L, 15_000L, 30_000L), OsdTimeout.entries.mapNotNull { it.ms })
        assertNull(OsdTimeout.NEVER.ms)
    }

    @Test fun noPanelOpacityLetsTheMutedTextFallUnderItsContrast() {
        fun channel(value: Float) = if (value <= .03928f) value / 12.92f else ((value + .055f) / 1.055f).pow(2.4f)
        fun luminance(r: Float, g: Float, b: Float) = .2126f * channel(r) + .7152f * channel(g) + .0722f * channel(b)
        // Ink.Background at [alpha] over a white frame, the worst picture behind a panel, against Ink.Muted.
        fun contrast(alpha: Float): Float {
            fun over(c: Float) = c * alpha + (1 - alpha)
            val background = luminance(over(Ink.Background.red), over(Ink.Background.green), over(Ink.Background.blue))
            val text = luminance(Ink.Muted.red, Ink.Muted.green, Ink.Muted.blue)
            return (maxOf(background, text) + .05f) / (minOf(background, text) + .05f)
        }
        for (opacity in PanelOpacity.entries) {
            assertTrue(opacity.name, opacity.alpha >= PanelOpacity.FLOOR)
            assertTrue("${opacity.name}: ${contrast(opacity.alpha)}", contrast(opacity.alpha) >= 4.5f)
        }
        // The floor is where it is because a little more picture would already be too much.
        assertTrue(contrast(.75f) < 4.5f)
        assertEquals(PanelOpacity.FLOOR, PanelOpacity.entries.minOf { it.alpha })
    }
}
