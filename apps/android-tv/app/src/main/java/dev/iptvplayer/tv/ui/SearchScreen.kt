package dev.iptvplayer.tv.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldDecorator
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.*
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import dev.iptvplayer.tv.*
import dev.iptvplayer.tv.data.*

private data class SearchRow(val key: String, val title: String, val entries: List<MediaEntry>)

/**
 * TV search: a wide field at the top and results in horizontal rows beneath it, one row per kind.
 * Rows follow relevance: the row holding the best match comes first and starts with it, so while the TV
 * keyboard covers the lower half of the screen the best row sits right under the field.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable internal fun SearchScreen(s: TvState, model: TvViewModel, back: () -> Unit, settings: () -> Unit) {
    val input = remember { FocusRequester() }
    val resultFocus = remember { FocusRequester() }
    val nav = remember { FocusRequester() }
    val list = rememberLazyListState()
    val rowStates = remember { mutableStateMapOf<String, LazyListState>() }
    val density = LocalDensity.current
    val largeText = density.fontScale >= 1.5f
    // One floor for every card: two title lines, the meta line and, with several sources, the source line.
    // Convert each sp separately: font scaling is non-linear on recent Android versions.
    val cardMin = with(density) { maxOf(132.dp, 20.dp + 19.sp.toDp() * 2 + 7.dp + 15.sp.toDp() +
        if (s.sources.size > 1) 7.dp + 14.sp.toDp() else 0.dp) }
    // At the screen edge only the next card's artwork peeks out, never half a title.
    val cardWidth = if (largeText) 380.dp else 260.dp
    var viewportHeight by remember { mutableStateOf(0.dp) }
    var resultsActive by remember { mutableStateOf(s.searchFocusedId != null) }
    var navActive by remember { mutableStateOf(false) }
    // Where Right from the side menu returns: the last focused result, or the field and its controls.
    var returnToResults by remember { mutableStateOf(s.searchFocusedId != null) }
    // A new query, kind or source starts each row at its first card and forgets the old focus position;
    // returning from detail keeps the saved rows.
    val rowSession = "${s.searchQuery}|${s.searchKind}|${s.searchSourceId}"
    var lastIndex by remember(rowSession) { mutableIntStateOf(s.searchResults.indexOfFirst { it.id == s.searchFocusedId }.coerceAtLeast(0)) }
    val target = s.searchResults.firstOrNull { it.id == s.searchFocusedId }
        ?: s.searchResults.getOrNull(lastIndex.coerceAtMost((s.searchResults.size - 1).coerceAtLeast(0)))
    val recoverRemoved = remember(s.searchResults) { resultsActive && s.searchResults.none { it.id == s.searchFocusedId } }
    val recoveryIndex = remember(s.searchResults) { lastIndex.coerceAtMost((s.searchResults.size - 1).coerceAtLeast(0)) }
    var focusAfterSearch by remember { mutableStateOf(false) }
    fun finishInput() { focusAfterSearch = true }
    val controls = Modifier.onFocusChanged { if (it.hasFocus) { resultsActive = false; returnToResults = false } }.focusGroup()
    // While the TV keyboard is open for this field, the kind chips and source filter step aside so the
    // first row of results sits right under the field, above the keyboard.
    // Some TV keyboards float without reporting insets, so typing in the focused field counts too, unless the
    // keyboard has reported itself: then closing it with Back brings the full layout back at once.
    var inputFocused by remember { mutableStateOf(false) }
    var edited by remember { mutableStateOf(false) }
    var imeSeen by remember { mutableStateOf(false) }
    // A composable getter: read it on every pass, never behind && (that shifts the remembered slots).
    val imeVisible = WindowInsets.isImeVisible
    LaunchedEffect(inputFocused, imeVisible) { if (inputFocused && imeVisible) imeSeen = true }
    val typing = inputFocused && (imeVisible || edited && !imeSeen)
    val rows = remember(s.searchResults) {
        // groupBy keeps first appearance, so the kind of the best match leads and each row keeps relevance order.
        s.searchResults.groupBy { it.kind }.map { (kind, entries) -> SearchRow(kind.name, kind.label, entries) }
    }
    suspend fun reveal(entry: MediaEntry) {
        val rowIndex = rows.indexOfFirst { row -> row.entries.any { it.id == entry.id } }
        if (rowIndex < 0) return
        if (list.layoutInfo.visibleItemsInfo.none { it.index == rowIndex }) {
            list.scrollToItem(rowIndex); withFrameNanos { }; withFrameNanos { }
        }
        val row = rows[rowIndex]
        rowStates[row.key]?.let { state ->
            val index = row.entries.indexOfFirst { it.id == entry.id }
            if (state.layoutInfo.visibleItemsInfo.none { it.index == index }) { state.scrollToItem(index); withFrameNanos { }; withFrameNanos { } }
        }
    }
    BackHandler {
        // A Back that arrives after the model left Search, before this screen is gone, is the new route's.
        if (model.state.value.route != Route.SEARCH) return@BackHandler back()
        when {
            navActive -> model.home()
            resultsActive || s.searching || s.searchResults.isEmpty() -> {
                focusAfterSearch = false
                nav.requestFocus()
            }
            else -> focusAfterSearch = true
        }
    }
    Navigation(s, model, Modifier.onFocusChanged {
        navActive = it.hasFocus
        if (it.hasFocus) resultsActive = false
    }, searchFocus = nav, contentEntry = { if (returnToResults && target != null) resultFocus else input }, settings = settings) {
        Column(Modifier.fillMaxSize()) {
            Row(controls.padding(horizontal = TvLayout.Gutter), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                SearchField(s.searchQuery, { edited = true; model.searchQuery(it) },
                    Modifier.weight(1f).focusRequester(input).onFocusChanged { inputFocused = it.hasFocus; if (!it.hasFocus) { edited = false; imeSeen = false } },
                    onDone = ::finishInput)
                if (s.searchQuery.isNotEmpty()) Action("Temizle", outlined = true) { model.searchQuery(""); input.requestFocus() }
            }
            if (!typing) LazyRow(controls.padding(top = 12.dp).fillMaxWidth().testTag("search-kinds"), horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = TvLayout.Gutter, vertical = 5.dp)) {
                item { Action("Tümü", selected = s.searchKind == null, compact = true, outlined = true) { model.searchKind(null) } }
                listOf(MediaKind.LIVE, MediaKind.MOVIE, MediaKind.SERIES).forEach { kind ->
                    item { Action(kind.label, selected = s.searchKind == kind, compact = true, outlined = true) { model.searchKind(kind) } }
                }
            }
            if (s.searchResults.isEmpty()) {
                // While typing the message sits under the field, above the TV keyboard.
                Column(Modifier.weight(1f).fillMaxWidth().padding(horizontal = TvLayout.Gutter, vertical = if (typing) 32.dp else 0.dp),
                    verticalArrangement = if (typing) Arrangement.Top else Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    LineIcon("search", Ink.Muted, Modifier.size(32.dp))
                    Spacer(Modifier.height(16.dp))
                    Text(when { s.searching -> "Aranıyor…"; s.searchQuery.isBlank() -> "Bir başlıkla başla"; else -> "Eşleşen içerik bulunamadı" },
                        color = Ink.Text, fontFamily = Roboto, fontSize = 20.sp)
                    Spacer(Modifier.height(8.dp))
                    Note(when {
                        s.searching -> "Kütüphanendeki başlıklar kontrol ediliyor."
                        s.searchQuery.isBlank() -> "Kumandayla yazabilir, ardından sonuçlara geçebilirsin."
                        s.searchKind != null -> "Daha kısa bir ad dene veya içerik türünden Tümü’nü seç."
                        else -> "Daha kısa bir ad dene veya seçili kaynağı kontrol et."
                    })
                }
            } else LazyColumn(Modifier.weight(1f).fillMaxWidth().padding(top = 14.dp).testTag("search-results")
                .onSizeChanged { viewportHeight = with(density) { it.height.toDp() } }
                // The partly shown next row fades out instead of being sliced through its title.
                .scrollEdgeFade(Ink.Background, top = { false }, bottom = { list.canScrollForward }, bottomFade = 18.dp),
                state = list, verticalArrangement = Arrangement.spacedBy(18.dp), contentPadding = PaddingValues(bottom = 28.dp)) {
                items(rows, key = { "${it.key}|$rowSession" }) { row ->
                    var headingHeight by remember { mutableStateOf(0.dp) }
                    val start = row.entries.indexOfFirst { it.id == target?.id }.coerceAtLeast(0)
                    val rowState = rememberLazyListState(initialFirstVisibleItemIndex = start)
                    DisposableEffect(row.key, rowState) {
                        rowStates[row.key] = rowState
                        onDispose { if (rowStates[row.key] === rowState) rowStates.remove(row.key) }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("${row.title} · ${row.entries.size}", Modifier.padding(horizontal = TvLayout.Gutter).testTag("search-row:${row.key}")
                            .onSizeChanged { headingHeight = with(density) { it.height.toDp() } },
                            color = Ink.Text, fontFamily = Roboto, fontWeight = FontWeight.Medium, fontSize = 17.sp, lineHeight = 22.sp)
                        LazyRow(state = rowState, horizontalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(horizontal = TvLayout.Gutter, vertical = 4.dp)) {
                            items(row.entries, key = { it.id }) { entry ->
                                SearchCard(entry, s.sourceName(entry.sourceId).takeIf { s.sources.size > 1 }, cardMin, cardWidth,
                                    headingHeight + 10.dp, viewportHeight, Modifier
                                    .then(if (target?.id == entry.id) Modifier.focusRequester(resultFocus) else Modifier)
                                    .onFocusChanged { if (it.isFocused) {
                                        resultsActive = true; returnToResults = true; lastIndex = s.searchResults.indexOf(entry); model.searchFocus(entry.id)
                                    } }) { model.searchFocus(entry.id); model.open(entry) }
                            }
                        }
                    }
                }
            }
        }
    }
    LaunchedEffect(Unit) {
        withFrameNanos { }; withFrameNanos { }
        if (s.searchFocusedId != null && target != null) {
            reveal(target)
            resultFocus.requestFocus()
        } else input.requestFocus()
    }
    LaunchedEffect(s.searchQuery, s.searchKind, s.searchSourceId) {
        // Returning from detail retains the saved result and scroll. Editing starts at the top.
        if (s.searchFocusedId == null) list.scrollToItem(0)
    }
    LaunchedEffect(s.searching, s.searchResults, focusAfterSearch) {
        if (!s.searching && focusAfterSearch) {
            if (target != null) {
                withFrameNanos { }
                reveal(target)
                resultFocus.requestFocus()
            }
            focusAfterSearch = false
        } else if (!s.searching && recoverRemoved) {
            if (target == null) input.requestFocus()
            else {
                model.searchFocus(s.searchResults[recoveryIndex].id)
                withFrameNanos { }
                reveal(s.searchResults[recoveryIndex])
                resultFocus.requestFocus()
            }
        }
    }
}

/** Wide pill field in the shared field colours, with the search icon and hint inside it. */
@Composable private fun SearchField(value: String, onChange: (String) -> Unit, modifier: Modifier, onDone: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val keyboard = rememberFieldKeyboard()
    val input = rememberFieldInput(value, onChange)
    val state = input.state
    val shape = RoundedCornerShape(50)
    // A field removed while it has focus (leaving Ara) counts as left.
    DisposableEffect(keyboard) { onDispose { if (focused) keyboard.focusChanged(false, true) } }
    BasicTextField(state,
        // Up/down leave this single-line field; OK opens the keyboard, focus alone does not. The TV IME consumes its own remote events.
        modifier.onPreviewKeyEvent { event -> fieldKey(event, focusManager, keyboard) }
            .onFocusChanged { keyboard.focusChanged(it.isFocused, focused); focused = it.isFocused }.testTag("search-field"),
        textStyle = TextStyle(color = Ink.Text, fontSize = 16.sp, fontFamily = Roboto),
        keyboardOptions = fieldKeyboardOptions(KeyboardType.Text, ImeAction.Search),
        inputTransformation = input.reporter,
        onKeyboardAction = KeyboardActionHandler { keyboard.close(); onDone() },
        lineLimits = TextFieldLineLimits.SingleLine,
        cursorBrush = SolidColor(Ink.Mint),
        decorator = TextFieldDecorator { field ->
            Row(Modifier.background(Ink.Container, shape)
                .border(if (focused) 2.dp else 1.dp, if (focused) Ink.Text else Ink.Outline.copy(alpha = .7f), shape)
                .padding(horizontal = 18.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
                LineIcon("search", if (focused) Ink.Text else Ink.Muted, Modifier.size(20.dp))
                Spacer(Modifier.width(14.dp))
                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (state.text.isEmpty()) Text("Kanal, film veya dizi ara", color = Ink.Muted, fontFamily = Roboto, fontSize = 16.sp,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                    field()
                }
            }
        })
}

@Composable private fun SearchCard(entry: MediaEntry, sourceName: String?, minHeight: Dp, width: Dp, contextAbove: Dp,
    viewportHeight: Dp, modifier: Modifier, open: () -> Unit) {
    val visibility = rememberCardVisibility(horizontalPadding = TvLayout.Gutter, contextAbove = contextAbove, viewportHeight = viewportHeight)
    Box(Modifier.width(width).then(visibility.container).testTag("tile:search:${entry.id}")) {
        Card(onClick = open, modifier = modifier.fillMaxWidth().heightIn(min = minHeight).then(visibility.focus)
            .semantics { contentDescription = entry.title; testTag = "search:${entry.id}" },
            shape = CardDefaults.shape(shape = RoundedCornerShape(TvLayout.CardRadius)),
            colors = CardDefaults.colors(containerColor = Ink.Container, focusedContainerColor = Ink.High),
            scale = CardDefaults.scale(focusedScale = 1f),
            border = CardDefaults.border(focusedBorder = focusRing(TvLayout.CardRadius))) {
            // The floor sits on the Row (outside its padding) so CenterVertically centres the content in the card.
            Row(Modifier.fillMaxWidth().heightIn(min = minHeight).padding(10.dp), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Artwork(entry.sourceId, entry.artwork, entry.title, Modifier.width(if (entry.kind == MediaKind.LIVE) 96.dp else 72.dp).height(if (entry.kind == MediaKind.LIVE) 54.dp else 108.dp).clip(RoundedCornerShape(6.dp)), entry.kind == MediaKind.LIVE, showFallbackTitle = false)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text(entry.title, color = Ink.Text, fontFamily = Roboto, fontWeight = FontWeight.Medium,
                        fontSize = 14.sp, lineHeight = 19.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(when (entry.kind) { MediaKind.LIVE -> "Canlı TV"; MediaKind.MOVIE -> "Film"; else -> "Dizi" } +
                        if (entry.year.isNotBlank()) " · ${entry.year}" else "", color = Ink.Mint, fontFamily = Roboto, fontSize = 11.sp, lineHeight = 15.sp)
                    sourceName?.let { Text(it, color = Ink.Muted, fontFamily = Roboto, fontSize = 10.sp, lineHeight = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                }
            }
        }
    }
}
