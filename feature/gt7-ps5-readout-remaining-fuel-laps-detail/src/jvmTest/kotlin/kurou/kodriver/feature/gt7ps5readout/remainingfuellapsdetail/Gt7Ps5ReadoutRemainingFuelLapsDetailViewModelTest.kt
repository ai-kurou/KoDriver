@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.gt7ps5readout.remainingfuellapsdetail

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
import kurou.kodriver.domain.model.GT7_PS5_REMAINING_FUEL_LAPS_DEFAULT
import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelLapsPreferencesRepository
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelLapsUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5RemainingFuelLapsUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class Gt7Ps5ReadoutRemainingFuelLapsDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @MockK
    private lateinit var repository: Gt7Ps5RemainingFuelLapsPreferencesRepository

    @MockK(relaxUnitFun = true)
    private lateinit var ttsEngine: TextToSpeechEngine

    private val remainingFuelLapsFlow = MutableStateFlow(GT7_PS5_REMAINING_FUEL_LAPS_DEFAULT)

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = Gt7Ps5ReadoutRemainingFuelLapsDetailViewModel(
        observeGt7Ps5RemainingFuelLaps = ObserveGt7Ps5RemainingFuelLapsUseCase(repository),
        saveGt7Ps5RemainingFuelLaps = SaveGt7Ps5RemainingFuelLapsUseCase(repository),
        playSpeechEvent = PlaySpeechEventUseCase(ttsEngine),
    )

    @Test
    fun `初期状態は燃料残り周回数3のUiStateを返す`() = runTest {
        every { repository.observeRemainingFuelLaps() } returns remainingFuelLapsFlow
        val viewModel = createViewModel()

        assertEquals(GT7_PS5_REMAINING_FUEL_LAPS_DEFAULT, viewModel.uiState.first().remainingFuelLaps)
        verify(exactly = 1) { repository.observeRemainingFuelLaps() }
        confirmVerified(repository)
    }

    @Test
    fun `onRemainingFuelLapsChangedに1を渡すと燃料残り周回数が1になる`() = runTest {
        every { repository.observeRemainingFuelLaps() } returns remainingFuelLapsFlow
        coEvery { repository.saveRemainingFuelLaps(1) } answers { remainingFuelLapsFlow.update { 1 } }
        val viewModel = createViewModel()

        viewModel.onRemainingFuelLapsChanged(1)

        assertEquals(1, viewModel.uiState.first().remainingFuelLaps)
        verify(exactly = 1) { repository.observeRemainingFuelLaps() }
        coVerify(exactly = 1) { repository.saveRemainingFuelLaps(1) }
        confirmVerified(repository)
    }

    @Test
    fun `onResetRemainingFuelLapsを呼ぶと燃料残り周回数が3になる`() = runTest {
        remainingFuelLapsFlow.update { 5 }
        every { repository.observeRemainingFuelLaps() } returns remainingFuelLapsFlow
        coEvery { repository.saveRemainingFuelLaps(GT7_PS5_REMAINING_FUEL_LAPS_DEFAULT) } answers {
            remainingFuelLapsFlow.update { GT7_PS5_REMAINING_FUEL_LAPS_DEFAULT }
        }
        val viewModel = createViewModel()

        viewModel.onResetRemainingFuelLaps()

        assertEquals(GT7_PS5_REMAINING_FUEL_LAPS_DEFAULT, viewModel.uiState.first().remainingFuelLaps)
        verify(exactly = 1) { repository.observeRemainingFuelLaps() }
        coVerify(exactly = 1) { repository.saveRemainingFuelLaps(GT7_PS5_REMAINING_FUEL_LAPS_DEFAULT) }
        confirmVerified(repository)
    }

    @Test
    fun `onPreviewClickedを呼ぶと設定中の燃料残り周回数イベントが再生される`() = runTest {
        remainingFuelLapsFlow.update { 4 }
        every { repository.observeRemainingFuelLaps() } returns remainingFuelLapsFlow
        val viewModel = createViewModel()
        assertEquals(4, viewModel.uiState.first().remainingFuelLaps)

        viewModel.onPreviewClicked()

        verify(exactly = 1) { repository.observeRemainingFuelLaps() }
        verify(exactly = 1) { ttsEngine.speak(SpeechEvent.RemainingFuelLapsWarning(4), false) }
        verify(exactly = 1) { ttsEngine.speak(SpeechEvent.RemainingFuelLapsWarning(0), true) }
        confirmVerified(repository, ttsEngine)
    }
}
