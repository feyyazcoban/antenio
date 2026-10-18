package dev.iptvplayer.tv.ui.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.iptvplayer.tv.BuildConfig
import dev.iptvplayer.tv.CatalogOperation
import dev.iptvplayer.tv.R
import dev.iptvplayer.tv.data.CategoryVisibility
import dev.iptvplayer.tv.data.Feature
import dev.iptvplayer.tv.data.MediaKind
import dev.iptvplayer.tv.data.UiText
import dev.iptvplayer.tv.ui.Action
import dev.iptvplayer.tv.ui.NextvLogo
import dev.iptvplayer.tv.ui.resolve
import dev.iptvplayer.tv.ui.shownCategory
import dev.iptvplayer.tv.ui.updatedTime

/** The main page's sections; the one holding focus names itself above the list. */
internal enum class SectionId(@StringRes val label: Int) {
    APPEARANCE(R.string.settings_section_appearance), SOURCES(R.string.settings_section_sources),
    CONTENT(R.string.settings_section_content), WATCHING(R.string.settings_section_watching)
}

/** What a page asks before it opens; the panel's [PageGate] decides. Every page is open for now. */
internal sealed interface PageGuard {
    data object None : PageGuard
    /** The parental PIN. */
    data object Pin : PageGuard
    /** NEXTV Plus, for [feature]. */
    data class Plus(val feature: Feature) : PageGuard
}

/** How a page's content sits in the panel. */
internal enum class PageBody {
    /** Rows in a scrolling column; rows cut by the divider or the hint row fade out instead of resting as half a line. */
    LIST,
    /** The same column without the fade, as the short confirmation and Kategoriler pages have it. */
    SHORT_LIST,
    /** Text in a frame that takes focus and scrolls with Up and Down; its text starts on the 28 dp edge of the title. */
    READING,
    /** The page lays out its own lazy list, for lists as long as a provider's categories. */
    LAZY
}

/**
 * A page of the Settings panel: its [title] in the header, the [section] it belongs to (none for the main page) and
 * what it asks before opening. A new setting is a page here (PLAN §2, rule 5).
 */
internal data class SettingsPageSpec(
    val id: String,
    val title: UiText,
    val section: SectionId?,
    val guard: PageGuard = PageGuard.None,
    val body: PageBody = PageBody.LIST,
    val content: @Composable SettingsScope.() -> Unit
)

internal val mainPage = SettingsRoute.Page("main")
private val removeSourcePage = SettingsRoute.Page("remove-source")
private val clearHistoryPage = SettingsRoute.Page("clear-history")
private val aboutPage = SettingsRoute.Page("about")
private val categoriesPage = SettingsRoute.Page("categories")
private val categoryKinds = listOf(MediaKind.LIVE, MediaKind.MOVIE, MediaKind.SERIES)
private fun categoryListPage(kind: MediaKind) = SettingsRoute.Page("category-list:${kind.name}")

/** Every page of the panel by id. A ChoicePage is not among them: its ChoiceRow carries the options. */
internal val settingsPageList: List<SettingsPageSpec> = listOf(
    SettingsPageSpec(mainPage.id, UiText.Res(R.string.settings_title), section = null) { MainList() },
    SettingsPageSpec(removeSourcePage.id, UiText.Res(R.string.remove_source_question), SectionId.SOURCES, body = PageBody.SHORT_LIST) {
        Confirmation(stringResource(R.string.settings_remove_confirm, s.source?.name.orEmpty()), stringResource(R.string.remove_source)) {
            model.removeSource()
        }
    },
    SettingsPageSpec(clearHistoryPage.id, UiText.Res(R.string.settings_clear_history_question), SectionId.WATCHING, body = PageBody.SHORT_LIST) {
        Confirmation(stringResource(R.string.settings_clear_history_confirm), stringResource(R.string.settings_clear_history)) {
            model.clearHistory()
        }
    },
    SettingsPageSpec(aboutPage.id, UiText.Res(R.string.settings_about), SectionId.WATCHING, body = PageBody.READING) { About() },
    SettingsPageSpec(categoriesPage.id, UiText.Res(R.string.settings_categories), SectionId.CONTENT, body = PageBody.SHORT_LIST) { CategoryKinds() },
) + categoryKinds.map { kind ->
    SettingsPageSpec(categoryListPage(kind).id, UiText.Res(kind.label), SectionId.CONTENT, body = PageBody.LAZY) { CategoryList(kind) }
}

internal val settingsPages: Map<String, SettingsPageSpec> = settingsPageList.associateBy { it.id }

@Composable private fun SettingsScope.MainList() {
    val visibleCount = remember(s.catalog) { s.catalog.items.count { !it.adult && it.kind != MediaKind.EPISODE } }
    ToggleRow("reduced-motion", stringResource(R.string.settings_reduced_motion), stringResource(R.string.settings_reduced_motion_note),
        s.reducedMotion, section = SectionId.APPEARANCE) { model.reducedMotion() }
    ToggleRow("startup-sound", stringResource(R.string.settings_startup_sound), stringResource(R.string.settings_startup_sound_note),
        s.startupSound, section = SectionId.APPEARANCE) { model.startupSound() }
    Spacer(Modifier.height(6.dp))
    NoteRow(stringResource(R.string.text_pair, pluralStringResource(R.plurals.settings_sources, s.sources.size, s.sources.size.toString()),
        pluralStringResource(R.plurals.settings_visible_items, visibleCount, visibleCount.toString())))
    ActionRow("manage-sources", stringResource(R.string.manage_sources), stringResource(R.string.settings_manage_sources_note), section = SectionId.SOURCES) {
        dismiss(); model.manageSources()
    }
    val refreshNotice = s.catalogNotice?.takeIf { it.operation == CatalogOperation.REFRESH }
    // The timestamp captions the refresh row: 4 dp more above, 8 dp below (clear of the focus ring).
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (s.catalogUpdatedAt > 0) NoteRow(stringResource(R.string.last_updated, updatedTime(s.catalogUpdatedAt)),
            modifier = Modifier.padding(top = 4.dp))
        ActionRow("refresh", stringResource(R.string.settings_refresh), if (s.refreshing) stringResource(R.string.settings_refreshing)
            else refreshNotice?.message?.resolve() ?: stringResource(R.string.settings_refresh_note),
            section = SectionId.SOURCES, busy = s.refreshing, attention = refreshNotice?.needsAttention == true) { model.refresh() }
    }
    s.catalogNotice?.takeIf { it.operation != CatalogOperation.REFRESH }?.let { NoteRow(it.message.resolve(), error = it.needsAttention) }
    if (s.sources.size == 1) ActionRow("edit-source", stringResource(R.string.settings_edit_source), stringResource(R.string.settings_edit_source_note),
        section = SectionId.SOURCES) { dismiss(); model.editSource() }
    if (s.sources.size == 1) PageRow("remove-source", stringResource(R.string.remove_source), stringResource(R.string.settings_remove_source_note),
        removeSourcePage, section = SectionId.SOURCES)
    Spacer(Modifier.height(6.dp))
    // D068: what the screens show. A switch anyone can turn back, not a PIN lock.
    ToggleRow("hide-adult", stringResource(R.string.settings_hide_adult), stringResource(R.string.settings_hide_adult_note),
        s.hideAdultCategories, section = SectionId.CONTENT) { model.hideAdultCategories() }
    val hiddenCount = s.hiddenCategories.size
    PageRow("manage-categories", stringResource(R.string.settings_manage_categories), if (hiddenCount > 0)
        pluralStringResource(R.plurals.settings_manage_categories_hidden, hiddenCount, hiddenCount.toString())
        else stringResource(R.string.settings_manage_categories_note), categoriesPage, section = SectionId.CONTENT)
    Spacer(Modifier.height(6.dp))
    ToggleRow("auto-next", stringResource(R.string.settings_auto_next), stringResource(R.string.settings_auto_next_note),
        s.autoNextEpisode, section = SectionId.WATCHING) { model.autoNextEpisode() }
    PageRow("watch-history", stringResource(R.string.settings_watch_history), stringResource(R.string.settings_watch_history_note),
        clearHistoryPage, section = SectionId.WATCHING)
    PageRow("about", stringResource(R.string.settings_about), stringResource(R.string.settings_about_note), aboutPage, section = SectionId.WATCHING)
}

/** Vazgeç first, so a repeated OK never confirms; confirming closes the panel. */
@Composable private fun SettingsScope.Confirmation(note: String, confirm: String, action: () -> Unit) {
    NoteRow(note)
    Spacer(Modifier.height(12.dp))
    Action(stringResource(R.string.action_cancel), Modifier.focusRequester(rowFocus("cancel"))) { back() }
    Action(confirm, Modifier.focusRequester(rowFocus("confirm"))) {
        action()
        dismiss()
    }
}

@Composable private fun SettingsScope.About() {
    NextvLogo(height = 24.dp)
    NoteRow(stringResource(R.string.settings_version, BuildConfig.VERSION_NAME))
    NoteRow("Android ${android.os.Build.VERSION.RELEASE} · API ${android.os.Build.VERSION.SDK_INT}")
    Spacer(Modifier.height(6.dp))
    SettingsLabel(stringResource(R.string.settings_about_library))
    NoteRow(stringResource(R.string.settings_about_library_note,
        stringResource(R.string.source_type_m3u), stringResource(R.string.source_type_xtream)))
    NoteRow(stringResource(R.string.settings_about_storage_note))
    Spacer(Modifier.height(6.dp))
    SettingsLabel(stringResource(R.string.settings_about_release))
    NoteRow(stringResource(R.string.settings_about_release_note))
}

/** Canlı TV, Filmler and Diziler, each with its count of categories and of those turned off. */
@Composable private fun SettingsScope.CategoryKinds() {
    NoteRow(stringResource(R.string.settings_categories_note))
    categoryKinds.forEach { kind ->
        val list = s.categoryChoices[kind].orEmpty()
        val off = list.count { CategoryVisibility.key(kind, it.name) in s.hiddenCategories || s.hideAdultCategories && it.adult }
        PageRow("category-kind:${kind.name}", stringResource(kind.label), if (off > 0)
            pluralStringResource(R.plurals.settings_kind_categories_hidden, list.size, list.size.toString(), off.toString())
            else pluralStringResource(R.plurals.settings_kind_categories, list.size, list.size.toString()),
            categoryListPage(kind), modifier = Modifier.testTag("category-kind:${kind.name}"))
    }
}

/**
 * One section's categories as switches (Açık = shown), in the provider's order. Adult-looking ones stay off while the
 * adult filter is on, and say so. A lazy list: a provider can have hundreds of categories.
 */
@Composable private fun SettingsScope.CategoryList(kind: MediaKind) {
    val choices = s.categoryChoices[kind].orEmpty()
    if (choices.isEmpty()) {
        NoteRow(stringResource(R.string.settings_no_categories), modifier = Modifier.padding(28.dp)); return
    }
    LazyColumn(Modifier.fillMaxSize().testTag("settings-categories"),
        contentPadding = PaddingValues(start = 28.dp, end = 28.dp, top = 18.dp, bottom = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(choices.size, key = { choices[it].name }) { index ->
            val choice = choices[index]
            val byAdult = s.hideAdultCategories && choice.adult
            val visible = !byAdult && CategoryVisibility.key(kind, choice.name) !in s.hiddenCategories
            val (label, tag) = shownCategory(choice.name)
            ToggleRow("category:${choice.name}", label + if (tag.isNotEmpty()) " · $tag" else "",
                pluralStringResource(if (byAdult) R.plurals.settings_category_items_adult else R.plurals.count_items, choice.count, choice.count.toString()),
                visible, modifier = Modifier.testTag("category-choice:$index")) {
                if (!byAdult) model.categoryVisible(kind, choice.name, !visible)
            }
        }
    }
}
