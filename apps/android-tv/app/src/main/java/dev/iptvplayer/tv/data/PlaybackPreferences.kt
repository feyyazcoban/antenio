package dev.iptvplayer.tv.data

/** Whether the screen's refresh rate follows the stream's frame rate: Media3's two strategies, off or only without a gap. */
enum class FrameRateMatching { OFF, SEAMLESS }

/**
 * How far ahead the player loads a stream, as DefaultLoadControl's durations: at least [minMs], up to [maxMs], [playMs]
 * before it starts and [rebufferMs] before it starts again after running dry. NORMAL is Media3's default. Media3's byte
 * limit stays, so a high bitrate fills less time than this and memory never runs out.
 */
enum class BufferPreset(val minMs: Int, val maxMs: Int, val playMs: Int, val rebufferMs: Int) {
    NORMAL(50_000, 50_000, 1_000, 2_000),
    LARGE(50_000, 60_000, 2_500, 5_000),
    MAX(60_000, 120_000, 5_000, 10_000)
}

/**
 * Which decoder opens a stream. Both fall back to another decoder on the device when the first cannot open it;
 * SOFTWARE_FIRST tries the software ones first, for a picture the hardware decoder garbles.
 */
enum class DecoderMode { AUTO, SOFTWARE_FIRST }

/** AUTO sends Dolby and DTS as they are where the sound system takes them; PCM decodes all audio on the device. */
enum class AudioOutput { AUTO, PCM }

/** SYSTEM follows Android's caption settings, as the player always did; APP uses the size, background and colour here. */
enum class SubtitleSource { SYSTEM, APP }
enum class SubtitleSize(val factor: Float) { SMALL(.8f), NORMAL(1f), LARGE(1.25f), XLARGE(1.5f) }
/** NONE draws a shadow under the letters instead. */
enum class SubtitleBackground { NONE, SEMI, SOLID }
enum class SubtitleColor { WHITE, YELLOW }

data class SubtitleStyle(val source: SubtitleSource = SubtitleSource.SYSTEM, val size: SubtitleSize = SubtitleSize.NORMAL,
    val background: SubtitleBackground = SubtitleBackground.SEMI, val color: SubtitleColor = SubtitleColor.WHITE)

/** How long the player's controls stay after the last key; NEVER keeps them until Back hides them. */
enum class OsdTimeout(val ms: Long?) { S4(4_000), S8(8_000), S15(15_000), S30(30_000), NEVER(null) }

/**
 * How much of the picture shows through the player's notes and panels. None is under [FLOOR]: on Ink.Background at 80 %
 * over a white frame, Ink.Muted still reads 5.1:1 and Ink.Text 8.4:1; at 75 % Muted falls under 4.5:1 (COLOR.md).
 */
enum class PanelOpacity(val alpha: Float) {
    OPAQUE(1f), TRANSLUCENT(.9f), SHEER(.8f);

    companion object { const val FLOOR = .8f }
}

/**
 * Settings → Oynatıcı (playback.md stage 1): how the player is made for a stream and how its controls behave, for the
 * whole TV. The defaults are what the player did before these settings, so a TV that never opens the page plays as it
 * did. The player reads them when it is made: a change holds from the next stream.
 */
data class PlaybackPreferences(
    val frameRate: FrameRateMatching = FrameRateMatching.SEAMLESS,
    val buffer: BufferPreset = BufferPreset.NORMAL,
    val decoder: DecoderMode = DecoderMode.AUTO,
    val audioOutput: AudioOutput = AudioOutput.AUTO,
    val tunneling: Boolean = false,
    val subtitle: SubtitleStyle = SubtitleStyle(),
    val osdTimeout: OsdTimeout = OsdTimeout.S8,
    val panelOpacity: PanelOpacity = PanelOpacity.TRANSLUCENT
)

/**
 * PlaybackPreferences as DeviceStore keeps them: one value per field, an enum by its name and a switch as a Boolean,
 * under DeviceKeys.playback. A value no build wrote, or of another type, reads as the default; nothing in it names a
 * source, an address or an account.
 */
internal object PlaybackPreferencesCodec {
    fun decode(read: (String) -> Any?): PlaybackPreferences {
        val defaults = PlaybackPreferences()
        fun <E : Enum<E>> pick(key: String, entries: List<E>, default: E): E =
            (read(key) as? String)?.let { name -> entries.firstOrNull { it.name == name } } ?: default
        val subtitle = defaults.subtitle
        return PlaybackPreferences(
            frameRate = pick("frameRate", FrameRateMatching.entries, defaults.frameRate),
            buffer = pick("buffer", BufferPreset.entries, defaults.buffer),
            decoder = pick("decoder", DecoderMode.entries, defaults.decoder),
            audioOutput = pick("audioOutput", AudioOutput.entries, defaults.audioOutput),
            tunneling = read("tunneling") as? Boolean ?: defaults.tunneling,
            subtitle = SubtitleStyle(pick("subtitle.source", SubtitleSource.entries, subtitle.source),
                pick("subtitle.size", SubtitleSize.entries, subtitle.size),
                pick("subtitle.background", SubtitleBackground.entries, subtitle.background),
                pick("subtitle.color", SubtitleColor.entries, subtitle.color)),
            osdTimeout = pick("osdTimeout", OsdTimeout.entries, defaults.osdTimeout),
            panelOpacity = pick("panelOpacity", PanelOpacity.entries, defaults.panelOpacity))
    }

    fun encode(p: PlaybackPreferences): Map<String, Any> = mapOf(
        "frameRate" to p.frameRate.name, "buffer" to p.buffer.name, "decoder" to p.decoder.name,
        "audioOutput" to p.audioOutput.name, "tunneling" to p.tunneling,
        "subtitle.source" to p.subtitle.source.name, "subtitle.size" to p.subtitle.size.name,
        "subtitle.background" to p.subtitle.background.name, "subtitle.color" to p.subtitle.color.name,
        "osdTimeout" to p.osdTimeout.name, "panelOpacity" to p.panelOpacity.name)
}
