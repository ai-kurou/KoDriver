@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.acewindowsnarrator

import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
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
import kurou.kodriver.domain.model.AceWindowsFlagData
import kurou.kodriver.domain.model.AceWindowsFlagType
import kurou.kodriver.domain.model.AceWindowsFuelData
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.AceWindowsFlagPreferencesRepository
import kurou.kodriver.domain.repository.AceWindowsFlagRepository
import kurou.kodriver.domain.repository.AceWindowsFuelRepository
import kurou.kodriver.domain.repository.AceWindowsRemainingFuelPreferencesRepository
import kurou.kodriver.domain.repository.QueuePreferencesRepository
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.repository.TelemetryLogRepository
import kurou.kodriver.domain.usecase.ObserveAceWindowsFlagEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsFlagUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsFuelUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsRemainingFuelThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.ObserveQueueEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.SaveTelemetryLogUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class AceWindowsNarratorViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @MockK
    private lateinit var fuelRepository: AceWindowsFuelRepository

    @MockK
    private lateinit var remainingFuelPreferencesRepository: AceWindowsRemainingFuelPreferencesRepository

    @MockK
    private lateinit var simulatorPreferencesRepository: SimulatorPreferencesRepository

    @MockK
    private lateinit var readoutPreferencesRepository: ReadoutPreferencesRepository

    @MockK
    private lateinit var telemetryLogRepository: TelemetryLogRepository

    @MockK
    private lateinit var queuePreferencesRepository: QueuePreferencesRepository

    @MockK
    private lateinit var flagRepository: AceWindowsFlagRepository

    @MockK
    private lateinit var flagPreferencesRepository: AceWindowsFlagPreferencesRepository

    @MockK
    private lateinit var ttsEngine: TextToSpeechEngine

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        fuelChannel: Channel<AceWindowsFuelData>,
        ttsEngine: TextToSpeechEngine,
        flagChannel: Channel<AceWindowsFlagData> = Channel(Channel.UNLIMITED),
        currentTimeMs: () -> Long = { 0L },
    ): AceWindowsNarratorViewModel {
        every { fuelRepository.fuelStream() } returns fuelChannel.receiveAsFlow()
        every { flagRepository.flagStream() } returns flagChannel.receiveAsFlow()
        return AceWindowsNarratorViewModel(
            remainingFuelUseCases = RemainingFuelUseCases(
                observeAceWindowsFuel = ObserveAceWindowsFuelUseCase(fuelRepository),
                observeThresholdPercentage =
                    ObserveAceWindowsRemainingFuelThresholdPercentageUseCase(remainingFuelPreferencesRepository),
            ),
            readoutListUseCases = ReadoutListUseCases(
                observeSelectedSimulator = ObserveSelectedSimulatorUseCase(simulatorPreferencesRepository),
                observeReadoutEnabledStates = ObserveReadoutEnabledStatesUseCase(readoutPreferencesRepository),
                observeReadoutOrder = ObserveReadoutOrderUseCase(readoutPreferencesRepository),
                observeQueueEnabledStates = ObserveQueueEnabledStatesUseCase(queuePreferencesRepository),
            ),
            flagUseCases = FlagUseCases(
                observeAceWindowsFlag = ObserveAceWindowsFlagUseCase(flagRepository),
                observeFlagEnabledStates = ObserveAceWindowsFlagEnabledStatesUseCase(flagPreferencesRepository),
            ),
            eventProcessor = AceWindowsNarratorEventProcessor(
                ttsEngine = ttsEngine,
                saveTelemetryLog = SaveTelemetryLogUseCase(telemetryLogRepository),
            ),
            currentTimeMs = currentTimeMs,
        )
    }

    @Test
    fun `ACE非選択時は読み上げない`() = runTest(testDispatcher) {
        val channel = Channel<AceWindowsFuelData>(Channel.UNLIMITED)
        val spokenTexts = mutableListOf<SpeechEvent>()
        val ttsEngine = mockTts(spokenTexts)
        every { simulatorPreferencesRepository.selectedSimulator() } returns MutableStateFlow(null)
        every {
            readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.AceWindows.id)
        } returns MutableStateFlow(emptyMap())
        every {
            readoutPreferencesRepository.observeReadoutOrder(Simulator.AceWindows.id)
        } returns MutableStateFlow(emptyList())
        every {
            remainingFuelPreferencesRepository.observeThresholdPercentage()
        } returns MutableStateFlow(30)
        every { queuePreferencesRepository.observeQueueEnabledStates() } returns MutableStateFlow(emptyMap())
        every { flagPreferencesRepository.observeFlagEnabledStates() } returns MutableStateFlow(emptyMap())
        createViewModel(fuelChannel = channel, ttsEngine = ttsEngine)

        channel.send(fuel(20.0))

        assertEquals(emptyList<SpeechEvent>(), spokenTexts)
        verify(exactly = 1) { simulatorPreferencesRepository.selectedSimulator() }
        confirmVerified(simulatorPreferencesRepository)
    }

    @Test
    fun `残量が閾値以下になると読み上げる`() = runTest(testDispatcher) {
        val channel = Channel<AceWindowsFuelData>(Channel.UNLIMITED)
        val spokenTexts = mutableListOf<SpeechEvent>()
        val ttsEngine = mockTts(spokenTexts)
        stubReadoutDefaults(thresholdPercentage = 30)
        createViewModel(fuelChannel = channel, ttsEngine = ttsEngine)

        channel.send(fuel(50.0))
        channel.send(fuel(20.0))

        assertEquals(listOf<SpeechEvent>(SpeechEvent.AceWindowsRemainingFuelWarning), spokenTexts)
    }

    @Test
    fun `給油後は残り燃料警告を再度読み上げる`() = runTest(testDispatcher) {
        val channel = Channel<AceWindowsFuelData>(Channel.UNLIMITED)
        val spokenTexts = mutableListOf<SpeechEvent>()
        val ttsEngine = mockTts(spokenTexts)
        stubReadoutDefaults(thresholdPercentage = 30)
        createViewModel(fuelChannel = channel, ttsEngine = ttsEngine)

        channel.send(fuel(50.0))
        channel.send(fuel(20.0))
        channel.send(fuel(20.0))
        channel.send(fuel(80.0))
        channel.send(fuel(20.0))

        assertEquals(
            listOf<SpeechEvent>(SpeechEvent.AceWindowsRemainingFuelWarning, SpeechEvent.AceWindowsRemainingFuelWarning),
            spokenTexts,
        )
    }

    @Test
    fun `読み上げが発生したら現在と直前の燃料データを保存する`() = runTest(testDispatcher) {
        val channel = Channel<AceWindowsFuelData>(Channel.UNLIMITED)
        val spokenTexts = mutableListOf<SpeechEvent>()
        val telemetryJsons = mutableListOf<String>()
        val ttsEngine = mockTts(spokenTexts)
        stubReadoutDefaults(thresholdPercentage = 30)
        coEvery {
            telemetryLogRepository.saveTelemetryLog(
                123_456L,
                Simulator.AceWindows,
                ReadoutItemKey.AceWindows.RemainingFuel.Root,
                capture(telemetryJsons),
            )
        } just Runs
        createViewModel(fuelChannel = channel, ttsEngine = ttsEngine, currentTimeMs = { 123_456L })

        channel.send(fuel(50.0))
        channel.send(fuel(20.0))

        assertEquals(1, telemetryJsons.size)
        assertEquals(true, telemetryJsons.single().contains(""""previousFuel":{"remainingPercent":50.0}"""))
        assertEquals(true, telemetryJsons.single().contains(""""fuel":{"remainingPercent":20.0}"""))
        assertEquals(true, telemetryJsons.single().contains(""""observedAtMs":123456"""))
    }

    @Test
    fun `残り燃料項目が無効のときは読み上げない`() = runTest(testDispatcher) {
        val channel = Channel<AceWindowsFuelData>(Channel.UNLIMITED)
        val spokenTexts = mutableListOf<SpeechEvent>()
        val ttsEngine = mockTts(spokenTexts)
        stubReadoutDefaults(
            thresholdPercentage = 30,
            enabledOverrides = mapOf(ReadoutItemKey.AceWindows.RemainingFuel.Root to false),
        )
        createViewModel(fuelChannel = channel, ttsEngine = ttsEngine)

        channel.send(fuel(50.0))
        channel.send(fuel(20.0))

        assertEquals(emptyList<SpeechEvent>(), spokenTexts)
    }

    /**
     * simulator/enabledStates/readoutOrder/thresholdの標準スタブをまとめて設定する。
     * ViewModelがコンストラクタ内で即座にFlowを購読・combineするため、必ず [createViewModel] の前に呼ぶこと。
     */
    private fun stubReadoutDefaults(
        thresholdPercentage: Int,
        enabledOverrides: Map<ReadoutItemKey, Boolean> = emptyMap(),
        orderOverride: List<ReadoutItemKey> = listOf(ReadoutItemKey.AceWindows.RemainingFuel.Root),
        flagEnabledOverrides: Map<ReadoutItemKey, Boolean> = emptyMap(),
    ) {
        every { simulatorPreferencesRepository.selectedSimulator() } returns MutableStateFlow(Simulator.AceWindows)
        every {
            readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.AceWindows.id)
        } returns MutableStateFlow(enabledOverrides)
        every {
            readoutPreferencesRepository.observeReadoutOrder(Simulator.AceWindows.id)
        } returns MutableStateFlow(orderOverride)
        every {
            remainingFuelPreferencesRepository.observeThresholdPercentage()
        } returns MutableStateFlow(thresholdPercentage)
        every { queuePreferencesRepository.observeQueueEnabledStates() } returns MutableStateFlow(emptyMap())
        every { flagPreferencesRepository.observeFlagEnabledStates() } returns MutableStateFlow(flagEnabledOverrides)
        coEvery {
            telemetryLogRepository.saveTelemetryLog(
                any(),
                Simulator.AceWindows,
                ReadoutItemKey.AceWindows.RemainingFuel.Root,
                any(),
            )
        } just Runs
    }

    @Test
    fun `フラグが変化すると読み上げる`() = runTest(testDispatcher) {
        val fuelChannel = Channel<AceWindowsFuelData>(Channel.UNLIMITED)
        val flagChannel = Channel<AceWindowsFlagData>(Channel.UNLIMITED)
        val spokenTexts = mutableListOf<SpeechEvent>()
        val ttsEngine = mockTts(spokenTexts)
        stubReadoutDefaults(thresholdPercentage = 30)
        createViewModel(fuelChannel = fuelChannel, ttsEngine = ttsEngine, flagChannel = flagChannel)

        flagChannel.send(flag(AceWindowsFlagType.NO_FLAG))
        flagChannel.send(flag(AceWindowsFlagType.BLUE_FLAG))

        assertEquals(listOf<SpeechEvent>(SpeechEvent.AceWindowsBlueFlag), spokenTexts)
    }

    @Test
    fun `フラグ項目が無効のときは読み上げない`() = runTest(testDispatcher) {
        val fuelChannel = Channel<AceWindowsFuelData>(Channel.UNLIMITED)
        val flagChannel = Channel<AceWindowsFlagData>(Channel.UNLIMITED)
        val spokenTexts = mutableListOf<SpeechEvent>()
        val ttsEngine = mockTts(spokenTexts)
        stubReadoutDefaults(
            thresholdPercentage = 30,
            enabledOverrides = mapOf(ReadoutItemKey.AceWindows.Flag.Root to false),
        )
        createViewModel(fuelChannel = fuelChannel, ttsEngine = ttsEngine, flagChannel = flagChannel)

        flagChannel.send(flag(AceWindowsFlagType.NO_FLAG))
        flagChannel.send(flag(AceWindowsFlagType.BLUE_FLAG))

        assertEquals(emptyList<SpeechEvent>(), spokenTexts)
    }

    @Test
    fun `個別のフラグ項目が無効のときは読み上げない`() = runTest(testDispatcher) {
        val fuelChannel = Channel<AceWindowsFuelData>(Channel.UNLIMITED)
        val flagChannel = Channel<AceWindowsFlagData>(Channel.UNLIMITED)
        val spokenTexts = mutableListOf<SpeechEvent>()
        val ttsEngine = mockTts(spokenTexts)
        stubReadoutDefaults(
            thresholdPercentage = 30,
            flagEnabledOverrides = mapOf(ReadoutItemKey.AceWindows.Flag.BlueFlag to false),
        )
        createViewModel(fuelChannel = fuelChannel, ttsEngine = ttsEngine, flagChannel = flagChannel)

        flagChannel.send(flag(AceWindowsFlagType.NO_FLAG))
        flagChannel.send(flag(AceWindowsFlagType.BLUE_FLAG))

        assertEquals(emptyList<SpeechEvent>(), spokenTexts)
    }

    private fun flag(flagType: AceWindowsFlagType) = AceWindowsFlagData(flag = flagType)

    private fun mockTts(spokenTexts: MutableList<SpeechEvent>): TextToSpeechEngine {
        every { ttsEngine.currentReadoutItemKey } returns null
        every { ttsEngine.speak(capture(spokenTexts), capture(mutableListOf<Boolean>())) } just Runs
        every { ttsEngine.stop() } just Runs
        return ttsEngine
    }

    private fun fuel(remainingPercent: Double) = AceWindowsFuelData(remainingPercent = remainingPercent)
}
