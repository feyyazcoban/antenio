package dev.iptvplayer.tv.ui.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.iptvplayer.tv.R
import dev.iptvplayer.tv.data.AudioOutput
import dev.iptvplayer.tv.data.BufferPreset
import dev.iptvplayer.tv.data.DecoderMode
import dev.iptvplayer.tv.data.FrameRateMatching
import dev.iptvplayer.tv.data.OsdTimeout
import dev.iptvplayer.tv.data.PanelOpacity
import dev.iptvplayer.tv.data.PlaybackPreferences
import dev.iptvplayer.tv.data.SubtitleBackground
import dev.iptvplayer.tv.data.SubtitleColor
import dev.iptvplayer.tv.data.SubtitleSize
import dev.iptvplayer.tv.data.SubtitleSource
import dev.iptvplayer.tv.data.SubtitleStyle
import dev.iptvplayer.tv.data.UiText

internal val playerPage = SettingsRoute.Page("player")
private val subtitlesPage = SettingsRoute.Page("player-subtitles")

/** Oynatıcı and its Altyazı görünümü page (playback.md §3.3, §7). Nothing here asks for the PIN or for NEXTV Plus. */
internal val playerPageList: List<SettingsPageSpec> = listOf(
    SettingsPageSpec(playerPage.id, UiText.Res(SectionId.PLAYER.label), SectionId.PLAYER) { Player() },
    SettingsPageSpec(subtitlesPage.id, UiText.Res(R.string.settings_subtitles), SectionId.PLAYER) { Subtitles() },
)

/**
 * The episode countdown and what the viewer sees over the picture first, then how a stream is opened, which holds from
 * the next one. Settings never opens over the player, so every change here meets the next stream.
 */
@Composable private fun SettingsScope.Player() {
    val p = s.playback
    ToggleRow("auto-next", stringResource(R.string.settings_auto_next), stringResource(R.string.settings_auto_next_note),
        s.autoNextEpisode) { model.autoNextEpisode() }
    PageRow("subtitles", stringResource(R.string.settings_subtitles), subtitleSummary(p.subtitle), subtitlesPage,
        Modifier.testTag("player-setting:subtitles"))
    val never = stringResource(R.string.settings_osd_never_note)
    PlaybackRow("osd-timeout", R.string.settings_osd_timeout, OsdTimeout.entries, p.osdTimeout, OsdTimeout.entries.map { it.text() },
        OsdTimeout.entries.map { if (it == OsdTimeout.NEVER) never else null },
        description = stringResource(R.string.settings_osd_timeout_note)) { prefs, it -> prefs.copy(osdTimeout = it) }
    PlaybackRow("panel-opacity", R.string.settings_panel_opacity, PanelOpacity.entries, p.panelOpacity,
        PanelOpacity.entries.texts { it.label() }, PanelOpacity.entries.texts { it.note() },
        description = stringResource(R.string.settings_panel_opacity_note)) { prefs, it -> prefs.copy(panelOpacity = it) }
    Spacer(Modifier.height(6.dp))
    val nextStream = stringResource(R.string.settings_player_next_stream)
    NoteRow(nextStream)
    PlaybackRow("frame-rate", R.string.settings_frame_rate, FrameRateMatching.entries, p.frameRate,
        FrameRateMatching.entries.texts { it.label() }, FrameRateMatching.entries.texts { it.note() }, note = nextStream) { prefs, it ->
        prefs.copy(frameRate = it)
    }
    PlaybackRow("buffer", R.string.settings_buffer, BufferPreset.entries, p.buffer, BufferPreset.entries.texts { it.label() },
        BufferPreset.entries.texts { it.note() }, note = nextStream) { prefs, it -> prefs.copy(buffer = it) }
    PlaybackRow("decoder", R.string.settings_decoder, DecoderMode.entries, p.decoder, DecoderMode.entries.texts { it.label() },
        DecoderMode.entries.texts { it.note() }, note = nextStream) { prefs, it -> prefs.copy(decoder = it) }
    PlaybackRow("audio-output", R.string.settings_audio_output, AudioOutput.entries, p.audioOutput, AudioOutput.entries.texts { it.label() },
        AudioOutput.entries.texts { it.note() }, note = nextStream) { prefs, it -> prefs.copy(audioOutput = it) }
    ToggleRow("tunneling", stringResource(R.string.settings_tunneling), stringResource(R.string.settings_tunneling_note), p.tunneling,
        Modifier.testTag("player-setting:tunneling")) { model.playback { it.copy(tunneling = !it.tunneling) } }
}

/**
 * Where the subtitles' look comes from, then NEXTV's size, background and colour. Choosing one of those moves to NEXTV's
 * setting, as on Android's a size chosen here would change nothing; the note says so beforehand.
 */
@Composable private fun SettingsScope.Subtitles() {
    val style = s.playback.subtitle
    fun pick(change: (SubtitleStyle) -> SubtitleStyle) =
        model.playback { it.copy(subtitle = change(it.subtitle).copy(source = SubtitleSource.APP)) }
    SubtitleRow("subtitle-source", R.string.settings_subtitle_source, SubtitleSource.entries, style.source,
        SubtitleSource.entries.texts { it.label() }, SubtitleSource.entries.texts { it.note() }) {
        model.playback { prefs -> prefs.copy(subtitle = prefs.subtitle.copy(source = it)) }
    }
    SubtitleRow("subtitle-size", R.string.settings_subtitle_size, SubtitleSize.entries, style.size, SubtitleSize.entries.texts { it.label() }) { size ->
        pick { it.copy(size = size) }
    }
    SubtitleRow("subtitle-background", R.string.settings_subtitle_background, SubtitleBackground.entries, style.background,
        SubtitleBackground.entries.texts { it.label() }) { background -> pick { it.copy(background = background) } }
    SubtitleRow("subtitle-color", R.string.settings_subtitle_color, SubtitleColor.entries, style.color,
        SubtitleColor.entries.texts { it.label() }) { color -> pick { it.copy(color = color) } }
    NoteRow(stringResource(if (style.source == SubtitleSource.SYSTEM) R.string.settings_subtitle_system_hint else R.string.settings_subtitle_app_hint))
}

/** Each entry's text: [id] names its string. */
@Composable private fun <E> List<E>.texts(id: (E) -> Int): List<String> = map { stringResource(id(it)) }

/**
 * A ChoiceRow over a playback setting's [entries], named [names] and doing what [notes] say: the row reads [description],
 * or what the [chosen] one does, with its name at the end; its page lists each with what it does, under [note]. [change]
 * puts the picked one in the preferences.
 */
@Composable private fun <E : Enum<E>> SettingsScope.PlaybackRow(key: String, @StringRes label: Int, entries: List<E>, chosen: E,
    names: List<String>, notes: List<String?>, description: String? = null, note: String? = null,
    change: (PlaybackPreferences, E) -> PlaybackPreferences) {
    val index = entries.indexOf(chosen)
    ChoiceRow(key, stringResource(label), names, index, Modifier.testTag("player-setting:$key"),
        description = description ?: notes[index] ?: names[index], value = names[index], optionDescriptions = notes, note = note) { picked ->
        model.playback { change(it, entries[picked]) }
    }
}

/** A ChoiceRow of the subtitles page: the chosen one as its description, as the other choices in Settings read. */
@Composable private fun <E : Enum<E>> SettingsScope.SubtitleRow(key: String, @StringRes label: Int, entries: List<E>, chosen: E,
    names: List<String>, notes: List<String>? = null, choose: (E) -> Unit) {
    ChoiceRow(key, stringResource(label), names, entries.indexOf(chosen), Modifier.testTag("player-setting:$key"),
        optionDescriptions = notes) { picked -> choose(entries[picked]) }
}

/** "Android ayarı", or NEXTV's size, background and colour: "Büyük · Yarı saydam · Beyaz". */
@Composable private fun subtitleSummary(style: SubtitleStyle): String =
    if (style.source == SubtitleSource.SYSTEM) stringResource(R.string.settings_subtitle_system)
    else stringResource(R.string.text_pair, stringResource(R.string.text_pair, stringResource(style.size.label()),
        stringResource(style.background.label())), stringResource(style.color.label()))

@StringRes private fun FrameRateMatching.label() = when (this) {
    FrameRateMatching.OFF -> R.string.settings_frame_rate_off
    FrameRateMatching.SEAMLESS -> R.string.settings_frame_rate_seamless
}
@StringRes private fun FrameRateMatching.note() = when (this) {
    FrameRateMatching.OFF -> R.string.settings_frame_rate_off_note
    FrameRateMatching.SEAMLESS -> R.string.settings_frame_rate_seamless_note
}
@StringRes private fun BufferPreset.label() = when (this) {
    BufferPreset.NORMAL -> R.string.settings_buffer_normal
    BufferPreset.LARGE -> R.string.settings_buffer_large
    BufferPreset.MAX -> R.string.settings_buffer_max
}
@StringRes private fun BufferPreset.note() = when (this) {
    BufferPreset.NORMAL -> R.string.settings_buffer_normal_note
    BufferPreset.LARGE -> R.string.settings_buffer_large_note
    BufferPreset.MAX -> R.string.settings_buffer_max_note
}
@StringRes private fun DecoderMode.label() = when (this) {
    DecoderMode.AUTO -> R.string.settings_decoder_auto
    DecoderMode.SOFTWARE_FIRST -> R.string.settings_decoder_software
}
@StringRes private fun DecoderMode.note() = when (this) {
    DecoderMode.AUTO -> R.string.settings_decoder_auto_note
    DecoderMode.SOFTWARE_FIRST -> R.string.settings_decoder_software_note
}
@StringRes private fun AudioOutput.label() = when (this) {
    AudioOutput.AUTO -> R.string.settings_audio_auto
    AudioOutput.PCM -> R.string.settings_audio_pcm
}
@StringRes private fun AudioOutput.note() = when (this) {
    AudioOutput.AUTO -> R.string.settings_audio_auto_note
    AudioOutput.PCM -> R.string.settings_audio_pcm_note
}
@StringRes private fun PanelOpacity.label() = when (this) {
    PanelOpacity.OPAQUE -> R.string.settings_panel_opaque
    PanelOpacity.TRANSLUCENT -> R.string.settings_panel_translucent
    PanelOpacity.SHEER -> R.string.settings_panel_sheer
}
@StringRes private fun PanelOpacity.note() = when (this) {
    PanelOpacity.OPAQUE -> R.string.settings_panel_opaque_note
    PanelOpacity.TRANSLUCENT -> R.string.settings_panel_translucent_note
    PanelOpacity.SHEER -> R.string.settings_panel_sheer_note
}
@StringRes private fun SubtitleSource.label() = when (this) {
    SubtitleSource.SYSTEM -> R.string.settings_subtitle_system
    SubtitleSource.APP -> R.string.settings_subtitle_app
}
@StringRes private fun SubtitleSource.note() = when (this) {
    SubtitleSource.SYSTEM -> R.string.settings_subtitle_system_note
    SubtitleSource.APP -> R.string.settings_subtitle_app_note
}
@StringRes private fun SubtitleSize.label() = when (this) {
    SubtitleSize.SMALL -> R.string.settings_subtitle_small
    SubtitleSize.NORMAL -> R.string.settings_subtitle_normal
    SubtitleSize.LARGE -> R.string.settings_subtitle_large
    SubtitleSize.XLARGE -> R.string.settings_subtitle_xlarge
}
@StringRes private fun SubtitleBackground.label() = when (this) {
    SubtitleBackground.NONE -> R.string.settings_subtitle_background_none
    SubtitleBackground.SEMI -> R.string.settings_subtitle_background_semi
    SubtitleBackground.SOLID -> R.string.settings_subtitle_background_solid
}
@StringRes private fun SubtitleColor.label() = when (this) {
    SubtitleColor.WHITE -> R.string.settings_subtitle_white
    SubtitleColor.YELLOW -> R.string.settings_subtitle_yellow
}

/** The timeout's name: "8 saniye", or "Hiçbir zaman". */
@Composable private fun OsdTimeout.text(): String =
    ms?.let { val seconds = (it / 1000).toInt(); pluralStringResource(R.plurals.settings_osd_seconds, seconds, seconds.toString()) }
        ?: stringResource(R.string.settings_osd_never)
