package dev.iptvplayer.tv.data

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** AtomicFile protects one replacement, not concurrent source/cache operations or separate view models. */
internal class SourceTransactions(private val io: CoroutineDispatcher = Dispatchers.IO) {
    private val mutex = Mutex()

    suspend fun <T> read(block: suspend () -> T): T = mutex.withLock { withContext(io) { block() } }

    suspend fun <T> write(block: suspend () -> T): T = mutex.withLock {
        currentCoroutineContext().ensureActive()
        // Once accepted, finish the disk transaction before a new reader/removal can enter.
        // Keep NonCancellable outside the dispatcher switch so cancellation cannot drop its result.
        withContext(NonCancellable) { withContext(io) { block() } }
    }

    companion object { val shared = SourceTransactions() }
}
