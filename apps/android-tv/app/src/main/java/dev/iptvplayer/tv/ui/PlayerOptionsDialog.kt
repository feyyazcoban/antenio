@file:androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
package dev.iptvplayer.tv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.BringIntoViewSpec
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.tv.material3.Text
import dev.iptvplayer.tv.R
import dev.iptvplayer.tv.data.PanelOpacity
import dev.iptvplayer.tv.data.SubtitleSize
import dev.iptvplayer.tv.data.SubtitleSource
import dev.iptvplayer.tv.data.SubtitleStyle
import kotlinx.coroutines.flow.first

/** Same panel surface as Ses / Altyazı; Back and media keys remain PlayerKeyRouter's. */
@OptIn(ExperimentalFoundationApi::class)
@Composable internal fun PlayerOptionsDialog(picking: Boolean, speed: Float, opacity: PanelOpacity,
    enabled: Boolean, unavailable: String, openSpeeds: () -> Unit, choose: (Float) -> Unit,
    dismiss: () -> Unit, key: (KeyEvent) -> Boolean,
    resizeMode: Int = AspectRatioFrameLayout.RESIZE_MODE_FIT,
    chooseResize: (Int) -> Unit = {},
    subtitleStyle: SubtitleStyle = SubtitleStyle(),
    chooseSubtitleSource: (SubtitleSource) -> Unit = {},
    chooseSubtitleSize: (SubtitleSize) -> Unit = {},
    streamInfo: String? = null) {
    val edge = with(LocalDensity.current) { 8.dp.toPx() }
    val visibility = remember(edge) { object : BringIntoViewSpec {
        override fun calculateScrollDistance(offset: Float, size: Float, containerSize: Float): Float = when {
            size > containerSize - 2 * edge -> offset - edge
            offset < edge -> offset - edge
            offset + size > containerSize - edge -> offset + size - containerSize + edge
            else -> 0f
        }
    } }
    androidx.compose.runtime.key(picking) {
        val first = remember { FocusRequester() }
        // Keep the initial target stable while the engine reports an adjusted speed.
        val initial = remember { PlaybackSpeed.choices.minBy { kotlin.math.abs(it - speed) } }
        Dialog(onDismissRequest = dismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
            val window = LocalWindowInfo.current
            val view = LocalView.current
            if (opacity.alpha < 1f) SideEffect { (view.parent as? DialogWindowProvider)?.window?.setDimAmount(0f) }
            LaunchedEffect(Unit) { snapshotFlow { window.isWindowFocused }.first { it }; first.requestFocus() }
            Box(Modifier.fillMaxSize().onPreviewKeyEvent(key).focusProperties { onEnter = { first.requestFocus() } }.focusGroup(),
                contentAlignment = Alignment.CenterEnd) {
                Column(Modifier.width(440.dp).fillMaxHeight().testTag("player-options-panel")
                    .background(Ink.Background.copy(alpha = opacity.alpha), RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp))) {
                    Row(Modifier.fillMaxWidth().padding(28.dp), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(stringResource(if (picking) R.string.player_speed else R.string.player_options), Modifier.weight(1f),
                            color = Ink.Text, fontFamily = Roboto, fontWeight = FontWeight.Medium, fontSize = 24.sp, lineHeight = 30.sp)
                        Action(stringResource(R.string.action_back), (if (!enabled) Modifier.focusRequester(first) else Modifier)
                            .testTag("player-options-back"), onClick = dismiss)
                    }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(Ink.Divider))
                    CompositionLocalProvider(LocalBringIntoViewSpec provides visibility) {
                    Column(Modifier.weight(1f).fillMaxWidth().testTag("player-options-viewport")
                        .verticalScroll(rememberScrollState()).padding(28.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        if (picking) {
                            PlaybackSpeed.choices.forEach { value ->
                                val label = PlaybackSpeed.label(value)
                                RadioRow(if (value == 1f) stringResource(R.string.player_speed_normal, label) else label,
                                    selected = speed == value, modifier = (if (value == initial) Modifier.focusRequester(first) else Modifier)
                                        .testTag("player-speed:$value")) { choose(value) }
                            }
                            Note(stringResource(R.string.player_speed_note))
                        } else {
                            Action(stringResource(R.string.player_speed_value, PlaybackSpeed.label(speed)),
                                (if (enabled) Modifier.focusRequester(first) else Modifier).testTag("player-speed-open"),
                                enabled = enabled, onClick = openSpeeds)
                            if (!enabled) Note(unavailable)
                            else Note(stringResource(R.string.player_speed_note))
                            Spacer(Modifier.height(4.dp))
                            Text(stringResource(R.string.player_aspect_title), color = Ink.Mint, fontFamily = Roboto, fontSize = 12.sp)
                            listOf(
                                AspectRatioFrameLayout.RESIZE_MODE_FIT to R.string.player_aspect_fit,
                                AspectRatioFrameLayout.RESIZE_MODE_ZOOM to R.string.player_aspect_zoom,
                                AspectRatioFrameLayout.RESIZE_MODE_FILL to R.string.player_aspect_fill,
                            ).forEach { (mode, labelRes) ->
                                RadioRow(stringResource(labelRes), selected = resizeMode == mode,
                                    modifier = Modifier.testTag("player-aspect:$mode")) { chooseResize(mode) }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(stringResource(R.string.settings_subtitle_size), color = Ink.Mint, fontFamily = Roboto, fontSize = 12.sp)
                            RadioRow(stringResource(R.string.settings_subtitle_system), selected = subtitleStyle.source == SubtitleSource.SYSTEM,
                                modifier = Modifier.testTag("player-subtitle:SYSTEM")) { chooseSubtitleSource(SubtitleSource.SYSTEM) }
                            listOf(
                                SubtitleSize.SMALL to R.string.settings_subtitle_small,
                                SubtitleSize.NORMAL to R.string.settings_subtitle_normal,
                                SubtitleSize.LARGE to R.string.settings_subtitle_large,
                                SubtitleSize.XLARGE to R.string.settings_subtitle_xlarge,
                            ).forEach { (size, labelRes) ->
                                RadioRow(stringResource(labelRes), selected = subtitleStyle.source == SubtitleSource.APP && subtitleStyle.size == size,
                                    modifier = Modifier.testTag("player-subtitle:${size.name}")) { chooseSubtitleSize(size) }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(stringResource(R.string.player_stream_info), color = Ink.Mint, fontFamily = Roboto, fontSize = 12.sp)
                            Note(streamInfo ?: stringResource(R.string.player_stream_info_empty),
                                modifier = Modifier.testTag("player-stream-info"))
                        }
                    }
                    }
                }
            }
        }
    }
}
