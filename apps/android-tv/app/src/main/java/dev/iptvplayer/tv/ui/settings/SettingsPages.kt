package dev.iptvplayer.tv.ui.settings

import androidx.activity.compose.LocalActivity
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
import dev.iptvplayer.tv.AppLanguage
import dev.iptvplayer.tv.BuildConfig
import dev.iptvplayer.tv.CatalogOperation
import dev.iptvplayer.tv.LocaleController
import dev.iptvplayer.tv.R
import dev.iptvplayer.tv.TvState
import dev.iptvplayer.tv.data.AdminReason
import dev.iptvplayer.tv.data.CategoryAccess
import dev.iptvplayer.tv.data.CategoryVisibility
import dev.iptvplayer.tv.data.Feature
import dev.iptvplayer.tv.data.MediaKind
import dev.iptvplayer.tv.data.ParentalStatus
import dev.iptvplayer.tv.data.UiText
import dev.iptvplayer.tv.ui.Action
import dev.iptvplayer.tv.ui.NextvLogo
import dev.iptvplayer.tv.ui.rememberPinOpener
import dev.iptvplayer.tv.ui.resolve
import dev.iptvplayer.tv.ui.shownCategory
import dev.iptvplayer.tv.ui.updatedTime

/** The main page's sections; the one holding focus names itself above the list. */
internal enum class SectionId(@StringRes val label: Int) {
    APPEARANCE(R.string.settings_section_appearance), SOURCES(R.string.settings_section_sources),
    CONTENT(R.string.settings_section_content), WATCHING(R.string.settings_section_watching)
}

/** What a page asks before it opens; the panel's [PageGate] decides. */
internal sealed interface PageGuard {
    data object None : PageGuard
    /** The parental PIN while one is set, asked with [reason]'s sentence; once per Settings visit. */
    data class Pin(val reason: AdminReason) : PageGuard
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
    /** The page lays out its own lazy list on [SettingsScope.listState], for lists as long as a provider's categories. */
    LAZY
}

/**
 * A page of the Settings panel: its [title] in the header, the [section] it belongs to (none for the main page) and
 * what it asks before opening. [notice] is what the last change made on the page said, shown under the header above
 * the rows, so it is in view whichever row has focus. A new setting is a page here (PLAN §2, rule 5).
 */
internal data class SettingsPageSpec(
    val id: String,
    val title: UiText,
    val section: SectionId?,
    val guard: PageGuard = PageGuard.None,
    val body: PageBody = PageBody.LIST,
    val notice: (TvState) -> UiText? = { null },
    val content: @Composable SettingsScope.() -> Unit
)

/** The note of a page where the PIN, its locks or a category row's lock change (ParentalUi.notice). */
internal val parentalNotice: (TvState) -> UiText? = { it.parental.notice }

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
    // Kaldır asks for the PIN that guards the sources (pin.md §3.8), over this page.
    SettingsPageSpec(removeSourcePage.id, UiText.Res(R.string.remove_source_question), SectionId.SOURCES, body = PageBody.SHORT_LIST) {
        Confirmation(stringResource(R.string.settings_remove_confirm, s.source?.name.orEmpty()), stringResource(R.string.remove_source)) {
            model.removeSource()
        }
    },
    SettingsPageSpec(clearHistoryPage.id, UiText.Res(R.string.settings_clear_history_question), SectionId.WATCHING, body = PageBody.SHORT_LIST) {
        Confirmation(stringResource(R.string.settings_clear_history_confirm), stringResource(R.string.settings_clear_history)) {
            model.clearHistory(); false
        }
    },
    SettingsPageSpec(aboutPage.id, UiText.Res(R.string.settings_about), SectionId.WATCHING, body = PageBody.READING) { About() },
    // Its lists name the adult categories and can show them again (pin.md §2).
    SettingsPageSpec(categoriesPage.id, UiText.Res(R.string.settings_categories), SectionId.CONTENT,
        PageGuard.Pin(AdminReason.CATEGORIES), PageBody.SHORT_LIST) { CategoryKinds() },
) + categoryKinds.map { kind ->
    SettingsPageSpec(categoryListPage(kind).id, UiText.Res(kind.label), SectionId.CONTENT, body = PageBody.LAZY,
        notice = parentalNotice) { CategoryList(kind) }
} + parentalPageList

internal val settingsPages: Map<String, SettingsPageSpec> = settingsPageList.associateBy { it.id }

@Composable private fun SettingsScope.MainList() {
    val openPin = rememberPinOpener()
    ToggleRow("reduced-motion", stringResource(R.string.settings_reduced_motion), stringResource(R.string.settings_reduced_motion_note),
        s.reducedMotion, section = SectionId.APPEARANCE) { model.reducedMotion() }
    // Named in both languages and each language in itself, so it is found whatever language is on screen (S13). A new
    // language recreates the activity; the panel comes back on this row (Settings).
    val activity = LocalActivity.current
    val languages = AppLanguage.entries
    // Read again each time the page composes, as it does when its options close.
    val chosen = remember(activity) { activity?.let(LocaleController::choice) ?: AppLanguage.SYSTEM }
    ChoiceRow("language", stringResource(R.string.settings_language), languages.map { stringResource(it.label) }, languages.indexOf(chosen),
        section = SectionId.APPEARANCE) { picked -> activity?.let { LocaleController.choose(it, languages[picked]) } }
    ToggleRow("startup-sound", stringResource(R.string.settings_startup_sound), stringResource(R.string.settings_startup_sound_note),
        s.startupSound, section = SectionId.APPEARANCE) { model.startupSound() }
    Spacer(Modifier.height(6.dp))
    NoteRow(stringResource(R.string.text_pair, pluralStringResource(R.plurals.settings_sources, s.sources.size, s.sources.size.toString()),
        pluralStringResource(R.plurals.settings_visible_items, visibleItems, visibleItems.toString())))
    // Source management asks for its PIN over the panel first (pin.md §3.8); the panel closes once the sources open.
    ActionRow("manage-sources", stringResource(R.string.manage_sources), stringResource(R.string.settings_manage_sources_note), section = SectionId.SOURCES) {
        if (model.manageSources()) openPin { rowEntry("manage-sources") } else dismiss()
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
        section = SectionId.SOURCES) { if (model.editSource()) openPin { rowEntry("edit-source") } else dismiss() }
    if (s.sources.size == 1) PageRow("remove-source", stringResource(R.string.remove_source), stringResource(R.string.settings_remove_source_note),
        removeSourcePage, section = SectionId.SOURCES)
    Spacer(Modifier.height(6.dp))
    // D068: what the screens show. Turning it on is free; off asks for the PIN while one is set, and without one first
    // offers to make one (pin.md §3.3).
    ToggleRow("hide-adult", stringResource(R.string.settings_hide_adult), stringResource(if (s.parental.guarded)
        R.string.settings_hide_adult_note_pin else R.string.settings_hide_adult_note),
        s.hideAdultCategories, section = SectionId.CONTENT) {
        if (s.hideAdultCategories && s.parental.status == ParentalStatus.OFF) open(adultPinOfferPage, "hide-adult")
        else if (model.setHideAdult(!s.hideAdultCategories)) openPin { rowEntry("hide-adult") }
    }
    val hiddenCount = s.hiddenCategories.size
    PageRow("manage-categories", stringResource(R.string.settings_manage_categories), if (hiddenCount > 0)
        pluralStringResource(R.plurals.settings_manage_categories_hidden, hiddenCount, hiddenCount.toString())
        else stringResource(R.string.settings_manage_categories_note), categoriesPage, section = SectionId.CONTENT)
    PageRow("parental", stringResource(R.string.parental_title), stringResource(when (s.parental.status) {
        ParentalStatus.ON -> if (s.parental.sourcesNeedPin) R.string.parental_row_on_sources else R.string.parental_row_on
        ParentalStatus.UNAVAILABLE -> R.string.parental_row_unavailable
        else -> R.string.parental_row_off
    }), parentalPage, section = SectionId.CONTENT)
    Spacer(Modifier.height(6.dp))
    ToggleRow("auto-next", stringResource(R.string.settings_auto_next), stringResource(R.string.settings_auto_next_note),
        s.autoNextEpisode, section = SectionId.WATCHING) { model.autoNextEpisode() }
    PageRow("watch-history", stringResource(R.string.settings_watch_history), stringResource(R.string.settings_watch_history_note),
        clearHistoryPage, section = SectionId.WATCHING)
    PageRow("about", stringResource(R.string.settings_about), stringResource(R.string.settings_about_note), aboutPage, section = SectionId.WATCHING)
}

/**
 * Vazgeç first, so a repeated OK never confirms; confirming closes the panel. An [action] that opened the PIN pad first
 * says true: the page stays under the pad, and the panel closes once the action runs.
 */
@Composable private fun SettingsScope.Confirmation(note: String, confirm: String, action: () -> Boolean) {
    val openPin = rememberPinOpener()
    NoteRow(note)
    Spacer(Modifier.height(12.dp))
    Action(stringResource(R.string.action_cancel), Modifier.focusRequester(rowFocus("cancel"))) { back() }
    Action(confirm, Modifier.focusRequester(rowFocus("confirm")).testTag("settings-confirm")) {
        if (action()) openPin { rowEntry("confirm") } else dismiss()
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

/** Canlı TV, Filmler and Diziler, each with its count of categories and of those hidden or locked. */
@Composable private fun SettingsScope.CategoryKinds() {
    NoteRow(stringResource(R.string.settings_categories_note))
    categoryKinds.forEach { kind ->
        val list = s.categoryChoices[kind].orEmpty()
        val off = list.count { CategoryVisibility.key(kind, it.name) in s.hiddenCategories || s.hideAdultCategories && it.adult }
        val locked = list.count { CategoryVisibility.key(kind, it.name) in s.locks.categories }
        val counts = if (off > 0) pluralStringResource(R.plurals.settings_kind_categories_hidden, list.size, list.size.toString(), off.toString())
            else pluralStringResource(R.plurals.settings_kind_categories, list.size, list.size.toString())
        PageRow("category-kind:${kind.name}", stringResource(kind.label), if (locked > 0)
            stringResource(R.string.text_pair, counts, stringResource(R.string.settings_kind_categories_locked, locked.toString())) else counts,
            categoryListPage(kind), modifier = Modifier.testTag("category-kind:${kind.name}"))
    }
}

/**
 * One section's categories in the provider's order, each Açık, Kilitli or Gizli at its end; OK offers Göster, Kilitle
 * and Gizle as a ChoicePage (pin.md §3.4). Kilitle on a TV without a PIN makes one first. Adult-looking ones stay hidden
 * while the adult filter is on, and say so; they offer nothing. What the last change could not do reads above the list
 * (SettingsPageSpec.notice). A lazy list: a provider can have hundreds of categories.
 */
@Composable private fun SettingsScope.CategoryList(kind: MediaKind) {
    val choices = s.categoryChoices[kind].orEmpty()
    if (choices.isEmpty()) {
        NoteRow(stringResource(R.string.settings_no_categories), modifier = Modifier.padding(28.dp)); return
    }
    val openPin = rememberPinOpener()
    val options = listOf(stringResource(R.string.category_access_show), stringResource(if (s.parental.status == ParentalStatus.OFF)
        R.string.category_access_lock_first else R.string.category_access_lock), stringResource(R.string.category_access_hide))
    // In CategoryAccess order, as the options are.
    val states = listOf(R.string.category_state_shown, R.string.lock_state, R.string.category_state_hidden).map { stringResource(it) }
    LazyColumn(Modifier.fillMaxSize().testTag("settings-categories"), state = listState,
        contentPadding = PaddingValues(start = 28.dp, end = 28.dp, top = 18.dp, bottom = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(choices.size, key = { choices[it].name }) { index ->
            val choice = choices[index]
            val key = "category:${choice.name}"
            val (label, tag) = shownCategory(choice.name)
            val name = label + if (tag.isNotEmpty()) " · $tag" else ""
            if (s.hideAdultCategories && choice.adult) ActionRow(key, name,
                pluralStringResource(R.plurals.settings_category_items_adult, choice.count, choice.count.toString()),
                Modifier.testTag("category-choice:$index"), value = states[CategoryAccess.HIDDEN.ordinal]) { }
            else {
                val access = s.categoryAccess(kind, choice.name)
                ChoiceRow(key, name, options, access.ordinal, Modifier.testTag("category-choice:$index"),
                    description = pluralStringResource(R.plurals.count_items, choice.count, choice.count.toString()),
                    value = states[access.ordinal]) { picked ->
                    if (model.setCategoryAccess(kind, choice.name, CategoryAccess.entries[picked])) openPin { rowEntry(key) }
                }
            }
        }
    }
}
