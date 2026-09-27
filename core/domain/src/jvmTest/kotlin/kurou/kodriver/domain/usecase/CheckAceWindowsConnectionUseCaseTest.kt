package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.AceWindowsFuelRepository
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CheckAceWindowsConnectionUseCaseTest {
    private val repository: AceWindowsFuelRepository = mockk()

    @Test
    fun `Repositoryが接続済みならtrueを返す`() =
        runTest {
            coEvery { repository.isConnected() } returns true
            val useCase = CheckAceWindowsConnectionUseCase(repository)

            assertTrue(useCase())
            coVerify(exactly = 1) { repository.isConnected() }
            confirmVerified(repository)
        }

    @Test
    fun `Repositoryが未接続ならfalseを返す`() =
        runTest {
            coEvery { repository.isConnected() } returns false
            val useCase = CheckAceWindowsConnectionUseCase(repository)

            assertFalse(useCase())
            coVerify(exactly = 1) { repository.isConnected() }
            confirmVerified(repository)
        }
}
