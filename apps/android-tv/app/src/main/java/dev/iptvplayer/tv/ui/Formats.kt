package dev.iptvplayer.tv.ui

import dev.iptvplayer.tv.data.MediaEntry
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * The locale of every date, time, duration, number, language name and title order the app shows: the app's language,
 * never the TV's, so a date does not read in another language than the text around it and digits stay in one script.
 * The app speaks Turkish alone until the language setting arrives (S13), which decides it here; the date patterns
 * below follow Turkish order.
 */
internal val appLocale: Locale = Locale.forLanguageTag("tr")

private fun format(time: Long, zone: ZoneId, pattern: String, locale: Locale): String =
    Instant.ofEpochMilli(time).atZone(zone).format(DateTimeFormatter.ofPattern(pattern, locale))

/** "20:30" */
internal fun guideTime(time: Long, zone: ZoneId, locale: Locale = appLocale): String = format(time, zone, "HH:mm", locale)

/** "18 Eki Pazar": a guide day with its weekday. */
internal fun guideDate(time: Long, zone: ZoneId, locale: Locale = appLocale): String = format(time, zone, "d MMM EEEE", locale)

/** "19 Eki 01:30": a programme time where its start and end fall on different days. */
internal fun guideDayTime(time: Long, zone: ZoneId, locale: Locale = appLocale): String = format(time, zone, "d MMM HH:mm", locale)

/** "18 Eki Pazar, 20:30" on the guide's clock: when a guide was last downloaded. */
internal fun guideUpdatedTime(time: Long, zone: ZoneId, locale: Locale = appLocale): String = format(time, zone, "d MMM EEEE, HH:mm", locale)

/** "UTC+03:00", the same in every language. */
internal fun guideOffset(time: Long, zone: ZoneId): String = "UTC" + Instant.ofEpochMilli(time).atZone(zone).offset.id.let { if (it == "Z") "+00:00" else it }

/** "18 Eki, 20:30" on the TV's clock: when a source or the catalogue was last downloaded. */
internal fun updatedTime(time: Long, zone: ZoneId = ZoneId.systemDefault(), locale: Locale = appLocale): String =
    format(time, zone, "d MMM, HH:mm", locale)

/** A position or length on the player's clock: "4:07", "1:05:09". */
internal fun clockTime(ms: Long, locale: Locale = appLocale): String {
    val total = ms.coerceAtLeast(0) / 1000
    return if (total >= 3600) "%d:%02d:%02d".format(locale, total / 3600, (total / 60) % 60, total % 60)
        else "%d:%02d".format(locale, total / 60, total % 60)
}

/** "7,5" for a known rating, null otherwise. */
internal fun ratingText(rating: Float, locale: Locale = appLocale): String? = rating.takeIf { it > 0f }?.let { "%.1f".format(locale, it) }

internal val MediaEntry.ratingText: String? get() = ratingText(rating)

/** A stream's language tag by name, "İngilizce" for "en"; a tag without a name stays as it is, no language is null. */
internal fun languageName(tag: String?, locale: Locale = appLocale): String? = tag?.takeUnless { it == "und" || it.isBlank() }
    ?.let { Locale.forLanguageTag(it).getDisplayLanguage(locale).ifBlank { it } }
