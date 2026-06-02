package kurou.kodriver.domain.usecase

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DisconnectLmuUseCaseTest {

    @Test
    fun `invokeはリポジトリのdisconnectを呼ぶ`() = runBlocking {
        val repo = FakeLmuRepository()
        val useCase = DisconnectLmuUseCase(repo)

        useCase()

        assertTrue(repo.disconnectCalled)
    }

    @Test
    fun `invokeを呼ぶ前はdisconnectが呼ばれていない`() {
        val repo = FakeLmuRepository()

        assertFalse(repo.disconnectCalled)
    }
}
