package dev.iptvplayer.tv.ui

import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import dev.iptvplayer.tv.R
import dev.iptvplayer.tv.data.CategoryFilter
import dev.iptvplayer.tv.data.DisplayTitle
import dev.iptvplayer.tv.data.UiText
import dev.iptvplayer.tv.data.UnnamedTitle
import dev.iptvplayer.tv.data.displayCategory
import dev.iptvplayer.tv.data.displayTitle

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

/** [displayTitle] as the screens show it: a title the provider left unnamed reads in the app's language ([UnnamedTitle]). */
internal fun shownTitle(title: String, year: String, resources: Resources): DisplayTitle =
    UnnamedTitle.text(title)?.let { DisplayTitle(it.resolve(resources), year, "") } ?: displayTitle(title, year)

/** [shownTitle], worked out once per title, as cards in long lists need. */
@Composable internal fun rememberShownTitle(title: String, year: String = ""): DisplayTitle {
    val resources = LocalResources.current
    return remember(title, year, resources) { shownTitle(title, year, resources) }
}

/** [title] as the provider gave it; one it left unnamed in the app's language. */
@Composable @ReadOnlyComposable
internal fun shownName(title: String): String = UnnamedTitle.text(title)?.resolve() ?: title

/** [shownName], worked out once per title: what TalkBack reads for a card in a long list. */
@Composable internal fun rememberShownName(title: String): String {
    val resources = LocalResources.current
    return remember(title, resources) { UnnamedTitle.text(title)?.resolve(resources) ?: title }
}
