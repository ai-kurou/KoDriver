@file:Suppress("FunctionNaming")

package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.ServerIpPreferencesRepository
import kotlin.test.Test

class SaveServerIpUseCaseTest {
    private val repository: ServerIpPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `IPアドレスを保存できる`() =
        runTest {
            SaveServerIpUseCase(repository)("192.168.1.10")

            coVerify(exactly = 1) { repository.saveServerIp("192.168.1.10") }
            confirmVerified(repository)
        }

    @Test
    fun `上書き保存で最新のIPアドレスが返る`() =
        runTest {
            val useCase = SaveServerIpUseCase(repository)

            useCase("192.168.1.10")
            useCase("10.0.0.1")

            coVerify(exactly = 1) { repository.saveServerIp("192.168.1.10") }
            coVerify(exactly = 1) { repository.saveServerIp("10.0.0.1") }
            confirmVerified(repository)
        }
}
