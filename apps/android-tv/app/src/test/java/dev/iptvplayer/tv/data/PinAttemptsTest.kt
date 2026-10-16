package dev.iptvplayer.tv.data

import org.junit.Assert.*
import org.junit.Test

class PinAttemptsTest {
    private fun chargedFive(): PinAttempts {
        var state = PinAttempts.clear(PinClock(1, 1_000))
        repeat(5) {
            val attempt = state.reserve(PinClock(1, 1_000))
            assertTrue(attempt.mayVerify)
            state = attempt.next
        }
        return state
    }

    @Test fun fifthReservedAttemptLocksTheNextAttempt() {
        val state = chargedFive()
        val blocked = state.reserve(PinClock(1, 1_000))
        assertFalse(blocked.mayVerify)
        assertEquals(30_000L, blocked.retryAfterMs)
        assertEquals(5, blocked.next.charged)
        assertFalse(state.reserve(PinClock(1, 30_999)).mayVerify)
        assertTrue(state.reserve(PinClock(1, 31_000)).mayVerify)
    }

    @Test fun processRestartUsesThePersistedDeadlineWithoutExtendingIt() {
        val state = chargedFive()
        val restored = PinAttempts(state.charged, state.boot, state.lastObservedMs, state.availableAtMs)
        val first = restored.reserve(PinClock(1, 11_000))
        val second = first.next.reserve(PinClock(1, 21_000))
        assertEquals(20_000L, first.retryAfterMs)
        assertEquals(10_000L, second.retryAfterMs)
        assertEquals(state.availableAtMs, second.next.availableAtMs)
    }

    @Test fun rebootRestartsTheFullDelayEvenWhenNewUptimeIsLarger() {
        val result = chargedFive().reserve(PinClock(2, 100_000))
        assertFalse(result.mayVerify)
        assertEquals(30_000L, result.retryAfterMs)
        assertTrue(result.next.reserve(PinClock(2, 130_000)).mayVerify)
    }

    @Test fun monotonicRollbackRebasesInsteadOfOpeningTheGate() {
        val result = chargedFive().reserve(PinClock(1, 500))
        assertFalse(result.mayVerify)
        assertEquals(30_000L, result.retryAfterMs)
    }

    @Test fun cancelledOrCrashedAttemptRemainsCharged() {
        val reserved = chargedFive().reserve(PinClock(1, 31_000))
        assertTrue(reserved.mayVerify)
        assertEquals(6, reserved.next.charged)
        val afterCrash = reserved.next.reserve(PinClock(1, 32_000))
        assertFalse(afterCrash.mayVerify)
        assertEquals(59_000L, afterCrash.retryAfterMs)
    }

    @Test fun delayCapsWithoutCounterOverflow() {
        var state = chargedFive()
        for (expected in listOf(60_000L, 120_000L, 240_000L, 300_000L, 300_000L)) {
            val now = state.availableAtMs
            val result = state.reserve(PinClock(1, now))
            assertTrue(result.mayVerify)
            state = result.next
            assertEquals(expected, state.availableAtMs - now)
            assertTrue(state.charged <= 9)
        }
    }

    @Test fun authenticatedResetAndEarlyRebootDoNotInventLockouts() {
        val clean = PinAttempts.clear(PinClock(2, 100))
        assertTrue(clean.reserve(PinClock(2, 100)).mayVerify)
        val early = clean.reserve(PinClock(3, 0))
        assertTrue(early.mayVerify)
        assertEquals(1, early.next.charged)
    }

    @Test fun invalidStateAndOverflowingClockAreRejected() {
        assertThrows(IllegalArgumentException::class.java) { PinClock(-1, 0) }
        assertThrows(IllegalArgumentException::class.java) { PinClock(1, Long.MAX_VALUE) }
        assertThrows(IllegalArgumentException::class.java) { PinAttempts(10, 1, 0, 0) }
        assertThrows(IllegalArgumentException::class.java) { PinAttempts(0, 1, 0, 1) }
        assertThrows(IllegalArgumentException::class.java) { PinAttempts(5, 1, 0, 30_001) }
        val nearLimit = PinClock(1, Long.MAX_VALUE - 300_000)
        val locked = PinAttempts(9, 1, nearLimit.elapsedMs, nearLimit.elapsedMs)
            .reserve(nearLimit)
        assertEquals(Long.MAX_VALUE, locked.next.availableAtMs)
        assertTrue(locked.mayVerify)
    }
}
