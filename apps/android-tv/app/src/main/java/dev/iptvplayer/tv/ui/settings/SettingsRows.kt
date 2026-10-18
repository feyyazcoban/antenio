package dev.iptvplayer.tv.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import dev.iptvplayer.tv.R
import dev.iptvplayer.tv.TvState
import dev.iptvplayer.tv.TvViewModel
import dev.iptvplayer.tv.ui.Action
import dev.iptvplayer.tv.ui.Ink
import dev.iptvplayer.tv.ui.Note
import dev.iptvplayer.tv.ui.Roboto
import dev.iptvplayer.tv.ui.TvLayout
import dev.iptvplayer.tv.ui.focusRing
import kotlinx.coroutines.flow.collectLatest

/**
 * The focusable rows of the page on screen by key, in the order they appeared: where the page starts and where focus
 * returns from a page above. A row adds itself while it is composed, so a key never names a row that is gone.
 */
internal class PageRows {
    private val rows = LinkedHashMap<String, FocusRequester>()
    private var start: String? = null

    fun add(key: String, requester: FocusRequester, start: Boolean) {
        rows[key] = requester
        if (start) this.start = key
    }

    fun remove(key: String, requester: FocusRequester) {
        if (rows[key] === requester) rows.remove(key)
    }

    /** The row [returnKey] while it is on the page, else where the page starts: the row asked for, or the first one. */
    fun entry(returnKey: String?): FocusRequester? =
        returnKey?.let(rows::get) ?: start?.let(rows::get) ?: rows.values.firstOrNull()
}

/** What a ChoiceRow offers, kept by the panel while its ChoicePage is open. [selected] is an index of [options]. */
internal class ChoiceSpec(val title: String, val options: List<String>, val selected: Int, val choose: (Int) -> Unit)

/**
 * What a page's content works with: the app's state and model, the rows, and the panel around it. Each row takes a
 * key that stays the same while the page is open; focus returns to it from a page it opened. A row's section is the
 * main list's group it belongs to, named above the list while the row has focus.
 */
internal class SettingsScope(
    val s: TvState,
    val model: TvViewModel,
    private val rows: PageRows,
    /** The list of a [PageBody.LAZY] page; it keeps its place while a page above it is open. */
    val listState: LazyListState,
    /** The items the library shows, neither adult nor an episode, for the main page's summary. */
    val visibleItems: Int,
    private val navigate: (route: SettingsRoute, fromRowKey: String?) -> Unit,
    private val showChoice: (rowKey: String, choice: ChoiceSpec) -> Unit,
    private val showSection: (SectionId) -> Unit,
    /** Leaves this page, as Back does; nothing more once it has left (a repeated OK). */
    val back: () -> Unit,
    /** Closes the panel. */
    val dismiss: () -> Unit
) {
    fun open(route: SettingsRoute, fromRowKey: String?) = navigate(route, fromRowKey)

    /** Where focus goes back to after a window over the panel (the PIN pad): the row [key], else the page's start. */
    fun rowEntry(key: String): FocusRequester? = rows.entry(key)

    /** The focus of the row [key], for its Modifier.focusRequester; [start] makes it where the page starts. */
    @Composable fun rowFocus(key: String, start: Boolean = false): FocusRequester {
        val requester = remember { FocusRequester() }
        DisposableEffect(key, start) {
            rows.add(key, requester, start)
            onDispose { rows.remove(key, requester) }
        }
        return requester
    }

    private fun Modifier.row(focus: FocusRequester, section: SectionId?): Modifier = focusRequester(focus)
        .then(if (section != null) Modifier.onFocusChanged { if (it.isFocused) showSection(section) } else Modifier)

    /** A switch: Açık or Kapalı at the end, read as a switch by TalkBack. */
    @Composable fun ToggleRow(key: String, label: String, description: String, checked: Boolean, modifier: Modifier = Modifier,
        section: SectionId? = null, onClick: () -> Unit) {
        SettingRow(label, description, Modifier.row(rowFocus(key), section).then(modifier), checked = checked, onClick = onClick)
    }

    /** A row that acts on OK. [busy] reads as working; [attention] shows the description as a warning. */
    @Composable fun ActionRow(key: String, label: String, description: String, modifier: Modifier = Modifier,
        section: SectionId? = null, busy: Boolean = false, attention: Boolean = false, onClick: () -> Unit) {
        SettingRow(label, description, Modifier.row(rowFocus(key), section).then(modifier), busy = busy, attention = attention, onClick = onClick)
    }

    /** A row that opens [route]; Back returns to it. */
    @Composable fun PageRow(key: String, label: String, description: String, route: SettingsRoute, modifier: Modifier = Modifier,
        section: SectionId? = null) {
        ActionRow(key, label, description, modifier, section) { open(route, key) }
    }

    /** One of [options], the chosen one as its description; OK opens them as a ChoicePage and Back returns here. */
    @Composable fun ChoiceRow(key: String, label: String, options: List<String>, selected: Int, modifier: Modifier = Modifier,
        section: SectionId? = null, choose: (Int) -> Unit) {
        ActionRow(key, label, options.getOrElse(selected) { "" }, modifier, section) {
            showChoice(key, ChoiceSpec(label, options, selected, choose))
        }
    }

    /**
     * A value changed in steps: [value] as shown ("+30 dk"), the [decreaseLabel] and [increaseLabel] buttons ("−30 dk")
     * beside Sıfırla, which returns to the default; the buttons wrap at a large font scale. The row's focus is the
     * decrease button, or the increase one while that is off; a button that turns off under focus keeps it (Action).
     * Each button reads with the setting's name and the value, again when the value changes.
     */
    @Composable fun StepperRow(key: String, label: String, value: String, modifier: Modifier = Modifier, section: SectionId? = null,
        decreaseLabel: String = "−", increaseLabel: String = "+", canDecrease: Boolean = true, canIncrease: Boolean = true,
        isDefault: Boolean = false, decrease: () -> Unit, increase: () -> Unit, reset: () -> Unit) {
        val focus = rowFocus(key)
        val edge = rememberFocusEdge()
        val resetLabel = stringResource(R.string.settings_reset)
        val decreaseName = stringResource(R.string.settings_stepper_button, label, stringResource(R.string.settings_decrease))
        val increaseName = stringResource(R.string.settings_stepper_button, label, stringResource(R.string.settings_increase))
        val resetName = stringResource(R.string.settings_stepper_button, label, resetLabel)
        fun Modifier.step(name: String) = semantics { contentDescription = name; stateDescription = value }
        // The whole row comes into view with a button, so its name and value never scroll away from it.
        Column(modifier.then(edge.modifier).fillMaxWidth().background(Ink.Container, RoundedCornerShape(TvLayout.CardRadius))
            .then(if (section != null) Modifier.onFocusChanged { if (it.hasFocus) showSection(section) } else Modifier)
            .padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(label, color = Ink.Text, fontFamily = Roboto, fontSize = 14.sp, lineHeight = 19.sp)
                Text(value, color = Ink.Mint, fontFamily = Roboto, fontSize = 12.sp, lineHeight = 17.sp)
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Action(decreaseLabel, (if (canDecrease) Modifier.focusRequester(focus) else Modifier).step(decreaseName)
                    .testTag("$key:decrease"), enabled = canDecrease, compact = true, onClick = decrease)
                Action(increaseLabel, (if (canDecrease) Modifier else Modifier.focusRequester(focus)).step(increaseName)
                    .testTag("$key:increase"), enabled = canIncrease, compact = true, onClick = increase)
                Action(resetLabel, Modifier.step(resetName).testTag("$key:reset"), enabled = !isDefault,
                    compact = true, outlined = true, onClick = reset)
            }
        }
    }

    @Composable fun NoteRow(text: String, modifier: Modifier = Modifier, error: Boolean = false) {
        Note(text, error, modifier)
    }
}

/** The options of a ChoiceRow as full-width rows; the chosen one has the ✓ (D053) and takes focus first. */
@Composable internal fun SettingsScope.ChoicePage(choice: ChoiceSpec) {
    choice.options.forEachIndexed { index, option ->
        Action(option, rememberFocusEdge().modifier.focusRequester(rowFocus("option:$index", start = index == choice.selected))
            .fillMaxWidth().testTag("settings-option:$index"), selected = index == choice.selected, check = true) {
            choice.choose(index); back()
        }
    }
}

/** A small mint heading inside a page's text, as in Uygulama hakkında. */
@Composable internal fun SettingsLabel(label: String) {
    Text(label, color = Ink.Mint, fontFamily = Roboto, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 17.sp)
}

/**
 * Keeps a row holding focus in view with an 18 dp edge: the list's 14 dp edge fade plus the 4 dp focus-ring outset, so
 * focus never rests in the fade (D053). Never more than the list's 18 dp padding, or the request could not be
 * satisfied at the first/last row. A row whose size changes under focus (toggling can wrap the description onto
 * another line at large font scales) comes into view again whole.
 */
@Stable private class FocusEdge {
    val relocation = BringIntoViewRequester()
    var focused by mutableStateOf(false)
    var size by mutableStateOf(IntSize.Zero)
    val modifier: Modifier = Modifier.bringIntoViewRequester(relocation).onSizeChanged { size = it }
        .onFocusChanged { focused = it.hasFocus }
}

@Composable private fun rememberFocusEdge(): FocusEdge {
    val edge = remember { FocusEdge() }
    val safeEdge = with(LocalDensity.current) { 18.dp.toPx() }
    LaunchedEffect(edge, safeEdge) {
        snapshotFlow { if (edge.focused) edge.size else null }.collectLatest { size ->
            if (size != null) { withFrameNanos { }; edge.relocation.bringIntoView(Rect(0f, -safeEdge, size.width.toFloat(), size.height + safeEdge)) }
        }
    }
    return edge
}

@Composable private fun SettingRow(label: String, description: String, modifier: Modifier = Modifier,
    checked: Boolean? = null, busy: Boolean = false, attention: Boolean = false, onClick: () -> Unit) {
    val edge = rememberFocusEdge()
    val focused = edge.focused
    val state = stringResource(if (checked == true) R.string.switch_on else R.string.switch_off)
    Card(onClick, modifier.then(edge.modifier).fillMaxWidth().heightIn(min = 62.dp).semantics {
        if (checked != null) {
            role = Role.Switch
            toggleableState = if (checked) ToggleableState.On else ToggleableState.Off
            stateDescription = state
        }
        if (busy) progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate
    }, shape = CardDefaults.shape(shape = RoundedCornerShape(TvLayout.CardRadius)),
        colors = CardDefaults.colors(containerColor = Ink.Container, focusedContainerColor = Ink.High),
        scale = CardDefaults.scale(focusedScale = 1f),
        border = CardDefaults.border(focusedBorder = focusRing(TvLayout.CardRadius))) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(label, color = Ink.Text, fontFamily = Roboto, fontSize = 14.sp, lineHeight = 19.sp)
                Text(description, color = if (attention) Ink.Error else Ink.Muted, fontFamily = Roboto, fontSize = 12.sp, lineHeight = 17.sp)
            }
            if (checked != null) Text(state,
                // Ink.Selected nearly equals the focused row colour, so the focused ON chip uses the dark chip too.
                Modifier.background(if (checked && !focused) Ink.Selected else Ink.Background, RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 6.dp).clearAndSetSemantics { },
                color = if (checked) Ink.Mint else Ink.Muted, fontFamily = Roboto, fontSize = 12.sp)
        }
    }
}
