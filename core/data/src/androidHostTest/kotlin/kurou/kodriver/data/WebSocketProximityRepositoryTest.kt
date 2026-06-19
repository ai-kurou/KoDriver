@file:Suppress("FunctionNaming")

package kurou.kodriver.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class WebSocketProximityRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var fakeIpRepository: FakeServerIpRepositoryForProximity

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        fakeIpRepository = FakeServerIpRepositoryForProximity(null)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun buildRepository(retryDelayMs: Long = 0L) = WebSocketProximityRepository(
        serverIpRepository = fakeIpRepository,
        port = server.port,
        retryDelayMs = retryDelayMs,
    )

    @Test
    fun `ipがnullのときproximityStreamは何もemitしない`() = runTest {
        val result = withTimeoutOrNull(300) {
            buildRepository().proximityStream().first()
        }
        assertNull(result)
    }

    @Test
    fun `有効なJSONフレームを受信したときProximityDataをemitする`() = runTest {
        server.enqueue(
            MockResponse().withWebSocketUpgrade(
                object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        webSocket.send(PROXIMITY_JSON)
                        webSocket.close(1000, "done")
                    }
                },
            ),
        )
        fakeIpRepository.setIp("127.0.0.1")

        val result = buildRepository().proximityStream().first()

        assertEquals(setOf(3), result.sideBySideLeftVehicleIds)
        assertEquals(emptySet(), result.sideBySideRightVehicleIds)
        assertEquals("/ws/lmu_windows/proximity", server.takeRequest().path)
    }

    @Test
    fun `不正なJSONフレームは無視されて次のフレームが処理される`() = runTest {
        server.enqueue(
            MockResponse().withWebSocketUpgrade(
                object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        webSocket.send("invalid json")
                        webSocket.send(PROXIMITY_JSON)
                        webSocket.close(1000, "done")
                    }
                },
            ),
        )
        fakeIpRepository.setIp("127.0.0.1")

        val result = buildRepository().proximityStream().first()

        assertNotNull(result)
        assertEquals(setOf(3), result.sideBySideLeftVehicleIds)
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
                        webSocket.send(PROXIMITY_JSON)
                        webSocket.close(1000, "done")
                    }
                },
            ),
        )
        fakeIpRepository.setIp("127.0.0.1")

        val result = buildRepository(retryDelayMs = 0L).proximityStream().first()

        assertEquals(setOf(3), result.sideBySideLeftVehicleIds)
    }
}

private class FakeServerIpRepositoryForProximity(initialIp: String?) : ServerIpRepository {
    private val _ip = MutableStateFlow(initialIp)
    fun setIp(ip: String?) { _ip.value = ip }
    override fun serverIp(): Flow<String?> = _ip.asStateFlow()
    override suspend fun saveServerIp(ip: String) { _ip.value = ip }
}

private val PROXIMITY_JSON = """
    {
        "sideBySideLeftVehicleIds": [3],
        "sideBySideRightVehicleIds": [],
        "lateralDistanceLeftMeters": 1.5,
        "lateralDistanceRightMeters": 1.7976931348623157E308
    }
""".trimIndent()
