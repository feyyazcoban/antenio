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
     * Literals that read as Turkish but never reach the screen as text, each with the calls it may stand in and its
     * reason: icon names, keys kept on the device, provider data. The same word in any other call is still a text. An
     * entry or a call no source uses any more fails the test, so the list stays exact.
     */
    private class Allowed(val text: String, val within: Set<String>, val reason: String)

    private val allowed = listOf(
        Allowed("film", setOf("LineIcon", "iconPaths"), "icon name: LineIcon draws it from DesignSystem.iconPaths"),
    )

    /**
     * [call] is the function whose arguments hold the literal, `LineIcon` in `LineIcon(if (a) "tv" else "film")`, or
     * the property a collection of literals is kept in, `iconPaths` in `val iconPaths = mapOf("film" to ...)`.
     */
    private class Literal(val file: String, val line: Int, val text: String, val testTag: Boolean, val call: String?)

    private val testTagCall = Regex("testTag\\s*[(=]\\s*$")
    private val collectionCall = Regex("va[lr]\\s+(\\w+)[^=()]*=\\s*(?:mapOf|listOf|setOf)\\s*$")
    private val namedCall = Regex("(\\w+)\\s*$")

    /** The string literals of Kotlin [source], comments skipped; a template's own literals are listed as well. */
    private fun literals(source: String, file: String): List<Literal> {
        val found = mutableListOf<Literal>()
        val calls = mutableListOf<String?>()
        fun callAt(open: Int) = source.substring(maxOf(0, open - 80), open)
            .let { before -> (collectionCall.find(before) ?: namedCall.find(before))?.groupValues?.get(1) }
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
                            testTagCall.containsMatchIn(source.substring(maxOf(0, i - 40), i)), calls.lastOrNull())
                        i = j + quote.length
                    }
                    source[i] == '(' -> { calls += callAt(i); i++ }
                    source[i] == ')' -> { calls.removeLastOrNull(); i++ }
                    else -> i++
                }
            }
        }
        scan(0, source.length)
        return found
    }

    private fun looksTurkish(text: String) = turkishLetters.containsMatchIn(text) ||
        Regex("\\p{L}+").findAll(text).any { it.value.lowercase(Locale.ROOT) in turkishWords }

    private fun isAllowed(literal: Literal) = allowed.any { it.text == literal.text && literal.call in it.within }

    private fun offenders(literals: List<Literal>) = literals.filter { !it.testTag && !isAllowed(it) && looksTurkish(it.text) }

    @Test fun screensAndTheViewModelHoldNoTurkishText() {
        val root = File("src/main/java/dev/iptvplayer/tv")
        val files = root.resolve("ui").walkTopDown().filter { it.extension == "kt" }.toList() + root.resolve("TvViewModel.kt")
        assertTrue("the sources moved", files.size > 10 && files.all { it.isFile })
        val found = files.flatMap { literals(it.readText(), it.name) }
        val offenders = offenders(found)
        assertTrue(offenders.joinToString("\n", "Move these texts to res/values/strings.xml:\n") { "${it.file}:${it.line} \"${it.text}\"" },
            offenders.isEmpty())
        val used = found.map { it.text to it.call }.toSet()
        assertEquals("allowed but no longer used", emptyList<String>(),
            allowed.flatMap { entry -> entry.within.filter { entry.text to it !in used }.map { "\"${entry.text}\" in $it" } })
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
            LineIcon(if (live) "tv" else "film", Ink.Mint)
            private val iconPaths = mapOf("film" to "M3 3h18")
            Text("film")
            Text(stringResource(R.string.sources_title))
        """.trimIndent()
        val literals = literals(source, "Sample.kt")
        assertEquals(listOf("Kaynak ekle", "Kanal", "TV", "  ·  ", "Geçersiz adres", "kanal-listesi", "tv", "film", "film", "M3 3h18", "film"),
            literals.map { it.text })
        // An icon name is allowed where an icon is named, and the same word is a text everywhere else.
        assertEquals(listOf("LineIcon", "iconPaths", "Text"), literals.filter { it.text == "film" }.map { it.call })
        assertEquals(listOf("Kaynak ekle" to 4, "Kanal" to 5, "Geçersiz adres" to 6, "film" to 10), offenders(literals).map { it.text to it.line })
    }
}
