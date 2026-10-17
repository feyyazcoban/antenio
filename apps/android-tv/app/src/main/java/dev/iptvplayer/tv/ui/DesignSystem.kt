package dev.iptvplayer.tv.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border

/** Common 960×540 dp TV geometry, shared across application screens. */
object TvLayout {
    val Gutter = 24.dp
    val PageTop = 0.dp
    val Gap = 20.dp
    val RowGap = 20.dp
    val Focus = 2.dp
    val FocusInset = 3.dp
    val CardRadius = 12.dp
    val HeaderTop = 18.dp
    val HeaderBottom = 14.dp
    /** Side menu: icon rail at rest, labels over the content while it has focus. */
    val RailWidth = 72.dp
    val RailOpenWidth = 208.dp
    /** The home hero (D067): about two thirds of the 540 dp screen, so the first shelf still shows below it. */
    val HeroHeight = 372.dp
    val HeroRadius = 20.dp
    val LandscapeWidth = 196.dp
    val PosterWidth = 136.dp
    val ContinueWidth = 284.dp
    val LiveWidth = 168.dp
}

/**
 * The focus ring of a surface with [radius] corners, [TvLayout.FocusInset] outside it. tv-material strokes the
 * surface's own shape on the grown bounds, so a fixed radius stays the same and the gap widens at the corners;
 * radius + inset keeps the ring concentric with the fill. Pill (percent) shapes already grow with the bounds.
 */
fun focusRing(radius: Dp, color: Color = Ink.Text) =
    Border(BorderStroke(TvLayout.Focus, color), inset = TvLayout.FocusInset, shape = RoundedCornerShape(radius + TvLayout.FocusInset))

/** A turning mint arc while something opens (U22); still under reduced motion. Read only while drawing. */
@Composable internal fun LoadingArc(modifier: Modifier = Modifier, color: Color = Ink.Mint) {
    val still = LocalReducedMotion.current
    val turn = if (still) null else rememberInfiniteTransition(label = "loading")
        .animateFloat(0f, 360f, infiniteRepeatable(tween(900, easing = LinearEasing)), label = "turn")
    Canvas(modifier.size(18.dp)) {
        rotate(turn?.value ?: 0f) {
            drawArc(color, 0f, 270f, false, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
        }
    }
}

/** The NEXTV logo (D066): the horizontal logo in headings, at [height]. */
@Composable fun NextvLogo(modifier: Modifier = Modifier, height: Dp = 24.dp) {
    androidx.compose.foundation.Image(androidx.compose.ui.res.painterResource(dev.iptvplayer.tv.R.drawable.nextv_logo), "NEXTV",
        modifier.size(width = height * (460f / 130f), height = height))
}

// Reuses the stroke icon paths from prototypes/tv-flow/app.js.
private val iconPaths = mapOf(
    "home" to "M3 10 12 3l9 7v10a1 1 0 0 1-1 1h-5v-7H9v7H4a1 1 0 0 1-1-1Z",
    "tv" to "M4 5h16a2 2 0 0 1 2 2v11a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V7a2 2 0 0 1 2-2ZM8 23h8M8 1l4 4 4-4",
    "film" to "M3 3h18v18H3ZM7 3v18M17 3v18M3 8h4M3 16h4M17 8h4M17 16h4",
    "series" to "M6 3h12M4 6h16M2 9h20v12H2Z",
    "star" to "m12 3 2.8 5.7 6.2.9-4.5 4.4 1.1 6.2L12 17.3l-5.6 2.9 1.1-6.2L3 9.6l6.2-.9Z",
    "settings" to "M12 8a4 4 0 1 0 0 8 4 4 0 0 0 0-8ZM9 3h6l1 3 3 1 2 5-2 5-3 1-1 3H9l-1-3-3-1-2-5 2-5 3-1Z",
    "play" to "m8 4 12 8-12 8Z",
    "back" to "m14 5-7 7 7 7M7 12h14",
    "sound" to "m3 9 5 0 5-5v16l-5-5H3Zm14-2c3 3 3 7 0 10m3-13c5 5 5 11 0 16",
    "search" to "M10 3a7 7 0 1 0 0 14 7 7 0 0 0 0-14Zm5 12 6 6",
    "guide" to "M3 4h18v16H3ZM3 9h18M3 14h18M8 4v16M15 9v5",
)

@Composable fun LineIcon(name: String, color: Color, modifier: Modifier = Modifier, filled: Boolean = false) {
    val path = remember(name) { PathParser().parsePathString(iconPaths[name] ?: iconPaths.getValue("tv")).toPath() }
    Canvas(modifier.size(16.dp)) {
        scale(size.width / 24f, size.height / 24f, pivot = androidx.compose.ui.geometry.Offset.Zero) {
            drawPath(path, color, style = if (filled) Fill else Stroke(1.6f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }
}

/**
 * Fades scrolled content into the solid [surface] behind it at a viewport edge, so a row cut by
 * the edge never rests as half a line of text. Draw only: layout, focus and semantics are unchanged.
 * Keep each fade inside the margin the focused item reserves (CardVisibility keeps 7/27 dp).
 */
internal fun Modifier.scrollEdgeFade(surface: Color, top: () -> Boolean, bottom: () -> Boolean,
    topFade: Dp = 16.dp, bottomFade: Dp = 16.dp, hold: Float = 0f): Modifier = drawWithContent {
    drawContent()
    // A transparent stop of the same colour avoids the dark band of a fade towards transparent black.
    val clear = surface.copy(alpha = 0f)
    if (top()) {
        val height = topFade.toPx()
        drawRect(Brush.verticalGradient(0f to surface, hold to surface, 1f to clear, endY = height),
            size = Size(size.width, height))
    }
    if (bottom()) {
        val height = bottomFade.toPx()
        drawRect(Brush.verticalGradient(0f to clear, 1f - hold to surface, 1f to surface,
            startY = size.height - height, endY = size.height),
            topLeft = Offset(0f, size.height - height), size = Size(size.width, height))
    }
}

/** The same edge treatment over images: masks the content's alpha, so it needs its own layer. */
internal fun Modifier.scrollEdgeMask(top: () -> Boolean, topFade: Dp = 24.dp): Modifier =
    graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }.drawWithContent {
        drawContent()
        if (top()) drawRect(Brush.verticalGradient(0f to Color.Transparent, 1f to Color.Black, endY = topFade.toPx()),
            size = Size(size.width, topFade.toPx()), blendMode = BlendMode.DstIn)
    }
