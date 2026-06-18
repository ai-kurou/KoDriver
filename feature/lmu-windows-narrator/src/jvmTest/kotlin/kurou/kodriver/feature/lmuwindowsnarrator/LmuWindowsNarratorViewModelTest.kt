package kurou.kodriver.feature.lmuwindowsnarrator

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
import kurou.kodriver.domain.model.CountLapFlag
import kurou.kodriver.domain.model.EngineData
import kurou.kodriver.domain.model.FuelData
import kurou.kodriver.domain.model.InputsData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.PrimaryFlag
import kurou.kodriver.domain.model.ProximityData
import kurou.kodriver.domain.model.RaceFlagsData
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.SectorFlagState
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.SessionYellowFlagState
import kurou.kodriver.domain.model.TimingData
import kurou.kodriver.domain.model.TyreData
import kurou.kodriver.domain.model.VehicleApproachStartReadoutType
import kurou.kodriver.domain.model.VehicleDamageData
import kurou.kodriver.domain.model.VehicleData
import kurou.kodriver.domain.repository.FlagPreferencesRepository
import kurou.kodriver.domain.repository.FlagRepository
import kurou.kodriver.domain.repository.LmuWindowsRepository
import kurou.kodriver.domain.repository.ProximityRepository
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.repository.VehicleApproachPreferencesRepository
import kurou.kodriver.domain.repository.VehicleDamagePreferencesRepository
import kurou.kodriver.domain.repository.VehicleDamageRepository
import kurou.kodriver.domain.usecase.ObserveFlagEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsUseCase
import kurou.kodriver.domain.usecase.ObserveProximityUseCase
import kurou.kodriver.domain.usecase.ObserveRaceFlagsUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.ObserveSkipFirstLapUseCase
import kurou.kodriver.domain.usecase.ObserveVehicleApproachStartReadoutEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveVehicleDamageEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveVehicleDamageUseCase
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
        proximityChannel: Channel<ProximityData> = Channel(Channel.UNLIMITED),
        flagChannel: Channel<RaceFlagsData> = Channel(Channel.UNLIMITED),
        damageChannel: Channel<VehicleDamageData> = Channel(Channel.UNLIMITED),
        telemetryChannel: Channel<LmuWindowsTelemetryData> = Channel(Channel.UNLIMITED),
        ttsEngine: TextToSpeechEngine,
        enabledOverrides: Map<String, Boolean> = emptyMap(),
        flagEnabledOverrides: Map<String, Boolean> = emptyMap(),
        vehicleDamageEnabledOverrides: Map<String, Boolean> = emptyMap(),
        orderOverride: List<String> = listOf(ReadoutItemKey.FLAG, ReadoutItemKey.VEHICLE_APPROACH),
        skipFirstLap: Boolean = false,
        startReadoutEnabled: Boolean = true,
        simulator: String? = "lmu_windows",
    ): LmuWindowsNarratorViewModel {
        val readoutRepo = FakeAllEnabledReadoutPreferencesRepository(enabledOverrides, orderOverride)
        val vehicleApproachPreferencesRepository = FakeConstantVehicleApproachPreferencesRepository(
            skipFirstLap = skipFirstLap,
            startReadoutEnabled = startReadoutEnabled,
        )
        return LmuWindowsNarratorViewModel(
            vehicleApproachUseCases = VehicleApproachUseCases(
                observeProximity = ObserveProximityUseCase(
                    FakeChannelProximityRepository(proximityChannel.receiveAsFlow()),
                ),
                observeLmuWindows = ObserveLmuWindowsUseCase(
                    FakeChannelLmuWindowsRepository(telemetryChannel.receiveAsFlow()),
                ),
                observeSkipFirstLap = ObserveSkipFirstLapUseCase(
                    vehicleApproachPreferencesRepository,
                ),
                observeStartReadoutEnabled = ObserveVehicleApproachStartReadoutEnabledUseCase(
                    vehicleApproachPreferencesRepository,
                ),
            ),
            vehicleDamageUseCases = VehicleDamageUseCases(
                observeVehicleDamage = ObserveVehicleDamageUseCase(
                    FakeChannelVehicleDamageRepository(damageChannel.receiveAsFlow()),
                ),
                observeVehicleDamageEnabledStates = ObserveVehicleDamageEnabledStatesUseCase(
                    FakeVehicleDamagePreferencesRepository(vehicleDamageEnabledOverrides),
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
                observeRaceFlags = ObserveRaceFlagsUseCase(
                    FakeChannelFlagRepository(flagChannel.receiveAsFlow()),
                ),
                observeFlagEnabledStates = ObserveFlagEnabledStatesUseCase(
                    FakeFlagPreferencesRepository(flagEnabledOverrides),
                ),
            ),
            ttsEngine = ttsEngine,
        )
    }

    // --- シミュレータ選択 ---

    @Test
    fun `LMU非選択時は接近アナウンスをしない`() = runTest(testDispatcher) {
        val channel = Channel<ProximityData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(proximityChannel = channel, ttsEngine = tts, simulator = "other")

        channel.send(noProximity())
        channel.send(leftProximity(vehicleId = 1))

        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }

    @Test
    fun `LMU非選択時は旗アナウンスをしない`() = runTest(testDispatcher) {
        val flagChannel = Channel<RaceFlagsData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(flagChannel = flagChannel, ttsEngine = tts, simulator = "other")

        flagChannel.send(clearFlags())
        flagChannel.send(clearFlags(playerFlag = PrimaryFlag.BLUE))

        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }

    // --- 接近アナウンス ---

    @Test
    fun `左から接近するとCarLeftを読み上げる`() = runTest(testDispatcher) {
        val channel = Channel<ProximityData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(proximityChannel = channel, ttsEngine = tts)

        channel.send(noProximity())
        channel.send(leftProximity(vehicleId = 1))

        assertEquals(listOf<SpeechEvent>(SpeechEvent.CarLeft), tts.spokenTexts)
    }

    @Test
    fun `右から接近するとCarRightを読み上げる`() = runTest(testDispatcher) {
        val channel = Channel<ProximityData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(proximityChannel = channel, ttsEngine = tts)

        channel.send(noProximity())
        channel.send(rightProximity(vehicleId = 1))

        assertEquals(listOf<SpeechEvent>(SpeechEvent.CarRight), tts.spokenTexts)
    }

    @Test
    fun `接近開始時の読み上げが無効のときは接近アナウンスをしない`() = runTest(testDispatcher) {
        val channel = Channel<ProximityData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
            proximityChannel = channel,
            ttsEngine = tts,
            startReadoutEnabled = false,
        )

        channel.send(noProximity())
        channel.send(leftProximity(vehicleId = 1))

        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }

    @Test
    fun `既に並走中の車が継続して並走してもアナウンスしない`() = runTest(testDispatcher) {
        val channel = Channel<ProximityData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(proximityChannel = channel, ttsEngine = tts)

        channel.send(noProximity())
        channel.send(leftProximity(vehicleId = 1))
        val spokenAfterFirstApproach = tts.spokenTexts.toList()

        channel.send(leftProximity(vehicleId = 1))

        assertEquals(spokenAfterFirstApproach, tts.spokenTexts)
    }

    @Test
    fun `並走から離脱後に同じ車が再度並走するとアナウンスする`() = runTest(testDispatcher) {
        val channel = Channel<ProximityData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(proximityChannel = channel, ttsEngine = tts)

        channel.send(noProximity())
        channel.send(leftProximity(vehicleId = 1))
        channel.send(noProximity())
        channel.send(leftProximity(vehicleId = 1))

        assertEquals(listOf<SpeechEvent>(SpeechEvent.CarLeft, SpeechEvent.CarLeft), tts.spokenTexts)
    }

    @Test
    fun `別の車両が新たに並走ゾーンに入るとアナウンスする`() = runTest(testDispatcher) {
        val channel = Channel<ProximityData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(proximityChannel = channel, ttsEngine = tts)

        channel.send(noProximity())
        channel.send(leftProximity(vehicleId = 1))
        channel.send(leftProximity(vehicleId = 1, extraLeftId = 2))

        assertEquals(listOf<SpeechEvent>(SpeechEvent.CarLeft, SpeechEvent.CarLeft), tts.spokenTexts)
    }

    @Test
    fun `VEHICLE_APPROACHが無効のときはアナウンスしない`() = runTest(testDispatcher) {
        val channel = Channel<ProximityData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
            proximityChannel = channel,
            ttsEngine = tts,
            enabledOverrides = mapOf(ReadoutItemKey.VEHICLE_APPROACH to false),
        )

        channel.send(noProximity())
        channel.send(leftProximity(vehicleId = 1))

        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }

    @Test
    fun `1周目スキップONかつ現在ラップが0のときはアナウンスしない`() = runTest(testDispatcher) {
        val proximityChannel = Channel<ProximityData>(Channel.UNLIMITED)
        val telemetryChannel = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
            proximityChannel = proximityChannel,
            telemetryChannel = telemetryChannel,
            ttsEngine = tts,
            skipFirstLap = true,
        )

        // mLapNumber は 0 スタートのため、1周目（最初の計測周）は 0
        telemetryChannel.send(fakeTelemetryData(currentLap = 0))
        proximityChannel.send(noProximity())
        proximityChannel.send(leftProximity(vehicleId = 1))

        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }

    @Test
    fun `1周目スキップONかつフォーメーションラップ中はアナウンスしない`() = runTest(testDispatcher) {
        val proximityChannel = Channel<ProximityData>(Channel.UNLIMITED)
        val telemetryChannel = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
            proximityChannel = proximityChannel,
            telemetryChannel = telemetryChannel,
            ttsEngine = tts,
            skipFirstLap = true,
        )

        // フォーメーションラップは mLapNumber が負値（-1 等）になる可能性があるため <= 0 でスキップ
        telemetryChannel.send(fakeTelemetryData(currentLap = -1))
        proximityChannel.send(noProximity())
        proximityChannel.send(leftProximity(vehicleId = 1))

        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }

    @Test
    fun `1周目スキップONでも2周目以降はアナウンスする`() = runTest(testDispatcher) {
        val proximityChannel = Channel<ProximityData>(Channel.UNLIMITED)
        val telemetryChannel = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
            proximityChannel = proximityChannel,
            telemetryChannel = telemetryChannel,
            ttsEngine = tts,
            skipFirstLap = true,
        )

        // mLapNumber は 0 スタートのため、2周目は 1
        telemetryChannel.send(fakeTelemetryData(currentLap = 1))
        proximityChannel.send(noProximity())
        proximityChannel.send(leftProximity(vehicleId = 1))

        assertEquals(listOf<SpeechEvent>(SpeechEvent.CarLeft), tts.spokenTexts)
    }

    @Test
    fun `1周目スキップOFFのときは1周目でもアナウンスする`() = runTest(testDispatcher) {
        val proximityChannel = Channel<ProximityData>(Channel.UNLIMITED)
        val telemetryChannel = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
            proximityChannel = proximityChannel,
            telemetryChannel = telemetryChannel,
            ttsEngine = tts,
            skipFirstLap = false,
        )

        // mLapNumber は 0 スタートのため、1周目は 0
        telemetryChannel.send(fakeTelemetryData(currentLap = 0))
        proximityChannel.send(noProximity())
        proximityChannel.send(leftProximity(vehicleId = 1))

        assertEquals(listOf<SpeechEvent>(SpeechEvent.CarLeft), tts.spokenTexts)
    }

    // --- 旗アナウンス ---

    @Test
    fun `青旗に変化するとブルーフラッグを読み上げる`() = runTest(testDispatcher) {
        val flagChannel = Channel<RaceFlagsData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(flagChannel = flagChannel, ttsEngine = tts)

        flagChannel.send(clearFlags())
        flagChannel.send(clearFlags(playerFlag = PrimaryFlag.BLUE))

        assertEquals(listOf<SpeechEvent>(SpeechEvent.BlueFlag), tts.spokenTexts)
    }

    @Test
    fun `青旗が継続中は再度読み上げない`() = runTest(testDispatcher) {
        val flagChannel = Channel<RaceFlagsData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(flagChannel = flagChannel, ttsEngine = tts)

        flagChannel.send(clearFlags())
        flagChannel.send(clearFlags(playerFlag = PrimaryFlag.BLUE))
        flagChannel.send(clearFlags(playerFlag = PrimaryFlag.BLUE))

        assertEquals(listOf<SpeechEvent>(SpeechEvent.BlueFlag), tts.spokenTexts)
    }

    @Test
    fun `セクター黄旗発生でイエローフラッグを読み上げる`() = runTest(testDispatcher) {
        val flagChannel = Channel<RaceFlagsData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(flagChannel = flagChannel, ttsEngine = tts)

        flagChannel.send(clearFlags())
        flagChannel.send(
            clearFlags().copy(
                sectorFlags = listOf(SectorFlagState.YELLOW, SectorFlagState.CLEAR, SectorFlagState.CLEAR),
            ),
        )

        assertEquals(listOf<SpeechEvent>(SpeechEvent.YellowFlag), tts.spokenTexts)
    }

    @Test
    fun `FCY開始でフルコースイエローを読み上げる`() = runTest(testDispatcher) {
        val flagChannel = Channel<RaceFlagsData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(flagChannel = flagChannel, ttsEngine = tts)

        flagChannel.send(clearFlags())
        flagChannel.send(clearFlags(gamePhase = SessionPhase.FULL_COURSE_YELLOW))

        assertEquals(listOf<SpeechEvent>(SpeechEvent.FullCourseYellow), tts.spokenTexts)
    }

    @Test
    fun `セッションストップでセッションストップを読み上げる`() = runTest(testDispatcher) {
        val flagChannel = Channel<RaceFlagsData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(flagChannel = flagChannel, ttsEngine = tts)

        flagChannel.send(clearFlags())
        flagChannel.send(clearFlags(gamePhase = SessionPhase.RED_FLAG))

        assertEquals(listOf<SpeechEvent>(SpeechEvent.SessionStop), tts.spokenTexts)
    }

    // --- 優先度 ---

    @Test
    fun `フラグ読み上げ中に車両接近イベントが来ても読み上げない`() = runTest(testDispatcher) {
        val channel = Channel<ProximityData>(Channel.UNLIMITED)
        val tts = PriorityAwareTextToSpeechEngine(
            initialKey = ReadoutItemKey.FLAG,
        )
        buildViewModel(proximityChannel = channel, ttsEngine = tts)

        channel.send(noProximity())
        channel.send(leftProximity(vehicleId = 1))

        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }

    @Test
    fun `車両接近読み上げ中にフラグイベントが来ると読み上げを停止して割り込む`() = runTest(testDispatcher) {
        val flagChannel = Channel<RaceFlagsData>(Channel.UNLIMITED)
        val tts = PriorityAwareTextToSpeechEngine(
            initialKey = ReadoutItemKey.VEHICLE_APPROACH,
        )
        buildViewModel(flagChannel = flagChannel, ttsEngine = tts)

        flagChannel.send(clearFlags())
        flagChannel.send(clearFlags(playerFlag = PrimaryFlag.BLUE))

        assertEquals(true, tts.stopCalled)
        assertEquals(listOf<SpeechEvent>(SpeechEvent.BlueFlag), tts.spokenTexts)
    }

    // --- オーバーヒート ---

    @Test
    fun `オーバーヒートがfalseからtrueに変化するとOverheatingを読み上げる`() = runTest(testDispatcher) {
        val damageChannel = Channel<VehicleDamageData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(damageChannel = damageChannel, ttsEngine = tts)

        damageChannel.send(noDamage())
        damageChannel.send(noDamage(overheating = true))

        assertEquals(listOf<SpeechEvent>(SpeechEvent.Overheating), tts.spokenTexts)
    }

    @Test
    fun `オーバーヒートが継続中は再度読み上げない`() = runTest(testDispatcher) {
        val damageChannel = Channel<VehicleDamageData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(damageChannel = damageChannel, ttsEngine = tts)

        damageChannel.send(noDamage())
        damageChannel.send(noDamage(overheating = true))
        damageChannel.send(noDamage(overheating = true))

        assertEquals(listOf<SpeechEvent>(SpeechEvent.Overheating), tts.spokenTexts)
    }

    @Test
    fun `LMU非選択時はオーバーヒートアナウンスをしない`() = runTest(testDispatcher) {
        val damageChannel = Channel<VehicleDamageData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(damageChannel = damageChannel, ttsEngine = tts, simulator = "other")

        damageChannel.send(noDamage())
        damageChannel.send(noDamage(overheating = true))

        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }

    @Test
    fun `OVERHEATが無効のときはオーバーヒートを読み上げない`() = runTest(testDispatcher) {
        val damageChannel = Channel<VehicleDamageData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
            damageChannel = damageChannel,
            ttsEngine = tts,
            vehicleDamageEnabledOverrides = mapOf(ReadoutItemKey.OVERHEAT to false),
        )

        damageChannel.send(noDamage())
        damageChannel.send(noDamage(overheating = true))

        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }

    @Test
    fun `BLUE_FLAGが無効のときは青旗を読み上げない`() = runTest(testDispatcher) {
        val flagChannel = Channel<RaceFlagsData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
            flagChannel = flagChannel,
            ttsEngine = tts,
            flagEnabledOverrides = mapOf(ReadoutItemKey.BLUE_FLAG to false),
        )

        flagChannel.send(clearFlags())
        flagChannel.send(clearFlags(playerFlag = PrimaryFlag.BLUE))

        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }
}

private fun noProximity() = ProximityData(
    sideBySideLeftVehicleIds = emptySet(),
    sideBySideRightVehicleIds = emptySet(),
    lateralDistanceLeftMeters = Double.MAX_VALUE,
    lateralDistanceRightMeters = Double.MAX_VALUE,
)

private fun leftProximity(vehicleId: Int, extraLeftId: Int? = null) = ProximityData(
    sideBySideLeftVehicleIds = setOfNotNull(vehicleId, extraLeftId),
    sideBySideRightVehicleIds = emptySet(),
    lateralDistanceLeftMeters = 3.0,
    lateralDistanceRightMeters = Double.MAX_VALUE,
)

private fun rightProximity(vehicleId: Int) = ProximityData(
    sideBySideLeftVehicleIds = emptySet(),
    sideBySideRightVehicleIds = setOf(vehicleId),
    lateralDistanceLeftMeters = Double.MAX_VALUE,
    lateralDistanceRightMeters = 3.0,
)

private fun clearFlags(
    gamePhase: SessionPhase = SessionPhase.GREEN_FLAG,
    playerFlag: PrimaryFlag = PrimaryFlag.GREEN,
) = RaceFlagsData(
    gamePhase = gamePhase,
    yellowFlagState = SessionYellowFlagState.NONE,
    sectorFlags = listOf(SectorFlagState.CLEAR, SectorFlagState.CLEAR, SectorFlagState.CLEAR),
    startLight = 0,
    numRedLights = 0,
    playerFlag = playerFlag,
    playerUnderYellow = false,
    playerCountLapFlag = CountLapFlag.DO_NOT_COUNT_LAP_OR_TIME,
)

private class RecordingTextToSpeechEngine : TextToSpeechEngine {
    val spokenTexts = mutableListOf<SpeechEvent>()
    override val currentReadoutItemKey: String? = null
    override fun speak(event: SpeechEvent, queue: Boolean) { spokenTexts.add(event) }
    override fun stop() = Unit
}

/** 優先度テスト用: 再生中キーを手動で制御できる TTS エンジン */
private class PriorityAwareTextToSpeechEngine(
    initialKey: String? = null,
) : TextToSpeechEngine {
    val spokenTexts = mutableListOf<SpeechEvent>()
    var stopCalled = false
    override var currentReadoutItemKey: String? = initialKey
    override fun speak(event: SpeechEvent, queue: Boolean) { spokenTexts.add(event) }
    override fun stop() {
        stopCalled = true
        currentReadoutItemKey = null
    }
}

private class FakeChannelProximityRepository(
    private val stream: Flow<ProximityData>,
) : ProximityRepository {
    override fun proximityStream(): Flow<ProximityData> = stream
}

private class FakeChannelFlagRepository(
    private val stream: Flow<RaceFlagsData>,
) : FlagRepository {
    override fun flagStream(): Flow<RaceFlagsData> = stream
}

private class FakeConstantSimulatorRepository(
    private val simulator: String?,
) : SimulatorPreferencesRepository {
    override fun selectedSimulator(): Flow<String?> = MutableStateFlow(simulator)
    override suspend fun saveSelectedSimulator(simulator: String) = Unit
}

private class FakeAllEnabledReadoutPreferencesRepository(
    private val enabledOverrides: Map<String, Boolean> = emptyMap(),
    private val orderOverride: List<String> = listOf(ReadoutItemKey.FLAG, ReadoutItemKey.VEHICLE_APPROACH),
) : ReadoutPreferencesRepository {
    override fun observeReadoutEnabledStates(simulator: String): Flow<Map<String, Boolean>> =
        MutableStateFlow(enabledOverrides)

    override suspend fun saveReadoutEnabledState(simulator: String, label: String, enabled: Boolean) = Unit
    override fun observeReadoutOrder(simulator: String): Flow<List<String>> = MutableStateFlow(orderOverride)
    override suspend fun saveReadoutOrder(simulator: String, order: List<String>) = Unit
}

private class FakeFlagPreferencesRepository(
    private val enabledOverrides: Map<String, Boolean> = emptyMap(),
) : FlagPreferencesRepository {
    override fun observeFlagEnabledStates(): Flow<Map<String, Boolean>> =
        MutableStateFlow(enabledOverrides)

    override suspend fun saveFlagEnabledState(key: String, enabled: Boolean) = Unit
}

private class FakeChannelLmuWindowsRepository(
    private val stream: Flow<LmuWindowsTelemetryData>,
) : LmuWindowsRepository {
    override fun telemetryStream(): Flow<LmuWindowsTelemetryData> = stream
    override suspend fun isConnected(): Boolean = true
    override suspend fun disconnect() = Unit
}

private class FakeConstantVehicleApproachPreferencesRepository(
    private val skipFirstLap: Boolean,
    private val startReadoutEnabled: Boolean,
) : VehicleApproachPreferencesRepository {
    override fun observeSkipFirstLap(): Flow<Boolean> = MutableStateFlow(skipFirstLap)
    override suspend fun saveSkipFirstLap(skip: Boolean) = Unit
    override fun observeStartReadoutEnabled(): Flow<Boolean> = MutableStateFlow(startReadoutEnabled)
    override suspend fun saveStartReadoutEnabled(enabled: Boolean) = Unit
    override fun observeStartReadoutType(): Flow<VehicleApproachStartReadoutType> =
        MutableStateFlow(VehicleApproachStartReadoutType.CAR_LEFT_RIGHT)

    override suspend fun saveStartReadoutType(type: VehicleApproachStartReadoutType) = Unit
}

private class FakeVehicleDamagePreferencesRepository(
    initialStates: Map<String, Boolean> = emptyMap(),
) : VehicleDamagePreferencesRepository {
    private val states = MutableStateFlow(initialStates)
    override fun observeEnabledStates(): Flow<Map<String, Boolean>> = states
    override suspend fun saveEnabledState(key: String, enabled: Boolean) {
        states.update { it + (key to enabled) }
    }
}

private class FakeChannelVehicleDamageRepository(
    private val stream: Flow<VehicleDamageData>,
) : VehicleDamageRepository {
    override fun vehicleDamageStream(): Flow<VehicleDamageData> = stream
}

private fun noDamage(overheating: Boolean = false) = VehicleDamageData(
    overheating = overheating,
    partDetached = false,
    lastImpactMagnitude = 0.0,
)

private fun fakeTelemetryData(currentLap: Int) = LmuWindowsTelemetryData(
    timestampMs = 0L,
    engine = EngineData(rpm = 0.0, maxRpm = 0.0, gear = 0),
    inputs = InputsData(throttle = 0.0, brake = 0.0, clutch = 0.0, steering = 0.0),
    tyres = TyreData(wheels = emptyMap()),
    fuel = FuelData(currentLiters = 0.0, capacityLiters = 0.0),
    timing = TimingData(
        currentLapTimeMs = 0L,
        lastLapTimeMs = 0L,
        bestLapTimeMs = 0L,
        sector1Ms = 0L,
        sector2Ms = 0L,
        currentLap = currentLap,
        maxLaps = 0,
    ),
    vehicle = VehicleData(
        localVelocityX = 0.0,
        localVelocityY = 0.0,
        localVelocityZ = 0.0,
        positionX = 0.0,
        positionY = 0.0,
        positionZ = 0.0,
    ),
)
