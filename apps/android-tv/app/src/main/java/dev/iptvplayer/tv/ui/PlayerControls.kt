package dev.iptvplayer.tv.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.tv.material3.*
import dev.iptvplayer.tv.R
import dev.iptvplayer.tv.data.*
import java.time.ZoneId
import kotlin.math.*

internal enum class PlayerInfoKind { Full, Zap, Peek }

private val PlayerControlHeight = 48.dp
private val FocusLabelPadding = 4.dp
// Six dp clear of the outer focus ring, plus eight dp clear of the time row above.
private val FocusLabelButtonGap = TvLayout.FocusInset + TvLayout.Focus + 6.dp
private val FocusLabelStyle = TextStyle(fontFamily = Roboto, fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Normal)

@Composable private fun focusLabelSpace(): Dp {
    val height = rememberTextMeasurer().measure(AnnotatedString("Ag"), FocusLabelStyle, maxLines = 1).size.height
    return with(LocalDensity.current) { height.toDp() } + FocusLabelPadding * 2 + FocusLabelButtonGap + 8.dp
}

internal data class PlayerControl(val tag: String, val label: String, val symbol: String, val state: String = "",
    val enabled: Boolean = true, val primary: Boolean = false, val selected: Boolean = false,
    val requester: FocusRequester? = null, val onClick: () -> Unit)

/** One bottom block shared by the full controls, a channel band and the hidden-seek preview. */
@Composable internal fun PlayerInfoBlock(kind: PlayerInfoKind, entry: MediaEntry, info: PlayerInfoModel,
    number: Int?, schedule: GuideSlice?, zone: ZoneId, clock: PlaybackClock,
    scrub: ScrubHolder, speed: Float, playing: Boolean, status: String?, failed: Boolean,
    opening: Boolean, scrubFocus: FocusRequester, returnFocus: () -> Unit, toggle: () -> Unit,
    modifier: Modifier = Modifier, peekDelta: Long? = null, onScrubFocusChanged: (Boolean) -> Unit = {},
    controls: @Composable () -> Unit = {}) {
    val live = entry.kind == MediaKind.LIVE
    val large = LocalDensity.current.fontScale >= 1.3f
    Column(modifier.semantics { isTraversalGroup = true }) {
        if (kind != PlayerInfoKind.Peek) {
            if (!live) {
                Text(stringResource(if (entry.kind == MediaKind.EPISODE) R.string.player_kicker_series else R.string.player_kicker_movie),
                    Modifier.testTag("player-kind").border(1.dp, Ink.Outline.copy(alpha = .6f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 7.dp, vertical = 1.dp),
                    color = Ink.Text, fontFamily = Roboto, fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(6.dp))
            }
            Row(Modifier.fillMaxWidth().semantics(mergeDescendants = true) {}, verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (live) {
                    number?.let { Text(it.toString(), Modifier.widthIn(min = 28.dp), color = Ink.Muted,
                        fontFamily = Roboto, fontSize = 18.sp, textAlign = TextAlign.End) }
                    if (entry.artwork == null) Box(Modifier.size(56.dp, 34.dp).background(Ink.Container, RoundedCornerShape(6.dp)).clearAndSetSemantics {}, contentAlignment = Alignment.Center) {
                        Text(info.title.take(2).uppercase(appLocale), color = Ink.Muted, fontFamily = Roboto, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    } else Artwork(entry.sourceId, entry.artwork, info.title, Modifier.size(56.dp, 34.dp), logo = true, logoInset = 2.dp)
                }
                Text(info.title, Modifier.weight(1f).testTag("player-title"), color = Ink.Text, fontFamily = Roboto,
                    fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight.Medium,
                    maxLines = if (large) 2 else 1, overflow = TextOverflow.Ellipsis)
                if (live) LiveBadge()
                PlayerWallClock(clock)
            }
            Spacer(Modifier.height(if (live) 10.dp else 4.dp))
            PlayerMeta(entry, info, schedule, zone, clock, speed, playing, status, opening)
            Spacer(Modifier.height(12.dp))
        }
        if (live) ProgrammeBar(schedule, zone, clock) else PlayerScrubBar(clock, scrub, scrubFocus,
            enabled = kind == PlayerInfoKind.Full && !failed && !opening, interactive = kind == PlayerInfoKind.Full,
            peek = kind == PlayerInfoKind.Peek, returnFocus = returnFocus, toggle = toggle, peekDelta = peekDelta,
            onFocusChanged = onScrubFocusChanged)
        if (kind == PlayerInfoKind.Full) {
            Spacer(Modifier.height(focusLabelSpace()))
            controls()
        }
    }
}

@Composable private fun PlayerWallClock(clock: PlaybackClock) {
    val minute by remember(clock) { derivedStateOf { clock.now / 60_000 } }
    Text(guideTime(minute * 60_000, ZoneId.systemDefault()), Modifier.padding(start = 12.dp).testTag("player-clock"),
        color = Ink.Muted, fontFamily = Roboto, fontSize = 16.sp, lineHeight = 22.sp,
        style = TextStyle(fontFeatureSettings = "tnum"))
}

@Composable private fun LiveBadge() {
    Row(Modifier.testTag("player-live-badge").background(Ink.Background.copy(alpha = .72f), RoundedCornerShape(50))
        .padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(8.dp).background(Ink.Text, RoundedCornerShape(50)))
        Text(stringResource(R.string.player_live_short), color = Ink.Text, fontFamily = Roboto, fontSize = 12.sp,
            lineHeight = 16.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.sp)
    }
}

@Composable private fun PlayerMeta(entry: MediaEntry, info: PlayerInfoModel, schedule: GuideSlice?,
    zone: ZoneId, clock: PlaybackClock, speed: Float, playing: Boolean, status: String?, opening: Boolean) {
    val density = LocalDensity.current
    val end by remember(clock, speed, playing) { derivedStateOf {
        PlayerInfoModel.endMinute(clock.now, clock.position, clock.duration, speed, playing)
    } }
    val minute by remember(clock) { derivedStateOf { clock.now / 60_000 } }
    val live = entry.kind == MediaKind.LIVE
    val now = schedule?.now(minute * 60_000)
    val next = schedule?.next(minute * 60_000)
    val total = (info.durationSeconds * 1000).takeIf { it > 0 } ?: clock.duration
    val body = if (live) now?.let { programme -> listOfNotNull(programme.title,
        programme.end?.let { stringResource(R.string.player_remaining, clockTime((it - minute * 60_000).coerceAtLeast(0))) }).joinToString(" · ") }
        ?: stringResource(R.string.player_no_programme)
    else listOfNotNull(info.year.takeIf { it.isNotBlank() }, info.season?.let {
        stringResource(R.string.player_episode_order, it, info.episode ?: 1) }, info.episodeTitle,
        total.takeIf { it > 0 }?.let { runtimeText(it / 1000) }, end?.let { stringResource(R.string.player_ends_at, guideTime(it, ZoneId.systemDefault())) })
        .joinToString(" · ")
    val nextLabel = next?.let { stringResource(R.string.player_next_programme, guideTime(it.start, zone), it.title) }
    Column(Modifier.testTag("player-metadata")) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (opening && !live) LoadingArc()
            Text(status ?: body, Modifier.weight(1f).then(if (live) Modifier.testTag("player-current-programme") else Modifier),
                color = if (live || status != null) Ink.Text else Ink.Muted,
                fontFamily = Roboto, fontSize = 13.sp, lineHeight = 18.sp, maxLines = if (density.fontScale >= 1.3f) 2 else 1,
                overflow = TextOverflow.Ellipsis)
            if (live && status == null && nextLabel != null && density.fontScale < 1.3f) Text(nextLabel, Modifier.widthIn(max = 280.dp),
                color = Ink.Muted, fontFamily = Roboto, fontSize = 13.sp, lineHeight = 18.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (live && status == null && nextLabel != null && density.fontScale >= 1.3f && density.fontScale < 2f) Text(
            nextLabel,
            Modifier.fillMaxWidth(), color = Ink.Muted, fontFamily = Roboto, fontSize = 13.sp, lineHeight = 18.sp,
            maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.End)
    }
}

@Composable private fun ProgrammeBar(schedule: GuideSlice?, zone: ZoneId, clock: PlaybackClock) {
    val minute by remember(clock) { derivedStateOf { clock.now / 60_000 } }
    val programme = schedule?.now(minute * 60_000)
    val height = with(LocalDensity.current) { maxOf(28.dp, 26.sp.toDp()) }
    Row(Modifier.fillMaxWidth().height(height).clearAndSetSemantics {}, verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        if (programme?.end != null) {
            TimeLabel(guideTime(programme.start, zone), muted = true)
            Box(Modifier.weight(1f).height(4.dp).drawBehind {
                drawRoundRect(Ink.Text.copy(alpha = .24f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()))
                val ratio = ((clock.now - programme.start).toFloat() / (programme.end - programme.start)).coerceIn(0f, 1f)
                drawRect(Ink.Text.copy(alpha = .92f), size = Size(size.width * ratio, size.height))
            })
            TimeLabel(guideTime(programme.end, zone), muted = true)
        }
    }
}

@Composable private fun TimeLabel(value: String, modifier: Modifier = Modifier, muted: Boolean = false,
    align: TextAlign = TextAlign.End) {
    Text(value, modifier, color = if (muted) Ink.Muted else Ink.Text, fontFamily = Roboto, fontSize = 16.sp,
        lineHeight = 22.sp, fontWeight = if (muted) FontWeight.Normal else FontWeight.Medium, maxLines = 1,
        textAlign = align, style = TextStyle(fontFeatureSettings = "tnum"))
}

@Composable internal fun PlayerScrubBar(clock: PlaybackClock, scrub: ScrubHolder, requester: FocusRequester,
    enabled: Boolean, interactive: Boolean, peek: Boolean, returnFocus: () -> Unit, toggle: () -> Unit, peekDelta: Long? = null,
    onFocusChanged: (Boolean) -> Unit = {}) {
    var focused by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val height = with(density) { maxOf(28.dp, 26.sp.toDp()) }
    val label = stringResource(R.string.player_position)
    val backward = stringResource(R.string.player_seek_back_accessible)
    val forward = stringResource(R.string.player_seek_forward_accessible)
    val position = clock.position
    val duration = clock.duration
    val target = scrub.target
    val seekable = enabled && clock.seekable
    val measurer = rememberTextMeasurer()
    val labelWidth = with(density) { measurer.measure(AnnotatedString(if (duration >= 3_600_000) "−0:00:00" else "−00:00"),
        TextStyle(fontFamily = Roboto, fontSize = 16.sp, fontFeatureSettings = "tnum")).size.width.toDp() }
    val state = stringResource(R.string.player_position_total, spokenPlayerTime(position), spokenPlayerTime(duration))
    Box(Modifier.fillMaxWidth().height(height).testTag("player-time-row")) {
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            TimeLabel(clockTime(position), Modifier.width(labelWidth).clearAndSetSemantics { testTag = "player-time-elapsed" }, align = TextAlign.Start)
            Box(Modifier.weight(1f).fillMaxHeight().testTag("player-scrub").focusRequester(requester)
                .onFocusChanged { value ->
                    if (focused && !value.isFocused) scrub.commit()
                    if (focused != value.isFocused) onFocusChanged(value.isFocused)
                    focused = value.isFocused
                }.focusProperties { canFocus = interactive && (seekable || focused); up = FocusRequester.Cancel; down = FocusRequester.Cancel }
                .onPreviewKeyEvent { event ->
                    val key = event.nativeKeyEvent.keyCode
                    when (key) {
                        android.view.KeyEvent.KEYCODE_DPAD_LEFT, android.view.KeyEvent.KEYCODE_DPAD_RIGHT -> {
                            if (event.type == KeyEventType.KeyDown && seekable) scrub.nudge(PlayerKeyRouter.seekStep(event.nativeKeyEvent.repeatCount) *
                                if (key == android.view.KeyEvent.KEYCODE_DPAD_RIGHT) 1 else -1)
                            true
                        }
                        android.view.KeyEvent.KEYCODE_DPAD_CENTER, android.view.KeyEvent.KEYCODE_ENTER -> {
                            if (event.type == KeyEventType.KeyUp) { if (scrub.target != null) scrub.commit() else toggle() }; true
                        }
                        android.view.KeyEvent.KEYCODE_DPAD_DOWN -> { if (event.type == KeyEventType.KeyDown) { scrub.commit(); returnFocus() }; true }
                        android.view.KeyEvent.KEYCODE_DPAD_UP -> true
                        else -> false
                    }
                }.semantics {
                    contentDescription = label; stateDescription = state
                    progressBarRangeInfo = ProgressBarRangeInfo(position.toFloat().coerceIn(0f, duration.toFloat().coerceAtLeast(1f)), 0f..duration.toFloat().coerceAtLeast(1f))
                    if (interactive && seekable) {
                        setProgress { scrub.moveTo(it.toLong()) }
                        customActions = listOf(CustomAccessibilityAction(backward) { scrub.moveTo(clock.position - 10_000) },
                            CustomAccessibilityAction(forward) { scrub.moveTo(clock.position + 10_000) })
                    }
                }.focusable(interactive && (seekable || focused)).drawBehind {
                    val inset = 14.dp.toPx()
                    val trackWidth = (size.width - inset * 2).coerceAtLeast(0f)
                    fun x(ms: Long) = inset + trackWidth * (ms.toFloat() / clock.duration.coerceAtLeast(1)).coerceIn(0f, 1f)
                    val y = size.height / 2
                    val stroke = (if (focused) 6 else 4).dp.toPx()
                    if (clock.duration > 0) {
                        drawLine(Ink.Text.copy(alpha = .24f), Offset(inset, y), Offset(size.width - inset, y), stroke, StrokeCap.Round)
                        drawLine(Ink.Text.copy(alpha = .42f), Offset(inset, y), Offset(x(clock.buffered), y), stroke, StrokeCap.Round)
                        drawLine(if (focused) Ink.Mint else Ink.Text.copy(alpha = .92f), Offset(inset, y), Offset(x(clock.position), y), stroke, StrokeCap.Round)
                        scrub.target?.let {
                            drawLine(Ink.Mint.copy(alpha = .45f), Offset(x(scrub.origin), y), Offset(x(it), y), stroke)
                            drawLine(Ink.Text, Offset(x(scrub.origin), y - 8.dp.toPx()), Offset(x(scrub.origin), y + 8.dp.toPx()), 2.dp.toPx())
                        }
                        val at = Offset(x(scrub.target ?: clock.position), y)
                        if (clock.seekable) {
                            drawCircle(if (focused) Ink.Mint else Ink.Text, (if (focused) 9 else 6).dp.toPx(), at)
                            if (focused) drawCircle(Ink.Text, 13.dp.toPx(), at, style = Stroke(2.dp.toPx()))
                        }
                    }
                })
            TimeLabel(if (duration > 0) "−${clockTime((duration - position).coerceAtLeast(0))}" else "—",
                Modifier.width(labelWidth).clearAndSetSemantics { testTag = "player-time-remaining" }, muted = true)
        }
        if (focused || target != null || peek) {
            val value = target ?: position
            val delta = target?.let { it - scrub.origin } ?: peekDelta
            Layout(content = {
                Row(Modifier.testTag("player-scrub-bubble").clearAndSetSemantics {}
                    .background(Ink.Background.copy(alpha = .94f), RoundedCornerShape(10.dp)).drawBehind {
                        val arrow = Path().apply { moveTo(size.width / 2 - 6.dp.toPx(), size.height); lineTo(size.width / 2, size.height + 6.dp.toPx());
                            lineTo(size.width / 2 + 6.dp.toPx(), size.height); close() }
                        drawPath(arrow, Ink.Background.copy(alpha = .94f))
                    }.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TimeLabel(clockTime(value))
                    if (delta != null) Text((if (delta >= 0) "+" else "−") + clockTime(abs(delta)), color = Ink.Mint,
                        fontFamily = Roboto, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                }
            }, modifier = Modifier.fillMaxSize()) { children, constraints ->
                val bubble = children.single().measure(constraints.copy(minWidth = 0, minHeight = 0, maxHeight = Constraints.Infinity))
                val margin = (labelWidth + 30.dp).roundToPx()
                val x = margin + (constraints.maxWidth - margin * 2) * (value.toFloat() / duration.coerceAtLeast(1)).coerceIn(0f, 1f)
                layout(constraints.maxWidth, constraints.maxHeight) {
                    bubble.place((x - bubble.width / 2).roundToInt().coerceIn(0, (constraints.maxWidth - bubble.width).coerceAtLeast(0)),
                        constraints.maxHeight / 2 - 22.dp.roundToPx() - bubble.height)
                }
            }
        }
        scrub.announcement?.let { at ->
            val text = stringResource(R.string.player_position_applied, spokenPlayerTime(at))
            Box(Modifier.size(0.dp).semantics { contentDescription = text; liveRegion = LiveRegionMode.Polite })
        }
    }
}

@Composable internal fun PlayerControlRow(cluster: List<PlayerControl>, chips: List<PlayerControl>, scrub: FocusRequester,
    seekable: Boolean, rememberControl: (FocusRequester) -> Unit, hint: String) {
    val density = LocalDensity.current
    val measurer = rememberTextMeasurer()
    val style = TextStyle(fontFamily = Roboto, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    BoxWithConstraints(Modifier.fillMaxWidth().height(PlayerControlHeight).testTag("player-controls")) {
        val textWidths = remember(chips.map { it.label }, cluster.map { it.label }, density.fontScale, density.density) {
            (cluster + chips).associate { it.tag to measurer.measure(AnnotatedString(it.label), style).size.width }
        }
        val iconSize = with(density) { maxOf(44.dp, 20.sp.toDp() + 16.dp) }
        val large = density.fontScale >= 2f
        val clusterSizes = cluster.map { if (it.primary) PlayerControlHeight else if (it.tag in listOf("player-next-episode", "player-channels") && !large)
            with(density) { (textWidths.getValue(it.tag)).toDp() } + 60.dp else 44.dp }
        val clusterWidth = clusterSizes.fold(0.dp) { sum, value -> sum + value } + 12.dp * (cluster.size - 1).coerceAtLeast(0)
        val hintParts = hint.split(" · ")
        val hintWidths = remember(hint, density.fontScale, density.density) { hintParts.mapIndexed { i, part ->
            measurer.measure(AnnotatedString((if (i > 0) " · " else "") + part), TextStyle(fontFamily = Roboto, fontSize = 13.sp)).size.width
        } }
        val plan = with(density) { ControlRowPlan.fit(maxWidth.roundToPx(), clusterWidth.roundToPx(), chips.map { item ->
            ControlRowPlan.Chip(item.tag, if (item.tag == "player-tracks") textWidths.getValue(item.tag) + 60.dp.roundToPx() else iconSize.roundToPx(),
                iconSize.roundToPx(), 0)
        }, 8.dp.roundToPx(), 48.dp.roundToPx(), if (density.fontScale >= 1.3f) emptyList() else hintWidths, large) }
        val chipWidths = chips.map { if (it.tag in plan.labels) with(density) { textWidths.getValue(it.tag).toDp() } + 60.dp else iconSize }
        Layout(content = {
            cluster.forEachIndexed { index, control -> key(control.tag) { PlayerControlButton(control, clusterSizes[index], if (control.primary) PlayerControlHeight else 44.dp,
                labelled = control.tag in listOf("player-next-episode", "player-channels") && !large, chip = control.tag == "player-channels", scrub, seekable, rememberControl) } }
            chips.forEachIndexed { index, control -> key(control.tag) { PlayerControlButton(control, chipWidths[index], iconSize,
                labelled = control.tag in plan.labels, chip = true, scrub, seekable, rememberControl) } }
            Text(hintParts.take(plan.hints).joinToString(" · "), Modifier.testTag("player-hint").clearAndSetSemantics {}, color = Ink.Muted,
                fontFamily = Roboto, fontSize = 13.sp, lineHeight = 18.sp, maxLines = 1, overflow = TextOverflow.Clip)
        }) { children, constraints ->
            val clusterPlace = cluster.indices.map { i -> children[i].measure(Constraints.fixed(clusterSizes[i].roundToPx(), if (cluster[i].primary) PlayerControlHeight.roundToPx() else 44.dp.roundToPx())) }
            val chipPlace = chips.indices.map { i -> children[cluster.size + i].measure(Constraints.fixed(chipWidths[i].roundToPx(), iconSize.roundToPx())) }
            val chipsTotal = chipPlace.sumOf { it.width } + 8.dp.roundToPx() * (chips.size - 1).coerceAtLeast(0)
            val leftEnd = clusterPlace.sumOf { it.width } + 12.dp.roundToPx() * (cluster.size - 1).coerceAtLeast(0)
            val hintWidth = (constraints.maxWidth - leftEnd - chipsTotal - 48.dp.roundToPx()).coerceAtLeast(0)
            val hintPlace = children.last().measure(Constraints(maxWidth = hintWidth, maxHeight = constraints.maxHeight))
            layout(constraints.maxWidth, constraints.maxHeight) {
                var x = 0
                clusterPlace.forEach { it.place(x, (constraints.maxHeight - it.height) / 2); x += it.width + 12.dp.roundToPx() }
                x = constraints.maxWidth - chipsTotal
                chipPlace.forEach { it.place(x, (constraints.maxHeight - it.height) / 2); x += it.width + 8.dp.roundToPx() }
                if (density.fontScale < 1.3f && plan.hints > 0) hintPlace.place(leftEnd + 24.dp.roundToPx(), (constraints.maxHeight - hintPlace.height) / 2)
            }
        }
    }
}

@Composable private fun PlayerControlButton(control: PlayerControl, width: Dp, height: Dp, labelled: Boolean, chip: Boolean,
    scrub: FocusRequester, seekable: Boolean, rememberControl: (FocusRequester) -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val ownRequester = remember { FocusRequester() }
    val requester = control.requester ?: ownRequester
    val name = if (labelled) control.state else listOf(control.label, control.state).filter { it.isNotBlank() }.joinToString(" · ")
    val showLabel = name.isNotBlank() && (!control.primary || control.symbol !in listOf("play_arrow", "pause"))
    Box(Modifier.size(width, height)) {
        Button(control.onClick, Modifier.fillMaxSize().testTag(control.tag).focusRequester(requester)
            .onFocusChanged { focused = it.isFocused; if (it.isFocused) rememberControl(requester) }
            .enterWhenEnabled(control.enabled, focused).focusProperties { up = if (seekable) scrub else FocusRequester.Cancel; down = FocusRequester.Cancel }
            .semantics { contentDescription = control.label; stateDescription = control.state; if (control.tag == "player-favorite") selected = control.selected },
            enabled = control.enabled, shape = ButtonDefaults.shape(RoundedCornerShape(50)),
            colors = FilledControl.colors(if (control.selected) Ink.Selected else Ink.High.copy(alpha = .72f), if (control.primary || control.selected) Ink.Mint else Ink.Text),
            border = FilledControl.border(), scale = FilledControl.scale(), contentPadding = PaddingValues(0.dp)) {
            // TV Button's minimum content row is shorter than these circles. Fill the actual
            // control so the icon and any label share its center, not the minimum row's center.
            Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
                PlayerSymbol(control.symbol, LocalContentColor.current,
                    Modifier.size(if (control.primary) 24.dp else if (chip) 20.dp else 22.dp).testTag("${control.tag}-symbol"))
                if (labelled) Text(control.label, color = LocalContentColor.current, fontFamily = Roboto,
                    fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium, maxLines = 1)
            }
        }
        if (focused && showLabel) Layout(content = {
            Text(name, Modifier.testTag("player-focus-label").clearAndSetSemantics {}
                .background(Ink.Background.copy(alpha = .94f), RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = FocusLabelPadding),
                color = Ink.Text, style = FocusLabelStyle, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }) { children, _ ->
            val label = children.single().measure(Constraints(maxWidth = 360.dp.roundToPx()))
            val rowOffset = (PlayerControlHeight.roundToPx() - height.roundToPx()) / 2
            layout(0, 0) { label.place(if (chip) width.roundToPx() - label.width else 0,
                -label.height - FocusLabelButtonGap.roundToPx() - rowOffset) }
        }
    }
}

@Composable private fun spokenPlayerTime(ms: Long): String {
    val total = (ms.coerceAtLeast(0) / 1000).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    val hours = total / 3600; val minutes = total / 60 % 60; val seconds = total % 60
    return listOfNotNull(
        hours.takeIf { it > 0 }?.let { pluralStringResource(R.plurals.player_hours, it, it.toString()) },
        minutes.takeIf { it > 0 }?.let { pluralStringResource(R.plurals.player_minutes, it, it.toString()) },
        seconds.takeIf { it > 0 || total == 0 }?.let { pluralStringResource(R.plurals.player_seconds, it, it.toString()) }
    ).joinToString(" ")
}
