package dev.iptvplayer.tv.data

import androidx.annotation.StringRes
import dev.iptvplayer.tv.R
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.security.MessageDigest
import java.util.UUID

enum class SourceType { XTREAM, M3U }
enum class MediaKind(@StringRes val label: Int) {
    LIVE(R.string.kind_live), MOVIE(R.string.kind_movies), SERIES(R.string.kind_series), EPISODE(R.string.kind_episodes)
}

@androidx.compose.runtime.Immutable
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
            ?: throw SourceFailure(FailureReason.ADDRESS_INVALID)
        if (url.username.isNotEmpty() || url.password.isNotEmpty()) {
            throw SourceFailure(FailureReason.ADDRESS_HAS_CREDENTIALS)
        }
        if (type == SourceType.XTREAM && (username.isBlank() || password.isEmpty())) {
            throw SourceFailure(FailureReason.CREDENTIALS_MISSING)
        }
        val guide = epgUrl.trim().takeIf { it.isNotEmpty() }?.let {
            it.toHttpUrlOrNull()?.takeIf { value -> value.username.isEmpty() && value.password.isEmpty() }
                ?: throw SourceFailure(FailureReason.GUIDE_ADDRESS_INVALID)
        }
        // The setup form names an empty field in the app's language (source_unnamed); this is for a record without one.
        return copy(name = name.trim().ifEmpty { "Kaynağım" }, address =
            if (type == SourceType.XTREAM) xtreamBase(url).toString() else url.toString(),
            username = username.trim(), epgUrl = guide?.toString().orEmpty())
    }

    /**
     * This M3U link as the Xtream account it exports, when it is a panel's `get.php` link carrying the username and the
     * password; null otherwise. The id, name and guide stay.
     */
    fun asXtreamAccount(): SourceConfig? {
        if (type != SourceType.M3U) return null
        val url = address.trim().toHttpUrlOrNull() ?: return null
        if (!url.pathSegments.lastOrNull().equals("get.php", ignoreCase = true)) return null
        val user = url.queryParameter("username")?.takeIf { it.isNotBlank() } ?: return null
        val secret = url.queryParameter("password")?.takeIf { it.isNotEmpty() } ?: return null
        return copy(type = SourceType.XTREAM, address = xtreamBase(url).toString(), username = user, password = secret)
    }

    fun apiUrl(action: String? = null, extra: Pair<String, String>? = null): HttpUrl {
        val base = xtreamBase(address.toHttpUrlOrNull() ?: throw SourceFailure(FailureReason.SOURCE_ADDRESS_INVALID))
        return base.newBuilder().addPathSegment("player_api.php")
            .addQueryParameter("username", username).addQueryParameter("password", password)
            .apply {
                action?.let { addQueryParameter("action", it) }
                extra?.let { addQueryParameter(it.first, it.second) }
            }.build()
    }

    fun xmltvUrl(): HttpUrl {
        require(type == SourceType.XTREAM)
        return xtreamBase(address.toHttpUrlOrNull() ?: throw SourceFailure(FailureReason.SOURCE_ADDRESS_INVALID))
            .newBuilder().addPathSegment("xmltv.php").addQueryParameter("username", username)
            .addQueryParameter("password", password).build()
    }

    fun streamUrl(entry: MediaEntry): String {
        entry.uri?.let { return it }
        val path = when (entry.kind) {
            MediaKind.LIVE -> "live"
            MediaKind.MOVIE -> "movie"
            MediaKind.EPISODE -> "series"
            MediaKind.SERIES -> throw SourceFailure(FailureReason.EPISODE_NOT_CHOSEN)
        }
        val extension = entry.extension.takeIf { it.matches(Regex("[a-zA-Z0-9]{1,8}")) }
            ?: if (entry.kind == MediaKind.LIVE) "ts" else throw SourceFailure(FailureReason.STREAM_FORMAT_MISSING)
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

@androidx.compose.runtime.Immutable
data class MediaEntry(
    val id: String,
    val providerId: String,
    val kind: MediaKind,
    val title: String,
    val category: String = CategoryFilter.OTHER,
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

@androidx.compose.runtime.Immutable
data class Catalog(
    val items: List<MediaEntry>,
    val m3uEpisodes: Map<String, List<MediaEntry>> = emptyMap(),
    val warnings: List<CatalogWarning> = emptyList(),
    val epgUrls: List<String> = emptyList(),
    /** Category names per kind in the provider's own order (Xtream category lists). Empty: first appearance. */
    val categoryOrder: Map<MediaKind, List<String>> = emptyMap()
) {
    override fun toString() = "Catalog(items=${items.size}, groups=${m3uEpisodes.size}, feeds=${epgUrls.size})"
}

/**
 * A part of a list that could not be read while the rest of it opened. Snapshots keep the reason and its argument,
 * never a sentence, so the note follows the app's language.
 */
data class CatalogWarning(val reason: Reason, val kind: MediaKind? = null, val count: Int = 0) {
    enum class Reason { CATEGORIES_UNAVAILABLE, KIND_UNAVAILABLE, INVALID_ADDRESSES, FEEDS_LIMITED }
    init { require(kind != null || (reason != Reason.CATEGORIES_UNAVAILABLE && reason != Reason.KIND_UNAVAILABLE)) }

    val text: UiText get() = when (reason) {
        Reason.CATEGORIES_UNAVAILABLE -> UiText.Res(R.string.warning_categories_unavailable, listOf(UiText.Res(kind!!.label)))
        Reason.KIND_UNAVAILABLE -> UiText.Res(R.string.warning_kind_unavailable, listOf(UiText.Res(kind!!.label)))
        // The count goes in as text: Android formats %d with the device's locale, which on some devices would
        // write the digits in another script. Number formats belong to the app's language (gap plan S03).
        Reason.INVALID_ADDRESSES -> UiText.Plural(R.plurals.warning_invalid_addresses, count, listOf(count.toString()))
        Reason.FEEDS_LIMITED -> UiText.Res(R.string.warning_feeds_limited)
    }
}

/**
 * Category keys the app itself puts in the catalogue and in its state. They are keys, never shown as they are: the
 * screens show them in the app's language.
 */
object CategoryFilter {
    /**
     * Every title of the section: the library's selection before a category is chosen. It lives only in the model's
     * state, never on the device; like the library's "@favorites" and "@history" no provider category can take it.
     */
    const val ALL = "@all"
    /**
     * Titles the provider put in no category. Snapshots and hidden-category keys (D068) store this Turkish word, so
     * it stays the value it always was.
     */
    const val OTHER = "Diğer"
}

data class PlaybackMark(val positionMs: Long, val durationMs: Long, val updatedAt: Long) {
    val completed: Boolean get() = durationMs > 0 && positionMs.toDouble() / durationMs >= .95
    val resumeMs: Long get() = if (completed) 0 else positionMs
    /**
     * Worth resuming: past 2 % of the title (at most 2 minutes; 2 minutes when the length is unknown). A title opened
     * by mistake neither offers Devam et nor takes a place in İzlemeye devam et.
     */
    val resumable: Boolean get() = resumeMs > 0 &&
        resumeMs >= if (durationMs > 0) minOf(RESUME_FLOOR_MS, durationMs / 50) else RESUME_FLOOR_MS

    companion object { const val RESUME_FLOOR_MS = 120_000L }
}

/** Why a source operation failed. Each reason has one fixed message; tests compare reasons, not sentences. */
enum class FailureReason(@StringRes val message: Int) {
    // The source form, the device's records and the model's own checks.
    ADDRESS_INVALID(R.string.failure_address_invalid),
    ADDRESS_HAS_CREDENTIALS(R.string.failure_address_has_credentials),
    CREDENTIALS_MISSING(R.string.failure_credentials_missing),
    GUIDE_ADDRESS_INVALID(R.string.failure_guide_address_invalid),
    SOURCE_ADDRESS_INVALID(R.string.failure_source_address_invalid),
    EPISODE_NOT_CHOSEN(R.string.failure_episode_not_chosen),
    STREAM_FORMAT_MISSING(R.string.failure_stream_format_missing),
    STORED_SOURCES_UNAVAILABLE(R.string.failure_stored_sources_unavailable),
    SOURCE_NOT_STORED(R.string.failure_source_not_stored),
    SOURCES_NOT_STORED(R.string.failure_sources_not_stored),
    SOURCE_NOT_REMOVED(R.string.failure_source_not_removed),
    CONNECTION_ALREADY_ADDED(R.string.failure_connection_already_added),
    CONNECTION_ADDED_ELSEWHERE(R.string.failure_connection_added_elsewhere),
    SOURCE_NO_LONGER_SAVED(R.string.failure_source_no_longer_saved),
    SOURCE_CHANGED(R.string.failure_source_changed),
    SOURCE_CHANGED_IN_SETTINGS(R.string.failure_source_changed_in_settings),
    PARTIAL_IMPORT(R.string.failure_partial_import),
    PARTIAL_REFRESH(R.string.failure_partial_refresh),
    // The provider's account and catalogue.
    ACCOUNT_INFO_MISSING(R.string.failure_account_info_missing),
    ACCOUNT_NOT_AUTHENTICATED(R.string.failure_account_not_authenticated),
    ACCOUNT_INACTIVE(R.string.failure_account_inactive),
    CATALOG_UNAVAILABLE(R.string.failure_catalog_unavailable),
    MOVIE_DETAILS_UNAVAILABLE(R.string.failure_movie_details_unavailable),
    EPISODES_UNAVAILABLE(R.string.failure_episodes_unavailable),
    RESPONSE_MALFORMED(R.string.failure_response_malformed),
    SERVER_UNREACHABLE(R.string.failure_server_unreachable),
    SERVER_NAME_NOT_FOUND(R.string.failure_server_name_not_found),
    SERVER_REFUSED(R.string.failure_server_refused),
    SERVER_TIMEOUT(R.string.failure_server_timeout),
    SERVER_CONNECT_TIMEOUT(R.string.failure_server_connect_timeout),
    SECURE_CONNECTION_FAILED(R.string.failure_secure_connection),
    ACCESS_DENIED(R.string.failure_access_denied),
    SOURCE_NOT_FOUND(R.string.failure_source_not_found),
    TOO_MANY_REQUESTS(R.string.failure_too_many_requests),
    PROVIDER_UNAVAILABLE(R.string.failure_provider_unavailable),
    RESPONSE_EMPTY(R.string.failure_response_empty),
    RESPONSE_TOO_LARGE(R.string.failure_response_too_large),
    SOURCE_UNREADABLE(R.string.failure_source_unreadable),
    LIST_ADDRESS_INVALID(R.string.failure_list_address_invalid),
    LIST_TOO_LARGE(R.string.failure_list_too_large),
    LIST_SINGLE_STREAM(R.string.failure_list_single_stream),
    LIST_NOT_M3U(R.string.failure_list_not_m3u),
    // Guide feeds and their XMLTV.
    GUIDE_TOO_MANY_FEEDS(R.string.failure_guide_too_many_feeds),
    GUIDE_FEED_INVALID(R.string.failure_guide_feed_invalid),
    GUIDE_ENTRIES_UNREADABLE(R.string.failure_guide_entries_unreadable),
    GUIDE_NOT_SAVED(R.string.failure_guide_not_saved),
    GUIDE_ACCESS_DENIED(R.string.failure_guide_access_denied),
    GUIDE_NOT_FOUND(R.string.failure_guide_not_found),
    GUIDE_TOO_MANY_REQUESTS(R.string.failure_guide_too_many_requests),
    GUIDE_PROVIDER_UNAVAILABLE(R.string.failure_guide_provider_unavailable),
    GUIDE_RESPONSE_EMPTY(R.string.failure_guide_response_empty),
    GUIDE_RESPONSE_TOO_LARGE(R.string.failure_guide_response_too_large),
    GUIDE_DOWNLOAD_FAILED(R.string.failure_guide_download_failed),
    GUIDE_EXTERNAL_DEFINITION(R.string.failure_guide_external_definition),
    GUIDE_NOT_XMLTV(R.string.failure_guide_not_xmltv),
    GUIDE_UNREADABLE(R.string.failure_guide_unreadable),
    GUIDE_TOO_DEEP(R.string.failure_guide_too_deep),
    GUIDE_CHANNEL_ID_TOO_LONG(R.string.failure_guide_channel_id_too_long),
    GUIDE_TOO_MANY_CHANNELS(R.string.failure_guide_too_many_channels),
    GUIDE_TOO_MANY_PROGRAMMES(R.string.failure_guide_too_many_programmes),
    GUIDE_TEXT_TOO_LONG(R.string.failure_guide_text_too_long),
    GUIDE_ENCODING_UNSUPPORTED(R.string.failure_guide_encoding_unsupported),
    GUIDE_TOO_LARGE(R.string.failure_guide_too_large),
    GUIDE_PROLOG_TOO_LONG(R.string.failure_guide_prolog_too_long),
    GUIDE_PROLOG_MISSING(R.string.failure_guide_prolog_missing),
    GUIDE_INTERNAL_DEFINITIONS(R.string.failure_guide_internal_definitions),
    GUIDE_DOCTYPE_UNSUPPORTED(R.string.failure_guide_doctype_unsupported)
}

/** Only fixed reasons leave the network layer; the message is the reason's name. Never carry a raw URL/cause. */
class SourceFailure(val reason: FailureReason, vararg val args: Any) : Exception(reason.name) {
    val text: UiText get() = UiText.Res(reason.message, args.toList())
}

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
