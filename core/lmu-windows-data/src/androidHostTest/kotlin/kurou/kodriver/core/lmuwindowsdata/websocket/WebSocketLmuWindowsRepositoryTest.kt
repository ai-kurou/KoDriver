@file:Suppress("FunctionNaming")

package kurou.kodriver.core.lmuwindowsdata.websocket

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.repository.ServerIpPreferencesRepository
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WebSocketLmuWindowsRepositoryTest {
    private lateinit var server: MockWebServer
    private lateinit var fakeIpRepository: FakeServerIpPreferencesRepositoryForMyBestLap

    private val timingResponses = LinkedBlockingQueue<MockResponse>()
    private val openedSockets = CopyOnWriteArrayList<WebSocket>()

    @BeforeTest
    fun setUp() {
        server = MockWebServer()
        server.dispatcher =
            object : Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse =
                    when (request.path) {
                        "/ws/lmu_windows/my_best_lap" -> {
                            timingResponses.poll(1, TimeUnit.SECONDS) ?: MockResponse().setResponseCode(404)
                        }

                        else -> {
                            MockResponse().setResponseCode(404)
                        }
                    }
            }
        server.start()
        fakeIpRepository = FakeServerIpPreferencesRepositoryForMyBestLap(null)
    }

    @AfterTest
    fun tearDown() {
        openedSockets.forEach { it.close(1000, "done") }
        server.shutdown()
    }

    private fun buildRepository(retryDelayMs: Long = 3_000L) =
        WebSocketLmuWindowsRepository(
            serverIpRepository = fakeIpRepository,
            port = server.port,
            retryDelayMs = retryDelayMs,
        )

    @Test
    fun `ipがnullのときtelemetryStreamは何もemitしない`() =
        runTest {
            val result =
                withTimeoutOrNull(300) {
                    buildRepository().telemetryStream().first()
                }
            assertNull(result)
        }

    @Test
    fun `有効なJSONフレームを受信したときtimingを反映したLmuWindowsTelemetryDataをemitする`() =
        runTest {
            timingResponses.add(
                MockResponse().withWebSocketUpgrade(
                    object : WebSocketListener() {
                        override fun onOpen(
                            webSocket: WebSocket,
                            response: Response,
                        ) {
                            webSocket.send(TIMING_JSON)
                            webSocket.close(1000, "done")
                        }
                    },
                ),
            )
            fakeIpRepository.setIp("127.0.0.1")

            val result = buildRepository().telemetryStream().first()

            assertEquals(60_000L, result.timing.bestLapTimeMs)
            assertEquals(2, result.timing.currentLap)
        }

    @Test
    fun `不正なJSONフレームは無視されて次のフレームが処理される`() =
        runTest {
            timingResponses.add(
                MockResponse().withWebSocketUpgrade(
                    object : WebSocketListener() {
                        override fun onOpen(
                            webSocket: WebSocket,
                            response: Response,
                        ) {
                            webSocket.send("invalid json")
                            webSocket.send(TIMING_JSON)
                            webSocket.close(1000, "done")
                        }
                    },
                ),
            )
            fakeIpRepository.setIp("127.0.0.1")

            val result = buildRepository().telemetryStream().first()

            assertEquals(60_000L, result.timing.bestLapTimeMs)
        }

    @Test
    fun `接続切断後にリトライして再接続する`() =
        runTest {
            timingResponses.add(
                MockResponse().withWebSocketUpgrade(
                    object : WebSocketListener() {
                        override fun onOpen(
                            webSocket: WebSocket,
                            response: Response,
                        ) {
                            webSocket.close(1001, "drop")
                        }
                    },
                ),
            )
            timingResponses.add(
                MockResponse().withWebSocketUpgrade(
                    object : WebSocketListener() {
                        override fun onOpen(
                            webSocket: WebSocket,
                            response: Response,
                        ) {
                            webSocket.send(TIMING_JSON)
                            webSocket.close(1000, "done")
                        }
                    },
                ),
            )
            fakeIpRepository.setIp("127.0.0.1")

            val result = buildRepository(retryDelayMs = 0L).telemetryStream().first()

            assertEquals(60_000L, result.timing.bestLapTimeMs)
        }

    @Test
    fun `IPがnullになるとemitが止まり再設定すると再接続してデータをemitする`() =
        runTest {
            timingResponses.add(
                MockResponse().withWebSocketUpgrade(
                    object : WebSocketListener() {
                        override fun onOpen(
                            webSocket: WebSocket,
                            response: Response,
                        ) {
                            webSocket.send(TIMING_JSON)
                            webSocket.close(1000, "done")
                        }
                    },
                ),
            )

            fakeIpRepository.setIp(null)
            val repository = buildRepository()

            val noEmit = withTimeoutOrNull(300) { repository.telemetryStream().first() }
            assertNull(noEmit)

            fakeIpRepository.setIp("127.0.0.1")
            val result = repository.telemetryStream().first()
            assertEquals(60_000L, result.timing.bestLapTimeMs)
        }

    @Test
    fun `fuelを受信するとtelemetryの燃料に反映する`() =
        runTest {
            serveTimingAndFuel()
            fakeIpRepository.setIp("127.0.0.1")

            val result = buildRepository().telemetryStream().first { it.fuel.capacityLiters.value > 0.0 }

            assertEquals(42.0, result.fuel.currentLiters.value)
            assertEquals(100.0, result.fuel.capacityLiters.value)
            assertEquals(60_000L, result.timing.bestLapTimeMs)
        }

    @Test
    fun `fuelが404でもtimingと初期燃料をemitする`() =
        runTest {
            val fuelRequested = CountDownLatch(1)
            server.dispatcher =
                object : Dispatcher() {
                    override fun dispatch(request: RecordedRequest): MockResponse =
                        when (request.path) {
                            "/ws/lmu_windows/fuel" -> {
                                fuelRequested.countDown()
                                MockResponse().setResponseCode(404)
                            }

                            "/ws/lmu_windows/my_best_lap" -> {
                                fuelRequested.await(1, TimeUnit.SECONDS)
                                webSocketResponse(TIMING_JSON)
                            }

                            else -> {
                                MockResponse().setResponseCode(404)
                            }
                        }
                }
            fakeIpRepository.setIp("127.0.0.1")

            val result = buildRepository().telemetryStream().first()

            assertTrue(fuelRequested.await(1, TimeUnit.SECONDS))

            assertEquals(60_000L, result.timing.bestLapTimeMs)
            assertEquals(0.0, result.fuel.currentLiters.value)
            assertEquals(0.0, result.fuel.capacityLiters.value)
        }

    @Test
    fun `IP変更でtimingとfuelの両方を再接続する`() =
        runTest {
            serveTimingAndFuel()
            fakeIpRepository.setIp("127.0.0.1")
            val results = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
            val job =
                launch(Dispatchers.IO) {
                    buildRepository().telemetryStream().collect { results.send(it) }
                }
            try {
                withContext(Dispatchers.IO) {
                    withTimeout(5_000) {
                        while (results
                                .receive()
                                .fuel.capacityLiters.value == 0.0
                        ) {
                            // 燃料受信まで初期値を読み飛ばす。
                        }
                    }
                    val firstPaths = List(2) { server.takeRequest(1, TimeUnit.SECONDS)?.path }.toSet()
                    assertEquals(setOf("/ws/lmu_windows/my_best_lap", "/ws/lmu_windows/fuel"), firstPaths)
                    fakeIpRepository.setIp("localhost")
                    withTimeout(5_000) {
                        while (results
                                .receive()
                                .fuel.capacityLiters.value == 0.0
                        ) {
                            // 新しい接続で燃料を受信するまで待つ。
                        }
                    }
                    val secondPaths = List(2) { server.takeRequest(1, TimeUnit.SECONDS)?.path }.toSet()
                    assertEquals(firstPaths, secondPaths)
                }
            } finally {
                job.cancel()
            }
        }

    @Test
    fun `timing継続中もfuel切断後は期限切れになり再接続の新しいfuelで回復する`() =
        runTest {
            val timingSockets = Channel<WebSocket>(Channel.UNLIMITED)
            val fuelSockets = Channel<WebSocket>(Channel.UNLIMITED)
            server.dispatcher =
                object : Dispatcher() {
                    override fun dispatch(request: RecordedRequest): MockResponse =
                        MockResponse().withWebSocketUpgrade(
                            object : WebSocketListener() {
                                override fun onOpen(webSocket: WebSocket, response: Response) {
                                    openedSockets.add(webSocket)
                                    if (request.path == "/ws/lmu_windows/fuel") {
                                        fuelSockets.trySend(webSocket)
                                    } else {
                                        webSocket.send(TIMING_JSON)
                                        timingSockets.trySend(webSocket)
                                    }
                                }
                            },
                        )
                }
            fakeIpRepository.setIp("127.0.0.1")
            val results = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
            val job = launch(Dispatchers.IO) {
                buildRepository(retryDelayMs = 0L).telemetryStream().collect { results.send(it) }
            }
            try {
                withContext(Dispatchers.IO) {
                    withTimeout(15_000) {
                        val timing = timingSockets.receive()
                        val firstFuel = fuelSockets.receive()
                        firstFuel.send("""{"currentLiters":42.0,"capacityLiters":100.0}""")
                        var received = results.receive()
                        while (received.fuel.capacityLiters.value == 0.0) received = results.receive()
                        val receivedAt = received.fuel.receivedAtMs
                        assertTrue(receivedAt != null)
                        firstFuel.close(1000, "drop")
                        val reconnectedFuel = fuelSockets.receive()
                        timing.send(TIMING_JSON.replace("60000", "59000"))
                        var continued = results.receive()
                        while (continued.timing.bestLapTimeMs != 59_000L) continued = results.receive()
                        assertEquals(receivedAt, continued.fuel.receivedAtMs)
                        var expired = results.receive()
                        while (expired.fuel.capacityLiters.value > 0.0) expired = results.receive()
                        assertEquals(0.0, expired.fuel.currentLiters.value)
                        assertEquals(59_000L, expired.timing.bestLapTimeMs)
                        timing.send(TIMING_JSON)
                        assertEquals(0.0, results.receive().fuel.capacityLiters.value)
                        reconnectedFuel.send("""{"currentLiters":0.0,"capacityLiters":100.0}""")
                        val restored = results.receive()
                        assertEquals(100.0, restored.fuel.capacityLiters.value)
                        assertEquals(0.0, restored.fuel.currentLiters.value)
                        assertTrue(restored.fuel.receivedAtMs!! > receivedAt)
                    }
                }
            } finally {
                job.cancel()
            }
        }

    private fun serveTimingAndFuel() {
        server.dispatcher =
            object : Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse =
                    when (request.path) {
                        "/ws/lmu_windows/my_best_lap" -> webSocketResponse(TIMING_JSON)
                        "/ws/lmu_windows/fuel" -> webSocketResponse("""{"currentLiters":42.0,"capacityLiters":100.0}""")
                        else -> MockResponse().setResponseCode(404)
                    }
            }
    }

    private fun webSocketResponse(json: String): MockResponse =
        MockResponse().withWebSocketUpgrade(
            object : WebSocketListener() {
                override fun onOpen(
                    webSocket: WebSocket,
                    response: Response,
                ) {
                    openedSockets.add(webSocket)
                    webSocket.send(json)
                }
            },
        )

    @Test
    fun `isConnectedは常にfalseを返す`() =
        runTest {
            assertEquals(false, buildRepository().isConnected())
        }

    @Test
    fun `disconnectは何もしない`() =
        runTest {
            buildRepository().disconnect()
        }
}

private class FakeServerIpPreferencesRepositoryForMyBestLap(
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

private val TIMING_JSON =
    """
    {
        "currentLapTimeMs": 30000,
        "lastLapTimeMs": 61000,
        "bestLapTimeMs": 60000,
        "sector1Ms": 20000,
        "sector1And2Ms": 20000,
        "currentLap": 2,
        "maxLaps": 10
    }
    """.trimIndent()
