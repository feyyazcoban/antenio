package dev.iptvplayer.tv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import dev.iptvplayer.tv.R
import dev.iptvplayer.tv.data.LiveIndex
import dev.iptvplayer.tv.data.ParentalLocks
import kotlinx.coroutines.flow.first

private class ChannelDrawerFocus {
    var id: String? = null
    var index = 0
    var initialized = false
    var closeFocused = false
}

/** One overlay over the existing player. Browsing changes focus only; OK is the sole channel pick. */
@Composable internal fun ChannelDrawer(index: LiveIndex, playingId: String, locks: ParentalLocks,
    sourceNames: Map<String, String>, active: Boolean, problem: String?, failed: Boolean, opacity: Float,
    pick: (String, () -> FocusRequester?) -> Unit, close: () -> Unit) {
    val channels = index.channels
    val list = rememberLazyListState()
    val attached = remember { mutableStateMapOf<String, FocusRequester>() }
    val closeFocus = remember { FocusRequester() }
    val memory = remember { ChannelDrawerFocus() }
    val title = stringResource(R.string.player_channels)
    // Refreshes can remove a focused row; channel playback changes alone never reset focus or scroll.
    LaunchedEffect(active, channels) {
        if (!active) { memory.initialized = false; return@LaunchedEffect }
        val opening = !memory.initialized
        if (!opening && memory.closeFocused && (memory.id == null || channels.any { it.id == memory.id })) return@LaunchedEffect
        val selected = if (opening) channels.indexOfFirst { it.id == playingId }.coerceAtLeast(0)
            else channels.indexOfFirst { it.id == memory.id }.takeIf { it >= 0 }
                ?: memory.index.coerceIn(0, channels.lastIndex.coerceAtLeast(0))
        memory.initialized = true
        if (channels.isEmpty()) { withFrameNanos { }; closeFocus.requestFocus() }
        else {
            val id = channels[selected].id
            if (!opening && memory.id == id && attached[id] != null) return@LaunchedEffect
            list.scrollToItem((selected - 2).coerceAtLeast(0))
            snapshotFlow { attached[id] }.first { it != null }
            withFrameNanos { }
            if (!runCatching { attached[id]?.requestFocus() == true }.getOrDefault(false)) closeFocus.requestFocus()
        }
    }
    Column(Modifier.width(400.dp).fillMaxHeight().background(Ink.Background.copy(alpha = opacity))
        .then(if (active) Modifier else Modifier.clearAndSetSemantics { })
        .semantics { paneTitle = title }.testTag("player-channel-drawer")
        .focusProperties { onExit = { if (active) cancelFocusChange() } }.focusGroup()
        .padding(horizontal = 24.dp, vertical = 24.dp)) {
        Row(Modifier.fillMaxWidth().padding(bottom = 16.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(title, Modifier.weight(1f), color = Ink.Text, fontFamily = Roboto, fontSize = 24.sp, fontWeight = FontWeight.Medium)
            Action(stringResource(R.string.action_close), Modifier.focusRequester(closeFocus).focusProperties { canFocus = active }
                .onFocusChanged { memory.closeFocused = it.isFocused }
                .testTag("player-channels-close"), compact = true) { if (active) close() }
        }
        if (problem != null) Text(problem, Modifier.fillMaxWidth().padding(bottom = 12.dp)
            .semantics { liveRegion = LiveRegionMode.Polite }.testTag("player-channel-problem"),
            color = if (failed) Ink.Error else Ink.Muted, fontFamily = Roboto, fontSize = 14.sp, lineHeight = 20.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
        if (channels.isEmpty()) Text(stringResource(R.string.player_channels_empty), color = Ink.Muted,
            fontFamily = Roboto, fontSize = 16.sp, lineHeight = 22.sp)
        else LazyColumn(Modifier.fillMaxWidth().weight(1f).testTag("player-channel-list"), state = list,
            contentPadding = PaddingValues(6.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(channels, key = { _, entry -> entry.id }) { rowIndex, entry ->
                val requester = remember { FocusRequester() }
                DisposableEffect(entry.id) { attached[entry.id] = requester; onDispose { attached.remove(entry.id) } }
                val current = entry.id == playingId
                val locked = locks.asksPin(entry.id)
                val name = rememberShownTitle(entry.title, "").name
                val number = index.numbers[entry.id]?.toString().orEmpty()
                val playing = stringResource(R.string.player_channel_playing)
                val source = sourceNames[entry.sourceId].orEmpty().takeIf { sourceNames.size > 1 }.orEmpty()
                val spoken = if (locked) stringResource(R.string.lock_named, name) else name
                Surface(onClick = { if (active) pick(entry.id) { attached[entry.id] ?: closeFocus } },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp).focusRequester(requester)
                        .onFocusChanged { if (it.isFocused) { memory.id = entry.id; memory.index = rowIndex } }
                        .focusProperties { canFocus = active }
                        .semantics { contentDescription = "$number $spoken"; selected = current; if (current) stateDescription = playing }
                        .testTag("player-channel:${entry.id}"),
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
                    colors = ClickableSurfaceDefaults.colors(containerColor = if (current) Ink.Selected else Ink.Container,
                        contentColor = Ink.Text, focusedContainerColor = Ink.Mint, focusedContentColor = Ink.OnMint),
                    border = ClickableSurfaceDefaults.border(focusedBorder = focusRing(12.dp)),
                    scale = ClickableSurfaceDefaults.scale(focusedScale = 1f)) {
                    Row(Modifier.fillMaxWidth().heightIn(min = 72.dp).padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(number, Modifier.widthIn(min = 20.dp), color = LocalContentColor.current.copy(alpha = .7f),
                            fontFamily = Roboto, fontSize = 13.sp, maxLines = 1)
                        Artwork(entry.sourceId, entry.artwork.takeUnless { locked }, name, Modifier.size(44.dp, 36.dp), logo = true)
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(name, color = LocalContentColor.current, fontFamily = Roboto, fontSize = 15.sp,
                                lineHeight = 20.sp, fontWeight = FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            val secondary = if (current) playing else source
                            if (secondary.isNotEmpty()) Text(secondary, color = LocalContentColor.current.copy(alpha = .7f),
                                fontFamily = Roboto, fontSize = 12.sp, lineHeight = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        if (locked) LockMark(color = LocalContentColor.current)
                        else if (current) Box(Modifier.size(6.dp).background(LocalContentColor.current, RoundedCornerShape(50)))
                    }
                }
            }
        }
    }
}
