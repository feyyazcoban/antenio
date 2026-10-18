package dev.iptvplayer.tv.ui

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ScrubRulesTest {
    @Test fun boundsAndPendingOriginAreIndependentOfPlaybackAdvancing() {
        assertEquals(0L, ScrubRules.nudge(2_000, null, -10_000, 120_000))
        assertEquals(120_000L, ScrubRules.nudge(115_000, null, 10_000, 120_000))
        assertEquals(40_000L, ScrubRules.nudge(15_000, 30_000, 10_000, 120_000))
        assertEquals(120_000L, ScrubRules.nudge(15_000, null, Long.MAX_VALUE, 120_000))
        assertEquals(0L, ScrubRules.nudge(15_000, null, Long.MIN_VALUE, 120_000))
    }
    @Test fun severalNudgesProduceOnlyOneSeekAfterTheLastDelay() = runTest {
        val clock = PlaybackClock().apply { update(10_000, 120_000, 120_000, true, 0) }
        val seeks = mutableListOf<Long>()
        val holder = ScrubHolder(this, clock, { true }, { seeks += it })
        holder.nudge(10_000); advanceTimeBy(800)
        clock.moved(11_000); holder.nudge(10_000); advanceTimeBy(800)
        holder.nudge(10_000); advanceTimeBy(999); runCurrent()
        assertTrue(seeks.isEmpty()); assertEquals(40_000L, holder.target)
        advanceTimeBy(1); runCurrent()
        assertEquals(listOf(40_000L), seeks); assertNull(holder.target)
        holder.commit(); advanceUntilIdle(); assertEquals(1, seeks.size)
    }
    @Test fun cancelAndLostSeekabilityNeverSeekLater() = runTest {
        val clock = PlaybackClock().apply { update(10_000, 120_000, 120_000, true, 0) }
        val seeks = mutableListOf<Long>()
        var healthy = true
        val holder = ScrubHolder(this, clock, { healthy }, { seeks += it })
        holder.nudge(10_000); holder.cancel(); advanceUntilIdle(); assertTrue(seeks.isEmpty())
        holder.nudge(10_000); healthy = false; advanceUntilIdle(); assertTrue(seeks.isEmpty())
        healthy = true; holder.nudge(10_000); clock.update(10_000, 0, 0, false, 0)
        advanceUntilIdle(); assertTrue(seeks.isEmpty()); assertNull(holder.target)
    }
    @Test fun immediateCommitClampsToTheLatestDurationAndCancelsTheTimer() = runTest {
        val clock = PlaybackClock().apply { update(10_000, 120_000, 120_000, true, 0) }
        val seeks = mutableListOf<Long>()
        val holder = ScrubHolder(this, clock, { true }, { seeks += it })
        holder.nudge(60_000); clock.update(10_000, 50_000, 50_000, true, 0)
        assertTrue(holder.commit()); advanceUntilIdle(); assertEquals(listOf(50_000L), seeks)
        assertEquals(50_000L, holder.announcement)
    }
}
