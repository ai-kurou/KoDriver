@file:Suppress("FunctionNaming")

package kurou.kodriver.core.lmuwindowsdata.websocket

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.WheelIndex
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

class WebSocketLmuWindowsTyreCarcassTemperatureRepositoryTest {
    private lateinit var server: MockWebServer
    private lateinit var fakeIpRepository: FakeServerIpPreferencesRepositoryForTyreCarcassTemperature

    @BeforeTest
    fun setUp() {
        server = MockWebServer()
        server.start()
        fakeIpRepository = FakeServerIpPreferencesRepositoryForTyreCarcassTemperature(null)
    }

    @AfterTest
    fun tearDown() {
        try {
            server.shutdown()
        } catch (_: IllegalStateException) {
        }
    }

    private fun buildRepository(retryDelayMs: Long = 0L) =
        WebSocketLmuWindowsTyreCarcassTemperatureRepository(
            serverIpRepository = fakeIpRepository,
            port = server.port,
            retryDelayMs = retryDelayMs,
        )

    @Test
    fun `ipがnullのときtyreCarcassTemperatureStreamは何もemitしない`() =
        runTest {
            val result =
                withTimeoutOrNull(300) {
                    buildRepository().tyreCarcassTemperatureStream().first()
                }
            assertNull(result)
        }

    @Test
    fun `有効なJSONフレームを受信したときTyreCarcassTemperatureDataをemitする`() =
        runTest {
            server.enqueue(
                MockResponse().withWebSocketUpgrade(
                    object : WebSocketListener() {
                        override fun onOpen(
                            webSocket: WebSocket,
                            response: Response,
                        ) {
                            webSocket.send(TYRE_CARCASS_TEMPERATURE_JSON)
                            webSocket.close(1000, "done")
                        }
                    },
                ),
            )
            fakeIpRepository.setIp("127.0.0.1")

            val result = buildRepository().tyreCarcassTemperatureStream().first()

            assertEquals(CelsiusReading(80.0f), result.wheels[WheelIndex.FRONT_LEFT])
            assertEquals(CelsiusReading(82.0f), result.wheels[WheelIndex.FRONT_RIGHT])
            assertEquals(CelsiusReading(85.0f), result.wheels[WheelIndex.REAR_LEFT])
            assertEquals(CelsiusReading(87.0f), result.wheels[WheelIndex.REAR_RIGHT])
            assertEquals("/ws/lmu_windows/tyre_carcass_temperature", server.takeRequest().path)
        }
}

private class FakeServerIpPreferencesRepositoryForTyreCarcassTemperature(
    initialIp: String?,
) : ServerIpPreferencesRepository {
    private val _ip = MutableStateFlow(initialIp)

    fun setIp(ip: String?) {
        _ip.update { ip }
    }

    override fun serverIp(): Flow<String?> = _ip.asStateFlow()

    override suspend fun saveServerIp(ip: String) {
        _ip.update { ip }
    }
}

private val TYRE_CARCASS_TEMPERATURE_JSON =
    """
    {
        "wheels": {
            "FRONT_LEFT": 80.0,
            "FRONT_RIGHT": 82.0,
            "REAR_LEFT": 85.0,
            "REAR_RIGHT": 87.0
        }
    }
    """.trimIndent()
