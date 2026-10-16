package dev.iptvplayer.tv

import androidx.test.platform.app.InstrumentationRegistry
import dev.iptvplayer.tv.data.*
import java.io.File
import java.security.KeyStore
import java.util.UUID
import org.junit.*
import org.junit.Assert.*

/** Isolated namespaces only; never touches source credentials or the primary protection store. */
class ProtectionStoreTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val stores = mutableListOf<ProtectionStore>()
    private fun fresh() = ProtectionStore(context, "test_" + UUID.randomUUID().toString().replace("-", "")).also(stores::add)
    private fun snapshot(generation: Long = 0, revision: Long = 0, credential: PinCredential =
        PinCredential.create(CharArray(6) { ('0'.code + it).toChar() })) = ProtectionSnapshot(generation, revision,
        credential, PinAttempts.clear(PinClock(1, 1000)), listOf(AccessRule(AccessScope.Source("synthetic-source"), pinRequired = true)))
    @After fun cleanup() {
        val keys = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        stores.forEach { store ->
            listOf(store.file, File(store.file.path + ".bak"), File(store.file.path + ".new")).forEach { it.deleteRecursively() }
            keys.deleteEntry(store.alias)
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
}
