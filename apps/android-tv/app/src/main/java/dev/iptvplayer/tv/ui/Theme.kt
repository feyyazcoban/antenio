package dev.iptvplayer.tv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicSecureTextField
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.input.key.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.tv.material3.*
import dev.iptvplayer.tv.R

object Ink {
    val Background = Color(0xFF15191E)
    val Container = Color(0xFF252C33)
    val High = Color(0xFF343E47)
    val Text = Color(0xFFF0F3F1)
    val Muted = Color(0xFFB7C1C7)
    val Mint = Color(0xFFA8DFCB)
    val OnMint = Color(0xFF152B22)
    val Selected = Color(0xFF35423F)
    val OnSelected = Text
    val Outline = Color(0xFF9EAFB4)
    val Divider = Color(0xFF48535C)
    val Error = Color(0xFFFFB4AB)
}

val Roboto = FontFamily(Font(R.font.roboto))
val LocalReducedMotion = staticCompositionLocalOf { false }

@Composable fun PilotTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = darkColorScheme(
        primary = Ink.Mint, onPrimary = Ink.OnMint, surface = Ink.Background,
        onSurface = Ink.Text, surfaceVariant = Ink.Container, onSurfaceVariant = Ink.Muted,
        error = Ink.Error
    )) {
        ProvideTextStyle(TextStyle(fontFamily = Roboto, fontSize = 16.sp, color = Ink.Text, letterSpacing = 0.sp), content)
    }
}

/** Height of an [Action] pill; shared so lists of actions can snap to whole rows. */
@Composable internal fun actionHeight(compact: Boolean = false): Dp =
    maxOf(if (compact) 30.dp else 36.dp, with(LocalDensity.current) { (if (compact) 17.sp else 19.sp).toDp() } + if (compact) 10.dp else 16.dp)

/**
 * [check] is for full-width list rows: their selection is a trailing check like the other list rows,
 * not the centred chip marker that would land in the middle of a start-aligned label.
 */
@Composable fun Action(label: String, modifier: Modifier = Modifier, selected: Boolean? = null,
    enabled: Boolean = true, primary: Boolean = false, icon: String? = null, compact: Boolean = false,
    labelOverflow: TextOverflow = TextOverflow.Ellipsis, outlined: Boolean = false, check: Boolean = false,
    iconFilled: Boolean = false, onClick: () -> Unit) {
    val height = actionHeight(compact)
    // The TV Button stays focusable while disabled. The D-pad must not enter a disabled action;
    // one that becomes disabled while focused keeps focus, so focus never jumps or disappears.
    var focused by remember { mutableStateOf(false) }
    Button(onClick = onClick, modifier = modifier.onFocusChanged { focused = it.isFocused }
        .focusProperties { canFocus = enabled || focused }
        .heightIn(min = height, max = height)
        .then(if (selected != null) Modifier.semantics { this.selected = selected } else Modifier), enabled = enabled,
        shape = ButtonDefaults.shape(shape = RoundedCornerShape(50)),
        // Focus is the one mint fill (D064). A resting primary action is not white, which read as focused next to the
        // real focus; its mint label marks it instead. Outlined edges use the outline role (over 3:1 on the page).
        colors = ButtonDefaults.colors(containerColor = if (selected == true) Ink.Selected else if (outlined && !primary) Color.Transparent else Ink.High,
            contentColor = if (primary) Ink.Mint else Ink.Text, focusedContainerColor = Ink.Mint, focusedContentColor = Ink.OnMint),
        border = ButtonDefaults.border(border = Border(BorderStroke(1.dp, if (outlined && selected != true) Ink.Outline.copy(alpha = .6f) else Color.Transparent)),
            focusedBorder = Border(BorderStroke(TvLayout.Focus, Ink.Text), inset = 3.dp),
            focusedDisabledBorder = Border(BorderStroke(TvLayout.Focus, Ink.Muted), inset = 3.dp)),
        // No focus growth anywhere: tv-material animates it in composition (300 ms in, 500 ms out per item),
        // which stutters on TV SoCs. Focus shows instantly through colour and the 2 dp border (D056).
        scale = ButtonDefaults.scale(focusedScale = 1f),
        contentPadding = PaddingValues(horizontal = if (compact) 14.dp else 20.dp, vertical = if (compact) 5.dp else 8.dp)) {
        val contentColor = LocalContentColor.current
        if (check) Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (icon != null) LineIcon(icon, contentColor, filled = iconFilled)
            Text(label, Modifier.weight(1f), color = contentColor, fontFamily = Roboto, fontSize = if (compact) 13.sp else 14.sp, lineHeight = if (compact) 17.sp else 19.sp,
                fontWeight = FontWeight.Medium, maxLines = 1, overflow = labelOverflow)
            if (selected == true) Text("✓", Modifier.clearAndSetSemantics { }, color = if (contentColor == Ink.OnMint) Ink.OnMint else Ink.Mint,
                fontFamily = Roboto, fontSize = 16.sp, lineHeight = 16.sp)
        } else Row(Modifier.drawWithContent {
            drawContent()
            if (selected == true) {
                // Same offset and mint as the navigation marker, clear of descenders; the clamp keeps it inside the pill.
                val y = size.height + ((height.toPx() - size.height) / 2f - 2.dp.toPx()).coerceIn(2.dp.toPx(), 4.dp.toPx())
                drawLine(if (contentColor == Ink.Text) Ink.Mint else contentColor,
                    Offset(size.width / 2 - 6.dp.toPx(), y), Offset(size.width / 2 + 6.dp.toPx(), y),
                    strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
            }
        }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (icon != null) LineIcon(icon, contentColor, filled = iconFilled)
            if (label.isNotEmpty()) Text(label, color = contentColor, fontFamily = Roboto, fontSize = if (compact) 13.sp else 14.sp, lineHeight = if (compact) 17.sp else 19.sp,
                fontWeight = FontWeight.Medium, maxLines = 1, overflow = labelOverflow)
        }
    }
}

@Composable fun EntryField(label: String, value: String, onChange: (String) -> Unit,
    modifier: Modifier = Modifier, secret: Boolean = false, onDone: (() -> Unit)? = null,
    containerColor: Color = Ink.Container, revealed: Boolean = false) {
    var focused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val keyboard = rememberFieldKeyboard()
    val input = rememberFieldInput(value, onChange)
    val options = fieldKeyboardOptions(if (secret) KeyboardType.Password else KeyboardType.Text, if (onDone != null) ImeAction.Search else ImeAction.Next)
    val action = KeyboardActionHandler { if (onDone != null) { keyboard.close(); onDone() } else focusManager.moveFocus(FocusDirection.Down) }
    val style = TextStyle(color = Ink.Text, fontSize = 16.sp, fontFamily = Roboto)
    // A field removed while it has focus (a form closing) counts as left.
    DisposableEffect(keyboard) { onDispose { if (focused) keyboard.focusChanged(false, true) } }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(label, color = Ink.Muted, fontSize = 13.sp, lineHeight = 18.sp, fontFamily = Roboto)
        // Up/down leave this single-line field; OK opens the keyboard. The TV IME consumes its own remote events.
        val field = Modifier.fillMaxWidth().onPreviewKeyEvent { event -> fieldKey(event, focusManager, keyboard) }
            .onFocusChanged { keyboard.focusChanged(it.isFocused, focused); focused = it.isFocused }
            .background(containerColor, RoundedCornerShape(TvLayout.CardRadius))
            .border(if (focused) 2.dp else 1.dp, if (focused) Ink.Text else Ink.Outline.copy(alpha = .7f), RoundedCornerShape(TvLayout.CardRadius))
            .padding(12.dp)
        if (secret) BasicSecureTextField(input.state, field, inputTransformation = input.reporter, textStyle = style, keyboardOptions = options,
            onKeyboardAction = action, cursorBrush = SolidColor(Ink.Mint),
            textObfuscationMode = if (revealed) TextObfuscationMode.Visible else TextObfuscationMode.Hidden)
        else BasicTextField(input.state, field, inputTransformation = input.reporter, textStyle = style, keyboardOptions = options,
            onKeyboardAction = action, lineLimits = TextFieldLineLimits.SingleLine, cursorBrush = SolidColor(Ink.Mint))
    }
}

/**
 * The keyboard opens only when the viewer presses OK on a field (or taps it), never because focus arrived (D058).
 * Only the [TextFieldState] fields honour [KeyboardOptions.showKeyboardOnFocus]; the String ones always open it.
 */
internal fun fieldKeyboardOptions(type: KeyboardType, action: ImeAction) =
    KeyboardOptions(keyboardType = type, autoCorrectEnabled = false, imeAction = action, showKeyboardOnFocus = false)

/**
 * Remote keys on a focused single-line field: Up/Down leave it, OK opens the keyboard, Back with the keyboard open
 * closes only the keyboard. A remote's OK may arrive as Enter; a computer keyboard's Enter keeps its usual meaning
 * (the field's Next or Search).
 */
internal fun fieldKey(event: KeyEvent, focusManager: FocusManager, keyboard: FieldKeyboard): Boolean {
    if (event.key == Key.Back && keyboard.isOpen) {
        if (event.type == KeyEventType.KeyUp) keyboard.close()
        return true
    }
    if (event.type != KeyEventType.KeyDown) return false
    val remoteEnter = (event.key == Key.Enter || event.key == Key.NumPadEnter) &&
        event.nativeKeyEvent.device?.keyboardType != android.view.InputDevice.KEYBOARD_TYPE_ALPHABETIC
    return when {
        event.key == Key.DirectionDown -> focusManager.moveFocus(FocusDirection.Down)
        event.key == Key.DirectionUp -> focusManager.moveFocus(FocusDirection.Up)
        event.key == Key.DirectionCenter || remoteEnter -> { keyboard.open(); true }
        else -> false
    }
}

/**
 * The on-screen keyboard for the focused field. Compose's keyboard controller only reaches the String fields' input,
 * so requests go to the platform for the view that holds the field's input connection.
 */
internal class FieldKeyboard(private val view: View) {
    private val manager get() = view.context.getSystemService(InputMethodManager::class.java)
    val isOpen: Boolean get() = ViewCompat.getRootWindowInsets(view)?.isVisible(WindowInsetsCompat.Type.ime()) == true
    fun open() { manager?.showSoftInput(view, 0) }
    fun close() { manager?.hideSoftInputFromWindow(view.windowToken, 0) }

    /**
     * Focus moved onto or off a field. Leaving one field for another keeps the keyboard; once no field has focus
     * (a button, the side menu, a closed form) it closes, which Compose's own close no longer does for these fields.
     */
    fun focusChanged(now: Boolean, before: Boolean) {
        if (now == before) return
        FocusedFields.count = (FocusedFields.count + if (now) 1 else -1).coerceAtLeast(0)
        if (!now) view.post { if (FocusedFields.count == 0) close() }
    }
}

/** How many text fields have focus right now (zero or one; a hand-over between two briefly counts both). */
private object FocusedFields { var count = 0 }

@Composable internal fun rememberFieldKeyboard(): FieldKeyboard {
    val view = LocalView.current
    return remember(view) { FieldKeyboard(view) }
}

/** A field's text and the transformation that reports each edit as it happens, as the String fields did. */
internal class FieldInput(val state: TextFieldState, val reporter: InputTransformation)

/**
 * A field that shows [value] and reports the viewer's edits through [onChange] at once. A different value from
 * outside (Temizle, a reload) replaces the text; late echoes of the viewer's own edits never rewind it.
 */
@Composable internal fun rememberFieldInput(value: String, onChange: (String) -> Unit): FieldInput {
    val state = rememberTextFieldState(value)
    val report by rememberUpdatedState(onChange)
    val sync = remember { FieldSync(value) }
    val reporter = remember(sync) {
        InputTransformation { val text = asCharSequence().toString(); if (sync.edited(text)) report(text) }
    }
    SideEffect { if (sync.external(value)) state.setTextAndPlaceCursorAtEnd(value) }
    return remember(state, reporter) { FieldInput(state, reporter) }
}

/** The last text both sides agree on, and the viewer's edits reported since the last change from outside. */
private class FieldSync(private var known: String) {
    private val echoes = ArrayDeque<String>()
    fun edited(text: String): Boolean {
        if (text == known) return false
        known = text; echoes.addLast(text); if (echoes.size > 32) echoes.removeFirst()
        return true
    }
    fun external(value: String): Boolean {
        if (value == known || value in echoes) return false
        known = value; echoes.clear()
        return true
    }
}

/** The provider's rating on a poster: a small dark pill, the star in mint. The card already names the title. */
@Composable internal fun RatingBadge(text: String, modifier: Modifier = Modifier) {
    Row(modifier.background(Ink.Background.copy(alpha = .82f), RoundedCornerShape(6.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
        .clearAndSetSemantics { }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        Text("★", color = Ink.Mint, fontFamily = Roboto, fontSize = 11.sp, lineHeight = 15.sp)
        Text(text, color = Ink.Text, fontFamily = Roboto, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 15.sp)
    }
}

@Composable fun Heading(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier, color = Ink.Text, fontFamily = Roboto, fontWeight = FontWeight.Medium, fontSize = 31.sp, lineHeight = 38.sp)
}

@Composable fun Note(text: String, error: Boolean = false, modifier: Modifier = Modifier) {
    Text(text, modifier, color = if (error) Ink.Error else Ink.Muted, fontFamily = Roboto,
        fontSize = 13.sp, lineHeight = 19.sp)
}

/**
 * "3 / 120" beside a heading, as a [Note]. [position] is read only here, so moving focus along a list
 * recomposes this text alone; tabular digits keep its width, and so the heading's layout, steady.
 */
@Composable internal fun PositionNote(position: IntState, total: Int, modifier: Modifier = Modifier) {
    Text("${position.intValue.coerceIn(1, total.coerceAtLeast(1))} / $total", modifier, color = Ink.Muted, fontFamily = Roboto,
        fontSize = 13.sp, lineHeight = 19.sp, maxLines = 1, style = LocalTextStyle.current.copy(fontFeatureSettings = "tnum"))
}
