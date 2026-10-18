package dev.iptvplayer.tv.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.ui.layout.onSizeChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.tv.material3.*
import coil3.compose.AsyncImage
import dev.iptvplayer.tv.R
import dev.iptvplayer.tv.TvState
import dev.iptvplayer.tv.TvViewModel
import dev.iptvplayer.tv.data.*

/**
 * One season's shelf, kept outside Compose state like the library's focus memory: the row's scroll position,
 * the episode it remembers (what its counter shows and where Up/Down enter it) and a requester per episode.
 */
private class SeasonShelf(start: Int) {
    val row = LazyListState(firstVisibleItemIndex = start)
    val position = mutableIntStateOf(start + 1)
    private val cards = HashMap<Int, FocusRequester>()
    fun card(index: Int) = cards.getOrPut(index) { FocusRequester() }
    fun focusRemembered(): Boolean = try { cards[position.intValue - 1]?.requestFocus() == true } catch (_: IllegalStateException) { false }
}

@Composable internal fun DetailScreen(s: TvState, model: TvViewModel, settings: () -> Unit) {
    val entry = s.selected ?: return
    val series = entry.kind == MediaKind.SERIES
    val shown = rememberShownTitle(entry.title, entry.year)
    val first = remember { FocusRequester() }
    val back = remember { FocusRequester() }
    // The detail of a locked title asks again once the session ended (the app left the screen): the PIN pad opens over
    // it, and focus goes back to the button or episode that asked (pin.md §3.6).
    val openPin = rememberPinOpener()
    val playFocus = remember { FocusRequester() }
    val restartFocus = remember { FocusRequester() }
    val retryFocus = remember { FocusRequester() }
    val visibleEpisodes = remember(s.episodes) { s.episodes.filter { !it.adult } }
    // Provider order, one shelf per season.
    val seasons = remember(visibleEpisodes) { visibleEpisodes.groupBy { it.season }.toList() }
    val restored = remember(entry.id) { s.playing?.entry?.id?.takeIf { id -> visibleEpisodes.any { it.id == id } } }
    val list = rememberLazyListState()
    var description by remember(entry.id) { mutableStateOf(false) }
    var restoreDescription by remember { mutableStateOf(false) }
    val descriptionFocus = remember { FocusRequester() }
    val window = LocalWindowInfo.current
    LaunchedEffect(description, restoreDescription) {
        if (!description && restoreDescription) {
            snapshotFlow { window.isWindowFocused }.first { it }
            descriptionFocus.requestFocus()
            restoreDescription = false
        }
    }
    val history = s.history.firstOrNull { it.parentId == entry.id }
    val watched = s.episodes.firstOrNull { it.id == history?.id && !it.adult }
    val next = when {
        watched != null && history?.progress?.completed == true ->
            s.sourceFor(watched)?.let { EpisodeSequence.next(it, watched, s.episodes)?.entry } ?: watched
        watched != null -> watched
        else -> s.episodes.firstOrNull { !it.adult }
    }
    val mark = if (!series) model.progress(entry) else next?.let(model::progress)
    val resume = mark?.resumable == true
    val canPlay = !s.detailLoading && (!series || next != null)
    // As in TiviMate, focus lands on an episode: the one just played, else the one Oynat would start.
    val target = if (series) (restored ?: next?.id)?.takeIf { id -> visibleEpisodes.any { it.id == id } } else null
    val shelves = remember(entry.id, visibleEpisodes) { HashMap<Int, SeasonShelf>() }
    fun shelf(season: Int, episodes: List<MediaEntry>) = shelves.getOrPut(season) {
        SeasonShelf(episodes.indexOfFirst { it.id == target }.coerceAtLeast(0))
    }
    val actionVisibility = rememberCardVisibility()
    val scope = rememberCoroutineScope()
    val detailsView = remember { BringIntoViewRequester() }
    var detailsHeight by remember { mutableIntStateOf(0) }
    var menuFocused by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    var viewportHeight by remember { mutableStateOf(0.dp) }

    // The backdrop runs to the top edge; the Geri row and the text keep the menu's top margin themselves.
    Navigation(s, model, Modifier.onFocusChanged { menuFocused = it.hasFocus }, contentEntry = { first }, contentTop = 0.dp, settings = settings) {
        Box(Modifier.fillMaxSize()) {
        if (entry.hasBackdrop) AsyncImage(SourceArtwork(entry.sourceId, entry.backdrop!!), null, Modifier.matchParentSize(), contentScale = ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(0f to Ink.Background.copy(alpha = .98f),
            .30f to Ink.Background.copy(alpha = .96f), .48f to Ink.Background.copy(alpha = .9f), .7f to Ink.Background.copy(alpha = .45f), 1f to Ink.Background.copy(alpha = .28f))))
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0f to Color.Transparent, .55f to Color.Transparent, 1f to Ink.Background)))
        if (!series && !entry.hasBackdrop && entry.artwork != null) Artwork(entry.sourceId, entry.artwork, shownName(entry.title),
            Modifier.align(Alignment.CenterEnd).padding(end = TvLayout.Gutter).width(180.dp).aspectRatio(2f / 3)
                .clip(RoundedCornerShape(TvLayout.CardRadius)).testTag("detail-poster-fallback"))
        // While scrolled, the line cut under the fixed Geri row fades out instead of resting half visible.
        LazyColumn(Modifier.fillMaxSize().padding(top = 52.dp + TvLayout.HeaderTop)
            .onSizeChanged { viewportHeight = with(density) { it.height.toDp() } }
            .scrollEdgeMask(top = { list.canScrollBackward }).testTag("detail-content"),
            state = list, contentPadding = PaddingValues(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)) {
            item(key = "details") {
                Box(Modifier.fillMaxWidth().heightIn(min = if (series) 206.dp else 350.dp)
                    .onSizeChanged { detailsHeight = it.height }
                    // Coming back up from the episodes shows the whole title group again when it fits. A
                    // bring-into-view request joins the focus relocation queue instead of racing it.
                    .bringIntoViewRequester(detailsView)
                    .onFocusChanged { f ->
                        if (f.hasFocus && list.canScrollBackward && detailsHeight <= list.layoutInfo.viewportSize.height)
                            scope.launch { detailsView.bringIntoView() }
                    }
                    .padding(horizontal = TvLayout.Gutter)) {
                    Column(Modifier.width(if (series) 700.dp else 560.dp).padding(top = if (series) 8.dp else 34.dp),
                        verticalArrangement = Arrangement.spacedBy(if (series) 12.dp else 14.dp)) {
                        Text(stringResource(if (series) R.string.detail_series_kicker else R.string.detail_movie_kicker), color = Ink.Mint,
                            fontFamily = Roboto, fontWeight = FontWeight.Medium, fontSize = 12.sp, letterSpacing = 1.2.sp)
                        Text(shown.name, fontFamily = Roboto, color = Ink.Text, fontWeight = FontWeight.Medium,
                            fontSize = if (series) 34.sp else 36.sp, lineHeight = if (series) 39.sp else 41.sp,
                            maxLines = 2, overflow = TextOverflow.Ellipsis)
                        val seasonCount = s.episodes.map { it.season }.distinct().size
                        val metadata = listOf(if (s.sources.size > 1) s.sourceName(entry.sourceId) else "", entry.ratingText?.let { "★ $it" }.orEmpty(), shown.year, shown.tag,
                            if (entry.durationSeconds > 0) stringResource(R.string.runtime_minutes, (entry.durationSeconds / 60).toString()) else "",
                            if (series && !s.detailLoading) pluralStringResource(R.plurals.detail_seasons, seasonCount, seasonCount.toString()) else "",
                            // What is left of a started film (U18).
                            if (!series && resume) remainingText(mark!!.positionMs, mark.durationMs) else "").filter { it.isNotBlank() }
                        if (metadata.isNotEmpty()) Text(metadata.joinToString(" · "), color = Ink.Muted, fontFamily = Roboto, fontSize = 14.sp, lineHeight = 20.sp)
                        // Most M3U titles carry no plot; a placeholder sentence on each of them is only noise.
                        if (entry.description.isNotBlank()) Text(entry.description,
                            Modifier.widthIn(max = 560.dp), color = Ink.Muted, fontFamily = Roboto, fontSize = 15.sp, lineHeight = 22.sp,
                            maxLines = if (series) 2 else 3, overflow = TextOverflow.Ellipsis)
                        if (s.detailLoading) Note(stringResource(R.string.detail_loading))
                        s.detailError?.let { Note(it.resolve(), error = true) }
                        FlowRow(Modifier.then(actionVisibility.container).then(actionVisibility.focus).focusGroup().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (canPlay) Action(stringResource(if (resume) R.string.action_resume else R.string.action_play),
                                Modifier.focusRequester(playFocus).then(if (target == null) Modifier.focusRequester(first) else Modifier), primary = true, icon = "play") {
                                if (model.play(if (series) next!! else entry)) openPin { playFocus }
                            }
                            if (s.detailError != null) Action(stringResource(R.string.action_retry),
                                Modifier.focusRequester(retryFocus).then(if (!canPlay && target == null) Modifier.focusRequester(first) else Modifier)) {
                                if (model.retryDetail()) openPin { retryFocus }
                            }
                            // The label names what OK does; the filled star shows that it is a favourite now.
                            val favorite = entry.id in s.favoriteIds
                            Action(stringResource(if (favorite) R.string.favorite_remove else R.string.detail_favorite_add), icon = "star", iconFilled = favorite) { model.toggleFavorite(entry) }
                            if (entry.description.isNotBlank()) Action(stringResource(R.string.detail_description), Modifier.focusRequester(descriptionFocus).testTag("open-description")) { description = true }
                            // On the same 12 dp grid as the other actions, and only while playback is offered.
                            if (canPlay && resume && !series) Action(stringResource(R.string.detail_restart), Modifier.focusRequester(restartFocus)) {
                                if (model.play(entry, restart = true)) openPin { restartFocus }
                            }
                        }
                    }

                }
            }
            if (series) {
                if (!s.detailLoading && seasons.isEmpty()) item(key = "no-episodes") {
                    Note(stringResource(R.string.detail_no_episodes), modifier = Modifier.padding(horizontal = TvLayout.Gutter))
                }
                items(seasons, key = { "season:${it.first}" }) { (season, episodes) ->
                    SeasonRow(season, episodes, entry.title, shelf(season, episodes), target, first, viewportHeight, model::progress) { episode, card ->
                        if (model.play(episode)) openPin { card }
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(Ink.Background.copy(alpha = .82f), Color.Transparent)))
            .padding(start = TvLayout.Gutter, end = TvLayout.Gutter, top = 5.dp + TvLayout.HeaderTop, bottom = 5.dp), verticalAlignment = Alignment.CenterVertically) {
            Action(stringResource(R.string.action_back), Modifier.focusRequester(back), icon = "back") { model.back() }
        }
    }
    }
    LaunchedEffect(entry.id, s.detailLoading) {
        if (!s.detailLoading) {
            withFrameNanos { }
            // A target beyond the first shelf opens on its season; the first season stays under the details.
            val season = seasons.indexOfFirst { (_, episodes) -> episodes.any { it.id == target } }
            if (season > 0 && list.layoutInfo.visibleItemsInfo.none { it.key == "season:${seasons[season].first}" }) list.scrollToItem(1 + season)
            withFrameNanos { }; withFrameNanos { }
            // A viewer already in the side menu keeps it; the finished load does not pull focus back.
            if (menuFocused) return@LaunchedEffect
            val focused = (canPlay || s.detailError != null || target != null) &&
                try { first.requestFocus() } catch (_: IllegalStateException) { false }
            if (!focused) back.requestFocus()
        }
    }
    if (description) {
        val reader = remember { FocusRequester() }
        val close = remember { FocusRequester() }
        fun dismissDescription() { description = false; restoreDescription = true }
        SourceDialog(stringResource(R.string.detail_description), reader, ::dismissDescription, width = 600.dp) {
            RemoteTextReader(shownName(entry.title) + "\n\n" + entry.description,
                Modifier.weight(1f, fill = false).focusRequester(reader).focusProperties { down = close }, "detail-description-reader")
            Action(stringResource(R.string.action_back), Modifier.focusRequester(close).focusProperties { up = reader }.testTag("detail-description-back"), onClick = ::dismissDescription)
        }
    }
}

/**
 * "Sezon N" with the remembered episode's place ("2 / 10") and one row of 16:9 episodes. Up and Down enter a
 * row on the episode it remembers; moving along a row redraws only its counter.
 */
@Composable private fun SeasonRow(season: Int, episodes: List<MediaEntry>, show: String, shelf: SeasonShelf, target: String?, first: FocusRequester,
    viewportHeight: Dp, progress: (MediaEntry) -> PlaybackMark?, play: (episode: MediaEntry, card: FocusRequester) -> Unit) {
    val density = LocalDensity.current
    var headingHeight by remember { mutableStateOf(0.dp) }
    Column(Modifier.fillMaxWidth().testTag("season:$season"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // As the library heading ("Tüm filmler  30 / 80"): the counter right beside the title, on the dark side
        // of the backdrop, not on its bright right edge.
        Row(Modifier.fillMaxWidth().padding(horizontal = TvLayout.Gutter).onSizeChanged { headingHeight = with(density) { it.height.toDp() } },
            verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(stringResource(R.string.detail_season, season.toString()), color = Ink.Text, fontFamily = Roboto, fontWeight = FontWeight.Medium,
                fontSize = 18.sp, lineHeight = 24.sp, maxLines = 1)
            PositionNote(shelf.position, episodes.size, Modifier.testTag("season-position:$season"))
        }
        LazyRow(Modifier.fillMaxWidth()
            .focusProperties {
                onEnter = {
                    if (requestedFocusDirection == FocusDirection.Up || requestedFocusDirection == FocusDirection.Down) shelf.focusRemembered()
                }
            }.focusGroup(),
            state = shelf.row, horizontalArrangement = Arrangement.spacedBy(TvLayout.Gap),
            contentPadding = PaddingValues(horizontal = TvLayout.Gutter, vertical = 4.dp)) {
            itemsIndexed(episodes, key = { _, episode -> episode.id }) { index, episode ->
                EpisodeCard(episode, show, progress(episode), Modifier.focusRequester(shelf.card(index))
                    .then(if (episode.id == target) Modifier.focusRequester(first) else Modifier)
                    .onFocusChanged { if (it.isFocused) shelf.position.intValue = index + 1 },
                    headingHeight + 8.dp, viewportHeight) { play(episode, shelf.card(index)) }
            }
        }
    }
}

@Composable private fun EpisodeCard(entry: MediaEntry, show: String, mark: PlaybackMark?, modifier: Modifier, headingContext: Dp, viewportHeight: Dp, play: () -> Unit) {
    // The season heading comes into view with the episode when both fit; the details above are not pushed away.
    val visibility = rememberCardVisibility(horizontalPadding = TvLayout.Gutter, contextAbove = headingContext,
        viewportHeight = viewportHeight, frameShelf = false)
    val resume = mark?.resumable == true
    val status = listOf(if (entry.durationSeconds > 0) stringResource(R.string.runtime_minutes, (entry.durationSeconds / 60).toString()) else "",
        when { resume -> stringResource(R.string.episode_in_progress); mark?.completed == true -> stringResource(R.string.episode_watched); else -> "" })
        .filter { it.isNotBlank() }
    Card(onClick = play, modifier = modifier.width(TvLayout.LandscapeWidth).then(visibility.container).then(visibility.focus).testTag("episode:${entry.id}"),
            shape = CardDefaults.shape(shape = RoundedCornerShape(TvLayout.CardRadius)),
            colors = CardDefaults.colors(containerColor = Ink.Container, focusedContainerColor = Ink.High),
            scale = CardDefaults.scale(focusedScale = 1f),
            border = CardDefaults.border(focusedBorder = focusRing(TvLayout.CardRadius))) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.fillMaxWidth().aspectRatio(16f / 9).clip(RoundedCornerShape(TvLayout.CardRadius))) {
                Artwork(entry.sourceId, entry.artwork, entry.title, Modifier.fillMaxSize(), showFallbackTitle = false)
                if (resume && mark!!.durationMs > 0) Box(Modifier.align(Alignment.BottomStart).fillMaxWidth().height(3.dp).background(Ink.High)) {
                    Box(Modifier.fillMaxHeight().fillMaxWidth((mark.positionMs.toFloat() / mark.durationMs).coerceIn(0f, 1f)).background(Ink.Mint))
                }
            }
            // The episode's own name, without the show's name and SxxEyy the provider repeats on every card. "E1."
            // only for a number the source states; with no name left, "Bölüm 1" alone.
            val numbered = entry.episodeOrderKnown && entry.episode > 0
            val name = UnnamedTitle.text(entry.title)?.resolve() ?: remember(entry.title, show) { episodeDisplayTitle(entry.title, show) }.ifBlank { null }
            Row(Modifier.padding(horizontal = 10.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (numbered && name != null) Text(stringResource(R.string.episode_number_short, entry.episode.toString()), color = Ink.Muted, fontFamily = Roboto, fontWeight = FontWeight.Medium,
                    fontSize = 12.sp, lineHeight = 17.sp, maxLines = 1)
                Text(name ?: if (numbered) stringResource(R.string.episode_number, entry.episode.toString()) else entry.title, Modifier.weight(1f).testTag("episode-title:${entry.id}"),
                    fontFamily = Roboto, color = Ink.Text, fontSize = 12.sp, lineHeight = 17.sp,
                    minLines = 2, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Text(status.joinToString(" · "),
                Modifier.padding(start = 10.dp, end = 10.dp, bottom = 10.dp).testTag("episode-metadata:${entry.id}"),
                fontFamily = Roboto, color = if (resume) Ink.Mint else Ink.Muted, fontSize = 12.sp, lineHeight = 16.sp,
                minLines = 1, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
