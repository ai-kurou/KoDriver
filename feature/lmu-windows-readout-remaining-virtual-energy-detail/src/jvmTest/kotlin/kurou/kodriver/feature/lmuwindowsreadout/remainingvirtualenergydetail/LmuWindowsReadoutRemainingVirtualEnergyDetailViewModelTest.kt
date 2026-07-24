package kurou.kodriver.feature.lmuwindowsreadout.remainingvirtualenergydetail

import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsReadoutRemainingVirtualEnergyDetailViewModelTest {

    @MockK
    private lateinit var ttsEngine: TextToSpeechEngine

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    private fun createViewModel() = LmuWindowsReadoutRemainingVirtualEnergyDetailViewModel(
        playSpeechEvent = PlaySpeechEventUseCase(ttsEngine),
    )

    @Test
    fun `uiStateは空の状態を返す`() {
        val viewModel = createViewModel()

        assertEquals(
            LmuWindowsReadoutRemainingVirtualEnergyDetailUiState,
            viewModel.uiState.value,
        )
    }

    @Test
    fun `onWarningChipClickedを呼ぶとRemainingVirtualEnergyWarningイベントが再生される`() {
        every { ttsEngine.speak(SpeechEvent.RemainingVirtualEnergyWarning, false) } returns Unit
        val viewModel = createViewModel()

        viewModel.onWarningChipClicked()

        verify(exactly = 1) { ttsEngine.speak(SpeechEvent.RemainingVirtualEnergyWarning, false) }
        confirmVerified(ttsEngine)
    }
}
