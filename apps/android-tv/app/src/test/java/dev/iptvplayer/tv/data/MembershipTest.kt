package dev.iptvplayer.tv.data

import dev.iptvplayer.tv.data.Membership.Companion.DAY
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MembershipTest {
    private val start = 1_800_000_000_000L

    @Test fun theFreeWeekStartsWithTheFirstPlaybackAndLastsSevenDays() {
        val fresh = Membership()
        assertTrue(fresh.mayWatch(start))
        assertNull(fresh.freeDaysLeft(start))
        val week = fresh.started(start)
        assertEquals(start, week.firstPlayAt)
        assertEquals(week, week.started(start + DAY))
        assertEquals(7, week.freeDaysLeft(start))
        assertEquals(1, week.freeDaysLeft(start + 7 * DAY - 1))
        assertTrue(week.mayWatch(start + 7 * DAY - 1))
        assertFalse(week.mayWatch(start + 7 * DAY))
        assertNull(week.freeDaysLeft(start + 7 * DAY))
    }

    @Test fun turningTheClockBackDoesNotLengthenTheWeek() {
        val seen = Membership().started(start).seen(start + 8 * DAY)
        assertFalse(seen.mayWatch(start + DAY))
        assertEquals(seen, seen.seen(start))
    }

    @Test fun anActiveSubscriptionHoldsThreeDaysWithoutPlayAndEndsWhenPlaySaysSo() {
        val paid = Membership().started(start).answered(PlayStatus.ACTIVE, start + 10 * DAY)
        assertTrue(paid.mayWatch(start + 12 * DAY))
        assertFalse(paid.mayWatch(start + 13 * DAY))
        assertEquals(paid, paid.answered(PlayStatus.UNKNOWN, start + 12 * DAY))
        assertFalse(paid.answered(PlayStatus.NONE, start + 11 * DAY).mayWatch(start + 11 * DAY))
        assertFalse(paid.answered(PlayStatus.ON_HOLD, start + 11 * DAY).mayWatch(start + 11 * DAY))
    }

    @Test fun itIsKeptAsTextAndAnUnreadableValueIsANewTv() {
        val kept = Membership(start, start + DAY, PlayStatus.PENDING, start + 2)
        assertEquals(kept, Membership.decode(kept.encode()))
        assertEquals(Membership(), Membership.decode(Membership().encode()))
        assertEquals(Membership(), Membership.decode("broken"))
        assertEquals(Membership(), Membership.decode(null))
    }

    @Test fun theStageReadsTheSubscriptionFirstThenThePaymentThenTheWeek() {
        val week = Membership().started(start)
        assertEquals(MemberStage.NOT_STARTED, memberStage(Membership(), renews = true, start))
        assertEquals(MemberStage.FREE, memberStage(week, renews = true, start))
        assertEquals(MemberStage.ENDED, memberStage(week, renews = true, start + 8 * DAY))
        val paid = week.answered(PlayStatus.ACTIVE, start + 8 * DAY)
        assertEquals(MemberStage.ACTIVE, memberStage(paid, renews = true, start + 8 * DAY))
        assertEquals(MemberStage.CANCELLED, memberStage(paid, renews = false, start + 8 * DAY))
        assertEquals(MemberStage.PENDING, memberStage(week.answered(PlayStatus.PENDING, start), renews = true, start))
        assertEquals(MemberStage.ON_HOLD, memberStage(week.answered(PlayStatus.ON_HOLD, start), renews = true, start))
    }

    @Test fun playsPurchasesMakeOneStatusForOurProduct() {
        fun purchase(product: String = "antenio_premium", purchased: Boolean = true, pending: Boolean = false, suspended: Boolean = false) =
            PlayPurchase(product, purchased, pending, suspended, autoRenewing = true)
        assertEquals(PlayStatus.NONE, playStatus(emptyList(), "antenio_premium"))
        assertEquals(PlayStatus.NONE, playStatus(listOf(purchase(product = "other")), "antenio_premium"))
        assertEquals(PlayStatus.ACTIVE, playStatus(listOf(purchase()), "antenio_premium"))
        assertEquals(PlayStatus.ON_HOLD, playStatus(listOf(purchase(suspended = true)), "antenio_premium"))
        assertEquals(PlayStatus.PENDING, playStatus(listOf(purchase(purchased = false, pending = true)), "antenio_premium"))
        assertEquals(PlayStatus.ACTIVE, playStatus(listOf(purchase(purchased = false, pending = true), purchase()), "antenio_premium"))
    }
}
