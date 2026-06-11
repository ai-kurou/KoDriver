package kurou.kodriver.feature.narrator

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.CountLapFlag
import kurou.kodriver.domain.model.PrimaryFlag
import kurou.kodriver.domain.model.ProximityData
import kurou.kodriver.domain.model.RaceFlagsData
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.SectorFlagState
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.SessionYellowFlagState
import kurou.kodriver.domain.repository.FlagPreferencesRepository
import kurou.kodriver.domain.repository.FlagRepository
import kurou.kodriver.domain.repository.ProximityRepository
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.usecase.ObserveFlagEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveProximityUseCase
import kurou.kodriver.domain.usecase.ObserveRaceFlagsUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuNarratorViewModelTest {

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
        proximityChannel: Channel<ProximityData> = Channel(Channel.UNLIMITED),
        flagChannel: Channel<RaceFlagsData> = Channel(Channel.UNLIMITED),
        ttsEngine: TextToSpeechEngine,
        enabledOverrides: Map<String, Boolean> = emptyMap(),
        flagEnabledOverrides: Map<String, Boolean> = emptyMap(),
    ) = LmuNarratorViewModel(
        observeProximityUseCase = ObserveProximityUseCase(
            FakeChannelProximityRepository(proximityChannel.receiveAsFlow()),
        ),
        observeRaceFlagsUseCase = ObserveRaceFlagsUseCase(
            FakeChannelFlagRepository(flagChannel.receiveAsFlow()),
        ),
        observeSelectedSimulatorUseCase = ObserveSelectedSimulatorUseCase(
            FakeConstantSimulatorRepository("lmu"),
        ),
        observeReadoutEnabledStatesUseCase = ObserveReadoutEnabledStatesUseCase(
            FakeAllEnabledReadoutPreferencesRepository(enabledOverrides),
        ),
        observeFlagEnabledStatesUseCase = ObserveFlagEnabledStatesUseCase(
            FakeFlagPreferencesRepository(flagEnabledOverrides),
        ),
        ttsEngine = ttsEngine,
    )

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
    override fun speak(event: SpeechEvent) { spokenTexts.add(event) }
    override fun stop() = Unit
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
) : ReadoutPreferencesRepository {
    override fun observeReadoutEnabledStates(simulator: String): Flow<Map<String, Boolean>> =
        MutableStateFlow(enabledOverrides)

    override suspend fun saveReadoutEnabledState(simulator: String, label: String, enabled: Boolean) = Unit
    override fun observeReadoutOrder(simulator: String): Flow<List<String>> = emptyFlow()
    override suspend fun saveReadoutOrder(simulator: String, order: List<String>) = Unit
}

private class FakeFlagPreferencesRepository(
    private val enabledOverrides: Map<String, Boolean> = emptyMap(),
) : FlagPreferencesRepository {
    override fun observeFlagEnabledStates(): Flow<Map<String, Boolean>> =
        MutableStateFlow(enabledOverrides)

    override suspend fun saveFlagEnabledState(key: String, enabled: Boolean) = Unit
}
