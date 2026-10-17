package dev.iptvplayer.tv.data

import org.junit.Assert.*
import org.junit.Test

class GuideTimelineTest {
    @Test fun partitionsGapsAndOverlapsWithoutInventingProgrammeDuration() {
        val a = EpgProgramme("c", "A", 10, 50)
        val b = EpgProgramme("c", "B", 30, 70)
        val point = EpgProgramme("c", "Unknown end", 80, null)
        val slots = guideSlots(listOf(a, b, point), GuideWindow(0, 100))
        assertEquals(listOf(0L, 10L, 30L, 50L, 70L, 80L, 81L), slots.map { it.from })
        assertEquals(listOf(a, b), slots.first { it.contains(40) }.programmes)
        assertTrue(slots.first { it.contains(80) }.point)
        assertTrue(slots.first { it.contains(82) }.programmes.isEmpty())
    }
    @Test fun aVerticalTimeAnchorFindsTheContainingProgrammeAcrossUnequalDurations() {
        val a = guideSlots(listOf(EpgProgramme("a", "Long", 0, 80)), GuideWindow(0, 100))
        val b = guideSlots(listOf(EpgProgramme("b", "Short", 0, 20), EpgProgramme("b", "Next", 20, 90)), GuideWindow(0, 100))
        val anchor = 35L
        assertEquals("Long", a.single { it.contains(anchor) }.programme!!.title)
        assertEquals("Next", b.single { it.contains(anchor) }.programme!!.title)
        assertEquals(90L, guideHorizontalTarget(b, anchor, 1))
        assertEquals(0L, guideHorizontalTarget(b, anchor, -1))
    }
    @Test fun clippingKeepsOriginalProgrammeTimesAndAnEmptyDayIsExplicitlyEmpty() {
        val p = EpgProgramme("c", "Across midnight", 0, 200)
        val slot = guideSlots(listOf(p), GuideWindow(50, 100)).single()
        assertEquals(50L, slot.from); assertEquals(100L, slot.until); assertEquals(p, slot.programme)
        assertTrue(guideSlots(emptyList(), GuideWindow(50, 100)).single().programmes.isEmpty())
    }
    @Test fun repeatedDstClockTimesShowBothOffsetsInsteadOfAnApparentZeroLength() {
        val p = EpgProgramme("c", "DST", XmltvTime.parse("20261025023000 +0200")!!, XmltvTime.parse("20261025023000 +0100"))
        assertEquals("02:30 (UTC+02:00) — 02:30 (UTC+01:00)", dev.iptvplayer.tv.ui.programmeTime(p, java.time.ZoneId.of("Europe/Berlin"), unknownEnd = "?"))
    }
}
