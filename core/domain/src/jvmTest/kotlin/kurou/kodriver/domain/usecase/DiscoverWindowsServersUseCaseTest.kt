package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.DiscoveredServer
import kurou.kodriver.domain.repository.WindowsServerDiscoveryRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class DiscoverWindowsServersUseCaseTest {
    private val repository: WindowsServerDiscoveryRepository = mockk()

    @Test
    fun `検出されたサーバーの一覧を返す`() =
        runTest {
            val servers = listOf(DiscoveredServer("DESKTOP-A", "192.168.1.10"))
            every { repository.discover() } returns flowOf(servers)

            assertEquals(servers, DiscoverWindowsServersUseCase(repository)().first())

            verify(exactly = 1) { repository.discover() }
            confirmVerified(repository)
        }
}
