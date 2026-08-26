package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kurou.kodriver.domain.repository.HapticFeedbackAvailabilityRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CheckHapticFeedbackAvailableUseCaseTest {
    @MockK
    private lateinit var repository: HapticFeedbackAvailabilityRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

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
