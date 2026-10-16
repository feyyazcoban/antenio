package dev.iptvplayer.tv.data

import kotlinx.coroutines.*

/** Accessed by the ViewModel on main; construction belongs to its scope, not to a typed query. */
internal class TitleIndexCache(
    private val scope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val build: (List<MediaEntry>, () -> Unit) -> TitleIndex = { entries, checkpoint -> TitleIndex(entries, checkpoint) }
) {
    private var catalog: Catalog? = null
    private var pending: Deferred<TitleIndex>? = null

    fun prepare(value: Catalog): Deferred<TitleIndex> {
        pending?.takeIf { catalog === value && !it.isCancelled }?.let { return it }
        invalidate()
        catalog = value
        return scope.async(dispatcher) {
            val context = currentCoroutineContext()
            build(value.items) { context.ensureActive() }
        }.also { pending = it }
    }

    suspend fun get(value: Catalog): TitleIndex = prepare(value).await()

    fun invalidate() {
        pending?.cancel(); pending = null; catalog = null
    }
}
