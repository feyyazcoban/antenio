package dev.iptvplayer.tv.ui

import androidx.compose.runtime.*
import androidx.compose.ui.focus.FocusRequester
import dev.iptvplayer.tv.data.ChannelScope

internal enum class ChannelColumn { CHANNELS, CATEGORIES, PROGRAMMES }

/** Local presentation state. Row focus is deliberately not observable at the screen level. */
@Stable internal class ChannelPanelState {
    var scope by mutableStateOf(ChannelScope())
    var column by mutableStateOf(ChannelColumn.CHANNELS)
    var session by mutableIntStateOf(0)
        private set
    var page by mutableStateOf<Pair<Int, Int>?>(null)
        private set
    var number by mutableStateOf<Pair<Int, Int>?>(null)
        private set
    var focusRequest by mutableIntStateOf(0)
        private set
    var focusedId: String? = null
    var focusedIndex = 0
    // Focus can move implicitly while a column/dialog disappears. Keep the requested destination independent.
    var focusTarget: Pair<String?, Int>? = null
    var visibleRows = 1
    var returnFocus: () -> FocusRequester? = { null }
    private var request = 0
    fun open(scope: ChannelScope, playing: String) {
        this.scope = scope; column = ChannelColumn.CHANNELS; focusedId = playing; focusedIndex = 0
        focusTarget = playing to 0
        page = null; number = null; session++
    }
    fun channels() = restore(focusedId, focusedIndex)
    fun restore(id: String?, index: Int) {
        focusTarget = id to index; focusedId = id; focusedIndex = index
        column = ChannelColumn.CHANNELS; focusRequest++
    }
    fun page(direction: Int) { page = ++request to direction }
    fun number(number: Int) { this.number = ++request to number }
}

internal object PanelPaging {
    fun target(index: Int, direction: Int, visible: Int, count: Int): Int =
        if (count <= 0) 0 else (index + direction * (visible - 1).coerceAtLeast(1)).coerceIn(0, count - 1)
}

internal object PanelAutoClose {
    const val TIMEOUT_MS = 15_000L
    fun shouldClose(now: Long, lastInput: Long, timeoutMs: Long?, talkBack: Boolean): Boolean =
        timeoutMs != null && !talkBack && now >= lastInput && now - lastInput >= timeoutMs
}
