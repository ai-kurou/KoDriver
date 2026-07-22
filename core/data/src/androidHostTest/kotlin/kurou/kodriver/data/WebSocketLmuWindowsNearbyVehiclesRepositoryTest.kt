@file:Suppress("FunctionNaming")

package kurou.kodriver.data

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
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class WebSocketNearbyVehiclesRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var fakeIpRepository: FakeServerIpPreferencesRepositoryForNearbyVehicles

    @BeforeTest
    fun setUp() {
        server = MockWebServer()
        server.start()
        fakeIpRepository = FakeServerIpPreferencesRepositoryForNearbyVehicles(null)
    }

    @AfterTest
    fun tearDown() {
        server.shutdown()
    }

    private fun buildRepository(retryDelayMs: Long = 0L) = WebSocketLmuWindowsNearbyVehiclesRepository(
        serverIpRepository = fakeIpRepository,
        port = server.port,
        retryDelayMs = retryDelayMs,
    )

    @Test
    fun `ipがnullのときnearbyVehiclesStreamは何もemitしない`() = runTest {
        val result = withTimeoutOrNull(300) {
            buildRepository().nearbyVehiclesStream().first()
        }
        assertNull(result)
    }

    @Test
    fun `有効なJSONフレームを受信したときNearbyVehiclesDataをemitする`() = runTest {
        server.enqueue(
            MockResponse().withWebSocketUpgrade(
                object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        webSocket.send(NEARBY_VEHICLES_JSON)
                        webSocket.close(1000, "done")
                    }
                },
            ),
        )
        fakeIpRepository.setIp("127.0.0.1")

        val result = buildRepository().nearbyVehiclesStream().first()

        assertEquals(1, result.vehicles.size)
        assertEquals(2, result.vehicles.first().vehicleId)
        assertEquals("/ws/lmu_windows/nearby_vehicles", server.takeRequest().path)
    }

    @Test
    fun `不正なJSONフレームは無視されて次のフレームが処理される`() = runTest {
        server.enqueue(
            MockResponse().withWebSocketUpgrade(
                object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        webSocket.send("invalid json")
                        webSocket.send(NEARBY_VEHICLES_JSON)
                        webSocket.close(1000, "done")
                    }
                },
            ),
        )
        fakeIpRepository.setIp("127.0.0.1")

        val result = buildRepository().nearbyVehiclesStream().first()

        assertNotNull(result)
        assertEquals(1, result.vehicles.size)
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
                        webSocket.send(NEARBY_VEHICLES_JSON)
                        webSocket.close(1000, "done")
                    }
                },
            ),
        )
        fakeIpRepository.setIp("127.0.0.1")

        val result = buildRepository(retryDelayMs = 0L).nearbyVehiclesStream().first()

        assertEquals(1, result.vehicles.size)
    }
}

private class FakeServerIpPreferencesRepositoryForNearbyVehicles(initialIp: String?) : ServerIpPreferencesRepository {
    private val _ip = MutableStateFlow(initialIp)
    fun setIp(ip: String?) { _ip.value = ip }
    override fun serverIp(): Flow<String?> = _ip.asStateFlow()
    override suspend fun saveServerIp(ip: String) { _ip.value = ip }
}

private val NEARBY_VEHICLES_JSON = """
    {
        "vehicles": [
            {"vehicleId": 2, "longitudinalDistanceMeters": 3.5, "lateralDistanceMeters": -1.2}
        ]
    }
""".trimIndent()
