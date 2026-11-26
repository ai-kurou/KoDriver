@file:Suppress("FunctionNaming")

package kurou.kodriver.core.acewindowsdata.websocket

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

class WebSocketAceWindowsBrakeTemperatureRepositoryTest {
    private lateinit var server: MockWebServer
    private lateinit var fakeIpRepository: FakeServerIpPreferencesRepositoryForAceBrakeTemperature

    @BeforeTest
    fun setUp() {
        server = MockWebServer()
        server.start()
        fakeIpRepository = FakeServerIpPreferencesRepositoryForAceBrakeTemperature(null)
    }

    @AfterTest
    fun tearDown() {
        try {
            server.shutdown()
        } catch (_: IllegalStateException) {
        }
    }

    private fun buildRepository() =
        WebSocketAceWindowsBrakeTemperatureRepository(
            serverIpRepository = fakeIpRepository,
            port = server.port,
            retryDelayMs = 0L,
        )

    @Test
    fun `ipがnullのときbrakeTemperatureStreamは何もemitしない`() =
        runTest {
            val result =
                withTimeoutOrNull(300) {
                    buildRepository().brakeTemperatureStream().first()
                }
            assertNull(result)
        }

    @Test
    fun `有効なJSONフレームを受信したときBrakeTemperatureDataをemitする`() =
        runTest {
            server.enqueue(
                MockResponse().withWebSocketUpgrade(
                    object : WebSocketListener() {
                        override fun onOpen(
                            webSocket: WebSocket,
                            response: Response,
                        ) {
                            webSocket.send(ACE_BRAKE_TEMPERATURE_JSON)
                            webSocket.close(1000, "done")
                        }
                    },
                ),
            )
            fakeIpRepository.setIp("127.0.0.1")

            val result = buildRepository().brakeTemperatureStream().first()

            assertEquals(CelsiusReading(600.0f), result.wheels[WheelIndex.FRONT_LEFT])
            assertEquals(CelsiusReading(620.0f), result.wheels[WheelIndex.FRONT_RIGHT])
            assertEquals(CelsiusReading(550.0f), result.wheels[WheelIndex.REAR_LEFT])
            assertEquals(CelsiusReading(570.0f), result.wheels[WheelIndex.REAR_RIGHT])
            assertEquals("/ws/ace_windows/brake_temperature", server.takeRequest().path)
        }
}

private class FakeServerIpPreferencesRepositoryForAceBrakeTemperature(
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

private val ACE_BRAKE_TEMPERATURE_JSON =
    """
    {
        "wheels": {
            "FRONT_LEFT": 600.0,
            "FRONT_RIGHT": 620.0,
            "REAR_LEFT": 550.0,
            "REAR_RIGHT": 570.0
        }
    }
    """.trimIndent()
