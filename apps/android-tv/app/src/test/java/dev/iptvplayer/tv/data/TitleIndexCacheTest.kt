package dev.iptvplayer.tv.data

import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TitleIndexCacheTest {
    private fun catalog(title: String) = Catalog(listOf(MediaEntry("1", "1", MediaKind.MOVIE, title)))

    @Test fun cancellingTypedQueryDoesNotCancelSharedPreparation() = runTest {
        val cache = TitleIndexCache(backgroundScope, StandardTestDispatcher(testScheduler))
        val value = catalog("İstanbul")
        val prepared = cache.prepare(value)
        val query = async(start = CoroutineStart.UNDISPATCHED) { cache.get(value) }
        query.cancelAndJoin()
        assertFalse(prepared.isCancelled)
        runCurrent()
        assertSame(prepared.await(), cache.get(value))
        assertEquals(1, cache.get(value).find("istanbul").size)
    }

    @Test fun multipleWaitersReuseOneBuildAndCompletedIndex() = runTest {
        var builds = 0
        val cache = TitleIndexCache(backgroundScope, StandardTestDispatcher(testScheduler)) { items, checkpoint ->
            builds++; TitleIndex(items, checkpoint)
        }
        val value = catalog("Ortak")
        val first = async { cache.get(value) }; val second = async { cache.get(value) }
        runCurrent()
        assertSame(first.await(), second.await()); assertSame(first.await(), cache.get(value)); assertEquals(1, builds)
    }

    @Test fun replacingOrInvalidatingCatalogCancelsOldPreparation() = runTest {
        val cache = TitleIndexCache(backgroundScope, StandardTestDispatcher(testScheduler))
        val old = cache.prepare(catalog("Eski"))
        val replacement = catalog("Yeni")
        val current = cache.prepare(replacement)
        assertTrue(old.isCancelled); runCurrent()
        assertTrue(current.await().find("eski").isEmpty()); assertEquals(1, current.await().find("yeni").size)
        cache.invalidate()
        assertNotSame(current, cache.prepare(replacement))
    }

    @Test fun failedPreparationCanBeRetried() = runTest {
        val owner = CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler))
        var attempts = 0
        try {
            val cache = TitleIndexCache(owner, StandardTestDispatcher(testScheduler)) { entries, checkpoint ->
                if (++attempts == 1) error("fixture failure")
                TitleIndex(entries, checkpoint)
            }
            val value = catalog("Tekrar")
            val first = cache.prepare(value); runCurrent()
            assertTrue(runCatching { first.await() }.isFailure)
            val second = cache.prepare(value); runCurrent()
            assertEquals(1, second.await().find("tekrar").size); assertEquals(2, attempts)
        } finally { owner.cancel() }
    }
}
