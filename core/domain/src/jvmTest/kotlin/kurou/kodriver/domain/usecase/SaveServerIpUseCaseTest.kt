@file:Suppress("FunctionNaming")

package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.ServerIpPreferencesRepository
import kotlin.test.Test

class SaveServerIpUseCaseTest {

    @Test
    fun `IPアドレスを保存できる`() = runBlocking {
        val repository = mockk<ServerIpPreferencesRepository>(relaxUnitFun = true)

        SaveServerIpUseCase(repository)("192.168.1.10")

        coVerify(exactly = 1) { repository.saveServerIp("192.168.1.10") }
        confirmVerified(repository)
    }

    @Test
    fun `上書き保存で最新のIPアドレスが返る`() = runBlocking {
        val repository = mockk<ServerIpPreferencesRepository>(relaxUnitFun = true)
        val useCase = SaveServerIpUseCase(repository)

        useCase("192.168.1.10")
        useCase("10.0.0.1")

        coVerify(exactly = 1) { repository.saveServerIp("192.168.1.10") }
        coVerify(exactly = 1) { repository.saveServerIp("10.0.0.1") }
        confirmVerified(repository)
    }
}
