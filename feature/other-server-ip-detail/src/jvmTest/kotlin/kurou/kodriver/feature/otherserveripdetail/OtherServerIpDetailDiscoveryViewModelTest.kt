@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otherserveripdetail

import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.repository.ServerIpPreferencesRepository
import kurou.kodriver.domain.usecase.ObserveServerIpUseCase
import kurou.kodriver.domain.usecase.SaveServerIpUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class OtherServerIpDetailDiscoveryViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    @MockK
    private lateinit var repository: ServerIpPreferencesRepository

    private val ipFlow = MutableStateFlow<String?>("192.168.1.1")

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(discoveredServers: List<DiscoveredServer> = emptyList()) =
        OtherServerIpDetailViewModel(
            observeServerIp = ObserveServerIpUseCase(repository),
            validateServerIpAddress = ValidateServerIpAddressUseCase(),
            saveServerIpWithConnectivityCheck =
                SaveServerIpWithConnectivityCheckUseCase(
                    validateServerIpAddress = ValidateServerIpAddressUseCase(),
                    connectivityChecker = ServerConnectivityChecker { true },
                    saveServerIp = SaveServerIpUseCase(repository),
                ),
            windowsServerDiscovery = WindowsServerDiscovery { flowOf(discoveredServers) },
        )

    @Test
    fun `Windows版KoDriverが検出されると検出ダイアログが表示される`() =
        runTest {
            every { repository.serverIp() } returns ipFlow
            val server = DiscoveredServer(hostName = "DESKTOP-ABC", ipAddress = "192.168.1.50")
            val viewModel = createViewModel(discoveredServers = listOf(server))

            val state = viewModel.uiState.first()
            assertTrue(state.isDiscoveryDialogVisible)
            assertEquals(listOf(server), state.discoveredServers)
            assertEquals(server, state.selectedDiscoveredServer)
            verify(exactly = 1) { repository.serverIp() }
            confirmVerified(repository)
        }

    @Test
    fun `検出ダイアログで選択するとIP入力欄へ自動入力されダイアログが閉じる`() =
        runTest {
            every { repository.serverIp() } returns ipFlow
            val server = DiscoveredServer(hostName = "DESKTOP-ABC", ipAddress = "192.168.1.50")
            val viewModel = createViewModel(discoveredServers = listOf(server))
            viewModel.uiState.first()

            viewModel.onDiscoveryDialogConfirm()

            val state = viewModel.uiState.first()
            assertEquals("192.168.1.50", state.inputIp)
            assertFalse(state.isDiscoveryDialogVisible)
            verify(exactly = 1) { repository.serverIp() }
            confirmVerified(repository)
        }

    @Test
    fun `複数検出時にサーバーを選択すると選択中サーバーが変わる`() =
        runTest {
            every { repository.serverIp() } returns ipFlow
            val serverA = DiscoveredServer(hostName = "DESKTOP-A", ipAddress = "192.168.1.10")
            val serverB = DiscoveredServer(hostName = "DESKTOP-B", ipAddress = "192.168.1.20")
            val viewModel = createViewModel(discoveredServers = listOf(serverA, serverB))

            viewModel.onDiscoveredServerSelected(serverB)
            viewModel.onDiscoveryDialogConfirm()

            assertEquals("192.168.1.20", viewModel.uiState.first().inputIp)
            verify(exactly = 1) { repository.serverIp() }
            confirmVerified(repository)
        }

    @Test
    fun `検出ダイアログをキャンセルすると閉じたままになる`() =
        runTest {
            every { repository.serverIp() } returns ipFlow
            val server = DiscoveredServer(hostName = "DESKTOP-ABC", ipAddress = "192.168.1.50")
            val viewModel = createViewModel(discoveredServers = listOf(server))

            viewModel.onDiscoveryDialogDismiss()

            val state = viewModel.uiState.first()
            assertFalse(state.isDiscoveryDialogVisible)
            assertEquals("192.168.1.1", state.inputIp)
            verify(exactly = 1) { repository.serverIp() }
            confirmVerified(repository)
        }

    @Test
    fun `検出ダイアログを閉じた後onShowDiscoveredServersで再表示できる`() =
        runTest {
            every { repository.serverIp() } returns ipFlow
            val server = DiscoveredServer(hostName = "DESKTOP-ABC", ipAddress = "192.168.1.50")
            val viewModel = createViewModel(discoveredServers = listOf(server))

            viewModel.onDiscoveryDialogDismiss()
            viewModel.onShowDiscoveredServers()

            assertTrue(viewModel.uiState.first().isDiscoveryDialogVisible)
            verify(exactly = 1) { repository.serverIp() }
            confirmVerified(repository)
        }

    @Test
    fun `検出結果がない場合は検出ダイアログを表示しない`() =
        runTest {
            every { repository.serverIp() } returns ipFlow
            val viewModel = createViewModel(discoveredServers = emptyList())

            assertFalse(viewModel.uiState.first().isDiscoveryDialogVisible)
            verify(exactly = 1) { repository.serverIp() }
            confirmVerified(repository)
        }
}
