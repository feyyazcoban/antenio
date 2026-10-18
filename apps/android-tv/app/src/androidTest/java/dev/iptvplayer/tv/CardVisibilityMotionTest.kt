package dev.iptvplayer.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.BringIntoViewSpec
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.requireLayoutCoordinates
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.relocation.BringIntoViewModifierNode
import androidx.compose.ui.relocation.bringIntoView
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.Text
import dev.iptvplayer.tv.ui.rememberCardVisibility
import kotlinx.coroutines.CancellationException
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/** Real scroll/relocation, synthetic geometry, no source or network. Font scale stays unchanged. */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
class CardVisibilityMotionTest {
    @get:Rule val ui = createComposeRule()

    @Test fun firstDpadRequestAlreadyIncludesTheWholeShelfCardAndMargins() {
        val requests = Requests()
        var density = 1f
        lateinit var position: () -> Int
        ui.setContent {
            density = LocalDensity.current.density
            val scroll = rememberLazyListState()
            position = { scroll.firstVisibleItemIndex * 10_000 + scroll.firstVisibleItemScrollOffset }
            CompositionLocalProvider(LocalBringIntoViewSpec provides remember { object : BringIntoViewSpec {} }) {
                LazyColumn(Modifier.width(300.dp).height(300.dp).testTag("viewport"), state = scroll) {
                    item {
                        Box(Modifier.height(300.dp)) {
                            Button({}, Modifier.testTag("hero")) { Text("Watch") }
                        }
                    }
                    item {
                        Column(Modifier.then(ObserveRequests(requests))) {
                            Box(Modifier.height(24.dp).testTag("heading"))
                            Spacer(Modifier.height(6.dp))
                            LazyRow(contentPadding = PaddingValues(horizontal = 24.dp, vertical = 4.dp)) {
                                item {
                                    val visibility = rememberCardVisibility(horizontalPadding = 24.dp,
                                        contextAbove = 34.dp, viewportHeight = 300.dp, frameShelf = false, normalizeFocusRequest = true)
                                    Column(Modifier.width(180.dp).then(visibility.container).testTag("whole-card")) {
                                        Card({}, Modifier.fillMaxWidth().height(120.dp).then(visibility.focus).testTag("card"),
                                            scale = CardDefaults.scale(focusedScale = 1f)) {}
                                        Spacer(Modifier.height(8.dp))
                                        Box(Modifier.height(32.dp))
                                    }
                                }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(300.dp)) }
                }
            }
        }
        ui.onNodeWithTag("hero").performSemanticsAction(SemanticsActions.RequestFocus)
        ui.waitForIdle()
        // Real beyond-bounds focus search: no card query, pre-scroll or direct card focus before Down.
        ui.onNodeWithTag("hero").performKeyInput { pressKey(Key.DirectionDown) }
        ui.waitForIdle()
        assertTrue("Down must issue a relocation request", requests.rects.isNotEmpty())
        val first = requests.rects.first()
        android.util.Log.i("CardMotionCheck", "firstShelfRect=$first density=$density all=${requests.rects}")
        assertEquals("The FIRST request includes card, caption and both margins", 228f, first.height / density, 1f)
        assertEquals(-7f, first.top / density, 1f)
        assertEquals(221f, first.bottom / density, 1f)
        requests.rects.forEach { rect ->
            assertEquals("Every later request keeps the same shelf target", first.top, rect.top, 1f)
            assertEquals("No second request extends the first scroll target", first.bottom, rect.bottom, 1f)
        }
        ui.onNodeWithTag("card").assertIsFocused()
        val frame = ui.onNodeWithTag("viewport").getUnclippedBoundsInRoot()
        val card = ui.onNodeWithTag("whole-card").getUnclippedBoundsInRoot()
        assertEquals(frame.bottom.value - 27f, card.bottom.value, 1f)
        assertTrue(ui.onNodeWithTag("heading").getUnclippedBoundsInRoot().top >= frame.top)
        var settled = 0
        ui.runOnIdle { settled = position() }
        ui.mainClock.advanceTimeBy(300)
        ui.runOnIdle { assertEquals("Late equal-target requests must not scroll again", settled, position()) }
    }

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
                    val visibility = rememberCardVisibility(frameShelf = false, normalizeFocusRequest = true)
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

    private class Requests { var started = 0; var completed = 0; var cancelled = 0; val rects = mutableListOf<Rect>() }
    private data class ObserveRequests(val requests: Requests) : ModifierNodeElement<RequestNode>() {
        override fun create() = RequestNode(requests)
        override fun update(node: RequestNode) { node.requests = requests }
        override fun InspectorInfo.inspectableProperties() { name = "testRelocationObserver" }
    }
    private class RequestNode(var requests: Requests) : Modifier.Node(), BringIntoViewModifierNode {
        override suspend fun bringIntoView(childCoordinates: LayoutCoordinates, boundsProvider: () -> Rect?) {
            if (childCoordinates.isAttached) boundsProvider()?.let {
                requests.rects += it.translate(requireLayoutCoordinates().localPositionOf(childCoordinates, Offset.Zero))
            }
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
