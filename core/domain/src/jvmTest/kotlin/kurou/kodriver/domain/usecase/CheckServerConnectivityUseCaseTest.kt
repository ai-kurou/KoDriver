package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.ServerConnectivityRepository
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CheckServerConnectivityUseCaseTest {
    private val repository: ServerConnectivityRepository = mockk()

    @Test
    fun `到達できる場合はtrueを返す`() =
        runTest {
            coEvery { repository.isReachable("192.168.1.10") } returns true

            assertTrue(CheckServerConnectivityUseCase(repository)("192.168.1.10"))

            coVerify(exactly = 1) { repository.isReachable("192.168.1.10") }
            confirmVerified(repository)
        }

    @Test
    fun `到達できない場合はfalseを返す`() =
        runTest {
            coEvery { repository.isReachable("192.168.1.20") } returns false

            assertFalse(CheckServerConnectivityUseCase(repository)("192.168.1.20"))

            coVerify(exactly = 1) { repository.isReachable("192.168.1.20") }
            confirmVerified(repository)
        }
}
