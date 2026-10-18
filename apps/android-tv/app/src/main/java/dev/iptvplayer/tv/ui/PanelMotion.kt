package dev.iptvplayer.tv.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp

/** One interruptible position; focus never waits for this visual transition (MOT04). */
@Composable internal fun panelPosition(target: Dp, opening: Boolean, label: String): State<Dp> =
    animateDpAsState(target, if (LocalReducedMotion.current) snap() else
        tween(if (opening) 240 else 180, easing = FastOutSlowInEasing), label = label)

/** Measure the expensive grid at its destination only. Read motion in placement, never in measurement. */
internal fun Modifier.slidingInset(target: Dp, position: State<Dp>): Modifier = layout { measurable, constraints ->
    val inset = target.roundToPx().coerceIn(0, constraints.maxWidth)
    val width = constraints.maxWidth - inset
    val child = measurable.measure(constraints.copy(minWidth = width, maxWidth = width))
    layout(constraints.maxWidth, child.height) { child.placeRelative(position.value.roundToPx(), 0) }
}

/** Small header only: keep right-hand actions pinned while its left edge follows the category panel. */
internal fun Modifier.slidingHeaderInset(position: State<Dp>): Modifier = layout { measurable, constraints ->
    val inset = position.value.roundToPx().coerceIn(0, constraints.maxWidth)
    val width = constraints.maxWidth - inset
    val child = measurable.measure(constraints.copy(minWidth = width, maxWidth = width))
    layout(constraints.maxWidth, child.height) { child.placeRelative(inset, 0) }
}

/** Full-span input/empty/header items stay inside the visible part of the sliding poster grid. */
internal fun Modifier.panelVisibleWidth(position: State<Dp>, base: Dp): Modifier = layout { measurable, constraints ->
    val hidden = (position.value - base).roundToPx().coerceIn(0, constraints.maxWidth)
    val width = constraints.maxWidth - hidden
    val child = measurable.measure(constraints.copy(minWidth = width, maxWidth = width))
    layout(constraints.maxWidth, child.height) { child.placeRelative(0, 0) }
}
