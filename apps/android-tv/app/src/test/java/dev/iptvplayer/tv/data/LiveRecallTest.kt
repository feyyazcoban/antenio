package dev.iptvplayer.tv.data

import org.junit.Assert.*
import org.junit.Test

class LiveRecallTest {
    private val scope = ChannelScope(category = "News")
    private fun pick(id: String) = LivePick(ContentRef("source", id).key, scope)

    @Test fun sameChannelDoesNotReplacePreviousAndRecallToggles() {
        val recall = LiveRecall()
        recall.played(pick("one")); recall.played(pick("two")); recall.played(pick("two"))
        assertEquals(pick("one"), recall.previous)
        recall.played(recall.previous!!)
        assertEquals(pick("one"), recall.current); assertEquals(pick("two"), recall.previous)
        recall.removeSource("other"); assertEquals(pick("two"), recall.previous)
        recall.removeSource("source"); assertNull(recall.current); assertNull(recall.previous)
    }

    @Test fun lastChannelCodecRejectsInvalidDataAndKeepsOpaqueIdentity() {
        val value = LastLive("live:one:TR", 91)
        assertEquals(value, LastLive.decode(value.encode()))
        listOf(null, "", "[]", "{\"id\":\"\",\"at\":1}", "{\"id\":\"a\",\"at\":-1}").forEach {
            assertNull(LastLive.decode(it))
        }
    }

    @Test fun autoStartOnlyUsesValidLatestLiveAndWaitsForAppUnlock() {
        val first = MediaEntry("one", "1", MediaKind.LIVE, "One", "News").inSource("source")
        val second = MediaEntry("two", "2", MediaKind.LIVE, "Two", "News").inSource("other")
        val records = linkedMapOf("source" to LastLive("one", 20), "other" to LastLive("two", 21))
        fun decide(enabled: Boolean = true, launch: Boolean = true, channels: List<MediaEntry> = listOf(first, second),
            appLocked: Boolean = false, locked: (String) -> Boolean = { false }) =
            AutoStart.decide(enabled, launch, records, channels, appLocked, locked)
        assertEquals(AutoStartDecision.Play(second.id), decide())
        assertEquals(AutoStartDecision.AfterUnlock(second.id), decide(appLocked = true))
        assertEquals(AutoStartDecision.Home, decide(enabled = false))
        assertEquals(AutoStartDecision.Home, decide(launch = false))
        assertEquals(AutoStartDecision.Home, decide(channels = listOf(first)))
        assertEquals(AutoStartDecision.Home, decide(locked = { it == second.id }))
        assertEquals(AutoStartDecision.Home, decide(channels = listOf(first, second.copy(adult = true))))
    }
}
