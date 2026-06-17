package kurou.kodriver.domain.usecase

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CheckLmuWindowsConnectionUseCaseTest {

    @Test
    fun `Repositoryが接続済みならtrueを返す`() = runBlocking {
        val useCase = CheckLmuWindowsConnectionUseCase(FakeLmuWindowsRepository(connected = true))

        assertTrue(useCase())
    }

    @Test
    fun `Repositoryが未接続ならfalseを返す`() = runBlocking {
        val useCase = CheckLmuWindowsConnectionUseCase(FakeLmuWindowsRepository(connected = false))

        assertFalse(useCase())
    }
}
