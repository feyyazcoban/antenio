package dev.iptvplayer.tv.data

import android.content.res.Resources
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes

/**
 * A text the screens show, kept as a string resource and its arguments until the UI resolves it in the app's
 * language: the model and the data layer hold no ready sentences. [Raw] is for provider data only (names, titles).
 * An argument that is itself a [UiText] is resolved first.
 */
sealed interface UiText {
    data class Res(@StringRes val id: Int, val args: List<Any> = emptyList()) : UiText
    /** Without [args] the count itself is the format argument. */
    data class Plural(@PluralsRes val id: Int, val count: Int, val args: List<Any> = emptyList()) : UiText
    data class Raw(val text: String) : UiText

    /** Resolution through plain lookups, so JVM tests need no Android resources. */
    fun resolve(string: (Int, Array<Any>) -> String, plural: (Int, Int, Array<Any>) -> String): String {
        fun arguments(values: List<Any>) = values.map { (it as? UiText)?.resolve(string, plural) ?: it }.toTypedArray()
        return when (this) {
            is Res -> string(id, arguments(args))
            is Plural -> plural(id, count, arguments(args.ifEmpty { listOf(count) }))
            is Raw -> text
        }
    }

    // A text without arguments is not formatted, so a literal % in it stays as it is.
    fun resolve(resources: Resources): String = resolve(
        { id, args -> if (args.isEmpty()) resources.getString(id) else resources.getString(id, *args) },
        { id, count, args -> resources.getQuantityString(id, count, *args) })
}
