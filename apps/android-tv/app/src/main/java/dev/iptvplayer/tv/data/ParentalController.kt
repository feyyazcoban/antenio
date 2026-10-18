package dev.iptvplayer.tv.data

import dev.iptvplayer.tv.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Whether this TV has a parental PIN. Until the record is read, and while it cannot be, loosening asks for one. */
enum class ParentalStatus { LOADING, OFF, ON, UNAVAILABLE }

/** Why Ayarlar asks for the PIN (pin.md §9); each reason has its sentence on the pad. */
enum class AdminReason { SHOW_ADULT, CATEGORIES, PARENTAL }

/** What a PIN verified or made on the pad then does in the model. Opening a page stays with the Settings panel. */
enum class AdminFollowUp { SHOW_ADULT }

/** What the pad is for: the first PIN, a new one for the PIN set, or the PIN checked before [Verify.reason]. */
sealed interface PinPurpose {
    data object Create : PinPurpose
    data object Change : PinPurpose
    data class Verify(val reason: AdminReason) : PinPurpose
}

/** A new PIN, the same PIN again, or the PIN to check. */
enum class PinStep { NEW, CONFIRM, VERIFY }

sealed interface PinPhase {
    /** The keys take digits. */
    data object Entry : PinPhase
    /** The PIN is being checked or saved ("Kontrol ediliyor…"); the keys take nothing. */
    data object Checking : PinPhase
    /** Too many wrong tries: the keys take nothing until [untilMs] on the elapsed clock, [totalMs] after it began. */
    data class Waiting(val untilMs: Long, val totalMs: Long) : PinPhase
}

/** The open PIN pad (pin.md §7). It holds no digit: those stay on the pad until it hands them over. */
data class PinPrompt(val id: Int, val purpose: PinPurpose, val step: PinStep, val phase: PinPhase = PinPhase.Entry,
    val feedback: UiText? = null, val then: AdminFollowUp? = null) {
    /** "PIN'i unuttum" belongs to a check; a parent making a PIN knows it. */
    val canForget: Boolean get() = step == PinStep.VERIFY
}

/**
 * Parental control as the screens see it. [adminUnlocked]: a PIN verified or made since Settings opened, which a
 * loosening needs while [guarded]. [prompt] is the PIN pad on screen. [notice] says how the last change on Ebeveyn
 * denetimi went. [lockEpoch] grows when Settings must close: the app left the screen, or the TV starts over.
 */
data class ParentalUi(val status: ParentalStatus = ParentalStatus.LOADING, val adminUnlocked: Boolean = false,
    val prompt: PinPrompt? = null, val notice: UiText? = null, val lockEpoch: Int = 0) {
    /** Loosening asks for the PIN: one is set, or the record is not read yet or cannot be (fail closed, D049). */
    val guarded: Boolean get() = status != ParentalStatus.OFF
    val mayLoosen: Boolean get() = !guarded || adminUnlocked
}

/**
 * The PIN pad's steps and the grant in Ayarlar (pin.md §3.1–3.3, §3.10–3.11), between the screens and ParentalGuard,
 * which does the slow and durable work. Main thread. A new PIN is asked twice and kept in memory only between the two
 * steps. A verified PIN grants Ayarlar until the panel closes or the app leaves the screen. A result that arrives after
 * the pad closed only updates the status: Vazgeç never grants anything, and a try it cut short stays charged (D048).
 */
internal class ParentalController(private val scope: CoroutineScope, private val guard: ParentalGuard,
    private val elapsed: () -> Long, private val followUp: (AdminFollowUp) -> Unit) {
    private val mutable = MutableStateFlow(ParentalUi())
    val state = mutable.asStateFlow()
    private var prompts = 0
    /** The first step's PIN while the second step asks for it again. */
    private var firstPin: CharArray? = null
    /** The policy revision the last verified or new PIN saw; a PIN change made with an older one is refused. */
    private var revision: Long? = null
    private var waitJob: Job? = null

    /** Reads whether this TV has a PIN: as the app opens, and after the record may have changed. */
    suspend fun refresh() {
        val status = when (guard.read()) {
            ProtectionRead.Unconfigured -> ParentalStatus.OFF
            is ProtectionRead.Ready -> ParentalStatus.ON
            ProtectionRead.Unavailable -> ParentalStatus.UNAVAILABLE
        }
        mutable.update { it.copy(status = status, adminUnlocked = it.adminUnlocked && status == ParentalStatus.ON) }
    }

    /** True when the PIN pad opened before [reason], [then] to follow; false when nothing needs asking. */
    fun requestAdmin(reason: AdminReason, then: AdminFollowUp? = null): Boolean {
        if (state.value.mayLoosen) return false
        val id = open(PinPurpose.Verify(reason), PinStep.VERIFY, then)
        // A wait that earlier tries started is still running: the pad opens waiting. A PIN already sent meets it itself.
        scope.launch {
            val ms = guard.waitMs() ?: 0L
            if (ms > 0 && state.value.prompt?.let { it.id == id && it.phase == PinPhase.Entry } == true) wait(id, ms)
        }
        return true
    }

    /** True when the pad opened for the first PIN, [then] to follow once it is saved; false while a PIN may be set. */
    fun createPin(then: AdminFollowUp? = null): Boolean {
        if (state.value.status != ParentalStatus.OFF) return false
        open(PinPurpose.Create, PinStep.NEW, then)
        return true
    }

    /** True when the pad opened for a new PIN; only with the PIN verified in this visit (pin.md §3.10). */
    fun changePin(): Boolean {
        if (state.value.status != ParentalStatus.ON || !state.value.adminUnlocked) return false
        open(PinPurpose.Change, PinStep.NEW, null)
        return true
    }

    /** Four digits from the pad: kept for the second step, or wiped here or by the guard, whatever happens. */
    fun submit(pin: CharArray) {
        val prompt = state.value.prompt?.takeIf { it.phase == PinPhase.Entry && PinFormat.isValid(pin) }
        if (prompt == null) { pin.fill('\u0000'); return }
        when (prompt.step) {
            PinStep.NEW -> if (PinFormat.isEasy(pin)) {
                pin.fill('\u0000')
                show(prompt.copy(feedback = UiText.Res(R.string.pin_easy)))
            } else {
                forgetFirst(); firstPin = pin
                show(prompt.copy(step = PinStep.CONFIRM, feedback = null))
            }
            PinStep.CONFIRM -> {
                val first = firstPin ?: run { pin.fill('\u0000'); return }
                firstPin = null
                val same = first.contentEquals(pin)
                pin.fill('\u0000')
                if (!same) {
                    first.fill('\u0000')
                    show(prompt.copy(step = PinStep.NEW, feedback = UiText.Res(R.string.pin_mismatch)))
                    return
                }
                show(prompt.copy(phase = PinPhase.Checking, feedback = null))
                scope.launch {
                    saved(prompt, if (prompt.purpose == PinPurpose.Change) guard.replacePin(first, revision ?: -1) else guard.create(first))
                }
            }
            PinStep.VERIFY -> {
                show(prompt.copy(phase = PinPhase.Checking, feedback = null))
                scope.launch { verified(prompt, guard.verify(pin)) }
            }
        }
    }

    /** Back on the pad: the repeat returns to the first step; any other step closes as Vazgeç does. */
    fun back() {
        val prompt = state.value.prompt ?: return
        if (prompt.step == PinStep.CONFIRM && prompt.phase == PinPhase.Entry) {
            forgetFirst()
            show(prompt.copy(step = PinStep.NEW, feedback = null))
        } else cancel()
    }

    /** Vazgeç: the pad closes and grants nothing. A check or save under way finishes for the record only. */
    fun cancel() {
        forgetFirst(); waitJob?.cancel()
        mutable.update { it.copy(prompt = null) }
    }

    /** "PIN'i kaldır", with the PIN verified in this visit; hidden categories and the adult filter stay as they are. */
    fun removePin() {
        if (state.value.status != ParentalStatus.ON || !state.value.adminUnlocked) return
        mutable.update { it.copy(notice = null) }
        scope.launch {
            val removed = guard.reset()
            if (removed) revision = null
            // Off, or a removal stopped halfway that reads as unavailable until it is finished.
            refresh()
            mutable.update { it.copy(notice = UiText.Res(if (removed) R.string.parental_removed else R.string.parental_not_removed)) }
        }
    }

    /** The Settings panel closed: what a verified PIN opened there ends with it. */
    fun closeSettings() { mutable.update { it.copy(adminUnlocked = false, notice = null) } }

    /** The app left the screen (MainActivity.onStop): a grant or an open pad ends, and Settings closes over it. */
    fun stopped() { if (state.value.adminUnlocked || state.value.prompt != null) relock() }

    /** Ends the grant and the pad, and closes Settings ([ParentalUi.lockEpoch]). */
    fun relock() {
        forgetFirst(); waitJob?.cancel()
        mutable.update { it.copy(adminUnlocked = false, prompt = null, notice = null, lockEpoch = it.lockEpoch + 1) }
    }

    /**
     * "Her şeyi sıfırla" removes the record once the sources are gone (pin.md §3.11); false when it could not be, and
     * the same call finishes it later.
     */
    suspend fun reset(): Boolean {
        val removed = guard.reset()
        revision = null
        refresh()
        return removed
    }

    private fun open(purpose: PinPurpose, step: PinStep, then: AdminFollowUp?): Int {
        forgetFirst(); waitJob?.cancel()
        val prompt = PinPrompt(++prompts, purpose, step, then = then)
        mutable.update { it.copy(prompt = prompt, notice = null) }
        return prompt.id
    }

    /** [prompt] in place of the one on screen, unless that one closed or another took its place. */
    private fun show(prompt: PinPrompt) {
        mutable.update { if (it.prompt?.id == prompt.id) it.copy(prompt = prompt) else it }
    }

    private fun isOpen(prompt: PinPrompt) = state.value.prompt?.id == prompt.id

    private suspend fun saved(prompt: PinPrompt, change: ParentalGuard.Change) {
        if (change is ParentalGuard.Change.Saved) {
            revision = change.snapshot.policyRevision
            val open = isOpen(prompt)
            mutable.update { it.copy(status = ParentalStatus.ON, adminUnlocked = it.adminUnlocked || open,
                prompt = if (open) null else it.prompt, notice = if (!open) it.notice else
                    UiText.Res(if (prompt.purpose == PinPurpose.Change) R.string.parental_changed else R.string.parental_created)) }
            if (open) prompt.then?.let(followUp)
            return
        }
        // A PIN set meanwhile, or the record lost: the status says which; the pad starts over.
        refresh()
        show(prompt.copy(step = PinStep.NEW, phase = PinPhase.Entry, feedback = UiText.Res(R.string.pin_not_saved)))
    }

    private suspend fun verified(prompt: PinPrompt, result: ParentalGuard.Verify) {
        when (result) {
            is ParentalGuard.Verify.Correct -> {
                revision = result.snapshot.policyRevision
                val open = isOpen(prompt)
                mutable.update { it.copy(status = ParentalStatus.ON, adminUnlocked = it.adminUnlocked || open,
                    prompt = if (open) null else it.prompt) }
                if (open) prompt.then?.let(followUp)
            }
            // The fifth wrong try in a row starts the wait; the fourth warns of it.
            is ParentalGuard.Verify.Wrong -> if (result.retryAfterMs > 0) wait(prompt.id, result.retryAfterMs)
                else show(prompt.copy(phase = PinPhase.Entry, feedback = UiText.Res(
                    if (result.charged == 4) R.string.pin_wrong_last else R.string.pin_wrong)))
            is ParentalGuard.Verify.Wait -> wait(prompt.id, result.retryAfterMs)
            is ParentalGuard.Verify.Failed -> {
                refresh()
                show(prompt.copy(phase = PinPhase.Entry, feedback = UiText.Res(R.string.pin_unavailable)))
                if (result.retryAfterMs > 0) wait(prompt.id, result.retryAfterMs)
            }
        }
    }

    /** The keys of pad [id] rest for [ms], then take digits again; nothing when that pad is no longer open. */
    private fun wait(id: Int, ms: Long) {
        val prompt = state.value.prompt?.takeIf { it.id == id } ?: return
        show(prompt.copy(phase = PinPhase.Waiting(elapsed() + ms, ms)))
        waitJob?.cancel()
        waitJob = scope.launch {
            delay(ms)
            state.value.prompt?.takeIf { it.id == id && it.phase is PinPhase.Waiting }
                ?.let { show(it.copy(phase = PinPhase.Entry, feedback = null)) }
        }
    }

    private fun forgetFirst() { firstPin?.fill('\u0000'); firstPin = null }
}
