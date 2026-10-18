package dev.iptvplayer.tv.ui

import dev.iptvplayer.tv.data.*

/** Provider information only; never a URL, guessed episode order or inferred language. */
internal data class PlayerInfoModel(val title: String, val year: String, val episodeTitle: String?,
    val season: Int?, val episode: Int?, val durationSeconds: Long) {
    companion object {
        fun from(entry: MediaEntry, series: MediaEntry?): PlayerInfoModel {
            val episode = entry.kind == MediaKind.EPISODE
            val parent = series?.takeIf { episode && it.kind == MediaKind.SERIES && it.sourceId == entry.sourceId }
            val title = displayTitle(parent?.title ?: entry.title, parent?.year ?: entry.year)
            val name = parent?.let { episodeDisplayTitle(entry.title, it.title) }?.takeIf { it.isNotBlank() && it != title.name }
            return PlayerInfoModel(title.name, title.year, name,
                entry.season.takeIf { episode && entry.episodeOrderKnown },
                entry.episode.takeIf { episode && entry.episodeOrderKnown }, entry.durationSeconds)
        }
        fun endMinute(now: Long, position: Long, duration: Long, speed: Float, playing: Boolean): Long? =
            if (!playing) null else PlaybackSpeed.remainingMs(position, duration, speed)?.let { (now + it + 30_000) / 60_000 * 60_000 }
    }
}
