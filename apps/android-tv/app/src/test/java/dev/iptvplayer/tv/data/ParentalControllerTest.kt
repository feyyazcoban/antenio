@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
package dev.iptvplayer.tv.data

import dev.iptvplayer.tv.R
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

/**
 * The PIN pad's steps and the grant in Ayarlar, on a record kept in memory. Synthetic PINs only; each check or new PIN
 * runs the real 600 000-round KDF, so those are kept few, and earlier wrong tries are put in the record instead.
 */
class ParentalControllerTest {
    private class Storage(var record: ProtectionSnapshot?) : ProtectionStorage {
        var unavailable = false
        var failWrites = false
        var failReset = false
        override fun read(): ProtectionRead = if (unavailable) ProtectionRead.Unavailable
            else record?.let { ProtectionRead.Ready(it) } ?: ProtectionRead.Unconfigured
        override fun write(snapshot: ProtectionSnapshot, expectedGeneration: Long?) {
            check(!unavailable && !failWrites && expectedGeneration == record?.generation) { "synthetic write failure" }
            record = snapshot
        }
        override fun reset() {
            check(!failReset) { "synthetic reset failure" }
            record = null; unavailable = false
        }
    }

    private class Clock(var now: PinClock) : PinClockSource { override fun now() = now }

    private val clock = Clock(PinClock(1, 1_000))
    private val followUps = mutableListOf<AdminFollowUp>()
    private val unlocks = mutableListOf<PinPurpose.Unlock>()
    private var policyChanges = 0

    private fun TestScope.controller(storage: Storage): ParentalController {
        val dispatcher = StandardTestDispatcher(testScheduler)
        return ParentalController(this, ParentalGuard(storage, clock, dispatcher, dispatcher), { testScheduler.currentTime },
            unlocked = { unlocks += it }, policyChanged = { policyChanges++ }) { followUps += it }
    }

    private fun stored(attempts: PinAttempts = PinAttempts.clear(PinClock(1, 1_000)), rules: List<AccessRule> = emptyList()) =
        ProtectionSnapshot(4, 3, credential, attempts, rules)

    private fun pin(digits: String) = digits.toCharArray()
    private fun wiped(pin: CharArray) = pin.all { it == '\u0000' }
    private val ParentalController.prompt get() = state.value.prompt!!

    @Test fun aFirstPinIsAskedTwiceAndAnEasyOrDifferentOneIsRefused() = runTest {
        val storage = Storage(null)
        val parental = controller(storage)
        assertTrue(parental.state.value.guarded)
        parental.refresh()
        assertEquals(ParentalStatus.OFF, parental.state.value.status)
        assertTrue(parental.state.value.mayLoosen)
        assertFalse(parental.requestAdmin(AdminReason.SHOW_ADULT))
        assertFalse(parental.changePin())

        assertTrue(parental.createPin(then = AdminFollowUp.SHOW_ADULT))
        assertEquals(PinPurpose.Create, parental.prompt.purpose); assertEquals(PinStep.NEW, parental.prompt.step)
        assertFalse(parental.prompt.canForget)
        val easy = pin("1234")
        parental.submit(easy)
        assertTrue(wiped(easy))
        assertEquals(PinStep.NEW, parental.prompt.step); assertEquals(UiText.Res(R.string.pin_easy), parental.prompt.feedback)
        parental.submit(pin("2580"))
        assertEquals(PinStep.CONFIRM, parental.prompt.step); assertNull(parental.prompt.feedback)
        // Back on the repeat returns to the first step; Back there closes.
        parental.back()
        assertEquals(PinStep.NEW, parental.prompt.step)
        parental.submit(pin("2580"))
        parental.submit(pin("2581"))
        assertEquals(PinStep.NEW, parental.prompt.step); assertEquals(UiText.Res(R.string.pin_mismatch), parental.prompt.feedback)

        parental.submit(pin("2580"))
        val repeat = pin("2580")
        parental.submit(repeat)
        assertTrue(wiped(repeat))
        assertEquals(PinPhase.Checking, parental.prompt.phase)
        // Nothing reaches the pad while the PIN is saved.
        val extra = pin("1397")
        parental.submit(extra)
        assertTrue(wiped(extra))
        advanceUntilIdle()
        val state = parental.state.value
        assertEquals(ParentalStatus.ON, state.status); assertTrue(state.adminUnlocked); assertNull(state.prompt)
        assertEquals(UiText.Res(R.string.parental_created), state.notice)
        assertEquals(listOf(AdminFollowUp.SHOW_ADULT), followUps)
        assertEquals(0L, storage.record!!.policyRevision)
        assertFalse(parental.createPin())
    }

    @Test fun aFirstPinThatIsNotSavedStartsOver() = runTest {
        val storage = Storage(null).apply { failWrites = true }
        val parental = controller(storage)
        parental.refresh()
        parental.createPin()
        parental.submit(pin("2580")); parental.submit(pin("2580"))
        advanceUntilIdle()
        assertEquals(ParentalStatus.OFF, parental.state.value.status); assertFalse(parental.state.value.adminUnlocked)
        assertEquals(PinStep.NEW, parental.prompt.step); assertEquals(PinPhase.Entry, parental.prompt.phase)
        assertEquals(UiText.Res(R.string.pin_not_saved), parental.prompt.feedback)
        parental.back()
        assertNull(parental.state.value.prompt)
    }

    @Test fun theFourthWrongTryWarnsAndTheFifthWaitsEvenAcrossAClosedPad() = runTest {
        val storage = Storage(stored(PinAttempts(3, 1, 1_000, 0)))
        val parental = controller(storage)
        parental.refresh()
        assertFalse(parental.state.value.mayLoosen)
        assertTrue(parental.requestAdmin(AdminReason.PARENTAL))
        runCurrent()
        assertEquals(PinStep.VERIFY, parental.prompt.step); assertTrue(parental.prompt.canForget)
        assertEquals(PinPhase.Entry, parental.prompt.phase)
        parental.submit(pin("1111"))
        runCurrent()
        assertEquals(UiText.Res(R.string.pin_wrong_last), parental.prompt.feedback)
        parental.submit(pin("0000"))
        runCurrent()
        assertEquals(PinPhase.Waiting(currentTime + 30_000, 30_000), parental.prompt.phase)
        assertEquals(5, storage.record!!.attempts.charged)
        // The keys take nothing while it lasts, and closing the pad does not end it.
        val ignored = pin("2580")
        parental.submit(ignored)
        assertTrue(wiped(ignored))
        parental.cancel()
        assertNull(parental.state.value.prompt); assertFalse(parental.state.value.adminUnlocked)
        assertTrue(parental.requestAdmin(AdminReason.PARENTAL))
        runCurrent()
        assertTrue(parental.prompt.phase is PinPhase.Waiting)
        clock.now = PinClock(1, 31_000)
        advanceTimeBy(30_001)
        assertEquals(PinPhase.Entry, parental.prompt.phase); assertNull(parental.prompt.feedback)
        assertEquals(5, storage.record!!.attempts.charged)
        assertEquals(emptyList<AdminFollowUp>(), followUps)
    }

    @Test fun vazgecGrantsNothingAndACorrectPinGrantsAyarlarUntilThePanelCloses() = runTest {
        val storage = Storage(stored())
        val parental = controller(storage)
        parental.refresh()
        assertEquals(ParentalStatus.ON, parental.state.value.status)
        assertFalse(parental.createPin())
        // Vazgeç while the PIN is checked: the right PIN grants nothing.
        parental.requestAdmin(AdminReason.SHOW_ADULT, AdminFollowUp.SHOW_ADULT)
        runCurrent()
        parental.submit(pin("2580"))
        parental.cancel()
        advanceUntilIdle()
        assertNull(parental.state.value.prompt); assertFalse(parental.state.value.adminUnlocked)
        assertEquals(emptyList<AdminFollowUp>(), followUps)

        parental.requestAdmin(AdminReason.SHOW_ADULT, AdminFollowUp.SHOW_ADULT)
        runCurrent()
        parental.submit(pin("2580"))
        advanceUntilIdle()
        assertNull(parental.state.value.prompt); assertTrue(parental.state.value.adminUnlocked)
        assertTrue(parental.state.value.mayLoosen)
        assertEquals(listOf(AdminFollowUp.SHOW_ADULT), followUps)
        // Verified in this visit: nothing more is asked until the panel closes.
        assertFalse(parental.requestAdmin(AdminReason.CATEGORIES))
        parental.closeSettings()
        assertFalse(parental.state.value.adminUnlocked)

        // Leaving the screen closes an open pad and Settings with it; with nothing open, Settings stays.
        val epoch = parental.state.value.lockEpoch
        parental.stopped()
        assertEquals(epoch, parental.state.value.lockEpoch)
        parental.requestAdmin(AdminReason.CATEGORIES)
        parental.stopped()
        assertNull(parental.state.value.prompt)
        assertEquals(epoch + 1, parental.state.value.lockEpoch)
    }

    @Test fun changingAndRemovingThePinNeedItVerified() = runTest {
        val storage = Storage(stored())
        val parental = controller(storage)
        parental.refresh()
        assertFalse(parental.changePin())
        parental.removePin()
        advanceUntilIdle()
        assertNotNull(storage.record)

        parental.requestAdmin(AdminReason.PARENTAL)
        runCurrent()
        parental.submit(pin("2580"))
        advanceUntilIdle()
        // What the old PIN opened locks again with the new one, and with none.
        parental.grant(setOf(AccessScope.Source("a")))
        assertTrue(parental.changePin())
        assertEquals(PinPurpose.Change, parental.prompt.purpose); assertEquals(PinStep.NEW, parental.prompt.step)
        parental.submit(pin("1397")); parental.submit(pin("1397"))
        advanceUntilIdle()
        assertNull(parental.state.value.prompt); assertTrue(parental.state.value.adminUnlocked)
        assertEquals(UiText.Res(R.string.parental_changed), parental.state.value.notice)
        assertEquals(4L, storage.record!!.policyRevision)
        assertTrue(parental.grants.isEmpty()); assertEquals(0, parental.state.value.sessionGrants)
        parental.grant(setOf(AccessScope.Source("a")))

        parental.removePin()
        assertNull(parental.state.value.notice)
        advanceUntilIdle()
        assertNull(storage.record)
        assertEquals(ParentalStatus.OFF, parental.state.value.status); assertFalse(parental.state.value.adminUnlocked)
        assertEquals(UiText.Res(R.string.parental_removed), parental.state.value.notice)
        assertTrue(parental.grants.isEmpty())
        assertEquals(emptyList<AccessRule>(), parental.policy!!.rules)
    }

    @Test fun aLockedTitlesPinOpensItsLocksForTheSessionAndNotAyarlar() = runTest {
        val sources = AccessRule(AccessScope.Source("a"), pinRequired = true)
        val storage = Storage(stored(rules = listOf(sources)))
        val parental = controller(storage)
        assertNull(parental.policy)
        parental.refresh()
        assertEquals(listOf(sources), parental.policy!!.rules); assertEquals(3L, parental.policy!!.revision)
        assertEquals(1, policyChanges)
        val purpose = PinPurpose.Unlock(AccessTarget.Open("1:amovie:1"), "1:amovie:1", "Film")
        // Vazgeç while the PIN is checked: the right PIN opens nothing.
        parental.unlock(purpose)
        runCurrent()
        assertEquals(purpose, parental.prompt.purpose); assertEquals(PinStep.VERIFY, parental.prompt.step)
        assertTrue(parental.prompt.canForget)
        parental.submit(pin("2580"))
        parental.cancel()
        advanceUntilIdle()
        assertTrue(unlocks.isEmpty())

        parental.unlock(purpose)
        runCurrent()
        parental.submit(pin("2580"))
        advanceUntilIdle()
        assertNull(parental.state.value.prompt)
        assertEquals(listOf(purpose), unlocks)
        // A title's PIN is not a PIN verified in Ayarlar.
        assertFalse(parental.state.value.adminUnlocked); assertTrue(followUps.isEmpty())
        assertFalse(parental.state.value.mayLoosen)
        // The model opens what locks the title.
        parental.grant(setOf(sources.scope))
        assertEquals(setOf(sources.scope), parental.grants); assertEquals(1, parental.state.value.sessionGrants)

        // A lock added (a locked row's new identity) keeps it.
        val other = AccessRule(AccessScope.Source("b"), pinRequired = true)
        parental.addLocks(listOf(other, sources))
        advanceUntilIdle()
        assertEquals(listOf(sources, other), storage.record!!.rules); assertEquals(4L, storage.record!!.policyRevision)
        assertEquals(storage.record!!.rules, parental.policy!!.rules); assertEquals(2, policyChanges)
        assertEquals(setOf(sources.scope), parental.grants)

        // Leaving the screen locks it again; with nothing open, Settings stays.
        val epoch = parental.state.value.lockEpoch
        parental.stopped()
        assertTrue(parental.grants.isEmpty()); assertEquals(0, parental.state.value.sessionGrants)
        assertEquals(epoch, parental.state.value.lockEpoch)
        // Açılan kilitleri kapat.
        parental.grant(setOf(sources.scope))
        parental.relockContent()
        assertTrue(parental.grants.isEmpty())
        // A lock removed elsewhere ends the session as it is read.
        parental.grant(setOf(sources.scope))
        storage.record = ProtectionSnapshot(9, 5, credential, storage.record!!.attempts, listOf(other))
        parental.refresh()
        assertTrue(parental.grants.isEmpty()); assertEquals(3, policyChanges)
        // An unreadable record: no rules known, every title asks.
        storage.unavailable = true
        parental.refresh()
        assertNull(parental.policy); assertEquals(4, policyChanges)
        parental.addLocks(listOf(sources))
        advanceUntilIdle()
        assertEquals(listOf(other), storage.record!!.rules)
    }

    @Test fun anUnreadableRecordAllowsNothingAndStartingOverRemovesIt() = runTest {
        val storage = Storage(stored()).apply { unavailable = true }
        val parental = controller(storage)
        parental.refresh()
        assertEquals(ParentalStatus.UNAVAILABLE, parental.state.value.status)
        assertFalse(parental.state.value.mayLoosen)
        assertFalse(parental.createPin())
        assertTrue(parental.requestAdmin(AdminReason.SHOW_ADULT))
        runCurrent()
        parental.submit(pin("2580"))
        runCurrent()
        assertEquals(UiText.Res(R.string.pin_unavailable), parental.prompt.feedback)
        assertEquals(PinPhase.Entry, parental.prompt.phase); assertTrue(parental.prompt.canForget)
        assertFalse(parental.state.value.adminUnlocked)

        // PIN'i unuttum: the TV starts over, the pad closes and Settings with it, then the record goes.
        parental.relock()
        assertNull(parental.state.value.prompt); assertEquals(1, parental.state.value.lockEpoch)
        storage.failReset = true
        assertEquals(StartOver.PIN_KEPT, parental.startOver { })
        assertEquals(ParentalStatus.UNAVAILABLE, parental.state.value.status)
        storage.failReset = false
        assertEquals(StartOver.DONE, parental.startOver { })
        assertEquals(ParentalStatus.OFF, parental.state.value.status)
        assertTrue(parental.state.value.mayLoosen)
    }

    @Test fun startingOverRemovesTheRecordOnlyOnceTheSourcesAreGone() = runTest {
        val storage = Storage(stored())
        val parental = controller(storage)
        parental.refresh()
        // Sources that could not all be removed keep their PIN.
        assertEquals(StartOver.FAILED, parental.startOver { error("synthetic source failure") })
        assertNotNull(storage.record); assertEquals(ParentalStatus.ON, parental.state.value.status)
        // The record goes after the sources, never before; one that stays is told.
        var cleared = false
        storage.failReset = true
        assertEquals(StartOver.PIN_KEPT, parental.startOver { assertNotNull(storage.record); cleared = true })
        assertTrue(cleared); assertNotNull(storage.record)
        storage.failReset = false
        assertEquals(StartOver.DONE, parental.startOver { assertNotNull(storage.record) })
        assertNull(storage.record); assertEquals(ParentalStatus.OFF, parental.state.value.status)

        assertEquals(R.string.notice_start_over, StartOver.DONE.notice); assertFalse(StartOver.DONE.needsAttention)
        assertEquals(R.string.notice_start_over_pin_kept, StartOver.PIN_KEPT.notice); assertTrue(StartOver.PIN_KEPT.needsAttention)
        assertEquals(R.string.notice_start_over_failed, StartOver.FAILED.notice); assertTrue(StartOver.FAILED.needsAttention)
    }

    @Test fun aNewPinBeingSavedIsNotCalledBack() = runTest {
        val storage = Storage(null)
        val parental = controller(storage)
        parental.refresh()
        parental.createPin(then = AdminFollowUp.SHOW_ADULT)
        parental.submit(pin("2580")); parental.submit(pin("2580"))
        assertTrue(parental.prompt.saving)
        // Vazgeç and Back wait: the PIN lands all the same, and the parent learns it did.
        parental.cancel(); parental.back()
        assertTrue(parental.prompt.saving)
        advanceUntilIdle()
        var state = parental.state.value
        assertNull(state.prompt); assertEquals(ParentalStatus.ON, state.status); assertTrue(state.adminUnlocked)
        assertEquals(UiText.Res(R.string.parental_created), state.notice)
        assertEquals(listOf(AdminFollowUp.SHOW_ADULT), followUps)

        // A pad its opener took along closes at once; the PIN it saved grants nothing and runs nothing.
        followUps.clear()
        val other = controller(Storage(null))
        other.refresh()
        other.createPin(then = AdminFollowUp.SHOW_ADULT)
        other.submit(pin("2580")); other.submit(pin("2580"))
        other.close()
        assertNull(other.state.value.prompt)
        advanceUntilIdle()
        state = other.state.value
        assertEquals(ParentalStatus.ON, state.status); assertFalse(state.adminUnlocked); assertNull(state.notice)
        assertEquals(emptyList<AdminFollowUp>(), followUps)
    }

    private companion object {
        /** One KDF for the whole class: every stored record holds this PIN. */
        val credential: PinCredential by lazy { PinCredential.create(charArrayOf('2', '5', '8', '0')) }
    }
}
