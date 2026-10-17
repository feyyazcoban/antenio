package dev.iptvplayer.tv.data

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class SourceRepository(private val client: OkHttpClient = client()) {
    suspend fun load(source: SourceConfig, onProgress: (String) -> Unit): Catalog = withContext(Dispatchers.IO) {
        if (source.type == SourceType.M3U) {
            onProgress("Liste indiriliyor…")
            val text = get(source.address, 64 * 1024 * 1024)
            coroutineContext.ensureActive()
            onProgress("Kanallar ve kütüphane düzenleniyor…")
            return@withContext M3uParser.parse(text.reader(), source.address)
        }
        onProgress("Hesap doğrulanıyor…")
        val auth = jsonObject(get(source.apiUrl()))
        val user = auth.optJSONObject("user_info") ?: throw SourceFailure("Sağlayıcı hesap bilgisi döndürmedi.")
        if (user.optString("auth") != "1") throw SourceFailure("Hesap doğrulanamadı. Kullanıcı adı ve şifreyi kontrol et.")
        if (user.optString("status").isNotEmpty() && !user.optString("status").equals("Active", true)) {
            throw SourceFailure("Bu hesap aktif görünmüyor. Sağlayıcı bilgilerini kontrol et.")
        }
        val entries = mutableListOf<MediaEntry>()
        val warnings = mutableListOf<String>()
        val categoryOrder = mutableMapOf<MediaKind, List<String>>()
        var loadedKinds = 0
        for (kind in listOf(MediaKind.LIVE, MediaKind.MOVIE, MediaKind.SERIES)) {
            coroutineContext.ensureActive()
            onProgress("${kind.label} yükleniyor…")
            val prefix = when (kind) { MediaKind.LIVE -> "live"; MediaKind.MOVIE -> "vod"; else -> "series" }
            val cats = try {
                CatalogParser.categories(get(source.apiUrl("get_${prefix}_categories")))
            } catch (e: CancellationException) { throw e } catch (_: Exception) {
                warnings += "${kind.label}: kategori adları alınamadı."
                emptyMap()
            }
            categoryOrder[kind] = cats.values.distinct()
            try {
                val action = if (kind == MediaKind.SERIES) "get_series" else "get_${prefix}_streams"
                entries += CatalogParser.xtream(get(source.apiUrl(action)), kind, cats)
                loadedKinds++
            } catch (e: CancellationException) { throw e } catch (_: Exception) {
                warnings += "${kind.label} yüklenemedi. Kaynağı yenileyerek tekrar deneyebilirsin."
            }
        }
        if (loadedKinds == 0) throw SourceFailure("Hesap doğrulandı ancak katalog alınamadı. Tekrar dene.")
        Catalog(entries, warnings = warnings, categoryOrder = categoryOrder)
    }

    suspend fun movie(source: SourceConfig, entry: MediaEntry): MediaEntry = withContext(Dispatchers.IO) {
        if (source.type == SourceType.M3U) return@withContext entry
        try { CatalogParser.movie(get(source.apiUrl("get_vod_info", "vod_id" to entry.providerId)), entry) }
        catch (e: CancellationException) { throw e }
        catch (e: SourceFailure) { throw e }
        catch (_: Exception) { throw SourceFailure("Film ayrıntıları alınamadı.") }
    }

    suspend fun episodes(source: SourceConfig, series: MediaEntry, catalog: Catalog): Pair<MediaEntry, List<MediaEntry>> = withContext(Dispatchers.IO) {
        if (source.type == SourceType.M3U) return@withContext series to catalog.m3uEpisodes[series.id].orEmpty()
        try { CatalogParser.episodes(get(source.apiUrl("get_series_info", "series_id" to series.providerId)), series) }
        catch (e: CancellationException) { throw e }
        catch (e: SourceFailure) { throw e }
        catch (_: Exception) { throw SourceFailure("Bölümler alınamadı. Tekrar dene.") }
    }

    private fun jsonObject(text: String): JSONObject = try { JSONObject(text) }
        catch (_: Exception) { throw SourceFailure("Sunucu beklenen biçimde yanıt vermedi.") }

    private suspend fun get(url: HttpUrl): String = get(url.toString())

    private suspend fun get(url: String, maxBytes: Int = 32 * 1024 * 1024): String {
        try {
            val request = Request.Builder().url(url).header("User-Agent", "NEXTV AndroidTV").build()
            val call = client.newCall(request)
            val response = suspendCancellableCoroutine<Response> { continuation ->
                continuation.invokeOnCancellation { call.cancel() }
                call.enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        if (continuation.isActive) continuation.resumeWithException(SourceFailure("Sunucuya ulaşılamadı. Adresi ve internet bağlantısını kontrol et."))
                    }
                    override fun onResponse(call: Call, response: Response) {
                        continuation.resume(response, onCancellation = { _, value, _ -> value.close() })
                    }
                })
            }
            return response.use {
                if (!it.isSuccessful) throw SourceFailure(when (it.code) {
                    401, 403 -> "Kaynağa erişim reddedildi. Hesap bilgilerini kontrol et."
                    404 -> "Kaynak adresi bulunamadı."
                    429 -> "Sağlayıcı çok fazla istek aldı. Biraz sonra tekrar dene."
                    else -> "Sağlayıcı şu anda yanıt veremiyor. Tekrar dene."
                })
                val body = it.body ?: throw SourceFailure("Sunucu boş yanıt verdi.")
                if (body.contentLength() > maxBytes) throw SourceFailure("Kaynak yanıtı pilot sürümün boyut sınırını aşıyor.")
                val output = ByteArrayOutputStream()
                body.byteStream().use { input ->
                    val buffer = ByteArray(16 * 1024)
                    while (true) {
                        coroutineContext.ensureActive()
                        val count = input.read(buffer)
                        if (count < 0) break
                        if (output.size() + count > maxBytes) throw SourceFailure("Kaynak yanıtı pilot sürümün boyut sınırını aşıyor.")
                        output.write(buffer, 0, count)
                    }
                }
                output.toString("UTF-8").removePrefix("\uFEFF")
            }
        } catch (e: CancellationException) { throw e }
        catch (e: SourceFailure) { throw e }
        catch (_: Exception) { throw SourceFailure("Kaynak okunamadı. İnternet bağlantısını ve adresi kontrol et.") }
    }

    companion object {
        fun client(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS).readTimeout(45, TimeUnit.SECONDS)
            .callTimeout(120, TimeUnit.SECONDS).followSslRedirects(false).build()
    }
}
