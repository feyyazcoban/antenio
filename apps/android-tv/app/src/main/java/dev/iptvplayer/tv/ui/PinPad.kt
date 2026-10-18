package dev.iptvplayer.tv.ui

import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import dev.iptvplayer.tv.R
import dev.iptvplayer.tv.TvViewModel
import dev.iptvplayer.tv.data.AdminReason
import dev.iptvplayer.tv.data.PinFormat
import dev.iptvplayer.tv.data.PinPhase
import dev.iptvplayer.tv.data.PinPrompt
import dev.iptvplayer.tv.data.PinPurpose
import dev.iptvplayer.tv.data.PinStep
import dev.iptvplayer.tv.data.displayTitle
import kotlinx.coroutines.delay

/**
 * Opens the PIN pad over the calling screen or Settings page for the prompt the model has just opened (createPin,
 * requestAdmin, a locked title's open …). It closes when the prompt ends, and focus goes back to what `returnFocus`
 * names then.
 */
@Composable internal fun rememberPinOpener(): (returnFocus: () -> FocusRequester?) -> Unit {
    val open = rememberOverlayOpener()
    return remember(open) { { returnFocus -> open(AppOverlay.Pin, returnFocus) } }
}

private val keyRows = listOf("123", "456", "789")
private val keyGap = 10.dp
/** The most of a locked title's name the pad's sentence holds: at 2,0 text it then takes no more lines than the others. */
private const val UNLOCK_TITLE_MAX = 32

/**
 * The PIN pad (pin.md §3.1–3.2, §4): four digits from the remote's number keys or the pad on screen, masked as dots.
 * The fourth digit hands the PIN to the model, which checks or keeps it; until then the digits live in a CharArray
 * only, wiped when handed over and when the pad closes. While the PIN is checked, and during a wait after too many
 * wrong tries, the keys take nothing and Vazgeç stays; while a new PIN is saved Vazgeç and Back wait for it, as it would
 * land anyway. Back steps from the repeat to the first step, else closes.
 */
@Composable internal fun PinDialog(prompt: PinPrompt, model: TvViewModel) {
    val digits = remember { CharArray(PinFormat.LENGTH) }
    var count by remember { mutableIntStateOf(0) }
    DisposableEffect(Unit) { onDispose { digits.fill('\u0000') } }
    var forgetting by remember { mutableStateOf(false) }
    var askedForget by remember { mutableStateOf(false) }
    var typed by remember { mutableStateOf(false) }
    val checking = prompt.phase == PinPhase.Checking
    val waiting = prompt.phase as? PinPhase.Waiting
    val takesDigits = !checking && waiting == null && !forgetting
    fun type(digit: Char) {
        if (!takesDigits || count >= PinFormat.LENGTH) return
        typed = true
        digits[count++] = digit
        if (count == PinFormat.LENGTH) {
            val pin = digits.copyOf()
            digits.fill('\u0000'); count = 0
            model.submitPin(pin)
        }
    }
    fun erase() { if (takesDigits && count > 0) digits[--count] = '\u0000' }
    val first = remember { FocusRequester() }
    val cancel = remember { FocusRequester() }
    val forget = remember { FocusRequester() }
    // Opening during a wait, focus starts on Vazgeç: the keys take nothing yet.
    val startOnCancel = waiting != null
    // Back from the repeat starts the first step empty.
    SourceDialog(prompt.title(), first, { digits.fill('\u0000'); count = 0; model.pinBack() }, width = 640.dp, hidden = forgetting) {
        val window = LocalWindowInfo.current
        LaunchedEffect(forgetting) { if (!forgetting && askedForget) refocusAfterDialog(window) { forget } }
        // A wait found just after the pad opened moves focus to Vazgeç too; one a wrong try starts leaves it on the key.
        LaunchedEffect(waiting != null) { if (waiting != null && !typed) refocusAfterDialog(window) { cancel } }
        // The number keys type wherever focus is in the pad, as in the player; Delete erases.
        Row(Modifier.fillMaxWidth().testTag("pin-pad").onPreviewKeyEvent { event ->
            if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
            val code = event.nativeKeyEvent.keyCode
            val digit = (PlayerKey.of(code) as? PlayerKey.Digit)?.value
            when {
                digit != null -> { type('0' + digit); true }
                code == android.view.KeyEvent.KEYCODE_DEL -> { erase(); true }
                else -> false
            }
        }, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            // A large font scrolls this column; the keys keep their size beside it.
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // A new step ("Aynı 4 haneyi bir kez daha gir.") is read as it comes, not only the boxes emptying.
                Note(prompt.message(), modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                PinBoxes(if (checking) PinFormat.LENGTH else count)
                PinFeedback(prompt)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Action(stringResource(R.string.action_cancel), (if (startOnCancel) Modifier.focusRequester(first) else Modifier)
                        .focusRequester(cancel).testTag("pin-cancel"), enabled = !prompt.saving, outlined = true) { model.cancelPin() }
                    if (prompt.canForget) Action(stringResource(R.string.pin_forgot), Modifier.focusRequester(forget).testTag("pin-forgot"),
                        enabled = !checking, outlined = true) { askedForget = true; forgetting = true }
                }
            }
            PinKeys(takesDigits, if (startOnCancel) null else first, ::type, ::erase)
        }
    }
    if (forgetting) ForgotPinDialog({ forgetting = false }) { model.forgotPinReset() }
}

/**
 * Four boxes, a dot in each digit typed and a mint line under the next one's place; the boxes take no focus. TalkBack
 * reads how many digits are in, never which.
 */
@Composable private fun PinBoxes(filled: Int) {
    val spoken = stringResource(R.string.pin_entered, filled.toString(), PinFormat.LENGTH.toString())
    Row(Modifier.testTag("pin-boxes").clearAndSetSemantics { contentDescription = spoken; liveRegion = LiveRegionMode.Polite },
        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(PinFormat.LENGTH) { index ->
            Box(Modifier.size(40.dp, 48.dp).background(Ink.Background, RoundedCornerShape(8.dp))
                .border(1.dp, Ink.Outline, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                if (index < filled) Text("•", color = Ink.Text, fontFamily = Roboto, fontSize = 24.sp, lineHeight = 24.sp)
                if (index == filled) Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 7.dp).size(16.dp, 2.dp)
                    .background(Ink.Mint, RoundedCornerShape(1.dp)))
            }
        }
    }
}

/**
 * One line kept for what the pad has to say: the check under way, the wait, or why the last PIN was not taken. It is
 * the same node in every phase, so TalkBack hears each new message as a change of its live region.
 */
@Composable private fun PinFeedback(prompt: PinPrompt) {
    val waiting = prompt.phase as? PinPhase.Waiting
    val checking = prompt.phase == PinPhase.Checking
    val (shown, spoken) = when {
        checking -> stringResource(R.string.pin_checking).let { it to it }
        waiting != null -> {
            var left by remember(waiting) { mutableLongStateOf(waiting.untilMs - SystemClock.elapsedRealtime()) }
            LaunchedEffect(waiting) { while (left > 0) { delay(250); left = waiting.untilMs - SystemClock.elapsedRealtime() } }
            val seconds = (left.coerceAtLeast(0) + 999) / 1000
            // Read once as the wait starts; the countdown itself is not read every second.
            val total = ((waiting.totalMs + 999) / 1000).toInt()
            stringResource(R.string.pin_wait, clockTime(seconds * 1000)) to
                pluralStringResource(R.plurals.pin_wait_spoken, total, total.toString())
        }
        else -> prompt.feedback?.resolve().orEmpty().let { it to it }
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        if (checking) LoadingArc()
        // The error is said in words, never by colour alone; an empty line keeps its place.
        Text(shown, Modifier.testTag("pin-feedback").clearAndSetSemantics {
            contentDescription = spoken; liveRegion = if (checking) LiveRegionMode.Polite else LiveRegionMode.Assertive },
            color = if (checking) Ink.Text else Ink.Error, fontFamily = Roboto, fontSize = 13.sp, lineHeight = 19.sp, minLines = 1)
    }
}

/**
 * The on-screen keys as on a phone: 1 2 3 / 4 5 6 / 7 8 9 / · 0 Sil. Off while the pad takes no digits; a key that
 * goes off under focus keeps it (Action). [first] is where focus starts, on 1.
 */
@Composable private fun PinKeys(enabled: Boolean, first: FocusRequester?, type: (Char) -> Unit, erase: () -> Unit) {
    val deleteDescription = stringResource(R.string.pin_delete_description)
    // Sil still fits at the largest font.
    val keyWidth = if (LocalDensity.current.fontScale > 1.5f) 84.dp else 72.dp
    Column(Modifier.width(keyWidth * 3 + keyGap * 2), verticalArrangement = Arrangement.spacedBy(keyGap)) {
        keyRows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(keyGap)) {
                row.forEach { digit ->
                    Action(digit.toString(), Modifier.width(keyWidth).then(if (digit == '1' && first != null) Modifier.focusRequester(first) else Modifier)
                        .testTag("pin-key:$digit"), enabled = enabled) { type(digit) }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(keyGap)) {
            Spacer(Modifier.width(keyWidth))
            Action("0", Modifier.width(keyWidth).testTag("pin-key:0"), enabled = enabled) { type('0') }
            Action(stringResource(R.string.pin_delete), Modifier.width(keyWidth).semantics { contentDescription = deleteDescription }
                .testTag("pin-key:delete"), enabled = enabled) { erase() }
        }
    }
}

/**
 * "PIN'i unuttun mu?" (pin.md §3.11): what starting over removes, then Vazgeç first, so a repeated OK never wipes the
 * TV. The text scrolls with the arrows at a large font, as the source removal's does.
 */
@Composable private fun ForgotPinDialog(dismiss: () -> Unit, reset: () -> Unit) {
    val cancel = remember { FocusRequester() }
    val reader = remember { FocusRequester() }
    SourceDialog(stringResource(R.string.pin_forgot_question), cancel, dismiss) {
        RemoteTextReader(stringResource(R.string.pin_forgot_note), Modifier.weight(1f, fill = false).focusRequester(reader), "pin-forgot-reader")
        Column(Modifier.padding(vertical = 5.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Action(stringResource(R.string.action_cancel), Modifier.focusRequester(cancel).focusProperties { up = reader }
                .testTag("pin-forgot-cancel")) { dismiss() }
            Action(stringResource(R.string.pin_forgot_reset), Modifier.testTag("pin-forgot-reset"), outlined = true) { reset() }
        }
    }
}

@Composable @ReadOnlyComposable
private fun PinPrompt.title(): String = stringResource(when (step) {
    PinStep.NEW -> if (purpose == PinPurpose.Change) R.string.pin_title_change else R.string.pin_title_create
    PinStep.CONFIRM -> R.string.pin_title_confirm
    PinStep.VERIFY -> R.string.pin_title_verify
})

@Composable @ReadOnlyComposable
private fun PinPrompt.message(): String {
    // A locked title is named as its card shows it (D065), cut short so that a long name at large text leaves the boxes
    // and the feedback line in view below it.
    (purpose as? PinPurpose.Unlock)?.let { unlock ->
        val name = displayTitle(unlock.title).name
        return stringResource(R.string.pin_reason_unlock,
            if (name.length <= UNLOCK_TITLE_MAX) name else name.take(UNLOCK_TITLE_MAX - 1).trimEnd() + "…")
    }
    return stringResource(when (step) {
        PinStep.NEW -> if (purpose == PinPurpose.Change) R.string.pin_message_change else R.string.pin_message_create
        PinStep.CONFIRM -> R.string.pin_message_confirm
        PinStep.VERIFY -> when ((purpose as? PinPurpose.Verify)?.reason) {
            AdminReason.SHOW_ADULT -> R.string.pin_reason_show_adult
            AdminReason.CATEGORIES -> R.string.pin_reason_categories
            AdminReason.PARENTAL, null -> R.string.pin_reason_parental
        }
    })
}
