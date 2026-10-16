package dev.iptvplayer.tv.data

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream

internal class ProtectionSnapshot(val generation: Long, val policyRevision: Long,
    val credential: PinCredential, val attempts: PinAttempts, rules: List<AccessRule>) {
    val rules: List<AccessRule> = java.util.Collections.unmodifiableList(rules.toList())
    init {
        require(generation >= 0 && policyRevision >= 0 && rules.size <= 1024)
        require(rules.map { it.scope }.distinct().size == rules.size)
    }
    override fun toString() = "ProtectionSnapshot([redacted])"
}

internal object ProtectionCodec {
    const val MAX_BYTES = 1_048_576
    fun encode(value: ProtectionSnapshot): ByteArray {
        val buffer = ByteArrayOutputStream()
        DataOutputStream(buffer).use { out ->
            out.writeInt(0x50525601)
            out.writeLong(value.generation); out.writeLong(value.policyRevision)
            val credential = value.credential.encode()
            try { out.write(credential) } finally { credential.fill(0) }
            with(value.attempts) { out.writeInt(charged); out.writeLong(boot); out.writeLong(lastObservedMs); out.writeLong(availableAtMs) }
            out.writeInt(value.rules.size)
            fun text(s: String) { require(s.isNotBlank() && s.length <= 4096); out.writeUTF(s) }
            value.rules.forEach { rule ->
                when (val scope = rule.scope) {
                    is AccessScope.Source -> { out.writeByte(0); text(scope.sourceId) }
                    is AccessScope.Content -> { out.writeByte(1); text(scope.sourceId); text(scope.ref.localId) }
                    is AccessScope.Category -> {
                        out.writeByte(2); text(scope.sourceId); text(scope.kind.name)
                        when (val key = scope.key) {
                            is AccessCategory.ProviderId -> { out.writeByte(0); text(key.value) }
                            is AccessCategory.M3uName -> { out.writeByte(1); text(key.value) }
                        }
                    }
                }
                out.writeBoolean(rule.hidden); out.writeBoolean(rule.pinRequired)
                require(buffer.size() <= MAX_BYTES)
            }
        }
        return buffer.toByteArray()
    }

    fun decode(bytes: ByteArray): ProtectionSnapshot {
        require(bytes.size <= MAX_BYTES)
        return DataInputStream(ByteArrayInputStream(bytes)).use { input ->
            require(input.readInt() == 0x50525601)
            val generation = input.readLong(); val revision = input.readLong()
            val raw = ByteArray(52); input.readFully(raw)
            val credential = try { PinCredential.decode(raw) } finally { raw.fill(0) }
            val attempts = PinAttempts(input.readInt(), input.readLong(), input.readLong(), input.readLong())
            val count = input.readInt(); require(count in 0..1024)
            fun text(): String = input.readUTF().also { require(it.isNotBlank() && it.length <= 4096) }
            fun flag(): Boolean = input.readUnsignedByte().also { require(it in 0..1) } == 1
            val rules = List(count) {
                val scope = when (input.readUnsignedByte()) {
                    0 -> AccessScope.Source(text())
                    1 -> AccessScope.Content(ContentRef(text(), text()))
                    2 -> {
                        val source = text(); val kind = MediaKind.valueOf(text())
                        val category = when (input.readUnsignedByte()) {
                            0 -> AccessCategory.ProviderId(text())
                            1 -> AccessCategory.M3uName(text())
                            else -> error("Invalid category kind")
                        }
                        AccessScope.Category(source, kind, category)
                    }
                    else -> error("Invalid rule scope")
                }
                AccessRule(scope, flag(), flag())
            }
            require(input.read() == -1)
            ProtectionSnapshot(generation, revision, credential, attempts, rules)
        }
    }
}
