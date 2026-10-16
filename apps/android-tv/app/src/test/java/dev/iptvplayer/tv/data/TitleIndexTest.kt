package dev.iptvplayer.tv.data

import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.CancellationException

class TitleIndexTest {
    private fun entry(id: String, title: String, kind: MediaKind = MediaKind.MOVIE, adult: Boolean = false) =
        MediaEntry(id, id, kind, title, adult = adult)

    @Test fun turkishKeyboardVariantsAndCombiningMarksMatch() {
        val index = TitleIndex(listOf(entry("1", "İstanbul IŞIĞI: Çığ, Öykü ve Üzüm")))
        listOf("istanbul", "İSTANBUL", "ISIGI", "ışığı", "cig", "ÖYKU", "uzu\u0308m", "ısık").forEach { query ->
            assertEquals(query, if (query == "ısık") 0 else 1, index.find(query).size)
        }
    }
    @Test fun wordsCanBeReorderedAndPunctuationSeparatesWords() {
        val index = TitleIndex(listOf(entry("1", "TR | Yeşil—Çam (2026)"), entry("2", "Yeşil Dünya")))
        assertEquals(listOf("1"), index.find("cam yesil").map { it.id })
        assertEquals(listOf("1"), index.find("TR | yesil").map { it.id })
        assertTrue(index.find("yeşil deniz").isEmpty())
    }
    @Test fun ranksExactThenPrefixThenContainsWithStableOrder() {
        val index = TitleIndex(listOf(entry("1", "Bir İstanbul Masalı"), entry("2", "İstanbul Sokakları"),
            entry("3", "İstanbul"), entry("4", "İstanbul Hatırası")))
        assertEquals(listOf("3", "2", "4", "1"), index.find("istanbul").map { it.id })
    }
    @Test fun allKindsFilterAndAdultPolicyAreAppliedBeforeMatching() {
        val index = TitleIndex(listOf(entry("1", "Ortak", MediaKind.LIVE), entry("2", "Ortak", MediaKind.MOVIE),
            entry("3", "Ortak", MediaKind.SERIES), entry("4", "Ortak", adult = true), entry("5", "Ortak", MediaKind.EPISODE)))
        assertEquals(listOf("1", "2", "3"), index.find("ortak").map { it.id })
        assertEquals(listOf("3"), index.find("ortak", MediaKind.SERIES).map { it.id })
    }
    @Test fun blankOrPunctuationQueryNeverDumpsCatalog() {
        val index = TitleIndex(listOf(entry("1", "Bir başlık")))
        listOf("", "  ", " | --!? ").forEach { assertTrue(index.find(it).isEmpty()) }
    }
    @Test fun nonLatinTitlesAndNonBmpLettersRemainSearchable() {
        val index = TitleIndex(listOf(entry("1", "東京物語"), entry("2", "Москва"), entry("3", "𐐀 Example")))
        assertEquals("1", index.find("東京").single().id)
        assertEquals("2", index.find("МОСКВА").single().id)
        assertEquals("3", index.find("𐐨").single().id)
    }
    @Test fun indexingAndScanningHonorCancellation() {
        val catalog = (1..1000).map { entry("$it", "Başlık $it") }
        assertThrows(CancellationException::class.java) { TitleIndex(catalog) { throw CancellationException() } }
        val index = TitleIndex(catalog)
        assertThrows(CancellationException::class.java) { index.find("başlık") { throw CancellationException() } }
    }
    @Test fun largeCatalogKeepsAllResultsAndFindsFinalItem() {
        val index = TitleIndex((1..63175).map { entry("$it", "Film $it") })
        assertEquals(63175, index.find("film").size)
        assertEquals("63175", index.find("Film 63175").first().id)
    }
}
