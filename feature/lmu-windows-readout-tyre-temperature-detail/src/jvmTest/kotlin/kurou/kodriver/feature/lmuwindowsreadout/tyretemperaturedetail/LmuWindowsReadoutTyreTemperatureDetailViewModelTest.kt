package kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRaceFlagsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsTyreTemperatureHighThresholdUseCase
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

private class FakeTextToSpeechEngine(
    private val onSpeak: (SpeechEvent) -> Unit,
) : TextToSpeechEngine {
    override val currentReadoutItemKey: ReadoutItemKey? = null
    override fun speak(event: SpeechEvent, queue: Boolean) = onSpeak(event)
    override fun stop() = Unit
    override fun previewStartSound(type: ReadoutStartSoundType) = Unit
}

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsReadoutTyreTemperatureDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: FakeLmuWindowsTyreTemperaturePreferencesRepository
    private lateinit var flagRepository: FakeLmuWindowsFlagRepository
    private val playedEvents = mutableListOf<SpeechEvent>()
    private lateinit var viewModel: LmuWindowsReadoutTyreTemperatureDetailViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeLmuWindowsTyreTemperaturePreferencesRepository()
        flagRepository = FakeLmuWindowsFlagRepository()
        viewModel = LmuWindowsReadoutTyreTemperatureDetailViewModel(
            observeHighThreshold = ObserveLmuWindowsTyreTemperatureHighThresholdUseCase(repository),
            observeRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(flagRepository),
            saveHighThreshold = SaveLmuWindowsTyreTemperatureHighThresholdUseCase(repository),
            playSpeechEvent = PlaySpeechEventUseCase(FakeTextToSpeechEngine { playedEvents.add(it) }),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `初期状態はリポジトリのデフォルト値を反映したUiStateを返す`() = runTest {
        assertEquals(
            LmuWindowsReadoutTyreTemperatureDetailUiState(highThresholdCelsius = 90),
            viewModel.uiState.first(),
        )
    }

    @Test
    fun `onHighThresholdChangedを呼ぶとuiStateのhighThresholdCelsiusが更新される`() = runTest {
        viewModel.onHighThresholdChanged(100)
        assertEquals(100, viewModel.uiState.first().highThresholdCelsius)
    }

    @Test
    fun `onHighThresholdResetを呼ぶとhighThresholdCelsiusがデフォルト値90に戻る`() = runTest {
        viewModel.onHighThresholdChanged(100)
        viewModel.onHighThresholdReset()
        assertEquals(90, viewModel.uiState.first().highThresholdCelsius)
    }

    @Test
    fun `onPreviewClickedを呼ぶとTyreOverheatイベントが再生される`() {
        viewModel.onPreviewClicked()
        assertEquals(listOf<SpeechEvent>(SpeechEvent.TyreOverheat), playedEvents)
    }

    @Test
    fun `RaceFlagsのgamePhaseが変化するとuiStateのgamePhaseが更新される`() = runTest {
        flagRepository.updateGamePhase(SessionPhase.GREEN_FLAG)
        assertEquals(SessionPhase.GREEN_FLAG, viewModel.uiState.first().gamePhase)
    }
}
