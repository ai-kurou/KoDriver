package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.LmuWindowsRepository
import kotlin.test.BeforeTest
import kotlin.test.Test

class DisconnectLmuWindowsUseCaseTest {
    @MockK
    private lateinit var repo: LmuWindowsRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `invokeはリポジトリのdisconnectを呼ぶ`() =
        runBlocking {
            coEvery { repo.disconnect() } returns Unit
            val useCase = DisconnectLmuWindowsUseCase(repo)

            useCase()

            coVerify(exactly = 1) { repo.disconnect() }
            confirmVerified(repo)
        }

    @Test
    fun `invokeを呼ぶ前はdisconnectが呼ばれていない`() {
        confirmVerified(repo)
    }
}
