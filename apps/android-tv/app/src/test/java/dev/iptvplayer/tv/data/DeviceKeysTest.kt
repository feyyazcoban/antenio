package dev.iptvplayer.tv.data

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class DeviceKeysTest {
    /** Two sources whose ids start the same, as SourceRegistryStorageTest uses on the device. */
    private val owners = listOf("source", "source:a")

    /** Oynatıcı settings unlike the defaults in every field. */
    private val player = PlaybackPreferences(FrameRateMatching.OFF, BufferPreset.LARGE, DecoderMode.SOFTWARE_FIRST, AudioOutput.PCM,
        tunneling = true, SubtitleStyle(SubtitleSource.APP, SubtitleSize.XLARGE, SubtitleBackground.NONE, SubtitleColor.YELLOW),
        OsdTimeout.NEVER, PanelOpacity.SHEER)

    /** Everything DeviceStore writes without the Keystore: per-source data for [owners] and every device setting. */
    private fun filled(): Pair<MemoryPreferences, DeviceStore> {
        val memory = MemoryPreferences()
        val store = DeviceStore(memory)
        owners.forEach { id ->
            store.favorites(id, setOf("movie:1"))
            store.history(id, listOf(WatchRecord("movie:1", MediaKind.MOVIE, positionMs = 1000, durationMs = 10_000)))
            store.saveProgress(id, "movie:1", 1000, 10_000)
        }
        store.reducedMotion = true; store.startupSound = false; store.guideZoneId = "Europe/Istanbul"
        MediaKind.entries.forEach { store.librarySort(it, LibrarySort.TITLE) }
        store.hiddenCategories = setOf(CategoryVisibility.key(MediaKind.LIVE, "Haber")); store.hideAdultCategories = false
        store.autoNextEpisode = false; store.audioLanguage = "tr"; store.subtitleLanguage = "en"; store.subtitlesOff = false
        store.appLanguage = "tr"; store.appLanguageHandedOver = true
        store.playback = player
        return memory to store
    }

    @Test fun everyKeyDeviceStoreWritesIsRegistered() {
        val (memory, _) = filled()
        for (key in memory.values.keys) assertNotNull("Not in DeviceKeys: $key", DeviceKeys.find(key))
        // The rest are written only with the Keystore (the registry and its migration), checked by the next test.
        val sealed = setOf(DeviceKeys.sources, DeviceKeys.legacySource, DeviceKeys.legacyProgress)
        assertEquals(DeviceKeys.all.toSet() - sealed, memory.values.keys.mapTo(HashSet()) { DeviceKeys.find(it)!! })
        for (key in memory.values.keys) {
            val entry = DeviceKeys.find(key)!!
            if (entry.scope == KeyScope.SOURCE) assertTrue(key, entry.owner(key) in owners) else assertNull(key, entry.owner(key))
        }
    }

    @Test fun deviceStoreNamesNoPreferenceKeyItself() {
        val source = File("src/main/java/dev/iptvplayer/tv/data/DeviceStore.kt").readText()
        // Editor puts and removes, reads on prefs; the JSON values' own getString/put calls name fields, not keys.
        val literalKey = Regex("\\bput(String|StringSet|Boolean|Int|Long|Float)\\(\\s*\"|\\bprefs\\s*\\.\\s*(get\\w*\\(|all\\[)\\s*\"|" +
            "\\b(remove|contains)\\(\\s*\"")
        assertEquals(emptyList<String>(), literalKey.findAll(source).map { it.value }.toList())
        assertTrue(source.contains("DeviceKeys.legacySource.name") && source.contains("DeviceKeys.sources.name"))
    }

    @Test fun everyEntryIsListedOnceAndSourceDataLeavesWithItsSource() {
        val declared = DeviceKeys::class.java.declaredFields.filter { it.type == DeviceKey::class.java }
            .map { field -> field.isAccessible = true; field.get(DeviceKeys) as DeviceKey }
        assertEquals(declared.toSet(), DeviceKeys.all.toSet())
        assertEquals(DeviceKeys.all.size, DeviceKeys.all.map { it.name }.distinct().size)
        // No entry hides another: each one's own name finds it.
        for (entry in DeviceKeys.all) assertSame(entry, DeviceKeys.find(entry.name))
        for (entry in DeviceKeys.all.filter { it.scope == KeyScope.SOURCE }) {
            assertTrue(entry.name, entry.reset)
            assertNotEquals(entry.name, BackupClass.SETTINGS, entry.backup)
        }
        // Sealed with this TV's Keystore key: never copied as they are.
        for (entry in listOf(DeviceKeys.sources, DeviceKeys.legacySource)) assertEquals(BackupClass.NONE, entry.backup)
        assertNull(DeviceKeys.find("unknown"))
    }

    @Test fun removingASourceLeavesNoKeyOfItAndKeepsTheOneWhoseIdStartsTheSame() {
        val (memory, _) = filled()
        val keys = memory.values.keys.toList()
        val left = DeviceKeys.leftBehind(keys, setOf("source"))
        assertEquals(setOf(DeviceKeys.favorites.of("source:a"), DeviceKeys.history.of("source:a")),
            left.filterNot { it.startsWith(DeviceKeys.progress.of("source:a")) }.toSet())
        assertEquals(1, left.count { it.startsWith(DeviceKeys.progress.of("source:a")) })
        assertEquals(3, left.size)
        assertEquals(emptyList<String>(), DeviceKeys.leftBehind(keys, owners.toSet()))
        // Device settings belong to no source; the pre-registry progress keys are saveSources' own migration.
        assertEquals(emptyList<String>(), DeviceKeys.leftBehind(listOf("p:abc", "reduced", "sort:MOVIE", "unknown"), emptySet()))
        assertNull(DeviceKeys.progress.owner("p2:99:source:abc")); assertNull(DeviceKeys.progress.owner("p2:x:source:abc"))
    }

    @Test fun aKeyHoldingSeveralSourcesKeepsTheEntriesOfThoseThatStay() {
        // One key for all sources, as the planned reminders and library are: their entries go, the key stays.
        val acks = DeviceKey("acks", KeyScope.SOURCE, reset = true, BackupClass.PERSONAL, DeviceKey.Layout.SHARED) {
            value, retained -> (value as Set<*>).filterTo(HashSet()) { it in retained }.ifEmpty { null }
        }
        val values = mapOf("acks" to setOf("source", "source:a"), DeviceKeys.reducedMotion.name to true)
        assertEquals(mapOf("acks" to setOf("source")), DeviceKeys.pruned(values, setOf("source"), listOf(acks)))
        assertEquals(mapOf("acks" to null), DeviceKeys.pruned(values, emptySet(), listOf(acks)))
        assertEquals(emptyMap<String, Any?>(), DeviceKeys.pruned(values, owners.toSet(), listOf(acks)))
        // A value the entry cannot read stays, and the whole-key sweep never takes it.
        assertEquals(emptyMap<String, Any?>(), DeviceKeys.pruned(mapOf("acks" to "unreadable"), emptySet(), listOf(acks)))
        assertTrue(acks.matches("acks")); assertFalse(acks.matches("acks:source")); assertNull(acks.owner("acks"))
        // A key of one source names it, and only a SHARED key prunes.
        assertThrows(IllegalArgumentException::class.java) { DeviceKey("x", KeyScope.SOURCE, reset = true, BackupClass.PERSONAL) }
        assertThrows(IllegalArgumentException::class.java) {
            DeviceKey("x", KeyScope.SOURCE, reset = true, BackupClass.PERSONAL, DeviceKey.Layout.SHARED)
        }
        assertEquals(emptyMap<String, Any?>(), DeviceKeys.pruned(mapOf(DeviceKeys.favorites.of("source") to setOf("1")), emptySet()))
    }

    @Test fun clearingHistoryTakesOnlyThatSourcesHistoryAndProgress() {
        val (memory, store) = filled()
        store.clearHistory("source")
        assertTrue(store.history("source").isEmpty()); assertNull(store.progress("source", "movie:1"))
        assertEquals(1, store.history("source:a").size); assertEquals(1000L, store.progress("source:a", "movie:1")!!.positionMs)
        assertEquals(setOf("movie:1"), store.favorites("source"))
        assertTrue(memory.values.keys.none { DeviceKeys.history.owner(it) == "source" || DeviceKeys.progress.owner(it) == "source" })
    }

    @Test fun startingOverKeepsTheTvsOwnSettings() {
        val (memory, store) = filled()
        // What the Keystore paths would have left: the registry, the pre-registry source and its progress.
        memory.values += mapOf(DeviceKeys.sources.name to "sealed", DeviceKeys.legacySource.name to "sealed",
            DeviceKeys.legacyProgress.name + "abc" to "{}", "unknown" to true)
        val settings = memory.values.keys.filter { DeviceKeys.find(it)?.scope == KeyScope.DEVICE && DeviceKeys.find(it)?.reset == false }
        store.forgetSource()
        assertEquals((settings + "unknown").toSet(), memory.values.keys)
        assertTrue(store.reducedMotion); assertFalse(store.startupSound); assertEquals(LibrarySort.TITLE, store.librarySort(MediaKind.MOVIE))
        assertEquals(player, store.playback)
    }

    @Test fun forgettingThePinStartsOverWithTheAdultFilterBackOn() {
        val (memory, store) = filled()
        memory.values += mapOf(DeviceKeys.sources.name to "sealed", DeviceKeys.legacyProgress.name + "abc" to "{}")
        val settings = memory.values.keys.filter { DeviceKeys.find(it)?.reset == false }
        assertFalse(store.hideAdultCategories)
        store.startOver()
        assertEquals(settings.toSet(), memory.values.keys)
        assertTrue(store.hideAdultCategories)
        // Hidden categories stay hidden, and the TV's own settings stay.
        assertEquals(setOf(CategoryVisibility.key(MediaKind.LIVE, "Haber")), store.hiddenCategories)
        assertTrue(store.reducedMotion); assertEquals("Europe/Istanbul", store.guideZoneId)
        assertEquals(player, store.playback)
    }
}
