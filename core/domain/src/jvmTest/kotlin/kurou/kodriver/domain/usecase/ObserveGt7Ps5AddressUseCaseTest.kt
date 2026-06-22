package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ObserveGt7Ps5AddressUseCaseTest {

    @Test
    fun `保存済みアドレスを返す`() = runBlocking {
        val repo = FakeGt7Ps5AddressRepository(initial = "192.168.1.100")
        assertEquals("192.168.1.100", ObserveGt7Ps5AddressUseCase(repo)().first())
    }

    @Test
    fun `未設定の場合はnullを返す`() = runBlocking {
        val repo = FakeGt7Ps5AddressRepository(initial = null)
        assertNull(ObserveGt7Ps5AddressUseCase(repo)().first())
    }
}
