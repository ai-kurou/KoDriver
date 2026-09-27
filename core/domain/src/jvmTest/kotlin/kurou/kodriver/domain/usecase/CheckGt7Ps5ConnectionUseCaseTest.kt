package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.Gt7Ps5Repository
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CheckGt7Ps5ConnectionUseCaseTest {
    private val repository: Gt7Ps5Repository = mockk()

    @Test
    fun `Repositoryが接続済みならtrueを返す`() =
        runTest {
            coEvery { repository.isConnected() } returns true
            val useCase = CheckGt7Ps5ConnectionUseCase(repository)

            assertTrue(useCase())
            coVerify(exactly = 1) { repository.isConnected() }
            confirmVerified(repository)
        }

    @Test
    fun `Repositoryが未接続ならfalseを返す`() =
        runTest {
            coEvery { repository.isConnected() } returns false
            val useCase = CheckGt7Ps5ConnectionUseCase(repository)

            assertFalse(useCase())
            coVerify(exactly = 1) { repository.isConnected() }
            confirmVerified(repository)
        }
}
