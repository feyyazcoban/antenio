package dev.iptvplayer.tv

import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.iptvplayer.tv.data.*
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.security.MessageDigest
import java.time.Instant
import java.time.ZoneId

/** Opt-in, emulator-only audit of previously authorized metadata, copied into app-private files.
 * No Activity, provider credentials, image requests or media player. Only aggregate results leave
 * this process. Runtime inputs and this audit's guide snapshot are removed even on failure. */
@RunWith(AndroidJUnit4::class)
class PrivateGuideAuditTest {
    @Test fun auditOfflineProviderMetadataThroughProductionGuidePipeline() = runBlocking {
        val args = InstrumentationRegistry.getArguments()
        assumeTrue(args.getString("privateGuideAudit") == "true")
        assumeTrue(android.os.Build.HARDWARE.contains("ranchu") || android.os.Build.FINGERPRINT.contains("generic"))
        val instant = Instant.parse(requireNotNull(args.getString("auditInstant")))
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val input = File(context.filesDir, "private-guide-audit")
        val xml = File(input, "guide.xml")
        val live = File(input, "live.json")
        val output = File(context.filesDir, "private-guide-audit-result.json")
        output.delete()
        val source = SourceConfig("private-offline-guide-audit", "Metadata audit", SourceType.M3U, "https://example.invalid/list")
        val store = EpgStore(context)
        val report = JSONObject().put("build", BuildConfig.VERSION_NAME)
            .put("observedAtUtc", instant.toString()).put("executedAtUtc", Instant.now().toString())
            .put("device", "Android emulator API ${android.os.Build.VERSION.SDK_INT}")
            .put("scope", "Downloaded metadata replayed offline through production parser, repository and SQLite store; debug emulator timings, not physical TV p95 or live provider transport.")
            .put("providerRequests", 0).put("mediaStreamsOpened", 0).put("rawValuesExported", false)
        try {
            check(xml.isFile && live.isFile) { "Private metadata inputs are required" }
            check(xml.length() in 1..(128L * 1024 * 1024) && live.length() in 1..(32L * 1024 * 1024))
            report.put("xmlBytes", xml.length()).put("liveBytes", live.length())
            fun digest(file: File): String {
                val hash = MessageDigest.getInstance("SHA-256")
                file.inputStream().use { stream ->
                    val bytes = ByteArray(8192)
                    while (true) { val n = stream.read(bytes); if (n < 0) break; hash.update(bytes, 0, n) }
                }
                return hash.digest().joinToString("") { "%02x".format(it) }
            }
            report.put("xmlSha256", digest(xml)).put("liveSha256", digest(live))
            val window = GuideWindow.around(instant, ZoneId.of("Europe/Istanbul"))
            val ids = linkedSetOf<String>()
            var first = Long.MAX_VALUE
            var last = Long.MIN_VALUE
            val scanStart = SystemClock.elapsedRealtimeNanos()
            val parsed = xml.inputStream().use { stream -> XmltvParser().parse(stream, window, object : EpgSink {
                override fun channel(value: EpgChannel) { ids += value.id }
                override fun programme(value: EpgProgramme) {
                    ids += value.channelId
                    first = minOf(first, value.start); value.end?.let { last = maxOf(last, it) }
                }
            }) }
            report.put("parserMs", (SystemClock.elapsedRealtimeNanos() - scanStart) / 1_000_000.0)
                .put("parsedChannels", parsed.channels).put("acceptedProgrammes", parsed.programmes)
                .put("invalid", parsed.invalid).put("outsideWindow", parsed.outsideWindow)
                .put("firstStartUtc", if (first == Long.MAX_VALUE) JSONObject.NULL else Instant.ofEpochMilli(first).toString())
                .put("lastEndUtc", if (last == Long.MIN_VALUE) JSONObject.NULL else Instant.ofEpochMilli(last).toString())
            val entries = CatalogParser.xtream(live.readText(), MediaKind.LIVE, emptyMap()).map { it.copy(sourceId = source.id) }
            assertTrue(entries.isNotEmpty()); assertTrue(parsed.programmes > 0)
            report.put("liveEntries", entries.size).put("entriesWithEpgId", entries.count { it.epgId.isNotBlank() })
            MockWebServer().use { server ->
                server.start()
                val plan = EpgPlan.create(source, Catalog(emptyList(), epgUrls = listOf(server.url("/guide").toString())))!!
                val refreshTimes = JSONArray()
                repeat(3) {
                    val body = Buffer().write(xml.readBytes())
                    server.enqueue(MockResponse().setBody(body).setHeader("Content-Type", "application/xml"))
                    val start = SystemClock.elapsedRealtimeNanos()
                    assertTrue(EpgRepository().refresh(plan, store, window, { true }, { instant.toEpochMilli() }))
                    refreshTimes.put((SystemClock.elapsedRealtimeNanos() - start) / 1_000_000.0)
                }
                report.put("localReplayRefreshMs", refreshTimes).put("localReplayRequests", server.requestCount)
                assertEquals(3, server.requestCount)
                val reopened = EpgStore(context)
                val info = requireNotNull(reopened.info(plan))
                assertEquals(parsed.invalid, info.invalid); assertEquals(parsed.outsideWindow, info.outsideWindow)
                report.put("storedChannels", info.channels).put("storedProgrammes", info.programmes)
                    .put("duplicateProgrammesRemoved", parsed.programmes - info.programmes)
                val classes = mutableMapOf<String, Int>()
                val matchedChannels = mutableSetOf<EpgChannelRef>()
                val nowChannels = mutableSetOf<EpgChannelRef>()
                val overlapChannels = mutableSetOf<EpgChannelRef>()
                var nowCount = 0; var nextCount = 0; var overlapCount = 0; var truncated = 0
                val batchTimes = JSONArray()
                // Live cards/player ask for now + 12h; a whole retention window can exceed 512 rows.
                val nowWindow = GuideWindow(instant.toEpochMilli(), instant.toEpochMilli() + 12 * 3_600_000L)
                entries.chunked(16).forEach { batch ->
                    val start = SystemClock.elapsedRealtimeNanos()
                    val slices = reopened.queryMany(plan, batch, nowWindow)
                    batchTimes.put((SystemClock.elapsedRealtimeNanos() - start) / 1_000_000.0)
                    assertEquals(batch.size, slices.size)
                    slices.values.forEach { slice ->
                        classes.merge(slice.match.kind.name, 1, Int::plus)
                        slice.match.channel?.let { matchedChannels += it }
                        if (slice.truncated) truncated++
                        if (slice.now(instant.toEpochMilli()) != null) { nowCount++; nowChannels += slice.match.channel!! }
                        if (slice.next(instant.toEpochMilli()) != null) nextCount++
                        if (slice.programmes.count { it.isOnAt(instant.toEpochMilli()) } > 1) {
                            overlapCount++; overlapChannels += slice.match.channel!!
                            assertNull(slice.now(instant.toEpochMilli()))
                        }
                    }
                }
                report.put("matching", JSONObject(classes.toMap())).put("uniqueMatchedChannels", matchedChannels.size)
                    .put("entriesWithNow", nowCount).put("entriesWithNext", nextCount)
                    .put("uniqueChannelsWithNow", nowChannels.size).put("entriesWithOverlappingNow", overlapCount)
                    .put("uniqueChannelsWithOverlappingNow", overlapChannels.size).put("truncatedEntryQueries", truncated)
                    .put("queryBatchSize", 16).put("queryBatchMs", batchTimes)
                assertEquals(0, truncated)
                val channels = ids.mapIndexed { i, id -> MediaEntry("audit-channel:$i", "$i", MediaKind.LIVE, "", epgId = id, sourceId = source.id) }
                val boundedChannels = reopened.queryMany(plan, channels, window)
                report.put("fullRetentionChannelsTruncatedAt512", boundedChannels.values.count { it.truncated })
                // An offline storage audit may use the API's explicit higher limit. UI limits stay unchanged.
                val allChannels = channels.map { requireNotNull(reopened.query(plan, it, window, limit = 2000)) }
                assertEquals(ids.size, allChannels.size)
                val completeRows = allChannels.sumOf { it.programmes.size }
                assertTrue(allChannels.none { it.truncated })
                assertEquals(info.programmes, completeRows)
                report.put("allChannelsReopened", allChannels.size).put("allProgrammesReopened", completeRows)
                    .put("maximumProgrammesPerChannelInRetention", allChannels.maxOf { it.programmes.size })
                val day = instant.atZone(ZoneId.of("Europe/Istanbul")).toLocalDate()
                val dayWindow = GuideWindow(day.atStartOfDay(ZoneId.of("Europe/Istanbul")).toInstant().toEpochMilli(),
                    day.plusDays(1).atStartOfDay(ZoneId.of("Europe/Istanbul")).toInstant().toEpochMilli())
                val dayRows = reopened.queryMany(plan, channels, dayWindow)
                report.put("todayChannelsTruncatedAt512", dayRows.values.count { it.truncated })
                    .put("maximumProgrammesPerChannelToday", dayRows.values.maxOf { it.programmes.size })
                assertTrue(dayRows.values.none { it.truncated })
                val files = File(context.filesDir, "guides").listFiles().orEmpty().filter { it.name.startsWith(plan.owner + "-") }
                report.put("snapshotBytesIncludingSidecars", files.sumOf { it.length() })
                val runtime = Runtime.getRuntime()
                report.put("usedJavaHeapBytesAtEnd", runtime.totalMemory() - runtime.freeMemory())
                val sample = entries.first { reopened.query(plan, it, window)?.match?.channel != null }
                assertEquals(EpgMatchKind.NONE, reopened.query(plan, sample.copy(sourceId = "other-owner"), window)!!.match.kind)
                report.put("wrongOwnerRejected", true)
                store.remove(source.id)
                assertNull(reopened.info(plan))
                report.put("snapshotRemoved", true)
            }
        } catch (failure: Exception) {
            // Malformed private input must not become a JSON/XML excerpt in instrumentation logs.
            throw AssertionError("Private metadata audit failed (${failure.javaClass.simpleName}); raw exception omitted.")
        } finally {
            store.remove(source.id)
            check(xml.delete() || !xml.exists()); check(live.delete() || !live.exists())
            input.delete()
        }
        report.put("privateInputsRemoved", !xml.exists() && !live.exists())
        report.put("passed", true)
        output.writeText(report.toString(2))
    }
}
