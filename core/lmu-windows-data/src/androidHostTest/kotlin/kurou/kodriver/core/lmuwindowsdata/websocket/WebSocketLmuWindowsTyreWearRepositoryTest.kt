@file:Suppress("FunctionNaming")

package kurou.kodriver.core.lmuwindowsdata.websocket

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreWearRatio
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

class WebSocketLmuWindowsTyreWearRepositoryTest {
    private lateinit var server: MockWebServer
    private lateinit var fakeIpRepository: FakeServerIpPreferencesRepositoryForTyreWear

    @BeforeTest
    fun setUp() {
        server = MockWebServer()
        server.start()
        fakeIpRepository = FakeServerIpPreferencesRepositoryForTyreWear(null)
    }

    @AfterTest
    fun tearDown() {
        try {
            server.shutdown()
        } catch (_: IllegalStateException) {
        }
    }

    private fun buildRepository(retryDelayMs: Long = 0L) =
        WebSocketLmuWindowsTyreWearRepository(
            serverIpRepository = fakeIpRepository,
            port = server.port,
            retryDelayMs = retryDelayMs,
        )

    @Test
    fun `ipがnullのときtyreWearStreamは何もemitしない`() =
        runTest {
            val result =
                withTimeoutOrNull(300) {
                    buildRepository().tyreWearStream().first()
                }
            assertNull(result)
        }

    @Test
    fun `有効なJSONフレームを受信したときTyreWearDataをemitする`() =
        runTest {
            server.enqueue(
                MockResponse().withWebSocketUpgrade(
                    object : WebSocketListener() {
                        override fun onOpen(
                            webSocket: WebSocket,
                            response: Response,
                        ) {
                            webSocket.send(TYRE_WEAR_JSON)
                            webSocket.close(1000, "done")
                        }
                    },
                ),
            )
            fakeIpRepository.setIp("127.0.0.1")

            val result = buildRepository().tyreWearStream().first()

            assertEquals(LmuWindowsTyreWearRatio(0.8), result.wheels[WheelIndex.FRONT_LEFT])
            assertEquals(LmuWindowsTyreWearRatio(0.82), result.wheels[WheelIndex.FRONT_RIGHT])
            assertEquals(LmuWindowsTyreWearRatio(0.85), result.wheels[WheelIndex.REAR_LEFT])
            assertEquals(LmuWindowsTyreWearRatio(0.87), result.wheels[WheelIndex.REAR_RIGHT])
            assertEquals("/ws/lmu_windows/tyre_wear", server.takeRequest().path)
        }
}

private class FakeServerIpPreferencesRepositoryForTyreWear(
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

private val TYRE_WEAR_JSON =
    """
    {
        "wheels": {
            "FRONT_LEFT": 0.8,
            "FRONT_RIGHT": 0.82,
            "REAR_LEFT": 0.85,
            "REAR_RIGHT": 0.87
        }
    }
    """.trimIndent()
