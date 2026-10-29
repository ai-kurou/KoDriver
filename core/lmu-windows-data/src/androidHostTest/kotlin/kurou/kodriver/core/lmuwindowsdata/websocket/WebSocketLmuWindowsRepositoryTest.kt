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
import kotlinx.serialization.json.Json
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
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class WebSocketLmuWindowsRepositoryTest {
    private lateinit var server: MockWebServer
    private lateinit var fakeIpRepository: FakeServerIpPreferencesRepositoryForTelemetry

    private val telemetryResponses = LinkedBlockingQueue<MockResponse>()
    private val openedSockets = CopyOnWriteArrayList<WebSocket>()

    @BeforeTest
    fun setUp() {
        server = MockWebServer()
        server.dispatcher =
            object : Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse =
                    when (request.path) {
                        "/ws/lmu_windows/telemetry" -> {
                            telemetryResponses.poll(1, TimeUnit.SECONDS) ?: MockResponse().setResponseCode(404)
                        }

                        else -> {
                            MockResponse().setResponseCode(404)
                        }
                    }
            }
        server.start()
        fakeIpRepository = FakeServerIpPreferencesRepositoryForTelemetry(null)
    }

    @AfterTest
    fun tearDown() {
        openedSockets.forEach { it.close(1000, "done") }
        server.shutdown()
    }

    private fun buildRepository(
        retryDelayMs: Long = 3_000L,
        currentTimeMs: () -> Long = System::currentTimeMillis,
    ) = WebSocketLmuWindowsRepository(
        serverIpRepository = fakeIpRepository,
        port = server.port,
        retryDelayMs = retryDelayMs,
        currentTimeMs = currentTimeMs,
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
    fun `有効なJSONフレームを受信したとき全フィールドを復元する`() =
        runTest {
            telemetryResponses.add(
                MockResponse().withWebSocketUpgrade(
                    object : WebSocketListener() {
                        override fun onOpen(
                            webSocket: WebSocket,
                            response: Response,
                        ) {
                            webSocket.send(TELEMETRY_JSON)
                            webSocket.close(1000, "done")
                        }
                    },
                ),
            )
            fakeIpRepository.setIp("127.0.0.1")

            val result = buildRepository(currentTimeMs = { 1234L }).telemetryStream().first()

            assertEquals(60_000L, result.timing.bestLapTimeMs)
            assertEquals(2, result.timing.currentLap)
            val expected =
                Json { ignoreUnknownKeys = true }.decodeFromString<LmuWindowsTelemetryData>(TELEMETRY_JSON)
            assertEquals(expected.copy(fuel = result.fuel), result)
            assertEquals(expected.fuel.copy(receivedAtMs = result.fuel.receivedAtMs), result.fuel)
            assertEquals(1234L, result.fuel.receivedAtMs)
            assertEquals("/ws/lmu_windows/telemetry", server.takeRequest(1, TimeUnit.SECONDS)?.path)
        }

    @Test
    fun `不正なJSONフレームは無視されて次のフレームが処理される`() =
        runTest {
            telemetryResponses.add(
                MockResponse().withWebSocketUpgrade(
                    object : WebSocketListener() {
                        override fun onOpen(
                            webSocket: WebSocket,
                            response: Response,
                        ) {
                            webSocket.send("invalid json")
                            webSocket.send(TELEMETRY_JSON)
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
            telemetryResponses.add(
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
            telemetryResponses.add(
                MockResponse().withWebSocketUpgrade(
                    object : WebSocketListener() {
                        override fun onOpen(
                            webSocket: WebSocket,
                            response: Response,
                        ) {
                            webSocket.send(TELEMETRY_JSON)
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
            telemetryResponses.add(
                MockResponse().withWebSocketUpgrade(
                    object : WebSocketListener() {
                        override fun onOpen(
                            webSocket: WebSocket,
                            response: Response,
                        ) {
                            webSocket.send(TELEMETRY_JSON)
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
    fun `IP変更で全テレメトリの接続を切り替える`() =
        runTest {
            repeat(2) { telemetryResponses.add(webSocketResponse(TELEMETRY_JSON)) }
            fakeIpRepository.setIp("127.0.0.1")
            val results = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
            val job =
                launch(Dispatchers.IO) {
                    buildRepository().telemetryStream().collect { results.send(it) }
                }
            try {
                withContext(Dispatchers.IO) {
                    withTimeout(5_000) {
                        assertEquals(123456L, results.receive().timestampMs)
                        assertEquals("/ws/lmu_windows/telemetry", server.takeRequest(1, TimeUnit.SECONDS)?.path)
                        fakeIpRepository.setIp("localhost")
                        assertEquals(123456L, results.receive().timestampMs)
                        assertEquals("/ws/lmu_windows/telemetry", server.takeRequest(1, TimeUnit.SECONDS)?.path)
                    }
                }
            } finally {
                job.cancel()
            }
        }

    @Test
    fun `受信が止まると燃料が失効し再受信で回復する`() =
        runTest {
            val sockets = Channel<WebSocket>(Channel.UNLIMITED)
            var currentTimeMs = 1_000L
            telemetryResponses.add(
                MockResponse().withWebSocketUpgrade(
                    object : WebSocketListener() {
                        override fun onOpen(
                            webSocket: WebSocket,
                            response: Response,
                        ) {
                            openedSockets.add(webSocket)
                            sockets.trySend(webSocket)
                        }
                    },
                ),
            )
            fakeIpRepository.setIp("127.0.0.1")
            val results = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
            val job =
                launch(Dispatchers.IO) {
                    buildRepository(currentTimeMs = { currentTimeMs++ })
                        .telemetryStream()
                        .collect { results.send(it) }
                }
            try {
                withContext(Dispatchers.IO) {
                    withTimeout(15_000) {
                        val socket = sockets.receive()
                        socket.send(TELEMETRY_JSON)
                        val received = results.receive()
                        assertEquals(1_000L, received.fuel.receivedAtMs)
                        val expired = results.receive()
                        assertEquals(0.0, expired.fuel.capacityLiters.value)
                        assertNull(expired.fuel.receivedAtMs)
                        assertEquals(received.copy(fuel = expired.fuel), expired)
                        socket.send(TELEMETRY_JSON)
                        val restored = results.receive()
                        assertEquals(100.0, restored.fuel.capacityLiters.value)
                        assertEquals(1_001L, restored.fuel.receivedAtMs)
                    }
                }
            } finally {
                job.cancel()
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

private class FakeServerIpPreferencesRepositoryForTelemetry(
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

private val TELEMETRY_JSON =
    """
    {
        "timestampMs": 123456,
        "engine": {"rpm": 6500.0, "maxRpm": 9000.0, "gear": 4},
        "inputs": {"throttle": 0.8, "brake": 0.2, "clutch": 0.1, "steering": -0.3},
        "tyres": {"wheels": {
            "FRONT_LEFT": {
                "surfaceTemperature": 91.0, "carcassTemperature": 85.0,
                "brakeTemperature": 400.0, "pressureKpa": 180.0, "wear": 0.9
            }
        }},
        "fuel": {"currentLiters": 42.0, "capacityLiters": 100.0, "receivedAtMs": 1},
        "timing": {
            "currentLapTimeMs": 30000, "lastLapTimeMs": 61000, "bestLapTimeMs": 60000,
            "sector1Ms": 20000, "sector1And2Ms": 40000, "currentLap": 2, "maxLaps": 10
        },
        "vehicle": {
            "localVelocityX": 1.0, "localVelocityY": 2.0, "localVelocityZ": 50.0,
            "positionX": 100.0, "positionY": 200.0, "positionZ": 300.0
        },
        "futureField": true
    }
    """.trimIndent()
