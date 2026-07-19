@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.gt7ps5narrator

import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.TelemetryLog
import kurou.kodriver.domain.repository.Gt7Ps5MyBestLapPreferencesRepository
import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelLapsPreferencesRepository
import kurou.kodriver.domain.repository.Gt7Ps5Repository
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.repository.TelemetryLogRepository
import kurou.kodriver.domain.usecase.ObserveGt7Ps5MyBestLapVoiceTypeUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelLapsUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5UseCase
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

    @MockK
    private lateinit var telemetryRepository: Gt7Ps5Repository

    @MockK
    private lateinit var myBestLapPreferencesRepository: Gt7Ps5MyBestLapPreferencesRepository

    @MockK
    private lateinit var remainingFuelLapsPreferencesRepository: Gt7Ps5RemainingFuelLapsPreferencesRepository

    @MockK
    private lateinit var simulatorPreferencesRepository: SimulatorPreferencesRepository

    @MockK
    private lateinit var readoutPreferencesRepository: ReadoutPreferencesRepository

    @MockK
    private lateinit var telemetryLogRepository: TelemetryLogRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        telemetryChannel: Channel<Gt7Ps5TelemetryData>,
        ttsEngine: TextToSpeechEngine,
        currentTimeMs: () -> Long = { 0L },
    ): Gt7Ps5NarratorViewModel {
        every { telemetryRepository.telemetryStream() } returns telemetryChannel.receiveAsFlow()
        return Gt7Ps5NarratorViewModel(
            myBestLapUseCases = MyBestLapUseCases(
                observeGt7Ps5 = ObserveGt7Ps5UseCase(telemetryRepository),
                observeMyBestLapVoiceType = ObserveGt7Ps5MyBestLapVoiceTypeUseCase(myBestLapPreferencesRepository),
            ),
            readoutListUseCases = ReadoutListUseCases(
                observeSelectedSimulator = ObserveSelectedSimulatorUseCase(simulatorPreferencesRepository),
                observeReadoutEnabledStates = ObserveReadoutEnabledStatesUseCase(readoutPreferencesRepository),
                observeReadoutOrder = ObserveReadoutOrderUseCase(readoutPreferencesRepository),
            ),
            remainingFuelLapsUseCases = RemainingFuelLapsUseCases(
                observeRemainingFuelLapsThreshold =
                    ObserveGt7Ps5RemainingFuelLapsUseCase(remainingFuelLapsPreferencesRepository),
            ),
            eventProcessor = Gt7Ps5NarratorEventProcessor(
                ttsEngine = ttsEngine,
                saveTelemetryLog = SaveTelemetryLogUseCase(telemetryLogRepository),
            ),
            currentTimeMs = currentTimeMs,
        )
    }

    @Test
    fun `GT7非選択時は読み上げない`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val spokenTexts = mutableListOf<SpeechEvent>()
        val ttsEngine = mockTts(spokenTexts)
        every { simulatorPreferencesRepository.selectedSimulator() } returns MutableStateFlow(null)
        every { readoutPreferencesRepository.observeReadoutEnabledStates(any()) } returns MutableStateFlow(emptyMap())
        every { readoutPreferencesRepository.observeReadoutOrder(any()) } returns MutableStateFlow(emptyList())
        every { myBestLapPreferencesRepository.observeVoiceType() } returns MutableStateFlow(MyBestLapVoiceType.FORMAL)
        every { remainingFuelLapsPreferencesRepository.observeRemainingFuelLaps() } returns MutableStateFlow(3)
        createViewModel(telemetryChannel = channel, ttsEngine = ttsEngine)

        channel.send(gt7Telemetry(bestLapTimeMs = 60_000))
        channel.send(gt7Telemetry(bestLapTimeMs = 59_000))
        channel.send(gt7Telemetry(lapCount = 1, gasLevel = 50f, gasCapacity = 100f))

        assertEquals(emptyList<SpeechEvent>(), spokenTexts)
        verify(atLeast = 1) { simulatorPreferencesRepository.selectedSimulator() }
    }

    @Test
    fun `起動直後の最初のemitではベストラップが設定済みでもアナウンスしない`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val spokenTexts = mutableListOf<SpeechEvent>()
        val ttsEngine = mockTts(spokenTexts)
        stubReadoutDefaults()
        createViewModel(telemetryChannel = channel, ttsEngine = ttsEngine)

        channel.send(gt7Telemetry(bestLapTimeMs = 60_000))

        assertEquals(emptyList<SpeechEvent>(), spokenTexts)
    }

    @Test
    fun `自己ベストラップの声種別設定を反映して読み上げる`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val spokenTexts = mutableListOf<SpeechEvent>()
        val ttsEngine = mockTts(spokenTexts)
        stubReadoutDefaults(voiceType = MyBestLapVoiceType.CASUAL)
        createViewModel(telemetryChannel = channel, ttsEngine = ttsEngine)

        channel.send(gt7Telemetry(bestLapTimeMs = 60_000))
        channel.send(gt7Telemetry(bestLapTimeMs = 59_000))

        assertEquals(listOf<SpeechEvent>(SpeechEvent.Gt7Ps5MyBestLapCasual), spokenTexts)
    }

    @Test
    fun `読み上げが発生したら現在と直前のテレメトリを保存する`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val spokenTexts = mutableListOf<SpeechEvent>()
        val logs = mutableListOf<TelemetryLog>()
        val ttsEngine = mockTts(spokenTexts)
        stubReadoutDefaults()
        coEvery { telemetryLogRepository.saveTelemetryLog(any(), any(), any(), any()) } answers {
            logs.add(
                TelemetryLog(
                    id = 0,
                    createdAt = firstArg(),
                    simulatorId = secondArg(),
                    readoutItemKey = thirdArg(),
                    telemetryJson = arg(3),
                ),
            )
        }
        createViewModel(telemetryChannel = channel, ttsEngine = ttsEngine, currentTimeMs = { 123_456L })

        channel.send(gt7Telemetry(bestLapTimeMs = 60_000))
        channel.send(gt7Telemetry(bestLapTimeMs = 59_000))

        assertEquals(1, logs.size)
        assertEquals(123_456L, logs.single().createdAt)
        assertEquals(Simulator.Gt7Ps5.id, logs.single().simulatorId)
        assertEquals(ReadoutItemKey.Gt7Ps5.MyBestLap.Root.value, logs.single().readoutItemKey)
        assertEquals(true, logs.single().telemetryJson.startsWith("{\"state\":{\"raw\":"))
        assertEquals(
            true,
            logs.single().telemetryJson.contains(
                """"previousTelemetry":{"lapCount":0,"lapsInRace":0,"bestLapTimeMs":60000,""" +
                    """"gasLevel":0.0,"gasCapacity":100.0}""",
            ),
        )
        assertEquals(
            true,
            logs.single().telemetryJson.contains(
                """"telemetry":{"lapCount":0,"lapsInRace":0,"bestLapTimeMs":59000,""" +
                    """"gasLevel":0.0,"gasCapacity":100.0}""",
            ),
        )
        assertEquals(true, logs.single().telemetryJson.contains(""""settings":{"raw":"""))
        assertEquals(true, logs.single().telemetryJson.contains(""""observedAtMs":123456"""))
        assertEquals(true, logs.single().telemetryJson.contains(""""finalState":{"raw":"""))
    }

    @Test
    fun `自己ベストラップが無効のときは読み上げない`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val spokenTexts = mutableListOf<SpeechEvent>()
        val ttsEngine = mockTts(spokenTexts)
        stubReadoutDefaults(enabledOverrides = mapOf(ReadoutItemKey.Gt7Ps5.MyBestLap.Root to false))
        createViewModel(telemetryChannel = channel, ttsEngine = ttsEngine)

        channel.send(gt7Telemetry(bestLapTimeMs = 60_000))
        channel.send(gt7Telemetry(bestLapTimeMs = 59_000))

        assertEquals(emptyList<SpeechEvent>(), spokenTexts)
    }

    @Test
    fun `燃料残り周回数の閾値設定を反映して読み上げる`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val spokenTexts = mutableListOf<SpeechEvent>()
        val ttsEngine = mockTts(spokenTexts)
        stubReadoutDefaults(fuelThreshold = 3)
        createViewModel(telemetryChannel = channel, ttsEngine = ttsEngine)

        channel.send(gt7Telemetry(lapCount = 0, gasLevel = 40f, gasCapacity = 100f))
        channel.send(gt7Telemetry(lapCount = 1, gasLevel = 30f, gasCapacity = 100f))
        channel.send(gt7Telemetry(lapCount = 2, gasLevel = 20f, gasCapacity = 100f))

        assertEquals(listOf<SpeechEvent>(SpeechEvent.RemainingFuelLapsWarning(2)), spokenTexts)
    }

    @Test
    fun `燃料残り周回数が無効のときは読み上げない`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val spokenTexts = mutableListOf<SpeechEvent>()
        val ttsEngine = mockTts(spokenTexts)
        stubReadoutDefaults(
            enabledOverrides = mapOf(ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root to false),
            fuelThreshold = 3,
        )
        createViewModel(telemetryChannel = channel, ttsEngine = ttsEngine)

        channel.send(gt7Telemetry(lapCount = 0, gasLevel = 30f, gasCapacity = 100f))
        channel.send(gt7Telemetry(lapCount = 1, gasLevel = 20f, gasCapacity = 100f))
        channel.send(gt7Telemetry(lapCount = 2, gasLevel = 10f, gasCapacity = 100f))

        assertEquals(emptyList<SpeechEvent>(), spokenTexts)
    }

    @Test
    fun `優先度の高いアイテム読み上げ中にベストラップが来ても読み上げない`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val spokenTexts = mutableListOf<SpeechEvent>()
        val ttsEngine = mockPriorityAwareTts(
            spokenTexts = spokenTexts,
            initialKey = ReadoutItemKey.LmuWindows.Flag.Root,
        )
        stubReadoutDefaults(
            orderOverride = listOf(ReadoutItemKey.LmuWindows.Flag.Root, ReadoutItemKey.Gt7Ps5.MyBestLap.Root),
        )
        createViewModel(telemetryChannel = channel, ttsEngine = ttsEngine)

        channel.send(gt7Telemetry(bestLapTimeMs = 60_000))
        channel.send(gt7Telemetry(bestLapTimeMs = 59_000))

        assertEquals(false, ttsEngine.stopCalled)
        assertEquals(emptyList<SpeechEvent>(), spokenTexts)
        verify(exactly = 0) { ttsEngine.stop() }
    }

    @Test
    fun `優先度制御で読み上げなかったイベントは保存しない`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val spokenTexts = mutableListOf<SpeechEvent>()
        val logs = mutableListOf<TelemetryLog>()
        val ttsEngine = mockPriorityAwareTts(
            spokenTexts = spokenTexts,
            initialKey = ReadoutItemKey.LmuWindows.Flag.Root,
        )
        stubReadoutDefaults(
            orderOverride = listOf(ReadoutItemKey.LmuWindows.Flag.Root, ReadoutItemKey.Gt7Ps5.MyBestLap.Root),
        )
        coEvery { telemetryLogRepository.saveTelemetryLog(any(), any(), any(), any()) } answers {
            logs.add(
                TelemetryLog(
                    id = 0,
                    createdAt = firstArg(),
                    simulatorId = secondArg(),
                    readoutItemKey = thirdArg(),
                    telemetryJson = arg(3),
                ),
            )
        }
        createViewModel(telemetryChannel = channel, ttsEngine = ttsEngine)

        channel.send(gt7Telemetry(bestLapTimeMs = 60_000))
        channel.send(gt7Telemetry(bestLapTimeMs = 59_000))

        assertEquals(emptyList<TelemetryLog>(), logs)
    }

    @Test
    fun `優先度の低いアイテム読み上げ中にベストラップが来ると割り込む`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val spokenTexts = mutableListOf<SpeechEvent>()
        val ttsEngine = mockPriorityAwareTts(
            spokenTexts = spokenTexts,
            initialKey = ReadoutItemKey.LmuWindows.Flag.Root,
        )
        stubReadoutDefaults(
            orderOverride = listOf(ReadoutItemKey.Gt7Ps5.MyBestLap.Root, ReadoutItemKey.LmuWindows.Flag.Root),
        )
        createViewModel(telemetryChannel = channel, ttsEngine = ttsEngine)

        channel.send(gt7Telemetry(bestLapTimeMs = 60_000))
        channel.send(gt7Telemetry(bestLapTimeMs = 59_000))

        assertEquals(true, ttsEngine.stopCalled)
        assertEquals(listOf<SpeechEvent>(SpeechEvent.Gt7Ps5MyBestLapFormal), spokenTexts)
    }

    @Test
    fun `再生中の項目が優先度リストにないときは新しい読み上げで割り込む`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val spokenTexts = mutableListOf<SpeechEvent>()
        val ttsEngine = mockPriorityAwareTts(
            spokenTexts = spokenTexts,
            initialKey = ReadoutItemKey.LmuWindows.Flag.Root,
        )
        stubReadoutDefaults(orderOverride = listOf(ReadoutItemKey.Gt7Ps5.MyBestLap.Root))
        createViewModel(telemetryChannel = channel, ttsEngine = ttsEngine)

        channel.send(gt7Telemetry(bestLapTimeMs = 60_000))
        channel.send(gt7Telemetry(bestLapTimeMs = 59_000))

        assertEquals(true, ttsEngine.stopCalled)
        assertEquals(listOf<SpeechEvent>(SpeechEvent.Gt7Ps5MyBestLapFormal), spokenTexts)
    }

    @Test
    fun `新しい項目が優先度リストにないときは再生中の読み上げを優先する`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val spokenTexts = mutableListOf<SpeechEvent>()
        val ttsEngine = mockPriorityAwareTts(
            spokenTexts = spokenTexts,
            initialKey = ReadoutItemKey.LmuWindows.Flag.Root,
        )
        stubReadoutDefaults(orderOverride = listOf(ReadoutItemKey.LmuWindows.Flag.Root))
        createViewModel(telemetryChannel = channel, ttsEngine = ttsEngine)

        channel.send(gt7Telemetry(bestLapTimeMs = 60_000))
        channel.send(gt7Telemetry(bestLapTimeMs = 59_000))

        assertEquals(false, ttsEngine.stopCalled)
        assertEquals(emptyList<SpeechEvent>(), spokenTexts)
    }

    /**
     * simulator/enabledStates/readoutOrder/voiceType/fuelThresholdの標準スタブをまとめて設定する。
     * ViewModelがコンストラクタ内で即座にFlowを購読・combineするため、必ず [createViewModel] の前に呼ぶこと。
     */
    private fun stubReadoutDefaults(
        simulator: Simulator? = Simulator.Gt7Ps5,
        enabledOverrides: Map<ReadoutItemKey, Boolean> = emptyMap(),
        orderOverride: List<ReadoutItemKey> = listOf(ReadoutItemKey.Gt7Ps5.MyBestLap.Root),
        voiceType: MyBestLapVoiceType = MyBestLapVoiceType.FORMAL,
        fuelThreshold: Int = 3,
    ) {
        every { simulatorPreferencesRepository.selectedSimulator() } returns MutableStateFlow(simulator)
        every {
            readoutPreferencesRepository.observeReadoutEnabledStates(any())
        } returns MutableStateFlow(enabledOverrides)
        every { readoutPreferencesRepository.observeReadoutOrder(any()) } returns MutableStateFlow(orderOverride)
        every { myBestLapPreferencesRepository.observeVoiceType() } returns MutableStateFlow(voiceType)
        every {
            remainingFuelLapsPreferencesRepository.observeRemainingFuelLaps()
        } returns MutableStateFlow(fuelThreshold)
        coEvery { telemetryLogRepository.saveTelemetryLog(any(), any(), any(), any()) } just Runs
    }

    private fun mockTts(spokenTexts: MutableList<SpeechEvent>): TextToSpeechEngine {
        val ttsEngine: TextToSpeechEngine = mockk()
        every { ttsEngine.currentReadoutItemKey } returns null
        every { ttsEngine.speak(any(), any()) } answers { spokenTexts.add(firstArg()) }
        every { ttsEngine.stop() } just Runs
        return ttsEngine
    }

    private fun mockPriorityAwareTts(
        spokenTexts: MutableList<SpeechEvent>,
        initialKey: ReadoutItemKey?,
    ): PriorityAwareTts {
        val ttsEngine: PriorityAwareTts = mockk()
        var currentKey = initialKey
        var stopCalled = false
        every { ttsEngine.currentReadoutItemKey } answers { currentKey }
        every { ttsEngine.speak(any(), any()) } answers { spokenTexts.add(firstArg()) }
        every { ttsEngine.stop() } answers {
            stopCalled = true
            currentKey = null
        }
        every { ttsEngine.stopCalled } answers { stopCalled }
        return ttsEngine
    }

    private interface PriorityAwareTts : TextToSpeechEngine {
        val stopCalled: Boolean
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
