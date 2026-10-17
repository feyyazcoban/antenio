package dev.iptvplayer.tv.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import dev.iptvplayer.tv.*
import dev.iptvplayer.tv.data.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import java.time.ZoneId

/** Virtual categories above the provider's own; "Tümü" stays the model's key for "all of this kind". */
internal const val FavoritesCategory = "@favorites"
internal const val HistoryCategory = "@history"

private data class SectionCategory(val key: String, val label: String)

/** Focus memory kept out of Compose state: moving through cards must not recompose the screen. */
private class LibraryMemory(var lastId: String?, var lastIndex: Int) {
    val cards = HashMap<String, FocusRequester>()
    fun focus(id: String?): Boolean = try { id != null && cards[id]?.requestFocus() == true } catch (_: IllegalStateException) { false }
    /** The first card laid out in [grid], for when the remembered one is out of view. */
    fun focusVisible(grid: LazyGridState): Boolean =
        grid.layoutInfo.visibleItemsInfo.firstOrNull { it.offset.y >= 0 && it.key in cards }?.let { focus(it.key as String) } == true
}

/**
 * Canlı TV, Filmler, Diziler and Favoriler, TiviMate-like: a category column beside the side menu (virtual
 * categories first, then the provider's categories in the provider's order) and, beside it, an information
 * panel for the focused title above the list. Live channels are numbered rows with now/next; films and series
 * are a poster grid. Focus moves only redraw the information panel.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable internal fun Library(s: TvState, model: TvViewModel, settings: () -> Unit) {
    val live = s.kind == MediaKind.LIVE && !s.onlyFavorites
    val itemsOfKind = remember(s.catalog, s.kind, s.onlyFavorites) {
        catalogDisplayOrder(s.catalog.items.filter { (s.onlyFavorites || it.kind == s.kind) && !it.adult })
    }
    // Channel numbers follow "Tüm kanallar" and stay the same in every category.
    val numbers = remember(itemsOfKind, live) {
        if (!live) emptyMap() else HashMap<String, Int>(itemsOfKind.size).also { map -> itemsOfKind.forEachIndexed { i, e -> map[e.id] = i + 1 } }
    }
    val historyIds = remember(s.history, s.kind) {
        s.history.mapNotNull { r -> if (s.kind == MediaKind.SERIES) r.parentId?.takeIf { r.kind == MediaKind.EPISODE } ?: r.id.takeIf { r.kind == MediaKind.SERIES }
            else r.id.takeIf { r.kind == s.kind } }.distinct()
    }
    val categories = remember(itemsOfKind, s.onlyFavorites, s.favoriteIds, s.category, s.kind, s.catalog.categoryOrder) {
        if (s.onlyFavorites) {
            // Favoriler offers only its own categories; keep the active one if its last favourite was just removed.
            val pool = itemsOfKind.filter { it.id in s.favoriteIds }
            val active = listOfNotNull(s.category.takeIf { it != "Tümü" && pool.none { e -> e.category == it } })
            listOf(SectionCategory("Tümü", "Tüm favoriler")) + (providerCategories(pool, emptyList()) + active).distinct().map { SectionCategory(it, it) }
        } else {
            val all = when (s.kind) { MediaKind.LIVE -> "Tüm kanallar"; MediaKind.MOVIE -> "Tüm filmler"; else -> "Tüm diziler" }
            listOfNotNull(SectionCategory(FavoritesCategory, "Favoriler"),
                SectionCategory(HistoryCategory, "Geçmiş").takeIf { s.kind != MediaKind.LIVE },
                SectionCategory("Tümü", all)) +
                providerCategories(itemsOfKind, s.catalog.categoryOrder[s.kind].orEmpty()).map { SectionCategory(it, it) }
        }
    }
    val categoryLabel = displayCategory(categories.firstOrNull { it.key == s.category }?.label ?: s.category).first
    // The section search filters the selected category; its label says so, and an empty result offers the whole section.
    val searchScoped = s.category != "Tümü"
    val searchLabel = if (searchScoped) "Bu kategoride ara" else "Bu bölümde ara"
    val widenLabel = when {
        s.onlyFavorites -> "Tüm favorilerde ara"
        s.kind == MediaKind.LIVE -> "Tüm kanallarda ara"
        s.kind == MediaKind.MOVIE -> "Tüm filmlerde ara"
        else -> "Tüm dizilerde ara"
    }
    var widened by remember { mutableStateOf(false) }
    // Sources with a guide: without one, rows and the panel do not repeat "Program bilgisi yok" (U14).
    val guided = s.guideStatus.filterValues { it.configured }.keys
    val noGuide = live && itemsOfKind.none { it.sourceId in guided }
    // Held OK on a card or row: the quick actions (U8); focus goes back to that card afterwards.
    var quick by remember { mutableStateOf<MediaEntry?>(null) }
    var refocus by remember { mutableStateOf<String?>(null) }
    val window = LocalWindowInfo.current
    // Films and series can be ordered (Sırala); channels keep their numbers' order, Favoriler its own and
    // Geçmiş the order they were watched in.
    val sortable = !s.onlyFavorites && s.category != HistoryCategory && (s.kind == MediaKind.MOVIE || s.kind == MediaKind.SERIES)
    val sort = if (sortable) s.librarySorts[s.kind] ?: LibrarySort.DEFAULT else LibrarySort.DEFAULT
    // The model keeps the sorted section between visits; categories and search only filter it, which keeps its order.
    val ordered = remember(itemsOfKind, sort) { if (sort == LibrarySort.DEFAULT) itemsOfKind else model.libraryOrder(s.catalog, s.kind, sort) }
    val filtered = remember(ordered, s.category, s.query, s.libraryMatches, s.onlyFavorites, s.favoriteIds, historyIds) {
        val base = when (s.category) {
            "Tümü" -> ordered
            FavoritesCategory -> ordered.filter { it.id in s.favoriteIds }
            HistoryCategory -> { val byId = itemsOfKind.associateBy { it.id }; historyIds.mapNotNull(byId::get) }
            else -> ordered.filter { it.category == s.category }
        }
        base.filter { (s.query.isBlank() || it.id in s.libraryMatches) && (!s.onlyFavorites || it.id in s.favoriteIds) }
    }
    val initialIndex = remember { filtered.indexOfFirst { it.id == s.focusedId }.coerceAtLeast(0) }
    val memory = remember { LibraryMemory(filtered.getOrNull(initialIndex)?.id, initialIndex) }
    LaunchedEffect(refocus) { refocus?.let { id -> refocusAfterDialog(window) { memory.cards[id] }; refocus = null } }
    // The focused card's place for the heading's "3 / 120"; only that counter reads it.
    val position = remember { mutableIntStateOf(initialIndex + 1) }
    var gridActive by remember { mutableStateOf(false) }
    var restoreCategory by remember { mutableStateOf(false) }
    // Capture before removal makes Compose move focus to a fallback of its own.
    val recoverRemoved = remember(filtered) { gridActive && filtered.none { it.id == memory.lastId } }
    val recoveryIndex = remember(filtered) { memory.lastIndex.coerceAtMost((filtered.size - 1).coerceAtLeast(0)) }
    val navFocus = remember { FocusRequester() }
    val categoryFocus = remember { FocusRequester() }
    val selectedCategoryFocus = remember { FocusRequester() }
    val searchFocus = remember { FocusRequester() }
    val emptyFocus = remember { FocusRequester() }
    var searchOpen by rememberSaveable { mutableStateOf(s.query.isNotEmpty()) }
    val density = LocalDensity.current
    val largeText = density.fontScale >= 1.5f
    // As in TiviMate, the heading stays above the list, so its counter is in view while the posters scroll.
    // Large text lets it scroll with the cards: there a fixed heading would leave too little room for a tile.
    val fixedHeading = !largeText
    val headerCount = (if (fixedHeading) 1 else 2) + if (searchOpen) 1 else 0
    val inputIndex = if (fixedHeading) 0 else 1
    // While the TV keyboard is open for the section search it covers the lower half of the screen: the panel
    // and heading step aside, the field sits at the top and results use compact rows above the keyboard.
    // Some TV keyboards float without reporting insets, so typing in the focused field counts too, unless the
    // keyboard has reported itself: then closing it with Back brings the full layout back at once.
    var inputFocused by remember { mutableStateOf(false) }
    var edited by remember { mutableStateOf(false) }
    var imeSeen by remember { mutableStateOf(false) }
    // A composable getter: read it on every pass, never behind && (that shifts the remembered slots).
    val imeVisible = WindowInsets.isImeVisible
    LaunchedEffect(inputFocused, imeVisible) { if (inputFocused && imeVisible) imeSeen = true }
    val typing = searchOpen && inputFocused && (imeVisible || edited && !imeSeen)
    // Where focus is, for Back alone: plain holders, so moving between the levels does not recompose the screen.
    val menuFocused = remember { mutableStateOf(false) }
    val categoryFocused = remember { mutableStateOf(false) }
    LibraryBack(model, { menuFocused.value }, { categoryFocused.value },
        // Typing ends first (a floating keyboard may still count as typing), so the category column is laid out again.
        toCategory = { edited = false; gridActive = false; restoreCategory = true },
        toMenu = { try { navFocus.requestFocus() } catch (_: IllegalStateException) { model.back() } })
    val compactMin = with(density) { maxOf(132.dp, 20.dp + 19.sp.toDp() * 2 + 7.dp + 16.sp.toDp() +
        if (s.sources.size > 1) 7.dp + 16.sp.toDp() else 0.dp) }
    // A poster grid with focus takes the category column's place; Left from its first column brings the column back
    // on the selected category. Live rows keep the column. Large posters (D064): 4 columns beside the category column
    // and 5 without it, so a poster keeps nearly the same size (about 151 and 155 dp) when the column steps aside.
    val gridMode = gridActive && !live && !typing
    val columns = if (live) 1 else if (typing) (if (largeText) 1 else 2) else if (gridMode) (if (largeText) 4 else 5) else if (largeText) 3 else 4
    var viewportHeight by remember { mutableIntStateOf(0) }
    val headingVisibility = rememberCardVisibility()
    val grid = rememberLazyGridState(initialFirstVisibleItemIndex = if (initialIndex != 0) initialIndex + headerCount else 0)
    val reducedMotion = LocalReducedMotion.current
    LaunchedEffect(gridActive, headerCount, largeText, reducedMotion) {
        if (largeText && gridActive) snapshotFlow {
            val heading = grid.layoutInfo.visibleItemsInfo.firstOrNull { it.key == "heading" }
            Triple(grid.isScrollInProgress, heading?.offset?.y, heading?.size?.height)
        }.collect { (scrolling, top, height) ->
            if (!scrolling && top != null && height != null && top < 0 &&
                top + height > -grid.layoutInfo.beforeContentPadding) {
                // Settle between sections, never on half a title. A new focus cancels this move.
                try {
                    val inset = grid.layoutInfo.beforeContentPadding
                    if (reducedMotion) grid.scrollToItem(headerCount - 1, inset)
                    else grid.animateScrollToItem(headerCount - 1, inset)
                } catch (_: CancellationException) {
                    currentCoroutineContext().ensureActive()
                }
            }
        }
    }
    val now = guideClock()
    val zone = remember(s.guideZoneId) { runCatching { if (s.guideZoneId.isEmpty()) ZoneId.systemDefault() else ZoneId.of(s.guideZoneId) }.getOrDefault(ZoneId.systemDefault()) }
    val visible by remember { derivedStateOf { grid.layoutInfo.visibleItemsInfo.map { it.index } } }
    val liveEntries = remember(filtered, visible, headerCount) { visible.mapNotNull { filtered.getOrNull(it - headerCount) }.filter { it.kind == MediaKind.LIVE } }
    val programmes = guideRows(liveEntries, GuideWindow(now, now + 12 * 3_600_000L), s, model)
    fun focusContent(): Boolean = if (filtered.isEmpty()) try { emptyFocus.requestFocus() } catch (_: IllegalStateException) { false }
        else memory.focus(filtered.firstOrNull { it.id == memory.lastId }?.id ?: filtered.firstOrNull()?.id) || memory.focusVisible(grid)
    // The category, its size (or, with focus in the poster grid, the focused place) and the section's controls.
    @Composable fun LibraryHeading(container: Modifier, focus: Modifier) {
        Column(container.testTag("library-heading"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(focus.onFocusChanged { if (it.hasFocus) gridActive = false }.focusGroup(),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.weight(1f), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(categoryLabel, Modifier.weight(1f, fill = false), color = Ink.Text, fontFamily = Roboto, fontWeight = FontWeight.Medium,
                        fontSize = 18.sp, lineHeight = 24.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    when {
                        s.librarySearching -> Note("Aranıyor…")
                        gridMode -> PositionNote(position, filtered.size, Modifier.testTag("library-position"))
                        else -> Note("${filtered.size} " + if (live) "kanal" else "içerik")
                    }
                    if (noGuide && !gridMode) Note("· Rehber yok")
                }
                if (live) Action("Rehber", Modifier.testTag("open-guide"), compact = true, outlined = true, icon = "guide") { model.guide() }
                if (sortable) SortAction(sort, short = largeText) { model.librarySort(s.kind, it) }
                Action(if (s.query.isBlank()) searchLabel else "Arama: ${s.query}",
                    Modifier.widthIn(max = 200.dp), compact = true, outlined = true, icon = "search", selected = searchOpen || s.query.isNotBlank()) { searchOpen = !searchOpen }
            }
            if (s.catalog.warnings.isNotEmpty()) Note(s.catalog.warnings.joinToString(" "), error = true)
            s.catalogNotice?.takeIf { it.needsAttention }?.let { Note(it.message, error = true) }
        }
    }
    // An empty section keeps focus on its own tab, where the active marker is, as Home and Guide do.
    Navigation(s, model, modifier = Modifier.onFocusChanged { menuFocused.value = it.hasFocus; if (it.hasFocus) gridActive = false }, activeFocus = navFocus,
        contentEntry = { selectedCategoryFocus }, settings = settings) {
        Row(Modifier.fillMaxSize()) {
            if (!typing && !gridMode) CategoryColumn(categories, s.category, Modifier.focusRequester(categoryFocus)
                .onFocusChanged { categoryFocused.value = it.hasFocus; if (it.hasFocus) gridActive = false }
                .onPreviewKeyEvent { event ->
                    // Right returns to the card or row that had focus; with none, the list's first item.
                    event.type == KeyEventType.KeyDown && event.key == Key.DirectionRight && focusContent()
                }, selectedCategoryFocus) { key -> if (key != s.category) model.category(key) }
            Column(Modifier.weight(1f).fillMaxHeight()) {
                // No information panel above the list (D064 posters, D067 channels): the room goes to the list itself.
                // Lined up with the cards below; the grid's own top padding and spacer keep the old 28 dp gap.
                if (fixedHeading && !typing) LibraryHeading(Modifier.padding(start = if (gridMode) TvLayout.Gutter else 16.dp,
                    end = TvLayout.Gutter, top = 7.dp, bottom = 7.dp), Modifier)
                // At large text the header scrolls with the cards: a focused tile's labels must never be cropped.
                LazyVerticalGrid(columns = EvenColumns(columns), state = grid,
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(start = (if (gridMode) TvLayout.Gutter else 16.dp) - 7.dp, end = TvLayout.Gutter - 7.dp)
                        .onPreviewKeyEvent { event ->
                            // Left from a first-column poster: the category column comes back on the selected category.
                            if (gridMode && event.type == KeyEventType.KeyDown && event.key == Key.DirectionLeft && memory.lastIndex % columns == 0) {
                                gridActive = false; restoreCategory = true; true
                            } else false
                        }
                        .onSizeChanged { viewportHeight = it.height }.testTag("library-grid"),
                    contentPadding = PaddingValues(start = 7.dp, end = 7.dp, top = 7.dp, bottom = 27.dp),
                    verticalArrangement = Arrangement.spacedBy(if (live) 6.dp else 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(if (live) 0.dp else 16.dp, Alignment.CenterHorizontally)) {
                    if (!fixedHeading) item(key = "heading", span = { GridItemSpan(maxLineSpan) }) {
                        // As on Ara, the heading steps aside while typing; a short result list could not scroll it away.
                        if (!typing) LibraryHeading(headingVisibility.container, headingVisibility.focus)
                    }
                    if (searchOpen) item(key = "input", span = { GridItemSpan(maxLineSpan) }) {
                        Row(Modifier.onFocusChanged { if (it.hasFocus) gridActive = false }.focusGroup(),
                            verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Down goes to the results under the field, keyboard open or closed; Left reaches the categories.
                            EntryField(searchLabel + if (typing && s.query.isNotBlank()) " · ${if (s.librarySearching) "Aranıyor…" else "${filtered.size} sonuç"}" else "", s.query, { edited = true; model.query(it) },
                                Modifier.weight(1f).focusRequester(searchFocus).onFocusChanged { inputFocused = it.hasFocus; if (!it.hasFocus) { edited = false; imeSeen = false } },
                                onDone = { if (!focusContent()) categoryFocus.requestFocus() })
                            if (s.query.isNotBlank()) Action("Temizle", Modifier.padding(bottom = 4.dp), outlined = true) { model.query(""); searchFocus.requestFocus() }
                        }
                    }
                    // Kept for focus order; the category list itself lives in the column beside the grid.
                    item(key = "categories", span = { GridItemSpan(maxLineSpan) }) { Spacer(Modifier.height(0.dp)) }
                    if (filtered.isEmpty()) item(key = "empty", span = { GridItemSpan(maxLineSpan) }) {
                        val hasFilter = s.query.isNotBlank() || s.category != "Tümü"
                        // A search that found nothing in one category: the same search over the whole section is one OK away.
                        val widen = s.query.isNotBlank() && searchScoped && !s.librarySearching
                        val emptyVisibility = rememberCardVisibility()
                        Column(Modifier.fillMaxWidth().then(emptyVisibility.container).testTag("library-empty")
                            .background(Ink.Container, RoundedCornerShape(20.dp)).padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text(when {
                                s.librarySearching -> "Aranıyor…"
                                s.category == FavoritesCategory && s.query.isBlank() -> "Favorilerin burada görünecek"
                                s.category == HistoryCategory && s.query.isBlank() -> "İzlediklerin burada görünecek"
                                widen -> "Bu kategoride bulunamadı"
                                hasFilter -> "Bu filtrelerle içerik bulunamadı"
                                s.onlyFavorites -> "Favorilerin burada görünecek"
                                else -> "Bu bölümde içerik yok"
                            }, color = Ink.Text, fontFamily = Roboto, fontSize = 24.sp, lineHeight = 30.sp)
                            Note(when {
                                s.librarySearching -> "Kütüphanendeki başlıklar kontrol ediliyor."
                                s.category == FavoritesCategory && s.query.isBlank() || s.onlyFavorites && !hasFilter -> "Bir içeriği ayrıntı ekranından veya oynatıcıdan favorilerine ekle."
                                s.category == HistoryCategory && s.query.isBlank() -> "İzlemeye başladığın içerikler burada sıralanır."
                                widen -> "“${s.query}” $categoryLabel içinde yok. Bölümün tamamında arayabilirsin."
                                hasFilter -> "Aramayı ve kategori seçimini temizleyerek bu bölümdeki tüm içerikleri görebilirsin."
                                else -> "Seçili kaynakta bu türde gösterilebilen içerik bulunamadı. Kaynaklarını kontrol edebilirsin."
                            })
                            if (!s.librarySearching) Action(when {
                                widen -> widenLabel
                                hasFilter -> "Filtreleri temizle"
                                s.onlyFavorites -> "İçerikleri keşfet"
                                else -> "Kaynakları kontrol et"
                            }, Modifier.focusRequester(emptyFocus).then(emptyVisibility.focus).testTag("library-empty-action"), primary = true) {
                                when {
                                    widen -> { widened = true; model.category("Tümü") }
                                    hasFilter -> { model.query(""); model.category("Tümü"); selectedCategoryFocus.requestFocus() }
                                    s.onlyFavorites -> model.home()
                                    else -> model.manageSources()
                                }
                            }
                        }
                    }
                    itemsIndexed(filtered, key = { _, entry -> entry.id }) { index, entry ->
                        val own = remember(entry.id) { FocusRequester() }
                        DisposableEffect(entry.id, own) {
                            memory.cards[entry.id] = own
                            onDispose { if (memory.cards[entry.id] === own) memory.cards.remove(entry.id) }
                        }
                        val card = Modifier.focusRequester(own).onFocusChanged { if (it.isFocused) {
                            memory.lastId = entry.id; memory.lastIndex = index; gridActive = true
                            position.intValue = index + 1
                        } }
                        if (entry.kind == MediaKind.LIVE && !typing && live)
                            ChannelRow(entry, numbers[entry.id] ?: 0, entry.id in s.favoriteIds, card, programmes[entry.id], now, zone,
                                guideAvailable = entry.sourceId in guided, longClick = { quick = entry }) { model.open(entry) }
                        else MediaCard(entry, entry.id in s.favoriteIds, if (s.sources.size > 1) s.sourceName(entry.sourceId) else null,
                            card, programmes[entry.id], now, compact = typing, compactMin = compactMin,
                            guideAvailable = entry.sourceId in guided, longClick = { quick = entry }) { model.open(entry) }
                    }
                }
            }
        }
    }
    LaunchedEffect(restoreCategory) {
        if (restoreCategory) {
            withFrameNanos { }; withFrameNanos { }
            try { selectedCategoryFocus.requestFocus() } catch (_: IllegalStateException) {
                try { categoryFocus.requestFocus() } catch (_: IllegalStateException) { navFocus.requestFocus() }
            }
            restoreCategory = false
        }
    }
    quick?.let { entry ->
        QuickActions(entry, entry.id in s.favoriteIds, toggleFavorite = { model.toggleFavorite(entry) }, open = { model.open(entry) },
            dismiss = { quick = null; refocus = entry.id })
    }
    // The field is the list's first row: show the list from its top, so the whole field and its label are in view.
    LaunchedEffect(searchOpen) {
        if (searchOpen) {
            withFrameNanos { }; grid.scrollToItem(0); withFrameNanos { }
            try { searchFocus.requestFocus() } catch (_: IllegalStateException) { }
        }
    }
    // After "Tüm filmlerde ara": focus the first result, or the empty state's action when the section has none either.
    LaunchedEffect(widened, filtered, s.librarySearching) {
        if (widened && !s.librarySearching) { withFrameNanos { }; withFrameNanos { }; focusContent(); widened = false }
    }
    // The field rests at the top of the content while typing, so the first result row stays above the keyboard.
    LaunchedEffect(typing) { if (typing) { withFrameNanos { }; grid.scrollToItem(inputIndex) } }
    LaunchedEffect(Unit) {
        withFrameNanos { }; withFrameNanos { }
        // Entered from the side menu (no title to return to): the category column on the selected category, so the
        // category level is never skipped. Back from a title returns to that title's card instead.
        if (s.focusedId == null && filtered.isNotEmpty()) {
            // The list starts at its top, as the focus memory does: a saved scroll position would come back without
            // the card it belonged to, and Right and the information panel would point at a title out of view.
            if (grid.firstVisibleItemIndex != 0 || grid.firstVisibleItemScrollOffset != 0) grid.scrollToItem(0)
            if (!(try { selectedCategoryFocus.requestFocus() } catch (_: IllegalStateException) { false })) navFocus.requestFocus()
            return@LaunchedEffect
        }
        if (filtered.isNotEmpty() && grid.layoutInfo.visibleItemsInfo.none { it.key == memory.lastId }) {
            grid.scrollToItem(initialIndex + headerCount); withFrameNanos { }; withFrameNanos { }
        }
        if (filtered.isEmpty() || !memory.focus(memory.lastId ?: filtered.firstOrNull()?.id)) navFocus.requestFocus()
    }
    LaunchedEffect(filtered) {
        if (recoverRemoved && !s.librarySearching) {
            if (filtered.isEmpty()) navFocus.requestFocus()
            else {
                val next = filtered[recoveryIndex]
                memory.lastId = next.id; memory.lastIndex = recoveryIndex
                grid.scrollToItem(recoveryIndex + headerCount); withFrameNanos { }; withFrameNanos { }
                memory.focus(next.id)
            }
        } else if (!recoverRemoved) {
            // A refresh can move the focused card without moving focus: keep its place and counter true.
            val index = filtered.indexOfFirst { it.id == memory.lastId }
            if (index >= 0) { memory.lastIndex = index; position.intValue = index + 1 }
        }
    }
    // A new filter starts at the beginning; returning from playback creates this view at the saved card.
    var firstFilter by remember { mutableStateOf(true) }
    LaunchedEffect(s.kind, s.category, s.query, s.onlyFavorites, sort) {
        if (firstFilter) firstFilter = false else {
            memory.lastId = null; memory.lastIndex = 0; position.intValue = 1
            grid.scrollToItem(if (typing) inputIndex else 0)
        }
    }
}


/**
 * Back steps out one level at a time (SCREENS TV-020/030/040): the list, its heading or the section search to the
 * selected category, the category column to the section's tab in the side menu. Back on the menu is PilotApp's
 * route Back, to Ana Sayfa. A Back that arrives after the model left the library is the new route's.
 */
@Composable private fun LibraryBack(model: TvViewModel, menuFocused: () -> Boolean, categoryFocused: () -> Boolean,
    toCategory: () -> Unit, toMenu: () -> Unit) {
    BackHandler(enabled = !menuFocused()) {
        when {
            model.state.value.route != Route.LIBRARY -> model.back()
            categoryFocused() -> toMenu()
            else -> toCategory()
        }
    }
}

/**
 * The category column. Moving through it does not change the list; OK on a category shows it (D060).
 * Every remote entry lands on the selected category.
 */
@Composable private fun CategoryColumn(categories: List<SectionCategory>, current: String, modifier: Modifier,
    selectedFocus: FocusRequester, select: (String) -> Unit) {
    val selectedIndex = categories.indexOfFirst { it.key == current }.coerceAtLeast(0)
    val list = rememberLazyListState(initialFirstVisibleItemIndex = (selectedIndex - 3).coerceAtLeast(0))
    LazyColumn(modifier.width(196.dp).fillMaxHeight()
        .focusProperties { onEnter = { if (requestedFocusDirection != FocusDirection.Enter) try { selectedFocus.requestFocus() } catch (_: IllegalStateException) { } } }
        .focusGroup().testTag("library-categories"),
        state = list, contentPadding = PaddingValues(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        itemsIndexed(categories, key = { _, c -> c.key }) { _, category ->
            val isSelected = category.key == current
            var focused by remember { mutableStateOf(false) }
            // Moving through the column does not change the list; OK on a category shows it.
            Surface(onClick = { select(category.key) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 40.dp)
                    .then(if (isSelected) Modifier.focusRequester(selectedFocus) else Modifier)
                    .onFocusChanged { focused = it.isFocused }
                    .semantics { selected = isSelected; contentDescription = category.label }.testTag("category:${category.key}"),
                shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(10.dp)),
                colors = ClickableSurfaceDefaults.colors(containerColor = if (isSelected) Ink.Selected else Color.Transparent,
                    contentColor = Ink.Text, focusedContainerColor = Ink.Mint, focusedContentColor = Ink.OnMint),
                border = ClickableSurfaceDefaults.border(focusedBorder = focusRing(10.dp)),
                scale = ClickableSurfaceDefaults.scale(focusedScale = 1f)) {
                // The row takes the surface's 40 dp itself: the surface does not stretch its content, and a row of
                // its own text height sat at the top, its label about 2 dp above the centre.
                Row(Modifier.fillMaxWidth().heightIn(min = 40.dp).padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    // "TR/FILM ~ YERLI 2023" reads "YERLI 2023" with its country after it (D065).
                    val (label, tag) = remember(category.label) { displayCategory(category.label) }
                    Text(label, Modifier.weight(1f), color = LocalContentColor.current, fontFamily = Roboto,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal, fontSize = 14.sp, lineHeight = 19.sp,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (tag.isNotEmpty()) Text(tag, Modifier.padding(start = 8.dp), color = if (focused) LocalContentColor.current else Ink.Muted,
                        fontFamily = Roboto, fontSize = 12.sp, lineHeight = 16.sp, maxLines = 1)
                    if (isSelected && !focused) Box(Modifier.size(6.dp).background(Ink.Mint, RoundedCornerShape(50)))
                }
            }
        }
    }
}

/**
 * "Sırala: …" in the heading; the choices open in a dialog and focus returns to the button. With large text it
 * says only "Sırala" so the category name keeps its room; its selected look still marks a non-default order.
 */
@Composable private fun SortAction(current: LibrarySort, short: Boolean, choose: (LibrarySort) -> Unit) {
    var open by remember { mutableStateOf(false) }
    val opener = remember { FocusRequester() }
    fun dismiss() { open = false; try { opener.requestFocus() } catch (_: IllegalStateException) { } }
    Action(if (short) "Sırala" else "Sırala: ${current.label}", Modifier.widthIn(max = 220.dp).focusRequester(opener).testTag("library-sort"),
        compact = true, outlined = true, selected = current != LibrarySort.DEFAULT) { open = true }
    if (open) {
        val initial = remember { FocusRequester() }
        SourceDialog("Sırala", initial, ::dismiss, width = 420.dp) {
            LibrarySort.entries.forEach { option ->
                Action(option.label, Modifier.fillMaxWidth().then(if (option == current) Modifier.focusRequester(initial) else Modifier)
                    .testTag("sort:${option.name}"), selected = option == current, check = true) { choose(option); dismiss() }
            }
        }
    }
}

/** A numbered channel row: logo, name, the programme on now with its progress, and the next one. */
@Composable private fun ChannelRow(entry: MediaEntry, number: Int, favorite: Boolean, modifier: Modifier,
    guide: GuideSlice?, now: Long, zone: ZoneId, guideAvailable: Boolean = true, longClick: (() -> Unit)? = null, click: () -> Unit) {
    val shown = remember(entry.title) { displayTitle(entry.title) }
    val visibility = rememberCardVisibility()
    val current = guide?.now(now)
    val next = guide?.next(now)
    Box(Modifier.fillMaxWidth().then(visibility.container).testTag("tile:library:${entry.id}")) {
        Surface(onClick = click, onLongClick = longClick, modifier = modifier.then(visibility.focus).fillMaxWidth().heightIn(min = 48.dp)
            .semantics { contentDescription = entry.title },
            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(10.dp)),
            colors = ClickableSurfaceDefaults.colors(containerColor = Ink.Container, contentColor = Ink.Text,
                focusedContainerColor = Ink.High, focusedContentColor = Ink.Text),
            border = ClickableSurfaceDefaults.border(focusedBorder = focusRing(10.dp)),
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1f)) {
            Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).padding(horizontal = 12.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(if (number > 0) "$number" else "", Modifier.width(36.dp), color = Ink.Muted, fontFamily = Roboto,
                    fontSize = 14.sp, maxLines = 1)
                Artwork(entry.sourceId, entry.artwork, entry.title, Modifier.width(56.dp).height(32.dp), true, logoInset = 0.dp)
                Text((if (favorite) "★ " else "") + shown.name, Modifier.weight(.42f), color = Ink.Text, fontFamily = Roboto,
                    fontWeight = FontWeight.Medium, fontSize = 15.sp, lineHeight = 20.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Column(Modifier.weight(.58f), verticalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterVertically)) {
                    // Without a guide the column names the channel's category instead of "Program bilgisi yok" on every row.
                    Text(current?.title ?: if (guideAvailable) "Program bilgisi yok" else displayCategory(entry.category).first,
                        color = if (current != null) Ink.Text else Ink.Muted, fontFamily = Roboto,
                        fontSize = 14.sp, lineHeight = 19.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (current != null) Progress(current, now)
                    if (next != null) Text("Sonra · ${guideTime(next.start, zone)} ${next.title}", color = Ink.Muted, fontFamily = Roboto,
                        fontSize = 12.sp, lineHeight = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable private fun Progress(programme: EpgProgramme, now: Long, modifier: Modifier = Modifier) {
    val end = programme.end ?: return
    val fraction = ((now - programme.start).toFloat() / (end - programme.start).coerceAtLeast(1)).coerceIn(0f, 1f)
    Box(modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(50)).background(Ink.Divider)) {
        Box(Modifier.fillMaxWidth(fraction).fillMaxHeight().background(Ink.Mint))
    }
}

