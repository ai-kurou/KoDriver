@file:Suppress("FunctionNaming")

package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class SaveServerIpUseCaseTest {

    private val repo = FakeServerIpRepository()
    private val useCase = SaveServerIpUseCase(repo)

    @Test
    fun `IPアドレスを保存できる`() = runBlocking {
        useCase("192.168.1.10")

        assertEquals("192.168.1.10", repo.serverIp().first())
    }

    @Test
    fun `上書き保存で最新のIPアドレスが返る`() = runBlocking {
        useCase("192.168.1.10")
        useCase("10.0.0.1")

        assertEquals("10.0.0.1", repo.serverIp().first())
    }
}
