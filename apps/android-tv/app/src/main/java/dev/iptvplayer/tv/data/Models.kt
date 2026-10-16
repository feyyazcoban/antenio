package dev.iptvplayer.tv.data

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.security.MessageDigest
import java.util.UUID

enum class SourceType { XTREAM, M3U }
enum class MediaKind(val label: String) {
    LIVE("Canlı TV"), MOVIE("Filmler"), SERIES("Diziler"), EPISODE("Bölümler")
}

data class SourceConfig(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: SourceType,
    val address: String,
    val username: String = "",
    val password: String = "",
    val epgUrl: String = ""
) {
    override fun toString() = "SourceConfig(id=$id, type=$type, credentials=REDACTED)"

    fun validated(): SourceConfig {
        val url = address.trim().toHttpUrlOrNull()
            ?: throw SourceFailure("http:// veya https:// ile başlayan geçerli bir adres gir.")
        if (url.username.isNotEmpty() || url.password.isNotEmpty()) {
            throw SourceFailure("Adres içinde kullanıcı bilgisi kullanma; hesap alanlarını doldur.")
        }
        if (type == SourceType.XTREAM && (username.isBlank() || password.isEmpty())) {
            throw SourceFailure("Kullanıcı adı ve şifre gerekli.")
        }
        val guide = epgUrl.trim().takeIf { it.isNotEmpty() }?.let {
            it.toHttpUrlOrNull()?.takeIf { value -> value.username.isEmpty() && value.password.isEmpty() }
                ?: throw SourceFailure("Rehber için geçerli bir http veya https adresi gir.")
        }
        return copy(name = name.trim().ifEmpty { "Kaynağım" }, address =
            if (type == SourceType.XTREAM) xtreamBase(url).toString() else url.toString(),
            username = username.trim(), epgUrl = guide?.toString().orEmpty())
    }

    fun apiUrl(action: String? = null, extra: Pair<String, String>? = null): HttpUrl {
        val base = xtreamBase(address.toHttpUrlOrNull() ?: throw SourceFailure("Kaynak adresi geçersiz."))
        return base.newBuilder().addPathSegment("player_api.php")
            .addQueryParameter("username", username).addQueryParameter("password", password)
            .apply {
                action?.let { addQueryParameter("action", it) }
                extra?.let { addQueryParameter(it.first, it.second) }
            }.build()
    }

    fun xmltvUrl(): HttpUrl {
        require(type == SourceType.XTREAM)
        return xtreamBase(address.toHttpUrlOrNull() ?: throw SourceFailure("Kaynak adresi geçersiz."))
            .newBuilder().addPathSegment("xmltv.php").addQueryParameter("username", username)
            .addQueryParameter("password", password).build()
    }

    fun streamUrl(entry: MediaEntry): String {
        entry.uri?.let { return it }
        val path = when (entry.kind) {
            MediaKind.LIVE -> "live"
            MediaKind.MOVIE -> "movie"
            MediaKind.EPISODE -> "series"
            MediaKind.SERIES -> throw SourceFailure("Önce bir bölüm seç.")
        }
        val extension = entry.extension.takeIf { it.matches(Regex("[a-zA-Z0-9]{1,8}")) }
            ?: if (entry.kind == MediaKind.LIVE) "ts" else throw SourceFailure("Bu içeriğin yayın biçimi eksik.")
        return xtreamBase(address.toHttpUrlOrNull()!!).newBuilder()
            .addPathSegment(path).addPathSegment(username).addPathSegment(password)
            .addPathSegment("${entry.providerId}.$extension").build().toString()
    }

    private fun xtreamBase(url: HttpUrl): HttpUrl {
        val builder = url.newBuilder().query(null).fragment(null)
        if (url.pathSegments.lastOrNull() in listOf("get.php", "player_api.php", "xmltv.php")) {
            builder.removePathSegment(url.pathSegments.lastIndex)
        }
        val result = builder.build()
        return if (result.encodedPath.endsWith("/")) result else result.newBuilder().addPathSegment("").build()
    }
}

data class MediaEntry(
    val id: String,
    val providerId: String,
    val kind: MediaKind,
    val title: String,
    val category: String = "Diğer",
    val artwork: String? = null,
    val backdrop: String? = null,
    val description: String = "",
    val year: String = "",
    val durationSeconds: Long = 0,
    val extension: String = "",
    val uri: String? = null,
    val headers: Map<String, String> = emptyMap(),
    val season: Int = 1,
    val episode: Int = 1,
    val adult: Boolean = false,
    val episodeOrderKnown: Boolean = false,
    val sourceId: String = "",
    val localId: String? = null,
    val epgId: String = "",
    val epgName: String = "",
    val categoryId: String = "",
    // Source-local series ID; never a title or a cross-source ContentRef.
    val parentLocalId: String? = null,
    /** Provider rating on a 0–10 scale; 0 when the provider gives none (M3U never does). */
    val rating: Float = 0f,
    /** When the provider added the title, in Unix seconds; 0 when unknown. */
    val addedAt: Long = 0
) {
    override fun toString() = "MediaEntry(id=$id, kind=$kind)"
}

data class Catalog(
    val items: List<MediaEntry>,
    val m3uEpisodes: Map<String, List<MediaEntry>> = emptyMap(),
    val warnings: List<String> = emptyList(),
    val epgUrls: List<String> = emptyList(),
    /** Category names per kind in the provider's own order (Xtream category lists). Empty: first appearance. */
    val categoryOrder: Map<MediaKind, List<String>> = emptyMap()
) {
    override fun toString() = "Catalog(items=${items.size}, groups=${m3uEpisodes.size}, feeds=${epgUrls.size})"
}

data class PlaybackMark(val positionMs: Long, val durationMs: Long, val updatedAt: Long) {
    val completed: Boolean get() = durationMs > 0 && positionMs.toDouble() / durationMs >= .95
    val resumeMs: Long get() = if (completed) 0 else positionMs
}

/** Only fixed, user-facing messages leave the network layer. Never carry a raw URL/cause. */
open class SourceFailure(message: String) : Exception(message)

fun stableId(value: String): String = MessageDigest.getInstance("SHA-256")
    .digest(value.toByteArray()).joinToString("") { "%02x".format(it) }

fun httpArtwork(value: String?): String? = value?.trim()
    ?.takeUnless { it.isEmpty() || it == "null" }?.toHttpUrlOrNull()?.toString()

fun durationSeconds(value: String): Long {
    value.toLongOrNull()?.let { return it.coerceAtLeast(0) }
    val parts = value.split(":").map { it.toLongOrNull() ?: return 0 }
    return when (parts.size) {
        3 -> parts[0] * 3600 + parts[1] * 60 + parts[2]
        2 -> parts[0] * 60 + parts[1]
        else -> 0
    }.coerceAtLeast(0)
}

internal class StoredSourcesUnavailable : SourceFailure("Kayıtlı kaynaklar açılamadı.")
