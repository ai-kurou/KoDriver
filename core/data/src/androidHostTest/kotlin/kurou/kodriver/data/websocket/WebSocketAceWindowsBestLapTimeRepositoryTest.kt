@file:Suppress("FunctionNaming")

package kurou.kodriver.data.websocket

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import kurou.kodriver.domain.repository.ServerIpPreferencesRepository
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class WebSocketAceWindowsBestLapTimeRepositoryTest {
    private lateinit var server: MockWebServer
    private lateinit var fakeIpRepository: FakeAceBestLapTimeServerIpPreferencesRepository

    @BeforeTest
    fun setUp() {
        server = MockWebServer()
        server.start()
        fakeIpRepository = FakeAceBestLapTimeServerIpPreferencesRepository(null)
    }

    @AfterTest
    fun tearDown() {
        server.shutdown()
    }

    private fun buildRepository(retryDelayMs: Long = 0L) =
        WebSocketAceWindowsBestLapTimeRepository(
            serverIpRepository = fakeIpRepository,
            port = server.port,
            retryDelayMs = retryDelayMs,
        )

    @Test
    fun `ipがnullのときbestLapTimeStreamは何もemitしない`() =
        runTest {
            val result =
                withTimeoutOrNull(300) {
                    buildRepository().bestLapTimeStream().first()
                }
            assertNull(result)
        }

    @Test
    fun `有効なJSONフレームを受信したときAceWindowsBestLapTimeDataをemitする`() =
        runTest {
            server.enqueue(
                MockResponse().withWebSocketUpgrade(
                    object : WebSocketListener() {
                        override fun onOpen(
                            webSocket: WebSocket,
                            response: Response,
                        ) {
                            webSocket.send(BEST_LAP_TIME_JSON)
                            webSocket.close(1000, "done")
                        }
                    },
                ),
            )
            fakeIpRepository.setIp("127.0.0.1")

            val result = buildRepository().bestLapTimeStream().first()

            assertEquals(95_123, result.bestLapTimeMs)
            assertEquals("/ws/ace_windows/my_best_lap", server.takeRequest().path)
        }

    @Test
    fun `不正なJSONフレームは無視されて次のフレームが処理される`() =
        runTest {
            server.enqueue(
                MockResponse().withWebSocketUpgrade(
                    object : WebSocketListener() {
                        override fun onOpen(
                            webSocket: WebSocket,
                            response: Response,
                        ) {
                            webSocket.send("invalid json")
                            webSocket.send(BEST_LAP_TIME_JSON)
                            webSocket.close(1000, "done")
                        }
                    },
                ),
            )
            fakeIpRepository.setIp("127.0.0.1")

            val result = buildRepository().bestLapTimeStream().first()

            assertEquals(95_123, result.bestLapTimeMs)
        }

    @Test
    fun `接続切断後にリトライして再接続する`() =
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
                            webSocket.send(BEST_LAP_TIME_JSON)
                            webSocket.close(1000, "done")
                        }
                    },
                ),
            )
            fakeIpRepository.setIp("127.0.0.1")

            val result = buildRepository(retryDelayMs = 0L).bestLapTimeStream().first()

            assertEquals(95_123, result.bestLapTimeMs)
        }

    @Test
    fun `IPがnullになるとemitが止まり再設定すると再接続してデータをemitする`() =
        runTest {
            server.enqueue(
                MockResponse().withWebSocketUpgrade(
                    object : WebSocketListener() {
                        override fun onOpen(
                            webSocket: WebSocket,
                            response: Response,
                        ) {
                            webSocket.send(BEST_LAP_TIME_JSON)
                            webSocket.close(1000, "done")
                        }
                    },
                ),
            )

            fakeIpRepository.setIp(null)
            val repository = buildRepository()

            val noEmit = withTimeoutOrNull(300) { repository.bestLapTimeStream().first() }
            assertNull(noEmit)

            fakeIpRepository.setIp("127.0.0.1")
            val result = repository.bestLapTimeStream().first()
            assertEquals(95_123, result.bestLapTimeMs)
        }
}

private class FakeAceBestLapTimeServerIpPreferencesRepository(
    initialIp: String?,
) : ServerIpPreferencesRepository {
    private val _ip = MutableStateFlow(initialIp)

    fun setIp(ip: String?) {
        _ip.value = ip
    }

    override fun serverIp(): Flow<String?> = _ip.asStateFlow()

    override suspend fun saveServerIp(ip: String) {
        _ip.value = ip
    }
}

private const val BEST_LAP_TIME_JSON = """{"bestLapTimeMs": 95123}"""
