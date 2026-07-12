package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.LmuWindowsRepository
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CheckLmuWindowsConnectionUseCaseTest {

    @Test
    fun `Repositoryが接続済みならtrueを返す`() = runBlocking {
        val repository = mockk<LmuWindowsRepository>()
        coEvery { repository.isConnected() } returns true
        val useCase = CheckLmuWindowsConnectionUseCase(repository)

        assertTrue(useCase())
    }

    @Test
    fun `Repositoryが未接続ならfalseを返す`() = runBlocking {
        val repository = mockk<LmuWindowsRepository>()
        coEvery { repository.isConnected() } returns false
        val useCase = CheckLmuWindowsConnectionUseCase(repository)

        assertFalse(useCase())
    }
}
