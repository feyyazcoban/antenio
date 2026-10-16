package dev.iptvplayer.tv.data

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

data class NextEpisodeChoice(val entry: MediaEntry, val automaticAllowed: Boolean, val notice: String? = null)

/** The provider list defines membership. Never invent a missing episode or skip an inaccessible target. */
object EpisodeSequence {
    fun next(source: SourceConfig, current: MediaEntry, episodes: List<MediaEntry>): NextEpisodeChoice? {
        if (current.kind != MediaKind.EPISODE || current.adult) return null
        val list = episodes.filter { it.kind == MediaKind.EPISODE }.distinctBy { it.id }
            .sortedWith(compareBy({ it.season }, { it.episode }))
        val index = list.indexOfFirst { it.id == current.id }
        if (index < 0) return null
        val entry = list.getOrNull(index + 1) ?: return null
        if (entry.adult || entry.id == current.id) return null
        val uri = try { source.streamUrl(entry).toHttpUrlOrNull() } catch (_: Exception) { null }
        if (uri == null || uri.username.isNotEmpty() || uri.password.isNotEmpty()) return null
        val orderedCurrent = list[index]
        val duplicateNumber = list.count { it.season == orderedCurrent.season && it.episode == orderedCurrent.episode } > 1 ||
            list.count { it.season == entry.season && it.episode == entry.episode } > 1
        val known = orderedCurrent.episodeOrderKnown && entry.episodeOrderKnown && !duplicateNumber &&
            orderedCurrent.season >= 0 && entry.season >= 0 && orderedCurrent.episode > 0 && entry.episode > 0
        val consecutive = (entry.season == orderedCurrent.season && entry.episode == orderedCurrent.episode + 1) ||
            (entry.season == orderedCurrent.season + 1 && entry.episode == 1)
        val notice = when {
            !known -> "Bölüm sırası net değil. Sonraki bölümü sen başlat."
            !consecutive -> "Arada eksik bölüm olabilir. Sonraki bölümü sen başlat."
            source.type == SourceType.M3U -> "Bölüm sırası listedeki başlıklardan eşleştirildi."
            else -> null
        }
        return NextEpisodeChoice(entry, known && consecutive, notice)
    }
}
