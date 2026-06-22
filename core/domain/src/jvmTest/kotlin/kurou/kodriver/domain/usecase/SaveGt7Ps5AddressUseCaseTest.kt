package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class SaveGt7Ps5AddressUseCaseTest {

    @Test
    fun `アドレスを保存する`() = runBlocking {
        val repo = FakeGt7Ps5AddressRepository()
        SaveGt7Ps5AddressUseCase(repo)("192.168.1.50")
        assertEquals("192.168.1.50", repo.gt7Ps5Address().first())
    }
}
