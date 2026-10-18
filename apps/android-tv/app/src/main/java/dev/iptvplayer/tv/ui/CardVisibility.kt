package dev.iptvplayer.tv.ui

import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.requireLayoutCoordinates
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.relocation.BringIntoViewModifierNode
import androidx.compose.ui.relocation.bringIntoView as bringNodeIntoView
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp

internal data class CardVisibility(val container: Modifier, val focus: Modifier)

/**
 * Relocate the image and its labels together; cancel relocation when focus moves on. With [frameShelf] off, a
 * fitting heading ([contextAbove]) is kept in view with the tile but the shelf is not pinned to the viewport top.
 */
@Composable internal fun rememberCardVisibility(horizontalPadding: Dp = 0.dp, contextAbove: Dp = 0.dp,
    viewportHeight: Dp = 0.dp, frameShelf: Boolean = true, topPadding: Dp = 7.dp, bottomPadding: Dp = 27.dp,
    normalizeFocusRequest: Boolean = false): CardVisibility {
    val request = remember { BringIntoViewRequester() }
    var focused by remember { mutableStateOf(false) }
    var clipped by remember { mutableStateOf(false) }
    var size by remember { mutableStateOf(IntSize.Zero) }
    val edge = with(LocalDensity.current) { horizontalPadding.toPx() }
    val top = with(LocalDensity.current) { topPadding.toPx() }
    val bottom = with(LocalDensity.current) { bottomPadding.toPx() }
    val context = with(LocalDensity.current) { contextAbove.toPx() }
    val viewport = with(LocalDensity.current) { viewportHeight.toPx() }
    val reveal = RevealBounds(edge, top, bottom, context, viewport, frameShelf)
    LaunchedEffect(focused, context, viewport, frameShelf, edge, top, bottom) {
        if (focused) {
            // Becoming visible is an outcome of this scroll, not a new destination. In particular,
            // the card becomes unclipped before its bottom safety margin has finished moving.
            // Collect sequentially: a resize is rechecked after the current request, without
            // cancelling and restarting its motion. A new focus/viewport still cancels promptly.
            snapshotFlow { size to clipped }.collect { (measured, _) ->
                withFrameNanos { }
                request.bringIntoView(reveal.rect(measured))
            }
        }
    }
    val firstRequest = if (normalizeFocusRequest) Modifier.then(CardRevealElement(reveal)) else Modifier
    return CardVisibility(firstRequest.bringIntoViewRequester(request).onGloballyPositioned { coordinates ->
        val visible = coordinates.boundsInRoot()
        size = coordinates.size
        // Recheck after a viewport change without restarting relocation on every scroll pixel.
        clipped = visible.height + 1f < coordinates.size.height || visible.width + 1f < coordinates.size.width
    }, Modifier.onFocusChanged { focused = it.hasFocus })
}

private data class RevealBounds(val edge: Float, val top: Float, val bottom: Float, val context: Float,
    val viewport: Float, val frameShelf: Boolean) {
    fun rect(size: IntSize): Rect {
        val canFrameShelf = context > 0 && size.height + top + context + bottom <= viewport
        val requestedTop = if (canFrameShelf) top + context else top
        // A viewport-sized rectangle anchors a fitting shelf. Oversized content reveals the tile itself.
        val requestedBottom = if (canFrameShelf && frameShelf) viewport - requestedTop else size.height + bottom
        return Rect(-edge, -requestedTop, size.width + edge, requestedBottom)
    }
}

private data class CardRevealElement(val bounds: RevealBounds) : ModifierNodeElement<CardRevealNode>() {
    override fun create() = CardRevealNode(bounds)
    override fun update(node: CardRevealNode) { node.bounds = bounds }
    override fun InspectorInfo.inspectableProperties() { name = "cardRevealBounds" }
}

/** Focusable's immediate request must have the same target as the later resize/visibility check. */
private class CardRevealNode(var bounds: RevealBounds) : Modifier.Node(), BringIntoViewModifierNode {
    override suspend fun bringIntoView(childCoordinates: LayoutCoordinates, boundsProvider: () -> Rect?) {
        if (!isAttached || !childCoordinates.isAttached || boundsProvider() == null) return
        // Use current layout, including on the first beyond-bounds focus search before Compose state is updated.
        // Forward directly to the ancestor; keep the caller's cancellation and never re-enter our requester.
        bringNodeIntoView {
            if (isAttached) bounds.rect(requireLayoutCoordinates().size) else null
        }
    }
}
