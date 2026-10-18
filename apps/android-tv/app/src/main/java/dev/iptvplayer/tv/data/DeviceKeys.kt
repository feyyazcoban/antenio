package dev.iptvplayer.tv.data

/** Whether a stored preference belongs to the whole TV, or to one saved source that takes it along when removed. */
enum class KeyScope { DEVICE, SOURCE }

/** What a backup (gap plan S41) may carry of a preference: nothing, a viewer setting, or one source's personal data. */
enum class BackupClass { NONE, SETTINGS, PERSONAL }

/**
 * One preference in DeviceStore's `device_state`, or a family of keys that start with [name]; [layout] says how the rest
 * of a key reads. [reset] says whether starting over (DeviceStore.forgetSource, later "Her şeyi sıfırla") removes it:
 * the TV's own settings survive a source change.
 */
internal class DeviceKey(val name: String, val scope: KeyScope, val reset: Boolean, val backup: BackupClass,
    private val layout: Layout = Layout.WHOLE) {

    enum class Layout {
        /** The key is [name]. */
        WHOLE,
        /** [name] and a part that names no source: `sort:MOVIE`. */
        FAMILY,
        /** [name] and the source id: `f:<id>`. */
        OWNER,
        /** [name], the id's length, the id and an item: `p2:6:source:<hash>`, so `source` and `source:a` never mix. */
        SIZED,
        /** [name] and an item of the one source saved before the registry; nothing in the key names that source. */
        LEGACY
    }

    init { require((scope == KeyScope.SOURCE) == (layout in setOf(Layout.OWNER, Layout.SIZED, Layout.LEGACY))) }

    fun matches(key: String) = if (layout == Layout.WHOLE) key == name else key.startsWith(name)

    /** The key [source] keeps under, or for a SIZED family the start all of its keys share. */
    fun of(source: String): String = when (layout) {
        Layout.OWNER -> name + source
        Layout.SIZED -> "$name${source.length}:$source:"
        else -> error("$name names no source")
    }

    /** The source a stored [key] belongs to; null when the key is not of this family or names no source. */
    fun owner(key: String): String? {
        if (!matches(key)) return null
        val rest = key.substring(name.length)
        return when (layout) {
            Layout.OWNER -> rest
            Layout.SIZED -> {
                val colon = rest.indexOf(':')
                val size = rest.substring(0, colon.coerceAtLeast(0)).toIntOrNull()?.takeIf { it >= 0 } ?: return null
                val end = colon + 1 + size
                if (end < rest.length && rest[end] == ':') rest.substring(colon + 1, end) else null
            }
            else -> null
        }
    }

    override fun toString() = "DeviceKey($name, $scope)"
}

/**
 * Every preference DeviceStore keeps, with its scope, whether starting over removes it and its backup class (gap plan
 * T5). DeviceStore names no key itself: a new key is added here first, and a SOURCE key then leaves with its source.
 */
internal object DeviceKeys {
    /** The encrypted source registry. Sealed with this TV's Keystore key, so a backup carries the decoded sources instead. */
    val sources = DeviceKey("sources_v2", KeyScope.DEVICE, reset = true, BackupClass.NONE)
    /** The one source saved before the registry (before 0.9), sealed the same way; the first registry save removes it. */
    val legacySource = DeviceKey("source", KeyScope.DEVICE, reset = true, BackupClass.NONE)
    /** That source's progress under a bare hash; the first registry save moves it to [progress], so no backup meets it. */
    val legacyProgress = DeviceKey("p:", KeyScope.SOURCE, reset = true, BackupClass.NONE, DeviceKey.Layout.LEGACY)
    /** Where a title stopped, under a one-way hash of its id. */
    val progress = DeviceKey("p2:", KeyScope.SOURCE, reset = true, BackupClass.PERSONAL, DeviceKey.Layout.SIZED)
    val favorites = DeviceKey("f:", KeyScope.SOURCE, reset = true, BackupClass.PERSONAL, DeviceKey.Layout.OWNER)
    val history = DeviceKey("history:", KeyScope.SOURCE, reset = true, BackupClass.PERSONAL, DeviceKey.Layout.OWNER)
    val reducedMotion = DeviceKey("reduced", KeyScope.DEVICE, reset = false, BackupClass.SETTINGS)
    val startupSound = DeviceKey("startupSound", KeyScope.DEVICE, reset = false, BackupClass.SETTINGS)
    val guideZone = DeviceKey("guideZoneId", KeyScope.DEVICE, reset = false, BackupClass.SETTINGS)
    /** Sırala's choice, one key per section: `sort:MOVIE`, `sort:SERIES`. */
    val librarySort = DeviceKey("sort:", KeyScope.DEVICE, reset = false, BackupClass.SETTINGS, DeviceKey.Layout.FAMILY)
    /** D068: category names hidden across all sources, so they stay when a source goes. */
    val hiddenCategories = DeviceKey("hiddenCategories", KeyScope.DEVICE, reset = false, BackupClass.SETTINGS)
    val hideAdultCategories = DeviceKey("hideAdultCategories", KeyScope.DEVICE, reset = false, BackupClass.SETTINGS)
    val autoNextEpisode = DeviceKey("autoNextEpisode", KeyScope.DEVICE, reset = false, BackupClass.SETTINGS)
    val audioLanguage = DeviceKey("audioLanguage", KeyScope.DEVICE, reset = false, BackupClass.SETTINGS)
    val subtitleLanguage = DeviceKey("subtitleLanguage", KeyScope.DEVICE, reset = false, BackupClass.SETTINGS)
    val subtitlesOff = DeviceKey("subtitlesOff", KeyScope.DEVICE, reset = false, BackupClass.SETTINGS)

    val all = listOf(sources, legacySource, legacyProgress, progress, favorites, history, reducedMotion, startupSound,
        guideZone, librarySort, hiddenCategories, hideAdultCategories, autoNextEpisode, audioLanguage, subtitleLanguage,
        subtitlesOff)

    /** The entry a stored [key] falls under; null for a key no NEXTV build wrote. */
    fun find(key: String): DeviceKey? = all.firstOrNull { it.matches(key) }

    /**
     * Of the stored [keys], those of a source not among [retained]. Removing a source takes every key of it along,
     * including one an interrupted removal left behind. The legacy progress keys name no source; saveSources moves them.
     */
    fun leftBehind(keys: Collection<String>, retained: Set<String>): List<String> =
        keys.filter { key -> all.firstNotNullOfOrNull { it.owner(key) }?.let { it !in retained } == true }

    /** Of the stored [keys], those starting over removes. */
    fun cleared(keys: Collection<String>): List<String> = keys.filter { find(it)?.reset == true }
}
