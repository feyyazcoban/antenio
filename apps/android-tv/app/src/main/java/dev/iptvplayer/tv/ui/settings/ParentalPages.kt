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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.iptvplayer.tv.R
import dev.iptvplayer.tv.TvState
import dev.iptvplayer.tv.TvViewModel
import dev.iptvplayer.tv.data.AdminFollowUp
import dev.iptvplayer.tv.data.AdminReason
import dev.iptvplayer.tv.data.ParentalStatus
import dev.iptvplayer.tv.data.UiText
import dev.iptvplayer.tv.ui.Action
import dev.iptvplayer.tv.ui.rememberPinOpener
import dev.iptvplayer.tv.ui.resolve

internal val parentalPage = SettingsRoute.Page("parental")
internal val adultPinOfferPage = SettingsRoute.Page("adult-pin-offer")
private val removePinPage = SettingsRoute.Page("parental-remove")

/**
 * Ebeveyn denetimi and the pages around it (pin.md §3.1–3.3, §3.10, §4). The page itself asks for the PIN while one
 * is set; the question and the confirmation are reached only through it or through the adult filter's row.
 */
internal val parentalPageList: List<SettingsPageSpec> = listOf(
    SettingsPageSpec(parentalPage.id, UiText.Res(R.string.parental_title), SectionId.CONTENT,
        PageGuard.Pin(AdminReason.PARENTAL)) { ParentalControl() },
    SettingsPageSpec(removePinPage.id, UiText.Res(R.string.parental_remove_question), SectionId.CONTENT, body = PageBody.SHORT_LIST) {
        RemovePin()
    },
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
 * Without a PIN: make one. With one (verified to get here): change it, lock again what PINs opened in this session
 * ("Açılan kilitleri kapat"), or remove it. What the last change did reads at the top; focus then rests on the page's
 * first row, as the row that opened the pad may be gone. A PIN this visit did not verify or make (one saved after its
 * pad was closed from elsewhere) sends the page back to its row, rather than offer rows that would refuse.
 */
@Composable private fun SettingsScope.ParentalControl() {
    val openPin = rememberPinOpener()
    val locked = s.parental.status == ParentalStatus.ON && !s.parental.adminUnlocked
    LaunchedEffect(locked) { if (locked) back() }
    if (locked) return
    s.parental.notice?.let { NoteRow(it.resolve()) }
    when (s.parental.status) {
        ParentalStatus.ON -> {
            NoteRow(stringResource(R.string.parental_note))
            ActionRow("change-pin", stringResource(R.string.parental_change), stringResource(R.string.parental_change_note),
                Modifier.testTag("parental-change")) { if (model.changePin()) openPin { rowEntry("change-pin") } }
            // Only while PINs opened locked titles in this session; focus moves up as the row leaves.
            if (s.parental.sessionGrants > 0) ActionRow("relock", stringResource(R.string.parental_relock),
                stringResource(R.string.parental_relock_note), Modifier.testTag("parental-relock")) {
                rowEntry("change-pin")?.requestFocus(); model.relockNow()
            }
            PageRow("remove-pin", stringResource(R.string.parental_remove), stringResource(R.string.parental_remove_note),
                removePinPage, Modifier.testTag("parental-remove"))
        }
        // A removal stopped halfway: the guard of this page then asks for a PIN that cannot be checked, and the pad
        // offers PIN'i unuttum.
        ParentalStatus.UNAVAILABLE -> NoteRow(stringResource(R.string.parental_row_unavailable), error = true)
        else -> {
            ActionRow("create-pin", stringResource(R.string.parental_create), stringResource(R.string.parental_create_note),
                Modifier.testTag("parental-create")) { if (model.createPin()) openPin { rowEntry("create-pin") } }
            NoteRow(stringResource(R.string.parental_off_note))
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
        if (model.createPin(then = AdminFollowUp.SHOW_ADULT)) openPin { rowEntry("create-pin") }
    }
    // Should a PIN guard the filter after all, this asks for it, as the main page's row does.
    Action(stringResource(R.string.parental_offer_without), Modifier.focusRequester(rowFocus("without-pin"))
        .testTag("offer-without-pin")) { if (model.setHideAdult(false)) openPin { rowEntry("without-pin") } }
    Action(stringResource(R.string.action_cancel), Modifier.focusRequester(rowFocus("cancel"))) { back() }
}
