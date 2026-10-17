@file:androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)

package dev.iptvplayer.tv.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.BringIntoViewSpec
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.*
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import androidx.tv.material3.Text
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Border
import androidx.compose.foundation.BorderStroke
import dev.iptvplayer.tv.PlayingMedia
import dev.iptvplayer.tv.Route
import dev.iptvplayer.tv.BuildConfig
import dev.iptvplayer.tv.TvViewModel
import dev.iptvplayer.tv.data.MediaKind
import dev.iptvplayer.tv.data.EpisodeSequence
import dev.iptvplayer.tv.data.displayTitle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable internal fun PlayerScreen(media: PlayingMedia, model: TvViewModel, back: () -> Unit) {
    val context = LocalContext.current
    val controlEdge = with(LocalDensity.current) { 12.dp.toPx() }
    val controlVisibility = remember(controlEdge) { object : BringIntoViewSpec {
        override fun calculateScrollDistance(offset: Float, size: Float, containerSize: Float): Float = when {
            offset < controlEdge -> offset - controlEdge
            offset + size > containerSize - controlEdge -> offset + size - containerSize + controlEdge
            else -> 0f
        }
    } }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val screenState by model.state.collectAsStateWithLifecycle()
    val guideNow = guideClock()
    val guideEntries = remember(media.entry) { if (media.entry.kind == MediaKind.LIVE) listOf(media.entry) else emptyList() }
    val schedule = guideRows(guideEntries, dev.iptvplayer.tv.data.GuideWindow(guideNow, guideNow + 12 * 3_600_000L), screenState, model)[media.entry.id]
    val currentProgramme = schedule?.now(guideNow)
    val player = remember { ExoPlayer.Builder(context).build().apply {
        setAudioAttributes(AudioAttributes.DEFAULT, true)
        setHandleAudioBecomingNoisy(true)
        trackSelectionParameters = trackSelectionParameters.buildUpon()
            .setPreferredAudioLanguage(model.store.audioLanguage)
            .setPreferredTextLanguage(model.store.subtitleLanguage)
            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, model.store.subtitlesOff).build()
    } }
    val scope = rememberCoroutineScope()
    val recovery = remember(player) { PlaybackRecovery(player, scope) }
    val recoveryState by recovery.state.collectAsStateWithLifecycle()
    val failure = recoveryState.failure
    var active by remember { mutableStateOf<PlayingMedia?>(null) }
    var visible by remember { mutableStateOf(true) }
    var trackPanel by remember { mutableStateOf(false) }
    var tracks by remember { mutableStateOf(Tracks.EMPTY) }
    var trackParameters by remember { mutableStateOf(player.trackSelectionParameters) }
    var status by remember { mutableIntStateOf(Player.STATE_IDLE) }
    var playing by remember { mutableStateOf(false) }
    var hasPlayed by remember { mutableStateOf(false) }
    var position by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var lastInteraction by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val first = remember { FocusRequester() }
    val canvas = remember { FocusRequester() }
    val live = media.entry.kind == MediaKind.LIVE
    val favoriteTarget = remember(media.entry, screenState.catalog, screenState.selected, screenState.episodes, screenState.history) {
        model.favoriteTarget(media.entry)
    }
    val isFavorite = favoriteTarget?.id in screenState.favoriteIds
    fun save() { active?.let { model.saveProgress(it.entry, player.currentPosition.coerceAtLeast(0), player.duration.coerceAtLeast(0)) } }
    fun interact() { lastInteraction = System.currentTimeMillis(); visible = true }
    val episodeEnd = remember(player) {
        EpisodeEndController(scope) { sourceId, currentId, nextId, automatic ->
            save(); model.playNextEpisode(sourceId, currentId, nextId, automatic)
        }
    }
    val endState by episodeEnd.state.collectAsStateWithLifecycle()
    val nextChoice = remember(media.entry, screenState.sources, screenState.episodes) {
        screenState.sourceFor(media.entry)?.let { EpisodeSequence.next(it, media.entry, screenState.episodes) }
    }
    val opening = failure == null && !recoveryState.recovering && (status == Player.STATE_BUFFERING || status == Player.STATE_IDLE)
    val controlsShown = visible && !trackPanel && endState == null
    // Remote shortcuts while the controls are hidden: the next channel opens without bringing them up, and a short
    // note in the corner says what happened (the channel, a seek, the number being typed).
    var quiet by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf<Pair<String, String?>?>(null) }
    var noteAt by remember { mutableLongStateOf(0L) }
    var digits by remember { mutableStateOf("") }
    fun flash(text: String, detail: String? = null) { note = text to detail; noteAt = System.currentTimeMillis() }
    fun zap(direction: Int) { quiet = !visible; digits = ""; model.changeChannel(direction) }
    fun seekBy(deltaMs: Long) {
        if (!player.isCurrentMediaItemSeekable) return
        val end = player.duration.takeIf { it > 0 } ?: Long.MAX_VALUE
        val target = (player.currentPosition + deltaMs).coerceIn(0, end)
        player.seekTo(target); position = target
        flash("${if (deltaMs < 0) "−" else "+"}${kotlin.math.abs(deltaMs) / 1000} sn", "${clockTime(target)} / ${if (duration > 0) clockTime(duration) else "—"}")
    }
    LaunchedEffect(noteAt) { if (note != null) { delay(2_500); note = null } }
    LaunchedEffect(digits) {
        if (digits.isEmpty()) return@LaunchedEffect
        delay(1_500)
        val number = digits.toInt(); digits = ""
        quiet = !visible
        if (!model.playChannelNumber(number)) { quiet = false; flash("$number numaralı kanal yok") }
    }

    DisposableEffect(player, lifecycle) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                status = playbackState
                if (playbackState == Player.STATE_ENDED) {
                    visible = true; trackPanel = false
                    position = player.currentPosition.coerceAtLeast(0); duration = player.duration.coerceAtLeast(0)
                    save()
                    val current = active?.entry
                    val s = model.state.value
                    if (hasPlayed && current != null && s.playing?.entry?.id == current.id && s.sourceFor(current) != null)
                        episodeEnd.ended(current.sourceId, current, model.nextEpisodeChoice(), s.autoNextEpisode)
                }
            }
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                playing = isPlaying
                if (isPlaying) { hasPlayed = true; active?.entry?.let { model.recordViewed(it) } }
            }
            override fun onTracksChanged(value: Tracks) { tracks = value }
            override fun onTrackSelectionParametersChanged(parameters: TrackSelectionParameters) { trackParameters = parameters }
        }
        var resumeOnStart = false
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    resumeOnStart = player.playWhenReady && player.playbackState == Player.STATE_READY
                    save(); recovery.background(); episodeEnd.background()
                }
                Lifecycle.Event.ON_START -> { recovery.foreground(); episodeEnd.foreground(); if (resumeOnStart) player.play() }
                else -> Unit
            }
        }
        player.addListener(listener)
        lifecycle.addObserver(observer)
        onDispose { save(); recovery.close(); episodeEnd.close(); lifecycle.removeObserver(observer); player.removeListener(listener); player.release() }
    }

    LaunchedEffect(media) {
        save()
        // Stop the old request before opening a new channel: one source session at a time.
        player.stop(); player.clearMediaItems()
        recovery.newMedia(media.entry.kind == MediaKind.LIVE)
        episodeEnd.newMedia(); hasPlayed = false
        tracks = Tracks.EMPTY; position = media.startMs; duration = 0
        trackPanel = false; active = media
        if (quiet) {
            quiet = false; lastInteraction = System.currentTimeMillis()
            flash(listOfNotNull(model.channelNumber(media.entry)?.toString(), displayTitle(media.entry.title).name).joinToString("  "))
        } else interact()
        player.trackSelectionParameters = player.trackSelectionParameters.buildUpon().clearOverrides()
            .setPreferredAudioLanguage(model.store.audioLanguage)
            .setPreferredTextLanguage(model.store.subtitleLanguage)
            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, model.store.subtitlesOff).build()
        val http = DefaultHttpDataSource.Factory().setUserAgent("NEXTV/${BuildConfig.VERSION_NAME} AndroidTV")
            .setConnectTimeoutMs(8_000).setReadTimeoutMs(8_000)
            .setDefaultRequestProperties(media.entry.headers)
        val factory = DefaultMediaSourceFactory(DefaultDataSource.Factory(context, http))
            .setLoadErrorHandlingPolicy(SingleAttemptLoadPolicy())
        player.setMediaSource(factory.createMediaSource(MediaItem.fromUri(media.url)), media.startMs)
        player.prepare(); player.play()
    }
    LaunchedEffect(player) {
        var ticks = 0
        while (true) {
            delay(500)
            position = player.currentPosition.coerceAtLeast(0)
            duration = player.duration.coerceAtLeast(0)
            if (++ticks % 10 == 0) save()
            if (player.isPlaying && visible && !trackPanel && recovery.state.value.failure == null &&
                !recovery.state.value.recovering && System.currentTimeMillis() - lastInteraction > 8000) visible = false
        }
    }
    LaunchedEffect(visible, trackPanel, endState != null) {
        if (!trackPanel && endState == null) { withFrameNanos { }; if (visible) first.requestFocus() else canvas.requestFocus() }
    }
    LaunchedEffect(screenState.autoNextEpisode) { episodeEnd.preferenceChanged(screenState.autoNextEpisode) }
    LaunchedEffect(nextChoice) { episodeEnd.availabilityChanged(nextChoice) }
    LaunchedEffect(recoveryState.recovering, failure) {
        if (recoveryState.recovering || failure != null) { trackPanel = false; interact() }
    }
    BackHandler {
        // A Back that arrives after the model left the player, before this screen is gone, is the new route's.
        if (model.state.value.route != Route.PLAYER) return@BackHandler back()
        when {
            trackPanel -> { trackPanel = false; interact() }
            recoveryState.recovering -> { recovery.cancel(); save(); model.back() }
            visible && failure == null -> visible = false
            else -> { save(); model.back() }
        }
    }
    Box(Modifier.fillMaxSize().background(Color.Black)
        .onPreviewKeyEvent { event ->
            if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
            lastInteraction = System.currentTimeMillis()
            val hidden = !visible && !trackPanel
            val healthy = failure == null && !recoveryState.recovering
            val code = event.nativeKeyEvent.keyCode
            val digit = when (code) {
                in android.view.KeyEvent.KEYCODE_0..android.view.KeyEvent.KEYCODE_9 -> code - android.view.KeyEvent.KEYCODE_0
                in android.view.KeyEvent.KEYCODE_NUMPAD_0..android.view.KeyEvent.KEYCODE_NUMPAD_9 -> code - android.view.KeyEvent.KEYCODE_NUMPAD_0
                else -> -1
            }
            // Held Left/Right seek further the longer they are held: 10 s, then 30 s, then a minute per step.
            val repeat = event.nativeKeyEvent.repeatCount
            val step = when { repeat >= 30 -> 60_000L; repeat >= 10 -> 30_000L; else -> 10_000L }
            when {
                event.key == Key.MediaPlayPause -> {
                    if (recoveryState.recovering) recovery.cancel()
                    else if (failure != null) recovery.retryManually()
                    else if (player.isPlaying) player.pause() else player.play()
                    true
                }
                event.key == Key.MediaPlay -> { if (failure != null) recovery.retryManually() else if (!recoveryState.recovering) player.play(); true }
                event.key == Key.MediaPause -> { if (recoveryState.recovering) recovery.cancel() else player.pause(); true }
                // Live: CH+/CH− always; with the controls hidden Up/Down too (Up is the next number, as CH+).
                live && (event.key == Key.ChannelUp || event.key == Key.ChannelDown) -> { zap(if (event.key == Key.ChannelUp) 1 else -1); true }
                live && hidden && healthy && (event.key == Key.DirectionUp || event.key == Key.DirectionDown) -> {
                    zap(if (event.key == Key.DirectionUp) 1 else -1); true
                }
                // Digits pick a channel by its Canlı TV number, 1.5 s after the last one.
                live && !trackPanel && digit >= 0 -> { if (digits.length < 4) digits += digit; flash(digits); true }
                // The remote's guide key opens the programme guide on this channel.
                live && event.key == Key.Guide -> { model.guide(); true }
                !live && healthy && (event.key == Key.MediaFastForward || event.key == Key.MediaRewind) -> {
                    seekBy(if (event.key == Key.MediaFastForward) 30_000 else -10_000); true
                }
                !live && hidden && healthy && (event.key == Key.DirectionLeft || event.key == Key.DirectionRight) -> {
                    seekBy(if (event.key == Key.DirectionRight) step else -step); true
                }
                hidden && event.key in setOf(Key.DirectionCenter, Key.Enter, Key.DirectionLeft, Key.DirectionRight, Key.DirectionUp, Key.DirectionDown) -> {
                    visible = true; true
                }
                else -> false
            }
        }) {
        AndroidView(factory = { PlayerView(it).apply {
            this.player = player; useController = false; keepScreenOn = true
            isFocusable = false; descendantFocusability = android.view.ViewGroup.FOCUS_BLOCK_DESCENDANTS
            setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
        } }, modifier = Modifier.fillMaxSize(), onRelease = { it.player = null })
        Box(Modifier.fillMaxSize().focusRequester(canvas).focusable(!visible && !trackPanel))
        note?.takeIf { !controlsShown }?.let { (text, detail) ->
            Column(Modifier.align(Alignment.TopEnd).padding(top = 22.dp, end = TvLayout.Gutter).widthIn(max = 420.dp)
                .background(Ink.Background.copy(alpha = .9f), RoundedCornerShape(12.dp)).padding(horizontal = 16.dp, vertical = 10.dp)
                .testTag("player-note"), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text, color = Ink.Text, fontFamily = Roboto, fontWeight = FontWeight.Medium, fontSize = 18.sp, lineHeight = 24.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                detail?.let { Text(it, color = Ink.Muted, fontFamily = Roboto, fontSize = 13.sp, lineHeight = 18.sp, maxLines = 1) }
            }
        }
        // While the controls are up, their status line reports opening; the centred pill would sit under the panel.
        if (opening && !controlsShown) {
            Row(Modifier.align(Alignment.Center).offset { IntOffset(if (trackPanel) (-220).dp.roundToPx() else 0, 0) }
                .background(Ink.Background.copy(alpha = .9f), RoundedCornerShape(12.dp)).padding(20.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LoadingArc()
                Text("Yayın açılıyor…", color = Ink.Text, fontFamily = Roboto, fontSize = 16.sp)
            }
        }
        if (controlsShown) {
            // No panel over the picture: a scrim darkens the bottom, where the controls sit, and a lighter one keeps the
            // top label readable. The picture stays visible above the controls.
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0f to Color.Black.copy(alpha = .45f),
                .16f to Color.Transparent, .5f to Color.Transparent, .74f to Color.Black.copy(alpha = .72f), 1f to Color.Black.copy(alpha = .92f))))
            // Its own surface keeps the label readable over bright frames and burned-in video text.
            Row(Modifier.align(Alignment.TopStart).padding(start = TvLayout.Gutter, end = TvLayout.Gutter, top = 22.dp)
                .background(Ink.Background.copy(alpha = .9f), RoundedCornerShape(50)).padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LineIcon(if (live) "tv" else "film", Ink.Mint, Modifier.size(20.dp))
                Text(if (live) "CANLI TV" else if (media.entry.kind == MediaKind.EPISODE) "DİZİ" else "FİLM",
                    color = Ink.Text, fontFamily = Roboto, fontSize = 12.sp, letterSpacing = 1.4.sp)
                if (screenState.sources.size > 1) Text(screenState.sourceName(media.entry.sourceId),
                    Modifier.widthIn(max = 360.dp), color = Ink.Muted, fontFamily = Roboto, fontSize = 12.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            // The controls stand on the scrim, one title line above them; 5 dp of side room keeps the focus ring whole.
            Column(Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(start = TvLayout.Gutter, end = TvLayout.Gutter, bottom = 18.dp)
                .testTag("player-panel").padding(bottom = 2.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(remember(media.entry.title) { displayTitle(media.entry.title).name }, Modifier.fillMaxWidth().padding(horizontal = 5.dp).testTag("player-title"),
                    color = Ink.Text, fontSize = 24.sp, lineHeight = 30.sp,
                    fontWeight = FontWeight.Medium, fontFamily = Roboto, maxLines = 1, overflow = TextOverflow.Ellipsis)
                // Status on the left, the remote's shortcuts on the right.
                Row(Modifier.fillMaxWidth().padding(horizontal = 5.dp), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(when {
                        recoveryState.recovering -> recoveryState.message
                        failure != null -> failure
                        opening && live -> "Yayın açılıyor…"
                        live -> "● CANLI"
                        else -> "${clockTime(position)} / ${if (duration > 0) clockTime(duration) else "—"}" +
                            when { opening -> " · Yayın açılıyor…"; status == Player.STATE_ENDED -> " · Bitti"; else -> "" }
                    }, Modifier.weight(1f), color = if (failure != null && !recoveryState.recovering) Ink.Error else Ink.Text,
                        fontFamily = Roboto, fontSize = 14.sp, lineHeight = 19.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(when {
                        recoveryState.recovering -> "Geri: yeniden bağlanmayı durdur"
                        failure != null -> ""
                        live -> "Kontroller kapalıyken Yukarı/Aşağı: kanal · Rakam: kanal no"
                        else -> "Kontroller kapalıyken Sol/Sağ: sar"
                    }, color = Ink.Muted, fontFamily = Roboto, fontSize = 12.sp, lineHeight = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (live && failure == null && !recoveryState.recovering) {
                    Text(currentProgramme?.let { "${it.title} · ${programmeTime(it, model.guideZone())}" } ?: "Program bilgisi bulunmuyor",
                        Modifier.padding(horizontal = 5.dp).testTag("player-current-programme"), color = Ink.Text, fontFamily = Roboto,
                        fontSize = 14.sp, lineHeight = 19.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    schedule?.next(guideNow)?.let { next -> Text("Sonra · ${guideTime(next.start, model.guideZone())}  ${next.title}",
                        Modifier.padding(horizontal = 5.dp), color = Ink.Muted, fontFamily = Roboto, fontSize = 12.sp, lineHeight = 16.sp,
                        maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    currentProgramme?.end?.let { end -> Box(Modifier.padding(horizontal = 5.dp).fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(Color.White.copy(alpha = .25f))) {
                        Box(Modifier.fillMaxWidth(((guideNow - currentProgramme.start).toFloat() / (end - currentProgramme.start)).coerceIn(0f, 1f))
                            .fillMaxHeight().background(Ink.Mint))
                    } }
                }
                // Reserve the VOD progress slot so the controls do not move when the duration arrives.
                if (!live) Box(Modifier.padding(horizontal = 5.dp).fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp))
                    .background(if (duration > 0) Color.White.copy(alpha = .25f) else Color.Transparent)) {
                    if (duration > 0) Box(Modifier.fillMaxWidth((position.toFloat() / duration).coerceIn(0f, 1f)).fillMaxHeight().background(Ink.Mint))
                }
                CompositionLocalProvider(LocalBringIntoViewSpec provides controlVisibility) {
                Row(Modifier.fillMaxWidth().testTag("player-controls").horizontalScroll(rememberScrollState()).padding(horizontal = 5.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Action(if (recoveryState.recovering) "İptal et" else if (failure != null) "Tekrar dene" else if (playing) "Duraklat" else if (status == Player.STATE_ENDED) "Baştan izle" else "Oynat",
                        Modifier.focusRequester(first), primary = true) {
                        interact()
                        if (recoveryState.recovering) recovery.cancel()
                        else if (failure != null) recovery.retryManually()
                        else if (playing) player.pause()
                        else { if (status == Player.STATE_ENDED) { episodeEnd.newMedia(); player.seekTo(0) }; player.play() }
                    }
                    if (live) {
                        Action("‹ Kanal") { model.changeChannel(-1) }
                        Action("Kanal ›") { model.changeChannel(1) }
                    } else {
                        Action("−10 sn", enabled = player.isCurrentMediaItemSeekable) { interact(); player.seekTo((player.currentPosition - 10_000).coerceAtLeast(0)) }
                        Action("+10 sn", enabled = player.isCurrentMediaItemSeekable) { interact(); player.seekTo((player.currentPosition + 10_000).coerceAtMost(duration.takeIf { it > 0 } ?: Long.MAX_VALUE)) }
                    }
                    Action("Ses / Altyazı", icon = "sound", enabled = failure == null && !recoveryState.recovering) { interact(); trackPanel = true }
                    // The shared star icon at icon size, filled while it is a favourite; the meaning stays in semantics.
                    Action("", Modifier.testTag("player-favorite").semantics {
                        contentDescription = when {
                            favoriteTarget == null -> "Favori için içerik bilgisi bulunamadı"
                            media.entry.kind == MediaKind.EPISODE -> if (isFavorite) "Diziyi favorilerden çıkar" else "Diziyi favorilere ekle"
                            else -> if (isFavorite) "Favorilerden çıkar" else "Favorilere ekle"
                        }
                        stateDescription = if (isFavorite) "Favorilerde" else "Favorilerde değil"
                    }, icon = "star", iconFilled = isFavorite, selected = isFavorite, enabled = favoriteTarget != null) { model.toggleFavorite(media.entry); interact() }
                    if (nextChoice != null) Action("Sonraki bölüm") {
                        save()
                        screenState.sourceFor(media.entry)?.let { model.playNextEpisode(it.id, media.entry.id, nextChoice.entry.id) }
                    }
                    Action("Kapat") { save(); model.back() }
                }
                }
            }
        }
        if (trackPanel) TracksDialog(tracks, trackParameters, player, model) { trackPanel = false; interact() }
        endState?.let { end -> EpisodeEndDialog(end,
            play = { episodeEnd.playNow() },
            stay = { episodeEnd.dismiss(); interact() },
            returnToEpisodes = { episodeEnd.dismiss(); save(); model.back() },
            replay = { episodeEnd.newMedia(); player.seekTo(0); player.play(); interact() }) }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable internal fun TracksDialog(tracks: Tracks, parameters: TrackSelectionParameters, player: ExoPlayer, model: TvViewModel, dismiss: () -> Unit) {
    val first = remember { FocusRequester() }
    val focusEdge = with(LocalDensity.current) { 8.dp.toPx() }
    val trackVisibility = remember(focusEdge) { object : BringIntoViewSpec {
        override fun calculateScrollDistance(offset: Float, size: Float, containerSize: Float): Float = when {
            size > containerSize - 2 * focusEdge -> offset - focusEdge
            offset < focusEdge -> offset - focusEdge
            offset + size > containerSize - focusEdge -> offset + size - containerSize + focusEdge
            else -> 0f
        }
    } }
    Dialog(onDismissRequest = dismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        val window = LocalWindowInfo.current
        LaunchedEffect(Unit) {
            snapshotFlow { window.isWindowFocused }.first { it }
            first.requestFocus()
        }
        Box(Modifier.fillMaxSize().focusProperties { onEnter = { first.requestFocus() } }.focusGroup(), contentAlignment = Alignment.CenterEnd) {
        Column(Modifier.width(440.dp).fillMaxHeight()
            .background(Ink.Background, RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp)).testTag("tracks-panel")) {
        Row(Modifier.fillMaxWidth().padding(start = 28.dp, end = 28.dp, top = 24.dp, bottom = 18.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Ses ve altyazı", Modifier.weight(1f).testTag("tracks-title"), color = Ink.Text,
                fontFamily = Roboto, fontWeight = FontWeight.Medium, fontSize = 24.sp, lineHeight = 30.sp)
            Action("Kapat", Modifier.focusRequester(first).testTag("tracks-close"), onClick = dismiss)
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(Ink.Divider))
        CompositionLocalProvider(LocalBringIntoViewSpec provides trackVisibility) {
        Column(Modifier.weight(1f).fillMaxWidth().testTag("tracks-viewport")
            .verticalScroll(rememberScrollState()).padding(horizontal = 28.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)) {
        listOf(C.TRACK_TYPE_AUDIO, C.TRACK_TYPE_TEXT).forEach { type ->
            Text(if (type == C.TRACK_TYPE_AUDIO) "Ses dili" else "Altyazı", color = Ink.Mint, fontFamily = Roboto, fontSize = 12.sp)
            if (type == C.TRACK_TYPE_TEXT) TrackChoice("Kapalı", selected = parameters.disabledTrackTypes.contains(type)) {
                player.trackSelectionParameters = player.trackSelectionParameters.buildUpon()
                    .clearOverridesOfType(type).setTrackTypeDisabled(type, true).build()
                model.store.subtitlesOff = true
            }
            val groups = tracks.groups.filter { it.type == type }
            if (groups.isEmpty()) Note(if (type == C.TRACK_TYPE_TEXT) "Bu yayında gömülü altyazı bulunamadı." else "Ses parçası bulunamadı; yayın henüz açılmamış olabilir.")
            var number = 0
            groups.forEach { group ->
                for (index in 0 until group.length) {
                    val format = group.getTrackFormat(index)
                    val supported = group.isTrackSupported(index)
                    val label = trackLabel(format, ++number, type)
                    TrackChoice(label, selected = group.isTrackSelected(index) && !parameters.disabledTrackTypes.contains(type), enabled = supported,
                        status = if (supported) null else "Bu cihaz desteklemiyor") {
                        player.trackSelectionParameters = player.trackSelectionParameters.buildUpon()
                            .setTrackTypeDisabled(type, false).clearOverridesOfType(type)
                            .setOverrideForType(TrackSelectionOverride(group.mediaTrackGroup, index)).build()
                        if (type == C.TRACK_TYPE_AUDIO) model.store.audioLanguage = format.language
                        else { model.store.subtitleLanguage = format.language; model.store.subtitlesOff = false }
                    }
                }
            }
        }
        }
        }
        Note("Yalnızca bu yayında bulunan dil ve altyazılar listelenir.",
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp).testTag("tracks-note"))
        }
        }
    }
}

@Composable private fun TrackChoice(label: String, selected: Boolean, enabled: Boolean = true, status: String? = null, onClick: () -> Unit) {
    var overflow by remember(label) { mutableStateOf(false) }
    var reading by remember(label) { mutableStateOf(false) }
    var restore by remember { mutableStateOf(false) }
    val explanation = remember { FocusRequester() }
    val window = LocalWindowInfo.current
    LaunchedEffect(reading, restore) {
        if (!reading && restore) {
            snapshotFlow { window.isWindowFocused }.first { it }
            explanation.requestFocus()
            restore = false
        }
    }
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Button(onClick = onClick, modifier = Modifier.focusProperties { canFocus = enabled }.fillMaxWidth()
        .semantics { role = Role.RadioButton; this.selected = selected; stateDescription = if (selected) "Seçili" else "Seçili değil" },
        enabled = enabled, contentPadding = PaddingValues(0.dp), shape = ButtonDefaults.shape(shape = RoundedCornerShape(TvLayout.CardRadius)),
        colors = ButtonDefaults.colors(containerColor = if (selected) Ink.Selected else Ink.Container, focusedContainerColor = Ink.High),
        scale = ButtonDefaults.scale(focusedScale = 1f),
        border = ButtonDefaults.border(focusedBorder = focusRing(TvLayout.CardRadius))) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // The status line is never ellipsized, so an unavailable track always says why.
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(label, color = if (enabled) Ink.Text else Ink.Muted,
                    fontFamily = Roboto, fontSize = 14.sp, lineHeight = 20.sp,
                    maxLines = 5, overflow = TextOverflow.Ellipsis,
                    onTextLayout = { overflow = it.hasVisualOverflow })
                if (status != null) Note(status)
            }
            // Always lay out the glyph so rows keep one height when the selection changes.
            Text("✓", Modifier.width(24.dp).clearAndSetSemantics {}, color = if (selected) Ink.Mint else Color.Transparent,
                fontFamily = Roboto, fontSize = 16.sp)
        }
    }
    if (overflow) Action("Tam açıklama", Modifier.focusRequester(explanation)) { reading = true }
    }
    if (reading) {
        val reader = remember { FocusRequester() }
        val close = remember { FocusRequester() }
        fun dismissReading() { reading = false; restore = true }
        SourceDialog("Parça açıklaması", reader, ::dismissReading, width = 560.dp) {
            RemoteTextReader(label, Modifier.weight(1f, fill = false).focusRequester(reader)
                .focusProperties { down = close }, "track-description-reader")
            Action("Geri", Modifier.focusRequester(close).focusProperties { up = reader }
                .testTag("track-description-back"), onClick = ::dismissReading)
        }
    }
}

private fun trackLabel(format: Format, number: Int, type: Int): String {
    val language = format.language?.takeUnless { it == "und" || it.isBlank() }
        ?.let { Locale.forLanguageTag(it).getDisplayLanguage(Locale.forLanguageTag("tr")).ifBlank { it } }
        ?: if (type == C.TRACK_TYPE_AUDIO) "Ses" else "Altyazı"
    val channels = if (format.channelCount > 0) when (format.channelCount) { 1 -> "mono"; 2 -> "stereo"; 6 -> "5.1"; 8 -> "7.1"; else -> "${format.channelCount} kanal" } else ""
    return listOf("$language · $number", format.label.orEmpty(), channels).filter { it.isNotBlank() }.distinct().joinToString(" · ")
}
