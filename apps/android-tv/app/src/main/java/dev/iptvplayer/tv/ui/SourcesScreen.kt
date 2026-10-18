package dev.iptvplayer.tv.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.*
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.tv.material3.*
import dev.iptvplayer.tv.*
import dev.iptvplayer.tv.R
import dev.iptvplayer.tv.data.*
import kotlinx.coroutines.flow.first

/**
 * Kaynakların. While a PIN guards source management the PIN that opened this screen holds for what is done here (pin.md
 * §3.8); the pad still opens if it ended meanwhile. With a PIN set, a source's Bilgiler locks the whole source, and
 * TalkBack reads a locked source's Bilgiler as locked, as the card's lock mark is silent.
 */
@Composable internal fun SourcesScreen(s: TvState, model: TvViewModel) {
    val first = remember { FocusRequester() }
    val openPin = rememberPinOpener()
    val addFocus = remember { FocusRequester() }
    val target = s.sourceManagerFocus.takeIf { key -> key == "add-source" || s.sources.any { key == "edit-source:${it.id}" || key == "remove-source:${it.id}" } } ?: "add-source"
    val targetIndex = s.sources.indexOfFirst { target.endsWith(":${it.id}") }.coerceAtLeast(0)
    val list = rememberLazyListState(initialFirstVisibleItemIndex = targetIndex)
    val listEntry = remember { FocusRequester() }
    val firstVisibleSource by remember(list, s.sources) { derivedStateOf { s.sources.getOrNull(list.firstVisibleItemIndex)?.id } }
    fun focus(key: String): Modifier = if (target == key) Modifier.focusRequester(first) else Modifier
    var removal by remember { mutableStateOf<SourceConfig?>(null) }
    var info by remember { mutableStateOf<SourceConfig?>(null) }
    var guideOptions by remember { mutableStateOf(false) }
    val guideOpener = remember { FocusRequester() }
    val dialogOpener = remember { FocusRequester() }
    val lockedState = stringResource(R.string.lock_state)
    Column(Modifier.fillMaxSize().padding(horizontal = TvLayout.Gutter, vertical = 28.dp).testTag("sources-screen"),
        verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Row(Modifier.fillMaxWidth().testTag("sources-header")
            .focusProperties { if (s.sources.isNotEmpty()) down = listEntry }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Heading(stringResource(R.string.sources_title), Modifier.testTag("sources-title"))
                Note(pluralStringResource(R.plurals.sources_summary, s.sources.size, s.sources.size.toString()))
            }
            Action(stringResource(R.string.sources_add), focus("add-source").focusRequester(addFocus).testTag("add-source"), primary = true) {
                if (model.addSource()) openPin { addFocus }
            }
            Action(stringResource(R.string.action_back)) { model.back() }
        }
        s.catalogNotice?.let { Note(it.message.resolve(), error = it.needsAttention) }
        LazyColumn(Modifier.weight(1f).testTag("sources-list")
            .scrollEdgeFade(Ink.Background, top = { list.canScrollBackward }, bottom = { list.canScrollForward }),
            state = list, verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 20.dp)) {
            items(s.sources, key = { it.id }) { source ->
                val status = s.sourceStatus[source.id] ?: SourceStatus()
                val relocation = remember { BringIntoViewRequester() }
                var focused by remember { mutableStateOf(false) }
                var size by remember { mutableStateOf(IntSize.Zero) }
                // Keep the focused card outside the edge fades; the list padding matches, so the first and last cards can satisfy it.
                val edge = with(LocalDensity.current) { 20.dp.toPx() }
                LaunchedEffect(focused, size) { if (focused) { withFrameNanos { }
                    relocation.bringIntoView(Rect(0f, -edge, size.width.toFloat(), size.height + edge)) } }
                Column(Modifier.bringIntoViewRequester(relocation).onSizeChanged { size = it }
                    .onFocusChanged { focused = it.hasFocus }.focusGroup()
                    .fillMaxWidth().testTag("source:${source.id}")
                    .background(Ink.Container, RoundedCornerShape(TvLayout.HeroRadius))
                    .border(1.dp, if (focused) Ink.Mint else Ink.Divider, RoundedCornerShape(TvLayout.HeroRadius))
                    .padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(sourceNumber(s, source), Modifier.weight(1f, fill = false),
                            color = Ink.Mint, fontFamily = Roboto, fontSize = 12.sp, lineHeight = 17.sp,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                        // A locked source (Bilgiler says what it locks).
                        if (source.id in s.locks.sources) Box(Modifier.testTag("source-locked:${source.id}")) { LockMark(color = Ink.Mint) }
                        Spacer(Modifier.weight(1f))
                        Text(pluralStringResource(R.plurals.count_items, status.count, status.count.toString()), color = Ink.Muted, fontFamily = Roboto,
                            fontSize = 12.sp, lineHeight = 17.sp, maxLines = 1)
                    }
                    Text(s.sourceName(source.id), Modifier.fillMaxWidth().testTag("source-name:${source.id}"),
                        color = Ink.Text, fontFamily = Roboto, fontWeight = FontWeight.Medium,
                        fontSize = 18.sp, lineHeight = 24.sp, maxLines = 1, overflow = TextOverflow.MiddleEllipsis)
                    Note(if (status.updatedAt > 0) stringResource(R.string.last_updated, updatedTime(status.updatedAt))
                        else stringResource(R.string.sources_not_downloaded))
                    if (status.refreshing) Note(stringResource(R.string.sources_refreshing))
                    else if (status.notices.isNotEmpty()) Text(status.notices.resolve(), color = if (status.failed) Ink.Error else Ink.Muted,
                        fontFamily = Roboto, fontSize = 12.sp, lineHeight = 18.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Action(stringResource(R.string.sources_info), Modifier.testTag("info-source:${source.id}")
                            .then(if (source.id in s.locks.sources) Modifier.semantics { stateDescription = lockedState } else Modifier)
                            .then(if (firstVisibleSource == source.id) Modifier.focusRequester(listEntry) else Modifier)
                            .then(if (info?.id == source.id) Modifier.focusRequester(dialogOpener) else Modifier)) { info = source }
                        Action(stringResource(R.string.sources_refresh), Modifier.testTag("refresh-source:${source.id}")) { model.refresh(source.id) }
                        val edit = remember { FocusRequester() }
                        Action(stringResource(R.string.sources_edit), focus("edit-source:${source.id}").focusRequester(edit).testTag("edit-source:${source.id}")) {
                            if (model.editSource(source.id)) openPin { edit }
                        }
                        Action(stringResource(R.string.sources_remove), focus("remove-source:${source.id}").testTag("remove-source:${source.id}")
                            .then(if (removal?.id == source.id) Modifier.focusRequester(dialogOpener) else Modifier)) { removal = source }
                    }
                }
            }
        }
    }
    LaunchedEffect(Unit) { withFrameNanos { }; first.requestFocus() }
    info?.let { selected ->
        val source = s.sources.firstOrNull { it.id == selected.id } ?: selected
        val reader = remember { FocusRequester() }
        LaunchedEffect(source.id) { model.ensureGuide(listOf(source.id), download = false) }
        fun dismiss() { dialogOpener.requestFocus(); info = null }
        // With a PIN set, the whole source locks at once; its lock goes with the PIN that opened Kaynaklar, or asks for it
        // over this window (pin.md §3.8). Not a fifth button on the card: it would not fit at large text. At large text
        // the window widens and its three actions share rows, so the details keep lines to read above them.
        val lockable = s.parental.status == ParentalStatus.ON
        val shared = lockable && LocalDensity.current.fontScale >= 1.5f
        SourceDialog(stringResource(R.string.sources_info_title), reader, ::dismiss, width = if (shared) 640.dp else 480.dp, hidden = guideOptions) {
            val status = s.sourceStatus[source.id] ?: SourceStatus()
            val locked = source.id in s.locks.sources
            RemoteTextReader("${s.sourceName(source.id)}\n\n${sourceNumber(s, source)}\n" +
                pluralStringResource(R.plurals.count_items, status.count, status.count.toString()) +
                (if (locked) "\n" + stringResource(R.string.parental_locked_source_note) else "") +
                (if (status.notices.isNotEmpty()) "\n\n${status.notices.resolve()}" else "") + "\n\n" + guideStatusText(s.guideStatus[source.id], model.guideZone()),
                Modifier.weight(1f, fill = false).focusRequester(reader).focusProperties { down = guideOpener }, "source-info-reader")
            val openLockPin = rememberPinOpener()
            val lockFocus = remember { FocusRequester() }
            val actions = @Composable {
                Action(stringResource(R.string.sources_guide_settings), Modifier.focusRequester(guideOpener).focusProperties { up = reader }.testTag("source-guide-settings")) { guideOptions = true }
                if (lockable) Action(stringResource(if (locked) R.string.source_lock_remove else R.string.source_lock_add),
                    Modifier.focusRequester(lockFocus).testTag("source-lock"), icon = "lock") {
                    if (model.setSourceLock(source.id, !locked)) openLockPin { lockFocus }
                }
                Action(stringResource(R.string.action_close), Modifier.testTag("close-source-info")) { dismiss() }
            }
            if (shared) FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { actions() }
            else actions()
        }
        if (guideOptions) GuideSourceDialog(source, s, model) { guideOptions = false; guideOpener.requestFocus() }
    }
    removal?.let { source ->
        val cancel = remember { FocusRequester() }
        val reader = remember { FocusRequester() }
        fun dismiss() { dialogOpener.requestFocus(); removal = null }
        SourceDialog(stringResource(R.string.remove_source_question), cancel, ::dismiss) {
            RemoteTextReader(sourceNumber(s, source) + "\n" + stringResource(R.string.sources_remove_note) + "\n\n" +
                stringResource(R.string.sources_full_name, s.sourceName(source.id)),
                Modifier.weight(1f, fill = false).focusRequester(reader), "source-removal-reader")
            Column(Modifier.padding(vertical = 5.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Action(stringResource(R.string.action_cancel), Modifier.focusRequester(cancel).focusProperties { up = reader }.testTag("cancel-source-removal")) { dismiss() }
                Action(stringResource(R.string.remove_source), Modifier.testTag("confirm-source-removal")) {
                    removal = null
                    if (model.removeSource(source.id)) openPin { listEntry }
                }
            }
        }
    }
}

/** [hidden] keeps a dialog (and its focus return path) alive while a child dialog covers it, without its edges peeking out. */
@Composable internal fun SourceDialog(title: String, initial: FocusRequester, dismiss: () -> Unit, scrollBody: Boolean = false, width: Dp = 480.dp,
    hidden: Boolean = false, content: @Composable ColumnScope.() -> Unit) {
    Dialog(onDismissRequest = dismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        val window = LocalWindowInfo.current
        Column(Modifier.width(width).heightIn(max = 460.dp).testTag("source-dialog")
            .graphicsLayer { alpha = if (hidden) 0f else 1f }.then(if (hidden) Modifier.clearAndSetSemantics { } else Modifier)
            .background(Ink.Container, RoundedCornerShape(18.dp))
            .focusProperties { onEnter = { initial.requestFocus() } }.focusGroup().padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Heading(title, Modifier.semantics { heading() })
            if (scrollBody) Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()).padding(5.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp), content = content)
            else content()
        }
        LaunchedEffect(Unit) {
            snapshotFlow { window.isWindowFocused }.first { it }
            initial.requestFocus()
        }
    }
}

/** "Kaynak 2 · M3U": the source's place in the list and its kind. */
@Composable @ReadOnlyComposable
private fun sourceNumber(s: TvState, source: SourceConfig): String =
    stringResource(R.string.sources_number, (s.sources.indexOf(source) + 1).toString(), source.type.label())

@Composable @ReadOnlyComposable
private fun SourceType.label() = stringResource(if (this == SourceType.XTREAM) R.string.source_type_xtream else R.string.source_type_m3u)

