package dev.iptvplayer.tv.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.net.ProtocolException

class ArtworkRetryTest {
    @Test fun aDroppedConnectionIsRetriedTwice() {
        val dropped = ProtocolException("unexpected end of stream")
        assertEquals(1_000L, artworkRetryDelay(0, dropped))
        assertEquals(3_000L, artworkRetryDelay(1, dropped))
        assertNull(artworkRetryDelay(2, dropped))
    }

    @Test fun otherErrorsAreNotRetried() {
        assertNull(artworkRetryDelay(0, IllegalStateException("HTTP 404")))
        assertNull(artworkRetryDelay(0, null))
    }
}
