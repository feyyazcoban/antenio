package dev.iptvplayer.tv.ui.settings

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.tv.material3.Text
import dev.iptvplayer.tv.R
import dev.iptvplayer.tv.TvState
import dev.iptvplayer.tv.TvViewModel
import dev.iptvplayer.tv.ui.Action
import dev.iptvplayer.tv.ui.Ink
import dev.iptvplayer.tv.ui.Roboto
import dev.iptvplayer.tv.ui.resolve
import dev.iptvplayer.tv.ui.scrollEdgeFade
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Decides whether a page with a [PageGuard] opens: [open] runs now, after a question, or not at all. The parental PIN
 * and NEXTV Plus answer here once they exist; until then every page opens.
 */
internal fun interface PageGate {
    fun request(guard: PageGuard, open: () -> Unit)

    companion object {
        val Open = PageGate { _, open -> open() }
    }
}

/**
 * The Settings panel at the right edge: the main page, and the pages its rows open from [settingsPages] stacked above
 * it. Back leaves one page and focus returns to the row that opened it; Back on the main page closes the panel.
 */
@Composable internal fun Settings(s: TvState, model: TvViewModel, gate: PageGate = PageGate.Open, dismiss: () -> Unit) {
    val nav = remember { SettingsNav(mainPage) }
    var current by remember { mutableStateOf(nav.top) }
    // What the ChoiceRow below offers while its ChoicePage is on top.
    var choice by remember { mutableStateOf<ChoiceSpec?>(null) }
    val rows = remember(current) { PageRows() }
    val headerAction = remember { FocusRequester() }
    var readingFocused by remember { mutableStateOf(false) }
    var activeSection by remember { mutableStateOf(SectionId.APPEARANCE) }
    val scope = rememberCoroutineScope()
    val readingStep = with(LocalDensity.current) { 56.dp.toPx() }
    val scrollHintThreshold = with(LocalDensity.current) { 20.dp.toPx() }
    fun back() { if (nav.back()) current = nav.top else dismiss() }
    fun open(route: SettingsRoute, fromRowKey: String?) {
        val guard = (route as? SettingsRoute.Page)?.let { settingsPages.getValue(it.id).guard } ?: PageGuard.None
        gate.request(guard) { nav.open(route, fromRowKey); current = nav.top }
    }
    val atRoot = current.route == nav.root
    val page = (current.route as? SettingsRoute.Page)?.let { settingsPages.getValue(it.id) }
    val body = page?.body ?: PageBody.LIST
    val reading = body == PageBody.READING
    val mainScroll = rememberScrollState()
    val secondaryScroll = key(current) { rememberScrollState() }
    val scroll = if (atRoot) mainScroll else secondaryScroll
    val pageScope = SettingsScope(s, model, rows, navigate = ::open,
        showChoice = { rowKey, offered -> choice = offered; open(SettingsRoute.Choice(rowKey), rowKey) },
        showSection = { activeSection = it }, back = ::back, dismiss = dismiss)
    // The row the page starts on or returns to; the header action on a page without rows (Uygulama hakkında).
    fun entry() = rows.entry(current.focusKey) ?: headerAction

    Dialog(onDismissRequest = ::back, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        val windowInfo = LocalWindowInfo.current
        // A key pressed on this page before the window reported its focus has already moved from the entry row.
        var moved by remember(current) { mutableStateOf(false) }
        LaunchedEffect(current) {
            // The platform can assign its default focus after composition; wait for this dialog's window. A quick
            // Down can arrive before that report, so the entry is not forced back over the viewer's move.
            snapshotFlow { windowInfo.isWindowFocused }.first { it }
            if (!moved) entry().requestFocus()
        }
        Box(Modifier.fillMaxSize().onPreviewKeyEvent { if (it.type == KeyEventType.KeyDown) moved = true; false }
            .focusProperties { onEnter = { entry().requestFocus() } }.focusGroup()) {
            Column(Modifier.align(Alignment.CenterEnd).width(440.dp).fillMaxHeight()
                .background(Ink.Background, RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp))
                .testTag("settings-panel")) {
                Row(Modifier.fillMaxWidth().padding(start = 28.dp, end = 28.dp, top = 24.dp, bottom = 18.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(page?.title?.resolve() ?: choice?.title.orEmpty(), Modifier.weight(1f).testTag("settings-title"), color = Ink.Text,
                        fontFamily = Roboto, fontWeight = FontWeight.Medium, fontSize = 24.sp, lineHeight = 30.sp)
                    Action(stringResource(if (atRoot) R.string.action_close else R.string.action_back),
                        Modifier.focusRequester(headerAction).testTag("settings-close"), onClick = ::back)
                }
                if (atRoot) Text(stringResource(activeSection.label),
                    Modifier.padding(start = 28.dp, end = 28.dp, bottom = 12.dp)
                        .testTag("settings-section").semantics { heading() },
                    color = Ink.Mint, fontFamily = Roboto, fontWeight = FontWeight.Medium,
                    fontSize = 12.sp, lineHeight = 17.sp)
                Box(Modifier.fillMaxWidth().height(1.dp).background(Ink.Divider))
                Box(Modifier.weight(1f).testTag(if (reading) "settings-reading" else "settings-viewport")
                    .then(if (reading) Modifier
                    .padding(12.dp)
                    .border(if (readingFocused) 2.dp else 0.dp, if (readingFocused) Ink.Text else Color.Transparent, RoundedCornerShape(12.dp))
                    .onFocusChanged { readingFocused = it.isFocused }
                    .onPreviewKeyEvent { event ->
                        if (event.type != KeyEventType.KeyDown) false
                        else when {
                            event.key == Key.DirectionDown && scroll.canScrollForward -> {
                                scope.launch { scroll.scrollBy(readingStep) }; true
                            }
                            event.key == Key.DirectionUp && scroll.canScrollBackward -> {
                                scope.launch { scroll.scrollBy(-readingStep) }; true
                            }
                            else -> false
                        }
                    }.focusable() else Modifier)) {
                    if (body == PageBody.LAZY) key(current) { page?.content?.invoke(pageScope) }
                    // Rows cut by the divider or the hint row fade out instead of resting as half a line;
                    // the focused row keeps a 20 dp edge (SettingRow), so it never sits in the fade.
                    // Reading text starts on the 28 dp edge of the title inside its 12 dp frame.
                    if (body != PageBody.LAZY) Column(Modifier.fillMaxSize()
                        .then(if (body == PageBody.LIST || reading) Modifier.scrollEdgeFade(Ink.Background,
                            top = { scroll.canScrollBackward }, bottom = { scroll.canScrollForward }, topFade = 14.dp, bottomFade = 14.dp) else Modifier)
                        .verticalScroll(scroll)
                        .padding(start = if (reading) 16.dp else 28.dp, end = 28.dp,
                            top = if (reading) 12.dp else 18.dp, bottom = if (reading) 12.dp else 18.dp)
                        .testTag("settings-content"), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Each page composes on its own: the registered pages share this call site.
                        key(current) {
                            if (page != null) page.content(pageScope)
                            else choice?.let { pageScope.ChoicePage(it) }
                        }
                    }
                    if (scroll.maxValue > 0) ScrollIndicator(scroll, Modifier.align(Alignment.CenterEnd).padding(end = 10.dp, top = 10.dp, bottom = 10.dp))
                }
                Row(Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(if (scroll.value > scrollHintThreshold) stringResource(R.string.settings_scroll_up) else "", color = Ink.Muted, fontFamily = Roboto, fontSize = 12.sp)
                    Text(if (scroll.maxValue - scroll.value > scrollHintThreshold) stringResource(R.string.settings_scroll_more) else "", color = Ink.Muted, fontFamily = Roboto, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable private fun ScrollIndicator(scroll: ScrollState, modifier: Modifier) {
    Canvas(modifier.width(3.dp).fillMaxHeight()) {
        val total = scroll.viewportSize + scroll.maxValue
        val thumb = (size.height * scroll.viewportSize / total.toFloat()).coerceAtLeast(20.dp.toPx()).coerceAtMost(size.height)
        val top = (size.height - thumb) * scroll.value / scroll.maxValue.toFloat()
        drawRoundRect(Ink.Divider, cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.width / 2))
        drawRoundRect(Ink.Outline, topLeft = androidx.compose.ui.geometry.Offset(0f, top),
            size = androidx.compose.ui.geometry.Size(size.width, thumb), cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.width / 2))
    }
}
