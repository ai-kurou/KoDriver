package kurou.kodriver.domain.usecase

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CheckGt7Ps5ConnectionUseCaseTest {

    @Test
    fun `Repositoryが接続済みならtrueを返す`() = runBlocking {
        val useCase = CheckGt7Ps5ConnectionUseCase(FakeGt7Ps5Repository(connected = true))

        assertTrue(useCase())
    }

    @Test
    fun `Repositoryが未接続ならfalseを返す`() = runBlocking {
        val useCase = CheckGt7Ps5ConnectionUseCase(FakeGt7Ps5Repository(connected = false))

        assertFalse(useCase())
    }
}
