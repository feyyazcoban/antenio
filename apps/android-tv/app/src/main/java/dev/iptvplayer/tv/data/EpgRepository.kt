package dev.iptvplayer.tv.data

import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.FilterInputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit
import java.util.concurrent.Executors
import java.util.zip.GZIPInputStream

class EpgRepository(private val client: OkHttpClient = OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS).callTimeout(180, TimeUnit.SECONDS).followSslRedirects(false).build()) {

    /** Reads feeds sequentially. A failed or cancelled feed never replaces the last complete guide.
     * accept is checked inside the shared source transaction; UI must validate its current source binding there. */
    suspend fun refresh(plan: EpgPlan, store: EpgStore, window: GuideWindow, accept: () -> Boolean,
        updatedAt: () -> Long = System::currentTimeMillis): Boolean =
        Executors.newSingleThreadExecutor { task -> Thread(task, "GuideWriter") }.asCoroutineDispatcher().use { writer -> withContext(writer) {
        // SQLite transaction state belongs to one physical thread, including seal/rollback after suspension.
        try {
            store.stage(plan, window).use { stage ->
                val reports = plan.urls.mapIndexed { index, url -> download(url, window, stage.sink(index), writer) }
                ensureActive()
                stage.seal(updatedAt(), reports)
                SourceTransactions.shared.write { accept() && store.publish(stage) }
            }
        } catch (e: CancellationException) { throw e }
        catch (e: SourceFailure) { throw e }
        catch (_: Exception) { ensureActive(); throw SourceFailure("Yayın rehberi kaydedilemedi. Son rehber korunuyor.") }
    } }

    internal suspend fun download(url: String, window: GuideWindow, sink: EpgSink,
        dispatcher: CoroutineDispatcher = Dispatchers.IO): EpgParseReport = coroutineScope {
        val call = client.newCall(Request.Builder().url(url).header("User-Agent", "IPTVPlayerPilot AndroidTV").build())
        // Stays alive through body parsing, unlike a continuation completed as soon as headers arrive.
        val cancellation = launch(Dispatchers.IO, start = CoroutineStart.UNDISPATCHED) {
            try { awaitCancellation() } finally { call.cancel() }
        }
        try {
            withContext(dispatcher) {
                ensureActive()
                call.execute().use { response ->
                    if (!response.isSuccessful) throw SourceFailure(when (response.code) {
                        401, 403 -> "Yayın rehberine erişim reddedildi. Kaynak bilgilerini kontrol et."
                        404 -> "Yayın rehberi adresi bulunamadı."
                        429 -> "Sağlayıcı yoğun. Yayın rehberini daha sonra yenileyebilirsin."
                        else -> "Yayın rehberi şu anda alınamıyor. Son rehber korunuyor."
                    })
                    val body = response.body ?: throw SourceFailure("Yayın rehberi yanıtı boş.")
                    if (body.contentLength() > MAX_BYTES) throw SourceFailure("Yayın rehberi boyut sınırını aşıyor.")
                    val raw = bounded(body.byteStream()).buffered()
                    raw.mark(2)
                    val gzip = raw.read() == 0x1f && raw.read() == 0x8b
                    raw.reset()
                    val decoded = bounded(if (gzip) GZIPInputStream(raw) else raw)
                    val context = currentCoroutineContext()
                    XmltvParser().parse(decoded, window, sink) { context.ensureActive() }
                }
            }
        } catch (e: CancellationException) { throw e }
        catch (e: SourceFailure) { currentCoroutineContext().ensureActive(); throw e }
        catch (_: Exception) { currentCoroutineContext().ensureActive(); throw SourceFailure("Yayın rehberi indirilemedi. Bağlantıyı kontrol et.") }
        finally { cancellation.cancel() }
    }

    private fun bounded(input: InputStream) = object : FilterInputStream(input) {
        var count = 0L
        fun accept(n: Int) { if (n > 0) count += n; if (count > MAX_BYTES) throw SourceFailure("Yayın rehberi boyut sınırını aşıyor.") }
        override fun read() = `in`.read().also { accept(if (it < 0) 0 else 1) }
        override fun read(b: ByteArray, off: Int, len: Int) = `in`.read(b, off, len).also { accept(it) }
    }
    companion object { private const val MAX_BYTES = 128L * 1024 * 1024 }
}
