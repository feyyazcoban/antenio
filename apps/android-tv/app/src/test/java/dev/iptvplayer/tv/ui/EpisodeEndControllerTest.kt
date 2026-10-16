@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
package dev.iptvplayer.tv.ui

import dev.iptvplayer.tv.data.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

class EpisodeEndControllerTest {
    private val current = MediaEntry("one", "one", MediaKind.EPISODE, "Birinci", episodeOrderKnown = true)
    private val next = NextEpisodeChoice(current.copy(id = "two", episode = 2), true)

    @Test fun waitsTenSecondsAndTransitionsExactlyOnce() = runTest {
        val calls = mutableListOf<String>()
        val c = EpisodeEndController(this) { source, from, to, automatic -> calls.add("$source/$from/$to/$automatic"); true }
        c.ended("source", current, next, true); runCurrent()
        assertEquals(10, c.state.value!!.seconds)
        advanceTimeBy(9_999); runCurrent(); assertTrue(calls.isEmpty())
        advanceTimeBy(1); runCurrent(); assertEquals(listOf("source/one/two/true"), calls)
        c.ended("source", current, next, true); advanceTimeBy(20_000); runCurrent()
        assertEquals(1, calls.size); assertNull(c.state.value); c.close()
    }
    @Test fun dismissPreventsCountdownAndRepeatedEndDoesNotReopenIt() = runTest {
        var calls = 0; val c = EpisodeEndController(this) { _, _, _, _ -> calls++; true }
        c.ended("s", current, next, true); runCurrent(); advanceTimeBy(3_000); c.dismiss()
        c.ended("s", current, next, true); advanceTimeBy(20_000); runCurrent()
        assertEquals(0, calls); assertNull(c.state.value); c.close()
    }
    @Test fun disabledOrAmbiguousAutomaticPlaybackStillAllowsManualChoice() = runTest {
        val flags = mutableListOf<Boolean>(); val c = EpisodeEndController(this) { _, _, _, auto -> flags.add(auto); true }
        c.ended("s", current, next, false); advanceTimeBy(20_000); runCurrent()
        assertNull(c.state.value!!.seconds); assertTrue(flags.isEmpty()); c.playNow()
        c.newMedia(); c.ended("s", current, next.copy(automaticAllowed = false), true)
        advanceTimeBy(20_000); runCurrent(); assertNull(c.state.value!!.seconds); c.playNow()
        assertEquals(listOf(false, false), flags); c.close()
    }
    @Test fun preferenceChangeCancelsTheLiveCountdown() = runTest {
        var calls = 0; val c = EpisodeEndController(this) { _, _, _, _ -> calls++; true }
        c.ended("s", current, next, true); runCurrent(); c.preferenceChanged(false)
        c.preferenceChanged(true); advanceTimeBy(20_000); runCurrent()
        assertEquals(0, calls); assertNull(c.state.value!!.seconds); c.close()
    }
    @Test fun backgroundDoesNotResumeAnAutomaticCountdown() = runTest {
        var calls = 0; val c = EpisodeEndController(this) { _, _, _, _ -> calls++; true }
        c.ended("s", current, next, true); runCurrent(); c.background(); c.playNow()
        advanceTimeBy(20_000); runCurrent(); c.foreground(); advanceTimeBy(20_000); runCurrent()
        assertEquals(0, calls); assertNull(c.state.value!!.seconds)
        c.playNow(); assertEquals(1, calls); c.close()
    }
    @Test fun targetChangeAndRejectedFinalValidationDoNotOpenAnotherEpisode() = runTest {
        var calls = 0; val c = EpisodeEndController(this) { _, _, _, _ -> calls++; false }
        c.ended("s", current, next, true); runCurrent(); c.availabilityChanged(next.copy(entry = next.entry.copy(id = "other")))
        advanceTimeBy(20_000); runCurrent(); assertEquals(0, calls); assertNull(c.state.value!!.next)
        c.newMedia(); c.ended("s", current, next, true); runCurrent(); advanceTimeBy(10_000); runCurrent()
        assertEquals(1, calls); assertNull(c.state.value!!.next); assertNotNull(c.state.value!!.notice); c.close()
    }
    @Test fun newMediaAndDisposalCancelOldCountdowns() = runTest {
        var calls = 0; val c = EpisodeEndController(this) { _, _, _, _ -> calls++; true }
        c.ended("s", current, next, true); runCurrent(); c.newMedia()
        advanceTimeBy(20_000); runCurrent(); assertEquals(0, calls)
        c.ended("s", current, next, true); runCurrent(); c.close()
        advanceTimeBy(20_000); runCurrent(); assertEquals(0, calls)
    }
    @Test fun finalEpisodeHasNoTimerAndMoviesDoNotOpenAnEpisodePrompt() = runTest {
        var calls = 0; val c = EpisodeEndController(this) { _, _, _, _ -> calls++; true }
        c.ended("s", current.copy(kind = MediaKind.MOVIE), next, true); assertNull(c.state.value)
        c.ended("s", current, null, true); advanceTimeBy(20_000); runCurrent()
        assertNotNull(c.state.value); assertNull(c.state.value!!.next); assertNull(c.state.value!!.seconds)
        assertEquals(0, calls); c.close()
    }
}
