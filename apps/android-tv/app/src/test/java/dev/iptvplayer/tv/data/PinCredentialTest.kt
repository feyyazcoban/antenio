package dev.iptvplayer.tv.data

import org.junit.Assert.*
import org.junit.Test

class PinCredentialTest {
    private fun example() = CharArray(6) { ('0'.code + it).toChar() }

    @Test fun independentPythonVectorAndCallerInputErasure() {
        // Python hashlib.pbkdf2_hmac SHA256, 600000 iterations, bytes(48..53), salt bytes(0..15).
        val expected = "51394b56fe604a83204431d587a8c2d99c5cd735ff85957b79d7fbebfb585743"
            .chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        val record = byteArrayOf(0x50, 0x49, 0x4e, 1) + ByteArray(16) { it.toByte() } + expected
        val credential = PinCredential.decode(record)
        val input = example()
        assertTrue(credential.matches(input))
        assertTrue(input.all { it == '\u0000' })
        val wrong = example().also { it[5] = '9' }
        assertFalse(credential.matches(wrong))
        assertTrue(wrong.all { it == '\u0000' })
    }

    @Test fun randomSaltRoundtripAndDefensiveCopies() {
        val input = example()
        val first = PinCredential.create(input)
        assertTrue(input.all { it == '\u0000' })
        val second = PinCredential.create(example())
        assertFalse(first.encode().copyOfRange(4, 20).contentEquals(second.encode().copyOfRange(4, 20)))
        val encoded = first.encode()
        val restored = PinCredential.decode(encoded)
        encoded.fill(0)
        first.encode().fill(0)
        assertTrue(first.matches(example()))
        assertTrue(restored.matches(example()))
        assertEquals("PinCredential([redacted])", first.toString())
    }

    @Test fun malformedRecordsNeverBecomeAnUnlockedCredential() {
        val validShape = byteArrayOf(0x50, 0x49, 0x4e, 1) + ByteArray(48)
        for (bytes in listOf(ByteArray(0), validShape.copyOf(51), validShape + 0,
            validShape.copyOf().also { it[3] = 2 }, validShape.copyOf().also { it[0] = 0 })) {
            assertThrows(IllegalArgumentException::class.java) { PinCredential.decode(bytes) }
        }
        // Structurally valid but bogus verifier does not authenticate.
        assertFalse(PinCredential.decode(validShape).matches(example()))
    }

    @Test fun invalidInputsAreRejectedAndErasedWithoutNormalization() {
        val credential = PinCredential.decode(byteArrayOf(0x50, 0x49, 0x4e, 1) + ByteArray(48))
        val invalid = listOf(CharArray(3) { '1' }, CharArray(9) { '1' }, charArrayOf(' ', '1', '2', '3'),
            charArrayOf('\u0661', '\u0662', '\u0663', '\u0664'), charArrayOf('1', '2', '\u0000', '4'))
        for (input in invalid) {
            val creationInput = input.copyOf()
            assertThrows(IllegalArgumentException::class.java) { PinCredential.create(creationInput) }
            assertTrue(creationInput.all { it == '\u0000' })
            assertFalse(credential.matches(input))
            assertTrue(input.all { it == '\u0000' })
        }
    }
}
