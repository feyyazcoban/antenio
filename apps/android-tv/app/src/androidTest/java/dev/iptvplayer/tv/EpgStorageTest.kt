package dev.iptvplayer.tv

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.iptvplayer.tv.data.*
import kotlinx.coroutines.*
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.io.File
import java.time.Instant
import java.util.concurrent.TimeUnit

/** Isolated emulator fixtures; no player, provider request, or real account is used. */
@RunWith(AndroidJUnit4::class)
class EpgStorageTest {
    private lateinit var context: Context
    private lateinit var store: EpgStore
    private val source = SourceConfig("epg-fixture-a", "Fixture", SourceType.M3U, "https://example.invalid/list?secret=private-marker")
    private val catalog = Catalog(emptyList(), epgUrls = listOf("https://example.invalid/epg?secret=private-marker"))
    private val plan get() = EpgPlan.create(source, catalog)!!
    private val window = GuideWindow(Instant.parse("2026-10-20T00:00:00Z").toEpochMilli(), Instant.parse("2026-11-03T00:00:00Z").toEpochMilli())
    private val channel = MediaEntry("l:1", "1", MediaKind.LIVE, "Örnek Haber", epgId = "news.fixture")

    @Before fun before() {
        Assume.assumeTrue(android.os.Build.HARDWARE.contains("ranchu") || android.os.Build.FINGERPRINT.contains("generic"))
        context = InstrumentationRegistry.getInstrumentation().targetContext
        store = EpgStore(context); store.clear()
    }
    @After fun after() { if (::store.isInitialized) store.clear() }
    private fun fixture(name: String) = InstrumentationRegistry.getInstrumentation().context.assets.open("epg/$name.xml")
    /** The fixture as a Turkish app on an English TV reads it. */
    private fun save(p: EpgPlan = plan, name: String = "europe", updated: Long = 1234) {
        store.stage(p, window).use { stage ->
            val report = fixture(name).use { XmltvParser(listOf("tr", "en")).parse(it, window, stage.sink(0)) }
            stage.seal(updated, listOf(report), listOf("tr", "en")); assertTrue(store.publish(stage))
        }
    }
    private fun fill(stage: EpgStore.Stage, title: String = "Replacement") {
        stage.sink(0).channel(EpgChannel("news.fixture", listOf("Örnek Haber")))
        stage.sink(0).programme(EpgProgramme("news.fixture", title, window.from + 1, window.from + 60_000))
        stage.seal(2345, listOf(EpgParseReport(1, 1, 0, 0)))
    }

    @Test fun androidParserAndIndexedSnapshotPreserveDstUnicodeAndUnknownEndAfterReopen() {
        save()
        val reopened = EpgStore(context)
        assertEquals(EpgSnapshotInfo(1234, window, 3, 3, 0, 0, listOf("tr", "en")), reopened.info(plan))
        val result = reopened.query(plan, channel, window)!!
        assertEquals(EpgMatchKind.ID, result.match.kind); assertEquals(3, result.programmes.size)
        assertEquals("İlk haber & gündem", result.programmes.first().title)
        assertEquals(3_600_000L, result.programmes[1].start - result.programmes[0].start)
        assertNull(result.programmes.last().end)
        assertTrue(File(context.cacheDir, "guide-staging").listFiles().orEmpty().isEmpty())
    }
    @Test fun aGuideSavedBeforeTheAppSpokeEnglishReadsAsTurkishAndStaysUsable() {
        save()
        // The snapshot table as 0.14.44 wrote it, without the languages its titles were picked in.
        SQLiteDatabase.openDatabase(File(context.filesDir, "guides/${plan.owner}-${plan.binding}.db").path, null, SQLiteDatabase.OPEN_READWRITE).use { db ->
            db.execSQL("CREATE TABLE before_s13 AS SELECT updated, from_time, until_time, channels, programmes, invalid, outside FROM snapshot")
            db.execSQL("DROP TABLE snapshot"); db.execSQL("ALTER TABLE before_s13 RENAME TO snapshot")
        }
        assertEquals(EpgSnapshotInfo(1234, window, 3, 3, 0, 0, listOf("tr")), store.info(plan))
        assertEquals("İlk haber & gündem", store.query(plan, channel, window)!!.programmes.first().title)
    }
    @Test fun matchingIsExactConservativeAndSourceScoped() {
        save()
        fun match(entry: MediaEntry) = store.query(plan, entry, window)!!.match.kind
        assertEquals(EpgMatchKind.ID, match(channel.copy(title = "Different title")))
        assertEquals(EpgMatchKind.MISSING_ID, match(channel.copy(epgId = "wrong")))
        assertEquals(EpgMatchKind.NAME, match(channel.copy(epgId = "", title = "  Örnek   Haber ")))
        assertEquals(EpgMatchKind.AMBIGUOUS, match(channel.copy(epgId = "", title = "Ortak Kanal")))
        assertEquals(EpgMatchKind.NONE, match(channel.copy(epgId = "", title = "Örnek Haber HD")))
        assertEquals(EpgMatchKind.AMBIGUOUS, match(channel.copy(epgId = "", epgName = "Örnek Haber", title = "Ortak Kanal")))
        assertEquals(EpgMatchKind.NONE, match(channel.copy(sourceId = "other-owner")))
        assertEquals(EpgMatchKind.ID, match(channel.copy(sourceId = source.id)))
        assertEquals(EpgMatchKind.NONE, match(channel.copy(kind = MediaKind.MOVIE)))
    }
    @Test fun channelsWithoutDeclarationsAreAvailableByIdOnAndroid() {
        save(name = "america")
        val result = store.query(plan, channel.copy(epgId = "without-definition.fixture"), window)!!
        assertEquals(EpgMatchKind.ID, result.match.kind); assertEquals(2, result.programmes.size)
        assertEquals(3_600_000L, result.programmes[0].end!! - result.programmes[0].start)
    }
    @Test fun multipleFeedsDoNotSilentlyMergeConflictingChannelIds() {
        val multi = EpgPlan.create(source, catalog.copy(epgUrls = catalog.epgUrls + "https://example.invalid/second"))!!
        store.stage(multi, window).use { stage ->
            val reports = (0..1).map { feed -> fixture("europe").use { XmltvParser().parse(it, window, stage.sink(feed)) } }
            stage.seal(99, reports); assertTrue(store.publish(stage))
        }
        assertEquals(EpgMatchKind.AMBIGUOUS, store.query(multi, channel, window)!!.match.kind)
        assertEquals(6, store.info(multi)!!.programmes)
    }
    @Test fun failedStageAndSupersededStageNeverReplaceLastGoodSnapshot() {
        save()
        store.stage(plan, window).use { bad ->
            assertThrows(SourceFailure::class.java) { XmltvParser().parse("<tv><programme>".byteInputStream(), window, bad.sink(0)) }
        }
        assertEquals(1234L, store.info(plan)!!.updatedAt)
        store.stage(plan, window).use { first ->
            fill(first, "Older request")
            store.stage(plan, window).use { second ->
                fill(second, "Latest request"); assertTrue(store.publish(second))
            }
            assertFalse(store.publish(first))
        }
        assertEquals("Latest request", store.query(plan, channel, window)!!.programmes.single().title)
    }
    @Test fun removalInvalidatesInFlightStageAndPreservesOtherOwnerAcrossStoreInstances() {
        val otherSource = source.copy(id = "epg-fixture-b")
        val other = EpgPlan.create(otherSource, catalog)!!
        save(); save(other)
        store.stage(plan, window).use { pending ->
            fill(pending)
            EpgStore(context).remove(source.id)
            assertFalse(store.publish(pending))
        }
        assertNull(store.info(plan)); assertNotNull(store.info(other))
    }
    @Test fun queriesBoundRowsKeepGapsAndExcludeEndedProgrammesAtWindowBoundary() {
        store.stage(plan, window).use { stage ->
            val sink = stage.sink(0)
            repeat(1_000) { n -> sink.programme(EpgProgramme("news.fixture", "Programme $n", window.from + n * 60_000L, window.from + n * 60_000L + 30_000)) }
            stage.seal(9, listOf(EpgParseReport(0, 1_000, 0, 0))); assertTrue(store.publish(stage))
        }
        val result = store.query(plan, channel, window, 64)!!
        assertEquals(64, result.programmes.size); assertTrue(result.truncated)
        val gap = store.query(plan, channel, GuideWindow(window.from + 30_000, window.from + 60_000))!!
        assertTrue(gap.programmes.isEmpty())
    }
    @Test fun credentialsAndFeedAddressesAreNotCopiedIntoGuideDatabase() {
        save()
        val files = File(context.filesDir, "guides").listFiles().orEmpty()
        assertEquals(1, files.size)
        assertFalse(files.single().name.contains("fixture"))
        val text = files.single().readBytes().toString(Charsets.ISO_8859_1)
        listOf("private-marker", "example.invalid", source.id).forEach { assertFalse(text.contains(it)) }
        val changed = EpgPlan.create(source, catalog, "https://example.invalid/changed")!!
        assertNull(store.info(changed))
    }
    @Test fun repositoryKeepsSnapshotOnHttpFailureAndRejectsUnacceptedSuccess() = runBlocking {
        save()
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setResponseCode(503))
            server.enqueue(MockResponse().setBody("<tv/>")); server.start()
            val localPlan = EpgPlan.create(source, catalog, server.url("/epg").toString())!!
            save(localPlan)
            try { EpgRepository().refresh(localPlan, store, window, { true }); fail("Expected error") } catch (_: SourceFailure) {}
            assertEquals(1234L, store.info(localPlan)!!.updatedAt)
            assertFalse(EpgRepository().refresh(localPlan, store, window, { false }))
            assertEquals(1234L, store.info(localPlan)!!.updatedAt)
        }
    }
    @Test fun sequentialFeedsKeepTheirSqliteTransactionAcrossSuspensions() = runBlocking {
        MockWebServer().use { server ->
            val xml = fixture("europe").bufferedReader().use { it.readText() }
            repeat(16) { server.enqueue(MockResponse().setBody(xml).setBodyDelay(5, TimeUnit.MILLISECONDS)) }
            server.start()
            val localPlan = EpgPlan.create(source, catalog.copy(epgUrls = listOf(server.url("/first").toString(), server.url("/second").toString())))!!
            withTimeout(15_000) {
                repeat(8) { n ->
                    assertTrue(EpgRepository().refresh(localPlan, store, window, { true }, { n.toLong() }))
                    assertEquals(6, store.info(localPlan)!!.programmes)
                    assertEquals(n.toLong(), store.info(localPlan)!!.updatedAt)
                    assertTrue(File(context.cacheDir, "guide-staging").listFiles().orEmpty().isEmpty())
                    yield()
                }
            }
        }
    }
    @Test fun repositoryPublishesGenuineEmptyGuideButRejectsAllInvalidProgrammes() = runBlocking {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody("<tv><programme channel='c' start='bad'><title>Bad date</title></programme></tv>"))
            server.enqueue(MockResponse().setBody("<tv/>")); server.start()
            val localPlan = EpgPlan.create(source, catalog, server.url("/epg").toString())!!
            save(localPlan)
            try { EpgRepository().refresh(localPlan, store, window, { true }); fail("Expected error") } catch (_: SourceFailure) {}
            assertEquals(1234L, store.info(localPlan)!!.updatedAt)
            assertTrue(EpgRepository().refresh(localPlan, store, window, { true }, { 4567 }))
            assertEquals(0, store.info(localPlan)!!.programmes); assertEquals(4567L, store.info(localPlan)!!.updatedAt)
        }
    }
    @Test fun cancellationDuringNetworkBodyCleansStageAndPreservesPreviousGuide() = runBlocking {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody("<tv>${" ".repeat(100)}</tv>").throttleBody(1, 3, TimeUnit.SECONDS)); server.start()
            val localPlan = EpgPlan.create(source, catalog, server.url("/epg").toString())!!
            save(localPlan)
            val bodyStarted = java.util.concurrent.CountDownLatch(1)
            val client = okhttp3.OkHttpClient.Builder().eventListener(object : okhttp3.EventListener() {
                override fun responseBodyStart(call: okhttp3.Call) { bodyStarted.countDown() }
            }).build()
            val job = launch(Dispatchers.IO) { EpgRepository(client).refresh(localPlan, store, window, { true }) }
            assertNotNull(server.takeRequest(5, TimeUnit.SECONDS))
            assertTrue(bodyStarted.await(5, TimeUnit.SECONDS))
            withTimeout(2_000) { job.cancelAndJoin() }
            assertEquals(1234L, store.info(localPlan)!!.updatedAt)
            assertTrue(File(context.cacheDir, "guide-staging").listFiles().orEmpty().isEmpty())
        }
    }
    @Test fun androidRejectsInternalEntitiesAndSupportsUtf16() {
        store.stage(plan, window).use { stage ->
            assertThrows(SourceFailure::class.java) {
                XmltvParser().parse("<!DOCTYPE tv [<!ENTITY x SYSTEM 'file:///never-read'>]><tv>&x;</tv>".byteInputStream(), window, stage.sink(0))
            }
        }
        store.stage(plan, window).use { stage ->
            val xml = "<?xml version='1.0' encoding='UTF-16'?><tv><channel id='c'><display-name>Örnek</display-name></channel></tv>"
            val result = XmltvParser().parse(xml.toByteArray(Charsets.UTF_16).inputStream(), window, stage.sink(0))
            stage.seal(9, listOf(result)); assertTrue(store.publish(stage))
            assertEquals(1, store.info(plan)!!.channels)
        }
    }
}
