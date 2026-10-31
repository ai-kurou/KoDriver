@file:Suppress("FunctionNaming")

package kurou.kodriver.core.acewindowsdata.websocket

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import kurou.kodriver.domain.acewindows.model.AceWindowsFlagType
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

class WebSocketAceWindowsFlagRepositoryTest {
    private lateinit var server: MockWebServer
    private lateinit var fakeIpRepository: FakeAceFlagServerIpPreferencesRepository

    @BeforeTest
    fun setUp() {
        server = MockWebServer()
        server.start()
        fakeIpRepository = FakeAceFlagServerIpPreferencesRepository(null)
    }

    @AfterTest
    fun tearDown() {
        server.shutdown()
    }

    private fun buildRepository() =
        WebSocketAceWindowsFlagRepository(
            serverIpRepository = fakeIpRepository,
            port = server.port,
            retryDelayMs = 0L,
        )

    @Test
    fun `ipがnullのときflagStreamは何もemitしない`() =
        runTest {
            val result =
                withTimeoutOrNull(300) {
                    buildRepository().flagStream().first()
                }
            assertNull(result)
        }

    @Test
    fun `有効なJSONフレームを受信したときAceWindowsFlagDataをemitする`() =
        runTest {
            server.enqueue(
                MockResponse().withWebSocketUpgrade(
                    object : WebSocketListener() {
                        override fun onOpen(
                            webSocket: WebSocket,
                            response: Response,
                        ) {
                            webSocket.send(BLUE_FLAG_JSON)
                            webSocket.close(1000, "done")
                        }
                    },
                ),
            )
            fakeIpRepository.setIp("127.0.0.1")

            val result = buildRepository().flagStream().first()

            assertEquals(AceWindowsFlagType.BLUE_FLAG, result.flag)
            assertEquals("/ws/ace_windows/flags", server.takeRequest().path)
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
                            webSocket.send(YELLOW_FLAG_JSON)
                            webSocket.close(1000, "done")
                        }
                    },
                ),
            )

            fakeIpRepository.setIp(null)
            val repository = buildRepository()

            val noEmit = withTimeoutOrNull(300) { repository.flagStream().first() }
            assertNull(noEmit)

            fakeIpRepository.setIp("127.0.0.1")
            val result = repository.flagStream().first()
            assertEquals(AceWindowsFlagType.YELLOW_FLAG, result.flag)
        }
}

private class FakeAceFlagServerIpPreferencesRepository(
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

private const val BLUE_FLAG_JSON = """{"flag": "BLUE_FLAG"}"""
private const val YELLOW_FLAG_JSON = """{"flag": "YELLOW_FLAG"}"""
