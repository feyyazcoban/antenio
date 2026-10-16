package dev.iptvplayer.tv.data

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.io.Reader

object CatalogParser {
    fun categories(json: String): Map<String, String> = objects(JSONArray(json))
        .associate { it.optString("category_id") to it.optString("category_name", "Diğer") }

    fun xtream(json: String, kind: MediaKind, categories: Map<String, String>): List<MediaEntry> {
        return objects(JSONArray(json)).mapNotNull { row ->
            val key = if (kind == MediaKind.SERIES) "series_id" else "stream_id"
            val id = row.optString(key).takeUnless { it.isBlank() || it == "null" } ?: return@mapNotNull null
            MediaEntry(
                id = "${kind.name.lowercase()}:$id", providerId = id, kind = kind,
                title = row.optString("name").ifBlank { "İsimsiz içerik $id" },
                category = categories[row.optString("category_id")] ?: "Diğer",
                categoryId = row.optString("category_id").takeUnless { it == "null" }.orEmpty().trim(),
                artwork = httpArtwork(row.optString(if (kind == MediaKind.SERIES) "cover" else "stream_icon")),
                backdrop = firstBackdrop(row), description = row.optString("plot", ""),
                year = releaseYear(row), extension = row.optString("container_extension", if (kind == MediaKind.LIVE) "ts" else ""),
                adult = row.optString("is_adult") == "1",
                epgId = if (kind == MediaKind.LIVE) row.optString("epg_channel_id").takeUnless { it == "null" }.orEmpty().trim() else "",
                rating = if (kind == MediaKind.LIVE) 0f else rating(row), addedAt = addedAt(row)
            )
        }.distinctBy { it.id }
    }

    fun movie(json: String, original: MediaEntry): MediaEntry {
        val root = JSONObject(json)
        val info = root.optJSONObject("info") ?: return original
        val data = root.optJSONObject("movie_data")
        return original.copy(
            description = info.optString("plot").ifBlank { info.optString("description") },
            artwork = httpArtwork(info.optString("movie_image")) ?: httpArtwork(info.optString("cover_big")) ?: original.artwork,
            backdrop = firstBackdrop(info) ?: original.backdrop,
            durationSeconds = durationSeconds(info.optString("duration_secs").ifBlank { info.optString("duration") }),
            year = releaseYear(info).ifBlank { original.year },
            extension = data?.optString("container_extension")?.takeIf { it.isNotBlank() } ?: original.extension,
            rating = rating(info).takeIf { it > 0f } ?: original.rating
        )
    }

    fun episodes(json: String, series: MediaEntry): Pair<MediaEntry, List<MediaEntry>> {
        val root = JSONObject(json)
        val info = root.optJSONObject("info") ?: JSONObject()
        val show = series.copy(
            description = info.optString("plot").ifBlank { series.description },
            backdrop = firstBackdrop(info) ?: series.backdrop,
            artwork = httpArtwork(info.optString("cover")) ?: series.artwork,
            rating = rating(info).takeIf { it > 0f } ?: series.rating
        )
        val episodes = root.optJSONObject("episodes") ?: return show to emptyList()
        val results = mutableListOf<MediaEntry>()
        episodes.keys().forEach { seasonKey ->
            val rows = episodes.optJSONArray(seasonKey) ?: return@forEach
            objects(rows).forEachIndexed { index, row ->
                val id = row.optString("id").takeUnless { it.isBlank() || it == "null" } ?: return@forEachIndexed
                val detail = row.optJSONObject("info") ?: JSONObject()
                results += MediaEntry(
                    id = "episode:$id", providerId = id, kind = MediaKind.EPISODE,
                    title = row.optString("title").ifBlank { "${index + 1}. bölüm" },
                    category = series.title, artwork = httpArtwork(detail.optString("movie_image")) ?: show.backdrop ?: show.artwork,
                    categoryId = series.categoryId, parentLocalId = series.localId ?: series.id,
                    description = detail.optString("plot"),
                    durationSeconds = durationSeconds(detail.optString("duration_secs").ifBlank { detail.optString("duration") }),
                    extension = row.optString("container_extension"),
                    season = row.optInt("season", seasonKey.toIntOrNull() ?: 1),
                    episode = row.optInt("episode_num", index + 1), adult = series.adult,
                    episodeOrderKnown = row.optInt("episode_num", -1) > 0 &&
                        row.optInt("season", seasonKey.toIntOrNull() ?: -1) >= 0
                )
            }
        }
        return show to results.distinctBy { it.id }.sortedWith(compareBy({ it.season }, { it.episode }))
    }

    /** A 0–10 rating ("7.5", "7,5" or a number); a five-point value counts double; anything else is unknown (0). */
    internal fun rating(row: JSONObject): Float {
        fun number(key: String) = row.optString(key).trim().replace(',', '.').toFloatOrNull()
        return number("rating")?.takeIf { it > 0f && it <= 10f }
            ?: number("rating_5based")?.takeIf { it > 0f && it <= 5f }?.times(2f) ?: 0f
    }

    /** Unix seconds the provider gives for films ("added") and series ("last_modified"); 0 when unknown. */
    private fun addedAt(row: JSONObject): Long =
        sequenceOf("added", "last_modified").map { row.optString(it).trim().toLongOrNull() ?: 0L }.firstOrNull { it > 0 } ?: 0L

    private fun releaseYear(row: JSONObject): String = sequenceOf("releaseDate", "release_date", "releasedate", "year")
        .map { row.optString(it) }.firstOrNull { it.matches(Regex("\\d{4}.*")) }?.take(4).orEmpty()

    private fun firstBackdrop(row: JSONObject): String? {
        val candidates = row.optJSONArray("backdrop_path")
        return candidates?.let { (0 until it.length()).asSequence().mapNotNull { index -> httpArtwork(it.optString(index)) }.firstOrNull() }
            ?: httpArtwork(row.optString("backdrop_path"))
    }

    private fun objects(array: JSONArray): List<JSONObject> = (0 until array.length()).mapNotNull { array.optJSONObject(it) }
}

object M3uParser {
    private val attribute = Regex("([\\w-]+)\\s*=\\s*(?:\"([^\"]*)\"|'([^']*)'|([^\\s,]+))")
    private val episodeTag = Regex("(?i)(?:S(\\d{1,3})\\s*E(\\d{1,4})|(\\d{1,3})x(\\d{1,4}))")

    fun parse(reader: Reader, baseUrl: String): Catalog {
        val all = mutableListOf<MediaEntry>()
        val episodes = linkedMapOf<String, MutableList<MediaEntry>>()
        val shows = linkedMapOf<String, MediaEntry>()
        var attrs = emptyMap<String, String>()
        var title = ""
        var group = ""
        var headers = mutableMapOf<String, String>()
        var sawHeader = false
        var invalid = 0
        var chars = 0L
        var entryCount = 0
        val epgUrls = linkedSetOf<String>()
        var droppedFeeds = false
        val base = baseUrl.toHttpUrlOrNull() ?: throw SourceFailure("Liste adresi geçersiz.")
        reader.buffered().useLines { lines ->
            lines.forEachIndexed { number, raw ->
                chars += raw.length
                if (chars > 64L * 1024 * 1024 || entryCount > 120_000) {
                    throw SourceFailure("Liste bu pilot sürümünün boyut sınırını aşıyor.")
                }
                val line = raw.trim().removePrefix("\uFEFF")
                if (line.isEmpty()) return@forEachIndexed
                when {
                    line.startsWith("#EXTM3U", true) -> {
                        sawHeader = true
                        attribute.findAll(line).filter { it.groupValues[1].lowercase() in setOf("x-tvg-url", "url-tvg") }
                            .flatMap { it.groupValues.drop(2).firstOrNull { part -> part.isNotEmpty() }.orEmpty().split(',').asSequence() }
                            .filter { it.isNotBlank() }
                            .mapNotNull { base.resolve(it.trim())?.takeIf { url -> url.username.isEmpty() && url.password.isEmpty() } }
                            .forEach {
                                if (epgUrls.size < 4) epgUrls += it.toString()
                                else if (it.toString() !in epgUrls) droppedFeeds = true
                            }
                    }
                    line.startsWith("#EXT-X-TARGETDURATION", true) -> throw SourceFailure("Bu adres bir kanal listesi yerine tek bir HLS yayını içeriyor.")
                    line.startsWith("#EXTINF:", true) -> {
                        attrs = attribute.findAll(line).associate { match ->
                            match.groupValues[1].lowercase() to match.groupValues.drop(2).firstOrNull { it.isNotEmpty() }.orEmpty()
                        }
                        // The title separator is the first comma outside a quoted attribute.
                        var quote: Char? = null
                        val comma = line.indices.firstOrNull { i ->
                            val c = line[i]
                            if (quote != null) { if (c == quote) quote = null; false }
                            else if (c == '"' || c == '\'') { quote = c; false } else c == ','
                        }
                        title = comma?.let { line.substring(it + 1).trim() }.orEmpty()
                        group = attrs["group-title"].orEmpty()
                        headers = mutableMapOf()
                    }
                    line.startsWith("#EXTGRP:", true) -> group = line.substringAfter(':').trim()
                    line.startsWith("#EXTVLCOPT:http-user-agent=", true) -> headers["User-Agent"] = line.substringAfter('=')
                    line.startsWith("#EXTVLCOPT:http-referrer=", true) -> headers["Referer"] = line.substringAfter('=')
                    line.startsWith("#") -> Unit
                    else -> {
                        val uri = base.resolve(line)?.takeIf { it.scheme == "http" || it.scheme == "https" }
                        if (uri == null) { invalid++; return@forEachIndexed }
                        val name = title.ifBlank { attrs["tvg-name"].orEmpty() }.ifBlank { "İçerik ${number + 1}" }
                        val tag = episodeTag.find(name)
                        val paths = uri.pathSegments
                        val kind = when {
                            "series" in paths || tag != null -> MediaKind.EPISODE
                            "movie" in paths || uri.encodedPath.matches(Regex("(?i).*\\.(mp4|mkv|avi|mov|webm)$")) -> MediaKind.MOVIE
                            else -> MediaKind.LIVE
                        }
                        val id = "m3u:${stableId(uri.toString())}"
                        val entry = MediaEntry(id, id, kind, name, group.ifBlank { "Diğer" },
                            artwork = httpArtwork(attrs["tvg-logo"]), uri = uri.toString(), headers = headers.toMap(),
                            season = tag?.let { (it.groupValues[1].ifBlank { it.groupValues[3] }).toIntOrNull() } ?: 1,
                            episode = tag?.let { (it.groupValues[2].ifBlank { it.groupValues[4] }).toIntOrNull() } ?: 1,
                            episodeOrderKnown = tag != null,
                            epgId = attrs["tvg-id"].orEmpty().trim(), epgName = attrs["tvg-name"].orEmpty().trim())
                        if (kind == MediaKind.EPISODE) {
                            val showTitle = tag?.let { name.substring(0, it.range.first).trim().trimEnd('-', ' ') }?.ifBlank { name } ?: name
                            val showId = "m3u-show:${stableId(group + "|" + showTitle.lowercase(java.util.Locale.ROOT))}"
                            shows.putIfAbsent(showId, entry.copy(id = showId, providerId = showId, kind = MediaKind.SERIES, title = showTitle, uri = null))
                            episodes.getOrPut(showId) { mutableListOf() }.add(entry.copy(parentLocalId = showId))
                        } else all += entry
                        entryCount++
                        attrs = emptyMap(); title = ""; group = ""; headers = mutableMapOf()
                    }
                }
            }
        }
        if (!sawHeader) throw SourceFailure("Geçerli bir M3U listesi bulunamadı.")
        val warning = buildList {
            if (invalid > 0) add("$invalid geçersiz adres atlandı.")
            if (droppedFeeds) add("Listenin ilk dört yayın rehberi adresi alındı.")
        }
        return Catalog((all + shows.values).distinctBy { it.id }, episodes.mapValues { (_, values) ->
            values.distinctBy { it.id }.sortedWith(compareBy({ it.season }, { it.episode }))
        }, warning, epgUrls.toList())
    }
}
