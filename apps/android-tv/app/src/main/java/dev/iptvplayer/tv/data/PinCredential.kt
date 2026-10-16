package dev.iptvplayer.tv.data

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** Not an access gate. Requires encrypted storage, persisted throttling and off-main execution. */
internal class PinCredential private constructor(private val salt: ByteArray, private val digest: ByteArray) {
    /** Consumes and clears the caller's input even when validation or crypto fails. */
    fun matches(pin: CharArray): Boolean = try {
        if (!valid(pin)) false else {
            val candidate = derive(pin, salt)
            try { MessageDigest.isEqual(digest, candidate) } finally { candidate.fill(0) }
        }
    } finally { pin.fill('\u0000') }

    /** Sensitive verifier bytes: encrypt before writing; never log or export. */
    fun encode(): ByteArray = byteArrayOf(0x50, 0x49, 0x4e, 1) + salt + digest

    override fun toString(): String = "PinCredential([redacted])"

    companion object {
        private const val ITERATIONS = 600_000
        private const val SALT_BYTES = 16
        private const val DIGEST_BYTES = 32
        private const val RECORD_BYTES = 4 + SALT_BYTES + DIGEST_BYTES
        private val random = SecureRandom()

        /** Primitive supports 4–8 ASCII digits. UI length policy is specified separately. */
        private fun valid(pin: CharArray) = pin.size in 4..8 && pin.all { it in '0'..'9' }

        fun create(pin: CharArray): PinCredential = try {
            require(valid(pin)) { "Invalid PIN format" }
            val salt = ByteArray(SALT_BYTES).also(random::nextBytes)
            PinCredential(salt, derive(pin, salt))
        } finally { pin.fill('\u0000') }

        /** Unknown/malformed records are errors, never an unconfigured or unlocked state. */
        fun decode(bytes: ByteArray): PinCredential {
            require(bytes.size == RECORD_BYTES && bytes[0] == 0x50.toByte() &&
                bytes[1] == 0x49.toByte() && bytes[2] == 0x4e.toByte() && bytes[3] == 1.toByte()) {
                "Invalid PIN credential record"
            }
            return PinCredential(bytes.copyOfRange(4, 4 + SALT_BYTES), bytes.copyOfRange(4 + SALT_BYTES, RECORD_BYTES))
        }

        private fun derive(pin: CharArray, salt: ByteArray): ByteArray {
            val spec = PBEKeySpec(pin, salt, ITERATIONS, DIGEST_BYTES * 8)
            return try { SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded }
            finally { spec.clearPassword() }
        }
    }
}
