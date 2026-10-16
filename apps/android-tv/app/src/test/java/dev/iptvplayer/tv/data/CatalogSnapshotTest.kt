package dev.iptvplayer.tv.data

import org.junit.Assert.*
import org.junit.Test

internal fun legacyCatalogFixture(): ByteArray = java.io.ByteArrayOutputStream().also { out ->
    java.util.zip.GZIPOutputStream(out).use { zipped ->
        CatalogSnapshotTest::class.java.getResourceAsStream("/catalog-v1.jsonl")!!.use { it.copyTo(zipped) }
    }
}.toByteArray()

class CatalogSnapshotTest {
    @Test fun roundTripPreservesUnicodeHeadersEpisodesAndEmptyGroups() {
        val entry = MediaEntry("m:1", "1", MediaKind.MOVIE, "Işık, yağmur ve İstanbul", "Özel",
            "https://art.example/a.jpg", "https://art.example/bg.jpg", "Açıklama\nİkinci satır", "2026", 3600,
            "mp4", "https://media.example/film?token=%26%2B", mapOf("User-Agent" to "Fixture/1", "Referer" to "https://media.example"), adult = true)
        val episode = entry.copy(id = "e:2", kind = MediaKind.EPISODE, season = 2, episode = 7, artwork = null,
            episodeOrderKnown = true, epgId = "channel.id", epgName = "İstanbul", categoryId = "17", parentLocalId = "s:1")
        val snapshot = CatalogSnapshot(Catalog(listOf(entry), mapOf("s:1" to listOf(episode), "empty" to emptyList()), listOf("Kısmi veri"), listOf("https://guide.example/epg.xml")), 12345)
        assertEquals(snapshot, CatalogSnapshotCodec.decode(CatalogSnapshotCodec.encode(snapshot)))
    }
    @Test fun ratingAndAddedTimeSurviveTheSnapshot() {
        val film = MediaEntry("m:9", "9", MediaKind.MOVIE, "Film", rating = 7.5f, addedAt = 1_700_000_000L)
        val restored = CatalogSnapshotCodec.decode(CatalogSnapshotCodec.encode(CatalogSnapshot(Catalog(listOf(film)), 1)))
        assertEquals(7.5f, restored.catalog.items.single().rating)
        assertEquals(1_700_000_000L, restored.catalog.items.single().addedAt)
    }
    @Test fun largeCatalogueRoundTripHasNoTruncation() {
        val entries = (1..63175).map { MediaEntry("l:$it", "$it", MediaKind.LIVE, "Kanal $it", "Kategori ${it % 200}") }
        val restored = CatalogSnapshotCodec.decode(CatalogSnapshotCodec.encode(CatalogSnapshot(Catalog(entries), 7)))
        assertEquals(entries, restored.catalog.items)
        assertEquals(7L, restored.updatedAt)
    }
    @Test fun damagedPayloadIsRejected() {
        val bytes = CatalogSnapshotCodec.encode(CatalogSnapshot(Catalog(emptyList()), 2))
        assertThrows(Exception::class.java) { CatalogSnapshotCodec.decode(bytes.copyOf(bytes.size / 2)) }
    }
    @Test fun preGuideVersionOneSnapshotRemainsReadableWithEmptyOptionalMetadata() {
        val snapshot = CatalogSnapshotCodec.decode(legacyCatalogFixture())
        assertEquals(listOf(MediaEntry("c", "c", MediaKind.LIVE, "Kanal")), snapshot.catalog.items)
        assertEquals(12L, snapshot.updatedAt)
        assertTrue(snapshot.catalog.epgUrls.isEmpty())
        assertEquals(emptyList<MediaEntry>(),snapshot.catalog.m3uEpisodes["empty"])
        assertEquals(snapshot, CatalogSnapshotCodec.decode(CatalogSnapshotCodec.encode(snapshot)))
    }

    @Test fun compactFormatRejectsUnknownVersionsAndMalformedRows() {
        fun compressed(text:String)=java.io.ByteArrayOutputStream().also { out ->
            java.util.zip.GZIPOutputStream(out).use { it.write(text.toByteArray()) }
        }.toByteArray()
        for (text in listOf("""{"version":5,"updatedAt":1,"warnings":[]}""",
            """{"version":2,"updatedAt":1,"warnings":[]}"""+"\n[null]",
            """{"version":2,"updatedAt":1,"warnings":[]}"""+"\n[null,\"incomplete\"]")) {
            assertThrows(Exception::class.java) { CatalogSnapshotCodec.decode(compressed(text)) }
        }
        val overflow=java.io.ByteArrayOutputStream().also { out ->
            java.util.zip.GZIPOutputStream(out).bufferedWriter().use { writer ->
                writer.appendLine("""{"version":2,"updatedAt":1,"warnings":[]}""")
                repeat(150001) { writer.appendLine("[\"empty\"]") }
            }
        }.toByteArray()
        assertThrows(Exception::class.java) { CatalogSnapshotCodec.decode(overflow) }
    }

    @Test fun expandedPayloadLimitStillAppliesToCompactRows() {
        val oversized = java.io.ByteArrayOutputStream().also { out ->
            java.util.zip.GZIPOutputStream(out).bufferedWriter().use { writer ->
                writer.appendLine("""{"version":2,"updatedAt":1,"warnings":[]}""")
                val row="[\"" + "g".repeat(8192) + "\"]"
                repeat(20480) { writer.appendLine(row) }
            }
        }.toByteArray()
        assertThrows(Exception::class.java) { CatalogSnapshotCodec.decode(oversized) }
    }

    @Test fun providerCategoryOrderSurvivesTheSnapshotAndOldSnapshotsHaveNone() {
        val order = mapOf(MediaKind.LIVE to listOf("TURK SPOR", "TURK ULUSAL"), MediaKind.MOVIE to listOf("YERLI 2026"))
        val snapshot = CatalogSnapshot(Catalog(listOf(MediaEntry("l:1", "1", MediaKind.LIVE, "Kanal", "TURK ULUSAL")), categoryOrder = order), 3)
        assertEquals(order, CatalogSnapshotCodec.decode(CatalogSnapshotCodec.encode(snapshot)).catalog.categoryOrder)
        assertTrue(CatalogSnapshotCodec.decode(legacyCatalogFixture()).catalog.categoryOrder.isEmpty())
    }
}
