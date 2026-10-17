package dev.iptvplayer.tv.data

import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler
import java.io.*
import java.nio.charset.CodingErrorAction
import javax.xml.parsers.SAXParserFactory

/** Streamed XMLTV normalisation. Nothing outside the requested window is retained. */
class XmltvParser(private val language: String = "tr") {
    fun parse(input: InputStream, window: GuideWindow, sink: EpgSink, checkpoint: () -> Unit = {}): EpgParseReport {
        val handler = Handler(window, sink, checkpoint, language)
        val reader = SAXParserFactory.newInstance().apply { isNamespaceAware = true }.newSAXParser().xmlReader
        // External declarations are removed by SafeProlog before SAX sees them. The resolver is a second barrier.
        listOf("http://xml.org/sax/features/external-general-entities", "http://xml.org/sax/features/external-parameter-entities",
            "http://apache.org/xml/features/nonvalidating/load-external-dtd").forEach {
            try { reader.setFeature(it, false) } catch (_: SAXException) { /* Android/JVM feature sets differ. */ }
        }
        reader.entityResolver = org.xml.sax.EntityResolver { _, _ -> throw SourceFailure(FailureReason.GUIDE_EXTERNAL_DEFINITION) }
        reader.contentHandler = handler
        reader.errorHandler = handler
        try {
            SafeProlog.open(input, checkpoint).use { reader.parse(InputSource(it)) }
            if (!handler.rootSeen) throw SourceFailure(FailureReason.GUIDE_NOT_XMLTV)
        } catch (e: java.util.concurrent.CancellationException) { throw e }
        catch (e: SourceFailure) { throw e }
        catch (_: Exception) { throw SourceFailure(FailureReason.GUIDE_UNREADABLE) }
        return handler.report()
    }

    private class Handler(val window: GuideWindow, val sink: EpgSink, val checkpoint: () -> Unit, val language: String) : DefaultHandler() {
        var rootSeen = false
        var depth = 0
        var channels = 0; var programmes = 0; var invalid = 0; var outside = 0; var seen = 0
        var channelId = ""; var names = mutableListOf<String>()
        var start: Long? = null; var end: Long? = null; var badEnd = false
        var title = ""; var description = ""; var category = ""
        var titleRank = -1; var descriptionRank = -1; var categoryRank = -1
        var field = ""; var rank = 0; val text = StringBuilder()
        var parent = ""
        override fun startElement(uri: String?, localName: String, qName: String?, a: Attributes) {
            checkpoint()
            depth++
            if (depth > 32) throw SourceFailure(FailureReason.GUIDE_TOO_DEEP)
            if (depth == 1) {
                if (localName != "tv" || !uri.isNullOrEmpty()) throw SourceFailure(FailureReason.GUIDE_NOT_XMLTV)
                rootSeen = true
            }
            if (depth == 2) {
                parent = localName
                channelId = (a.getValue(if (parent == "channel") "id" else "channel") ?: "").trim()
                if (channelId.length > 512) throw SourceFailure(FailureReason.GUIDE_CHANNEL_ID_TOO_LONG)
                if (parent == "channel") {
                    if (++channels > 20_000) throw SourceFailure(FailureReason.GUIDE_TOO_MANY_CHANNELS)
                    names = mutableListOf()
                } else if (parent == "programme") {
                    if (++seen > 1_000_000) throw SourceFailure(FailureReason.GUIDE_TOO_MANY_PROGRAMMES)
                    start = XmltvTime.parse(a.getValue("start"))
                    val stop = a.getValue("stop")
                    end = XmltvTime.parse(stop)
                    badEnd = !stop.isNullOrBlank() && end == null
                    title = ""; description = ""; category = ""
                    titleRank = -1; descriptionRank = -1; categoryRank = -1
                }
            }
            if (depth == 3 && ((parent == "channel" && localName == "display-name") ||
                    (parent == "programme" && localName in listOf("title", "desc", "category")))) {
                field = localName; text.setLength(0)
                val lang = a.getValue("lang").orEmpty().substringBefore('-').substringBefore('_')
                rank = when { lang.equals(language, true) -> 2; lang.isEmpty() -> 1; else -> 0 }
            }
        }
        override fun characters(ch: CharArray, offset: Int, length: Int) {
            if (field.isNotEmpty()) {
                if (text.length + length > 16_384) throw SourceFailure(FailureReason.GUIDE_TEXT_TOO_LONG)
                text.append(ch, offset, length)
            }
        }
        override fun endElement(uri: String?, localName: String, qName: String?) {
            if (depth == 3 && field.isNotEmpty()) {
                val value = text.toString().trim()
                if (value.isNotEmpty()) when (field) {
                    "display-name" -> if (names.size < 32 && value !in names) names.add(value)
                    "title" -> if (rank > titleRank) { title = value; titleRank = rank }
                    "desc" -> if (rank > descriptionRank) { description = value; descriptionRank = rank }
                    "category" -> if (rank > categoryRank) { category = value; categoryRank = rank }
                }
                field = ""
            }
            if (depth == 2) when (parent) {
                "channel" -> if (channelId.isNotEmpty()) sink.channel(EpgChannel(channelId, names.toList())) else invalid++
                "programme" -> {
                    val s = start; val e = end
                    if (channelId.isEmpty() || title.isEmpty() || s == null || badEnd || (e != null && e <= s)) invalid++
                    else if (s >= window.until || (e ?: s) < window.from || e == window.from) outside++
                    else { sink.programme(EpgProgramme(channelId, title, s, e, description, category)); programmes++ }
                }
            }
            depth--
        }
        override fun error(e: org.xml.sax.SAXParseException) { throw e }
        override fun fatalError(e: org.xml.sax.SAXParseException) { throw e }
        fun report() = EpgParseReport(channels, programmes, invalid, outside)
    }
}

/** XMLTV's conventional external DOCTYPE is accepted without fetching it. Internal subsets are rejected
 * before the XML parser can expand entities. UTF-8 and BOM-marked UTF-16 are supported explicitly. */
private object SafeProlog {
    fun open(input: InputStream, checkpoint: () -> Unit): Reader {
        val stream = PushbackInputStream(input, 4)
        val bytes = ByteArray(4)
        var size = 0
        while (size < 4) { val n = stream.read(bytes, size, 4 - size); if (n < 0) break; size += n }
        fun at(i: Int) = if (i < size) bytes[i].toInt() and 255 else -1
        val (charset, skip) = when {
            at(0) == 255 && at(1) == 254 && at(2) == 0 && at(3) == 0 -> throw SourceFailure(FailureReason.GUIDE_ENCODING_UNSUPPORTED)
            at(0) == 255 && at(1) == 254 -> Charsets.UTF_16LE to 2
            at(0) == 254 && at(1) == 255 -> Charsets.UTF_16BE to 2
            at(0) == 239 && at(1) == 187 && at(2) == 191 -> Charsets.UTF_8 to 3
            else -> Charsets.UTF_8 to 0
        }
        stream.unread(bytes, skip, size - skip)
        val decoded = InputStreamReader(stream, charset.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)).buffered()
        val bounded = object : FilterReader(decoded) {
            var count = 0L
            fun accepted(n: Int) { checkpoint(); if (n > 0) count += n; if (count > 128L * 1024 * 1024) throw SourceFailure(FailureReason.GUIDE_TOO_LARGE) }
            override fun read() = super.read().also { accepted(if (it < 0) 0 else 1) }
            override fun read(b: CharArray, off: Int, len: Int) = super.read(b, off, len).also { accepted(it) }
        }
        val prefix = StringBuilder()
        var prologLength = 0
        fun take(): Char {
            if (++prologLength > 65_536) throw SourceFailure(FailureReason.GUIDE_PROLOG_TOO_LONG)
            val value = bounded.read()
            if (value < 0) throw SourceFailure(FailureReason.GUIDE_PROLOG_MISSING)
            return value.toChar()
        }
        var hasDoctype = false
        while (true) {
            val first = take()
            if (first.isWhitespace()) { prefix.append(first); continue }
            if (first != '<') throw SourceFailure(FailureReason.GUIDE_NOT_XMLTV)
            val second = take()
            if (second != '?' && second != '!') { prefix.append('<').append(second); break }
            val token = StringBuilder().append('<').append(second)
            if (second == '?') {
                while (!token.endsWith("?>")) token.append(take())
                if (Regex("^<\\?xml\\s").containsMatchIn(token)) {
                    val encoding = Regex("encoding\\s*=\\s*['\"]([^'\"]+)['\"]").find(token)?.groupValues?.get(1)
                    val allowed = if (charset == Charsets.UTF_8) setOf("UTF-8") else setOf("UTF-16", charset.name())
                    if (encoding != null && encoding.uppercase() !in allowed) throw SourceFailure(FailureReason.GUIDE_ENCODING_UNSUPPORTED)
                }
                prefix.append(token)
            } else {
                token.append(take()).append(take())
                if (token.toString() == "<!--") {
                    while (!token.endsWith("-->")) token.append(take())
                    prefix.append(token)
                } else {
                    var quote: Char? = null
                    while (true) {
                        val c = take(); token.append(c)
                        if (quote != null) { if (c == quote) quote = null }
                        else if (c == '\'' || c == '"') quote = c
                        else if (c == '[') throw SourceFailure(FailureReason.GUIDE_INTERNAL_DEFINITIONS)
                        else if (c == '>') break
                    }
                    val quoted = "(?:\"[^\"]*\"|'[^']*')"
                    val external = Regex("<!DOCTYPE\\s+tv(?:\\s+(?:SYSTEM\\s+$quoted|PUBLIC\\s+$quoted\\s+$quoted))?\\s*>")
                    if (hasDoctype || !external.matches(token)) throw SourceFailure(FailureReason.GUIDE_DOCTYPE_UNSUPPORTED)
                    hasDoctype = true
                    prefix.append('\n')
                }
            }
        }
        val initial = StringReader(prefix.toString())
        return object : Reader() {
            var prefixDone = false
            override fun read(b: CharArray, off: Int, len: Int): Int {
                if (len == 0) return 0
                if (!prefixDone) { val n = initial.read(b, off, len); if (n >= 0) return n; prefixDone = true }
                return bounded.read(b, off, len)
            }
            override fun close() { initial.close(); bounded.close() }
        }
    }
}
