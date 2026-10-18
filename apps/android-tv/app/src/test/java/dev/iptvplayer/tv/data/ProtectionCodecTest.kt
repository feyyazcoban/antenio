package dev.iptvplayer.tv.data

import org.junit.Assert.*
import org.junit.Test

class ProtectionCodecTest {
    private val credential get() = PinCredential.decode(byteArrayOf(0x50, 0x49, 0x4e, 1) + ByteArray(48))
    private fun record(rules: List<AccessRule>, options: ProtectionOptions = ProtectionOptions()) =
        ProtectionSnapshot(7, 3, credential, PinAttempts(5, 4, 1000, 31000), rules, options)
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
        assertThrows(Exception::class.java) { ProtectionCodec.decode(bytes.copyOf().also { it[3] = 3 }) }
        // The last rule's pinRequired flag, just before the options byte.
        assertThrows(Exception::class.java) { ProtectionCodec.decode(bytes.copyOf().also { it[it.lastIndex - 1] = 2 }) }
    }
    @Test fun optionsRoundTripAndEncodeAlwaysWritesV2() {
        for (sources in listOf(true, false)) for (launch in listOf(true, false)) {
            val options = ProtectionOptions(sourcesNeedPin = sources, launchNeedsPin = launch)
            val bytes = ProtectionCodec.encode(record(rules, options))
            assertArrayEquals(byteArrayOf(0x50, 0x52, 0x56, 0x02), bytes.copyOf(4))
            val decoded = ProtectionCodec.decode(bytes)
            assertEquals(options, decoded.options); assertEquals(rules, decoded.rules)
        }
        assertEquals(ProtectionOptions(sourcesNeedPin = true, launchNeedsPin = false), ProtectionOptions())
    }
    @Test fun v1RecordReadsWithDefaultOptions() {
        // v1 is v2 without the options byte at the end.
        val v2 = ProtectionCodec.encode(record(rules, ProtectionOptions(sourcesNeedPin = false, launchNeedsPin = true)))
        val v1 = v2.copyOf(v2.size - 1).also { it[3] = 1 }
        val decoded = ProtectionCodec.decode(v1)
        assertEquals(ProtectionOptions(), decoded.options)
        assertEquals(7L, decoded.generation); assertEquals(rules, decoded.rules)
        assertEquals(PinAttempts(5, 4, 1000, 31000), decoded.attempts)
        // A v1 record never carries an options byte, and v2 always does.
        assertThrows(Exception::class.java) { ProtectionCodec.decode(v2.copyOf().also { it[3] = 1 }) }
        assertThrows(Exception::class.java) { ProtectionCodec.decode(v1.copyOf().also { it[3] = 2 }) }
    }
    @Test fun unknownOptionBitsAreRejected() {
        val bytes = ProtectionCodec.encode(record(rules))
        for (bits in listOf(4, 8, 0x80, 0xff)) {
            assertThrows(Exception::class.java) { ProtectionCodec.decode(bytes.copyOf().also { it[it.lastIndex] = bits.toByte() }) }
        }
        assertEquals(ProtectionOptions(false, true), ProtectionCodec.decode(bytes.copyOf().also { it[it.lastIndex] = 2 }).options)
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
