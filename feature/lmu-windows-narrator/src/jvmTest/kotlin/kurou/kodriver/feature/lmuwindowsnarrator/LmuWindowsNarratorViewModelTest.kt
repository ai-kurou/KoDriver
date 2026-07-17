@file:Suppress("FunctionNaming", "TooManyFunctions", "LargeClass")

package kurou.kodriver.feature.lmuwindowsnarrator

import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
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
import kurou.kodriver.domain.model.CountLapFlag
import kurou.kodriver.domain.model.LmuWindowsEngineData
import kurou.kodriver.domain.model.LmuWindowsFuelData
import kurou.kodriver.domain.model.LmuWindowsInputsData
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsTimingData
import kurou.kodriver.domain.model.LmuWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.model.LmuWindowsTyreData
import kurou.kodriver.domain.model.LmuWindowsVehicleApproachData
import kurou.kodriver.domain.model.LmuWindowsVehicleDamageData
import kurou.kodriver.domain.model.LmuWindowsVehicleData
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kurou.kodriver.domain.model.PrimaryFlag
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.RedFlagVoiceType
import kurou.kodriver.domain.model.SectorFlagState
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.SessionYellowFlagState
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.TelemetryLog
import kurou.kodriver.domain.model.VehicleApproachStartReadoutType
import kurou.kodriver.domain.model.VehicleApproachSustainedReadoutType
import kurou.kodriver.domain.model.WheelIndex
import kurou.kodriver.domain.repository.LmuWindowsFlagPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsFlagRepository
import kurou.kodriver.domain.repository.LmuWindowsMyBestLapPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsRedFlagPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsRepository
import kurou.kodriver.domain.repository.LmuWindowsTyreCarcassTemperatureRepository
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachThresholdsPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleDamagePreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleDamageRepository
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.repository.TelemetryLogRepository
import kurou.kodriver.domain.usecase.DetermineLmuWindowsNarratorReadoutUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsFlagEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsMyBestLapVoiceTypeUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRaceFlagsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRedFlagVoiceTypeUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreCarcassTemperatureUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureLowWarningPhasesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachSkipFirstLapUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachStartReadoutTypeUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachSustainedDurationUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachSustainedReadoutTypeUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleDamageEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleDamageUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.SaveTelemetryLogUseCase
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsNarratorViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @MockK
    private lateinit var vehicleApproachRepository: LmuWindowsVehicleApproachRepository

    @MockK
    private lateinit var lmuWindowsRepository: LmuWindowsRepository

    @MockK
    private lateinit var vehicleApproachPreferencesRepository: LmuWindowsVehicleApproachPreferencesRepository

    @MockK
    private lateinit var vehicleApproachThresholdsPreferencesRepository:
        LmuWindowsVehicleApproachThresholdsPreferencesRepository

    @MockK
    private lateinit var vehicleDamageRepository: LmuWindowsVehicleDamageRepository

    @MockK
    private lateinit var vehicleDamagePreferencesRepository: LmuWindowsVehicleDamagePreferencesRepository

    @MockK
    private lateinit var simulatorPreferencesRepository: SimulatorPreferencesRepository

    @MockK
    private lateinit var readoutPreferencesRepository: ReadoutPreferencesRepository

    @MockK
    private lateinit var flagRepository: LmuWindowsFlagRepository

    @MockK
    private lateinit var flagPreferencesRepository: LmuWindowsFlagPreferencesRepository

    @MockK
    private lateinit var tyreCarcassTemperatureRepository: LmuWindowsTyreCarcassTemperatureRepository

    @MockK
    private lateinit var tyreTemperaturePreferencesRepository: LmuWindowsTyreTemperaturePreferencesRepository

    @MockK
    private lateinit var myBestLapPreferencesRepository: LmuWindowsMyBestLapPreferencesRepository

    @MockK
    private lateinit var redFlagPreferencesRepository: LmuWindowsRedFlagPreferencesRepository

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

    @Suppress("LongParameterList")
    private fun createViewModel(
        vehicleApproachChannel: Channel<LmuWindowsVehicleApproachData> = Channel(Channel.UNLIMITED),
        flagChannel: Channel<LmuWindowsRaceFlagsData> = Channel(Channel.UNLIMITED),
        damageChannel: Channel<LmuWindowsVehicleDamageData> = Channel(Channel.UNLIMITED),
        telemetryChannel: Channel<LmuWindowsTelemetryData> = Channel(Channel.UNLIMITED),
        tyreTemperatureChannel: Channel<LmuWindowsTyreCarcassTemperatureData> = Channel(Channel.UNLIMITED),
        ttsEngine: TextToSpeechEngine,
        enabledOverrides: Map<ReadoutItemKey, Boolean> = emptyMap(),
        flagEnabledOverrides: Map<ReadoutItemKey, Boolean> = emptyMap(),
        vehicleDamageEnabledOverrides: Map<ReadoutItemKey, Boolean> = emptyMap(),
        orderOverride: List<ReadoutItemKey> = listOf(
            ReadoutItemKey.LmuWindows.Flag.Root,
            ReadoutItemKey.LmuWindows.VehicleApproach.Root,
        ),
        voiceType: MyBestLapVoiceType = MyBestLapVoiceType.FORMAL,
        redFlagVoiceType: RedFlagVoiceType = RedFlagVoiceType.SESSION_STOP,
        skipFirstLap: Boolean = false,
        startReadoutEnabled: Boolean = true,
        startReadoutType: VehicleApproachStartReadoutType = VehicleApproachStartReadoutType.CAR_LEFT_RIGHT,
        sustainedReadoutEnabled: Boolean = true,
        sustainedReadoutType: VehicleApproachSustainedReadoutType = VehicleApproachSustainedReadoutType.KEEP_LEFT_RIGHT,
        sustainedApproachDurationSeconds: Int = 7,
        tyreTemperatureHighThreshold: Int = 90,
        tyreTemperatureOverheatWarningEnabled: Boolean = true,
        tyreTemperatureLowWarningEnabled: Boolean = true,
        tyreTemperatureLowWarningPhasesOverride: Map<SessionPhase, Boolean> = emptyMap(),
        simulator: Simulator? = Simulator.LmuWindows,
        currentTimeMs: () -> Long = { 0L },
    ): LmuWindowsNarratorViewModel {
        every { vehicleApproachRepository.vehicleApproachStream() } returns vehicleApproachChannel.receiveAsFlow()
        every { lmuWindowsRepository.telemetryStream() } returns telemetryChannel.receiveAsFlow()
        every { vehicleApproachPreferencesRepository.observeSkipFirstLap() } returns MutableStateFlow(skipFirstLap)
        every { vehicleApproachPreferencesRepository.observeEnabledStates() } returns MutableStateFlow(
            mapOf(
                ReadoutItemKey.LmuWindows.VehicleApproach.StartReadout to startReadoutEnabled,
                ReadoutItemKey.LmuWindows.VehicleApproach.Sustained to sustainedReadoutEnabled,
            ),
        )
        every { vehicleApproachPreferencesRepository.observeStartReadoutType() } returns
            MutableStateFlow(startReadoutType)
        every { vehicleApproachPreferencesRepository.observeSustainedReadoutType() } returns
            MutableStateFlow(sustainedReadoutType)
        every { vehicleApproachThresholdsPreferencesRepository.observeSustainedApproachDurationSeconds() } returns
            MutableStateFlow(sustainedApproachDurationSeconds)
        every { vehicleDamageRepository.vehicleDamageStream() } returns damageChannel.receiveAsFlow()
        every { vehicleDamagePreferencesRepository.observeEnabledStates() } returns
            MutableStateFlow(vehicleDamageEnabledOverrides)
        every { simulatorPreferencesRepository.selectedSimulator() } returns MutableStateFlow(simulator)
        every { readoutPreferencesRepository.observeReadoutEnabledStates(any()) } returns
            MutableStateFlow(enabledOverrides)
        every { readoutPreferencesRepository.observeReadoutOrder(any()) } returns MutableStateFlow(orderOverride)
        every { flagRepository.flagStream() } returns flagChannel.receiveAsFlow()
        every { flagPreferencesRepository.observeFlagEnabledStates() } returns MutableStateFlow(flagEnabledOverrides)
        every { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
            tyreTemperatureChannel.receiveAsFlow()
        every { tyreTemperaturePreferencesRepository.observeHighThresholdCelsius() } returns
            MutableStateFlow(tyreTemperatureHighThreshold)
        every { tyreTemperaturePreferencesRepository.observeEnabledStates() } returns MutableStateFlow(
            mapOf(
                ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning to tyreTemperatureOverheatWarningEnabled,
                ReadoutItemKey.LmuWindows.TyreTemperature.LowWarning to tyreTemperatureLowWarningEnabled,
            ),
        )
        every { tyreTemperaturePreferencesRepository.observeLowWarningPhases() } returns
            MutableStateFlow(tyreTemperatureLowWarningPhasesOverride)
        every { myBestLapPreferencesRepository.observeVoiceType() } returns MutableStateFlow(voiceType)
        every { redFlagPreferencesRepository.observeVoiceType() } returns MutableStateFlow(redFlagVoiceType)
        coEvery { telemetryLogRepository.saveTelemetryLog(any(), any(), any(), any()) } just Runs

        return LmuWindowsNarratorViewModel(
            vehicleApproachUseCases = VehicleApproachUseCases(
                observeVehicleApproach = ObserveLmuWindowsVehicleApproachUseCase(vehicleApproachRepository),
                observeLmuWindows = ObserveLmuWindowsUseCase(lmuWindowsRepository),
                observeSkipFirstLap = ObserveLmuWindowsVehicleApproachSkipFirstLapUseCase(
                    vehicleApproachPreferencesRepository,
                ),
                observeEnabledStates = ObserveLmuWindowsVehicleApproachEnabledStatesUseCase(
                    vehicleApproachPreferencesRepository,
                ),
                observeStartReadoutType = ObserveLmuWindowsVehicleApproachStartReadoutTypeUseCase(
                    vehicleApproachPreferencesRepository,
                ),
                observeSustainedApproachDuration = ObserveLmuWindowsVehicleApproachSustainedDurationUseCase(
                    vehicleApproachThresholdsPreferencesRepository,
                ),
                observeSustainedReadoutType = ObserveLmuWindowsVehicleApproachSustainedReadoutTypeUseCase(
                    vehicleApproachPreferencesRepository,
                ),
            ),
            vehicleDamageUseCases = VehicleDamageUseCases(
                observeVehicleDamage = ObserveLmuWindowsVehicleDamageUseCase(vehicleDamageRepository),
                observeVehicleDamageEnabledStates = ObserveLmuWindowsVehicleDamageEnabledStatesUseCase(
                    vehicleDamagePreferencesRepository,
                ),
            ),
            readoutListUseCases = ReadoutListUseCases(
                observeSelectedSimulator = ObserveSelectedSimulatorUseCase(simulatorPreferencesRepository),
                observeReadoutEnabledStates = ObserveReadoutEnabledStatesUseCase(readoutPreferencesRepository),
                observeReadoutOrder = ObserveReadoutOrderUseCase(readoutPreferencesRepository),
            ),
            flagUseCases = FlagUseCases(
                observeRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(flagRepository),
                observeFlagEnabledStates = ObserveLmuWindowsFlagEnabledStatesUseCase(flagPreferencesRepository),
            ),
            tyreTemperatureUseCases = TyreTemperatureUseCases(
                observeTyreCarcassTemperature = ObserveLmuWindowsTyreCarcassTemperatureUseCase(
                    tyreCarcassTemperatureRepository,
                ),
                observeHighThreshold = ObserveLmuWindowsTyreTemperatureHighThresholdUseCase(
                    tyreTemperaturePreferencesRepository,
                ),
                observeTyreTemperatureEnabledStates = ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase(
                    tyreTemperaturePreferencesRepository,
                ),
                observeLowWarningPhases = ObserveLmuWindowsTyreTemperatureLowWarningPhasesUseCase(
                    tyreTemperaturePreferencesRepository,
                ),
            ),
            ttsEngine = ttsEngine,
            narratorUseCases = NarratorUseCases(
                determineReadout = DetermineLmuWindowsNarratorReadoutUseCase(),
                observeMyBestLapVoiceType = ObserveLmuWindowsMyBestLapVoiceTypeUseCase(myBestLapPreferencesRepository),
                observeRedFlagVoiceType = ObserveLmuWindowsRedFlagVoiceTypeUseCase(redFlagPreferencesRepository),
                saveTelemetryLog = SaveTelemetryLogUseCase(telemetryLogRepository),
            ),
            currentTimeMs = currentTimeMs,
        )
    }

    private fun mockTts(spokenTexts: MutableList<SpeechEvent>): TextToSpeechEngine {
        val tts: TextToSpeechEngine = mockk()
        every { tts.currentReadoutItemKey } returns null
        every { tts.speak(any(), any()) } answers { spokenTexts.add(firstArg()) }
        every { tts.stop() } just Runs
        every { tts.previewStartSound(any()) } just Runs
        return tts
    }

    private interface PriorityAwareTts : TextToSpeechEngine {
        val stopCalled: Boolean
    }

    private fun mockPriorityAwareTts(
        spokenTexts: MutableList<SpeechEvent>,
        initialKey: ReadoutItemKey?,
    ): PriorityAwareTts {
        val tts: PriorityAwareTts = mockk()
        var currentKey = initialKey
        var stopCalled = false
        every { tts.currentReadoutItemKey } answers { currentKey }
        every { tts.speak(any(), any()) } answers { spokenTexts.add(firstArg()) }
        every { tts.stop() } answers {
            stopCalled = true
            currentKey = null
        }
        every { tts.stopCalled } answers { stopCalled }
        return tts
    }

    // --- シミュレータ選択 ---

    @Test
    fun `LMU非選択時は接近アナウンスをしない`() = runTest(testDispatcher) {
        var fakeTime = 0L
        val channel = Channel<LmuWindowsVehicleApproachData>(Channel.UNLIMITED)
        val spokenTexts = mutableListOf<SpeechEvent>()
        val tts = mockTts(spokenTexts)
        createViewModel(
            vehicleApproachChannel = channel,
            ttsEngine = tts,
            simulator = null,
            currentTimeMs = { fakeTime },
        )

        channel.send(noVehicleApproach())
        channel.send(leftVehicleApproach(vehicleId = 1))
        fakeTime = 50L
        channel.send(leftVehicleApproach(vehicleId = 1))

        assertEquals(emptyList<SpeechEvent>(), spokenTexts)
    }

    @Test
    fun `LMU非選択時は旗アナウンスをしない`() = runTest(testDispatcher) {
        val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
        val spokenTexts = mutableListOf<SpeechEvent>()
        val tts = mockTts(spokenTexts)
        createViewModel(flagChannel = flagChannel, ttsEngine = tts, simulator = null)

        flagChannel.send(clearFlags())
        flagChannel.send(clearFlags(playerFlag = PrimaryFlag.BLUE))

        assertEquals(emptyList<SpeechEvent>(), spokenTexts)
    }

    // --- 自己ベストラップ ---

    @Test
    fun `自己ベストラップの声種別設定を反映して読み上げる`() = runTest(testDispatcher) {
        val telemetryChannel = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
        val spokenTexts = mutableListOf<SpeechEvent>()
        val tts = mockTts(spokenTexts)
        createViewModel(
            telemetryChannel = telemetryChannel,
            ttsEngine = tts,
            voiceType = MyBestLapVoiceType.CASUAL,
            enabledOverrides = mapOf(ReadoutItemKey.LmuWindows.MyBestLap.Root to true),
            orderOverride = listOf(ReadoutItemKey.LmuWindows.MyBestLap.Root),
        )

        telemetryChannel.send(fakeTelemetryData(bestLapTimeMs = 60_000L))
        telemetryChannel.send(fakeTelemetryData(bestLapTimeMs = 59_000L))

        assertEquals(listOf<SpeechEvent>(SpeechEvent.LmuWindowsMyBestLapCasual), spokenTexts)
    }

    @Test
    fun `自己ベストラップが無効のときは読み上げない`() = runTest(testDispatcher) {
        val telemetryChannel = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
        val spokenTexts = mutableListOf<SpeechEvent>()
        val tts = mockTts(spokenTexts)
        createViewModel(
            telemetryChannel = telemetryChannel,
            ttsEngine = tts,
            enabledOverrides = mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.LmuWindows.MyBestLap.Root to false),
            orderOverride = listOf(ReadoutItemKey.LmuWindows.MyBestLap.Root),
        )

        telemetryChannel.send(fakeTelemetryData(bestLapTimeMs = 60_000L))
        telemetryChannel.send(fakeTelemetryData(bestLapTimeMs = 59_000L))

        assertEquals(emptyList<SpeechEvent>(), spokenTexts)
    }

    @Test
    fun `自己ベストラップ読み上げが発生したら現在と直前のテレメトリを保存する`() = runTest(testDispatcher) {
        val telemetryChannel = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
        val spokenTexts = mutableListOf<SpeechEvent>()
        val logs = mutableListOf<TelemetryLog>()
        val tts = mockTts(spokenTexts)
        createViewModel(
            telemetryChannel = telemetryChannel,
            ttsEngine = tts,
            enabledOverrides = mapOf(ReadoutItemKey.LmuWindows.MyBestLap.Root to true),
            orderOverride = listOf(ReadoutItemKey.LmuWindows.MyBestLap.Root),
            currentTimeMs = { 456L },
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

        telemetryChannel.send(fakeTelemetryData(bestLapTimeMs = 60_000L, currentLap = 1))
        telemetryChannel.send(fakeTelemetryData(bestLapTimeMs = 59_000L, currentLap = 2))

        assertEquals(
            listOf(
                TelemetryLog(
                    id = 0,
                    createdAt = 456L,
                    simulatorId = Simulator.LmuWindows.id,
                    readoutItemKey = ReadoutItemKey.LmuWindows.MyBestLap.Root.value,
                    telemetryJson =
                        """{"previous":{"currentLapTimeMs":0,"lastLapTimeMs":0,"bestLapTimeMs":60000,""" +
                            """"currentLap":1,"maxLaps":0},"current":{"currentLapTimeMs":0,""" +
                            """"lastLapTimeMs":0,"bestLapTimeMs":59000,"currentLap":2,"maxLaps":0}}""",
                ),
            ),
            logs,
        )
    }

    // --- 接近アナウンス ---

    @Test
    fun `接近開始時の読み上げが無効のときは接近アナウンスをしない`() = runTest(testDispatcher) {
        var fakeTime = 0L
        val channel = Channel<LmuWindowsVehicleApproachData>(Channel.UNLIMITED)
        val spokenTexts = mutableListOf<SpeechEvent>()
        val tts = mockTts(spokenTexts)
        createViewModel(
            vehicleApproachChannel = channel,
            ttsEngine = tts,
            startReadoutEnabled = false,
            currentTimeMs = { fakeTime },
        )

        channel.send(noVehicleApproach())
        channel.send(leftVehicleApproach(vehicleId = 1))
        fakeTime = 50L
        channel.send(leftVehicleApproach(vehicleId = 1))

        assertEquals(emptyList<SpeechEvent>(), spokenTexts)
    }

    @Test
    fun `VEHICLE_APPROACHが無効のときはアナウンスしない`() = runTest(testDispatcher) {
        var fakeTime = 0L
        val channel = Channel<LmuWindowsVehicleApproachData>(Channel.UNLIMITED)
        val spokenTexts = mutableListOf<SpeechEvent>()
        val tts = mockTts(spokenTexts)
        createViewModel(
            vehicleApproachChannel = channel,
            ttsEngine = tts,
            enabledOverrides = mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.LmuWindows.VehicleApproach.Root to false),
            currentTimeMs = { fakeTime },
        )

        channel.send(noVehicleApproach())
        channel.send(leftVehicleApproach(vehicleId = 1))
        fakeTime = 50L
        channel.send(leftVehicleApproach(vehicleId = 1))

        assertEquals(emptyList<SpeechEvent>(), spokenTexts)
    }

    @Test
    fun `1周目スキップONかつ現在ラップが0のときはアナウンスしない`() = runTest(testDispatcher) {
        var fakeTime = 0L
        val vehicleApproachChannel = Channel<LmuWindowsVehicleApproachData>(Channel.UNLIMITED)
        val telemetryChannel = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
        val spokenTexts = mutableListOf<SpeechEvent>()
        val tts = mockTts(spokenTexts)
        createViewModel(
            vehicleApproachChannel = vehicleApproachChannel,
            telemetryChannel = telemetryChannel,
            ttsEngine = tts,
            skipFirstLap = true,
            currentTimeMs = { fakeTime },
        )

        // mLapNumber は 0 スタートのため、1周目（最初の計測周）は 0
        telemetryChannel.send(fakeTelemetryData(currentLap = 0))
        vehicleApproachChannel.send(noVehicleApproach())
        vehicleApproachChannel.send(leftVehicleApproach(vehicleId = 1))
        fakeTime = 50L
        vehicleApproachChannel.send(leftVehicleApproach(vehicleId = 1))

        assertEquals(emptyList<SpeechEvent>(), spokenTexts)
    }

    @Test
    fun `1周目スキップONでも2周目以降はアナウンスする`() = runTest(testDispatcher) {
        var fakeTime = 0L
        val vehicleApproachChannel = Channel<LmuWindowsVehicleApproachData>(Channel.UNLIMITED)
        val telemetryChannel = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
        val spokenTexts = mutableListOf<SpeechEvent>()
        val tts = mockTts(spokenTexts)
        createViewModel(
            vehicleApproachChannel = vehicleApproachChannel,
            telemetryChannel = telemetryChannel,
            ttsEngine = tts,
            skipFirstLap = true,
            currentTimeMs = { fakeTime },
        )

        // mLapNumber は 0 スタートのため、2周目は 1
        telemetryChannel.send(fakeTelemetryData(currentLap = 1))
        vehicleApproachChannel.send(noVehicleApproach())
        vehicleApproachChannel.send(leftVehicleApproach(vehicleId = 1))
        fakeTime = 50L
        vehicleApproachChannel.send(leftVehicleApproach(vehicleId = 1))

        assertEquals(listOf<SpeechEvent>(SpeechEvent.CarLeft), spokenTexts)
    }

    @Test
    fun `左接近が閾値秒数継続するとKeepRightを読み上げる`() = runTest(testDispatcher) {
        var fakeTime = 0L
        val channel = Channel<LmuWindowsVehicleApproachData>(Channel.UNLIMITED)
        val spokenTexts = mutableListOf<SpeechEvent>()
        val tts = mockTts(spokenTexts)
        createViewModel(
            vehicleApproachChannel = channel,
            ttsEngine = tts,
            sustainedApproachDurationSeconds = 7,
            currentTimeMs = { fakeTime },
        )

        channel.send(noVehicleApproach())
        channel.send(leftVehicleApproach(vehicleId = 1))
        fakeTime = 7_000L
        channel.send(leftVehicleApproach(vehicleId = 1))

        assertEquals(listOf(SpeechEvent.CarLeft, SpeechEvent.KeepRight), spokenTexts)
    }

    @Test
    fun `接近継続時の読み上げが無効のときは継続接近を読み上げない`() = runTest(testDispatcher) {
        var fakeTime = 0L
        val channel = Channel<LmuWindowsVehicleApproachData>(Channel.UNLIMITED)
        val spokenTexts = mutableListOf<SpeechEvent>()
        val tts = mockTts(spokenTexts)
        createViewModel(
            vehicleApproachChannel = channel,
            ttsEngine = tts,
            sustainedReadoutEnabled = false,
            sustainedApproachDurationSeconds = 7,
            currentTimeMs = { fakeTime },
        )

        channel.send(noVehicleApproach())
        channel.send(leftVehicleApproach(vehicleId = 1))
        fakeTime = 7_000L
        channel.send(leftVehicleApproach(vehicleId = 1))

        assertEquals(listOf<SpeechEvent>(SpeechEvent.CarLeft), spokenTexts)
    }

    @Test
    fun `接近読み上げが発生したら現在と直前のテレメトリを保存する`() = runTest(testDispatcher) {
        var fakeTime = 0L
        val channel = Channel<LmuWindowsVehicleApproachData>(Channel.UNLIMITED)
        val logs = mutableListOf<TelemetryLog>()
        val spokenTexts = mutableListOf<SpeechEvent>()
        val tts = mockTts(spokenTexts)
        createViewModel(
            vehicleApproachChannel = channel,
            ttsEngine = tts,
            sustainedReadoutEnabled = false,
            currentTimeMs = { fakeTime },
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

        channel.send(noVehicleApproach())
        channel.send(leftVehicleApproach(vehicleId = 1))
        fakeTime = 123_456L
        channel.send(leftVehicleApproach(vehicleId = 1))

        assertEquals(
            listOf(
                TelemetryLog(
                    id = 0,
                    createdAt = 123_456L,
                    simulatorId = Simulator.LmuWindows.id,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach.Root.value,
                    telemetryJson =
                        """{"previous":{"sideBySideLeftVehicleIds":[1],""" +
                            """"sideBySideRightVehicleIds":[],"lateralDistanceLeftMeters":3.0,""" +
                            """"lateralDistanceRightMeters":${Double.MAX_VALUE}},""" +
                            """"current":{"sideBySideLeftVehicleIds":[1],""" +
                            """"sideBySideRightVehicleIds":[],"lateralDistanceLeftMeters":3.0,""" +
                            """"lateralDistanceRightMeters":${Double.MAX_VALUE}}}""",
                ),
            ),
            logs,
        )
    }

    // --- 優先度 ---

    @Test
    fun `フラグ読み上げ中に車両接近イベントが来ても読み上げない`() = runTest(testDispatcher) {
        var fakeTime = 0L
        val channel = Channel<LmuWindowsVehicleApproachData>(Channel.UNLIMITED)
        val spokenTexts = mutableListOf<SpeechEvent>()
        val tts = mockPriorityAwareTts(
            spokenTexts = spokenTexts,
            initialKey = ReadoutItemKey.LmuWindows.Flag.Root,
        )
        createViewModel(vehicleApproachChannel = channel, ttsEngine = tts, currentTimeMs = { fakeTime })

        channel.send(noVehicleApproach())
        channel.send(leftVehicleApproach(vehicleId = 1))
        fakeTime = 50L
        channel.send(leftVehicleApproach(vehicleId = 1))

        assertEquals(emptyList<SpeechEvent>(), spokenTexts)
    }

    @Test
    fun `優先度制御で読み上げなかったイベントは保存しない`() = runTest(testDispatcher) {
        var fakeTime = 0L
        val channel = Channel<LmuWindowsVehicleApproachData>(Channel.UNLIMITED)
        val logs = mutableListOf<TelemetryLog>()
        val spokenTexts = mutableListOf<SpeechEvent>()
        val tts = mockPriorityAwareTts(
            spokenTexts = spokenTexts,
            initialKey = ReadoutItemKey.LmuWindows.Flag.Root,
        )
        createViewModel(
            vehicleApproachChannel = channel,
            ttsEngine = tts,
            currentTimeMs = { fakeTime },
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

        channel.send(noVehicleApproach())
        channel.send(leftVehicleApproach(vehicleId = 1))
        fakeTime = 50L
        channel.send(leftVehicleApproach(vehicleId = 1))

        assertEquals(emptyList<TelemetryLog>(), logs)
    }

    @Test
    fun `車両接近読み上げ中にフラグイベントが来ると読み上げを停止して割り込む`() = runTest(testDispatcher) {
        val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
        val spokenTexts = mutableListOf<SpeechEvent>()
        val tts = mockPriorityAwareTts(
            spokenTexts = spokenTexts,
            initialKey = ReadoutItemKey.LmuWindows.VehicleApproach.Root,
        )
        createViewModel(flagChannel = flagChannel, ttsEngine = tts)

        flagChannel.send(clearFlags())
        flagChannel.send(clearFlags(playerFlag = PrimaryFlag.BLUE))

        assertEquals(true, tts.stopCalled)
        assertEquals(listOf<SpeechEvent>(SpeechEvent.BlueFlag), spokenTexts)
    }

    @Test
    fun `再生中の項目が優先度リストにないときは新しい読み上げで割り込む`() = runTest(testDispatcher) {
        var fakeTime = 0L
        val channel = Channel<LmuWindowsVehicleApproachData>(Channel.UNLIMITED)
        val spokenTexts = mutableListOf<SpeechEvent>()
        val tts = mockPriorityAwareTts(
            spokenTexts = spokenTexts,
            initialKey = ReadoutItemKey.LmuWindows.Flag.Root,
        )
        createViewModel(
            vehicleApproachChannel = channel,
            ttsEngine = tts,
            orderOverride = listOf(ReadoutItemKey.LmuWindows.VehicleApproach.Root),
            currentTimeMs = { fakeTime },
        )

        channel.send(noVehicleApproach())
        channel.send(leftVehicleApproach(vehicleId = 1))
        fakeTime = 50L
        channel.send(leftVehicleApproach(vehicleId = 1))

        assertEquals(true, tts.stopCalled)
        assertEquals(listOf<SpeechEvent>(SpeechEvent.CarLeft), spokenTexts)
    }

    @Test
    fun `新しい項目が優先度リストにないときは再生中の読み上げを優先する`() = runTest(testDispatcher) {
        var fakeTime = 0L
        val channel = Channel<LmuWindowsVehicleApproachData>(Channel.UNLIMITED)
        val spokenTexts = mutableListOf<SpeechEvent>()
        val tts = mockPriorityAwareTts(
            spokenTexts = spokenTexts,
            initialKey = ReadoutItemKey.LmuWindows.Flag.Root,
        )
        createViewModel(
            vehicleApproachChannel = channel,
            ttsEngine = tts,
            orderOverride = listOf(ReadoutItemKey.LmuWindows.Flag.Root),
            currentTimeMs = { fakeTime },
        )

        channel.send(noVehicleApproach())
        channel.send(leftVehicleApproach(vehicleId = 1))
        fakeTime = 50L
        channel.send(leftVehicleApproach(vehicleId = 1))

        assertEquals(false, tts.stopCalled)
        assertEquals(emptyList<SpeechEvent>(), spokenTexts)
    }

    // --- オーバーヒート / 旗 ---

    @Test
    fun `OVERHEATが無効のときはオーバーヒートを読み上げない`() = runTest(testDispatcher) {
        val damageChannel = Channel<LmuWindowsVehicleDamageData>(Channel.UNLIMITED)
        val spokenTexts = mutableListOf<SpeechEvent>()
        val tts = mockTts(spokenTexts)
        createViewModel(
            damageChannel = damageChannel,
            ttsEngine = tts,
            vehicleDamageEnabledOverrides = mapOf<ReadoutItemKey, Boolean>(
                ReadoutItemKey.LmuWindows.VehicleDamage.Overheat to false,
            ),
        )

        damageChannel.send(noDamage())
        damageChannel.send(noDamage(overheating = true))

        assertEquals(emptyList<SpeechEvent>(), spokenTexts)
    }

    @Test
    fun `BLUE_FLAGが無効のときは青旗を読み上げない`() = runTest(testDispatcher) {
        val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
        val spokenTexts = mutableListOf<SpeechEvent>()
        val tts = mockTts(spokenTexts)
        createViewModel(
            flagChannel = flagChannel,
            ttsEngine = tts,
            flagEnabledOverrides = mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.LmuWindows.Flag.BlueFlag to false),
        )

        flagChannel.send(clearFlags())
        flagChannel.send(clearFlags(playerFlag = PrimaryFlag.BLUE))

        assertEquals(emptyList<SpeechEvent>(), spokenTexts)
    }

    @Test
    fun `フラッグが無効のときは詳細フラッグ設定が有効でも読み上げない`() = runTest(testDispatcher) {
        val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
        val spokenTexts = mutableListOf<SpeechEvent>()
        val tts = mockTts(spokenTexts)
        createViewModel(
            flagChannel = flagChannel,
            ttsEngine = tts,
            enabledOverrides = mapOf(ReadoutItemKey.LmuWindows.Flag.Root to false),
            flagEnabledOverrides = mapOf(
                ReadoutItemKey.LmuWindows.Flag.BlueFlag to true,
                ReadoutItemKey.LmuWindows.Flag.SectorYellowFlag to true,
                ReadoutItemKey.LmuWindows.Flag.FullCourseYellow to true,
                ReadoutItemKey.LmuWindows.Flag.RedFlag to true,
            ),
        )

        flagChannel.send(clearFlags())
        flagChannel.send(
            clearFlags(
                gamePhase = SessionPhase.FULL_COURSE_YELLOW,
                playerFlag = PrimaryFlag.BLUE,
                sectorFlags = listOf(SectorFlagState.YELLOW, SectorFlagState.CLEAR, SectorFlagState.CLEAR),
            ),
        )

        assertEquals(emptyList<SpeechEvent>(), spokenTexts)
    }

    @Test
    fun `青旗読み上げが発生したら現在と直前のテレメトリを保存する`() = runTest(testDispatcher) {
        var fakeTime = 0L
        val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
        val logs = mutableListOf<TelemetryLog>()
        val spokenTexts = mutableListOf<SpeechEvent>()
        val tts = mockTts(spokenTexts)
        createViewModel(
            flagChannel = flagChannel,
            ttsEngine = tts,
            currentTimeMs = { fakeTime },
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

        flagChannel.send(clearFlags())
        fakeTime = 789L
        flagChannel.send(clearFlags(playerFlag = PrimaryFlag.BLUE))

        assertEquals(
            listOf(
                TelemetryLog(
                    id = 0,
                    createdAt = 789L,
                    simulatorId = Simulator.LmuWindows.id,
                    readoutItemKey = ReadoutItemKey.LmuWindows.Flag.Root.value,
                    telemetryJson =
                        """{"previous":{"gamePhase":"GREEN_FLAG","yellowFlagState":"NONE",""" +
                            """"sectorFlags":["CLEAR","CLEAR","CLEAR"],"startLight":0,"numRedLights":0,""" +
                            """"playerFlag":"GREEN","playerUnderYellow":false,""" +
                            """"playerCountLapFlag":"DO_NOT_COUNT_LAP_OR_TIME"},""" +
                            """"current":{"gamePhase":"GREEN_FLAG","yellowFlagState":"NONE",""" +
                            """"sectorFlags":["CLEAR","CLEAR","CLEAR"],"startLight":0,"numRedLights":0,""" +
                            """"playerFlag":"BLUE","playerUnderYellow":false,""" +
                            """"playerCountLapFlag":"DO_NOT_COUNT_LAP_OR_TIME"}}""",
                ),
            ),
            logs,
        )
    }

    @Test
    fun `赤旗音声タイプがRED_FLAGのときはRedFlagイベントを読み上げる`() = runTest(testDispatcher) {
        val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
        val spokenTexts = mutableListOf<SpeechEvent>()
        val tts = mockTts(spokenTexts)
        createViewModel(
            flagChannel = flagChannel,
            ttsEngine = tts,
            redFlagVoiceType = RedFlagVoiceType.RED_FLAG,
        )

        flagChannel.send(clearFlags())
        flagChannel.send(clearFlags(gamePhase = SessionPhase.RED_FLAG))

        assertEquals(listOf<SpeechEvent>(SpeechEvent.RedFlag), spokenTexts)
    }

    @Test
    fun `ログ保存に失敗しても以後の読み上げは継続する`() = runTest(testDispatcher) {
        val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
        val spokenTexts = mutableListOf<SpeechEvent>()
        val tts = mockTts(spokenTexts)
        createViewModel(
            flagChannel = flagChannel,
            ttsEngine = tts,
        )
        coEvery {
            telemetryLogRepository.saveTelemetryLog(any(), any(), any(), any())
        } throws IllegalStateException("Failed to save")

        flagChannel.send(clearFlags())
        flagChannel.send(clearFlags(playerFlag = PrimaryFlag.BLUE))
        flagChannel.send(clearFlags())
        flagChannel.send(clearFlags(gamePhase = SessionPhase.RED_FLAG))

        assertEquals(
            listOf<SpeechEvent>(SpeechEvent.BlueFlag, SpeechEvent.SessionStop),
            spokenTexts,
        )
    }

    @Test
    fun `オーバーヒート読み上げが発生したら現在と直前のテレメトリを保存する`() = runTest(testDispatcher) {
        var fakeTime = 0L
        val damageChannel = Channel<LmuWindowsVehicleDamageData>(Channel.UNLIMITED)
        val logs = mutableListOf<TelemetryLog>()
        val spokenTexts = mutableListOf<SpeechEvent>()
        val tts = mockTts(spokenTexts)
        createViewModel(
            damageChannel = damageChannel,
            ttsEngine = tts,
            currentTimeMs = { fakeTime },
            enabledOverrides = mapOf(ReadoutItemKey.LmuWindows.VehicleDamage.Root to true),
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

        damageChannel.send(noDamage())
        fakeTime = 987L
        damageChannel.send(noDamage(overheating = true))

        assertEquals(
            listOf(
                TelemetryLog(
                    id = 0,
                    createdAt = 987L,
                    simulatorId = Simulator.LmuWindows.id,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleDamage.Root.value,
                    telemetryJson =
                        """{"previous":{"overheating":false,"partDetached":false,"lastImpactMagnitude":0.0},""" +
                            """"current":{"overheating":true,"partDetached":false,"lastImpactMagnitude":0.0}}""",
                ),
            ),
            logs,
        )
    }

    // --- タイヤ温度 ---

    @Test
    fun `閾値以上のタイヤ温度が来ると TyreOverheat を読み上げる`() = runTest(testDispatcher) {
        val channel = Channel<LmuWindowsTyreCarcassTemperatureData>(Channel.UNLIMITED)
        val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
        val spokenTexts = mutableListOf<SpeechEvent>()
        val tts = mockTts(spokenTexts)
        createViewModel(
            tyreTemperatureChannel = channel,
            flagChannel = flagChannel,
            ttsEngine = tts,
            tyreTemperatureHighThreshold = 90,
            enabledOverrides = mapOf(ReadoutItemKey.LmuWindows.TyreTemperature.Root to true),
        )
        flagChannel.send(clearFlags())

        channel.send(tyreTemperature(fl = 95.0))

        assertEquals(listOf<SpeechEvent>(SpeechEvent.TyreOverheat), spokenTexts)
    }

    @Test
    fun `高温状態が継続しても2回目は読み上げない`() = runTest(testDispatcher) {
        val channel = Channel<LmuWindowsTyreCarcassTemperatureData>(Channel.UNLIMITED)
        val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
        val spokenTexts = mutableListOf<SpeechEvent>()
        val tts = mockTts(spokenTexts)
        createViewModel(
            tyreTemperatureChannel = channel,
            flagChannel = flagChannel,
            ttsEngine = tts,
            tyreTemperatureHighThreshold = 90,
            enabledOverrides = mapOf(ReadoutItemKey.LmuWindows.TyreTemperature.Root to true),
        )
        flagChannel.send(clearFlags())

        channel.send(tyreTemperature(fl = 95.0))
        channel.send(tyreTemperature(fl = 95.0))

        assertEquals(listOf<SpeechEvent>(SpeechEvent.TyreOverheat), spokenTexts)
    }

    @Test
    fun `全タイヤが閾値以下に戻ると再度読み上げ可能になる`() = runTest(testDispatcher) {
        val channel = Channel<LmuWindowsTyreCarcassTemperatureData>(Channel.UNLIMITED)
        val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
        val spokenTexts = mutableListOf<SpeechEvent>()
        val tts = mockTts(spokenTexts)
        createViewModel(
            tyreTemperatureChannel = channel,
            flagChannel = flagChannel,
            ttsEngine = tts,
            tyreTemperatureHighThreshold = 90,
            enabledOverrides = mapOf(ReadoutItemKey.LmuWindows.TyreTemperature.Root to true),
        )
        flagChannel.send(clearFlags())

        channel.send(tyreTemperature(fl = 95.0))
        channel.send(tyreTemperature(fl = 20.0))
        channel.send(tyreTemperature(fl = 95.0))

        assertEquals(listOf<SpeechEvent>(SpeechEvent.TyreOverheat, SpeechEvent.TyreOverheat), spokenTexts)
    }

    @Test
    fun `タイヤ温度項目が無効なら読み上げない`() = runTest(testDispatcher) {
        val channel = Channel<LmuWindowsTyreCarcassTemperatureData>(Channel.UNLIMITED)
        val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
        val spokenTexts = mutableListOf<SpeechEvent>()
        val tts = mockTts(spokenTexts)
        createViewModel(
            tyreTemperatureChannel = channel,
            flagChannel = flagChannel,
            ttsEngine = tts,
            tyreTemperatureHighThreshold = 90,
            enabledOverrides = mapOf(ReadoutItemKey.LmuWindows.TyreTemperature.Root to false),
        )
        flagChannel.send(clearFlags())

        channel.send(tyreTemperature(fl = 95.0))

        assertEquals(emptyList<SpeechEvent>(), spokenTexts)
    }

    @Test
    fun `過熱警告スイッチがOFFのときは読み上げない`() = runTest(testDispatcher) {
        val channel = Channel<LmuWindowsTyreCarcassTemperatureData>(Channel.UNLIMITED)
        val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
        val spokenTexts = mutableListOf<SpeechEvent>()
        val tts = mockTts(spokenTexts)
        createViewModel(
            tyreTemperatureChannel = channel,
            flagChannel = flagChannel,
            ttsEngine = tts,
            tyreTemperatureHighThreshold = 90,
            enabledOverrides = mapOf(ReadoutItemKey.LmuWindows.TyreTemperature.Root to true),
            tyreTemperatureOverheatWarningEnabled = false,
        )
        flagChannel.send(clearFlags())

        channel.send(tyreTemperature(fl = 95.0))

        assertEquals(emptyList<SpeechEvent>(), spokenTexts)
    }

    @Test
    fun `LMU非選択時はタイヤ温度アナウンスをしない`() = runTest(testDispatcher) {
        val channel = Channel<LmuWindowsTyreCarcassTemperatureData>(Channel.UNLIMITED)
        val spokenTexts = mutableListOf<SpeechEvent>()
        val tts = mockTts(spokenTexts)
        createViewModel(tyreTemperatureChannel = channel, ttsEngine = tts, simulator = null)

        channel.send(tyreTemperature(fl = 95.0))

        assertEquals(emptyList<SpeechEvent>(), spokenTexts)
    }

    @Test
    fun `タイヤ温度がOFFの場合は読み上げない`() = runTest(testDispatcher) {
        val channel = Channel<LmuWindowsTyreCarcassTemperatureData>(Channel.UNLIMITED)
        val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
        val spokenTexts = mutableListOf<SpeechEvent>()
        val tts = mockTts(spokenTexts)
        createViewModel(
            tyreTemperatureChannel = channel,
            flagChannel = flagChannel,
            ttsEngine = tts,
            tyreTemperatureHighThreshold = 90,
            enabledOverrides = mapOf(ReadoutItemKey.LmuWindows.TyreTemperature.Root to false),
        )
        flagChannel.send(clearFlags())

        channel.send(tyreTemperature(fl = 95.0))

        assertEquals(emptyList<SpeechEvent>(), spokenTexts)
    }

    @Test
    fun `タイヤ温度読み上げが発生したらテレメトリを保存する`() = runTest(testDispatcher) {
        val channel = Channel<LmuWindowsTyreCarcassTemperatureData>(Channel.UNLIMITED)
        val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
        val logs = mutableListOf<TelemetryLog>()
        val spokenTexts = mutableListOf<SpeechEvent>()
        val tts = mockTts(spokenTexts)
        createViewModel(
            tyreTemperatureChannel = channel,
            flagChannel = flagChannel,
            ttsEngine = tts,
            tyreTemperatureHighThreshold = 90,
            enabledOverrides = mapOf(ReadoutItemKey.LmuWindows.TyreTemperature.Root to true),
            currentTimeMs = { 123L },
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
        flagChannel.send(clearFlags())

        channel.send(tyreTemperature(fl = 95.0))

        assertEquals(1, logs.size)
        val log = logs.first()
        assertEquals(123L, log.createdAt)
        assertEquals(Simulator.LmuWindows.id, log.simulatorId)
        assertEquals(ReadoutItemKey.LmuWindows.TyreTemperature.Root.value, log.readoutItemKey)
    }

    // --- タイヤ低温警告 ---

    @Test
    fun `ガレージに遷移した瞬間に低温タイヤがあるとTyreColdを読み上げる`() = runTest(testDispatcher) {
        val channel = Channel<LmuWindowsTyreCarcassTemperatureData>(Channel.UNLIMITED)
        val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
        val spokenTexts = mutableListOf<SpeechEvent>()
        val tts = mockTts(spokenTexts)
        createViewModel(
            tyreTemperatureChannel = channel,
            flagChannel = flagChannel,
            ttsEngine = tts,
            enabledOverrides = mapOf(ReadoutItemKey.LmuWindows.TyreTemperature.Root to true),
            tyreTemperatureLowWarningPhasesOverride = mapOf(SessionPhase.GARAGE to true),
        )
        flagChannel.send(clearFlags(gamePhase = SessionPhase.GREEN_FLAG))
        channel.send(tyreTemperature(fl = 55.0))

        flagChannel.send(clearFlags(gamePhase = SessionPhase.GARAGE))

        assertEquals(listOf<SpeechEvent>(SpeechEvent.TyreCold), spokenTexts)
    }

    @Test
    fun `gamePhaseが変化しなければ低温でも読み上げない`() = runTest(testDispatcher) {
        val channel = Channel<LmuWindowsTyreCarcassTemperatureData>(Channel.UNLIMITED)
        val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
        val spokenTexts = mutableListOf<SpeechEvent>()
        val tts = mockTts(spokenTexts)
        createViewModel(
            tyreTemperatureChannel = channel,
            flagChannel = flagChannel,
            ttsEngine = tts,
            enabledOverrides = mapOf(ReadoutItemKey.LmuWindows.TyreTemperature.Root to true),
            tyreTemperatureLowWarningPhasesOverride = mapOf(SessionPhase.GARAGE to true),
        )
        flagChannel.send(clearFlags(gamePhase = SessionPhase.GARAGE))
        channel.send(tyreTemperature(fl = 55.0))

        assertEquals(emptyList<SpeechEvent>(), spokenTexts)
    }

    @Test
    fun `低温警告スイッチがOFFのときは読み上げない`() = runTest(testDispatcher) {
        val channel = Channel<LmuWindowsTyreCarcassTemperatureData>(Channel.UNLIMITED)
        val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
        val spokenTexts = mutableListOf<SpeechEvent>()
        val tts = mockTts(spokenTexts)
        createViewModel(
            tyreTemperatureChannel = channel,
            flagChannel = flagChannel,
            ttsEngine = tts,
            enabledOverrides = mapOf(ReadoutItemKey.LmuWindows.TyreTemperature.Root to true),
            tyreTemperatureLowWarningEnabled = false,
            tyreTemperatureLowWarningPhasesOverride = mapOf(SessionPhase.GARAGE to true),
        )
        flagChannel.send(clearFlags(gamePhase = SessionPhase.GREEN_FLAG))
        channel.send(tyreTemperature(fl = 55.0))

        flagChannel.send(clearFlags(gamePhase = SessionPhase.GARAGE))

        assertEquals(emptyList<SpeechEvent>(), spokenTexts)
    }

    @Test
    fun `detailPaneで選択解除したgamePhaseに遷移しても読み上げない`() = runTest(testDispatcher) {
        val channel = Channel<LmuWindowsTyreCarcassTemperatureData>(Channel.UNLIMITED)
        val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
        val spokenTexts = mutableListOf<SpeechEvent>()
        val tts = mockTts(spokenTexts)
        createViewModel(
            tyreTemperatureChannel = channel,
            flagChannel = flagChannel,
            ttsEngine = tts,
            enabledOverrides = mapOf(ReadoutItemKey.LmuWindows.TyreTemperature.Root to true),
            tyreTemperatureLowWarningPhasesOverride = mapOf(SessionPhase.GARAGE to false),
        )
        flagChannel.send(clearFlags(gamePhase = SessionPhase.GREEN_FLAG))
        channel.send(tyreTemperature(fl = 55.0))

        flagChannel.send(clearFlags(gamePhase = SessionPhase.GARAGE))

        assertEquals(emptyList<SpeechEvent>(), spokenTexts)
    }

    @Test
    fun `低温警告読み上げが発生したらテレメトリを保存する`() = runTest(testDispatcher) {
        val channel = Channel<LmuWindowsTyreCarcassTemperatureData>(Channel.UNLIMITED)
        val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
        val logs = mutableListOf<TelemetryLog>()
        val spokenTexts = mutableListOf<SpeechEvent>()
        val tts = mockTts(spokenTexts)
        createViewModel(
            tyreTemperatureChannel = channel,
            flagChannel = flagChannel,
            ttsEngine = tts,
            enabledOverrides = mapOf(ReadoutItemKey.LmuWindows.TyreTemperature.Root to true),
            currentTimeMs = { 123L },
            tyreTemperatureLowWarningPhasesOverride = mapOf(SessionPhase.GARAGE to true),
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
        flagChannel.send(clearFlags(gamePhase = SessionPhase.GREEN_FLAG))
        channel.send(tyreTemperature(fl = 55.0))

        flagChannel.send(clearFlags(gamePhase = SessionPhase.GARAGE))

        assertEquals(1, logs.size)
        val log = logs.first()
        assertEquals(123L, log.createdAt)
        assertEquals(Simulator.LmuWindows.id, log.simulatorId)
        assertEquals(ReadoutItemKey.LmuWindows.TyreTemperature.Root.value, log.readoutItemKey)
    }
}

private fun noVehicleApproach() = LmuWindowsVehicleApproachData(
    sideBySideLeftVehicleIds = emptySet(),
    sideBySideRightVehicleIds = emptySet(),
    lateralDistanceLeftMeters = Double.MAX_VALUE,
    lateralDistanceRightMeters = Double.MAX_VALUE,
)

private fun leftVehicleApproach(vehicleId: Int) = LmuWindowsVehicleApproachData(
    sideBySideLeftVehicleIds = setOf(vehicleId),
    sideBySideRightVehicleIds = emptySet(),
    lateralDistanceLeftMeters = 3.0,
    lateralDistanceRightMeters = Double.MAX_VALUE,
)

private fun clearFlags(
    gamePhase: SessionPhase = SessionPhase.GREEN_FLAG,
    playerFlag: PrimaryFlag = PrimaryFlag.GREEN,
    sectorFlags: List<SectorFlagState> = listOf(
        SectorFlagState.CLEAR,
        SectorFlagState.CLEAR,
        SectorFlagState.CLEAR,
    ),
) = LmuWindowsRaceFlagsData(
    gamePhase = gamePhase,
    yellowFlagState = SessionYellowFlagState.NONE,
    sectorFlags = sectorFlags,
    startLight = 0,
    numRedLights = 0,
    playerFlag = playerFlag,
    playerUnderYellow = false,
    playerCountLapFlag = CountLapFlag.DO_NOT_COUNT_LAP_OR_TIME,
)

private fun noDamage(overheating: Boolean = false) = LmuWindowsVehicleDamageData(
    overheating = overheating,
    partDetached = false,
    lastImpactMagnitude = 0.0,
)

private fun fakeTelemetryData(
    currentLap: Int = 0,
    bestLapTimeMs: Long = 0L,
) = LmuWindowsTelemetryData(
    timestampMs = 0L,
    engine = LmuWindowsEngineData(rpm = 0.0, maxRpm = 0.0, gear = 0),
    inputs = LmuWindowsInputsData(throttle = 0.0, brake = 0.0, clutch = 0.0, steering = 0.0),
    tyres = LmuWindowsTyreData(wheels = emptyMap()),
    fuel = LmuWindowsFuelData(currentLiters = 0.0, capacityLiters = 0.0),
    timing = LmuWindowsTimingData(
        currentLapTimeMs = 0L,
        lastLapTimeMs = 0L,
        bestLapTimeMs = bestLapTimeMs,
        sector1Ms = 0L,
        sector1And2Ms = 0L,
        currentLap = currentLap,
        maxLaps = 0,
    ),
    vehicle = LmuWindowsVehicleData(
        localVelocityX = 0.0,
        localVelocityY = 0.0,
        localVelocityZ = 0.0,
        positionX = 0.0,
        positionY = 0.0,
        positionZ = 0.0,
    ),
)

private fun tyreTemperature(
    fl: Double = 20.0,
    fr: Double = 20.0,
    rl: Double = 20.0,
    rr: Double = 20.0,
) = LmuWindowsTyreCarcassTemperatureData(
    wheels = mapOf(
        WheelIndex.FRONT_LEFT to fl,
        WheelIndex.FRONT_RIGHT to fr,
        WheelIndex.REAR_LEFT to rl,
        WheelIndex.REAR_RIGHT to rr,
    ),
)
