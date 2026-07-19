package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.Gt7Ps5Repository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CheckGt7Ps5ConnectionUseCaseTest {

    @MockK
    private lateinit var repository: Gt7Ps5Repository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `Repositoryが接続済みならtrueを返す`() = runBlocking {
        coEvery { repository.isConnected() } returns true
        val useCase = CheckGt7Ps5ConnectionUseCase(repository)

        assertTrue(useCase())
        coVerify(exactly = 1) { repository.isConnected() }
        confirmVerified(repository)
    }

    @Test
    fun `Repositoryが未接続ならfalseを返す`() = runBlocking {
        coEvery { repository.isConnected() } returns false
        val useCase = CheckGt7Ps5ConnectionUseCase(repository)

        assertFalse(useCase())
        coVerify(exactly = 1) { repository.isConnected() }
        confirmVerified(repository)
    }
}
