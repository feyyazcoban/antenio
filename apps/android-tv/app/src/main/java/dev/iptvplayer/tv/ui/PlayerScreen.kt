@file:androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)

package dev.iptvplayer.tv.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.BringIntoViewSpec
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import android.view.accessibility.AccessibilityManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.*
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.tv.material3.Text
import androidx.tv.material3.Border
import androidx.compose.foundation.BorderStroke
import dev.iptvplayer.tv.PlaybackMode
import dev.iptvplayer.tv.PlayingMedia
import dev.iptvplayer.tv.Route
import dev.iptvplayer.tv.R
import dev.iptvplayer.tv.TvViewModel
import dev.iptvplayer.tv.data.MediaKind
import dev.iptvplayer.tv.data.EpisodeSequence
import dev.iptvplayer.tv.data.GateResult
import dev.iptvplayer.tv.data.PanelOpacity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalFoundationApi::class)
@Composable internal fun PlayerScreen(media: PlayingMedia, model: TvViewModel, back: () -> Unit) {
    val context = LocalContext.current
    // The corner notes are written when a key arrives, outside composition.
    val resources = LocalResources.current
    val subtitleGapPx = with(LocalDensity.current) { 12.dp.toPx() }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val screenState by model.state.collectAsStateWithLifecycle()
    val guideNow = guideClock()
    val guideEntries = remember(media.entry) { if (media.entry.kind == MediaKind.LIVE) listOf(media.entry) else emptyList() }
    val schedule = guideRows(guideEntries, dev.iptvplayer.tv.data.GuideWindow(guideNow, guideNow + 12 * 3_600_000L), screenState, model)[media.entry.id]
    val currentProgramme = schedule?.now(guideNow)
    // Settings → Oynatıcı as they were when the player opened: Settings opens from the other screens only, so a change
    // holds from the next stream.
    val opened = remember { screenState.playback }
    val player = remember { PlayerFactory.create(context, model.store, opened) }
    val accessibility = remember(context) { context.getSystemService(AccessibilityManager::class.java) }
    // The notes, the top label and the Ses / Altyazı panel let as much of the picture through as Oynatıcı panelleri says.
    val panelAlpha = screenState.playback.panelOpacity.alpha
    val scope = rememberCoroutineScope()
    val recovery = remember(player) { PlaybackRecovery(player, scope) }
    val recoveryState by recovery.state.collectAsStateWithLifecycle()
    val failure = recoveryState.failure
    var active by remember { mutableStateOf<PlayingMedia?>(null) }
    // What the viewer has open over the picture: the controls, the tracks panel or nothing.
    var layer by remember { mutableStateOf<PlayerLayer>(PlayerLayer.Controls) }
    var tracks by remember { mutableStateOf(Tracks.EMPTY) }
    // Warn once per media if decoding on this device leaves every audio track unsupported.
    var silent by remember { mutableStateOf(false) }
    var trackParameters by remember { mutableStateOf(player.trackSelectionParameters) }
    var status by remember { mutableIntStateOf(Player.STATE_IDLE) }
    var playing by remember { mutableStateOf(false) }
    var speed by remember { mutableFloatStateOf(player.playbackParameters.speed) }
    var speedRequest by remember { mutableStateOf<Float?>(null) }
    val optionsFocus = remember { FocusRequester() }
    val tracksFocus = remember { FocusRequester() }
    val channelsFocus = remember { FocusRequester() }
    var returnControl by remember { mutableStateOf<FocusRequester?>(null) }
    var hasPlayed by remember { mutableStateOf(false) }
    val clock = remember(player) { PlaybackClock() }
    fun updateClock() = clock.update(player.currentPosition, player.duration, player.bufferedPosition,
        model.state.value.playing?.mode != PlaybackMode.LIVE && player.isCurrentMediaItemSeekable && player.isCommandAvailable(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM),
        System.currentTimeMillis())
    val scrub = remember(player) { ScrubHolder(scope, clock,
        canSeek = { player.isCurrentMediaItemSeekable && player.isCommandAvailable(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM) &&
            !recovery.state.value.recovering && recovery.state.value.failure == null }, seek = { player.seekTo(it) }) }
    val scrubFocus = remember { FocusRequester() }
    var scrubFocused by remember { mutableStateOf(false) }
    var playerView by remember { mutableStateOf<PlayerView?>(null) }
    var peekAt by remember { mutableLongStateOf(0L) }
    var peek by remember { mutableStateOf(false) }
    var peekDelta by remember { mutableLongStateOf(0L) }
    var lastInteraction by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val first = remember { FocusRequester() }
    val lastControl = remember { arrayOf(first) }
    val canvas = remember { FocusRequester() }
    val nextEpisodeFocus = remember { FocusRequester() }
    // A locked channel typed by number, or a locked series' next episode, asks for the PIN over the picture; what plays
    // goes on behind it (pin.md §3.7).
    val openPin = rememberPinOpener()
    val live = media.mode == PlaybackMode.LIVE
    val favoriteTarget = remember(media.entry, screenState.catalog, screenState.selected, screenState.episodes, screenState.history) {
        model.favoriteTarget(media.entry)
    }
    val isFavorite = favoriteTarget?.id in screenState.favoriteIds
    fun save() { active?.let { model.saveProgress(it.entry, player.currentPosition.coerceAtLeast(0), player.duration.coerceAtLeast(0)) } }
    fun interact() { lastInteraction = System.currentTimeMillis(); if (layer == PlayerLayer.Video) layer = PlayerLayer.Controls }
    val episodeEnd = remember(player) {
        EpisodeEndController(scope) { sourceId, currentId, nextId, automatic ->
            save(); model.playNextEpisode(sourceId, currentId, nextId, automatic)
        }
    }
    val endState by episodeEnd.state.collectAsStateWithLifecycle()
    // An episode that ends under the PIN pad its Sonraki bölüm opened keeps the card back until the pad closes, so its
    // window never covers the pad: a right PIN starts the next episode, and Vazgeç shows the card without a countdown.
    var endedUnderPin by remember { mutableStateOf(false) }
    fun episodeEnded(needsPin: Boolean = false) {
        val current = active?.entry
        val s = model.state.value
        if (hasPlayed && current != null && s.playing?.entry?.id == current.id && s.sourceFor(current) != null)
            episodeEnd.ended(current.sourceId, current, model.nextEpisodeChoice(), s.autoNextEpisode, needsPin)
    }
    // The end card covers whatever was open; EpisodeEndController keeps it.
    fun top(): PlayerLayer = if (endState != null) PlayerLayer.EpisodeEnd else layer
    val nextChoice = remember(media.entry, screenState.sources, screenState.episodes) {
        screenState.sourceFor(media.entry)?.let { EpisodeSequence.next(it, media.entry, screenState.episodes) }
    }
    val opening = failure == null && !recoveryState.recovering && (status == Player.STATE_BUFFERING || status == Player.STATE_IDLE)
    val controlsShown = top() == PlayerLayer.Controls
    // Remote shortcuts while the controls are hidden: the next channel opens without bringing them up, and a short
    // note in the corner says what happened (the channel, a seek, the number being typed).
    var quiet by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf<Pair<String, String?>?>(null) }
    var noteAt by remember { mutableLongStateOf(0L) }
    var audioNote by remember { mutableStateOf(false) }
    var digits by remember { mutableStateOf("") }
    fun flash(text: String, detail: String? = null, audio: Boolean = false) {
        note = text to detail; audioNote = audio; noteAt = System.currentTimeMillis()
    }
    fun zap(direction: Int) { quiet = layer == PlayerLayer.Video; digits = ""; model.changeChannel(direction) }
    fun seekBy(deltaMs: Long) {
        if (!player.isCurrentMediaItemSeekable) return
        val end = player.duration.takeIf { it > 0 } ?: Long.MAX_VALUE
        val target = (player.currentPosition + deltaMs).coerceIn(0, end)
        scrub.cancel(); player.seekTo(target); clock.moved(target)
        if (layer == PlayerLayer.Video) { peek = true; peekDelta = deltaMs; peekAt = System.currentTimeMillis() }
    }
    // The player as PlayerKeyRouter reads it when a key or Back arrives; its commands are carried out here.
    fun keys(repeat: Int = 0) = KeyContext(media.mode, top(), recoveryState.recovering, failure != null, player.isPlaying, repeat, scrub.target != null)
    fun perform(command: PlayerCommand) {
        when (command) {
            PlayerCommand.Play -> { scrub.commit(); player.play() }
            PlayerCommand.Pause -> { scrub.commit(); player.pause() }
            PlayerCommand.Retry -> recovery.retryManually()
            PlayerCommand.StopRecovery -> recovery.cancel()
            PlayerCommand.Consume -> Unit
            is PlayerCommand.Zap -> zap(command.direction)
            is PlayerCommand.AppendDigit -> { digits = PlayerKeyRouter.appendDigit(digits, command.digit); flash(digits) }
            PlayerCommand.OpenGuide -> model.guide()
            is PlayerCommand.Seek -> seekBy(command.deltaMs)
            is PlayerCommand.ScrubNudge -> { updateClock(); scrub.nudge(command.deltaMs) }
            is PlayerCommand.CommitScrub -> {
                // seekTo may enter BUFFERING synchronously; toggle from the state at the key press.
                val wasPlaying = player.isPlaying
                scrub.commit()
                if (command.thenToggle) { if (wasPlaying) player.pause() else player.play() }
            }
            PlayerCommand.CancelScrub -> scrub.cancel()
            PlayerCommand.ShowControls -> { peek = false; layer = PlayerLayer.Controls }
            PlayerCommand.HideControls -> layer = PlayerLayer.Video
            PlayerCommand.CloseTracks -> { returnControl = tracksFocus; layer = PlayerLayer.Controls; interact() }
            PlayerCommand.CloseOptions -> { returnControl = optionsFocus; layer = PlayerLayer.Controls; interact() }
            PlayerCommand.CloseSpeed -> { layer = PlayerLayer.Options; interact() }
            PlayerCommand.OpenChannels -> { digits = ""; quiet = false; note = null; peek = false; interact(); layer = PlayerLayer.Channels }
            PlayerCommand.CloseChannels -> { returnControl = channelsFocus; layer = PlayerLayer.Controls; interact() }
            PlayerCommand.CloseEndCard -> { episodeEnd.dismiss(); interact() }
            is PlayerCommand.Exit -> { if (command.stopRecovery) recovery.cancel(); save(); model.back() }
        }
    }
    // PlayerKeyRouter decides; a key it leaves goes on to the focused control. The end card's window sends its keys here.
    fun onKey(event: KeyEvent): Boolean {
        if (event.type != KeyEventType.KeyDown) return false
        lastInteraction = System.currentTimeMillis()
        val key = PlayerKey.of(event.nativeKeyEvent.keyCode) ?: return false
        val command = PlayerKeyRouter.route(key, keys(event.nativeKeyEvent.repeatCount)) ?: return false
        perform(command); return true
    }
    LaunchedEffect(peekAt) { if (peek) { delay(2_500); peek = false } }
    LaunchedEffect(failure, recoveryState.recovering) { if (failure != null || recoveryState.recovering) scrub.cancel() }
    LaunchedEffect(controlsShown) { if (!controlsShown) {
        scrubFocused = false
        playerView?.subtitleView?.setBottomPaddingFraction(.08f)
    } }
    LaunchedEffect(noteAt) { if (note != null) { delay(if (audioNote) 8_000 else 2_500); note = null } }
    LaunchedEffect(digits) {
        val pending = digits
        if (pending.isEmpty()) return@LaunchedEffect
        delay(PlayerKeyRouter.DIGIT_COMMIT_MS)
        if (digits != pending || layer == PlayerLayer.Channels) return@LaunchedEffect
        val number = pending.toInt(); digits = ""
        quiet = layer == PlayerLayer.Video
        when (model.playChannelNumber(number)) {
            GateResult.Deny -> { quiet = false; flash(resources.getString(R.string.player_no_channel, number.toString())) }
            // Once the PIN is right the channel opens with its number in the corner, as it would have at once.
            is GateResult.NeedsPin -> openPin { if (layer == PlayerLayer.Video) canvas else first }
            else -> Unit
        }
    }

    DisposableEffect(player, lifecycle) {
        val listener = object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) { updateClock() }
            override fun onPlaybackStateChanged(playbackState: Int) {
                status = playbackState
                if (playbackState == Player.STATE_ENDED) {
                    if (layer != PlayerLayer.Channels) layer = PlayerLayer.Controls
                    updateClock(); scrub.cancel()
                    save()
                    if (model.state.value.parental.prompt != null) endedUnderPin = true else episodeEnded()
                }
            }
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                playing = isPlaying
                if (isPlaying) { hasPlayed = true; active?.entry?.let { model.recordViewed(it) } }
            }
            override fun onTracksChanged(value: Tracks) {
                tracks = value
                if (!silent && unplayableAudio(value, opened.audioOutput)) {
                    silent = true; flash(resources.getString(R.string.player_audio_unplayable), audio = true)
                }
            }
            override fun onPlaybackParametersChanged(parameters: PlaybackParameters) {
                speed = parameters.speed
                if (speedRequest?.let { kotlin.math.abs(it - speed) > .001f } == true) {
                    speedRequest = null
                    flash(resources.getString(R.string.player_speed_unavailable), audio = true)
                }
            }
            override fun onTrackSelectionParametersChanged(parameters: TrackSelectionParameters) { trackParameters = parameters }
        }
        var resumeOnStart = false
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    resumeOnStart = player.playWhenReady && player.playbackState == Player.STATE_READY
                    // Should the lock screen cover this player, the one that comes back after it stands as this one did.
                    model.playerStopped(paused = !player.playWhenReady)
                    scrub.cancel(); save(); recovery.background(); episodeEnd.background()
                }
                // A locked title left the player as the app stopped (TvViewModel.appStopped), or the lock screen came up
                // over it (pin.md §3.9): this screen is on its way out and must not be heard first, before the next frame
                // removes it. The player that comes back once the PIN is right starts it again.
                Lifecycle.Event.ON_START -> {
                    recovery.foreground(); episodeEnd.foreground()
                    if (resumeOnStart && model.state.value.screen == Route.PLAYER) player.play()
                }
                else -> Unit
            }
        }
        player.addListener(listener)
        lifecycle.addObserver(observer)
        onDispose { scrub.cancel(); save(); recovery.close(); episodeEnd.close(); lifecycle.removeObserver(observer); player.removeListener(listener); player.release() }
    }

    LaunchedEffect(media) {
        val request = PlayerFactory.request(media)
        save()
        // Stop the old request before opening a new channel: one source session at a time.
        player.stop(); player.clearMediaItems()
        recovery.newMedia(request.mode == PlaybackMode.LIVE)
        episodeEnd.newMedia(); endedUnderPin = false; hasPlayed = false
        tracks = Tracks.EMPTY; silent = false; scrub.reset(); clock.reset(request.startMs); peek = false
        if (audioNote) { note = null; audioNote = false }
        speedRequest = null
        if (request.mode == PlaybackMode.LIVE) player.playbackParameters = PlaybackParameters.DEFAULT
        if (layer == PlayerLayer.Tracks || layer == PlayerLayer.Options || layer == PlayerLayer.Speed) layer = PlayerLayer.Controls
        active = media
        if (quiet) {
            quiet = false; lastInteraction = System.currentTimeMillis()
            flash(listOfNotNull(model.channelNumber(media.entry)?.toString(), shownTitle(media.entry.title, "", resources).name).joinToString("  "))
        } else interact()
        player.trackSelectionParameters = PlayerFactory.rememberedTracks(player.trackSelectionParameters, model.store)
        player.setMediaSource(PlayerFactory.mediaSource(context, request), request.startMs)
        // A title the viewer had paused before the lock screen covered it comes back paused.
        player.prepare(); if (!media.paused) player.play()
    }
    LaunchedEffect(player) {
        var ticks = 0
        while (true) {
            delay(500)
            updateClock()
            if (++ticks % 10 == 0) save()
            // The PIN pad over the picture takes the remote's keys: the controls that opened it stay until it closes,
            // and for the usual while after, so focus can go back to them.
            if (model.state.value.parental.prompt != null) lastInteraction = System.currentTimeMillis()
            if (ControlsAutoHide.shouldHide(System.currentTimeMillis(), lastInteraction, model.state.value.playback.osdTimeout,
                    player.isPlaying, top() == PlayerLayer.Controls, recovery.state.value.failure != null, recovery.state.value.recovering,
                    talkBack = accessibility?.isTouchExplorationEnabled == true, scrubbing = scrub.target != null)) layer = PlayerLayer.Video
        }
    }
    LaunchedEffect(top()) {
        if (top() == PlayerLayer.Controls || top() == PlayerLayer.Video) {
            withFrameNanos { }
            if (layer == PlayerLayer.Video) canvas.requestFocus()
            else if (returnControl != null) { if (!runCatching { returnControl!!.requestFocus() }.getOrDefault(false)) first.requestFocus(); returnControl = null }
            else first.requestFocus()
        }
    }
    LaunchedEffect(endedUnderPin, screenState.parental.prompt == null) {
        if (endedUnderPin && screenState.parental.prompt == null) { endedUnderPin = false; episodeEnded(needsPin = true) }
    }
    LaunchedEffect(screenState.autoNextEpisode) { episodeEnd.preferenceChanged(screenState.autoNextEpisode) }
    LaunchedEffect(nextChoice) { episodeEnd.availabilityChanged(nextChoice) }
    LaunchedEffect(recoveryState.recovering, failure) {
        if (recoveryState.recovering || failure != null) {
            if (layer != PlayerLayer.Channels) layer = PlayerLayer.Controls
            interact()
        }
    }
    BackHandler {
        // A Back that arrives after the model left the player, before this screen is gone, is the new route's.
        if (model.state.value.route != Route.PLAYER) return@BackHandler back()
        perform(PlayerKeyRouter.back(keys()))
    }
    Box(Modifier.fillMaxSize().background(Color.Black).onPreviewKeyEvent(::onKey)) {
        AndroidView(factory = { PlayerView(it).apply {
            playerView = this
            this.player = player; useController = false; keepScreenOn = true
            isFocusable = false; descendantFocusability = android.view.ViewGroup.FOCUS_BLOCK_DESCENDANTS
            setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
        } }, modifier = Modifier.fillMaxSize(), update = { view ->
            view.subtitleView?.let { PlayerFactory.subtitles(it, screenState.playback.subtitle) }
        }, onRelease = { it.player = null })
        Box(Modifier.fillMaxSize().focusRequester(canvas).focusable(layer == PlayerLayer.Video))
        val problemText = when {
            recoveryState.recovering -> recoveryState.message.resolve()
            failure != null -> failure.resolve()
            else -> null
        }
        val showProblem = problemText != null && (controlsShown || top() == PlayerLayer.Video) && screenState.parental.prompt == null
        // Persistent playback failures take priority over transient corner notes without taking remote focus.
        if (showProblem) Row(Modifier.align(Alignment.TopCenter).padding(top = 24.dp, start = TvLayout.Gutter, end = TvLayout.Gutter)
            .widthIn(max = 640.dp).background(Ink.Background.copy(alpha = .96f), RoundedCornerShape(12.dp))
            .border(1.dp, (if (recoveryState.recovering) Ink.Outline else Ink.Error).copy(alpha = .35f), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite }.testTag("player-problem"),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (recoveryState.recovering) LoadingArc(color = Ink.Muted) else LineIcon("info", Ink.Error, Modifier.size(20.dp))
            Text(problemText.orEmpty(), color = if (recoveryState.recovering) Ink.Text else Ink.Error,
                fontFamily = Roboto, fontSize = 16.sp, lineHeight = 22.sp)
        }
        note?.takeIf { !showProblem && (!controlsShown || audioNote) }?.let { (text, detail) ->
            Column(Modifier.align(Alignment.TopEnd).padding(top = 22.dp, end = TvLayout.Gutter).widthIn(max = 420.dp)
                .background(Ink.Background.copy(alpha = panelAlpha), RoundedCornerShape(12.dp)).padding(horizontal = 16.dp, vertical = 10.dp)
                .semantics { liveRegion = LiveRegionMode.Polite }.testTag("player-note"), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text, color = Ink.Text, fontFamily = Roboto, fontWeight = FontWeight.Medium, fontSize = 18.sp, lineHeight = 24.sp,
                    maxLines = if (audioNote) 4 else 1, overflow = TextOverflow.Ellipsis)
                detail?.let { Text(it, color = Ink.Muted, fontFamily = Roboto, fontSize = 13.sp, lineHeight = 18.sp, maxLines = 1) }
            }
        }
        // While the controls are up, their status line reports opening; the centred pill would sit under the panel.
        if (opening && !controlsShown && layer != PlayerLayer.Channels) {
            Row(Modifier.align(Alignment.Center).offset { IntOffset(if (layer == PlayerLayer.Tracks) (-220).dp.roundToPx() else 0, 0) }
                .background(Ink.Background.copy(alpha = panelAlpha), RoundedCornerShape(12.dp)).padding(20.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LoadingArc()
                Text(stringResource(R.string.player_opening), color = Ink.Text, fontFamily = Roboto, fontSize = 16.sp)
            }
        }
        val info = remember(media.entry, favoriteTarget, resources) { PlayerInfoModel.from(media.entry,
            favoriteTarget?.takeIf { it.kind == MediaKind.SERIES }).let { it.copy(title = shownTitle(it.title, it.year, resources).name) } }
        val statusText = when {
            opening -> stringResource(R.string.player_opening)
            status == Player.STATE_ENDED -> stringResource(R.string.player_ended)
            else -> null
        }
        val largeText = LocalDensity.current.fontScale >= 1.3f
        val reducedMotion = screenState.reducedMotion
        fun primaryAction() {
            val wasPlaying = player.isPlaying
            interact(); scrub.commit()
            if (recoveryState.recovering) recovery.cancel()
            else if (failure != null) recovery.retryManually()
            else if (wasPlaying) player.pause()
            else { if (status == Player.STATE_ENDED) { episodeEnd.newMedia(); player.seekTo(0) }; player.play() }
        }
        AnimatedVisibility(controlsShown, Modifier.fillMaxSize(),
            enter = fadeIn(tween(if (reducedMotion) 0 else 120)), exit = fadeOut(tween(if (reducedMotion) 0 else 100))) {
            Box(Modifier.fillMaxSize()) {
                Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().fillMaxHeight(if (largeText) .7f else .6f)
                    .background(Brush.verticalGradient(0f to Color.Transparent,
                        (if (largeText) .343f else .367f) to Color.Black.copy(alpha = .62f),
                        (if (largeText) .657f else .667f) to Color.Black.copy(alpha = .86f), 1f to Color.Black.copy(alpha = .94f))))
                PlayerInfoBlock(PlayerInfoKind.Full, media.entry, info, model.channelNumber(media.entry),
                    schedule, model.guideZone(),
                    clock, scrub, speed, playing, statusText, failure != null || recoveryState.recovering, opening, scrubFocus,
                    returnFocus = { if (!runCatching { lastControl[0].requestFocus() }.getOrDefault(false)) first.requestFocus() }, toggle = ::primaryAction,
                    modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(horizontal = TvLayout.Gutter, vertical = 24.dp)
                        .testTag("player-panel").onGloballyPositioned { coordinates ->
                            val view = playerView
                            if (controlsShown && view != null && view.height > 0) view.subtitleView?.setBottomPaddingFraction(
                                ((view.height - coordinates.positionInRoot().y + subtitleGapPx) / view.height).coerceIn(.08f, .85f))
                        }, onScrubFocusChanged = { scrubFocused = it }) {
                    val healthy = failure == null && !recoveryState.recovering
                    val canSeek = !live && clock.seekable && healthy && !opening
                    val primary = PlayerControl("player-primary", stringResource(if (recoveryState.recovering) R.string.action_stop
                        else if (failure != null) R.string.action_retry else if (playing) R.string.player_pause
                        else if (status == Player.STATE_ENDED) R.string.action_replay else R.string.action_play),
                        if (recoveryState.recovering) "stop" else if (failure != null) "refresh" else if (playing) "pause"
                        else if (status == Player.STATE_ENDED) "replay" else "play_arrow", primary = true, requester = first, onClick = ::primaryAction)
                    val cluster = buildList {
                        if (!live && (clock.seekable || opening)) add(PlayerControl("player-seek-back", stringResource(R.string.player_seek_back_accessible),
                            "replay_10", enabled = canSeek) { interact(); seekBy(-10_000) })
                        add(primary)
                        if (live) add(PlayerControl("player-channels", stringResource(R.string.player_channels), "channels",
                            requester = channelsFocus) { perform(PlayerCommand.OpenChannels) })
                        if (!live && (clock.seekable || opening)) add(PlayerControl("player-seek-forward", stringResource(R.string.player_seek_forward_accessible),
                            "forward_10", enabled = canSeek) { interact(); seekBy(10_000) })
                        if (nextChoice != null) add(PlayerControl("player-next-episode", stringResource(R.string.player_next_episode), "skip_next",
                            state = shownTitle(nextChoice.entry.title, "", resources).name, requester = nextEpisodeFocus) {
                            scrub.cancel(); save()
                            screenState.sourceFor(media.entry)?.let { source ->
                                if (model.playNextEpisode(source.id, media.entry.id, nextChoice.entry.id) == NextResult.NEEDS_PIN) openPin { nextEpisodeFocus }
                            }
                        })
                    }
                    val favoriteLabel = stringResource(when {
                        favoriteTarget == null -> R.string.player_favorite_unavailable
                        media.entry.kind == MediaKind.EPISODE -> if (isFavorite) R.string.player_series_favorite_remove else R.string.player_series_favorite_add
                        else -> if (isFavorite) R.string.favorite_remove else R.string.favorite_add
                    })
                    val favoriteState = stringResource(if (isFavorite) R.string.player_in_favorites else R.string.player_not_in_favorites)
                    val chips = buildList {
                        if (live) {
                            add(PlayerControl("player-channel-down", stringResource(R.string.player_channel_minus), "keyboard_arrow_down") { model.changeChannel(-1); interact() })
                            add(PlayerControl("player-channel-up", stringResource(R.string.player_channel_plus), "keyboard_arrow_up") { model.changeChannel(1); interact() })
                        }
                        add(PlayerControl("player-tracks", stringResource(R.string.player_tracks), "subtitles", state = selectedTrackSummary(tracks, trackParameters), enabled = healthy, requester = tracksFocus) {
                            scrub.commit(); interact(); layer = PlayerLayer.Tracks
                        })
                        if (!live) add(PlayerControl("player-options", stringResource(R.string.player_options), "settings", state = PlaybackSpeed.label(speed),
                            enabled = healthy, requester = optionsFocus) { scrub.commit(); interact(); layer = PlayerLayer.Options })
                        add(PlayerControl("player-favorite", favoriteLabel, if (isFavorite) "star_filled" else "star", state = favoriteState,
                            selected = isFavorite, enabled = favoriteTarget != null) { model.toggleFavorite(media.entry); interact() })
                    }
                    PlayerControlRow(cluster, chips, scrubFocus, canSeek, { lastControl[0] = it },
                        if (live) "" else stringResource(if (recoveryState.recovering) R.string.player_keys_recovering
                            else if (scrubFocused) R.string.player_hint_scrub else R.string.player_hint_vod))
                }
            }
        }
        if (peek && layer == PlayerLayer.Video && !live) {
            Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(150.dp)
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = .6f)))))
            PlayerInfoBlock(PlayerInfoKind.Peek, media.entry, info, null, null, model.guideZone(), clock, scrub, speed,
                playing, null, false, false, scrubFocus, {}, {}, Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                    .padding(horizontal = TvLayout.Gutter, vertical = 24.dp).testTag("player-peek"), peekDelta = peekDelta)
        }
        if (layer == PlayerLayer.Tracks) TracksDialog(tracks, trackParameters, player, model, screenState.playback.panelOpacity) {
            perform(PlayerCommand.CloseTracks)
        }
        AnimatedVisibility(live && layer == PlayerLayer.Channels, Modifier.align(Alignment.CenterStart).fillMaxHeight(),
            enter = slideInHorizontally(tween(if (reducedMotion) 0 else 240, easing = FastOutSlowInEasing)) { -it },
            exit = slideOutHorizontally(tween(if (reducedMotion) 0 else 180, easing = FastOutSlowInEasing)) { -it }) {
            val channelIndex = remember(screenState.catalog, screenState.allCatalog, screenState.playerReturn,
                screenState.searchSourceId, screenState.category, screenState.onlyFavorites, screenState.favoriteIds) { model.liveIndex() }
            val sources = remember(screenState.sources) { screenState.sources.associate { it.id to it.name } }
            ChannelDrawer(channelIndex, media.entry.id, screenState.locks, sources, layer == PlayerLayer.Channels,
                problemText ?: statusText, failure != null, panelAlpha, pick = { id, returnFocus ->
                    when (val result = model.pickChannel(id)) {
                        is GateResult.NeedsPin -> openPin(returnFocus)
                        is GateResult.Allow -> if (result.entry.id == media.entry.id) {
                            if (failure != null || status == Player.STATE_ENDED) recovery.retryManually()
                            else if (!recoveryState.recovering) perform(PlayerCommand.CloseChannels)
                        }
                        else -> Unit
                    }
                }, close = { perform(PlayerCommand.CloseChannels) })
        }
        if (layer == PlayerLayer.Options || layer == PlayerLayer.Speed) PlayerOptionsDialog(
            picking = layer == PlayerLayer.Speed, speed = speed, opacity = screenState.playback.panelOpacity,
            enabled = !opened.tunneling && player.isCommandAvailable(Player.COMMAND_SET_SPEED_AND_PITCH),
            unavailable = stringResource(if (opened.tunneling) R.string.player_speed_tunneling else R.string.player_speed_unavailable),
            openSpeeds = { interact(); layer = PlayerLayer.Speed },
            choose = { value ->
                speedRequest = value
                if (PlaybackSpeed.apply(player, media.mode, value, opened.tunneling)) perform(PlayerCommand.CloseSpeed)
                else speedRequest = null
            }, dismiss = { perform(PlayerKeyRouter.back(keys())) }, key = ::onKey)
        endState?.let { end -> EpisodeEndDialog(end,
            play = { episodeEnd.playNow() == NextResult.NEEDS_PIN },
            stay = { perform(PlayerCommand.CloseEndCard) },
            returnToEpisodes = { episodeEnd.dismiss(); save(); model.back() },
            replay = { episodeEnd.newMedia(); player.seekTo(0); player.play(); interact() },
            back = { perform(PlayerKeyRouter.back(keys())) }, key = ::onKey) }
    }
}

/**
 * The Ses / Altyazı panel at the right edge. [opacity] lets the picture through its body (Oynatıcı panelleri); while it
 * does, the window behind is not dimmed either, or the picture would show through as a dark grey.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable internal fun TracksDialog(tracks: Tracks, parameters: TrackSelectionParameters, player: ExoPlayer, model: TvViewModel,
    opacity: PanelOpacity, dismiss: () -> Unit) {
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
        val dialogView = LocalView.current
        LaunchedEffect(Unit) {
            snapshotFlow { window.isWindowFocused }.first { it }
            first.requestFocus()
        }
        if (opacity.alpha < 1f) SideEffect { (dialogView.parent as? DialogWindowProvider)?.window?.setDimAmount(0f) }
        Box(Modifier.fillMaxSize().focusProperties { onEnter = { first.requestFocus() } }.focusGroup(), contentAlignment = Alignment.CenterEnd) {
        Column(Modifier.width(440.dp).fillMaxHeight()
            .background(Ink.Background.copy(alpha = opacity.alpha), RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp)).testTag("tracks-panel")) {
        Row(Modifier.fillMaxWidth().padding(start = 28.dp, end = 28.dp, top = 24.dp, bottom = 18.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(stringResource(R.string.tracks_title), Modifier.weight(1f).testTag("tracks-title"), color = Ink.Text,
                fontFamily = Roboto, fontWeight = FontWeight.Medium, fontSize = 24.sp, lineHeight = 30.sp)
            Action(stringResource(R.string.action_close), Modifier.focusRequester(first).testTag("tracks-close"), onClick = dismiss)
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(Ink.Divider))
        CompositionLocalProvider(LocalBringIntoViewSpec provides trackVisibility) {
        Column(Modifier.weight(1f).fillMaxWidth().testTag("tracks-viewport")
            .verticalScroll(rememberScrollState()).padding(horizontal = 28.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)) {
        listOf(C.TRACK_TYPE_AUDIO, C.TRACK_TYPE_TEXT).forEach { type ->
            Text(stringResource(if (type == C.TRACK_TYPE_AUDIO) R.string.tracks_audio else R.string.tracks_subtitles), color = Ink.Mint, fontFamily = Roboto, fontSize = 12.sp)
            if (type == C.TRACK_TYPE_TEXT) TrackChoice(stringResource(R.string.tracks_off), selected = parameters.disabledTrackTypes.contains(type)) {
                player.trackSelectionParameters = player.trackSelectionParameters.buildUpon()
                    .clearOverridesOfType(type).setTrackTypeDisabled(type, true).build()
                model.store.subtitlesOff = true
            }
            val groups = tracks.groups.filter { it.type == type }
            if (groups.isEmpty()) Note(stringResource(if (type == C.TRACK_TYPE_TEXT) R.string.tracks_no_subtitles else R.string.tracks_no_audio))
            var number = 0
            groups.forEach { group ->
                for (index in 0 until group.length) {
                    val format = group.getTrackFormat(index)
                    val supported = group.isTrackSupported(index)
                    val label = trackLabel(format, ++number, type)
                    TrackChoice(label, selected = group.isTrackSelected(index) && !parameters.disabledTrackTypes.contains(type), enabled = supported,
                        status = if (supported) null else stringResource(R.string.tracks_unsupported)) {
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
        Note(stringResource(R.string.tracks_note),
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
    // The status line is never ellipsized, so an unavailable track always says why.
    RadioRow(label, selected, description = status, enabled = enabled, maxLines = 5, onLabelOverflow = { overflow = it }, onClick = onClick)
    if (overflow) Action(stringResource(R.string.tracks_full_description), Modifier.focusRequester(explanation)) { reading = true }
    }
    if (reading) {
        val reader = remember { FocusRequester() }
        val close = remember { FocusRequester() }
        fun dismissReading() { reading = false; restore = true }
        SourceDialog(stringResource(R.string.tracks_description_title), reader, ::dismissReading, width = 560.dp) {
            RemoteTextReader(label, Modifier.weight(1f, fill = false).focusRequester(reader)
                .focusProperties { down = close }, "track-description-reader")
            Action(stringResource(R.string.action_back), Modifier.focusRequester(close).focusProperties { up = reader }
                .testTag("track-description-back"), onClick = ::dismissReading)
        }
    }
}

@Composable @ReadOnlyComposable
private fun trackLabel(format: Format, number: Int, type: Int): String {
    val language = languageName(format.language)
        ?: stringResource(if (type == C.TRACK_TYPE_AUDIO) R.string.tracks_audio_fallback else R.string.tracks_subtitles)
    val channels = if (format.channelCount > 0) when (format.channelCount) {
        1 -> stringResource(R.string.tracks_mono); 2 -> stringResource(R.string.tracks_stereo); 6 -> "5.1"; 8 -> "7.1"
        else -> pluralStringResource(R.plurals.tracks_channels, format.channelCount, format.channelCount.toString())
    } else ""
    return listOf("$language · $number", format.label.orEmpty(), channels).filter { it.isNotBlank() }.distinct().joinToString(" · ")
}

@Composable private fun selectedTrackSummary(tracks: Tracks, parameters: TrackSelectionParameters): String {
    fun selectedFormat(type: Int): Format? = tracks.groups.filter { it.type == type }.firstNotNullOfOrNull { group ->
        (0 until group.length).firstOrNull { group.isTrackSelected(it) }?.let { group.getTrackFormat(it) }
    }
    val audio = selectedFormat(C.TRACK_TYPE_AUDIO)
    val subtitle = selectedFormat(C.TRACK_TYPE_TEXT)?.takeUnless { C.TRACK_TYPE_TEXT in parameters.disabledTrackTypes }
    val audioLabel = audio?.let { languageName(it.language) ?: stringResource(R.string.tracks_audio_fallback) }
        ?: stringResource(R.string.tracks_no_audio)
    val subtitleLabel = subtitle?.let { languageName(it.language) ?: stringResource(R.string.tracks_subtitles) }
        ?: stringResource(R.string.player_subtitles_off)
    return "$audioLabel · $subtitleLabel"
}
