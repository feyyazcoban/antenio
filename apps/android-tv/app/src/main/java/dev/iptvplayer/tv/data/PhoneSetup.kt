package dev.iptvplayer.tv.data

import java.net.URLDecoder
import java.security.SecureRandom
import java.util.Random

/**
 * What the phone page sends to the TV (SETUP_FLOW, "Telefonla ekleme"). The TV still asks before it adds anything, and
 * only the TV connects to the provider. Nothing here is logged or kept after the setup screen closes.
 */
data class PhoneSubmission(val type: SourceType, val name: String, val address: String, val username: String,
    val password: String, val epgUrl: String) {
    override fun toString() = "PhoneSubmission(type=$type, credentials=REDACTED)"
}

/** The phone page's form body: application/x-www-form-urlencoded, at most [MAX_BODY] bytes. */
object PhoneSetupForm {
    const val MAX_BODY = 16 * 1024

    fun parse(body: String): Map<String, String> = body.split('&').filter { it.isNotEmpty() }.associate { pair ->
        val key = pair.substringBefore('=')
        val value = if ('=' in pair) pair.substringAfter('=') else ""
        decode(key) to decode(value)
    }

    /** The source the page describes, or null when the type is unknown or the address is missing. */
    fun submission(fields: Map<String, String>): PhoneSubmission? {
        val type = when (fields["type"]) { "m3u" -> SourceType.M3U; "xtream" -> SourceType.XTREAM; else -> return null }
        val address = fields["address"].orEmpty().trim()
        if (address.isEmpty()) return null
        return PhoneSubmission(type, fields["name"].orEmpty().trim(), address,
            if (type == SourceType.XTREAM) fields["username"].orEmpty().trim() else "",
            if (type == SourceType.XTREAM) fields["password"].orEmpty() else "",
            fields["epg"].orEmpty().trim())
    }

    // A malformed escape keeps the raw text rather than dropping the field.
    private fun decode(text: String) = runCatching { URLDecoder.decode(text, Charsets.UTF_8.name()) }.getOrDefault(text)
}

/**
 * One pairing: a six-digit [code] shown on the TV and typed (or carried in the QR) on the phone. It lasts five minutes,
 * accepts one submission and locks after five wrong codes, so a neighbour on the same network cannot guess it.
 */
class PhoneSetupSession(val code: String, private val startedAt: Long, private val lifetimeMs: Long = LIFETIME_MS,
    private val maxTries: Int = MAX_TRIES) {
    enum class Check { ACCEPTED, WRONG_CODE, EXPIRED, LOCKED, USED }

    private var tries = 0
    private var used = false

    fun remainingMs(now: Long) = (startedAt + lifetimeMs - now).coerceAtLeast(0)
    fun expired(now: Long) = remainingMs(now) == 0L

    @Synchronized fun check(candidate: String, now: Long): Check = when {
        used -> Check.USED
        expired(now) -> Check.EXPIRED
        tries >= maxTries -> Check.LOCKED
        candidate.filter(Char::isDigit) != code -> { tries++; if (tries >= maxTries) Check.LOCKED else Check.WRONG_CODE }
        else -> { used = true; Check.ACCEPTED }
    }

    companion object {
        const val LIFETIME_MS = 5 * 60_000L
        const val MAX_TRIES = 5
        fun newCode(random: Random = SecureRandom()) = "%06d".format(random.nextInt(1_000_000))
    }
}
