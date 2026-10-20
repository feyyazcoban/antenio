package dev.iptvplayer.tv.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.BringIntoViewSpec
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.*
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import dev.iptvplayer.tv.R
import dev.iptvplayer.tv.TvState
import dev.iptvplayer.tv.TvViewModel
import dev.iptvplayer.tv.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first

@Composable private fun scopeName(scope: ChannelScope): String = when {
    scope.favoritesOnly -> stringResource(R.string.section_favorites)
    scope.category != null -> displayCategory(scope.category).first
    else -> stringResource(R.string.library_all_channels)
}

/** The D094 drawer, extended in place. Browsing never opens a second playback session. */
@OptIn(ExperimentalFoundationApi::class)
@Composable internal fun ChannelPanel(state: ChannelPanelState, s: TvState, model: TvViewModel,
    playingId: String, active: Boolean, problem: String?, failed: Boolean, opacity: Float,
    pick: (String, ChannelScope, () -> FocusRequester?) -> GateResult, close: () -> Unit,
    input: () -> Unit) {
    val density = LocalDensity.current
    val single = density.fontScale >= 1.8f
    val channelWidth = if (single) 520.dp else if (density.fontScale >= 1.3f) 440.dp else 400.dp
    val categoryWidth = if (density.fontScale >= 1.3f) 232.dp else 200.dp
    val channelsActive = active && state.column == ChannelColumn.CHANNELS
    val parentVisibility = LocalBringIntoViewSpec.current
    val channelVisibility = remember(channelsActive, parentVisibility) { object : BringIntoViewSpec {
        override fun calculateScrollDistance(offset: Float, size: Float, containerSize: Float): Float =
            if (channelsActive) parentVisibility.calculateScrollDistance(offset, size, containerSize) else 0f
    } }
    val index = remember(s.allCatalog, s.catalog, s.favoriteIds, state.scope) { model.liveIndex(state.scope) }
    val channels = index.channels
    val choices = remember(s.allCatalog, s.favoriteIds, state.scope.sourceId) { model.liveChoices(state.scope.sourceId) }
    val sources = remember(s.sources) { s.sources.associate { it.id to it.name } }
    val attached = remember { mutableStateMapOf<String, FocusRequester>() }
    val closeFocus = remember { FocusRequester() }
    val panelFocus = remember { FocusRequester() }
    val allFocus = remember { FocusRequester() }
    val list = rememberLazyListState()
    var initialized by remember { mutableStateOf(false) }
    var miniId by remember { mutableStateOf<String?>(null) }
    var quick by remember { mutableStateOf<MediaEntry?>(null) }
    var quickReturn by remember { mutableStateOf<Pair<String, Int>?>(null) }
    var afterQuick by remember { mutableStateOf<(() -> Unit)?>(null) }
    val window = LocalWindowInfo.current
    val openPin = rememberPinOpener()
    val now = guideClock()
    val anchor = remember(state.session) { System.currentTimeMillis() / 300_000 * 300_000 }
    val rows = remember(state.session, s.guideRevision, s.guideZoneId, s.locks) { mutableStateMapOf<String, GuideSlice>() }
    val requested = remember(state.session, s.guideRevision, s.guideZoneId, s.locks) { HashSet<String>() }
    val title = stringResource(R.string.panel_channels_title)
    val programmesLabel = stringResource(R.string.panel_programmes)
    val scopeTitle = scopeName(state.scope)
    fun returnRow(): FocusRequester? = if (state.column != ChannelColumn.CHANNELS) panelFocus else attached[state.focusedId] ?: if (channels.isEmpty()) allFocus else closeFocus
    SideEffect { state.returnFocus = ::returnRow }
    DisposableEffect(state) { onDispose { state.returnFocus = { null } } }
    fun programmes() { miniId = state.focusedId; state.column = ChannelColumn.PROGRAMMES; input() }
    fun category(scope: ChannelScope) {
        state.scope = scope
        state.focusedId = model.liveIndex(scope).channels.firstOrNull { it.id == playingId }?.id
        state.focusedIndex = 0; state.channels(); input()
    }
    LaunchedEffect(active, state.session, channels, state.focusRequest, state.column) {
        if (!active) { initialized = false; return@LaunchedEffect }
        if (!channelsActive) return@LaunchedEffect
        val opening = !initialized
        val target = state.focusTarget
        val wanted = if (opening) playingId else if (target != null) target.first else state.focusedId
        val selected = channels.indexOfFirst { it.id == wanted }
            .takeIf { it >= 0 } ?: (target?.second ?: state.focusedIndex).coerceIn(0, channels.lastIndex.coerceAtLeast(0))
        initialized = true
        if (channels.isEmpty()) { withFrameNanos { }; allFocus.requestFocus() }
        else {
            val id = channels[selected].id
            val layout = list.layoutInfo
            if (layout.visibleItemsInfo.none { it.key == id && it.offset >= layout.viewportStartOffset &&
                    it.offset + it.size <= layout.viewportEndOffset }) list.scrollToItem((selected - 3).coerceAtLeast(0))
            snapshotFlow { attached[id] }.first { it != null }
            withFrameNanos { }
            if (runCatching { attached[id]?.requestFocus() == true }.getOrDefault(false)) {
                state.focusedId = id; state.focusedIndex = selected
            } else closeFocus.requestFocus()
        }
        state.focusTarget = null
    }
    LaunchedEffect(state.page) {
        val direction = state.page?.second ?: return@LaunchedEffect
        if (!active) return@LaunchedEffect
        if (state.column != ChannelColumn.CHANNELS) state.channels()
        val count = list.layoutInfo.visibleItemsInfo.count { it.offset >= list.layoutInfo.viewportStartOffset &&
            it.offset + it.size <= list.layoutInfo.viewportEndOffset }.coerceAtLeast(2)
        val next = PanelPaging.target(state.focusedIndex, direction, count, channels.size)
        state.focusedId = channels.getOrNull(next)?.id; state.focusedIndex = next; state.channels()
    }
    LaunchedEffect(state.number) {
        val number = state.number?.second ?: return@LaunchedEffect
        val entry = index.numbered.getOrNull(number - 1) ?: return@LaunchedEffect
        if (channels.none { it.id == entry.id }) state.scope = ChannelScope()
        state.focusedId = entry.id; state.focusedIndex = 0; state.channels()
    }
    // Only visible rows (+3) and the playing channel are read. A scroll cancels the previous debounce/query.
    LaunchedEffect(active, channels, state.session, s.guideRevision, s.guideZoneId, s.locks) {
        if (!active) return@LaunchedEffect
        snapshotFlow {
            val visible = list.layoutInfo.visibleItemsInfo
            val from = ((visible.firstOrNull()?.index ?: 0) - 3).coerceAtLeast(0)
            val to = ((visible.lastOrNull()?.index ?: 0) + 3).coerceAtMost(channels.lastIndex)
            (if (to >= from) channels.subList(from, to + 1) else emptyList()).map { it.id } + playingId
        }.distinctUntilChanged().collectLatest { ids ->
            delay(150)
            val entries = ids.distinct().filter { it !in requested && !s.locks.asksPin(it) }.mapNotNull(model::liveEntry)
            if (entries.isNotEmpty()) {
                model.ensureGuide(entries.map { it.sourceId }.distinct())
                rows.putAll(model.guideRows(entries, GuideWindow(anchor, anchor + 6 * 3_600_000L)))
                requested.addAll(entries.map { it.id })
            }
        }
    }
    LaunchedEffect(s.allCatalog, miniId) {
        if (state.column == ChannelColumn.PROGRAMMES && model.liveEntry(miniId) == null) state.channels()
        if (quick != null && model.liveEntry(quick?.id) == null) quick = null
    }
    LaunchedEffect(quick) {
        if (quick == null && afterQuick != null) {
            // Wait for the window, then restore the saved row through the scrolling/attachment path.
            refocusAfterDialog(window) { null }
            quickReturn?.let { state.restore(it.first, it.second) } ?: state.channels()
            quickReturn = null
            afterQuick?.invoke(); afterQuick = null
        }
    }
    Row(Modifier.animateContentSize(tween(if (s.reducedMotion) 0 else 160))
        .clip(RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp))
        .background(Ink.Background.copy(alpha = opacity.coerceAtLeast(.8f))).fillMaxHeight()
        .then(if (active) Modifier else Modifier.clearAndSetSemantics { })
        .semantics { paneTitle = title; isTraversalGroup = true }.testTag("player-channel-drawer")
        .focusRequester(panelFocus).focusProperties { onExit = { if (active) cancelFocusChange() } }.focusGroup()) {
        if (state.column == ChannelColumn.CATEGORIES) ScopeColumn(choices, state.scope,
            if (single) channelWidth else categoryWidth, active, s.locks, ::category, state::channels)
        if (!single || state.column == ChannelColumn.CHANNELS) Column(Modifier.width(channelWidth).fillMaxHeight()
            .padding(horizontal = 24.dp, vertical = 22.dp)) {
            Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f)) {
                    Text(scopeTitle, Modifier.testTag("channel-panel-scope"), color = Ink.Text, fontFamily = Roboto, fontSize = 18.sp,
                        fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(pluralStringResource(R.plurals.panel_channel_count, channels.size, channels.size), color = Ink.Muted,
                        fontFamily = Roboto, fontSize = 13.sp)
                }
                Action(stringResource(R.string.action_close), Modifier.focusRequester(closeFocus)
                    .focusProperties { canFocus = channelsActive }.testTag("player-channels-close"), compact = true) { if (active) close() }
            }
            if (channels.isEmpty()) {
                Text(stringResource(if (state.scope.favoritesOnly) R.string.panel_empty_favorites else R.string.player_channels_empty),
                    color = Ink.Muted, fontFamily = Roboto, fontSize = 15.sp, lineHeight = 21.sp)
                Spacer(Modifier.height(16.dp))
                Action(stringResource(R.string.library_all_channels), Modifier.focusRequester(allFocus)
                    .focusProperties { canFocus = channelsActive }.testTag("player-channels-all")) { category(ChannelScope()) }
            } else CompositionLocalProvider(LocalBringIntoViewSpec provides channelVisibility) {
            LazyColumn(Modifier.fillMaxWidth().weight(1f).testTag("player-channel-list"), state = list,
                contentPadding = PaddingValues(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                itemsIndexed(channels, key = { _, entry -> entry.id }, contentType = { _, _ -> "channel" }) { i, entry ->
                    val focus = remember { FocusRequester() }
                    DisposableEffect(entry.id) { attached[entry.id] = focus; onDispose { attached.remove(entry.id) } }
                    val locked = s.locks.asksPin(entry.id)
                    val current = entry.id == playingId
                    val programme = rows[entry.id]?.takeUnless { locked }?.now(now)
                    val name = rememberShownTitle(entry.title, "").name
                    val second = when {
                        locked -> stringResource(R.string.lock_channel)
                        current && problem != null -> problem
                        programme != null -> programme.title
                        rows[entry.id]?.programmes?.count { it.isOnAt(now) }?.let { it > 1 } == true -> stringResource(R.string.guide_overlapping_now)
                        s.guideStatus[entry.sourceId]?.configured != true -> displayCategory(entry.category).first
                        else -> stringResource(guideEmptyText(s.guideStatus[entry.sourceId], rows[entry.id]))
                    }
                    val number = index.numbers[entry.id]?.toString().orEmpty()
                    val description = listOfNotNull(if (current) stringResource(R.string.player_channel_playing) else null,
                        if (locked) stringResource(R.string.lock_channel) else null,
                        if (entry.id in s.favoriteIds) stringResource(R.string.player_in_favorites) else null).joinToString(", ")
                    PanelItem(current, channelsActive, Modifier.focusRequester(focus)
                        .onFocusChanged {
                            // A disabled/returning column can receive transient spatial focus during teardown.
                            if (it.isFocused && active && state.column == ChannelColumn.CHANNELS && state.focusTarget == null) {
                                state.focusedId = entry.id; state.focusedIndex = i; input()
                            }
                        }
                        .onPreviewKeyEvent { event ->
                            if (event.type != KeyEventType.KeyDown || !channelsActive) false
                            else when (event.key) {
                                Key.DirectionLeft -> { state.column = ChannelColumn.CATEGORIES; input(); true }
                                Key.DirectionRight -> { programmes(); true }
                                else -> false
                            }
                        }.focusProperties { up = if (i == 0) closeFocus else FocusRequester.Default
                            down = if (i == channels.lastIndex) FocusRequester.Cancel else FocusRequester.Default }
                        .semantics { contentDescription = "$number, $name, $second"; stateDescription = description
                            customActions = listOf(CustomAccessibilityAction(label = programmesLabel) { state.focusedId = entry.id; programmes(); true }) }
                        .testTag("player-channel:${entry.id}"),
                        click = { pick(entry.id, state.scope) { returnRow() } },
                        longClick = { input(); quickReturn = entry.id to i; quick = entry; afterQuick = {} }) {
                        Text(number, Modifier.widthIn(min = (if (density.fontScale >= 1.3f) 42 else 30).dp),
                            color = LocalContentColor.current.copy(alpha = .7f), fontFamily = Roboto, fontSize = 14.sp, maxLines = 1)
                        Artwork(entry.sourceId, entry.artwork.takeUnless { locked }, name, Modifier.size(56.dp, 32.dp), logo = true, logoInset = 0.dp)
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text((if (entry.id in s.favoriteIds) "★ " else "") + name, Modifier.weight(1f), color = LocalContentColor.current,
                                    fontFamily = Roboto, fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                                if (locked) LockMark(color = LocalContentColor.current)
                                else if (current) Box(Modifier.size(6.dp).background(Ink.Mint, RoundedCornerShape(50)))
                            }
                            Text(second, color = if (current && failed) Ink.Error else LocalContentColor.current.copy(alpha = .7f),
                                fontFamily = Roboto, fontSize = 13.sp, lineHeight = 17.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            if (programme != null) Progress(programme, now)
                            if (sources.size > 1) Text(sources[entry.sourceId].orEmpty(), color = LocalContentColor.current.copy(alpha = .6f),
                                fontFamily = Roboto, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
            }
            if (density.fontScale < 1.3f) Text(stringResource(R.string.panel_hint), Modifier.padding(top = 10.dp),
                color = Ink.Muted, fontFamily = Roboto, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (state.column == ChannelColumn.PROGRAMMES) model.liveEntry(miniId)?.let { entry ->
            MiniGuide(entry, if (single) channelWidth else 320.dp, s, model, active, anchor, playingId,
                pick = { returnFocus -> pick(entry.id, state.scope, returnFocus) }, back = state::channels, close = close)
        }
    }
    quick?.let { entry ->
        fun dismiss() { quick = null }
        QuickActions(entry, quickActions(entry, entry.id in s.favoriteIds,
            toggleFavorite = { model.toggleFavorite(entry) }, open = { pick(entry.id, state.scope) { returnRow() } }, close = ::dismiss,
            locked = entry.id.takeIf { s.parental.status == ParentalStatus.ON }?.let { it in s.locks.contents },
            toggleLock = { lock -> if (model.setTitleLock(entry, lock)) openPin { returnRow() } }) +
            QuickAction("programmes", R.string.panel_programmes, "guide") { afterQuick = { programmes() }; dismiss() }, ::dismiss)
    }
}

@Composable private fun PanelItem(selected: Boolean, active: Boolean, modifier: Modifier,
    click: () -> Unit, longClick: (() -> Unit)? = null, mint: Boolean = false, content: @Composable RowScope.() -> Unit) {
    Surface(onClick = { if (active) click() }, onLongClick = longClick?.let { { if (active) it() } },
        modifier = modifier.fillMaxWidth().heightIn(min = 52.dp).focusProperties { canFocus = active }.semantics { this.selected = selected },
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
        colors = ClickableSurfaceDefaults.colors(containerColor = if (selected) Ink.Selected else Ink.Background,
            contentColor = Ink.Text, focusedContainerColor = if (mint) Ink.Mint else Ink.High,
            focusedContentColor = if (mint) Ink.OnMint else Ink.Text),
        border = ClickableSurfaceDefaults.border(focusedBorder = focusRing(12.dp)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f)) {
        Row(Modifier.fillMaxWidth().heightIn(min = 52.dp).padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

@Composable private fun ScopeColumn(choices: List<ScopeChoice>, selected: ChannelScope,
    width: androidx.compose.ui.unit.Dp, active: Boolean, locks: ParentalLocks, choose: (ChannelScope) -> Unit, back: () -> Unit) {
    val selectedIndex = remember(choices, selected) { choices.indexOfFirst { it.scope == selected }.coerceAtLeast(0) }
    val list = rememberLazyListState(selectedIndex)
    val initial = remember { FocusRequester() }
    LaunchedEffect(active) { if (active) { withFrameNanos { }; runCatching { initial.requestFocus() } } }
    Column(Modifier.width(width).fillMaxHeight().padding(horizontal = 16.dp, vertical = 22.dp).testTag("channel-categories")) {
        Text(stringResource(R.string.settings_categories), color = Ink.Text, fontFamily = Roboto, fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 12.dp))
        LazyColumn(state = list, contentPadding = PaddingValues(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            itemsIndexed(choices, key = { _, choice -> choice.scope.category?.let { "category:$it" } ?: if (choice.scope.favoritesOnly) "favorites" else "all" }) { i, choice ->
                val label = scopeName(choice.scope)
                val locked = choice.scope.category?.let { locks.unknown || CategoryVisibility.key(MediaKind.LIVE, it) in locks.categories } == true
                val count = pluralStringResource(R.plurals.panel_channel_count, choice.count, choice.count)
                val lockLabel = stringResource(R.string.lock_state)
                PanelItem(choice.scope == selected, active,
                    (if (i == selectedIndex) Modifier.focusRequester(initial) else Modifier)
                        .onPreviewKeyEvent { if (it.type == KeyEventType.KeyDown && it.key == Key.DirectionRight) { back(); true } else false }
                        .semantics { contentDescription = "$label, $count"; if (locked) stateDescription = lockLabel }
                        .testTag("channel-scope:${if (choice.scope.favoritesOnly) "@favorites" else choice.scope.category ?: "@all"}"),
                    click = { choose(choice.scope) }, mint = true) {
                    Text(label, Modifier.weight(1f), color = LocalContentColor.current, fontFamily = Roboto,
                        fontSize = 13.sp, lineHeight = 18.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    if (locked) LockMark(color = LocalContentColor.current)
                    Text(choice.count.toString(), color = LocalContentColor.current.copy(alpha = .7f), fontFamily = Roboto, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable private fun MiniGuide(entry: MediaEntry, width: androidx.compose.ui.unit.Dp, s: TvState, model: TvViewModel,
    active: Boolean, anchor: Long, playingId: String, pick: (() -> FocusRequester?) -> GateResult, back: () -> Unit, close: () -> Unit) {
    val now = guideClock()
    val zone = model.guideZone()
    val locked = s.locks.asksPin(entry.id)
    var slice by remember(entry.id, anchor, s.guideRevision, s.guideZoneId, locked) { mutableStateOf<GuideSlice?>(null) }
    var detail by remember(entry.id) { mutableStateOf<EpgProgramme?>(null) }
    val focus = remember { FocusRequester() }
    val window = LocalWindowInfo.current
    val attached = remember { mutableStateMapOf<Long, FocusRequester>() }
    var detailTarget by remember { mutableStateOf<Long?>(null) }
    var returnDetail by remember { mutableStateOf(false) }
    LaunchedEffect(entry.id, anchor, s.guideRevision, s.guideZoneId, locked, active) {
        if (active && !locked) {
            model.ensureGuide(listOf(entry.sourceId))
            slice = model.guideRows(listOf(entry), GuideWindow(anchor, anchor + 12 * 3_600_000L))[entry.id]
        }
    }
    val programmes = remember(slice, anchor) { slice?.programmes.orEmpty().filter { it.end?.let { end -> end > now } ?: (it.start >= now) }.take(8) }
    LaunchedEffect(programmes, locked, active) { if (active) { withFrameNanos { }; runCatching { focus.requestFocus() } } }
    LaunchedEffect(detail, returnDetail) { if (detail == null && returnDetail) {
        refocusAfterDialog(window) { attached[detailTarget] ?: focus }; returnDetail = false
    } }
    Column(Modifier.width(width).fillMaxHeight().padding(horizontal = 20.dp, vertical = 22.dp)
        .onPreviewKeyEvent { if (it.type == KeyEventType.KeyDown && it.key == Key.DirectionLeft) { back(); true } else false }
        .testTag("channel-mini-guide")) {
        Text(stringResource(R.string.panel_programmes), color = Ink.Mint, fontFamily = Roboto, fontSize = 13.sp)
        Text(rememberShownTitle(entry.title, "").name, Modifier.padding(top = 4.dp, bottom = 16.dp), color = Ink.Text,
            fontFamily = Roboto, fontSize = 16.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        if (locked || programmes.isEmpty()) {
            Text(stringResource(if (locked) R.string.panel_guide_locked else guideEmptyText(s.guideStatus[entry.sourceId], slice)),
                color = Ink.Muted, fontFamily = Roboto, fontSize = 14.sp, lineHeight = 20.sp)
            Spacer(Modifier.height(16.dp))
            Action(stringResource(if (locked) R.string.panel_open_channel else R.string.action_back), Modifier.focusRequester(focus)
                .focusProperties { canFocus = active }.testTag("channel-mini-action")) { if (locked) pick { focus } else back() }
        } else {
            if (slice?.programmes?.count { it.isOnAt(now) }?.let { it > 1 } == true)
                Text(stringResource(R.string.guide_overlapping_now), color = Ink.Muted, fontFamily = Roboto,
                    fontSize = 13.sp, lineHeight = 18.sp, modifier = Modifier.padding(bottom = 8.dp))
            LazyColumn(contentPadding = PaddingValues(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            itemsIndexed(programmes, key = { i, p -> "$i:${p.start}" }) { i, programme ->
                val own = remember { FocusRequester() }
                val requester = if (i == 0) focus else own
                DisposableEffect(programme.start) { attached[programme.start] = requester; onDispose { attached.remove(programme.start) } }
                val current = slice?.now(now) == programme
                val times = programmeTime(programme, zone)
                PanelItem(current, active, Modifier.focusRequester(requester)
                    .semantics { contentDescription = "$times, ${programme.title}" }
                    .testTag("channel-programme:$i"), click = {
                        if (current) {
                            if (entry.id == playingId) close()
                            else if (pick { requester } is GateResult.Allow) back()
                        } else { detailTarget = programme.start; detail = programme; returnDetail = true }
                    }) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text((if (current) stringResource(R.string.guide_now) + " · " else "") + times, color = LocalContentColor.current.copy(alpha = .7f),
                            fontFamily = Roboto, fontSize = 12.sp)
                        Text(programme.title, color = LocalContentColor.current, fontFamily = Roboto, fontSize = 14.sp, lineHeight = 19.sp,
                            maxLines = 2, overflow = TextOverflow.Ellipsis)
                        if (current) Progress(programme, now)
                    }
                }
            }
        }
        }
    }
    detail?.takeUnless { locked }?.let { programme ->
        val reader = remember { FocusRequester() }
        val play = remember { FocusRequester() }
        SourceDialog(stringResource(R.string.guide_programme_detail), reader, { detail = null }, width = 600.dp) {
            val note = when {
                programme.start > now -> R.string.guide_detail_upcoming
                programme.end?.let { it <= now } == true -> R.string.guide_detail_ended
                else -> R.string.guide_detail_live_only
            }
            val text = programme.title + "\n" + programmeTime(programme, zone) + "\n\n" +
                programme.description.ifBlank { stringResource(R.string.guide_detail_no_description) } + "\n\n" +
                stringResource(note)
            RemoteTextReader(text, Modifier.weight(1f, fill = false).focusRequester(reader).focusProperties { down = play }, "channel-programme-reader")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Action(stringResource(R.string.panel_watch_channel), Modifier.focusRequester(play).focusProperties { up = reader }
                    .testTag("channel-programme-play"), primary = true) { detail = null; if (pick { attached[detailTarget] ?: focus } is GateResult.Allow) back() }
                Action(stringResource(R.string.action_close), Modifier.focusProperties { up = reader }) { detail = null }
            }
        }
    }
    if (locked && detail != null) LaunchedEffect(Unit) { detail = null }
}
