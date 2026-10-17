package dev.iptvplayer.tv.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.BiasAlignment
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import coil3.SingletonImageLoader
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import dev.iptvplayer.tv.*
import dev.iptvplayer.tv.data.*
import kotlinx.coroutines.delay

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
            .graphicsLayer { translationX = (width.value - TvLayout.RailWidth).toPx() }
            // The open menu dims the content behind it, in draw only, so the labels read as the active layer.
            .drawWithContent {
                drawContent()
                val open = ((width.value - TvLayout.RailWidth) / (openWidth - TvLayout.RailWidth)).coerceIn(0f, 1f)
                if (open > 0f) drawRect(Ink.Background.copy(alpha = .6f * open))
            }) { content() }
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
            // The NEXTV emblem on the rail; the open menu adds the letters beside it, as in the horizontal logo (D066).
            Row(Modifier.fillMaxWidth().padding(start = 16.dp).heightIn(min = 40.dp).semantics(mergeDescendants = true) { },
                verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(40.dp), contentAlignment = Alignment.Center) {
                    androidx.compose.foundation.Image(androidx.compose.ui.res.painterResource(dev.iptvplayer.tv.R.drawable.ic_nextv_splash_mark), "NEXTV",
                        Modifier.size(width = 22.dp * (119f / 148f), height = 22.dp))
                }
                if (expanded) androidx.compose.foundation.Image(androidx.compose.ui.res.painterResource(dev.iptvplayer.tv.R.drawable.nextv_wordmark), null,
                    Modifier.padding(start = 6.dp).size(width = 11.3.dp * (300f / 67f), height = 11.3.dp))
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
            contentColor = Ink.Text, focusedContainerColor = Ink.Mint, focusedContentColor = Ink.OnMint),
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
            // Starter shelves until there is history: watched channels replace "Canlı yayınlar", and
            // "İzlemeye devam et" replaces "Film kütüphanen", so a new or channel-only list never opens empty.
            if (library.recentChannels.isEmpty() && library.channels.isNotEmpty()) add(HomeRow("live", "Canlı yayınlar", library.channels, MediaKind.LIVE))
            if (library.continueWatching.isEmpty() && library.movies.isNotEmpty()) add(HomeRow("movies", "Film kütüphanen", library.movies, MediaKind.MOVIE))
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
    val slides = remember(library) { library.newest.ifEmpty { listOfNotNull(library.featured) } }
    val validKeys = rows.flatMap { r -> r.entries.map { "${r.key}:${it.id}" } }.toSet() + if (slides.isNotEmpty()) setOf("hero") else emptySet()
    // Home opens on the hero's Ayrıntılar (D070); coming back from a title or the player keeps the remembered card.
    val target = remember { model.homeFocusKey?.takeIf { it in validKeys } ?: when {
        slides.isNotEmpty() -> "hero"
        library.continueWatching.isNotEmpty() -> "continue:${library.continueWatching.first().entry.id}"
        library.recentChannels.isNotEmpty() -> "recent:${library.recentChannels.first().id}"
        else -> rows.firstOrNull()?.let { "${it.key}:${it.entries.first().id}" }
    } }
    val startRow = rows.indexOfFirst { r -> r.entries.any { "${r.key}:${it.id}" == target } }
    val list = rememberLazyListState(initialFirstVisibleItemIndex = if (startRow >= 2) startRow + 1 else 0)
    val density = LocalDensity.current
    var viewportHeight by remember { mutableStateOf(0.dp) }
    val shelfHeights = remember { mutableStateMapOf<String, Dp>() }
    // Held OK on a card: the quick actions (U8); focus returns to that card.
    var quick by remember { mutableStateOf<MediaEntry?>(null) }
    var refocus by remember { mutableStateOf<FocusRequester?>(null) }
    val window = LocalWindowInfo.current
    LaunchedEffect(refocus) { refocus?.let { target -> refocusAfterDialog(window) { target }; refocus = null } }
    // The hero runs to the top edge (D067); without it the page keeps the usual top margin.
    Navigation(s, model, Modifier.onFocusChanged { menuFocused.value = it.hasFocus }, first = nav,
        contentTop = if (slides.isNotEmpty() && rows.isNotEmpty()) 0.dp else TvLayout.HeaderTop,
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
                if (slides.isNotEmpty()) Hero(slides, newest = library.newest.isNotEmpty(), reducedMotion = s.reducedMotion,
                    Modifier.then(if (target == "hero") Modifier.focusRequester(focus) else Modifier)
                    .focusRequester(heroFocus)
                    .onFocusChanged { if (it.hasFocus) { model.homeFocus("hero"); lastFocus.requester = heroFocus } },
                    detail = model::heroDetail, ensure = model::ensureHeroDetail) { model.open(it) }
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
                                    .onFocusChanged { if (it.isFocused) { model.homeFocus(key); lastFocus.requester = own } }, row.key, headingHeight + 10.dp, viewportHeight, reserveEpisode,
                                longClick = { quick = entry; refocus = null }) {
                                model.homeFocus(key)
                                model.open(entry, continuing?.record)
                            }
                        }
                    }
                }
            }
        }
    } }
    quick?.let { entry ->
        QuickActions(entry, entry.id in s.favoriteIds, toggleFavorite = { model.toggleFavorite(entry) }, open = { model.open(entry) },
            dismiss = { quick = null; refocus = lastFocus.requester })
    }
    LaunchedEffect(Unit) {
        // A remembered key whose starter shelf gave way to its personal row (the first watch) already fell back to
        // the new target; record that before the stale-key effect below looks, so focus does not jump to the menu.
        if (target != null) model.homeFocus(target)
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

/**
 * The hero carousel (D067): a large, full-width picture from the top edge down to the shelves. A slide with a wide
 * picture fills the hero with it; a poster-only slide fills it with its own poster, much enlarged and softened, and
 * shows the poster beside the text. Films from an Xtream source get their wide picture from their details
 * ([detail], fetched through [ensure] for the slide on screen and the next). Slides advance every 8 s (15 s while
 * Ayrıntılar has focus, none with TalkBack or reduced motion); Right and Left on Ayrıntılar step through them.
 * The action is one composable across slides, so focus never leaves it.
 */
@Composable private fun Hero(slides: List<MediaEntry>, newest: Boolean, reducedMotion: Boolean, actionModifier: Modifier,
    detail: (MediaEntry) -> MediaEntry, ensure: (MediaEntry) -> Unit, open: (MediaEntry) -> Unit) {
    val largeText = LocalDensity.current.fontScale >= 1.5f
    val motion = !reducedMotion && android.animation.ValueAnimator.areAnimatorsEnabled()
    val many = slides.size > 1
    val context = LocalContext.current
    // With TalkBack the slides turn only by remote: the spoken text must not change under the viewer.
    val spoken = remember(context) { context.getSystemService(android.view.accessibility.AccessibilityManager::class.java)?.isTouchExplorationEnabled == true }
    var actionFocused by remember { mutableStateOf(false) }
    var turnedAt by remember { mutableLongStateOf(0L) }
    var turnedFrom by remember { mutableStateOf<MediaEntry?>(null) }
    var index by rememberSaveable(slides) { mutableIntStateOf(0) }
    // A restored index can outlive a shorter slide list; every use goes through the clamped one.
    val current = index.coerceIn(0, slides.lastIndex)
    val entry = detail(slides[current])
    // Every slide's details at once (at most six small requests per session), so each slide opens on its wide picture.
    LaunchedEffect(slides) { slides.forEach(ensure) }
    if (many) LaunchedEffect(current, slides, motion, actionFocused) {
        // Fetch the next slide's picture now, so the fade does not reveal an empty frame. Disk only, kept small.
        val next = detail(slides[(current + 1) % slides.size])
        (if (next.hasBackdrop) next.backdrop else next.artwork)?.let { url ->
            SingletonImageLoader.get(context).enqueue(ImageRequest.Builder(context).data(SourceArtwork(next.sourceId, url))
                .memoryCachePolicy(CachePolicy.DISABLED).size(coil3.size.Size(960, 540)).build())
        }
        // 8 s (D063); 15 s while Ayrıntılar has focus, so the text being read does not change so soon.
        if (motion && !spoken) {
            delay(if (actionFocused) 15_000 else 8_000)
            turnedFrom = entry; turnedAt = System.currentTimeMillis(); index = (current + 1) % slides.size
        }
    }
    val fade = if (motion) tween<Float>(450) else snap()
    val visibility = rememberCardVisibility()
    val heroHeight = TvLayout.HeroHeight
    Box(Modifier.fillMaxWidth().heightIn(min = heroHeight)
        .then(visibility.container).then(visibility.focus).clipToBounds().background(Ink.Background).testTag("home-hero")) {
        Crossfade(entry, Modifier.matchParentSize(), animationSpec = fade, label = "hero-backdrop") { slide -> HeroPicture(slide, largeText) }
        // Dark on the text side, clear on the picture side, and into the page at the bottom where the shelves begin.
        Box(Modifier.matchParentSize().background(Brush.horizontalGradient(
            0f to Ink.Background, .26f to Ink.Background.copy(alpha = .9f), .5f to Ink.Background.copy(alpha = .5f),
            .72f to Ink.Background.copy(alpha = .12f), 1f to Color.Transparent)))
        Box(Modifier.matchParentSize().background(Brush.verticalGradient(
            0f to Ink.Background.copy(alpha = .35f), .18f to Color.Transparent, .58f to Color.Transparent,
            .86f to Ink.Background.copy(alpha = .8f), 1f to Ink.Background)))
        Column(Modifier.align(Alignment.BottomStart).fillMaxWidth().heightIn(min = heroHeight)
            .padding(start = TvLayout.Gutter, end = TvLayout.Gutter, top = 28.dp, bottom = 22.dp), verticalArrangement = Arrangement.Bottom) {
            Text(if (newest) "YENİ EKLENEN" else "KÜTÜPHANENDEN", Modifier.testTag("hero-label"), color = Ink.Mint, fontFamily = Roboto,
                fontWeight = FontWeight.Medium, fontSize = 13.sp, letterSpacing = 1.6.sp, maxLines = 1)
            Spacer(Modifier.height(10.dp))
            // Every slide's text is laid out, unseen, in the same slot: the hero keeps the tallest slide's height,
            // so the shelves below never move as it turns.
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomStart) {
                if (many) slides.forEach { HeroText(detail(it), largeText, Modifier.graphicsLayer { alpha = 0f }.clearAndSetSemantics { }) }
                // Text fades out, then in: two large titles cross-fading over each other read as a smear.
                AnimatedContent(entry, contentAlignment = Alignment.BottomStart, label = "hero-text", transitionSpec = {
                    (if (motion) fadeIn(tween(240, delayMillis = 200)) togetherWith fadeOut(tween(200))
                    else EnterTransition.None togetherWith ExitTransition.None) using null
                }) { slide -> HeroText(slide, largeText, tagged = true) }
            }
            Row(Modifier.padding(top = 18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                // Right and Left turn the slides; with one slide, Right stays here rather than jumping to the first shelf.
                Action("Ayrıntılar", actionModifier.onPreviewKeyEvent { event ->
                    if (!many || event.type != KeyEventType.KeyDown) false
                    else when (event.key) {
                        Key.DirectionRight -> { index = (current + 1) % slides.size; true }
                        Key.DirectionLeft -> if (current > 0) { index = current - 1; true } else false
                        else -> false
                    }
                }.focusProperties { right = FocusRequester.Cancel }.onFocusChanged { actionFocused = it.isFocused }
                    .semantics { if (many) stateDescription = "${entry.title}, ${current + 1} / ${slides.size}" }
                    // An OK within half a second of an automatic turn meant the slide that was on screen.
                    .testTag("hero-details"), primary = true, icon = "play") {
                    open(turnedFrom?.takeIf { System.currentTimeMillis() - turnedAt < 600 } ?: slides[current])
                }
                if (many) SlideDots(slides.size, current)
            }
        }
    }
}

/**
 * A slide's picture across the whole hero. A wide picture is shown as it is; a poster is enlarged and softened behind
 * (decoded tiny, so the scaling itself blurs it; Android 12+ smooths it further) and shown sharp on the right.
 */
@Composable private fun HeroPicture(slide: MediaEntry, largeText: Boolean) {
    val context = LocalContext.current
    Box(Modifier.fillMaxSize()) {
        if (slide.hasBackdrop) AsyncImage(SourceArtwork(slide.sourceId, slide.backdrop!!), null, Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop, alignment = BiasAlignment(0f, -.35f))
        else if (slide.artwork != null) {
            val soft = remember(slide.sourceId, slide.artwork) {
                ImageRequest.Builder(context).data(SourceArtwork(slide.sourceId, slide.artwork)).size(coil3.size.Size(48, 72)).build()
            }
            AsyncImage(soft, null, Modifier.fillMaxSize().graphicsLayer { alpha = .75f }
                .then(if (android.os.Build.VERSION.SDK_INT >= 31) Modifier.blur(24.dp) else Modifier), contentScale = ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(Ink.Background.copy(alpha = if (android.os.Build.VERSION.SDK_INT >= 31) .35f else .55f)))
            Box(Modifier.align(Alignment.CenterEnd).fillMaxHeight().padding(top = if (largeText) 64.dp else 44.dp, bottom = 34.dp, end = 56.dp)) {
                Artwork(slide.sourceId, slide.artwork, slide.title, Modifier.fillMaxHeight().aspectRatio(2f / 3)
                    .shadow(24.dp, RoundedCornerShape(TvLayout.CardRadius)).clip(RoundedCornerShape(TvLayout.CardRadius)).testTag("home-hero-poster"))
            }
        }
    }
}

/** A slide's title, its rating, year, kind, runtime and language, and its plot, as wide as the picture leaves room for. */
@Composable private fun HeroText(slide: MediaEntry, largeText: Boolean, modifier: Modifier = Modifier, tagged: Boolean = false) {
    val width = if (largeText) .64f else if (slide.hasBackdrop) .5f else .52f
    val shown = remember(slide.title, slide.year) { displayTitle(slide.title, slide.year) }
    fun Modifier.tag(tag: String) = if (tagged) testTag(tag) else this
    Column(modifier.fillMaxWidth(width), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(shown.name, Modifier.tag("hero-title"), color = Ink.Text, fontFamily = Roboto, fontWeight = FontWeight.Medium,
            fontSize = if (largeText) 32.sp else 50.sp, lineHeight = if (largeText) 38.sp else 56.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            slide.ratingText?.let { Text("★ $it", color = Ink.Mint, fontFamily = Roboto, fontWeight = FontWeight.Medium, fontSize = 15.sp, maxLines = 1) }
            Text(listOf(shown.year, if (slide.kind == MediaKind.SERIES) "Dizi" else "Film",
                if (slide.durationSeconds > 0) runtimeText(slide.durationSeconds) else "", shown.tag).filter { it.isNotBlank() }.joinToString("  ·  "),
                color = Ink.Text.copy(alpha = .8f), fontFamily = Roboto, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (slide.description.isNotBlank()) Text(slide.description, Modifier.tag("hero-description"), fontFamily = Roboto, fontSize = 15.sp,
            lineHeight = 22.sp, color = Ink.Text.copy(alpha = .78f), maxLines = if (largeText) 1 else 3, overflow = TextOverflow.Ellipsis)
    }
}

/** "1 sa 48 dk" or "52 dk". */
internal fun runtimeText(seconds: Long): String {
    val minutes = seconds / 60
    return if (minutes >= 60) "${minutes / 60} sa ${minutes % 60} dk" else "$minutes dk"
}

@Composable private fun SlideDots(count: Int, current: Int) {
    // The tag is set inside clearAndSetSemantics; a separate testTag on this row would be cleared with the rest.
    Row(Modifier.clearAndSetSemantics { testTag = "hero-dots" }, horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically) {
        repeat(count) { i ->
            Box(Modifier.size(width = if (i == current) 18.dp else 6.dp, height = 6.dp)
                .background(if (i == current) Ink.Mint else Ink.Muted.copy(alpha = .45f), RoundedCornerShape(50)))
        }
    }
}

@Composable private fun HomeCard(entry: MediaEntry, record: WatchRecord?, sourceName: String?, modifier: Modifier, section: String, headingContext: Dp, viewportHeight: Dp,
    reserveEpisode: Boolean = false, longClick: (() -> Unit)? = null, open: () -> Unit) {
    val live = entry.kind == MediaKind.LIVE
    val shown = remember(entry.title, entry.year) { displayTitle(entry.title, entry.year) }
    // The row's own 24 dp gutter: a focused card stops on the heading edge instead of 7 dp from the screen,
    // which left a strip of the previous card with cut text at the other edge.
    val visibility = rememberCardVisibility(horizontalPadding = TvLayout.Gutter, contextAbove = headingContext, viewportHeight = viewportHeight)
    if (record != null) {
        // A finished latest episode: the card offers the series' next episode instead of a position.
        val next = record.kind == MediaKind.EPISODE && record.progress.completed
        Box(Modifier.width(TvLayout.ContinueWidth).then(visibility.container)) {
            Card(onClick = open, onLongClick = longClick, modifier = modifier.then(visibility.focus).fillMaxWidth()
                .semantics {
                    contentDescription = "Devam et: ${entry.title}"
                    stateDescription = listOfNotNull(sourceName,
                        if (record.kind == MediaKind.EPISODE) "Sezon ${record.season}, bölüm ${record.episode}${if (next) " izlendi" else ""}" else null,
                        if (next) "Sıradaki bölüm" else remainingText(record.positionMs, record.durationMs)).joinToString(". ")
                    testTag = "$section:${entry.id}"
                },
                shape = CardDefaults.shape(shape = RoundedCornerShape(TvLayout.CardRadius)),
                colors = CardDefaults.colors(containerColor = Ink.Container, focusedContainerColor = Ink.High),
                scale = CardDefaults.scale(focusedScale = 1f),
                border = CardDefaults.border(focusedBorder = focusRing(TvLayout.CardRadius))) {
                Box(Modifier.fillMaxWidth()) {
                    Box(Modifier.matchParentSize()) {
                        Box(Modifier.width(76.dp).fillMaxHeight().background(Ink.High), contentAlignment = Alignment.Center) {
                            Artwork(entry.sourceId, entry.artwork ?: entry.backdrop, entry.title, Modifier.fillMaxSize(), showFallbackTitle = false)
                        }
                    }
                    Column(Modifier.fillMaxWidth().padding(start = 90.dp, end = 14.dp, top = 14.dp, bottom = 17.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        // Shared title and episode slots keep card heights and progress bars aligned across the row.
                        Text(shown.name, color = Ink.Text, fontFamily = Roboto, fontWeight = FontWeight.Medium,
                            fontSize = 14.sp, lineHeight = 19.sp, minLines = 2, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        sourceName?.let { Text(it, color = Ink.Muted, fontFamily = Roboto, fontSize = 12.sp, lineHeight = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                        if (record.kind == MediaKind.EPISODE) Text("S${record.season} · B${record.episode}${if (next) " izlendi" else ""}",
                            color = Ink.Muted, fontFamily = Roboto, fontSize = 12.sp, lineHeight = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        else if (reserveEpisode) Text(" ", Modifier.clearAndSetSemantics { },
                            color = Ink.Muted, fontFamily = Roboto, fontSize = 12.sp, lineHeight = 16.sp, maxLines = 1)
                        // What is left reads better than a position ("1 sa 17 dk kaldı", U18).
                        Text(if (next) "Sıradaki bölüm" else remainingText(record.positionMs, record.durationMs),
                            color = Ink.Mint, fontFamily = Roboto, fontSize = 12.sp, lineHeight = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    if (record.durationMs > 0 && !next) Box(Modifier.align(Alignment.BottomStart).fillMaxWidth().height(3.dp).background(Ink.High)) {
                        Box(Modifier.fillMaxHeight().fillMaxWidth((record.positionMs.toFloat() / record.durationMs).coerceIn(0f, 1f)).background(Ink.Mint))
                    }
                }
            }
        }
        return
    }
    Column(Modifier.width(if (live) TvLayout.LiveWidth else TvLayout.PosterWidth).then(visibility.container).testTag("tile:$section:${entry.id}"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Card(onClick = open, onLongClick = longClick, modifier = modifier.then(visibility.focus).fillMaxWidth().aspectRatio(if (live) 16f / 9 else 2f / 3)
            .semantics { contentDescription = "${if (section == "continue") "Devam et: " else ""}${entry.title}"; testTag = "$section:${entry.id}" },
            scale = CardDefaults.scale(focusedScale = 1f),
            shape = CardDefaults.shape(shape = RoundedCornerShape(TvLayout.CardRadius)),
            colors = CardDefaults.colors(containerColor = Ink.Container, focusedContainerColor = Ink.High),
            border = CardDefaults.border(focusedBorder = focusRing(TvLayout.CardRadius))) {
            if (live) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Artwork(entry.sourceId, entry.artwork, entry.title, Modifier.width(130.dp).height(40.dp), true, logoInset = 0.dp)
            } else Box(Modifier.fillMaxSize()) {
                Artwork(entry.sourceId, entry.artwork, entry.title, Modifier.fillMaxSize(), showFallbackTitle = false)
                entry.ratingText?.let { RatingBadge(it, Modifier.align(Alignment.TopStart).padding(7.dp)) }
            }
        }
        Text(shown.name, color = Ink.Text, fontFamily = Roboto, fontSize = 14.sp, minLines = if (live) 1 else 2, maxLines = 2, lineHeight = 19.sp, overflow = TextOverflow.Ellipsis)
        // As in the library: year, language or country and source on one line of constant height.
        if (!live) Text(listOf(shown.year, shown.tag, sourceName.orEmpty()).filter { it.isNotBlank() }.joinToString(" · "),
            Modifier.testTag("home-year:$section:${entry.id}"), color = Ink.Muted,
            fontFamily = Roboto, fontSize = 12.sp, lineHeight = 16.sp, minLines = 1, maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (live) sourceName?.let { Text(it, color = Ink.Muted, fontFamily = Roboto, fontSize = 12.sp, lineHeight = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
    }
}
