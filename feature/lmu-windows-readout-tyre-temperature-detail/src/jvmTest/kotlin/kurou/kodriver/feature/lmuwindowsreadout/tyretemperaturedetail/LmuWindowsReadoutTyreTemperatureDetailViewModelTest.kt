@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperaturePreferencesRepository
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

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsReadoutTyreTemperatureDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @MockK
    private lateinit var repository: LmuWindowsTyreTemperaturePreferencesRepository

    @MockK
    private lateinit var ttsEngine: TextToSpeechEngine

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = LmuWindowsReadoutTyreTemperatureDetailViewModel(
        observeHighThreshold = ObserveLmuWindowsTyreTemperatureHighThresholdUseCase(repository),
        observeEnabledStates = ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase(repository),
        observeLowWarningPhases = ObserveLmuWindowsTyreTemperatureLowWarningPhasesUseCase(repository),
        saveHighThreshold = SaveLmuWindowsTyreTemperatureHighThresholdUseCase(repository),
        saveEnabledState = SaveLmuWindowsTyreTemperatureEnabledStateUseCase(repository),
        saveLowWarningPhases = SaveLmuWindowsTyreTemperatureLowWarningPhasesUseCase(repository),
        playSpeechEvent = PlaySpeechEventUseCase(ttsEngine),
    )

    @Test
    fun `初期状態はリポジトリのデフォルト値を反映したUiStateを返す`() = runTest {
        every { repository.observeHighThresholdCelsius() } returns MutableStateFlow(90)
        every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
        every { repository.observeLowWarningPhases() } returns MutableStateFlow(emptyMap())
        val viewModel = createViewModel()

        assertEquals(
            LmuWindowsReadoutTyreTemperatureDetailUiState(highThresholdCelsius = 90, overheatWarningEnabled = true),
            viewModel.uiState.first(),
        )
        verify(exactly = 1) { repository.observeHighThresholdCelsius() }
        verify(exactly = 1) { repository.observeEnabledStates() }
        verify(exactly = 1) { repository.observeLowWarningPhases() }
        confirmVerified(repository)
    }

    @Test
    fun `onOverheatWarningEnabledChangedを呼ぶとuiStateのoverheatWarningEnabledが更新される`() = runTest {
        every { repository.observeHighThresholdCelsius() } returns MutableStateFlow(90)
        val enabledStatesFlow = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
        every { repository.observeEnabledStates() } returns enabledStatesFlow
        every { repository.observeLowWarningPhases() } returns MutableStateFlow(emptyMap())
        coEvery {
            repository.saveEnabledState(ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning, false)
        } answers {
            enabledStatesFlow.update { it + (ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning to false) }
        }
        val viewModel = createViewModel()

        viewModel.onOverheatWarningEnabledChanged(false)

        assertEquals(false, viewModel.uiState.first().overheatWarningEnabled)
        verify(exactly = 1) { repository.observeHighThresholdCelsius() }
        verify(exactly = 1) { repository.observeEnabledStates() }
        verify(exactly = 1) { repository.observeLowWarningPhases() }
        coVerify(exactly = 1) {
            repository.saveEnabledState(ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning, false)
        }
        confirmVerified(repository)
    }

    @Test
    fun `onHighThresholdChangedを呼ぶとuiStateのhighThresholdCelsiusが更新される`() = runTest {
        val highThresholdFlow = MutableStateFlow(90)
        every { repository.observeHighThresholdCelsius() } returns highThresholdFlow
        every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
        every { repository.observeLowWarningPhases() } returns MutableStateFlow(emptyMap())
        coEvery { repository.saveHighThresholdCelsius(100) } answers { highThresholdFlow.update { 100 } }
        val viewModel = createViewModel()

        viewModel.onHighThresholdChanged(100)

        assertEquals(100, viewModel.uiState.first().highThresholdCelsius)
        verify(exactly = 1) { repository.observeHighThresholdCelsius() }
        verify(exactly = 1) { repository.observeEnabledStates() }
        verify(exactly = 1) { repository.observeLowWarningPhases() }
        coVerify(exactly = 1) { repository.saveHighThresholdCelsius(100) }
        confirmVerified(repository)
    }

    @Test
    fun `onHighThresholdResetを呼ぶとhighThresholdCelsiusがデフォルト値90に戻る`() = runTest {
        val highThresholdFlow = MutableStateFlow(90)
        every { repository.observeHighThresholdCelsius() } returns highThresholdFlow
        every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
        every { repository.observeLowWarningPhases() } returns MutableStateFlow(emptyMap())
        coEvery { repository.saveHighThresholdCelsius(100) } answers { highThresholdFlow.update { 100 } }
        coEvery { repository.saveHighThresholdCelsius(90) } answers { highThresholdFlow.update { 90 } }
        val viewModel = createViewModel()

        viewModel.onHighThresholdChanged(100)
        viewModel.onHighThresholdReset()

        assertEquals(90, viewModel.uiState.first().highThresholdCelsius)
        verify(exactly = 1) { repository.observeHighThresholdCelsius() }
        verify(exactly = 1) { repository.observeEnabledStates() }
        verify(exactly = 1) { repository.observeLowWarningPhases() }
        coVerify(exactly = 1) { repository.saveHighThresholdCelsius(100) }
        coVerify(exactly = 1) { repository.saveHighThresholdCelsius(90) }
        confirmVerified(repository)
    }

    @Test
    fun `onPreviewClickedを呼ぶとTyreOverheatイベントが再生される`() {
        every { repository.observeHighThresholdCelsius() } returns MutableStateFlow(90)
        every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
        every { repository.observeLowWarningPhases() } returns MutableStateFlow(emptyMap())
        every { ttsEngine.speak(SpeechEvent.TyreOverheat, false) } returns Unit
        val viewModel = createViewModel()

        viewModel.onPreviewClicked()

        verify(exactly = 1) { repository.observeHighThresholdCelsius() }
        verify(exactly = 1) { repository.observeEnabledStates() }
        verify(exactly = 1) { repository.observeLowWarningPhases() }
        verify(exactly = 1) { ttsEngine.speak(SpeechEvent.TyreOverheat, false) }
        confirmVerified(repository, ttsEngine)
    }

    @Test
    fun `onLowWarningPreviewClickedを呼ぶとTyreColdイベントが再生される`() {
        every { repository.observeHighThresholdCelsius() } returns MutableStateFlow(90)
        every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
        every { repository.observeLowWarningPhases() } returns MutableStateFlow(emptyMap())
        every { ttsEngine.speak(SpeechEvent.TyreCold, false) } returns Unit
        val viewModel = createViewModel()

        viewModel.onLowWarningPreviewClicked()

        verify(exactly = 1) { repository.observeHighThresholdCelsius() }
        verify(exactly = 1) { repository.observeEnabledStates() }
        verify(exactly = 1) { repository.observeLowWarningPhases() }
        verify(exactly = 1) { ttsEngine.speak(SpeechEvent.TyreCold, false) }
        confirmVerified(repository, ttsEngine)
    }

    @Test
    fun `onLowWarningEnabledChangedを呼ぶとuiStateのlowWarningEnabledが更新される`() = runTest {
        every { repository.observeHighThresholdCelsius() } returns MutableStateFlow(90)
        val enabledStatesFlow = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
        every { repository.observeEnabledStates() } returns enabledStatesFlow
        every { repository.observeLowWarningPhases() } returns MutableStateFlow(emptyMap())
        coEvery {
            repository.saveEnabledState(ReadoutItemKey.LmuWindows.TyreTemperature.LowWarning, false)
        } answers {
            enabledStatesFlow.update { it + (ReadoutItemKey.LmuWindows.TyreTemperature.LowWarning to false) }
        }
        val viewModel = createViewModel()

        viewModel.onLowWarningEnabledChanged(false)

        assertEquals(false, viewModel.uiState.first().lowWarningEnabled)
        verify(exactly = 1) { repository.observeHighThresholdCelsius() }
        verify(exactly = 1) { repository.observeEnabledStates() }
        verify(exactly = 1) { repository.observeLowWarningPhases() }
        coVerify(exactly = 1) {
            repository.saveEnabledState(ReadoutItemKey.LmuWindows.TyreTemperature.LowWarning, false)
        }
        confirmVerified(repository)
    }

    @Test
    fun `onLowWarningPhaseToggledで未選択のフェーズを渡すと選択に追加される`() = runTest {
        every { repository.observeHighThresholdCelsius() } returns MutableStateFlow(90)
        every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
        val lowWarningPhasesFlow = MutableStateFlow(
            mapOf(
                SessionPhase.GARAGE to false,
                SessionPhase.WARM_UP to false,
                SessionPhase.GRID_WALK to false,
                SessionPhase.FORMATION to false,
            ),
        )
        every { repository.observeLowWarningPhases() } returns lowWarningPhasesFlow
        coEvery { repository.saveLowWarningPhases(setOf(SessionPhase.GARAGE)) } answers {
            lowWarningPhasesFlow.update {
                mapOf(
                    SessionPhase.GARAGE to true,
                    SessionPhase.WARM_UP to false,
                    SessionPhase.GRID_WALK to false,
                    SessionPhase.FORMATION to false,
                )
            }
        }
        val viewModel = createViewModel()
        viewModel.uiState.first()

        viewModel.onLowWarningPhaseToggled(SessionPhase.GARAGE)

        assertEquals(setOf(SessionPhase.GARAGE), viewModel.uiState.first().lowWarningPhases)
        verify(exactly = 1) { repository.observeHighThresholdCelsius() }
        verify(exactly = 1) { repository.observeEnabledStates() }
        verify(exactly = 1) { repository.observeLowWarningPhases() }
        coVerify(exactly = 1) { repository.saveLowWarningPhases(setOf(SessionPhase.GARAGE)) }
        confirmVerified(repository)
    }

    @Test
    fun `onLowWarningPhaseToggledで選択済みのフェーズを渡すと選択から除外される`() = runTest {
        every { repository.observeHighThresholdCelsius() } returns MutableStateFlow(90)
        every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
        val defaultPhases = mapOf(
            SessionPhase.GARAGE to false,
            SessionPhase.WARM_UP to true,
            SessionPhase.GRID_WALK to true,
            SessionPhase.FORMATION to true,
        )
        val lowWarningPhasesFlow = MutableStateFlow(defaultPhases)
        every { repository.observeLowWarningPhases() } returns lowWarningPhasesFlow
        coEvery {
            repository.saveLowWarningPhases(setOf(SessionPhase.WARM_UP, SessionPhase.GRID_WALK))
        } answers {
            lowWarningPhasesFlow.update {
                mapOf(
                    SessionPhase.GARAGE to false,
                    SessionPhase.WARM_UP to true,
                    SessionPhase.GRID_WALK to true,
                    SessionPhase.FORMATION to false,
                )
            }
        }
        val viewModel = createViewModel()
        viewModel.uiState.first()

        viewModel.onLowWarningPhaseToggled(SessionPhase.FORMATION)

        assertEquals(
            setOf(SessionPhase.WARM_UP, SessionPhase.GRID_WALK),
            viewModel.uiState.first().lowWarningPhases,
        )
        verify(exactly = 1) { repository.observeHighThresholdCelsius() }
        verify(exactly = 1) { repository.observeEnabledStates() }
        verify(exactly = 1) { repository.observeLowWarningPhases() }
        coVerify(exactly = 1) {
            repository.saveLowWarningPhases(setOf(SessionPhase.WARM_UP, SessionPhase.GRID_WALK))
        }
        confirmVerified(repository)
    }
}
