package dev.iptvplayer.tv.data

/** Trusted boot identity + monotonic time, never the user-adjustable wall clock. */
internal data class PinClock(val boot: Long, val elapsedMs: Long) {
    init { require(boot >= 0 && elapsedMs >= 0 && elapsedMs <= Long.MAX_VALUE - 300_000) }
}

/** Pure persisted budget. The repository must serialize callers and commit each reservation first. */
internal data class PinAttempts(
    val charged: Int,
    val boot: Long,
    val lastObservedMs: Long,
    val availableAtMs: Long,
) {
    init {
        require(charged in 0..9 && boot >= 0 && lastObservedMs >= 0 && availableAtMs >= 0)
        require(lastObservedMs <= Long.MAX_VALUE - MAX_DELAY)
        require(availableAtMs <= lastObservedMs + delay(charged))
        require(charged >= 5 || availableAtMs == 0L)
    }

    data class Reservation(val next: PinAttempts, val mayVerify: Boolean, val retryAfterMs: Long)

    /**
     * Charge before the expensive PIN comparison. A crash/cancel consumes the attempt.
     * mayVerify is usable ONLY after next is durably committed; a write failure grants nothing.
     */
    fun reserve(now: PinClock): Reservation {
        val rebased = rebase(now)
        val remaining = (rebased.availableAtMs - now.elapsedMs).coerceAtLeast(0)
        if (remaining > 0) return Reservation(rebased, false, remaining)
        val nextCount = (charged + 1).coerceAtMost(9)
        val next = rebased.copy(charged = nextCount,
            availableAtMs = if (nextCount >= 5) now.elapsedMs + delay(nextCount) else 0)
        return Reservation(next, true, 0)
    }

    /** What [reserve] would make the caller wait at [now], charging nothing: the PIN pad can open already waiting. */
    fun waitMs(now: PinClock): Long = (rebase(now).availableAtMs - now.elapsedMs).coerceAtLeast(0)

    /**
     * This budget as seen at [now]. After a reboot or a monotonic clock that went back, the full delay starts over
     * from [now]; the caller saves that once, or every later look would start it over again.
     */
    fun rebase(now: PinClock): PinAttempts =
        if (now.boot != boot || now.elapsedMs < lastObservedMs) copy(boot = now.boot, lastObservedMs = now.elapsedMs,
            availableAtMs = if (charged >= 5) now.elapsedMs + delay(charged) else 0)
        else copy(lastObservedMs = now.elapsedMs)

    companion object {
        private const val MAX_DELAY = 300_000L
        /** Initial setup or successfully authenticated reset only; never a parse-error fallback. */
        fun clear(now: PinClock) = PinAttempts(0, now.boot, now.elapsedMs, 0)
        private fun delay(count: Int): Long = when (count) {
            in 0..4 -> 0
            5 -> 30_000
            6 -> 60_000
            7 -> 120_000
            8 -> 240_000
            else -> MAX_DELAY
        }
    }
}
