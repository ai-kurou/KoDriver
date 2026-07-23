package kurou.kodriver.feature.lmuwindowsreadout.tyreweardetail

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

class LmuWindowsReadoutTyreWearDetailViewModelTest {

    @MockK
    private lateinit var ttsEngine: TextToSpeechEngine

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `onWarningChipClickedを呼ぶとTyreWearWarningイベントが再生される`() {
        every { ttsEngine.speak(SpeechEvent.TyreWearWarning, false) } returns Unit
        val viewModel = LmuWindowsReadoutTyreWearDetailViewModel(PlaySpeechEventUseCase(ttsEngine))

        viewModel.onWarningChipClicked()

        verify(exactly = 1) { ttsEngine.speak(SpeechEvent.TyreWearWarning, false) }
        confirmVerified(ttsEngine)
    }
}
