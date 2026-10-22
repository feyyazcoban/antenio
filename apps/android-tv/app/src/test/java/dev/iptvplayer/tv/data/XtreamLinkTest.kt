package dev.iptvplayer.tv.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** A panel's get.php export loads as its Xtream account. Every address and account here is synthetic. */
class XtreamLinkTest {
    @Test fun getPhpLinkBecomesItsAccount() {
        val link = SourceConfig(id = "a", name = "Ev", type = SourceType.M3U, epgUrl = "http://example.test/epg.xml",
            address = "http://example.test:8080/get.php?username=demo%40mail.test&password=12345&type=m3u_plus&output=ts")
        val account = link.asXtreamAccount()!!
        assertEquals(SourceType.XTREAM, account.type)
        assertEquals("http://example.test:8080/", account.address)
        assertEquals("demo@mail.test", account.username)
        assertEquals("12345", account.password)
        assertEquals("a", account.id); assertEquals("Ev", account.name); assertEquals("http://example.test/epg.xml", account.epgUrl)
        assertEquals("http://example.test:8080/player_api.php?username=demo%40mail.test&password=12345", account.validated().apiUrl().toString())
    }

    @Test fun otherLinksStayLists() {
        assertNull(SourceConfig(name = "", type = SourceType.M3U, address = "http://example.test/list.m3u").asXtreamAccount())
        assertNull(SourceConfig(name = "", type = SourceType.M3U, address = "http://example.test/get.php?username=demo").asXtreamAccount())
        assertNull(SourceConfig(name = "", type = SourceType.XTREAM, address = "http://example.test/get.php?username=a&password=b",
            username = "a", password = "b").asXtreamAccount())
    }
}
