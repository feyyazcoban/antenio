package dev.iptvplayer.tv.data

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream

/**
 * What a set PIN guards besides locked titles (pin.md §5). [sourcesNeedPin]: adding, editing and removing sources asks
 * for it. [launchNeedsPin]: the app opens on a lock screen.
 */
internal data class ProtectionOptions(val sourcesNeedPin: Boolean = true, val launchNeedsPin: Boolean = false)

internal class ProtectionSnapshot(val generation: Long, val policyRevision: Long,
    val credential: PinCredential, val attempts: PinAttempts, rules: List<AccessRule>,
    val options: ProtectionOptions = ProtectionOptions()) {
    val rules: List<AccessRule> = java.util.Collections.unmodifiableList(rules.toList())
    init {
        require(generation >= 0 && policyRevision >= 0 && rules.size <= 1024)
        require(rules.map { it.scope }.distinct().size == rules.size)
    }
    override fun toString() = "ProtectionSnapshot([redacted])"
}

/** v2 adds one byte of [ProtectionOptions] after the rules; a v1 record reads with the default options. */
internal object ProtectionCodec {
    const val MAX_BYTES = 1_048_576
    private const val V1 = 0x50525601
    private const val V2 = 0x50525602
    private const val SOURCES_NEED_PIN = 1
    private const val LAUNCH_NEEDS_PIN = 2

    fun encode(value: ProtectionSnapshot): ByteArray {
        val buffer = ByteArrayOutputStream()
        DataOutputStream(buffer).use { out ->
            out.writeInt(V2)
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
            with(value.options) {
                out.writeByte((if (sourcesNeedPin) SOURCES_NEED_PIN else 0) or (if (launchNeedsPin) LAUNCH_NEEDS_PIN else 0))
            }
            require(buffer.size() <= MAX_BYTES)
        }
        return buffer.toByteArray()
    }

    fun decode(bytes: ByteArray): ProtectionSnapshot {
        require(bytes.size <= MAX_BYTES)
        return DataInputStream(ByteArrayInputStream(bytes)).use { input ->
            val version = input.readInt()
            require(version == V1 || version == V2)
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
            val options = if (version == V1) ProtectionOptions() else {
                // An option this build does not know could be a protection it would silently drop.
                val bits = input.readUnsignedByte()
                require(bits and (SOURCES_NEED_PIN or LAUNCH_NEEDS_PIN).inv() == 0)
                ProtectionOptions(bits and SOURCES_NEED_PIN != 0, bits and LAUNCH_NEEDS_PIN != 0)
            }
            require(input.read() == -1)
            ProtectionSnapshot(generation, revision, credential, attempts, rules, options)
        }
    }
}
