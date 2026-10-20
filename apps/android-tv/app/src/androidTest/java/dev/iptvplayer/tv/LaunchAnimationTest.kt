package dev.iptvplayer.tv

import android.graphics.Bitmap
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import dev.iptvplayer.tv.ui.LaunchCover
import dev.iptvplayer.tv.ui.LocalReducedMotion
import dev.iptvplayer.tv.ui.PilotTheme
import dev.iptvplayer.tv.ui.RestoringCover
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/** Drawn logo frames and lifecycle, with a controlled clock and synthetic readiness. No font-scale overrides. */
class LaunchAnimationTest {
    @get:Rule val ui = createComposeRule()

    @Test fun logoMovesBeforeContentPreparationAndWaitsForRestore() {
        var loading by mutableStateOf(true)
        var preparations = 0
        var finished = false
        val starts = java.util.concurrent.atomic.AtomicInteger()
        ui.mainClock.autoAdvance = false
        try {
            ui.setContent { PilotTheme {
                LaunchCover(false, loading, prepareContent = { preparations++ }, entranceStarted = { starts.incrementAndGet() }, finish = { finished = true })
            } }
            // The controlled animation clock starts only after an actual black warm-up frame has been submitted.
            ui.mainClock.advanceTimeByFrame(); ui.waitForIdle()
            ui.waitUntil(5_000) { starts.get() == 1 }
            ui.mainClock.advanceTimeBy(400); ui.waitForIdle()
            val before = logoPixels("before")
            ui.mainClock.advanceTimeBy(800); ui.waitForIdle()
            val mark = logoPixels("mark")
            ui.mainClock.advanceTimeBy(800); ui.waitForIdle()
            val letters = logoPixels("letters")
            assertEquals("Content preparation must not interrupt the entrance", 0, preparations)
            assertFalse(finished)
            assertEquals("The entrance starts on black", 0, before)
            assertTrue("The play mark must appear in an intermediate frame", mark > 100)
            assertTrue("The letters must add visible pixels after the mark", letters > mark)
            ui.mainClock.advanceTimeBy(1800); ui.waitForIdle()
            val complete = logoPixels("complete")
            assertTrue("The completed wordmark must be visible", complete >= letters)
            ui.onNodeWithTag("launch-loading").assertExists()
            assertEquals("Restore still owns the wait", 0, preparations)
            assertFalse(finished)
            ui.runOnIdle { loading = false }
            ui.mainClock.advanceTimeBy(600); ui.waitForIdle()
            assertEquals(1, preparations)
            assertEquals("Entrance starts once, independently of restore completion", 1, starts.get())
            assertTrue("Ready content must be revealed", finished)
            android.util.Log.i("LaunchMotionCheck", "logoPixels=$before,$mark,$letters,$complete; prepare=$preparations; finished=$finished")
        } finally { ui.mainClock.autoAdvance = true }
    }

    @Test fun reducedMotionWaitsForRestoreWithoutAnEntranceDelay() {
        var loading by mutableStateOf(true)
        var prepared = false
        var finished = false
        var starts = 0
        ui.mainClock.autoAdvance = false
        try {
            ui.setContent { PilotTheme { CompositionLocalProvider(LocalReducedMotion provides true) {
                LaunchCover(true, loading, prepareContent = { prepared = true }, entranceStarted = { starts++ }, finish = { finished = true })
            } } }
            ui.mainClock.advanceTimeBy(100); ui.waitForIdle()
            ui.onNodeWithTag("launch-loading").assertExists()
            assertFalse(prepared)
            assertFalse(finished)
            ui.runOnIdle { loading = false }
            ui.mainClock.advanceTimeBy(100); ui.waitForIdle()
            assertTrue(prepared)
            assertTrue("Reduced motion has no three-second minimum or fade", finished)
            assertEquals("A still cover starts once without GPU warm-up or an entrance delay", 1, starts)
        } finally { ui.mainClock.autoAdvance = true }
    }

    @Test fun preparingSpinnerMovesWhenMotionIsEnabled() = checkPreparingSpinner(reduced = false)

    @Test fun preparingSpinnerIsStillOnlyWhenMotionIsReduced() = checkPreparingSpinner(reduced = true)

    private fun checkPreparingSpinner(reduced: Boolean) {
        ui.mainClock.autoAdvance = false
        try {
            ui.setContent { PilotTheme { CompositionLocalProvider(LocalReducedMotion provides reduced) { RestoringCover() } } }
            ui.mainClock.advanceTimeBy(600); ui.waitForIdle()
            val before = ui.onNodeWithTag("launch-loading").captureToImage().asAndroidBitmap()
            ui.mainClock.advanceTimeBy(333); ui.waitForIdle()
            val after = ui.onNodeWithTag("launch-loading").captureToImage().asAndroidBitmap()
            assertEquals(before.width, after.width)
            assertEquals(before.height, after.height)
            val a = IntArray(before.width * before.height)
            val b = IntArray(after.width * after.height)
            before.getPixels(a, 0, before.width, 0, 0, before.width, before.height)
            after.getPixels(b, 0, after.width, 0, 0, after.width, after.height)
            val changed = a.indices.count { a[it] != b[it] }
            if (reduced) assertEquals("Reduced motion shows a fixed arc", 0, changed)
            else assertTrue("Preparing must show actual changing spinner pixels: $changed", changed > 50)
        } finally { ui.mainClock.autoAdvance = true }
    }

    private fun logoPixels(name: String): Int {
        val bitmap = ui.onRoot().captureToImage().asAndroidBitmap()
        val directory = InstrumentationRegistry.getInstrumentation().targetContext.filesDir.resolve("splash-preview").apply { mkdirs() }
        directory.resolve("$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        // Only the centre logo; corner glows and the loading label cannot satisfy the motion check.
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        var count = 0
        for (y in bitmap.height * 33 / 100 until bitmap.height * 67 / 100)
            for (x in bitmap.width * 40 / 100 until bitmap.width * 60 / 100)
                if ((pixels[y * bitmap.width + x] shr 8 and 255) > 160) count++
        return count
    }
}
