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
        assertEquals(FailureReason.SERVER_CONNECT_TIMEOUT, networkFailure(SocketTimeoutException("connect timed out")))
        assertEquals(FailureReason.SERVER_CONNECT_TIMEOUT, networkFailure(SocketTimeoutException(
            "failed to connect to /203.0.113.5 (port 8080) from /192.168.1.126 (port 40000) after 15000ms")))
        assertEquals(FailureReason.SERVER_TIMEOUT, networkFailure(SocketTimeoutException("timeout")))
        assertEquals(FailureReason.SERVER_TIMEOUT, networkFailure(InterruptedIOException("timeout")))
        assertEquals(FailureReason.SECURE_CONNECTION_FAILED, networkFailure(SSLHandshakeException("handshake")))
        assertEquals(FailureReason.SERVER_UNREACHABLE, networkFailure(IOException("reset")))
    }
}

/** A whole list waits longer for its first byte than an account check does (some providers build it for a minute). */
class PatientListTest {
    @org.junit.Test fun listWaitsPastTheShortReadTimeout() = kotlinx.coroutines.runBlocking {
        okhttp3.mockwebserver.MockWebServer().use { server ->
            server.enqueue(okhttp3.mockwebserver.MockResponse().setHeadersDelay(1500, java.util.concurrent.TimeUnit.MILLISECONDS)
                .setBody("#EXTM3U\n#EXTINF:-1 group-title=\"Haber\",Kanal\nhttp://example.test/live/1.ts\n"))
            val impatient = okhttp3.OkHttpClient.Builder().readTimeout(500, java.util.concurrent.TimeUnit.MILLISECONDS).build()
            val source = SourceConfig(name = "Fixture", type = SourceType.M3U, address = server.url("/list.m3u").toString())
            val catalog = SourceRepository(impatient).load(source) {}
            assertEquals(1, catalog.items.size)
        }
    }
}
