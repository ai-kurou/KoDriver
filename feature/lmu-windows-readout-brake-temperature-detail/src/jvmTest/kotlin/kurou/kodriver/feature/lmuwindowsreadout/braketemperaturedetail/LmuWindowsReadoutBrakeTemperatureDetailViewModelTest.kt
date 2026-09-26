@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.lmuwindowsreadout.braketemperaturedetail

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
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
import kurou.kodriver.domain.repository.LmuWindowsBrakeTemperaturePreferencesRepository
import kurou.kodriver.domain.usecase.ObserveLmuWindowsBrakeTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsBrakeTemperatureHighThresholdUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsReadoutBrakeTemperatureDetailViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val repository: LmuWindowsBrakeTemperaturePreferencesRepository = mockk()

    private val ttsEngine: TextToSpeechEngine = mockk()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        LmuWindowsReadoutBrakeTemperatureDetailViewModel(
            observeHighThresholdCelsius = ObserveLmuWindowsBrakeTemperatureHighThresholdUseCase(repository),
            saveHighThresholdCelsius = SaveLmuWindowsBrakeTemperatureHighThresholdUseCase(repository),
            playSpeechEvent = PlaySpeechEventUseCase(ttsEngine),
        )

    @Test
    fun `初期状態はリポジトリのデフォルト値を反映したUiStateを返す`() =
        runTest {
            every { repository.observeHighThresholdCelsius() } returns MutableStateFlow(700)
            val viewModel = createViewModel()

            assertEquals(
                LmuWindowsReadoutBrakeTemperatureDetailUiState(highThresholdCelsius = 700),
                viewModel.uiState.first(),
            )
            verify(exactly = 1) { repository.observeHighThresholdCelsius() }
            confirmVerified(repository)
        }

    @Test
    fun `onThresholdChangedを呼ぶとuiStateのhighThresholdCelsiusが更新される`() =
        runTest {
            val thresholdFlow = MutableStateFlow(700)
            every { repository.observeHighThresholdCelsius() } returns thresholdFlow
            coEvery { repository.saveHighThresholdCelsius(600) } answers { thresholdFlow.update { 600 } }
            val viewModel = createViewModel()

            viewModel.onThresholdChanged(600)

            assertEquals(600, viewModel.uiState.first().highThresholdCelsius)
            verify(exactly = 1) { repository.observeHighThresholdCelsius() }
            coVerify(exactly = 1) { repository.saveHighThresholdCelsius(600) }
            confirmVerified(repository)
        }

    @Test
    fun `onThresholdResetを呼ぶとhighThresholdCelsiusがデフォルト値700に戻る`() =
        runTest {
            val thresholdFlow = MutableStateFlow(700)
            every { repository.observeHighThresholdCelsius() } returns thresholdFlow
            coEvery { repository.saveHighThresholdCelsius(600) } answers { thresholdFlow.update { 600 } }
            coEvery { repository.saveHighThresholdCelsius(700) } answers { thresholdFlow.update { 700 } }
            val viewModel = createViewModel()

            viewModel.onThresholdChanged(600)
            viewModel.onThresholdReset()

            assertEquals(700, viewModel.uiState.first().highThresholdCelsius)
            verify(exactly = 1) { repository.observeHighThresholdCelsius() }
            coVerify(exactly = 1) { repository.saveHighThresholdCelsius(600) }
            coVerify(exactly = 1) { repository.saveHighThresholdCelsius(700) }
            confirmVerified(repository)
        }

    @Test
    fun `onWarningChipClickedを呼ぶとBrakeOverheatイベントが再生される`() {
        every { repository.observeHighThresholdCelsius() } returns MutableStateFlow(700)
        every { ttsEngine.speak(SpeechEvent.BrakeOverheat, false) } returns Unit
        val viewModel = createViewModel()

        viewModel.onWarningChipClicked()

        verify(exactly = 1) { repository.observeHighThresholdCelsius() }
        verify(exactly = 1) { ttsEngine.speak(SpeechEvent.BrakeOverheat, false) }
        confirmVerified(repository, ttsEngine)
    }
}
