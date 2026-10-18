package dev.iptvplayer.tv.data

import androidx.annotation.StringRes
import dev.iptvplayer.tv.R
import kotlinx.coroutines.CancellationException
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
sealed interface AdminFollowUp {
    /** The adult filter goes off. */
    data object ShowAdult : AdminFollowUp
    /** The category row [name] of [kind] locks: Kilitle in Kategorileri yönet on a TV that had no PIN yet (pin.md §3.4). */
    data class LockCategory(val kind: MediaKind, val name: String) : AdminFollowUp
}

/** How a lock change from the screens ended (pin.md §5, §9): saved, refused at the record's 1024 rules, or not saved. */
enum class LockEdit { SAVED, LIMIT, NOT_SAVED }

/**
 * What the pad is for: the first PIN, a new one for the PIN set, the PIN checked before [Verify.reason], or the PIN that
 * opens a locked title ([Unlock]).
 */
sealed interface PinPurpose {
    data object Create : PinPurpose
    data object Change : PinPurpose
    data class Verify(val reason: AdminReason) : PinPurpose
    /**
     * A locked title (pin.md §3.6): [target] is decided again once the PIN is right, and runs if it still names [id];
     * [title] is the name the pad shows. It grants nothing in Ayarlar.
     */
    data class Unlock(val target: AccessTarget, val id: String, val title: String) : PinPurpose
    /**
     * Kilidi kaldır in a title's quick menu (pin.md §3.5): once the PIN is right the lock of its own on [id] goes;
     * [title] is the name the pad shows. It grants nothing else, neither the title nor Ayarlar.
     */
    data class Release(val id: String, val title: String) : PinPurpose
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
    /** A new PIN is being saved. It cannot be called back, so Vazgeç and Back wait until the pad closes with it. */
    val saving: Boolean get() = step == PinStep.CONFIRM && phase == PinPhase.Checking
}

/** How "PIN'i unuttum → Her şeyi sıfırla" ended, and what the screen it ends on says ([notice]). */
enum class StartOver(@StringRes val notice: Int, val needsAttention: Boolean) {
    /** The sources and the PIN record are gone. */
    DONE(R.string.notice_start_over, false),
    /** The sources are gone but the PIN record stayed; the same reset finishes it later. */
    PIN_KEPT(R.string.notice_start_over_pin_kept, true),
    /** Not every source could be removed: the PIN stays, and the library opens with what is left. */
    FAILED(R.string.notice_start_over_failed, true),
}

/**
 * Parental control as the screens see it. [adminUnlocked]: a PIN verified or made since Settings opened, which a
 * loosening needs while [guarded]. [prompt] is the PIN pad on screen. [notice] says how the last change on Ebeveyn
 * denetimi went. [lockEpoch] grows when Settings must close: the app left the screen, or the TV starts over.
 * [sessionGrants]: how many lock scopes PINs typed for locked titles opened in this session (ParentalSession).
 */
data class ParentalUi(val status: ParentalStatus = ParentalStatus.LOADING, val adminUnlocked: Boolean = false,
    val prompt: PinPrompt? = null, val notice: UiText? = null, val lockEpoch: Int = 0, val sessionGrants: Int = 0) {
    /** Loosening asks for the PIN: one is set, or the record is not read yet or cannot be (fail closed, D049). */
    val guarded: Boolean get() = status != ParentalStatus.OFF
    val mayLoosen: Boolean get() = !guarded || adminUnlocked
}

/**
 * The PIN pad's steps, the grant in Ayarlar and the content locks (pin.md §3.1–3.3, §3.6, §3.10–3.11), between the
 * screens and ParentalGuard, which does the slow and durable work. Main thread. A new PIN is asked twice and kept in
 * memory only between the two steps. A verified PIN grants Ayarlar until the panel closes or the app leaves the screen;
 * a PIN typed for a locked title hands its purpose to [unlocked], and the model opens that title's locks for the
 * session ([grant]); one typed to remove a title's lock hands it to [released] with the policy revision it saw, for
 * [editLocks]. A result that arrives after the pad closed only updates the status: Vazgeç never grants anything, and a
 * try it cut short stays charged (D048). Vazgeç does not cut a new PIN's save short, as the PIN would change all the
 * same without the parent knowing. [policyChanged] runs when the lock rules read from the record change.
 */
internal class ParentalController(private val scope: CoroutineScope, private val guard: ParentalGuard,
    private val elapsed: () -> Long, private val unlocked: (PinPurpose.Unlock) -> Unit = {},
    private val released: (PinPurpose.Release, Long) -> Unit = { _, _ -> }, private val policyChanged: () -> Unit = {},
    private val followUp: (AdminFollowUp) -> Unit) {
    private val mutable = MutableStateFlow(ParentalUi())
    val state = mutable.asStateFlow()
    private var prompts = 0
    /** The first step's PIN while the second step asks for it again. */
    private var firstPin: CharArray? = null
    /** The policy revision the last verified or new PIN saw; a PIN change made with an older one is refused. */
    private var revision: Long? = null
    private var waitJob: Job? = null
    private val session = ParentalSession()

    /**
     * The lock rules as last read from the record, empty without a PIN; null until it is read, and while it cannot be,
     * when every title asks for the PIN (D049).
     */
    var policy: AccessPolicy? = null
        private set

    /** The rules last given to [addLocks]: a locked category row's new identities, saved or not yet. */
    private var pendingLocks: List<AccessRule> = emptyList()

    /**
     * The rules the gate decides with: [policy] and the rules [addLocks] is adding, so a locked row's new identity asks
     * for the PIN before the record holds its rule, and still does while the record refuses it (fail closed).
     */
    val gatePolicy: AccessPolicy? get() {
        val saved = policy ?: return null
        if (pendingLocks.isEmpty()) return saved
        val ruled = saved.rules.mapTo(HashSet()) { it.scope }
        val adding = pendingLocks.filter { it.scope !in ruled }
        return if (adding.isEmpty()) saved else AccessPolicy(saved.rules + adding, saved.revision)
    }

    /** The lock scopes PINs opened in this session. */
    val grants: Set<AccessScope> get() = session.grants

    /** Reads whether this TV has a PIN and its locks: as the app opens, and after the record may have changed. */
    suspend fun refresh() {
        val read = guard.read()
        val status = when (read) {
            ProtectionRead.Unconfigured -> ParentalStatus.OFF
            is ProtectionRead.Ready -> ParentalStatus.ON
            ProtectionRead.Unavailable -> ParentalStatus.UNAVAILABLE
        }
        mutable.update { it.copy(status = status, adminUnlocked = it.adminUnlocked && status == ParentalStatus.ON) }
        learn(when (read) {
            ProtectionRead.Unconfigured -> AccessPolicy(emptyList())
            is ProtectionRead.Ready -> read.snapshot.policy()
            ProtectionRead.Unavailable -> null
        })
    }

    /** True when the PIN pad opened before [reason], [then] to follow; false when nothing needs asking. */
    fun requestAdmin(reason: AdminReason, then: AdminFollowUp? = null): Boolean {
        if (state.value.mayLoosen) return false
        ask(PinPurpose.Verify(reason), then)
        return true
    }

    /** Opens the pad for a locked title; [purpose] goes back to the model once the PIN is right. */
    fun unlock(purpose: PinPurpose.Unlock) { ask(purpose, null) }

    /** Opens the pad to remove a title's lock; [purpose] goes back to the model once the PIN is right. */
    fun release(purpose: PinPurpose.Release) { ask(purpose, null) }

    /** Opens [scopes] for the session: the locks on the title a PIN was just typed for. */
    fun grant(scopes: Set<AccessScope>) {
        session.grant(scopes)
        mutable.update { it.copy(sessionGrants = session.grants.size) }
    }

    /**
     * Locks again what PINs opened: "Açılan kilitleri kapat", or a protection loosened in Ayarlar (a hidden category
     * shown, the adult filter off).
     */
    fun relockContent() {
        session.clear()
        mutable.update { it.copy(sessionGrants = 0) }
    }

    /**
     * Adds [rules] the library needs (a locked category row's new identity, pin.md §5), in place of those the last
     * publish needed. A tightening, so it asks for no PIN, and a PIN verified in Ayarlar just before still changes the
     * PIN and the locks. The gate reads them as locks at once ([gatePolicy]); what the record refuses (the 1024 limit, a
     * failed write) is tried again at the next publish.
     */
    fun addLocks(rules: List<AccessRule>) {
        pendingLocks = rules
        val read = policy?.revision ?: return
        if (rules.isEmpty()) return
        scope.launch {
            val change = guard.edit(read) { current ->
                current.copy(rules = current.rules + rules.filter { rule -> current.rules.none { it.scope == rule.scope } })
            }
            when (change) {
                is ParentalGuard.Change.Saved -> {
                    if (revision == read) revision = change.snapshot.policyRevision
                    learn(change.snapshot.policy())
                }
                ParentalGuard.Change.Stale -> refresh()
                else -> Unit
            }
        }
    }

    /**
     * Adds the lock rules [add] and removes those on [remove] (Kilitle and Kilidi kaldır, Kategorileri yönet, Kilitli
     * içerikler; pin.md §3.4–3.5). Adding tightens and asks for nothing. Removing loosens: it needs the PIN verified in
     * this Settings visit, or [verified], the revision a PIN typed for this removal saw; a hide on the same scope stays.
     * A tightening that another change overtook (a publish adding a row's identity) is made again on the record as it
     * is then; a removal only over this controller's own changes, which keep the verified revision current.
     */
    suspend fun editLocks(add: List<AccessRule> = emptyList(), remove: Set<AccessScope> = emptySet(), verified: Long? = null): LockEdit {
        fun expected(): Long? = if (remove.isEmpty()) policy?.revision else verified ?: revision?.takeIf { state.value.adminUnlocked }
        fun edited(rules: List<AccessRule>): List<AccessRule> {
            val kept = rules.mapNotNull { rule -> if (rule.scope !in remove) rule else rule.takeIf { it.hidden }?.copy(pinRequired = false) }
            return kept + add.filter { rule -> kept.none { it.scope == rule.scope } }.distinctBy { it.scope }
        }
        var used = expected() ?: return LockEdit.NOT_SAVED
        var change = guard.edit(used) { it.copy(rules = edited(it.rules)) }
        if (change == ParentalGuard.Change.Stale) {
            if (remove.isEmpty()) refresh()
            val again = expected()
            if (again != null && again != used) { used = again; change = guard.edit(used) { it.copy(rules = edited(it.rules)) } }
        }
        return when (change) {
            is ParentalGuard.Change.Saved -> {
                if (revision == used) revision = change.snapshot.policyRevision
                learn(change.snapshot.policy())
                LockEdit.SAVED
            }
            ParentalGuard.Change.LimitReached -> LockEdit.LIMIT
            ParentalGuard.Change.Stale -> { refresh(); LockEdit.NOT_SAVED }
            else -> LockEdit.NOT_SAVED
        }
    }

    /** Says [notice] on the Settings page open, as the last change made there; the panel closing forgets it. */
    fun tell(notice: UiText) { mutable.update { it.copy(notice = notice) } }

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

    /**
     * Vazgeç: the pad closes and grants nothing; a check under way finishes for the record only. A new PIN being saved
     * would land all the same, so the pad stays until it has, and then says so.
     */
    fun cancel() { if (state.value.prompt?.saving != true) close() }

    /** The pad's opener left the screen: it closes even while a new PIN is saved, which then lands without a grant. */
    fun close() {
        forgetFirst(); waitJob?.cancel()
        mutable.update { it.copy(prompt = null) }
    }

    /** "PIN'i kaldır", with the PIN verified in this visit; hidden categories and the adult filter stay as they are. */
    fun removePin() {
        if (state.value.status != ParentalStatus.ON || !state.value.adminUnlocked) return
        relockContent()
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

    /**
     * The app left the screen (MainActivity.onStop): what PINs opened locks again, and a grant or an open pad ends,
     * Settings closing over it.
     */
    fun stopped() {
        relockContent()
        if (state.value.adminUnlocked || state.value.prompt != null) relock()
    }

    /** Ends the grant, the pad and what PINs opened, and closes Settings ([ParentalUi.lockEpoch]). */
    fun relock() {
        forgetFirst(); waitJob?.cancel(); session.clear()
        mutable.update { it.copy(adminUnlocked = false, prompt = null, notice = null, lockEpoch = it.lockEpoch + 1, sessionGrants = 0) }
    }

    /**
     * "PIN'i unuttum → Her şeyi sıfırla" (pin.md §3.11), once [relock] closed the pad: [clear] removes the sources and
     * what hangs on them, and only once it has, the PIN record goes. A reset cut short never leaves the sources without
     * their PIN; run again, it finishes what is left.
     */
    suspend fun startOver(clear: suspend () -> Unit): StartOver {
        try { clear() } catch (e: CancellationException) { throw e } catch (_: Exception) { return StartOver.FAILED }
        val removed = guard.reset()
        revision = null
        refresh()
        return if (removed) StartOver.DONE else StartOver.PIN_KEPT
    }

    /** Opens the pad to check the PIN for [purpose]. */
    private fun ask(purpose: PinPurpose, then: AdminFollowUp?) {
        val id = open(purpose, PinStep.VERIFY, then)
        // A wait that earlier tries started is still running: the pad opens waiting. A PIN already sent meets it itself.
        scope.launch {
            val ms = guard.waitMs() ?: 0L
            if (ms > 0 && state.value.prompt?.let { it.id == id && it.phase == PinPhase.Entry } == true) wait(id, ms)
        }
    }

    /**
     * The lock rules are now [next]: the session keeps what PINs opened only while no restriction went, and the model
     * hears of a change.
     */
    private fun learn(next: AccessPolicy?) {
        val old = policy
        policy = next
        if ((old == null) == (next == null) && old?.rules == next?.rules) return
        // With no category lock left (the PIN removed, say), no row gains a rule.
        if (next != null && next.rules.none { it.pinRequired && !it.hidden && it.scope is AccessScope.Category }) pendingLocks = emptyList()
        if (next == null) session.clear() else session.policyChanged(old?.rules.orEmpty(), next.rules)
        mutable.update { it.copy(sessionGrants = session.grants.size) }
        policyChanged()
    }

    private fun ProtectionSnapshot.policy() = AccessPolicy(rules, policyRevision)

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
            // A new PIN: what the old one opened locks again.
            if (prompt.purpose == PinPurpose.Change) relockContent()
            learn(change.snapshot.policy())
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
                learn(result.snapshot.policy())
                val open = isOpen(prompt)
                val purpose = prompt.purpose
                if (purpose is PinPurpose.Unlock || purpose is PinPurpose.Release) {
                    // A title's PIN opens that title's locks, or removes its own, never Ayarlar.
                    mutable.update { it.copy(status = ParentalStatus.ON, prompt = if (open) null else it.prompt) }
                    if (open && purpose is PinPurpose.Unlock) unlocked(purpose)
                    if (open && purpose is PinPurpose.Release) released(purpose, result.snapshot.policyRevision)
                    return
                }
                revision = result.snapshot.policyRevision
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
