@file:Suppress("FunctionNaming", "TooManyFunctions")

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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.AceWindowsCarLocation
import kurou.kodriver.domain.model.AceWindowsFlagData
import kurou.kodriver.domain.model.AceWindowsFlagType
import kurou.kodriver.domain.model.AceWindowsFuelData
import kurou.kodriver.domain.model.AceWindowsStatusData
import kurou.kodriver.domain.model.AceWindowsStatusType
import kurou.kodriver.domain.model.AceWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.WheelIndex
import kurou.kodriver.domain.repository.AceWindowsFlagPreferencesRepository
import kurou.kodriver.domain.repository.AceWindowsFlagRepository
import kurou.kodriver.domain.repository.AceWindowsFuelRepository
import kurou.kodriver.domain.repository.AceWindowsRemainingFuelPreferencesRepository
import kurou.kodriver.domain.repository.AceWindowsStatusRepository
import kurou.kodriver.domain.repository.AceWindowsTyreCarcassTemperatureRepository
import kurou.kodriver.domain.repository.AceWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.repository.QueuePreferencesRepository
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.repository.TelemetryLogRepository
import kurou.kodriver.domain.usecase.ObserveAceWindowsFlagEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsFlagUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsFuelUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsRemainingFuelThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsStatusUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsTyreCarcassTemperatureUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsTyreTemperatureEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsTyreTemperatureHighThresholdUseCase
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
    private lateinit var statusRepository: AceWindowsStatusRepository

    @MockK
    private lateinit var tyreCarcassTemperatureRepository: AceWindowsTyreCarcassTemperatureRepository

    @MockK
    private lateinit var tyreTemperaturePreferencesRepository: AceWindowsTyreTemperaturePreferencesRepository

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
        tyreCarcassTemperatureChannel: Channel<AceWindowsTyreCarcassTemperatureData> = Channel(Channel.UNLIMITED),
        currentTimeMs: () -> Long = { 0L },
    ): AceWindowsNarratorViewModel {
        every { fuelRepository.fuelStream() } returns fuelChannel.receiveAsFlow()
        every { flagRepository.flagStream() } returns flagChannel.receiveAsFlow()
        every {
            tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream()
        } returns tyreCarcassTemperatureChannel.receiveAsFlow()
        return AceWindowsNarratorViewModel(
            remainingFuelUseCases =
                RemainingFuelUseCases(
                    observeAceWindowsFuel = ObserveAceWindowsFuelUseCase(fuelRepository),
                    observeThresholdPercentage =
                        ObserveAceWindowsRemainingFuelThresholdPercentageUseCase(remainingFuelPreferencesRepository),
                ),
            readoutListUseCases =
                ReadoutListUseCases(
                    observeSelectedSimulator = ObserveSelectedSimulatorUseCase(simulatorPreferencesRepository),
                    observeReadoutEnabledStates = ObserveReadoutEnabledStatesUseCase(readoutPreferencesRepository),
                    observeReadoutOrder = ObserveReadoutOrderUseCase(readoutPreferencesRepository),
                    observeQueueEnabledStates = ObserveQueueEnabledStatesUseCase(queuePreferencesRepository),
                ),
            flagUseCases =
                FlagUseCases(
                    observeAceWindowsFlag = ObserveAceWindowsFlagUseCase(flagRepository),
                    observeFlagEnabledStates = ObserveAceWindowsFlagEnabledStatesUseCase(flagPreferencesRepository),
                ),
            tyreTemperatureUseCases =
                TyreTemperatureUseCases(
                    observeAceWindowsTyreCarcassTemperature =
                        ObserveAceWindowsTyreCarcassTemperatureUseCase(tyreCarcassTemperatureRepository),
                    observeHighThreshold =
                        ObserveAceWindowsTyreTemperatureHighThresholdUseCase(tyreTemperaturePreferencesRepository),
                    observeTyreTemperatureEnabledStates =
                        ObserveAceWindowsTyreTemperatureEnabledStatesUseCase(tyreTemperaturePreferencesRepository),
                ),
            observeAceWindowsStatus = ObserveAceWindowsStatusUseCase(statusRepository),
            eventProcessor =
                AceWindowsNarratorEventProcessor(
                    ttsEngine = ttsEngine,
                    saveTelemetryLog = SaveTelemetryLogUseCase(telemetryLogRepository),
                ),
            currentTimeMs = currentTimeMs,
        )
    }

    @Test
    fun `ACE非選択時は読み上げない`() =
        runTest(testDispatcher) {
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
            every {
                tyreTemperaturePreferencesRepository.observeHighThresholdCelsius()
            } returns MutableStateFlow(90)
            every {
                tyreTemperaturePreferencesRepository.observeEnabledStates()
            } returns MutableStateFlow(emptyMap())
            createViewModel(fuelChannel = channel, ttsEngine = ttsEngine)

            channel.send(fuel(20.0))

            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
            verify(exactly = 1) { simulatorPreferencesRepository.selectedSimulator() }
            confirmVerified(simulatorPreferencesRepository)
        }

    @Test
    fun `carLocationがTRACK以外の場合は残量が閾値以下でもフラグが変化しても読み上げない`() =
        runTest(testDispatcher) {
            listOf(
                AceWindowsCarLocation.PITLANE,
                AceWindowsCarLocation.PITENTRY,
                AceWindowsCarLocation.PITEXIT,
            ).forEach { carLocation ->
                val fuelChannel = Channel<AceWindowsFuelData>(Channel.UNLIMITED)
                val flagChannel = Channel<AceWindowsFlagData>(Channel.UNLIMITED)
                val spokenTexts = mutableListOf<SpeechEvent>()
                val ttsEngine = mockTts(spokenTexts)
                stubReadoutDefaults(thresholdPercentage = 30, carLocation = carLocation)
                createViewModel(fuelChannel = fuelChannel, ttsEngine = ttsEngine, flagChannel = flagChannel)

                fuelChannel.send(fuel(50.0))
                fuelChannel.send(fuel(20.0))
                flagChannel.send(flag(AceWindowsFlagType.NO_FLAG))
                flagChannel.send(flag(AceWindowsFlagType.BLUE_FLAG))

                assertEquals(emptyList<SpeechEvent>(), spokenTexts)
            }
        }

    @Test
    fun `コース外滞在中も状態は更新され復帰時に古い状態との差分で誤って読み上げたり読み上げ漏れが発生したりしない`() =
        runTest(testDispatcher) {
            val fuelChannel = Channel<AceWindowsFuelData>(Channel.UNLIMITED)
            val flagChannel = Channel<AceWindowsFlagData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val ttsEngine = mockTts(spokenTexts)
            val statusFlow =
                MutableStateFlow(
                    AceWindowsStatusData(status = AceWindowsStatusType.LIVE, carLocation = AceWindowsCarLocation.TRACK),
                )
            stubReadoutDefaults(
                thresholdPercentage = 30,
                orderOverride =
                    listOf(ReadoutItemKey.AceWindows.RemainingFuel.Root, ReadoutItemKey.AceWindows.Flag.Root),
                statusFlowOverride = statusFlow,
            )
            createViewModel(fuelChannel = fuelChannel, ttsEngine = ttsEngine, flagChannel = flagChannel)

            // コース上で残量警告(1回目)と旗の初期状態を確定させる。
            fuelChannel.send(fuel(20.0))
            flagChannel.send(flag(AceWindowsFlagType.NO_FLAG))

            // オフトラック中も状態(previousFlag・残量警告フラグ)は更新される。
            statusFlow.update { it.copy(carLocation = AceWindowsCarLocation.PITLANE) }
            flagChannel.send(flag(AceWindowsFlagType.BLUE_FLAG))
            flagChannel.send(flag(AceWindowsFlagType.WHITE_FLAG))
            fuelChannel.send(fuel(80.0))

            // 復帰時、旗は同じWHITE_FLAGのままなら誤って読み上げず、給油後に再度残量が減れば読み上げ漏れしない。
            statusFlow.update { it.copy(carLocation = AceWindowsCarLocation.TRACK) }
            flagChannel.send(flag(AceWindowsFlagType.WHITE_FLAG))
            fuelChannel.send(fuel(20.0))

            assertEquals(
                listOf<SpeechEvent>(
                    SpeechEvent.AceWindowsRemainingFuelWarning,
                    SpeechEvent.AceWindowsRemainingFuelWarning,
                ),
                spokenTexts,
            )
        }

    @Test
    fun `statusがLIVE以外の場合はcarLocationがTRACKでも残量が閾値以下でもフラグが変化しても読み上げない`() =
        runTest(testDispatcher) {
            listOf(
                AceWindowsStatusType.OFF,
                AceWindowsStatusType.REPLAY,
                AceWindowsStatusType.PAUSE,
                AceWindowsStatusType.UNKNOWN,
            ).forEach { status ->
                val fuelChannel = Channel<AceWindowsFuelData>(Channel.UNLIMITED)
                val flagChannel = Channel<AceWindowsFlagData>(Channel.UNLIMITED)
                val spokenTexts = mutableListOf<SpeechEvent>()
                val ttsEngine = mockTts(spokenTexts)
                stubReadoutDefaults(thresholdPercentage = 30, status = status)
                createViewModel(fuelChannel = fuelChannel, ttsEngine = ttsEngine, flagChannel = flagChannel)

                fuelChannel.send(fuel(50.0))
                fuelChannel.send(fuel(20.0))
                flagChannel.send(flag(AceWindowsFlagType.NO_FLAG))
                flagChannel.send(flag(AceWindowsFlagType.BLUE_FLAG))

                assertEquals(emptyList<SpeechEvent>(), spokenTexts)
            }
        }

    @Test
    fun `ACEを離れて戻した際に古いLIVE状態が残らない`() =
        runTest(testDispatcher) {
            val fuelChannel = Channel<AceWindowsFuelData>(Channel.UNLIMITED)
            val statusChannel = Channel<AceWindowsStatusData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val ttsEngine = mockTts(spokenTexts)
            val simulatorFlow = MutableStateFlow<Simulator?>(Simulator.AceWindows)
            every { simulatorPreferencesRepository.selectedSimulator() } returns simulatorFlow
            every {
                readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.AceWindows.id)
            } returns MutableStateFlow(emptyMap())
            every {
                readoutPreferencesRepository.observeReadoutOrder(Simulator.AceWindows.id)
            } returns MutableStateFlow(listOf(ReadoutItemKey.AceWindows.RemainingFuel.Root))
            every {
                remainingFuelPreferencesRepository.observeThresholdPercentage()
            } returns MutableStateFlow(30)
            every { queuePreferencesRepository.observeQueueEnabledStates() } returns MutableStateFlow(emptyMap())
            every { flagPreferencesRepository.observeFlagEnabledStates() } returns MutableStateFlow(emptyMap())
            every {
                tyreTemperaturePreferencesRepository.observeHighThresholdCelsius()
            } returns MutableStateFlow(90)
            every {
                tyreTemperaturePreferencesRepository.observeEnabledStates()
            } returns MutableStateFlow(emptyMap())
            every { statusRepository.statusStream() } returns statusChannel.receiveAsFlow()
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    any(),
                    Simulator.AceWindows,
                    ReadoutItemKey.AceWindows.RemainingFuel.Root,
                    any(),
                )
            } just Runs
            createViewModel(fuelChannel = fuelChannel, ttsEngine = ttsEngine)

            statusChannel.send(AceWindowsStatusData(status = AceWindowsStatusType.LIVE))
            simulatorFlow.value = null
            simulatorFlow.value = Simulator.AceWindows

            fuelChannel.send(fuel(50.0))
            fuelChannel.send(fuel(20.0))

            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
        }

    @Test
    fun `残量が閾値以下になると読み上げる`() =
        runTest(testDispatcher) {
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
    fun `給油後は残り燃料警告を再度読み上げる`() =
        runTest(testDispatcher) {
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
                listOf<SpeechEvent>(
                    SpeechEvent.AceWindowsRemainingFuelWarning,
                    SpeechEvent.AceWindowsRemainingFuelWarning,
                ),
                spokenTexts,
            )
        }

    @Test
    fun `読み上げが発生したら現在と直前の燃料データを保存する`() =
        runTest(testDispatcher) {
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
    fun `残り燃料項目が無効のときは読み上げない`() =
        runTest(testDispatcher) {
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
    @Suppress("LongParameterList")
    private fun stubReadoutDefaults(
        thresholdPercentage: Int,
        enabledOverrides: Map<ReadoutItemKey, Boolean> = emptyMap(),
        orderOverride: List<ReadoutItemKey> = listOf(ReadoutItemKey.AceWindows.RemainingFuel.Root),
        flagEnabledOverrides: Map<ReadoutItemKey, Boolean> = emptyMap(),
        tyreTemperatureHighThresholdCelsius: Int = 90,
        tyreTemperatureEnabledOverrides: Map<ReadoutItemKey, Boolean> = emptyMap(),
        carLocation: AceWindowsCarLocation = AceWindowsCarLocation.TRACK,
        status: AceWindowsStatusType = AceWindowsStatusType.LIVE,
        statusFlowOverride: MutableStateFlow<AceWindowsStatusData>? = null,
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
        every {
            tyreTemperaturePreferencesRepository.observeHighThresholdCelsius()
        } returns MutableStateFlow(tyreTemperatureHighThresholdCelsius)
        every {
            tyreTemperaturePreferencesRepository.observeEnabledStates()
        } returns MutableStateFlow(tyreTemperatureEnabledOverrides)
        every { statusRepository.statusStream() } returns
            (statusFlowOverride ?: MutableStateFlow(AceWindowsStatusData(status = status, carLocation = carLocation)))
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
    fun `フラグが変化すると読み上げる`() =
        runTest(testDispatcher) {
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
    fun `フラグ項目が無効のときは読み上げない`() =
        runTest(testDispatcher) {
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
    fun `個別のフラグ項目が無効のときは読み上げない`() =
        runTest(testDispatcher) {
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

    @Test
    fun `タイヤが高温になると読み上げる`() =
        runTest(testDispatcher) {
            val fuelChannel = Channel<AceWindowsFuelData>(Channel.UNLIMITED)
            val tyreCarcassTemperatureChannel = Channel<AceWindowsTyreCarcassTemperatureData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val ttsEngine = mockTts(spokenTexts)
            stubReadoutDefaults(thresholdPercentage = 30, tyreTemperatureHighThresholdCelsius = 90)
            createViewModel(
                fuelChannel = fuelChannel,
                ttsEngine = ttsEngine,
                tyreCarcassTemperatureChannel = tyreCarcassTemperatureChannel,
            )

            tyreCarcassTemperatureChannel.send(tyreCarcassTemperature(fl = 85.0))
            tyreCarcassTemperatureChannel.send(tyreCarcassTemperature(fl = 95.0))

            assertEquals(listOf<SpeechEvent>(SpeechEvent.AceWindowsTyreOverheat), spokenTexts)
        }

    @Test
    fun `タイヤ温度項目が無効のときは読み上げない`() =
        runTest(testDispatcher) {
            val fuelChannel = Channel<AceWindowsFuelData>(Channel.UNLIMITED)
            val tyreCarcassTemperatureChannel = Channel<AceWindowsTyreCarcassTemperatureData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val ttsEngine = mockTts(spokenTexts)
            stubReadoutDefaults(
                thresholdPercentage = 30,
                enabledOverrides = mapOf(ReadoutItemKey.AceWindows.TyreTemperature.Root to false),
                tyreTemperatureHighThresholdCelsius = 90,
            )
            createViewModel(
                fuelChannel = fuelChannel,
                ttsEngine = ttsEngine,
                tyreCarcassTemperatureChannel = tyreCarcassTemperatureChannel,
            )

            tyreCarcassTemperatureChannel.send(tyreCarcassTemperature(fl = 85.0))
            tyreCarcassTemperatureChannel.send(tyreCarcassTemperature(fl = 95.0))

            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
        }

    @Test
    fun `過熱警告スイッチが無効のときは読み上げない`() =
        runTest(testDispatcher) {
            val fuelChannel = Channel<AceWindowsFuelData>(Channel.UNLIMITED)
            val tyreCarcassTemperatureChannel = Channel<AceWindowsTyreCarcassTemperatureData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val ttsEngine = mockTts(spokenTexts)
            stubReadoutDefaults(
                thresholdPercentage = 30,
                tyreTemperatureHighThresholdCelsius = 90,
                tyreTemperatureEnabledOverrides =
                    mapOf(ReadoutItemKey.AceWindows.TyreTemperature.OverheatWarning to false),
            )
            createViewModel(
                fuelChannel = fuelChannel,
                ttsEngine = ttsEngine,
                tyreCarcassTemperatureChannel = tyreCarcassTemperatureChannel,
            )

            tyreCarcassTemperatureChannel.send(tyreCarcassTemperature(fl = 85.0))
            tyreCarcassTemperatureChannel.send(tyreCarcassTemperature(fl = 95.0))

            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
        }

    @Test
    fun `読み上げが発生したら現在と直前のタイヤカーカス温度を保存する`() =
        runTest(testDispatcher) {
            val fuelChannel = Channel<AceWindowsFuelData>(Channel.UNLIMITED)
            val tyreCarcassTemperatureChannel = Channel<AceWindowsTyreCarcassTemperatureData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val telemetryJsons = mutableListOf<String>()
            val ttsEngine = mockTts(spokenTexts)
            stubReadoutDefaults(thresholdPercentage = 30, tyreTemperatureHighThresholdCelsius = 90)
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    123_456L,
                    Simulator.AceWindows,
                    ReadoutItemKey.AceWindows.TyreTemperature.Root,
                    capture(telemetryJsons),
                )
            } just Runs
            createViewModel(
                fuelChannel = fuelChannel,
                ttsEngine = ttsEngine,
                tyreCarcassTemperatureChannel = tyreCarcassTemperatureChannel,
                currentTimeMs = { 123_456L },
            )

            tyreCarcassTemperatureChannel.send(tyreCarcassTemperature(fl = 85.0))
            tyreCarcassTemperatureChannel.send(tyreCarcassTemperature(fl = 95.0))

            assertEquals(1, telemetryJsons.size)
            assertEquals(
                true,
                telemetryJsons.single().contains(""""previousTyreCarcassTemperature":{"wheels":{"FRONT_LEFT":85.0}}"""),
            )
            assertEquals(
                true,
                telemetryJsons.single().contains(""""tyreCarcassTemperature":{"wheels":{"FRONT_LEFT":95.0}}"""),
            )
            assertEquals(true, telemetryJsons.single().contains(""""observedAtMs":123456"""))
        }

    private fun flag(flagType: AceWindowsFlagType) = AceWindowsFlagData(flag = flagType)

    private fun mockTts(spokenTexts: MutableList<SpeechEvent>): TextToSpeechEngine {
        every { ttsEngine.currentReadoutItemKey } returns null
        every { ttsEngine.speak(capture(spokenTexts), capture(mutableListOf<Boolean>())) } just Runs
        every { ttsEngine.stop() } just Runs
        return ttsEngine
    }

    private fun fuel(remainingPercent: Double) = AceWindowsFuelData(remainingPercent = remainingPercent)

    private fun tyreCarcassTemperature(fl: Double) =
        AceWindowsTyreCarcassTemperatureData(wheels = mapOf(WheelIndex.FRONT_LEFT to fl))
}
