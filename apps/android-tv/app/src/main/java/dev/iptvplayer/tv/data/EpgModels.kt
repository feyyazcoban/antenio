package dev.iptvplayer.tv.data

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.text.Normalizer
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle
import java.util.Locale

data class GuideWindow(val from: Long, val until: Long) {
    init { require(from < until) }
    companion object {
        /** Yesterday, today and the next seven calendar days, including DST transitions. */
        fun around(now: Instant, zone: ZoneId = ZoneId.systemDefault()): GuideWindow {
            val today = now.atZone(zone).toLocalDate()
            return GuideWindow(today.minusDays(1).atStartOfDay(zone).toInstant().toEpochMilli(),
                today.plusDays(8).atStartOfDay(zone).toInstant().toEpochMilli())
        }
    }
}

data class EpgChannel(val id: String, val names: List<String>)
data class EpgProgramme(val channelId: String, val title: String, val start: Long, val end: Long?,
    val description: String = "", val category: String = "") {
    fun isOnAt(time: Long) = end != null && time >= start && time < end
}
data class EpgParseReport(val channels: Int, val programmes: Int, val invalid: Int, val outsideWindow: Int)

interface EpgSink {
    fun channel(value: EpgChannel)
    fun programme(value: EpgProgramme)
}

/** No provider-local clock guessing: only unambiguous offsets; missing zone means UTC in XMLTV. */
object XmltvTime {
    private val format = DateTimeFormatter.ofPattern("uuuuMMddHHmmss", Locale.ROOT).withResolverStyle(ResolverStyle.STRICT)
    private val value = Regex("^(\\d{12}|\\d{14})(?:\\s+([+-]\\d{4}|UTC|GMT|Z|BST))?$")
    fun parse(text: String?): Long? {
        val match = value.matchEntire(text?.trim().orEmpty()) ?: return null
        return try {
            val digits = match.groupValues[1].let { if (it.length == 12) it + "00" else it }
            val zone = when (val token = match.groupValues[2]) {
                "", "UTC", "GMT", "Z" -> ZoneOffset.UTC
                "BST" -> ZoneOffset.ofHours(1)
                else -> ZoneOffset.of(token)
            }
            LocalDateTime.parse(digits, format).toInstant(zone).toEpochMilli()
        } catch (_: DateTimeException) { null }
    }
}

internal fun epgNameKey(name: String): String = Normalizer.normalize(name.trim(), Normalizer.Form.NFC)
    .replace(Regex("\\s+"), " ").lowercase(Locale.ROOT)

data class EpgChannelRef(val feed: Int, val id: String)
enum class EpgMatchKind { ID, NAME, MISSING_ID, AMBIGUOUS, NONE }
data class EpgMatch(val kind: EpgMatchKind, val channel: EpgChannelRef? = null)
data class GuideSlice(val match: EpgMatch, val programmes: List<EpgProgramme>, val truncated: Boolean = false) {
    // Overlapping listings are uncertain. Do not label either programme as current.
    fun now(time: Long): EpgProgramme? = programmes.filter { it.isOnAt(time) }.singleOrNull()
    fun next(time: Long): EpgProgramme? {
        val first = programmes.firstOrNull { it.start > time } ?: return null
        return first.takeIf { programmes.count { item -> item.start == first.start } == 1 }
    }
}

/** A transient request plan; strings may contain credentials and must never be logged. */
class EpgPlan private constructor(val owner: String, val binding: String, internal val urls: List<String>) {
    override fun toString() = "EpgPlan(feeds=${urls.size}, addresses=REDACTED)"
    companion object {
        fun create(source: SourceConfig, catalog: Catalog, overrideUrl: String? = source.epgUrl): EpgPlan? {
            val raw = when {
                !overrideUrl.isNullOrBlank() -> listOf(overrideUrl.trim())
                source.type == SourceType.XTREAM -> listOf(source.xmltvUrl().toString())
                else -> catalog.epgUrls
            }
            if (raw.isEmpty()) return null
            if (raw.size > 4) throw SourceFailure(FailureReason.GUIDE_TOO_MANY_FEEDS)
            val urls = raw.map { address ->
                address.toHttpUrlOrNull()?.takeIf { it.username.isEmpty() && it.password.isEmpty() }?.toString()
                    ?: throw SourceFailure(FailureReason.GUIDE_FEED_INVALID)
            }.distinct()
            val binding = stableId((listOf(source.id, source.type.name, source.address, source.username, source.password) + urls)
                .joinToString("\u0000"))
            return EpgPlan(stableId(source.id), binding, urls)
        }
    }
}
