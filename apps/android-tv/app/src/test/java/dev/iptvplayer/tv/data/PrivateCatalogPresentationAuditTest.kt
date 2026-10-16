package dev.iptvplayer.tv.data

import java.io.File
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test

/** Explicit local audit only: never embeds private input, identifiers, titles or URLs in the report. */
class PrivateCatalogPresentationAuditTest {
    @Test fun auditStoredCatalogWithProductionPresentationRules() {
        val path = System.getenv("NEXTV_PRIVATE_CATALOG_DIR")
        assumeTrue("No private catalog selected", !path.isNullOrBlank())
        val directory = File(path!!)
        val report = JSONArray()
        val entries = buildList {
            for ((kind, prefix) in listOf(MediaKind.LIVE to "live", MediaKind.MOVIE to "vod", MediaKind.SERIES to "series")) {
                val cats = CatalogParser.categories(directory.resolve("get_${prefix}_categories.raw").readText())
                val file = if (kind == MediaKind.SERIES) "get_series.raw" else "get_${prefix}_streams.raw"
                val items = CatalogParser.xtream(directory.resolve(file).readText(), kind, cats)
                val displayed = catalogDisplayOrder(items)
                assertTrue("Every raw identity is retained", items.map { it.id }.toSet() == displayed.map { it.id }.toSet())
                assertEquals("No catalog row removed", items.size, displayed.size)
                report.put(JSONObject().put("kind", kind.name).put("count", items.size)
                    .put("dividerCandidates", items.count { it.isProviderDivider() })
                    .put("usableArtwork", items.count { it.hasArtwork })
                    .put("usableBackdrop", items.count { it.hasBackdrop })
                    .put("dated", items.count { it.addedAt > 0 }).put("rated", items.count { it.rating > 0f }))
                addAll(items)
            }
        }
        val home = homeLibrary(Catalog(entries), emptyList(), emptySet())
        assertTrue("Automatic rows exclude divider hints", (home.channels + home.movies + home.series).none { it.isProviderDivider() })
        assertTrue("Hero is real catalog content", home.featured != null && home.featured in entries && !home.featured!!.isProviderDivider())
        // The device path: the stored snapshot, read back and scoped to its source, feeds the same home rules.
        val source = SourceConfig("audit", "Audit", SourceType.XTREAM, "http://example.invalid", "u", "p")
        val stored = CatalogSnapshotCodec.decode(CatalogSnapshotCodec.encode(CatalogSnapshot(Catalog(entries), 1L))).catalog
        val device = homeLibrary(SourceLibrary.combine(listOf(source), mapOf(source.id to stored)), emptyList(), emptySet())
        println("CATALOG_PRESENTATION_AUDIT=" + JSONObject().put("types", report)
            .put("homeLive", home.channels.size).put("homeMovies", home.movies.size).put("homeSeries", home.series.size)
            .put("heroHasBackdrop", home.featured?.hasBackdrop == true).put("heroSlides", home.newest.size)
            .put("storedDated", stored.items.count { it.addedAt > 0 }).put("storedRated", stored.items.count { it.rating > 0f })
            .put("deviceHeroSlides", device.newest.size).put("rawEntriesRetained", true))
    }
}
