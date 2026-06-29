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
import kurou.kodriver.domain.model.TelemetryLog
import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelLapsEnabledRepository
import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelLapsPreferencesRepository
import kurou.kodriver.domain.repository.Gt7Ps5Repository
import kurou.kodriver.domain.repository.MyBestLapPreferencesRepository
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.repository.TelemetryLogRepository
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelLapsEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelLapsUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5UseCase
import kurou.kodriver.domain.usecase.ObserveMyBestLapVoiceTypeUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.SaveTelemetryLogUseCase
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
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

    private data class ReadoutSettings(
        val enabledOverrides: Map<ReadoutItemKey, Boolean> = emptyMap(),
        val remainingFuelLapsEnabled: Boolean = true,
        val fuelThreshold: Int = 3,
    )

    private data class TelemetryLogSettings(
        val currentTimeMs: () -> Long = { 0L },
        val repository: FakeTelemetryLogRepository = FakeTelemetryLogRepository(),
    )

    private fun buildViewModel(
        telemetryChannel: Channel<Gt7Ps5TelemetryData> = Channel(Channel.UNLIMITED),
        ttsEngine: TextToSpeechEngine,
        readoutSettings: ReadoutSettings = ReadoutSettings(),
        orderOverride: List<ReadoutItemKey> = listOf(ReadoutItemKey.MyBestLap),
        voiceType: MyBestLapVoiceType = MyBestLapVoiceType.FORMAL,
        simulator: Simulator? = Simulator.Gt7Ps5,
        telemetryLogSettings: TelemetryLogSettings = TelemetryLogSettings(),
    ): Gt7Ps5NarratorViewModel {
        val readoutRepo = FakeReadoutPreferencesRepo(readoutSettings.enabledOverrides, orderOverride)
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
                    FakeRemainingFuelLapsPreferencesRepo(readoutSettings.fuelThreshold),
                ),
                observeRemainingFuelLapsEnabled = ObserveGt7Ps5RemainingFuelLapsEnabledUseCase(
                    FakeGt7Ps5RemainingFuelLapsEnabledRepo(readoutSettings.remainingFuelLapsEnabled),
                ),
            ),
            ttsEngine = ttsEngine,
            saveTelemetryLog = SaveTelemetryLogUseCase(telemetryLogSettings.repository),
            currentTimeMs = telemetryLogSettings.currentTimeMs,
        )
    }

    @Test
    fun `GT7非選択時は読み上げない`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(telemetryChannel = channel, ttsEngine = tts, simulator = null)

        channel.send(gt7Telemetry(bestLapTimeMs = 60_000))
        channel.send(gt7Telemetry(bestLapTimeMs = 59_000))
        channel.send(gt7Telemetry(lapCount = 1, gasLevel = 50f, gasCapacity = 100f))

        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }

    @Test
    fun `起動直後の最初のemitではベストラップが設定済みでもアナウンスしない`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(telemetryChannel = channel, ttsEngine = tts)

        channel.send(gt7Telemetry(bestLapTimeMs = 60_000))

        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }

    @Test
    fun `自己ベストラップの声種別設定を反映して読み上げる`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(telemetryChannel = channel, ttsEngine = tts, voiceType = MyBestLapVoiceType.CASUAL)

        channel.send(gt7Telemetry(bestLapTimeMs = 60_000))
        channel.send(gt7Telemetry(bestLapTimeMs = 59_000))

        assertEquals(listOf<SpeechEvent>(SpeechEvent.MyBestLapCasual), tts.spokenTexts)
    }

    @Test
    fun `読み上げが発生したら現在と直前のテレメトリを保存する`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val telemetryLogRepository = FakeTelemetryLogRepository()
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
            telemetryChannel = channel,
            ttsEngine = tts,
            telemetryLogSettings = TelemetryLogSettings(
                currentTimeMs = { 123_456L },
                repository = telemetryLogRepository,
            ),
        )

        channel.send(gt7Telemetry(bestLapTimeMs = 60_000))
        channel.send(gt7Telemetry(bestLapTimeMs = 59_000))

        assertEquals(
            listOf(
                TelemetryLog(
                    createdAt = 123_456L,
                    simulatorId = Simulator.Gt7Ps5.id,
                    readoutItemKey = ReadoutItemKey.MyBestLap.value,
                    telemetryJson =
                        """{"previous":{"lapCount":0,"lapsInRace":0,"bestLapTimeMs":60000,""" +
                            """"gasLevel":0.0,"gasCapacity":100.0},"current":{"lapCount":0,""" +
                            """"lapsInRace":0,"bestLapTimeMs":59000,"gasLevel":0.0,"gasCapacity":100.0}}""",
                ),
            ),
            telemetryLogRepository.logs.value,
        )
    }

    @Test
    fun `自己ベストラップが無効のときは読み上げない`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
            telemetryChannel = channel,
            ttsEngine = tts,
            readoutSettings = ReadoutSettings(enabledOverrides = mapOf(ReadoutItemKey.MyBestLap to false)),
        )

        channel.send(gt7Telemetry(bestLapTimeMs = 60_000))
        channel.send(gt7Telemetry(bestLapTimeMs = 59_000))

        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }

    @Test
    fun `燃料残り周回数の閾値設定を反映して読み上げる`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
            telemetryChannel = channel,
            ttsEngine = tts,
            readoutSettings = ReadoutSettings(remainingFuelLapsEnabled = true, fuelThreshold = 3),
        )

        channel.send(gt7Telemetry(lapCount = 0, gasLevel = 40f, gasCapacity = 100f))
        channel.send(gt7Telemetry(lapCount = 1, gasLevel = 30f, gasCapacity = 100f))
        channel.send(gt7Telemetry(lapCount = 2, gasLevel = 20f, gasCapacity = 100f))

        assertEquals(listOf<SpeechEvent>(SpeechEvent.RemainingFuelLapsWarning(2)), tts.spokenTexts)
    }

    @Test
    fun `燃料残り周回数が無効のときは読み上げない`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
            telemetryChannel = channel,
            ttsEngine = tts,
            readoutSettings = ReadoutSettings(remainingFuelLapsEnabled = false, fuelThreshold = 3),
        )

        channel.send(gt7Telemetry(lapCount = 0, gasLevel = 30f, gasCapacity = 100f))
        channel.send(gt7Telemetry(lapCount = 1, gasLevel = 20f, gasCapacity = 100f))
        channel.send(gt7Telemetry(lapCount = 2, gasLevel = 10f, gasCapacity = 100f))

        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }

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

        assertEquals(false, tts.stopCalled)
        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }

    @Test
    fun `優先度制御で読み上げなかったイベントは保存しない`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val telemetryLogRepository = FakeTelemetryLogRepository()
        val tts = PriorityAwareTextToSpeechEngine(initialKey = ReadoutItemKey.Flag)
        buildViewModel(
            telemetryChannel = channel,
            ttsEngine = tts,
            orderOverride = listOf(ReadoutItemKey.Flag, ReadoutItemKey.MyBestLap),
            telemetryLogSettings = TelemetryLogSettings(repository = telemetryLogRepository),
        )

        channel.send(gt7Telemetry(bestLapTimeMs = 60_000))
        channel.send(gt7Telemetry(bestLapTimeMs = 59_000))

        assertEquals(emptyList<TelemetryLog>(), telemetryLogRepository.logs.value)
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

    @Test
    fun `再生中の項目が優先度リストにないときは新しい読み上げで割り込む`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val tts = PriorityAwareTextToSpeechEngine(initialKey = ReadoutItemKey.Flag)
        buildViewModel(
            telemetryChannel = channel,
            ttsEngine = tts,
            orderOverride = listOf(ReadoutItemKey.MyBestLap),
        )

        channel.send(gt7Telemetry(bestLapTimeMs = 60_000))
        channel.send(gt7Telemetry(bestLapTimeMs = 59_000))

        assertEquals(true, tts.stopCalled)
        assertEquals(listOf<SpeechEvent>(SpeechEvent.MyBestLapFormal), tts.spokenTexts)
    }

    @Test
    fun `新しい項目が優先度リストにないときは再生中の読み上げを優先する`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val tts = PriorityAwareTextToSpeechEngine(initialKey = ReadoutItemKey.Flag)
        buildViewModel(
            telemetryChannel = channel,
            ttsEngine = tts,
            orderOverride = listOf(ReadoutItemKey.Flag),
        )

        channel.send(gt7Telemetry(bestLapTimeMs = 60_000))
        channel.send(gt7Telemetry(bestLapTimeMs = 59_000))

        assertEquals(false, tts.stopCalled)
        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }
}

private fun gt7Telemetry(bestLapTimeMs: Int) = Gt7Ps5TelemetryData(
    lapCount = 0,
    lapsInRace = 0,
    bestLapTimeMs = bestLapTimeMs,
    gasLevel = 0f,
    gasCapacity = 100f,
)

private fun gt7Telemetry(
    lapCount: Int,
    gasLevel: Float,
    gasCapacity: Float,
    bestLapTimeMs: Int = 30_000,
) = Gt7Ps5TelemetryData(
    lapCount = lapCount,
    lapsInRace = 5,
    bestLapTimeMs = bestLapTimeMs,
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

private class FakeGt7Ps5RemainingFuelLapsEnabledRepo(
    private val enabled: Boolean = true,
) : Gt7Ps5RemainingFuelLapsEnabledRepository {
    override fun observeEnabled(): Flow<Boolean> = MutableStateFlow(enabled)
    override suspend fun saveEnabled(enabled: Boolean) = Unit
}

private class FakeTelemetryLogRepository : TelemetryLogRepository {
    val logs = MutableStateFlow(emptyList<TelemetryLog>())
    override fun observeTelemetryLogs(): Flow<List<TelemetryLog>> = logs
    override suspend fun saveTelemetryLog(log: TelemetryLog) {
        logs.update { it + log }
    }
}
