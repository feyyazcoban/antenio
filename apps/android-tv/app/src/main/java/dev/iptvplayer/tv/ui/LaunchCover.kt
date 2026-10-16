package dev.iptvplayer.tv.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlin.math.abs

/**
 * TV has no platform custom SplashScreen animation, so the launch window shows the static mark and this cover plays the
 * NEXTV splash over the content (D062): a black stage, two mint corner glows, the folded play mark popping on the
 * startup chime's hit and the pixel "NEXTV" wordmark cascading in. Laid out on the 1920×1080 handoff, scaled to the
 * screen. It stays at least [MinShowMs]; while saved sources are still restored it holds with a spinner and
 * "Hazırlanıyor...", then fades into the content. Any remote key removes it at once (PilotApp). Reduced motion or
 * disabled system animations skip the entrance and remove it after the first frame, as before.
 */
@Composable internal fun LaunchCover(reducedMotion: Boolean, loading: Boolean, finish: () -> Unit) {
    val still = remember { reducedMotion || !android.animation.ValueAnimator.areAnimatorsEnabled() }
    // One linear master clock; every item eases its own window of it via segment() so the cascade stays in lockstep.
    val intro = remember { Animatable(if (still) 1f else 0f) }
    val alpha = remember { Animatable(1f) }
    val done by rememberUpdatedState(finish)
    val busy by rememberUpdatedState(loading)
    var holding by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        withFrameNanos { }
        if (still) { done(); return@LaunchedEffect }
        intro.animateTo(1f, tween(EntranceMs, easing = LinearEasing))
        delay((MinShowMs - EntranceMs).toLong())
        holding = true
        snapshotFlow { busy }.first { !it }
        alpha.animateTo(0f, tween(220))
        done()
    }
    // Idle breath of the glows and the mark once everything has landed; read only while drawing.
    val breath = rememberInfiniteTransition(label = "splash-idle").animateFloat(0f, 1f,
        infiniteRepeatable(tween(2400, easing = Ease.InOut), RepeatMode.Reverse), label = "breath")
    val mark = remember { UnionPaths.map { PathParser().parsePathString(it).toPath() } }
    val markCenter = remember(mark) {
        val b = mark.map { it.getBounds() }
        Offset((b.minOf { it.left } + b.maxOf { it.right }) / 2f, (b.minOf { it.top } + b.maxOf { it.bottom }) / 2f)
    }
    val glyphs = remember {
        NextvGlyphs.map { g -> PathParser().parsePathString(g.data).toPath().let { ParsedGlyph(it, it.getBounds().center, g.mint) } }
    }
    BoxWithConstraints(Modifier.fillMaxSize().graphicsLayer { this.alpha = alpha.value }.background(Color.Black)) {
        Canvas(Modifier.fillMaxSize()) {
            val t = intro.value
            // Corner glows stay dark through the build, arrive with the mark on the impact and bloom briefly on it.
            val glowIn = Ease.Out.transform(segment(t, Impact - .14f, Impact + .05f))
            val bloom = smoothPulse(t, Impact, .13f)
            val glow = glowIn * lerp(.82f, 1f, breath.value) * (1f + .24f * bloom)
            val span = maxOf(size.width, size.height) * .62f
            drawRect(Brush.radialGradient(listOf(NextvMint.copy(alpha = .34f * glow), NextvMint.copy(alpha = .11f * glow),
                Color.Transparent), Offset(size.width * (-319f / RefW), size.height * (1306f / RefH)), span))
            drawRect(Brush.radialGradient(listOf(NextvMint.copy(alpha = .30f * glow), NextvMint.copy(alpha = .10f * glow),
                Color.Transparent), Offset(size.width * (1774f / RefW), size.height * (-414f / RefH)), span))

            // Play mark + wordmark, authored in a GroupW × GroupH box centred on the 1920×1080 stage.
            val unit = minOf(size.width / RefW, size.height / RefH)
            withTransform({
                translate((size.width - GroupW * unit) / 2f, (size.height - GroupH * unit) / 2f)
                scale(unit, unit, pivot = Offset.Zero)
            }) {
                // The mark pops on the impact with an overshoot and a small drift, then eases into the breath.
                val rise = Ease.Out.transform(segment(t, Impact - .17f, Impact + .05f))
                val markAlpha = Ease.Out.transform(segment(t, Impact - .17f, Impact - .01f))
                val pop = lerp(.6f, 1f, Ease.Back.transform(segment(t, Impact - .15f, Impact + .07f)))
                val pulse = lerp(1f, lerp(.985f, 1.02f, breath.value), Ease.InOut.transform(segment(t, .86f, 1f)))
                withTransform({
                    translate(0f, lerp(-8f, 0f, rise))
                    scale(pop * pulse, pop * pulse, pivot = markCenter)
                }) { mark.forEach { drawPath(it, NextvMint.copy(alpha = markAlpha)) } }

                // Glyphs rise left to right; alpha resolves in half the window so each reads while still settling.
                glyphs.forEachIndexed { i, g ->
                    val start = LettersStart + i * LetterStagger
                    val move = Ease.Out.transform(segment(t, start, start + LetterSpan))
                    val glyphAlpha = Ease.Out.transform(segment(t, start, start + LetterSpan * .5f))
                    val glyphScale = lerp(.82f, 1f, move)
                    withTransform({
                        translate(lerp(-12f, 0f, move), lerp(14f, 0f, move))
                        scale(glyphScale, glyphScale, pivot = g.center)
                    }) { drawPath(g.path, (if (g.mint) NextvMint else Color.White).copy(alpha = glyphAlpha)) }
                }
            }
        }
        // Restore still running after the entrance: spinner centred at y=884 and label at y=946 of the handoff.
        val ref = minOf(maxWidth / RefW, maxHeight / RefH)
        AnimatedVisibility(holding && loading, Modifier.align(Alignment.TopCenter),
            enter = fadeIn(tween(320, easing = Ease.InOut)), exit = fadeOut(tween(180, easing = LinearEasing))) {
            Column(Modifier.padding(top = maxHeight / 2 + ref * (856f - RefH / 2)).testTag("launch-loading").semantics {
                    liveRegion = LiveRegionMode.Polite
                    progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate
                }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(ref * 34f)) {
                SplashSpinner(ref * 56f, ref * 8f)
                Text("Hazırlanıyor...", color = Color.White, fontFamily = Roboto, fontSize = (21f * ref.value).sp,
                    lineHeight = (25f * ref.value).sp, textAlign = TextAlign.Center)
            }
        }
    }
}

/** Round-capped mint arc that grows and shrinks while it turns; drawn here so no progress library is needed. */
@Composable private fun SplashSpinner(diameter: Dp, stroke: Dp) {
    val phase = rememberInfiniteTransition(label = "splash-spinner").animateFloat(0f, 1f,
        infiniteRepeatable(tween(1333, easing = LinearEasing)), label = "phase")
    Canvas(Modifier.size(diameter)) {
        val p = phase.value
        // The head runs ahead in the first half, the tail catches up in the second; 420° + 300° = two full turns,
        // so the arc ends each cycle exactly where the next one starts.
        val head = 300f * Ease.InOut.transform((p * 2f).coerceAtMost(1f))
        val tail = 300f * Ease.InOut.transform((p * 2f - 1f).coerceAtLeast(0f))
        val w = stroke.toPx()
        drawArc(NextvMint, startAngle = -90f + 420f * p + tail, sweepAngle = 12f + head - tail, useCenter = false,
            topLeft = Offset(w / 2f, w / 2f), size = Size(size.width - w, size.height - w), style = Stroke(w, cap = StrokeCap.Round))
    }
}

// Brand mint of the NEXTV logo, icon and banner (#1CECAB); the app's own accent stays Ink.Mint.
private val NextvMint = Color(0xFF1CECAB)

private const val RefW = 1920f
private const val RefH = 1080f
private const val GroupW = 298f
private const val GroupH = 260.341f

// The entrance is tuned to res/raw/splash_chime: StartupSound skips its 0.4 s lead, so the impact lands ~1.34 s in,
// i.e. master t≈Impact. Move Impact and StartupSound's lead skip together.
private const val EntranceMs = 2800
private const val MinShowMs = 3000
private const val Impact = .48f
private const val LettersStart = Impact + .02f
private const val LetterStagger = .045f
private const val LetterSpan = .20f

/** Folded play mark ("Union"), in group-local coordinates. */
private val UnionPaths = listOf(
    "M124.893 92.1276L97.2701 109.685V117.467L118.55 129.104L193.454 80.8733L175.365 72.0886C168.402 68.6989 160.175 69.2062 153.679 73.4261L124.893 92.1276Z",
    "M124.893 77.5124L124.893 4.08433L118.55 0L97.2701 11.6369V109.685L116.263 95.0606C121.704 90.8709 124.893 84.3868 124.893 77.5124Z",
    "M200.73 52.9153L124.893 4.08433L127.258 28.1475C127.919 34.8685 131.607 40.9196 137.273 44.5801L193.454 80.8733L200.73 76.1888L200.73 52.9153Z",
)

private class GlyphSpec(val data: String, val mint: Boolean)
private class ParsedGlyph(val path: Path, val center: Offset, val mint: Boolean)

/** "NEXTV", left to right. N/E/X are white; T/V take the mint. */
private val NextvGlyphs = listOf(
    GlyphSpec("M26.3205 240.601H19.7404V227.441H13.1603V260.341H0V194.54H19.7404V207.7H26.3205V220.86H32.9007V234.021H39.4808V194.54H52.6411V260.341H32.9007V253.761H26.3205V240.601Z", mint = false),
    GlyphSpec("M84.6987 247.181H111.019V260.341H71.5384V194.54H111.019V207.7H84.6987V220.861H104.439V234.021H84.6987V247.181Z", mint = false),
    GlyphSpec("M129.882 234.021H136.462V227.441H143.042V220.861H136.462V214.28H129.882V194.54H143.042V207.7H149.623V214.28H156.203V207.7H162.783V194.54H175.943V214.28H169.363V220.861H162.783V227.441H169.363V234.021H175.943V260.341H162.783V240.601H156.203V234.021H149.623V240.601H143.042V260.341H129.882V234.021Z", mint = false),
    GlyphSpec("M206.755 207.7H193.595V194.54H233.076V207.7H219.916V260.341H206.755V207.7Z", mint = true),
    GlyphSpec("M278.259 234.021H284.84V194.54H298V240.601H291.42V247.181H284.84V253.761H278.259V260.341H271.679V253.761H265.099V247.181H258.519V240.601H251.939V194.54H265.099V234.021H271.679V240.601H278.259V234.021Z", mint = true),
)

/** Carve a normalised [start, end] window out of master progress [t]. */
private fun segment(t: Float, start: Float, end: Float): Float = ((t - start) / (end - start)).coerceIn(0f, 1f)

private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t

/** Smoothstepped triangular pulse: 1 at [center], 0 beyond [halfWidth]. */
private fun smoothPulse(t: Float, center: Float, halfWidth: Float): Float {
    val x = (1f - abs(t - center) / halfWidth).coerceIn(0f, 1f)
    return x * x * (3f - 2f * x)
}

private object Ease {
    val Out = CubicBezierEasing(.22f, 1f, .36f, 1f)
    val InOut = CubicBezierEasing(.42f, 0f, .58f, 1f)
    val Back = CubicBezierEasing(.34f, 1.42f, .5f, 1f)
}
