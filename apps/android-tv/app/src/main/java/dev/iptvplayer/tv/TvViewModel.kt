package dev.iptvplayer.tv

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.mutableStateMapOf
import dev.iptvplayer.tv.data.*
import dev.iptvplayer.tv.ui.NextResult
import dev.iptvplayer.tv.ui.appLocale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/**
 * LOCKED is the lock screen (pin.md §3.9). It is only ever [TvState.screen], never [TvState.route]: it covers the route,
 * which goes on underneath (a restore finishing, the player waiting), so nothing that sets a route can uncover it.
 */
enum class Route { SETUP, LOADING, HOME, LIBRARY, GUIDE, SEARCH, DETAIL, PLAYER, SOURCES, RECOVERY, LOCKED }
enum class SourceTask { RESTORING, IMPORTING, SAVING, REMOVING, RESETTING, IDLE }
/** The source form; a null [name] is the untouched default name, which the setup screen shows in the app's language. */
data class SourceDraft(val name: String? = null, val type: SourceType = SourceType.XTREAM,
    val address: String = "", val username: String = "", val password: String = "", val epgUrl: String = "") {
    override fun toString() = "SourceDraft(type=$type, credentials=REDACTED)"
}
/** How the player treats what it plays (gap plan T4). ARCHIVE, a past programme, arrives with catch-up (S24). */
enum class PlaybackMode { LIVE, VOD, ARCHIVE;
    companion object { fun of(kind: MediaKind) = if (kind == MediaKind.LIVE) LIVE else VOD }
}
/** [paused]: the player opens it paused, as the viewer had left it when the lock screen covered it (pin.md §3.9). */
data class PlayingMedia(val entry: MediaEntry, val url: String, val startMs: Long, val mode: PlaybackMode, val paused: Boolean = false) {
    override fun toString() = "PlayingMedia(id=${entry.id}, mode=$mode, url=REDACTED)"
    /**
     * This media as the player that comes back after the lock screen opens it: a film or episode where the player last
     * saved it ([mark]), its last minutes too (the credits, the next episode's card), and [paused] as the viewer left it.
     * A live channel joins its live edge again.
     */
    fun resumed(mark: PlaybackMark?, paused: Boolean): PlayingMedia =
        copy(startMs = if (mode == PlaybackMode.LIVE) startMs else mark?.positionMs ?: startMs, paused = paused)
}
/** [notices] read as one paragraph: the list's warnings, or how its last refresh went. */
data class SourceStatus(val count: Int = 0, val updatedAt: Long = 0, val refreshing: Boolean = false,
    val notices: List<UiText> = emptyList(), val failed: Boolean = false)
/**
 * Success belongs beside the operation; actionable failures remain visible in the catalog too. START_OVER is "PIN'i
 * unuttum → Her şeyi sıfırla", told on the setup screen it ends on.
 */
enum class CatalogOperation { REFRESH, SOURCE_REMOVAL, SOURCE_FILTER, START_OVER }
data class CatalogNotice(val message: UiText, val operation: CatalogOperation, val needsAttention: Boolean = false)
/** Its texts are [UiText]s: the state holds no ready sentences; the screens resolve them in the app's language. */
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
    val guideSaveError: UiText? = null,
    val editingSourceId: String? = null,
    val setupReturn: Route = Route.HOME,
    val sourcesReturn: Route = Route.HOME,
    val sourceManagerFocus: String = "add-source",
    val allCatalog: Catalog = Catalog(emptyList()),
    val catalog: Catalog = Catalog(emptyList()),
    val kind: MediaKind = MediaKind.LIVE,
    val category: String = CategoryFilter.ALL,
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
    val progressLabel: UiText = UiText.Res(R.string.progress_preparing),
    val sourceTask: SourceTask = SourceTask.RESTORING,
    /** The restore opening the app (D069): shown as the NEXTV cover. Retries and resets keep the operation card. */
    val restoringAtLaunch: Boolean = true,
    /** The hero's İzle is fetching a series' episodes for this title (D071); its label says "Açılıyor…". */
    val heroStarting: String? = null,
    val refreshing: Boolean = false,
    val catalogUpdatedAt: Long = 0,
    val catalogNotice: CatalogNotice? = null,
    val recoveryCanReset: Boolean = false,
    val error: UiText? = null,
    val detailError: UiText? = null,
    val reducedMotion: Boolean = false,
    val startupSound: Boolean = true,
    /** D068: categories hidden in Ayarlar and the adult-name filter; [categoryChoices] lists every category for that page. */
    val hiddenCategories: Set<String> = emptySet(),
    val hideAdultCategories: Boolean = true,
    val categoryChoices: Map<MediaKind, List<CategoryChoice>> = emptyMap(),
    val autoNextEpisode: Boolean = true,
    /** The Sırala choice of the film and series lists, kept on the device. */
    val librarySorts: Map<MediaKind, LibrarySort> = emptyMap(),
    /**
     * The parental PIN, the PIN pad on screen, what a PIN verified in Ayarlar or for the sources allows, and the lock
     * screen (pin.md §7).
     */
    val parental: ParentalUi = ParentalUi(),
    /**
     * What the lock rules make of the published library: the titles, category rows and sources that ask for the PIN,
     * the titles locked on their own, and those PINs opened in this session.
     */
    val locks: ParentalLocks = ParentalLocks(),
    /**
     * What the access gate has to say in the corner: a title gone by the time its PIN was typed, a lock it cannot read,
     * a lock from the quick menu that was not saved.
     */
    val accessNotice: UiText? = null,
    val revision: Int = 0
) {
    /**
     * What is on screen: the [route], or the lock screen over it while the app is locked, once the sources are open. The
     * recovery screen stays, as it can only remove them.
     */
    val screen: Route get() = if (parental.appLocked && route != Route.LOADING && route != Route.RECOVERY) Route.LOCKED else route
    // Display/default selection only. Playback and personal data must use sourceFor(entry).
    val source: SourceConfig? get() = sources.firstOrNull { it.id == sourceFilterId } ?: sources.firstOrNull()
    fun sourceFor(entry: MediaEntry): SourceConfig? = sources.firstOrNull { it.id == entry.sourceId }
    /** How Kategorileri yönet shows the category row [name] of [kind]; a hidden row keeps a lock it had under it. */
    fun categoryAccess(kind: MediaKind, name: String): CategoryAccess = when (CategoryVisibility.key(kind, name)) {
        in hiddenCategories -> CategoryAccess.HIDDEN
        in locks.categories -> CategoryAccess.LOCKED
        else -> CategoryAccess.SHOWN
    }
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
    private val guides = GuideController(viewModelScope, epgStore, store, catalogStore, { guideZone() }, { LocaleController.guideLanguages })
    private val parental = ParentalController(viewModelScope,
        ParentalGuard(ProtectionStore(application), AndroidPinClock(application.contentResolver)), SystemClock::elapsedRealtime,
        unlocked = ::unlocked, released = ::released,
        // New lock rules change what the library shows locked; a restore or an import publishes it on its own.
        policyChanged = { if (state.value.sourceTask == SourceTask.IDLE) viewModelScope.launch { publishLibrary() } }) { followUp ->
        when (followUp) {
            AdminFollowUp.ShowAdult -> setHideAdult(false)
            is AdminFollowUp.LockCategory -> setCategoryAccess(followUp.kind, followUp.name, CategoryAccess.LOCKED)
            // The PIN for the sources was just right: what asked for it goes on, on the state as it is now.
            AdminFollowUp.OpenSources -> openSources()
            AdminFollowUp.AddSource -> if (state.value.sourceTask == SourceTask.IDLE) newSource()
            is AdminFollowUp.EditSource -> editable(followUp.id)?.let(::edit)
            is AdminFollowUp.RemoveSource -> removable(followUp.id)?.let(::remove)
        }
    }
    /** The locked category rows of the last publish and the rules each rests on; a renumbered row follows them. */
    private var lockedRows: Map<String, Set<AccessScope.Category>> = emptyMap()
    /** The locked titles of the last publish by the scopes a PIN opens for each (ParentalView.groups). */
    private var lockGroups: Map<Set<AccessScope>, Set<String>> = emptyMap()
    /**
     * One publish at a time, each reading the sources, hidden categories and locks as they are when its turn comes.
     * [publishCalls] counts the calls; every call up to [publishedThrough] is on screen.
     */
    private val publishing = Mutex()
    private var publishCalls = 0
    private var publishedThrough = 0

    init {
        viewModelScope.launch { guides.state.collect { value -> mutable.update { it.copy(guideStatus = value.statuses, guideRevision = value.revision) } } }
        // A PIN typed for a locked title, or the session ending, changes which titles the screens mark as locked. The
        // lock screen giving way brings back the player it covered.
        viewModelScope.launch {
            var locked = false
            parental.state.collect { value ->
                mutable.update { it.copy(parental = value, locks = opened(it.locks)) }
                if (locked && !value.appLocked) lockLifted()
                locked = value.appLocked
            }
        }
        // Outside the sources' screens what a PIN opened there ends (pin.md §3.8), whatever moved the route.
        viewModelScope.launch { state.map { it.route }.distinctUntilChanged().collect { if (it !in SourceScreens.routes) parental.leftSources() } }
        // The restore reads the PIN and its locks first.
        restore(atLaunch = true)
    }

    /**
     * NEXTV came on screen (MainActivity.onStart) or left it (onStop: Home, another app, standby). What may live only
     * while the viewer sees the app hangs here as it arrives (gap plan T3, T6): what PINs opened locks again, a PIN
     * verified in Ayarlar ends and an open PIN pad closes, Settings with them, and a locked title in the player leaves it
     * as Back would (its progress is already saved), so it does not play on behind the TV's standby or come back without
     * its PIN (pin.md §3.9). The sources' screens are left too while a PIN guards them, as they show a source's details
     * and password (§3.8); an import or a removal under way finishes. With "Açılışta PIN iste" the lock screen is up on
     * return; the player it covers stays the route, and starts again once the PIN is right. A configuration change
     * (MainActivity recreated) is neither. The foreground timer and a local transfer join later.
     */
    fun appStarted() {
        // Back without the lock screen, the player went on as it was: how it stood as the app left no longer counts.
        if (!parental.state.value.appLocked) stoppedPaused = false
    }
    fun appStopped() {
        parental.stopped()
        if (parental.state.value.sourcesGuarded) leaveSources()
        val s = state.value
        val playing = s.playing?.entry?.takeIf { s.route == Route.PLAYER } ?: return
        val target = AccessTarget.Play(playing.id)
        // A title the gate cannot find now (a refresh dropped it) is not known to be free of locks: it leaves too
        // (pin.md §0, failures fall on the closed side).
        if (AccessGate.find(target, gateSnapshot(target))?.opens != true) back()
    }

    /** Leaves the sources' screens as Back would, the setup form's draft with them (SourceScreens.left). */
    private fun leaveSources() = mutable.update(SourceScreens::left)

    /**
     * True when source management waits for the PIN (pin.md §3.8): the pad opened, and [then] runs once it is right.
     * Asked from outside the sources' screens, a grant they kept ends first, so one left over never opens them again.
     */
    private fun asksForSources(then: AdminFollowUp): Boolean {
        if (state.value.route !in SourceScreens.routes) parental.leftSources()
        return parental.requestSources(then)
    }

    /** Whether the viewer had paused the player as the app last left the screen (PlayerScreen's ON_STOP). */
    private var stoppedPaused = false
    /** The player as the app leaves the screen: [paused] says whether the viewer had paused it. */
    fun playerStopped(paused: Boolean) { stoppedPaused = paused }

    /**
     * The lock screen gave way (pin.md §3.9): the player it covered comes back, a film or episode where it stopped and
     * paused if the viewer had paused it. Only while its title opens without asking: a lock screen that came up in the
     * app (a record that could not be read) ended what PINs opened, and a title one opened leaves the player as Back would.
     */
    private fun lockLifted() {
        val paused = stoppedPaused.also { stoppedPaused = false }
        val s = state.value
        val media = s.playing?.takeIf { s.route == Route.PLAYER } ?: return
        val target = AccessTarget.Play(media.entry.id)
        if (AccessGate.find(target, gateSnapshot(target))?.opens != true) { back(); return }
        val mark = if (media.mode == PlaybackMode.LIVE) null else progress(media.entry)
        mutable.update { it.copy(playing = media.resumed(mark, paused)) }
    }

    /** Yeniden dene on the lock screen of a protection record that could not be read: it is read again. */
    fun retryProtection() = parental.retry()

    /** Migration is serialized with accepted writes from a previous ViewModel. */
    private fun readRegistry(): SourceRegistry {
        val current = store.loadSources()
        if (!store.hasSourceRegistry) {
            catalogStore.migrateLegacy(current.sources.singleOrNull())
            store.saveSources(current)
        }
        return current
    }

    /** [notice]: what led here (a start-over that failed), kept through the refresh this may start. */
    fun restore(atLaunch: Boolean = false, notice: CatalogNotice? = null) {
        if (state.value.sourceTask in setOf(SourceTask.SAVING, SourceTask.REMOVING, SourceTask.RESETTING)) return
        val token = ++generation
        loadJob?.cancel()
        guides.forget()
        mutable.update { it.copy(route = Route.LOADING, sourceTask = SourceTask.RESTORING, restoringAtLaunch = atLaunch,
            progressLabel = UiText.Res(R.string.progress_checking_library), error = null) }
        loadJob = viewModelScope.launch {
            var observedStamp: String? = null
            try {
                // The locks before the library: until they are read, the gate opens nothing without the PIN. Opening the
                // app, "Açılışta PIN iste" puts the lock screen up, to show once the sources are open.
                parental.refresh(launching = atLaunch)
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
                    sourceTask = SourceTask.IDLE, recoveryCanReset = false, catalogNotice = notice ?: it.catalogNotice) }
                // The screens no longer offer a source selector (D060): an older saved choice must not keep hiding sources.
                if (saved.filterId != null) sourceFilter(null)
                val stale = saved.sources.filter { (cached[it.id]?.updatedAt ?: 0) < System.currentTimeMillis() - 6 * 60 * 60 * 1000L }
                if (stale.isNotEmpty()) refreshSources(stale, token, keep = notice)
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) {
                if (generation == token) {
                    recoveryStamp = observedStamp.takeIf { (e as? SourceFailure)?.reason == FailureReason.STORED_SOURCES_UNAVAILABLE }
                    mutable.update { it.copy(route = Route.RECOVERY, sourceTask = SourceTask.IDLE,
                        playing = null, recoveryCanReset = recoveryStamp != null,
                        error = UiText.Res(R.string.error_library_unavailable)) }
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
            progressLabel = UiText.Res(R.string.progress_resetting_sources)) }
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
                mutable.value = startedOver()
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) {
                if (generation == token) mutable.update { it.copy(route = Route.RECOVERY, sourceTask = SourceTask.IDLE,
                    error = UiText.Res(R.string.error_reset_failed), recoveryCanReset = true) }
            }
        }
    }

    /** A TV without sources, on the setup screen; the TV's own settings and the PIN's status stay. */
    private fun startedOver(notice: CatalogNotice? = null) = TvState(route = Route.SETUP, sourceTask = SourceTask.IDLE,
        reducedMotion = store.reducedMotion, startupSound = store.startupSound, autoNextEpisode = store.autoNextEpisode,
        guideZoneId = store.guideZoneId, librarySorts = storedSorts(), hideAdultCategories = store.hideAdultCategories,
        parental = parental.state.value, catalogNotice = notice)

    /**
     * "PIN'i unuttum → Her şeyi sıfırla" (pin.md §3.11), from the PIN pad. No server keeps the PIN, so this TV starts
     * over: the sources, their lists and guides, favourites, history and progress go and the adult filter is turned back
     * on; ParentalController.startOver removes the PIN record only after that. Setup opens, or, when not every source
     * could be removed, the library with what is left. The lock screen offers it as well, also for a record that cannot
     * be read (Sıfırla…).
     */
    fun forgotPinReset() {
        val asked = parental.state.value.let { it.prompt?.canForget == true || it.appLocked }
        if (!asked || state.value.sourceTask != SourceTask.IDLE) return
        val token = ++generation
        val previousJob = loadJob
        previousJob?.cancel(); detailJob?.cancel(); filterGeneration++; filterJob?.cancel(); resetSearchIndex()
        guides.forget()
        parental.relock()
        homeFocusKey = null
        mutable.update { it.copy(route = Route.LOADING, sourceTask = SourceTask.RESETTING, restoringAtLaunch = false,
            playing = null, refreshing = false, progressLabel = UiText.Res(R.string.progress_resetting_everything)) }
        loadJob = viewModelScope.launch {
            previousJob?.join()
            val result = parental.startOver {
                transactions.write {
                    artwork.clear()
                    catalogStore.clear()
                    epgStore.clear()
                    store.startOver()
                }
                registry = SourceRegistry(); snapshots.clear()
            }
            currentCoroutineContext().ensureActive()
            if (generation != token) return@launch
            val notice = CatalogNotice(UiText.Res(result.notice), CatalogOperation.START_OVER, result.needsAttention)
            if (result != StartOver.FAILED) mutable.value = startedOver(notice)
            else {
                mutable.update { it.copy(sourceTask = SourceTask.IDLE) }
                restore(notice = notice)
            }
        }
    }

    private fun filtered(catalog: Catalog, id: String?): Catalog = if (id == null) catalog else Catalog(
        catalog.items.filter { it.sourceId == id },
        catalog.m3uEpisodes.filterKeys { ContentRef.parse(it)?.sourceId == id }, catalog.warnings, categoryOrder = catalog.categoryOrder)

    /**
     * The library the screens read: every source's list, without hidden categories (D068) and titles whose lock cannot
     * be read, with what the lock rules lock (ParentalProjection). A locked category row's new identity gets its rule,
     * and locks before the record holds it. A call returns once a publish that read its inputs, or newer ones, is on
     * screen. One whose inputs changed while it computed (a lock saved, then its hidden row shown) leaves the screens as
     * they are and the next turn reads them again, so an older library never lands over a newer one, even when the call
     * that changed them is cancelled with its job.
     */
    private suspend fun publishLibrary() {
        val call = ++publishCalls
        while (publishedThrough < call) publishing.withLock { if (publishedThrough < call) publish() }
    }

    private suspend fun publish() {
        val upTo = publishCalls
        val saved = registry
        val raw = snapshots.mapValues { it.value.catalog }
        val hidden = store.hiddenCategories
        val hideAdult = store.hideAdultCategories
        val policy = parental.policy
        val before = lockedRows
        val (view, choices) = withContext(Dispatchers.Default) {
            val combined = SourceLibrary.combine(saved.sources, raw)
            ParentalProjection.project(saved.sources, CategoryVisibility.apply(combined, hidden, hideAdult), policy, before) to
                CategoryVisibility.choices(combined)
        }
        if (upTo != publishCalls) return
        lockedRows = view.rows
        lockGroups = view.groups
        // Before the library goes out, so the gate already reads the rules being added.
        parental.addLocks(view.missing)
        val all = view.catalog
        val favorites = SourceLibrary.favorites(saved.sources, store::favorites)
        val history = SourceLibrary.history(saved.sources, store::history)
        guides.synchronize(saved.sources, snapshots)
        mutable.update { old ->
            val filter = registry.filterId?.takeIf { id -> saved.sources.any { it.id == id } }
            val catalog = filtered(all, filter)
            old.copy(sources = saved.sources, sourceFilterId = filter, allCatalog = all, catalog = catalog, locks = opened(view.locks),
                hiddenCategories = hidden, hideAdultCategories = hideAdult, categoryChoices = choices,
                favoriteIds = favorites, history = history,
                searchSourceId = old.searchSourceId?.takeIf { id -> saved.sources.any { it.id == id } },
                sourceStatus = saved.sources.associate { source ->
                    val snapshot = snapshots[source.id]
                    source.id to (old.sourceStatus[source.id] ?: SourceStatus()).copy(
                        count = snapshot?.catalog?.items?.count { !it.adult && it.kind != MediaKind.EPISODE } ?: 0,
                        updatedAt = snapshot?.updatedAt ?: 0)
                }, catalogUpdatedAt = snapshots.values.maxOfOrNull { it.updatedAt } ?: 0,
                category = old.category.takeIf { category -> catalog.items.any { !it.adult &&
                    (if (old.onlyFavorites) it.id in favorites else it.kind == old.kind) && it.category == category } } ?: CategoryFilter.ALL)
        }
        publishedThrough = upTo
        // A detail the gate now refuses (its category hidden from Ayarlar, a refresh dropped the title) closes as Back
        // would, instead of keeping an Oynat and episodes that do nothing, like a card that is no longer shown. One that
        // now asks for the PIN stays: its Oynat asks. Under the lock screen, too, it is judged on the title's own locks.
        state.value.selected?.takeIf { state.value.route == Route.DETAIL }?.let { detail ->
            val target = AccessTarget.Open(detail.id)
            if (AccessGate.decide(target, gateSnapshot(target).copy(appLocked = false)) == GateResult.Deny) back()
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
    /**
     * [defaultName] is what an untouched name field saves and [unnamed] what one left empty saves; the setup screen
     * passes both in the app's language.
     */
    fun connectDraft(defaultName: String = "", unnamed: String = "") {
        if (state.value.route != Route.SETUP) return
        if (state.value.sourceTask != SourceTask.IDLE && state.value.sourceTask != SourceTask.IMPORTING) return
        // A form that added or edited a source opened with the PIN that guards them (pin.md §3.8); without it, it goes.
        if (state.value.sources.isNotEmpty() && !parental.state.value.mayManageSources) { leaveSources(); return }
        val draft = state.value.draft
        try {
            val source = SourceConfig(name = draft.name?.ifBlank { unnamed } ?: defaultName, type = draft.type, address = draft.address,
                username = draft.username, password = draft.password, epgUrl = draft.epgUrl).validated().let { value ->
                state.value.editingSourceId?.let { value.copy(id = it) } ?: value
            }
            if (state.value.sources.any { it.id != source.id && sameConnection(it, source) })
                throw SourceFailure(FailureReason.CONNECTION_ALREADY_ADDED)
            connect(source)
        } catch (e: SourceFailure) { mutable.update { it.copy(error = e.text) } }
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
                    throw SourceFailure(FailureReason.PARTIAL_IMPORT)
                val snapshot = CatalogSnapshot(catalog, System.currentTimeMillis())
                mutable.update { it.copy(sourceTask = SourceTask.SAVING, progressLabel = UiText.Res(R.string.progress_saving_library)) }
                val saved = transactions.write {
                    val latest = readRegistry()
                    if (latest.sources.any { it.id != source.id && sameConnection(it, source) })
                        throw SourceFailure(FailureReason.CONNECTION_ADDED_ELSEWHERE)
                    if (state.value.editingSourceId != null && latest.sources.none { it.id == source.id })
                        throw SourceFailure(FailureReason.SOURCE_NO_LONGER_SAVED)
                    // A successful import always has a durable snapshot before publishing the account.
                    catalogStore.write(source, snapshot)
                    latest.upsert(source).also { store.saveSources(it); artwork.synchronizeSources(it.sources.map { source -> source.id }) }
                }
                currentCoroutineContext().ensureActive()
                if (generation != token) return@launch
                registry = saved; snapshots[source.id] = snapshot
                publishLibrary()
                homeFocusKey = null
                // Back to Kaynaklar while the PIN that opened them still holds (the app did not leave the screen).
                mutable.update { it.copy(route = SourceScreens.afterSave(it, parental.state.value.mayManageSources),
                    sourceTask = SourceTask.IDLE, editingSourceId = null, draft = SourceDraft(), playing = null,
                    selected = null, episodes = emptyList(), focusedId = null,
                    sourceStatus = it.sourceStatus + (source.id to SourceStatus(
                        catalog.items.count { entry -> !entry.adult }, snapshot.updatedAt,
                        notices = catalog.warnings.map { warning -> warning.text }, failed = catalog.warnings.isNotEmpty()))) }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) {
                if (generation == token) {
                    mutable.update { it.copy(route = Route.SETUP, sourceTask = SourceTask.IDLE,
                        error = (e as? SourceFailure)?.text ?: UiText.Res(R.string.error_source_not_saved)) }
                    // The PIN that opened the form ended meanwhile: the form and the details in it go.
                    if (!parental.state.value.mayManageSources) leaveSources()
                }
            }
        }
    }

    fun cancelImport() {
        if (state.value.sourceTask != SourceTask.IMPORTING) return
        generation++; loadJob?.cancel()
        mutable.update { it.copy(route = Route.SETUP, sourceTask = SourceTask.IDLE, error = null) }
        // The PIN that opened the form ended meanwhile (the app left the screen): the form goes with the details in it.
        if (!parental.state.value.mayManageSources) leaveSources()
    }

    fun refresh(sourceId: String? = null) {
        if (state.value.refreshing || state.value.sourceTask != SourceTask.IDLE) return
        val sources = state.value.sources.filter { sourceId == null || it.id == sourceId }
        if (sources.isEmpty()) return
        val token = ++generation
        loadJob?.cancel()
        loadJob = viewModelScope.launch { refreshSources(sources, token) }
    }

    /** [keep]: a notice that outranks how the refresh went, shown throughout (a start-over that failed and led here). */
    private suspend fun refreshSources(sources: List<SourceConfig>, token: Int, keep: CatalogNotice? = null) {
        mutable.update { it.copy(refreshing = true, catalogNotice = keep) }
        for (source in sources) {
            if (generation != token) return
            status(source.id) { it.copy(refreshing = true, notices = emptyList(), failed = false) }
            try {
                val catalog = repository.load(source) { message ->
                    if (generation == token) mutable.update { it.copy(progressLabel = message) }
                }
                currentCoroutineContext().ensureActive()
                if (generation != token) return
                if (catalog.warnings.isNotEmpty() && snapshots[source.id] != null)
                    throw SourceFailure(FailureReason.PARTIAL_REFRESH)
                val snapshot = CatalogSnapshot(catalog, System.currentTimeMillis())
                transactions.write {
                    // A different model may have removed/edited this account while the network was pending.
                    if (store.loadSources().sources.none { it == source }) throw SourceFailure(FailureReason.SOURCE_CHANGED)
                    catalogStore.write(source, snapshot)
                }
                currentCoroutineContext().ensureActive()
                if (generation != token) return
                snapshots[source.id] = snapshot
                publishLibrary()
                status(source.id) { it.copy(refreshing = false, notices = catalog.warnings.map { warning -> warning.text }
                    .ifEmpty { listOf(UiText.Res(R.string.notice_library_updated)) }, failed = catalog.warnings.isNotEmpty()) }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) {
                if (generation != token) return
                status(source.id) { it.copy(refreshing = false, failed = true,
                    notices = listOf((e as? SourceFailure)?.text ?: UiText.Res(R.string.notice_refresh_failed))) }
            }
        }
        if (generation == token) mutable.update { it.copy(refreshing = false,
            catalogNotice = keep ?: if (sources.any { source -> it.sourceStatus[source.id]?.failed == true })
                CatalogNotice(UiText.Res(R.string.notice_sources_not_refreshed), CatalogOperation.REFRESH, needsAttention = true)
            else CatalogNotice(UiText.Res(R.string.notice_library_updated), CatalogOperation.REFRESH)) }
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
        mutable.update { it.copy(sourceFilterId = id, catalog = filtered(it.allCatalog, id), category = CategoryFilter.ALL,
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
            catch (_: Exception) { if (token == filterGeneration) mutable.update { it.copy(catalogNotice = CatalogNotice(UiText.Res(R.string.notice_filter_not_saved), CatalogOperation.SOURCE_FILTER, needsAttention = true)) } }
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
        // Only Kaynaklar edits a guide address; while a PIN guards them it is the one that opened them (pin.md §3.8).
        if (!parental.state.value.mayManageSources) {
            mutable.update { it.copy(guideSaveError = UiText.Res(R.string.error_sources_need_pin)) }; return
        }
        if (state.value.sourceTask != SourceTask.IDLE || state.value.refreshing) {
            mutable.update { it.copy(guideSaveError = UiText.Res(R.string.error_source_busy)) }; return
        }
        val original = state.value.sources.firstOrNull { it.id == id } ?: return
        val changed = try { original.copy(epgUrl = address).validated() }
            catch (e: SourceFailure) { mutable.update { it.copy(guideSaveError = e.text) }; return }
        val token = ++generation
        loadJob?.cancel()
        mutable.update { it.copy(sourceTask = SourceTask.SAVING, guideSaveError = null) }
        loadJob = viewModelScope.launch {
            try {
                val saved = transactions.write {
                    val latest = readRegistry()
                    if (latest.sources.none { it == original }) throw SourceFailure(FailureReason.SOURCE_CHANGED_IN_SETTINGS)
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
                guideSaveError = (e as? SourceFailure)?.text ?: UiText.Res(R.string.error_guide_url_not_saved)) } }
        }
    }
    fun showKind(kind: MediaKind) {
        clearLibraryQuery()
        mutable.update { it.copy(kind = kind, onlyFavorites = false, category = CategoryFilter.ALL, focusedId = null, route = Route.LIBRARY) }
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
    fun favorites() { clearLibraryQuery(); mutable.update { it.copy(onlyFavorites = true, category = CategoryFilter.ALL, focusedId = null, route = Route.LIBRARY) } }
    fun category(value: String) { mutable.update { it.copy(category = value, focusedId = null) } }
    // Each letter keeps the last results until the new ones arrive, so the list does not blink empty while typing.
    fun query(value: String) { mutable.update { it.copy(query = value, focusedId = null) }; updateLibrarySearch(clearResults = false) }
    fun onlyFavorites() { mutable.update { it.copy(onlyFavorites = !it.onlyFavorites, focusedId = null) } }
    fun season(value: Int) { mutable.update { it.copy(season = value) } }
    fun reducedMotion() { store.reducedMotion = !state.value.reducedMotion; mutable.update { it.copy(reducedMotion = store.reducedMotion) } }
    fun startupSound() { store.startupSound = !state.value.startupSound; mutable.update { it.copy(startupSound = store.startupSound) } }
    /**
     * Göster, Kilitle or Gizle for a category row of Kategorileri yönet (pin.md §3.4), by section and name across every
     * source as hiding works (D068); the library is published again without reloading any source. CategoryAccessPlan
     * decides what the choice may do: hiding and locking ask for nothing, and loosening needs the PIN verified in this
     * Settings visit. A locked row that is hidden keeps its rules, which Göster removes; a lock is saved before its hidden
     * row comes back, so the row never shows unlocked. Kilitle on a TV without a PIN opens the pad for a first one and
     * locks the row once it is saved: true says the pad opened.
     */
    fun setCategoryAccess(kind: MediaKind, name: String, access: CategoryAccess): Boolean {
        when (val plan = CategoryAccessPlan.of(state.value.categoryAccess(kind, name), access, parental.state.value)) {
            CategoryAccessPlan.None -> Unit
            CategoryAccessPlan.CreatePin -> return parental.createPin(AdminFollowUp.LockCategory(kind, name))
            CategoryAccessPlan.Hide -> hideCategory(kind, name, hidden = true)
            is CategoryAccessPlan.Change -> viewModelScope.launch {
                val scopes = rowScopes(kind, name)
                val saved = when {
                    // An Xtream row of titles the provider filed under no category ID has no identity a rule can name.
                    plan.lock && scopes.isEmpty() -> { parental.tell(UiText.Res(R.string.lock_category_unavailable)); false }
                    plan.lock -> changeLocks(add = scopes.map { AccessRule(it, pinRequired = true) }, inSettings = true)
                    parental.policy?.rules.orEmpty().any { it.pinRequired && it.scope in scopes } -> changeLocks(remove = scopes, inSettings = true)
                    else -> true
                }
                if (saved && plan.unhide) hideCategory(kind, name, hidden = false)
            }
        }
        return false
    }

    /** Hides the category row [name] of [kind] or shows it again (D068); showing it locks again what PINs opened (pin.md §2). */
    private fun hideCategory(kind: MediaKind, name: String, hidden: Boolean) {
        val key = CategoryVisibility.key(kind, name)
        if (!hidden) parental.relockContent()
        store.hiddenCategories = if (hidden) store.hiddenCategories + key else store.hiddenCategories - key
        mutable.update { it.copy(hiddenCategories = store.hiddenCategories) }
        viewModelScope.launch { publishLibrary() }
    }

    /**
     * The identities of a category row in every source's list, hidden or not, and the rules its lock rested on at the
     * last publish (a row the provider renumbered).
     */
    private suspend fun rowScopes(kind: MediaKind, name: String): Set<AccessScope> {
        val sources = registry.sources
        val raw = snapshots.mapValues { it.value.catalog }
        val before = lockedRows[CategoryVisibility.key(kind, name)].orEmpty()
        return withContext(Dispatchers.Default) {
            ParentalProjection.categoryScopes(sources, SourceLibrary.combine(sources, raw), kind, name)
        } + before
    }

    /**
     * Kilitle and Kilidi kaldır in a title's quick menu, with a PIN set (pin.md §3.5). The lock goes on the library title
     * the card stands for, as a favourite does (an episode's series). Locking tightens and is done at once; removing
     * the lock asks for the PIN first, and true says the pad opened.
     */
    fun setTitleLock(entry: MediaEntry, locked: Boolean): Boolean {
        if (parental.state.value.status != ParentalStatus.ON) return false
        val target = favoriteTarget(entry) ?: return false
        val ref = ContentRef.parse(target.id) ?: return false
        if (!locked) { parental.release(PinPurpose.Release(target.id, target.title)); return true }
        viewModelScope.launch { changeLocks(add = listOf(AccessRule(AccessScope.Content(ref), pinRequired = true)), inSettings = false) }
        return false
    }

    /**
     * Kaynağı kilitle and Kaynağın kilidini kaldır in Kaynaklar' Bilgiler, with a PIN set (pin.md §3.8): every channel,
     * film and series of source [id] then asks for it. Locking is done at once. Removing the lock goes on with the PIN
     * that opened Kaynaklar; without one (Kaynak yönetimi PIN istesin is off) the pad asks first, and true says it opened.
     */
    fun setSourceLock(id: String, locked: Boolean): Boolean {
        val s = state.value
        val source = s.sources.firstOrNull { it.id == id } ?: return false
        if (parental.state.value.status != ParentalStatus.ON) return false
        val scope = AccessScope.Source(source.id)
        if (locked) {
            viewModelScope.launch { changeLocks(add = listOf(AccessRule(scope, pinRequired = true)), inSettings = false) }
            return false
        }
        if (s.route != Route.SOURCES || !parental.state.value.sourcesUnlocked) {
            parental.release(PinPurpose.Release(source.id, s.sourceName(source.id), source = true)); return true
        }
        viewModelScope.launch { changeLocks(remove = setOf(scope), inSettings = false) }
        return false
    }

    /** Kilitli içerikler (pin.md §3.5): a title's own lock goes, with the PIN verified in this Settings visit. */
    fun removeTitleLock(id: String) { ContentRef.parse(id)?.let { ref -> removeLock { setOf(AccessScope.Content(ref)) } } }
    /** Kilitli içerikler: a source's lock goes, with the PIN verified in this Settings visit. */
    fun removeSourceLock(id: String) { if (id.isNotBlank()) removeLock { setOf(AccessScope.Source(id)) } }
    /**
     * Kilitli içerikler: a category row's lock goes, with the PIN verified in this Settings visit. Only the lock: hiding
     * is the parent's own choice in Kategorileri yönet (pin.md §0.1).
     */
    fun removeCategoryLock(kind: MediaKind, name: String) { removeLock { rowScopes(kind, name) } }
    private fun removeLock(scopes: suspend () -> Set<AccessScope>) {
        if (parental.state.value.status != ParentalStatus.ON || !parental.state.value.adminUnlocked) return
        // What the page said of the last removal no longer stands once another starts.
        parental.tell(null)
        viewModelScope.launch { changeLocks(remove = scopes(), inSettings = true) }
    }

    /** The PIN typed for Kilidi kaldır was right: the title's or the source's own lock goes, on the policy that PIN saw. */
    private fun released(pending: PinPurpose.Release, revision: Long) {
        val scope = if (pending.source) AccessScope.Source(pending.id) else ContentRef.parse(pending.id)?.let(AccessScope::Content) ?: return
        viewModelScope.launch { changeLocks(remove = setOf(scope), verified = revision, inSettings = false) }
    }

    /**
     * Makes a lock change (ParentalController.editLocks) and says why one was not made: in Ayarlar on its page, elsewhere
     * in the corner. True once saved; the library is then published again with it.
     */
    private suspend fun changeLocks(add: List<AccessRule> = emptyList(), remove: Set<AccessScope> = emptySet(),
        verified: Long? = null, inSettings: Boolean): Boolean {
        val failure = when (parental.editLocks(add, remove, verified)) {
            LockEdit.SAVED -> return true
            LockEdit.LIMIT -> UiText.Res(R.string.lock_limit)
            LockEdit.NOT_SAVED -> UiText.Res(R.string.lock_not_saved)
        }
        if (inSettings) parental.tell(failure) else notice(failure)
        return false
    }

    /** [locks] with what PINs opened in this session ([ParentalLocks.open]); nothing while the locks cannot be read. */
    private fun opened(locks: ParentalLocks): ParentalLocks {
        val open = if (locks.unknown) emptySet() else ParentalProjection.opened(lockGroups, parental.grants)
        return if (open == locks.open) locks else locks.copy(open = open)
    }
    /**
     * Turns the adult-name filter on or off (D068). Off loosens a protection: while a PIN guards it and was not verified
     * in this Settings visit, the PIN pad opens instead and true says so; the filter goes off once the PIN is right.
     */
    fun setHideAdult(hide: Boolean): Boolean {
        if (!hide && !parental.state.value.mayLoosen) return parental.requestAdmin(AdminReason.SHOW_ADULT, AdminFollowUp.ShowAdult)
        if (!hide) parental.relockContent()
        store.hideAdultCategories = hide
        mutable.update { it.copy(hideAdultCategories = hide) }
        viewModelScope.launch { publishLibrary() }
        return false
    }

    // Parental control (pin.md §3): ParentalController keeps the PIN pad's steps; Ayarlar and the pad call these.
    /** True when the PIN pad opened before [reason]; false when nothing needs asking (no PIN, or verified in this visit). */
    fun requestAdmin(reason: AdminReason): Boolean = parental.requestAdmin(reason)
    /** True when the PIN pad opened for the first PIN; [then] runs once it is saved. */
    fun createPin(then: AdminFollowUp? = null): Boolean = parental.createPin(then)
    /** True when the PIN pad opened for a new PIN, which needs the PIN verified in this visit. */
    fun changePin(): Boolean = parental.changePin()
    fun removePin() = parental.removePin()
    /** Four digits from the PIN pad; the model wipes them once used. */
    fun submitPin(pin: CharArray) = parental.submit(pin)
    fun pinBack() = parental.back()
    fun cancelPin() = parental.cancel()
    /** The PIN pad's opener left the screen: the pad closes even while a new PIN is saved, and grants nothing. */
    fun closePin() = parental.close()
    /** The Settings panel closed: what a PIN verified there allowed ends with it. */
    fun closeSettings() = parental.closeSettings()
    /** A Settings page opened over another: what the last change said stays with the page it was made on. */
    fun settingsPageOpened() = parental.tell(null)
    /** "Açılan kilitleri kapat": what PINs opened in this session asks again. */
    fun relockNow() = parental.relockContent()
    /** "Kaynak yönetimi PIN istesin" on Ebeveyn denetimi (pin.md §3.8); off loosens, with the PIN verified to get there. */
    fun setSourcesNeedPin(on: Boolean) = editOptions { it.copy(sourcesNeedPin = on) }
    /** "Açılışta PIN iste" on Ebeveyn denetimi (pin.md §3.9): the lock screen at launch and on return, from the next one. */
    fun setLaunchNeedsPin(on: Boolean) = editOptions { it.copy(launchNeedsPin = on) }
    private fun editOptions(change: (ProtectionOptions) -> ProtectionOptions) {
        parental.tell(null)
        viewModelScope.launch { if (parental.editOptions(change) != LockEdit.SAVED) parental.tell(UiText.Res(R.string.lock_not_saved)) }
    }
    /** The corner note was shown. */
    fun accessNoticeShown() { mutable.update { it.copy(accessNotice = null) } }
    private fun storedSorts(): Map<MediaKind, LibrarySort> = listOf(MediaKind.MOVIE, MediaKind.SERIES).associateWith(store::librarySort)
    private val libraryOrders = LibraryOrderCache()
    /** Films or series in a Sırala order; sorted once per catalogue and choice. */
    fun libraryOrder(catalog: Catalog, kind: MediaKind, sort: LibrarySort): List<MediaEntry> = libraryOrders.sorted(catalog, kind, sort, appLocale)
    fun librarySort(kind: MediaKind, sort: LibrarySort) {
        store.librarySort(kind, sort)
        val catalog = state.value.catalog
        viewModelScope.launch {
            // Sort away from the main thread first; the list then changes in one step.
            if (sort != LibrarySort.DEFAULT) withContext(Dispatchers.Default) { libraryOrders.sorted(catalog, kind, sort, appLocale) }
            mutable.update { it.copy(librarySorts = it.librarySorts + (kind to sort), focusedId = null) }
        }
    }
    /** The activity came back in another language (LocaleController): A–Z orders are sorted again away from the main thread. */
    fun languageChanged() = prepareLibraryOrders()
    /** Sorts a newly published catalogue for the saved choices before Library asks for it. */
    private fun prepareLibraryOrders() {
        val s = state.value
        val pending = s.librarySorts.filterValues { it != LibrarySort.DEFAULT }
        if (pending.isNotEmpty()) viewModelScope.launch(Dispatchers.Default) { pending.forEach { (kind, sort) -> libraryOrders.sorted(s.catalog, kind, sort, appLocale) } }
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

    /** What the app may use of NEXTV Plus: everything until sales open (gap plan S37). */
    val entitlements: Entitlements = AllFree

    /**
     * Every open and play passes here (gap plan T3). The gate finds the title by its identity in this model's state,
     * never in a screen's copy, and decides; an allowed target then runs on the entry the gate found. Deny leaves
     * everything as it is. NeedsPin opens the PIN pad for the title (pin.md §3.6), which the screen that asked shows;
     * only the target waits, and it is decided again once the PIN is right. The Plus page (S37) will answer NeedsPlus.
     */
    fun request(target: AccessTarget): GateResult = request(target, ask = true)

    /** [ask]: false for what runs on its own after an allowed target (a detail's resume): a lock then only stops it. */
    private fun request(target: AccessTarget, ask: Boolean): GateResult {
        val snapshot = gateSnapshot(target)
        val finding = AccessGate.find(target, snapshot)
        val result = AccessGate.answer(target, finding, snapshot)
        when (result) {
            is GateResult.Allow -> perform(target, result.entry)
            is GateResult.NeedsPin -> if (ask && finding != null)
                parental.unlock(PinPurpose.Unlock(target, finding.entry.id, finding.entry.title))
            // An episode whose series cannot be told, or a title of an older list, while a lock of its source needs it.
            // Behind the lock screen nothing opens (pin.md §3.9), and nothing is said either.
            else -> if (ask && !snapshot.appLocked && finding?.verdict?.decision == AccessDecision.UNRESOLVED)
                notice(UiText.Res(R.string.access_unresolved))
        }
        return result
    }

    /**
     * The PIN typed for a locked title was right: the target is found again on the state as it is now, what locks it
     * now opens for the session, and it runs (AccessGate.unlockScopes). A title gone meanwhile (a refresh, a removed
     * source, renumbered channels) runs nothing.
     */
    private fun unlocked(pending: PinPurpose.Unlock) {
        val scopes = AccessGate.unlockScopes(pending.target, pending.id, gateSnapshot(pending.target))
            ?: return notice(UiText.Res(R.string.access_gone))
        parental.grant(scopes)
        request(pending.target, ask = false)
    }

    private fun notice(text: UiText) { mutable.update { it.copy(accessNotice = text) } }

    private fun gateSnapshot(target: AccessTarget): GateSnapshot {
        val s = state.value
        return GateSnapshot(s.sources, s.allCatalog, s.selected, s.episodes, s.playing?.entry, s.route == Route.PLAYER,
            channels = when (target) {
                is AccessTarget.ChannelNumber -> numberedChannels()
                is AccessTarget.ChannelStep -> zapChannels()
                else -> emptyList()
            }, autoNextEpisode = s.autoNextEpisode, policy = parental.gatePolicy, unlocked = parental.grants,
            appLocked = parental.state.value.appLocked, entitlements = entitlements)
    }

    private fun perform(target: AccessTarget, entry: MediaEntry) {
        when (target) {
            is AccessTarget.Open -> openAllowed(entry, target.resume)
            is AccessTarget.Watch -> watchAllowed(entry)
            is AccessTarget.Play -> startPlayback(entry, target.restart)
            // The channel already playing stays as it is.
            is AccessTarget.ChannelNumber -> if (entry.id != state.value.playing?.entry?.id) startPlayback(entry)
            is AccessTarget.ChannelStep -> startPlayback(entry)
            is AccessTarget.NextEpisode -> startPlayback(entry, restart = true)
        }
    }

    // The screens' openers; each asks the gate with the title's identity only. True when the title is locked and the
    // PIN pad opened for it: the screen shows the pad, and focus goes back to the card when it closes.
    fun open(entry: MediaEntry, resume: WatchRecord? = null): Boolean = request(AccessTarget.Open(entry.id, resume)) is GateResult.NeedsPin
    fun watch(entry: MediaEntry): Boolean = request(AccessTarget.Watch(entry.id)) is GateResult.NeedsPin
    fun play(entry: MediaEntry, restart: Boolean = false): Boolean = request(AccessTarget.Play(entry.id, restart)) is GateResult.NeedsPin

    private fun openAllowed(entry: MediaEntry, resume: WatchRecord?) {
        mutable.update { it.copy(focusedId = entry.id) }
        if (entry.kind == MediaKind.LIVE) { startPlayback(entry); return }
        detailJob?.cancel()
        mutable.update { it.copy(route = Route.DETAIL, selected = entry, episodes = emptyList(), detailLoading = true,
            detailError = null, detailReturn = when (it.route) {
                Route.HOME -> Route.HOME; Route.SEARCH -> Route.SEARCH; Route.DETAIL -> it.detailReturn; else -> Route.LIBRARY
            }) }
        loadDetail(entry, resume)
    }

    /**
     * Yeniden dene on a detail loads it again once the gate still allows its title, so nothing of a locked one is
     * fetched without the PIN. A session that ended since it opened asks again, and the right PIN opens it anew; true
     * when the PIN pad opened.
     */
    fun retryDetail(): Boolean {
        val detail = state.value.selected ?: return false
        val target = AccessTarget.Open(detail.id)
        return when (AccessGate.decide(target, gateSnapshot(target))) {
            is GateResult.Allow -> { loadDetail(detail); false }
            is GateResult.NeedsPin -> request(target) is GateResult.NeedsPin
            else -> false
        }
    }
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
                            episode == null -> mutable.update { it.copy(detailError = UiText.Res(R.string.error_episode_missing)) }
                            !resume.progress.completed -> { mutable.update { it.copy(season = episode.season) }; if (stillOn(show)) continueWith(episode) }
                            next == null -> mutable.update { it.copy(season = episode.season) }
                            next.automaticAllowed -> { mutable.update { it.copy(season = next.entry.season) }; if (stillOn(show)) continueWith(next.entry) }
                            else -> mutable.update { it.copy(season = next.entry.season) }
                        }
                    }
                } else {
                    val movie = repository.movie(source, entry.withoutSource()).inSource(source.id)
                    mutable.update { it.copy(selected = movie, detailLoading = false) }
                    if (resume != null && stillOn(movie)) continueWith(movie)
                }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { mutable.update { it.copy(detailLoading = false, detailError = (e as? SourceFailure)?.text ?: UiText.Res(R.string.error_details_unavailable)) } }
        }
    }

    /**
     * The hero's İzle (D071): a film plays, from where it was left when it was started; a series plays its next
     * episode, the way its detail's Oynat/Devam et would: the episode being watched, the one after a finished one, else
     * the first. The episodes load first; leaving Ana Sayfa meanwhile cancels the start. With no episode, or when they
     * cannot be loaded, the series' detail opens and explains. What runs after the load asks the gate again.
     */
    private fun watchAllowed(entry: MediaEntry) {
        val source = state.value.sourceFor(entry) ?: return
        mutable.update { it.copy(focusedId = entry.id) }
        if (entry.kind != MediaKind.SERIES) { startPlayback(entry); return }
        detailJob?.cancel()
        mutable.update { it.copy(heroStarting = entry.id) }
        detailJob = viewModelScope.launch {
            try {
                val (rawShow, rawEpisodes) = repository.episodes(source, entry.withoutSource(), snapshots[source.id]?.catalog ?: Catalog(emptyList()))
                val show = rawShow.inSource(source.id)
                val episodes = rawEpisodes.map { it.inSource(source.id) }
                val history = state.value.history.firstOrNull { it.parentId == entry.id }
                val watched = episodes.firstOrNull { it.id == history?.id && !it.adult }
                val next = when {
                    watched != null && history?.progress?.completed == true -> EpisodeSequence.next(source, watched, episodes)?.entry ?: watched
                    watched != null -> watched
                    else -> episodes.firstOrNull { !it.adult }
                }
                mutable.update { it.copy(heroStarting = null) }
                if (state.value.route != Route.HOME) return@launch
                if (next == null) { request(AccessTarget.Open(entry.id), ask = false); return@launch }
                // The player's next-episode countdown reads the series from the state, as after the detail's Oynat.
                mutable.update { it.copy(selected = show, episodes = episodes, season = next.season) }
                continueWith(next)
            } catch (e: CancellationException) { mutable.update { it.copy(heroStarting = null) }; throw e }
            catch (_: Exception) {
                mutable.update { it.copy(heroStarting = null) }
                if (state.value.route == Route.HOME) request(AccessTarget.Open(entry.id), ask = false)
            }
        }
    }

    /** A resume opened from a card plays only while its detail is still the screen; leaving during the load cancels it. */
    private fun stillOn(entry: MediaEntry) = state.value.route == Route.DETAIL && state.value.selected?.id == entry.id

    /**
     * Plays what an allowed open goes on with (a resume, the hero's episode), through the gate again. Its locks are its
     * series' or its own, which the open passed; should the session have ended meanwhile, it stops without asking.
     */
    private fun continueWith(entry: MediaEntry) { request(AccessTarget.Play(entry.id), ask = false) }

    /**
     * Starts [entry] in the player. Only an allowed target reaches here, with the entry the gate found; anything that
     * plays later (a detail's resume, the hero's episode) calls [continueWith], which asks the gate again then.
     */
    private fun startPlayback(entry: MediaEntry, restart: Boolean = false) {
        val source = state.value.sourceFor(entry) ?: return
        try {
            val uri = source.streamUrl(entry)
            val position = if (restart || entry.kind == MediaKind.LIVE) 0 else store.progress(source.id, entry.withoutSource().id)?.takeIf { it.resumable }?.resumeMs ?: 0
            mutable.update { it.copy(route = Route.PLAYER, playing = PlayingMedia(entry, uri, position, PlaybackMode.of(entry.kind)),
                guideChannelId = if (entry.kind == MediaKind.LIVE && it.route == Route.PLAYER && it.playerReturn == Route.GUIDE) entry.id else it.guideChannelId,
                playerReturn = if (it.route == Route.PLAYER) it.playerReturn else it.route,
                season = if (entry.kind == MediaKind.EPISODE && it.episodes.any { episode -> episode.id == entry.id }) entry.season else it.season,
                error = null) }
        } catch (e: SourceFailure) { mutable.update { it.copy(detailError = e.text) } }
    }

    fun nextEpisodeChoice(): NextEpisodeChoice? {
        val s = state.value
        val entry = s.playing?.entry ?: return null
        return EpisodeSequence.next(s.sourceFor(entry) ?: return null, entry, s.episodes)
    }
    fun nextEpisode(): MediaEntry? = nextEpisodeChoice()?.entry

    /**
     * Starts [nextId]: the gate checks that the player still shows [currentId] and that it comes next. A locked series
     * whose PIN this session has not opened answers NEEDS_PIN; by hand the PIN pad opens, the countdown asks nothing.
     */
    fun playNextEpisode(sourceId: String, currentId: String, nextId: String, automatic: Boolean = false): NextResult =
        when (request(AccessTarget.NextEpisode(sourceId, currentId, nextId, automatic), ask = !automatic)) {
            is GateResult.Allow -> if (state.value.playing?.entry?.id == nextId) NextResult.STARTED else NextResult.UNAVAILABLE
            is GateResult.NeedsPin -> NextResult.NEEDS_PIN
            else -> NextResult.UNAVAILABLE
        }

    /**
     * The hero's films with their details (D067): an Xtream list sends no wide picture or plot for films, only their
     * detail does. Fetched for the slide on screen and the next one, once per session, kept in memory only.
     */
    private val heroDetails = mutableStateMapOf<String, MediaEntry>()
    private val heroRequests = HashSet<String>()
    fun heroDetail(entry: MediaEntry): MediaEntry = heroDetails[entry.id] ?: entry
    fun ensureHeroDetail(entry: MediaEntry) {
        if (entry.kind != MediaKind.MOVIE && entry.kind != MediaKind.SERIES) return
        // Nothing of a locked title is fetched before its PIN (pin.md §3.6), nor of any while the locks cannot be read.
        if (state.value.locks.locked(entry.id)) return
        if ((entry.hasBackdrop && entry.description.isNotBlank()) || !heroRequests.add(entry.id)) return
        val source = state.value.sourceFor(entry)?.takeIf { it.type == SourceType.XTREAM } ?: return
        viewModelScope.launch {
            try {
                val local = entry.withoutSource()
                val detailed = if (entry.kind == MediaKind.MOVIE) repository.movie(source, local)
                    else repository.episodes(source, local, snapshots[source.id]?.catalog ?: Catalog(emptyList())).first
                // Keep the list's picture when the detail has none, and the kind and identity of the slide.
                heroDetails[entry.id] = detailed.inSource(source.id).let { it.copy(backdrop = it.backdrop ?: entry.backdrop, artwork = it.artwork ?: entry.artwork) }
            }
            catch (e: CancellationException) { throw e }
            catch (_: Exception) { } // The slide keeps its poster; the detail screen reports errors.
        }
    }

    /** Channel numbers as Canlı TV shows them: the order of "Tüm kanallar". */
    private fun numberedChannels(): List<MediaEntry> =
        catalogDisplayOrder(state.value.catalog.items.filter { it.kind == MediaKind.LIVE && !it.adult })
    fun channelNumber(entry: MediaEntry): Int? = numberedChannels().indexOfFirst { it.id == entry.id }.takeIf { it >= 0 }?.plus(1)
    /**
     * Plays channel [number] of "Tüm kanallar" (the one playing stays as it is). Deny: there is no such channel.
     * NeedsPin: it is locked, and the PIN pad opened; the channel playing goes on behind it until the PIN is right.
     */
    fun playChannelNumber(number: Int): GateResult = request(AccessTarget.ChannelNumber(number))

    /** CH±: the next channel that plays without the PIN; with none, the player stays where it is. */
    fun changeChannel(direction: Int) { request(AccessTarget.ChannelStep(direction)) }
    /** The channels CH± steps through: those of the list the player was opened from (a category, Ara's source, all). */
    private fun zapChannels(): List<MediaEntry> {
        val s = state.value
        val context = if (s.playerReturn == Route.SEARCH) filtered(s.allCatalog, s.searchSourceId) else s.catalog
        return context.items.filter { it.kind == MediaKind.LIVE && !it.adult &&
            (s.playerReturn != Route.LIBRARY || s.category == CategoryFilter.ALL || it.category == s.category) }
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

    // Source management (pin.md §3.8): while a PIN guards it, each way in asks for the PIN first, and true says the pad
    // opened; once it is right, the same step runs (the follow-ups above). Within Kaynaklar and the setup form the PIN
    // that opened them holds.
    /** Kaynakları yönet: in Ayarlar, the guide, an empty Ana Sayfa or library. */
    fun manageSources(): Boolean {
        if (asksForSources(AdminFollowUp.OpenSources)) return true
        openSources(); return false
    }
    private fun openSources() {
        mutable.update { it.copy(route = Route.SOURCES, sourceManagerFocus = "add-source", sourcesReturn = when (it.route) {
            Route.HOME, Route.LIBRARY, Route.SEARCH, Route.GUIDE -> it.route
            else -> Route.HOME
        }) }
    }
    fun addSource(): Boolean {
        if (state.value.sourceTask != SourceTask.IDLE) return false
        if (asksForSources(AdminFollowUp.AddSource)) return true
        newSource(); return false
    }
    private fun newSource() {
        mutable.update { it.copy(route = Route.SETUP, editingSourceId = null, draft = SourceDraft(), error = null, sourceManagerFocus = "add-source",
            setupReturn = if (it.route == Route.SOURCES) Route.SOURCES else Route.HOME) }
    }
    fun editSource(sourceId: String? = state.value.source?.id): Boolean {
        val source = editable(sourceId) ?: return false
        if (asksForSources(AdminFollowUp.EditSource(source.id))) return true
        edit(source); return false
    }
    private fun editable(id: String?): SourceConfig? =
        state.value.sources.firstOrNull { it.id == id }?.takeIf { state.value.sourceTask == SourceTask.IDLE }
    /** The setup form on [source]; its draft holds the source's password, which only the PIN that opened it shows. */
    private fun edit(source: SourceConfig) {
        mutable.update { it.copy(route = Route.SETUP, editingSourceId = source.id, error = null, sourceManagerFocus = "edit-source:${source.id}",
            setupReturn = if (it.route == Route.SOURCES) Route.SOURCES else Route.HOME,
            draft = SourceDraft(source.name, source.type, source.address, source.username, source.password, source.epgUrl)) }
    }

    fun removeSource(sourceId: String? = state.value.source?.id): Boolean {
        if (state.value.sourceTask == SourceTask.REMOVING) return false
        val owner = removable(sourceId) ?: run {
            if (state.value.sourceTask == SourceTask.IDLE) mutable.update { it.copy(route = Route.SETUP) }
            return false
        }
        if (asksForSources(AdminFollowUp.RemoveSource(owner.id))) return true
        remove(owner); return false
    }
    private fun removable(id: String?): SourceConfig? =
        state.value.sources.firstOrNull { it.id == id }?.takeIf { state.value.sourceTask != SourceTask.REMOVING }
    private fun remove(owner: SourceConfig) {
        val previous = state.value
        val token = ++generation
        val previousJob = loadJob
        previousJob?.cancel(); detailJob?.cancel(); filterGeneration++; filterJob?.cancel()
        guides.forget(owner.id)
        resetSearchIndex()
        // Detach only this owner before player disposal: its final progress callback cannot recreate data.
        mutable.value = previous.copy(route = Route.LOADING, sourceTask = SourceTask.REMOVING,
            sources = previous.sources.filterNot { it.id == owner.id }, playing = null,
            guideStatus = previous.guideStatus - owner.id,
            progressLabel = UiText.Res(R.string.progress_removing_source), refreshing = false,
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
                mutable.update { it.copy(route = if (saved.sources.isEmpty()) Route.SETUP
                    else SourceScreens.afterRemoval(previous, parental.state.value.mayManageSources),
                    sourceTask = SourceTask.IDLE, editingSourceId = null, draft = SourceDraft(),
                    selected = null, episodes = emptyList(), error = null,
                    sourceManagerFocus = saved.sources.getOrNull(previous.sources.indexOf(owner).coerceAtMost(saved.sources.lastIndex).coerceAtLeast(0))
                        ?.let { source -> "remove-source:${source.id}" } ?: "add-source",
                    catalogNotice = CatalogNotice(UiText.Res(R.string.notice_source_removed), CatalogOperation.SOURCE_REMOVAL)) }
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) {
                guides.synchronize(previous.sources, snapshots)
                runCatching { transactions.read { artwork.synchronizeSources(store.loadSources().sources.map { it.id }) } }
                // The parental state as it is now, not as the removal began: the app may have been locked meanwhile.
                if (generation == token) mutable.value = previous.copy(route = SourceScreens.afterRemoval(previous, parental.state.value.mayManageSources),
                    sourceTask = SourceTask.IDLE, playing = null, refreshing = false,
                    sourceStatus = previous.sourceStatus.mapValues { (_, value) -> value.copy(refreshing = false) },
                    error = UiText.Res(R.string.notice_source_not_removed), parental = parental.state.value, locks = opened(previous.locks),
                    catalogNotice = CatalogNotice(UiText.Res(R.string.notice_source_not_removed), CatalogOperation.SOURCE_REMOVAL, needsAttention = true))
            }
        }
    }
}
