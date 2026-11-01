@file:Suppress("FunctionNaming")

package kurou.kodriver.core.lmuwindowsdata.websocket

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
import kotlin.test.assertNull

class WebSocketLmuWindowsTyreDetachedRepositoryTest {
    private lateinit var server: MockWebServer
    private lateinit var fakeIpRepository: FakeServerIpPreferencesRepositoryForTyreDetached

    @BeforeTest
    fun setUp() {
        server = MockWebServer()
        server.start()
        fakeIpRepository = FakeServerIpPreferencesRepositoryForTyreDetached(null)
    }

    @AfterTest
    fun tearDown() {
        try {
            server.shutdown()
        } catch (_: IllegalStateException) {
        }
    }

    private fun buildRepository() =
        WebSocketLmuWindowsTyreDetachedRepository(
            serverIpRepository = fakeIpRepository,
            port = server.port,
            retryDelayMs = 0L,
        )

    @Test
    fun `ipがnullのときtyreDetachedStreamは何もemitしない`() =
        runTest {
            val result =
                withTimeoutOrNull(300) {
                    buildRepository().tyreDetachedStream().first()
                }
            assertNull(result)
        }

    @Test
    fun `有効なJSONフレームを受信したときLmuWindowsTyreDetachedDataをemitする`() =
        runTest {
            server.enqueue(
                MockResponse().withWebSocketUpgrade(
                    object : WebSocketListener() {
                        override fun onOpen(
                            webSocket: WebSocket,
                            response: Response,
                        ) {
                            webSocket.send(TYRE_DETACHED_JSON)
                            webSocket.close(1000, "done")
                        }
                    },
                ),
            )
            fakeIpRepository.setIp("127.0.0.1")

            val result = buildRepository().tyreDetachedStream().first()

            assertEquals(true, result.wheels[WheelIndex.FRONT_LEFT])
            assertEquals(false, result.wheels[WheelIndex.FRONT_RIGHT])
            assertEquals(false, result.wheels[WheelIndex.REAR_LEFT])
            assertEquals(false, result.wheels[WheelIndex.REAR_RIGHT])
            assertEquals("/ws/lmu_windows/tyre_detached", server.takeRequest().path)
        }
}

private class FakeServerIpPreferencesRepositoryForTyreDetached(
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

private val TYRE_DETACHED_JSON =
    """
    {
        "wheels": {
            "FRONT_LEFT": true,
            "FRONT_RIGHT": false,
            "REAR_LEFT": false,
            "REAR_RIGHT": false
        }
    }
    """.trimIndent()
