package dev.iptvplayer.tv

import android.os.SystemClock
import androidx.test.platform.app.InstrumentationRegistry
import dev.iptvplayer.tv.data.*
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.Ignore
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/** In-memory experiment only. Not a cache format: no migration, limits, groups or store integration. */
@Ignore("Historical v1 experiment: production encoder is now v2. Use CatalogStageProbeTest and archived baseline.")
class CompactCatalogExperimentTest {
    private fun encode(items: List<MediaEntry>): ByteArray = ByteArrayOutputStream().use { out ->
        GZIPOutputStream(out).bufferedWriter(Charsets.UTF_8).use { writer ->
            items.forEach { e ->
                writer.appendLine(JSONArray(listOf(e.id,e.providerId,e.kind.name,e.title,e.category,e.artwork,
                    e.backdrop,e.description,e.year,e.durationSeconds,e.extension,e.uri,JSONObject(e.headers),
                    e.season,e.episode,e.adult,e.episodeOrderKnown,e.epgId,e.epgName)).toString())
            }
        }
        out.toByteArray()
    }
    private fun decode(bytes: ByteArray): List<MediaEntry> = GZIPInputStream(bytes.inputStream()).bufferedReader(Charsets.UTF_8).use { reader ->
        reader.lineSequence().map { line ->
            val a=JSONArray(line)
            fun optional(i: Int)=if(a.isNull(i)) null else a.getString(i)
            val headers=a.getJSONObject(12)
            MediaEntry(a.getString(0),a.getString(1),MediaKind.valueOf(a.getString(2)),a.getString(3),a.getString(4),
                optional(5),optional(6),a.getString(7),a.getString(8),a.getLong(9),a.getString(10),optional(11),
                headers.keys().asSequence().associateWith { headers.getString(it) },a.getInt(13),a.getInt(14),
                a.getBoolean(15),a.getBoolean(16),epgId=a.getString(17),epgName=a.getString(18))
        }.toList()
    }
    @Test fun compareInMemoryFormatsWithFullEntryEquality() {
        assumeTrue(android.os.Build.HARDWARE.contains("ranchu") || android.os.Build.FINGERPRINT.contains("generic"))
        val entries=(1..60000).map { n -> MediaEntry("item:$n","$n",MediaKind.entries[n%4],
            "Işık, İstanbul ve dünya $n","Kategori ${n%100}",
            artwork=if(n%3==0) "http://127.0.0.1:1/a.jpg" else null,
            backdrop=if(n%5==0) "http://127.0.0.1:1/b.jpg" else null,
            description=if(n%4==0) "İlk satır\nİkinci satır: \"örnek\"" else "",year="2026",durationSeconds=3600,
            extension="mp4",uri=if(n%2==0) "http://127.0.0.1:1/$n.mp4" else null,
            headers=if(n%7==0) mapOf("User-Agent" to "Fixture/1") else emptyMap(),season=n%20,episode=n%80,
            adult=n%13==0,episodeOrderKnown=n%2==0,epgId="epg:$n",epgName="Kanal $n") }
        val samples=JSONArray()
        fun <T> measure(pass:Int,stage:String,work:()->T):T {
            val start=SystemClock.elapsedRealtimeNanos();val result=work()
            samples.put(JSONObject().put("pass",pass).put("stage",stage).put("ms",(SystemClock.elapsedRealtimeNanos()-start)/1000000.0))
            return result
        }
        var originalSize=0;var compactSize=0
        repeat(3) { pass ->
            val order=if(pass%2==0) listOf(false,true) else listOf(true,false)
            for(compact in order) {
                val name=if(compact) "array" else "object"
                val bytes=measure(pass,"$name-encode") { if(compact) encode(entries) else CatalogSnapshotCodec.encode(CatalogSnapshot(Catalog(entries),1)) }
                val restored=measure(pass,"$name-decode") { if(compact) decode(bytes) else CatalogSnapshotCodec.decode(bytes).catalog.items }
                assertEquals("Every MediaEntry field survives",entries,restored)
                if(compact) compactSize=bytes.size else originalSize=bytes.size
                bytes.fill(0)
            }
        }
        val result=JSONObject().put("build",BuildConfig.VERSION_NAME).put("entries",entries.size)
            .put("scope","In-memory experimental rows only; excludes group/header compatibility, limits, encryption, UI and device performance gates. Alternating order; no claim of cold samples.")
            .put("objectBytes",originalSize).put("arrayBytes",compactSize).put("samples",samples)
        InstrumentationRegistry.getInstrumentation().targetContext.filesDir.resolve("compact-catalog-experiment.json").writeText(result.toString(2))
    }
}
