@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.acewindowsreadout.tyretemperaturedetail

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
import kurou.kodriver.domain.model.ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT
import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.AceWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.usecase.ObserveAceWindowsTyreTemperatureEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsTyreTemperatureEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsTyreTemperatureHighThresholdUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class AceWindowsReadoutTyreTemperatureDetailViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    @MockK
    private lateinit var repository: AceWindowsTyreTemperaturePreferencesRepository

    @MockK
    private lateinit var ttsEngine: TextToSpeechEngine

    private val highThresholdFlow = MutableStateFlow(ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT)

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        AceWindowsReadoutTyreTemperatureDetailViewModel(
            observeEnabledStates = ObserveAceWindowsTyreTemperatureEnabledStatesUseCase(repository),
            observeHighThreshold = ObserveAceWindowsTyreTemperatureHighThresholdUseCase(repository),
            saveEnabledState = SaveAceWindowsTyreTemperatureEnabledStateUseCase(repository),
            saveHighThreshold = SaveAceWindowsTyreTemperatureHighThresholdUseCase(repository),
            playSpeechEvent = PlaySpeechEventUseCase(ttsEngine),
        )

    @Test
    fun `初期状態はデフォルト値のUiStateを返す`() =
        runTest {
            every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
            every { repository.observeHighThresholdCelsius() } returns highThresholdFlow
            val viewModel = createViewModel()

            assertEquals(AceWindowsReadoutTyreTemperatureDetailUiState(), viewModel.uiState.first())
            verify(exactly = 1) { repository.observeEnabledStates() }
            verify(exactly = 1) { repository.observeHighThresholdCelsius() }
            confirmVerified(repository)
        }

    @Test
    fun `onOverheatWarningEnabledChangedを呼ぶとuiStateのoverheatWarningEnabledが更新される`() =
        runTest {
            val enabledStatesFlow = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
            every { repository.observeEnabledStates() } returns enabledStatesFlow
            every { repository.observeHighThresholdCelsius() } returns highThresholdFlow
            coEvery {
                repository.saveEnabledState(ReadoutItemKey.AceWindows.TyreTemperature.OverheatWarning, false)
            } answers {
                enabledStatesFlow.update { it + (ReadoutItemKey.AceWindows.TyreTemperature.OverheatWarning to false) }
            }
            val viewModel = createViewModel()

            viewModel.onOverheatWarningEnabledChanged(false)

            assertEquals(false, viewModel.uiState.first().overheatWarningEnabled)
            verify(exactly = 1) { repository.observeEnabledStates() }
            verify(exactly = 1) { repository.observeHighThresholdCelsius() }
            coVerify(exactly = 1) {
                repository.saveEnabledState(ReadoutItemKey.AceWindows.TyreTemperature.OverheatWarning, false)
            }
            confirmVerified(repository)
        }

    @Test
    fun `onHighThresholdChangedを呼ぶとuiStateのhighThresholdCelsiusが更新される`() =
        runTest {
            every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
            every { repository.observeHighThresholdCelsius() } returns highThresholdFlow
            coEvery {
                repository.saveHighThresholdCelsius(Celsius(105))
            } answers { highThresholdFlow.update { Celsius(105) } }
            val viewModel = createViewModel()

            viewModel.onHighThresholdChanged(105)

            assertEquals(105, viewModel.uiState.first().highThresholdCelsius)
            verify(exactly = 1) { repository.observeEnabledStates() }
            verify(exactly = 1) { repository.observeHighThresholdCelsius() }
            coVerify(exactly = 1) { repository.saveHighThresholdCelsius(Celsius(105)) }
            confirmVerified(repository)
        }

    @Test
    fun `onHighThresholdResetを呼ぶとhighThresholdCelsiusがデフォルト値に戻る`() =
        runTest {
            highThresholdFlow.update { Celsius(105) }
            every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
            every { repository.observeHighThresholdCelsius() } returns highThresholdFlow
            coEvery {
                repository.saveHighThresholdCelsius(ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT)
            } answers {
                highThresholdFlow.update { ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT }
            }
            val viewModel = createViewModel()

            viewModel.onHighThresholdReset()

            assertEquals(
                ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT.value,
                viewModel.uiState.first().highThresholdCelsius,
            )
            verify(exactly = 1) { repository.observeEnabledStates() }
            verify(exactly = 1) { repository.observeHighThresholdCelsius() }
            coVerify(exactly = 1) {
                repository.saveHighThresholdCelsius(ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT)
            }
            confirmVerified(repository)
        }

    @Test
    fun `onPreviewClickedを呼ぶとAceWindowsTyreOverheatイベントが再生される`() {
        every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
        every { repository.observeHighThresholdCelsius() } returns highThresholdFlow
        every { ttsEngine.speak(SpeechEvent.AceWindowsTyreOverheat, false) } returns Unit
        val viewModel = createViewModel()

        viewModel.onPreviewClicked()

        verify(exactly = 1) { repository.observeEnabledStates() }
        verify(exactly = 1) { repository.observeHighThresholdCelsius() }
        verify(exactly = 1) { ttsEngine.speak(SpeechEvent.AceWindowsTyreOverheat, false) }
        confirmVerified(repository, ttsEngine)
    }
}
