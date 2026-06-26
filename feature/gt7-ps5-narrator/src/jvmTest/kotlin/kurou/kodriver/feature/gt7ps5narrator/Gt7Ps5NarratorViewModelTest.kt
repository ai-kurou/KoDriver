@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.gt7ps5narrator

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelLapsPreferencesRepository
import kurou.kodriver.domain.repository.Gt7Ps5Repository
import kurou.kodriver.domain.repository.MyBestLapPreferencesRepository
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelLapsUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5UseCase
import kurou.kodriver.domain.usecase.ObserveMyBestLapVoiceTypeUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("TooManyFunctions")
class Gt7Ps5NarratorViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(
        telemetryChannel: Channel<Gt7Ps5TelemetryData> = Channel(Channel.UNLIMITED),
        ttsEngine: TextToSpeechEngine,
        enabledOverrides: Map<ReadoutItemKey, Boolean> = emptyMap(),
        orderOverride: List<ReadoutItemKey> = listOf(ReadoutItemKey.MyBestLap),
        voiceType: MyBestLapVoiceType = MyBestLapVoiceType.FORMAL,
        simulator: Simulator? = Simulator.Gt7Ps5,
        fuelThreshold: Int = 3,
    ): Gt7Ps5NarratorViewModel {
        val readoutRepo = FakeReadoutPreferencesRepo(enabledOverrides, orderOverride)
        return Gt7Ps5NarratorViewModel(
            myBestLapUseCases = MyBestLapUseCases(
                observeGt7Ps5 = ObserveGt7Ps5UseCase(
                    FakeChannelGt7Ps5Repository(telemetryChannel.receiveAsFlow()),
                ),
                observeMyBestLapVoiceType = ObserveMyBestLapVoiceTypeUseCase(
                    FakeMyBestLapPreferencesRepo(voiceType),
                ),
            ),
            readoutListUseCases = ReadoutListUseCases(
                observeSelectedSimulator = ObserveSelectedSimulatorUseCase(
                    FakeSimulatorPreferencesRepo(simulator),
                ),
                observeReadoutEnabledStates = ObserveReadoutEnabledStatesUseCase(readoutRepo),
                observeReadoutOrder = ObserveReadoutOrderUseCase(readoutRepo),
            ),
            remainingFuelLapsUseCases = RemainingFuelLapsUseCases(
                observeRemainingFuelLapsThreshold = ObserveGt7Ps5RemainingFuelLapsUseCase(
                    FakeRemainingFuelLapsPreferencesRepo(fuelThreshold),
                ),
            ),
            ttsEngine = ttsEngine,
        )
    }

    // --- シミュレータ選択 ---

    @Test
    fun `GT7非選択時はベストラップアナウンスをしない`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(telemetryChannel = channel, ttsEngine = tts, simulator = null)

        channel.send(gt7Telemetry(bestLapTimeMs = 60_000))
        channel.send(gt7Telemetry(bestLapTimeMs = 59_000))

        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }

    @Test
    fun `simulator が null のときはベストラップアナウンスをしない`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(telemetryChannel = channel, ttsEngine = tts, simulator = null)

        channel.send(gt7Telemetry(bestLapTimeMs = 60_000))
        channel.send(gt7Telemetry(bestLapTimeMs = 59_000))

        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }

    // --- 初期状態 ---

    @Test
    fun `起動直後の最初のemitではベストラップが設定済みでもアナウンスしない`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(telemetryChannel = channel, ttsEngine = tts)

        channel.send(gt7Telemetry(bestLapTimeMs = 60_000))

        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }

    // --- ベストラップ更新 ---

    @Test
    fun `ベストラップが更新されるとFormalボイスでアナウンスする`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(telemetryChannel = channel, ttsEngine = tts, voiceType = MyBestLapVoiceType.FORMAL)

        channel.send(gt7Telemetry(bestLapTimeMs = 60_000))
        channel.send(gt7Telemetry(bestLapTimeMs = 59_000))

        assertEquals(listOf<SpeechEvent>(SpeechEvent.MyBestLapFormal), tts.spokenTexts)
    }

    @Test
    fun `ベストラップが更新されるとCasualボイスでアナウンスする`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(telemetryChannel = channel, ttsEngine = tts, voiceType = MyBestLapVoiceType.CASUAL)

        channel.send(gt7Telemetry(bestLapTimeMs = 60_000))
        channel.send(gt7Telemetry(bestLapTimeMs = 59_000))

        assertEquals(listOf<SpeechEvent>(SpeechEvent.MyBestLapCasual), tts.spokenTexts)
    }

    @Test
    fun `ベストラップ未計測から初計測でもアナウンスする`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(telemetryChannel = channel, ttsEngine = tts)

        channel.send(gt7Telemetry(bestLapTimeMs = -1))
        channel.send(gt7Telemetry(bestLapTimeMs = 60_000))

        assertEquals(listOf<SpeechEvent>(SpeechEvent.MyBestLapFormal), tts.spokenTexts)
    }

    @Test
    fun `同じベストラップ値が続いてもアナウンスしない`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(telemetryChannel = channel, ttsEngine = tts)

        channel.send(gt7Telemetry(bestLapTimeMs = 60_000))
        channel.send(gt7Telemetry(bestLapTimeMs = 60_000))

        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }

    @Test
    fun `ベストラップより遅いラップではアナウンスしない`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(telemetryChannel = channel, ttsEngine = tts)

        channel.send(gt7Telemetry(bestLapTimeMs = 60_000))
        channel.send(gt7Telemetry(bestLapTimeMs = 61_000))

        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }

    @Test
    fun `ベストラップが無効のときはアナウンスしない`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
            telemetryChannel = channel,
            ttsEngine = tts,
            enabledOverrides = mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.MyBestLap to false),
        )

        channel.send(gt7Telemetry(bestLapTimeMs = 60_000))
        channel.send(gt7Telemetry(bestLapTimeMs = 59_000))

        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }

    @Test
    fun `ベストラップが正値から-1にリセットされてもアナウンスしない`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(telemetryChannel = channel, ttsEngine = tts)

        channel.send(gt7Telemetry(bestLapTimeMs = 60_000))
        channel.send(gt7Telemetry(bestLapTimeMs = -1))

        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }

    @Test
    fun `ベストラップを複数回更新すると都度アナウンスする`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(telemetryChannel = channel, ttsEngine = tts)

        channel.send(gt7Telemetry(bestLapTimeMs = 60_000))
        channel.send(gt7Telemetry(bestLapTimeMs = 59_000))
        channel.send(gt7Telemetry(bestLapTimeMs = 58_000))

        assertEquals(
            listOf<SpeechEvent>(SpeechEvent.MyBestLapFormal, SpeechEvent.MyBestLapFormal),
            tts.spokenTexts,
        )
    }

    @Test
    fun `セッションリセット後に同じベストラップが来てもアナウンスしない`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(telemetryChannel = channel, ttsEngine = tts)

        channel.send(gt7Telemetry(bestLapTimeMs = -1))
        channel.send(gt7Telemetry(bestLapTimeMs = 60_000))
        channel.send(gt7Telemetry(bestLapTimeMs = -1))
        channel.send(gt7Telemetry(bestLapTimeMs = 60_000))

        assertEquals(listOf<SpeechEvent>(SpeechEvent.MyBestLapFormal), tts.spokenTexts)
    }

    @Test
    fun `セッションリセット後により良いタイムが来たらアナウンスする`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(telemetryChannel = channel, ttsEngine = tts)

        channel.send(gt7Telemetry(bestLapTimeMs = -1))
        channel.send(gt7Telemetry(bestLapTimeMs = 60_000))
        channel.send(gt7Telemetry(bestLapTimeMs = -1))
        channel.send(gt7Telemetry(bestLapTimeMs = 59_000))

        assertEquals(
            listOf<SpeechEvent>(SpeechEvent.MyBestLapFormal, SpeechEvent.MyBestLapFormal),
            tts.spokenTexts,
        )
    }

    // --- 燃料残り周回数 ---

    @Test
    fun `GT7非選択時は燃料アナウンスをしない`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
            telemetryChannel = channel,
            ttsEngine = tts,
            simulator = null,
            enabledOverrides = mapOf(ReadoutItemKey.RemainingFuelLaps to true),
            fuelThreshold = 2,
        )

        channel.send(gt7Telemetry(lapCount = 0, gasLevel = 60f, gasCapacity = 100f))
        channel.send(gt7Telemetry(lapCount = 1, gasLevel = 50f, gasCapacity = 100f))
        channel.send(gt7Telemetry(lapCount = 2, gasLevel = 40f, gasCapacity = 100f))

        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }

    @Test
    fun `燃料残り周回数が閾値以下になるとアナウンスする`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
            telemetryChannel = channel,
            ttsEngine = tts,
            enabledOverrides = mapOf(ReadoutItemKey.RemainingFuelLaps to true),
            fuelThreshold = 3,
        )

        // レーススタート: lapCount=0 → gasLevel=60L
        channel.send(gt7Telemetry(lapCount = 0, gasLevel = 60f, gasCapacity = 100f))
        // 1周完了: lapCount=1 → gasLevel=50L（10L消費）
        channel.send(gt7Telemetry(lapCount = 1, gasLevel = 50f, gasCapacity = 100f))
        // 2周完了: lapCount=2 → gasLevel=40L（平均10L/周、残り4周 → 閾値3以下なのでアナウンス待ち）
        channel.send(gt7Telemetry(lapCount = 2, gasLevel = 40f, gasCapacity = 100f))
        // 3周完了: lapCount=3 → gasLevel=30L（残り3周 → 閾値以下でアナウンス）
        channel.send(gt7Telemetry(lapCount = 3, gasLevel = 30f, gasCapacity = 100f))

        assertEquals(listOf<SpeechEvent>(SpeechEvent.RemainingFuelLapsWarning), tts.spokenTexts)
    }

    @Test
    fun `燃料残り周回数が閾値より多い場合はアナウンスしない`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
            telemetryChannel = channel,
            ttsEngine = tts,
            enabledOverrides = mapOf(ReadoutItemKey.RemainingFuelLaps to true),
            fuelThreshold = 2,
        )

        // 1周あたり10L消費、60Lスタートなら残り6周 → 閾値2を超えるのでアナウンスしない
        channel.send(gt7Telemetry(lapCount = 0, gasLevel = 60f, gasCapacity = 100f))
        channel.send(gt7Telemetry(lapCount = 1, gasLevel = 50f, gasCapacity = 100f))

        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }

    @Test
    fun `燃料残り周回数アナウンスが無効のときはアナウンスしない`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
            telemetryChannel = channel,
            ttsEngine = tts,
            enabledOverrides = mapOf(ReadoutItemKey.RemainingFuelLaps to false),
            fuelThreshold = 3,
        )

        channel.send(gt7Telemetry(lapCount = 0, gasLevel = 30f, gasCapacity = 100f))
        channel.send(gt7Telemetry(lapCount = 1, gasLevel = 20f, gasCapacity = 100f))
        // 残り2周（閾値3以下）だが無効
        channel.send(gt7Telemetry(lapCount = 2, gasLevel = 10f, gasCapacity = 100f))

        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }

    @Test
    fun `lapCountがリセットされると燃料追跡がリセットされる`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
            telemetryChannel = channel,
            ttsEngine = tts,
            enabledOverrides = mapOf(ReadoutItemKey.RemainingFuelLaps to true),
            fuelThreshold = 3,
        )

        // 1周目セッション
        channel.send(gt7Telemetry(lapCount = 0, gasLevel = 60f, gasCapacity = 100f))
        channel.send(gt7Telemetry(lapCount = 3, gasLevel = 30f, gasCapacity = 100f))
        // リセット後: lapCountが減少してリセット
        channel.send(gt7Telemetry(lapCount = 0, gasLevel = 60f, gasCapacity = 100f))
        channel.send(gt7Telemetry(lapCount = 1, gasLevel = 50f, gasCapacity = 100f))

        // リセット後の1周完了では残り5周(>閾値3)なのでアナウンスしない
        assertEquals(listOf<SpeechEvent>(SpeechEvent.RemainingFuelLapsWarning), tts.spokenTexts)
    }

    @Test
    fun `燃料消費が0以下のときはアナウンスしない`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
            telemetryChannel = channel,
            ttsEngine = tts,
            enabledOverrides = mapOf(ReadoutItemKey.RemainingFuelLaps to true),
            fuelThreshold = 3,
        )

        // 燃料が増えている（補給等）
        channel.send(gt7Telemetry(lapCount = 0, gasLevel = 30f, gasCapacity = 100f))
        channel.send(gt7Telemetry(lapCount = 1, gasLevel = 50f, gasCapacity = 100f))

        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }

    // --- 優先度 ---

    @Test
    fun `優先度の高いアイテム読み上げ中にベストラップが来ても読み上げない`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val tts = PriorityAwareTextToSpeechEngine(initialKey = ReadoutItemKey.Flag)
        buildViewModel(
            telemetryChannel = channel,
            ttsEngine = tts,
            orderOverride = listOf(ReadoutItemKey.Flag, ReadoutItemKey.MyBestLap),
        )

        channel.send(gt7Telemetry(bestLapTimeMs = 60_000))
        channel.send(gt7Telemetry(bestLapTimeMs = 59_000))

        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }

    @Test
    fun `優先度の低いアイテム読み上げ中にベストラップが来ると割り込む`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val tts = PriorityAwareTextToSpeechEngine(initialKey = ReadoutItemKey.Flag)
        buildViewModel(
            telemetryChannel = channel,
            ttsEngine = tts,
            orderOverride = listOf(ReadoutItemKey.MyBestLap, ReadoutItemKey.Flag),
        )

        channel.send(gt7Telemetry(bestLapTimeMs = 60_000))
        channel.send(gt7Telemetry(bestLapTimeMs = 59_000))

        assertEquals(true, tts.stopCalled)
        assertEquals(listOf<SpeechEvent>(SpeechEvent.MyBestLapFormal), tts.spokenTexts)
    }
}

private fun gt7Telemetry(bestLapTimeMs: Int) = Gt7Ps5TelemetryData(
    lapCount = 0,
    lapsInRace = 0,
    bestLapTimeMs = bestLapTimeMs,
    gasLevel = 0f,
    gasCapacity = 100f,
)

private fun gt7Telemetry(lapCount: Int, gasLevel: Float, gasCapacity: Float) = Gt7Ps5TelemetryData(
    lapCount = lapCount,
    lapsInRace = 5,
    bestLapTimeMs = -1,
    gasLevel = gasLevel,
    gasCapacity = gasCapacity,
)

private class RecordingTextToSpeechEngine : TextToSpeechEngine {
    val spokenTexts = mutableListOf<SpeechEvent>()
    override val currentReadoutItemKey: ReadoutItemKey? = null
    override fun speak(event: SpeechEvent, queue: Boolean) { spokenTexts.add(event) }
    override fun stop() = Unit
    override fun previewStartSound(type: ReadoutStartSoundType) = Unit
}

private class PriorityAwareTextToSpeechEngine(
    initialKey: ReadoutItemKey? = null,
) : TextToSpeechEngine {
    val spokenTexts = mutableListOf<SpeechEvent>()
    var stopCalled = false
    override var currentReadoutItemKey: ReadoutItemKey? = initialKey
    override fun speak(event: SpeechEvent, queue: Boolean) { spokenTexts.add(event) }
    override fun stop() {
        stopCalled = true
        currentReadoutItemKey = null
    }
    override fun previewStartSound(type: ReadoutStartSoundType) = Unit
}

private class FakeChannelGt7Ps5Repository(
    private val stream: Flow<Gt7Ps5TelemetryData>,
) : Gt7Ps5Repository {
    override fun telemetryStream(): Flow<Gt7Ps5TelemetryData> = stream
    override suspend fun isConnected(): Boolean = true
}

private class FakeSimulatorPreferencesRepo(
    private val simulator: Simulator?,
) : SimulatorPreferencesRepository {
    override fun selectedSimulator(): Flow<Simulator?> = MutableStateFlow(simulator)
    override suspend fun saveSelectedSimulator(simulator: Simulator) = Unit
}

private class FakeReadoutPreferencesRepo(
    private val enabledOverrides: Map<ReadoutItemKey, Boolean> = emptyMap(),
    private val orderOverride: List<ReadoutItemKey> = listOf(ReadoutItemKey.MyBestLap),
) : ReadoutPreferencesRepository {
    override fun observeReadoutEnabledStates(simulator: String): Flow<Map<ReadoutItemKey, Boolean>> =
        MutableStateFlow(enabledOverrides)

    override suspend fun saveReadoutEnabledState(simulator: String, key: ReadoutItemKey, enabled: Boolean) = Unit
    override fun observeReadoutOrder(simulator: String): Flow<List<ReadoutItemKey>> = MutableStateFlow(orderOverride)
    override suspend fun saveReadoutOrder(simulator: String, order: List<ReadoutItemKey>) = Unit
}

private class FakeMyBestLapPreferencesRepo(
    initialType: MyBestLapVoiceType = MyBestLapVoiceType.FORMAL,
) : MyBestLapPreferencesRepository {
    private val type = MutableStateFlow(initialType)
    override fun observeVoiceType(): Flow<MyBestLapVoiceType> = type
    override suspend fun saveVoiceType(type: MyBestLapVoiceType) {
        this.type.update { type }
    }
}

private class FakeRemainingFuelLapsPreferencesRepo(
    private val threshold: Int = 3,
) : Gt7Ps5RemainingFuelLapsPreferencesRepository {
    override fun observeRemainingFuelLaps(): Flow<Int> = MutableStateFlow(threshold)
    override suspend fun saveRemainingFuelLaps(laps: Int) = Unit
}
