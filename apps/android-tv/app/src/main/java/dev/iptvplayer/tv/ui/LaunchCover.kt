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
import androidx.compose.ui.draw.drawWithCache
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import dev.iptvplayer.tv.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
import kotlin.math.abs

/**
 * TV has no platform custom SplashScreen animation, so the launch window shows the static mark and this cover plays the
 * Antenio splash over the content (D062): a black stage, two mint corner glows, the folded play mark popping on the
 * startup chime's hit and the pixel "Antenio" wordmark cascading in. Laid out on the 1920×1080 handoff, scaled to the
 * screen. It stays at least [MinShowMs]; while saved sources are still restored it holds with a spinner and
 * "Hazırlanıyor...", then fades into the content. A new remote press removes it at once (MainActivity). Reduced motion or
 * disabled system animations skip the entrance: the finished logo stays, still, only while sources are restored (D069).
 */
@Composable internal fun LaunchCover(reducedMotion: Boolean, loading: Boolean,
    prepareContent: () -> Unit = {}, entranceStarted: () -> Unit = {}, finish: () -> Unit) {
    val still = remember { reducedMotion || !android.animation.ValueAnimator.areAnimatorsEnabled() }
    // One linear master clock; every item eases its own window of it via segment() so the cascade stays in lockstep.
    val intro = remember { Animatable(if (still) 1f else 0f) }
    val alpha = remember { Animatable(1f) }
    val done by rememberUpdatedState(finish)
    val prepare by rememberUpdatedState(prepareContent)
    val busy by rememberUpdatedState(loading)
    val started by rememberUpdatedState(entranceStarted)
    val view = LocalView.current
    var warming by remember { mutableStateOf(!still) }
    // Still: the spinner shows at once, beside the finished logo.
    var holding by remember { mutableStateOf(still) }
    // The spinner turns unless motion is reduced in Ayarlar or the system's animations are off; then it is a fixed arc
    // (as LoadingArc, U22), never a frozen frame of the turn. RestoringCover is still for its logo only.
    val spinnerStill = LocalReducedMotion.current || !android.animation.ValueAnimator.areAnimatorsEnabled()
    LaunchedEffect(Unit) {
        if (!still) {
            // Submit the same gradients and paths before starting the clock. At alpha 1/255 they are
            // visually black, but nonzero so the renderer cannot skip their first GPU preparation.
            awaitLaunchFrame(view)
            warming = false
        } else withFrameNanos { }
        started()
        if (!still) {
            intro.animateTo(1f, tween(EntranceMs, easing = LinearEasing))
            delay((MinShowMs - EntranceMs).toLong())
        }
        holding = true
        snapshotFlow { busy }.first { !it }
        // Home's first composition/measure can be expensive for a large library. Let it happen only after the logo
        // has landed, under the opaque cover. Give the new tree a frame before starting the reveal animation.
        prepare()
        withFrameNanos { }
        withFrameNanos { }
        if (!still) alpha.animateTo(0f, tween(220))
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
        BrandGlyphs.map { g -> PathParser().parsePathString(g.data).toPath().let { ParsedGlyph(it, it.getBounds().center, g.mint) } }
    }
    BoxWithConstraints(Modifier.fillMaxSize().graphicsLayer { this.alpha = alpha.value }.background(Color.Black)) {
        Spacer(Modifier.fillMaxSize().drawWithCache {
            val span = maxOf(size.width, size.height) * .62f
            // Cache shaders by viewport size; only their draw alpha changes with the entrance clock.
            val maxGlow = 1.24f
            val bottomGlow = Brush.radialGradient(listOf(BrandMint.copy(alpha = .34f * maxGlow),
                BrandMint.copy(alpha = .11f * maxGlow), Color.Transparent),
                Offset(size.width * (-319f / RefW), size.height * (1306f / RefH)), span)
            val topGlow = Brush.radialGradient(listOf(BrandMint.copy(alpha = .30f * maxGlow),
                BrandMint.copy(alpha = .10f * maxGlow), Color.Transparent),
                Offset(size.width * (1774f / RefW), size.height * (-414f / RefH)), span)
            onDrawBehind {
                val t = intro.value
                // Corner glows stay dark through the build, arrive with the mark on the impact and bloom briefly on it.
                val glowIn = Ease.Out.transform(segment(t, Impact - .14f, Impact + .05f))
                val bloom = smoothPulse(t, Impact, .13f)
                // Still covers do not breathe: nothing moves but the spinner.
                val idle = if (still) .5f else breath.value
                val glow = glowIn * lerp(.82f, 1f, idle) * (1f + .24f * bloom)
                drawRect(bottomGlow, alpha = if (warming) WarmAlpha else glow / maxGlow)
                drawRect(topGlow, alpha = if (warming) WarmAlpha else glow / maxGlow)
            }
        })
        Canvas(Modifier.fillMaxSize()) {
            val t = if (warming) 1f else intro.value
            // Play mark + wordmark (design/brand/antenio-dikey.svg), a GroupW × GroupH box centred on the 1920×1080 stage.
            val unit = minOf(size.width / RefW, size.height / RefH)
            withTransform({
                translate((size.width - GroupW * unit) / 2f, (size.height - GroupH * unit) / 2f)
                scale(unit, unit, pivot = Offset.Zero)
            }) {
                // The mark pops on the impact with an overshoot and a small drift, then eases into the breath.
                val rise = Ease.Out.transform(segment(t, Impact - .17f, Impact + .05f))
                val markAlpha = Ease.Out.transform(segment(t, Impact - .17f, Impact - .01f))
                val pop = lerp(.6f, 1f, Ease.Back.transform(segment(t, Impact - .15f, Impact + .07f)))
                val pulse = if (still) 1f else lerp(1f, lerp(.985f, 1.02f, breath.value), Ease.InOut.transform(segment(t, .86f, 1f)))
                withTransform({
                    translate(MarkShift, lerp(-8f, 0f, rise))
                    scale(pop * pulse, pop * pulse, pivot = markCenter)
                }) { mark.forEach { drawPath(it, BrandMint.copy(alpha = if (warming) WarmAlpha else markAlpha)) } }

                // Glyphs rise left to right; alpha resolves in half the window so each reads while still settling.
                glyphs.forEachIndexed { i, g ->
                    val start = LettersStart + i * LetterStagger
                    val move = Ease.Out.transform(segment(t, start, start + LetterSpan))
                    val glyphAlpha = Ease.Out.transform(segment(t, start, start + LetterSpan * .5f))
                    val glyphScale = lerp(.82f, 1f, move)
                    withTransform({
                        translate(lerp(-12f, 0f, move), lerp(14f, 0f, move))
                        scale(glyphScale, glyphScale, pivot = g.center)
                    }) { drawPath(g.path, (if (g.mint) BrandMint else Color.White).copy(alpha = if (warming) WarmAlpha else glyphAlpha)) }
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
                SplashSpinner(ref * 56f, ref * 8f, spinnerStill)
                // The handoff's 21 px label is 10.5 sp on a 960 dp TV; it is the screen's only text, so it keeps 14 sp.
                Text(stringResource(R.string.launch_preparing), color = Color.White, fontFamily = Roboto, fontSize = (21f * ref.value).coerceAtLeast(14f).sp,
                    lineHeight = (25f * ref.value).coerceAtLeast(18f).sp, textAlign = TextAlign.Center)
            }
        }
    }
}

/** Wait for GPU submission, rather than a UI frame callback that can run before the first draw. */
private suspend fun awaitLaunchFrame(view: android.view.View) {
    val observer = view.viewTreeObserver
    if (android.os.Build.VERSION.SDK_INT >= 29 && view.isHardwareAccelerated && observer.isAlive) {
        var callback: Runnable? = null
        try {
            // A lost/inactive surface must never leave an unskippable cover. Cancellation also removes the callback.
            withTimeoutOrNull(2_000) {
                suspendCancellableCoroutine<Unit> { ready ->
                    callback = Runnable { if (ready.isActive) ready.resume(Unit) }
                    observer.registerFrameCommitCallback(callback!!)
                    view.postInvalidateOnAnimation()
                }
            }
        } finally {
            if (observer.isAlive) callback?.let(observer::unregisterFrameCommitCallback)
        }
    } else {
        // API26–28 and software rendering have no frame-commit callback.
        withFrameNanos { }; withFrameNanos { }
    }
}

private const val WarmAlpha = 1f / 255f

/**
 * The Antenio cover while saved sources are restored without the launch splash (D069): a later launch in the same
 * process (the splash plays once per process, D062), a remote key during the splash, or reduced motion. The same logo,
 * still and silent, with the spinner; it leaves with the restore. The source-operation card stays for imports and saves.
 */
@Composable internal fun RestoringCover() = LaunchCover(reducedMotion = true, loading = true, finish = {})

/**
 * Round-capped mint arc that grows and shrinks while it turns; drawn here so no progress library is needed. [still]
 * draws a fixed three-quarter arc instead (reduced motion, system animations off).
 */
@Composable private fun SplashSpinner(diameter: Dp, stroke: Dp, still: Boolean) {
    if (still) {
        Canvas(Modifier.size(diameter)) {
            val w = stroke.toPx()
            drawArc(BrandMint, startAngle = -90f, sweepAngle = 270f, useCenter = false,
                topLeft = Offset(w / 2f, w / 2f), size = Size(size.width - w, size.height - w), style = Stroke(w, cap = StrokeCap.Round))
        }
        return
    }
    val phase = rememberInfiniteTransition(label = "splash-spinner").animateFloat(0f, 1f,
        infiniteRepeatable(tween(1333, easing = LinearEasing)), label = "phase")
    Canvas(Modifier.size(diameter)) {
        val p = phase.value
        // The head runs ahead in the first half, the tail catches up in the second; 420° + 300° = two full turns,
        // so the arc ends each cycle exactly where the next one starts.
        val head = 300f * Ease.InOut.transform((p * 2f).coerceAtMost(1f))
        val tail = 300f * Ease.InOut.transform((p * 2f - 1f).coerceAtLeast(0f))
        val w = stroke.toPx()
        drawArc(BrandMint, startAngle = -90f + 420f * p + tail, sweepAngle = 12f + head - tail, useCenter = false,
            topLeft = Offset(w / 2f, w / 2f), size = Size(size.width - w, size.height - w), style = Stroke(w, cap = StrokeCap.Round))
    }
}

// Brand mint of the Antenio logo, icon and banner (#1CECAB); the app's own accent stays Ink.Mint.
private val BrandMint = Color(0xFF1CECAB)

private const val RefW = 1920f
private const val RefH = 1080f
// The mark (UnionPaths, centred over the old 298-unit wordmark) moves right to centre over ANTENIO.
private const val GroupW = 429.025f
private const val MarkShift = 65.512f
private const val GroupH = 260f

// The entrance is tuned to res/raw/splash_chime: StartupSound skips its 0.4 s lead, so the impact lands ~1.34 s in,
// i.e. master t≈Impact. Move Impact and StartupSound's lead skip together.
private const val EntranceMs = 2800
private const val MinShowMs = 3000
private const val Impact = .48f
private const val LettersStart = Impact + .02f
private const val LetterStagger = .045f
private const val LetterSpan = .20f

/** Folded play mark ("Union"), in group-local coordinates of design/brand/antenio-dikey.svg. */
private val UnionPaths = listOf(
    "M124.893 92.0069L97.2701 109.541V117.313L118.55 128.935L193.454 80.7673L175.365 71.9941C168.402 68.6088 160.175 69.1155 153.679 73.3298L124.893 92.0069Z",
    "M124.893 77.4108L124.893 4.07898L118.55 0L97.2701 11.6217V109.541L116.263 94.936C121.704 90.7518 124.893 84.2762 124.893 77.4108Z",
    "M200.73 52.8459L124.893 4.07898L127.258 28.1106C127.919 34.8228 131.607 40.866 137.273 44.5217L193.454 80.7673L200.73 76.089L200.73 52.8459Z",
)

private class GlyphSpec(val data: String, val mint: Boolean)
private class ParsedGlyph(val path: Path, val center: Offset, val mint: Boolean)

/** "ANTENIO", left to right (interim pixel wordmark on the NEXTV grid, BKL-045). ANTEN is white; IO takes the mint. */
private val BrandGlyphs = listOf(
    GlyphSpec("M0 200.857L6.5801 200.857L6.5801 194.285L39.4809 194.285L39.4809 200.857L46.061 200.857L46.061 260L32.9008 260L32.9008 240.286L13.1603 240.286L13.1603 260L0 260ZM13.1603 207.428L13.1603 227.143L32.9008 227.143L32.9008 207.428Z", mint = false),
    GlyphSpec("M91.2445 240.285H84.6644V227.142H78.0844V260H64.924V194.285H84.6644V207.428H91.2445V220.571H97.8247V233.714H104.405V194.285H117.565V260H97.8247V253.428H91.2445V240.285Z", mint = false),
    GlyphSpec("M149.588 207.428H136.428V194.285H175.909V207.428H162.749V260H149.588V207.428Z", mint = false),
    GlyphSpec("M207.932 246.857H234.253V260H194.772V194.285H234.253V207.428H207.932V220.571H227.673V233.714H207.932V246.857Z", mint = false),
    GlyphSpec("M279.436 240.285H272.856V227.142H266.276V260H253.116V194.285H272.856V207.428H279.436V220.571H286.016V233.714H292.596V194.285H305.757V260H286.016V253.428H279.436V240.285Z", mint = false),
    GlyphSpec("M324.62 194.285L364.101 194.285L364.101 207.428L350.94 207.428L350.94 246.857L364.101 246.857L364.101 260L324.62 260L324.62 246.857L337.78 246.857L337.78 207.428L324.62 207.428Z", mint = true),
    GlyphSpec("M382.964 200.857L389.544 200.857L389.544 194.285L422.445 194.285L422.445 200.857L429.025 200.857L429.025 253.429L422.445 253.429L422.445 260L389.544 260L389.544 253.429L382.964 253.429ZM396.124 207.428L396.124 246.857L415.865 246.857L415.865 207.428Z", mint = true),
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
