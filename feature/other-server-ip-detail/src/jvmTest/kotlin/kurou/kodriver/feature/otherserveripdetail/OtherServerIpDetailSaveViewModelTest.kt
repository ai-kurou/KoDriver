package kurou.kodriver.feature.otherserveripdetail

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.serverconnection.repository.ServerConnectivityRepository
import kurou.kodriver.domain.serverconnection.repository.ServerIpPreferencesRepository
import kurou.kodriver.domain.serverconnection.repository.WindowsServerDiscoveryRepository
import kurou.kodriver.domain.serverconnection.usecase.CheckAccessLocalNetworkPermissionGrantedUseCase
import kurou.kodriver.domain.serverconnection.usecase.CheckServerConnectivityUseCase
import kurou.kodriver.domain.serverconnection.usecase.DiscoverWindowsServersUseCase
import kurou.kodriver.domain.serverconnection.usecase.ObserveServerIpUseCase
import kurou.kodriver.domain.serverconnection.usecase.SaveServerIpUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** 入力値の省略・保存済みIPの補完・接続確認中の二重保存など、保存操作の境界を検証する。 */
@OptIn(ExperimentalCoroutinesApi::class)
class OtherServerIpDetailSaveViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val repository: ServerIpPreferencesRepository = mockk()

    private val ipFlow = MutableStateFlow<String?>("192.168.1.1")

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        reachable: Boolean = true,
        connectivity: ServerConnectivityRepository = ServerConnectivityRepository { reachable },
    ) = OtherServerIpDetailViewModel(
        observeServerIp = ObserveServerIpUseCase(repository),
        validateServerIpAddress = ValidateServerIpAddressUseCase(),
        saveServerIpWithConnectivityCheck =
            SaveServerIpWithConnectivityCheckUseCase(
                validateServerIpAddress = ValidateServerIpAddressUseCase(),
                checkServerConnectivity = CheckServerConnectivityUseCase(connectivity),
                saveServerIp = SaveServerIpUseCase(repository),
            ),
        discoverWindowsServers =
            DiscoverWindowsServersUseCase(
                WindowsServerDiscoveryRepository { flowOf(emptyList()) },
            ),
        checkAccessLocalNetworkPermissionGranted =
            CheckAccessLocalNetworkPermissionGrantedUseCase(FakeAccessLocalNetworkPermissionRepository()),
    )

    @Test
    fun `入力せずに保存すると保存済みのIPアドレスで接続を確認して保存する`() =
        runTest {
            every { repository.serverIp() } returns ipFlow
            coEvery { repository.saveServerIp("192.168.1.1") } returns Unit
            val viewModel = createViewModel(reachable = true)
            viewModel.uiState.launchIn(backgroundScope)
            runCurrent()

            viewModel.onSave()
            runCurrent()

            assertTrue(viewModel.uiState.value.isSaved)
            verify(exactly = 1) { repository.serverIp() }
            coVerify(exactly = 1) { repository.saveServerIp("192.168.1.1") }
            confirmVerified(repository)
        }

    @Test
    fun `入力せずに接続確認なしで保存すると保存済みのIPアドレスを保存する`() =
        runTest {
            every { repository.serverIp() } returns ipFlow
            coEvery { repository.saveServerIp("192.168.1.1") } returns Unit
            val viewModel = createViewModel(reachable = false)
            viewModel.uiState.launchIn(backgroundScope)
            runCurrent()

            viewModel.onSaveAnyway()
            runCurrent()

            assertTrue(viewModel.uiState.value.isSaved)
            verify(exactly = 1) { repository.serverIp() }
            coVerify(exactly = 1) { repository.saveServerIp("192.168.1.1") }
            confirmVerified(repository)
        }

    @Test
    fun `保存済みのIPアドレスも入力もなければ空文字として扱い保存しない`() =
        runTest {
            ipFlow.value = null
            every { repository.serverIp() } returns ipFlow
            val viewModel = createViewModel(reachable = true)
            viewModel.uiState.launchIn(backgroundScope)
            runCurrent()

            viewModel.onSave()
            viewModel.onSaveAnyway()
            runCurrent()

            assertFalse(viewModel.uiState.value.isSaved)
            verify(exactly = 1) { repository.serverIp() }
            confirmVerified(repository)
        }

    @Test
    fun `接続確認中に再度保存しても接続確認を重ねない`() =
        runTest {
            val reachable = CompletableDeferred<Boolean>()
            var checkCount = 0
            every { repository.serverIp() } returns ipFlow
            coEvery { repository.saveServerIp("10.0.0.2") } returns Unit
            val viewModel =
                createViewModel(
                    connectivity =
                        ServerConnectivityRepository {
                            checkCount++
                            reachable.await()
                        },
                )
            viewModel.uiState.launchIn(backgroundScope)
            runCurrent()

            viewModel.onIpChanged("10.0.0.2")
            viewModel.onSave()
            assertTrue(viewModel.uiState.value.isCheckingConnectivity)
            viewModel.onSave()
            reachable.complete(true)
            runCurrent()

            assertEquals(1, checkCount)
            assertFalse(viewModel.uiState.value.isCheckingConnectivity)
            assertTrue(viewModel.uiState.value.isSaved)
            verify(exactly = 1) { repository.serverIp() }
            coVerify(exactly = 1) { repository.saveServerIp("10.0.0.2") }
            confirmVerified(repository)
        }
}
