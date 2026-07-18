@file:Suppress("FunctionNaming")

package kurou.kodriver.data

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
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class WebSocketVirtualEnergyRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var fakeIpRepository: FakeServerIpPreferencesRepositoryForVirtualEnergy

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        fakeIpRepository = FakeServerIpPreferencesRepositoryForVirtualEnergy(null)
    }

    @After
    fun tearDown() {
        try {
            server.shutdown()
        } catch (_: IllegalStateException) {
        }
    }

    private fun buildRepository(retryDelayMs: Long = 0L) = WebSocketLmuWindowsVirtualEnergyRepository(
        serverIpRepository = fakeIpRepository,
        port = server.port,
        retryDelayMs = retryDelayMs,
    )

    @Test
    fun `ipがnullのときvirtualEnergyStreamは何もemitしない`() = runTest {
        val result = withTimeoutOrNull(300) {
            buildRepository().virtualEnergyStream().first()
        }
        assertNull(result)
    }

    @Test
    fun `有効なJSONフレームを受信したときLmuWindowsVirtualEnergyDataをemitする`() = runTest {
        server.enqueue(
            MockResponse().withWebSocketUpgrade(
                object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        webSocket.send(VIRTUAL_ENERGY_JSON)
                        webSocket.close(1000, "done")
                    }
                },
            ),
        )
        fakeIpRepository.setIp("127.0.0.1")

        val result = buildRepository().virtualEnergyStream().first()

        assertEquals(0.5, result.remainingRatio)
        assertEquals("/ws/lmu_windows/virtual_energy", server.takeRequest().path)
    }

    @Test
    fun `不正なJSONフレームは無視されて次のフレームが処理される`() = runTest {
        server.enqueue(
            MockResponse().withWebSocketUpgrade(
                object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        webSocket.send("invalid json")
                        webSocket.send(VIRTUAL_ENERGY_JSON)
                        webSocket.close(1000, "done")
                    }
                },
            ),
        )
        fakeIpRepository.setIp("127.0.0.1")

        val result = buildRepository().virtualEnergyStream().first()

        assertNotNull(result)
        assertEquals(0.5, result.remainingRatio)
    }

    @Test
    fun `接続に失敗した場合は例外を捕捉してリトライする`() = runTest {
        val closedPort = server.port
        server.shutdown()
        fakeIpRepository.setIp("127.0.0.1")
        val repository = WebSocketLmuWindowsVirtualEnergyRepository(
            serverIpRepository = fakeIpRepository,
            port = closedPort,
            retryDelayMs = 0L,
        )

        val result = withTimeoutOrNull(300) {
            repository.virtualEnergyStream().first()
        }

        assertNull(result)
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
                        webSocket.send(VIRTUAL_ENERGY_JSON)
                        webSocket.close(1000, "done")
                    }
                },
            ),
        )
        fakeIpRepository.setIp("127.0.0.1")

        val result = buildRepository(retryDelayMs = 0L).virtualEnergyStream().first()

        assertEquals(0.5, result.remainingRatio)
    }
}

private class FakeServerIpPreferencesRepositoryForVirtualEnergy(
    initialIp: String?,
) : ServerIpPreferencesRepository {
    private val _ip = MutableStateFlow(initialIp)
    fun setIp(ip: String?) { _ip.update { ip } }
    override fun serverIp(): Flow<String?> = _ip.asStateFlow()
    override suspend fun saveServerIp(ip: String) { _ip.update { ip } }
}

private val VIRTUAL_ENERGY_JSON = """
    {
        "remainingRatio": 0.5
    }
""".trimIndent()
