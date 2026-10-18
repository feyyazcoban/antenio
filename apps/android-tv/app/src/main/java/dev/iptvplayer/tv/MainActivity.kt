package dev.iptvplayer.tv

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
    private var opening by mutableStateOf(false)
    private val openingInput = OpeningInput()
    // Android 12 and older: the language chosen in Settings (LocaleController); Android 13 applies it on its own.
    override fun attachBaseContext(newBase: Context) { super.attachBaseContext(LocaleController.wrap(newBase)) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Before the first frame: dates, numbers and title orders follow the language the texts resolved to. The model
        // outlives a new language, so it sorts Library's A–Z again for its alphabet before Library asks.
        if (LocaleController.update(this)) model.languageChanged()
        if (savedInstanceState == null) startupSound.prepare()
        opening = savedInstanceState == null && (application as TvApplication).claimOpening()
        // Use the public window callback: ComponentActivity's own key-dispatch override is library-restricted.
        // A skip press/repeat/release cannot activate the newly composed screen. Other keys dispatch normally;
        // an orphan UP from the launcher cannot skip the entrance.
        val callback = window.callback
        window.callback = object : Window.Callback by callback {
            override fun dispatchKeyEvent(event: KeyEvent): Boolean {
                if (openingInput.consume(event.keyCode, event.action, opening) {
                    opening = false
                    startupSound.stop()
                }) return true
                return callback.dispatchKeyEvent(event)
            }
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        setContent { PilotTheme { PilotApp(model, opening) { opening = false } } }
    }
    override fun onStart() { super.onStart(); startupSound.start(); model.appStarted() }
    // A configuration change (a keyboard plugged in, another language) recreates the activity while the viewer stays:
    // nothing locks again, and a source form being filled in stays open.
    override fun onStop() { openingInput.clear(); startupSound.stop(); if (!isChangingConfigurations) model.appStopped(); super.onStop() }
    override fun onDestroy() { startupSound.stop(); super.onDestroy() }
}
