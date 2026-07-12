package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.LmuWindowsRepository
import kotlin.test.Test

class DisconnectLmuWindowsUseCaseTest {

    @Test
    fun `invokeはリポジトリのdisconnectを呼ぶ`() = runBlocking {
        val repo = mockk<LmuWindowsRepository>()
        coEvery { repo.disconnect() } returns Unit
        val useCase = DisconnectLmuWindowsUseCase(repo)

        useCase()

        coVerify(exactly = 1) { repo.disconnect() }
        confirmVerified(repo)
    }

    @Test
    fun `invokeを呼ぶ前はdisconnectが呼ばれていない`() {
        val repo = mockk<LmuWindowsRepository>()

        confirmVerified(repo)
    }
}
