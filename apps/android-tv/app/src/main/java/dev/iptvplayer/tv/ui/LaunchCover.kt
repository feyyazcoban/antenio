package dev.iptvplayer.tv.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.iptvplayer.tv.R

/** TV has no platform custom SplashScreen animation. Match its launch window, then reveal content. */
@Composable internal fun LaunchCover(reducedMotion: Boolean, finish: () -> Unit) {
    val alpha = remember { Animatable(1f) }
    val done by rememberUpdatedState(finish)
    LaunchedEffect(Unit) {
        withFrameNanos { }
        if (!reducedMotion && android.animation.ValueAnimator.areAnimatorsEnabled()) alpha.animateTo(0f, tween(220))
        done()
    }
    Box(Modifier.fillMaxSize().graphicsLayer { this.alpha = alpha.value }.background(Ink.Background), contentAlignment = Alignment.Center) {
        Image(painterResource(R.drawable.splash_mark), contentDescription = null, modifier = Modifier.size(288.dp))
    }
}
