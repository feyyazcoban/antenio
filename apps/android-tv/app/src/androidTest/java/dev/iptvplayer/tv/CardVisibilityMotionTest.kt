package dev.iptvplayer.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.BringIntoViewSpec
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.requireLayoutCoordinates
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.relocation.BringIntoViewModifierNode
import androidx.compose.ui.relocation.bringIntoView
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import dev.iptvplayer.tv.ui.rememberCardVisibility
import kotlinx.coroutines.CancellationException
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/** Real scroll/relocation, synthetic geometry, no source or network. Font scale stays unchanged. */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
class CardVisibilityMotionTest {
    @get:Rule val ui = createComposeRule()

    @Test fun revealingTheCardDoesNotCancelItsRemainingSafetyMarginScroll() {
        val requests = Requests()
        val offsets = mutableListOf<Int>()
        var viewport by mutableStateOf(300.dp)
        lateinit var position: () -> Int
        ui.setContent {
            val scroll = rememberScrollState()
            position = { scroll.value }
            CompositionLocalProvider(LocalBringIntoViewSpec provides remember { object : BringIntoViewSpec {} }) {
                Column(Modifier.width(300.dp).height(viewport).testTag("viewport").verticalScroll(scroll).then(ObserveRequests(requests))) {
                    Box(Modifier.size(100.dp).testTag("before").focusable())
                    Spacer(Modifier.height(150.dp))
                    val visibility = rememberCardVisibility(frameShelf = false)
                    Box(Modifier.size(200.dp, 100.dp).then(visibility.container)) {
                        Box(Modifier.fillMaxSize().background(Color.Blue).testTag("card").then(visibility.focus).focusable())
                    }
                    Spacer(Modifier.height(300.dp))
                }
            }
        }
        ui.onNodeWithTag("before").performSemanticsAction(SemanticsActions.RequestFocus)
        ui.waitForIdle()
        ui.mainClock.autoAdvance = false
        try {
            ui.onNodeWithTag("card").performSemanticsAction(SemanticsActions.RequestFocus)
            repeat(90) {
                ui.mainClock.advanceTimeByFrame(); ui.waitForIdle()
                ui.runOnIdle { offsets += position() }
            }
        } finally { ui.mainClock.autoAdvance = true }
        android.util.Log.i("CardMotionCheck", "starts=${requests.started}, cancelled=${requests.cancelled}, completed=${requests.completed}, offsets=$offsets")
        assertTrue("The padded card request must run", requests.started > 0)
        assertEquals("A card becoming visible must not interrupt its remaining 27dp margin scroll", 0, requests.cancelled)
        assertTrue("Scroll must not reverse on its way to the same card", offsets.zipWithNext().all { (a, b) -> b >= a })
        fun assertMargin() {
            val card = ui.onNodeWithTag("card").getUnclippedBoundsInRoot()
            val frame = ui.onNodeWithTag("viewport").getUnclippedBoundsInRoot()
            assertTrue("The full card and its 27dp margin must be visible", card.bottom <= frame.bottom - 26.dp)
        }
        assertMargin()
        // Keeping the motion alive must not lose the original resize/visibility correction.
        ui.runOnIdle { viewport = 240.dp }
        ui.waitForIdle(); assertMargin()
        ui.onNodeWithTag("card").assertIsFocused()
    }

    private class Requests { var started = 0; var completed = 0; var cancelled = 0 }
    private data class ObserveRequests(val requests: Requests) : ModifierNodeElement<RequestNode>() {
        override fun create() = RequestNode(requests)
        override fun update(node: RequestNode) { node.requests = requests }
        override fun InspectorInfo.inspectableProperties() { name = "testRelocationObserver" }
    }
    private class RequestNode(var requests: Requests) : Modifier.Node(), BringIntoViewModifierNode {
        override suspend fun bringIntoView(childCoordinates: LayoutCoordinates, boundsProvider: () -> Rect?) {
            // The framework also reveals the focused button. Observe only the larger full-card + margin request.
            val padded = (boundsProvider()?.height ?: 0f) > childCoordinates.size.height + 1f
            if (padded) requests.started++
            try {
                bringIntoView {
                    if (!childCoordinates.isAttached) null else boundsProvider()?.translate(
                        requireLayoutCoordinates().localPositionOf(childCoordinates, Offset.Zero))
                }
                if (padded) requests.completed++
            } catch (cancelled: CancellationException) {
                if (padded) requests.cancelled++
                throw cancelled
            }
        }
    }
}
