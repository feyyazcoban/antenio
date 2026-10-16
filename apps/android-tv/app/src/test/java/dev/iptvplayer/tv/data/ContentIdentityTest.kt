package dev.iptvplayer.tv.data

import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

class ContentIdentityTest {
    @Test fun sameCategoryLabelKeepsDistinctProviderIdsAndSourceOwnership() {
        val rows = """[{"stream_id":1,"name":"Bir","category_id":"10"},{"stream_id":2,"name":"İki","category_id":"20"}]"""
        val items = CatalogParser.xtream(rows, MediaKind.MOVIE, mapOf("10" to "Aile", "20" to "Aile"))
        assertEquals(listOf("10", "20"), items.map { it.categoryId })
        assertEquals(listOf("movie:1", "movie:2"), items.map { it.id })
        val a = items.first().inSource("a"); val b = items.first().inSource("b")
        assertNotEquals(a.id, b.id)
        assertEquals(items.first(), a.withoutSource())
        val renamed = CatalogParser.xtream(rows, MediaKind.MOVIE, mapOf("10" to "Yeni ad", "20" to "Aile"))
        assertEquals(items.first().categoryId, renamed.first().categoryId)
    }

    @Test fun xtreamEpisodesCarryLocalParentAndInheritedCategoryIdentity() {
        val show = MediaEntry("series:7", "7", MediaKind.SERIES, "Dizi", categoryId = "42").inSource("source-a")
        val (_, episodes) = CatalogParser.episodes("""{"episodes":{"1":[{"id":"8","episode_num":1,"season":1}]}}""", show)
        val episode = episodes.single()
        assertEquals("series:7", episode.parentLocalId)
        assertEquals("42", episode.categoryId)
        assertEquals("series:7", episode.inSource("source-a").withoutSource().parentLocalId)
    }

    @Test fun m3uGroupedEpisodesCarryParentWithoutInventingProviderCategoryId() {
        val text = "#EXTM3U\n#EXTINF:-1 group-title=\"Diziler\",Örnek S01E01\nhttp://127.0.0.1/series/1.mp4\n"
        val catalog = M3uParser.parse(text.reader(), "http://127.0.0.1/list.m3u")
        val show = catalog.items.single()
        val episode = catalog.m3uEpisodes.getValue(show.id).single()
        assertEquals(show.id, episode.parentLocalId)
        assertEquals("", show.categoryId)
        assertNull(show.parentLocalId)
    }

    @Test fun fixedV2SnapshotLoadsWithoutInventedMetadataAndUpgradesLosslessly() {
        val bytes = ByteArrayOutputStream().also { out -> GZIPOutputStream(out).use { zip ->
            javaClass.getResourceAsStream("/catalog-v2.jsonl")!!.use { it.copyTo(zip) }
        } }.toByteArray()
        val old = CatalogSnapshotCodec.decode(bytes)
        assertEquals("", old.catalog.items.single().categoryId)
        assertNull(old.catalog.m3uEpisodes.getValue("series:2").single().parentLocalId)
        assertEquals(old, CatalogSnapshotCodec.decode(CatalogSnapshotCodec.encode(old)))
    }
}
