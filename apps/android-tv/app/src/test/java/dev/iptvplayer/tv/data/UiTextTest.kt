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
     * The app's Turkish texts, read from the resource file the way Android formats them: with the device's locale,
     * whatever language the text is in.
     */
    private object Turkish {
        private val names = (R.string::class.java.fields + R.plurals::class.java.fields).associate { it.getInt(null) to it.name }
        private val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(File("src/main/res/values/strings.xml"))
        private val strings = document.getElementsByTagName("string").let { list ->
            (0 until list.length).associate { list.item(it).attributes.getNamedItem("name").nodeValue to list.item(it).textContent }
        }
        private val plurals = document.getElementsByTagName("item").let { list ->
            (0 until list.length).associate { list.item(it).let { item ->
                (item.parentNode.attributes.getNamedItem("name").nodeValue to item.attributes.getNamedItem("quantity").nodeValue) to item.textContent
            } }
        }
        fun of(text: UiText, device: Locale = Locale.forLanguageTag("tr")) =
            text.resolve({ id, args -> strings.getValue(names.getValue(id)).format(device, *args) },
                { id, count, args -> plurals.getValue(names.getValue(id) to if (count == 1) "one" else "other").format(device, *args) })
    }

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

    @Test fun movedTextsReadExactlyAsBefore() {
        assertEquals("http:// veya https:// ile başlayan geçerli bir adres gir.", Turkish.of(SourceFailure(FailureReason.ADDRESS_INVALID).text))
        assertEquals("Filmler yükleniyor…", Turkish.of(UiText.Res(R.string.progress_loading_kind, listOf(UiText.Res(MediaKind.MOVIE.label)))))
        assertEquals("Canlı TV: kategori adları alınamadı.", Turkish.of(CatalogWarning(CatalogWarning.Reason.CATEGORIES_UNAVAILABLE, MediaKind.LIVE).text))
        assertEquals("Diziler yüklenemedi. Kaynağı yenileyerek tekrar deneyebilirsin.",
            Turkish.of(CatalogWarning(CatalogWarning.Reason.KIND_UNAVAILABLE, MediaKind.SERIES).text))
        assertEquals("1 geçersiz adres atlandı.", Turkish.of(CatalogWarning(CatalogWarning.Reason.INVALID_ADDRESSES, count = 1).text))
        assertEquals("1250 geçersiz adres atlandı.", Turkish.of(CatalogWarning(CatalogWarning.Reason.INVALID_ADDRESSES, count = 1250).text))
        // The count keeps its plain digits on a device whose own language writes numbers in another script.
        listOf("ar-EG", "fa").forEach { device ->
            assertEquals(device, "12 geçersiz adres atlandı.",
                Turkish.of(CatalogWarning(CatalogWarning.Reason.INVALID_ADDRESSES, count = 12).text, Locale.forLanguageTag(device)))
        }
        assertEquals("Bazı kaynaklar yenilenemedi. Kayıtlı içerikler korunuyor; Kaynaklar’dan ayrıntıları görebilirsin.",
            Turkish.of(UiText.Res(R.string.notice_sources_not_refreshed)))
        assertEquals("Ad (A–Z)", Turkish.of(UiText.Res(LibrarySort.TITLE.label)))
        // Every reason has its own sentence in the file.
        FailureReason.entries.forEach { assertTrue(it.name, Turkish.of(SourceFailure(it).text).endsWith(".")) }
    }
}
