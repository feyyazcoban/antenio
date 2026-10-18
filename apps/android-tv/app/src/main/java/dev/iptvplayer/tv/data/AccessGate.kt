package dev.iptvplayer.tv.data

/**
 * What the viewer asks to open or play, named by identity only (gap plan T3). The gate finds the title itself in the
 * model's state and never trusts the copy a screen holds, which can be older than a refresh or a hidden category.
 * Later steps add Archive, ChannelPick, Resume (a deep link), Reminder, SettingsPage and SourceManagement.
 */
sealed interface AccessTarget {
    /** A card, row or the hero's Ayrıntılar: a channel plays, a film or series opens its detail; [resume] goes on. */
    data class Open(val id: String, val resume: WatchRecord? = null) : AccessTarget
    /** Oynat, Devam et or Baştan başlat ([restart]) on a detail, or one of its episodes. */
    data class Play(val id: String, val restart: Boolean = false) : AccessTarget
    /** The hero's İzle (D071). */
    data class Watch(val id: String) : AccessTarget
    /** Channel [n] of "Tüm kanallar", typed with the remote's digits in the player. */
    data class ChannelNumber(val n: Int) : AccessTarget
    /**
     * CH± or the player's channel buttons: the next channel in [direction] of the list the player was opened from that
     * plays without asking; a locked one is stepped over (pin.md §3.7).
     */
    data class ChannelStep(val direction: Int) : AccessTarget
    /** The episode after [currentId], by hand or when the countdown ends ([automatic]). */
    data class NextEpisode(val sourceId: String, val currentId: String, val nextId: String, val automatic: Boolean) : AccessTarget
}

/** The gate's answer. [Allow] carries the entry the gate found: the one to open, never the screen's copy. */
sealed interface GateResult {
    data class Allow(val entry: MediaEntry) : GateResult
    /**
     * Hidden, adult, without its source, gone or of a lock that cannot be read: nothing happens, as nothing would on a
     * card that is no longer shown.
     */
    data object Deny : GateResult
    /**
     * Locked (BKL-028): the PIN pad asks for the title [target] names, and once the PIN is right the target is decided
     * again on the state as it is then. Only the identity waits, never an action.
     */
    data class NeedsPin(val target: AccessTarget) : GateResult
    /** A NEXTV Plus [feature]. Not returned until sales open. */
    data class NeedsPlus(val feature: Feature) : GateResult
}

/**
 * The model's state the gate reads, taken when a target is asked for. [allCatalog] is the library as published, its
 * hidden categories (D068) and its titles of unknown lock already left out. [selected] and [episodes] are the detail
 * open, or last opened: an episode is reached through its series only. [playing] is the title in the player and
 * [playerShown] whether the player is the screen. [channels] is the list a channel target counts in: "Tüm kanallar" in
 * number order for [AccessTarget.ChannelNumber], the list the player was opened from for [AccessTarget.ChannelStep],
 * empty otherwise. [policy] is the lock rules, null while the protection record is not read or cannot be: then every
 * title asks for the PIN (D049). [unlocked] is what PINs opened in this session (ParentalSession). [appLocked]: the lock
 * screen is up (pin.md §3.9), and nothing opens behind it.
 */
internal data class GateSnapshot(
    val sources: List<SourceConfig>,
    val allCatalog: Catalog,
    val selected: MediaEntry? = null,
    val episodes: List<MediaEntry> = emptyList(),
    val playing: MediaEntry? = null,
    val playerShown: Boolean = false,
    val channels: List<MediaEntry> = emptyList(),
    val autoNextEpisode: Boolean = true,
    val policy: AccessPolicy? = AccessPolicy(emptyList()),
    val unlocked: Set<AccessScope> = emptySet(),
    val appLocked: Boolean = false,
    /** Read by the targets of Plus features as they arrive; [AllFree] until sales open. */
    val entitlements: Entitlements = AllFree
)

/** What the gate found for a target: the [entry] to open, the lock rules' [verdict] and whether the session opened it. */
internal data class GateFinding(val entry: MediaEntry, val verdict: AccessVerdict, val unlocked: Boolean) {
    /** Nothing locks it, or a PIN opened every lock on it in this session. */
    val opens: Boolean get() = verdict.decision == AccessDecision.ALLOW || verdict.decision == AccessDecision.REQUIRE_PIN && unlocked
}

/**
 * The one decision every open and play passes (gap plan T3): AccessTargets finds the target by identity in
 * [GateSnapshot]; it is allowed when the title is still published, not adult, its source still saved and no lock asks
 * for the PIN, or the session opened every lock on it (pin.md §6), and the lock screen is not up (§3.9). Pure, so its
 * rules are tested on the JVM. The NEXTV Plus answer (S37) joins here.
 */
internal object AccessGate {
    fun decide(target: AccessTarget, snapshot: GateSnapshot): GateResult = answer(target, find(target, snapshot), snapshot)

    /** The answer [finding], from [find] on [snapshot], gives for [target]; behind the lock screen, none opens. */
    fun answer(target: AccessTarget, finding: GateFinding?, snapshot: GateSnapshot): GateResult = when {
        snapshot.appLocked || finding == null -> GateResult.Deny
        finding.opens -> GateResult.Allow(finding.entry)
        finding.verdict.decision == AccessDecision.REQUIRE_PIN -> GateResult.NeedsPin(target)
        else -> GateResult.Deny
    }

    /**
     * What [target] names and what its locks say; null when it names nothing published, adult or without its source.
     * The lock screen leaves this as it is: [answer] refuses everything behind it, and whether the player it covers may
     * come back once it goes is read here.
     */
    fun find(target: AccessTarget, snapshot: GateSnapshot): GateFinding? {
        val resolved = AccessTargets.resolve(target, snapshot) { judge(it, snapshot)?.opens == true } ?: return null
        return judge(resolved, snapshot)
    }

    /**
     * The PIN typed for [target], asked for the title [id], was right (pin.md §3.6): the target is found again on
     * [snapshot], and what locks it now is what the session opens before it runs, a lock added since the pad opened
     * included. Null when it names another title by now (renumbered channels) or nothing it may open (a refresh, a
     * removed source, a hidden category): nothing runs.
     */
    fun unlockScopes(target: AccessTarget, id: String, snapshot: GateSnapshot): Set<AccessScope>? =
        find(target, snapshot)?.takeIf { finding ->
            finding.entry.id == id && finding.verdict.decision.let { it == AccessDecision.ALLOW || it == AccessDecision.REQUIRE_PIN }
        }?.verdict?.pinScopes

    private fun judge(target: ResolvedTarget, snapshot: GateSnapshot): GateFinding? = with(snapshot) {
        val entry = target.entry
        val source = sources.firstOrNull { it.id == entry.sourceId }
        if (entry.adult || source == null) return null
        // A record not read yet, or unreadable, allows nothing without the PIN, not even what a PIN opened before.
        val verdict = policy?.evaluate(source, target.judged, target.series)
            ?: return GateFinding(entry, AccessVerdict(AccessDecision.REQUIRE_PIN), unlocked = false)
        GateFinding(entry, verdict, unlocked.containsAll(verdict.pinScopes))
    }
}
