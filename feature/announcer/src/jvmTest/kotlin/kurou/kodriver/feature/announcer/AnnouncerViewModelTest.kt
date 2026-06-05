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
        channel.send(noProximity().copy(isSideBySideLeft = true))

        assertEquals(listOf("CarLeft"), tts.spokenTexts)
    }

    @Test
    fun `右から接近するとCarRightを読み上げる`() = runTest(testDispatcher) {
        val channel = Channel<ProximityData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(channel, tts)

        channel.send(noProximity())
        channel.send(noProximity().copy(isSideBySideRight = true))

        assertEquals(listOf("CarRight"), tts.spokenTexts)
    }

    @Test
    fun `両側から同時に接近するとCarLeftとCarRightを順番に読み上げる`() = runTest(testDispatcher) {
        val channel = Channel<ProximityData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(channel, tts)

        channel.send(noProximity())
        channel.send(noProximity().copy(isSideBySideLeft = true, isSideBySideRight = true))

        assertEquals(listOf("CarLeft", "CarRight"), tts.spokenTexts)
    }

    @Test
    fun `既に並走中の車が継続して並走してもアナウンスしない`() = runTest(testDispatcher) {
        val channel = Channel<ProximityData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(channel, tts)

        // 接近開始 → CarLeft が発話される
        channel.send(noProximity())
        channel.send(noProximity().copy(isSideBySideLeft = true))
        val spokenAfterFirstApproach = tts.spokenTexts.toList()

        // 継続して並走 → 追加発話なし
        channel.send(noProximity().copy(isSideBySideLeft = true))

        assertEquals(spokenAfterFirstApproach, tts.spokenTexts)
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
        channel.send(noProximity().copy(isSideBySideLeft = true))

        assertEquals(emptyList<String>(), tts.spokenTexts)
    }
}

private fun noProximity() = ProximityData(
    isSideBySideLeft = false,
    isSideBySideRight = false,
    lateralDistanceLeftMeters = Double.MAX_VALUE,
    lateralDistanceRightMeters = Double.MAX_VALUE,
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
