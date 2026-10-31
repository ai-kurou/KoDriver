@file:Suppress("FunctionNaming")

package kurou.kodriver.core.acewindowsdata.websocket

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import kurou.kodriver.domain.repository.ServerIpPreferencesRepository
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import kotlin.properties.Delegates
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class WebSocketAceWindowsOverallPositionRepositoryTest {
    private var server: MockWebServer by Delegates.notNull()
    private var fakeIpRepository: FakeAceOverallPositionServerIpPreferencesRepository by Delegates.notNull()

    @BeforeTest
    fun setUp() {
        server = MockWebServer()
        server.start()
        fakeIpRepository = FakeAceOverallPositionServerIpPreferencesRepository(null)
    }

    @AfterTest
    fun tearDown() {
        server.shutdown()
    }

    private fun buildRepository(retryDelayMs: Long = 0L) =
        WebSocketAceWindowsOverallPositionRepository(
            serverIpRepository = fakeIpRepository,
            port = server.port,
            retryDelayMs = retryDelayMs,
        )

    @Test
    fun `ipがnullのときoverallPositionStreamは何もemitしない`() =
        runTest {
            val result =
                withTimeoutOrNull(300) {
                    buildRepository().overallPositionStream().first()
                }
            assertNull(result)
        }

    @Test
    fun `有効なJSONフレームを受信したときAceWindowsOverallPositionDataをemitする`() =
        runTest {
            server.enqueue(
                MockResponse().withWebSocketUpgrade(
                    object : WebSocketListener() {
                        override fun onOpen(
                            webSocket: WebSocket,
                            response: Response,
                        ) {
                            webSocket.send(OVERALL_POSITION_JSON)
                            webSocket.close(1000, "done")
                        }
                    },
                ),
            )
            fakeIpRepository.setIp("127.0.0.1")

            val result = buildRepository().overallPositionStream().first()

            assertEquals(2, result.position)
            assertEquals(20, result.totalDrivers)
            assertEquals("/ws/ace_windows/overall_position", server.takeRequest().path)
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
                            webSocket.send(OVERALL_POSITION_JSON)
                            webSocket.close(1000, "done")
                        }
                    },
                ),
            )

            fakeIpRepository.setIp(null)
            val repository = buildRepository()

            val noEmit = withTimeoutOrNull(300) { repository.overallPositionStream().first() }
            assertNull(noEmit)

            fakeIpRepository.setIp("127.0.0.1")
            val result = repository.overallPositionStream().first()
            assertEquals(2, result.position)
            assertEquals(20, result.totalDrivers)
        }
}

private class FakeAceOverallPositionServerIpPreferencesRepository(
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

private const val OVERALL_POSITION_JSON = """{"position": 2, "totalDrivers": 20}"""
