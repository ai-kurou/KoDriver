@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.lmuwindowsreadout.tyreweardetail

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
import kurou.kodriver.domain.repository.LmuWindowsTyreWearPreferencesRepository
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreWearThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsTyreWearThresholdPercentageUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsReadoutTyreWearDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @MockK
    private lateinit var repository: LmuWindowsTyreWearPreferencesRepository

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
        LmuWindowsReadoutTyreWearDetailViewModel(
        observeThresholdPercentage = ObserveLmuWindowsTyreWearThresholdPercentageUseCase(repository),
        saveThresholdPercentage = SaveLmuWindowsTyreWearThresholdPercentageUseCase(repository),
        playSpeechEvent = PlaySpeechEventUseCase(ttsEngine),
    )

    @Test
    fun `初期状態はリポジトリのデフォルト値を反映したUiStateを返す`() =
        runTest {
        every { repository.observeThresholdPercentage() } returns MutableStateFlow(50)
        val viewModel = createViewModel()

        assertEquals(
            LmuWindowsReadoutTyreWearDetailUiState(thresholdPercentage = 50),
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
    fun `onThresholdResetを呼ぶとthresholdPercentageがデフォルト値50に戻る`() =
        runTest {
        val thresholdFlow = MutableStateFlow(50)
        every { repository.observeThresholdPercentage() } returns thresholdFlow
        coEvery { repository.saveThresholdPercentage(30) } answers { thresholdFlow.update { 30 } }
        coEvery { repository.saveThresholdPercentage(50) } answers { thresholdFlow.update { 50 } }
        val viewModel = createViewModel()

        viewModel.onThresholdChanged(30)
        viewModel.onThresholdReset()

        assertEquals(50, viewModel.uiState.first().thresholdPercentage)
        verify(exactly = 1) { repository.observeThresholdPercentage() }
        coVerify(exactly = 1) { repository.saveThresholdPercentage(30) }
        coVerify(exactly = 1) { repository.saveThresholdPercentage(50) }
        confirmVerified(repository)
    }

    @Test
    fun `onWarningChipClickedを呼ぶとTyreWearWarningイベントが再生される`() {
        every { repository.observeThresholdPercentage() } returns MutableStateFlow(50)
        every { ttsEngine.speak(SpeechEvent.TyreWearWarning, false) } returns Unit
        val viewModel = createViewModel()

        viewModel.onWarningChipClicked()

        verify(exactly = 1) { repository.observeThresholdPercentage() }
        verify(exactly = 1) { ttsEngine.speak(SpeechEvent.TyreWearWarning, false) }
        confirmVerified(repository, ttsEngine)
    }
}
