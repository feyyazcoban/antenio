package dev.iptvplayer.tv

import android.content.Context
import android.content.ContextWrapper
import androidx.test.platform.app.InstrumentationRegistry
import dev.iptvplayer.tv.data.*
import java.io.File
import java.security.KeyStore
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import org.junit.*
import org.junit.Assert.*

/** Isolated namespaces only; never touches source credentials or the primary protection store. */
class ProtectionStoreTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val stores = mutableListOf<ProtectionStore>()
    private fun fresh(within: Context = context) =
        ProtectionStore(within, "test_" + UUID.randomUUID().toString().replace("-", "")).also(stores::add)
    private fun snapshot(generation: Long = 0, revision: Long = 0, credential: PinCredential =
        PinCredential.create(CharArray(6) { ('0'.code + it).toChar() })) = ProtectionSnapshot(generation, revision,
        credential, PinAttempts.clear(PinClock(1, 1000)), listOf(AccessRule(AccessScope.Source("synthetic-source"), pinRequired = true)))
    @After fun cleanup() {
        val keys = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        stores.forEach { store ->
            listOf(store.file, File(store.file.path + ".bak"), File(store.file.path + ".new")).forEach { it.deleteRecursively() }
            if (keys.containsAlias(store.alias)) keys.deleteEntry(store.alias)
        }
    }
    @Test fun encryptedRoundTripNewInstanceAndStaleWriterRejection() {
        val store = fresh(); val initial = snapshot()
        assertEquals(ProtectionRead.Unconfigured, store.read())
        store.write(initial, null)
        val bytes = store.file.readBytes()
        assertFalse(bytes.contentEquals(ProtectionCodec.encode(initial)))
        val namespace = store.file.name.removeSuffix(".bin")
        val reopened = ProtectionStore(context, namespace)
        val restored = (reopened.read() as ProtectionRead.Ready).snapshot
        assertEquals(initial.rules, restored.rules)
        assertTrue(restored.credential.matches(CharArray(6) { ('0'.code + it).toChar() }))
        val next = snapshot(1, credential = initial.credential)
        reopened.write(next, 0)
        assertThrows(IllegalStateException::class.java) { store.write(next, 0) }
        assertEquals(1L, (store.read() as ProtectionRead.Ready).snapshot.generation)
    }
    @Test fun missingFileAndMissingKeyNeverReinitialize() {
        val store = fresh(); val initial = snapshot(); store.write(initial, null)
        assertTrue(store.file.delete())
        assertEquals(ProtectionRead.Unavailable, store.read())
        assertThrows(IllegalStateException::class.java) { store.write(initial, null) }
        val other = fresh(); other.write(initial, null)
        KeyStore.getInstance("AndroidKeyStore").apply { load(null); deleteEntry(other.alias) }
        assertEquals(ProtectionRead.Unavailable, other.read())
        assertThrows(IllegalStateException::class.java) { other.write(initial, null) }
    }
    @Test fun tamperingAndCrossStoreCiphertextAreRejected() {
        val a = fresh(); val b = fresh(); val initial = snapshot()
        a.write(initial, null); b.write(initial, null)
        b.file.writeBytes(a.file.readBytes())
        assertEquals(ProtectionRead.Unavailable, b.read())
        val bytes = a.file.readBytes(); bytes[bytes.lastIndex] = (bytes.last().toInt() xor 1).toByte(); a.file.writeBytes(bytes)
        assertEquals(ProtectionRead.Unavailable, a.read())
    }
    @Test fun failedWritePreservesPreviousStateAndChangedPolicyNeedsRevision() {
        val store = fresh(); val initial = snapshot(); store.write(initial, null)
        val changed = ProtectionSnapshot(1, 0, initial.credential, initial.attempts, emptyList())
        assertThrows(IllegalStateException::class.java) { store.write(changed, 0) }
        val blocker = File(store.file.path + ".new").apply { mkdirs() }
        File(blocker, "blocked").writeText("test")
        assertThrows(IllegalStateException::class.java) { store.write(snapshot(1, credential = initial.credential), 0) }
        assertEquals(0L, (store.read() as ProtectionRead.Ready).snapshot.generation)
        assertTrue(blocker.deleteRecursively())
        store.write(ProtectionSnapshot(1, 1, initial.credential, initial.attempts, emptyList()), 0)
        assertEquals(1L, (store.read() as ProtectionRead.Ready).snapshot.policyRevision)
    }
    @Test fun changedOptionsNeedANewRevision() {
        val store = fresh(); val initial = snapshot(); store.write(initial, null)
        val sourcesFree = ProtectionOptions(sourcesNeedPin = false)
        assertThrows(IllegalStateException::class.java) {
            store.write(ProtectionSnapshot(1, 0, initial.credential, initial.attempts, initial.rules, sourcesFree), 0)
        }
        store.write(ProtectionSnapshot(1, 1, initial.credential, initial.attempts, initial.rules, sourcesFree), 0)
        assertEquals(sourcesFree, (store.read() as ProtectionRead.Ready).snapshot.options)
    }
    @Test fun failedFirstWriteLeavesNoKeySoItCanBeRetried() {
        // Its own files folder, so making it read-only blocks no other store.
        val root = File(context.cacheDir, "protection_test_" + UUID.randomUUID().toString().replace("-", ""))
        val folder = File(root, "protection").apply { check(mkdirs()) }
        val store = fresh(object : ContextWrapper(context) { override fun getFilesDir() = root })
        val initial = snapshot()
        try {
            // The key is made before the file; a folder that takes no file fails the write after it.
            check(folder.setWritable(false, false))
            assertThrows(IllegalStateException::class.java) { store.write(initial, null) }
            assertFalse(KeyStore.getInstance("AndroidKeyStore").apply { load(null) }.containsAlias(store.alias))
            assertEquals(ProtectionRead.Unconfigured, store.read())
            check(folder.setWritable(true, false))
            store.write(initial, null)
            assertTrue(store.read() is ProtectionRead.Ready)
        } finally {
            folder.setWritable(true, false)
            root.deleteRecursively()
        }
    }
    @Test fun resetRemovesRecordAndKeyAndFinishesAHalfDoneReset() {
        val store = fresh(); val initial = snapshot(); store.write(initial, null)
        store.reset()
        assertEquals(ProtectionRead.Unconfigured, store.read())
        assertFalse(store.file.exists())
        assertFalse(KeyStore.getInstance("AndroidKeyStore").apply { load(null) }.containsAlias(store.alias))
        store.reset()
        store.write(initial, null)
        assertTrue(store.read() is ProtectionRead.Ready)
        // A reset stopped after the files leaves the key: Unavailable, and the next reset ends it.
        assertTrue(store.file.delete())
        assertEquals(ProtectionRead.Unavailable, store.read())
        store.reset()
        assertEquals(ProtectionRead.Unconfigured, store.read())
    }
    @Test fun v1RecordReadsWithDefaultOptionsAndTheNextWriteIsV2() {
        val store = fresh(); val initial = snapshot(); store.write(initial, null)
        val aad = "iptv-protection-v1:${store.file.name.removeSuffix(".bin")}".toByteArray(Charsets.UTF_8)
        val key = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }.getKey(store.alias, null) as SecretKey
        // v1 is v2 without the options byte at the end.
        val v2 = ProtectionCodec.encode(initial)
        val v1 = v2.copyOf(v2.size - 1).also { it[3] = 1 }
        val sealing = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.ENCRYPT_MODE, key); updateAAD(aad) }
        store.file.writeBytes(sealing.iv + sealing.doFinal(v1))
        val read = (store.read() as ProtectionRead.Ready).snapshot
        assertEquals(ProtectionOptions(), read.options); assertEquals(initial.rules, read.rules)
        store.write(ProtectionSnapshot(1, 0, read.credential, read.attempts, read.rules, read.options), 0)
        val bytes = store.file.readBytes()
        val plain = Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, bytes.copyOfRange(0, 12))); updateAAD(aad)
        }.doFinal(bytes, 12, bytes.size - 12)
        assertArrayEquals(byteArrayOf(0x50, 0x52, 0x56, 0x02), plain.copyOf(4))
    }
}
