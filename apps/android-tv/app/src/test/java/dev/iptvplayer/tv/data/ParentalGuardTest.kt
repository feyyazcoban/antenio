@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
package dev.iptvplayer.tv.data

import dev.iptvplayer.tv.data.ParentalGuard.Verify
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

/** Synthetic PINs only. A correct check runs the real 600 000-round KDF, so those are kept few. */
class ParentalGuardTest {
    /** Keeps the record as ProtectionStore does: one generation at a time, and a policy change only with a new revision. */
    private class Storage(var record: ProtectionSnapshot?, private val events: MutableList<String>) : ProtectionStorage {
        var unavailable = false
        var failWrites = false
        var failReset = false
        override fun read(): ProtectionRead = if (unavailable) ProtectionRead.Unavailable
            else record?.let { ProtectionRead.Ready(it) } ?: ProtectionRead.Unconfigured
        override fun write(snapshot: ProtectionSnapshot, expectedGeneration: Long?) {
            val previous = record
            check(!unavailable && expectedGeneration == previous?.generation && snapshot.generation == (expectedGeneration ?: -1) + 1)
            if (previous != null) {
                val changed = snapshot.rules != previous.rules || snapshot.options != previous.options ||
                    !snapshot.credential.encode().contentEquals(previous.credential.encode())
                check(snapshot.policyRevision == previous.policyRevision + (if (changed) 1 else 0))
            }
            events += "save ${snapshot.attempts.charged}"
            check(!failWrites) { "synthetic write failure" }
            record = snapshot
        }
        override fun reset() {
            check(!failReset) { "synthetic reset failure" }
            record = null; unavailable = false
        }
    }

    /** The guard's CPU dispatcher: logs each KDF, and while [hold] is set parks it so a test can cancel first. */
    private class Cpu(private val events: MutableList<String>, private val inner: CoroutineDispatcher) : CoroutineDispatcher() {
        var hold = false
        var held: Runnable? = null
        override fun dispatch(context: CoroutineContext, block: Runnable) {
            events += "kdf"
            if (hold) held = block else inner.dispatch(context, block)
        }
    }

    private class Clock(var now: PinClock) : PinClockSource { override fun now() = now }

    private val events = mutableListOf<String>()
    private val clock = Clock(PinClock(1, 1_000))

    private fun TestScope.guard(storage: ProtectionStorage, cpu: Cpu = Cpu(events, StandardTestDispatcher(testScheduler))) =
        ParentalGuard(storage, clock, StandardTestDispatcher(testScheduler), cpu)

    private fun stored(attempts: PinAttempts = PinAttempts.clear(PinClock(1, 1_000))) = ProtectionSnapshot(4, 3, credential,
        attempts, listOf(AccessRule(AccessScope.Source("source-a"), pinRequired = true)))

    private fun correctPin() = charArrayOf('2', '5', '8', '0')
    private fun wrongPin() = charArrayOf('2', '5', '8', '1')
    /** Fails without the slow KDF; the guard charges it like any wrong PIN. */
    private fun malformedPin() = charArrayOf('1')
    private fun wiped(pin: CharArray) = pin.all { it == '\u0000' }

    @Test fun aWrongPinIsChargedAndSavedBeforeTheComparison() = runTest {
        val storage = Storage(stored(), events)
        val pin = wrongPin()
        val result = guard(storage).verify(pin)
        assertEquals(listOf("save 1", "kdf"), events)
        assertEquals(Verify.Wrong(storage.record!!, 1, 0), result)
        assertEquals(5L, storage.record!!.generation)
        assertTrue(wiped(pin))
    }

    @Test fun aChargeThatCannotBeSavedComparesNothing() = runTest {
        val storage = Storage(stored(), events).apply { failWrites = true }
        val pin = correctPin()
        assertEquals(Verify.Failed, guard(storage).verify(pin))
        assertEquals(listOf("save 1"), events)
        assertEquals(0, storage.record!!.attempts.charged)
        assertTrue(wiped(pin))
    }

    @Test fun aCorrectPinCountsOnlyOnceTheClearedBudgetIsSaved() = runTest {
        val storage = Storage(stored(PinAttempts(3, 1, 1_000, 0)), events)
        val clearFails = object : ProtectionStorage by storage {
            override fun write(snapshot: ProtectionSnapshot, expectedGeneration: Long?) {
                check(snapshot.attempts.charged != 0) { "synthetic write failure" }
                storage.write(snapshot, expectedGeneration)
            }
        }
        assertEquals(Verify.Failed, guard(clearFails).verify(correctPin()))
        assertEquals(listOf("save 4", "kdf"), events)
        assertEquals(4, storage.record!!.attempts.charged)

        events.clear()
        clock.now = PinClock(1, 9_000)
        val result = guard(storage).verify(correctPin())
        assertEquals(listOf("save 5", "kdf", "save 0"), events)
        assertEquals(Verify.Correct(storage.record!!), result)
        assertEquals(PinAttempts.clear(PinClock(1, 9_000)), storage.record!!.attempts)
        assertEquals(3L, storage.record!!.policyRevision)
    }

    @Test fun theFifthWrongPinStartsTheWaitAndWaitingChargesNothing() = runTest {
        val storage = Storage(stored(), events)
        val guard = guard(storage)
        repeat(4) { tries ->
            val wrong = guard.verify(malformedPin()) as Verify.Wrong
            assertEquals(tries + 1, wrong.charged); assertEquals(0L, wrong.retryAfterMs)
        }
        val fifth = guard.verify(malformedPin()) as Verify.Wrong
        assertEquals(5, fifth.charged); assertEquals(30_000L, fifth.retryAfterMs)

        events.clear()
        clock.now = PinClock(1, 11_000)
        val pin = correctPin()
        assertEquals(Verify.Wait(20_000), guard.verify(pin))
        assertEquals(20_000L, guard.waitMs())
        assertEquals(emptyList<String>(), events)
        assertEquals(5, storage.record!!.attempts.charged)
        assertTrue(wiped(pin))
    }

    @Test fun aRebootsFreshWaitIsSavedOnceSoItCountsDown() = runTest {
        val storage = Storage(stored(PinAttempts(5, 1, 1_000, 31_000)), events)
        val guard = guard(storage)
        clock.now = PinClock(2, 100_000)
        assertEquals(Verify.Wait(30_000), guard.verify(correctPin()))
        assertEquals(listOf("save 5"), events)
        assertEquals(130_000L, storage.record!!.attempts.availableAtMs)
        clock.now = PinClock(2, 120_000)
        assertEquals(10_000L, guard.waitMs())
        assertEquals(listOf("save 5"), events)

        // Opening the PIN pad after the next reboot saves its wait too.
        clock.now = PinClock(3, 5_000)
        assertEquals(30_000L, guard.waitMs())
        clock.now = PinClock(3, 25_000)
        assertEquals(Verify.Wait(10_000), guard.verify(correctPin()))
        assertEquals(listOf("save 5", "save 5"), events)
    }

    @Test fun simultaneousChecksRunOneAfterTheOther() = runTest {
        val storage = Storage(stored(), events)
        val guard = guard(storage)
        val first = async { guard.verify(malformedPin()) }
        val second = async { guard.verify(malformedPin()) }
        assertEquals(1, (first.await() as Verify.Wrong).charged)
        assertEquals(2, (second.await() as Verify.Wrong).charged)
        assertEquals(listOf("save 1", "kdf", "save 2", "kdf"), events)
        assertEquals(6L, storage.record!!.generation)
    }

    @Test fun cancellingDuringTheCheckKeepsTheTryChargedAndWipesThePin() = runTest {
        val storage = Storage(stored(), events)
        val cpu = Cpu(events, StandardTestDispatcher(testScheduler)).apply { hold = true }
        val guard = guard(storage, cpu)
        val pin = correctPin()
        val check = launch { guard.verify(pin) }
        runCurrent()
        assertEquals(listOf("save 1", "kdf"), events)
        // Vazgeç while "Kontrol ediliyor…": the try is not given back (D048).
        check.cancel()
        cpu.held!!.run()
        check.join()
        assertTrue(check.isCancelled)
        assertTrue(wiped(pin))
        assertEquals(1, storage.record!!.attempts.charged)
        cpu.hold = false
        assertEquals(2, (guard.verify(malformedPin()) as Verify.Wrong).charged)
    }

    @Test fun createSetsTheFirstPinOnly() = runTest {
        val storage = Storage(null, events)
        val guard = guard(storage)
        val options = ProtectionOptions(sourcesNeedPin = false, launchNeedsPin = true)
        val pin = correctPin()
        val created = guard.create(pin, options)!!
        assertTrue(wiped(pin))
        assertSame(created, storage.record)
        assertEquals(0L, created.generation); assertEquals(0L, created.policyRevision)
        assertEquals(PinAttempts.clear(clock.now), created.attempts)
        assertEquals(emptyList<AccessRule>(), created.rules); assertEquals(options, created.options)

        events.clear()
        val second = wrongPin()
        assertNull(guard.create(second))
        assertTrue(wiped(second))
        assertEquals(emptyList<String>(), events)
        assertSame(created, storage.record)
        assertTrue(guard.verify(correctPin()) is Verify.Correct)

        storage.record = null; storage.unavailable = true
        assertNull(guard.create(correctPin()))
    }

    @Test fun pinAndPolicyChangesRaiseTheRevisionAndAStaleRevisionChangesNothing() = runTest {
        val storage = Storage(stored(), events)
        val guard = guard(storage)
        val stale = wrongPin()
        assertNull(guard.replacePin(stale, expectedRevision = 2))
        assertTrue(wiped(stale))
        assertEquals(emptyList<String>(), events)

        val replaced = guard.replacePin(wrongPin(), expectedRevision = 3)!!
        assertEquals(4L, replaced.policyRevision)
        assertFalse(replaced.credential.encode().contentEquals(credential.encode()))

        val rule = AccessRule(AccessScope.Content(ContentRef("source-a", "movie:1")), pinRequired = true)
        val locked = guard.edit(4) { it.copy(rules = it.rules + rule) }!!
        assertEquals(5L, locked.policyRevision); assertEquals(rule, locked.rules.last())
        val onLaunch = guard.edit(5) { it.copy(options = it.options.copy(launchNeedsPin = true)) }!!
        assertEquals(6L, onLaunch.policyRevision); assertTrue(onLaunch.options.launchNeedsPin)

        // Nothing changed, nothing saved.
        assertSame(onLaunch, guard.edit(6) { it })
        // Verified before the last change, or past the record's limits (one rule per scope).
        assertNull(guard.edit(5) { it.copy(rules = emptyList()) })
        assertNull(guard.edit(6) { it.copy(rules = it.rules + rule) })
        assertSame(onLaunch, storage.record)
    }

    @Test fun resetEmptiesTheRecordAndCanBeRepeated() = runTest {
        val storage = Storage(stored(), events).apply { failReset = true }
        val guard = guard(storage)
        assertFalse(guard.reset())
        assertTrue(guard.read() is ProtectionRead.Ready)
        storage.failReset = false
        assertTrue(guard.reset())
        assertEquals(ProtectionRead.Unconfigured, guard.read())
        assertTrue(guard.reset())

        storage.record = stored(); storage.unavailable = true
        assertTrue(guard.reset())
        assertEquals(ProtectionRead.Unconfigured, guard.read())
    }

    @Test fun anUnreadableOrMissingRecordAllowsNothing() = runTest {
        val storage = Storage(stored(), events).apply { unavailable = true }
        val guard = guard(storage)
        val pin = correctPin()
        assertEquals(Verify.Failed, guard.verify(pin))
        assertTrue(wiped(pin))
        assertNull(guard.waitMs())
        assertNull(guard.edit(3) { it.copy(rules = emptyList()) })
        val replacement = wrongPin()
        assertNull(guard.replacePin(replacement, 3))
        assertTrue(wiped(replacement))
        assertEquals(emptyList<String>(), events)

        val empty = guard(Storage(null, events))
        assertEquals(Verify.Failed, empty.verify(correctPin()))
        assertNull(empty.waitMs())
    }

    private companion object {
        /** One KDF for the whole class: every test starts from a record that holds this PIN. */
        val credential: PinCredential by lazy { PinCredential.create(charArrayOf('2', '5', '8', '0')) }
    }
}
