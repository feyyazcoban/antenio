package dev.iptvplayer.tv.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** The part of the protection record a parent edits: the lock rules and the options. */
internal data class ProtectionPolicy(val rules: List<AccessRule>, val options: ProtectionOptions)

/**
 * The one way to check, set or change the parental PIN (pin.md §6). Calls run one at a time. A try is charged and
 * saved before the slow comparison, and a correct PIN counts only once the cleared budget is saved, so neither a
 * crash, a cancel nor a failed write buys a free try (D048). The PIN arrives as a CharArray and is wiped whatever
 * happens; it never becomes a String.
 *
 * Not an authorization by itself: the caller decides which change needs a verified PIN and passes the policy revision
 * that verification saw, so a policy changed since refuses the change.
 */
internal class ParentalGuard(private val storage: ProtectionStorage, private val clock: PinClockSource,
    private val io: CoroutineDispatcher = Dispatchers.IO, private val cpu: CoroutineDispatcher = Dispatchers.Default) {
    private val mutex = Mutex()

    sealed interface Verify {
        /** The record after the budget was cleared. */
        data class Correct(val snapshot: ProtectionSnapshot) : Verify
        /** [charged] tries in a row so far; [retryAfterMs] stays 0 until the fifth. */
        data class Wrong(val snapshot: ProtectionSnapshot, val charged: Int, val retryAfterMs: Long) : Verify
        /** Too many wrong tries: nothing was charged or compared. */
        data class Wait(val retryAfterMs: Long) : Verify
        /** No PIN, or the record could not be read or saved: nothing was allowed, and a failed charge compared nothing. */
        data object Failed : Verify
    }

    suspend fun read(): ProtectionRead = mutex.withLock { load() }

    /** How long the PIN pad waits before it takes a try, 0 for not at all; null when there is no PIN to check. */
    suspend fun waitMs(): Long? = mutex.withLock {
        val snapshot = (load() as? ProtectionRead.Ready)?.snapshot ?: return@withLock null
        val now = clock.now()
        keepRebase(snapshot, now)
        snapshot.attempts.waitMs(now)
    }

    /**
     * The first PIN, on a device without one. Null when a PIN is set, the record is unreadable or it was not saved.
     * A PIN that is not 4–8 ASCII digits throws, as in PinCredential.create.
     */
    suspend fun create(pin: CharArray, options: ProtectionOptions = ProtectionOptions()): ProtectionSnapshot? = wiping(pin) {
        mutex.withLock {
            if (load() != ProtectionRead.Unconfigured) return@withLock null
            val credential = withContext(cpu) { PinCredential.create(pin) }
            save(ProtectionSnapshot(0, 0, credential, PinAttempts.clear(clock.now()), emptyList(), options), null)
        }
    }

    suspend fun verify(pin: CharArray): Verify = wiping(pin) {
        mutex.withLock {
            val snapshot = (load() as? ProtectionRead.Ready)?.snapshot ?: return@withLock Verify.Failed
            val now = clock.now()
            val reservation = snapshot.attempts.reserve(now)
            if (!reservation.mayVerify) {
                keepRebase(snapshot, now)
                return@withLock Verify.Wait(reservation.retryAfterMs)
            }
            // No saved charge, no comparison.
            val reserved = save(snapshot.next(attempts = reservation.next), snapshot.generation)
                ?: return@withLock Verify.Failed
            if (!withContext(cpu) { reserved.credential.matches(pin) }) {
                return@withLock Verify.Wrong(reserved, reserved.attempts.charged, reserved.attempts.waitMs(clock.now()))
            }
            save(reserved.next(attempts = PinAttempts.clear(clock.now())), reserved.generation)
                ?.let { Verify.Correct(it) } ?: Verify.Failed
        }
    }

    /** A new PIN for the one verified at [expectedRevision]; null when the policy changed since or it was not saved. */
    suspend fun replacePin(pin: CharArray, expectedRevision: Long): ProtectionSnapshot? = wiping(pin) {
        mutex.withLock {
            val current = current(expectedRevision) ?: return@withLock null
            val credential = withContext(cpu) { PinCredential.create(pin) }
            save(current.next(credential = credential, revision = current.policyRevision + 1), current.generation)
        }
    }

    /**
     * The rules and options [change] makes of the policy verified at [expectedRevision]. A change raises the revision;
     * no change saves nothing and returns the record as it is. Null when the policy changed since, the result breaks
     * the record's limits (1024 rules, one per scope) or it was not saved.
     */
    suspend fun edit(expectedRevision: Long, change: (ProtectionPolicy) -> ProtectionPolicy): ProtectionSnapshot? =
        mutex.withLock {
            val current = current(expectedRevision) ?: return@withLock null
            val policy = change(ProtectionPolicy(current.rules, current.options))
            if (policy.rules == current.rules && policy.options == current.options) return@withLock current
            val next = runCatching {
                current.next(rules = policy.rules, options = policy.options, revision = current.policyRevision + 1)
            }.getOrNull() ?: return@withLock null
            save(next, current.generation)
        }

    /** Back to no PIN. False when the record could not be removed; the same call finishes it later. */
    suspend fun reset(): Boolean = mutex.withLock {
        withContext(io) { try { storage.reset(); true } catch (_: Exception) { false } }
    }

    private suspend fun load(): ProtectionRead = withContext(io) { storage.read() }

    private suspend fun current(expectedRevision: Long): ProtectionSnapshot? =
        (load() as? ProtectionRead.Ready)?.snapshot?.takeIf { it.policyRevision == expectedRevision }

    /** [next] once it is on disk; null when the write failed. */
    private suspend fun save(next: ProtectionSnapshot, expectedGeneration: Long?): ProtectionSnapshot? =
        withContext(io) { try { storage.write(next, expectedGeneration); next } catch (_: Exception) { null } }

    /**
     * Saves the fresh wait a reboot or a clock gone back starts, or every look would start it over. A failed save
     * only means a longer wait, so its result is not needed.
     */
    private suspend fun keepRebase(snapshot: ProtectionSnapshot, now: PinClock) {
        val rebased = snapshot.attempts.rebase(now)
        if (rebased.boot != snapshot.attempts.boot || rebased.availableAtMs != snapshot.attempts.availableAtMs) {
            save(snapshot.next(attempts = rebased), snapshot.generation)
        }
    }

    private fun ProtectionSnapshot.next(attempts: PinAttempts = this.attempts, credential: PinCredential = this.credential,
        rules: List<AccessRule> = this.rules, options: ProtectionOptions = this.options, revision: Long = policyRevision) =
        ProtectionSnapshot(generation + 1, revision, credential, attempts, rules, options)

    private inline fun <T> wiping(pin: CharArray, block: () -> T): T = try { block() } finally { pin.fill('\u0000') }
}
