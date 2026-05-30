package kurou.kodriver.domain.usecase

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CheckLmuConnectionUseCaseTest {

    @Test
    fun `接続中の場合にtrueを返す`() = runBlocking {
        val useCase = CheckLmuConnectionUseCase(FakeLmuRepository(connected = true))

        assertTrue(useCase())
    }

    @Test
    fun `未接続の場合にfalseを返す`() = runBlocking {
        val useCase = CheckLmuConnectionUseCase(FakeLmuRepository(connected = false))

        assertFalse(useCase())
    }
}
