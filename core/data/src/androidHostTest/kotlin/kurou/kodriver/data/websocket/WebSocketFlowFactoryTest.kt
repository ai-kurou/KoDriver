@file:Suppress("FunctionNaming")

package kurou.kodriver.data.websocket

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import java.io.IOException
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WebSocketFlowFactoryTest {
    private lateinit var server: MockWebServer

    @BeforeTest
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @AfterTest
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `有効なフレームを受信したときdecodeした値をemitする`() =
        runTest {
            server.enqueue(
                MockResponse().withWebSocketUpgrade(
                    object : WebSocketListener() {
                        override fun onOpen(
                            webSocket: WebSocket,
                            response: Response,
                        ) {
                            webSocket.send("hello")
                            webSocket.close(1000, "done")
                        }
                    },
                ),
            )
            val client = createWebSocketHttpClient()

            val result =
                client
                    .webSocketFlow(
                        host = "127.0.0.1",
                        port = server.port,
                        path = "/ws/test",
                        retryDelayMs = 0L,
                        decode = { it.uppercase() },
                    ).first()

            assertEquals("HELLO", result)
            client.close()
        }

    @Test
    fun `decodeがSerializationExceptionを投げたフレームは無視されて次のフレームが処理される`() =
        runTest {
            server.enqueue(
                MockResponse().withWebSocketUpgrade(
                    object : WebSocketListener() {
                        override fun onOpen(
                            webSocket: WebSocket,
                            response: Response,
                        ) {
                            webSocket.send("invalid")
                            webSocket.send("hello")
                            webSocket.close(1000, "done")
                        }
                    },
                ),
            )
            val client = createWebSocketHttpClient()
            val exception = SerializationException("boom")
            val capturedExceptions = mutableListOf<Throwable>()

            val result =
                client
                    .webSocketFlow(
                        host = "127.0.0.1",
                        port = server.port,
                        path = "/ws/test",
                        retryDelayMs = 0L,
                        decode = { if (it == "invalid") throw exception else it.uppercase() },
                        captureException = { capturedExceptions += it },
                    ).first()

            assertEquals("HELLO", result)
            assertEquals(listOf<Throwable>(exception), capturedExceptions)
            client.close()
        }

    @Test
    fun `接続に失敗した場合はretryDelayMs後にリトライして再接続する`() =
        runTest {
            server.enqueue(
                MockResponse().withWebSocketUpgrade(
                    object : WebSocketListener() {
                        override fun onOpen(
                            webSocket: WebSocket,
                            response: Response,
                        ) {
                            webSocket.close(1001, "drop")
                        }
                    },
                ),
            )
            server.enqueue(
                MockResponse().withWebSocketUpgrade(
                    object : WebSocketListener() {
                        override fun onOpen(
                            webSocket: WebSocket,
                            response: Response,
                        ) {
                            webSocket.send("hello")
                            webSocket.close(1000, "done")
                        }
                    },
                ),
            )
            val client = createWebSocketHttpClient()

            val result =
                client
                    .webSocketFlow(
                        host = "127.0.0.1",
                        port = server.port,
                        path = "/ws/test",
                        retryDelayMs = 0L,
                        decode = { it.uppercase() },
                    ).first()

            assertEquals("HELLO", result)
            client.close()
        }

    @Test
    fun `予期しない例外はSentryへ送信してリトライ後に再接続する`() =
        runTest {
            server.enqueue(
                MockResponse().withWebSocketUpgrade(
                    object : WebSocketListener() {
                        override fun onOpen(
                            webSocket: WebSocket,
                            response: Response,
                        ) {
                            webSocket.send("invalid")
                            webSocket.close(1001, "drop")
                        }
                    },
                ),
            )
            server.enqueue(
                MockResponse().withWebSocketUpgrade(
                    object : WebSocketListener() {
                        override fun onOpen(
                            webSocket: WebSocket,
                            response: Response,
                        ) {
                            webSocket.send("hello")
                            webSocket.close(1000, "done")
                        }
                    },
                ),
            )
            val client = createWebSocketHttpClient()
            val exception = IllegalStateException("boom")
            val capturedExceptions = mutableListOf<Throwable>()

            val result =
                client
                    .webSocketFlow(
                        host = "127.0.0.1",
                        port = server.port,
                        path = "/ws/test",
                        retryDelayMs = 0L,
                        decode = { if (it == "invalid") throw exception else it.uppercase() },
                        captureException = { capturedExceptions += it },
                    ).first()

            assertEquals("HELLO", result)
            assertEquals(listOf<Throwable>(exception), capturedExceptions)
            client.close()
        }

    @Test
    fun `接続自体が例外を投げてもクラッシュせずリトライを継続する`() =
        runTest {
            val unusedPort = server.port
            server.shutdown()
            val client = createWebSocketHttpClient()
            var emitted = false
            val capturedExceptions = mutableListOf<Throwable>()

            val job =
                launch {
                    client
                        .webSocketFlow(
                            host = "127.0.0.1",
                            port = unusedPort,
                            path = "/ws/test",
                            retryDelayMs = 0L,
                            decode = { it.uppercase() },
                            captureException = { capturedExceptions += it },
                        ).collect { emitted = true }
                }
            withContext(Dispatchers.Default) { delay(300) }
            job.cancel()
            job.join()

            assertFalse(emitted)
            assertEquals(emptyList<Throwable>(), capturedExceptions)
            client.close()
            server = MockWebServer().apply { start() }
        }

    @Test
    fun `想定内のネットワーク例外のみSentry送信対象から除外する`() {
        assertTrue(SocketTimeoutException("x").isExpectedNetworkFailure())
        assertTrue(ConnectException("x").isExpectedNetworkFailure())
        assertTrue(UnknownHostException("x").isExpectedNetworkFailure())
        assertTrue(SocketException("x").isExpectedNetworkFailure())
        assertFalse(IOException("x").isExpectedNetworkFailure())
        assertFalse(IllegalStateException("x").isExpectedNetworkFailure())
    }
}
