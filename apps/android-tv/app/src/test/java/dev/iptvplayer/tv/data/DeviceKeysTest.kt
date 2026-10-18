package dev.iptvplayer.tv.data

import android.content.SharedPreferences
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class DeviceKeysTest {
    /** SharedPreferences in memory, so DeviceStore's preference calls run without a device; null in a change removes. */
    private class MemoryPreferences : SharedPreferences {
        val values = HashMap<String, Any>()
        override fun getAll(): Map<String, *> = HashMap(values)
        override fun getString(key: String, defValue: String?) = values[key] as String? ?: defValue
        @Suppress("UNCHECKED_CAST")
        override fun getStringSet(key: String, defValues: Set<String>?) = values[key] as Set<String>? ?: defValues
        override fun getInt(key: String, defValue: Int) = values[key] as Int? ?: defValue
        override fun getLong(key: String, defValue: Long) = values[key] as Long? ?: defValue
        override fun getFloat(key: String, defValue: Float) = values[key] as Float? ?: defValue
        override fun getBoolean(key: String, defValue: Boolean) = values[key] as Boolean? ?: defValue
        override fun contains(key: String) = key in values
        override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {}
        override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {}
        override fun edit(): SharedPreferences.Editor = object : SharedPreferences.Editor {
            private val changes = LinkedHashMap<String, Any?>()
            private var clearFirst = false
            private fun change(key: String, value: Any?): SharedPreferences.Editor { changes[key] = value; return this }
            override fun putString(key: String, value: String?) = change(key, value)
            override fun putStringSet(key: String, values: Set<String>?) = change(key, values?.toSet())
            override fun putInt(key: String, value: Int) = change(key, value)
            override fun putLong(key: String, value: Long) = change(key, value)
            override fun putFloat(key: String, value: Float) = change(key, value)
            override fun putBoolean(key: String, value: Boolean) = change(key, value)
            override fun remove(key: String) = change(key, null)
            override fun clear(): SharedPreferences.Editor { clearFirst = true; return this }
            override fun commit(): Boolean {
                if (clearFirst) values.clear()
                changes.forEach { (key, value) -> if (value == null) values.remove(key) else values[key] = value }
                return true
            }
            override fun apply() { commit() }
        }
    }

    /** Two sources whose ids start the same, as SourceRegistryStorageTest uses on the device. */
    private val owners = listOf("source", "source:a")

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
    }
}
