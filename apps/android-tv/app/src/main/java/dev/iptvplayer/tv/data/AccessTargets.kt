package dev.iptvplayer.tv.data

/**
 * A target found in the model's state (pin.md §6): [entry] is what opens; [judged] is the library's entry the lock rules
 * read, as a film's detail can lack its category; [series] is the series an episode is reached through.
 */
internal data class ResolvedTarget(val entry: MediaEntry, val series: AccessPolicy.SeriesContext? = null,
    val judged: MediaEntry = entry)

/**
 * The canonical title an [AccessTarget] names, looked up by identity in a [GateSnapshot]: a title of the published
 * library, so one hidden (D068) or dropped by a refresh is reached by no path, or an episode through its series. Pure.
 */
internal object AccessTargets {
    /**
     * [opens]: whether a channel may play as it stands, for CH±, which steps over the ones that would ask for the PIN.
     * Null when the target names nothing that is still published.
     */
    fun resolve(target: AccessTarget, snapshot: GateSnapshot, opens: (ResolvedTarget) -> Boolean = { true }): ResolvedTarget? =
        with(snapshot) {
            when (target) {
                is AccessTarget.Open -> title(target.id)?.let(::ResolvedTarget)
                is AccessTarget.Watch -> title(target.id)?.let(::ResolvedTarget)
                is AccessTarget.Play -> playable(target.id)
                is AccessTarget.ChannelNumber -> channels.getOrNull(target.n - 1)?.let { channel(it.id) }?.let(::ResolvedTarget)
                is AccessTarget.ChannelPick -> if (playerShown && playing?.kind == MediaKind.LIVE && channels.any { it.id == target.id })
                    channel(target.id)?.let(::ResolvedTarget) else null
                is AccessTarget.ChannelStep -> if (channels.isEmpty()) null else {
                    // One lookup table for the steps: a locked category can make CH± pass many channels.
                    val published = liveById ?: allCatalog.items.filter { it.kind == MediaKind.LIVE && !it.adult }.associateBy { it.id }
                    nextZap(channels, playing?.id, target.direction) { candidate ->
                        published[candidate.id]?.let { opens(ResolvedTarget(it)) } == true
                    }?.let { published[it.id] }?.let(::ResolvedTarget)
                }
                is AccessTarget.NextEpisode -> nextEpisode(target)
            }
        }

    /**
     * The channel after [currentId] in [direction] that [allowed] lets play, around the ends of [channels]; null when no
     * other one may, and the player stays where it is (pin.md §3.7). From a title outside the list CH+ starts at its first
     * channel.
     */
    fun nextZap(channels: List<MediaEntry>, currentId: String?, direction: Int, allowed: (MediaEntry) -> Boolean): MediaEntry? {
        if (channels.isEmpty() || direction == 0) return null
        var index = channels.indexOfFirst { it.id == currentId }
        repeat(channels.size) {
            index = Math.floorMod(index + direction, channels.size)
            val candidate = channels[index]
            if (candidate.id == currentId) return null
            if (allowed(candidate)) return candidate
        }
        return null
    }

    /** A channel, film or series of the published library. */
    private fun GateSnapshot.title(id: String): MediaEntry? = allCatalog.items.firstOrNull { it.id == id && !it.adult }

    private fun GateSnapshot.channel(id: String): MediaEntry? = if (liveById != null) liveById[id] else title(id)?.takeIf { it.kind == MediaKind.LIVE }

    /**
     * What Play starts: the open detail when it is this title (a film's detail can name the stream's real container),
     * else the library's entry; or an episode.
     */
    private fun GateSnapshot.playable(id: String): ResolvedTarget? {
        val title = title(id) ?: return episode(id)
        return ResolvedTarget(selected?.takeIf { it.id == id && it.kind == title.kind } ?: title, judged = title)
    }

    /**
     * An episode of the open series, or of a published M3U series. Its series must be in the library: an episode of a
     * hidden or adult series is not reached through a detail that was open before. The library's series is the one its
     * locks are read on.
     */
    private fun GateSnapshot.episode(id: String): ResolvedTarget? {
        val series = selected?.takeIf { it.kind == MediaKind.SERIES }?.let { title(it.id) }?.takeIf { it.kind == MediaKind.SERIES }
        if (series != null) episodes.firstOrNull { it.id == id && it.kind == MediaKind.EPISODE && it.sourceId == series.sourceId &&
            (it.parentLocalId == null || it.parentLocalId == series.localId) }?.let { return ResolvedTarget(it, AccessPolicy.SeriesContext(series, episodes)) }
        // Membership first: the series is looked up only for a list that holds the episode, not for each of the
        // thousands of series a large M3U list can have.
        for ((parent, list) in allCatalog.m3uEpisodes) {
            if (list.none { it.id == id }) continue
            val show = title(parent)?.takeIf { it.kind == MediaKind.SERIES } ?: continue
            return list.firstOrNull { it.id == id && it.kind == MediaKind.EPISODE }
                ?.let { ResolvedTarget(it, AccessPolicy.SeriesContext(show, list)) }
        }
        return null
    }

    /**
     * The episode after the one in the player, while the player still shows [AccessTarget.NextEpisode.currentId]. The
     * countdown's own start is refused when the viewer turned it off or the order is uncertain (EpisodeSequence).
     */
    private fun GateSnapshot.nextEpisode(target: AccessTarget.NextEpisode): ResolvedTarget? {
        val current = playing?.takeIf { playerShown && it.sourceId == target.sourceId && it.id == target.currentId } ?: return null
        val source = sources.firstOrNull { it.id == current.sourceId } ?: return null
        val next = EpisodeSequence.next(source, current, episodes) ?: return null
        if (next.entry.id != target.nextId || (target.automatic && (!autoNextEpisode || !next.automaticAllowed))) return null
        return episode(target.nextId)
    }
}
