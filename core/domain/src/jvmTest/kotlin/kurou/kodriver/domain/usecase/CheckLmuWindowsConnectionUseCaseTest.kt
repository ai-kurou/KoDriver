package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.LmuWindowsRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CheckLmuWindowsConnectionUseCaseTest {

    @MockK
    private lateinit var repository: LmuWindowsRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `Repositoryが接続済みならtrueを返す`() = runBlocking {
        coEvery { repository.isConnected() } returns true
        val useCase = CheckLmuWindowsConnectionUseCase(repository)

        assertTrue(useCase())
        coVerify(exactly = 1) { repository.isConnected() }
        confirmVerified(repository)
    }

    @Test
    fun `Repositoryが未接続ならfalseを返す`() = runBlocking {
        coEvery { repository.isConnected() } returns false
        val useCase = CheckLmuWindowsConnectionUseCase(repository)

        assertFalse(useCase())
        coVerify(exactly = 1) { repository.isConnected() }
        confirmVerified(repository)
    }
}
