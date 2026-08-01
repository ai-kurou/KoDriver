@file:Suppress("FunctionNaming")

package kurou.kodriver.data

import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.test.runTest
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class WebSocketHttpClientFactoryTest {

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
    fun `生成したクライアントでWebSocket接続してフレームを受信できる`() =
        runTest {
        server.enqueue(
            MockResponse().withWebSocketUpgrade(
                object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        webSocket.send("hello")
                        webSocket.close(1000, "done")
                    }
                },
            ),
        )
        val client = createWebSocketHttpClient()

        var received: String? = null
        client.webSocket(host = "127.0.0.1", port = server.port, path = "/ws/test") {
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    received = frame.readText()
                }
            }
        }

        assertEquals("hello", received)
        client.close()
    }
}
