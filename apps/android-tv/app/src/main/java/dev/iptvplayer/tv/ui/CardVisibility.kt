package dev.iptvplayer.tv.ui

import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp

internal data class CardVisibility(val container: Modifier, val focus: Modifier)

/**
 * Relocate the image and its labels together; cancel relocation when focus moves on. With [frameShelf] off, a
 * fitting heading ([contextAbove]) is kept in view with the tile but the shelf is not pinned to the viewport top.
 */
@Composable internal fun rememberCardVisibility(horizontalPadding: Dp = 0.dp, contextAbove: Dp = 0.dp,
    viewportHeight: Dp = 0.dp, frameShelf: Boolean = true): CardVisibility {
    val request = remember { BringIntoViewRequester() }
    var focused by remember { mutableStateOf(false) }
    var clipped by remember { mutableStateOf(false) }
    var size by remember { mutableStateOf(IntSize.Zero) }
    val edge = with(LocalDensity.current) { horizontalPadding.toPx() }
    val top = with(LocalDensity.current) { 7.dp.toPx() }
    val bottom = with(LocalDensity.current) { 27.dp.toPx() }
    val context = with(LocalDensity.current) { contextAbove.toPx() }
    val viewport = with(LocalDensity.current) { viewportHeight.toPx() }
    LaunchedEffect(focused, context, viewport, frameShelf, edge) {
        if (focused) {
            // Becoming visible is an outcome of this scroll, not a new destination. In particular,
            // the card becomes unclipped before its bottom safety margin has finished moving.
            // Collect sequentially: a resize is rechecked after the current request, without
            // cancelling and restarting its motion. A new focus/viewport still cancels promptly.
            snapshotFlow { size to clipped }.collect { (measured, _) ->
                withFrameNanos { }
                val canFrameShelf = context > 0 && measured.height + top + context + bottom <= viewport
                val requestedTop = if (canFrameShelf) top + context else top
                // A viewport-sized rectangle anchors a fitting shelf heading and the whole tile.
                // Oversized accessibility content falls back to keeping the tile itself visible.
                val requestedBottom = if (canFrameShelf && frameShelf) viewport - requestedTop else measured.height + bottom
                request.bringIntoView(Rect(-edge, -requestedTop, measured.width + edge, requestedBottom))
            }
        }
    }
    return CardVisibility(Modifier.bringIntoViewRequester(request).onGloballyPositioned { coordinates ->
        val visible = coordinates.boundsInRoot()
        size = coordinates.size
        // Recheck after a viewport change without restarting relocation on every scroll pixel.
        clipped = visible.height + 1f < coordinates.size.height || visible.width + 1f < coordinates.size.width
    }, Modifier.onFocusChanged { focused = it.hasFocus })
}
