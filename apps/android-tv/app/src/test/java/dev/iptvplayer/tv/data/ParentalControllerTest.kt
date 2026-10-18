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
    private val releases = mutableListOf<Pair<PinPurpose.Release, Long>>()
    private var policyChanges = 0

    private fun TestScope.controller(storage: Storage): ParentalController {
        val dispatcher = StandardTestDispatcher(testScheduler)
        return ParentalController(this, ParentalGuard(storage, clock, dispatcher, dispatcher), { testScheduler.currentTime },
            unlocked = { unlocks += it }, released = { purpose, revision -> releases += purpose to revision },
            policyChanged = { policyChanges++ }) { followUps += it }
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

        assertTrue(parental.createPin(then = AdminFollowUp.ShowAdult))
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
        assertEquals(listOf(AdminFollowUp.ShowAdult), followUps)
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
        parental.requestAdmin(AdminReason.SHOW_ADULT, AdminFollowUp.ShowAdult)
        runCurrent()
        parental.submit(pin("2580"))
        parental.cancel()
        advanceUntilIdle()
        assertNull(parental.state.value.prompt); assertFalse(parental.state.value.adminUnlocked)
        assertEquals(emptyList<AdminFollowUp>(), followUps)

        parental.requestAdmin(AdminReason.SHOW_ADULT, AdminFollowUp.ShowAdult)
        runCurrent()
        parental.submit(pin("2580"))
        advanceUntilIdle()
        assertNull(parental.state.value.prompt); assertTrue(parental.state.value.adminUnlocked)
        assertTrue(parental.state.value.mayLoosen)
        assertEquals(listOf(AdminFollowUp.ShowAdult), followUps)
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

    @Test fun locksAddedOnTheirOwnLockAtOnceAndKeepAPinVerifiedInAyarlar() = runTest {
        fun sports(id: String) = AccessRule(AccessScope.Category("a", MediaKind.LIVE, AccessCategory.ProviderId(id)), pinRequired = true)
        val storage = Storage(stored(rules = listOf(sports("10"))))
        val parental = controller(storage)
        parental.refresh()
        parental.requestAdmin(AdminReason.PARENTAL)
        runCurrent()
        parental.submit(pin("2580"))
        advanceUntilIdle()
        assertTrue(parental.state.value.adminUnlocked)
        // A publish finds a locked row's new identity: the gate reads its rule before the record holds it.
        parental.addLocks(listOf(sports("12")))
        assertEquals(listOf(sports("10")), parental.policy!!.rules)
        assertEquals(listOf(sports("10"), sports("12")), parental.gatePolicy!!.rules)
        advanceUntilIdle()
        assertEquals(listOf(sports("10"), sports("12")), storage.record!!.rules); assertEquals(4L, storage.record!!.policyRevision)
        assertSame(parental.policy, parental.gatePolicy)
        // That tightening leaves the PIN verified just before it good for a change.
        assertTrue(parental.changePin())
        parental.submit(pin("1397")); parental.submit(pin("1397"))
        advanceUntilIdle()
        assertEquals(UiText.Res(R.string.parental_changed), parental.state.value.notice)
        assertEquals(5L, storage.record!!.policyRevision)
        // One the record refuses still locks, and the next publish tries it again.
        storage.failWrites = true
        parental.addLocks(listOf(sports("13")))
        advanceUntilIdle()
        assertEquals(listOf(sports("10"), sports("12")), storage.record!!.rules)
        assertEquals(sports("13"), parental.gatePolicy!!.rules.last())
        // With the PIN gone no row is locked, so nothing is being added.
        storage.failWrites = false
        parental.removePin()
        advanceUntilIdle()
        assertEquals(emptyList<AccessRule>(), parental.gatePolicy!!.rules)
    }

    @Test fun locksAreAddedFreelyAndRemovedOnlyWithThePinVerifiedInAyarlar() = runTest {
        fun locked(scope: AccessScope) = AccessRule(scope, pinRequired = true)
        val film = AccessScope.Content(ContentRef("a", "movie:1"))
        val sports = AccessScope.Category("a", MediaKind.LIVE, AccessCategory.ProviderId("10"))
        val storage = Storage(stored())
        val parental = controller(storage)
        parental.refresh()
        val changes = policyChanges
        parental.grant(setOf(sports))
        // Kilitle: a tightening, saved without the PIN; the session keeps what it opened.
        assertEquals(LockEdit.SAVED, parental.editLocks(add = listOf(locked(film))))
        assertEquals(listOf(locked(film)), storage.record!!.rules); assertEquals(4L, storage.record!!.policyRevision)
        assertEquals(storage.record!!.rules, parental.policy!!.rules); assertEquals(changes + 1, policyChanges)
        assertEquals(setOf<AccessScope>(sports), parental.grants)
        // A lock it already has changes nothing.
        assertEquals(LockEdit.SAVED, parental.editLocks(add = listOf(locked(film))))
        assertEquals(4L, storage.record!!.policyRevision)
        // Removing loosens: refused without the PIN.
        assertEquals(LockEdit.NOT_SAVED, parental.editLocks(remove = setOf(film)))
        assertEquals(listOf(locked(film)), storage.record!!.rules)

        parental.requestAdmin(AdminReason.CATEGORIES)
        runCurrent()
        parental.submit(pin("2580"))
        advanceUntilIdle()
        assertTrue(parental.state.value.adminUnlocked)
        // A tightening in the same visit leaves the PIN good for the removal after it.
        assertEquals(LockEdit.SAVED, parental.editLocks(add = listOf(locked(sports))))
        assertEquals(LockEdit.SAVED, parental.editLocks(remove = setOf(film, sports)))
        assertTrue(storage.record!!.rules.isEmpty()); assertEquals(6L, storage.record!!.policyRevision)
        // A lock removed ends what PINs opened.
        assertTrue(parental.grants.isEmpty())
        // A hide on the same scope is not a lock: it stays.
        storage.record = ProtectionSnapshot(storage.record!!.generation + 1, 6, credential, storage.record!!.attempts,
            listOf(AccessRule(film, hidden = true, pinRequired = true)))
        parental.refresh()
        assertEquals(LockEdit.SAVED, parental.editLocks(remove = setOf(film)))
        assertEquals(listOf(AccessRule(film, hidden = true)), storage.record!!.rules)
        // The record holds 1024 rules at most; the parent is told to remove some (pin.md §5).
        val full = List(1024) { locked(AccessScope.Content(ContentRef("a", "movie:${it + 10}"))) }
        storage.record = ProtectionSnapshot(storage.record!!.generation + 1, 9, credential, storage.record!!.attempts, full)
        parental.refresh()
        assertEquals(LockEdit.LIMIT, parental.editLocks(add = listOf(locked(film))))
        assertEquals(full, storage.record!!.rules)
        // A write that fails saves nothing.
        storage.failWrites = true
        assertEquals(LockEdit.NOT_SAVED, parental.editLocks(remove = setOf(full.first().scope)))
        assertEquals(full, storage.record!!.rules)
    }

    @Test fun aLockAddedOverAChangeMadeElsewhereIsMadeOnTheRecordAsItIsThen() = runTest {
        val film = AccessRule(AccessScope.Content(ContentRef("a", "movie:1")), pinRequired = true)
        val other = AccessRule(AccessScope.Content(ContentRef("a", "movie:2")), pinRequired = true)
        val storage = Storage(stored())
        val parental = controller(storage)
        parental.refresh()
        // Another model saved a lock since this one read the record.
        storage.record = ProtectionSnapshot(storage.record!!.generation + 1, 5, credential, storage.record!!.attempts, listOf(other))
        assertEquals(LockEdit.SAVED, parental.editLocks(add = listOf(film)))
        assertEquals(listOf(other, film), storage.record!!.rules); assertEquals(6L, storage.record!!.policyRevision)
        assertEquals(storage.record!!.rules, parental.policy!!.rules)
    }

    @Test fun kilidiKaldirsPinRemovesThatLockAndGrantsNothingElse() = runTest {
        val film = AccessRule(AccessScope.Content(ContentRef("a", "movie:1")), pinRequired = true)
        val storage = Storage(stored(rules = listOf(film)))
        val parental = controller(storage)
        parental.refresh()
        val purpose = PinPurpose.Release("1:amovie:1", "Film")
        parental.release(purpose)
        runCurrent()
        assertEquals(purpose, parental.prompt.purpose); assertEquals(PinStep.VERIFY, parental.prompt.step)
        parental.submit(pin("2580"))
        advanceUntilIdle()
        assertNull(parental.state.value.prompt)
        // The model hears the revision the PIN saw, and removes the lock with it.
        assertEquals(listOf(purpose to 3L), releases)
        assertFalse(parental.state.value.adminUnlocked); assertTrue(unlocks.isEmpty()); assertTrue(followUps.isEmpty())
        // On a policy that changed since, nothing is removed.
        assertEquals(LockEdit.NOT_SAVED, parental.editLocks(remove = setOf(film.scope), verified = 2))
        assertEquals(listOf(film), storage.record!!.rules)
        assertEquals(LockEdit.SAVED, parental.editLocks(remove = setOf(film.scope), verified = 3))
        assertTrue(storage.record!!.rules.isEmpty())
    }

    @Test fun anUnreadableRecordAllowsNothingAndStartingOverRemovesIt() = runTest {
        val storage = Storage(stored()).apply { unavailable = true }
        val parental = controller(storage)
        parental.refresh()
        assertEquals(ParentalStatus.UNAVAILABLE, parental.state.value.status)
        // The lock screen comes up, without a pad: no PIN can be checked (fail closed, D049).
        assertTrue(parental.state.value.appLocked); assertNull(parental.state.value.prompt)
        assertEquals(1, parental.state.value.lockEpoch)
        assertFalse(parental.state.value.mayLoosen); assertFalse(parental.state.value.mayManageSources)
        assertFalse(parental.createPin())
        assertTrue(parental.requestAdmin(AdminReason.SHOW_ADULT))
        runCurrent()
        parental.submit(pin("2580"))
        runCurrent()
        assertEquals(UiText.Res(R.string.pin_unavailable), parental.prompt.feedback)
        assertEquals(PinPhase.Entry, parental.prompt.phase); assertTrue(parental.prompt.canForget)
        assertFalse(parental.state.value.adminUnlocked)

        // PIN'i unuttum, or Sıfırla… on the lock screen: the TV starts over, the pad closes and Settings with it, then
        // the record goes.
        parental.relock()
        assertNull(parental.state.value.prompt); assertEquals(2, parental.state.value.lockEpoch)
        storage.failReset = true
        assertEquals(StartOver.PIN_KEPT, parental.startOver { })
        assertEquals(ParentalStatus.UNAVAILABLE, parental.state.value.status); assertTrue(parental.state.value.appLocked)
        storage.failReset = false
        assertEquals(StartOver.DONE, parental.startOver { })
        assertEquals(ParentalStatus.OFF, parental.state.value.status)
        assertTrue(parental.state.value.mayLoosen); assertFalse(parental.state.value.appLocked)
    }

    @Test fun anUnreadableRecordKeepsTheAppLockedUntilItReadsAgain() = runTest {
        val storage = Storage(stored()).apply { unavailable = true }
        val parental = controller(storage)
        parental.refresh()
        assertTrue(parental.state.value.appLocked)
        // Yeniden dene while it still cannot be read: still locked.
        parental.refresh()
        assertTrue(parental.state.value.appLocked)
        // Read again without "Açılışta PIN iste": the app opens.
        storage.unavailable = false
        parental.refresh()
        assertFalse(parental.state.value.appLocked); assertNull(parental.state.value.prompt)
        // With it, the lock screen stays and its pad asks.
        storage.unavailable = true
        parental.refresh()
        assertTrue(parental.state.value.appLocked)
        storage.record = ProtectionSnapshot(5, 4, credential, storage.record!!.attempts, emptyList(), ProtectionOptions(launchNeedsPin = true))
        storage.unavailable = false
        parental.refresh()
        runCurrent()
        val state = parental.state.value
        assertTrue(state.appLocked); assertEquals(PinPurpose.Launch, state.prompt?.purpose); assertTrue(state.launchNeedsPin)
    }

    @Test fun theLaunchPinLocksTheAppAsItOpensAndOnReturnAndItsPinOpensOnlyTheApp() = runTest {
        val storage = Storage(ProtectionSnapshot(4, 3, credential, PinAttempts.clear(PinClock(1, 1_000)), emptyList(),
            ProtectionOptions(launchNeedsPin = true)))
        val parental = controller(storage)
        // Read again (a restore), the app stays as it is; opening it locks it.
        parental.refresh()
        assertFalse(parental.state.value.appLocked)
        parental.refresh(launching = true)
        runCurrent()
        var state = parental.state.value
        assertTrue(state.appLocked); assertEquals(PinPurpose.Launch, state.prompt?.purpose); assertTrue(state.prompt!!.canForget)
        // What closes a pad's window does not close the lock screen's.
        parental.close(); parental.cancel(); parental.back()
        assertEquals(PinPurpose.Launch, parental.state.value.prompt?.purpose)
        parental.submit(pin("1111"))
        advanceUntilIdle()
        assertEquals(UiText.Res(R.string.pin_wrong), parental.prompt.feedback); assertTrue(parental.state.value.appLocked)
        parental.submit(pin("2580"))
        advanceUntilIdle()
        // The right PIN opens the app and grants nothing else.
        state = parental.state.value
        assertFalse(state.appLocked); assertNull(state.prompt)
        assertFalse(state.adminUnlocked); assertFalse(state.sourcesUnlocked); assertFalse(state.mayLoosen)
        assertTrue(followUps.isEmpty()); assertTrue(unlocks.isEmpty())
        // The app leaving the screen locks it again, Settings closing with it.
        val epoch = state.lockEpoch
        parental.stopped()
        runCurrent()
        state = parental.state.value
        assertTrue(state.appLocked); assertEquals(PinPurpose.Launch, state.prompt?.purpose); assertEquals(epoch + 1, state.lockEpoch)
    }

    @Test fun sourceManagementAsksForThePinOnceForTheSourcesScreens() = runTest {
        val sourceLock = AccessRule(AccessScope.Source("a"), pinRequired = true)
        val storage = Storage(stored(rules = listOf(sourceLock)))
        val parental = controller(storage)
        parental.refresh()
        assertTrue(parental.state.value.sourcesGuarded); assertFalse(parental.state.value.mayManageSources)
        // Vazgeç grants nothing and runs nothing.
        assertTrue(parental.requestSources(AdminFollowUp.OpenSources))
        runCurrent()
        assertEquals(PinPurpose.Verify(AdminReason.SOURCES), parental.prompt.purpose)
        parental.cancel()
        assertNull(parental.state.value.prompt)
        assertTrue(parental.requestSources(AdminFollowUp.EditSource("a")))
        runCurrent()
        parental.submit(pin("2580"))
        advanceUntilIdle()
        // The right PIN opens the sources' screens and runs what asked; it is not a PIN verified in Ayarlar.
        var state = parental.state.value
        assertNull(state.prompt); assertTrue(state.sourcesUnlocked); assertTrue(state.mayManageSources)
        assertFalse(state.adminUnlocked); assertFalse(state.mayLoosen)
        assertEquals(listOf<AdminFollowUp>(AdminFollowUp.EditSource("a")), followUps)
        assertFalse(parental.requestSources(AdminFollowUp.OpenSources))
        // Within them, a source's lock goes with that PIN (Kaynağın kilidini kaldır).
        assertEquals(LockEdit.SAVED, parental.editLocks(remove = setOf(sourceLock.scope)))
        assertTrue(storage.record!!.rules.isEmpty())
        // Leaving them ends it.
        parental.leftSources()
        assertTrue(parental.requestSources(AdminFollowUp.OpenSources))
        parental.cancel()

        // A PIN verified in Ayarlar carries over, and the panel closing behind the sources keeps it.
        parental.requestAdmin(AdminReason.PARENTAL)
        runCurrent()
        parental.submit(pin("2580"))
        advanceUntilIdle()
        assertFalse(parental.requestSources(AdminFollowUp.OpenSources))
        parental.closeSettings()
        state = parental.state.value
        assertTrue(state.sourcesUnlocked); assertFalse(state.adminUnlocked)
        // The app leaving the screen ends it, and the sources ask again.
        val epoch = state.lockEpoch
        parental.stopped()
        assertFalse(parental.state.value.sourcesUnlocked); assertEquals(epoch + 1, parental.state.value.lockEpoch)
        assertTrue(parental.requestSources(AdminFollowUp.OpenSources))
    }

    @Test fun withSourceManagementOffASourcesLockStillGoesOnlyWithThePin() = runTest {
        val sourceLock = AccessRule(AccessScope.Source("a"), pinRequired = true)
        val storage = Storage(ProtectionSnapshot(4, 3, credential, PinAttempts.clear(PinClock(1, 1_000)), listOf(sourceLock),
            ProtectionOptions(sourcesNeedPin = false)))
        val parental = controller(storage)
        parental.refresh()
        assertFalse(parental.state.value.sourcesGuarded); assertFalse(parental.state.value.sourcesNeedPin)
        assertFalse(parental.requestSources(AdminFollowUp.OpenSources))
        assertNull(parental.state.value.prompt); assertFalse(parental.state.value.sourcesUnlocked)
        // Removing a source's lock loosens: without a PIN for it nothing is removed; the pad's PIN removes it.
        assertEquals(LockEdit.NOT_SAVED, parental.editLocks(remove = setOf(sourceLock.scope)))
        val purpose = PinPurpose.Release("a", "Kaynak", source = true)
        parental.release(purpose)
        runCurrent()
        parental.submit(pin("2580"))
        advanceUntilIdle()
        assertEquals(listOf(purpose to 3L), releases)
        assertFalse(parental.state.value.sourcesUnlocked); assertFalse(parental.state.value.adminUnlocked)
        assertEquals(LockEdit.SAVED, parental.editLocks(remove = setOf(sourceLock.scope), verified = 3))
        assertTrue(storage.record!!.rules.isEmpty())
    }

    @Test fun theOptionsTightenFreelyAndLoosenOnlyWithThePinVerifiedInAyarlar() = runTest {
        val storage = Storage(stored())
        val parental = controller(storage)
        parental.refresh()
        assertTrue(parental.state.value.sourcesNeedPin); assertFalse(parental.state.value.launchNeedsPin)
        // On: at once, without the PIN; the revision grows with it.
        assertEquals(LockEdit.SAVED, parental.editOptions { it.copy(launchNeedsPin = true) })
        assertEquals(ProtectionOptions(sourcesNeedPin = true, launchNeedsPin = true), storage.record!!.options)
        assertEquals(4L, storage.record!!.policyRevision); assertTrue(parental.state.value.launchNeedsPin)
        // Off: only with the PIN verified in this Settings visit.
        assertEquals(LockEdit.NOT_SAVED, parental.editOptions { it.copy(sourcesNeedPin = false) })
        assertTrue(storage.record!!.options.sourcesNeedPin)
        parental.requestAdmin(AdminReason.PARENTAL)
        runCurrent()
        parental.submit(pin("2580"))
        advanceUntilIdle()
        assertEquals(LockEdit.SAVED, parental.editOptions { it.copy(sourcesNeedPin = false) })
        assertEquals(LockEdit.SAVED, parental.editOptions { it.copy(launchNeedsPin = false) })
        assertEquals(ProtectionOptions(sourcesNeedPin = false, launchNeedsPin = false), storage.record!!.options)
        assertEquals(6L, storage.record!!.policyRevision)
        assertFalse(parental.state.value.sourcesGuarded); assertTrue(parental.state.value.mayManageSources)
        // The panel closed, on is still free.
        parental.closeSettings()
        assertEquals(LockEdit.SAVED, parental.editOptions { it.copy(sourcesNeedPin = true) })
        assertTrue(parental.state.value.sourcesGuarded)
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
        parental.createPin(then = AdminFollowUp.ShowAdult)
        parental.submit(pin("2580")); parental.submit(pin("2580"))
        assertTrue(parental.prompt.saving)
        // Vazgeç and Back wait: the PIN lands all the same, and the parent learns it did.
        parental.cancel(); parental.back()
        assertTrue(parental.prompt.saving)
        advanceUntilIdle()
        var state = parental.state.value
        assertNull(state.prompt); assertEquals(ParentalStatus.ON, state.status); assertTrue(state.adminUnlocked)
        assertEquals(UiText.Res(R.string.parental_created), state.notice)
        assertEquals(listOf(AdminFollowUp.ShowAdult), followUps)

        // A pad its opener took along closes at once; the PIN it saved grants nothing and runs nothing.
        followUps.clear()
        val other = controller(Storage(null))
        other.refresh()
        other.createPin(then = AdminFollowUp.ShowAdult)
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
