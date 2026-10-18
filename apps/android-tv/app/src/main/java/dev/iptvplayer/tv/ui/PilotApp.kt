package dev.iptvplayer.tv.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.annotation.StringRes
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
import androidx.compose.ui.platform.WindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
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
import dev.iptvplayer.tv.R
import dev.iptvplayer.tv.data.*
import dev.iptvplayer.tv.ui.settings.Settings
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type

/** [opening] shows the launch splash; the host clears it through [endOpening] or on any remote key (MainActivity). */
@OptIn(ExperimentalFoundationApi::class)
@Composable fun PilotApp(model: TvViewModel, opening: Boolean = false, endOpening: () -> Unit = {}) {
    val state by model.state.collectAsStateWithLifecycle()
    var settings by remember { mutableStateOf(false) }
    var exitAsk by remember { mutableStateOf(false) }
    val overlays = remember { AppOverlays() }
    val savedScreens = rememberSaveableStateHolder()
    // Keep already-visible cards in place instead of moving every TV focus to a fixed pivot.
    val bringIntoView = remember { object : BringIntoViewSpec {} }
    // Back decided by the model's route when it arrives: enabled flags follow a route change only once the next
    // composition is applied (up to 400 ms for the first Home frame after an import), and a Back in that gap used to
    // reach the previous route's handler, whose model.back() ignores Home. Search, Guide and Player hand such a Back
    // here too. Back on Home asks before leaving the app instead of closing it at once.
    val routeBack = remember(model) { { if (model.state.value.route == Route.HOME) exitAsk = true else model.back() } }
    // One handler for the routes without their own. It stays enabled while the question opens, so a quick
    // second Back cannot reach the system before the dialog has window focus. The lock screen has its own.
    val screen = state.screen
    BackHandler(screen != Route.RECOVERY && screen != Route.PLAYER && screen != Route.SEARCH && screen != Route.GUIDE &&
        screen != Route.LOCKED && !(screen == Route.SETUP && state.source == null)) { routeBack() }
    LaunchedEffect(screen) { if (screen != Route.HOME) exitAsk = false }
    // The app left the screen with a PIN verified in Ayarlar, or the TV starts over: Settings closes (pin.md §3.9).
    LaunchedEffect(state.parental.lockEpoch) { settings = false }
    // Source management opened from Ayarlar, at once or once its PIN was right: the panel closes behind it (pin.md §3.8).
    LaunchedEffect(state.route) {
        if (settings && (state.route == Route.SOURCES || state.route == Route.SETUP || state.route == Route.LOADING)) {
            settings = false; model.closeSettings()
        }
    }
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
    CompositionLocalProvider(LocalReducedMotion provides state.reducedMotion, LocalBringIntoViewSpec provides bringIntoView,
        LocalAppOverlays provides overlays) {
        Box(Modifier.fillMaxSize().background(Ink.Background).onPreviewKeyEvent { event -> escapeAsBack(event) }) {
            when (screen) {
                // "Açılışta PIN iste", or a protection record that cannot be read (pin.md §3.9): the route waits under it.
                Route.LOCKED -> AppLockScreen(state, model)
                Route.RECOVERY -> SourceRecoveryScreen(state, model)
                Route.SOURCES -> SourcesScreen(state, model)
                Route.SETUP -> Setup(state, model)
                // Opening the app on saved sources shows the NEXTV cover, never the operation card (D069). Imports, saves,
                // removals and a Yeniden dene or reset from the recovery screen keep the card, so no brand flash cuts in.
                Route.LOADING -> if (state.sourceTask == SourceTask.RESTORING && state.restoringAtLaunch) RestoringCover()
                    else LoadingScreen(state) { model.cancelImport() }
                Route.HOME -> savedScreens.SaveableStateProvider("home:${state.sourceFilterId}:${state.sources.map { it.id }}") {
                    key(state.sourceFilterId) { HomeScreen(state, model) { settings = true } }
                }
                Route.LIBRARY -> savedScreens.SaveableStateProvider("library:${state.sourceFilterId}:${state.kind}:${state.onlyFavorites}") {
                    key(state.kind, state.onlyFavorites, state.sourceFilterId) { Library(state, model, routeBack) { settings = true } }
                }
                Route.SEARCH -> savedScreens.SaveableStateProvider("search") { SearchScreen(state, model, routeBack) { settings = true } }
                Route.GUIDE -> savedScreens.SaveableStateProvider("guide:${state.sourceFilterId}") {
                    key(state.sourceFilterId) { GuideScreen(state, model, routeBack) { settings = true } }
                }
                Route.DETAIL -> DetailScreen(state, model) { settings = true }
                Route.PLAYER -> state.playing?.let { PlayerScreen(it, model, routeBack) }
            }
            // Nothing opens over the lock screen; its own pad is part of it.
            if (screen != Route.LOCKED) {
                if (settings) Settings(state, model) { settings = false; model.closeSettings() }
                AppOverlayHost(overlays, state, model)
                state.accessNotice?.let { notice -> AccessNote(notice.resolve()) { model.accessNoticeShown() } }
            }
            if (exitAsk && screen == Route.HOME) ExitDialog { exitAsk = false }
            if (opening) LaunchCover(state.reducedMotion,
                loading = state.route == Route.LOADING && state.sourceTask == SourceTask.RESTORING, finish = endOpening)
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
    // Hidden fields (the M3U link, the password, the guide address) can be shown while typing them with the remote (U13).
    var reveal by rememberSaveable { mutableStateOf(false) }
    val density = LocalDensity.current
    // An untouched name field shows, and saves, the default name in the app's language.
    val defaultName = stringResource(R.string.source_default_name)
    fun fieldView(view: BringIntoViewRequester) = Modifier.bringIntoViewRequester(view).onSizeChanged { fieldHeights[view] = it.height }
    LaunchedEffect(s.error) {
        val error = s.error ?: return@LaunchedEffect
        val d = s.draft
        // Only form-validation messages point at a field; import, duplicate and storage errors keep the scroll.
        val config = SourceConfig(name = d.name ?: defaultName, type = d.type, address = d.address,
            username = d.username, password = d.password, epgUrl = d.epgUrl)
        if ((runCatching { config.validated() }.exceptionOrNull() as? SourceFailure)?.text != error) return@LaunchedEffect
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
            NextvLogo(height = 28.dp)
            Text(stringResource(R.string.setup_title), color = Ink.Text, fontFamily = Roboto,
                fontSize = if (largeText) 24.sp else 34.sp, lineHeight = if (largeText) 30.sp else 40.sp)
            if (!largeText) Note(stringResource(R.string.setup_tagline))
            // How "PIN'i unuttum → Her şeyi sıfırla" ended, until a source is added. This column does not scroll, so at a
            // large font the notice takes the privacy note's place and its last lines (what to do next) stay in view.
            val startOver = s.catalogNotice?.takeIf { it.operation == CatalogOperation.START_OVER }
            if (!largeText || startOver == null) Note(stringResource(R.string.setup_privacy))
            startOver?.let { Note(it.message.resolve(), error = it.needsAttention, modifier = Modifier.testTag("setup-notice")) }
        }
        Column(Modifier.weight(1.4f).fillMaxHeight()
            .background(Ink.Container, RoundedCornerShape(24.dp)).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(stringResource(R.string.setup_heading), color = Ink.Text, fontFamily = Roboto, fontSize = 22.sp, lineHeight = 28.sp)
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
                    item { Action(stringResource(R.string.source_type_xtream), Modifier.focusRequester(first).testTag("setup-xtream"), selected = s.draft.type == SourceType.XTREAM) {
                        model.draft(s.draft.copy(type = SourceType.XTREAM))
                    } }
                    item { Action(stringResource(R.string.setup_m3u), Modifier.testTag("setup-m3u"), selected = s.draft.type == SourceType.M3U) {
                        model.draft(s.draft.copy(type = SourceType.M3U))
                    } }
                }
                Note(stringResource(if (s.draft.type == SourceType.XTREAM) R.string.setup_hint_xtream else R.string.setup_hint_m3u))
                SetupEntry(stringResource(R.string.setup_name), s.draft.name ?: defaultName, { model.draft(s.draft.copy(name = it)) })
                SetupEntry(stringResource(if (s.draft.type == SourceType.M3U) R.string.setup_m3u else R.string.setup_server), s.draft.address,
                    { model.draft(s.draft.copy(address = it)) }, secret = s.draft.type == SourceType.M3U,
                    modifier = fieldView(addressView), revealed = reveal)
                if (s.draft.type == SourceType.XTREAM) {
                    SetupEntry(stringResource(R.string.setup_username), s.draft.username, { model.draft(s.draft.copy(username = it)) },
                        modifier = fieldView(userView))
                    SetupEntry(stringResource(R.string.setup_password), s.draft.password, { model.draft(s.draft.copy(password = it)) }, secret = true,
                        modifier = fieldView(passwordView), revealed = reveal)
                }
                SetupEntry(stringResource(R.string.setup_guide), s.draft.epgUrl, { model.draft(s.draft.copy(epgUrl = it)) }, secret = true,
                    modifier = fieldView(guideView), revealed = reveal)
            }
            s.error?.let { Note(it.resolve(), error = true, modifier = Modifier.testTag("setup-error")) }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Action(stringResource(R.string.setup_connect), Modifier.testTag("setup-connect"), primary = true) { model.connectDraft(defaultName) }
                if (s.source != null) Action(stringResource(R.string.action_cancel)) { model.back() }
                Action(stringResource(if (reveal) R.string.setup_hide else R.string.setup_show), Modifier.testTag("setup-reveal"), outlined = true,
                    selected = reveal) { reveal = !reveal }
            }
        }
    }
    LaunchedEffect(Unit) { first.requestFocus() }
}

@Composable private fun SetupEntry(label: String, value: String, onChange: (String) -> Unit,
    modifier: Modifier = Modifier, secret: Boolean = false, revealed: Boolean = false) {
    val visibility = rememberCardVisibility()
    EntryField(label, value, onChange, modifier.then(visibility.container).then(visibility.focus),
        secret = secret, containerColor = Ink.Background, revealed = revealed)
}

/** Equal cells: GridCells.Fixed gives leftover pixels to the first columns, so 2:3 posters ended 1 dp apart. */
internal class EvenColumns(private val count: Int) : GridCells {
    override fun Density.calculateCrossAxisCellSizes(availableSize: Int, spacing: Int): List<Int> =
        List(count) { (availableSize - spacing * (count - 1)) / count }
}

/**
 * [compact] is the search-result row used while the section search keyboard is open; the same Card keeps focus across
 * modes. Names are shown through [displayTitle] (D065). [guideAvailable] false: the source has no guide, so the card
 * does not repeat "Program bilgisi yok". [longClick] opens the quick actions (a held OK). [locked]: opening it asks for
 * the PIN (pin.md §4); the card shows the lock, TalkBack reads "Kilitli", and a channel keeps its programmes to itself.
 */
@Composable internal fun MediaCard(entry: MediaEntry, favorite: Boolean, sourceName: String?, modifier: Modifier = Modifier,
    guide: GuideSlice? = null, now: Long = 0, onMeasured: (Int) -> Unit = {}, compact: Boolean = false,
    compactMin: androidx.compose.ui.unit.Dp = 132.dp, guideAvailable: Boolean = true, locked: Boolean = false,
    longClick: (() -> Unit)? = null, click: () -> Unit) {
    val live = entry.kind == MediaKind.LIVE
    val radius = if (live && !compact) 14.dp else TvLayout.CardRadius
    val visibility = rememberCardVisibility()
    val shown = remember(entry.title, entry.year) { displayTitle(entry.title, entry.year) }
    val name = (if (favorite) "★ " else "") + shown.name
    val lockedState = stringResource(R.string.lock_state)
    val lockedChannel = stringResource(R.string.lock_channel)
    Column(visibility.container.onSizeChanged { onMeasured(it.height) }.testTag("tile:library:${entry.id}")) {
        Card(onClick = click, onLongClick = longClick, modifier = modifier.then(visibility.focus).fillMaxWidth()
            .then(if (compact) Modifier.heightIn(min = compactMin) else if (live) Modifier.heightIn(min = 126.dp) else Modifier.aspectRatio(2f / 3))
            .semantics { contentDescription = entry.title; if (locked) stateDescription = lockedState },
            shape = CardDefaults.shape(shape = RoundedCornerShape(radius)),
            colors = CardDefaults.colors(containerColor = Ink.Container, focusedContainerColor = Ink.High),
            scale = CardDefaults.scale(focusedScale = 1f),
            border = CardDefaults.border(focusedBorder = focusRing(radius))) {
            if (compact) Row(Modifier.fillMaxWidth().heightIn(min = compactMin).padding(10.dp), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                // Same row as the Ara results: small artwork, title, type line and source.
                Artwork(entry.sourceId, entry.artwork, entry.title, Modifier.width(if (live) 96.dp else 72.dp).height(if (live) 54.dp else 108.dp)
                    .clip(RoundedCornerShape(6.dp)), live, showFallbackTitle = false)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text(name, color = Ink.Text, fontFamily = Roboto, fontWeight = FontWeight.Medium,
                        fontSize = 14.sp, lineHeight = 19.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (locked) LockMark(color = Ink.Mint)
                        Text(if (live && locked) lockedChannel
                            else if (live) guide?.now(now)?.let { stringResource(R.string.programme_now, it.title) } ?: shownCategory(entry.category).first
                            else listOf(stringResource(entry.kind.label), shown.year, shown.tag).filter { it.isNotBlank() }.joinToString(" · "),
                            color = Ink.Mint, fontFamily = Roboto, fontSize = 12.sp, lineHeight = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    sourceName?.let { Text(it, color = Ink.Muted, fontFamily = Roboto, fontSize = 12.sp, lineHeight = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                }
            } else if (live) Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Artwork(entry.sourceId, entry.artwork, entry.title, Modifier.width(130.dp).height(40.dp), true, logoInset = 0.dp)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(name, Modifier.weight(1f, fill = false), color = Ink.Text, fontFamily = Roboto, fontSize = 15.sp,
                        lineHeight = 20.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (locked) LockMark()
                }
                Text(shownCategory(entry.category).first, color = Ink.Muted, fontFamily = Roboto, fontSize = 12.sp, lineHeight = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            } else Box(Modifier.fillMaxSize()) {
                Artwork(entry.sourceId, entry.artwork, entry.title, Modifier.fillMaxSize(), showFallbackTitle = false)
                entry.ratingText?.let { RatingBadge(it, Modifier.align(Alignment.TopStart).padding(7.dp)) }
                if (locked) LockBadge(Modifier.align(Alignment.TopEnd).padding(7.dp))
            }
        }
        if (compact) return@Column
        // Captions sized for the larger library posters (D064): title 15 sp, the lines under it 12 sp.
        Spacer(Modifier.height(8.dp))
        if (!live) Text(name, fontFamily = Roboto, fontSize = 15.sp,
            minLines = 2, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 20.sp)
        // Year, the language or country of the title and, with several sources, the source share one line, which keeps
        // its height without any of them.
        if (!live) Text(listOf(shown.year, shown.tag, sourceName.orEmpty()).filter { it.isNotBlank() }.joinToString(" · "),
            Modifier.testTag("library-year:${entry.id}"), color = Ink.Muted,
            fontFamily = Roboto, fontSize = 12.sp, lineHeight = 16.sp, minLines = 1, maxLines = 1,
            overflow = TextOverflow.Ellipsis)
        if (live) sourceName?.let { Text(it, color = Ink.Muted, fontFamily = Roboto, fontSize = 12.sp, lineHeight = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
        if (live) {
            // A locked channel's programmes are not shown until a PIN opens it (pin.md §4); the lines keep their height.
            Text(if (locked) lockedChannel else guide?.now(now)?.let { stringResource(R.string.programme_now, it.title) }
                ?: if (guideAvailable) stringResource(R.string.programme_none) else "", color = Ink.Muted, fontFamily = Roboto,
                fontSize = 12.sp, lineHeight = 16.sp, minLines = 1, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(guide?.next(now)?.takeIf { !locked }?.let { stringResource(R.string.programme_next, it.title) }.orEmpty(), color = Ink.Muted, fontFamily = Roboto,
                fontSize = 12.sp, lineHeight = 16.sp, minLines = 1, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

/**
 * A window above every screen and the Settings panel (gap plan T3), one at a time: the quick actions and the PIN pad
 * today; a reminder and the Plus page later. Screens open one through [rememberOverlayOpener]; [PilotApp] shows it.
 */
internal sealed interface AppOverlay {
    /** Held OK on [entry]'s card (U8). */
    data class Quick(val entry: MediaEntry) : AppOverlay
    /** The PIN pad for the model's open prompt ([ParentalUi.prompt]), as long as it stays open (rememberPinOpener). */
    data object Pin : AppOverlay
}

/**
 * The [AppOverlay] on screen. A new one takes the place of the one open. Once it closes, focus goes back to its
 * opener's requester in the opener's window (refocusAfterDialog). An opener that leaves the screen first takes its
 * overlay and that focus return along, as it did when each screen composed its own dialog.
 */
@Stable internal class AppOverlays {
    class Opened(val overlay: AppOverlay, val owner: Any, val window: WindowInfo, val returnFocus: () -> FocusRequester?)

    var shown by mutableStateOf<Opened?>(null)
        private set
    /** The overlay that closed, until its opener's window has focus again and the requester has taken it. */
    var returning by mutableStateOf<Opened?>(null)
        private set

    fun show(opened: Opened) { returning = null; shown = opened }
    /** An action, Vazgeç or Back closed the overlay on screen. */
    fun close() { returning = shown ?: return; shown = null }
    fun returned(opened: Opened) { if (returning === opened) returning = null }
    fun leave(owner: Any) {
        if (shown?.owner === owner) shown = null
        if (returning?.owner === owner) returning = null
    }
}

/** The app's [AppOverlays]; a screen composed on its own (a layout test) gets one that nothing shows. */
internal val LocalAppOverlays = staticCompositionLocalOf { AppOverlays() }

/**
 * Opens [AppOverlay]s for the calling screen, which owns them: one closes when the screen leaves. Focus goes back to
 * what `returnFocus` names when the overlay closes, looked up then, as the card may have been composed again.
 */
@Composable internal fun rememberOverlayOpener(): (overlay: AppOverlay, returnFocus: () -> FocusRequester?) -> Unit {
    val overlays = LocalAppOverlays.current
    val window = LocalWindowInfo.current
    val owner = remember { Any() }
    DisposableEffect(overlays, owner) { onDispose { overlays.leave(owner) } }
    return remember(overlays, window) { { overlay, returnFocus -> overlays.show(AppOverlays.Opened(overlay, owner, window, returnFocus)) } }
}

/** The overlay on screen, composed after the screens and Settings so its window opens above them, and focus back. */
@Composable private fun AppOverlayHost(overlays: AppOverlays, s: TvState, model: TvViewModel) {
    overlays.shown?.let { opened -> key(opened) {
        when (val overlay = opened.overlay) {
            // A locked title's PIN pad takes the menu's place for the same card, focus going back to it, as does the pad
            // for Kilidi kaldır. Kilitle and Kilidi kaldır are offered with a PIN set, for the title's own lock.
            is AppOverlay.Quick -> {
                fun pin() = overlays.show(AppOverlays.Opened(AppOverlay.Pin, opened.owner, opened.window, opened.returnFocus))
                val target = remember(overlay.entry) { model.favoriteTarget(overlay.entry) }
                QuickActions(overlay.entry, quickActions(overlay.entry, overlay.entry.id in s.favoriteIds,
                    toggleFavorite = { model.toggleFavorite(overlay.entry) }, open = { if (model.open(overlay.entry)) pin() },
                    close = overlays::close, locked = target?.takeIf { s.parental.status == ParentalStatus.ON }?.let { it.id in s.locks.contents },
                    toggleLock = { lock -> if (model.setTitleLock(overlay.entry, lock)) pin() }), overlays::close)
            }
            // The prompt ended in the model (a PIN taken, Vazgeç, Back): the window closes and focus goes back.
            AppOverlay.Pin -> s.parental.prompt.let { prompt ->
                if (prompt != null) PinDialog(prompt, model) else LaunchedEffect(Unit) { overlays.close() }
            }
        }
    } }
    // A PIN pad whose opener left the screen asks nothing more, nor does a prompt that no opener showed: a prompt never
    // waits unseen in the model.
    val pinShown = overlays.shown?.overlay == AppOverlay.Pin
    LaunchedEffect(pinShown, s.parental.prompt?.id) { if (!pinShown && model.state.value.parental.prompt != null) model.closePin() }
    val returning = overlays.returning
    LaunchedEffect(returning) { returning?.let { refocusAfterDialog(it.window, it.returnFocus); overlays.returned(it) } }
}

/**
 * A row of the quick actions: [key] names it for tests ("quick-$key"); [run] does what it says and closes the menu,
 * before or after as the action needs. Later steps add rows (Yeniden adlandır, Taşı, Gizle …).
 */
internal data class QuickAction(val key: String, @StringRes val label: Int, val icon: String? = null,
    val iconFilled: Boolean = false, val run: () -> Unit)

/**
 * Held OK on a card (U8): add or remove the favourite, or open the title, without going through its detail or player.
 * [locked], with a PIN set, offers the title's own lock (pin.md §3.5): Kilitle when it has none, else Kilidi kaldır;
 * [toggleLock] gets what to do, and its PIN pad takes the menu's place.
 */
internal fun quickActions(entry: MediaEntry, favorite: Boolean, toggleFavorite: () -> Unit, open: () -> Unit, close: () -> Unit,
    locked: Boolean? = null, toggleLock: (Boolean) -> Unit = {}) = listOfNotNull(
    QuickAction("favorite", if (favorite) R.string.favorite_remove else R.string.favorite_add, "star", iconFilled = favorite) { toggleFavorite(); close() },
    QuickAction("open", if (entry.kind == MediaKind.LIVE) R.string.action_watch else R.string.action_details,
        if (entry.kind == MediaKind.LIVE) "play" else null) { close(); open() },
    locked?.let { own -> QuickAction("lock", if (own) R.string.lock_remove else R.string.lock_add, "lock") { close(); toggleLock(!own) } })

/**
 * The quick actions for [entry], then Vazgeç. A list longer than the window (large text) scrolls by whole rows (D053),
 * and the focused row keeps half a gap around it in view, the room of its focus ring.
 */
@Composable internal fun QuickActions(entry: MediaEntry, actions: List<QuickAction>, dismiss: () -> Unit) {
    val first = remember { FocusRequester() }
    val gap = 16.dp
    val pitch = with(LocalDensity.current) { (actionHeight() + gap).roundToPx() }
    SourceDialog(displayTitle(entry.title, entry.year).name, first, dismiss, width = 440.dp) {
        // Half a gap above and below the rows, pulled out over the dialog's spacing: at rest each row sits where the
        // dialog's own column put it, and a scrolled window holds whole rows.
        Column(Modifier.weight(1f, fill = false).testTag("quick-actions").layout { measurable, constraints ->
                val room = (gap / 2).roundToPx()
                val max = if (!constraints.hasBoundedHeight) constraints.maxHeight
                    else ((constraints.maxHeight + 2 * room) / pitch).coerceAtLeast(1) * pitch
                val placeable = measurable.measure(constraints.copy(minHeight = 0, maxHeight = max))
                layout(placeable.width, (placeable.height - 2 * room).coerceIn(constraints.minHeight, constraints.maxHeight)) {
                    placeable.place(0, -room)
                }
            }.verticalScroll(rememberScrollState()).padding(vertical = gap / 2), verticalArrangement = Arrangement.spacedBy(gap)) {
            actions.forEachIndexed { index, action ->
                QuickRow(stringResource(action.label), Modifier.then(if (index == 0) Modifier.focusRequester(first) else Modifier)
                    .testTag("quick-${action.key}"), gap / 2, action.icon, action.iconFilled, onClick = action.run)
            }
            QuickRow(stringResource(R.string.action_cancel), Modifier.testTag("quick-cancel"), gap / 2, outlined = true, onClick = dismiss)
        }
    }
}

/** A full-width row of the quick actions that keeps [edge] around it in view while it has focus. */
@Composable private fun QuickRow(label: String, modifier: Modifier, edge: Dp, icon: String? = null, iconFilled: Boolean = false,
    outlined: Boolean = false, onClick: () -> Unit) {
    val relocation = remember { BringIntoViewRequester() }
    var focused by remember { mutableStateOf(false) }
    var height by remember { mutableIntStateOf(0) }
    val room = with(LocalDensity.current) { edge.toPx() }
    LaunchedEffect(focused, height) {
        if (focused) { withFrameNanos { }; relocation.bringIntoView(Rect(0f, -room, 1f, height + room)) }
    }
    Action(label, modifier.fillMaxWidth().bringIntoViewRequester(relocation).onSizeChanged { height = it.height }
        .onFocusChanged { focused = it.isFocused }, icon = icon, iconFilled = iconFilled, outlined = outlined, onClick = onClick)
}

/**
 * What the access gate has to say (TvState.accessNotice) in the top corner for three seconds, where the player's notes
 * sit; TalkBack reads it once. It takes no focus.
 */
@Composable private fun BoxScope.AccessNote(text: String, shown: () -> Unit) {
    LaunchedEffect(text) { delay(3_000); shown() }
    Text(text, Modifier.align(Alignment.TopEnd).padding(top = 22.dp, end = TvLayout.Gutter).widthIn(max = 420.dp)
        .background(Ink.Background.copy(alpha = .9f), RoundedCornerShape(12.dp)).padding(horizontal = 16.dp, vertical = 10.dp)
        .semantics { liveRegion = LiveRegionMode.Polite }.testTag("access-note"),
        color = Ink.Text, fontFamily = Roboto, fontSize = 15.sp, lineHeight = 21.sp)
}

/** Focus for [requester] once this window has focus again (after a dialog), or false if it is gone. */
internal suspend fun refocusAfterDialog(window: androidx.compose.ui.platform.WindowInfo, requester: () -> FocusRequester?) {
    snapshotFlow { window.isWindowFocused }.first { it }
    withFrameNanos { }
    try { requester()?.requestFocus() } catch (_: IllegalStateException) { }
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
            Text(stringResource(R.string.exit_question), color = Ink.Text, fontFamily = Roboto, fontWeight = FontWeight.Medium,
                fontSize = 22.sp, lineHeight = 28.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Action(stringResource(R.string.action_cancel), Modifier.focusRequester(stay).testTag("exit-cancel")) { dismiss() }
                Action(stringResource(R.string.exit_confirm), Modifier.testTag("exit-confirm"), outlined = true) { activity?.finish() }
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

/**
 * What is left of a title, for Devam et: "35 dk kaldı", "1 sa 17 dk kaldı"; the position when the length is unknown.
 * Not read-only for the same reason as [guideStatusText]: the early return emits one group fewer than the rest.
 */
@Composable
internal fun remainingText(positionMs: Long, durationMs: Long): String {
    if (durationMs <= 0) return clockTime(positionMs)
    val minutes = ((durationMs - positionMs).coerceAtLeast(0) + 59_999) / 60_000
    return when {
        minutes < 1 -> stringResource(R.string.remaining_soon)
        minutes < 60 -> stringResource(R.string.remaining_minutes, minutes.toString())
        minutes % 60 == 0L -> stringResource(R.string.remaining_hours, (minutes / 60).toString())
        else -> stringResource(R.string.remaining_hours_minutes, (minutes / 60).toString(), (minutes % 60).toString())
    }
}

