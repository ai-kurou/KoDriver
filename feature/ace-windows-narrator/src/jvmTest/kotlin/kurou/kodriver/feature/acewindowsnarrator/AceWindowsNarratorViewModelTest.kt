@file:Suppress("TooManyFunctions")

package kurou.kodriver.feature.acewindowsnarrator

import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kurou.kodriver.domain.acewindows.model.AceWindowsBestLapTimeData
import kurou.kodriver.domain.acewindows.model.AceWindowsFlagData
import kurou.kodriver.domain.acewindows.model.AceWindowsFlagType
import kurou.kodriver.domain.acewindows.model.AceWindowsFuelData
import kurou.kodriver.domain.acewindows.model.AceWindowsNearbyVehicleData
import kurou.kodriver.domain.acewindows.model.AceWindowsRemainingFuelLapsData
import kurou.kodriver.domain.acewindows.model.AceWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.acewindows.model.AceWindowsVehicleApproachData
import kurou.kodriver.domain.acewindows.repository.AceWindowsBestLapTimeRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsFlagPreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsFlagRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsFuelRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsRemainingFuelLapsPreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsRemainingFuelLapsRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsRemainingFuelPreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsTyreCarcassTemperatureRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsVehicleApproachPreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsVehicleApproachRepository
import kurou.kodriver.domain.acewindows.usecase.AceWindowsVehicleApproachThresholdsUseCases
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsBestLapTimeUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsFlagEnabledStatesUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsFlagUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsFuelUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsRemainingFuelLapsThresholdUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsRemainingFuelLapsUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsRemainingFuelThresholdPercentageUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsTyreCarcassTemperatureUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsTyreTemperatureEnabledStatesUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsVehicleApproachEnabledStatesUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsVehicleApproachUseCase
import kurou.kodriver.domain.engine.AceWindowsBlackFlag
import kurou.kodriver.domain.engine.AceWindowsBlackWhiteFlag
import kurou.kodriver.domain.engine.AceWindowsBlueFlag
import kurou.kodriver.domain.engine.AceWindowsGreenFlag
import kurou.kodriver.domain.engine.AceWindowsManualReadoutText
import kurou.kodriver.domain.engine.AceWindowsMyBestLap
import kurou.kodriver.domain.engine.AceWindowsOrangeCircleFlag
import kurou.kodriver.domain.engine.AceWindowsRedFlag
import kurou.kodriver.domain.engine.AceWindowsRedYellowStripesFlag
import kurou.kodriver.domain.engine.AceWindowsRemainingFuelLapsWarning
import kurou.kodriver.domain.engine.AceWindowsRemainingFuelWarning
import kurou.kodriver.domain.engine.AceWindowsTyreOverheat
import kurou.kodriver.domain.engine.AceWindowsVehicleApproach
import kurou.kodriver.domain.engine.AceWindowsWhiteFlag
import kurou.kodriver.domain.engine.AceWindowsYellowFlag
import kurou.kodriver.domain.engine.DebugReadoutText
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.DebugReadoutRequest
import kurou.kodriver.domain.model.FuelPercent
import kurou.kodriver.domain.model.ManualReadoutRequest
import kurou.kodriver.domain.model.ManualReadoutSet
import kurou.kodriver.domain.model.NarrationOutcome
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.WheelIndex
import kurou.kodriver.domain.repository.AutoReadoutPreferencesRepository
import kurou.kodriver.domain.repository.DebugReadoutRequestRepository
import kurou.kodriver.domain.repository.ManualReadoutHotkeyRepository
import kurou.kodriver.domain.repository.ManualReadoutRequestRepository
import kurou.kodriver.domain.repository.ManualReadoutSetPreferencesRepository
import kurou.kodriver.domain.repository.QueuePreferencesRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.repository.TelemetryLogRepository
import kurou.kodriver.domain.usecase.ObserveAutoReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveAutoReadoutOrderUseCase
import kurou.kodriver.domain.usecase.ObserveDebugReadoutRequestUseCase
import kurou.kodriver.domain.usecase.ObserveManualReadoutHotkeyPressesUseCase
import kurou.kodriver.domain.usecase.ObserveManualReadoutRequestsUseCase
import kurou.kodriver.domain.usecase.ObserveManualReadoutSetsUseCase
import kurou.kodriver.domain.usecase.ObserveQueueEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveResolvedAutoReadoutOrderUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.ResolveAutoReadoutOrderUseCase
import kurou.kodriver.domain.usecase.SaveTelemetryLogUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class AceWindowsNarratorViewModelTest {
    private val manualRequests = MutableSharedFlow<ManualReadoutRequest>()
    private val manualHotkeyPresses = MutableSharedFlow<ManualReadoutRequest>()
    private val manualSets = MutableStateFlow(listOf(ManualReadoutSet("set-1")))
    private val manualRequestRepository: ManualReadoutRequestRepository = mockk()
    private val manualHotkeyRepository: ManualReadoutHotkeyRepository = mockk()
    private val manualSetRepository: ManualReadoutSetPreferencesRepository = mockk()

    private val debugRequests = MutableSharedFlow<DebugReadoutRequest>()
    private val debugRequestRepository: DebugReadoutRequestRepository = mockk()

    private val testDispatcher = UnconfinedTestDispatcher()

    private val bestLapTimeRepository: AceWindowsBestLapTimeRepository = mockk()

    private val fuelRepository: AceWindowsFuelRepository = mockk()

    private val remainingFuelPreferencesRepository: AceWindowsRemainingFuelPreferencesRepository = mockk()

    private val remainingFuelLapsRepository: AceWindowsRemainingFuelLapsRepository = mockk()

    private val remainingFuelLapsPreferencesRepository: AceWindowsRemainingFuelLapsPreferencesRepository = mockk()

    private val simulatorPreferencesRepository: SimulatorPreferencesRepository = mockk()

    private val autoReadoutPreferencesRepository: AutoReadoutPreferencesRepository = mockk()

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
        every { debugRequestRepository.observeRequests() } returns debugRequests
        every { manualRequestRepository.observeRequests() } returns manualRequests
        every { manualHotkeyRepository.pressStream() } returns manualHotkeyPresses
        every { manualSetRepository.observeSets() } returns manualSets
        return AceWindowsNarratorViewModel(
            manualReadoutUseCases =
                ManualReadoutUseCases(
                    ObserveManualReadoutRequestsUseCase(manualRequestRepository),
                    ObserveManualReadoutHotkeyPressesUseCase(manualHotkeyRepository),
                    ObserveManualReadoutSetsUseCase(manualSetRepository),
                ),
            observeDebugReadoutRequest = ObserveDebugReadoutRequestUseCase(debugRequestRepository),
            myBestLapUseCases =
                MyBestLapUseCases(
                    observeBestLapTime = ObserveAceWindowsBestLapTimeUseCase(bestLapTimeRepository),
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
                    observeAutoReadoutEnabledStates =
                        ObserveAutoReadoutEnabledStatesUseCase(autoReadoutPreferencesRepository),
                    observeAutoReadoutOrder =
                        ObserveResolvedAutoReadoutOrderUseCase(
                            ObserveAutoReadoutOrderUseCase(autoReadoutPreferencesRepository),
                            ResolveAutoReadoutOrderUseCase(),
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
    fun `一覧の依頼はキュー設定が無効でも項目なし文言を末尾に追加してログ保存する`() =
        runTest(testDispatcher) {
            assertManualReadout(hotkey = false)
        }

    @Test
    fun `ホットキーの依頼も項目なし文言を末尾に追加してログ保存する`() =
        runTest(testDispatcher) {
            assertManualReadout(hotkey = true)
        }

    @Test
    fun `手動燃料は受信から5秒以内なら現在値を読み上げる`() =
        runTest(testDispatcher) {
            assertManualReadout(hotkey = false, fuelAgeMs = 5_000L, expectedText = "燃料は残り43パーセント")
        }

    @Test
    fun `手動燃料は5秒を超えたらデータなしを読み上げる`() =
        runTest(testDispatcher) {
            assertManualReadout(hotkey = false, fuelAgeMs = 5_001L, expectedText = "残り燃料はデータがありません")
        }

    @Test
    fun `手動燃料は未受信ならデータなしを読み上げる`() =
        runTest(testDispatcher) {
            assertManualReadout(hotkey = true, expectedText = "残り燃料はデータがありません")
        }

    @Test
    fun `手動燃料は保存順に編集文言を連結する`() =
        runTest(testDispatcher) {
            assertManualReadout(hotkey = false, fuelAgeMs = 0L, expectedText = "後43、後43", concatenate = true)
        }

    @Test
    fun `シミュレーターを切り替えると手動燃料の現在値をクリアする`() =
        runTest(testDispatcher) {
            assertManualReadout(hotkey = false, fuelAgeMs = 0L, expectedText = "残り燃料はデータがありません", switchSimulator = true)
        }

    @Test
    fun `手動燃料周回数は受信から5秒以内なら切り捨てた周回数を読み上げる`() =
        runTest(testDispatcher) {
            assertManualReadout(
                hotkey = false,
                lapsAgeMs = 5_000L,
                expectedText = "燃料は残り約5周",
                itemKeys = listOf(AceWindowsReadoutItemKey.RemainingFuelLaps.Root.value),
            )
        }

    @Test
    fun `手動燃料周回数は5秒を超えたらデータなしを読み上げる`() =
        runTest(testDispatcher) {
            assertManualReadout(
                hotkey = false,
                lapsAgeMs = 5_001L,
                expectedText = "残り燃料の周回数はデータがありません",
                itemKeys = listOf(AceWindowsReadoutItemKey.RemainingFuelLaps.Root.value),
            )
        }

    @Test
    fun `手動燃料周回数は未受信ならデータなしを読み上げる`() =
        runTest(testDispatcher) {
            assertManualReadout(
                hotkey = true,
                expectedText = "残り燃料の周回数はデータがありません",
                itemKeys = listOf(AceWindowsReadoutItemKey.RemainingFuelLaps.Root.value),
            )
        }

    @Test
    fun `シミュレーターを切り替えると手動燃料周回数の現在値をクリアする`() =
        runTest(testDispatcher) {
            assertManualReadout(
                hotkey = false,
                lapsAgeMs = 0L,
                expectedText = "残り燃料の周回数はデータがありません",
                switchSimulator = true,
                itemKeys = listOf(AceWindowsReadoutItemKey.RemainingFuelLaps.Root.value),
            )
        }

    @Test
    fun `手動自己ベストラップは受信から5秒以内ならラップタイムを読み上げる`() =
        runTest(testDispatcher) {
            assertManualReadout(
                hotkey = false,
                lapsAgeMs = 5_000L,
                expectedText = "自己ベストラップ 1分23秒456",
                itemKeys = listOf(AceWindowsReadoutItemKey.MyBestLap.Root.value),
            )
        }

    @Test
    fun `手動自己ベストラップは5秒を超えても最後に受信した値を読み上げる`() =
        runTest(testDispatcher) {
            assertManualReadout(
                hotkey = false,
                lapsAgeMs = 60_000L,
                expectedText = "自己ベストラップ 1分23秒456",
                itemKeys = listOf(AceWindowsReadoutItemKey.MyBestLap.Root.value),
            )
        }

    @Test
    fun `シミュレーターを切り替えると手動自己ベストラップの現在値をクリアする`() =
        runTest(testDispatcher) {
            assertManualReadout(
                hotkey = true,
                lapsAgeMs = 0L,
                expectedText = "自己ベストラップはデータがありません",
                switchSimulator = true,
                itemKeys = listOf(AceWindowsReadoutItemKey.MyBestLap.Root.value),
            )
        }

    private suspend fun TestScope.assertManualReadout(
        hotkey: Boolean,
        fuelAgeMs: Long? = null,
        expectedText: String = "読み上げる項目がありません",
        concatenate: Boolean = false,
        switchSimulator: Boolean = false,
        lapsAgeMs: Long? = null,
        itemKeys: List<String>? = null,
    ) {
        val key = AceWindowsReadoutItemKey.RemainingFuel.Root
        val event = AceWindowsManualReadoutText(key, expectedText, expectedText)
        val items = itemKeys ?: if (concatenate) listOf(key.value, key.value) else listOf(key.value)
        if (expectedText != "読み上げる項目がありません") {
            manualSets.update { listOf(ManualReadoutSet("set-1", items)) }
        }
        if (concatenate) {
            manualSets.update {
                listOf(ManualReadoutSet("set-1", items, mapOf(key.value to "後{percent}")))
            }
        }
        val json = slot<String>()
        stubReadoutDefaults(thresholdPercentage = 30, queueEnabledOverrides = mapOf(key to false))
        every { ttsEngine.speak(event, true) } just Runs
        coEvery {
            telemetryLogRepository.saveTelemetryLog(
                100L,
                Simulator.AceWindows,
                key,
                event.text,
                NarrationOutcome.QUEUED,
                capture(json),
            )
        } just Runs
        val selected = MutableStateFlow<Simulator>(Simulator.AceWindows)
        every { simulatorPreferencesRepository.selectedSimulator() } returns selected
        every { autoReadoutPreferencesRepository.observeAutoReadoutEnabledStates(Simulator.Gt7Ps5.id) } returns
            MutableStateFlow(emptyMap())
        every { autoReadoutPreferencesRepository.observeAutoReadoutOrder(Simulator.Gt7Ps5.id) } returns
            MutableStateFlow(emptyList())
        var nowMs = 100L - (fuelAgeMs ?: 0L)
        val fuelChannel = Channel<AceWindowsFuelData>(Channel.UNLIMITED)
        val lapsChannel = Channel<AceWindowsRemainingFuelLapsData>(Channel.UNLIMITED)
        val bestLapChannel = Channel<AceWindowsBestLapTimeData>(Channel.UNLIMITED)
        val viewModel =
            createViewModel(
                fuelChannel,
                ttsEngine,
                remainingFuelLapsChannel = lapsChannel,
                bestLapTimeChannel = bestLapChannel,
                currentTimeMs = { nowMs },
            )
        try {
            val request = ManualReadoutRequest(Simulator.AceWindows.id, "set-1")
            runCurrent()
            if (fuelAgeMs != null) {
                fuelChannel.send(AceWindowsFuelData(FuelPercent(42.5)))
                runCurrent()
            }
            if (lapsAgeMs != null) {
                nowMs = 100L - lapsAgeMs
                if (AceWindowsReadoutItemKey.MyBestLap.Root.value in items) {
                    bestLapChannel.send(AceWindowsBestLapTimeData(83_456))
                } else {
                    lapsChannel.send(AceWindowsRemainingFuelLapsData(remainingLaps = 5.9f))
                }
                runCurrent()
            }
            nowMs = 100L
            if (switchSimulator) {
                selected.update { Simulator.Gt7Ps5 }
                runCurrent()
                selected.update { Simulator.AceWindows }
                runCurrent()
            }
            if (hotkey) {
                manualHotkeyPresses.emit(request)
            } else {
                manualRequests.emit(request)
            }
            runCurrent()
            verifyManualReadout(event, json.captured)
        } finally {
            viewModel.viewModelScope.cancel()
        }
    }

    private suspend fun verifyManualReadout(
        event: AceWindowsManualReadoutText,
        json: String,
    ) {
        val logged = Json.parseToJsonElement(json).jsonObject
        assertEquals(setOf("state", "settings", "observedAtMs"), logged.keys)
        assertEquals("100", logged.getValue("observedAtMs").jsonPrimitive.content)
        verify(exactly = 1) { ttsEngine.speak(event, true) }
        coVerify(exactly = 1) {
            telemetryLogRepository.saveTelemetryLog(
                100L,
                Simulator.AceWindows,
                event.readoutItemKey,
                event.text,
                NarrationOutcome.QUEUED,
                json,
            )
        }
        verify(exactly = 1) { manualRequestRepository.observeRequests() }
        verify(exactly = 1) { manualHotkeyRepository.pressStream() }
        verify(exactly = 1) { manualSetRepository.observeSets() }
        confirmVerified(
            ttsEngine,
            telemetryLogRepository,
            manualRequestRepository,
            manualHotkeyRepository,
            manualSetRepository,
        )
    }

    @Test
    fun `別シミュレーター宛てとセットなしとACE未選択の依頼は読み上げない`() =
        runTest(testDispatcher) {
            val selected = MutableStateFlow<Simulator>(Simulator.AceWindows)
            stubReadoutDefaults(thresholdPercentage = 30)
            every { simulatorPreferencesRepository.selectedSimulator() } returns selected
            every {
                autoReadoutPreferencesRepository.observeAutoReadoutEnabledStates(Simulator.LmuWindows.id)
            } returns MutableStateFlow(emptyMap())
            every {
                autoReadoutPreferencesRepository.observeAutoReadoutOrder(Simulator.LmuWindows.id)
            } returns MutableStateFlow(emptyList())
            val viewModel = createViewModel(Channel(Channel.UNLIMITED), ttsEngine)
            try {
                runCurrent()
                manualRequests.emit(ManualReadoutRequest(Simulator.LmuWindows.id, "set-1"))
                manualRequests.emit(ManualReadoutRequest(Simulator.AceWindows.id, "missing"))
                runCurrent()
                selected.value = Simulator.LmuWindows
                runCurrent()
                manualRequests.emit(ManualReadoutRequest(Simulator.AceWindows.id, "set-1"))
                manualHotkeyPresses.emit(ManualReadoutRequest(Simulator.LmuWindows.id, "set-1"))
                manualHotkeyPresses.emit(ManualReadoutRequest(Simulator.AceWindows.id, "missing"))
                manualHotkeyPresses.emit(ManualReadoutRequest(Simulator.AceWindows.id, "set-1"))
                runCurrent()
                verify(exactly = 1) { manualRequestRepository.observeRequests() }
                verify(exactly = 1) { manualHotkeyRepository.pressStream() }
                verify(exactly = 4) { manualSetRepository.observeSets() }
                confirmVerified(
                    ttsEngine,
                    telemetryLogRepository,
                    manualRequestRepository,
                    manualHotkeyRepository,
                    manualSetRepository,
                )
            } finally {
                viewModel.viewModelScope.cancel()
            }
        }

    @Test
    fun `自由文言の解決に失敗しても次のテレメトリを収集して読み上げる`() =
        runTest(testDispatcher) {
            val channel = Channel<AceWindowsFlagData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            val key = AceWindowsReadoutItemKey.Flag.Root
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
                    if (it == AceWindowsBlueFlag()) error("preference error")
                    "復旧"
                },
            )

            channel.send(flag(AceWindowsFlagType.NO_FLAG))
            channel.send(flag(AceWindowsFlagType.BLUE_FLAG))
            channel.send(flag(AceWindowsFlagType.RED_FLAG))

            assertEquals(listOf<SpeechEvent>(AceWindowsRedFlag("復旧")), spokenTexts)
            verify(exactly = 1) { tts.currentReadoutItemKey }
            verify(exactly = 1) { tts.speak(AceWindowsRedFlag("復旧"), false) }
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
            createViewModel(fuelChannel = channel, ttsEngine = ttsEngine, readoutText = { "残り20%" })

            channel.send(fuel(50.0))
            channel.send(fuel(20.0))

            assertEquals(
                listOf<SpeechEvent>(AceWindowsRemainingFuelWarning(20, "残り20%")),
                spokenTexts,
            )
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
                    AceWindowsRemainingFuelWarning(20, "燃料は残り20パーセント"),
                    AceWindowsRemainingFuelWarning(20, "燃料は残り20パーセント"),
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
                    AceWindowsReadoutItemKey.RemainingFuel.Root,
                    "燃料は残り20パーセント",
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
                    AceWindowsReadoutItemKey.RemainingFuel.Root,
                    "燃料は残り20パーセント",
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
                enabledOverrides = mapOf(AceWindowsReadoutItemKey.RemainingFuel.Root to false),
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
                    AceWindowsRemainingFuelLapsWarning(2, "燃料は残り約2周"),
                    AceWindowsRemainingFuelLapsWarning(1, "燃料は残り約1周"),
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
                    AceWindowsReadoutItemKey.RemainingFuelLaps.Root,
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
                    AceWindowsReadoutItemKey.RemainingFuelLaps.Root,
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
                enabledOverrides = mapOf(AceWindowsReadoutItemKey.RemainingFuelLaps.Root to false),
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
                AceWindowsReadoutItemKey.RemainingFuelLaps.Root,
                "燃料は残り約3周",
                NarrationOutcome.SPOKEN,
                "{}",
            )
        } just Runs
    }

    /**
     * simulator/enabledStates/autoReadoutOrder/thresholdの標準スタブをまとめて設定する。
     * ViewModelがコンストラクタ内で即座にFlowを購読・combineするため、必ず [createViewModel] の前に呼ぶこと。
     */
    @Suppress("LongParameterList")
    private fun stubReadoutDefaults(
        thresholdPercentage: Int,
        enabledOverrides: Map<ReadoutItemKey, Boolean> = emptyMap(),
        orderOverride: List<ReadoutItemKey> = listOf(AceWindowsReadoutItemKey.RemainingFuel.Root),
        flagEnabledOverrides: Map<ReadoutItemKey, Boolean> = emptyMap(),
        tyreTemperatureHighThresholdCelsius: Int = 90,
        tyreTemperatureEnabledOverrides: Map<ReadoutItemKey, Boolean> = emptyMap(),
        vehicleApproachEnabledOverrides: Map<ReadoutItemKey, Boolean> =
            mapOf(
                AceWindowsReadoutItemKey.VehicleApproach.Root to true,
                AceWindowsReadoutItemKey.VehicleApproach.StartReadout to true,
            ),
        vehicleApproachThresholdMeters: Double = 10.0,
        remainingFuelLapsThreshold: Int = 3,
        selectedSimulator: Simulator = Simulator.AceWindows,
        queueEnabledOverrides: Map<ReadoutItemKey, Boolean> = emptyMap(),
    ) {
        every { simulatorPreferencesRepository.selectedSimulator() } returns MutableStateFlow(selectedSimulator)
        every {
            autoReadoutPreferencesRepository.observeAutoReadoutEnabledStates(selectedSimulator.id)
        } returns MutableStateFlow(enabledOverrides)
        every {
            autoReadoutPreferencesRepository.observeAutoReadoutOrder(selectedSimulator.id)
        } returns MutableStateFlow(orderOverride)
        every {
            remainingFuelPreferencesRepository.observeThresholdPercentage()
        } returns MutableStateFlow(thresholdPercentage)
        every {
            remainingFuelLapsPreferencesRepository.observeThresholdLaps()
        } returns MutableStateFlow(remainingFuelLapsThreshold)
        every {
            queuePreferencesRepository.observeQueueEnabledStates()
        } returns MutableStateFlow(queueEnabledOverrides)
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
                AceWindowsReadoutItemKey.RemainingFuel.Root,
                "燃料は残り20パーセント",
                NarrationOutcome.SPOKEN,
                "{}",
            )
        } just Runs
    }

    @Test
    fun `ACE以外のシミュレーターを選択中は各テレメトリを読み上げない`() =
        runTest(testDispatcher) {
            val fuelChannel = Channel<AceWindowsFuelData>(Channel.UNLIMITED)
            val flagChannel = Channel<AceWindowsFlagData>(Channel.UNLIMITED)
            val tyreCarcassTemperatureChannel = Channel<AceWindowsTyreCarcassTemperatureData>(Channel.UNLIMITED)
            val vehicleApproachChannel = Channel<AceWindowsVehicleApproachData>(Channel.UNLIMITED)
            val bestLapTimeChannel = Channel<AceWindowsBestLapTimeData>(Channel.UNLIMITED)
            val remainingFuelLapsChannel = Channel<AceWindowsRemainingFuelLapsData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val ttsEngine = mockTts(spokenTexts)
            stubReadoutDefaults(
                thresholdPercentage = 30,
                enabledOverrides = mapOf(AceWindowsReadoutItemKey.MyBestLap.Root to true),
                remainingFuelLapsThreshold = 2,
                selectedSimulator = Simulator.LmuWindows,
            )
            createViewModel(
                fuelChannel = fuelChannel,
                ttsEngine = ttsEngine,
                flagChannel = flagChannel,
                tyreCarcassTemperatureChannel = tyreCarcassTemperatureChannel,
                vehicleApproachChannel = vehicleApproachChannel,
                bestLapTimeChannel = bestLapTimeChannel,
                remainingFuelLapsChannel = remainingFuelLapsChannel,
            )

            fuelChannel.send(fuel(50.0))
            fuelChannel.send(fuel(20.0))
            flagChannel.send(flag(AceWindowsFlagType.NO_FLAG))
            flagChannel.send(flag(AceWindowsFlagType.WHITE_FLAG))
            tyreCarcassTemperatureChannel.send(tyreCarcassTemperature(fl = 85.0f))
            tyreCarcassTemperatureChannel.send(tyreCarcassTemperature(fl = 95.0f))
            vehicleApproachChannel.send(vehicleApproach(distanceMeters = 5.0))
            bestLapTimeChannel.send(bestLapTime(90_000))
            bestLapTimeChannel.send(bestLapTime(89_000))
            remainingFuelLapsChannel.send(AceWindowsRemainingFuelLapsData(remainingLaps = 3.5f))
            remainingFuelLapsChannel.send(AceWindowsRemainingFuelLapsData(remainingLaps = 2.5f))

            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
            verify(exactly = 0) {
                ttsEngine.speak(AceWindowsRemainingFuelWarning(20, "燃料は残り20パーセント"), false)
                ttsEngine.speak(AceWindowsWhiteFlag("ホワイトフラッグ"), false)
                ttsEngine.speak(AceWindowsTyreOverheat(95, "タイヤ過熱 95度"), false)
                ttsEngine.speak(AceWindowsVehicleApproach("車両接近"), false)
                ttsEngine.speak(AceWindowsMyBestLap(89_000, "自己ベストラップ更新 1分29秒000"), false)
                ttsEngine.speak(AceWindowsRemainingFuelLapsWarning(2, "燃料は残り約2周"), false)
            }
            confirmVerified(ttsEngine)
        }

    @Test
    fun `フラッグが変化すると読み上げる`() =
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

            assertEquals<List<SpeechEvent>>(
                listOf(
                    AceWindowsWhiteFlag("ホワイトフラッグ"),
                    AceWindowsGreenFlag("グリーンフラッグ"),
                    AceWindowsRedFlag("レッドフラッグ"),
                    AceWindowsBlueFlag("ブルーフラッグ"),
                    AceWindowsYellowFlag("イエローフラッグ"),
                    AceWindowsBlackFlag("ブラックフラッグ"),
                    AceWindowsBlackWhiteFlag("ブラック・ホワイトフラッグ"),
                    AceWindowsOrangeCircleFlag("オレンジボールフラッグ、車両に不具合があります"),
                    AceWindowsRedYellowStripesFlag("レッド・イエローストライプフラッグ、路面が滑りやすいです"),
                ),
                spokenTexts,
            )
        }

    @Test
    fun `フラッグ項目が無効のときは読み上げない`() =
        runTest(testDispatcher) {
            val fuelChannel = Channel<AceWindowsFuelData>(Channel.UNLIMITED)
            val flagChannel = Channel<AceWindowsFlagData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val ttsEngine = mockTts(spokenTexts)
            stubReadoutDefaults(
                thresholdPercentage = 30,
                enabledOverrides = mapOf(AceWindowsReadoutItemKey.Flag.Root to false),
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
    fun `個別のフラッグ項目が無効のときは読み上げない`() =
        runTest(testDispatcher) {
            val fuelChannel = Channel<AceWindowsFuelData>(Channel.UNLIMITED)
            val flagChannel = Channel<AceWindowsFlagData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val ttsEngine = mockTts(spokenTexts)
            stubReadoutDefaults(
                thresholdPercentage = 30,
                flagEnabledOverrides =
                    mapOf(
                        AceWindowsReadoutItemKey.Flag.WhiteFlag to false,
                        AceWindowsReadoutItemKey.Flag.GreenFlag to false,
                        AceWindowsReadoutItemKey.Flag.RedFlag to false,
                        AceWindowsReadoutItemKey.Flag.BlueFlag to false,
                        AceWindowsReadoutItemKey.Flag.YellowFlag to false,
                        AceWindowsReadoutItemKey.Flag.BlackFlag to false,
                        AceWindowsReadoutItemKey.Flag.BlackWhiteFlag to false,
                        AceWindowsReadoutItemKey.Flag.OrangeCircleFlag to false,
                        AceWindowsReadoutItemKey.Flag.RedYellowStripesFlag to false,
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

            assertEquals(listOf<SpeechEvent>(AceWindowsTyreOverheat(95, "過熱注意 95度")), spokenTexts)
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
                enabledOverrides = mapOf(AceWindowsReadoutItemKey.TyreTemperature.Root to false),
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
                    mapOf(AceWindowsReadoutItemKey.TyreTemperature.OverheatWarning to false),
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
                    AceWindowsReadoutItemKey.TyreTemperature.Root,
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
                    AceWindowsReadoutItemKey.TyreTemperature.Root,
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
                orderOverride = listOf(AceWindowsReadoutItemKey.VehicleApproach.Root),
            )
            createViewModel(
                fuelChannel = fuelChannel,
                ttsEngine = ttsEngine,
                vehicleApproachChannel = vehicleApproachChannel,
            )

            vehicleApproachChannel.send(vehicleApproach(distanceMeters = 5.0))

            assertEquals(listOf<SpeechEvent>(AceWindowsVehicleApproach("車両接近")), spokenTexts)
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
                orderOverride = listOf(AceWindowsReadoutItemKey.VehicleApproach.Root),
            )
            val json = slot<String>()
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.AceWindows,
                    AceWindowsReadoutItemKey.VehicleApproach.Root,
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

            assertEquals(listOf<SpeechEvent>(AceWindowsVehicleApproach("周囲に注意")), spokenTexts)
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(AceWindowsVehicleApproach("周囲に注意"), false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.AceWindows,
                    AceWindowsReadoutItemKey.VehicleApproach.Root,
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
                orderOverride = listOf(AceWindowsReadoutItemKey.VehicleApproach.Root),
            )
            val json = slot<String>()
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.AceWindows,
                    AceWindowsReadoutItemKey.VehicleApproach.Root,
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
            verify(exactly = 0) { ttsEngine.speak(AceWindowsVehicleApproach("車両接近"), false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.AceWindows,
                    AceWindowsReadoutItemKey.VehicleApproach.Root,
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
                vehicleApproachEnabledOverrides = mapOf(AceWindowsReadoutItemKey.VehicleApproach.Root to false),
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
                        AceWindowsReadoutItemKey.VehicleApproach.Root to true,
                        AceWindowsReadoutItemKey.VehicleApproach.StartReadout to false,
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

            assertEquals(listOf<SpeechEvent>(AceWindowsVehicleApproach("車両接近")), spokenTexts)
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
                listOf<SpeechEvent>(
                    AceWindowsVehicleApproach("車両接近"),
                    AceWindowsVehicleApproach("車両接近"),
                ),
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
                orderOverride = listOf(AceWindowsReadoutItemKey.VehicleApproach.Root),
            )
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    123_456L,
                    Simulator.AceWindows,
                    AceWindowsReadoutItemKey.VehicleApproach.Root,
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
                    AceWindowsReadoutItemKey.VehicleApproach.Root,
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
                enabledOverrides = mapOf(AceWindowsReadoutItemKey.MyBestLap.Root to true),
                orderOverride = listOf(AceWindowsReadoutItemKey.MyBestLap.Root),
            )
            createViewModel(
                fuelChannel = fuelChannel,
                ttsEngine = ttsEngine,
                bestLapTimeChannel = bestLapTimeChannel,
            )

            bestLapTimeChannel.send(bestLapTime(90_000))
            bestLapTimeChannel.send(bestLapTime(89_000))

            assertEquals(
                listOf<SpeechEvent>(AceWindowsMyBestLap(89_000, "自己ベストラップ更新 1分29秒000")),
                spokenTexts,
            )
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
                enabledOverrides = mapOf(AceWindowsReadoutItemKey.MyBestLap.Root to false),
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
                enabledOverrides = mapOf(AceWindowsReadoutItemKey.MyBestLap.Root to true),
                orderOverride = listOf(AceWindowsReadoutItemKey.MyBestLap.Root),
            )
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    123_456L,
                    Simulator.AceWindows,
                    AceWindowsReadoutItemKey.MyBestLap.Root,
                    "自己ベストラップ更新 1分29秒000",
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
                    AceWindowsReadoutItemKey.MyBestLap.Root,
                    "自己ベストラップ更新 1分29秒000",
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

    @Test
    fun `デバッグ要求は未受信かつ項目無効でも読み上げ他シミュレーターと空白は読み上げない`() =
        runTest(testDispatcher) {
            val key = ReadoutItemKey.topLevelEntriesOf(Simulator.AceWindows).first()
            val request = DebugReadoutRequest(Simulator.AceWindows, key, "入力 {laps}")
            val spokenJson = slot<String>()
            val skippedJson = slot<String>()
            stubReadoutDefaults(
                thresholdPercentage = 30,
                enabledOverrides = mapOf(key to false),
                vehicleApproachEnabledOverrides = mapOf(key to false),
            )
            every { ttsEngine.currentReadoutItemKey } returns null
            every { ttsEngine.speak(DebugReadoutText(key, request.text), false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    123L,
                    Simulator.AceWindows,
                    key,
                    request.text,
                    NarrationOutcome.SPOKEN,
                    capture(spokenJson),
                )
            } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    123L,
                    Simulator.AceWindows,
                    key,
                    "",
                    NarrationOutcome.SKIPPED,
                    capture(skippedJson),
                )
            } just Runs
            val viewModel =
                createViewModel(
                    fuelChannel = Channel(Channel.UNLIMITED),
                    ttsEngine = ttsEngine,
                    currentTimeMs = { 123L },
                )
            try {
                Simulator.entries.filter { it != Simulator.AceWindows }.forEach { simulator ->
                    debugRequests.emit(request.copy(simulator = simulator))
                }
                debugRequests.emit(request)
                debugRequests.emit(request.copy(text = " \n\t "))
                val json = Json.parseToJsonElement(spokenJson.captured).jsonObject
                assertEquals("true", json.getValue("debugReadout").jsonPrimitive.content)
                assertEquals("123", json.getValue("observedAtMs").jsonPrimitive.content)
                assertEquals(true, json.containsKey("state"))
                assertEquals(true, json.containsKey("settings"))
                assertEquals(setOf("state", "settings", "observedAtMs", "debugReadout"), json.keys)
                assertEquals(spokenJson.captured, skippedJson.captured)
                verify(exactly = 1) { debugRequestRepository.observeRequests() }
                verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
                verify(exactly = 1) { ttsEngine.speak(DebugReadoutText(key, request.text), false) }
                verify(exactly = 0) { ttsEngine.speak(DebugReadoutText(key, " \n\t "), false) }
                coVerify(exactly = 1) {
                    telemetryLogRepository.saveTelemetryLog(
                        123L,
                        Simulator.AceWindows,
                        key,
                        request.text,
                        NarrationOutcome.SPOKEN,
                        spokenJson.captured,
                    )
                }
                coVerify(exactly = 1) {
                    telemetryLogRepository.saveTelemetryLog(
                        123L,
                        Simulator.AceWindows,
                        key,
                        "",
                        NarrationOutcome.SKIPPED,
                        skippedJson.captured,
                    )
                }
                confirmVerified(debugRequestRepository, ttsEngine, telemetryLogRepository)
            } finally {
                viewModel.viewModelScope.cancel()
            }
        }
}
