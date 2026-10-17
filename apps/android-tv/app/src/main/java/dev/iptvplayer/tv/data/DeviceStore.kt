package dev.iptvplayer.tv.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import org.json.JSONObject
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Source details are encrypted with a non-exportable Android Keystore key. Backups are disabled. */
class DeviceStore(context: Context) {
    private val prefs = context.getSharedPreferences("device_state", Context.MODE_PRIVATE)
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

    @Synchronized fun saveSource(source: SourceConfig) {
        val plain = JSONObject().put("id", source.id).put("name", source.name).put("type", source.type.name)
            .put("address", source.address).put("username", source.username).put("password", source.password).put("epgUrl", source.epgUrl)
            .toString().toByteArray()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.ENCRYPT_MODE, key()) }
        val encrypted = cipher.doFinal(plain)
        val saved = prefs.edit().putString("source", Base64.encodeToString(cipher.iv + encrypted, Base64.NO_WRAP)).commit()
        plain.fill(0)
        if (!saved) throw SourceFailure("Kaynak cihazda saklanamadı. Boş alanı kontrol et.")
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
            val stored = prefs.getString("source", null) ?: return null
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
            throw StoredSourcesUnavailable()
        }
    }

    /** Opaque snapshot token; prevents a stale recovery screen from clearing newly repaired accounts. */
    internal fun recoveryStamp(): String = stableId(listOf("source", "sources_v2").joinToString("\u0000") {
        val value = prefs.all[it]
        "$it:${value?.javaClass?.name}:$value"
    })

    val hasSourceRegistry: Boolean get() = prefs.contains("sources_v2")

    /** An existing registry is authoritative, including when it is unreadable. */
    @Synchronized fun loadSources(): SourceRegistry {
        try {
            val stored = prefs.getString("sources_v2", null)
                ?: return SourceRegistry(loadSource()?.let(::listOf).orEmpty())
            require(stored.length <= 1_400_000)
            val plain = unseal(Base64.decode(stored, Base64.NO_WRAP), "source-registry-v1")
            return try { SourceRegistryCodec.decode(plain) } finally { plain.fill(0) }
        } catch (_: Exception) { throw StoredSourcesUnavailable() }
    }

    /** Call inside SourceTransactions. Registry replacement and scoped state cleanup share one preference commit. */
    @Synchronized fun saveSources(registry: SourceRegistry) {
        val accepted = registry.copy(sources = registry.sources.map { it.validated() }).checked()
        val previous = loadSources()
        val legacy = loadSource()
        val retained = accepted.sources.mapTo(HashSet()) { it.id }
        val removed = previous.sources.map { it.id }.filterNot { it in retained }
        val plain = SourceRegistryCodec.encode(accepted)
        val sealed = try {
            require(plain.size <= 1_000_000)
            seal(plain, "source-registry-v1")
        } finally { plain.fill(0) }
        val editor = prefs.edit().putString("sources_v2", Base64.encodeToString(sealed, Base64.NO_WRAP)).remove("source")
        // Pre-0.9 progress keys were opaque hashes. There was one source, so their owner is the legacy source.
        if (legacy != null) prefs.all.keys.filter { it.startsWith("p:") }.forEach { key ->
            if (legacy.id in retained) prefs.getString(key, null)?.let { value ->
                val target = progressPrefix(legacy.id) + key.removePrefix("p:")
                if (!prefs.contains(target)) editor.putString(target, value)
            }
            editor.remove(key)
        }
        removed.forEach { id ->
            editor.remove("f:$id").remove("history:$id")
            prefs.all.keys.filter { it.startsWith(progressPrefix(id)) }.forEach { editor.remove(it) }
        }
        if (!editor.commit()) throw SourceFailure("Kaynaklar cihazda saklanamadı. Boş alanı kontrol et.")
    }

    @Synchronized fun forgetSource(id: String) { saveSources(loadSources().without(id)) }

    private fun progressPrefix(source: String) = "p2:${source.length}:$source:"
    private fun progressKey(source: String, id: String) = progressPrefix(source) + stableId("$source:$id")

    fun forgetSource() {
        // Device accessibility and sound preferences survive a source change.
        val removed = prefs.edit().remove("source").remove("sources_v2").apply {
            prefs.all.keys.filter { it.startsWith("p:") || it.startsWith("p2:") || it.startsWith("f:") || it.startsWith("history:") }.forEach { remove(it) }
        }.commit()
        if (!removed) throw SourceFailure("Kaynak cihazdan kaldırılamadı. Tekrar dene.")
    }

    /** Explicit unreadable-record recovery only, after all encrypted catalog files are removed. */
    @Synchronized internal fun resetUnreadableSources() {
        KeyStore.getInstance("AndroidKeyStore").apply { load(null); deleteEntry(alias) }
        forgetSource()
    }

    fun history(source: String): List<WatchRecord> = WatchHistory.decode(prefs.getString("history:$source", null))
    fun history(source: String, records: List<WatchRecord>) {
        prefs.edit().putString("history:$source", WatchHistory.encode(records)).apply()
    }
    fun clearHistory(source: String) {
        val ownsLegacyProgress = runCatching { loadSource()?.id == source }.getOrDefault(false)
        prefs.edit().remove("history:$source").apply {
            prefs.all.keys.filter { it.startsWith(progressPrefix(source)) || (ownsLegacyProgress && it.startsWith("p:")) }.forEach { remove(it) }
        }.apply()
    }

    fun progress(source: String, id: String): PlaybackMark? {
        return try {
            val value = prefs.getString(progressKey(source, id), null)
                ?: if (loadSource()?.id == source) prefs.getString("p:${stableId("$source:$id")}", null) else null
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

    fun favorites(source: String): Set<String> = prefs.getStringSet("f:$source", emptySet()).orEmpty().toSet()
    fun favorites(source: String, ids: Set<String>) { prefs.edit().putStringSet("f:$source", ids.toSet()).apply() }
    var reducedMotion: Boolean
        get() = prefs.getBoolean("reduced", false)
        set(value) { prefs.edit().putBoolean("reduced", value).apply() }
    var startupSound: Boolean
        get() = prefs.getBoolean("startupSound", true)
        set(value) { prefs.edit().putBoolean("startupSound", value).apply() }
    var guideZoneId: String
        get() = prefs.getString("guideZoneId", "").orEmpty().takeIf { it.isEmpty() || runCatching { java.time.ZoneId.of(it) }.isSuccess }.orEmpty()
        set(value) { require(value.isEmpty() || runCatching { java.time.ZoneId.of(value) }.isSuccess); prefs.edit().putString("guideZoneId", value).apply() }
    fun librarySort(kind: MediaKind): LibrarySort =
        prefs.getString("sort:${kind.name}", null)?.let { name -> LibrarySort.entries.firstOrNull { it.name == name } } ?: LibrarySort.DEFAULT
    fun librarySort(kind: MediaKind, sort: LibrarySort) { prefs.edit().putString("sort:${kind.name}", sort.name).apply() }
    /** Hidden categories as [CategoryVisibility.key]s (D068); names only, never addresses or accounts. */
    var hiddenCategories: Set<String>
        get() = prefs.getStringSet("hiddenCategories", emptySet()).orEmpty().toSet()
        set(value) { prefs.edit().putStringSet("hiddenCategories", value.toSet()).apply() }
    var hideAdultCategories: Boolean
        get() = prefs.getBoolean("hideAdultCategories", true)
        set(value) { prefs.edit().putBoolean("hideAdultCategories", value).apply() }
    var autoNextEpisode: Boolean
        get() = prefs.getBoolean("autoNextEpisode", true)
        set(value) { prefs.edit().putBoolean("autoNextEpisode", value).apply() }
    var audioLanguage: String?
        get() = prefs.getString("audioLanguage", null)
        set(value) { prefs.edit().putString("audioLanguage", value).apply() }
    var subtitleLanguage: String?
        get() = prefs.getString("subtitleLanguage", null)
        set(value) { prefs.edit().putString("subtitleLanguage", value).apply() }
    var subtitlesOff: Boolean
        get() = prefs.getBoolean("subtitlesOff", true)
        set(value) { prefs.edit().putBoolean("subtitlesOff", value).apply() }
}
