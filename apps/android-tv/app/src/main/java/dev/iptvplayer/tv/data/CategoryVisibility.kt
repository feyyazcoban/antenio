package dev.iptvplayer.tv.data

import java.text.Normalizer
import java.util.Locale

/** A provider category in Ayarlar → Kategorileri yönet: its name, how many visible titles it holds, whether it reads as adult. */
data class CategoryChoice(val name: String, val count: Int, val adult: Boolean)

/**
 * Categories the viewer hides (D068), by kind and provider name across all sources, and the categories whose name
 * reads as adult (XXX, Adult, +18, Yetişkin, Erwachsene, Adultos …), hidden by default. Hidden titles leave the catalog
 * the screens read: Ana Sayfa, the library, Ara, the guide, channel numbers and the player's next channel. The
 * provider's own adult flag stays hidden in any case. Hiding is not a lock: with a parental PIN set, showing a category
 * or turning the filter off asks for it (TvViewModel), and ends what PINs opened in the session; without one, anyone
 * with the remote can. A locked category stays listed and asks for the PIN on opening (AccessPolicy, ParentalProjection).
 */
object CategoryVisibility {
    /**
     * Words that name adult categories in the languages providers write them in: English, Turkish, German, Dutch,
     * French, Spanish, Portuguese and Italian, and Arabic in Latin letters ("lil kibar"). Whole words only, so "Essex",
     * "Sexta-feira" and "Erwachsenenbildung" stay; "Adult Swim" and "Young Adult" name an audience, not adult content.
     */
    private val adultWords = Regex("(?<![a-z0-9])(?:xxx|porno?s?|hentai|sex[eo]?|sesso|(?<!young\\s)adults?(?!\\s*swim)|" +
        "adult(?:os|as|o|a|i|e|es)|erwachsenen?|volwassenen|yetiskin[a-z]*|" +
        "erot(?:ic|ik|iq|isch|ism)[a-z]*|lil\\s*kibar|18\\s*\\+|\\+\\s*18|vm\\s*18)(?![a-z0-9])")
    /** The same in Arabic script: "للكبار" (for adults), "سكس" and "اباحي" (pornographic, its hamza folded away). */
    private val arabicAdultWords = Regex("(?<![\\u0600-\\u06FF])(?:للكبار|سكس|اباحي)(?![\\u0600-\\u06FF])")
    private val marks = Regex("\\p{Mn}+")

    /**
     * Matched on the name in lower case without accents or other marks, the Turkish dotless i included: "YETİŞKİN",
     * "Erótico", "Pornô" and "إباحي" read as their plain forms.
     */
    fun looksAdult(category: String): Boolean {
        val folded = Normalizer.normalize(category.replace('ı', 'i'), Normalizer.Form.NFD).replace(marks, "").lowercase(Locale.ROOT)
        return adultWords.containsMatchIn(folded) || arabicAdultWords.containsMatchIn(folded)
    }

    /** The section a title's category belongs to: episodes go with their series. */
    private fun section(kind: MediaKind) = if (kind == MediaKind.EPISODE) MediaKind.SERIES else kind
    fun key(kind: MediaKind, category: String) = "${section(kind).name}|$category"

    /** The section and the category name a [key] was made of; null for one this app did not make. */
    fun row(key: String): Pair<MediaKind, String>? {
        val bar = key.indexOf('|')
        val kind = MediaKind.entries.firstOrNull { it.name == key.substring(0, bar.coerceAtLeast(0)) } ?: return null
        return kind to key.substring(bar + 1)
    }

    /** [catalog] without the titles of hidden categories; the same instance when nothing is hidden. */
    fun apply(catalog: Catalog, hidden: Set<String>, hideAdult: Boolean): Catalog {
        if (hidden.isEmpty() && !hideAdult) return catalog
        val verdicts = HashMap<String, Boolean>()
        fun hides(kind: MediaKind, category: String): Boolean {
            val key = key(kind, category)
            return verdicts.getOrPut(key) { key in hidden || hideAdult && looksAdult(category) }
        }
        val items = catalog.items.filterNot { hides(it.kind, it.category) }
        val episodes = catalog.m3uEpisodes.mapValues { (_, list) -> list.filterNot { hides(it.kind, it.category) } }
        if (items.size == catalog.items.size && episodes.values.sumOf { it.size } == catalog.m3uEpisodes.values.sumOf { it.size }) return catalog
        return catalog.copy(items = items, m3uEpisodes = episodes)
    }

    /** Every category of the three sections in the provider's order, with its size, for the settings list. */
    fun choices(catalog: Catalog): Map<MediaKind, List<CategoryChoice>> =
        listOf(MediaKind.LIVE, MediaKind.MOVIE, MediaKind.SERIES).associateWith { kind ->
            val items = catalog.items.filter { it.kind == kind && !it.adult }
            val counts = items.groupingBy { it.category }.eachCount()
            providerCategories(items, catalog.categoryOrder[kind].orEmpty()).map { CategoryChoice(it, counts[it] ?: 0, looksAdult(it)) }
        }
}
