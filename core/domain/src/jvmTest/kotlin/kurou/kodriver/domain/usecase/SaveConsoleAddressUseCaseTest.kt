package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class SaveConsoleAddressUseCaseTest {

    @Test
    fun `アドレスを保存する`() = runBlocking {
        val repo = FakeConsoleAddressRepository()
        SaveConsoleAddressUseCase(repo)("192.168.1.50")
        assertEquals("192.168.1.50", repo.consoleAddress().first())
    }
}
