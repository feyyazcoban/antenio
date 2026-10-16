package dev.iptvplayer.tv.data

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import java.time.ZoneId

data class GuideStatus(val configured: Boolean = false, val initialized: Boolean = false, val refreshing: Boolean = false,
    val info: EpgSnapshotInfo? = null, val error: String? = null)
data class GuideState(val statuses: Map<String, GuideStatus> = emptyMap(), val revision: Int = 0)

/** Main-thread orchestration; cache/network work is IO. Scope is owned by the ViewModel. */
internal class GuideController(private val scope: CoroutineScope, private val store: EpgStore,
    private val device: DeviceStore, private val catalogs: CatalogStore, private val zone: () -> ZoneId,
    private val repository: EpgRepository = EpgRepository()) {
    private class Owner(val source: SourceConfig, val plan: EpgPlan?, val error: String? = null)
    private var owners = emptyMap<String, Owner>()
    private val jobs = mutableMapOf<String, Job>()
    private val attempts = mutableMapOf<String, Long>()
    private val pendingDownload = mutableSetOf<String>()
    private val cancelled = mutableSetOf<String>()
    private val network = Mutex()
    private val mutable = MutableStateFlow(GuideState())
    val state = mutable.asStateFlow()

    fun synchronize(sources: List<SourceConfig>, snapshots: Map<String, CatalogSnapshot>) {
        val next = sources.associate { source ->
            val parsed = runCatching { EpgPlan.create(source, snapshots[source.id]?.catalog ?: Catalog(emptyList())) }
            val plan = parsed.getOrNull()
            val old = owners[source.id]
            source.id to if (old != null && old.source == source && old.plan?.binding == plan?.binding && parsed.isSuccess) old
                else Owner(source, plan, if (parsed.isFailure) "Rehber adresi okunamadı. Kaynak ayarlarını kontrol et." else null)
        }
        val changed = (owners.keys + next.keys).filter { id -> owners[id] !== next[id] }
        owners = next
        changed.forEach { id ->
            jobs.remove(id)?.cancel(); attempts.remove(id); pendingDownload.remove(id); cancelled.remove(id)
            update(id, next[id]?.let { GuideStatus(configured = it.plan != null, error = it.error) })
        }
    }

    fun forget(id: String? = null) {
        val ids = if (id == null) owners.keys.toList() else listOf(id)
        owners = owners.filterKeys { it !in ids }
        ids.forEach { owner -> jobs.remove(owner)?.cancel(); attempts.remove(owner); pendingDownload.remove(owner); cancelled.remove(owner); update(owner, null) }
    }

    fun cancel(id: String) {
        if (jobs[id]?.isActive != true) return
        cancelled += id; pendingDownload.remove(id); attempts[id] = System.currentTimeMillis()
        jobs[id]?.cancel()
    }

    fun ensure(ids: Collection<String>, force: Boolean = false, download: Boolean = true) {
        ids.distinct().forEach { id ->
            val owner = owners[id] ?: return@forEach
            val plan = owner.plan ?: return@forEach
            if (jobs[id]?.isCompleted == false) {
                if (download && !force && id !in cancelled) pendingDownload += id
                return@forEach
            }
            val status = mutable.value.statuses[id] ?: GuideStatus(configured = true)
            if (status.initialized && (!download || (!force && (!stale(status.info) ||
                (status.error != null && System.currentTimeMillis() - (attempts[id] ?: 0) < 5 * 60_000))))) return@forEach
            jobs[id] = scope.launch {
                var current = status
                try {
                    if (!current.initialized) {
                        val info = withContext(Dispatchers.IO) { store.info(plan) }
                        current = current.copy(initialized = true, info = info)
                        if (owners[id] !== owner) return@launch
                        update(id, current)
                    }
                    if (download && (force || stale(current.info))) {
                        attempts[id] = System.currentTimeMillis()
                        current = current.copy(refreshing = true, error = null)
                        update(id, current)
                        val accepted = network.withLock {
                            repository.refresh(plan, store, GuideWindow.around(Instant.now(), zone()), accept = {
                                // Also protects against a different model editing the persisted source/feed during download.
                                val saved = device.loadSources().sources.firstOrNull { it.id == id }
                                saved == owner.source && (saved.type != SourceType.M3U || saved.epgUrl.isNotEmpty() ||
                                    catalogs.readEpgFeeds(saved)?.let { feeds ->
                                        EpgPlan.create(saved, Catalog(emptyList(), epgUrls = feeds))?.binding == plan.binding
                                    } == true)
                            })
                        }
                        ensureActive()
                        if (owners[id] !== owner) return@launch
                        val info = withContext(Dispatchers.IO) { store.info(plan) }
                        current = current.copy(info = info, error = if (accepted) null else "Kaynak değişti. Rehberi yeniden açabilirsin.")
                    }
                } catch (e: CancellationException) { throw e }
                catch (e: Exception) { current = current.copy(error = (e as? SourceFailure)?.message ?: "Rehber yenilenemedi. Kayıtlı rehber korunuyor.") }
                finally {
                    if (owners[id] === owner) {
                        jobs.remove(id)
                        update(id, current.copy(refreshing = false, error = if (cancelled.remove(id)) "Rehber yenilemesi iptal edildi." else current.error))
                        if (pendingDownload.remove(id)) ensure(listOf(id))
                    }
                }
            }
        }
    }

    suspend fun query(entries: List<MediaEntry>, window: GuideWindow): Map<String, GuideSlice> {
        val plans = owners.mapValues { it.value.plan }
        return withContext(Dispatchers.IO) {
            buildMap {
                entries.filter { it.kind == MediaKind.LIVE && !it.adult }.groupBy { it.sourceId }.forEach { (id, channels) ->
                    ensureActive()
                    plans[id]?.let { putAll(store.queryMany(it, channels, window)) }
                }
            }
        }
    }

    private fun stale(info: EpgSnapshotInfo?): Boolean {
        val now = System.currentTimeMillis()
        val wanted = GuideWindow.around(Instant.ofEpochMilli(now), zone())
        return info == null || info.updatedAt < now - 6 * 60 * 60_000L || info.updatedAt > now + 5 * 60_000 ||
            info.window.from > wanted.from || info.window.until < wanted.until
    }
    private fun update(id: String, value: GuideStatus?) {
        val old = mutable.value
        if (old.statuses[id] == value) return
        mutable.value = GuideState(if (value == null) old.statuses - id else old.statuses + (id to value), old.revision + 1)
    }
}
