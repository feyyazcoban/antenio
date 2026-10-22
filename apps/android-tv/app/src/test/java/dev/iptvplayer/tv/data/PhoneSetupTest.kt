package dev.iptvplayer.tv.data

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.HttpURLConnection
import java.net.URL
import java.util.Random

/** The phone way of adding a source (SETUP_FLOW, D117). Every address and account here is synthetic. */
class PhoneSetupTest {
    @Test fun formDecodesFieldsAndKeepsAccountOnlyForXtream() {
        val fields = PhoneSetupForm.parse("code=123456&type=xtream&address=http%3A%2F%2Fexample.test%3A8080&username=demo" +
            "&password=p%26ss+word&name=Salon&epg=")
        val sent = PhoneSetupForm.submission(fields)!!
        assertEquals(SourceType.XTREAM, sent.type)
        assertEquals("http://example.test:8080", sent.address)
        assertEquals("p&ss word", sent.password)
        assertEquals("Salon", sent.name)
        val m3u = PhoneSetupForm.submission(fields + ("type" to "m3u"))!!
        assertEquals("", m3u.username); assertEquals("", m3u.password)
        assertTrue("REDACTED" in sent.toString()); assertTrue("demo" !in sent.toString())
    }

    @Test fun formRejectsUnknownTypeAndMissingAddress() {
        assertNull(PhoneSetupForm.submission(mapOf("type" to "ftp", "address" to "http://example.test")))
        assertNull(PhoneSetupForm.submission(mapOf("type" to "m3u", "address" to "  ")))
    }

    @Test fun sessionAcceptsOnceExpiresAndLocks() {
        val session = PhoneSetupSession("123456", startedAt = 0)
        assertEquals(PhoneSetupSession.Check.WRONG_CODE, session.check("000000", 1_000))
        assertEquals(PhoneSetupSession.Check.ACCEPTED, session.check("123 456", 2_000))
        assertEquals(PhoneSetupSession.Check.USED, session.check("123456", 3_000))

        val late = PhoneSetupSession("123456", startedAt = 0)
        assertEquals(PhoneSetupSession.Check.EXPIRED, late.check("123456", PhoneSetupSession.LIFETIME_MS))

        val guessed = PhoneSetupSession("123456", startedAt = 0)
        repeat(PhoneSetupSession.MAX_TRIES - 1) { assertEquals(PhoneSetupSession.Check.WRONG_CODE, guessed.check("111111", 1)) }
        assertEquals(PhoneSetupSession.Check.LOCKED, guessed.check("222222", 1))
        assertEquals(PhoneSetupSession.Check.LOCKED, guessed.check("123456", 1))
    }

    @Test fun codesAreSixDigits() {
        val random = Random(7)
        repeat(50) { assertTrue(PhoneSetupSession.newCode(random).matches(Regex("\\d{6}"))) }
    }

    @Test fun serverServesPageAndTakesOneSubmission() = runBlocking {
        var received: PhoneSubmission? = null
        val server = PhoneSetupServer(page = { "<html>setup</html>".toByteArray() }, clock = { 1_000 }) { received = it }
        server.session = PhoneSetupSession("654321", startedAt = 0)
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        try {
            val port = server.start(scope, preferredPort = 0)
            fun post(body: String): Int = (URL("http://127.0.0.1:$port/send").openConnection() as HttpURLConnection).run {
                requestMethod = "POST"; doOutput = true
                setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                outputStream.use { it.write(body.toByteArray()) }
                responseCode.also { disconnect() }
            }
            val page = (URL("http://127.0.0.1:$port/").openConnection() as HttpURLConnection)
            assertEquals(200, page.responseCode); page.disconnect()
            assertEquals(403, post("code=000000&type=m3u&address=http%3A%2F%2Fexample.test%2Flist.m3u"))
            assertNull(received)
            assertEquals(200, post("code=654321&type=m3u&address=http%3A%2F%2Fexample.test%2Flist.m3u"))
            assertEquals("http://example.test/list.m3u", received?.address)
            assertEquals(410, post("code=654321&type=m3u&address=http%3A%2F%2Fexample.test%2Fother.m3u"))
        } finally { server.stop(); scope.cancel() }
    }
}
