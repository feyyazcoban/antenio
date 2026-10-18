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
    /** CH± or the player's channel buttons: the next channel in [direction] of the list the player was opened from. */
    data class ChannelStep(val direction: Int) : AccessTarget
    /** The episode after [currentId], by hand or when the countdown ends ([automatic]). */
    data class NextEpisode(val sourceId: String, val currentId: String, val nextId: String, val automatic: Boolean) : AccessTarget
}

/** The gate's answer. [Allow] carries the entry the gate found: the one to open, never the screen's copy. */
sealed interface GateResult {
    data class Allow(val entry: MediaEntry) : GateResult
    /** Hidden, adult, without its source or gone: nothing happens, as nothing would on a card that is no longer shown. */
    data object Deny : GateResult
    /** Locked (BKL-028): the PIN pad asks, then [target] is decided again. Not returned yet. */
    data class NeedsPin(val target: AccessTarget) : GateResult
    /** A NEXTV Plus [feature]. Not returned until sales open. */
    data class NeedsPlus(val feature: Feature) : GateResult
}

/**
 * The model's state the gate reads, taken when a target is asked for. [allCatalog] is the library as published, its
 * hidden categories (D068) already left out. [selected] and [episodes] are the detail open, or last opened: an episode
 * is reached through its series only. [playing] is the title in the player and [playerShown] whether the player is
 * the screen. [channels] is the list a channel target counts in: "Tüm kanallar" in number order for
 * [AccessTarget.ChannelNumber], the list the player was opened from for [AccessTarget.ChannelStep], empty otherwise.
 */
data class GateSnapshot(
    val sources: List<SourceConfig>,
    val allCatalog: Catalog,
    val selected: MediaEntry? = null,
    val episodes: List<MediaEntry> = emptyList(),
    val playing: MediaEntry? = null,
    val playerShown: Boolean = false,
    val channels: List<MediaEntry> = emptyList(),
    val autoNextEpisode: Boolean = true,
    /** Read by the targets of Plus features as they arrive; [AllFree] until sales open. */
    val entitlements: Entitlements = AllFree
)

/**
 * The one decision every open and play passes (gap plan T3): the target is resolved by identity in [GateSnapshot]
 * and allowed only when the title is still published, not adult and its source is still saved. Pure, so its rules
 * are tested on the JVM. The PIN (S10) and NEXTV Plus (S37) answers join here.
 */
object AccessGate {
    fun decide(target: AccessTarget, snapshot: GateSnapshot): GateResult = with(snapshot) {
        allowed(when (target) {
            is AccessTarget.Open -> title(target.id)
            is AccessTarget.Watch -> title(target.id)
            is AccessTarget.Play -> playable(target.id)
            is AccessTarget.ChannelNumber -> channels.getOrNull(target.n - 1)?.let { channel(it.id) }
            is AccessTarget.ChannelStep -> if (channels.isEmpty()) null else {
                val index = channels.indexOfFirst { it.id == playing?.id }
                channel(channels[Math.floorMod(index + target.direction, channels.size)].id)
            }
            is AccessTarget.NextEpisode -> nextEpisode(target)
        })
    }

    private fun GateSnapshot.allowed(entry: MediaEntry?): GateResult =
        if (entry == null || entry.adult || sources.none { it.id == entry.sourceId }) GateResult.Deny else GateResult.Allow(entry)

    /** A channel, film or series of the published library. */
    private fun GateSnapshot.title(id: String): MediaEntry? = allCatalog.items.firstOrNull { it.id == id && !it.adult }

    private fun GateSnapshot.channel(id: String): MediaEntry? = title(id)?.takeIf { it.kind == MediaKind.LIVE }

    /**
     * What Play starts: the open detail when it is this title (a film's detail can name the stream's real container),
     * else the library's entry; or an episode.
     */
    private fun GateSnapshot.playable(id: String): MediaEntry? {
        val title = title(id) ?: return episode(id)
        return selected?.takeIf { it.id == id && it.kind == title.kind } ?: title
    }

    /**
     * An episode of the open series, or of a published M3U series. Its series must be in the library: an episode of a
     * hidden or adult series is not reached through a detail that was open before.
     */
    private fun GateSnapshot.episode(id: String): MediaEntry? {
        val series = selected?.takeIf { it.kind == MediaKind.SERIES && title(it.id)?.kind == MediaKind.SERIES }
        if (series != null) episodes.firstOrNull { it.id == id && it.kind == MediaKind.EPISODE && it.sourceId == series.sourceId &&
            (it.parentLocalId == null || it.parentLocalId == series.localId) }?.let { return it }
        return allCatalog.m3uEpisodes.entries.firstOrNull { (parent, list) ->
            title(parent)?.kind == MediaKind.SERIES && list.any { it.id == id }
        }?.value?.firstOrNull { it.id == id && it.kind == MediaKind.EPISODE }
    }

    /**
     * The episode after the one in the player, while the player still shows [AccessTarget.NextEpisode.currentId]. The
     * countdown's own start is refused when the viewer turned it off or the order is uncertain (EpisodeSequence).
     */
    private fun GateSnapshot.nextEpisode(target: AccessTarget.NextEpisode): MediaEntry? {
        val current = playing?.takeIf { playerShown && it.sourceId == target.sourceId && it.id == target.currentId } ?: return null
        val source = sources.firstOrNull { it.id == current.sourceId } ?: return null
        val next = EpisodeSequence.next(source, current, episodes) ?: return null
        if (next.entry.id != target.nextId || (target.automatic && (!autoNextEpisode || !next.automaticAllowed))) return null
        return episode(target.nextId)
    }
}
