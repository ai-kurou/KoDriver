@file:Suppress("FunctionNaming")

package kurou.kodriver.core.lmuwindowsdata.websocket

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class WebSocketLmuWindowsBrakeWearRepositoryTest {
    private lateinit var server: MockWebServer
    private lateinit var fakeIpRepository: FakeServerIpPreferencesRepositoryForBrakeWear

    @BeforeTest
    fun setUp() {
        server = MockWebServer()
        server.start()
        fakeIpRepository = FakeServerIpPreferencesRepositoryForBrakeWear(null)
    }

    @AfterTest
    fun tearDown() {
        try {
            server.shutdown()
        } catch (_: IllegalStateException) {
        }
    }

    private fun buildRepository() =
        WebSocketLmuWindowsBrakeWearRepository(
            serverIpRepository = fakeIpRepository,
            port = server.port,
            retryDelayMs = 0L,
        )

    @Test
    fun `ipがnullのときbrakeWearStreamは何もemitしない`() =
        runTest {
            val result =
                withTimeoutOrNull(300) {
                    buildRepository().brakeWearStream().first()
                }
            assertNull(result)
        }

    @Test
    fun `有効なJSONフレームを受信したときLmuWindowsBrakeWearDataをemitする`() =
        runTest {
            server.enqueue(
                MockResponse().withWebSocketUpgrade(
                    object : WebSocketListener() {
                        override fun onOpen(
                            webSocket: WebSocket,
                            response: Response,
                        ) {
                            webSocket.send(BRAKE_WEAR_JSON)
                            webSocket.close(1000, "done")
                        }
                    },
                ),
            )
            fakeIpRepository.setIp("127.0.0.1")

            val result = buildRepository().brakeWearStream().first()

            assertNotNull(result)
            assertEquals(0.03f, result.wheels[WheelIndex.FRONT_LEFT]?.value)
            assertEquals("/ws/lmu_windows/brake_wear", server.takeRequest().path)
        }

    @Test
    fun `nullフレームをemitした後も次のデータを受信する`() =
        runTest {
            server.enqueue(
                MockResponse().withWebSocketUpgrade(
                    object : WebSocketListener() {
                        override fun onOpen(
                            webSocket: WebSocket,
                            response: Response,
                        ) {
                            webSocket.send("null")
                            webSocket.send(BRAKE_WEAR_JSON)
                            webSocket.close(1000, "done")
                        }
                    },
                ),
            )
            fakeIpRepository.setIp("127.0.0.1")

            val results = buildRepository().brakeWearStream().take(2).toList()

            assertEquals(2, results.size)
            assertNull(results[0])
            val result = results[1]

            assertNotNull(result)
            assertEquals(0.03f, result.wheels[WheelIndex.FRONT_LEFT]?.value)
            assertEquals("/ws/lmu_windows/brake_wear", server.takeRequest().path)
        }
}

private class FakeServerIpPreferencesRepositoryForBrakeWear(
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

private val BRAKE_WEAR_JSON =
    """
    {
        "wheels": { "FRONT_LEFT": 0.03 }
    }
    """.trimIndent()
