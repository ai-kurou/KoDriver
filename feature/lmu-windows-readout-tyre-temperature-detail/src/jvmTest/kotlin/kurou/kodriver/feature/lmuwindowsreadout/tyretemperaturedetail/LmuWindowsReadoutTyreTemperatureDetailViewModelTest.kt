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
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureLowWarningPhasesUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsTyreTemperatureEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsTyreTemperatureLowWarningPhasesUseCase
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
    private val playedEvents = mutableListOf<SpeechEvent>()
    private lateinit var viewModel: LmuWindowsReadoutTyreTemperatureDetailViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeLmuWindowsTyreTemperaturePreferencesRepository()
        viewModel = LmuWindowsReadoutTyreTemperatureDetailViewModel(
            observeHighThreshold = ObserveLmuWindowsTyreTemperatureHighThresholdUseCase(repository),
            observeEnabledStates = ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase(repository),
            observeLowWarningPhases = ObserveLmuWindowsTyreTemperatureLowWarningPhasesUseCase(repository),
            saveHighThreshold = SaveLmuWindowsTyreTemperatureHighThresholdUseCase(repository),
            saveEnabledState = SaveLmuWindowsTyreTemperatureEnabledStateUseCase(repository),
            saveLowWarningPhases = SaveLmuWindowsTyreTemperatureLowWarningPhasesUseCase(repository),
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
            LmuWindowsReadoutTyreTemperatureDetailUiState(highThresholdCelsius = 90, overheatWarningEnabled = true),
            viewModel.uiState.first(),
        )
    }

    @Test
    fun `onOverheatWarningEnabledChangedを呼ぶとuiStateのoverheatWarningEnabledが更新される`() = runTest {
        viewModel.onOverheatWarningEnabledChanged(false)
        assertEquals(false, viewModel.uiState.first().overheatWarningEnabled)
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
    fun `onLowWarningEnabledChangedを呼ぶとuiStateのlowWarningEnabledが更新される`() = runTest {
        viewModel.onLowWarningEnabledChanged(false)
        assertEquals(false, viewModel.uiState.first().lowWarningEnabled)
    }

    @Test
    fun `onLowWarningPhaseToggledで未選択のフェーズを渡すと選択に追加される`() = runTest {
        repository = FakeLmuWindowsTyreTemperaturePreferencesRepository(
            initialLowWarningPhases = mapOf(
                SessionPhase.GARAGE to false,
                SessionPhase.WARM_UP to false,
                SessionPhase.GRID_WALK to false,
                SessionPhase.FORMATION to false,
            ),
        )
        viewModel = LmuWindowsReadoutTyreTemperatureDetailViewModel(
            observeHighThreshold = ObserveLmuWindowsTyreTemperatureHighThresholdUseCase(repository),
            observeEnabledStates = ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase(repository),
            observeLowWarningPhases = ObserveLmuWindowsTyreTemperatureLowWarningPhasesUseCase(repository),
            saveHighThreshold = SaveLmuWindowsTyreTemperatureHighThresholdUseCase(repository),
            saveEnabledState = SaveLmuWindowsTyreTemperatureEnabledStateUseCase(repository),
            saveLowWarningPhases = SaveLmuWindowsTyreTemperatureLowWarningPhasesUseCase(repository),
            playSpeechEvent = PlaySpeechEventUseCase(FakeTextToSpeechEngine { playedEvents.add(it) }),
        )
        viewModel.uiState.first()
        viewModel.onLowWarningPhaseToggled(SessionPhase.GARAGE)
        assertEquals(setOf(SessionPhase.GARAGE), viewModel.uiState.first().lowWarningPhases)
    }

    @Test
    fun `onLowWarningPhaseToggledで選択済みのフェーズを渡すと選択から除外される`() = runTest {
        viewModel.uiState.first()
        viewModel.onLowWarningPhaseToggled(SessionPhase.GARAGE)
        assertEquals(
            setOf(SessionPhase.WARM_UP, SessionPhase.GRID_WALK, SessionPhase.FORMATION),
            viewModel.uiState.first().lowWarningPhases,
        )
    }
}
