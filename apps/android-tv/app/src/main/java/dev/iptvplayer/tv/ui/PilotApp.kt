package dev.iptvplayer.tv.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.BringIntoViewSpec
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Density
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.*
import coil3.compose.AsyncImage
import dev.iptvplayer.tv.*
import dev.iptvplayer.tv.data.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type

@OptIn(ExperimentalFoundationApi::class)
@Composable fun PilotApp(model: TvViewModel, showOpening: Boolean = false) {
    val state by model.state.collectAsStateWithLifecycle()
    var settings by remember { mutableStateOf(false) }
    var opening by remember { mutableStateOf(showOpening) }
    var exitAsk by remember { mutableStateOf(false) }
    val savedScreens = rememberSaveableStateHolder()
    // Keep already-visible cards in place instead of moving every TV focus to a fixed pivot.
    val bringIntoView = remember { object : BringIntoViewSpec {} }
    // Back decided by the model's route when it arrives: enabled flags follow a route change only once the next
    // composition is applied (up to 400 ms for the first Home frame after an import), and a Back in that gap used to
    // reach the previous route's handler, whose model.back() ignores Home. Search, Guide and Player hand such a Back
    // here too. Back on Home asks before leaving the app instead of closing it at once.
    val routeBack = remember(model) { { if (model.state.value.route == Route.HOME) exitAsk = true else model.back() } }
    // One handler for the routes without their own. It stays enabled while the question opens, so a quick
    // second Back cannot reach the system before the dialog has window focus.
    BackHandler(state.route != Route.RECOVERY && state.route != Route.PLAYER && state.route != Route.SEARCH && state.route != Route.GUIDE &&
        !(state.route == Route.SETUP && state.source == null)) { routeBack() }
    LaunchedEffect(state.route) { if (state.route != Route.HOME) exitAsk = false }
    // A computer keyboard's Esc (the emulator, a USB keyboard) is the remote's Back. Compose would otherwise take it
    // as "leave the focused group". On release it goes back into this window as a Back press, so the platform routes
    // it as it routes the remote's: an open keyboard closes first, otherwise the BackHandlers act. A TV keyboard
    // reports itself in the window insets late or not at all, so the app does not guess. Only an Esc whose press
    // reached this handler counts: a keyboard that took the press has acted on it. Dialogs close on Esc themselves.
    val window = LocalActivity.current?.window
    val escapeAsBack = remember(window) {
        var pressed = false
        val handler = { event: androidx.compose.ui.input.key.KeyEvent ->
            val esc = event.nativeKeyEvent
            if (event.key != Key.Escape || !esc.hasNoModifiers()) false
            else {
                if (event.type == KeyEventType.KeyDown) pressed = true
                else if (event.type == KeyEventType.KeyUp) {
                    if (pressed && !esc.isCanceled && window != null)
                        for (action in intArrayOf(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.ACTION_UP))
                            window.injectInputEvent(android.view.KeyEvent(esc.downTime, esc.eventTime, action,
                                android.view.KeyEvent.KEYCODE_BACK, 0, 0, esc.deviceId, 0, 0, esc.source))
                    pressed = false
                }
                true
            }
        }
        handler
    }
    CompositionLocalProvider(LocalReducedMotion provides state.reducedMotion, LocalBringIntoViewSpec provides bringIntoView) {
        Box(Modifier.fillMaxSize().background(Ink.Background).onPreviewKeyEvent { event -> if (opening) opening = false; escapeAsBack(event) }) {
            when (state.route) {
                Route.RECOVERY -> SourceRecoveryScreen(state, model)
                Route.SOURCES -> SourcesScreen(state, model)
                Route.SETUP -> Setup(state, model)
                Route.LOADING -> LoadingScreen(state) { model.cancelImport() }
                Route.HOME -> savedScreens.SaveableStateProvider("home:${state.sourceFilterId}:${state.sources.map { it.id }}") {
                    key(state.sourceFilterId) { HomeScreen(state, model) { settings = true } }
                }
                Route.LIBRARY -> savedScreens.SaveableStateProvider("library:${state.sourceFilterId}:${state.kind}:${state.onlyFavorites}") {
                    key(state.kind, state.onlyFavorites, state.sourceFilterId) { Library(state, model) { settings = true } }
                }
                Route.SEARCH -> savedScreens.SaveableStateProvider("search") { SearchScreen(state, model, routeBack) { settings = true } }
                Route.GUIDE -> savedScreens.SaveableStateProvider("guide:${state.sourceFilterId}") {
                    key(state.sourceFilterId) { GuideScreen(state, model, routeBack) { settings = true } }
                }
                Route.DETAIL -> DetailScreen(state, model) { settings = true }
                Route.PLAYER -> state.playing?.let { PlayerScreen(it, model, routeBack) }
            }
            if (settings) Settings(state, model) { settings = false }
            if (exitAsk && state.route == Route.HOME) ExitDialog { exitAsk = false }
            if (opening) LaunchCover(state.reducedMotion) { opening = false }
        }
    }
}

@Composable private fun Setup(s: TvState, model: TvViewModel) {
    val first = remember { FocusRequester() }
    val largeText = LocalDensity.current.fontScale >= 1.5f
    val fieldScroll = rememberScrollState()
    var chipsFocused by remember { mutableStateOf(false) }
    // At 1.0 the next label fits inside the focused field's 27 dp bottom reserve; a fade would only ghost clean text.
    val fadeBottom = LocalDensity.current.fontScale > 1f
    val addressView = remember { BringIntoViewRequester() }
    val userView = remember { BringIntoViewRequester() }
    val passwordView = remember { BringIntoViewRequester() }
    val guideView = remember { BringIntoViewRequester() }
    val fieldHeights = remember { mutableStateMapOf<BringIntoViewRequester, Int>() }
    val density = LocalDensity.current
    fun fieldView(view: BringIntoViewRequester) = Modifier.bringIntoViewRequester(view).onSizeChanged { fieldHeights[view] = it.height }
    LaunchedEffect(s.error) {
        val error = s.error ?: return@LaunchedEffect
        val d = s.draft
        // Only form-validation messages point at a field; import, duplicate and storage errors keep the scroll.
        val config = SourceConfig(name = d.name, type = d.type, address = d.address,
            username = d.username, password = d.password, epgUrl = d.epgUrl)
        if (runCatching { config.validated() }.exceptionOrNull()?.message != error) return@LaunchedEffect
        val url = d.address.trim().toHttpUrlOrNull()
        val target = when {
            url == null || url.username.isNotEmpty() || url.password.isNotEmpty() -> addressView
            d.type == SourceType.XTREAM && d.username.isBlank() -> userView
            d.type == SourceType.XTREAM && d.password.isEmpty() -> passwordView
            else -> guideView
        }
        withFrameNanos { } // let the error note shrink the fields viewport first
        // The same 7/27 dp margins as a focused field, so the edge fades never cover the invalid box.
        with(density) { target.bringIntoView(Rect(0f, -7.dp.toPx(), 1f, (fieldHeights[target] ?: 0) + 27.dp.toPx())) }
    }
    Row(Modifier.fillMaxSize().padding(horizontal = TvLayout.Gutter, vertical = 28.dp),
        horizontalArrangement = Arrangement.spacedBy(32.dp)) {
        Column(Modifier.weight(.8f).align(Alignment.CenterVertically), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Text("IPTV PLAYER", color = Ink.Mint, fontSize = 12.sp, fontFamily = Roboto)
            Text("Televizyonun.\nSenin kütüphanen.", color = Ink.Text, fontFamily = Roboto,
                fontSize = if (largeText) 24.sp else 34.sp, lineHeight = if (largeText) 30.sp else 40.sp)
            if (!largeText) Note("Canlı yayınların, filmlerin ve dizilerin tek yerde.")
            Note("Kendi kaynağınla başla. Hesap bilgilerin bu cihazda şifreli saklanır.")
        }
        Column(Modifier.weight(1.4f).fillMaxHeight()
            .background(Ink.Container, RoundedCornerShape(24.dp)).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Kaynak ekle", color = Ink.Text, fontFamily = Roboto, fontSize = 22.sp, lineHeight = 28.sp)
            // Field remnants at the viewport edges fade out instead of resting cut through their glyphs;
            // the top fade never paints over the focused method chips.
            Column(Modifier.weight(1f).testTag("setup-fields")
                .scrollEdgeFade(Ink.Container, top = { fieldScroll.canScrollBackward && !chipsFocused },
                    bottom = { fadeBottom && fieldScroll.canScrollForward }, topFade = 9.dp, hold = .7f)
                .verticalScroll(fieldScroll).padding(vertical = 5.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Returning to the method chips reveals their focus ring, not just the chip bounds. The
                // row is pulled out by its 4 dp ring room so the pills share the panel's content edge.
                val methods = rememberCardVisibility()
                LazyRow(Modifier.layout { measurable, constraints ->
                        val room = 4.dp.roundToPx()
                        val placeable = measurable.measure(constraints.copy(maxWidth = constraints.maxWidth + 2 * room))
                        layout(placeable.width - 2 * room, placeable.height) { placeable.place(-room, 0) }
                    }.then(methods.container).then(methods.focus).onFocusChanged { chipsFocused = it.hasFocus },
                    horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(4.dp)) {
                    item { Action("Xtream Codes", Modifier.focusRequester(first).testTag("setup-xtream"), selected = s.draft.type == SourceType.XTREAM) {
                        model.draft(s.draft.copy(type = SourceType.XTREAM))
                    } }
                    item { Action("M3U bağlantısı", Modifier.testTag("setup-m3u"), selected = s.draft.type == SourceType.M3U) {
                        model.draft(s.draft.copy(type = SourceType.M3U))
                    } }
                }
                Note(if (s.draft.type == SourceType.XTREAM) "Sunucu adresini ve hesap bilgilerini gir." else "Sağlayıcının verdiği liste bağlantısını gir.")
                SetupEntry("Kaynak adı", s.draft.name, { model.draft(s.draft.copy(name = it)) })
                SetupEntry(if (s.draft.type == SourceType.M3U) "M3U bağlantısı" else "Sunucu adresi · http(s)://…", s.draft.address,
                    { model.draft(s.draft.copy(address = it)) }, secret = s.draft.type == SourceType.M3U,
                    modifier = fieldView(addressView))
                if (s.draft.type == SourceType.XTREAM) {
                    SetupEntry("Kullanıcı adı", s.draft.username, { model.draft(s.draft.copy(username = it)) },
                        modifier = fieldView(userView))
                    SetupEntry("Şifre", s.draft.password, { model.draft(s.draft.copy(password = it)) }, secret = true,
                        modifier = fieldView(passwordView))
                }
                SetupEntry("XMLTV rehber adresi · isteğe bağlı", s.draft.epgUrl, { model.draft(s.draft.copy(epgUrl = it)) }, secret = true,
                    modifier = fieldView(guideView))
            }
            s.error?.let { Note(it, error = true, modifier = Modifier.testTag("setup-error")) }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Action("Kütüphaneyi aç", Modifier.testTag("setup-connect"), primary = true) { model.connectDraft() }
                if (s.source != null) Action("Vazgeç") { model.back() }
            }
        }
    }
    LaunchedEffect(Unit) { first.requestFocus() }
}

@Composable private fun SetupEntry(label: String, value: String, onChange: (String) -> Unit,
    modifier: Modifier = Modifier, secret: Boolean = false) {
    val visibility = rememberCardVisibility()
    EntryField(label, value, onChange, modifier.then(visibility.container).then(visibility.focus),
        secret = secret, containerColor = Ink.Background)
}

/** Equal cells: GridCells.Fixed gives leftover pixels to the first columns, so 2:3 posters ended 1 dp apart. */
internal class EvenColumns(private val count: Int) : GridCells {
    override fun Density.calculateCrossAxisCellSizes(availableSize: Int, spacing: Int): List<Int> =
        List(count) { (availableSize - spacing * (count - 1)) / count }
}

/** [compact] is the search-result row used while the section search keyboard is open; the same Card keeps focus across modes. */
@Composable internal fun MediaCard(entry: MediaEntry, favorite: Boolean, sourceName: String?, modifier: Modifier = Modifier,
    guide: GuideSlice? = null, now: Long = 0, onMeasured: (Int) -> Unit = {}, compact: Boolean = false,
    compactMin: androidx.compose.ui.unit.Dp = 132.dp, click: () -> Unit) {
    val live = entry.kind == MediaKind.LIVE
    val visibility = rememberCardVisibility()
    Column(visibility.container.onSizeChanged { onMeasured(it.height) }.testTag("tile:library:${entry.id}")) {
        Card(onClick = click, modifier = modifier.then(visibility.focus).fillMaxWidth()
            .then(if (compact) Modifier.heightIn(min = compactMin) else if (live) Modifier.heightIn(min = 126.dp) else Modifier.aspectRatio(2f / 3))
            .semantics { contentDescription = entry.title },
            shape = CardDefaults.shape(shape = RoundedCornerShape(if (live && !compact) 14.dp else TvLayout.CardRadius)),
            colors = CardDefaults.colors(containerColor = Ink.Container, focusedContainerColor = Ink.High),
            scale = CardDefaults.scale(focusedScale = 1f),
            border = CardDefaults.border(focusedBorder = Border(BorderStroke(TvLayout.Focus, Ink.Text), inset = 3.dp))) {
            if (compact) Row(Modifier.fillMaxWidth().heightIn(min = compactMin).padding(10.dp), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                // Same row as the Ara results: small artwork, title, type line and source.
                Artwork(entry.sourceId, entry.artwork, entry.title, Modifier.width(if (live) 96.dp else 72.dp).height(if (live) 54.dp else 108.dp)
                    .clip(RoundedCornerShape(6.dp)), live, showFallbackTitle = false)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text((if (favorite) "★ " else "") + entry.title, color = Ink.Text, fontFamily = Roboto, fontWeight = FontWeight.Medium,
                        fontSize = 14.sp, lineHeight = 19.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(if (live) guide?.now(now)?.let { "Şimdi: ${it.title}" } ?: entry.category
                        else listOf(entry.kind.label, entry.year).filter { it.isNotBlank() }.joinToString(" · "),
                        color = Ink.Mint, fontFamily = Roboto, fontSize = 11.sp, lineHeight = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    sourceName?.let { Text(it, color = Ink.Muted, fontFamily = Roboto, fontSize = 10.sp, lineHeight = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                }
            } else if (live) Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Artwork(entry.sourceId, entry.artwork, entry.title, Modifier.width(130.dp).height(40.dp), true, logoInset = 0.dp)
                Text((if (favorite) "★ " else "") + entry.title, color = Ink.Text, fontFamily = Roboto, fontSize = 13.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(entry.category, color = Ink.Muted, fontFamily = Roboto, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            } else Box(Modifier.fillMaxSize()) {
                Artwork(entry.sourceId, entry.artwork, entry.title, Modifier.fillMaxSize(), showFallbackTitle = false)
                entry.ratingText?.let { RatingBadge(it, Modifier.align(Alignment.TopStart).padding(7.dp)) }
            }
        }
        if (compact) return@Column
        Spacer(Modifier.height(8.dp))
        if (!live) Text((if (favorite) "★ " else "") + entry.title, fontFamily = Roboto, fontSize = 12.sp,
            minLines = 2, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 17.sp)
        // Keep the caption baseline stable even when the provider has no year.
        if (!live) Text(entry.year, Modifier.testTag("library-year:${entry.id}"), color = Ink.Muted,
            fontFamily = Roboto, fontSize = 10.sp, lineHeight = 14.sp, minLines = 1, maxLines = 1,
            overflow = TextOverflow.Ellipsis)
        sourceName?.let { Text(it, color = Ink.Muted, fontFamily = Roboto, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
        if (live) {
            Text(guide?.now(now)?.let { "Şimdi: ${it.title}" } ?: "Program bilgisi yok", color = Ink.Muted, fontFamily = Roboto,
                fontSize = 10.sp, lineHeight = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(guide?.next(now)?.let { "Sonra: ${it.title}" }.orEmpty(), color = Ink.Muted, fontFamily = Roboto,
                fontSize = 10.sp, lineHeight = 14.sp, minLines = 1, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable internal fun Artwork(owner: String, url: String?, title: String, modifier: Modifier = Modifier, logo: Boolean = false,
    logoInset: androidx.compose.ui.unit.Dp = 16.dp, showFallbackTitle: Boolean = true) {
    var loaded by remember(owner, url) { mutableStateOf(false) }
    var failed by remember(owner, url) { mutableStateOf(url == null) }
    Box(modifier.background(if (logo) Color.Transparent else Ink.Container), contentAlignment = Alignment.Center) {
        if (!loaded) Column(Modifier.padding(if (logo) 0.dp else 12.dp).clearAndSetSemantics { },
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            LineIcon(if (logo) "tv" else "film", Ink.Muted.copy(alpha = .6f), Modifier.size(if (logo) 26.dp else 30.dp))
            if (!logo && failed && showFallbackTitle) Text(title, color = Ink.Muted, fontFamily = Roboto, fontSize = 13.sp, lineHeight = 18.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center, maxLines = 3, overflow = TextOverflow.Ellipsis)
        }
        if (url != null) AsyncImage(model = SourceArtwork(owner, url), contentDescription = null,
            modifier = Modifier.fillMaxSize().then(if (logo) Modifier.padding(logoInset) else Modifier),
            contentScale = if (logo) ContentScale.Fit else ContentScale.Crop,
            onSuccess = { loaded = true; failed = false }, onError = { loaded = false; failed = true })
    }
}

/** Asked on Home's Back. Focus starts on Vazgeç so a repeated Back/OK never closes the app by accident. */
@Composable private fun ExitDialog(dismiss: () -> Unit) {
    val activity = LocalActivity.current
    val stay = remember { FocusRequester() }
    Dialog(onDismissRequest = dismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(Modifier.width(480.dp).background(Ink.Container, RoundedCornerShape(18.dp)).padding(28.dp).testTag("exit-dialog"),
            verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Text("Uygulamadan çıkmak istiyor musunuz?", color = Ink.Text, fontFamily = Roboto, fontWeight = FontWeight.Medium,
                fontSize = 22.sp, lineHeight = 28.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Action("Vazgeç", Modifier.focusRequester(stay).testTag("exit-cancel")) { dismiss() }
                Action("Çık", Modifier.testTag("exit-confirm"), outlined = true) { activity?.finish() }
            }
        }
        // The platform gives the dialog window focus on its own schedule; ask for Vazgeç once it has it, or
        // after half a second regardless: the request then waits in the dialog until its window gets focus.
        val window = LocalWindowInfo.current
        LaunchedEffect(Unit) {
            withTimeoutOrNull(500) { snapshotFlow { window.isWindowFocused }.first { it } }
            withFrameNanos { }
            stay.requestFocus()
        }
    }
}

@Composable internal fun PilotDialog(title: String, dismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Dialog(onDismissRequest = dismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(Modifier.width(610.dp).heightIn(max = 470.dp).background(Ink.Container, RoundedCornerShape(18.dp))
            .padding(28.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Heading(title)
            content()
        }
    }
}

internal fun clockTime(ms: Long): String {
    val total = ms.coerceAtLeast(0) / 1000
    return if (total >= 3600) "%d:%02d:%02d".format(total / 3600, (total / 60) % 60, total % 60)
        else "%d:%02d".format(total / 60, total % 60)
}

