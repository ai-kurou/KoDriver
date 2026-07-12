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

private fun createServerIpPreferencesRepository(): ServerIpPreferencesRepository {
    val repository = mockk<ServerIpPreferencesRepository>()
    val state = MutableStateFlow<String?>(null)
    every { repository.serverIp() } returns state
    coEvery { repository.saveServerIp(any()) } answers { state.update { firstArg() } }
    return repository
}

class SaveServerIpUseCaseTest {

    private val repo = createServerIpPreferencesRepository()
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
