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
import androidx.compose.ui.unit.dp
import dev.iptvplayer.tv.*
import dev.iptvplayer.tv.data.*
import java.time.ZoneId
import java.util.Locale

/** [header] is off where the surrounding dialog already carries the "Yayın rehberi" title. */
internal fun guideStatusText(status: GuideStatus?, zone: ZoneId, header: Boolean = true): String {
    if (status == null || (!status.initialized && status.configured)) return "Rehber kontrol ediliyor…"
    return buildString {
        if (!status.configured) append("Rehber adresi eklenmemiş") else if (header) append("Yayın rehberi")
        status.info?.let { info ->
            append("\nSon güncelleme: ${guideDate(info.updatedAt, zone)}, ${guideTime(info.updatedAt, zone)}")
            append("\n${info.programmes} program · ${info.channels} kanal")
            if (info.updatedAt < System.currentTimeMillis() - 6 * 3_600_000L) append("\nKayıtlı rehber eski olabilir; yenileyebilirsin.")
            if (info.invalid > 0) append("\n${info.invalid} geçersiz kayıt atlandı.")
            if (info.programmes == 0) append("\nSağlayıcı bu dönem için program bilgisi göndermedi.")
        }
        if (status.info == null && status.configured) append("\nKayıtlı rehber bulunmuyor.")
        if (status.refreshing) append("\nYenileniyor… Kayıtlı rehber kullanılabilir.")
        status.error?.let { append("\n$it") }
        append("\nRehber bilgisi olmasa da canlı kanallar izlenebilir.")
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
        if (editing) SourceDialog("Rehber adresi", editorFocus, { editing = false }, width = 600.dp) {
            val scroll = rememberScrollState()
            LaunchedEffect(s.guideSaveError) { if (s.guideSaveError != null) scroll.scrollTo(scroll.maxValue) }
            Column(Modifier.weight(1f, fill = false).verticalScroll(scroll), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                EntryField("XMLTV adresi", address, { address = it; model.clearGuideSaveError() },
                    Modifier.focusRequester(editorFocus).testTag("guide-url-field"), secret = true)
                Note("Boş bırak: otomatik rehberi kullan.")
                s.guideSaveError?.let { Note(it, error = true) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Action(if (s.sourceTask == SourceTask.SAVING) "Kaydediliyor…" else "Kaydet", Modifier.testTag("save-guide-url"), primary = true) { model.saveGuideUrl(source.id, address) }
                Action("Vazgeç", Modifier.testTag("cancel-guide-url")) { editing = false }
            }
        } else SourceDialog("Yayın rehberi", first, dismiss, width = 600.dp) {
            val origin = if (source.epgUrl.isNotBlank()) "Özel rehber adresi kullanılıyor."
                else if (source.type == SourceType.XTREAM) "Hesabının rehber adresi kullanılıyor." else "M3U listesinin rehber adresi kullanılıyor."
            RemoteTextReader(guideStatusText(status, model.guideZone(), header = false) + "\n\n" + origin,
                Modifier.weight(1f, fill = false).focusRequester(first).focusProperties { down = refreshFocus }, "guide-status-reader")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Action(if (status?.refreshing == true) "Yenileniyor…" else "Rehberi yenile", Modifier.focusRequester(refreshFocus).focusProperties { up = first; down = editFocus }.testTag("refresh-guide")) { model.refreshGuide(source.id) }
                if (status?.refreshing == true) Action("İptal et", Modifier.testTag("cancel-guide-refresh")) { model.cancelGuide(source.id) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Action("Adresi düzenle", Modifier.focusRequester(editFocus).focusProperties { up = refreshFocus }.testTag("edit-guide-url")) { editing = true }
                Action("Kapat", Modifier.focusProperties { up = refreshFocus }.testTag("close-guide-settings"), onClick = dismiss)
            }
        }
    }
}

@Composable internal fun GuideZoneDialog(s: TvState, model: TvViewModel, dismiss: () -> Unit) {
    val first = remember { FocusRequester() }
    var query by remember { mutableStateOf("") }
    val zones = remember { (listOf("Europe/Istanbul", "Europe/Berlin", "UTC") + ZoneId.getAvailableZoneIds().sorted()).distinct() }
    val filtered = remember(query) { zones.filter { it.lowercase(Locale.ROOT).replace('_', ' ').contains(query.replace('İ', 'I').lowercase(Locale.ROOT).replace('ı', 'i')) } }
    SourceDialog("Rehber saat dilimi", first, dismiss, width = 600.dp) {
        EntryField("Şehir veya bölge ara", query, { query = it }, Modifier.testTag("guide-zone-search"))
        val pitch = with(LocalDensity.current) { actionHeight().roundToPx() + 8.dp.roundToPx() }
        // Whole rows only, so no zone label rests cut above Kapat: the 4 + 4 dp padding equals one 8 dp gap.
        LazyColumn(Modifier.weight(1f, fill = false).layout { measurable, constraints ->
                val max = if (!constraints.hasBoundedHeight) constraints.maxHeight
                    else ((constraints.maxHeight / pitch).coerceAtLeast(1) * pitch).coerceIn(constraints.minHeight, constraints.maxHeight)
                val placeable = measurable.measure(constraints.copy(maxHeight = max))
                layout(placeable.width, placeable.height) { placeable.place(0, 0) }
            }, verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 4.dp)) {
            item { Action("TV’nin saat dilimi · ${ZoneId.systemDefault().id}", Modifier.fillMaxWidth().focusRequester(first).testTag("guide-zone-system"),
                selected = s.guideZoneId.isEmpty(), check = true) { model.guideZone(""); dismiss() } }
            items(filtered, key = { it }) { zone ->
                Action(zone.replace('_', ' '), Modifier.fillMaxWidth().testTag("guide-zone:$zone"), selected = s.guideZoneId == zone, check = true) { model.guideZone(zone); dismiss() }
            }
            if (filtered.isEmpty()) item { Note("Bu adla saat dilimi bulunamadı.") }
        }
        Action("Kapat", Modifier.testTag("close-guide-zone"), onClick = dismiss)
    }
}
