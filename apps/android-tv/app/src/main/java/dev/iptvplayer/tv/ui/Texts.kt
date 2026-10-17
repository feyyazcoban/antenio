package dev.iptvplayer.tv.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import dev.iptvplayer.tv.R
import dev.iptvplayer.tv.data.CategoryFilter
import dev.iptvplayer.tv.data.UiText
import dev.iptvplayer.tv.data.displayCategory

/** The text in the app's language; read again when the configuration, and with it the language, changes. */
@Composable @ReadOnlyComposable
internal fun UiText.resolve(): String = resolve(LocalResources.current)

/** Several notes as one paragraph, the way source cards and the catalogue warning show them. */
@Composable @ReadOnlyComposable
internal fun List<UiText>.resolve(): String = map { it.resolve() }.joinToString(" ")

/** [displayCategory], with the catalogue's own "Diğer" ([CategoryFilter.OTHER], a stored key) in the app's language. */
@Composable @ReadOnlyComposable
internal fun shownCategory(category: String): Pair<String, String> =
    if (category == CategoryFilter.OTHER) stringResource(R.string.category_other) to "" else displayCategory(category)
