@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.gt7ps5readout.remainingfueldetail

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
import kurou.kodriver.domain.model.GT7_PS5_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT
import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelPreferencesRepository
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5RemainingFuelThresholdPercentageUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class Gt7Ps5ReadoutRemainingFuelDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @MockK
    private lateinit var repository: Gt7Ps5RemainingFuelPreferencesRepository

    @MockK
    private lateinit var ttsEngine: TextToSpeechEngine

    private val thresholdFlow = MutableStateFlow(GT7_PS5_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT)

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
        Gt7Ps5ReadoutRemainingFuelDetailViewModel(
        observeThresholdPercentage = ObserveGt7Ps5RemainingFuelThresholdPercentageUseCase(repository),
        saveThresholdPercentage = SaveGt7Ps5RemainingFuelThresholdPercentageUseCase(repository),
        playSpeechEvent = PlaySpeechEventUseCase(ttsEngine),
    )

    @Test
    fun `初期状態は燃料残量閾値30パーセントのUiStateを返す`() =
        runTest {
        every { repository.observeThresholdPercentage() } returns thresholdFlow
        val viewModel = createViewModel()

        assertEquals(GT7_PS5_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT, viewModel.uiState.first().thresholdPercentage)
        verify(exactly = 1) { repository.observeThresholdPercentage() }
        confirmVerified(repository)
    }

    @Test
    fun `onThresholdChangedに45を渡すと燃料残量閾値が45パーセントになる`() =
        runTest {
        every { repository.observeThresholdPercentage() } returns thresholdFlow
        coEvery { repository.saveThresholdPercentage(45) } answers { thresholdFlow.update { 45 } }
        val viewModel = createViewModel()

        viewModel.onThresholdChanged(45)

        assertEquals(45, viewModel.uiState.first().thresholdPercentage)
        verify(exactly = 1) { repository.observeThresholdPercentage() }
        coVerify(exactly = 1) { repository.saveThresholdPercentage(45) }
        confirmVerified(repository)
    }

    @Test
    fun `onThresholdResetを呼ぶと燃料残量閾値が30パーセントになる`() =
        runTest {
        thresholdFlow.update { 60 }
        every { repository.observeThresholdPercentage() } returns thresholdFlow
        coEvery {
            repository.saveThresholdPercentage(GT7_PS5_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT)
        } answers {
            thresholdFlow.update { GT7_PS5_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT }
        }
        val viewModel = createViewModel()

        viewModel.onThresholdReset()

        assertEquals(GT7_PS5_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT, viewModel.uiState.first().thresholdPercentage)
        verify(exactly = 1) { repository.observeThresholdPercentage() }
        coVerify(exactly = 1) {
            repository.saveThresholdPercentage(GT7_PS5_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT)
        }
        confirmVerified(repository)
    }

    @Test
    fun `onPreviewClickedを呼ぶと燃料残量警告を読み上げる`() =
        runTest {
        every { repository.observeThresholdPercentage() } returns thresholdFlow
        every { ttsEngine.speak(SpeechEvent.Gt7Ps5RemainingFuelWarning, false) } returns Unit
        val viewModel = createViewModel()

        viewModel.onPreviewClicked()

        verify(exactly = 1) { repository.observeThresholdPercentage() }
        verify(exactly = 1) { ttsEngine.speak(SpeechEvent.Gt7Ps5RemainingFuelWarning, false) }
        confirmVerified(repository, ttsEngine)
    }
}
