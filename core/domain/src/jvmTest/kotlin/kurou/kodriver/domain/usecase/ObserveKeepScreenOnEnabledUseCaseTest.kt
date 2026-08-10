package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.KeepScreenOnEnabledRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class ObserveKeepScreenOnEnabledUseCaseTest {
    @MockK
    private lateinit var repository: KeepScreenOnEnabledRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `Repositoryの値を返す`() =
        runTest {
            every { repository.keepScreenOn() } returns flowOf(true)

            assertTrue(ObserveKeepScreenOnEnabledUseCase(repository)().first())
            verify(exactly = 1) { repository.keepScreenOn() }
            confirmVerified(repository)
        }
}
