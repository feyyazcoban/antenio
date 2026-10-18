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
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
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
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.tv.material3.Text
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
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
import dev.iptvplayer.tv.data.displayTitle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalFoundationApi::class)
@Composable internal fun PlayerScreen(media: PlayingMedia, model: TvViewModel, back: () -> Unit) {
    val context = LocalContext.current
    // The corner notes are written when a key arrives, outside composition.
    val resources = LocalResources.current
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
    val player = remember { PlayerFactory.create(context, model.store) }
    val scope = rememberCoroutineScope()
    val recovery = remember(player) { PlaybackRecovery(player, scope) }
    val recoveryState by recovery.state.collectAsStateWithLifecycle()
    val failure = recoveryState.failure
    var active by remember { mutableStateOf<PlayingMedia?>(null) }
    // What the viewer has open over the picture: the controls, the tracks panel or nothing.
    var layer by remember { mutableStateOf<PlayerLayer>(PlayerLayer.Controls) }
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
    var digits by remember { mutableStateOf("") }
    fun flash(text: String, detail: String? = null) { note = text to detail; noteAt = System.currentTimeMillis() }
    fun zap(direction: Int) { quiet = layer == PlayerLayer.Video; digits = ""; model.changeChannel(direction) }
    fun seekBy(deltaMs: Long) {
        if (!player.isCurrentMediaItemSeekable) return
        val end = player.duration.takeIf { it > 0 } ?: Long.MAX_VALUE
        val target = (player.currentPosition + deltaMs).coerceIn(0, end)
        player.seekTo(target); position = target
        flash(resources.getString(if (deltaMs < 0) R.string.player_seek_back else R.string.player_seek_forward, (kotlin.math.abs(deltaMs) / 1000).toString()),
            "${clockTime(target)} / ${if (duration > 0) clockTime(duration) else "—"}")
    }
    // The player as PlayerKeyRouter reads it when a key or Back arrives; its commands are carried out here.
    fun keys(repeat: Int = 0) = KeyContext(media.mode, top(), recoveryState.recovering, failure != null, player.isPlaying, repeat)
    fun perform(command: PlayerCommand) {
        when (command) {
            PlayerCommand.Play -> player.play()
            PlayerCommand.Pause -> player.pause()
            PlayerCommand.Retry -> recovery.retryManually()
            PlayerCommand.StopRecovery -> recovery.cancel()
            PlayerCommand.Consume -> Unit
            is PlayerCommand.Zap -> zap(command.direction)
            is PlayerCommand.AppendDigit -> { digits = PlayerKeyRouter.appendDigit(digits, command.digit); flash(digits) }
            PlayerCommand.OpenGuide -> model.guide()
            is PlayerCommand.Seek -> seekBy(command.deltaMs)
            PlayerCommand.ShowControls -> layer = PlayerLayer.Controls
            PlayerCommand.HideControls -> layer = PlayerLayer.Video
            PlayerCommand.CloseTracks -> { layer = PlayerLayer.Controls; interact() }
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
    LaunchedEffect(noteAt) { if (note != null) { delay(2_500); note = null } }
    LaunchedEffect(digits) {
        if (digits.isEmpty()) return@LaunchedEffect
        delay(PlayerKeyRouter.DIGIT_COMMIT_MS)
        val number = digits.toInt(); digits = ""
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
            override fun onPlaybackStateChanged(playbackState: Int) {
                status = playbackState
                if (playbackState == Player.STATE_ENDED) {
                    layer = PlayerLayer.Controls
                    position = player.currentPosition.coerceAtLeast(0); duration = player.duration.coerceAtLeast(0)
                    save()
                    if (model.state.value.parental.prompt != null) endedUnderPin = true else episodeEnded()
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
                    // Should the lock screen cover this player, the one that comes back after it stands as this one did.
                    model.playerStopped(paused = !player.playWhenReady)
                    save(); recovery.background(); episodeEnd.background()
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
        onDispose { save(); recovery.close(); episodeEnd.close(); lifecycle.removeObserver(observer); player.removeListener(listener); player.release() }
    }

    LaunchedEffect(media) {
        val request = PlayerFactory.request(media)
        save()
        // Stop the old request before opening a new channel: one source session at a time.
        player.stop(); player.clearMediaItems()
        recovery.newMedia(request.mode == PlaybackMode.LIVE)
        episodeEnd.newMedia(); endedUnderPin = false; hasPlayed = false
        tracks = Tracks.EMPTY; position = request.startMs; duration = 0
        if (layer == PlayerLayer.Tracks) layer = PlayerLayer.Controls
        active = media
        if (quiet) {
            quiet = false; lastInteraction = System.currentTimeMillis()
            flash(listOfNotNull(model.channelNumber(media.entry)?.toString(), displayTitle(media.entry.title).name).joinToString("  "))
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
            position = player.currentPosition.coerceAtLeast(0)
            duration = player.duration.coerceAtLeast(0)
            if (++ticks % 10 == 0) save()
            // The PIN pad over the picture takes the remote's keys: the controls that opened it stay until it closes,
            // and for the usual while after, so focus can go back to them.
            if (model.state.value.parental.prompt != null) lastInteraction = System.currentTimeMillis()
            if (player.isPlaying && layer == PlayerLayer.Controls && recovery.state.value.failure == null &&
                !recovery.state.value.recovering && System.currentTimeMillis() - lastInteraction > 8000) layer = PlayerLayer.Video
        }
    }
    LaunchedEffect(top()) {
        if (top() == PlayerLayer.Controls || top() == PlayerLayer.Video) { withFrameNanos { }; if (layer == PlayerLayer.Video) canvas.requestFocus() else first.requestFocus() }
    }
    LaunchedEffect(endedUnderPin, screenState.parental.prompt == null) {
        if (endedUnderPin && screenState.parental.prompt == null) { endedUnderPin = false; episodeEnded(needsPin = true) }
    }
    LaunchedEffect(screenState.autoNextEpisode) { episodeEnd.preferenceChanged(screenState.autoNextEpisode) }
    LaunchedEffect(nextChoice) { episodeEnd.availabilityChanged(nextChoice) }
    LaunchedEffect(recoveryState.recovering, failure) {
        if (recoveryState.recovering || failure != null) { layer = PlayerLayer.Controls; interact() }
    }
    BackHandler {
        // A Back that arrives after the model left the player, before this screen is gone, is the new route's.
        if (model.state.value.route != Route.PLAYER) return@BackHandler back()
        perform(PlayerKeyRouter.back(keys()))
    }
    Box(Modifier.fillMaxSize().background(Color.Black).onPreviewKeyEvent(::onKey)) {
        AndroidView(factory = { PlayerView(it).apply {
            this.player = player; useController = false; keepScreenOn = true
            isFocusable = false; descendantFocusability = android.view.ViewGroup.FOCUS_BLOCK_DESCENDANTS
            setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
        } }, modifier = Modifier.fillMaxSize(), onRelease = { it.player = null })
        Box(Modifier.fillMaxSize().focusRequester(canvas).focusable(layer == PlayerLayer.Video))
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
            Row(Modifier.align(Alignment.Center).offset { IntOffset(if (layer == PlayerLayer.Tracks) (-220).dp.roundToPx() else 0, 0) }
                .background(Ink.Background.copy(alpha = .9f), RoundedCornerShape(12.dp)).padding(20.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LoadingArc()
                Text(stringResource(R.string.player_opening), color = Ink.Text, fontFamily = Roboto, fontSize = 16.sp)
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
                Text(stringResource(if (live) R.string.player_kicker_live else if (media.entry.kind == MediaKind.EPISODE) R.string.player_kicker_series
                    else R.string.player_kicker_movie),
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
                    val clock = "${clockTime(position)} / ${if (duration > 0) clockTime(duration) else "—"}"
                    Text(when {
                        recoveryState.recovering -> recoveryState.message.resolve()
                        failure != null -> failure.resolve()
                        opening && live -> stringResource(R.string.player_opening)
                        live -> stringResource(R.string.player_live)
                        opening -> stringResource(R.string.text_pair, clock, stringResource(R.string.player_opening))
                        status == Player.STATE_ENDED -> stringResource(R.string.text_pair, clock, stringResource(R.string.player_ended))
                        else -> clock
                    }, Modifier.weight(1f), color = if (failure != null && !recoveryState.recovering) Ink.Error else Ink.Text,
                        fontFamily = Roboto, fontSize = 14.sp, lineHeight = 19.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(when {
                        recoveryState.recovering -> stringResource(R.string.player_keys_recovering)
                        failure != null -> ""
                        live -> stringResource(R.string.player_keys_live)
                        else -> stringResource(R.string.player_keys_vod)
                    }, color = Ink.Muted, fontFamily = Roboto, fontSize = 12.sp, lineHeight = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (live && failure == null && !recoveryState.recovering) {
                    Text(currentProgramme?.let { "${it.title} · ${programmeTime(it, model.guideZone())}" } ?: stringResource(R.string.player_no_programme),
                        Modifier.padding(horizontal = 5.dp).testTag("player-current-programme"), color = Ink.Text, fontFamily = Roboto,
                        fontSize = 14.sp, lineHeight = 19.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    schedule?.next(guideNow)?.let { next -> Text(stringResource(R.string.player_next_programme, guideTime(next.start, model.guideZone()), next.title),
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
                    Action(stringResource(if (recoveryState.recovering) R.string.action_stop else if (failure != null) R.string.action_retry else if (playing) R.string.player_pause
                        else if (status == Player.STATE_ENDED) R.string.action_replay else R.string.action_play),
                        Modifier.focusRequester(first), primary = true) {
                        interact()
                        if (recoveryState.recovering) recovery.cancel()
                        else if (failure != null) recovery.retryManually()
                        else if (playing) player.pause()
                        else { if (status == Player.STATE_ENDED) { episodeEnd.newMedia(); player.seekTo(0) }; player.play() }
                    }
                    if (live) {
                        Action(stringResource(R.string.player_channel_previous)) { model.changeChannel(-1) }
                        Action(stringResource(R.string.player_channel_next)) { model.changeChannel(1) }
                    } else {
                        Action(stringResource(R.string.player_seek_back, "10"), enabled = player.isCurrentMediaItemSeekable) { interact(); player.seekTo((player.currentPosition - 10_000).coerceAtLeast(0)) }
                        Action(stringResource(R.string.player_seek_forward, "10"), enabled = player.isCurrentMediaItemSeekable) { interact(); player.seekTo((player.currentPosition + 10_000).coerceAtMost(duration.takeIf { it > 0 } ?: Long.MAX_VALUE)) }
                    }
                    Action(stringResource(R.string.player_tracks), icon = "sound", enabled = failure == null && !recoveryState.recovering) { interact(); layer = PlayerLayer.Tracks }
                    // The shared star icon at icon size, filled while it is a favourite; the meaning stays in semantics.
                    val favoriteLabel = stringResource(when {
                        favoriteTarget == null -> R.string.player_favorite_unavailable
                        media.entry.kind == MediaKind.EPISODE -> if (isFavorite) R.string.player_series_favorite_remove else R.string.player_series_favorite_add
                        else -> if (isFavorite) R.string.favorite_remove else R.string.favorite_add
                    })
                    val favoriteState = stringResource(if (isFavorite) R.string.player_in_favorites else R.string.player_not_in_favorites)
                    Action("", Modifier.testTag("player-favorite").semantics {
                        contentDescription = favoriteLabel
                        stateDescription = favoriteState
                    }, icon = "star", iconFilled = isFavorite, selected = isFavorite, enabled = favoriteTarget != null) { model.toggleFavorite(media.entry); interact() }
                    if (nextChoice != null) Action(stringResource(R.string.player_next_episode), Modifier.focusRequester(nextEpisodeFocus)) {
                        save()
                        screenState.sourceFor(media.entry)?.let { source ->
                            if (model.playNextEpisode(source.id, media.entry.id, nextChoice.entry.id) == NextResult.NEEDS_PIN) openPin { nextEpisodeFocus }
                        }
                    }
                    Action(stringResource(R.string.action_close)) { save(); model.back() }
                }
                }
            }
        }
        if (layer == PlayerLayer.Tracks) TracksDialog(tracks, trackParameters, player, model) { perform(PlayerCommand.CloseTracks) }
        endState?.let { end -> EpisodeEndDialog(end,
            play = { episodeEnd.playNow() == NextResult.NEEDS_PIN },
            stay = { perform(PlayerCommand.CloseEndCard) },
            returnToEpisodes = { episodeEnd.dismiss(); save(); model.back() },
            replay = { episodeEnd.newMedia(); player.seekTo(0); player.play(); interact() },
            back = { perform(PlayerKeyRouter.back(keys())) }, key = ::onKey) }
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
    val choice = stringResource(if (selected) R.string.tracks_selected else R.string.tracks_not_selected)
    LaunchedEffect(reading, restore) {
        if (!reading && restore) {
            snapshotFlow { window.isWindowFocused }.first { it }
            explanation.requestFocus()
            restore = false
        }
    }
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Button(onClick = onClick, modifier = Modifier.focusProperties { canFocus = enabled }.fillMaxWidth()
        .semantics { role = Role.RadioButton; this.selected = selected; stateDescription = choice },
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
