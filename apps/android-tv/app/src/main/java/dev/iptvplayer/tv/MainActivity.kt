package dev.iptvplayer.tv

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import coil3.ImageLoader
import coil3.SingletonImageLoader
import dev.iptvplayer.tv.data.ArtworkCache
import dev.iptvplayer.tv.ui.PilotApp
import dev.iptvplayer.tv.ui.PilotTheme

@androidx.annotation.OptIn(UnstableApi::class)
class TvApplication : Application(), SingletonImageLoader.Factory {
    private val openingShown = java.util.concurrent.atomic.AtomicBoolean(false)
    fun claimOpening(): Boolean = openingShown.compareAndSet(false, true)
    override fun onCreate() {
        super.onCreate()
        // Provider exceptions may contain signed URLs. Never forward raw media logs.
        Log.setLogLevel(Log.LOG_LEVEL_OFF)
    }
    internal val artworkCache by lazy { ArtworkCache(this) }
    override fun newImageLoader(context: android.content.Context): ImageLoader = artworkCache.loader
}

class MainActivity : ComponentActivity() {
    private val model: TvViewModel by viewModels()
    private val startupSound by lazy { StartupSound(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) startupSound.prepare()
        val opening = savedInstanceState == null && (application as TvApplication).claimOpening()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        setContent { PilotTheme { PilotApp(model, opening) } }
    }
    override fun onStart() { super.onStart(); startupSound.start() }
    override fun onStop() { startupSound.stop(); super.onStop() }
    override fun onDestroy() { startupSound.stop(); super.onDestroy() }
}
