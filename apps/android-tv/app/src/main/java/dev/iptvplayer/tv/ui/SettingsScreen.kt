package dev.iptvplayer.tv.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.tv.material3.*
import dev.iptvplayer.tv.*
import dev.iptvplayer.tv.data.MediaKind
import dev.iptvplayer.tv.data.CategoryVisibility
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

private enum class SettingsPage { MAIN, REMOVE, HISTORY, ABOUT, CATEGORIES, CATEGORY_LIST }

@Composable internal fun Settings(s: TvState, model: TvViewModel, dismiss: () -> Unit) {
    var page by remember { mutableStateOf(SettingsPage.MAIN) }
    var returnTo by remember { mutableStateOf(SettingsPage.MAIN) }
    val first = remember { FocusRequester() }
    val remove = remember { FocusRequester() }
    val history = remember { FocusRequester() }
    val about = remember { FocusRequester() }
    val categories = remember { FocusRequester() }
    val kindFocus = remember { listOf(MediaKind.LIVE, MediaKind.MOVIE, MediaKind.SERIES).associateWith { FocusRequester() } }
    val firstCategory = remember { FocusRequester() }
    // The section whose categories CATEGORY_LIST shows; also where focus returns on the CATEGORIES page.
    var categoryKind by remember { mutableStateOf(MediaKind.LIVE) }
    val cancel = remember { FocusRequester() }
    val headerAction = remember { FocusRequester() }
    var readingFocused by remember { mutableStateOf(false) }
    var activeSection by remember { mutableStateOf("Görünüm ve ses") }
    fun section(name: String) = Modifier.onFocusChanged { if (it.isFocused) activeSection = name }
    val scope = rememberCoroutineScope()
    val readingStep = with(LocalDensity.current) { 56.dp.toPx() }
    val scrollHintThreshold = with(LocalDensity.current) { 20.dp.toPx() }
    fun back() { when (page) {
        SettingsPage.MAIN -> dismiss()
        SettingsPage.CATEGORY_LIST -> { returnTo = SettingsPage.CATEGORY_LIST; page = SettingsPage.CATEGORIES }
        SettingsPage.CATEGORIES -> { returnTo = SettingsPage.CATEGORIES; page = SettingsPage.MAIN }
        else -> page = SettingsPage.MAIN
    } }
    fun open(next: SettingsPage) { returnTo = next; page = next }
    val mainScroll = rememberScrollState()
    val secondaryScroll = key(page) { rememberScrollState() }
    val scroll = if (page == SettingsPage.MAIN) mainScroll else secondaryScroll
    val visibleCount = remember(s.catalog) { s.catalog.items.count { !it.adult && it.kind != MediaKind.EPISODE } }
    val entryFocus = when (page) {
        SettingsPage.MAIN -> when (returnTo) {
            SettingsPage.REMOVE -> remove
            SettingsPage.HISTORY -> history
            SettingsPage.ABOUT -> about
            SettingsPage.CATEGORIES, SettingsPage.CATEGORY_LIST -> categories
            else -> first
        }
        SettingsPage.REMOVE, SettingsPage.HISTORY -> cancel
        SettingsPage.ABOUT -> headerAction
        SettingsPage.CATEGORIES -> kindFocus.getValue(categoryKind)
        SettingsPage.CATEGORY_LIST -> if (s.categoryChoices[categoryKind].isNullOrEmpty()) headerAction else firstCategory
    }

    Dialog(onDismissRequest = ::back, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        val windowInfo = LocalWindowInfo.current
        // A key pressed on this page before the window reported its focus has already moved from the entry row.
        var moved by remember(page) { mutableStateOf(false) }
        LaunchedEffect(page) {
            // The platform can assign its default focus after composition; wait for this dialog's window. A quick
            // Down can arrive before that report, so the entry is not forced back over the viewer's move.
            snapshotFlow { windowInfo.isWindowFocused }.first { it }
            if (!moved) entryFocus.requestFocus()
        }
        Box(Modifier.fillMaxSize().onPreviewKeyEvent { if (it.type == KeyEventType.KeyDown) moved = true; false }
            .focusProperties { onEnter = { entryFocus.requestFocus() } }.focusGroup()) {
            Column(Modifier.align(Alignment.CenterEnd).width(440.dp).fillMaxHeight()
                .background(Ink.Background, RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp))
                .testTag("settings-panel")) {
                Row(Modifier.fillMaxWidth().padding(start = 28.dp, end = 28.dp, top = 24.dp, bottom = 18.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(when (page) {
                        SettingsPage.MAIN -> "Ayarlar"
                        SettingsPage.REMOVE -> "Kaynağı kaldır?"
                        SettingsPage.HISTORY -> "Geçmişi temizle?"
                        SettingsPage.ABOUT -> "Uygulama hakkında"
                        SettingsPage.CATEGORIES -> "Kategoriler"
                        SettingsPage.CATEGORY_LIST -> when (categoryKind) { MediaKind.LIVE -> "Canlı TV"; MediaKind.MOVIE -> "Filmler"; else -> "Diziler" }
                    }, Modifier.weight(1f).testTag("settings-title"), color = Ink.Text, fontFamily = Roboto,
                        fontWeight = FontWeight.Medium, fontSize = 24.sp, lineHeight = 30.sp)
                    Action(if (page == SettingsPage.MAIN) "Kapat" else "Geri",
                        Modifier.focusRequester(headerAction).testTag("settings-close"), onClick = ::back)
                }
                if (page == SettingsPage.MAIN) Text(activeSection,
                    Modifier.padding(start = 28.dp, end = 28.dp, bottom = 12.dp)
                        .testTag("settings-section").semantics { heading() },
                    color = Ink.Mint, fontFamily = Roboto, fontWeight = FontWeight.Medium,
                    fontSize = 12.sp, lineHeight = 17.sp)
                Box(Modifier.fillMaxWidth().height(1.dp).background(Ink.Divider))
                Box(Modifier.weight(1f).testTag(if (page == SettingsPage.ABOUT) "settings-reading" else "settings-viewport")
                    .then(if (page == SettingsPage.ABOUT) Modifier
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
                    if (page == SettingsPage.CATEGORY_LIST) CategoryList(s.categoryChoices[categoryKind].orEmpty(), categoryKind, s, firstCategory,
                        { name, visible -> model.categoryVisible(categoryKind, name, visible) })
                    // Rows cut by the divider or the hint row fade out instead of resting as half a line;
                    // the focused row keeps a 20 dp edge (SettingRow), so it never sits in the fade.
                    // About text starts on the 28 dp edge of the title inside its 12 dp reading frame.
                    if (page != SettingsPage.CATEGORY_LIST) Column(Modifier.fillMaxSize()
                        .then(if (page == SettingsPage.MAIN || page == SettingsPage.ABOUT) Modifier.scrollEdgeFade(Ink.Background,
                            top = { scroll.canScrollBackward }, bottom = { scroll.canScrollForward }, topFade = 14.dp, bottomFade = 14.dp) else Modifier)
                        .verticalScroll(scroll)
                        .padding(start = if (page == SettingsPage.ABOUT) 16.dp else 28.dp, end = 28.dp,
                            top = if (page == SettingsPage.ABOUT) 12.dp else 18.dp, bottom = if (page == SettingsPage.ABOUT) 12.dp else 18.dp)
                        .testTag("settings-content"), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        when (page) {
                            SettingsPage.MAIN -> {
                                SettingRow("Hareketi azalt", "Vitrin geçişlerini, açılış animasyonunu ve menü kaymasını kapatır.", Modifier.focusRequester(first).then(section("Görünüm ve ses")),
                                    checked = s.reducedMotion) { model.reducedMotion() }
                                SettingRow("Açılış sesi", "Uygulama açılırken kısa bir ses çalar.", section("Görünüm ve ses"), checked = s.startupSound) { model.startupSound() }
                                Spacer(Modifier.height(6.dp))
                                Note("${s.sources.size} kaynak · $visibleCount görünür içerik")
                                SettingRow("Kaynakları yönet", "Ekle, düzenle, yenile veya kaldır.", section("Kaynaklar")) { dismiss(); model.manageSources() }
                                val refreshNotice = s.catalogNotice?.takeIf { it.operation == CatalogOperation.REFRESH }
                                // The timestamp captions the refresh row: 4 dp more above, 8 dp below (clear of the focus ring).
                                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (s.catalogUpdatedAt > 0) Note("Son güncelleme: " + java.text.SimpleDateFormat("d MMM, HH:mm",
                                        java.util.Locale.forLanguageTag("tr")).format(java.util.Date(s.catalogUpdatedAt)),
                                        modifier = Modifier.padding(top = 4.dp))
                                    SettingRow("Kataloğu yenile", if (s.refreshing) "Yenileniyor… Kütüphanen kullanılabilir."
                                        else refreshNotice?.message?.resolve() ?: "Yeni kanalları, filmleri ve dizileri getirir.",
                                        modifier = section("Kaynaklar"), busy = s.refreshing, attention = refreshNotice?.needsAttention == true) { model.refresh() }
                                }
                                s.catalogNotice?.takeIf { it.operation != CatalogOperation.REFRESH }?.let { Note(it.message.resolve(), error = it.needsAttention) }
                                if (s.sources.size == 1) SettingRow("Kaynağı düzenle", "Bağlantı ve hesap bilgilerini değiştir.", section("Kaynaklar")) { dismiss(); model.editSource() }
                                if (s.sources.size == 1) SettingRow("Kaynağı kaldır", "Bu kaynağı ve cihazdaki kayıtlarını kaldır.", Modifier.focusRequester(remove).then(section("Kaynaklar"))) { open(SettingsPage.REMOVE) }
                                Spacer(Modifier.height(6.dp))
                                // D068: what the screens show. A switch anyone can turn back, not a PIN lock.
                                SettingRow("Yetişkin kategorilerini gizle", "Adında XXX, Adult, +18, Yetişkin gibi ifadeler geçen kategoriler hiçbir yerde görünmez.",
                                    section("İçerik"), checked = s.hideAdultCategories) { model.hideAdultCategories() }
                                val hiddenCount = s.hiddenCategories.size
                                SettingRow("Kategorileri yönet", (if (hiddenCount > 0) "$hiddenCount kategori gizli. " else "") +
                                    "Canlı TV, film ve dizi kategorilerini aç veya kapat.",
                                    Modifier.focusRequester(categories).then(section("İçerik"))) { open(SettingsPage.CATEGORIES) }
                                Spacer(Modifier.height(6.dp))
                                SettingRow("Otomatik sonraki bölüm", "Bölüm bitince 10 saniye sonra devam eder. Geri sayımı iptal edebilirsin.",
                                    modifier = section("İzleme ve uygulama"), checked = s.autoNextEpisode) { model.autoNextEpisode() }
                                SettingRow("İzleme geçmişini temizle", "Tüm kaynaklardaki izleme konumlarını siler; favorilerini korur.", Modifier.focusRequester(history).then(section("İzleme ve uygulama"))) { open(SettingsPage.HISTORY) }
                                SettingRow("Uygulama hakkında", "Sürüm ve cihaz bilgileri.", Modifier.focusRequester(about).then(section("İzleme ve uygulama"))) { open(SettingsPage.ABOUT) }
                            }
                            SettingsPage.REMOVE, SettingsPage.HISTORY -> {
                                Note(if (page == SettingsPage.REMOVE)
                                    "${s.source?.name.orEmpty()} kaynağı, bu kaynağın favorileri ve izleme geçmişi bu cihazdan silinecek."
                                    else "Tüm kaynaklardaki son kanallar ve izleme konumları bu cihazdan kaldırılacak. Favorilerin korunacak.")
                                Spacer(Modifier.height(12.dp))
                                Action("Vazgeç", Modifier.focusRequester(cancel)) { page = SettingsPage.MAIN }
                                Action(if (page == SettingsPage.REMOVE) "Kaynağı kaldır" else "Geçmişi temizle") {
                                    if (page == SettingsPage.REMOVE) model.removeSource() else model.clearHistory()
                                    dismiss()
                                }
                            }
                            SettingsPage.CATEGORIES -> {
                                Note("Kapattığın kategoriler ana sayfada, kütüphanede, aramada ve rehberde görünmez. Kaynağın değişmez.")
                                listOf(MediaKind.LIVE to "Canlı TV", MediaKind.MOVIE to "Filmler", MediaKind.SERIES to "Diziler").forEach { (kind, label) ->
                                    val list = s.categoryChoices[kind].orEmpty()
                                    val off = list.count { CategoryVisibility.key(kind, it.name) in s.hiddenCategories || s.hideAdultCategories && it.adult }
                                    SettingRow(label, "${list.size} kategori" + if (off > 0) " · $off gizli" else "",
                                        Modifier.focusRequester(kindFocus.getValue(kind)).testTag("category-kind:${kind.name}")) {
                                        categoryKind = kind; returnTo = SettingsPage.CATEGORY_LIST; page = SettingsPage.CATEGORY_LIST
                                    }
                                }
                            }
                            SettingsPage.CATEGORY_LIST -> Unit
                            SettingsPage.ABOUT -> {
                                NextvLogo(height = 24.dp)
                                Note("Sürüm ${BuildConfig.VERSION_NAME}")
                                Note("Android ${android.os.Build.VERSION.RELEASE} · API ${android.os.Build.VERSION.SDK_INT}")
                                Spacer(Modifier.height(6.dp))
                                SettingsLabel("Kütüphane")
                                Note("Uygulama içerik sağlamaz. Kendi M3U veya Xtream Codes kaynağını ekleyerek izleyebilirsin.")
                                Note("Kaynağın ve katalog önbelleğin bu cihazda şifreli saklanır.")
                                Spacer(Modifier.height(6.dp))
                                SettingsLabel("Bu sürümde")
                                Note("Canlı TV, film, dizi, arama, favori, izlemeye devam et ve yayın rehberi kullanılabilir. Rehber bilgisi kaynağın desteğine bağlıdır. PIN yönetimi henüz yok. Sağlayıcının yetişkin işaretli içerikleri hiç gösterilmez; adı yetişkin içerik bildiren kategoriler Ayarlar'dan kapatılır.")
                            }
                        }
                    }
                    if (scroll.maxValue > 0) ScrollIndicator(scroll, Modifier.align(Alignment.CenterEnd).padding(end = 10.dp, top = 10.dp, bottom = 10.dp))
                }
                Row(Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(if (scroll.value > scrollHintThreshold) "↑ Yukarı" else "", color = Ink.Muted, fontFamily = Roboto, fontSize = 12.sp)
                    Text(if (scroll.maxValue - scroll.value > scrollHintThreshold) "↓ Daha fazla" else "", color = Ink.Muted, fontFamily = Roboto, fontSize = 12.sp)
                }
            }
        }
    }
}

/**
 * One section's categories as switches (Açık = shown), in the provider's order. Adult-looking ones stay off while the
 * adult filter is on, and say so. A lazy list: a provider can have hundreds of categories.
 */
@Composable private fun CategoryList(choices: List<dev.iptvplayer.tv.data.CategoryChoice>, kind: MediaKind, s: TvState,
    first: FocusRequester, toggle: (String, Boolean) -> Unit) {
    if (choices.isEmpty()) {
        Note("Bu bölümde kategori yok.", modifier = Modifier.padding(28.dp)); return
    }
    androidx.compose.foundation.lazy.LazyColumn(Modifier.fillMaxSize().testTag("settings-categories"),
        contentPadding = PaddingValues(start = 28.dp, end = 28.dp, top = 18.dp, bottom = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(choices.size, key = { choices[it].name }) { index ->
            val choice = choices[index]
            val byAdult = s.hideAdultCategories && choice.adult
            val visible = !byAdult && CategoryVisibility.key(kind, choice.name) !in s.hiddenCategories
            val (label, tag) = shownCategory(choice.name)
            SettingRow(label + if (tag.isNotEmpty()) " · $tag" else "",
                "${choice.count} içerik" + if (byAdult) " · Yetişkin filtresi gizliyor" else "",
                (if (index == 0) Modifier.focusRequester(first) else Modifier).testTag("category-choice:$index"), checked = visible) {
                if (!byAdult) toggle(choice.name, !visible)
            }
        }
    }
}

@Composable private fun SettingsLabel(label: String) {
    Text(label, color = Ink.Mint, fontFamily = Roboto, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 17.sp)
}

@Composable private fun SettingRow(label: String, description: String, modifier: Modifier = Modifier,
    checked: Boolean? = null, busy: Boolean = false, attention: Boolean = false, onClick: () -> Unit) {
    // Toggling can wrap the description onto another line at large font scales.
    // Keep the entire focused row visible after that size change as well as on initial focus.
    val relocation = remember { BringIntoViewRequester() }
    var focused by remember { mutableStateOf(false) }
    var size by remember { mutableStateOf(IntSize.Zero) }
    // 14 dp edge fade + 4 dp focus-ring outset; never more than the list's 18 dp padding, or the
    // request could not be satisfied at the first/last row.
    val safeEdge = with(LocalDensity.current) { 18.dp.toPx() }
    LaunchedEffect(focused, size) {
        if (focused) { withFrameNanos { }; relocation.bringIntoView(Rect(0f, -safeEdge, size.width.toFloat(), size.height + safeEdge)) }
    }
    Card(onClick, modifier.bringIntoViewRequester(relocation).onSizeChanged { size = it }
        .onFocusChanged { focused = it.isFocused }.fillMaxWidth().heightIn(min = 62.dp).semantics {
        if (checked != null) {
            role = Role.Switch
            toggleableState = if (checked) ToggleableState.On else ToggleableState.Off
            stateDescription = if (checked) "Açık" else "Kapalı"
        }
        if (busy) progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate
    }, shape = CardDefaults.shape(shape = RoundedCornerShape(TvLayout.CardRadius)),
        colors = CardDefaults.colors(containerColor = Ink.Container, focusedContainerColor = Ink.High),
        scale = CardDefaults.scale(focusedScale = 1f),
        border = CardDefaults.border(focusedBorder = focusRing(TvLayout.CardRadius))) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(label, color = Ink.Text, fontFamily = Roboto, fontSize = 14.sp, lineHeight = 19.sp)
                Text(description, color = if (attention) Ink.Error else Ink.Muted, fontFamily = Roboto, fontSize = 12.sp, lineHeight = 17.sp)
            }
            if (checked != null) Text(if (checked) "Açık" else "Kapalı",
                // Ink.Selected nearly equals the focused row colour, so the focused ON chip uses the dark chip too.
                Modifier.background(if (checked && !focused) Ink.Selected else Ink.Background, RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 6.dp).clearAndSetSemantics { },
                color = if (checked) Ink.Mint else Ink.Muted, fontFamily = Roboto, fontSize = 12.sp)
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
