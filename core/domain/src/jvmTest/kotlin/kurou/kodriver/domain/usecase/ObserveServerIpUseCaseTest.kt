@file:Suppress("FunctionNaming")

package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ObserveServerIpUseCaseTest {

    private val repo = FakeServerIpRepository()
    private val useCase = ObserveServerIpUseCase(repo)

    @Test
    fun `初期状態でnullを返す`() = runBlocking {
        assertNull(useCase().first())
    }

    @Test
    fun `保存後にIPアドレスを返す`() = runBlocking {
        repo.saveServerIp("192.168.1.10")

        assertEquals("192.168.1.10", useCase().first())
    }
}
