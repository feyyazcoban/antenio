package dev.iptvplayer.tv

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.iptvplayer.tv.data.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

enum class Route { SETUP, LOADING, HOME, LIBRARY, GUIDE, SEARCH, DETAIL, PLAYER, SOURCES, RECOVERY }
enum class SourceTask { RESTORING, IMPORTING, SAVING, REMOVING, RESETTING, IDLE }
data class SourceDraft(val name: String = "Evim", val type: SourceType = SourceType.XTREAM,
    val address: String = "", val username: String = "", val password: String = "", val epgUrl: String = "") {
    override fun toString() = "SourceDraft(type=$type, credentials=REDACTED)"
}
data class PlayingMedia(val entry: MediaEntry, val url: String, val startMs: Long = 0) {
    override fun toString() = "PlayingMedia(id=${entry.id}, url=REDACTED)"
}
data class SourceStatus(val count: Int = 0, val updatedAt: Long = 0, val refreshing: Boolean = false,
    val notice: String? = null, val failed: Boolean = false)
/** Success belongs beside the operation; actionable failures remain visible in the catalog too. */
enum class CatalogOperation { REFRESH, SOURCE_REMOVAL, SOURCE_FILTER }
data class CatalogNotice(val message: String, val operation: CatalogOperation, val needsAttention: Boolean = false)
data class TvState(
    val route: Route = Route.LOADING,
    val draft: SourceDraft = SourceDraft(),
    val sources: List<SourceConfig> = emptyList(),
    val sourceFilterId: String? = null,
    val searchSourceId: String? = null,
    val sourceStatus: Map<String, SourceStatus> = emptyMap(),
    val guideStatus: Map<String, GuideStatus> = emptyMap(),
    val guideRevision: Int = 0,
    val guideZoneId: String = "",
    val guideChannelId: String? = null,
    val guideAnchor: Long? = null,
    val guideSaveRevision: Int = 0,
    val guideSaveError: String? = null,
    val editingSourceId: String? = null,
    val setupReturn: Route = Route.HOME,
    val sourcesReturn: Route = Route.HOME,
    val sourceManagerFocus: String = "add-source",
    val allCatalog: Catalog = Catalog(emptyList()),
    val catalog: Catalog = Catalog(emptyList()),
    val kind: MediaKind = MediaKind.LIVE,
    val category: String = "Tümü",
    val query: String = "",
    val libraryMatches: Set<String> = emptySet(),
    val librarySearching: Boolean = false,
    val searchQuery: String = "",
    val searchKind: MediaKind? = null,
    val searchResults: List<MediaEntry> = emptyList(),
    val searching: Boolean = false,
    val searchFocusedId: String? = null,
    val onlyFavorites: Boolean = false,
    val favoriteIds: Set<String> = emptySet(),
    val history: List<WatchRecord> = emptyList(),
    val detailReturn: Route = Route.LIBRARY,
    val focusedId: String? = null,
    val selected: MediaEntry? = null,
    val episodes: List<MediaEntry> = emptyList(),
    val season: Int = 1,
    val detailLoading: Boolean = false,
    val playing: PlayingMedia? = null,
    val playerReturn: Route = Route.LIBRARY,
    val progressLabel: String = "Hazırlanıyor…",
    val sourceTask: SourceTask = SourceTask.RESTORING,
    val refreshing: Boolean = false,
    val catalogUpdatedAt: Long = 0,
    val catalogNotice: CatalogNotice? = null,
    val recoveryCanReset: Boolean = false,
    val error: String? = null,
    val detailError: String? = null,
    val reducedMotion: Boolean = false,
    val startupSound: Boolean = true,
    val autoNextEpisode: Boolean = true,
    /** The Sırala choice of the film and series lists, kept on the device. */
    val librarySorts: Map<MediaKind, LibrarySort> = emptyMap(),
    val revision: Int = 0
) {
    // Display/default selection only. Playback and personal data must use sourceFor(entry).
    val source: SourceConfig? get() = sources.firstOrNull { it.id == sourceFilterId } ?: sources.firstOrNull()
    val sourceLabel: String get() = sources.firstOrNull { it.id == sourceFilterId }?.name
        ?: if (sources.size == 1) sources.first().name else "Tüm kaynaklar"
    fun sourceFor(entry: MediaEntry): SourceConfig? = sources.firstOrNull { it.id == entry.sourceId }
    fun sourceName(id: String): String {
        val source = sources.firstOrNull { it.id == id } ?: return ""
        return if (sources.count { it.name == source.name } > 1) "${source.name} · ${sources.indexOf(source) + 1}" else source.name
    }
}

class TvViewModel(application: Application) : AndroidViewModel(application) {
    val store = DeviceStore(application)
    private val catalogStore = CatalogStore(application)
    private val epgStore = EpgStore(application)
    private val repository = SourceRepository()
    private val artwork = (application as TvApplication).artworkCache
    private var recoveryStamp: String? = null
    private val transactions = SourceTransactions.shared
    private val mutable = MutableStateFlow(TvState(reducedMotion = store.reducedMotion, startupSound = store.startupSound, autoNextEpisode = store.autoNextEpisode, guideZoneId = store.guideZoneId, librarySorts = storedSorts()))
    val state = mutable.asStateFlow()
    private var registry = SourceRegistry()
    private val snapshots = LinkedHashMap<String, CatalogSnapshot>()
    private var filterJob: Job? = null
    private var filterGeneration = 0
    private var loadJob: Job? = null
    private var detailJob: Job? = null
    private var generation = 0
    private var searchJob: Job? = null
    private val searchIndex = TitleIndexCache(viewModelScope)
    private var searchGeneration = 0
    private var librarySearchJob: Job? = null
    private var librarySearchGeneration = 0
    private val guides = GuideController(viewModelScope, epgStore, store, catalogStore, { guideZone() })

    init {
        viewModelScope.launch { guides.state.collect { value -> mutable.update { it.copy(guideStatus = value.statuses, guideRevision = value.revision) } } }
        restore()
    }

    /** Migration is serialized with accepted writes from a previous ViewModel. */
    private fun readRegistry(): SourceRegistry {
        val current = store.loadSources()
        if (!store.hasSourceRegistry) {
            catalogStore.migrateLegacy(current.sources.singleOrNull())
            store.saveSources(current)
        }
        return current
    }

    fun restore() {
        if (state.value.sourceTask in setOf(SourceTask.SAVING, SourceTask.REMOVING, SourceTask.RESETTING)) return
        val token = ++generation
        loadJob?.cancel()
        guides.forget()
        mutable.update { it.copy(route = Route.LOADING, sourceTask = SourceTask.RESTORING,
            progressLabel = "Kayıtlı kütüphanen kontrol ediliyor…", error = null) }
        loadJob = viewModelScope.launch {
            var observedStamp: String? = null
            try {
                val (saved, cached) = transactions.write {
                    observedStamp = store.recoveryStamp()
                    val saved = readRegistry()
                    artwork.synchronizeSources(saved.sources.map { it.id })
                    saved to saved.sources.mapNotNull { source -> catalogStore.read(source)?.let { source.id to it } }.toMap()
                }
                currentCoroutineContext().ensureActive()
                if (generation != token) return@launch
                recoveryStamp = null
                registry = saved; snapshots.clear(); snapshots.putAll(cached)
                publishLibrary()
                mutable.update { it.copy(route = if (saved.sources.isEmpty()) Route.SETUP else Route.HOME,
                    sourceTask = SourceTask.IDLE, recoveryCanReset = false) }
                // The screens no longer offer a source selector (D060): an older saved choice must not keep hiding sources.
                if (saved.filterId != null) sourceFilter(null)
                val stale = saved.sources.filter { (cached[it.id]?.updatedAt ?: 0) < System.currentTimeMillis() - 6 * 60 * 60 * 1000L }
                if (stale.isNotEmpty()) refreshSources(stale, token)
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) {
                if (generation == token) {
                    recoveryStamp = observedStamp.takeIf { e is StoredSourcesUnavailable }
                    mutable.update { it.copy(route = Route.RECOVERY, sourceTask = SourceTask.IDLE,
                        playing = null, recoveryCanReset = recoveryStamp != null,
                        error = "Kayıtlı kütüphanen açılamadı. Kayıtların korunuyor; yeniden deneyebilirsin.") }
                }
            }
        }
    }

    /** Only the explicit recovery confirmation calls this; unreadable records are never dropped on startup. */
    fun resetUnreadableSources() {
        val stamp = recoveryStamp ?: return
        if (state.value.route != Route.RECOVERY || state.value.sourceTask != SourceTask.IDLE) return
        val token = ++generation
        val previousJob = loadJob
        previousJob?.cancel(); detailJob?.cancel(); filterJob?.cancel(); resetSearchIndex()
        guides.forget()
        mutable.update { it.copy(route = Route.LOADING, sourceTask = SourceTask.RESETTING, playing = null,
            progressLabel = "Bu cihazdaki kaynak kayıtları temizleniyor…") }
        loadJob = viewModelScope.launch {
            try {
                previousJob?.join()
                val cleared = transactions.write {
                    if (store.recoveryStamp() != stamp || runCatching { store.loadSources() }.isSuccess) false
                    else {
                        artwork.clear()
                        catalogStore.clear()
                        epgStore.clear()
                        store.resetUnreadableSources()
                        true
                    }
                }
                currentCoroutineContext().ensureActive()
                if (generation != token) return@launch
                recoveryStamp = null
                if (!cleared) {
                    mutable.update { it.copy(sourceTask = SourceTask.IDLE) }
                    restore()
                    return@launch
                }
                registry = SourceRegistry(); snapshots.clear()
                mutable.value = TvState(route = Route.SETUP, sourceTask = SourceTask.IDLE,
                    reducedMotion = store.reducedMotion, startupSound = store.startupSound, autoNextEpisode = store.autoNextEpisode, guideZoneId = store.guideZoneId, librarySorts = storedSorts())
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) {
                if (generation == token) mutable.update { it.copy(route = Route.RECOVERY, sourceTask = SourceTask.IDLE,
                    error = "Temizlik tamamlanamadı. Boş alanı kontrol edip tekrar dene.", recoveryCanReset = true) }
            }
        }
    }

    private fun filtered(catalog: Catalog, id: String?): Catalog = if (id == null) catalog else Catalog(
        catalog.items.filter { it.sourceId == id },
        catalog.m3uEpisodes.filterKeys { ContentRef.parse(it)?.sourceId == id }, catalog.warnings, categoryOrder = catalog.categoryOrder)

    private suspend fun publishLibrary() {
        val saved = registry
        val raw = snapshots.mapValues { it.value.catalog }
        val all = withContext(Dispatchers.Default) { SourceLibrary.combine(saved.sources, raw) }
        val favorites = SourceLibrary.favorites(saved.sources, store::favorites)
        val history = SourceLibrary.history(saved.sources, store::history)
        guides.synchronize(saved.sources, snapshots)
        mutable.update { old ->
            val filter = registry.filterId?.takeIf { id -> saved.sources.any { it.id == id } }
            val catalog = filtered(all, filter)
            old.copy(sources = saved.sources, sourceFilterId = filter, allCatalog = all, catalog = catalog,
                favoriteIds = favorites, history = history,
                searchSourceId = old.searchSourceId?.takeIf { id -> saved.sources.any { it.id == id } },
                sourceStatus = saved.sources.associate { source ->
                    val snapshot = snapshots[source.id]
                    source.id to (old.sourceStatus[source.id] ?: SourceStatus()).copy(
                        count = snapshot?.catalog?.items?.count { !it.adult && it.kind != MediaKind.EPISODE } ?: 0,
                        updatedAt = snapshot?.updatedAt ?: 0)
                }, catalogUpdatedAt = snapshots.values.maxOfOrNull { it.updatedAt } ?: 0,
                category = old.category.takeIf { category -> catalog.items.any { !it.adult &&
                    (if (old.onlyFavorites) it.id in favorites else it.kind == old.kind) && it.category == category } } ?: "Tümü")
        }
        resetSearchIndex()
        // StateFlow may retain the previous, equal Catalog instance on an unchanged refresh.
        // Prepare the published instance so the first query reuses this work.
        searchIndex.prepare(state.value.allCatalog)
        prepareLibraryOrders()
        repairEpisodeFavorites()
        updateSearch(clearResults = false)
        updateLibrarySearch(clearResults = false)
    }

    fun draft(value: SourceDraft) { mutable.update { it.copy(draft = value, error = null) } }
    fun connectDraft() {
        if (state.value.route != Route.SETUP) return
        if (state.value.sourceTask != SourceTask.IDLE && state.value.sourceTask != SourceTask.IMPORTING) return
        val draft = state.value.draft
        try {
            val source = SourceConfig(name = draft.name, type = draft.type, address = draft.address,
                username = draft.username, password = draft.password, epgUrl = draft.epgUrl).validated().let { value ->
                state.value.editingSourceId?.let { value.copy(id = it) } ?: value
            }
            if (state.value.sources.any { it.id != source.id && sameConnection(it, source) })
                throw SourceFailure("Bu bağlantı zaten ekli. Kaynaklar ekranından düzenleyebilirsin.")
            connect(source)
        } catch (e: SourceFailure) { mutable.update { it.copy(error = e.message) } }
    }

    private fun sameConnection(a: SourceConfig, b: SourceConfig) =
        a.type == b.type && a.address == b.address && a.username == b.username

    private fun connect(source: SourceConfig) {
        val token = ++generation
        loadJob?.cancel(); detailJob?.cancel()
        mutable.update { it.copy(route = Route.LOADING, sourceTask = SourceTask.IMPORTING,
            error = null, refreshing = false, catalogNotice = null,
            sourceStatus = it.sourceStatus.mapValues { (_, status) -> status.copy(refreshing = false) }) }
        loadJob = viewModelScope.launch {
            try {
                val catalog = repository.load(source) { message ->
                    if (generation == token) mutable.update { it.copy(progressLabel = message) }
                }
                if (generation != token) return@launch
                if (snapshots[source.id] != null && catalog.warnings.isNotEmpty())
                    throw SourceFailure("Kaynağın bazı bölümleri alınamadı. Önceki hesap ve kütüphane korunuyor.")
                val snapshot = CatalogSnapshot(catalog, System.currentTimeMillis())
                mutable.update { it.copy(sourceTask = SourceTask.SAVING, progressLabel = "Kütüphanen cihazına kaydediliyor…") }
                val saved = transactions.write {
                    val latest = readRegistry()
                    if (latest.sources.any { it.id != source.id && sameConnection(it, source) })
                        throw SourceFailure("Bu bağlantı zaten ekli.")
                    if (state.value.editingSourceId != null && latest.sources.none { it.id == source.id })
                        throw SourceFailure("Bu kaynak artık kayıtlı değil. Kaynaklar ekranını yeniden aç.")
                    // A successful import always has a durable snapshot before publishing the account.
                    catalogStore.write(source, snapshot)
                    latest.upsert(source).also { store.saveSources(it); artwork.synchronizeSources(it.sources.map { source -> source.id }) }
                }
                currentCoroutineContext().ensureActive()
                if (generation != token) return@launch
                registry = saved; snapshots[source.id] = snapshot
                publishLibrary()
                homeFocusKey = null
                mutable.update { it.copy(route = if (it.setupReturn == Route.SOURCES) Route.SOURCES else Route.HOME,
                    sourceTask = SourceTask.IDLE, editingSourceId = null, draft = SourceDraft(), playing = null,
                    selected = null, episodes = emptyList(), focusedId = null,
                    sourceStatus = it.sourceStatus + (source.id to SourceStatus(
                        catalog.items.count { entry -> !entry.adult }, snapshot.updatedAt,
                        notice = catalog.warnings.takeIf { warnings -> warnings.isNotEmpty() }?.joinToString(" "), failed = catalog.warnings.isNotEmpty()))) }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) {
                if (generation == token) mutable.update { it.copy(route = Route.SETUP, sourceTask = SourceTask.IDLE,
                    error = (e as? SourceFailure)?.message ?: "Kaynak güvenle kaydedilemedi. Tekrar dene.") }
            }
        }
    }

    fun cancelImport() {
        if (state.value.sourceTask != SourceTask.IMPORTING) return
        generation++; loadJob?.cancel()
        mutable.update { it.copy(route = Route.SETUP, sourceTask = SourceTask.IDLE, error = null) }
    }

    fun refresh(sourceId: String? = null) {
        if (state.value.refreshing || state.value.sourceTask != SourceTask.IDLE) return
        val sources = state.value.sources.filter { sourceId == null || it.id == sourceId }
        if (sources.isEmpty()) return
        val token = ++generation
        loadJob?.cancel()
        loadJob = viewModelScope.launch { refreshSources(sources, token) }
    }

    private suspend fun refreshSources(sources: List<SourceConfig>, token: Int) {
        mutable.update { it.copy(refreshing = true, catalogNotice = null) }
        for (source in sources) {
            if (generation != token) return
            status(source.id) { it.copy(refreshing = true, notice = null, failed = false) }
            try {
                val catalog = repository.load(source) { message ->
                    if (generation == token) mutable.update { it.copy(progressLabel = message) }
                }
                currentCoroutineContext().ensureActive()
                if (generation != token) return
                if (catalog.warnings.isNotEmpty() && snapshots[source.id] != null)
                    throw SourceFailure("Kaynağın bazı bölümleri yenilenemedi. Mevcut liste korunuyor.")
                val snapshot = CatalogSnapshot(catalog, System.currentTimeMillis())
                transactions.write {
                    // A different model may have removed/edited this account while the network was pending.
                    if (store.loadSources().sources.none { it == source }) throw SourceFailure("Kaynak değişti; yeniden aç.")
                    catalogStore.write(source, snapshot)
                }
                currentCoroutineContext().ensureActive()
                if (generation != token) return
                snapshots[source.id] = snapshot
                publishLibrary()
                status(source.id) { it.copy(refreshing = false, notice = catalog.warnings.takeIf { it.isNotEmpty() }?.joinToString(" ")
                    ?: "Kütüphanen güncellendi.", failed = catalog.warnings.isNotEmpty()) }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) {
                if (generation != token) return
                status(source.id) { it.copy(refreshing = false, failed = true,
                    notice = (e as? SourceFailure)?.message ?: "Liste yenilenemedi. Son kaydedilen kütüphaneni kullanabilirsin.") }
            }
        }
        if (generation == token) mutable.update { it.copy(refreshing = false,
            catalogNotice = if (sources.any { source -> it.sourceStatus[source.id]?.failed == true })
                CatalogNotice("Bazı kaynaklar yenilenemedi. Kayıtlı içerikler korunuyor; Kaynaklar’dan ayrıntıları görebilirsin.", CatalogOperation.REFRESH, needsAttention = true)
            else CatalogNotice("Kütüphanen güncellendi.", CatalogOperation.REFRESH)) }
    }

    private fun status(id: String, change: (SourceStatus) -> SourceStatus) {
        mutable.update { it.copy(sourceStatus = it.sourceStatus + (id to change(it.sourceStatus[id] ?: SourceStatus()))) }
    }

    fun sourceFilter(id: String?) {
        if (id != null && state.value.sources.none { it.id == id }) return
        if (state.value.sourceTask != SourceTask.IDLE) return
        val token = ++filterGeneration
        filterJob?.cancel()
        registry = registry.copy(filterId = id)
        homeFocusKey = null
        mutable.update { it.copy(sourceFilterId = id, catalog = filtered(it.allCatalog, id), category = "Tümü",
            focusedId = null) }
        updateLibrarySearch()
        filterJob = viewModelScope.launch {
            try {
                transactions.write {
                    val latest = readRegistry()
                    if (token == filterGeneration && (id == null || latest.sources.any { it.id == id }))
                        store.saveSources(latest.copy(filterId = id))
                }
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) { if (token == filterGeneration) mutable.update { it.copy(catalogNotice = CatalogNotice("Kaynak seçimi kaydedilemedi. Tekrar seçebilirsin.", CatalogOperation.SOURCE_FILTER, needsAttention = true)) } }
        }
    }

    fun searchSource(id: String?) {
        if (id != null && state.value.sources.none { it.id == id }) return
        mutable.update { it.copy(searchSourceId = id, searchFocusedId = null) }
        updateSearch()
    }

    fun home() { homeFocusKey = null; mutable.update { it.copy(route = Route.HOME) } }
    /** The last focused Home item. Kept out of [state]: a focus move must not recompose the whole app. */
    var homeFocusKey: String? = null
        private set
    fun homeFocus(key: String) { homeFocusKey = key }
    fun guide() {
        mutable.update { it.copy(route = Route.GUIDE, guideAnchor = System.currentTimeMillis(), guideSaveError = null,
            guideChannelId = it.focusedId?.takeIf { id -> it.catalog.items.any { channel -> channel.id == id && channel.kind == MediaKind.LIVE && !channel.adult } }
                ?: it.history.firstOrNull { mark -> mark.kind == MediaKind.LIVE }?.id) }
    }
    fun guideCursor(id: String?, anchor: Long) { mutable.update { it.copy(guideChannelId = id, guideAnchor = anchor) } }
    fun guideZone(): java.time.ZoneId = state.value.guideZoneId.takeIf { it.isNotEmpty() }?.let(java.time.ZoneId::of) ?: java.time.ZoneId.systemDefault()
    fun guideZone(value: String) { store.guideZoneId = value; mutable.update { it.copy(guideZoneId = value) } }
    fun ensureGuide(ids: Collection<String>, download: Boolean = true) { guides.ensure(ids, download = download) }
    fun refreshGuide(id: String) { guides.ensure(listOf(id), force = true) }
    fun cancelGuide(id: String) { guides.cancel(id) }
    suspend fun guideRows(entries: List<MediaEntry>, window: GuideWindow) = guides.query(entries, window)
    fun clearGuideSaveError() { mutable.update { it.copy(guideSaveError = null) } }
    fun saveGuideUrl(id: String, address: String) {
        if (state.value.sourceTask != SourceTask.IDLE || state.value.refreshing) {
            mutable.update { it.copy(guideSaveError = "Kaynak işlemi tamamlanınca yeniden dene.") }; return
        }
        val original = state.value.sources.firstOrNull { it.id == id } ?: return
        val changed = try { original.copy(epgUrl = address).validated() }
            catch (e: SourceFailure) { mutable.update { it.copy(guideSaveError = e.message) }; return }
        val token = ++generation
        loadJob?.cancel()
        mutable.update { it.copy(sourceTask = SourceTask.SAVING, guideSaveError = null) }
        loadJob = viewModelScope.launch {
            try {
                val saved = transactions.write {
                    val latest = readRegistry()
                    if (latest.sources.none { it == original }) throw SourceFailure("Kaynak değişti. Ayarları yeniden aç.")
                    latest.upsert(changed).also(store::saveSources)
                }
                currentCoroutineContext().ensureActive()
                if (generation != token) return@launch
                registry = saved
                guides.synchronize(saved.sources, snapshots)
                mutable.update { it.copy(sources = saved.sources, sourceTask = SourceTask.IDLE, guideSaveRevision = it.guideSaveRevision + 1) }
                refreshGuide(id)
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { if (generation == token) mutable.update { it.copy(sourceTask = SourceTask.IDLE,
                guideSaveError = (e as? SourceFailure)?.message ?: "Rehber adresi kaydedilemedi.") } }
        }
    }
    fun showKind(kind: MediaKind) {
        clearLibraryQuery()
        mutable.update { it.copy(kind = kind, onlyFavorites = false, category = "Tümü", focusedId = null, route = Route.LIBRARY) }
    }
    fun search() { mutable.update { it.copy(route = Route.SEARCH, searchFocusedId = null) } }
    fun searchFocus(id: String) { mutable.update { it.copy(searchFocusedId = id) } }
    fun searchQuery(value: String) {
        mutable.update { it.copy(searchQuery = value, searchFocusedId = null) }
        updateSearch()
    }
    fun searchKind(value: MediaKind?) {
        mutable.update { it.copy(searchKind = value, searchFocusedId = null) }
        updateSearch()
    }
    private fun resetSearchIndex() {
        searchGeneration++; searchJob?.cancel(); searchIndex.invalidate()
        librarySearchGeneration++; librarySearchJob?.cancel()
    }
    private suspend fun indexFor(catalog: Catalog): TitleIndex = searchIndex.get(catalog)
    private fun updateSearch(clearResults: Boolean = true) {
        val token = ++searchGeneration
        searchJob?.cancel()
        val s = state.value
        if (TitleIndex.normalize(s.searchQuery).isEmpty()) {
            mutable.update { it.copy(searchResults = emptyList(), searching = false) }
            return
        }
        mutable.update { it.copy(searching = true, searchResults = if (clearResults) emptyList() else it.searchResults) }
        searchJob = viewModelScope.launch {
            val index = indexFor(s.allCatalog)
            val results = withContext(Dispatchers.Default) {
                val context = currentCoroutineContext()
                index.find(s.searchQuery, s.searchKind) { context.ensureActive() }.filter { s.searchSourceId == null || it.sourceId == s.searchSourceId }
            }
            if (token == searchGeneration) {
                mutable.update { it.copy(searchResults = results, searching = false) }
            }
        }
    }
    private fun clearLibraryQuery() {
        librarySearchGeneration++; librarySearchJob?.cancel()
        mutable.update { it.copy(query = "", libraryMatches = emptySet(), librarySearching = false) }
    }
    private fun updateLibrarySearch(clearResults: Boolean = true) {
        val token = ++librarySearchGeneration
        librarySearchJob?.cancel()
        val s = state.value
        if (s.query.isBlank()) {
            mutable.update { it.copy(libraryMatches = emptySet(), librarySearching = false) }
            return
        }
        mutable.update { it.copy(librarySearching = true, libraryMatches = if (clearResults) emptySet() else it.libraryMatches) }
        librarySearchJob = viewModelScope.launch {
            val index = indexFor(s.allCatalog)
            val ids = withContext(Dispatchers.Default) {
                val context = currentCoroutineContext()
                index.find(s.query) { context.ensureActive() }.mapTo(HashSet()) { it.id }
            }
            if (token == librarySearchGeneration) mutable.update { it.copy(libraryMatches = ids, librarySearching = false) }
        }
    }
    fun favorites() { clearLibraryQuery(); mutable.update { it.copy(onlyFavorites = true, category = "Tümü", focusedId = null, route = Route.LIBRARY) } }
    fun category(value: String) { mutable.update { it.copy(category = value, focusedId = null) } }
    fun query(value: String) { mutable.update { it.copy(query = value, focusedId = null) }; updateLibrarySearch() }
    fun onlyFavorites() { mutable.update { it.copy(onlyFavorites = !it.onlyFavorites, focusedId = null) } }
    fun season(value: Int) { mutable.update { it.copy(season = value) } }
    fun reducedMotion() { store.reducedMotion = !state.value.reducedMotion; mutable.update { it.copy(reducedMotion = store.reducedMotion) } }
    fun startupSound() { store.startupSound = !state.value.startupSound; mutable.update { it.copy(startupSound = store.startupSound) } }
    private fun storedSorts(): Map<MediaKind, LibrarySort> = listOf(MediaKind.MOVIE, MediaKind.SERIES).associateWith(store::librarySort)
    private val libraryOrders = LibraryOrderCache()
    /** Films or series in a Sırala order; sorted once per catalogue and choice. */
    fun libraryOrder(catalog: Catalog, kind: MediaKind, sort: LibrarySort): List<MediaEntry> = libraryOrders.sorted(catalog, kind, sort)
    fun librarySort(kind: MediaKind, sort: LibrarySort) {
        store.librarySort(kind, sort)
        val catalog = state.value.catalog
        viewModelScope.launch {
            // Sort away from the main thread first; the list then changes in one step.
            if (sort != LibrarySort.DEFAULT) withContext(Dispatchers.Default) { libraryOrders.sorted(catalog, kind, sort) }
            mutable.update { it.copy(librarySorts = it.librarySorts + (kind to sort), focusedId = null) }
        }
    }
    /** Sorts a newly published catalogue for the saved choices before Library asks for it. */
    private fun prepareLibraryOrders() {
        val s = state.value
        val pending = s.librarySorts.filterValues { it != LibrarySort.DEFAULT }
        if (pending.isNotEmpty()) viewModelScope.launch(Dispatchers.Default) { pending.forEach { (kind, sort) -> libraryOrders.sorted(s.catalog, kind, sort) } }
    }
    fun autoNextEpisode() { store.autoNextEpisode = !state.value.autoNextEpisode; mutable.update { it.copy(autoNextEpisode = store.autoNextEpisode) } }

    fun favoriteTarget(entry: MediaEntry): MediaEntry? {
        val s = state.value
        return FavoriteTargets.resolve(entry, s.allCatalog, s.selected, s.episodes, s.history)
    }
    private fun repairEpisodeFavorites() {
        val s = state.value
        val repaired = FavoriteTargets.migrate(s.favoriteIds, s.allCatalog, s.selected, s.episodes, s.history)
        if (repaired != s.favoriteIds) {
            s.sources.forEach { source -> store.favorites(source.id, repaired.mapNotNull { ContentRef.parse(it) }
                .filter { it.sourceId == source.id }.mapTo(HashSet()) { it.localId }) }
            mutable.update { it.copy(favoriteIds = repaired) }
        }
    }
    fun toggleFavorite(entry: MediaEntry) {
        val target = favoriteTarget(entry) ?: return
        val source = state.value.sourceFor(target) ?: return
        repairEpisodeFavorites()
        val local = target.withoutSource().id
        val next = store.favorites(source.id).toMutableSet().apply { if (!add(local)) remove(local) }.toSet()
        store.favorites(source.id, next)
        mutable.update { it.copy(favoriteIds = SourceLibrary.favorites(it.sources, store::favorites)) }
    }

    fun open(entry: MediaEntry, resume: WatchRecord? = null) {
        if (entry.adult || state.value.sourceFor(entry) == null) return
        mutable.update { it.copy(focusedId = entry.id) }
        if (entry.kind == MediaKind.LIVE) { play(entry); return }
        detailJob?.cancel()
        mutable.update { it.copy(route = Route.DETAIL, selected = entry, episodes = emptyList(), detailLoading = true,
            detailError = null, detailReturn = when (it.route) { Route.HOME -> Route.HOME; Route.SEARCH -> Route.SEARCH; else -> Route.LIBRARY }) }
        loadDetail(entry, resume)
    }

    fun retryDetail() { state.value.selected?.let { loadDetail(it) } }
    private fun loadDetail(entry: MediaEntry, resume: WatchRecord? = null) {
        val source = state.value.sourceFor(entry) ?: return
        detailJob?.cancel()
        mutable.update { it.copy(detailLoading = true, detailError = null) }
        detailJob = viewModelScope.launch {
            try {
                if (entry.kind == MediaKind.SERIES) {
                    val (rawShow, rawEpisodes) = repository.episodes(source, entry.withoutSource(), snapshots[source.id]?.catalog ?: Catalog(emptyList()))
                    val show = rawShow.inSource(source.id)
                    val episodes = rawEpisodes.map { it.inSource(source.id) }
                    mutable.update { it.copy(selected = show, episodes = episodes, season = episodes.firstOrNull()?.season ?: 1, detailLoading = false) }
                    repairEpisodeFavorites()
                    if (resume != null) {
                        val episode = episodes.firstOrNull { it.id == resume.id }
                        // A finished episode goes on with the next one when its order is certain; otherwise the
                        // detail opens on the next episode's season and the viewer starts it (as after autoplay).
                        val next = if (episode != null && resume.progress.completed) EpisodeSequence.next(source, episode, episodes) else null
                        when {
                            episode == null -> mutable.update { it.copy(detailError = "İzlediğin bölüm artık bu kaynakta bulunamıyor. Başka bir bölüm seçebilirsin.") }
                            !resume.progress.completed -> { mutable.update { it.copy(season = episode.season) }; if (stillOn(show)) play(episode) }
                            next == null -> mutable.update { it.copy(season = episode.season) }
                            next.automaticAllowed -> { mutable.update { it.copy(season = next.entry.season) }; if (stillOn(show)) play(next.entry) }
                            else -> mutable.update { it.copy(season = next.entry.season) }
                        }
                    }
                } else {
                    val movie = repository.movie(source, entry.withoutSource()).inSource(source.id)
                    mutable.update { it.copy(selected = movie, detailLoading = false) }
                    if (resume != null && stillOn(movie)) play(movie)
                }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { mutable.update { it.copy(detailLoading = false, detailError = (e as? SourceFailure)?.message ?: "Ayrıntılar alınamadı.") } }
        }
    }

    /** A resume opened from a card plays only while its detail is still the screen; leaving during the load cancels it. */
    private fun stillOn(entry: MediaEntry) = state.value.route == Route.DETAIL && state.value.selected?.id == entry.id

    fun play(entry: MediaEntry, restart: Boolean = false) {
        if (entry.adult || state.value.sourceFor(entry) == null) return
        val source = state.value.sourceFor(entry) ?: return
        try {
            val uri = source.streamUrl(entry)
            val position = if (restart || entry.kind == MediaKind.LIVE) 0 else store.progress(source.id, entry.withoutSource().id)?.resumeMs ?: 0
            mutable.update { it.copy(route = Route.PLAYER, playing = PlayingMedia(entry, uri, position),
                guideChannelId = if (entry.kind == MediaKind.LIVE && it.route == Route.PLAYER && it.playerReturn == Route.GUIDE) entry.id else it.guideChannelId,
                playerReturn = if (it.route == Route.PLAYER) it.playerReturn else it.route,
                season = if (entry.kind == MediaKind.EPISODE && it.episodes.any { episode -> episode.id == entry.id }) entry.season else it.season,
                error = null) }
        } catch (e: SourceFailure) { mutable.update { it.copy(detailError = e.message) } }
    }

    fun nextEpisodeChoice(): NextEpisodeChoice? {
        val s = state.value
        val entry = s.playing?.entry ?: return null
        return EpisodeSequence.next(s.sourceFor(entry) ?: return null, entry, s.episodes)
    }
    fun nextEpisode(): MediaEntry? = nextEpisodeChoice()?.entry

    fun playNextEpisode(sourceId: String, currentId: String, nextId: String, automatic: Boolean = false): Boolean {
        val s = state.value
        if (s.route != Route.PLAYER || s.playing?.entry?.sourceId != sourceId || s.playing?.entry?.id != currentId) return false
        val next = nextEpisodeChoice() ?: return false
        if (next.entry.id != nextId || (automatic && (!s.autoNextEpisode || !next.automaticAllowed))) return false
        play(next.entry, restart = true)
        return state.value.playing?.entry?.id == nextId
    }

    fun changeChannel(direction: Int) {
        val s = state.value
        val context = if (s.playerReturn == Route.SEARCH) filtered(s.allCatalog, s.searchSourceId) else s.catalog
        val channels = context.items.filter { it.kind == MediaKind.LIVE && !it.adult &&
            (s.playerReturn != Route.LIBRARY || s.category == "Tümü" || it.category == s.category) }
        if (channels.isEmpty()) return
        val index = channels.indexOfFirst { it.id == s.playing?.entry?.id }
        play(channels[Math.floorMod(index + direction, channels.size)])
    }

    fun progress(entry: MediaEntry): PlaybackMark? = state.value.sourceFor(entry)?.let { store.progress(it.id, entry.withoutSource().id) }
    fun recordViewed(entry: MediaEntry) {
        val s = state.value
        val source = s.sourceFor(entry) ?: return
        val local = entry.withoutSource()
        val mark = store.progress(source.id, local.id)
        val parent = if (entry.kind == MediaKind.EPISODE) s.selected?.takeIf {
            it.kind == MediaKind.SERIES && it.sourceId == source.id && s.episodes.any { episode -> episode.id == entry.id }
        }?.withoutSource()?.id else null
        val record = WatchRecord(local.id, local.kind, parent, local.season, local.episode,
            mark?.positionMs ?: 0, mark?.durationMs ?: 0, System.currentTimeMillis())
        store.history(source.id, WatchHistory.upsert(store.history(source.id), record))
        mutable.update { it.copy(history = SourceLibrary.history(it.sources, store::history)) }
    }
    fun clearHistory(sourceId: String? = null) {
        state.value.sources.filter { sourceId == null || it.id == sourceId }.forEach { store.clearHistory(it.id) }
        homeFocusKey = null
        mutable.update { it.copy(history = SourceLibrary.history(it.sources, store::history), revision = it.revision + 1) }
    }
    fun saveProgress(entry: MediaEntry, position: Long, duration: Long) {
        val source = state.value.sourceFor(entry) ?: return
        if (entry.kind != MediaKind.LIVE) {
            val local = entry.withoutSource().id
            store.saveProgress(source.id, local, position, duration)
            val records = store.history(source.id)
            val record = records.firstOrNull { it.id == local } ?: return
            store.history(source.id, WatchHistory.upsert(records, record.copy(positionMs = position.coerceAtLeast(0),
                durationMs = duration.coerceAtLeast(0), updatedAt = System.currentTimeMillis())))
            mutable.update { it.copy(history = SourceLibrary.history(it.sources, store::history)) }
        }
    }

    fun back() {
        when (state.value.route) {
            Route.LOADING -> cancelImport()
            Route.DETAIL -> { detailJob?.cancel(); mutable.update { it.copy(route = it.detailReturn, detailLoading = false) } }
            Route.PLAYER -> mutable.update { it.copy(route = it.playerReturn, revision = it.revision + 1) }
            Route.SETUP -> if (state.value.sources.isNotEmpty()) mutable.update { it.copy(route = it.setupReturn, error = null, draft = SourceDraft(), editingSourceId = null) }
            Route.SOURCES -> mutable.update { it.copy(route = it.sourcesReturn) }
            Route.LIBRARY, Route.SEARCH, Route.GUIDE -> home()
            else -> Unit
        }
    }

    fun manageSources() {
        mutable.update { it.copy(route = Route.SOURCES, sourceManagerFocus = "add-source", sourcesReturn = when (it.route) {
            Route.HOME, Route.LIBRARY, Route.SEARCH, Route.GUIDE -> it.route
            else -> Route.HOME
        }) }
    }
    fun addSource() {
        if (state.value.sourceTask != SourceTask.IDLE) return
        mutable.update { it.copy(route = Route.SETUP, editingSourceId = null, draft = SourceDraft(), error = null, sourceManagerFocus = "add-source",
            setupReturn = if (it.route == Route.SOURCES) Route.SOURCES else Route.HOME) }
    }
    fun editSource(sourceId: String? = state.value.source?.id) {
        if (state.value.sourceTask != SourceTask.IDLE) return
        val source = state.value.sources.firstOrNull { it.id == sourceId } ?: return
        mutable.update { it.copy(route = Route.SETUP, editingSourceId = source.id, error = null, sourceManagerFocus = "edit-source:${source.id}",
            setupReturn = if (it.route == Route.SOURCES) Route.SOURCES else Route.HOME,
            draft = SourceDraft(source.name, source.type, source.address, source.username, source.password, source.epgUrl)) }
    }

    fun removeSource(sourceId: String? = state.value.source?.id) {
        if (state.value.sourceTask == SourceTask.REMOVING) return
        val previous = state.value
        val owner = previous.sources.firstOrNull { it.id == sourceId } ?: run {
            if (previous.sourceTask == SourceTask.IDLE) mutable.update { it.copy(route = Route.SETUP) }
            return
        }
        val token = ++generation
        val previousJob = loadJob
        previousJob?.cancel(); detailJob?.cancel(); filterGeneration++; filterJob?.cancel()
        guides.forget(owner.id)
        resetSearchIndex()
        // Detach only this owner before player disposal: its final progress callback cannot recreate data.
        mutable.value = previous.copy(route = Route.LOADING, sourceTask = SourceTask.REMOVING,
            sources = previous.sources.filterNot { it.id == owner.id }, playing = null,
            guideStatus = previous.guideStatus - owner.id,
            progressLabel = "Kaynak kaldırılıyor…", refreshing = false,
            sourceStatus = previous.sourceStatus.mapValues { (_, value) -> value.copy(refreshing = false) })
        loadJob = viewModelScope.launch {
            try {
                previousJob?.join()
                val saved = transactions.write {
                    val latest = readRegistry()
                    // Includes an edit that finished its accepted write after cancellation.
                    artwork.removeSource(owner.id)
                    catalogStore.remove(latest.sources.firstOrNull { it.id == owner.id } ?: owner)
                    epgStore.remove(owner.id)
                    latest.without(owner.id).also { store.saveSources(it) }
                }
                currentCoroutineContext().ensureActive()
                if (generation != token) return@launch
                registry = saved; snapshots.remove(owner.id)
                publishLibrary()
                homeFocusKey = null
                mutable.update { it.copy(route = if (saved.sources.isEmpty()) Route.SETUP else
                    if (previous.route == Route.SOURCES) Route.SOURCES else Route.HOME,
                    sourceTask = SourceTask.IDLE, editingSourceId = null, draft = SourceDraft(),
                    selected = null, episodes = emptyList(), error = null,
                    sourceManagerFocus = saved.sources.getOrNull(previous.sources.indexOf(owner).coerceAtMost(saved.sources.lastIndex).coerceAtLeast(0))
                        ?.let { source -> "remove-source:${source.id}" } ?: "add-source",
                    catalogNotice = CatalogNotice("Kaynak kaldırıldı.", CatalogOperation.SOURCE_REMOVAL)) }
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) {
                guides.synchronize(previous.sources, snapshots)
                runCatching { transactions.read { artwork.synchronizeSources(store.loadSources().sources.map { it.id }) } }
                if (generation == token) mutable.value = previous.copy(route = if (previous.route == Route.SOURCES) Route.SOURCES else Route.HOME,
                    sourceTask = SourceTask.IDLE, playing = null, refreshing = false,
                    sourceStatus = previous.sourceStatus.mapValues { (_, value) -> value.copy(refreshing = false) },
                    error = "Kaynak kaldırılamadı. Tekrar dene.", catalogNotice = CatalogNotice("Kaynak kaldırılamadı. Tekrar dene.", CatalogOperation.SOURCE_REMOVAL, needsAttention = true))
            }
        }
    }
}
