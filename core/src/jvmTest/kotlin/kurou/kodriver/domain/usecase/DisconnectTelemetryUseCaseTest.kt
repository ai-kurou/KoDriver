package kurou.kodriver.domain.usecase

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DisconnectTelemetryUseCaseTest {

    @Test
    fun `invokeはリポジトリのdisconnectを呼ぶ`() = runBlocking {
        val repo = FakeTelemetryRepository()
        val useCase = DisconnectTelemetryUseCase(repo)

        useCase()

        assertTrue(repo.disconnectCalled)
    }

    @Test
    fun `invokeを呼ぶ前はdisconnectが呼ばれていない`() {
        val repo = FakeTelemetryRepository()

        assertFalse(repo.disconnectCalled)
    }
}
