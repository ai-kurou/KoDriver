package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ObserveConsoleAddressUseCaseTest {

    @Test
    fun `保存済みアドレスを返す`() = runBlocking {
        val repo = FakeConsoleAddressRepository(initial = "192.168.1.100")
        assertEquals("192.168.1.100", ObserveConsoleAddressUseCase(repo)().first())
    }

    @Test
    fun `未設定の場合はnullを返す`() = runBlocking {
        val repo = FakeConsoleAddressRepository(initial = null)
        assertNull(ObserveConsoleAddressUseCase(repo)().first())
    }
}
