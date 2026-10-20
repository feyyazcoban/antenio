package dev.iptvplayer.tv.data

import org.json.JSONObject

/** A session's navigation target; never a media address. */
internal data class LivePick(val id: String, val scope: ChannelScope)

internal class LiveRecall {
    var current: LivePick? = null
        private set
    var previous: LivePick? = null
        private set

    fun played(pick: LivePick) {
        if (current?.id != pick.id) previous = current
        current = pick
    }

    fun clear() { current = null; previous = null }
    fun removeSource(sourceId: String) {
        if (current?.id?.let(ContentRef::parse)?.sourceId == sourceId) current = null
        if (previous?.id?.let(ContentRef::parse)?.sourceId == sourceId) previous = null
    }
    fun retainSources(ids: Set<String>) {
        if (current?.id?.let(ContentRef::parse)?.sourceId !in ids) current = null
        if (previous?.id?.let(ContentRef::parse)?.sourceId !in ids) previous = null
    }
}

internal data class LastLive(val localId: String, val at: Long) {
    fun encode(): String {
        require(localId.isNotBlank() && at >= 0)
        return JSONObject().put("id", localId).put("at", at).toString()
    }

    companion object {
        fun decode(value: String?): LastLive? = runCatching {
            if (value == null) return null
            val item = JSONObject(value)
            LastLive(item.getString("id"), item.getLong("at")).also { require(it.localId.isNotBlank() && it.at >= 0) }
        }.getOrNull()
    }
}

internal sealed interface AutoStartDecision {
    data object Home : AutoStartDecision
    data class Play(val id: String) : AutoStartDecision
    data class AfterUnlock(val id: String) : AutoStartDecision
}

/** Picks the latest source record only; a hidden or locked latest channel never falls back to an older one. */
internal object AutoStart {
    fun decide(enabled: Boolean, atLaunch: Boolean, records: Map<String, LastLive>, channels: List<MediaEntry>,
        appLocked: Boolean, locked: (String) -> Boolean): AutoStartDecision {
        if (!enabled || !atLaunch) return AutoStartDecision.Home
        val latest = records.maxByOrNull { it.value.at } ?: return AutoStartDecision.Home
        val channel = channels.firstOrNull {
            it.sourceId == latest.key && it.withoutSource().id == latest.value.localId && it.kind == MediaKind.LIVE && !it.adult
        } ?: return AutoStartDecision.Home
        if (locked(channel.id)) return AutoStartDecision.Home
        return if (appLocked) AutoStartDecision.AfterUnlock(channel.id) else AutoStartDecision.Play(channel.id)
    }
}
