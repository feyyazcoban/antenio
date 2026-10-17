package dev.iptvplayer.tv.ui

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.*
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.iptvplayer.tv.*
import dev.iptvplayer.tv.R
import dev.iptvplayer.tv.data.*
import java.time.ZoneId
import java.util.Locale

/** [header] is off where the surrounding dialog already carries the "Yayın rehberi" title. */
@Composable @ReadOnlyComposable
internal fun guideStatusText(status: GuideStatus?, zone: ZoneId, header: Boolean = true): String {
    if (status == null || (!status.initialized && status.configured)) return stringResource(R.string.guide_checking)
    return buildString {
        if (!status.configured) append(stringResource(R.string.guide_no_address)) else if (header) append(stringResource(R.string.guide_title))
        status.info?.let { info ->
            append("\n" + stringResource(R.string.last_updated, "${guideDate(info.updatedAt, zone)}, ${guideTime(info.updatedAt, zone)}"))
            append("\n" + stringResource(R.string.text_pair, pluralStringResource(R.plurals.guide_programmes, info.programmes, info.programmes.toString()),
                pluralStringResource(R.plurals.count_channels, info.channels, info.channels.toString())))
            if (info.updatedAt < System.currentTimeMillis() - 6 * 3_600_000L) append("\n" + stringResource(R.string.guide_stale))
            if (info.invalid > 0) append("\n" + pluralStringResource(R.plurals.guide_invalid, info.invalid, info.invalid.toString()))
            if (info.programmes == 0) append("\n" + stringResource(R.string.guide_no_programmes))
        }
        if (status.info == null && status.configured) append("\n" + stringResource(R.string.guide_none_saved))
        if (status.refreshing) append("\n" + stringResource(R.string.guide_refreshing))
        status.error?.let { append("\n${it.resolve()}") }
        append("\n" + stringResource(R.string.guide_live_without))
    }.removePrefix("\n")
}

@Composable internal fun GuideSourceDialog(source: SourceConfig, s: TvState, model: TvViewModel, dismiss: () -> Unit) {
    val first = remember { FocusRequester() }
    val editorFocus = remember { FocusRequester() }
    val refreshFocus = remember { FocusRequester() }
    val editFocus = remember { FocusRequester() }
    var editing by remember { mutableStateOf(false) }
    var address by remember(source.id) { mutableStateOf(source.epgUrl) }
    val revision = remember { s.guideSaveRevision }
    val status = s.guideStatus[source.id]
    LaunchedEffect(source.id) { model.clearGuideSaveError(); model.ensureGuide(listOf(source.id), download = false) }
    LaunchedEffect(s.guideSaveRevision) { if (s.guideSaveRevision != revision) dismiss() }
    key(editing) {
        if (editing) SourceDialog(stringResource(R.string.guide_address_title), editorFocus, { editing = false }, width = 600.dp) {
            val scroll = rememberScrollState()
            LaunchedEffect(s.guideSaveError) { if (s.guideSaveError != null) scroll.scrollTo(scroll.maxValue) }
            Column(Modifier.weight(1f, fill = false).verticalScroll(scroll), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                EntryField(stringResource(R.string.guide_address_field), address, { address = it; model.clearGuideSaveError() },
                    Modifier.focusRequester(editorFocus).testTag("guide-url-field"), secret = true)
                Note(stringResource(R.string.guide_address_hint))
                s.guideSaveError?.let { Note(it.resolve(), error = true) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Action(stringResource(if (s.sourceTask == SourceTask.SAVING) R.string.action_saving else R.string.action_save), Modifier.testTag("save-guide-url"), primary = true) { model.saveGuideUrl(source.id, address) }
                Action(stringResource(R.string.action_cancel), Modifier.testTag("cancel-guide-url")) { editing = false }
            }
        } else SourceDialog(stringResource(R.string.guide_title), first, dismiss, width = 600.dp) {
            val origin = stringResource(if (source.epgUrl.isNotBlank()) R.string.guide_origin_custom
                else if (source.type == SourceType.XTREAM) R.string.guide_origin_account else R.string.guide_origin_list)
            RemoteTextReader(guideStatusText(status, model.guideZone(), header = false) + "\n\n" + origin,
                Modifier.weight(1f, fill = false).focusRequester(first).focusProperties { down = refreshFocus }, "guide-status-reader")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Action(stringResource(if (status?.refreshing == true) R.string.refreshing else R.string.guide_refresh), Modifier.focusRequester(refreshFocus).focusProperties { up = first; down = editFocus }.testTag("refresh-guide")) { model.refreshGuide(source.id) }
                if (status?.refreshing == true) Action(stringResource(R.string.action_stop), Modifier.testTag("cancel-guide-refresh")) { model.cancelGuide(source.id) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Action(stringResource(R.string.guide_edit_address), Modifier.focusRequester(editFocus).focusProperties { up = refreshFocus }.testTag("edit-guide-url")) { editing = true }
                Action(stringResource(R.string.action_close), Modifier.focusProperties { up = refreshFocus }.testTag("close-guide-settings"), onClick = dismiss)
            }
        }
    }
}

@Composable internal fun GuideZoneDialog(s: TvState, model: TvViewModel, dismiss: () -> Unit) {
    val first = remember { FocusRequester() }
    var query by remember { mutableStateOf("") }
    val zones = remember { (listOf("Europe/Istanbul", "Europe/Berlin", "UTC") + ZoneId.getAvailableZoneIds().sorted()).distinct() }
    val filtered = remember(query) { zones.filter { it.lowercase(Locale.ROOT).replace('_', ' ').contains(query.replace('İ', 'I').lowercase(Locale.ROOT).replace('ı', 'i')) } }
    SourceDialog(stringResource(R.string.guide_zone_title), first, dismiss, width = 600.dp) {
        EntryField(stringResource(R.string.guide_zone_search), query, { query = it }, Modifier.testTag("guide-zone-search"))
        val pitch = with(LocalDensity.current) { actionHeight().roundToPx() + 8.dp.roundToPx() }
        // Whole rows only, so no zone label rests cut above Kapat: the 4 + 4 dp padding equals one 8 dp gap.
        LazyColumn(Modifier.weight(1f, fill = false).layout { measurable, constraints ->
                val max = if (!constraints.hasBoundedHeight) constraints.maxHeight
                    else ((constraints.maxHeight / pitch).coerceAtLeast(1) * pitch).coerceIn(constraints.minHeight, constraints.maxHeight)
                val placeable = measurable.measure(constraints.copy(maxHeight = max))
                layout(placeable.width, placeable.height) { placeable.place(0, 0) }
            }, verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 4.dp)) {
            item { Action(stringResource(R.string.guide_zone_system, ZoneId.systemDefault().id), Modifier.fillMaxWidth().focusRequester(first).testTag("guide-zone-system"),
                selected = s.guideZoneId.isEmpty(), check = true) { model.guideZone(""); dismiss() } }
            items(filtered, key = { it }) { zone ->
                Action(zone.replace('_', ' '), Modifier.fillMaxWidth().testTag("guide-zone:$zone"), selected = s.guideZoneId == zone, check = true) { model.guideZone(zone); dismiss() }
            }
            if (filtered.isEmpty()) item { Note(stringResource(R.string.guide_zone_none)) }
        }
        Action(stringResource(R.string.action_close), Modifier.testTag("close-guide-zone"), onClick = dismiss)
    }
}
