package kurou.kodriver.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.EngineData
import kurou.kodriver.domain.model.FuelData
import kurou.kodriver.domain.model.InputsData
import kurou.kodriver.domain.model.LmuTelemetryData
import kurou.kodriver.domain.model.TimingData
import kurou.kodriver.domain.model.TyreData
import kurou.kodriver.domain.model.VehicleData
import kurou.kodriver.domain.repository.LmuRepository
import kurou.kodriver.domain.usecase.DisconnectLmuUseCase
import kurou.kodriver.domain.usecase.ObserveLmuUseCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LmuViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ---- ファクトリヘルパー ----

    private fun makeTelemetry(speedX: Double = 0.0) = LmuTelemetryData(
        timestampMs = 0L,
        engine = EngineData(rpm = 0.0, maxRpm = 0.0, gear = 0),
        inputs = InputsData(throttle = 0.0, brake = 0.0, clutch = 0.0, steering = 0.0),
        tyres = TyreData(wheels = emptyMap()),
        fuel = FuelData(currentLiters = 0.0, capacityLiters = 0.0),
        timing = TimingData(
            currentLapTimeMs = 0L, lastLapTimeMs = 0L, bestLapTimeMs = 0L,
            sector1Ms = 0L, sector2Ms = 0L, currentLap = 0,
        ),
        vehicle = VehicleData(
            localVelocityX = speedX, localVelocityY = 0.0, localVelocityZ = 0.0,
            positionX = 0.0, positionY = 0.0, positionZ = 0.0,
        ),
    )

    private fun buildViewModel(
        stream: Flow<LmuTelemetryData> = flowOf(),
        isConnected: Boolean = true,
        ttsEngine: TtsEngine = TtsEngine {},
    ): LmuViewModel {
        val repo = object : LmuRepository {
            override fun telemetryStream(): Flow<LmuTelemetryData> = stream
            override suspend fun isConnected(): Boolean = isConnected
            override suspend fun disconnect() {}
        }
        return LmuViewModel(
            observeLmu = ObserveLmuUseCase(repo),
            disconnect = DisconnectLmuUseCase(repo),
            ttsEngine = ttsEngine,
        )
    }

    // 本番の factory と同じく startObserving() を呼び出す
    private fun makeViewModel(
        stream: Flow<LmuTelemetryData> = flowOf(),
        isConnected: Boolean = true,
        ttsEngine: TtsEngine = TtsEngine {},
    ) = buildViewModel(stream, isConnected, ttsEngine).also { it.startObserving() }

    // ---- 初期状態・データ受信 ----

    @Test
    fun `startObserving前はConnectingでemit後は最新データがuiStateに反映される`() = runTest {
        val sharedFlow = MutableSharedFlow<LmuTelemetryData>(extraBufferCapacity = 10)
        val vm = buildViewModel(stream = sharedFlow)

        // startObserving 前は Connecting
        assertEquals(LmuUiState.Connecting, vm.uiState.value)

        vm.startObserving()

        // 1件目の emit → Connected に遷移
        val first = makeTelemetry(speedX = 5.0)
        sharedFlow.emit(first)
        assertEquals(LmuUiState.Connected(first), vm.uiState.value)

        // 2件目の emit → 最新データに更新
        val last = makeTelemetry(speedX = 20.0)
        sharedFlow.emit(last)
        assertEquals(LmuUiState.Connected(last), vm.uiState.value)
    }

    // ---- エラー処理 ----

    @Test
    fun `telemetryStreamが例外をthrowするとErrorになる`() = runTest {
        val errorMsg = "connection failed"
        val stream = flow<LmuTelemetryData> { error(errorMsg) }
        val vm = makeViewModel(stream = stream)

        val state = vm.uiState.value
        assertIs<LmuUiState.Error>(state)
        assertEquals(errorMsg, state.message)
    }

    @Test
    fun `例外メッセージがnullのときUnknown errorになる`() = runTest {
        val stream = flow<LmuTelemetryData> { throw RuntimeException(null as String?) }
        val vm = makeViewModel(stream = stream)

        val state = vm.uiState.value
        assertIs<LmuUiState.Error>(state)
        assertEquals("Unknown error", state.message)
    }

    // ---- TTS アナウンス ----

    @Test
    fun `速度が200km-h以上でTTSが発話される`() = runTest {
        val spokenMessages = mutableListOf<String>()
        // 200 km/h ≈ 55.56 m/s → localVelocityX = 55.56 で speedKmh ≈ 200.0
        val data = makeTelemetry(speedX = 55.56)
        val vm = makeViewModel(
            stream = flowOf(data),
            ttsEngine = TtsEngine { spokenMessages += it },
        )

        assertTrue(vm.uiState.value is LmuUiState.Connected)
        assertTrue(spokenMessages.isNotEmpty(), "200km/h到達時に発話が期待される")
    }

    @Test
    fun `速度が200km-h未満ではTTSは発話されない`() = runTest {
        val spokenMessages = mutableListOf<String>()
        // 199 km/h ≈ 55.28 m/s
        val data = makeTelemetry(speedX = 55.28)
        val vm = makeViewModel(
            stream = flowOf(data),
            ttsEngine = TtsEngine { spokenMessages += it },
        )

        assertTrue(vm.uiState.value is LmuUiState.Connected)
        assertTrue(spokenMessages.isEmpty(), "200km/h未満では発話されないはず")
    }

    @Test
    fun `200km-h到達後は195km-h未満に落ちるまで再発話せず落下後は再発話する`() = runTest {
        val spokenMessages = mutableListOf<String>()
        val sharedFlow = MutableSharedFlow<LmuTelemetryData>(extraBufferCapacity = 10)
        val vm = makeViewModel(
            stream = sharedFlow,
            ttsEngine = TtsEngine { spokenMessages += it },
        )

        // 1回目の 200km/h 到達 → 発話
        sharedFlow.emit(makeTelemetry(speedX = 55.56))
        assertEquals(1, spokenMessages.size)

        // 200km/h 超で継続 → 再発話しない
        sharedFlow.emit(makeTelemetry(speedX = 60.0))
        assertEquals(1, spokenMessages.size)

        // 195km/h 未満に低下（フラグリセット）
        sharedFlow.emit(makeTelemetry(speedX = 54.0)) // 54 * 3.6 = 194.4 km/h
        assertEquals(1, spokenMessages.size)

        // 2回目の 200km/h 到達 → 再発話
        sharedFlow.emit(makeTelemetry(speedX = 55.56))
        assertEquals(2, spokenMessages.size)
    }

    // ---- reconnect ----

    @Test
    fun `reconnectするとConnectingに戻り新しいデータを受信できる`() = runTest {
        val sharedFlow = MutableSharedFlow<LmuTelemetryData>(extraBufferCapacity = 10)
        val vm = makeViewModel(stream = sharedFlow)

        sharedFlow.emit(makeTelemetry(speedX = 1.0))
        assertIs<LmuUiState.Connected>(vm.uiState.value)

        vm.reconnect()
        assertEquals(LmuUiState.Connecting, vm.uiState.value)

        val newData = makeTelemetry(speedX = 99.0)
        sharedFlow.emit(newData)
        assertEquals(LmuUiState.Connected(newData), vm.uiState.value)
    }
}
