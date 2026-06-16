package kurou.kodriver.domain.usecase

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CheckServerConnectionUseCaseTest {

    @Test
    fun `Repositoryが接続済みならtrueを返す`() = runBlocking {
        val useCase = CheckServerConnectionUseCase(FakeServerConnectionRepository(connected = true))

        assertTrue(useCase("192.168.1.1"))
    }

    @Test
    fun `Repositoryが未接続ならfalseを返す`() = runBlocking {
        val useCase = CheckServerConnectionUseCase(FakeServerConnectionRepository(connected = false))

        assertFalse(useCase("192.168.1.1"))
    }
}
