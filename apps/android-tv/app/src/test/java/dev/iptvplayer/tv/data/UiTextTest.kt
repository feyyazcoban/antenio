package dev.iptvplayer.tv.data

import dev.iptvplayer.tv.R
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.util.Locale
import javax.xml.parsers.DocumentBuilderFactory

class UiTextTest {
    private fun fake(text: UiText) = text.resolve({ id, args -> "s$id${args.toList()}" }, { id, count, args -> "p$id:$count${args.toList()}" })

    /**
     * The app's texts in one language, read from its resource file the way Android formats them: with the device's
     * locale, whatever language the text is in. A text the file does not have comes from [fallback], as a text marked
     * translatable="false" comes from values/.
     */
    private class Texts(path: String, private val fallback: Texts? = null) {
        private val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(File(path))
        val strings: Map<String, String> = document.getElementsByTagName("string").let { list ->
            (0 until list.length).associate { list.item(it).attributes.getNamedItem("name").nodeValue to list.item(it).textContent }
        }
        val untranslatable: Set<String> = document.getElementsByTagName("string").let { list ->
            (0 until list.length).map { list.item(it) }.filter { it.attributes.getNamedItem("translatable")?.nodeValue == "false" }
                .mapTo(HashSet()) { it.attributes.getNamedItem("name").nodeValue }
        }
        val plurals: Map<Pair<String, String>, String> = document.getElementsByTagName("item").let { list ->
            (0 until list.length).associate { list.item(it).let { item ->
                (item.parentNode.attributes.getNamedItem("name").nodeValue to item.attributes.getNamedItem("quantity").nodeValue) to item.textContent
            } }
        }
        private fun string(name: String): String = strings[name] ?: fallback?.string(name) ?: error("no text $name")
        fun of(text: UiText, device: Locale = Locale.forLanguageTag("tr")) =
            text.resolve({ id, args -> string(names.getValue(id)).format(device, *args) },
                { id, count, args -> plurals.getValue(names.getValue(id) to if (count == 1) "one" else "other").format(device, *args) })

        companion object {
            private val names = (R.string::class.java.fields + R.plurals::class.java.fields).associate { it.getInt(null) to it.name }
        }
    }

    private val english = Texts("src/main/res/values/strings.xml")
    private val turkish = Texts("src/main/res/values-tr/strings.xml", fallback = english)

    @Test fun argumentsAreResolvedFirstAndAPluralDefaultsToItsCount() {
        assertEquals("s1[a, 2]", fake(UiText.Res(1, listOf("a", 2))))
        assertEquals("s1[s2[]]", fake(UiText.Res(1, listOf(UiText.Res(2)))))
        assertEquals("p3:4[4]", fake(UiText.Plural(3, 4)))
        assertEquals("p3:4[x]", fake(UiText.Plural(3, 4, listOf("x"))))
        assertEquals("Kanal", fake(UiText.Raw("Kanal")))
    }

    @Test fun failuresCarryAReasonAndNeverASentenceOrAddress() {
        val failure = SourceFailure(FailureReason.ACCESS_DENIED)
        assertEquals("ACCESS_DENIED", failure.message)
        assertEquals(UiText.Res(R.string.failure_access_denied), failure.text)
        assertEquals(FailureReason.entries.size, FailureReason.entries.map { it.message }.distinct().size)
    }

    @Test fun movedTextsReadExactlyAsBeforeInTurkish() {
        assertEquals("http:// veya https:// ile başlayan geçerli bir adres gir.", turkish.of(SourceFailure(FailureReason.ADDRESS_INVALID).text))
        assertEquals("Filmler yükleniyor…", turkish.of(UiText.Res(R.string.progress_loading_kind, listOf(UiText.Res(MediaKind.MOVIE.label)))))
        assertEquals("Canlı TV: kategori adları alınamadı.", turkish.of(CatalogWarning(CatalogWarning.Reason.CATEGORIES_UNAVAILABLE, MediaKind.LIVE).text))
        assertEquals("Diziler yüklenemedi. Kaynağı yenileyerek tekrar deneyebilirsin.",
            turkish.of(CatalogWarning(CatalogWarning.Reason.KIND_UNAVAILABLE, MediaKind.SERIES).text))
        assertEquals("1 geçersiz adres atlandı.", turkish.of(CatalogWarning(CatalogWarning.Reason.INVALID_ADDRESSES, count = 1).text))
        assertEquals("1250 geçersiz adres atlandı.", turkish.of(CatalogWarning(CatalogWarning.Reason.INVALID_ADDRESSES, count = 1250).text))
        // The count keeps its plain digits on a device whose own language writes numbers in another script.
        listOf("ar-EG", "fa").forEach { device ->
            assertEquals(device, "12 geçersiz adres atlandı.",
                turkish.of(CatalogWarning(CatalogWarning.Reason.INVALID_ADDRESSES, count = 12).text, Locale.forLanguageTag(device)))
        }
        assertEquals("Bazı kaynaklar yenilenemedi. Kayıtlı içerikler korunuyor; Kaynaklar’dan ayrıntıları görebilirsin.",
            turkish.of(UiText.Res(R.string.notice_sources_not_refreshed)))
        assertEquals("Ad (A–Z)", turkish.of(UiText.Res(LibrarySort.TITLE.label)))
        // Every reason has its own sentence in the file.
        FailureReason.entries.forEach { assertTrue(it.name, turkish.of(SourceFailure(it).text).endsWith(".")) }
    }

    @Test fun englishIsTheDefaultAndReadsInItsOwnWords() {
        assertEquals("Enter a valid address that starts with http:// or https://.", english.of(SourceFailure(FailureReason.ADDRESS_INVALID).text))
        assertEquals("Loading Movies…", english.of(UiText.Res(R.string.progress_loading_kind, listOf(UiText.Res(MediaKind.MOVIE.label)))))
        assertEquals("Live TV: category names could not be loaded.", english.of(CatalogWarning(CatalogWarning.Reason.CATEGORIES_UNAVAILABLE, MediaKind.LIVE).text))
        assertEquals("1 invalid address was skipped.", english.of(CatalogWarning(CatalogWarning.Reason.INVALID_ADDRESSES, count = 1).text))
        assertEquals("1250 invalid addresses were skipped.", english.of(CatalogWarning(CatalogWarning.Reason.INVALID_ADDRESSES, count = 1250).text))
        assertEquals("Title (A–Z)", english.of(UiText.Res(LibrarySort.TITLE.label)))
        listOf(MediaKind.LIVE to "Live TV", MediaKind.MOVIE to "Movies", MediaKind.SERIES to "Series").forEach { (kind, label) ->
            assertEquals(label, english.of(UiText.Res(kind.label)))
        }
        FailureReason.entries.forEach { assertTrue(it.name, english.of(SourceFailure(it).text).endsWith(".")) }
        // The language page reads the same in both languages, each language named in itself.
        listOf(R.string.settings_language, R.string.language_system, R.string.language_turkish, R.string.language_english).forEach {
            assertEquals(english.of(UiText.Res(it)), turkish.of(UiText.Res(it)))
        }
        assertEquals("Dil / Language", turkish.of(UiText.Res(R.string.settings_language)))
    }

    @Test fun bothLanguagesHaveEveryTextWithTheSameArguments() {
        val translated = english.strings.keys - english.untranslatable
        assertEquals(translated, turkish.strings.keys)
        assertEquals(english.plurals.keys, turkish.plurals.keys)
        val argument = Regex("%\\d\\$[sd]")
        fun arguments(text: String) = argument.findAll(text).map { it.value }.sorted().toList()
        translated.forEach { assertEquals(it, arguments(english.strings.getValue(it)), arguments(turkish.strings.getValue(it))) }
        english.plurals.forEach { (key, text) -> assertEquals(key.toString(), arguments(text), arguments(turkish.plurals.getValue(key))) }
    }

    @Test fun titlesAProviderLeftUnnamedReadInTheAppsLanguageAndStayStoredInTurkish() {
        assertEquals("İsimsiz içerik 12", UnnamedTitle.xtream("12"))
        assertEquals("İsimsiz içerik 12", turkish.of(UnnamedTitle.text(UnnamedTitle.xtream("12"))!!))
        assertEquals("Untitled 12", english.of(UnnamedTitle.text(UnnamedTitle.xtream("12"))!!))
        assertEquals("Item 40", english.of(UnnamedTitle.text(UnnamedTitle.m3u(40))!!))
        assertEquals("İçerik 40", turkish.of(UnnamedTitle.text(UnnamedTitle.m3u(40))!!))
        assertEquals("Episode 3", english.of(UnnamedTitle.text(UnnamedTitle.episode(3))!!))
        assertEquals("3. bölüm", turkish.of(UnnamedTitle.text(UnnamedTitle.episode(3))!!))
        // The provider's own names stay as they are.
        listOf("İçerikler", "1. Bölüm", "Bölüm 3", "TR: İsimsiz", "İçerik 4K").forEach { assertNull(it, UnnamedTitle.text(it)) }
    }
}
