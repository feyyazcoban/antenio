@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
package dev.iptvplayer.tv.data

import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

class SourceTransactionsTest {
    @Test fun readerCannotSeeHalfOfASourceAndCatalogCommit() = runTest {
        val gate = SourceTransactions(StandardTestDispatcher(testScheduler))
        val release = CompletableDeferred<Unit>()
        var source = "old"; var cache = "old"; var observed: Pair<String, String>? = null
        val writer = launch { gate.write { source = "new"; release.await(); cache = "new" } }
        runCurrent()
        val reader = launch { observed = gate.read { source to cache } }
        runCurrent(); assertNull(observed)
        release.complete(Unit); writer.join(); reader.join()
        assertEquals("new" to "new", observed)
    }
    @Test fun cancellingAnAcceptedCommitDoesNotLetRemovalRunEarly() = runTest {
        val gate = SourceTransactions(StandardTestDispatcher(testScheduler))
        val release = CompletableDeferred<Unit>(); val events = mutableListOf<String>()
        val writer = launch {
            gate.write { events.add("start"); release.await(); events.add("saved") }
            currentCoroutineContext().ensureActive()
            events.add("obsolete UI")
        }
        runCurrent(); writer.cancel()
        val removal = launch { gate.write { events.add("removed") } }
        runCurrent(); assertEquals(listOf("start"), events)
        release.complete(Unit); writer.join(); removal.join()
        assertEquals(listOf("start", "saved", "removed"), events)
    }
    @Test fun cancellingAQueuedWritePreventsItFromTouchingStorage() = runTest {
        val gate = SourceTransactions(StandardTestDispatcher(testScheduler))
        val release = CompletableDeferred<Unit>(); var staleWrite = false
        val first = launch { gate.write { release.await() } }; runCurrent()
        val queued = launch { gate.write { staleWrite = true } }; runCurrent()
        queued.cancel(); release.complete(Unit); first.join(); queued.join()
        assertFalse(staleWrite)
    }
    @Test fun diskFailureReleasesTheGateForRetry() = runTest {
        val gate = SourceTransactions(StandardTestDispatcher(testScheduler))
        val outcome = runCatching { gate.write<Unit> { throw java.io.IOException("synthetic") } }
        assertTrue(outcome.isFailure)
        assertEquals("retry", gate.write { "retry" })
    }
}
