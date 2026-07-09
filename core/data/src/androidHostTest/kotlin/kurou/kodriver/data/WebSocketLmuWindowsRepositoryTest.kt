@file:Suppress("FunctionNaming")

package kurou.kodriver.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import kurou.kodriver.domain.repository.ServerIpRepository
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class WebSocketLmuWindowsRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var fakeIpRepository: FakeServerIpRepositoryForMyBestLap

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        fakeIpRepository = FakeServerIpRepositoryForMyBestLap(null)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun buildRepository(retryDelayMs: Long = 0L) = WebSocketLmuWindowsRepository(
        serverIpRepository = fakeIpRepository,
        port = server.port,
        retryDelayMs = retryDelayMs,
    )

    @Test
    fun `ipがnullのときtelemetryStreamは何もemitしない`() = runTest {
        val result = withTimeoutOrNull(300) {
            buildRepository().telemetryStream().first()
        }
        assertNull(result)
    }

    @Test
    fun `有効なJSONフレームを受信したときtimingを反映したLmuWindowsTelemetryDataをemitする`() = runTest {
        server.enqueue(
            MockResponse().withWebSocketUpgrade(
                object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        webSocket.send(TIMING_JSON)
                        webSocket.close(1000, "done")
                    }
                },
            ),
        )
        fakeIpRepository.setIp("127.0.0.1")

        val result = buildRepository().telemetryStream().first()

        assertEquals(60_000L, result.timing.bestLapTimeMs)
        assertEquals(2, result.timing.currentLap)
        assertEquals("/ws/lmu_windows/my_best_lap", server.takeRequest().path)
    }

    @Test
    fun `不正なJSONフレームは無視されて次のフレームが処理される`() = runTest {
        server.enqueue(
            MockResponse().withWebSocketUpgrade(
                object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        webSocket.send("invalid json")
                        webSocket.send(TIMING_JSON)
                        webSocket.close(1000, "done")
                    }
                },
            ),
        )
        fakeIpRepository.setIp("127.0.0.1")

        val result = buildRepository().telemetryStream().first()

        assertEquals(60_000L, result.timing.bestLapTimeMs)
    }

    @Test
    fun `接続切断後にリトライして再接続する`() = runTest {
        server.enqueue(
            MockResponse().withWebSocketUpgrade(
                object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        webSocket.close(1001, "drop")
                    }
                },
            ),
        )
        server.enqueue(
            MockResponse().withWebSocketUpgrade(
                object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        webSocket.send(TIMING_JSON)
                        webSocket.close(1000, "done")
                    }
                },
            ),
        )
        fakeIpRepository.setIp("127.0.0.1")

        val result = buildRepository(retryDelayMs = 0L).telemetryStream().first()

        assertEquals(60_000L, result.timing.bestLapTimeMs)
    }

    @Test
    fun `IPがnullになるとemitが止まり再設定すると再接続してデータをemitする`() = runTest {
        server.enqueue(
            MockResponse().withWebSocketUpgrade(
                object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        webSocket.send(TIMING_JSON)
                        webSocket.close(1000, "done")
                    }
                },
            ),
        )

        fakeIpRepository.setIp(null)
        val repository = buildRepository()

        val noEmit = withTimeoutOrNull(300) { repository.telemetryStream().first() }
        assertNull(noEmit)

        fakeIpRepository.setIp("127.0.0.1")
        val result = repository.telemetryStream().first()
        assertEquals(60_000L, result.timing.bestLapTimeMs)
    }

    @Test
    fun `isConnectedは常にfalseを返す`() = runTest {
        assertEquals(false, buildRepository().isConnected())
    }

    @Test
    fun `disconnectは何もしない`() = runTest {
        buildRepository().disconnect()
    }
}

private class FakeServerIpRepositoryForMyBestLap(initialIp: String?) : ServerIpRepository {
    private val _ip = MutableStateFlow(initialIp)
    fun setIp(ip: String?) { _ip.update { ip } }
    override fun serverIp(): Flow<String?> = _ip.asStateFlow()
    override suspend fun saveServerIp(ip: String) { _ip.update { ip } }
}

private val TIMING_JSON = """
    {
        "currentLapTimeMs": 30000,
        "lastLapTimeMs": 61000,
        "bestLapTimeMs": 60000,
        "sector1Ms": 20000,
        "sector2Ms": 20000,
        "currentLap": 2,
        "maxLaps": 10
    }
""".trimIndent()
