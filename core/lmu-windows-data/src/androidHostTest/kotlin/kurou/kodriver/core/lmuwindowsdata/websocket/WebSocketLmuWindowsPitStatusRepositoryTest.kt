@file:Suppress("FunctionNaming")

package kurou.kodriver.core.lmuwindowsdata.websocket

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsPitState
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

class WebSocketLmuWindowsPitStatusRepositoryTest {
    private lateinit var server: MockWebServer
    private lateinit var fakeIpRepository: FakeServerIpPreferencesRepositoryForPitStatus

    @BeforeTest
    fun setUp() {
        server = MockWebServer()
        server.start()
        fakeIpRepository = FakeServerIpPreferencesRepositoryForPitStatus(null)
    }

    @AfterTest
    fun tearDown() {
        try {
            server.shutdown()
        } catch (_: IllegalStateException) {
        }
    }

    private fun buildRepository(retryDelayMs: Long = 0L) =
        WebSocketLmuWindowsPitStatusRepository(
            serverIpRepository = fakeIpRepository,
            port = server.port,
            retryDelayMs = retryDelayMs,
        )

    @Test
    fun `ipがnullのときpitStatusStreamは何もemitしない`() =
        runTest {
            val result =
                withTimeoutOrNull(300) {
                    buildRepository().pitStatusStream().first()
                }
            assertNull(result)
        }

    @Test
    fun `有効なJSONフレームを受信したときLmuWindowsPitStatusDataをemitする`() =
        runTest {
            server.enqueue(
                MockResponse().withWebSocketUpgrade(
                    object : WebSocketListener() {
                        override fun onOpen(
                            webSocket: WebSocket,
                            response: Response,
                        ) {
                            webSocket.send(PIT_STATUS_JSON)
                            webSocket.close(1000, "done")
                        }
                    },
                ),
            )
            fakeIpRepository.setIp("127.0.0.1")

            val result = buildRepository().pitStatusStream().first()

            assertEquals(true, result.inPits)
            assertEquals(LmuWindowsPitState.ENTERING, result.pitState)
            assertEquals(false, result.inGarageStall)
            assertEquals("/ws/lmu_windows/pit_status", server.takeRequest().path)
        }
}

private class FakeServerIpPreferencesRepositoryForPitStatus(
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

private val PIT_STATUS_JSON =
    """
    {
        "inPits": true,
        "pitState": "ENTERING",
        "inGarageStall": false
    }
    """.trimIndent()
