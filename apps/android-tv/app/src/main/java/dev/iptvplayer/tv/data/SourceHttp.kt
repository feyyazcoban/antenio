package dev.iptvplayer.tv.data

import dev.iptvplayer.tv.BuildConfig
import okhttp3.Request

/**
 * How NEXTV asks a source's servers for something, in one place (gap plan T5): the list and account calls, the guide
 * downloads and the streams. Each still sends the User-Agent it always has; S18 brings them to D066's one versioned
 * form and adds the source's own connection settings here.
 */
internal object SourceHttp {
    /** What the list, account and guide downloads have sent since the pilot. */
    private const val DOWNLOAD_AGENT = "NEXTV AndroidTV"

    /** The streams' User-Agent (D066). */
    val streamAgent = "NEXTV/${BuildConfig.VERSION_NAME} AndroidTV"

    /** A call for an M3U list, or for an Xtream account, one of its lists or a title's details. */
    fun list(url: String): Request = download(url)

    /** An XMLTV guide download. */
    fun guide(url: String): Request = download(url)

    /** The headers [entry]'s stream is opened with besides [streamAgent]: its own, as an M3U line can carry them. */
    fun streamHeaders(entry: MediaEntry): Map<String, String> = entry.headers

    private fun download(url: String) = Request.Builder().url(url).header("User-Agent", DOWNLOAD_AGENT).build()
}
