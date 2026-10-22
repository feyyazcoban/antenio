package dev.iptvplayer.tv.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.Inet4Address
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket

/**
 * The small web server the setup screen runs while "Telefonla ekle" is open: GET / returns the phone page, POST /send
 * takes one [PhoneSubmission] for the current [PhoneSetupSession]. Home network only, plain HTTP; it never contacts the
 * provider and closes with the screen. [onSubmit] runs on the server's thread.
 */
class PhoneSetupServer(private val page: () -> ByteArray, private val clock: () -> Long = System::currentTimeMillis,
    private val onSubmit: (PhoneSubmission) -> Unit) {
    @Volatile var session: PhoneSetupSession? = null
    @Volatile private var socket: ServerSocket? = null

    /** Binds [preferredPort], else any free one, and serves until [stop] or [scope] ends. Returns the port. */
    suspend fun start(scope: CoroutineScope, preferredPort: Int = PREFERRED_PORT): Int = withContext(Dispatchers.IO) {
        val server = runCatching { ServerSocket().apply { reuseAddress = true; bind(InetSocketAddress(preferredPort)) } }
            .getOrElse { ServerSocket(0) }
        socket = server
        scope.launch(Dispatchers.IO) {
            while (isActive && !server.isClosed) {
                val client = runCatching { server.accept() }.getOrNull() ?: break
                launch(Dispatchers.IO) { client.use { handle(it) } }
            }
        }
        server.localPort
    }

    fun stop() { runCatching { socket?.close() }; socket = null }

    private fun handle(client: Socket) {
        client.soTimeout = 5_000
        val input = BufferedInputStream(client.getInputStream())
        val head = readHead(input) ?: return
        val requestLine = head.lineSequence().firstOrNull().orEmpty().split(' ')
        val method = requestLine.getOrNull(0).orEmpty()
        val path = requestLine.getOrNull(1).orEmpty().substringBefore('?')
        val length = head.lineSequence().drop(1).firstOrNull { it.startsWith("content-length:", ignoreCase = true) }
            ?.substringAfter(':')?.trim()?.toIntOrNull() ?: 0
        val out = client.getOutputStream()
        fun reply(status: String, type: String, body: ByteArray) {
            out.write(("HTTP/1.1 $status\r\nContent-Type: $type\r\nContent-Length: ${body.size}\r\n" +
                "Cache-Control: no-store\r\nConnection: close\r\n\r\n").toByteArray())
            out.write(body); out.flush()
        }
        fun json(status: String, text: String) = reply(status, "application/json; charset=utf-8", text.toByteArray())
        when {
            method == "GET" && (path == "/" || path == "/index.html") -> reply("200 OK", "text/html; charset=utf-8", page())
            method == "POST" && path == "/send" -> {
                if (length <= 0 || length > PhoneSetupForm.MAX_BODY) return json("413 Payload Too Large", """{"error":"size"}""")
                val body = readBody(input, length) ?: return json("400 Bad Request", """{"error":"body"}""")
                val fields = PhoneSetupForm.parse(body)
                val current = session ?: return json("410 Gone", """{"error":"expired"}""")
                when (current.check(fields["code"].orEmpty(), clock())) {
                    PhoneSetupSession.Check.EXPIRED, PhoneSetupSession.Check.USED -> json("410 Gone", """{"error":"expired"}""")
                    PhoneSetupSession.Check.LOCKED -> json("429 Too Many Requests", """{"error":"locked"}""")
                    PhoneSetupSession.Check.WRONG_CODE -> json("403 Forbidden", """{"error":"code"}""")
                    PhoneSetupSession.Check.ACCEPTED -> {
                        val submission = PhoneSetupForm.submission(fields)
                            ?: return json("400 Bad Request", """{"error":"fields"}""")
                        onSubmit(submission)
                        json("200 OK", """{"ok":true}""")
                    }
                }
            }
            else -> json("404 Not Found", """{"error":"path"}""")
        }
    }

    private fun readHead(input: InputStream): String? {
        val bytes = ByteArrayOutputStream()
        var last4 = 0
        while (bytes.size() < 8 * 1024) {
            val b = input.read()
            if (b < 0) return null
            bytes.write(b)
            last4 = (last4 shl 8) or b
            if (last4 == 0x0D0A0D0A) return bytes.toString(Charsets.ISO_8859_1.name())
        }
        return null
    }

    private fun readBody(input: InputStream, length: Int): String? {
        val buffer = ByteArray(length)
        var read = 0
        while (read < length) {
            val n = input.read(buffer, read, length - read)
            if (n < 0) return null
            read += n
        }
        return String(buffer, Charsets.UTF_8)
    }

    companion object {
        const val PREFERRED_PORT = 8765

        /** The TV's address on the home network, or null when it has none (only loopback or a non-private address). */
        fun localAddress(): String? = runCatching {
            NetworkInterface.getNetworkInterfaces().toList().filter { it.isUp && !it.isLoopback && !it.isVirtual }
                .flatMap { it.inetAddresses.toList() }.filterIsInstance<Inet4Address>()
                .firstOrNull { it.isSiteLocalAddress }?.hostAddress
        }.getOrNull()
    }
}
