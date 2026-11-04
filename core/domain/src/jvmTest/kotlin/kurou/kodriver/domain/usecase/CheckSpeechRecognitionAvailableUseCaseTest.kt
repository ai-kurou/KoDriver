package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kurou.kodriver.domain.repository.SpeechRecognitionRepository
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CheckSpeechRecognitionAvailableUseCaseTest {
    private val repository: SpeechRecognitionRepository = mockk()

    @Test
    fun `Repositoryがtrueを返す場合trueを返す`() {
        every { repository.isAvailable() } returns true

        assertTrue(CheckSpeechRecognitionAvailableUseCase(repository)())
        verify(exactly = 1) { repository.isAvailable() }
        confirmVerified(repository)
    }

    @Test
    fun `Repositoryがfalseを返す場合falseを返す`() {
        every { repository.isAvailable() } returns false

        assertFalse(CheckSpeechRecognitionAvailableUseCase(repository)())
        verify(exactly = 1) { repository.isAvailable() }
        confirmVerified(repository)
    }
}
