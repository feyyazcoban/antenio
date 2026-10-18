package dev.iptvplayer.tv.data

import dev.iptvplayer.tv.R
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Response
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class SourceRepository(private val client: OkHttpClient = client()) {
    suspend fun load(source: SourceConfig, onProgress: (UiText) -> Unit): Catalog = withContext(Dispatchers.IO) {
        if (source.type == SourceType.M3U) {
            onProgress(UiText.Res(R.string.progress_downloading_list))
            val text = get(source.address, 64 * 1024 * 1024)
            coroutineContext.ensureActive()
            onProgress(UiText.Res(R.string.progress_arranging_list))
            return@withContext M3uParser.parse(text.reader(), source.address)
        }
        onProgress(UiText.Res(R.string.progress_checking_account))
        val auth = jsonObject(get(source.apiUrl()))
        val user = auth.optJSONObject("user_info") ?: throw SourceFailure(FailureReason.ACCOUNT_INFO_MISSING)
        if (user.optString("auth") != "1") throw SourceFailure(FailureReason.ACCOUNT_NOT_AUTHENTICATED)
        if (user.optString("status").isNotEmpty() && !user.optString("status").equals("Active", true)) {
            throw SourceFailure(FailureReason.ACCOUNT_INACTIVE)
        }
        val entries = mutableListOf<MediaEntry>()
        val warnings = mutableListOf<CatalogWarning>()
        val categoryOrder = mutableMapOf<MediaKind, List<String>>()
        var loadedKinds = 0
        for (kind in listOf(MediaKind.LIVE, MediaKind.MOVIE, MediaKind.SERIES)) {
            coroutineContext.ensureActive()
            onProgress(UiText.Res(R.string.progress_loading_kind, listOf(UiText.Res(kind.label))))
            val prefix = when (kind) { MediaKind.LIVE -> "live"; MediaKind.MOVIE -> "vod"; else -> "series" }
            val cats = try {
                CatalogParser.categories(get(source.apiUrl("get_${prefix}_categories")))
            } catch (e: CancellationException) { throw e } catch (_: Exception) {
                warnings += CatalogWarning(CatalogWarning.Reason.CATEGORIES_UNAVAILABLE, kind)
                emptyMap()
            }
            categoryOrder[kind] = cats.values.distinct()
            try {
                val action = if (kind == MediaKind.SERIES) "get_series" else "get_${prefix}_streams"
                entries += CatalogParser.xtream(get(source.apiUrl(action)), kind, cats)
                loadedKinds++
            } catch (e: CancellationException) { throw e } catch (_: Exception) {
                warnings += CatalogWarning(CatalogWarning.Reason.KIND_UNAVAILABLE, kind)
            }
        }
        if (loadedKinds == 0) throw SourceFailure(FailureReason.CATALOG_UNAVAILABLE)
        Catalog(entries, warnings = warnings, categoryOrder = categoryOrder)
    }

    suspend fun movie(source: SourceConfig, entry: MediaEntry): MediaEntry = withContext(Dispatchers.IO) {
        if (source.type == SourceType.M3U) return@withContext entry
        try { CatalogParser.movie(get(source.apiUrl("get_vod_info", "vod_id" to entry.providerId)), entry) }
        catch (e: CancellationException) { throw e }
        catch (e: SourceFailure) { throw e }
        catch (_: Exception) { throw SourceFailure(FailureReason.MOVIE_DETAILS_UNAVAILABLE) }
    }

    suspend fun episodes(source: SourceConfig, series: MediaEntry, catalog: Catalog): Pair<MediaEntry, List<MediaEntry>> = withContext(Dispatchers.IO) {
        if (source.type == SourceType.M3U) return@withContext series to catalog.m3uEpisodes[series.id].orEmpty()
        try { CatalogParser.episodes(get(source.apiUrl("get_series_info", "series_id" to series.providerId)), series) }
        catch (e: CancellationException) { throw e }
        catch (e: SourceFailure) { throw e }
        catch (_: Exception) { throw SourceFailure(FailureReason.EPISODES_UNAVAILABLE) }
    }

    private fun jsonObject(text: String): JSONObject = try { JSONObject(text) }
        catch (_: Exception) { throw SourceFailure(FailureReason.RESPONSE_MALFORMED) }

    private suspend fun get(url: HttpUrl): String = get(url.toString())

    private suspend fun get(url: String, maxBytes: Int = 32 * 1024 * 1024): String {
        try {
            val call = client.newCall(SourceHttp.list(url))
            val response = suspendCancellableCoroutine<Response> { continuation ->
                continuation.invokeOnCancellation { call.cancel() }
                call.enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        if (continuation.isActive) continuation.resumeWithException(SourceFailure(FailureReason.SERVER_UNREACHABLE))
                    }
                    override fun onResponse(call: Call, response: Response) {
                        continuation.resume(response, onCancellation = { _, value, _ -> value.close() })
                    }
                })
            }
            return response.use {
                if (!it.isSuccessful) throw SourceFailure(when (it.code) {
                    401, 403 -> FailureReason.ACCESS_DENIED
                    404 -> FailureReason.SOURCE_NOT_FOUND
                    429 -> FailureReason.TOO_MANY_REQUESTS
                    else -> FailureReason.PROVIDER_UNAVAILABLE
                })
                val body = it.body ?: throw SourceFailure(FailureReason.RESPONSE_EMPTY)
                if (body.contentLength() > maxBytes) throw SourceFailure(FailureReason.RESPONSE_TOO_LARGE)
                val output = ByteArrayOutputStream()
                body.byteStream().use { input ->
                    val buffer = ByteArray(16 * 1024)
                    while (true) {
                        coroutineContext.ensureActive()
                        val count = input.read(buffer)
                        if (count < 0) break
                        if (output.size() + count > maxBytes) throw SourceFailure(FailureReason.RESPONSE_TOO_LARGE)
                        output.write(buffer, 0, count)
                    }
                }
                output.toString("UTF-8").removePrefix("\uFEFF")
            }
        } catch (e: CancellationException) { throw e }
        catch (e: SourceFailure) { throw e }
        catch (_: Exception) { throw SourceFailure(FailureReason.SOURCE_UNREADABLE) }
    }

    companion object {
        fun client(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS).readTimeout(45, TimeUnit.SECONDS)
            .callTimeout(120, TimeUnit.SECONDS).followSslRedirects(false).build()
    }
}
