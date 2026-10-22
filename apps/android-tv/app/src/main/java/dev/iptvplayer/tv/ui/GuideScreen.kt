package dev.iptvplayer.tv.ui

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.focus.*
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import dev.iptvplayer.tv.*
import dev.iptvplayer.tv.R
import dev.iptvplayer.tv.data.*
import kotlinx.coroutines.delay
import java.time.*
import java.util.Locale

/** "20:00 — 21:30", with dates or offsets where they change; [unknownEnd] stands for an end the guide does not give. */
internal fun programmeTime(programme: EpgProgramme, zone: ZoneId, unknownEnd: String, locale: Locale = appLocale): String {
    val start = Instant.ofEpochMilli(programme.start).atZone(zone)
    val end = programme.end?.let { Instant.ofEpochMilli(it).atZone(zone) }
    val dates = end != null && start.toLocalDate() != end.toLocalDate()
    val offsets = end != null && start.offset != end.offset
    fun label(time: Long) = (if (dates) guideDayTime(time, zone, locale) else guideTime(time, zone, locale)) +
        if (offsets) " (${guideOffset(time, zone)})" else ""
    return label(programme.start) + " — " + (programme.end?.let(::label) ?: unknownEnd)
}

@Composable @ReadOnlyComposable
internal fun programmeTime(programme: EpgProgramme, zone: ZoneId): String =
    programmeTime(programme, zone, stringResource(R.string.guide_end_unknown))

@Composable internal fun guideClock(): Long {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { now = System.currentTimeMillis(); delay(15_000) } }
    return now
}

@Composable internal fun guideRows(entries: List<MediaEntry>, window: GuideWindow, s: TvState, model: TvViewModel): Map<String, GuideSlice> {
    val owners = remember(entries) { entries.map { it.sourceId }.distinct() }
    LaunchedEffect(owners, s.guideRevision, s.guideZoneId) { model.ensureGuide(owners) }
    val rows by produceState<Map<String, GuideSlice>>(emptyMap(), entries, window, s.guideRevision) {
        value = model.guideRows(entries, window)
    }
    return rows
}

/** Why a channel shows no programme at a time. */
@StringRes internal fun guideEmptyText(status: GuideStatus?, slice: GuideSlice?): Int = when {
    status?.refreshing == true && status.info == null -> R.string.guide_loading
    status?.error != null && status.info == null -> R.string.guide_unavailable
    status?.configured != true -> R.string.guide_no_address
    slice?.match?.kind == EpgMatchKind.AMBIGUOUS -> R.string.guide_match_ambiguous
    slice?.match?.kind == EpgMatchKind.MISSING_ID -> R.string.guide_match_missing_id
    slice?.match?.kind == EpgMatchKind.NONE -> R.string.guide_match_none
    status.info == null -> R.string.guide_no_info
    else -> R.string.guide_no_programme_now
}

@Composable internal fun GuideScreen(s: TvState, model: TvViewModel, back: () -> Unit, settings: () -> Unit) {
    val now = guideClock()
    val zone = model.guideZone()
    val density = LocalDensity.current
    val channels = remember(s.catalog) { s.catalog.items.filter { it.kind == MediaKind.LIVE && !it.adult } }
    // Canlı TV's numbers ("Tüm kanallar" order), so a number means the same channel on both screens.
    val numbers = remember(channels) { catalogDisplayOrder(channels).withIndex().associate { (i, e) -> e.id to i + 1 } }
    // Every channel's source has reported, and none has a guide: one explanation instead of a grid of empty rows (U9).
    val owners = remember(channels) { channels.map { it.sourceId }.distinct() }
    val guideless = channels.isNotEmpty() && owners.all { it in s.guideStatus } && owners.none { s.guideStatus[it]?.configured == true }
    val emptyFocus = remember { FocusRequester() }
    var menuFocused by remember { mutableStateOf(false) }
    val retention = remember(now / 60_000, zone) { GuideWindow.around(Instant.ofEpochMilli(now), zone) }
    val anchor = (s.guideAnchor ?: now).coerceIn(retention.from, retention.until - 1)
    val day = Instant.ofEpochMilli(anchor).atZone(zone).toLocalDate()
    val dayWindow = remember(day, zone) { GuideWindow(day.atStartOfDay(zone).toInstant().toEpochMilli(), day.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()) }
    val selectedIndex = channels.indexOfFirst { it.id == s.guideChannelId }.coerceAtLeast(0)
    val channel = channels.getOrNull(selectedIndex)
    val list = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex)
    val visible by remember { derivedStateOf { list.layoutInfo.visibleItemsInfo.map { it.index } } }
    // A locked channel's programmes are not read until a PIN opens it: its row is one "Kilitli kanal" cell (pin.md §4).
    val requested = remember(channels, visible, selectedIndex, s.locks) {
        (visible + (selectedIndex - 2..selectedIndex + 2)).distinct().sorted().mapNotNull { channels.getOrNull(it) }
            .filterNot { s.locks.asksPin(it.id) }
    }
    val rows = guideRows(requested, dayWindow, s, model)
    val allSlots = remember(rows, dayWindow) { rows.mapValues { guideSlots(it.value.programmes, dayWindow) } }
    val emptySlots = remember(dayWindow) { guideSlots(emptyList(), dayWindow) }
    val slots = channel?.let { allSlots[it.id] } ?: emptySlots
    val selectedSlot = slots.firstOrNull { it.contains(anchor) }
    val gridFocus = remember { FocusRequester() }
    val navigation = remember { FocusRequester() }
    val nowFocus = remember { FocusRequester() }
    var gridFocused by remember { mutableStateOf(false) }
    var detail by remember { mutableStateOf<Pair<MediaEntry, GuideSlot>?>(null) }
    var zones by remember { mutableStateOf(false) }
    val zoneFocus = remember { FocusRequester() }
    val hour = 3_600_000L
    val duration = if (density.fontScale >= 1.5f) 2 * hour else 3 * hour
    // Whole hours in the guide zone (as dayWindow uses local midnight), so half-hour zones keep :00 labels.
    val hourStart = Instant.ofEpochMilli(anchor).atZone(zone).truncatedTo(java.time.temporal.ChronoUnit.HOURS).toInstant().toEpochMilli()
    val viewportFrom = hourStart.coerceIn(dayWindow.from, dayWindow.until - duration)
    val viewport = GuideWindow(viewportFrom, viewportFrom + duration)
    val channelWidth = (if (density.fontScale >= 1.5f) 232 else 210).dp
    // 50 dp rows show six channels at once at normal text (four at 65 dp before U9).
    val rowHeight = (if (density.fontScale >= 1.5f) 90 else 50).dp
    val zoneName = if (s.guideZoneId.isEmpty()) stringResource(R.string.guide_tv_time) else zone.id

    fun select(id: String?, time: Long) { model.guideCursor(id, time.coerceIn(retention.from, retention.until - 1)) }
    // A locked channel asks for the PIN over the guide; focus goes back to the grid (pin.md §3.6).
    val openPin = rememberPinOpener()
    // Kaynakları yönet asks for the PIN that guards the sources first (pin.md §3.8); focus comes back to its button.
    val sourcesFocus = remember { FocusRequester() }
    val openSourcesFocus = remember { FocusRequester() }
    fun watch(entry: MediaEntry) { if (model.open(entry)) openPin { gridFocus } }
    fun open(entry: MediaEntry, slot: GuideSlot) {
        select(entry.id, (model.state.value.guideAnchor ?: anchor).takeIf(slot::contains) ?: slot.from)
        val programme = slot.programme
        // A locked channel shows no programme detail: OK asks for its PIN and opens it live.
        if (model.state.value.locks.asksPin(entry.id) || programme?.isOnAt(System.currentTimeMillis()) == true) watch(entry)
        else detail = entry to slot
    }
    // Typed channel numbers ("12"), shown in the corner and opened 1.2 s after the last digit.
    var digits by remember { mutableStateOf("") }
    LaunchedEffect(digits) {
        if (digits.isEmpty()) return@LaunchedEffect
        delay(1_200)
        val number = digits.toIntOrNull(); digits = ""
        numbers.entries.firstOrNull { it.value == number }?.key?.let { id ->
            select(id, (model.state.value.guideAnchor ?: anchor)); try { gridFocus.requestFocus() } catch (_: IllegalStateException) { }
        }
    }
    BackHandler {
        // A Back that arrives after the model left the guide, before this screen is gone, is the new route's.
        // From deep in the grid the day and time controls are one Back away (the grid's Up reaches them only from
        // the first channel); then the menu, then Ana Sayfa.
        when {
            model.state.value.route != Route.GUIDE -> back()
            gridFocused -> try { nowFocus.requestFocus() } catch (_: IllegalStateException) { navigation.requestFocus() }
            !menuFocused -> navigation.requestFocus()
            else -> model.home()
        }
    }
    LaunchedEffect(Unit) { withFrameNanos { }; if (channels.isEmpty()) navigation.requestFocus() else if (!guideless) gridFocus.requestFocus() }
    // The statuses arrive after the first frame: once they say no source has a guide, focus moves from the grid to the action.
    LaunchedEffect(guideless) { if (guideless && !menuFocused) { withFrameNanos { }; try { emptyFocus.requestFocus() } catch (_: IllegalStateException) { } } }
    LaunchedEffect(selectedIndex) {
        val item = list.layoutInfo.visibleItemsInfo.firstOrNull { it.index == selectedIndex }
        if (item == null || item.offset < list.layoutInfo.viewportStartOffset || item.offset + item.size > list.layoutInfo.viewportEndOffset)
            list.scrollToItem(selectedIndex)
    }
    LaunchedEffect(channel?.id, retention) { if (channel != null && (channel.id != s.guideChannelId || anchor != s.guideAnchor)) select(channel.id, anchor) }

    // Right from the side menu returns to the grid or header item that had focus.
    Navigation(s, model, Modifier.onFocusChanged { menuFocused = it.hasFocus }, guideFocus = navigation,
        contentEntry = { if (channels.isEmpty()) nowFocus else if (guideless) emptyFocus else gridFocus }, settings = settings) { Column(Modifier.fillMaxSize().testTag("guide-screen")) {
        Column(Modifier.weight(1f).fillMaxWidth().padding(start = TvLayout.Gutter, end = TvLayout.Gutter, top = TvLayout.PageTop, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Column(Modifier.weight(1f)) {
                    Heading(stringResource(R.string.guide_title))
                    Text("${guideDate(anchor, zone)} · $zoneName · ${guideOffset(anchor, zone)}",
                        color = Ink.Muted, fontFamily = Roboto, fontSize = 13.sp, lineHeight = 18.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Text(guideTime(now, zone), color = Ink.Mint, fontFamily = Roboto, fontSize = 18.sp)
            }
            if (guideless) Column(Modifier.weight(1f).fillMaxWidth().testTag("guide-unavailable"), verticalArrangement = Arrangement.Center) {
                Text(stringResource(R.string.guide_no_info), color = Ink.Text, fontFamily = Roboto, fontWeight = FontWeight.Medium, fontSize = 24.sp, lineHeight = 30.sp)
                Spacer(Modifier.height(10.dp))
                Text(stringResource(R.string.guide_unavailable_note), Modifier.widthIn(max = 640.dp), color = Ink.Muted, fontFamily = Roboto,
                    fontSize = 15.sp, lineHeight = 22.sp)
                Spacer(Modifier.height(18.dp))
                Action(stringResource(R.string.manage_sources), Modifier.focusRequester(emptyFocus).testTag("guide-manage-sources"), primary = true) {
                    if (model.manageSources()) openPin { emptyFocus }
                }
            } else LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(4.dp)) {
                item { Action(stringResource(R.string.guide_now), Modifier.focusRequester(nowFocus).focusProperties { down = if (channels.isEmpty()) FocusRequester.Default else gridFocus }.testTag("guide-now"), compact = true) { select(channel?.id, now); if (channels.isNotEmpty()) gridFocus.requestFocus() } }
                item { Action(stringResource(R.string.guide_previous_day), Modifier.focusProperties { down = if (channels.isEmpty()) FocusRequester.Default else gridFocus }.testTag("guide-day-previous"), compact = true,
                    enabled = dayWindow.from > retention.from) { select(channel?.id, Instant.ofEpochMilli(anchor).atZone(zone).minusDays(1).toInstant().toEpochMilli()) } }
                item { Action(stringResource(R.string.guide_next_day), Modifier.focusProperties { down = if (channels.isEmpty()) FocusRequester.Default else gridFocus }.testTag("guide-day-next"), compact = true,
                    enabled = dayWindow.until < retention.until) { select(channel?.id, Instant.ofEpochMilli(anchor).atZone(zone).plusDays(1).toInstant().toEpochMilli()) } }
                item { Action(stringResource(R.string.guide_zone), Modifier.focusRequester(zoneFocus).focusProperties { down = if (channels.isEmpty()) FocusRequester.Default else gridFocus }.testTag("guide-zone"), compact = true) { zones = true } }
                item { Action(stringResource(R.string.guide_sources), Modifier.focusRequester(sourcesFocus).focusProperties { down = if (channels.isEmpty()) FocusRequester.Default else gridFocus }
                    .testTag("guide-sources"), compact = true) { if (model.manageSources()) openPin { sourcesFocus } } }
                // The remote's shortcuts, where the bottom panel used to name them.
                item { Box(Modifier.height(actionHeight(compact = true)).padding(start = 12.dp), contentAlignment = Alignment.CenterStart) {
                    Text(stringResource(R.string.guide_keys), color = Ink.Muted, fontFamily = Roboto,
                        fontSize = 12.sp, lineHeight = 16.sp, maxLines = 1)
                } }
            }
            if (guideless) Unit else if (channels.isEmpty()) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                    Heading(stringResource(R.string.guide_no_channels))
                    Note(stringResource(R.string.guide_no_channels_note))
                    Action(stringResource(R.string.guide_open_sources), Modifier.focusRequester(openSourcesFocus)) { if (model.manageSources()) openPin { openSourcesFocus } }
                }
            } else {
                // The subtitle already names the offset; repeat it per hour only across a DST change.
                val mixedOffsets = (0 until (duration / hour).toInt()).any { guideOffset(viewport.from + it * hour, zone) != guideOffset(anchor, zone) }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(pluralStringResource(R.plurals.count_channels, channels.size, channels.size.toString()), Modifier.width(channelWidth),
                        color = Ink.Muted, fontFamily = Roboto, fontSize = 12.sp)
                    repeat((duration / hour).toInt()) { n ->
                        val at = viewport.from + n * hour
                        Column(Modifier.weight(1f)) {
                            Text(guideTime(at, zone), color = Ink.Text, fontFamily = Roboto, fontSize = 13.sp)
                            if (mixedOffsets) Text(guideOffset(at, zone), color = Ink.Muted, fontFamily = Roboto, fontSize = 12.sp)
                        }
                    }
                }
                // What the removed bottom panel said (D067): the cell's programme, or why there is none.
                val selectedText = if (channel != null && s.locks.asksPin(channel.id)) stringResource(R.string.lock_channel)
                    else selectedSlot?.programme?.title ?: stringResource(if ((selectedSlot?.programmes?.size ?: 0) > 1) R.string.guide_overlapping_now
                    else guideEmptyText(channel?.let { s.guideStatus[it.sourceId] }, channel?.let { rows[it.id] }))
                val openLabel = stringResource(R.string.guide_open_programme)
                val detailLabel = stringResource(R.string.guide_programme_detail)
                val channelName = channel?.let { shownName(it.title) }.orEmpty()
                BoxWithConstraints(Modifier.weight(1f).fillMaxWidth().focusRequester(gridFocus).onFocusChanged { gridFocused = it.isFocused }
                    .onPreviewKeyEvent { event ->
                        val latest = model.state.value
                        val at = (latest.guideAnchor ?: anchor).coerceIn(retention.from, retention.until - 1)
                        val index = channels.indexOfFirst { it.id == latest.guideChannelId }.coerceAtLeast(0)
                        val entry = channels.getOrNull(index)
                        val activeSlots = entry?.let { allSlots[it.id] } ?: emptySlots
                        val activeSlot = activeSlots.firstOrNull { it.contains(at) }
                        if (event.key in setOf(Key.DirectionCenter, Key.Enter, Key.NumPadEnter)) {
                            if (event.type == KeyEventType.KeyUp && entry != null && activeSlot != null) open(entry, activeSlot)
                            return@onPreviewKeyEvent true
                        }
                        if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                        when (event.key) {
                            Key.DirectionUp -> { if (index == 0) nowFocus.requestFocus() else select(channels[index - 1].id, at); true }
                            Key.DirectionDown -> { channels.getOrNull(index + 1)?.let { select(it.id, at) }; true }
                            // CH+/CH− move a page of channels (CH+ towards higher numbers, as in the player).
                            Key.ChannelUp, Key.ChannelDown, Key.PageUp, Key.PageDown -> {
                                val page = (list.layoutInfo.visibleItemsInfo.size - 1).coerceAtLeast(1)
                                val step = if (event.key == Key.ChannelUp || event.key == Key.PageDown) page else -page
                                channels.getOrNull((index + step).coerceIn(0, channels.lastIndex))?.let { select(it.id, at) }; true
                            }
                            // Fast-forward and rewind change the day, as Sonraki gün / Önceki gün.
                            Key.MediaFastForward, Key.MediaRewind -> {
                                val day = Instant.ofEpochMilli(at).atZone(zone).plusDays(if (event.key == Key.MediaFastForward) 1 else -1).toInstant().toEpochMilli()
                                if (day in retention.from until retention.until) select(entry?.id, day); true
                            }
                            Key.DirectionLeft, Key.DirectionRight -> {
                                val direction = if (event.key == Key.DirectionRight) 1 else -1
                                val target = if (activeSlot?.programmes.isNullOrEmpty()) at + direction * 30 * 60_000L
                                    else guideHorizontalTarget(activeSlots, at, direction)
                                if (target < retention.from) navigation.requestFocus() else select(entry?.id, target)
                                true
                            }
                            else -> {
                                val code = event.nativeKeyEvent.keyCode
                                val digit = when (code) {
                                    in android.view.KeyEvent.KEYCODE_0..android.view.KeyEvent.KEYCODE_9 -> code - android.view.KeyEvent.KEYCODE_0
                                    in android.view.KeyEvent.KEYCODE_NUMPAD_0..android.view.KeyEvent.KEYCODE_NUMPAD_9 -> code - android.view.KeyEvent.KEYCODE_NUMPAD_0
                                    else -> -1
                                }
                                if (digit >= 0) { if (digits.length < 4) digits += digit; true } else false
                            }
                        }
                    }.focusable().testTag("guide-grid").semantics {
                        contentDescription = channelName + ", " + guideTime(anchor, zone) + ", " + selectedText
                        liveRegion = LiveRegionMode.Polite
                        onClick(openLabel) { if (channel != null && selectedSlot != null) open(channel, selectedSlot); true }
                    }) {
                    // Size the list to whole rows: every scroll position, including the end, lands on a row
                    // boundary, so no channel row rests cut through its text at the grid edge.
                    val gapPx = with(density) { 4.dp.roundToPx() }
                    val padPx = with(density) { 4.dp.roundToPx() }
                    val pitchPx = with(density) { rowHeight.roundToPx() } + gapPx
                    val fit = ((constraints.maxHeight - 2 * padPx + gapPx) / pitchPx).coerceAtLeast(1)
                    val listHeight = with(density) { (fit * pitchPx - gapPx + 2 * padPx).toDp() }
                    LazyColumn(Modifier.height(listHeight), state = list, verticalArrangement = Arrangement.spacedBy(4.dp), contentPadding = PaddingValues(vertical = 4.dp)) {
                        itemsIndexed(channels, key = { _, entry -> entry.id }) { index, entry ->
                            val rowLocked = s.locks.asksPin(entry.id)
                            val rowSlots = allSlots[entry.id] ?: emptySlots
                            val spoken = rememberShownName(entry.title)
                            Row(Modifier.fillMaxWidth().height(rowHeight).testTag("guide-row:${entry.id}"), verticalAlignment = Alignment.CenterVertically) {
                                Row(Modifier.width(channelWidth).fillMaxHeight().padding(end = 12.dp), verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(numbers[entry.id]?.toString().orEmpty(), Modifier.width(30.dp), color = Ink.Muted, fontFamily = Roboto,
                                        fontSize = 12.sp, maxLines = 1)
                                    Artwork(entry.sourceId, entry.artwork, entry.title, Modifier.width(60.dp).height(if (density.fontScale >= 1.5f) 44.dp else 36.dp)
                                        .testTag("guide-logo:${entry.id}"), logo = true, logoInset = 2.dp)
                                    Column(Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(rememberShownTitle(entry.title).name, Modifier.weight(1f, fill = false), color = if (index == selectedIndex) Ink.Text else Ink.Muted,
                                                fontFamily = Roboto, fontSize = 13.sp, maxLines = if (s.sources.size > 1) 1 else 2, lineHeight = 17.sp,
                                                overflow = TextOverflow.Ellipsis)
                                            if (rowLocked) LockMark()
                                        }
                                        if (s.sources.size > 1) Text(s.sourceName(entry.sourceId), color = Ink.Muted, fontFamily = Roboto, fontSize = 12.sp,
                                            lineHeight = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                                BoxWithConstraints(Modifier.weight(1f).fillMaxHeight().clipToBounds()) {
                                    val width = maxWidth
                                    rowSlots.filter { it.from < viewport.until && it.until > viewport.from }.forEach { slot ->
                                        val start = maxOf(slot.from, viewport.from)
                                        val end = minOf(slot.until, viewport.until)
                                        val offset = width * ((start - viewport.from).toFloat() / duration)
                                        val cellWidth = (width * ((end - start).toFloat() / duration) - 3.dp).coerceAtLeast(3.dp)
                                        val chosen = index == selectedIndex && slot.contains(anchor)
                                        val title = if (rowLocked) stringResource(R.string.lock_channel)
                                            else slot.programme?.title ?: stringResource(if (slot.programmes.size > 1) R.string.guide_overlapping
                                            else if (rows[entry.id]?.match?.kind in listOf(EpgMatchKind.ID, EpgMatchKind.NAME)) R.string.guide_no_info_short
                                            else guideEmptyText(s.guideStatus[entry.sourceId], rows[entry.id]))
                                        Box(Modifier.offset(x = offset).width(cellWidth).fillMaxHeight()
                                            .background(if (chosen) Ink.Selected else Ink.Container, RoundedCornerShape(6.dp))
                                            // The now marker sits on the fill, under the title and the focus border.
                                            .drawBehind { if (now in start until end) drawRect(Ink.Mint.copy(alpha = .5f),
                                                topLeft = Offset((width * ((now - start).toFloat() / duration)).toPx(), 0f), size = Size(1.dp.toPx(), size.height)) }
                                            // The same white ring as a focused card (D064).
                                            .border(if (chosen && gridFocused) TvLayout.Focus else 1.dp,
                                                if (chosen && gridFocused) Ink.Text else Ink.Divider.copy(alpha = .5f), RoundedCornerShape(6.dp))
                                            .testTag("guide-cell:${entry.id}:${slot.from}").semantics {
                                                selected = chosen
                                                contentDescription = "$spoken, $title, ${guideTime(slot.from, zone)}"
                                                onClick(detailLabel) { open(entry, slot); true }
                                            }) {
                                            if (cellWidth > 48.dp) Column(Modifier.padding(horizontal = 10.dp, vertical = 6.dp).align(Alignment.CenterStart)) {
                                                val timed = cellWidth > 120.dp && density.fontScale < 1.5f && slot.programme != null
                                                Text(title, color = if (slot.programmes.isEmpty()) Ink.Muted else Ink.Text, fontFamily = Roboto,
                                                    fontWeight = if (chosen) FontWeight.Medium else FontWeight.Normal,
                                                    fontSize = 13.sp, maxLines = if (timed) 1 else 2, lineHeight = 17.sp, overflow = TextOverflow.Ellipsis)
                                                if (timed) slot.programme?.let { Text(programmeTime(it, zone),
                                                    color = Ink.Muted, fontFamily = Roboto, fontSize = 12.sp, lineHeight = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    } }
    // Below the clock, so the typed number never covers the time.
    if (digits.isNotEmpty()) Box(Modifier.fillMaxSize().padding(top = 64.dp, end = TvLayout.Gutter), contentAlignment = Alignment.TopEnd) {
        Text(digits, Modifier.background(Ink.Background.copy(alpha = .92f), RoundedCornerShape(12.dp)).padding(horizontal = 18.dp, vertical = 8.dp)
            .testTag("guide-digits"), color = Ink.Text, fontFamily = Roboto, fontWeight = FontWeight.Medium, fontSize = 22.sp)
    }
    detail?.let { (entry, slot) ->
        val focus = remember { FocusRequester() }
        val play = remember { FocusRequester() }
        fun dismiss() { detail = null; gridFocus.requestFocus() }
        // A channel a PIN opened locks again as the app leaves the screen: its programmes go with it (pin.md §4).
        if (s.locks.asksPin(entry.id)) LaunchedEffect(Unit) { dismiss() }
        else SourceDialog(stringResource(R.string.guide_programme_detail), focus, ::dismiss, width = 600.dp) {
            // A programme that has not started is not catch-up; point to what the live action opens instead.
            val upcoming = slot.programmes.isNotEmpty() && slot.programmes.all { it.start > now }
            val ended = slot.programmes.isNotEmpty() && slot.programmes.all { (it.end ?: (it.start + 1)) <= now }
            val note = when {
                slot.programmes.isEmpty() || slot.programme?.isOnAt(now) == true -> ""
                upcoming -> "\n\n" + stringResource(R.string.guide_detail_upcoming)
                ended -> "\n\n" + stringResource(R.string.guide_detail_ended)
                else -> "\n\n" + stringResource(R.string.guide_detail_live_only)
            }
            val unknownEnd = stringResource(R.string.guide_end_unknown)
            val noDescription = stringResource(R.string.guide_detail_no_description)
            val content = "${shownName(entry.title)}\n${guideDate(slot.from, zone)} · $zoneName · ${guideOffset(slot.from, zone)}\n\n" +
                (if (slot.programmes.isEmpty()) stringResource(R.string.guide_detail_no_programme)
                else slot.programmes.joinToString("\n\n") { "${it.title}\n${programmeTime(it, zone, unknownEnd)}\n${it.description.ifBlank { noDescription }}" }) + note
            RemoteTextReader(content, Modifier.weight(1f, fill = false).focusRequester(focus).focusProperties { down = play }, "guide-detail-reader")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Action(stringResource(R.string.guide_watch_live), Modifier.focusRequester(play).focusProperties { up = focus }.testTag("guide-play-live"), primary = true) { detail = null; watch(entry) }
                Action(stringResource(R.string.action_close), Modifier.focusProperties { up = focus }.testTag("guide-close-detail")) { dismiss() }
            }
        }
    }
    if (zones) GuideZoneDialog(s, model) { zones = false; zoneFocus.requestFocus() }
}
