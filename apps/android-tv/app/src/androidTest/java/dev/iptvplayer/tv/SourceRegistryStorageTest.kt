package dev.iptvplayer.tv

import android.content.Context
import android.util.Base64
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.iptvplayer.tv.data.*
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

/** Isolated local storage tests; emulator only, no provider calls or Activity. */
@RunWith(AndroidJUnit4::class)
class SourceRegistryStorageTest {
    private lateinit var context: Context
    private lateinit var store: DeviceStore
    private val a = SourceConfig("source:a", "Ev", SourceType.XTREAM, "http://127.0.0.1:1/", "fixture-user", "fixture-password")
    private val b = a.copy(id="source", name="Diğer", address="http://127.0.0.1:2/")
    private val item = MediaEntry("movie:1", "1", MediaKind.MOVIE, "Film", extension="mp4")
    private val prefs get() = context.getSharedPreferences("device_state",0)

    @Before fun setup() {
        Assume.assumeTrue(android.os.Build.HARDWARE.contains("ranchu") || android.os.Build.FINGERPRINT.contains("generic"))
        context = InstrumentationRegistry.getInstrumentation().targetContext
        store = DeviceStore(context)
        runBlocking { SourceTransactions.shared.write { store.forgetSource(); CatalogStore(context).clear() } }
    }
    @After fun cleanup() {
        if (::context.isInitialized) runBlocking { SourceTransactions.shared.write {
            store.forgetSource(); CatalogStore(context).clear()
        } }
    }
    private fun save(registry: SourceRegistry) = runBlocking { SourceTransactions.shared.write { store.saveSources(registry) } }

    @Test fun versionOneCacheUpgradesToCompactAndStillAuthenticatesOwnerAndHeader() {
        val cache = CatalogStore(context)
        val bytes = java.io.ByteArrayOutputStream().also { out ->
            java.util.zip.GZIPOutputStream(out).use { zipped ->
                InstrumentationRegistry.getInstrumentation().context.assets.open("catalog-v1.jsonl").use { it.copyTo(zipped) }
            }
        }.toByteArray()
        val binding = stableId(listOf(a.id,a.type.name,a.address,a.username,a.password).joinToString("\u0000"))
        val file = cache.file(a)
        file.parentFile!!.mkdirs()
        file.writeBytes(store.seal(bytes,"catalog-v1:$binding"))
        val old = cache.read(a)!!
        assertEquals("Kanal", old.catalog.items.single().title)
        assertEquals(2, old.catalog.m3uEpisodes["show"]!!.size)
        assertEquals(emptyList<String>(),cache.readEpgFeeds(a))
        val next = old.copy(catalog = old.catalog.copy(epgUrls = listOf("http://127.0.0.1:1/guide.xml")))
        cache.write(a,next)
        assertEquals(next,cache.read(a))
        assertEquals(next.catalog.epgUrls,cache.readEpgFeeds(a))
        val encrypted = file.readBytes()
        val plain = store.unseal(encrypted,"catalog-v1:$binding")
        val header = java.util.zip.GZIPInputStream(plain.inputStream()).bufferedReader().use { JSONObject(it.readLine()) }
        assertEquals(CatalogSnapshotCodec.VERSION,header.getInt("version"));plain.fill(0)
        val corrupt = encrypted.copyOf().also { it[it.lastIndex] = (it.last().toInt() xor 1).toByte() }
        file.writeBytes(corrupt)
        assertNull(cache.read(a));assertNull(cache.readEpgFeeds(a))
        file.writeBytes(encrypted)
        cache.file(b).writeBytes(encrypted)
        assertNull(cache.read(b));assertNull(cache.readEpgFeeds(b))
        assertEquals(next,cache.read(a))
    }

    @Test fun fixedVersionTwoEncryptedCachePreservesOldContentAndNewIdentityFields() {
        val cache = CatalogStore(context)
        val bytes = java.io.ByteArrayOutputStream().also { out -> java.util.zip.GZIPOutputStream(out).use { zipped ->
            InstrumentationRegistry.getInstrumentation().context.assets.open("catalog-v2.jsonl").use { it.copyTo(zipped) }
        } }.toByteArray()
        val binding = stableId(listOf(a.id,a.type.name,a.address,a.username,a.password).joinToString("\u0000"))
        cache.file(a).apply { parentFile!!.mkdirs(); writeBytes(store.seal(bytes, "catalog-v1:$binding")) }
        val old = cache.read(a)!!
        assertEquals("movie:1", old.catalog.items.single().id)
        assertEquals(emptyList<String>(), cache.readEpgFeeds(a))
        assertNull(old.catalog.m3uEpisodes.getValue("series:2").single().parentLocalId)
        val next = old.copy(catalog = old.catalog.copy(items = old.catalog.items.map { it.copy(categoryId = "17") },
            m3uEpisodes = old.catalog.m3uEpisodes.mapValues { (_, episodes) -> episodes.map { it.copy(categoryId = "17", parentLocalId = "series:2") } }))
        cache.write(a, next)
        assertEquals(next, cache.read(a))
    }

    @Test fun firstRegistryMigrationPreservesCurrentCacheAndCleansOnlyUnownedOldFiles() {
        val cache = CatalogStore(context)
        val snapshot = CatalogSnapshot(Catalog(listOf(item)), 1234)
        cache.write(a, snapshot)
        val current = cache.file(a)
        val old = java.io.File(current.parentFile, current.name.substringAfter('-'))
        assertTrue(current.renameTo(old))
        val orphan = java.io.File(old.parentFile, "a".repeat(64) + ".bin.bak")
        orphan.writeBytes(byteArrayOf(1, 2, 3))
        cache.write(b, snapshot)
        cache.migrateLegacy(a)
        assertEquals(snapshot, cache.read(a)); assertEquals(snapshot, cache.read(b))
        assertTrue(current.exists()); assertFalse(old.exists()); assertFalse(orphan.exists())
        cache.migrateLegacy(a)
        assertEquals(snapshot, cache.read(a)); assertEquals(snapshot, cache.read(b))
    }

    @Test fun legacySourceMigratesWithFavoritesHistoryAndUnlistedOldProgress() {
        store.saveSource(a)
        store.favorites(a.id,setOf(item.id))
        val record = WatchRecord(item.id,MediaKind.MOVIE,positionMs=45000,durationMs=120000,updatedAt=9)
        store.history(a.id,listOf(record))
        val oldKey = "p:" + stableId("${a.id}:old-outside-history")
        prefs.edit().putString(oldKey,JSONObject().put("position",9000).put("duration",90000).put("time",1).toString()).commit()
        assertEquals(9000L,store.progress(a.id,"old-outside-history")!!.positionMs)
        assertNull(store.progress(b.id,"old-outside-history"))
        assertEquals(listOf(a),store.loadSources().sources)
        save(store.loadSources().upsert(b))
        val reopened = DeviceStore(context)
        assertEquals(listOf(a,b),reopened.loadSources().sources)
        assertFalse(prefs.contains("source")); assertFalse(prefs.contains(oldKey))
        assertEquals(setOf(item.id),reopened.favorites(a.id))
        assertEquals(listOf(record),reopened.history(a.id))
        assertEquals(9000L,reopened.progress(a.id,"old-outside-history")!!.positionMs)
    }

    @Test fun versionOneRegistryOpensAndTheNextSaveWritesVersionTwo() {
        val old = JSONObject().put("version", 1).put("filter", b.id).put("sources", org.json.JSONArray().apply {
            listOf(a, b).forEach { s -> put(JSONObject().put("id", s.id).put("name", s.name).put("type", s.type.name)
                .put("address", s.address).put("username", s.username).put("password", s.password).put("epgUrl", s.epgUrl)) }
        }).toString().toByteArray()
        prefs.edit().putString("sources_v2", Base64.encodeToString(store.seal(old, "source-registry-v1"), Base64.NO_WRAP)).commit()
        store.favorites(a.id, setOf(item.id))
        assertEquals(SourceRegistry(listOf(a, b), b.id), DeviceStore(context).loadSources())
        save(store.loadSources().copy(filterId = null))
        val plain = store.unseal(Base64.decode(prefs.getString("sources_v2", null), Base64.NO_WRAP), "source-registry-v1")
        assertEquals(SourceRegistryCodec.VERSION, JSONObject(plain.toString(Charsets.UTF_8)).getInt("version")); plain.fill(0)
        assertEquals(SourceRegistry(listOf(a, b)), DeviceStore(context).loadSources())
        assertEquals(setOf(item.id), store.favorites(a.id))
    }

    @Test fun savingTheRegistryRemovesKeysASourceNoLongerSavedLeftBehind() {
        save(SourceRegistry(listOf(a)))
        store.favorites(b.id, setOf(item.id)); store.history(b.id, listOf(WatchRecord(item.id, MediaKind.MOVIE)))
        store.saveProgress(b.id, item.id, 1000, 10000); store.saveProgress(a.id, item.id, 2000, 10000)
        store.reducedMotion = true
        save(store.loadSources())
        assertTrue(store.favorites(b.id).isEmpty()); assertTrue(store.history(b.id).isEmpty()); assertNull(store.progress(b.id, item.id))
        assertEquals(2000L, store.progress(a.id, item.id)!!.positionMs); assertTrue(store.reducedMotion)
        store.reducedMotion = false
    }

    @Test fun newerNamespacedProgressWinsOverAnOldLegacyCopy() {
        store.saveSource(a)
        prefs.edit().putString("p:"+stableId("${a.id}:${item.id}"),
            JSONObject().put("position",1000).put("duration",120000).put("time",1).toString()).commit()
        store.saveProgress(a.id,item.id,60000,120000)
        save(store.loadSources())
        assertEquals(60000L,DeviceStore(context).progress(a.id,item.id)!!.positionMs)
    }

    @Test fun deletingOneSourceKeepsCollidingIdsAndTheOtherCacheAndPreferences() {
        save(SourceRegistry(listOf(a,b),a.id))
        listOf(a,b).forEach { source ->
            store.favorites(source.id,setOf(item.id))
            store.history(source.id,listOf(WatchRecord(item.id,MediaKind.MOVIE)))
            store.saveProgress(source.id,item.id,if(source==a)1000 else 2000,10000)
        }
        store.reducedMotion=true; store.startupSound=false
        val cache=CatalogStore(context)
        runBlocking { SourceTransactions.shared.write {
            cache.write(a,CatalogSnapshot(Catalog(listOf(item)),1))
            cache.write(b,CatalogSnapshot(Catalog(listOf(item.copy(title="Diğer film"))),2))
            cache.remove(a); store.forgetSource(a.id)
        } }
        val reopened=DeviceStore(context)
        assertEquals(SourceRegistry(listOf(b)),reopened.loadSources())
        assertNull(cache.read(a)); assertEquals("Diğer film",cache.read(b)!!.catalog.items.single().title)
        assertNull(reopened.progress(a.id,item.id)); assertTrue(reopened.favorites(a.id).isEmpty()); assertTrue(reopened.history(a.id).isEmpty())
        assertEquals(2000L,reopened.progress(b.id,item.id)!!.positionMs)
        assertEquals(setOf(item.id),reopened.favorites(b.id)); assertEquals(1,reopened.history(b.id).size)
        assertTrue(reopened.reducedMotion); assertFalse(reopened.startupSound)
    }

    @Test fun clearingHistoryDoesNotClearAnotherSourcesProgressOrAnyFavorites() {
        save(SourceRegistry(listOf(a,b)))
        listOf(a,b).forEach { source ->
            store.history(source.id,listOf(WatchRecord(item.id,MediaKind.MOVIE)))
            store.saveProgress(source.id,item.id,2000,10000)
            store.favorites(source.id,setOf(item.id))
        }
        store.clearHistory(b.id)
        assertNull(store.progress(b.id,item.id)); assertTrue(store.history(b.id).isEmpty())
        assertEquals(2000L,store.progress(a.id,item.id)!!.positionMs); assertEquals(1,store.history(a.id).size)
        assertEquals(setOf(item.id),store.favorites(a.id)); assertEquals(setOf(item.id),store.favorites(b.id))
    }

    @Test fun registryIsEncryptedAndCorruptionDoesNotFallBackToObsoleteLegacyCredentials() {
        save(SourceRegistry(listOf(a,b)))
        val encrypted=prefs.getString("sources_v2",null)!!
        val raw=Base64.decode(encrypted,Base64.NO_WRAP).toString(Charsets.UTF_8)
        assertFalse(raw.contains(a.password)); assertFalse(raw.contains(a.username)); assertFalse(raw.contains(a.address))
        store.saveSource(a)
        val changed=Base64.decode(encrypted,Base64.NO_WRAP).also { it[it.lastIndex]=(it.last().toInt() xor 1).toByte() }
        prefs.edit().putString("sources_v2",Base64.encodeToString(changed,Base64.NO_WRAP)).commit()
        val failure=assertThrows(SourceFailure::class.java) { DeviceStore(context).loadSources() }
        assertFalse(failure.message!!.contains(a.password))
        assertTrue(prefs.contains("sources_v2"))
    }

    @Test fun editingOneSourcePreservesItsIdentityAndOtherAccounts() {
        save(SourceRegistry(listOf(a,b),b.id))
        store.saveProgress(a.id,item.id,5000,20000)
        val changed=a.copy(name="Salon",password="updated-fixture-password")
        save(store.loadSources().upsert(changed))
        val reopened=DeviceStore(context)
        assertEquals(SourceRegistry(listOf(changed,b),b.id),reopened.loadSources())
        assertEquals(5000L,reopened.progress(a.id,item.id)!!.positionMs)
    }

    @Test fun invalidReplacementIsRejectedBeforeAnySavedAccountIsChanged() {
        save(SourceRegistry(listOf(a,b),b.id))
        assertThrows(SourceFailure::class.java) { save(SourceRegistry(listOf(a.copy(address="file:///not-a-provider")))) }
        assertEquals(SourceRegistry(listOf(a,b),b.id),DeviceStore(context).loadSources())
    }

    @Test fun catalogReadsTheOldFilenameAndReplacesItOnlyAfterWritingTheNewOne() {
        val cache=CatalogStore(context)
        val snapshot=CatalogSnapshot(Catalog(listOf(item)),1)
        cache.write(a,snapshot)
        val current=cache.file(a)
        val old=java.io.File(current.parentFile,current.name.substringAfter('-'))
        assertTrue(current.renameTo(old))
        assertEquals(snapshot,cache.read(a))
        cache.write(a,snapshot.copy(updatedAt=2))
        assertFalse(old.exists()); assertTrue(current.exists())
        assertEquals(2L,cache.read(a)!!.updatedAt)
    }

    @Test fun sourceRemovalClearsAllOfItsCredentialVersionsWithoutTouchingAnotherOwner() {
        val cache=CatalogStore(context)
        val changed=a.copy(password="changed-test-password")
        val snapshot=CatalogSnapshot(Catalog(listOf(item)),1)
        listOf(a,changed,b).forEach { cache.write(it,snapshot) }
        assertNotNull(cache.read(a)); assertNotNull(cache.read(changed))
        cache.remove(changed)
        assertNull(cache.read(a)); assertNull(cache.read(changed))
        assertEquals(snapshot,cache.read(b))
    }
}
