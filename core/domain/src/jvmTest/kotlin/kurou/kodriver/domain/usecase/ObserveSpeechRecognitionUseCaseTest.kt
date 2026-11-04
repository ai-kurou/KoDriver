package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.SpeechRecognitionEvent
import kurou.kodriver.domain.model.SpeechRecognitionFailureReason
import kurou.kodriver.domain.repository.SpeechRecognitionRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveSpeechRecognitionUseCaseTest {
    private val repository: SpeechRecognitionRepository = mockk()

    @Test
    fun `Repositoryの認識イベントをそのまま流す`() =
        runTest {
            val events =
                listOf(
                    SpeechRecognitionEvent.Listening,
                    SpeechRecognitionEvent.Partial("燃料"),
                    SpeechRecognitionEvent.Recognized(listOf("燃料残量", "燃料")),
                    SpeechRecognitionEvent.Failed(SpeechRecognitionFailureReason.Unavailable),
                )
            every { repository.observeRecognition() } returns flowOf(*events.toTypedArray())

            assertEquals(events, ObserveSpeechRecognitionUseCase(repository)().toList())
            verify(exactly = 1) { repository.observeRecognition() }
            confirmVerified(repository)
        }
}
