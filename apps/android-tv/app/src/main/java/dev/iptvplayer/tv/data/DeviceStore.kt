package dev.iptvplayer.tv.data

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import org.json.JSONObject
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Source details are encrypted with a non-exportable Android Keystore key. Backups are disabled. Every preference key
 * comes from [DeviceKeys], which says what each one belongs to and what removing a source or starting over takes along.
 */
class DeviceStore internal constructor(private val prefs: SharedPreferences) {
    constructor(context: Context) : this(context.getSharedPreferences("device_state", Context.MODE_PRIVATE))

    private val alias = "iptv_source_v1"

    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(alias, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true).build())
        }.generateKey()
    }

    @Synchronized internal fun saveSource(source: SourceConfig) {
        val plain = JSONObject().put("id", source.id).put("name", source.name).put("type", source.type.name)
            .put("address", source.address).put("username", source.username).put("password", source.password).put("epgUrl", source.epgUrl)
            .toString().toByteArray()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.ENCRYPT_MODE, key()) }
        val encrypted = cipher.doFinal(plain)
        val saved = prefs.edit().putString(DeviceKeys.legacySource.name, Base64.encodeToString(cipher.iv + encrypted, Base64.NO_WRAP)).commit()
        plain.fill(0)
        if (!saved) throw SourceFailure(FailureReason.SOURCE_NOT_STORED)
    }

    @Synchronized internal fun seal(plain: ByteArray, binding: String): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.ENCRYPT_MODE, key()); updateAAD(binding.toByteArray()) }
        return cipher.iv + cipher.doFinal(plain)
    }
    @Synchronized internal fun unseal(encrypted: ByteArray, binding: String): ByteArray {
        require(encrypted.size > 28)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, encrypted.copyOfRange(0, 12)))
            updateAAD(binding.toByteArray())
        }
        return cipher.doFinal(encrypted.copyOfRange(12, encrypted.size))
    }

    @Synchronized fun loadSource(): SourceConfig? {
        try {
            val stored = prefs.getString(DeviceKeys.legacySource.name, null) ?: return null
            val bytes = Base64.decode(stored, Base64.NO_WRAP)
            require(bytes.size > 28)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
                init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, bytes.copyOfRange(0, 12)))
            }
            val plain = cipher.doFinal(bytes.copyOfRange(12, bytes.size))
            val json = JSONObject(plain.toString(Charsets.UTF_8))
            plain.fill(0)
            return SourceConfig(json.getString("id"), json.getString("name"), SourceType.valueOf(json.getString("type")),
                json.getString("address"), json.optString("username"), json.optString("password"), json.optString("epgUrl", ""))
        } catch (_: Exception) {
            throw SourceFailure(FailureReason.STORED_SOURCES_UNAVAILABLE)
        }
    }

    /** Opaque snapshot token; prevents a stale recovery screen from clearing newly repaired accounts. */
    internal fun recoveryStamp(): String = stableId(listOf(DeviceKeys.legacySource.name, DeviceKeys.sources.name).joinToString("\u0000") {
        val value = prefs.all[it]
        "$it:${value?.javaClass?.name}:$value"
    })

    val hasSourceRegistry: Boolean get() = prefs.contains(DeviceKeys.sources.name)

    /** An existing registry is authoritative, including when it is unreadable. */
    @Synchronized fun loadSources(): SourceRegistry {
        try {
            val stored = prefs.getString(DeviceKeys.sources.name, null)
                ?: return SourceRegistry(loadSource()?.let(::listOf).orEmpty())
            require(stored.length <= 1_400_000)
            val plain = unseal(Base64.decode(stored, Base64.NO_WRAP), "source-registry-v1")
            return try { SourceRegistryCodec.decode(plain) } finally { plain.fill(0) }
        } catch (_: Exception) { throw SourceFailure(FailureReason.STORED_SOURCES_UNAVAILABLE) }
    }

    /**
     * Call inside SourceTransactions. Registry replacement and scoped state cleanup share one preference commit: every
     * SOURCE key in [DeviceKeys] of a source the registry no longer holds goes with it.
     */
    @Synchronized fun saveSources(registry: SourceRegistry) {
        val accepted = registry.copy(sources = registry.sources.map { it.validated() }).checked()
        loadSources() // An unreadable registry is never replaced here; only the explicit recovery reset may drop it.
        val legacy = loadSource()
        val retained = accepted.sources.mapTo(HashSet()) { it.id }
        val plain = SourceRegistryCodec.encode(accepted)
        val sealed = try {
            require(plain.size <= 1_000_000)
            seal(plain, "source-registry-v1")
        } finally { plain.fill(0) }
        val editor = prefs.edit().putString(DeviceKeys.sources.name, Base64.encodeToString(sealed, Base64.NO_WRAP))
            .remove(DeviceKeys.legacySource.name)
        // Pre-0.9 progress keys were opaque hashes. There was one source, so their owner is the legacy source.
        if (legacy != null) prefs.all.keys.filter(DeviceKeys.legacyProgress::matches).forEach { key ->
            if (legacy.id in retained) prefs.getString(key, null)?.let { value ->
                val target = DeviceKeys.progress.of(legacy.id) + key.removePrefix(DeviceKeys.legacyProgress.name)
                if (!prefs.contains(target)) editor.putString(target, value)
            }
            editor.remove(key)
        }
        DeviceKeys.leftBehind(prefs.all.keys, retained).forEach { editor.remove(it) }
        DeviceKeys.pruned(prefs.all, retained).forEach { (key, value) ->
            when (value) {
                null -> editor.remove(key)
                is String -> editor.putString(key, value)
                is Set<*> -> editor.putStringSet(key, value.filterIsInstanceTo(HashSet<String>()))
            }
        }
        if (!editor.commit()) throw SourceFailure(FailureReason.SOURCES_NOT_STORED)
    }

    @Synchronized fun forgetSource(id: String) { saveSources(loadSources().without(id)) }

    private fun progressKey(source: String, id: String) = DeviceKeys.progress.of(source) + stableId("$source:$id")

    fun forgetSource() {
        // Device accessibility and sound preferences survive a source change: only the keys DeviceKeys marks reset go.
        val removed = prefs.edit().apply { DeviceKeys.cleared(prefs.all.keys).forEach { remove(it) } }.commit()
        if (!removed) throw SourceFailure(FailureReason.SOURCE_NOT_REMOVED)
    }

    /**
     * "PIN'i unuttum → Her şeyi sıfırla" (pin.md §3.11), inside SourceTransactions: what starting over removes, and the
     * adult filter back on, in one commit. The TV's own settings stay, and hidden categories stay hidden.
     */
    fun startOver() {
        val editor = prefs.edit().apply { DeviceKeys.cleared(prefs.all.keys).forEach { remove(it) } }
            .putBoolean(DeviceKeys.hideAdultCategories.name, true)
        if (!editor.commit()) throw SourceFailure(FailureReason.SOURCE_NOT_REMOVED)
    }

    /** Explicit unreadable-record recovery only, after all encrypted catalog files are removed. */
    @Synchronized internal fun resetUnreadableSources() {
        KeyStore.getInstance("AndroidKeyStore").apply { load(null); deleteEntry(alias) }
        forgetSource()
    }

    fun history(source: String): List<WatchRecord> = WatchHistory.decode(prefs.getString(DeviceKeys.history.of(source), null))
    fun history(source: String, records: List<WatchRecord>) {
        prefs.edit().putString(DeviceKeys.history.of(source), WatchHistory.encode(records)).apply()
    }
    internal fun lastLive(source: String): LastLive? = LastLive.decode(prefs.getString(DeviceKeys.lastLive.of(source), null))
    internal fun lastLive(source: String, value: LastLive) { prefs.edit().putString(DeviceKeys.lastLive.of(source), value.encode()).apply() }
    fun clearHistory(source: String) {
        val ownsLegacyProgress = runCatching { loadSource()?.id == source }.getOrDefault(false)
        prefs.edit().remove(DeviceKeys.history.of(source)).remove(DeviceKeys.lastLive.of(source)).apply {
            prefs.all.keys.filter { DeviceKeys.progress.owner(it) == source || (ownsLegacyProgress && DeviceKeys.legacyProgress.matches(it)) }
                .forEach { remove(it) }
        }.apply()
    }

    fun progress(source: String, id: String): PlaybackMark? {
        return try {
            val value = prefs.getString(progressKey(source, id), null)
                ?: if (loadSource()?.id == source) prefs.getString(DeviceKeys.legacyProgress.name + stableId("$source:$id"), null) else null
                ?: return null
            val json = JSONObject(value)
            PlaybackMark(json.getLong("position"), json.getLong("duration"), json.getLong("time"))
        } catch (_: Exception) { null }
    }

    fun saveProgress(source: String, id: String, position: Long, duration: Long) {
        if (position < 0) return
        val json = JSONObject().put("position", position).put("duration", duration.coerceAtLeast(0)).put("time", System.currentTimeMillis())
        prefs.edit().putString(progressKey(source, id), json.toString()).apply()
    }

    fun favorites(source: String): Set<String> = prefs.getStringSet(DeviceKeys.favorites.of(source), emptySet()).orEmpty().toSet()
    fun favorites(source: String, ids: Set<String>) { prefs.edit().putStringSet(DeviceKeys.favorites.of(source), ids.toSet()).apply() }
    /** D121: the free week and Play's last answer (DeviceKeys.membership). */
    var membership: Membership
        get() = Membership.decode(prefs.getString(DeviceKeys.membership.name, null))
        set(value) { prefs.edit().putString(DeviceKeys.membership.name, value.encode()).apply() }
    var reducedMotion: Boolean
        get() = prefs.getBoolean(DeviceKeys.reducedMotion.name, false)
        set(value) { prefs.edit().putBoolean(DeviceKeys.reducedMotion.name, value).apply() }
    var startupSound: Boolean
        get() = prefs.getBoolean(DeviceKeys.startupSound.name, true)
        set(value) { prefs.edit().putBoolean(DeviceKeys.startupSound.name, value).apply() }
    var guideZoneId: String
        get() = prefs.getString(DeviceKeys.guideZone.name, "").orEmpty().takeIf { it.isEmpty() || runCatching { java.time.ZoneId.of(it) }.isSuccess }.orEmpty()
        set(value) { require(value.isEmpty() || runCatching { java.time.ZoneId.of(value) }.isSuccess); prefs.edit().putString(DeviceKeys.guideZone.name, value).apply() }
    fun librarySort(kind: MediaKind): LibrarySort =
        prefs.getString(DeviceKeys.librarySort.name + kind.name, null)?.let { name -> LibrarySort.entries.firstOrNull { it.name == name } } ?: LibrarySort.DEFAULT
    fun librarySort(kind: MediaKind, sort: LibrarySort) { prefs.edit().putString(DeviceKeys.librarySort.name + kind.name, sort.name).apply() }
    /** Hidden categories as [CategoryVisibility.key]s (D068); names only, never addresses or accounts. */
    var hiddenCategories: Set<String>
        get() = prefs.getStringSet(DeviceKeys.hiddenCategories.name, emptySet()).orEmpty().toSet()
        set(value) { prefs.edit().putStringSet(DeviceKeys.hiddenCategories.name, value.toSet()).apply() }
    var hideAdultCategories: Boolean
        get() = prefs.getBoolean(DeviceKeys.hideAdultCategories.name, true)
        set(value) { prefs.edit().putBoolean(DeviceKeys.hideAdultCategories.name, value).apply() }
    var autoNextEpisode: Boolean
        get() = prefs.getBoolean(DeviceKeys.autoNextEpisode.name, true)
        set(value) { prefs.edit().putBoolean(DeviceKeys.autoNextEpisode.name, value).apply() }
    var startLastChannel: Boolean
        get() = prefs.getBoolean(DeviceKeys.startLastChannel.name, false)
        set(value) { prefs.edit().putBoolean(DeviceKeys.startLastChannel.name, value).apply() }
    var audioLanguage: String?
        get() = prefs.getString(DeviceKeys.audioLanguage.name, null)
        set(value) { prefs.edit().putString(DeviceKeys.audioLanguage.name, value).apply() }
    var subtitleLanguage: String?
        get() = prefs.getString(DeviceKeys.subtitleLanguage.name, null)
        set(value) { prefs.edit().putString(DeviceKeys.subtitleLanguage.name, value).apply() }
    var subtitlesOff: Boolean
        get() = prefs.getBoolean(DeviceKeys.subtitlesOff.name, true)
        set(value) { prefs.edit().putBoolean(DeviceKeys.subtitlesOff.name, value).apply() }
    /** Settings → Oynatıcı, every field written in one go. */
    var playback: PlaybackPreferences
        get() { val stored = prefs.all; return PlaybackPreferencesCodec.decode { stored[DeviceKeys.playback.name + it] } }
        set(value) {
            prefs.edit().apply {
                PlaybackPreferencesCodec.encode(value).forEach { (field, stored) ->
                    if (stored is Boolean) putBoolean(DeviceKeys.playback.name + field, stored) else putString(DeviceKeys.playback.name + field, stored.toString())
                }
            }.apply()
        }
    /** The language tag chosen in Settings, "" for the TV's (LocaleController). */
    var appLanguage: String
        get() = prefs.getString(DeviceKeys.appLanguage.name, "").orEmpty()
        set(value) { prefs.edit().putString(DeviceKeys.appLanguage.name, value).apply() }
    /** Whether Android 13 or later has been given [appLanguage] once (LocaleController). */
    var appLanguageHandedOver: Boolean
        get() = prefs.getBoolean(DeviceKeys.appLanguageHandedOver.name, false)
        set(value) { prefs.edit().putBoolean(DeviceKeys.appLanguageHandedOver.name, value).apply() }
}
