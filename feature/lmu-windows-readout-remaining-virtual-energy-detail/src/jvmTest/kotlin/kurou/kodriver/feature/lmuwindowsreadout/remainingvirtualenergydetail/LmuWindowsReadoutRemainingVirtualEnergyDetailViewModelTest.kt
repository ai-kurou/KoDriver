@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.lmuwindowsreadout.remainingvirtualenergydetail

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
import kurou.kodriver.domain.repository.LmuWindowsRemainingVirtualEnergyPreferencesRepository
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRemainingVirtualEnergyThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsRemainingVirtualEnergyThresholdPercentageUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsReadoutRemainingVirtualEnergyDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @MockK
    private lateinit var repository: LmuWindowsRemainingVirtualEnergyPreferencesRepository

    @MockK
    private lateinit var ttsEngine: TextToSpeechEngine

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
        LmuWindowsReadoutRemainingVirtualEnergyDetailViewModel(
        observeThresholdPercentage = ObserveLmuWindowsRemainingVirtualEnergyThresholdPercentageUseCase(repository),
        saveThresholdPercentage = SaveLmuWindowsRemainingVirtualEnergyThresholdPercentageUseCase(repository),
        playSpeechEvent = PlaySpeechEventUseCase(ttsEngine),
    )

    @Test
    fun `初期状態はリポジトリのデフォルト値を反映したUiStateを返す`() =
        runTest {
        every { repository.observeThresholdPercentage() } returns MutableStateFlow(30)
        val viewModel = createViewModel()

        assertEquals(
            LmuWindowsReadoutRemainingVirtualEnergyDetailUiState(thresholdPercentage = 30),
            viewModel.uiState.first(),
        )
        verify(exactly = 1) { repository.observeThresholdPercentage() }
        confirmVerified(repository)
    }

    @Test
    fun `onThresholdChangedを呼ぶとuiStateのthresholdPercentageが更新される`() =
        runTest {
        val thresholdFlow = MutableStateFlow(50)
        every { repository.observeThresholdPercentage() } returns thresholdFlow
        coEvery { repository.saveThresholdPercentage(30) } answers { thresholdFlow.update { 30 } }
        val viewModel = createViewModel()

        viewModel.onThresholdChanged(30)

        assertEquals(30, viewModel.uiState.first().thresholdPercentage)
        verify(exactly = 1) { repository.observeThresholdPercentage() }
        coVerify(exactly = 1) { repository.saveThresholdPercentage(30) }
        confirmVerified(repository)
    }

    @Test
    fun `onThresholdResetを呼ぶとthresholdPercentageがデフォルト値30に戻る`() =
        runTest {
        val thresholdFlow = MutableStateFlow(50)
        every { repository.observeThresholdPercentage() } returns thresholdFlow
        coEvery { repository.saveThresholdPercentage(30) } answers { thresholdFlow.update { 30 } }
        val viewModel = createViewModel()

        viewModel.onThresholdReset()

        assertEquals(30, viewModel.uiState.first().thresholdPercentage)
        verify(exactly = 1) { repository.observeThresholdPercentage() }
        coVerify(exactly = 1) { repository.saveThresholdPercentage(30) }
        confirmVerified(repository)
    }

    @Test
    fun `onWarningChipClickedを呼ぶとRemainingVirtualEnergyWarningイベントが再生される`() {
        every { repository.observeThresholdPercentage() } returns MutableStateFlow(50)
        every { ttsEngine.speak(SpeechEvent.RemainingVirtualEnergyWarning, false) } returns Unit
        val viewModel = createViewModel()

        viewModel.onWarningChipClicked()

        verify(exactly = 1) { repository.observeThresholdPercentage() }
        verify(exactly = 1) { ttsEngine.speak(SpeechEvent.RemainingVirtualEnergyWarning, false) }
        confirmVerified(repository, ttsEngine)
    }
}
