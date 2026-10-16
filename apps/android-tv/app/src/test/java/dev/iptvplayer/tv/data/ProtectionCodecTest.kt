package dev.iptvplayer.tv.data

import org.junit.Assert.*
import org.junit.Test

class ProtectionCodecTest {
    private val credential get() = PinCredential.decode(byteArrayOf(0x50, 0x49, 0x4e, 1) + ByteArray(48))
    private fun record(rules: List<AccessRule>) = ProtectionSnapshot(7, 3, credential,
        PinAttempts(5, 4, 1000, 31000), rules)
    private val rules = listOf(
        AccessRule(AccessScope.Source("source-a"), pinRequired = true),
        AccessRule(AccessScope.Content(ContentRef("source-a", "movie:1")), hidden = true),
        AccessRule(AccessScope.Category("source-b", MediaKind.MOVIE, AccessCategory.ProviderId("8")), pinRequired = true),
        AccessRule(AccessScope.Category("source-c", MediaKind.SERIES, AccessCategory.M3uName("Türkçe diziler")), hidden = true))

    @Test fun allScopesCredentialAndAttemptsRoundTripTogether() {
        val original = record(rules)
        val decoded = ProtectionCodec.decode(ProtectionCodec.encode(original))
        assertEquals(7L, decoded.generation); assertEquals(3L, decoded.policyRevision)
        assertEquals(original.attempts, decoded.attempts); assertEquals(rules, decoded.rules)
        assertArrayEquals(original.credential.encode(), decoded.credential.encode())
    }
    @Test fun truncationTrailingBytesAndUnknownVersionFail() {
        val bytes = ProtectionCodec.encode(record(rules))
        for (length in bytes.indices) assertThrows(Exception::class.java) { ProtectionCodec.decode(bytes.copyOf(length)) }
        assertThrows(Exception::class.java) { ProtectionCodec.decode(bytes + 0) }
        assertThrows(Exception::class.java) { ProtectionCodec.decode(bytes.copyOf().also { it[3] = 2 }) }
        assertThrows(Exception::class.java) { ProtectionCodec.decode(bytes.copyOf().also { it[it.lastIndex] = 2 }) }
    }
    @Test fun inputLimitsAndDuplicateScopesAreRejected() {
        assertThrows(Exception::class.java) { ProtectionCodec.decode(ByteArray(ProtectionCodec.MAX_BYTES + 1)) }
        assertThrows(Exception::class.java) { record(listOf(rules.first(), rules.first())) }
        assertThrows(Exception::class.java) { ProtectionCodec.encode(record(listOf(AccessRule(AccessScope.Source("x".repeat(4097)))))) }
    }
    @Test fun rulesAreDefensivelyCopiedAndNotMutable() {
        val list = rules.toMutableList(); val snapshot = record(list); list.clear()
        assertEquals(4, snapshot.rules.size)
        assertThrows(UnsupportedOperationException::class.java) { (snapshot.rules as MutableList).clear() }
        assertEquals("ProtectionSnapshot([redacted])", snapshot.toString())
    }
}
