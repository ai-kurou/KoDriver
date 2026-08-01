@file:Suppress("FunctionNaming")

package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.ServerIpPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private fun createServerIpPreferencesRepository(repository: ServerIpPreferencesRepository): ServerIpPreferencesRepository {
    val state = MutableStateFlow<String?>(null)
    every { repository.serverIp() } returns state
    listOf("192.168.1.10", "10.0.0.1").forEach { serverIp ->
        coEvery { repository.saveServerIp(serverIp) } answers { state.update { serverIp } }
    }
    return repository
}

class ObserveServerIpUseCaseTest {
    @MockK
    private lateinit var repository: ServerIpPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `初期状態でnullを返す`() =
        runBlocking {
            val repo = createServerIpPreferencesRepository(repository)
            val useCase = ObserveServerIpUseCase(repo)

            assertNull(useCase().first())

            verify(exactly = 1) { repo.serverIp() }
            confirmVerified(repo)
        }

    @Test
    fun `保存後にIPアドレスを返す`() =
        runBlocking {
            val repo = createServerIpPreferencesRepository(repository)
            val useCase = ObserveServerIpUseCase(repo)

            repo.saveServerIp("192.168.1.10")

            assertEquals("192.168.1.10", useCase().first())
            coVerify(exactly = 1) { repo.saveServerIp("192.168.1.10") }
            verify(exactly = 1) { repo.serverIp() }
            confirmVerified(repo)
        }
}
