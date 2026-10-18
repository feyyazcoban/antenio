package dev.iptvplayer.tv.data

import dev.iptvplayer.tv.BuildConfig
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.TimeUnit

class SourceHttpTest {
    private val emptySink = object : EpgSink { override fun channel(value: EpgChannel) {}; override fun programme(value: EpgProgramme) {} }

    /** Until S18 each request keeps the User-Agent it has always sent. */
    @Test fun listGuideAndStreamKeepTheirUserAgents() = runBlocking {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody("#EXTM3U\n#EXTINF:-1,Kanal\nhttp://127.0.0.1/live/1.ts\n"))
            server.enqueue(MockResponse().setBody("<tv><channel id='c'><display-name>Kanal</display-name></channel></tv>"))
            server.start()
            SourceRepository().load(SourceConfig(name = "Fixture", type = SourceType.M3U, address = server.url("/list").toString())) {}
            EpgRepository().download(server.url("/guide").toString(), GuideWindow(0, Long.MAX_VALUE), emptySink)
            repeat(2) { assertEquals("NEXTV AndroidTV", server.takeRequest(3, TimeUnit.SECONDS)!!.getHeader("User-Agent")) }
        }
        assertEquals("NEXTV/${BuildConfig.VERSION_NAME} AndroidTV", SourceHttp.streamAgent)
        val channel = MediaEntry("m3u:1", "m3u:1", MediaKind.LIVE, "Kanal", headers = mapOf("Referer" to "http://127.0.0.1/"))
        assertEquals(channel.headers, SourceHttp.streamHeaders(channel))
    }
}
