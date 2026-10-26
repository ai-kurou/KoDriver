package kurou.kodriver.feature.otherlist

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5UdpPortPreferencesRepository
import kurou.kodriver.domain.gt7ps5.usecase.ObserveGt7Ps5UdpPortUseCase
import kurou.kodriver.domain.repository.ConsoleAddressPreferencesRepository
import kurou.kodriver.domain.repository.ServerIpPreferencesRepository
import kurou.kodriver.domain.usecase.ObserveConsoleAddressUseCase
import kurou.kodriver.domain.usecase.ObserveServerIpUseCase
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class OtherListConnectionFlowTest {
    private val serverIpRepository: ServerIpPreferencesRepository = mockk()
    private val consoleAddressRepository: ConsoleAddressPreferencesRepository = mockk()
    private val consolePortRepository: Gt7Ps5UdpPortPreferencesRepository = mockk()

    @Test
    fun `初期状態と3つの接続情報の更新を反映する`() =
        runTest {
            val serverIp = MutableStateFlow<String?>(null)
            val consoleAddress = MutableStateFlow<String?>(null)
            val consolePort = MutableStateFlow(33740)
            every { serverIpRepository.serverIp() } returns serverIp
            every { consoleAddressRepository.consoleAddress() } returns consoleAddress
            every { consolePortRepository.port() } returns consolePort
            val flow = createFlow()

            assertEquals(OtherListConnectionUiState(), flow.uiState.first())
            val updatedState =
                async(UnconfinedTestDispatcher(testScheduler)) {
                    flow.uiState.first { it == OtherListConnectionUiState("192.168.1.2", "192.168.1.3", 33741) }
                }
            serverIp.update { "192.168.1.2" }
            consoleAddress.update { "192.168.1.3" }
            consolePort.update { 33741 }

            assertEquals(OtherListConnectionUiState("192.168.1.2", "192.168.1.3", 33741), updatedState.await())
            verify(exactly = 1) { serverIpRepository.serverIp() }
            verify(exactly = 1) { consoleAddressRepository.consoleAddress() }
            verify(exactly = 1) { consolePortRepository.port() }
            confirmVerified(serverIpRepository, consoleAddressRepository, consolePortRepository)
        }

    @Test
    fun `サーバーIP UseCase省略時はnullを返し呼び出さない`() =
        runTest {
            every { consoleAddressRepository.consoleAddress() } returns MutableStateFlow("192.168.1.3")
            every { consolePortRepository.port() } returns MutableStateFlow(33741)
            val flow =
                OtherListConnectionFlow(
                    observeServerIp = null,
                    observeConsoleAddress = ObserveConsoleAddressUseCase(consoleAddressRepository),
                    observeGt7Ps5UdpPort = ObserveGt7Ps5UdpPortUseCase(consolePortRepository),
                )

            assertEquals(OtherListConnectionUiState(null, "192.168.1.3", 33741), flow.uiState.first())
            verify(exactly = 0) { serverIpRepository.serverIp() }
            verify(exactly = 1) { consoleAddressRepository.consoleAddress() }
            verify(exactly = 1) { consolePortRepository.port() }
            confirmVerified(serverIpRepository, consoleAddressRepository, consolePortRepository)
        }

    private fun createFlow() =
        OtherListConnectionFlow(
            observeServerIp = ObserveServerIpUseCase(serverIpRepository),
            observeConsoleAddress = ObserveConsoleAddressUseCase(consoleAddressRepository),
            observeGt7Ps5UdpPort = ObserveGt7Ps5UdpPortUseCase(consolePortRepository),
        )
}
