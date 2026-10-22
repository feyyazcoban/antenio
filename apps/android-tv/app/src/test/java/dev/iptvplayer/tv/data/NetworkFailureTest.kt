package dev.iptvplayer.tv.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException

/** A failed connection names its cause instead of the one "Sunucuya ulaşılamadı" for all of them. */
class NetworkFailureTest {
    @Test fun eachConnectionFailureHasItsOwnReason() {
        assertEquals(FailureReason.SERVER_NAME_NOT_FOUND, networkFailure(UnknownHostException("example.test")))
        assertEquals(FailureReason.SERVER_REFUSED, networkFailure(ConnectException("refused")))
        assertEquals(FailureReason.SERVER_TIMEOUT, networkFailure(SocketTimeoutException("connect timed out")))
        assertEquals(FailureReason.SERVER_TIMEOUT, networkFailure(InterruptedIOException("timeout")))
        assertEquals(FailureReason.SECURE_CONNECTION_FAILED, networkFailure(SSLHandshakeException("handshake")))
        assertEquals(FailureReason.SERVER_UNREACHABLE, networkFailure(IOException("reset")))
    }
}
