package dev.iptvplayer.tv.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.focus.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import coil3.compose.AsyncImage
import dev.iptvplayer.tv.*
import dev.iptvplayer.tv.data.*

/**
 * Side menu around a screen's [content]. At rest it is an icon rail; while it has focus it opens over the
 * content with labels, so the content is never laid out again (MOT04). Focusing an item never changes
 * the screen; OK opens it. Every entry into the menu (Left from the content's first column, a Back that
 * returns to the menu) lands on the current section. Right returns to the content item that had focus.
 */
@Composable internal fun Navigation(s: TvState, model: TvViewModel, modifier: Modifier = Modifier,
    first: FocusRequester? = null, searchFocus: FocusRequester? = null, guideFocus: FocusRequester? = null,
    activeFocus: FocusRequester? = null, contentEntry: (() -> FocusRequester?)? = null, contentTop: Dp = TvLayout.HeaderTop,
    settings: () -> Unit, content: @Composable () -> Unit) {
    val defaults = remember { List(7) { FocusRequester() } }
    val contentFocus = remember { FocusRequester() }
    // Requester index: 0 Ana Sayfa, 1 Canlı TV, 2 Filmler, 3 Diziler, 4 Favoriler, 5 Ara, 6 Ayarlar.
    val libraryIndex = if (s.onlyFavorites) 4 else when (s.kind) {
        MediaKind.LIVE -> 1; MediaKind.MOVIE -> 2; MediaKind.SERIES -> 3; else -> 0 }
    val activeIndex = when (s.route) {
        Route.LIBRARY -> libraryIndex
        Route.SEARCH -> 5
        Route.GUIDE -> 1
        Route.DETAIL -> when (s.selected?.kind) { MediaKind.LIVE -> 1; MediaKind.MOVIE -> 2; MediaKind.SERIES, MediaKind.EPISODE -> 3; else -> 0 }
        else -> 0
    }
    // [activeFocus] binds the library tab that carries the active marker (same rule as NavItem.active).
    val targets = listOf(first ?: defaults[0], guideFocus ?: defaults[1], defaults[2], defaults[3], defaults[4], searchFocus ?: defaults[5], defaults[6])
        .mapIndexed { index, target -> if (s.route == Route.LIBRARY && index == libraryIndex && activeFocus != null) activeFocus else target }
    val activeTarget by rememberUpdatedState(targets[activeIndex])
    // Top to bottom: Ara, the sections, then Ayarlar on its own at the bottom.
    val order = listOf(5, 0, 1, 2, 3, 4, 6)
    val itemModifiers = remember(targets, contentFocus) {
        order.indices.associate { position ->
            order[position] to Modifier.focusRequester(targets[order[position]]).focusProperties {
                up = if (position == 0) FocusRequester.Cancel else targets[order[position - 1]]
                down = if (position == order.lastIndex) FocusRequester.Cancel else targets[order[position + 1]]
                left = FocusRequester.Cancel
                // Right is handled by the menu's key handler below.
                right = FocusRequester.Cancel
            }
        }
    }
    val density = LocalDensity.current
    // Items keep one height in both states and their icon centre on the rail's centre line (inset + height / 2).
    val itemHeight = maxOf(40.dp, with(density) { 18.sp.toDp() } + 16.dp)
    val inset = (TvLayout.RailWidth - itemHeight) / 2
    // D029: the open menu widens for a label that does not fit (large text on linear font scaling).
    val measurer = rememberTextMeasurer()
    val widestLabel = remember(density, measurer) {
        listOf("Ara", "Ana Sayfa", "Canlı TV", "Filmler", "Diziler", "Favoriler", "Ayarlar").maxOf {
            with(density) { measurer.measure(it, TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Medium, fontSize = 13.sp,
                lineHeight = 18.sp, letterSpacing = 0.sp), softWrap = false).size.width.toDp() }
        }
    }
    val openWidth = maxOf(TvLayout.RailOpenWidth, TvLayout.RailWidth + 14.dp + widestLabel + 16.dp)
    var open by remember { mutableStateOf(false) }
    val reducedMotion = LocalReducedMotion.current
    // Read in layout and draw only: the open/close motion does not recompose the menu every frame.
    val width = animateDpAsState(if (open) openWidth else TvLayout.RailWidth,
        if (reducedMotion) snap() else tween(if (open) 160 else 120, easing = FastOutSlowInEasing), label = "rail")
    // Layout follows the panel edge, so labels stay until the closing panel has passed them.
    val expanded by remember { derivedStateOf { width.value > TvLayout.RailWidth + 0.5.dp } }
    // Right returns to the item the screen remembers ([contentEntry]: its last focused card, field or grid).
    // Compose's own restoration stops at the first lazy list, so it is not used here. Without a remembered
    // item, or while that item is not attached, the content's first item takes focus.
    val entry by rememberUpdatedState(contentEntry)
    fun enterContent() {
        val restored = try { entry?.invoke()?.requestFocus() == true } catch (_: IllegalStateException) { false }
        if (!restored) contentFocus.requestFocus()
    }
    Box(Modifier.fillMaxSize().background(Ink.Background)) {
        Box(Modifier.fillMaxSize().padding(start = TvLayout.RailWidth, top = contentTop)
            .focusRequester(contentFocus).focusGroup()
            // The content slides right by as much as the menu opens. A layer translation only: nothing is
            // measured or laid out again (MOT04), and its right edge simply leaves the screen.
            .graphicsLayer { translationX = (width.value - TvLayout.RailWidth).toPx() }) { content() }
        Column(modifier.layout { measurable, constraints ->
                val w = width.value.roundToPx()
                val placeable = measurable.measure(constraints.copy(minWidth = w, maxWidth = w))
                layout(w, placeable.height) { placeable.place(0, 0) }
            }.fillMaxHeight().background(Ink.Background).clipToBounds()
            .onFocusChanged { open = it.hasFocus }
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown && event.key == Key.DirectionRight) { enterContent(); true } else false
            }
            // Remote entry lands on the current section; an explicit request (Settings closing, a Back
            // handler, a test) keeps the item it asked for.
            .focusProperties { onEnter = { if (requestedFocusDirection != FocusDirection.Enter) activeTarget.requestFocus() } }.focusGroup()
            .testTag("navigation-bar").padding(vertical = 20.dp)) {
            Row(Modifier.fillMaxWidth().padding(start = 16.dp).heightIn(min = 40.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(40.dp), contentAlignment = Alignment.Center) {
                    Text("›", color = Ink.Mint, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
                if (expanded) Text("IPTV", color = Ink.Text, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp,
                    fontFamily = Roboto, maxLines = 1, softWrap = false)
            }
            Spacer(Modifier.weight(1f))
            Column(Modifier.fillMaxWidth().testTag("navigation-items").verticalScroll(rememberScrollState())
                .padding(horizontal = inset, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                NavItem("Ara", "search", s.route == Route.SEARCH, open, expanded, itemHeight, itemModifiers.getValue(5)) { model.search() }
                NavItem("Ana Sayfa", "home", s.route == Route.HOME, open, expanded, itemHeight, itemModifiers.getValue(0)) { model.home() }
                listOf(MediaKind.LIVE to "live_tv", MediaKind.MOVIE to "movie", MediaKind.SERIES to "video_library").forEachIndexed { index, (kind, icon) ->
                    NavItem(kind.label, icon, (s.route == Route.LIBRARY && s.kind == kind && !s.onlyFavorites) ||
                        (s.route == Route.GUIDE && kind == MediaKind.LIVE) || (s.route == Route.DETAIL && activeIndex == index + 1),
                        open, expanded, itemHeight, itemModifiers.getValue(index + 1)) { model.showKind(kind) }
                }
                NavItem("Favoriler", "star", s.route == Route.LIBRARY && s.onlyFavorites, open, expanded, itemHeight, itemModifiers.getValue(4)) { model.favorites() }
            }
            Spacer(Modifier.weight(1f))
            Box(Modifier.fillMaxWidth().padding(horizontal = inset)) {
                NavItem("Ayarlar", "settings", false, open, expanded, itemHeight, itemModifiers.getValue(6), onClick = settings)
            }
        }
    }
}

@Composable private fun NavItem(label: String, icon: String, active: Boolean, open: Boolean, expanded: Boolean, height: Dp,
    modifier: Modifier = Modifier, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    Button(onClick, modifier.onFocusChanged { focused = it.isFocused }.height(height)
        .then(if (expanded) Modifier.fillMaxWidth() else Modifier.width(height))
        .testTag("nav:$label").semantics { selected = active; if (!open) contentDescription = label },
        shape = ButtonDefaults.shape(shape = RoundedCornerShape(50)),
        colors = ButtonDefaults.colors(containerColor = if (active) Ink.Selected else Color.Transparent,
            contentColor = Ink.Text, focusedContainerColor = Ink.Text, focusedContentColor = Ink.Background),
        border = ButtonDefaults.border(focusedBorder = Border(BorderStroke(TvLayout.Focus, Ink.Text), inset = 3.dp)),
        scale = ButtonDefaults.scale(focusedScale = 1f), contentPadding = PaddingValues(horizontal = (height - 20.dp) / 2)) {
        Row(Modifier.fillMaxHeight(), verticalAlignment = Alignment.CenterVertically) {
            // Material Symbols Rounded; the current section uses the filled form.
            MenuSymbol(icon, filled = active, color = if (active && !focused) Ink.Mint else LocalContentColor.current, modifier = Modifier.size(22.dp))
            if (expanded) Text(label, Modifier.padding(start = 14.dp).testTag("nav-label:$label"), color = LocalContentColor.current,
                fontSize = 13.sp, lineHeight = 18.sp, fontFamily = Roboto, fontWeight = FontWeight.Medium, letterSpacing = 0.sp,
                maxLines = 1, softWrap = false)
        }
    }
}

private class FocusMemory { var requester: FocusRequester? = null }

private data class HomeRow(val key: String, val title: String, val entries: List<MediaEntry>,
    val kind: MediaKind? = null, val continuing: List<ContinueItem> = emptyList())

@Composable internal fun HomeScreen(s: TvState, model: TvViewModel, settings: () -> Unit) {
    val library = remember(s.catalog, s.history, s.favoriteIds) { homeLibrary(s.catalog, s.history, s.favoriteIds) }
    val rows = remember(library) {
        buildList {
            if (library.continueWatching.isNotEmpty()) add(HomeRow("continue", "İzlemeye devam et", library.continueWatching.map { it.entry }, continuing = library.continueWatching))
            if (library.recentChannels.isNotEmpty()) add(HomeRow("recent", "Son izlediğin kanallar", library.recentChannels, MediaKind.LIVE))
            if (library.favorites.isNotEmpty()) add(HomeRow("favorites", "Favorilerin", library.favorites))
            if (library.channels.isNotEmpty()) add(HomeRow("live", "Canlı yayınlar", library.channels, MediaKind.LIVE))
            if (library.movies.isNotEmpty()) add(HomeRow("movies", "Film kütüphanen", library.movies, MediaKind.MOVIE))
            if (library.series.isNotEmpty()) add(HomeRow("series", "Dizi kütüphanen", library.series, MediaKind.SERIES))
        }
    }
    val focus = remember { FocusRequester() }
    val nav = remember { FocusRequester() }
    // Back from the content opens the side menu on Ana Sayfa; Back from the menu asks before leaving (PilotApp).
    val menuFocused = remember { mutableStateOf(false) }
    HomeBackToMenu(model, { menuFocused.value }, nav)
    // The last focused card (or the hero) for Right from the side menu. A plain holder, not state:
    // moving focus must not recompose the screen.
    val heroFocus = remember { FocusRequester() }
    val lastFocus = remember { FocusMemory() }
    val hero = library.featured
    val validKeys = rows.flatMap { r -> r.entries.map { "${r.key}:${it.id}" } }.toSet() + if (hero != null) setOf("hero") else emptySet()
    val target = remember { model.homeFocusKey?.takeIf { it in validKeys } ?: when {
        library.continueWatching.isNotEmpty() -> "continue:${library.continueWatching.first().entry.id}"
        library.recentChannels.isNotEmpty() -> "recent:${library.recentChannels.first().id}"
        hero != null -> "hero"
        else -> rows.firstOrNull()?.let { "${it.key}:${it.entries.first().id}" }
    } }
    val startRow = rows.indexOfFirst { r -> r.entries.any { "${r.key}:${it.id}" == target } }
    val list = rememberLazyListState(initialFirstVisibleItemIndex = if (startRow >= 2) startRow + 1 else 0)
    val density = LocalDensity.current
    var viewportHeight by remember { mutableStateOf(0.dp) }
    val shelfHeights = remember { mutableStateMapOf<String, Dp>() }
    Navigation(s, model, Modifier.onFocusChanged { menuFocused.value = it.hasFocus }, first = nav,
        contentEntry = { lastFocus.requester }, settings = settings) { Column(Modifier.fillMaxSize()) {
        if (rows.isEmpty()) {
            // 19 + 5 dp: content on the 24 dp edge of the logo and shelves; 5 dp stays as focus-ring room.
            Column(Modifier.weight(1f).fillMaxWidth().padding(horizontal = TvLayout.Gutter - 5.dp, vertical = TvLayout.Gutter)
                .verticalScroll(rememberScrollState()).padding(5.dp).testTag("home-empty"), verticalArrangement = Arrangement.Center) {
                val warning = s.catalogNotice?.takeIf { it.needsAttention }?.message
                Heading(when {
                    s.refreshing -> "Kütüphanen hazırlanıyor"
                    warning != null -> "İçerikler gösterilemiyor"
                    else -> "Kütüphanen için yer hazır"
                })
                Spacer(Modifier.height(14.dp))
                Note(if (s.refreshing) s.progressLabel else warning ?: if (s.catalog.items.any { !it.adult })
                    "Ana sayfada öne çıkarılacak içerik bulunamadı. Tüm kayıtlarını soldaki menüden bölümlere girerek açabilirsin."
                    else "Kaynağında gösterilebilen içerik bulunamadı. Kaynağını kontrol edebilir veya yenileyebilirsin.", error = warning != null && !s.refreshing)
                Spacer(Modifier.height(16.dp))
                Action("Kaynakları kontrol et", Modifier.testTag("home-empty-action"), primary = true) { model.manageSources() }
            }
        } else LazyColumn(Modifier.weight(1f).fillMaxWidth().testTag("home-content")
            .onSizeChanged { viewportHeight = with(density) { it.height.toDp() } }
            .drawWithContent {
                drawContent()
                // Soften tiny remnants from the previous shelf at the top edge of the content.
                // The focused tile already reserves at least 7dp above its whole container.
                if (list.firstVisibleItemIndex > 0 || list.firstVisibleItemScrollOffset > 0) {
                    val edge = 7.dp.toPx()
                    drawRect(Brush.verticalGradient(0f to Ink.Background, .55f to Ink.Background,
                        1f to Color.Transparent, endY = edge),
                        size = androidx.compose.ui.geometry.Size(size.width, edge))
                }
                // Fade the next shelf into the background instead of slicing its captions at the screen edge.
                // 20 dp stays inside the 27 dp margin CardVisibility keeps below a focused tile or heading action.
                if (list.canScrollForward) {
                    val fade = 20.dp.toPx()
                    drawRect(Brush.verticalGradient(0f to Ink.Background.copy(alpha = 0f), 1f to Ink.Background,
                        startY = size.height - fade, endY = size.height),
                        topLeft = Offset(0f, size.height - fade), size = androidx.compose.ui.geometry.Size(size.width, fade))
                }
            }, state = list,
            verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(bottom = maxOf(28.dp, viewportHeight - (shelfHeights[rows.lastOrNull()?.key] ?: 0.dp) + 7.dp))) {
            item(key = "hero") {
                if (hero != null) Hero(hero, Modifier.then(if (target == "hero") Modifier.focusRequester(focus) else Modifier)
                    .focusRequester(heroFocus)
                    .onFocusChanged { if (it.hasFocus) { model.homeFocus("hero"); lastFocus.requester = heroFocus } }, openLive = { model.showKind(MediaKind.LIVE) }) { model.open(hero) }
                else Column(Modifier.padding(horizontal = TvLayout.Gutter, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("SENİN TELEVİZYONUN", color = Ink.Mint, fontFamily = Roboto, fontSize = 12.sp, letterSpacing = 2.sp)
                    Heading("Şimdi ne izleyelim?")
                    Note("Kanalların ve favorilerin, bıraktığın yerde.")
                }
                if (s.catalog.warnings.isNotEmpty()) Note(s.catalog.warnings.joinToString(" "), error = true, modifier = Modifier.padding(horizontal = TvLayout.Gutter, vertical = 8.dp))
                s.catalogNotice?.takeIf { it.needsAttention }?.let { Note(it.message, error = true,
                    modifier = Modifier.padding(horizontal = TvLayout.Gutter, vertical = 8.dp)) }
            }
            items(rows, key = { it.key }) { row ->
                var headingHeight by remember { mutableStateOf(0.dp) }
                Column(Modifier.onSizeChanged {
                    shelfHeights[row.key] = with(density) { it.height.toDp() }
                }.padding(top = if (row == rows.first()) 0.dp else 16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Every heading row keeps the compact action's height (it held "Tümünü gör"), so the shelf rhythm is unchanged.
                    Row(Modifier.fillMaxWidth().heightIn(min = actionHeight(compact = true)).padding(horizontal = TvLayout.Gutter)
                        .testTag("shelf-heading:${row.key}").onSizeChanged { headingHeight = with(density) { it.height.toDp() } }, verticalAlignment = Alignment.CenterVertically) {
                        Text(row.title, Modifier.weight(1f), color = Ink.Text, fontFamily = Roboto, fontWeight = FontWeight.Medium, fontSize = 17.sp, lineHeight = 22.sp)
                    }
                    val rowStart = row.entries.indexOfFirst { "${row.key}:${it.id}" == target }.coerceAtLeast(0)
                    val rowState = rememberLazyListState(initialFirstVisibleItemIndex = rowStart)
                    // A row that mixes movies and episodes reserves the episode line on every card.
                    val reserveEpisode = row.continuing.any { it.record.kind == MediaKind.EPISODE }
                    LazyRow(state = rowState, horizontalArrangement = Arrangement.spacedBy(TvLayout.Gap), contentPadding = PaddingValues(horizontal = TvLayout.Gutter, vertical = 4.dp)) {
                        items(row.entries, key = { it.id }) { entry ->
                            val key = "${row.key}:${entry.id}"
                            val own = remember(key) { FocusRequester() }
                            val continuing = row.continuing.firstOrNull { it.entry.id == entry.id }
                            HomeCard(entry, continuing?.record, if (s.sources.size > 1) s.sourceName(entry.sourceId) else null,
                                Modifier.then(if (key == target) Modifier.focusRequester(focus) else Modifier)
                                    .focusRequester(own)
                                    .onFocusChanged { if (it.isFocused) { model.homeFocus(key); lastFocus.requester = own } }, row.key, headingHeight + 10.dp, viewportHeight, reserveEpisode) {
                                model.homeFocus(key)
                                model.open(entry, continuing?.record)
                            }
                        }
                    }
                }
            }
        }
    } }
    LaunchedEffect(Unit) {
        withFrameNanos { }; withFrameNanos { }
        val rowKey = if (startRow >= 0) rows[startRow].key else "hero"
        if (target != null && list.layoutInfo.visibleItemsInfo.none { it.key == rowKey }) {
            list.scrollToItem(if (startRow >= 0) startRow + 1 else 0)
            withFrameNanos { }; withFrameNanos { }
        }
        if (target != null) focus.requestFocus() else nav.requestFocus()
    }
    LaunchedEffect(s.revision, validKeys) {
        if (model.homeFocusKey != null && model.homeFocusKey !in validKeys) {
            withFrameNanos { }; withFrameNanos { }
            nav.requestFocus()
        }
    }
}

/** Reads the menu focus here alone, so opening and closing the menu does not recompose Home. A Back that arrives after
 *  the model left Home, before this screen is gone, is the new route's: PilotApp's route-level Back, model.back() off Home. */
@Composable private fun HomeBackToMenu(model: TvViewModel, menuFocused: () -> Boolean, menu: FocusRequester) {
    BackHandler(enabled = !menuFocused()) {
        if (model.state.value.route != Route.HOME) model.back() else try { menu.requestFocus() } catch (_: IllegalStateException) { }
    }
}

@Composable private fun Hero(entry: MediaEntry, actionModifier: Modifier, openLive: () -> Unit, open: () -> Unit) {
    val largeText = LocalDensity.current.fontScale >= 1.5f
    val hasVisual = entry.hasBackdrop || entry.artwork != null
    val visibility = rememberCardVisibility()
    Box(Modifier.padding(horizontal = TvLayout.Gutter).fillMaxWidth().heightIn(min = TvLayout.HeroHeight)
        .then(visibility.container).then(visibility.focus)
        .clip(RoundedCornerShape(TvLayout.HeroRadius)).background(Ink.Container).testTag("home-hero")) {
        if (entry.hasBackdrop) Box(Modifier.matchParentSize()) {
            AsyncImage(SourceArtwork(entry.sourceId, entry.backdrop!!), null,
                Modifier.align(Alignment.CenterEnd).fillMaxHeight().fillMaxWidth(.78f),
                contentScale = ContentScale.Crop, alignment = Alignment.TopCenter)
        }
        Box(Modifier.matchParentSize().background(Brush.horizontalGradient(
            0f to Ink.Background, .28f to Ink.Background.copy(alpha = .96f),
            .48f to Ink.Background.copy(alpha = .82f), .72f to Ink.Background.copy(alpha = .22f),
            1f to Color.Transparent)))
        Box(Modifier.matchParentSize().background(Brush.verticalGradient(
            0f to Color.Transparent, .6f to Color.Transparent, 1f to Ink.Background.copy(alpha = .65f))))
        // A poster-only entry is drawn above the scrims, in its own colours, as on the detail screen.
        if (!entry.hasBackdrop && entry.artwork != null) Box(Modifier.matchParentSize()) {
            Box(Modifier.align(Alignment.CenterEnd).fillMaxHeight().width(220.dp).padding(top = if (largeText) 76.dp else 62.dp, bottom = 16.dp, end = 28.dp)) {
                Artwork(entry.sourceId, entry.artwork, entry.title, Modifier.align(Alignment.CenterEnd).fillMaxHeight()
                    .aspectRatio(2f / 3).clip(RoundedCornerShape(TvLayout.CardRadius)).testTag("home-hero-poster"))
            }
        }
        Column(Modifier.fillMaxWidth().heightIn(min = TvLayout.HeroHeight).padding(horizontal = 28.dp, vertical = 20.dp), verticalArrangement = Arrangement.SpaceBetween) {
            // The header line keeps the height of the source selector it used to hold, so the title and poster stay put.
            Row(Modifier.fillMaxWidth().heightIn(min = actionHeight(compact = true)), verticalAlignment = Alignment.CenterVertically) {
                Text("KÜTÜPHANENDEN",
                    Modifier.weight(1f).testTag("hero-label"), color = Ink.Mint, fontFamily = Roboto,
                    fontWeight = FontWeight.Medium, fontSize = 10.sp, letterSpacing = 1.2.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Column(Modifier.padding(top = 16.dp).fillMaxWidth(if (!hasVisual) 1f else if (largeText) .62f else .48f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(entry.title, Modifier.testTag("hero-title"), color = Ink.Text, fontFamily = Roboto, fontWeight = FontWeight.Medium,
                    fontSize = if (largeText) 28.sp else 44.sp, lineHeight = if (largeText) 33.sp else 50.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(listOf(entry.kind.label, entry.year).filter { it.isNotBlank() }.joinToString("  ·  "),
                    color = Ink.Muted, fontFamily = Roboto, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (entry.description.isNotBlank()) Text(entry.description, Modifier.testTag("hero-description"), fontFamily = Roboto, fontSize = 12.sp, lineHeight = 17.sp, color = Ink.Muted, maxLines = if (largeText) 1 else 2, overflow = TextOverflow.Ellipsis)
                Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Action("Ayrıntılar", actionModifier.testTag("hero-details"), primary = true, onClick = open)
                    Action("Kanalları aç", Modifier.testTag("hero-live"), outlined = true, onClick = openLive)
                }
            }
        }
    }
}

@Composable private fun HomeCard(entry: MediaEntry, record: WatchRecord?, sourceName: String?, modifier: Modifier, section: String, headingContext: Dp, viewportHeight: Dp,
    reserveEpisode: Boolean = false, open: () -> Unit) {
    val live = entry.kind == MediaKind.LIVE
    // The row's own 24 dp gutter: a focused card stops on the heading edge instead of 7 dp from the screen,
    // which left a strip of the previous card with cut text at the other edge.
    val visibility = rememberCardVisibility(horizontalPadding = TvLayout.Gutter, contextAbove = headingContext, viewportHeight = viewportHeight)
    if (record != null) {
        Box(Modifier.width(TvLayout.ContinueWidth).then(visibility.container)) {
            Card(onClick = open, modifier = modifier.then(visibility.focus).fillMaxWidth()
                .semantics {
                    contentDescription = "Devam et: ${entry.title}"
                    stateDescription = listOfNotNull(sourceName,
                        if (record.kind == MediaKind.EPISODE) "Sezon ${record.season}, bölüm ${record.episode}" else null,
                        "İzleme konumu ${clockTime(record.positionMs)}").joinToString(". ")
                    testTag = "$section:${entry.id}"
                },
                shape = CardDefaults.shape(shape = RoundedCornerShape(TvLayout.CardRadius)),
                colors = CardDefaults.colors(containerColor = Ink.Container, focusedContainerColor = Ink.High),
                scale = CardDefaults.scale(focusedScale = 1f),
                border = CardDefaults.border(focusedBorder = Border(BorderStroke(TvLayout.Focus, Ink.Text), inset = 3.dp))) {
                Box(Modifier.fillMaxWidth()) {
                    Box(Modifier.matchParentSize()) {
                        Box(Modifier.width(76.dp).fillMaxHeight().background(Ink.High), contentAlignment = Alignment.Center) {
                            Artwork(entry.sourceId, entry.artwork ?: entry.backdrop, entry.title, Modifier.fillMaxSize(), showFallbackTitle = false)
                        }
                    }
                    Column(Modifier.fillMaxWidth().padding(start = 90.dp, end = 14.dp, top = 14.dp, bottom = 17.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        // Shared title and episode slots keep card heights and progress bars aligned across the row.
                        Text(entry.title, color = Ink.Text, fontFamily = Roboto, fontWeight = FontWeight.Medium,
                            fontSize = 13.sp, lineHeight = 18.sp, minLines = 2, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        sourceName?.let { Text(it, color = Ink.Muted, fontFamily = Roboto, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                        if (record.kind == MediaKind.EPISODE) Text("S${record.season} · B${record.episode}",
                            color = Ink.Muted, fontFamily = Roboto, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        else if (reserveEpisode) Text(" ", Modifier.clearAndSetSemantics { },
                            color = Ink.Muted, fontFamily = Roboto, fontSize = 11.sp, maxLines = 1)
                        Text(clockTime(record.positionMs),
                            color = Ink.Mint, fontFamily = Roboto, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    if (record.durationMs > 0) Box(Modifier.align(Alignment.BottomStart).fillMaxWidth().height(3.dp).background(Ink.High)) {
                        Box(Modifier.fillMaxHeight().fillMaxWidth((record.positionMs.toFloat() / record.durationMs).coerceIn(0f, 1f)).background(Ink.Mint))
                    }
                }
            }
        }
        return
    }
    Column(Modifier.width(if (live) TvLayout.LiveWidth else TvLayout.PosterWidth).then(visibility.container).testTag("tile:$section:${entry.id}"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Card(onClick = open, modifier = modifier.then(visibility.focus).fillMaxWidth().aspectRatio(if (live) 16f / 9 else 2f / 3)
            .semantics { contentDescription = "${if (section == "continue") "Devam et: " else ""}${entry.title}"; testTag = "$section:${entry.id}" },
            scale = CardDefaults.scale(focusedScale = 1f),
            shape = CardDefaults.shape(shape = RoundedCornerShape(TvLayout.CardRadius)),
            colors = CardDefaults.colors(containerColor = Ink.Container, focusedContainerColor = Ink.High),
            border = CardDefaults.border(focusedBorder = Border(BorderStroke(TvLayout.Focus, Ink.Text), inset = 3.dp))) {
            if (live) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Artwork(entry.sourceId, entry.artwork, entry.title, Modifier.width(130.dp).height(40.dp), true, logoInset = 0.dp)
            } else Box(Modifier.fillMaxSize()) {
                Artwork(entry.sourceId, entry.artwork, entry.title, Modifier.fillMaxSize(), showFallbackTitle = false)
                entry.ratingText?.let { RatingBadge(it, Modifier.align(Alignment.TopStart).padding(7.dp)) }
            }
        }
        Text(entry.title, color = Ink.Text, fontFamily = Roboto, fontSize = 12.sp, minLines = if (live) 1 else 2, maxLines = 2, lineHeight = 17.sp, overflow = TextOverflow.Ellipsis)
        if (!live) Text(entry.year, Modifier.testTag("home-year:$section:${entry.id}"), color = Ink.Muted,
            fontFamily = Roboto, fontSize = 10.sp, lineHeight = 14.sp, minLines = 1, maxLines = 1)
        sourceName?.let { Text(it, color = Ink.Muted, fontFamily = Roboto, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
    }
}
