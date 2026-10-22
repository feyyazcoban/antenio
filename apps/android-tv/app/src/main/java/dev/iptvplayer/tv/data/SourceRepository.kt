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
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Why a connection to the provider failed, in words the viewer can act on: the name was not found (a typo, or a network
 * that does not resolve it), the server refused the port, it did not answer in time, or the secure handshake failed.
 * Anything else stays "Sunucuya ulaşılamadı".
 */
internal fun networkFailure(e: IOException): FailureReason = when {
    e is UnknownHostException -> FailureReason.SERVER_NAME_NOT_FOUND
    // Android's connect timeout reads "failed to connect to … after 15000ms"; a read timeout reads "timeout".
    e is SocketTimeoutException && e.message.orEmpty().contains("connect", ignoreCase = true) -> FailureReason.SERVER_CONNECT_TIMEOUT
    e is InterruptedIOException -> FailureReason.SERVER_TIMEOUT
    e is SSLException -> FailureReason.SECURE_CONNECTION_FAILED
    e is ConnectException -> FailureReason.SERVER_REFUSED
    else -> FailureReason.SERVER_UNREACHABLE
}

class SourceRepository(client: OkHttpClient? = null) {
    // All requests enter through IO; do not initialize TLS while constructing the ViewModel.
    private val client by lazy { client ?: Companion.client() }
    /**
     * The whole lists (an M3U, an Xtream section): some providers build them for a minute or more before the first byte,
     * and a large one downloads for minutes. Other players wait that long; the loading card keeps İptal meanwhile.
     */
    private val patientClient by lazy {
        this.client.newBuilder().readTimeout(PATIENT_READ_SECONDS, TimeUnit.SECONDS).callTimeout(PATIENT_CALL_MINUTES, TimeUnit.MINUTES).build()
    }
    suspend fun load(source: SourceConfig, onProgress: (UiText) -> Unit): Catalog = withContext(Dispatchers.IO) {
        if (source.type == SourceType.M3U) {
            onProgress(UiText.Res(R.string.progress_downloading_list))
            val text = get(source.address, 64 * 1024 * 1024, patient = true)
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
                entries += CatalogParser.xtream(get(source.apiUrl(action), patient = true), kind, cats)
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

    private suspend fun get(url: HttpUrl, patient: Boolean = false): String = get(url.toString(), patient = patient)

    private suspend fun get(url: String, maxBytes: Int = 32 * 1024 * 1024, patient: Boolean = false): String {
        try {
            val call = (if (patient) patientClient else client).newCall(SourceHttp.list(url))
            val response = suspendCancellableCoroutine<Response> { continuation ->
                continuation.invokeOnCancellation { call.cancel() }
                call.enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        if (continuation.isActive) continuation.resumeWithException(SourceFailure(networkFailure(e)))
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
        // The list stopped arriving partway (a read or the whole call ran out of time).
        catch (_: InterruptedIOException) { throw SourceFailure(FailureReason.SERVER_TIMEOUT) }
        catch (_: Exception) { throw SourceFailure(FailureReason.SOURCE_UNREADABLE) }
    }

    companion object {
        const val PATIENT_READ_SECONDS = 120L
        const val PATIENT_CALL_MINUTES = 10L

        fun client(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS).readTimeout(45, TimeUnit.SECONDS)
            .callTimeout(120, TimeUnit.SECONDS).followSslRedirects(false).build()
    }
}
