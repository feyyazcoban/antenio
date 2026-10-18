package dev.iptvplayer.tv.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.AtomicFile
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

internal sealed interface ProtectionRead {
    data object Unconfigured : ProtectionRead
    data class Ready(val snapshot: ProtectionSnapshot) : ProtectionRead
    data object Unavailable : ProtectionRead
}

/** What ParentalGuard needs of the record, so its JVM tests can stand in a fake. */
internal interface ProtectionStorage {
    fun read(): ProtectionRead
    fun write(snapshot: ProtectionSnapshot, expectedGeneration: Long?)
    fun reset()
}

/**
 * Off-main, single-process storage primitive, not an authentication boundary. [reset] has two callers only: removing
 * the PIN after it was verified, and "PIN'i unuttum" after the sources are wiped (pin.md §3.10, §3.11).
 */
internal class ProtectionStore(context: Context, namespace: String = "primary") : ProtectionStorage {
    init { require(namespace.matches(Regex("[a-z0-9_-]{1,48}"))) }
    internal val file = File(context.filesDir, "protection/$namespace.bin")
    internal val alias = "iptv_protection_v1_$namespace"
    private val aad = "iptv-protection-v1:$namespace".toByteArray(Charsets.UTF_8)
    private val atomic = AtomicFile(file)

    override fun read(): ProtectionRead = synchronized(lock) {
        try { readLocked() } catch (_: Exception) { ProtectionRead.Unavailable }
    }

    private fun keyStore() = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    private fun files() = listOf(file, File(file.path + ".bak"), File(file.path + ".new"))
    private fun hasFile() = files().any { it.exists() }
    private fun readLocked(): ProtectionRead {
        val store = keyStore()
        if (!store.containsAlias(alias)) return if (hasFile()) ProtectionRead.Unavailable else ProtectionRead.Unconfigured
        // Existing key is also the initialized marker; missing file must not disable protection.
        if (!hasFile()) return ProtectionRead.Unavailable
        val key = store.getKey(alias, null) as? SecretKey ?: return ProtectionRead.Unavailable
        val encrypted = readBytes()
        require(encrypted.size >= 29)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, encrypted.copyOfRange(0, 12)))
            updateAAD(aad)
        }
        val plain = cipher.doFinal(encrypted, 12, encrypted.size - 12)
        return try { ProtectionRead.Ready(ProtectionCodec.decode(plain)) } finally { plain.fill(0) }
    }

    private fun readBytes(): ByteArray = atomic.openRead().use { input ->
        val output = java.io.ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            require(output.size() + count <= ProtectionCodec.MAX_BYTES + 28)
            output.write(buffer, 0, count)
        }
        output.toByteArray()
    }

    /** null expected generation means first setup. Caller must authenticate all later policy edits. */
    override fun write(snapshot: ProtectionSnapshot, expectedGeneration: Long?) = synchronized(lock) {
        val plain = ProtectionCodec.encode(snapshot)
        try {
            val current = readLocked()
            when (current) {
                ProtectionRead.Unconfigured -> check(expectedGeneration == null && snapshot.generation == 0L)
                is ProtectionRead.Ready -> {
                    val previous = current.snapshot
                    check(expectedGeneration == previous.generation && previous.generation < Long.MAX_VALUE &&
                        snapshot.generation == previous.generation + 1 && snapshot.policyRevision >= previous.policyRevision)
                    val fresh = snapshot.credential.encode(); val old = previous.credential.encode()
                    val changed = try {
                        snapshot.rules != previous.rules || snapshot.options != previous.options || !fresh.contentEquals(old)
                    } finally { fresh.fill(0); old.fill(0) }
                    check(!changed || snapshot.policyRevision > previous.policyRevision)
                }
                ProtectionRead.Unavailable -> error("Protection unavailable")
            }
            check(file.parentFile!!.isDirectory || file.parentFile!!.mkdirs())
            val key = if (current is ProtectionRead.Ready) keyStore().getKey(alias, null) as SecretKey
            else KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
                init(KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setRandomizedEncryptionRequired(true).build())
            }.generateKey()
            val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.ENCRYPT_MODE, key); updateAAD(aad) }
            check(cipher.iv.size == 12)
            val encrypted = cipher.iv + cipher.doFinal(plain)
            var stream: java.io.FileOutputStream? = null
            try {
                stream = atomic.startWrite()
                stream.write(encrypted); stream.fd.sync()
                atomic.finishWrite(stream); stream = null
                check(readBytes().contentEquals(encrypted))
            } catch (failure: Exception) {
                stream?.let { atomic.failWrite(it) }
                throw failure
            }
        } catch (_: Exception) {
            throw IllegalStateException("Protection record could not be saved")
        } finally { plain.fill(0) }
    }

    /**
     * Back to Unconfigured: the files go first, then the key. Stopped halfway, the key without a file reads as
     * Unavailable, never as a cleared PIN, and the same call finishes the job.
     */
    override fun reset() {
        synchronized(lock) {
            try {
                files().forEach { check(it.deleteRecursively()) }
                val store = keyStore()
                if (store.containsAlias(alias)) store.deleteEntry(alias)
            } catch (_: Exception) {
                throw IllegalStateException("Protection record could not be reset")
            }
        }
    }

    private companion object { val lock = Any() }
}
