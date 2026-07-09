package kurou.kodriver.feature.lmuwindowsnarrator

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
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
import kurou.kodriver.domain.model.LmuWindowsProximityData
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsTimingData
import kurou.kodriver.domain.model.LmuWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.model.LmuWindowsTyreData
import kurou.kodriver.domain.model.LmuWindowsVehicleDamageData
import kurou.kodriver.domain.model.LmuWindowsVehicleData
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kurou.kodriver.domain.model.PrimaryFlag
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kurou.kodriver.domain.model.SectorFlagState
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.SessionYellowFlagState
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.TelemetryLog
import kurou.kodriver.domain.model.TelemetryLogDetail
import kurou.kodriver.domain.model.VehicleApproachStartReadoutType
import kurou.kodriver.domain.model.WheelIndex
import kurou.kodriver.domain.repository.LmuWindowsFlagPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsFlagRepository
import kurou.kodriver.domain.repository.LmuWindowsMyBestLapPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsProximityRepository
import kurou.kodriver.domain.repository.LmuWindowsRepository
import kurou.kodriver.domain.repository.LmuWindowsTyreCarcassTemperatureRepository
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleDamagePreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleDamageRepository
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.repository.TelemetryLogRepository
import kurou.kodriver.domain.usecase.DetermineLmuWindowsNarratorReadoutUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsFlagEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsMyBestLapVoiceTypeUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsProximityUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRaceFlagsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreCarcassTemperatureUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureLowWarningPhasesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachSkipFirstLapUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachStartReadoutEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachStartReadoutTypeUseCase
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
@Suppress("TooManyFunctions")
class LmuWindowsNarratorViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Suppress("LongParameterList")
    private fun buildViewModel(
        proximityChannel: Channel<LmuWindowsProximityData> = Channel(Channel.UNLIMITED),
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
        skipFirstLap: Boolean = false,
        startReadoutEnabled: Boolean = true,
        startReadoutType: VehicleApproachStartReadoutType = VehicleApproachStartReadoutType.CAR_LEFT_RIGHT,
        tyreTemperatureHighThreshold: Int = 90,
        tyreTemperatureOverheatWarningEnabled: Boolean = true,
        tyreTemperatureLowWarningEnabled: Boolean = true,
        tyreTemperatureLowWarningPhasesOverride: Map<SessionPhase, Boolean> = emptyMap(),
        simulator: Simulator? = Simulator.LmuWindows,
        currentTimeMs: () -> Long = { 0L },
        telemetryLogRepository: FakeTelemetryLogRepository = FakeTelemetryLogRepository(),
    ): LmuWindowsNarratorViewModel {
        val readoutRepo = FakeAllEnabledReadoutPreferencesRepository(enabledOverrides, orderOverride)
        val vehicleApproachPreferencesRepository = FakeConstantLmuWindowsVehicleApproachPreferencesRepository(
            skipFirstLap = skipFirstLap,
            startReadoutEnabled = startReadoutEnabled,
            startReadoutType = startReadoutType,
        )
        return LmuWindowsNarratorViewModel(
            vehicleApproachUseCases = VehicleApproachUseCases(
                observeProximity = ObserveLmuWindowsProximityUseCase(
                    FakeChannelProximityRepository(proximityChannel.receiveAsFlow()),
                ),
                observeLmuWindows = ObserveLmuWindowsUseCase(
                    FakeChannelLmuWindowsRepository(telemetryChannel.receiveAsFlow()),
                ),
                observeSkipFirstLap = ObserveLmuWindowsVehicleApproachSkipFirstLapUseCase(
                    vehicleApproachPreferencesRepository,
                ),
                observeStartReadoutEnabled = ObserveLmuWindowsVehicleApproachStartReadoutEnabledUseCase(
                    vehicleApproachPreferencesRepository,
                ),
                observeStartReadoutType = ObserveLmuWindowsVehicleApproachStartReadoutTypeUseCase(
                    vehicleApproachPreferencesRepository,
                ),
            ),
            vehicleDamageUseCases = VehicleDamageUseCases(
                observeVehicleDamage = ObserveLmuWindowsVehicleDamageUseCase(
                    FakeChannelVehicleDamageRepository(damageChannel.receiveAsFlow()),
                ),
                observeVehicleDamageEnabledStates = ObserveLmuWindowsVehicleDamageEnabledStatesUseCase(
                    FakeLmuWindowsVehicleDamagePreferencesRepository(vehicleDamageEnabledOverrides),
                ),
            ),
            readoutListUseCases = ReadoutListUseCases(
                observeSelectedSimulator = ObserveSelectedSimulatorUseCase(
                    FakeConstantSimulatorRepository(simulator),
                ),
                observeReadoutEnabledStates = ObserveReadoutEnabledStatesUseCase(readoutRepo),
                observeReadoutOrder = ObserveReadoutOrderUseCase(readoutRepo),
            ),
            flagUseCases = FlagUseCases(
                observeRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(
                    FakeChannelFlagRepository(flagChannel.receiveAsFlow()),
                ),
                observeFlagEnabledStates = ObserveLmuWindowsFlagEnabledStatesUseCase(
                    FakeLmuWindowsFlagPreferencesRepository(flagEnabledOverrides),
                ),
            ),
            tyreTemperatureUseCases = TyreTemperatureUseCases(
                observeTyreCarcassTemperature = ObserveLmuWindowsTyreCarcassTemperatureUseCase(
                    FakeChannelTyreCarcassTemperatureRepository(tyreTemperatureChannel.receiveAsFlow()),
                ),
                observeHighThreshold = ObserveLmuWindowsTyreTemperatureHighThresholdUseCase(
                    FakeConstantLmuWindowsTyreTemperaturePreferencesRepository(
                        threshold = tyreTemperatureHighThreshold,
                        overheatWarningEnabled = tyreTemperatureOverheatWarningEnabled,
                        lowWarningEnabled = tyreTemperatureLowWarningEnabled,
                    ),
                ),
                observeTyreTemperatureEnabledStates = ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase(
                    FakeConstantLmuWindowsTyreTemperaturePreferencesRepository(
                        threshold = tyreTemperatureHighThreshold,
                        overheatWarningEnabled = tyreTemperatureOverheatWarningEnabled,
                        lowWarningEnabled = tyreTemperatureLowWarningEnabled,
                    ),
                ),
                observeLowWarningPhases = ObserveLmuWindowsTyreTemperatureLowWarningPhasesUseCase(
                    FakeConstantLmuWindowsTyreTemperaturePreferencesRepository(
                        threshold = tyreTemperatureHighThreshold,
                        overheatWarningEnabled = tyreTemperatureOverheatWarningEnabled,
                        lowWarningEnabled = tyreTemperatureLowWarningEnabled,
                        lowWarningPhases = tyreTemperatureLowWarningPhasesOverride,
                    ),
                ),
            ),
            ttsEngine = ttsEngine,
            narratorUseCases = NarratorUseCases(
                determineReadout = DetermineLmuWindowsNarratorReadoutUseCase(),
                observeMyBestLapVoiceType = ObserveLmuWindowsMyBestLapVoiceTypeUseCase(
                    FakeLmuWindowsMyBestLapPreferencesRepository(voiceType),
                ),
                saveTelemetryLog = SaveTelemetryLogUseCase(telemetryLogRepository),
            ),
            currentTimeMs = currentTimeMs,
        )
    }

    // --- シミュレータ選択 ---

    @Test
    fun `LMU非選択時は接近アナウンスをしない`() = runTest(testDispatcher) {
        var fakeTime = 0L
        val channel = Channel<LmuWindowsProximityData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(proximityChannel = channel, ttsEngine = tts, simulator = null, currentTimeMs = { fakeTime })

        channel.send(noProximity())
        channel.send(leftProximity(vehicleId = 1))
        fakeTime = 50L
        channel.send(leftProximity(vehicleId = 1))

        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }

    @Test
    fun `LMU非選択時は旗アナウンスをしない`() = runTest(testDispatcher) {
        val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(flagChannel = flagChannel, ttsEngine = tts, simulator = null)

        flagChannel.send(clearFlags())
        flagChannel.send(clearFlags(playerFlag = PrimaryFlag.BLUE))

        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }

    // --- 自己ベストラップ ---

    @Test
    fun `自己ベストラップの声種別設定を反映して読み上げる`() = runTest(testDispatcher) {
        val telemetryChannel = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
            telemetryChannel = telemetryChannel,
            ttsEngine = tts,
            voiceType = MyBestLapVoiceType.CASUAL,
            enabledOverrides = mapOf(ReadoutItemKey.LmuWindows.MyBestLap.Root to true),
            orderOverride = listOf(ReadoutItemKey.LmuWindows.MyBestLap.Root),
        )

        telemetryChannel.send(fakeTelemetryData(bestLapTimeMs = 60_000L))
        telemetryChannel.send(fakeTelemetryData(bestLapTimeMs = 59_000L))

        assertEquals(listOf<SpeechEvent>(SpeechEvent.LmuWindowsMyBestLapCasual), tts.spokenTexts)
    }

    @Test
    fun `自己ベストラップが無効のときは読み上げない`() = runTest(testDispatcher) {
        val telemetryChannel = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
            telemetryChannel = telemetryChannel,
            ttsEngine = tts,
            enabledOverrides = mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.LmuWindows.MyBestLap.Root to false),
            orderOverride = listOf(ReadoutItemKey.LmuWindows.MyBestLap.Root),
        )

        telemetryChannel.send(fakeTelemetryData(bestLapTimeMs = 60_000L))
        telemetryChannel.send(fakeTelemetryData(bestLapTimeMs = 59_000L))

        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }

    @Test
    fun `自己ベストラップ読み上げが発生したら現在と直前のテレメトリを保存する`() = runTest(testDispatcher) {
        val telemetryChannel = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
        val telemetryLogRepository = FakeTelemetryLogRepository()
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
            telemetryChannel = telemetryChannel,
            ttsEngine = tts,
            enabledOverrides = mapOf(ReadoutItemKey.LmuWindows.MyBestLap.Root to true),
            orderOverride = listOf(ReadoutItemKey.LmuWindows.MyBestLap.Root),
            currentTimeMs = { 456L },
            telemetryLogRepository = telemetryLogRepository,
        )

        telemetryChannel.send(fakeTelemetryData(bestLapTimeMs = 60_000L, currentLap = 1))
        telemetryChannel.send(fakeTelemetryData(bestLapTimeMs = 59_000L, currentLap = 2))

        assertEquals(
            listOf(
                TelemetryLog(
                    createdAt = 456L,
                    simulatorId = Simulator.LmuWindows.id,
                    readoutItemKey = ReadoutItemKey.LmuWindows.MyBestLap.Root.value,
                    telemetryJson =
                        """{"previous":{"currentLapTimeMs":0,"lastLapTimeMs":0,"bestLapTimeMs":60000,""" +
                            """"currentLap":1,"maxLaps":0},"current":{"currentLapTimeMs":0,""" +
                            """"lastLapTimeMs":0,"bestLapTimeMs":59000,"currentLap":2,"maxLaps":0}}""",
                ),
            ),
            telemetryLogRepository.logs.value,
        )
    }

    // --- 接近アナウンス ---

    @Test
    fun `接近開始時の読み上げが無効のときは接近アナウンスをしない`() = runTest(testDispatcher) {
        var fakeTime = 0L
        val channel = Channel<LmuWindowsProximityData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
            proximityChannel = channel,
            ttsEngine = tts,
            startReadoutEnabled = false,
            currentTimeMs = { fakeTime },
        )

        channel.send(noProximity())
        channel.send(leftProximity(vehicleId = 1))
        fakeTime = 50L
        channel.send(leftProximity(vehicleId = 1))

        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }

    @Test
    fun `VEHICLE_APPROACHが無効のときはアナウンスしない`() = runTest(testDispatcher) {
        var fakeTime = 0L
        val channel = Channel<LmuWindowsProximityData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
            proximityChannel = channel,
            ttsEngine = tts,
            enabledOverrides = mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.LmuWindows.VehicleApproach.Root to false),
            currentTimeMs = { fakeTime },
        )

        channel.send(noProximity())
        channel.send(leftProximity(vehicleId = 1))
        fakeTime = 50L
        channel.send(leftProximity(vehicleId = 1))

        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }

    @Test
    fun `1周目スキップONかつ現在ラップが0のときはアナウンスしない`() = runTest(testDispatcher) {
        var fakeTime = 0L
        val proximityChannel = Channel<LmuWindowsProximityData>(Channel.UNLIMITED)
        val telemetryChannel = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
            proximityChannel = proximityChannel,
            telemetryChannel = telemetryChannel,
            ttsEngine = tts,
            skipFirstLap = true,
            currentTimeMs = { fakeTime },
        )

        // mLapNumber は 0 スタートのため、1周目（最初の計測周）は 0
        telemetryChannel.send(fakeTelemetryData(currentLap = 0))
        proximityChannel.send(noProximity())
        proximityChannel.send(leftProximity(vehicleId = 1))
        fakeTime = 50L
        proximityChannel.send(leftProximity(vehicleId = 1))

        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }

    @Test
    fun `1周目スキップONでも2周目以降はアナウンスする`() = runTest(testDispatcher) {
        var fakeTime = 0L
        val proximityChannel = Channel<LmuWindowsProximityData>(Channel.UNLIMITED)
        val telemetryChannel = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
            proximityChannel = proximityChannel,
            telemetryChannel = telemetryChannel,
            ttsEngine = tts,
            skipFirstLap = true,
            currentTimeMs = { fakeTime },
        )

        // mLapNumber は 0 スタートのため、2周目は 1
        telemetryChannel.send(fakeTelemetryData(currentLap = 1))
        proximityChannel.send(noProximity())
        proximityChannel.send(leftProximity(vehicleId = 1))
        fakeTime = 50L
        proximityChannel.send(leftProximity(vehicleId = 1))

        assertEquals(listOf<SpeechEvent>(SpeechEvent.CarLeft), tts.spokenTexts)
    }

    @Test
    fun `接近読み上げが発生したら現在と直前のテレメトリを保存する`() = runTest(testDispatcher) {
        var fakeTime = 0L
        val channel = Channel<LmuWindowsProximityData>(Channel.UNLIMITED)
        val telemetryLogRepository = FakeTelemetryLogRepository()
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
            proximityChannel = channel,
            ttsEngine = tts,
            currentTimeMs = { fakeTime },
            telemetryLogRepository = telemetryLogRepository,
        )

        channel.send(noProximity())
        channel.send(leftProximity(vehicleId = 1))
        fakeTime = 123_456L
        channel.send(leftProximity(vehicleId = 1))

        assertEquals(
            listOf(
                TelemetryLog(
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
            telemetryLogRepository.logs.value,
        )
    }

    // --- 旗アナウンス ---

    // --- 優先度 ---

    @Test
    fun `フラグ読み上げ中に車両接近イベントが来ても読み上げない`() = runTest(testDispatcher) {
        var fakeTime = 0L
        val channel = Channel<LmuWindowsProximityData>(Channel.UNLIMITED)
        val tts = PriorityAwareTextToSpeechEngine(
            initialKey = ReadoutItemKey.LmuWindows.Flag.Root,
        )
        buildViewModel(proximityChannel = channel, ttsEngine = tts, currentTimeMs = { fakeTime })

        channel.send(noProximity())
        channel.send(leftProximity(vehicleId = 1))
        fakeTime = 50L
        channel.send(leftProximity(vehicleId = 1))

        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }

    @Test
    fun `優先度制御で読み上げなかったイベントは保存しない`() = runTest(testDispatcher) {
        var fakeTime = 0L
        val channel = Channel<LmuWindowsProximityData>(Channel.UNLIMITED)
        val telemetryLogRepository = FakeTelemetryLogRepository()
        val tts = PriorityAwareTextToSpeechEngine(
            initialKey = ReadoutItemKey.LmuWindows.Flag.Root,
        )
        buildViewModel(
            proximityChannel = channel,
            ttsEngine = tts,
            currentTimeMs = { fakeTime },
            telemetryLogRepository = telemetryLogRepository,
        )

        channel.send(noProximity())
        channel.send(leftProximity(vehicleId = 1))
        fakeTime = 50L
        channel.send(leftProximity(vehicleId = 1))

        assertEquals(emptyList<TelemetryLog>(), telemetryLogRepository.logs.value)
    }

    @Test
    fun `車両接近読み上げ中にフラグイベントが来ると読み上げを停止して割り込む`() = runTest(testDispatcher) {
        val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
        val tts = PriorityAwareTextToSpeechEngine(
            initialKey = ReadoutItemKey.LmuWindows.VehicleApproach.Root,
        )
        buildViewModel(flagChannel = flagChannel, ttsEngine = tts)

        flagChannel.send(clearFlags())
        flagChannel.send(clearFlags(playerFlag = PrimaryFlag.BLUE))

        assertEquals(true, tts.stopCalled)
        assertEquals(listOf<SpeechEvent>(SpeechEvent.BlueFlag), tts.spokenTexts)
    }

    @Test
    fun `再生中の項目が優先度リストにないときは新しい読み上げで割り込む`() = runTest(testDispatcher) {
        var fakeTime = 0L
        val channel = Channel<LmuWindowsProximityData>(Channel.UNLIMITED)
        val tts = PriorityAwareTextToSpeechEngine(
            initialKey = ReadoutItemKey.LmuWindows.Flag.Root,
        )
        buildViewModel(
            proximityChannel = channel,
            ttsEngine = tts,
            orderOverride = listOf(ReadoutItemKey.LmuWindows.VehicleApproach.Root),
            currentTimeMs = { fakeTime },
        )

        channel.send(noProximity())
        channel.send(leftProximity(vehicleId = 1))
        fakeTime = 50L
        channel.send(leftProximity(vehicleId = 1))

        assertEquals(true, tts.stopCalled)
        assertEquals(listOf<SpeechEvent>(SpeechEvent.CarLeft), tts.spokenTexts)
    }

    @Test
    fun `新しい項目が優先度リストにないときは再生中の読み上げを優先する`() = runTest(testDispatcher) {
        var fakeTime = 0L
        val channel = Channel<LmuWindowsProximityData>(Channel.UNLIMITED)
        val tts = PriorityAwareTextToSpeechEngine(
            initialKey = ReadoutItemKey.LmuWindows.Flag.Root,
        )
        buildViewModel(
            proximityChannel = channel,
            ttsEngine = tts,
            orderOverride = listOf(ReadoutItemKey.LmuWindows.Flag.Root),
            currentTimeMs = { fakeTime },
        )

        channel.send(noProximity())
        channel.send(leftProximity(vehicleId = 1))
        fakeTime = 50L
        channel.send(leftProximity(vehicleId = 1))

        assertEquals(false, tts.stopCalled)
        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }

    // --- オーバーヒート ---

    @Test
    fun `OVERHEATが無効のときはオーバーヒートを読み上げない`() = runTest(testDispatcher) {
        val damageChannel = Channel<LmuWindowsVehicleDamageData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
            damageChannel = damageChannel,
            ttsEngine = tts,
            vehicleDamageEnabledOverrides = mapOf<ReadoutItemKey, Boolean>(
                ReadoutItemKey.LmuWindows.VehicleDamage.Overheat to false,
            ),
        )

        damageChannel.send(noDamage())
        damageChannel.send(noDamage(overheating = true))

        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }

    @Test
    fun `BLUE_FLAGが無効のときは青旗を読み上げない`() = runTest(testDispatcher) {
        val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
            flagChannel = flagChannel,
            ttsEngine = tts,
            flagEnabledOverrides = mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.LmuWindows.Flag.BlueFlag to false),
        )

        flagChannel.send(clearFlags())
        flagChannel.send(clearFlags(playerFlag = PrimaryFlag.BLUE))

        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }

    @Test
    fun `フラッグが無効のときは詳細フラッグ設定が有効でも読み上げない`() = runTest(testDispatcher) {
        val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
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

        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }

    @Test
    fun `青旗読み上げが発生したら現在と直前のテレメトリを保存する`() = runTest(testDispatcher) {
        var fakeTime = 0L
        val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
        val telemetryLogRepository = FakeTelemetryLogRepository()
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
            flagChannel = flagChannel,
            ttsEngine = tts,
            currentTimeMs = { fakeTime },
            telemetryLogRepository = telemetryLogRepository,
        )

        flagChannel.send(clearFlags())
        fakeTime = 789L
        flagChannel.send(clearFlags(playerFlag = PrimaryFlag.BLUE))

        assertEquals(
            listOf(
                TelemetryLog(
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
            telemetryLogRepository.logs.value,
        )
    }

    @Test
    fun `ログ保存に失敗しても以後の読み上げは継続する`() = runTest(testDispatcher) {
        val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
            flagChannel = flagChannel,
            ttsEngine = tts,
            telemetryLogRepository = FakeTelemetryLogRepository(throwOnSave = true),
        )

        flagChannel.send(clearFlags())
        flagChannel.send(clearFlags(playerFlag = PrimaryFlag.BLUE))
        flagChannel.send(clearFlags())
        flagChannel.send(clearFlags(gamePhase = SessionPhase.RED_FLAG))

        assertEquals(
            listOf<SpeechEvent>(SpeechEvent.BlueFlag, SpeechEvent.SessionStop),
            tts.spokenTexts,
        )
    }

    @Test
    fun `オーバーヒート読み上げが発生したら現在と直前のテレメトリを保存する`() = runTest(testDispatcher) {
        var fakeTime = 0L
        val damageChannel = Channel<LmuWindowsVehicleDamageData>(Channel.UNLIMITED)
        val telemetryLogRepository = FakeTelemetryLogRepository()
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
            damageChannel = damageChannel,
            ttsEngine = tts,
            currentTimeMs = { fakeTime },
            telemetryLogRepository = telemetryLogRepository,
        )

        damageChannel.send(noDamage())
        fakeTime = 987L
        damageChannel.send(noDamage(overheating = true))

        assertEquals(
            listOf(
                TelemetryLog(
                    createdAt = 987L,
                    simulatorId = Simulator.LmuWindows.id,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleDamage.Root.value,
                    telemetryJson =
                        """{"previous":{"overheating":false,"partDetached":false,"lastImpactMagnitude":0.0},""" +
                            """"current":{"overheating":true,"partDetached":false,"lastImpactMagnitude":0.0}}""",
                ),
            ),
            telemetryLogRepository.logs.value,
        )
    }

    // --- タイヤ温度 ---

    @Test
    fun `閾値以上のタイヤ温度が来ると TyreOverheat を読み上げる`() = runTest(testDispatcher) {
        val channel = Channel<LmuWindowsTyreCarcassTemperatureData>(Channel.UNLIMITED)
        val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
            tyreTemperatureChannel = channel,
            flagChannel = flagChannel,
            ttsEngine = tts,
            tyreTemperatureHighThreshold = 90,
            enabledOverrides = mapOf(ReadoutItemKey.LmuWindows.TyreTemperature.Root to true),
        )
        flagChannel.send(clearFlags())

        channel.send(tyreTemperature(fl = 95.0))

        assertEquals(listOf<SpeechEvent>(SpeechEvent.TyreOverheat), tts.spokenTexts)
    }

    @Test
    fun `高温状態が継続しても2回目は読み上げない`() = runTest(testDispatcher) {
        val channel = Channel<LmuWindowsTyreCarcassTemperatureData>(Channel.UNLIMITED)
        val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
            tyreTemperatureChannel = channel,
            flagChannel = flagChannel,
            ttsEngine = tts,
            tyreTemperatureHighThreshold = 90,
            enabledOverrides = mapOf(ReadoutItemKey.LmuWindows.TyreTemperature.Root to true),
        )
        flagChannel.send(clearFlags())

        channel.send(tyreTemperature(fl = 95.0))
        channel.send(tyreTemperature(fl = 95.0))

        assertEquals(listOf<SpeechEvent>(SpeechEvent.TyreOverheat), tts.spokenTexts)
    }

    @Test
    fun `全タイヤが閾値以下に戻ると再度読み上げ可能になる`() = runTest(testDispatcher) {
        val channel = Channel<LmuWindowsTyreCarcassTemperatureData>(Channel.UNLIMITED)
        val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
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

        assertEquals(listOf<SpeechEvent>(SpeechEvent.TyreOverheat, SpeechEvent.TyreOverheat), tts.spokenTexts)
    }

    @Test
    fun `タイヤ温度項目が無効なら読み上げない`() = runTest(testDispatcher) {
        val channel = Channel<LmuWindowsTyreCarcassTemperatureData>(Channel.UNLIMITED)
        val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
            tyreTemperatureChannel = channel,
            flagChannel = flagChannel,
            ttsEngine = tts,
            tyreTemperatureHighThreshold = 90,
            enabledOverrides = mapOf(ReadoutItemKey.LmuWindows.TyreTemperature.Root to false),
        )
        flagChannel.send(clearFlags())

        channel.send(tyreTemperature(fl = 95.0))

        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }

    @Test
    fun `過熱警告スイッチがOFFのときは読み上げない`() = runTest(testDispatcher) {
        val channel = Channel<LmuWindowsTyreCarcassTemperatureData>(Channel.UNLIMITED)
        val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
            tyreTemperatureChannel = channel,
            flagChannel = flagChannel,
            ttsEngine = tts,
            tyreTemperatureHighThreshold = 90,
            enabledOverrides = mapOf(ReadoutItemKey.LmuWindows.TyreTemperature.Root to true),
            tyreTemperatureOverheatWarningEnabled = false,
        )
        flagChannel.send(clearFlags())

        channel.send(tyreTemperature(fl = 95.0))

        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }

    @Test
    fun `LMU非選択時はタイヤ温度アナウンスをしない`() = runTest(testDispatcher) {
        val channel = Channel<LmuWindowsTyreCarcassTemperatureData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(tyreTemperatureChannel = channel, ttsEngine = tts, simulator = null)

        channel.send(tyreTemperature(fl = 95.0))

        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }

    @Test
    fun `タイヤ温度が未設定（デフォルト）の場合は読み上げない`() = runTest(testDispatcher) {
        val channel = Channel<LmuWindowsTyreCarcassTemperatureData>(Channel.UNLIMITED)
        val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
            tyreTemperatureChannel = channel,
            flagChannel = flagChannel,
            ttsEngine = tts,
            tyreTemperatureHighThreshold = 90,
        )
        flagChannel.send(clearFlags())

        channel.send(tyreTemperature(fl = 95.0))

        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }

    @Test
    fun `タイヤ温度読み上げが発生したらテレメトリを保存する`() = runTest(testDispatcher) {
        val channel = Channel<LmuWindowsTyreCarcassTemperatureData>(Channel.UNLIMITED)
        val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
        val telemetryLogRepository = FakeTelemetryLogRepository()
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
            tyreTemperatureChannel = channel,
            flagChannel = flagChannel,
            ttsEngine = tts,
            tyreTemperatureHighThreshold = 90,
            enabledOverrides = mapOf(ReadoutItemKey.LmuWindows.TyreTemperature.Root to true),
            currentTimeMs = { 123L },
            telemetryLogRepository = telemetryLogRepository,
        )
        flagChannel.send(clearFlags())

        channel.send(tyreTemperature(fl = 95.0))

        assertEquals(1, telemetryLogRepository.logs.value.size)
        val log = telemetryLogRepository.logs.value.first()
        assertEquals(123L, log.createdAt)
        assertEquals(Simulator.LmuWindows.id, log.simulatorId)
        assertEquals(ReadoutItemKey.LmuWindows.TyreTemperature.Root.value, log.readoutItemKey)
    }

    // --- タイヤ低温警告 ---

    @Test
    fun `ガレージに遷移した瞬間に低温タイヤがあるとTyreColdを読み上げる`() = runTest(testDispatcher) {
        val channel = Channel<LmuWindowsTyreCarcassTemperatureData>(Channel.UNLIMITED)
        val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
            tyreTemperatureChannel = channel,
            flagChannel = flagChannel,
            ttsEngine = tts,
            enabledOverrides = mapOf(ReadoutItemKey.LmuWindows.TyreTemperature.Root to true),
        )
        flagChannel.send(clearFlags(gamePhase = SessionPhase.GREEN_FLAG))
        channel.send(tyreTemperature(fl = 55.0))

        flagChannel.send(clearFlags(gamePhase = SessionPhase.GARAGE))

        assertEquals(listOf<SpeechEvent>(SpeechEvent.TyreCold), tts.spokenTexts)
    }

    @Test
    fun `gamePhaseが変化しなければ低温でも読み上げない`() = runTest(testDispatcher) {
        val channel = Channel<LmuWindowsTyreCarcassTemperatureData>(Channel.UNLIMITED)
        val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
            tyreTemperatureChannel = channel,
            flagChannel = flagChannel,
            ttsEngine = tts,
            enabledOverrides = mapOf(ReadoutItemKey.LmuWindows.TyreTemperature.Root to true),
        )
        flagChannel.send(clearFlags(gamePhase = SessionPhase.GARAGE))
        channel.send(tyreTemperature(fl = 55.0))

        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }

    @Test
    fun `低温警告スイッチがOFFのときは読み上げない`() = runTest(testDispatcher) {
        val channel = Channel<LmuWindowsTyreCarcassTemperatureData>(Channel.UNLIMITED)
        val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
            tyreTemperatureChannel = channel,
            flagChannel = flagChannel,
            ttsEngine = tts,
            enabledOverrides = mapOf(ReadoutItemKey.LmuWindows.TyreTemperature.Root to true),
            tyreTemperatureLowWarningEnabled = false,
        )
        flagChannel.send(clearFlags(gamePhase = SessionPhase.GREEN_FLAG))
        channel.send(tyreTemperature(fl = 55.0))

        flagChannel.send(clearFlags(gamePhase = SessionPhase.GARAGE))

        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }

    @Test
    fun `detailPaneで選択解除したgamePhaseに遷移しても読み上げない`() = runTest(testDispatcher) {
        val channel = Channel<LmuWindowsTyreCarcassTemperatureData>(Channel.UNLIMITED)
        val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
            tyreTemperatureChannel = channel,
            flagChannel = flagChannel,
            ttsEngine = tts,
            enabledOverrides = mapOf(ReadoutItemKey.LmuWindows.TyreTemperature.Root to true),
            tyreTemperatureLowWarningPhasesOverride = mapOf(SessionPhase.GARAGE to false),
        )
        flagChannel.send(clearFlags(gamePhase = SessionPhase.GREEN_FLAG))
        channel.send(tyreTemperature(fl = 55.0))

        flagChannel.send(clearFlags(gamePhase = SessionPhase.GARAGE))

        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }

    @Test
    fun `低温警告読み上げが発生したらテレメトリを保存する`() = runTest(testDispatcher) {
        val channel = Channel<LmuWindowsTyreCarcassTemperatureData>(Channel.UNLIMITED)
        val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
        val telemetryLogRepository = FakeTelemetryLogRepository()
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
            tyreTemperatureChannel = channel,
            flagChannel = flagChannel,
            ttsEngine = tts,
            enabledOverrides = mapOf(ReadoutItemKey.LmuWindows.TyreTemperature.Root to true),
            currentTimeMs = { 123L },
            telemetryLogRepository = telemetryLogRepository,
        )
        flagChannel.send(clearFlags(gamePhase = SessionPhase.GREEN_FLAG))
        channel.send(tyreTemperature(fl = 55.0))

        flagChannel.send(clearFlags(gamePhase = SessionPhase.GARAGE))

        assertEquals(1, telemetryLogRepository.logs.value.size)
        val log = telemetryLogRepository.logs.value.first()
        assertEquals(123L, log.createdAt)
        assertEquals(Simulator.LmuWindows.id, log.simulatorId)
        assertEquals(ReadoutItemKey.LmuWindows.TyreTemperature.Root.value, log.readoutItemKey)
    }
}

private fun noProximity() = LmuWindowsProximityData(
    sideBySideLeftVehicleIds = emptySet(),
    sideBySideRightVehicleIds = emptySet(),
    lateralDistanceLeftMeters = Double.MAX_VALUE,
    lateralDistanceRightMeters = Double.MAX_VALUE,
)

private fun leftProximity(vehicleId: Int) = LmuWindowsProximityData(
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

private class RecordingTextToSpeechEngine : TextToSpeechEngine {
    val spokenTexts = mutableListOf<SpeechEvent>()
    override val currentReadoutItemKey: ReadoutItemKey? = null
    override fun speak(event: SpeechEvent, queue: Boolean) { spokenTexts.add(event) }
    override fun stop() = Unit
    override fun previewStartSound(type: ReadoutStartSoundType) = Unit
}

/** 優先度テスト用: 再生中キーを手動で制御できる TTS エンジン */
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

private class FakeChannelProximityRepository(
    private val stream: Flow<LmuWindowsProximityData>,
) : LmuWindowsProximityRepository {
    override fun proximityStream(): Flow<LmuWindowsProximityData> = stream
}

private class FakeChannelFlagRepository(
    private val stream: Flow<LmuWindowsRaceFlagsData>,
) : LmuWindowsFlagRepository {
    override fun flagStream(): Flow<LmuWindowsRaceFlagsData> = stream
}

private class FakeConstantSimulatorRepository(
    private val simulator: Simulator?,
) : SimulatorPreferencesRepository {
    override fun selectedSimulator(): Flow<Simulator?> = MutableStateFlow(simulator)
    override suspend fun saveSelectedSimulator(simulator: Simulator) = Unit
}

private class FakeAllEnabledReadoutPreferencesRepository(
    private val enabledOverrides: Map<ReadoutItemKey, Boolean> = emptyMap(),
    private val orderOverride: List<ReadoutItemKey> = listOf(
        ReadoutItemKey.LmuWindows.Flag.Root,
        ReadoutItemKey.LmuWindows.VehicleApproach.Root,
    ),
) : ReadoutPreferencesRepository {
    override fun observeReadoutEnabledStates(simulator: String): Flow<Map<ReadoutItemKey, Boolean>> =
        MutableStateFlow(enabledOverrides)

    override suspend fun saveReadoutEnabledState(simulator: String, key: ReadoutItemKey, enabled: Boolean) = Unit
    override fun observeReadoutOrder(simulator: String): Flow<List<ReadoutItemKey>> = MutableStateFlow(orderOverride)
    override suspend fun saveReadoutOrder(simulator: String, order: List<ReadoutItemKey>) = Unit
}

private class FakeLmuWindowsFlagPreferencesRepository(
    private val enabledOverrides: Map<ReadoutItemKey, Boolean> = emptyMap(),
) : LmuWindowsFlagPreferencesRepository {
    override fun observeFlagEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>> =
        MutableStateFlow(enabledOverrides)

    override suspend fun saveFlagEnabledState(key: ReadoutItemKey, enabled: Boolean) = Unit
}

private class FakeChannelLmuWindowsRepository(
    private val stream: Flow<LmuWindowsTelemetryData>,
) : LmuWindowsRepository {
    override fun telemetryStream(): Flow<LmuWindowsTelemetryData> = stream
    override suspend fun isConnected(): Boolean = true
    override suspend fun disconnect() = Unit
}

private class FakeLmuWindowsMyBestLapPreferencesRepository(
    initialVoiceType: MyBestLapVoiceType,
) : LmuWindowsMyBestLapPreferencesRepository {
    private val voiceType = MutableStateFlow(initialVoiceType)
    override fun observeVoiceType(): Flow<MyBestLapVoiceType> = voiceType
    override suspend fun saveVoiceType(type: MyBestLapVoiceType) {
        voiceType.value = type
    }
}

private class FakeConstantLmuWindowsVehicleApproachPreferencesRepository(
    private val skipFirstLap: Boolean,
    private val startReadoutEnabled: Boolean,
    private val startReadoutType: VehicleApproachStartReadoutType = VehicleApproachStartReadoutType.CAR_LEFT_RIGHT,
) : LmuWindowsVehicleApproachPreferencesRepository {
    override fun observeSkipFirstLap(): Flow<Boolean> = MutableStateFlow(skipFirstLap)
    override suspend fun saveSkipFirstLap(skip: Boolean) = Unit
    override fun observeStartReadoutEnabled(): Flow<Boolean> = MutableStateFlow(startReadoutEnabled)
    override suspend fun saveStartReadoutEnabled(enabled: Boolean) = Unit
    override fun observeStartReadoutType(): Flow<VehicleApproachStartReadoutType> =
        MutableStateFlow(startReadoutType)

    override suspend fun saveStartReadoutType(type: VehicleApproachStartReadoutType) = Unit
}

private class FakeLmuWindowsVehicleDamagePreferencesRepository(
    initialStates: Map<ReadoutItemKey, Boolean> = emptyMap(),
) : LmuWindowsVehicleDamagePreferencesRepository {
    private val states = MutableStateFlow(initialStates)
    override fun observeEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>> = states
    override suspend fun saveEnabledState(key: ReadoutItemKey, enabled: Boolean) {
        states.update { it + (key to enabled) }
    }
}

private class FakeChannelVehicleDamageRepository(
    private val stream: Flow<LmuWindowsVehicleDamageData>,
) : LmuWindowsVehicleDamageRepository {
    override fun vehicleDamageStream(): Flow<LmuWindowsVehicleDamageData> = stream
}

private class FakeTelemetryLogRepository(
    private val throwOnSave: Boolean = false,
) : TelemetryLogRepository {
    val logs = MutableStateFlow(emptyList<TelemetryLog>())
    override fun observeTelemetryLogs(): Flow<List<TelemetryLog>> = logs
    override fun observeTelemetryLogDetail(id: Long): Flow<TelemetryLogDetail?> =
        logs.map { logs ->
            val current = logs.firstOrNull { it.id == id } ?: return@map null
            TelemetryLogDetail(current = current, previous = null)
        }

    override suspend fun saveTelemetryLog(log: TelemetryLog) {
        if (throwOnSave) error("Failed to save telemetry log")
        logs.update { it + log }
    }

    override suspend fun deleteAllTelemetryLogs() {
        logs.update { emptyList() }
    }
}

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
        sector2Ms = 0L,
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

private class FakeChannelTyreCarcassTemperatureRepository(
    private val stream: Flow<LmuWindowsTyreCarcassTemperatureData>,
) : LmuWindowsTyreCarcassTemperatureRepository {
    override fun tyreCarcassTemperatureStream(): Flow<LmuWindowsTyreCarcassTemperatureData> = stream
}

private class FakeConstantLmuWindowsTyreTemperaturePreferencesRepository(
    private val threshold: Int,
    private val overheatWarningEnabled: Boolean = true,
    private val lowWarningEnabled: Boolean = true,
    private val lowWarningPhases: Map<SessionPhase, Boolean> = emptyMap(),
) : LmuWindowsTyreTemperaturePreferencesRepository {
    override fun observeHighThresholdCelsius(): Flow<Int> = MutableStateFlow(threshold)
    override suspend fun saveHighThresholdCelsius(celsius: Int) = Unit
    override fun observeEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>> =
        MutableStateFlow(
            mapOf(
                ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning to overheatWarningEnabled,
                ReadoutItemKey.LmuWindows.TyreTemperature.LowWarning to lowWarningEnabled,
            ),
        )
    override suspend fun saveEnabledState(key: ReadoutItemKey, enabled: Boolean) = Unit
    override fun observeLowWarningPhases(): Flow<Map<SessionPhase, Boolean>> = MutableStateFlow(lowWarningPhases)
    override suspend fun saveLowWarningPhases(phases: Set<SessionPhase>) = Unit
}
