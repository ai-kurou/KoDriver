@file:Suppress("TooManyFunctions")

package kurou.kodriver.feature.acewindowsnarrator

import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
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
import kurou.kodriver.domain.model.AceWindowsBestLapTimeData
import kurou.kodriver.domain.model.AceWindowsFlagData
import kurou.kodriver.domain.model.AceWindowsFlagType
import kurou.kodriver.domain.model.AceWindowsFuelData
import kurou.kodriver.domain.model.AceWindowsNearbyVehicleData
import kurou.kodriver.domain.model.AceWindowsRemainingFuelLapsData
import kurou.kodriver.domain.model.AceWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.model.AceWindowsVehicleApproachData
import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.FuelPercent
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kurou.kodriver.domain.model.NarrationOutcome
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.WheelIndex
import kurou.kodriver.domain.repository.AceWindowsBestLapTimeRepository
import kurou.kodriver.domain.repository.AceWindowsFlagPreferencesRepository
import kurou.kodriver.domain.repository.AceWindowsFlagRepository
import kurou.kodriver.domain.repository.AceWindowsFuelRepository
import kurou.kodriver.domain.repository.AceWindowsMyBestLapPreferencesRepository
import kurou.kodriver.domain.repository.AceWindowsRemainingFuelLapsPreferencesRepository
import kurou.kodriver.domain.repository.AceWindowsRemainingFuelLapsRepository
import kurou.kodriver.domain.repository.AceWindowsRemainingFuelPreferencesRepository
import kurou.kodriver.domain.repository.AceWindowsTyreCarcassTemperatureRepository
import kurou.kodriver.domain.repository.AceWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.repository.AceWindowsVehicleApproachPreferencesRepository
import kurou.kodriver.domain.repository.AceWindowsVehicleApproachRepository
import kurou.kodriver.domain.repository.QueuePreferencesRepository
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.repository.TelemetryLogRepository
import kurou.kodriver.domain.usecase.AceWindowsVehicleApproachThresholdsUseCases
import kurou.kodriver.domain.usecase.ObserveAceWindowsBestLapTimeUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsFlagEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsFlagUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsFuelUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsMyBestLapVoiceTypeUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsRemainingFuelLapsThresholdUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsRemainingFuelLapsUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsRemainingFuelThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsTyreCarcassTemperatureUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsTyreTemperatureEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsVehicleApproachEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsVehicleApproachUseCase
import kurou.kodriver.domain.usecase.ObserveQueueEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.ObserveResolvedReadoutOrderUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.ResolveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.SaveTelemetryLogUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class AceWindowsNarratorViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val bestLapTimeRepository: AceWindowsBestLapTimeRepository = mockk()

    private val myBestLapPreferencesRepository: AceWindowsMyBestLapPreferencesRepository = mockk()

    private val fuelRepository: AceWindowsFuelRepository = mockk()

    private val remainingFuelPreferencesRepository: AceWindowsRemainingFuelPreferencesRepository = mockk()

    private val remainingFuelLapsRepository: AceWindowsRemainingFuelLapsRepository = mockk()

    private val remainingFuelLapsPreferencesRepository: AceWindowsRemainingFuelLapsPreferencesRepository = mockk()

    private val simulatorPreferencesRepository: SimulatorPreferencesRepository = mockk()

    private val readoutPreferencesRepository: ReadoutPreferencesRepository = mockk()

    private val telemetryLogRepository: TelemetryLogRepository = mockk()

    private val queuePreferencesRepository: QueuePreferencesRepository = mockk()

    private val flagRepository: AceWindowsFlagRepository = mockk()

    private val flagPreferencesRepository: AceWindowsFlagPreferencesRepository = mockk()

    private val tyreCarcassTemperatureRepository: AceWindowsTyreCarcassTemperatureRepository = mockk()

    private val tyreTemperaturePreferencesRepository: AceWindowsTyreTemperaturePreferencesRepository = mockk()

    private val vehicleApproachRepository: AceWindowsVehicleApproachRepository = mockk()

    private val vehicleApproachPreferencesRepository: AceWindowsVehicleApproachPreferencesRepository = mockk()

    private val ttsEngine: TextToSpeechEngine = mockk()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Suppress("LongParameterList")
    private fun createViewModel(
        fuelChannel: Channel<AceWindowsFuelData>,
        ttsEngine: TextToSpeechEngine,
        flagChannel: Channel<AceWindowsFlagData> = Channel(Channel.UNLIMITED),
        tyreCarcassTemperatureChannel: Channel<AceWindowsTyreCarcassTemperatureData> = Channel(Channel.UNLIMITED),
        vehicleApproachChannel: Channel<AceWindowsVehicleApproachData> = Channel(Channel.UNLIMITED),
        bestLapTimeChannel: Channel<AceWindowsBestLapTimeData> = Channel(Channel.UNLIMITED),
        remainingFuelLapsChannel: Channel<AceWindowsRemainingFuelLapsData> = Channel(Channel.UNLIMITED),
        currentTimeMs: () -> Long = { 0L },
        readoutText: suspend (SpeechEvent) -> String? = { it.narratedText },
    ): AceWindowsNarratorViewModel {
        every { fuelRepository.fuelStream() } returns fuelChannel.receiveAsFlow()
        every { flagRepository.flagStream() } returns flagChannel.receiveAsFlow()
        every {
            tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream()
        } returns tyreCarcassTemperatureChannel.receiveAsFlow()
        every {
            vehicleApproachRepository.vehicleApproachStream()
        } returns vehicleApproachChannel.receiveAsFlow()
        every { bestLapTimeRepository.bestLapTimeStream() } returns bestLapTimeChannel.receiveAsFlow()
        every {
            remainingFuelLapsRepository.remainingFuelLapsStream()
        } returns remainingFuelLapsChannel.receiveAsFlow()
        return AceWindowsNarratorViewModel(
            myBestLapUseCases =
                MyBestLapUseCases(
                    observeBestLapTime = ObserveAceWindowsBestLapTimeUseCase(bestLapTimeRepository),
                    observeMyBestLapVoiceType =
                        ObserveAceWindowsMyBestLapVoiceTypeUseCase(
                            myBestLapPreferencesRepository,
                        ),
                ),
            remainingFuelUseCases =
                RemainingFuelUseCases(
                    observeAceWindowsFuel = ObserveAceWindowsFuelUseCase(fuelRepository),
                    observeThresholdPercentage =
                        ObserveAceWindowsRemainingFuelThresholdPercentageUseCase(remainingFuelPreferencesRepository),
                ),
            remainingFuelLapsUseCases =
                RemainingFuelLapsUseCases(
                    observeAceWindowsRemainingFuelLaps =
                        ObserveAceWindowsRemainingFuelLapsUseCase(remainingFuelLapsRepository),
                    observeThreshold =
                        ObserveAceWindowsRemainingFuelLapsThresholdUseCase(remainingFuelLapsPreferencesRepository),
                ),
            simulatorUseCases =
                SimulatorUseCases(ObserveSelectedSimulatorUseCase(simulatorPreferencesRepository)),
            readoutListUseCases =
                ReadoutListUseCases(
                    observeReadoutEnabledStates = ObserveReadoutEnabledStatesUseCase(readoutPreferencesRepository),
                    observeReadoutOrder =
                        ObserveResolvedReadoutOrderUseCase(
                            ObserveReadoutOrderUseCase(readoutPreferencesRepository),
                            ResolveReadoutOrderUseCase(),
                        ),
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
            vehicleApproachUseCases =
                VehicleApproachUseCases(
                    observeVehicleApproach = ObserveAceWindowsVehicleApproachUseCase(vehicleApproachRepository),
                    observeEnabledStates =
                        ObserveAceWindowsVehicleApproachEnabledStatesUseCase(vehicleApproachPreferencesRepository),
                    thresholds = AceWindowsVehicleApproachThresholdsUseCases(vehicleApproachPreferencesRepository),
                ),
            eventProcessor =
                AceWindowsNarratorEventProcessor(
                    ttsEngine = ttsEngine,
                    saveTelemetryLog = SaveTelemetryLogUseCase(telemetryLogRepository),
                    readoutText = readoutText,
                ),
            currentTimeMs = currentTimeMs,
        )
    }

    @Test
    fun `自由文言の解決に失敗しても次のテレメトリを収集して読み上げる`() =
        runTest(testDispatcher) {
            val channel = Channel<AceWindowsFlagData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            val key = ReadoutItemKey.AceWindows.Flag.Root
            stubReadoutDefaults(thresholdPercentage = 30)
            val skippedJson = slot<String>()
            val spokenJson = slot<String>()
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.AceWindows,
                    key,
                    "",
                    NarrationOutcome.SKIPPED,
                    capture(skippedJson),
                )
            } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.AceWindows,
                    key,
                    "復旧",
                    NarrationOutcome.SPOKEN,
                    capture(spokenJson),
                )
            } just Runs
            createViewModel(
                fuelChannel = Channel(Channel.UNLIMITED),
                flagChannel = channel,
                ttsEngine = tts,
                readoutText = {
                    if (it == SpeechEvent.AceWindowsBlueFlag) error("preference error")
                    "復旧"
                },
            )

            channel.send(flag(AceWindowsFlagType.NO_FLAG))
            channel.send(flag(AceWindowsFlagType.BLUE_FLAG))
            channel.send(flag(AceWindowsFlagType.RED_FLAG))

            assertEquals(listOf<SpeechEvent>(SpeechEvent.AceWindowsRedFlag), spokenTexts)
            verify(exactly = 1) { tts.currentReadoutItemKey }
            verify(exactly = 1) { tts.speak(SpeechEvent.AceWindowsRedFlag, false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.AceWindows,
                    key,
                    "",
                    NarrationOutcome.SKIPPED,
                    skippedJson.captured,
                )
            }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.AceWindows,
                    key,
                    "復旧",
                    NarrationOutcome.SPOKEN,
                    spokenJson.captured,
                )
            }
            confirmVerified(tts, telemetryLogRepository)
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
            val telemetryJsonSlot = slot<String>()
            val ttsEngine = mockTts(spokenTexts)
            stubReadoutDefaults(thresholdPercentage = 30)
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    123_456L,
                    Simulator.AceWindows,
                    ReadoutItemKey.AceWindows.RemainingFuel.Root,
                    "残り燃料警告",
                    NarrationOutcome.QUEUED,
                    match { it.isNotEmpty() },
                )
            } just Runs
            createViewModel(fuelChannel = channel, ttsEngine = ttsEngine, currentTimeMs = { 123_456L })

            channel.send(fuel(50.0))
            channel.send(fuel(20.0))

            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    123_456L,
                    Simulator.AceWindows,
                    ReadoutItemKey.AceWindows.RemainingFuel.Root,
                    "残り燃料警告",
                    NarrationOutcome.QUEUED,
                    capture(telemetryJsonSlot),
                )
            }
            assertEquals(true, telemetryJsonSlot.captured.contains(""""previousFuel":{"remainingPercent":50.0}"""))
            assertEquals(true, telemetryJsonSlot.captured.contains(""""fuel":{"remainingPercent":20.0}"""))
            confirmVerified(telemetryLogRepository)
            assertEquals(true, telemetryJsonSlot.captured.contains(""""observedAtMs":123456"""))
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

    @Test
    fun `燃料残り周回数が閾値以下になると読み上げる`() =
        runTest(testDispatcher) {
            val remainingFuelLapsChannel = Channel<AceWindowsRemainingFuelLapsData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val ttsEngine = mockTts(spokenTexts)
            stubReadoutDefaults(thresholdPercentage = 30, remainingFuelLapsThreshold = 2)
            stubRemainingFuelLapsTelemetryLog()
            createViewModel(
                fuelChannel = Channel(Channel.UNLIMITED),
                ttsEngine = ttsEngine,
                remainingFuelLapsChannel = remainingFuelLapsChannel,
            )

            remainingFuelLapsChannel.send(AceWindowsRemainingFuelLapsData(remainingLaps = 3.5f))
            remainingFuelLapsChannel.send(AceWindowsRemainingFuelLapsData(remainingLaps = 2.5f))
            remainingFuelLapsChannel.send(AceWindowsRemainingFuelLapsData(remainingLaps = 2.1f))
            remainingFuelLapsChannel.send(AceWindowsRemainingFuelLapsData(remainingLaps = 1.9f))

            assertEquals(
                listOf<SpeechEvent>(
                    SpeechEvent.AceWindowsRemainingFuelLapsWarning(2),
                    SpeechEvent.AceWindowsRemainingFuelLapsWarning(1),
                ),
                spokenTexts,
            )
        }

    @Test
    fun `燃料残り周回数の読み上げ時に現在と直前のデータを保存する`() =
        runTest(testDispatcher) {
            val remainingFuelLapsChannel = Channel<AceWindowsRemainingFuelLapsData>(Channel.UNLIMITED)
            val telemetryJsonSlot = slot<String>()
            val ttsEngine = mockTts(mutableListOf())
            stubReadoutDefaults(thresholdPercentage = 30, remainingFuelLapsThreshold = 3)
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    123_456L,
                    Simulator.AceWindows,
                    ReadoutItemKey.AceWindows.RemainingFuelLaps.Root,
                    "燃料は残り約3周",
                    NarrationOutcome.QUEUED,
                    match { it.isNotEmpty() },
                )
            } just Runs
            createViewModel(
                fuelChannel = Channel(Channel.UNLIMITED),
                ttsEngine = ttsEngine,
                remainingFuelLapsChannel = remainingFuelLapsChannel,
                currentTimeMs = { 123_456L },
            )

            remainingFuelLapsChannel.send(AceWindowsRemainingFuelLapsData(remainingLaps = 4.5f))
            remainingFuelLapsChannel.send(AceWindowsRemainingFuelLapsData(remainingLaps = 3.5f))

            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    123_456L,
                    Simulator.AceWindows,
                    ReadoutItemKey.AceWindows.RemainingFuelLaps.Root,
                    "燃料は残り約3周",
                    NarrationOutcome.QUEUED,
                    capture(telemetryJsonSlot),
                )
            }
            assertEquals(
                true,
                telemetryJsonSlot.captured.contains(""""previousRemainingFuelLaps":{"remainingLaps":4.5}"""),
            )
            assertEquals(true, telemetryJsonSlot.captured.contains(""""remainingFuelLaps":{"remainingLaps":3.5}"""))
            confirmVerified(telemetryLogRepository)
        }

    @Test
    fun `燃料残り周回数項目が無効のときは読み上げない`() =
        runTest(testDispatcher) {
            val remainingFuelLapsChannel = Channel<AceWindowsRemainingFuelLapsData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val ttsEngine = mockTts(spokenTexts)
            stubReadoutDefaults(
                thresholdPercentage = 30,
                enabledOverrides = mapOf(ReadoutItemKey.AceWindows.RemainingFuelLaps.Root to false),
            )
            createViewModel(
                fuelChannel = Channel(Channel.UNLIMITED),
                ttsEngine = ttsEngine,
                remainingFuelLapsChannel = remainingFuelLapsChannel,
            )

            remainingFuelLapsChannel.send(AceWindowsRemainingFuelLapsData(remainingLaps = 1.5f))

            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
        }

    private fun stubRemainingFuelLapsTelemetryLog() {
        coEvery {
            telemetryLogRepository.saveTelemetryLog(
                0L,
                Simulator.AceWindows,
                ReadoutItemKey.AceWindows.RemainingFuelLaps.Root,
                "燃料は残り約3周",
                NarrationOutcome.SPOKEN,
                "{}",
            )
        } just Runs
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
        vehicleApproachEnabledOverrides: Map<ReadoutItemKey, Boolean> =
            mapOf(
                ReadoutItemKey.AceWindows.VehicleApproach.Root to true,
                ReadoutItemKey.AceWindows.VehicleApproach.StartReadout to true,
            ),
        vehicleApproachThresholdMeters: Double = 10.0,
        myBestLapVoiceType: MyBestLapVoiceType = MyBestLapVoiceType.FORMAL,
        remainingFuelLapsThreshold: Int = 3,
    ) {
        every { simulatorPreferencesRepository.selectedSimulator() } returns MutableStateFlow(Simulator.AceWindows)
        every { myBestLapPreferencesRepository.observeVoiceType() } returns MutableStateFlow(myBestLapVoiceType)
        every {
            readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.AceWindows.id)
        } returns MutableStateFlow(enabledOverrides)
        every {
            readoutPreferencesRepository.observeReadoutOrder(Simulator.AceWindows.id)
        } returns MutableStateFlow(orderOverride)
        every {
            remainingFuelPreferencesRepository.observeThresholdPercentage()
        } returns MutableStateFlow(thresholdPercentage)
        every {
            remainingFuelLapsPreferencesRepository.observeThresholdLaps()
        } returns MutableStateFlow(remainingFuelLapsThreshold)
        every { queuePreferencesRepository.observeQueueEnabledStates() } returns MutableStateFlow(emptyMap())
        every { flagPreferencesRepository.observeFlagEnabledStates() } returns MutableStateFlow(flagEnabledOverrides)
        every {
            tyreTemperaturePreferencesRepository.observeHighThresholdCelsius()
        } returns MutableStateFlow(Celsius(tyreTemperatureHighThresholdCelsius))
        every {
            tyreTemperaturePreferencesRepository.observeEnabledStates()
        } returns MutableStateFlow(tyreTemperatureEnabledOverrides)
        every {
            vehicleApproachPreferencesRepository.observeEnabledStates()
        } returns MutableStateFlow(vehicleApproachEnabledOverrides)
        every {
            vehicleApproachPreferencesRepository.observeThresholdMeters()
        } returns MutableStateFlow(vehicleApproachThresholdMeters)
        coEvery {
            telemetryLogRepository.saveTelemetryLog(
                0L,
                Simulator.AceWindows,
                ReadoutItemKey.AceWindows.RemainingFuel.Root,
                "残り燃料警告",
                NarrationOutcome.SPOKEN,
                "{}",
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
            listOf(
                AceWindowsFlagType.WHITE_FLAG,
                AceWindowsFlagType.GREEN_FLAG,
                AceWindowsFlagType.RED_FLAG,
                AceWindowsFlagType.BLUE_FLAG,
                AceWindowsFlagType.YELLOW_FLAG,
                AceWindowsFlagType.BLACK_FLAG,
                AceWindowsFlagType.BLACK_WHITE_FLAG,
                AceWindowsFlagType.ORANGE_CIRCLE_FLAG,
                AceWindowsFlagType.RED_YELLOW_STRIPES_FLAG,
            ).forEach { type ->
                flagChannel.send(flag(type))
            }

            assertEquals(
                listOf(
                    SpeechEvent.AceWindowsWhiteFlag,
                    SpeechEvent.AceWindowsGreenFlag,
                    SpeechEvent.AceWindowsRedFlag,
                    SpeechEvent.AceWindowsBlueFlag,
                    SpeechEvent.AceWindowsYellowFlag,
                    SpeechEvent.AceWindowsBlackFlag,
                    SpeechEvent.AceWindowsBlackWhiteFlag,
                    SpeechEvent.AceWindowsOrangeCircleFlag,
                    SpeechEvent.AceWindowsRedYellowStripesFlag,
                ),
                spokenTexts,
            )
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
            listOf(
                AceWindowsFlagType.WHITE_FLAG,
                AceWindowsFlagType.GREEN_FLAG,
                AceWindowsFlagType.RED_FLAG,
                AceWindowsFlagType.BLUE_FLAG,
                AceWindowsFlagType.YELLOW_FLAG,
                AceWindowsFlagType.BLACK_FLAG,
                AceWindowsFlagType.BLACK_WHITE_FLAG,
                AceWindowsFlagType.ORANGE_CIRCLE_FLAG,
                AceWindowsFlagType.RED_YELLOW_STRIPES_FLAG,
            ).forEach { type ->
                flagChannel.send(flag(type))
            }

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
                flagEnabledOverrides =
                    mapOf(
                        ReadoutItemKey.AceWindows.Flag.WhiteFlag to false,
                        ReadoutItemKey.AceWindows.Flag.GreenFlag to false,
                        ReadoutItemKey.AceWindows.Flag.RedFlag to false,
                        ReadoutItemKey.AceWindows.Flag.BlueFlag to false,
                        ReadoutItemKey.AceWindows.Flag.YellowFlag to false,
                        ReadoutItemKey.AceWindows.Flag.BlackFlag to false,
                        ReadoutItemKey.AceWindows.Flag.BlackWhiteFlag to false,
                        ReadoutItemKey.AceWindows.Flag.OrangeCircleFlag to false,
                        ReadoutItemKey.AceWindows.Flag.RedYellowStripesFlag to false,
                    ),
            )
            createViewModel(fuelChannel = fuelChannel, ttsEngine = ttsEngine, flagChannel = flagChannel)

            flagChannel.send(flag(AceWindowsFlagType.NO_FLAG))
            listOf(
                AceWindowsFlagType.WHITE_FLAG,
                AceWindowsFlagType.GREEN_FLAG,
                AceWindowsFlagType.RED_FLAG,
                AceWindowsFlagType.BLUE_FLAG,
                AceWindowsFlagType.YELLOW_FLAG,
                AceWindowsFlagType.BLACK_FLAG,
                AceWindowsFlagType.BLACK_WHITE_FLAG,
                AceWindowsFlagType.ORANGE_CIRCLE_FLAG,
                AceWindowsFlagType.RED_YELLOW_STRIPES_FLAG,
            ).forEach { type ->
                flagChannel.send(flag(type))
            }

            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
        }

    @Test
    fun `タイヤが高温になると判定時に解決した本文を保持して読み上げる`() =
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
                readoutText = { "過熱注意 95度" },
            )

            tyreCarcassTemperatureChannel.send(tyreCarcassTemperature(fl = 85.0f))
            tyreCarcassTemperatureChannel.send(tyreCarcassTemperature(fl = 95.0f))

            assertEquals(listOf<SpeechEvent>(SpeechEvent.AceWindowsTyreOverheat(95, "過熱注意 95度")), spokenTexts)
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

            tyreCarcassTemperatureChannel.send(tyreCarcassTemperature(fl = 85.0f))
            tyreCarcassTemperatureChannel.send(tyreCarcassTemperature(fl = 95.0f))

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

            tyreCarcassTemperatureChannel.send(tyreCarcassTemperature(fl = 85.0f))
            tyreCarcassTemperatureChannel.send(tyreCarcassTemperature(fl = 95.0f))

            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
        }

    @Test
    fun `読み上げが発生したら現在と直前のタイヤカーカス温度を保存する`() =
        runTest(testDispatcher) {
            val fuelChannel = Channel<AceWindowsFuelData>(Channel.UNLIMITED)
            val tyreCarcassTemperatureChannel = Channel<AceWindowsTyreCarcassTemperatureData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val telemetryJsonSlot = slot<String>()
            val ttsEngine = mockTts(spokenTexts)
            stubReadoutDefaults(thresholdPercentage = 30, tyreTemperatureHighThresholdCelsius = 90)
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    123_456L,
                    Simulator.AceWindows,
                    ReadoutItemKey.AceWindows.TyreTemperature.Root,
                    "タイヤ過熱 95度",
                    NarrationOutcome.QUEUED,
                    match { it.isNotEmpty() },
                )
            } just Runs
            createViewModel(
                fuelChannel = fuelChannel,
                ttsEngine = ttsEngine,
                tyreCarcassTemperatureChannel = tyreCarcassTemperatureChannel,
                currentTimeMs = { 123_456L },
            )

            tyreCarcassTemperatureChannel.send(tyreCarcassTemperature(fl = 85.0f))
            tyreCarcassTemperatureChannel.send(tyreCarcassTemperature(fl = 95.0f))

            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    123_456L,
                    Simulator.AceWindows,
                    ReadoutItemKey.AceWindows.TyreTemperature.Root,
                    "タイヤ過熱 95度",
                    NarrationOutcome.QUEUED,
                    capture(telemetryJsonSlot),
                )
            }
            assertEquals(
                true,
                telemetryJsonSlot.captured.contains(
                    """"previousTyreCarcassTemperature":{"wheels":{"FRONT_LEFT":85.0}}""",
                ),
            )
            assertEquals(
                true,
                telemetryJsonSlot.captured.contains(""""tyreCarcassTemperature":{"wheels":{"FRONT_LEFT":95.0}}"""),
            )
            assertEquals(true, telemetryJsonSlot.captured.contains(""""observedAtMs":123456"""))
            confirmVerified(telemetryLogRepository)
        }

    @Test
    fun `閾値内に車両が入ると読み上げる`() =
        runTest(testDispatcher) {
            val fuelChannel = Channel<AceWindowsFuelData>(Channel.UNLIMITED)
            val vehicleApproachChannel = Channel<AceWindowsVehicleApproachData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val ttsEngine = mockTts(spokenTexts)
            stubReadoutDefaults(
                thresholdPercentage = 30,
                orderOverride = listOf(ReadoutItemKey.AceWindows.VehicleApproach.Root),
            )
            createViewModel(
                fuelChannel = fuelChannel,
                ttsEngine = ttsEngine,
                vehicleApproachChannel = vehicleApproachChannel,
            )

            vehicleApproachChannel.send(vehicleApproach(distanceMeters = 5.0))

            assertEquals(listOf<SpeechEvent>(SpeechEvent.AceWindowsVehicleApproach), spokenTexts)
        }

    @Test
    fun `保存した車両接近文言をログに記録する`() =
        runTest(testDispatcher) {
            val fuelChannel = Channel<AceWindowsFuelData>(Channel.UNLIMITED)
            val vehicleApproachChannel = Channel<AceWindowsVehicleApproachData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val ttsEngine = mockTts(spokenTexts)
            stubReadoutDefaults(
                thresholdPercentage = 30,
                orderOverride = listOf(ReadoutItemKey.AceWindows.VehicleApproach.Root),
            )
            val json = slot<String>()
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.AceWindows,
                    ReadoutItemKey.AceWindows.VehicleApproach.Root,
                    "周囲に注意",
                    NarrationOutcome.SPOKEN,
                    capture(json),
                )
            } just Runs
            createViewModel(
                fuelChannel = fuelChannel,
                ttsEngine = ttsEngine,
                vehicleApproachChannel = vehicleApproachChannel,
                readoutText = { "周囲に注意" },
            )

            vehicleApproachChannel.send(vehicleApproach(distanceMeters = 5.0))

            assertEquals(listOf<SpeechEvent>(SpeechEvent.AceWindowsVehicleApproach), spokenTexts)
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.AceWindowsVehicleApproach, false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.AceWindows,
                    ReadoutItemKey.AceWindows.VehicleApproach.Root,
                    "周囲に注意",
                    NarrationOutcome.SPOKEN,
                    json.captured,
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `車両接近文言が空白またはTTS不可なら空文字のSKIPPEDを記録する`() =
        runTest(testDispatcher) {
            val fuelChannel = Channel<AceWindowsFuelData>(Channel.UNLIMITED)
            val vehicleApproachChannel = Channel<AceWindowsVehicleApproachData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val ttsEngine = mockTts(spokenTexts)
            stubReadoutDefaults(
                thresholdPercentage = 30,
                orderOverride = listOf(ReadoutItemKey.AceWindows.VehicleApproach.Root),
            )
            val json = slot<String>()
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.AceWindows,
                    ReadoutItemKey.AceWindows.VehicleApproach.Root,
                    "",
                    NarrationOutcome.SKIPPED,
                    capture(json),
                )
            } just Runs
            createViewModel(
                fuelChannel = fuelChannel,
                ttsEngine = ttsEngine,
                vehicleApproachChannel = vehicleApproachChannel,
                readoutText = { null },
            )

            vehicleApproachChannel.send(vehicleApproach(distanceMeters = 5.0))

            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
            verify(exactly = 0) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 0) { ttsEngine.speak(SpeechEvent.AceWindowsVehicleApproach, false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.AceWindows,
                    ReadoutItemKey.AceWindows.VehicleApproach.Root,
                    "",
                    NarrationOutcome.SKIPPED,
                    json.captured,
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `車両接近項目が無効のときは読み上げない`() =
        runTest(testDispatcher) {
            val fuelChannel = Channel<AceWindowsFuelData>(Channel.UNLIMITED)
            val vehicleApproachChannel = Channel<AceWindowsVehicleApproachData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val ttsEngine = mockTts(spokenTexts)
            stubReadoutDefaults(
                thresholdPercentage = 30,
                vehicleApproachEnabledOverrides = mapOf(ReadoutItemKey.AceWindows.VehicleApproach.Root to false),
            )
            createViewModel(
                fuelChannel = fuelChannel,
                ttsEngine = ttsEngine,
                vehicleApproachChannel = vehicleApproachChannel,
            )

            vehicleApproachChannel.send(vehicleApproach(distanceMeters = 5.0))

            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
        }

    @Test
    fun `接近開始時の読み上げが無効のときは読み上げない`() =
        runTest(testDispatcher) {
            val fuelChannel = Channel<AceWindowsFuelData>(Channel.UNLIMITED)
            val vehicleApproachChannel = Channel<AceWindowsVehicleApproachData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val ttsEngine = mockTts(spokenTexts)
            stubReadoutDefaults(
                thresholdPercentage = 30,
                vehicleApproachEnabledOverrides =
                    mapOf(
                        ReadoutItemKey.AceWindows.VehicleApproach.Root to true,
                        ReadoutItemKey.AceWindows.VehicleApproach.StartReadout to false,
                    ),
            )
            createViewModel(
                fuelChannel = fuelChannel,
                ttsEngine = ttsEngine,
                vehicleApproachChannel = vehicleApproachChannel,
            )

            vehicleApproachChannel.send(vehicleApproach(distanceMeters = 5.0))

            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
        }

    @Test
    fun `閾値より遠い場合は読み上げない`() =
        runTest(testDispatcher) {
            val fuelChannel = Channel<AceWindowsFuelData>(Channel.UNLIMITED)
            val vehicleApproachChannel = Channel<AceWindowsVehicleApproachData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val ttsEngine = mockTts(spokenTexts)
            stubReadoutDefaults(thresholdPercentage = 30)
            createViewModel(
                fuelChannel = fuelChannel,
                ttsEngine = ttsEngine,
                vehicleApproachChannel = vehicleApproachChannel,
            )

            vehicleApproachChannel.send(vehicleApproach(distanceMeters = 20.0))

            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
        }

    @Test
    fun `接近状態が継続しても再度読み上げない`() =
        runTest(testDispatcher) {
            val fuelChannel = Channel<AceWindowsFuelData>(Channel.UNLIMITED)
            val vehicleApproachChannel = Channel<AceWindowsVehicleApproachData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val ttsEngine = mockTts(spokenTexts)
            stubReadoutDefaults(thresholdPercentage = 30)
            createViewModel(
                fuelChannel = fuelChannel,
                ttsEngine = ttsEngine,
                vehicleApproachChannel = vehicleApproachChannel,
            )

            vehicleApproachChannel.send(vehicleApproach(distanceMeters = 5.0))
            vehicleApproachChannel.send(vehicleApproach(distanceMeters = 5.0))

            assertEquals(listOf<SpeechEvent>(SpeechEvent.AceWindowsVehicleApproach), spokenTexts)
        }

    @Test
    fun `車両が離れて再接近すると再度読み上げる`() =
        runTest(testDispatcher) {
            val fuelChannel = Channel<AceWindowsFuelData>(Channel.UNLIMITED)
            val vehicleApproachChannel = Channel<AceWindowsVehicleApproachData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val ttsEngine = mockTts(spokenTexts)
            stubReadoutDefaults(thresholdPercentage = 30)
            createViewModel(
                fuelChannel = fuelChannel,
                ttsEngine = ttsEngine,
                vehicleApproachChannel = vehicleApproachChannel,
            )

            vehicleApproachChannel.send(vehicleApproach(distanceMeters = 5.0))
            vehicleApproachChannel.send(vehicleApproach(distanceMeters = 20.0))
            vehicleApproachChannel.send(vehicleApproach(distanceMeters = 5.0))

            assertEquals(
                listOf<SpeechEvent>(SpeechEvent.AceWindowsVehicleApproach, SpeechEvent.AceWindowsVehicleApproach),
                spokenTexts,
            )
        }

    @Test
    fun `読み上げが発生したら現在と直前の車両接近データを保存する`() =
        runTest(testDispatcher) {
            val fuelChannel = Channel<AceWindowsFuelData>(Channel.UNLIMITED)
            val vehicleApproachChannel = Channel<AceWindowsVehicleApproachData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val telemetryJsons = mutableListOf<String>()
            val ttsEngine = mockTts(spokenTexts)
            stubReadoutDefaults(
                thresholdPercentage = 30,
                orderOverride = listOf(ReadoutItemKey.AceWindows.VehicleApproach.Root),
            )
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    123_456L,
                    Simulator.AceWindows,
                    ReadoutItemKey.AceWindows.VehicleApproach.Root,
                    "車両接近",
                    NarrationOutcome.SPOKEN,
                    capture(telemetryJsons),
                )
            } just Runs
            createViewModel(
                fuelChannel = fuelChannel,
                ttsEngine = ttsEngine,
                vehicleApproachChannel = vehicleApproachChannel,
                currentTimeMs = { 123_456L },
            )

            vehicleApproachChannel.send(vehicleApproach(distanceMeters = 20.0))
            vehicleApproachChannel.send(vehicleApproach(distanceMeters = 5.0))

            assertEquals(1, telemetryJsons.size)
            assertEquals(
                true,
                telemetryJsons.single().contains(
                    """"previousVehicleApproach":{"nearbyVehicles":[{"distanceMeters":20.0}]}""",
                ),
            )
            assertEquals(
                true,
                telemetryJsons.single().contains(""""vehicleApproach":{"nearbyVehicles":[{"distanceMeters":5.0}]}"""),
            )
            assertEquals(true, telemetryJsons.single().contains(""""observedAtMs":123456"""))
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    123_456L,
                    Simulator.AceWindows,
                    ReadoutItemKey.AceWindows.VehicleApproach.Root,
                    "車両接近",
                    NarrationOutcome.SPOKEN,
                    telemetryJsons.single(),
                )
            }
            confirmVerified(telemetryLogRepository)
        }

    @Test
    fun `自己ベストが更新されると読み上げる`() =
        runTest(testDispatcher) {
            val fuelChannel = Channel<AceWindowsFuelData>(Channel.UNLIMITED)
            val bestLapTimeChannel = Channel<AceWindowsBestLapTimeData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val ttsEngine = mockTts(spokenTexts)
            stubReadoutDefaults(
                thresholdPercentage = 30,
                enabledOverrides = mapOf(ReadoutItemKey.AceWindows.MyBestLap.Root to true),
                orderOverride = listOf(ReadoutItemKey.AceWindows.MyBestLap.Root),
            )
            createViewModel(
                fuelChannel = fuelChannel,
                ttsEngine = ttsEngine,
                bestLapTimeChannel = bestLapTimeChannel,
            )

            bestLapTimeChannel.send(bestLapTime(90_000))
            bestLapTimeChannel.send(bestLapTime(89_000))

            assertEquals(listOf<SpeechEvent>(SpeechEvent.AceWindowsMyBestLapFormal), spokenTexts)
        }

    @Test
    fun `声種別がCASUALならAceWindowsMyBestLapCasualを読み上げる`() =
        runTest(testDispatcher) {
            val fuelChannel = Channel<AceWindowsFuelData>(Channel.UNLIMITED)
            val bestLapTimeChannel = Channel<AceWindowsBestLapTimeData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val ttsEngine = mockTts(spokenTexts)
            stubReadoutDefaults(
                thresholdPercentage = 30,
                enabledOverrides = mapOf(ReadoutItemKey.AceWindows.MyBestLap.Root to true),
                orderOverride = listOf(ReadoutItemKey.AceWindows.MyBestLap.Root),
                myBestLapVoiceType = MyBestLapVoiceType.CASUAL,
            )
            createViewModel(
                fuelChannel = fuelChannel,
                ttsEngine = ttsEngine,
                bestLapTimeChannel = bestLapTimeChannel,
            )

            bestLapTimeChannel.send(bestLapTime(90_000))
            bestLapTimeChannel.send(bestLapTime(89_000))

            assertEquals(listOf<SpeechEvent>(SpeechEvent.AceWindowsMyBestLapCasual), spokenTexts)
        }

    @Test
    fun `自己ベストラップ項目が無効のときは読み上げない`() =
        runTest(testDispatcher) {
            val fuelChannel = Channel<AceWindowsFuelData>(Channel.UNLIMITED)
            val bestLapTimeChannel = Channel<AceWindowsBestLapTimeData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val ttsEngine = mockTts(spokenTexts)
            stubReadoutDefaults(
                thresholdPercentage = 30,
                enabledOverrides = mapOf(ReadoutItemKey.AceWindows.MyBestLap.Root to false),
            )
            createViewModel(
                fuelChannel = fuelChannel,
                ttsEngine = ttsEngine,
                bestLapTimeChannel = bestLapTimeChannel,
            )

            bestLapTimeChannel.send(bestLapTime(90_000))
            bestLapTimeChannel.send(bestLapTime(89_000))

            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
        }

    @Test
    fun `読み上げが発生したら現在と直前のベストラップデータを保存する`() =
        runTest(testDispatcher) {
            val fuelChannel = Channel<AceWindowsFuelData>(Channel.UNLIMITED)
            val bestLapTimeChannel = Channel<AceWindowsBestLapTimeData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val telemetryJsons = mutableListOf<String>()
            val ttsEngine = mockTts(spokenTexts)
            stubReadoutDefaults(
                thresholdPercentage = 30,
                enabledOverrides = mapOf(ReadoutItemKey.AceWindows.MyBestLap.Root to true),
                orderOverride = listOf(ReadoutItemKey.AceWindows.MyBestLap.Root),
            )
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    123_456L,
                    Simulator.AceWindows,
                    ReadoutItemKey.AceWindows.MyBestLap.Root,
                    "自己ベストラップ更新",
                    NarrationOutcome.SPOKEN,
                    capture(telemetryJsons),
                )
            } just Runs
            createViewModel(
                fuelChannel = fuelChannel,
                ttsEngine = ttsEngine,
                bestLapTimeChannel = bestLapTimeChannel,
                currentTimeMs = { 123_456L },
            )

            bestLapTimeChannel.send(bestLapTime(90_000))
            bestLapTimeChannel.send(bestLapTime(89_000))

            assertEquals(1, telemetryJsons.size)
            assertEquals(
                true,
                telemetryJsons.single().contains(""""previousBestLapTime":{"bestLapTimeMs":90000,"currentLap":0}"""),
            )
            assertEquals(
                true,
                telemetryJsons.single().contains(""""bestLapTime":{"bestLapTimeMs":89000,"currentLap":0}"""),
            )
            assertEquals(true, telemetryJsons.single().contains(""""observedAtMs":123456"""))
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    123_456L,
                    Simulator.AceWindows,
                    ReadoutItemKey.AceWindows.MyBestLap.Root,
                    "自己ベストラップ更新",
                    NarrationOutcome.SPOKEN,
                    telemetryJsons.single(),
                )
            }
            confirmVerified(telemetryLogRepository)
        }

    private fun bestLapTime(bestLapTimeMs: Int) = AceWindowsBestLapTimeData(bestLapTimeMs = bestLapTimeMs)

    private fun vehicleApproach(distanceMeters: Double) =
        AceWindowsVehicleApproachData(nearbyVehicles = listOf(AceWindowsNearbyVehicleData(distanceMeters)))

    private fun flag(flagType: AceWindowsFlagType) = AceWindowsFlagData(flag = flagType)

    private fun mockTts(spokenTexts: MutableList<SpeechEvent>): TextToSpeechEngine {
        every { ttsEngine.currentReadoutItemKey } returns null
        every { ttsEngine.speak(capture(spokenTexts), capture(mutableListOf<Boolean>())) } just Runs
        every { ttsEngine.stop() } just Runs
        return ttsEngine
    }

    private fun fuel(remainingPercent: Double) = AceWindowsFuelData(remainingPercent = FuelPercent(remainingPercent))

    private fun tyreCarcassTemperature(fl: Float) =
        AceWindowsTyreCarcassTemperatureData(wheels = mapOf(WheelIndex.FRONT_LEFT to CelsiusReading(fl)))
}
