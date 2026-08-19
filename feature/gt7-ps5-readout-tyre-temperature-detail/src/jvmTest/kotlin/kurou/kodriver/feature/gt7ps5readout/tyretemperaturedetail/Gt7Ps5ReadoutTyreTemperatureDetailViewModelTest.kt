@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.gt7ps5readout.tyretemperaturedetail

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
import kurou.kodriver.domain.model.GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.Gt7Ps5TyreTemperaturePreferencesRepository
import kurou.kodriver.domain.usecase.ObserveGt7Ps5TyreTemperatureEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5TyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5TyreTemperatureEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5TyreTemperatureHighThresholdUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class Gt7Ps5ReadoutTyreTemperatureDetailViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    @MockK
    private lateinit var repository: Gt7Ps5TyreTemperaturePreferencesRepository

    @MockK
    private lateinit var ttsEngine: TextToSpeechEngine

    private val highThresholdFlow = MutableStateFlow(GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT)

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
        Gt7Ps5ReadoutTyreTemperatureDetailViewModel(
            observeEnabledStates = ObserveGt7Ps5TyreTemperatureEnabledStatesUseCase(repository),
            observeHighThreshold = ObserveGt7Ps5TyreTemperatureHighThresholdUseCase(repository),
            saveEnabledState = SaveGt7Ps5TyreTemperatureEnabledStateUseCase(repository),
            saveHighThreshold = SaveGt7Ps5TyreTemperatureHighThresholdUseCase(repository),
            playSpeechEvent = PlaySpeechEventUseCase(ttsEngine),
        )

    @Test
    fun `初期状態はデフォルト値のUiStateを返す`() =
        runTest {
            every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
            every { repository.observeHighThresholdCelsius() } returns highThresholdFlow
            val viewModel = createViewModel()

            assertEquals(Gt7Ps5ReadoutTyreTemperatureDetailUiState(), viewModel.uiState.first())
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
                repository.saveEnabledState(ReadoutItemKey.Gt7Ps5.TyreTemperature.OverheatWarning, false)
            } answers {
                enabledStatesFlow.update { it + (ReadoutItemKey.Gt7Ps5.TyreTemperature.OverheatWarning to false) }
            }
            val viewModel = createViewModel()

            viewModel.onOverheatWarningEnabledChanged(false)

            assertEquals(false, viewModel.uiState.first().overheatWarningEnabled)
            verify(exactly = 1) { repository.observeEnabledStates() }
            verify(exactly = 1) { repository.observeHighThresholdCelsius() }
            coVerify(exactly = 1) {
                repository.saveEnabledState(ReadoutItemKey.Gt7Ps5.TyreTemperature.OverheatWarning, false)
            }
            confirmVerified(repository)
        }

    @Test
    fun `onHighThresholdChangedを呼ぶとuiStateのhighThresholdCelsiusが更新される`() =
        runTest {
            every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
            every { repository.observeHighThresholdCelsius() } returns highThresholdFlow
            coEvery { repository.saveHighThresholdCelsius(105) } answers { highThresholdFlow.update { 105 } }
            val viewModel = createViewModel()

            viewModel.onHighThresholdChanged(105)

            assertEquals(105, viewModel.uiState.first().highThresholdCelsius)
            verify(exactly = 1) { repository.observeEnabledStates() }
            verify(exactly = 1) { repository.observeHighThresholdCelsius() }
            coVerify(exactly = 1) { repository.saveHighThresholdCelsius(105) }
            confirmVerified(repository)
        }

    @Test
    fun `onHighThresholdResetを呼ぶとhighThresholdCelsiusがデフォルト値に戻る`() =
        runTest {
            highThresholdFlow.update { 105 }
            every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
            every { repository.observeHighThresholdCelsius() } returns highThresholdFlow
            coEvery {
                repository.saveHighThresholdCelsius(GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT)
            } answers {
                highThresholdFlow.update { GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT }
            }
            val viewModel = createViewModel()

            viewModel.onHighThresholdReset()

            assertEquals(
                GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT,
                viewModel.uiState.first().highThresholdCelsius,
            )
            verify(exactly = 1) { repository.observeEnabledStates() }
            verify(exactly = 1) { repository.observeHighThresholdCelsius() }
            coVerify(exactly = 1) {
                repository.saveHighThresholdCelsius(GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT)
            }
            confirmVerified(repository)
        }

    @Test
    fun `onPreviewClickedを呼ぶとタイヤ過熱警告を読み上げる`() =
        runTest {
            every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
            every { repository.observeHighThresholdCelsius() } returns highThresholdFlow
            every { ttsEngine.speak(SpeechEvent.Gt7Ps5TyreOverheat, false) } returns Unit
            val viewModel = createViewModel()

            viewModel.onPreviewClicked()

            verify(exactly = 1) { repository.observeEnabledStates() }
            verify(exactly = 1) { repository.observeHighThresholdCelsius() }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.Gt7Ps5TyreOverheat, false) }
            confirmVerified(repository, ttsEngine)
        }
}
