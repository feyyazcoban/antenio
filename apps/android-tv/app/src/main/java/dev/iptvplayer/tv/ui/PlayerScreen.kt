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
        trackPanel = false; active = media; interact()
        player.trackSelectionParameters = player.trackSelectionParameters.buildUpon().clearOverrides()
            .setPreferredAudioLanguage(model.store.audioLanguage)
            .setPreferredTextLanguage(model.store.subtitleLanguage)
            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, model.store.subtitlesOff).build()
        val http = DefaultHttpDataSource.Factory().setUserAgent("IPTVPlayerPilot/${BuildConfig.VERSION_NAME}")
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
            when (event.key) {
                Key.MediaPlayPause -> {
                    if (recoveryState.recovering) recovery.cancel()
                    else if (failure != null) recovery.retryManually()
                    else if (player.isPlaying) player.pause() else player.play()
                    true
                }
                Key.MediaPlay -> { if (failure != null) recovery.retryManually() else if (!recoveryState.recovering) player.play(); true }
                Key.MediaPause -> { if (recoveryState.recovering) recovery.cancel() else player.pause(); true }
                Key.DirectionCenter, Key.Enter, Key.DirectionLeft, Key.DirectionRight, Key.DirectionUp, Key.DirectionDown -> {
                    if (!visible && !trackPanel) { visible = true; true } else false
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
        // While the controls are up, their status line reports opening; the centred pill would sit under the panel.
        if (opening && !controlsShown) {
            Text("Yayın açılıyor…", Modifier.align(Alignment.Center).offset { IntOffset(if (trackPanel) (-220).dp.roundToPx() else 0, 0) }
                .background(Ink.Background.copy(alpha = .9f), RoundedCornerShape(12.dp)).padding(20.dp),
                color = Ink.Text, fontFamily = Roboto, fontSize = 16.sp)
        }
        if (controlsShown) {
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0f to Color.Black.copy(alpha = .4f),
                .25f to Color.Transparent, .4f to Color.Transparent, .58f to Color.Black.copy(alpha = .72f), 1f to Color.Black.copy(alpha = .9f))))
            // Its own surface keeps the label readable over bright frames and burned-in video text.
            Row(Modifier.align(Alignment.TopStart).padding(start = TvLayout.Gutter, end = TvLayout.Gutter, top = 22.dp)
                .background(Ink.Background.copy(alpha = .9f), RoundedCornerShape(50)).padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LineIcon(if (live) "tv" else "film", Ink.Mint, Modifier.size(20.dp))
                Text(if (live) "CANLI TV" else if (media.entry.kind == MediaKind.EPISODE) "DİZİ" else "FİLM",
                    color = Ink.Text, fontFamily = Roboto, fontSize = 11.sp, letterSpacing = 1.4.sp)
                if (screenState.sources.size > 1) Text(screenState.sourceName(media.entry.sourceId),
                    Modifier.widthIn(max = 360.dp), color = Ink.Muted, fontFamily = Roboto, fontSize = 11.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = TvLayout.Gutter, vertical = 26.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(Modifier.fillMaxWidth().testTag("player-panel")
                    .background(Ink.Background.copy(alpha = .96f), RoundedCornerShape(TvLayout.HeroRadius))
                    .border(1.dp, Ink.Divider.copy(alpha = .55f), RoundedCornerShape(TvLayout.HeroRadius))
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(13.dp)) {
                Text(media.entry.title, Modifier.fillMaxWidth().testTag("player-title"),
                    color = Ink.Text, fontSize = 28.sp, lineHeight = 33.sp,
                    fontWeight = FontWeight.Medium, fontFamily = Roboto, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (recoveryState.recovering) Note(recoveryState.message)
                else if (failure != null) Note(failure, error = true)
                else Note(when {
                    opening && live -> "Yayın açılıyor…"
                    live -> "● CANLI"
                    else -> "${clockTime(position)} / ${if (duration > 0) clockTime(duration) else "—"}" +
                        when { opening -> " · Yayın açılıyor…"; status == Player.STATE_ENDED -> " · Bitti"; else -> "" }
                })
                if (live && failure == null && !recoveryState.recovering) {
                    Text(currentProgramme?.let { "${it.title} · ${programmeTime(it, model.guideZone())}" } ?: "Program bilgisi bulunmuyor",
                        Modifier.testTag("player-current-programme"), color = Ink.Text, fontFamily = Roboto, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    schedule?.next(guideNow)?.let { next -> Text("Sonra · ${guideTime(next.start, model.guideZone())}  ${next.title}",
                        color = Ink.Muted, fontFamily = Roboto, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    currentProgramme?.end?.let { end -> Box(Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(Ink.High)) {
                        Box(Modifier.fillMaxWidth(((guideNow - currentProgramme.start).toFloat() / (end - currentProgramme.start)).coerceIn(0f, 1f))
                            .fillMaxHeight().background(Ink.Mint))
                    } }
                }
                // Reserve the VOD progress slot so the panel does not move when the duration arrives.
                if (!live) Box(Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp))
                    .background(if (duration > 0) Ink.High else Color.Transparent)) {
                    if (duration > 0) Box(Modifier.fillMaxWidth((position.toFloat() / duration).coerceIn(0f, 1f)).fillMaxHeight().background(Ink.Mint))
                }
                CompositionLocalProvider(LocalBringIntoViewSpec provides controlVisibility) {
                Row(Modifier.fillMaxWidth().testTag("player-controls").horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
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
                Text(if (recoveryState.recovering) "Geri: yeniden bağlanmayı durdur ve kütüphaneye dön"
                    else if (failure != null) "Geri: kütüphaneye dön" else "Geri: kontrolleri gizle · Yeniden Geri: kütüphaneye dön",
                    color = Ink.Muted, fontFamily = Roboto, fontSize = 10.sp)
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
        border = ButtonDefaults.border(focusedBorder = Border(BorderStroke(TvLayout.Focus, Ink.Text), inset = 3.dp))) {
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
