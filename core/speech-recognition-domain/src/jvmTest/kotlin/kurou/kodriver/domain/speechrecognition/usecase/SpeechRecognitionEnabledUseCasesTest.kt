package kurou.kodriver.domain.speechrecognition.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kurou.kodriver.domain.speechrecognition.repository.SpeechRecognitionEnabledRepository
import kotlin.test.Test
import kotlin.test.assertSame

class SpeechRecognitionEnabledUseCasesTest {
    private val repository: SpeechRecognitionEnabledRepository = mockk()

    @Test
    fun `ObserveはRepositoryの状態をそのまま返す`() {
        val enabled = MutableStateFlow(true)
        every { repository.enabled } returns enabled

        assertSame(enabled, ObserveSpeechRecognitionEnabledUseCase(repository)())

        verify(exactly = 1) { repository.enabled }
        confirmVerified(repository)
    }

    @Test
    fun `ReportはRepositoryへ状態を伝える`() {
        every { repository.update(true) } returns Unit

        ReportSpeechRecognitionEnabledUseCase(repository)(true)

        verify(exactly = 1) { repository.update(true) }
        confirmVerified(repository)
    }
}
