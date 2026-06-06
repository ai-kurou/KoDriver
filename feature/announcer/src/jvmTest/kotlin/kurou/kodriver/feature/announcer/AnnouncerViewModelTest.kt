package kurou.kodriver.feature.announcer

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
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.ProximityData
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.ProximityRepository
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.usecase.ObserveProximityUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class AnnouncerViewModelTest {

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
        proximityChannel: Channel<ProximityData>,
        ttsEngine: TextToSpeechEngine,
        enabledOverrides: Map<String, Boolean> = emptyMap(),
    ) = AnnouncerViewModel(
        observeProximityUseCase = ObserveProximityUseCase(
            FakeChannelProximityRepository(proximityChannel.receiveAsFlow()),
        ),
        observeSelectedSimulatorUseCase = ObserveSelectedSimulatorUseCase(
            FakeConstantSimulatorRepository("lmu"),
        ),
        observeReadoutEnabledStatesUseCase = ObserveReadoutEnabledStatesUseCase(
            FakeAllEnabledReadoutPreferencesRepository(enabledOverrides),
        ),
        ttsEngine = ttsEngine,
    )

    @Test
    fun `左から接近するとCarLeftを読み上げる`() = runTest(testDispatcher) {
        val channel = Channel<ProximityData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(channel, tts)

        channel.send(noProximity())
        channel.send(leftProximity(vehicleId = 1))

        assertEquals(listOf("カーレフト"), tts.spokenTexts)
    }

    @Test
    fun `右から接近するとCarRightを読み上げる`() = runTest(testDispatcher) {
        val channel = Channel<ProximityData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(channel, tts)

        channel.send(noProximity())
        channel.send(rightProximity(vehicleId = 1))

        assertEquals(listOf("カーライト"), tts.spokenTexts)
    }

    @Test
    fun `両側から同時に接近するとCarLeftとCarRightを順番に読み上げる`() = runTest(testDispatcher) {
        val channel = Channel<ProximityData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(channel, tts)

        channel.send(noProximity())
        channel.send(bothProximity(leftId = 1, rightId = 2))

        assertEquals(listOf("カーレフト", "カーライト"), tts.spokenTexts)
    }

    @Test
    fun `既に並走中の車が継続して並走してもアナウンスしない`() = runTest(testDispatcher) {
        val channel = Channel<ProximityData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(channel, tts)

        // 接近開始 → CarLeft が発話される
        channel.send(noProximity())
        channel.send(leftProximity(vehicleId = 1))
        val spokenAfterFirstApproach = tts.spokenTexts.toList()

        // 同一車両が継続して並走 → 追加発話なし
        channel.send(leftProximity(vehicleId = 1))

        assertEquals(spokenAfterFirstApproach, tts.spokenTexts)
    }

    @Test
    fun `並走から離脱後に同じ車が再度並走するとアナウンスする`() = runTest(testDispatcher) {
        val channel = Channel<ProximityData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(channel, tts)

        channel.send(noProximity())
        channel.send(leftProximity(vehicleId = 1)) // 1回目
        channel.send(noProximity()) // 離脱
        channel.send(leftProximity(vehicleId = 1)) // 再接近

        assertEquals(listOf("カーレフト", "カーレフト"), tts.spokenTexts)
    }

    @Test
    fun `別の車両が新たに並走ゾーンに入るとアナウンスする`() = runTest(testDispatcher) {
        val channel = Channel<ProximityData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(channel, tts)

        channel.send(noProximity())
        channel.send(leftProximity(vehicleId = 1)) // 車両1が入る
        channel.send(leftProximity(vehicleId = 1, extraLeftId = 2)) // 車両2も入る

        assertEquals(listOf("カーレフト", "カーレフト"), tts.spokenTexts)
    }

    @Test
    fun `VEHICLE_APPROACHが無効のときはアナウンスしない`() = runTest(testDispatcher) {
        val channel = Channel<ProximityData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
            channel,
            tts,
            enabledOverrides = mapOf(ReadoutItemKey.VEHICLE_APPROACH to false),
        )

        channel.send(noProximity())
        channel.send(leftProximity(vehicleId = 1))

        assertEquals(emptyList<String>(), tts.spokenTexts)
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

private fun bothProximity(leftId: Int, rightId: Int) = ProximityData(
    sideBySideLeftVehicleIds = setOf(leftId),
    sideBySideRightVehicleIds = setOf(rightId),
    lateralDistanceLeftMeters = 3.0,
    lateralDistanceRightMeters = 3.0,
)

private class RecordingTextToSpeechEngine : TextToSpeechEngine {
    val spokenTexts = mutableListOf<String>()
    override fun speak(text: String) { spokenTexts.add(text) }
    override fun stop() = Unit
}

private class FakeChannelProximityRepository(
    private val stream: Flow<ProximityData>,
) : ProximityRepository {
    override fun proximityStream(): Flow<ProximityData> = stream
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
