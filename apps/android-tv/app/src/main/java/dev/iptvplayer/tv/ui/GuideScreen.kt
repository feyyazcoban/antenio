package dev.iptvplayer.tv.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import dev.iptvplayer.tv.*
import dev.iptvplayer.tv.data.*
import kotlinx.coroutines.delay
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.Locale

private val guideLocale = Locale.forLanguageTag("tr")
internal fun guideTime(time: Long, zone: ZoneId): String = Instant.ofEpochMilli(time).atZone(zone).format(DateTimeFormatter.ofPattern("HH:mm", guideLocale))
internal fun guideDate(time: Long, zone: ZoneId): String = Instant.ofEpochMilli(time).atZone(zone).format(DateTimeFormatter.ofPattern("d MMM EEEE", guideLocale))
internal fun guideOffset(time: Long, zone: ZoneId): String = "UTC" + Instant.ofEpochMilli(time).atZone(zone).offset.id.let { if (it == "Z") "+00:00" else it }
internal fun programmeTime(programme: EpgProgramme, zone: ZoneId): String {
    val start = Instant.ofEpochMilli(programme.start).atZone(zone)
    val end = programme.end?.let { Instant.ofEpochMilli(it).atZone(zone) }
    val dates = end != null && start.toLocalDate() != end.toLocalDate()
    val offsets = end != null && start.offset != end.offset
    val format = DateTimeFormatter.ofPattern(if (dates) "d MMM HH:mm" else "HH:mm", guideLocale)
    fun label(value: ZonedDateTime) = value.format(format) + if (offsets) " (${guideOffset(value.toInstant().toEpochMilli(), zone)})" else ""
    return label(start) + " — " + (end?.let(::label) ?: "Bitiş bilinmiyor")
}

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

internal fun guideEmptyText(status: GuideStatus?, slice: GuideSlice?): String = when {
    status?.refreshing == true && status.info == null -> "Rehber yükleniyor…"
    status?.error != null && status.info == null -> "Rehber alınamadı"
    status?.configured != true -> "Rehber adresi eklenmemiş"
    slice?.match?.kind == EpgMatchKind.AMBIGUOUS -> "Kanal eşleşmesi belirsiz"
    slice?.match?.kind == EpgMatchKind.MISSING_ID -> "Rehberde kanal kimliği bulunamadı"
    slice?.match?.kind == EpgMatchKind.NONE -> "Kanal rehberde eşleşmedi"
    status.info == null -> "Rehber bilgisi yok"
    else -> "Bu saatte program bilgisi yok"
}

@Composable internal fun GuideScreen(s: TvState, model: TvViewModel, back: () -> Unit, settings: () -> Unit) {
    val now = guideClock()
    val zone = model.guideZone()
    val density = LocalDensity.current
    val channels = remember(s.catalog) { s.catalog.items.filter { it.kind == MediaKind.LIVE && !it.adult } }
    val retention = remember(now / 60_000, zone) { GuideWindow.around(Instant.ofEpochMilli(now), zone) }
    val anchor = (s.guideAnchor ?: now).coerceIn(retention.from, retention.until - 1)
    val day = Instant.ofEpochMilli(anchor).atZone(zone).toLocalDate()
    val dayWindow = remember(day, zone) { GuideWindow(day.atStartOfDay(zone).toInstant().toEpochMilli(), day.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()) }
    val selectedIndex = channels.indexOfFirst { it.id == s.guideChannelId }.coerceAtLeast(0)
    val channel = channels.getOrNull(selectedIndex)
    val list = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex)
    val visible by remember { derivedStateOf { list.layoutInfo.visibleItemsInfo.map { it.index } } }
    val requested = remember(channels, visible, selectedIndex) {
        (visible + (selectedIndex - 2..selectedIndex + 2)).distinct().sorted().mapNotNull { channels.getOrNull(it) }
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
    val channelWidth = (if (density.fontScale >= 1.5f) 232 else 196).dp
    val rowHeight = (if (density.fontScale >= 1.5f) 90 else 65).dp

    fun select(id: String?, time: Long) { model.guideCursor(id, time.coerceIn(retention.from, retention.until - 1)) }
    fun open(entry: MediaEntry, slot: GuideSlot) {
        select(entry.id, (model.state.value.guideAnchor ?: anchor).takeIf(slot::contains) ?: slot.from)
        val programme = slot.programme
        if (programme?.isOnAt(System.currentTimeMillis()) == true) model.open(entry)
        else detail = entry to slot
    }
    BackHandler {
        // A Back that arrives after the model left the guide, before this screen is gone, is the new route's.
        if (model.state.value.route != Route.GUIDE) back() else if (gridFocused) navigation.requestFocus() else model.home()
    }
    LaunchedEffect(Unit) { withFrameNanos { }; if (channels.isEmpty()) navigation.requestFocus() else gridFocus.requestFocus() }
    LaunchedEffect(selectedIndex) {
        val item = list.layoutInfo.visibleItemsInfo.firstOrNull { it.index == selectedIndex }
        if (item == null || item.offset < list.layoutInfo.viewportStartOffset || item.offset + item.size > list.layoutInfo.viewportEndOffset)
            list.scrollToItem(selectedIndex)
    }
    LaunchedEffect(channel?.id, retention) { if (channel != null && (channel.id != s.guideChannelId || anchor != s.guideAnchor)) select(channel.id, anchor) }

    // Right from the side menu returns to the grid or header item that had focus.
    Navigation(s, model, guideFocus = navigation, contentEntry = { if (channels.isEmpty()) nowFocus else gridFocus }, settings = settings) { Column(Modifier.fillMaxSize().testTag("guide-screen")) {
        Column(Modifier.weight(1f).fillMaxWidth().padding(start = TvLayout.Gutter, end = TvLayout.Gutter, top = TvLayout.PageTop, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Column(Modifier.weight(1f)) {
                    Heading("Yayın rehberi")
                    Text("${guideDate(anchor, zone)} · ${if (s.guideZoneId.isEmpty()) "TV saati" else zone.id} · ${guideOffset(anchor, zone)}",
                        color = Ink.Muted, fontFamily = Roboto, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Text(guideTime(now, zone), color = Ink.Mint, fontFamily = Roboto, fontSize = 18.sp)
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(4.dp)) {
                item { Action("Şimdi", Modifier.focusRequester(nowFocus).focusProperties { down = if (channels.isEmpty()) FocusRequester.Default else gridFocus }.testTag("guide-now"), compact = true) { select(channel?.id, now); if (channels.isNotEmpty()) gridFocus.requestFocus() } }
                item { Action("‹ Önceki gün", Modifier.focusProperties { down = if (channels.isEmpty()) FocusRequester.Default else gridFocus }.testTag("guide-day-previous"), compact = true,
                    enabled = dayWindow.from > retention.from) { select(channel?.id, Instant.ofEpochMilli(anchor).atZone(zone).minusDays(1).toInstant().toEpochMilli()) } }
                item { Action("Sonraki gün ›", Modifier.focusProperties { down = if (channels.isEmpty()) FocusRequester.Default else gridFocus }.testTag("guide-day-next"), compact = true,
                    enabled = dayWindow.until < retention.until) { select(channel?.id, Instant.ofEpochMilli(anchor).atZone(zone).plusDays(1).toInstant().toEpochMilli()) } }
                item { Action("Saat dilimi", Modifier.focusRequester(zoneFocus).focusProperties { down = if (channels.isEmpty()) FocusRequester.Default else gridFocus }.testTag("guide-zone"), compact = true) { zones = true } }
                item { Action("Kaynaklar", Modifier.focusProperties { down = if (channels.isEmpty()) FocusRequester.Default else gridFocus }, compact = true) { model.manageSources() } }
            }
            if (channels.isEmpty()) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                    Heading("Canlı kanal bulunamadı")
                    Note("Kaynak seçimini değiştirebilir veya yeni bir kaynak ekleyebilirsin.")
                    Action("Kaynakları aç") { model.manageSources() }
                }
            } else {
                // The subtitle already names the offset; repeat it per hour only across a DST change.
                val mixedOffsets = (0 until (duration / hour).toInt()).any { guideOffset(viewport.from + it * hour, zone) != guideOffset(anchor, zone) }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("${channels.size} kanal", Modifier.width(channelWidth), color = Ink.Muted, fontFamily = Roboto, fontSize = 10.sp)
                    repeat((duration / hour).toInt()) { n ->
                        val at = viewport.from + n * hour
                        Column(Modifier.weight(1f)) {
                            Text(guideTime(at, zone), color = Ink.Text, fontFamily = Roboto, fontSize = 11.sp)
                            if (mixedOffsets) Text(guideOffset(at, zone), color = Ink.Muted, fontFamily = Roboto, fontSize = 8.sp)
                        }
                    }
                }
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
                            Key.DirectionLeft, Key.DirectionRight -> {
                                val direction = if (event.key == Key.DirectionRight) 1 else -1
                                val target = if (activeSlot?.programmes.isNullOrEmpty()) at + direction * 30 * 60_000L
                                    else guideHorizontalTarget(activeSlots, at, direction)
                                if (target < retention.from) navigation.requestFocus() else select(entry?.id, target)
                                true
                            }
                            else -> false
                        }
                    }.focusable().testTag("guide-grid").semantics {
                        contentDescription = channel?.title.orEmpty() + ", " + guideTime(anchor, zone) + ", " +
                            (selectedSlot?.programme?.title ?: "Program bilgisi yok")
                        onClick("Programı aç") { if (channel != null && selectedSlot != null) open(channel, selectedSlot); true }
                    }) {
                    // Size the list to whole rows: every scroll position, including the end, lands on a row
                    // boundary, so no channel row rests cut through its text at the grid edge.
                    val gapPx = with(density) { 5.dp.roundToPx() }
                    val padPx = with(density) { 4.dp.roundToPx() }
                    val pitchPx = with(density) { rowHeight.roundToPx() } + gapPx
                    val fit = ((constraints.maxHeight - 2 * padPx + gapPx) / pitchPx).coerceAtLeast(1)
                    val listHeight = with(density) { (fit * pitchPx - gapPx + 2 * padPx).toDp() }
                    LazyColumn(Modifier.height(listHeight), state = list, verticalArrangement = Arrangement.spacedBy(5.dp), contentPadding = PaddingValues(vertical = 4.dp)) {
                        itemsIndexed(channels, key = { _, entry -> entry.id }) { index, entry ->
                            val rowSlots = allSlots[entry.id] ?: emptySlots
                            Row(Modifier.fillMaxWidth().height(rowHeight).testTag("guide-row:${entry.id}"), verticalAlignment = Alignment.CenterVertically) {
                                Row(Modifier.width(channelWidth).fillMaxHeight().padding(end = 12.dp), verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Artwork(entry.sourceId, entry.artwork, entry.title, Modifier.width(72.dp).height(if (density.fontScale >= 1.5f) 44.dp else 40.dp)
                                        .testTag("guide-logo:${entry.id}"), logo = true, logoInset = 2.dp)
                                    Column(Modifier.weight(1f)) {
                                        Text(entry.title, color = if (index == selectedIndex) Ink.Text else Ink.Muted, fontFamily = Roboto,
                                            fontSize = 11.sp, maxLines = 2, lineHeight = 15.sp, overflow = TextOverflow.Ellipsis)
                                        if (s.sources.size > 1) Text(s.sourceName(entry.sourceId), color = Ink.Muted, fontFamily = Roboto, fontSize = 8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
                                        val title = slot.programme?.title ?: if (slot.programmes.size > 1) "Çakışan programlar"
                                            else if (rows[entry.id]?.match?.kind in listOf(EpgMatchKind.ID, EpgMatchKind.NAME)) "Bilgi yok"
                                            else guideEmptyText(s.guideStatus[entry.sourceId], rows[entry.id])
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
                                                contentDescription = "${entry.title}, $title, ${guideTime(slot.from, zone)}"
                                                onClick("Program ayrıntısı") { open(entry, slot); true }
                                            }) {
                                            if (cellWidth > 48.dp) Column(Modifier.padding(horizontal = 10.dp, vertical = 7.dp).align(Alignment.CenterStart)) {
                                                Text(title, color = if (slot.programmes.isEmpty()) Ink.Muted else Ink.Text, fontFamily = Roboto,
                                                    fontWeight = if (chosen) FontWeight.Medium else FontWeight.Normal,
                                                    fontSize = 11.sp, maxLines = 2, lineHeight = 15.sp, overflow = TextOverflow.Ellipsis)
                                                if (cellWidth > 120.dp && density.fontScale < 1.5f) slot.programme?.let { Text(programmeTime(it, zone),
                                                    color = Ink.Muted, fontFamily = Roboto, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Column(Modifier.fillMaxWidth().background(Ink.Container, RoundedCornerShape(8.dp)).padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(selectedSlot?.programme?.title ?: if ((selectedSlot?.programmes?.size ?: 0) > 1) "Bu saatte çakışan programlar var"
                        else guideEmptyText(channel?.let { s.guideStatus[it.sourceId] }, channel?.let { rows[it.id] }),
                        color = Ink.Text, fontFamily = Roboto, fontSize = 14.sp, lineHeight = 18.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("guide-selection-title"))
                    Text(selectedSlot?.programme?.let { programmeTime(it, zone) + if (it.isOnAt(now)) " · OK: canlı izle" else " · OK: ayrıntılar" }
                        ?: "OK: kanal ve ayrıntılar · Geri: menü", color = Ink.Muted, fontFamily = Roboto, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (rows[channel?.id]?.truncated == true) Text("Bu kanalın rehberi çok yoğun; bazı kayıtlar gösterilemiyor.",
                        color = Ink.Muted, fontFamily = Roboto, fontSize = 10.sp, lineHeight = 14.sp)
                }
            }
        }
    } }
    detail?.let { (entry, slot) ->
        val focus = remember { FocusRequester() }
        val play = remember { FocusRequester() }
        fun dismiss() { detail = null; gridFocus.requestFocus() }
        SourceDialog("Program ayrıntısı", focus, ::dismiss, width = 600.dp) {
            // A programme that has not started is not catch-up; point to what the live action opens instead.
            val upcoming = slot.programmes.isNotEmpty() && slot.programmes.all { it.start > now }
            val ended = slot.programmes.isNotEmpty() && slot.programmes.all { (it.end ?: (it.start + 1)) <= now }
            val note = when {
                slot.programmes.isEmpty() || slot.programme?.isOnAt(now) == true -> ""
                upcoming -> "\n\nBu program henüz başlamadı. Canlı kanalı izle, şu an yayındaki programı açar."
                ended -> "\n\nBu program için geçmişten izleme desteklenmiyor. Yalnız canlı yayın açılabilir."
                else -> "\n\nYalnız canlı yayın açılabilir."
            }
            val content = "${entry.title}\n${guideDate(slot.from, zone)} · ${if (s.guideZoneId.isEmpty()) "TV saati" else zone.id} · ${guideOffset(slot.from, zone)}\n\n" +
                (if (slot.programmes.isEmpty()) "Program bilgisi bulunmuyor. Canlı kanal izlenebilir."
                else slot.programmes.joinToString("\n\n") { "${it.title}\n${programmeTime(it, zone)}\n${it.description.ifBlank { "Açıklama bulunmuyor." }}" }) + note
            RemoteTextReader(content, Modifier.weight(1f, fill = false).focusRequester(focus).focusProperties { down = play }, "guide-detail-reader")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Action("Canlı kanalı izle", Modifier.focusRequester(play).focusProperties { up = focus }.testTag("guide-play-live"), primary = true) { detail = null; model.open(entry) }
                Action("Kapat", Modifier.focusProperties { up = focus }.testTag("guide-close-detail")) { dismiss() }
            }
        }
    }
    if (zones) GuideZoneDialog(s, model) { zones = false; zoneFocus.requestFocus() }
}
