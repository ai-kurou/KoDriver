@file:Suppress("FunctionNaming")

package kurou.kodriver.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import kurou.kodriver.domain.model.CountLapFlag
import kurou.kodriver.domain.model.PrimaryFlag
import kurou.kodriver.domain.model.RaceFlagsData
import kurou.kodriver.domain.model.SectorFlagState
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.SessionYellowFlagState
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

class WebSocketFlagRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var fakeIpRepository: FakeServerIpRepository

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        fakeIpRepository = FakeServerIpRepository(null)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun buildRepository(retryDelayMs: Long = 0L) = WebSocketFlagRepository(
        serverIpRepository = fakeIpRepository,
        port = server.port,
        retryDelayMs = retryDelayMs,
    )

    @Test
    fun `ipがnullのときflagStreamは何もemitしない`() = runTest {
        val result = withTimeoutOrNull(300) {
            buildRepository().flagStream().first()
        }
        assertNull(result)
    }

    @Test
    fun `有効なJSONフレームを受信したときRaceFlagsDataをemitする`() = runTest {
        server.enqueue(MockResponse().withWebSocketUpgrade(object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                webSocket.send(GREEN_FLAG_JSON)
                webSocket.close(1000, "done")
            }
        }))
        fakeIpRepository.setIp("127.0.0.1")

        val result = buildRepository().flagStream().first()

        assertEquals(SessionPhase.GREEN_FLAG, result.gamePhase)
        assertEquals(SessionYellowFlagState.NONE, result.yellowFlagState)
    }

    @Test
    fun `不正なJSONフレームは無視されて次のフレームが処理される`() = runTest {
        server.enqueue(MockResponse().withWebSocketUpgrade(object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                webSocket.send("invalid json")
                webSocket.send(GREEN_FLAG_JSON)
                webSocket.close(1000, "done")
            }
        }))
        fakeIpRepository.setIp("127.0.0.1")

        val result = buildRepository().flagStream().first()

        assertEquals(SessionPhase.GREEN_FLAG, result.gamePhase)
    }

    @Test
    fun `接続切断後にリトライして再接続する`() = runTest {
        server.enqueue(MockResponse().withWebSocketUpgrade(object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                webSocket.close(1001, "drop")
            }
        }))
        server.enqueue(MockResponse().withWebSocketUpgrade(object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                webSocket.send(GREEN_FLAG_JSON)
                webSocket.close(1000, "done")
            }
        }))
        fakeIpRepository.setIp("127.0.0.1")

        val result = buildRepository(retryDelayMs = 0L).flagStream().first()

        assertEquals(SessionPhase.GREEN_FLAG, result.gamePhase)
    }

    @Test
    fun `IPが変わると新しいIPのサーバーに接続して新しいデータをemitする`() = runTest {
        val secondServer = MockWebServer()
        secondServer.start()
        try {
            server.enqueue(MockResponse().withWebSocketUpgrade(object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    webSocket.send(GREEN_FLAG_JSON)
                }
            }))
            secondServer.enqueue(MockResponse().withWebSocketUpgrade(object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    webSocket.send(RED_FLAG_JSON)
                    webSocket.close(1000, "done")
                }
            }))

            fakeIpRepository.setIp("127.0.0.1")
            val repository = WebSocketFlagRepository(
                serverIpRepository = fakeIpRepository,
                port = server.port,
                retryDelayMs = 0L,
            )

            val firstResult = repository.flagStream().first()
            assertEquals(SessionPhase.GREEN_FLAG, firstResult.gamePhase)

            val secondRepository = WebSocketFlagRepository(
                serverIpRepository = fakeIpRepository,
                port = secondServer.port,
                retryDelayMs = 0L,
            )
            fakeIpRepository.setIp("127.0.0.2")

            val results = secondRepository.flagStream().take(1).toList()
            assertEquals(SessionPhase.RED_FLAG, results.first().gamePhase)
        } finally {
            secondServer.shutdown()
        }
    }
}

private class FakeServerIpRepository(initialIp: String?) : ServerIpRepository {
    private val _ip = MutableStateFlow(initialIp)
    fun setIp(ip: String?) { _ip.value = ip }
    override fun serverIp(): Flow<String?> = _ip.asStateFlow()
    override suspend fun saveServerIp(ip: String) { _ip.value = ip }
}

private val GREEN_FLAG_JSON = """
    {
        "gamePhase": "GREEN_FLAG",
        "yellowFlagState": "NONE",
        "sectorFlags": [],
        "startLight": 0,
        "numRedLights": 0,
        "playerFlag": "GREEN",
        "playerUnderYellow": false,
        "playerCountLapFlag": "DO_NOT_COUNT_LAP_OR_TIME"
    }
""".trimIndent()

private val RED_FLAG_JSON = """
    {
        "gamePhase": "RED_FLAG",
        "yellowFlagState": "NONE",
        "sectorFlags": [],
        "startLight": 0,
        "numRedLights": 0,
        "playerFlag": "GREEN",
        "playerUnderYellow": false,
        "playerCountLapFlag": "DO_NOT_COUNT_LAP_OR_TIME"
    }
""".trimIndent()
