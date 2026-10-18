package dev.iptvplayer.tv

import android.os.SystemClock
import android.provider.Settings
import androidx.test.platform.app.InstrumentationRegistry
import dev.iptvplayer.tv.data.AndroidPinClock
import org.junit.Assert.*
import org.junit.Test

/** Reads the device's own counters only; writes nothing. */
class AndroidPinClockTest {
    private val resolver get() = InstrumentationRegistry.getInstrumentation().targetContext.contentResolver

    @Test fun bootCountStaysAndTimeSinceBootOnlyMovesForward() {
        val clock = AndroidPinClock(resolver)
        val before = SystemClock.elapsedRealtime()
        val first = clock.now()
        val second = clock.now()
        val after = SystemClock.elapsedRealtime()
        assertEquals(Settings.Global.getInt(resolver, Settings.Global.BOOT_COUNT, 0).coerceAtLeast(0).toLong(), first.boot)
        assertEquals(first.boot, second.boot)
        assertTrue(first.elapsedMs in before..second.elapsedMs)
        assertTrue(second.elapsedMs <= after)
    }
}
