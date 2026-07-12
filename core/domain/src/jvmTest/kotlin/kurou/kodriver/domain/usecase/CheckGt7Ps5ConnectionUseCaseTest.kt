package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.Gt7Ps5Repository
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CheckGt7Ps5ConnectionUseCaseTest {

    @Test
    fun `Repositoryが接続済みならtrueを返す`() = runBlocking {
        val repository = mockk<Gt7Ps5Repository>()
        coEvery { repository.isConnected() } returns true
        val useCase = CheckGt7Ps5ConnectionUseCase(repository)

        assertTrue(useCase())
    }

    @Test
    fun `Repositoryが未接続ならfalseを返す`() = runBlocking {
        val repository = mockk<Gt7Ps5Repository>()
        coEvery { repository.isConnected() } returns false
        val useCase = CheckGt7Ps5ConnectionUseCase(repository)

        assertFalse(useCase())
    }
}
