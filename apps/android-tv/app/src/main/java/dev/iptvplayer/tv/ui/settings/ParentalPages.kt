package dev.iptvplayer.tv.ui.settings

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.iptvplayer.tv.R
import dev.iptvplayer.tv.TvState
import dev.iptvplayer.tv.TvViewModel
import dev.iptvplayer.tv.data.AdminFollowUp
import dev.iptvplayer.tv.data.AdminReason
import dev.iptvplayer.tv.data.CategoryVisibility
import dev.iptvplayer.tv.data.MediaEntry
import dev.iptvplayer.tv.data.MediaKind
import dev.iptvplayer.tv.data.ParentalStatus
import dev.iptvplayer.tv.data.SourceConfig
import dev.iptvplayer.tv.data.UiText
import dev.iptvplayer.tv.data.displayTitle
import dev.iptvplayer.tv.ui.Action
import dev.iptvplayer.tv.ui.rememberPinOpener
import dev.iptvplayer.tv.ui.shownCategory

internal val parentalPage = SettingsRoute.Page("parental")
internal val adultPinOfferPage = SettingsRoute.Page("adult-pin-offer")
private val removePinPage = SettingsRoute.Page("parental-remove")
private val lockedPage = SettingsRoute.Page("parental-locked")

/**
 * Ebeveyn denetimi and the pages around it (pin.md §3.1–3.3, §3.10, §4). The page itself asks for the PIN while one
 * is set; the question and the confirmation are reached only through it or through the adult filter's row.
 */
internal val parentalPageList: List<SettingsPageSpec> = listOf(
    SettingsPageSpec(parentalPage.id, UiText.Res(R.string.parental_title), SectionId.CONTENT,
        PageGuard.Pin(AdminReason.PARENTAL), notice = parentalNotice) { ParentalControl() },
    SettingsPageSpec(removePinPage.id, UiText.Res(R.string.parental_remove_question), SectionId.CONTENT, body = PageBody.SHORT_LIST) {
        RemovePin()
    },
    SettingsPageSpec(lockedPage.id, UiText.Res(R.string.parental_locked), SectionId.CONTENT, PageGuard.Pin(AdminReason.PARENTAL),
        notice = parentalNotice) { LockedContent() },
    SettingsPageSpec(adultPinOfferPage.id, UiText.Res(R.string.parental_offer_title), SectionId.CONTENT, body = PageBody.SHORT_LIST) {
        AdultPinOffer()
    },
)

/**
 * Asks for the parental PIN before a page with [PageGuard.Pin] opens, while a PIN guards Ayarlar and none was verified
 * since the panel opened (pin.md §4). The PIN pad opens over the panel; the page opens once the model reports the PIN
 * verified, and Vazgeç or Back forgets it, focus back on the row. Call it inside the panel's window.
 */
@Composable internal fun rememberPinGate(s: TvState, model: TvViewModel): PageGate {
    val openPin = rememberPinOpener()
    var pending by remember { mutableStateOf<(() -> Unit)?>(null) }
    val unlocked = s.parental.adminUnlocked
    val asking = s.parental.prompt != null
    LaunchedEffect(unlocked, asking) {
        val open = pending ?: return@LaunchedEffect
        if (unlocked) { pending = null; open() } else if (!asking) pending = null
    }
    return remember(model, openPin) {
        PageGate { guard, returnFocus, open ->
            val reason = (guard as? PageGuard.Pin)?.reason
            if (reason != null && model.requestAdmin(reason)) { pending = open; openPin(returnFocus) } else open()
        }
    }
}

/**
 * Without a PIN: make one. With one (verified to get here): change it, see and remove the locks (Kilitli içerikler),
 * choose whether source management and the app's opening ask for it (pin.md §3.8–3.9), lock again what PINs opened in
 * this session ("Açılan kilitleri kapat"), or remove it. What the last change did reads
 * above the rows (SettingsPageSpec.notice); focus then rests on the page's first row, as the row that opened the pad may
 * be gone. A PIN this visit did not verify or make (one saved after its pad was closed from elsewhere) sends the page
 * back to its row, rather than offer rows that would refuse.
 */
@Composable private fun SettingsScope.ParentalControl() {
    val openPin = rememberPinOpener()
    val locked = s.parental.status == ParentalStatus.ON && !s.parental.adminUnlocked
    LaunchedEffect(locked) { if (locked) back() }
    if (locked) return
    when (s.parental.status) {
        ParentalStatus.ON -> {
            NoteRow(stringResource(R.string.parental_note))
            ActionRow("change-pin", stringResource(R.string.parental_change), stringResource(R.string.parental_change_note),
                Modifier.testTag("parental-change")) { if (model.changePin()) openPin { rowEntry("change-pin") } }
            val locks = rememberLockList().size
            PageRow("locked", stringResource(R.string.parental_locked), if (locks > 0)
                pluralStringResource(R.plurals.parental_locked_count, locks, locks.toString()) else stringResource(R.string.parental_locked_none),
                lockedPage, Modifier.testTag("parental-locked"))
            // On tightens; off loosens, with the PIN verified to get here. Source management asks by default.
            ToggleRow("sources-need-pin", stringResource(R.string.parental_sources_need_pin),
                stringResource(R.string.parental_sources_need_pin_note), s.parental.sourcesNeedPin,
                Modifier.testTag("parental-sources-need-pin")) { model.setSourcesNeedPin(!s.parental.sourcesNeedPin) }
            ToggleRow("launch-needs-pin", stringResource(R.string.parental_launch_needs_pin),
                stringResource(R.string.parental_launch_needs_pin_note), s.parental.launchNeedsPin,
                Modifier.testTag("parental-launch-needs-pin")) { model.setLaunchNeedsPin(!s.parental.launchNeedsPin) }
            // Only while PINs opened locked titles in this session; focus moves up as the row leaves.
            if (s.parental.sessionGrants > 0) ActionRow("relock", stringResource(R.string.parental_relock),
                stringResource(R.string.parental_relock_note), Modifier.testTag("parental-relock")) {
                rowEntry("change-pin")?.requestFocus(); model.relockNow()
            }
            PageRow("remove-pin", stringResource(R.string.parental_remove), stringResource(R.string.parental_remove_note),
                removePinPage, Modifier.testTag("parental-remove"))
        }
        // A record that cannot be read puts the lock screen up (pin.md §3.9), so this shows only for the moment before.
        ParentalStatus.UNAVAILABLE -> NoteRow(stringResource(R.string.parental_row_unavailable), error = true)
        else -> {
            ActionRow("create-pin", stringResource(R.string.parental_create), stringResource(R.string.parental_create_note),
                Modifier.testTag("parental-create")) { if (model.createPin()) openPin { rowEntry("create-pin") } }
            NoteRow(stringResource(R.string.parental_off_note))
        }
    }
}

/** What Kilitli içerikler lists: locked sources, category rows (section and name) and titles locked on their own. */
private class LockList(val sources: List<SourceConfig>, val categories: List<Pair<MediaKind, String>>, val titles: List<MediaEntry>) {
    val size: Int get() = sources.size + categories.size + titles.size
}

/**
 * The locks of the published library. A title locked on its own, or a category row, is listed while the library shows
 * it: one a refresh dropped or a category hidden in Ayarlar keeps its lock, out of sight, until it is shown again.
 */
@Composable private fun SettingsScope.rememberLockList(): LockList = remember(s.locks, s.sources, s.allCatalog) {
    LockList(s.sources.filter { it.id in s.locks.sources }, s.locks.categories.mapNotNull(CategoryVisibility::row),
        if (s.locks.contents.isEmpty()) emptyList() else s.allCatalog.items.filter { it.id in s.locks.contents })
}

/**
 * Kilitli içerikler (pin.md §3.5): the sources, category rows and titles that ask for the PIN, under their headings,
 * each reading "Kilitli" with OK as "Kilidi kaldır". OK on a row removes its lock, and only its lock, with the PIN
 * verified to get here. Focus moves to the next row first, and with the last lock gone the page leaves for Ebeveyn
 * denetimi. What a removal could not do reads above the rows (SettingsPageSpec.notice).
 */
@Composable private fun SettingsScope.LockedContent() {
    val locked = s.parental.status != ParentalStatus.ON || !s.parental.adminUnlocked
    LaunchedEffect(locked) { if (locked) back() }
    if (locked) return
    val list = rememberLockList()
    if (list.size == 0) { NoteRow(stringResource(R.string.parental_locked_empty)); return }
    NoteRow(stringResource(R.string.parental_locked_note))
    val keys = list.sources.map { "source:${it.id}" } + list.categories.map { (kind, name) -> "category:${kind.name}:$name" } +
        list.titles.map { "title:${it.id}" }
    val lockedState = stringResource(R.string.lock_state)
    val removeLabel = stringResource(R.string.lock_remove)
    fun remove(key: String, action: () -> Unit) {
        val index = keys.indexOf(key)
        val next = keys.getOrNull(index + 1) ?: keys.getOrNull(index - 1)
        if (next == null) back() else rowEntry(next)?.requestFocus()
        action()
    }
    if (list.sources.isNotEmpty()) SettingsLabel(stringResource(R.string.parental_locked_sources))
    list.sources.forEach { source ->
        val key = "source:${source.id}"
        ActionRow(key, s.sourceName(source.id), stringResource(R.string.parental_locked_source_note), Modifier.testTag("locked-$key"),
            value = lockedState, clickLabel = removeLabel) {
            remove(key) { model.removeSourceLock(source.id) }
        }
    }
    if (list.categories.isNotEmpty()) SettingsLabel(stringResource(R.string.parental_locked_categories))
    list.categories.forEach { (kind, name) ->
        val key = "category:${kind.name}:$name"
        val (label, tag) = shownCategory(name)
        ActionRow(key, label + if (tag.isNotEmpty()) " · $tag" else "", stringResource(kind.label), Modifier.testTag("locked-$key"),
            value = lockedState, clickLabel = removeLabel) {
            remove(key) { model.removeCategoryLock(kind, name) }
        }
    }
    if (list.titles.isNotEmpty()) SettingsLabel(stringResource(R.string.parental_locked_titles))
    list.titles.forEach { title ->
        val key = "title:${title.id}"
        val kind = stringResource(when (title.kind) { MediaKind.LIVE -> R.string.kind_live; MediaKind.MOVIE -> R.string.kind_movie_single; else -> R.string.kind_series_single })
        ActionRow(key, displayTitle(title.title, title.year).name,
            if (s.sources.size > 1) stringResource(R.string.text_pair, kind, s.sourceName(title.sourceId)) else kind, Modifier.testTag("locked-$key"),
            value = lockedState, clickLabel = removeLabel) {
            remove(key) { model.removeTitleLock(title.id) }
        }
    }
}

/**
 * "PIN kaldırılsın mı?" (pin.md §3.10): Vazgeç first. The page stays until the removal has an answer, then Back shows
 * Ebeveyn denetimi as it is by then, focus on a row that is still there.
 */
@Composable private fun SettingsScope.RemovePin() {
    var asked by remember { mutableStateOf(false) }
    LaunchedEffect(asked, s.parental.status, s.parental.notice) {
        if (asked && (s.parental.status != ParentalStatus.ON || s.parental.notice != null)) back()
    }
    NoteRow(stringResource(R.string.parental_remove_confirm))
    Spacer(Modifier.height(12.dp))
    Action(stringResource(R.string.action_cancel), Modifier.focusRequester(rowFocus("cancel"))) { back() }
    Action(stringResource(R.string.parental_remove), Modifier.focusRequester(rowFocus("confirm")).testTag("parental-remove-confirm")) {
        if (!asked) { asked = true; model.removePin() }
    }
}

/**
 * "Önce PIN oluşturmak ister misin?" (pin.md §3.3), before the adult filter goes off on a TV without a PIN. PIN
 * oluştur takes focus first and turns the filter off once the new PIN is saved. The question is answered, and the page
 * leaves, once the filter is off or a PIN is set.
 */
@Composable private fun SettingsScope.AdultPinOffer() {
    val openPin = rememberPinOpener()
    LaunchedEffect(s.hideAdultCategories, s.parental.status) {
        if (!s.hideAdultCategories || s.parental.status != ParentalStatus.OFF) back()
    }
    NoteRow(stringResource(R.string.parental_offer_note))
    Spacer(Modifier.height(12.dp))
    Action(stringResource(R.string.parental_create), Modifier.focusRequester(rowFocus("create-pin", start = true))
        .testTag("offer-create-pin")) {
        if (model.createPin(then = AdminFollowUp.ShowAdult)) openPin { rowEntry("create-pin") }
    }
    // Should a PIN guard the filter after all, this asks for it, as the main page's row does.
    Action(stringResource(R.string.parental_offer_without), Modifier.focusRequester(rowFocus("without-pin"))
        .testTag("offer-without-pin")) { if (model.setHideAdult(false)) openPin { rowEntry("without-pin") } }
    Action(stringResource(R.string.action_cancel), Modifier.focusRequester(rowFocus("cancel"))) { back() }
}
