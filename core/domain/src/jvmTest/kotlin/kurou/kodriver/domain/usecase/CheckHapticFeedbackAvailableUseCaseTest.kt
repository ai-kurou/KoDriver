package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kurou.kodriver.domain.repository.HapticFeedbackAvailabilityRepository
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CheckHapticFeedbackAvailableUseCaseTest {
    private val repository: HapticFeedbackAvailabilityRepository = mockk()

    @Test
    fun `Repositoryがtrueを返す場合trueを返す`() {
        every { repository.isHapticFeedbackAvailable() } returns true

        assertTrue(CheckHapticFeedbackAvailableUseCase(repository)())
        verify(exactly = 1) { repository.isHapticFeedbackAvailable() }
        confirmVerified(repository)
    }

    @Test
    fun `Repositoryがfalseを返す場合falseを返す`() {
        every { repository.isHapticFeedbackAvailable() } returns false

        assertFalse(CheckHapticFeedbackAvailableUseCase(repository)())
        verify(exactly = 1) { repository.isHapticFeedbackAvailable() }
        confirmVerified(repository)
    }
}
