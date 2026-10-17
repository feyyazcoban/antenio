package dev.iptvplayer.tv.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import dev.iptvplayer.tv.R
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

/** A bounded reading area: arrows scroll the text, then leave at the edge; Back belongs to the dialog. */
@Composable internal fun RemoteTextReader(text: String, modifier: Modifier = Modifier, tag: String) {
    val scroll = rememberScrollState()
    val scope = rememberCoroutineScope()
    var focused by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val measurer = rememberTextMeasurer()
    // Untrimmed line boxes make every line, including the first and last, one pitch tall.
    val style = remember { TextStyle(fontFamily = Roboto, fontSize = 12.sp, lineHeight = 18.sp,
        lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None)) }
    // Measure the real pitch: font scaling can be non-linear, so 18.sp.toPx() is not exact.
    val line = remember(density.density, density.fontScale) {
        measurer.measure("A\nA", style).let { (it.getLineTop(1) - it.getLineTop(0)).roundToInt() }.coerceAtLeast(1)
    }
    val whole = stringResource(R.string.reader_whole)
    val start = stringResource(R.string.reader_start)
    val end = stringResource(R.string.reader_end)
    val scrolling = stringResource(R.string.reader_scrolling)
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.weight(1f, fill = false).fillMaxWidth()
            // Snap the text viewport to whole lines so no line is sliced at either edge.
            .layout { measurable, constraints ->
                val pad = 16.dp.roundToPx()
                val snapped = if (!constraints.hasBoundedHeight) constraints else constraints.copy(maxHeight =
                    (pad + ((constraints.maxHeight - pad) / line).coerceAtLeast(1) * line)
                        .coerceIn(constraints.minHeight, constraints.maxHeight))
                val placeable = measurable.measure(snapped)
                layout(placeable.width, placeable.height) { placeable.place(0, 0) }
            }
            .testTag(tag)
            .onFocusChanged { focused = it.isFocused }
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) false else {
                    val direction = when {
                        event.key == Key.DirectionDown && scroll.canScrollForward -> 1
                        event.key == Key.DirectionUp && scroll.canScrollBackward -> -1
                        else -> 0
                    }
                    if (direction == 0) false else {
                        scope.launch {
                            // About 65% of the visible text, rounded to whole lines.
                            val visibleLines = (scroll.viewportSize / line).coerceAtLeast(1)
                            val step = (visibleLines * .65f).roundToInt().coerceIn(1, maxOf(1, visibleLines - 1)) * line
                            val base = (scroll.value + line / 2) / line * line
                            scroll.scrollTo((base + direction * step).coerceIn(0, scroll.maxValue))
                        }
                        true
                    }
                }
            }.semantics { stateDescription = when {
                !scroll.canScrollForward && !scroll.canScrollBackward -> whole
                !scroll.canScrollBackward -> start
                !scroll.canScrollForward -> end
                else -> scrolling
            } }.focusable().border(TvLayout.Focus, if (focused) Ink.Mint else Color.Transparent, RoundedCornerShape(8.dp))
            .padding(8.dp)) {
            Text(text, Modifier.fillMaxWidth().verticalScroll(scroll), color = Ink.Muted, style = style)
        }
        if (scroll.maxValue > 0) Note(stringResource(R.string.reader_keys))
    }
}
