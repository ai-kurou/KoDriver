@file:Suppress("FunctionNaming")

package kurou.kodriver.core.acewindowsdata.websocket

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

class WebSocketAceWindowsVehicleApproachRepositoryTest {
    private lateinit var server: MockWebServer
    private lateinit var fakeIpRepository: FakeAceVehicleApproachServerIpPreferencesRepository

    @BeforeTest
    fun setUp() {
        server = MockWebServer()
        server.start()
        fakeIpRepository = FakeAceVehicleApproachServerIpPreferencesRepository(null)
    }

    @AfterTest
    fun tearDown() {
        server.shutdown()
    }

    private fun buildRepository(retryDelayMs: Long = 0L) =
        WebSocketAceWindowsVehicleApproachRepository(
            serverIpRepository = fakeIpRepository,
            port = server.port,
            retryDelayMs = retryDelayMs,
        )

    @Test
    fun `ipがnullのときvehicleApproachStreamは何もemitしない`() =
        runTest {
            val result =
                withTimeoutOrNull(300) {
                    buildRepository().vehicleApproachStream().first()
                }
            assertNull(result)
        }

    @Test
    fun `有効なJSONフレームを受信したときAceWindowsVehicleApproachDataをemitする`() =
        runTest {
            server.enqueue(
                MockResponse().withWebSocketUpgrade(
                    object : WebSocketListener() {
                        override fun onOpen(
                            webSocket: WebSocket,
                            response: Response,
                        ) {
                            webSocket.send(ONE_VEHICLE_JSON)
                            webSocket.close(1000, "done")
                        }
                    },
                ),
            )
            fakeIpRepository.setIp("127.0.0.1")

            val result = buildRepository().vehicleApproachStream().first()

            assertEquals(1, result.nearbyVehicles.size)
            assertEquals(12.5, result.nearbyVehicles.first().distanceMeters)
            assertEquals("/ws/ace_windows/vehicle_approach", server.takeRequest().path)
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
                            webSocket.send(EMPTY_VEHICLES_JSON)
                            webSocket.close(1000, "done")
                        }
                    },
                ),
            )

            fakeIpRepository.setIp(null)
            val repository = buildRepository()

            val noEmit = withTimeoutOrNull(300) { repository.vehicleApproachStream().first() }
            assertNull(noEmit)

            fakeIpRepository.setIp("127.0.0.1")
            val result = repository.vehicleApproachStream().first()
            assertEquals(emptyList(), result.nearbyVehicles)
        }
}

private class FakeAceVehicleApproachServerIpPreferencesRepository(
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

private const val ONE_VEHICLE_JSON = """{"nearbyVehicles": [{"distanceMeters": 12.5}]}"""
private const val EMPTY_VEHICLES_JSON = """{"nearbyVehicles": []}"""
