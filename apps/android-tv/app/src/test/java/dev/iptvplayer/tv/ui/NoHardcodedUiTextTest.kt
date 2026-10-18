package dev.iptvplayer.tv.ui

import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.util.Locale

/**
 * The screens and the view model take every text from the string resources (S01, S02), so the app can speak another
 * language. This reads their sources and fails on a string literal that looks Turkish: one with a letter only Turkish
 * uses among the app's languages, or with a common word of its Turkish copy. Comments and test tags are not texts.
 */
class NoHardcodedUiTextTest {
    private val turkishLetters = Regex("[çğıöşüÇĞİÖŞÜ]")

    /** Everyday words of the Turkish copy that are written without Turkish letters, and so need a list. */
    private val turkishWords = setOf(
        "ve", "veya", "ile", "bir", "bu", "yok", "daha", "sonra", "sonraki", "tekrar", "yeniden", "dene", "devam", "et",
        "ekle", "geri", "kapat", "temizle", "yenile", "oynat", "izle", "gizle", "iptal", "tamam", "kaydet", "sil", "ara",
        "arama", "ayarlar", "kaynak", "kaynaklar", "kanal", "kanallar", "kategori", "kategoriler", "film", "filmler",
        "dizi", "diziler", "sezon", "favori", "favoriler", "rehber", "rehberi", "liste", "listesi", "adres", "adresi",
        "hesap", "sayfa", "saat", "dakika", "saniye", "ses", "dil", "kontrol"
    )

    /**
     * Literals that read as Turkish but never reach the screen as text, each with its reason: icon names, keys kept on
     * the device, provider data. An entry no source uses any more fails the test, so the list stays exact.
     */
    private val allowed = mapOf(
        "film" to "LineIcon name (DesignSystem.iconPaths)",
    )

    private class Literal(val file: String, val line: Int, val text: String, val testTag: Boolean)

    private val testTagCall = Regex("testTag\\s*[(=]\\s*$")

    /** The string literals of Kotlin [source], comments skipped; a template's own literals are listed as well. */
    private fun literals(source: String, file: String): List<Literal> {
        val found = mutableListOf<Literal>()
        fun closingBrace(open: Int): Int {
            var depth = 0
            for (k in open until source.length) {
                if (source[k] == '{') depth++ else if (source[k] == '}' && --depth == 0) return k
            }
            return source.length
        }
        fun scan(from: Int, until: Int) {
            var i = from
            while (i < until) {
                when {
                    source.startsWith("//", i) -> i = source.indexOf('\n', i).let { if (it < 0 || it > until) until else it }
                    source.startsWith("/*", i) -> i = source.indexOf("*/", i + 2).let { if (it < 0) until else it + 2 }
                    source[i] == '\'' -> i = source.indexOf('\'', i + if (source.getOrNull(i + 1) == '\\') 3 else 2).let { if (it < 0) until else it + 1 }
                    source[i] == '"' -> {
                        val quote = if (source.startsWith("\"\"\"", i)) "\"\"\"" else "\""
                        val text = StringBuilder()
                        var j = i + quote.length
                        // A template or an escape stands in as a space, so the words on either side stay apart.
                        while (j < until && !source.startsWith(quote, j)) when {
                            quote.length == 1 && source[j] == '\\' -> { text.append(' '); j += 2 }
                            source.startsWith("\${", j) -> { val end = closingBrace(j + 1); scan(j + 2, end); text.append(' '); j = end + 1 }
                            source[j] == '$' && source.getOrNull(j + 1)?.isJavaIdentifierStart() == true -> {
                                j++
                                while (source[j].isJavaIdentifierPart()) j++
                                text.append(' ')
                            }
                            else -> text.append(source[j++])
                        }
                        found += Literal(file, source.substring(0, i).count { it == '\n' } + 1, text.toString(),
                            testTagCall.containsMatchIn(source.substring(maxOf(0, i - 40), i)))
                        i = j + quote.length
                    }
                    else -> i++
                }
            }
        }
        scan(0, source.length)
        return found
    }

    private fun looksTurkish(text: String) = turkishLetters.containsMatchIn(text) ||
        Regex("\\p{L}+").findAll(text).any { it.value.lowercase(Locale.ROOT) in turkishWords }

    private fun offenders(literals: List<Literal>) = literals.filter { !it.testTag && it.text !in allowed && looksTurkish(it.text) }

    @Test fun screensAndTheViewModelHoldNoTurkishText() {
        val root = File("src/main/java/dev/iptvplayer/tv")
        val files = root.resolve("ui").walkTopDown().filter { it.extension == "kt" }.toList() + root.resolve("TvViewModel.kt")
        assertTrue("the sources moved", files.size > 10 && files.all { it.isFile })
        val found = files.flatMap { literals(it.readText(), it.name) }
        val offenders = offenders(found)
        assertTrue(offenders.joinToString("\n", "Move these texts to res/values/strings.xml:\n") { "${it.file}:${it.line} \"${it.text}\"" },
            offenders.isEmpty())
        assertEquals("allowed but no longer used", emptySet<String>(), allowed.keys - found.map { it.text }.toSet())
    }

    @Test fun theScanReadsCodeButNotCommentsOrTestTags() {
        val source = """
            // Text("Kanallar") in a comment
            /** "Ayarlar" in KDoc */
            val quote = '"'
            Text("Kaynak ekle")
            Text("${'$'}{if (live) "Kanal" else "TV"} · ${'$'}count")
            Note("Geçersiz adres")
            Modifier.testTag("kanal-listesi")
            LineIcon("film")
            Text(stringResource(R.string.sources_title))
        """.trimIndent()
        val literals = literals(source, "Sample.kt")
        assertEquals(listOf("Kaynak ekle", "Kanal", "TV", "  ·  ", "Geçersiz adres", "kanal-listesi", "film"), literals.map { it.text })
        assertEquals(listOf("Kaynak ekle" to 4, "Kanal" to 5, "Geçersiz adres" to 6), offenders(literals).map { it.text to it.line })
    }
}
