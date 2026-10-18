package dev.iptvplayer.tv.data

import org.junit.Assert.*
import org.junit.Test
import java.time.*
import java.util.concurrent.CancellationException

class XmltvParserTest {
    private val window = GuideWindow(Instant.parse("2026-10-20T00:00:00Z").toEpochMilli(), Instant.parse("2026-11-03T00:00:00Z").toEpochMilli())
    private class Sink : EpgSink {
        val channels = mutableListOf<EpgChannel>(); val programmes = mutableListOf<EpgProgramme>()
        override fun channel(value: EpgChannel) { channels += value }
        override fun programme(value: EpgProgramme) { programmes += value }
    }
    private fun parse(xml: String, sink: Sink = Sink()) = XmltvParser().parse(xml.byteInputStream(), window, sink)
    /** The fixtures as a Turkish app reads them. */
    private fun fixture(name: String, sink: Sink) = javaClass.getResourceAsStream("/epg/$name.xml")!!.use { XmltvParser(listOf("tr")).parse(it, window, sink) }

    @Test fun europeanFixtureKeepsUnicodePreferredLanguageAndDstInstants() {
        val sink = Sink(); val report = fixture("europe", sink)
        assertEquals(EpgParseReport(3, 3, 0, 0), report)
        assertEquals(listOf("Örnek Haber", "Sample News"), sink.channels.first().names)
        val first = sink.programmes[0]; val second = sink.programmes[1]
        assertEquals("İlk haber & gündem", first.title)
        assertEquals("İstanbul'dan güncel haberler.", first.description)
        assertEquals("Haber", first.category)
        assertEquals(3_600_000L, first.end!! - first.start)
        assertEquals(first.end, second.start)
        assertEquals(LocalTime.of(2, 30), Instant.ofEpochMilli(first.start).atZone(ZoneId.of("Europe/Berlin")).toLocalTime())
        assertEquals(LocalTime.of(2, 30), Instant.ofEpochMilli(second.start).atZone(ZoneId.of("Europe/Berlin")).toLocalTime())
    }
    @Test fun titlesFollowTheAppsLanguageThenTheTvsThenAnUntaggedOneThenTheFirstGiven() {
        fun programme(xml: String, vararg languages: String) = Sink().also {
            XmltvParser(languages.toList()).parse("<tv><programme channel='c' start='20261025010000' stop='20261025020000'>$xml</programme></tv>"
                .byteInputStream(), window, it)
        }.programmes.single()
        val both = "<title lang='en'>News</title><title lang='tr-TR'>Haber</title><title>Untagged</title>"
        assertEquals("Haber", programme(both, "tr", "en").title)
        assertEquals("News", programme(both, "en", "tr").title)
        val others = "<title lang='de'>Nachrichten</title><title>Haberler</title><title lang='fr'>Infos</title>" +
            "<desc lang='fr'>Français</desc><desc lang='de'>Deutsch</desc>"
        // An English app on a German TV, then on a TV whose language the guide does not have.
        assertEquals("Nachrichten", programme(others, "en", "de").title)
        assertEquals("Deutsch", programme(others, "en", "de").description)
        assertEquals("Haberler", programme(others, "en").title)
        assertEquals("Français", programme(others, "en").description)
        // Neither language nor an untagged title: the first one the guide gives.
        assertEquals("Nachrichten", programme("<title lang='de'>Nachrichten</title><title lang='fr'>Infos</title>", "en", "tr").title)
    }
    @Test fun americanFixtureAllowsNoChannelDeclarationsAndMinutePrecisionUtc() {
        val sink = Sink(); assertEquals(EpgParseReport(0, 3, 0, 0), fixture("america", sink))
        assertEquals(3_600_000L, sink.programmes[1].start - sink.programmes[0].start)
        assertEquals(Instant.parse("2026-11-01T12:00:00Z").toEpochMilli(), sink.programmes[2].start)
    }
    @Test fun missingStopIsRetainedWithoutInventingCurrentProgrammeOrClosingGaps() {
        val sink = Sink(); fixture("europe", sink)
        val last = sink.programmes.last()
        assertNull(last.end); assertFalse(last.isOnAt(last.start + 1))
        val slice = GuideSlice(EpgMatch(EpgMatchKind.ID, EpgChannelRef(0, "news.fixture")), sink.programmes)
        assertEquals(sink.programmes[1], slice.now(sink.programmes[1].start))
        assertNull(slice.now(sink.programmes[1].end!! + 1))
        assertEquals(last, slice.next(sink.programmes[1].end!!))
    }
    @Test fun overlapDoesNotPickAnArbitraryCurrentOrNextProgramme() {
        val a = EpgProgramme("c", "A", 10, 100)
        val b = a.copy(title = "B", start = 30)
        val slice = GuideSlice(EpgMatch(EpgMatchKind.ID), listOf(a, b))
        assertNull(slice.now(50)); assertEquals(a, slice.now(20))
        assertNull(slice.copy(programmes = listOf(a, a.copy(title = "C"))).next(0))
    }
    @Test fun datesAreStrictAndAmbiguousTimezoneAbbreviationsAreNotGuessed() {
        listOf("20260230012000 +0300", "20260201016000 +0300", "202601", "20261101013000 CST", "20261101013000 +2500").forEach { assertNull(it, XmltvTime.parse(it)) }
        assertEquals(XmltvTime.parse("20261025120000 +0100"), XmltvTime.parse("202610251200 BST"))
        assertEquals(XmltvTime.parse("20261025120000 +0000"), XmltvTime.parse("202610251200 GMT"))
        assertEquals(XmltvTime.parse("20261025120000 +0000"), XmltvTime.parse("202610251200"))
    }
    @Test fun retentionUsesCalendarDaysThroughSpringAndAutumnTransitions() {
        val zone = ZoneId.of("Europe/Berlin")
        val autumn = GuideWindow.around(Instant.parse("2026-10-25T12:00:00Z"), zone)
        assertEquals(LocalDate.of(2026, 10, 24), Instant.ofEpochMilli(autumn.from).atZone(zone).toLocalDate())
        assertEquals(LocalDate.of(2026, 11, 2), Instant.ofEpochMilli(autumn.until).atZone(zone).toLocalDate())
        assertEquals(217L, (autumn.until - autumn.from) / 3_600_000)
        val spring = GuideWindow.around(Instant.parse("2026-03-29T12:00:00Z"), zone)
        assertEquals(215L, (spring.until - spring.from) / 3_600_000)
    }
    @Test fun invalidAndOutsideProgrammesAreCountedSeparately() {
        val report = parse("""<tv>
            <programme channel="c" start="20261025010000" stop="20261025000000"><title>Reversed</title></programme>
            <programme channel="c" start="20261025010000" stop="broken"><title>Invalid stop</title></programme>
            <programme channel="c" start="20261025010000"><title/></programme>
            <programme channel="c" start="20250101000000" stop="20250101010000"><title>Old</title></programme>
            <programme channel="c" start="20261019120000" stop="20261020010000"><title>Crosses boundary</title></programme>
            </tv>""")
        assertEquals(EpgParseReport(0, 1, 3, 1), report)
    }
    @Test fun utf16BomAndUtf8BomWorkWithoutLeakingExternalDtdRequests() {
        val xml = "<?xml version=\"1.0\" encoding=\"UTF-16\"?><!DOCTYPE tv SYSTEM 'file:///never-read'><tv><channel id='c'><display-name>Örnek</display-name></channel></tv>"
        val sink = Sink()
        XmltvParser().parse(xml.toByteArray(Charsets.UTF_16).inputStream(), window, sink)
        assertEquals("Örnek", sink.channels.single().names.single())
        parse("\uFEFF<tv/>")
    }
    @Test fun internalEntitiesAreRejectedBeforeExpansionAndErrorsAreSanitized() {
        listOf("<!DOCTYPE tv [<!ENTITY x SYSTEM 'file:///private-marker'>]><tv>&x;</tv>",
            "<!DOCTYPE tv [<!ENTITY x 'private-marker'>]><tv>&x;</tv>",
            "<tv><programme start='private-marker'></tv>", "<html>private-marker</html>",
            "<?xml version='1.0' encoding='ISO-8859-1'?><tv/>").forEach { value ->
            val error = assertThrows(SourceFailure::class.java) { parse(value) }
            assertFalse(error.message.orEmpty().contains("private-marker")); assertNull(error.cause)
        }
    }
    @Test fun commentsAndPublicDoctypeDoNotTriggerFalseEntityDetection() {
        parse("<?xml version='1.0'?><!-- <!ENTITY is just a comment> --><!DOCTYPE tv PUBLIC 'x' 'https://example.invalid/dtd'><tv/>")
    }
    @Test fun longTextDeepNestingAndPrologAreBounded() {
        listOf("<tv><channel id='c'><display-name>${"a".repeat(16_385)}</display-name></channel></tv>",
            "<tv>${"<unknown>".repeat(33)}${"</unknown>".repeat(33)}</tv>",
            "<!--${"a".repeat(65_536)}--><tv/>").forEach { assertThrows(SourceFailure::class.java) { parse(it) } }
    }
    @Test fun cancellationPropagatesWithoutBeingReportedAsBadXml() {
        assertThrows(CancellationException::class.java) { XmltvParser().parse("<tv/>".byteInputStream(), window, Sink()) { throw CancellationException() } }
    }
}
