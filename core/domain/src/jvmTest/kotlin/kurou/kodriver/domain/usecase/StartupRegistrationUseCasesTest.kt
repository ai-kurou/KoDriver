package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.StartupEnabledRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class StartupRegistrationUseCasesTest {
    @MockK
    private lateinit var repository: StartupEnabledRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `getEnabledはRepositoryの値をそのまま返す`() =
        runTest {
            coEvery { repository.isEnabled() } returns true
            val useCases = StartupRegistrationUseCases(repository)

            val result = useCases.getEnabled()

            assertTrue(result)
            coVerify(exactly = 1) { repository.isEnabled() }
            confirmVerified(repository)
        }

    @Test
    fun `setEnabledはRepositoryにそのまま値を渡す`() =
        runTest {
            coEvery { repository.setEnabled(true) } returns Unit
            val useCases = StartupRegistrationUseCases(repository)

            useCases.setEnabled(true)

            coVerify(exactly = 1) { repository.setEnabled(true) }
            confirmVerified(repository)
        }
}
