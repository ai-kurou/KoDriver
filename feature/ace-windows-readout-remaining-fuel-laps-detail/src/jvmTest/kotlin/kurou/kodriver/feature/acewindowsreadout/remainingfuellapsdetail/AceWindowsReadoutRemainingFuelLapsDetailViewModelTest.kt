@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.acewindowsreadout.remainingfuellapsdetail

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
import kurou.kodriver.domain.model.ACE_WINDOWS_REMAINING_FUEL_LAPS_THRESHOLD_DEFAULT
import kurou.kodriver.domain.repository.AceWindowsRemainingFuelLapsPreferencesRepository
import kurou.kodriver.domain.usecase.ObserveAceWindowsRemainingFuelLapsThresholdUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsRemainingFuelLapsThresholdUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class AceWindowsReadoutRemainingFuelLapsDetailViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val repository: AceWindowsRemainingFuelLapsPreferencesRepository = mockk()

    private val ttsEngine: TextToSpeechEngine = mockk(relaxUnitFun = true)

    private val remainingFuelLapsFlow = MutableStateFlow(ACE_WINDOWS_REMAINING_FUEL_LAPS_THRESHOLD_DEFAULT)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        AceWindowsReadoutRemainingFuelLapsDetailViewModel(
            observeAceWindowsRemainingFuelLapsThreshold =
                ObserveAceWindowsRemainingFuelLapsThresholdUseCase(repository),
            saveAceWindowsRemainingFuelLapsThreshold = SaveAceWindowsRemainingFuelLapsThresholdUseCase(repository),
            playSpeechEvent = PlaySpeechEventUseCase(ttsEngine),
        )

    @Test
    fun `初期状態は燃料残り周回数3のUiStateを返す`() =
        runTest {
            every { repository.observeThresholdLaps() } returns remainingFuelLapsFlow
            val viewModel = createViewModel()

            assertEquals(ACE_WINDOWS_REMAINING_FUEL_LAPS_THRESHOLD_DEFAULT, viewModel.uiState.first().remainingFuelLaps)
            verify(exactly = 1) { repository.observeThresholdLaps() }
            confirmVerified(repository)
        }

    @Test
    fun `onRemainingFuelLapsChangedに1を渡すと燃料残り周回数が1になる`() =
        runTest {
            every { repository.observeThresholdLaps() } returns remainingFuelLapsFlow
            coEvery { repository.saveThresholdLaps(1) } answers { remainingFuelLapsFlow.update { 1 } }
            val viewModel = createViewModel()

            viewModel.onRemainingFuelLapsChanged(1)

            assertEquals(1, viewModel.uiState.first().remainingFuelLaps)
            verify(exactly = 1) { repository.observeThresholdLaps() }
            coVerify(exactly = 1) { repository.saveThresholdLaps(1) }
            confirmVerified(repository)
        }

    @Test
    fun `onResetRemainingFuelLapsを呼ぶと燃料残り周回数が3になる`() =
        runTest {
            remainingFuelLapsFlow.update { 5 }
            every { repository.observeThresholdLaps() } returns remainingFuelLapsFlow
            coEvery { repository.saveThresholdLaps(ACE_WINDOWS_REMAINING_FUEL_LAPS_THRESHOLD_DEFAULT) } answers {
                remainingFuelLapsFlow.update { ACE_WINDOWS_REMAINING_FUEL_LAPS_THRESHOLD_DEFAULT }
            }
            val viewModel = createViewModel()

            viewModel.onResetRemainingFuelLaps()

            assertEquals(ACE_WINDOWS_REMAINING_FUEL_LAPS_THRESHOLD_DEFAULT, viewModel.uiState.first().remainingFuelLaps)
            verify(exactly = 1) { repository.observeThresholdLaps() }
            coVerify(exactly = 1) { repository.saveThresholdLaps(ACE_WINDOWS_REMAINING_FUEL_LAPS_THRESHOLD_DEFAULT) }
            confirmVerified(repository)
        }

    @Test
    fun `onPreviewClickedを呼ぶと設定中の燃料残り周回数イベントが再生される`() =
        runTest {
            remainingFuelLapsFlow.update { 4 }
            every { repository.observeThresholdLaps() } returns remainingFuelLapsFlow
            val viewModel = createViewModel()
            assertEquals(4, viewModel.uiState.first().remainingFuelLaps)

            viewModel.onPreviewClicked()

            verify(exactly = 1) { repository.observeThresholdLaps() }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.AceWindowsRemainingFuelLapsWarning(4), false) }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.AceWindowsRemainingFuelLapsWarning(0), true) }
            confirmVerified(repository, ttsEngine)
        }
}
