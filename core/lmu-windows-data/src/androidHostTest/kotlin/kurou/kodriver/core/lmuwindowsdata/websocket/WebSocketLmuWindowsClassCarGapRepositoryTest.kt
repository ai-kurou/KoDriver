@file:Suppress("FunctionNaming")

package kurou.kodriver.core.lmuwindowsdata.websocket

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsClassCarGapData
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

class WebSocketLmuWindowsClassCarGapRepositoryTest {
    private lateinit var server: MockWebServer
    private lateinit var fakeIpRepository: FakeServerIpPreferencesRepositoryForClassCarGap

    @BeforeTest
    fun setUp() {
        server = MockWebServer()
        server.start()
        fakeIpRepository = FakeServerIpPreferencesRepositoryForClassCarGap(null)
    }

    @AfterTest
    fun tearDown() {
        try {
            server.shutdown()
        } catch (_: IllegalStateException) {
        }
    }

    private fun buildRepository(retryDelayMs: Long = 0L) =
        WebSocketLmuWindowsClassCarGapRepository(
            serverIpRepository = fakeIpRepository,
            port = server.port,
            retryDelayMs = retryDelayMs,
        )

    @Test
    fun `ipがnullのときclassCarGapStreamは何もemitしない`() =
        runTest {
            val result =
                withTimeoutOrNull(300) {
                    buildRepository().classCarGapStream().first()
                }
            assertNull(result)
        }

    @Test
    fun `有効なJSONフレームを受信したときLmuWindowsClassCarGapDataをemitする`() =
        runTest {
            server.enqueue(
                MockResponse().withWebSocketUpgrade(
                    object : WebSocketListener() {
                        override fun onOpen(
                            webSocket: WebSocket,
                            response: Response,
                        ) {
                            webSocket.send(CLASS_CAR_GAP_JSON)
                            webSocket.close(1000, "done")
                        }
                    },
                ),
            )
            fakeIpRepository.setIp("127.0.0.1")

            val result = buildRepository().classCarGapStream().first()

            assertEquals(LmuWindowsClassCarGapData(1.25, 2.5, classPosition = 2, classVehicleCount = 3), result)
            assertEquals("/ws/lmu_windows/class_car_gap", server.takeRequest().path)
        }

    @Test
    fun `nullを含むJSONフレームを受信したとき欠損値を保持する`() =
        runTest {
            server.enqueue(
                MockResponse().withWebSocketUpgrade(
                    object : WebSocketListener() {
                        override fun onOpen(
                            webSocket: WebSocket,
                            response: Response,
                        ) {
                            webSocket.send("""{"gapAheadSeconds":null,"gapBehindSeconds":2.5}""")
                            webSocket.close(1000, "done")
                        }
                    },
                ),
            )
            fakeIpRepository.setIp("127.0.0.1")

            val result = buildRepository().classCarGapStream().first()

            assertEquals(LmuWindowsClassCarGapData(null, 2.5), result)
            assertEquals("/ws/lmu_windows/class_car_gap", server.takeRequest().path)
        }
}

private class FakeServerIpPreferencesRepositoryForClassCarGap(
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

private val CLASS_CAR_GAP_JSON =
    """
    {
        "gapAheadSeconds": 1.25,
        "gapBehindSeconds": 2.5,
        "classPosition": 2,
        "classVehicleCount": 3,
        "unknownField": true
    }
    """.trimIndent()
