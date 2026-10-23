package dev.iptvplayer.tv.data

/** What Google Play last said of the yearly subscription on this TV (D121). */
enum class PlayStatus {
    /** Not asked yet, or Play could not be reached: the last answer stands. */
    UNKNOWN,
    /** No subscription, or one that ended or was refunded. */
    NONE,
    /** Paid for, or in Play's grace period after a failed payment. */
    ACTIVE,
    /** Bought, but the payment has not gone through yet. */
    PENDING,
    /** On account hold or paused: a payment failed for longer than the grace period. */
    ON_HOLD
}

/**
 * Whether this TV may play (D121): during the free week, which starts with the first playback, or with the yearly
 * Google Play subscription. Every other screen is open without either.
 *
 * [firstPlayAt] is when the week began, null before the first playback. [lastSeenAt] is the latest time the app saw,
 * so a clock turned back does not lengthen the week. [play] is the subscription as Play last said, at [checkedAt].
 * Kept on this TV only: without an account a reinstall starts the week again, a risk D121 accepts until accounts come.
 */
data class Membership(
    val firstPlayAt: Long? = null,
    val lastSeenAt: Long = 0,
    val play: PlayStatus = PlayStatus.UNKNOWN,
    val checkedAt: Long = 0
) {
    /** [clock], or the latest time seen when the clock was turned back. */
    fun now(clock: Long): Long = maxOf(clock, lastSeenAt)

    /** Still in the free week; true before the first playback, which starts it. */
    fun inFreeWeek(clock: Long): Boolean = firstPlayAt == null || now(clock) < firstPlayAt + FREE_WEEK

    /** The subscription is active as Play last said, within [TRUSTED] of that answer when Play cannot be reached now. */
    fun subscribed(clock: Long): Boolean = play == PlayStatus.ACTIVE && now(clock) - checkedAt < TRUSTED

    fun mayWatch(clock: Long): Boolean = subscribed(clock) || inFreeWeek(clock)

    /** Days of the free week left, today counted (7 on its first day); null outside it or before it starts. */
    fun freeDaysLeft(clock: Long): Int? {
        val start = firstPlayAt ?: return null
        val left = start + FREE_WEEK - now(clock)
        return if (left > 0) ((left + DAY - 1) / DAY).toInt() else null
    }

    /** The first playback starts the free week; later ones change nothing. */
    fun started(clock: Long): Membership = if (firstPlayAt == null) copy(firstPlayAt = now(clock)) else this

    fun seen(clock: Long): Membership = if (clock > lastSeenAt) copy(lastSeenAt = clock) else this

    /** Play's answer [status] at [clock]; UNKNOWN keeps the last one. */
    fun answered(status: PlayStatus, clock: Long): Membership =
        if (status == PlayStatus.UNKNOWN) this else copy(play = status, checkedAt = now(clock))

    /** As DeviceStore keeps it: `firstPlayAt;lastSeenAt;play;checkedAt`, the first empty before the first playback. */
    fun encode(): String = listOf(firstPlayAt?.toString().orEmpty(), lastSeenAt, play.name, checkedAt).joinToString(";")

    companion object {
        const val DAY = 24 * 60 * 60 * 1000L
        const val FREE_WEEK = 7 * DAY
        /** How long an active subscription holds while Play cannot be reached; Play saying it ended closes it at once. */
        const val TRUSTED = 3 * DAY

        /** What [encode] wrote; a value it cannot read counts as a TV that has not played yet. */
        fun decode(value: String?): Membership {
            val parts = value?.split(';') ?: return Membership()
            if (parts.size != 4) return Membership()
            return Membership(parts[0].toLongOrNull(), parts[1].toLongOrNull() ?: 0,
                PlayStatus.entries.firstOrNull { it.name == parts[2] } ?: PlayStatus.UNKNOWN, parts[3].toLongOrNull() ?: 0)
        }
    }
}

/** Where the membership stands, for Ayarlar → Üyelik and its row. */
enum class MemberStage { NOT_STARTED, FREE, ENDED, ACTIVE, CANCELLED, PENDING, ON_HOLD }

/** [membership] at [clock], [renews] as Play says: a subscription first, then a payment Play waits on, then the week. */
fun memberStage(membership: Membership, renews: Boolean, clock: Long): MemberStage = with(membership) {
    when {
        subscribed(clock) -> if (renews) MemberStage.ACTIVE else MemberStage.CANCELLED
        play == PlayStatus.PENDING -> MemberStage.PENDING
        play == PlayStatus.ON_HOLD -> MemberStage.ON_HOLD
        firstPlayAt == null -> MemberStage.NOT_STARTED
        inFreeWeek(clock) -> MemberStage.FREE
        else -> MemberStage.ENDED
    }
}

/** One purchase as Play reports it, reduced to what the status needs. */
data class PlayPurchase(val product: String, val purchased: Boolean, val pending: Boolean, val suspended: Boolean, val autoRenewing: Boolean)

/** What Play's purchases say of [product]: the strongest answer among them; none is [PlayStatus.NONE]. */
fun playStatus(purchases: List<PlayPurchase>, product: String): PlayStatus {
    val ours = purchases.filter { it.product == product }
    return when {
        ours.any { it.purchased && !it.suspended } -> PlayStatus.ACTIVE
        ours.any { it.purchased && it.suspended } -> PlayStatus.ON_HOLD
        ours.any { it.pending } -> PlayStatus.PENDING
        else -> PlayStatus.NONE
    }
}
