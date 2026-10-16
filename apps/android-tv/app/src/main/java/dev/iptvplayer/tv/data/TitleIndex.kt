package dev.iptvplayer.tv.data

import java.text.Normalizer
import java.util.Locale

/** Memory-only title index. Never indexes URLs, account fields or hidden/adult entries. */
class TitleIndex(items: List<MediaEntry>, checkpoint: () -> Unit = {}) {
    private data class Row(val entry: MediaEntry, val title: String)
    private val rows = buildList {
        items.forEachIndexed { index, entry ->
            if (index % 128 == 0) checkpoint()
            if (!entry.adult && entry.kind != MediaKind.EPISODE) add(Row(entry, normalize(entry.title)))
        }
    }

    fun find(query: String, kind: MediaKind? = null, checkpoint: () -> Unit = {}): List<MediaEntry> {
        val phrase = normalize(query)
        if (phrase.isEmpty()) return emptyList()
        val words = phrase.split(' ')
        val exact = ArrayList<MediaEntry>()
        val prefix = ArrayList<MediaEntry>()
        val other = ArrayList<MediaEntry>()
        rows.forEachIndexed { index, row ->
            if (index % 128 == 0) checkpoint()
            if ((kind == null || row.entry.kind == kind) && words.all { it in row.title }) {
                when {
                    row.title == phrase -> exact.add(row.entry)
                    row.title.startsWith(phrase) -> prefix.add(row.entry)
                    else -> other.add(row.entry)
                }
            }
        }
        return exact + prefix + other
    }

    companion object {
        fun normalize(text: String): String {
            val folded = Normalizer.normalize(text.lowercase(Locale.ROOT).replace('ı', 'i'), Normalizer.Form.NFKD)
            return buildString {
                var space = false
                folded.codePoints().forEach { code ->
                    when (Character.getType(code)) {
                        Character.NON_SPACING_MARK.toInt(), Character.COMBINING_SPACING_MARK.toInt(), Character.ENCLOSING_MARK.toInt() -> Unit
                        else -> if (Character.isLetterOrDigit(code)) {
                            if (space && isNotEmpty()) append(' ')
                            appendCodePoint(code)
                            space = false
                        } else space = true
                    }
                }
            }
        }
    }
}
