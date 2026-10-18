package dev.iptvplayer.tv.data

import android.content.ContentResolver
import android.os.SystemClock
import android.provider.Settings

/** Where ParentalGuard reads the time for the PIN wait; the JVM tests pass a fake. */
internal fun interface PinClockSource { fun now(): PinClock }

/**
 * The TV's boot count and time since boot (D048). Neither moves when the clock is set, and the boot count tells a
 * reboot from a restarted app, so a wait outlives both but no clock change shortens it.
 *
 * Some TV firmware may not report the boot count. Then every boot reads as boot 0 and only a time since boot lower
 * than the last one seen gives a reboot away: after a reboot that has run longer than the TV did before, the wait
 * can lose at most what was left of it. This is the fallback pin.md §6 accepts and PIN_ATTEMPTS has to state.
 */
internal class AndroidPinClock(private val resolver: ContentResolver) : PinClockSource {
    override fun now(): PinClock {
        val boot = runCatching { Settings.Global.getInt(resolver, Settings.Global.BOOT_COUNT, -1) }.getOrDefault(-1)
        return PinClock(boot.coerceAtLeast(0).toLong(), SystemClock.elapsedRealtime())
    }
}
