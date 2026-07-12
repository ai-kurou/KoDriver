@file:Suppress("FunctionNaming")

package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.ServerIpPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private fun createServerIpPreferencesRepository(): ServerIpPreferencesRepository {
    val repository = mockk<ServerIpPreferencesRepository>()
    val state = MutableStateFlow<String?>(null)
    every { repository.serverIp() } returns state
    coEvery { repository.saveServerIp(any()) } answers { state.update { firstArg() } }
    return repository
}

class ObserveServerIpUseCaseTest {

    private val repo = createServerIpPreferencesRepository()
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
