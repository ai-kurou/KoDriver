package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.KeepScreenOnEnabledRepository
import kotlin.test.Test
import kotlin.test.assertTrue

class ObserveKeepScreenOnEnabledUseCaseTest {
    private val repository: KeepScreenOnEnabledRepository = mockk()

    @Test
    fun `Repositoryの値を返す`() =
        runTest {
            every { repository.keepScreenOn() } returns flowOf(true)

            assertTrue(ObserveKeepScreenOnEnabledUseCase(repository)().first())
            verify(exactly = 1) { repository.keepScreenOn() }
            confirmVerified(repository)
        }
}
