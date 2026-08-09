package kurou.kodriver.feature.serverconnection

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.core.model.Simulator
import kurou.kodriver.domain.repository.ServerIpPreferencesRepository
import kurou.kodriver.domain.repository.ServerVersionRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.usecase.FetchServerVersionUseCase
import kurou.kodriver.domain.usecase.ObserveKoDriverServerConnectionUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.ObserveServerIpUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ServerConnectionViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @MockK
    private lateinit var serverIpRepository: ServerIpPreferencesRepository

    @MockK
    private lateinit var versionRepository: ServerVersionRepository

    @MockK
    private lateinit var simulatorRepository: SimulatorPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(appVersion: String = "1.0.0") =
        ServerConnectionViewModel(
            observeKoDriverServerConnection =
                ObserveKoDriverServerConnectionUseCase(
                    fetchServerVersion = FetchServerVersionUseCase(versionRepository),
                    observeServerIp = ObserveServerIpUseCase(serverIpRepository),
                    observeSelectedSimulator = ObserveSelectedSimulatorUseCase(simulatorRepository),
                ),
            appVersion = appVersion,
        )

    @Test
    fun `IP設定済みで接続成功時に接続済み状態を返す`() =
        runTest {
            val ipFlow = MutableStateFlow("192.168.1.1")
            every { serverIpRepository.serverIp() } returns ipFlow
            every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(null)
            coEvery { versionRepository.fetchVersion("192.168.1.1") } returns Result.success("1.0.0")
            val viewModel = createViewModel()
            val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }

            dispatcher.scheduler.runCurrent()

            assertEquals(ServerConnectionStatus.CONNECTED, viewModel.uiState.first().connectionStatus)
            verify(exactly = 1) { serverIpRepository.serverIp() }
            verify(exactly = 1) { simulatorRepository.selectedSimulator() }
            coVerify(exactly = 1) { versionRepository.fetchVersion("192.168.1.1") }
            confirmVerified(serverIpRepository, versionRepository, simulatorRepository)
            collectionJob.cancelAndJoin()
        }

    @Test
    fun `IP未設定時はNOT_CONFIGUREDを返す`() =
        runTest {
            every { serverIpRepository.serverIp() } returns MutableStateFlow(null)
            every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(null)
            val viewModel = createViewModel()
            val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }

            dispatcher.scheduler.runCurrent()

            assertEquals(ServerConnectionStatus.NOT_CONFIGURED, viewModel.uiState.first().connectionStatus)
            verify(exactly = 1) { serverIpRepository.serverIp() }
            verify(exactly = 1) { simulatorRepository.selectedSimulator() }
            confirmVerified(serverIpRepository, versionRepository, simulatorRepository)
            collectionJob.cancelAndJoin()
        }

    @Test
    fun `選択シミュレータがuiStateに反映される`() =
        runTest {
            every { serverIpRepository.serverIp() } returns MutableStateFlow("192.168.1.1")
            every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(Simulator.LmuWindows)
            coEvery { versionRepository.fetchVersion("192.168.1.1") } returns Result.success("1.0.0")
            val viewModel = createViewModel()
            val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }

            dispatcher.scheduler.runCurrent()

            assertEquals(Simulator.LmuWindows, viewModel.uiState.first().selectedSimulator)
            verify(exactly = 1) { serverIpRepository.serverIp() }
            verify(exactly = 1) { simulatorRepository.selectedSimulator() }
            coVerify(exactly = 1) { versionRepository.fetchVersion("192.168.1.1") }
            confirmVerified(serverIpRepository, versionRepository, simulatorRepository)
            collectionJob.cancelAndJoin()
        }

    @Test
    fun `IP設定後に接続確認を開始する`() =
        runTest {
            val ipFlow = MutableStateFlow<String?>(null)
            every { serverIpRepository.serverIp() } returns ipFlow
            every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(null)
            coEvery { serverIpRepository.saveServerIp("192.168.1.100") } answers {
                ipFlow.update { "192.168.1.100" }
            }
            coEvery { versionRepository.fetchVersion("192.168.1.100") } returns Result.success("1.0.0")
            val viewModel = createViewModel()
            val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }
            dispatcher.scheduler.runCurrent()
            assertFalse(viewModel.uiState.first().isConnectionChecked)

            serverIpRepository.saveServerIp("192.168.1.100")
            dispatcher.scheduler.runCurrent()

            assertEquals(ServerConnectionStatus.CONNECTED, viewModel.uiState.first().connectionStatus)
            verify(exactly = 1) { serverIpRepository.serverIp() }
            verify(exactly = 1) { simulatorRepository.selectedSimulator() }
            coVerify(exactly = 1) { serverIpRepository.saveServerIp("192.168.1.100") }
            coVerify(exactly = 1) { versionRepository.fetchVersion("192.168.1.100") }
            confirmVerified(serverIpRepository, versionRepository, simulatorRepository)
            collectionJob.cancelAndJoin()
        }

    @Test
    fun `一定間隔で接続状態を更新する`() =
        runTest {
            every { serverIpRepository.serverIp() } returns MutableStateFlow("192.168.1.1")
            every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(null)
            coEvery { versionRepository.fetchVersion("192.168.1.1") } returnsMany
                listOf(Result.failure(RuntimeException("down")), Result.success("1.0.0"))
            val viewModel = createViewModel()
            val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }
            dispatcher.scheduler.runCurrent()
            assertEquals(ServerConnectionStatus.DISCONNECTED, viewModel.uiState.first().connectionStatus)

            dispatcher.scheduler.advanceTimeBy(1_000L)
            dispatcher.scheduler.runCurrent()

            assertEquals(ServerConnectionStatus.CONNECTED, viewModel.uiState.first().connectionStatus)
            verify(exactly = 1) { serverIpRepository.serverIp() }
            verify(exactly = 1) { simulatorRepository.selectedSimulator() }
            coVerify(exactly = 2) { versionRepository.fetchVersion("192.168.1.1") }
            confirmVerified(serverIpRepository, versionRepository, simulatorRepository)
            collectionJob.cancelAndJoin()
        }

    @Test
    fun `接続確認で例外が発生しても未接続として監視を継続する`() =
        runTest {
            every { serverIpRepository.serverIp() } returns MutableStateFlow("192.168.1.1")
            every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(null)
            coEvery { versionRepository.fetchVersion("192.168.1.1") } returnsMany
                listOf(Result.failure(RuntimeException("error")), Result.success("1.0.0"))
            val viewModel = createViewModel()
            val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }
            dispatcher.scheduler.runCurrent()
            assertEquals(ServerConnectionStatus.DISCONNECTED, viewModel.uiState.first().connectionStatus)

            dispatcher.scheduler.advanceTimeBy(1_000L)
            dispatcher.scheduler.runCurrent()

            assertEquals(ServerConnectionStatus.CONNECTED, viewModel.uiState.first().connectionStatus)
            verify(exactly = 1) { serverIpRepository.serverIp() }
            verify(exactly = 1) { simulatorRepository.selectedSimulator() }
            coVerify(exactly = 2) { versionRepository.fetchVersion("192.168.1.1") }
            confirmVerified(serverIpRepository, versionRepository, simulatorRepository)
            collectionJob.cancelAndJoin()
        }

    @Test
    fun `LMU選択時はrequiresKoDriverServerがtrueになる`() =
        runTest {
            every { serverIpRepository.serverIp() } returns MutableStateFlow("192.168.1.1")
            every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(Simulator.LmuWindows)
            coEvery { versionRepository.fetchVersion("192.168.1.1") } returns Result.success("1.0.0")
            val viewModel = createViewModel()
            val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }

            dispatcher.scheduler.runCurrent()

            assertTrue(viewModel.uiState.first().requiresKoDriverServer)
            verify(exactly = 1) { serverIpRepository.serverIp() }
            verify(exactly = 1) { simulatorRepository.selectedSimulator() }
            coVerify(exactly = 1) { versionRepository.fetchVersion("192.168.1.1") }
            confirmVerified(serverIpRepository, versionRepository, simulatorRepository)
            collectionJob.cancelAndJoin()
        }

    @Test
    fun `シミュレータ未選択時はrequiresKoDriverServerがfalseになる`() =
        runTest {
            every { serverIpRepository.serverIp() } returns MutableStateFlow(null)
            every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(null)
            val viewModel = createViewModel()
            val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }

            dispatcher.scheduler.runCurrent()

            assertFalse(viewModel.uiState.first().requiresKoDriverServer)
            verify(exactly = 1) { serverIpRepository.serverIp() }
            verify(exactly = 1) { simulatorRepository.selectedSimulator() }
            confirmVerified(serverIpRepository, versionRepository, simulatorRepository)
            collectionJob.cancelAndJoin()
        }

    @Test
    fun `シミュレータ未選択時はnullを返す`() =
        runTest {
            every { serverIpRepository.serverIp() } returns MutableStateFlow(null)
            every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(null)
            val viewModel = createViewModel()
            val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }

            dispatcher.scheduler.runCurrent()

            assertNull(viewModel.uiState.first().selectedSimulator)
            verify(exactly = 1) { serverIpRepository.serverIp() }
            verify(exactly = 1) { simulatorRepository.selectedSimulator() }
            confirmVerified(serverIpRepository, versionRepository, simulatorRepository)
            collectionJob.cancelAndJoin()
        }

    @Test
    fun `接続成功時にサーバーバージョンがuiStateに反映される`() =
        runTest {
            every { serverIpRepository.serverIp() } returns MutableStateFlow("192.168.1.1")
            every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(null)
            coEvery { versionRepository.fetchVersion("192.168.1.1") } returns Result.success("1.0.0")
            val viewModel = createViewModel()
            val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }

            dispatcher.scheduler.runCurrent()

            assertEquals("1.0.0", viewModel.uiState.first().serverVersion)
            verify(exactly = 1) { serverIpRepository.serverIp() }
            verify(exactly = 1) { simulatorRepository.selectedSimulator() }
            coVerify(exactly = 1) { versionRepository.fetchVersion("192.168.1.1") }
            confirmVerified(serverIpRepository, versionRepository, simulatorRepository)
            collectionJob.cancelAndJoin()
        }

    @Test
    fun `未接続時はサーバーバージョンがnullになる`() =
        runTest {
            every { serverIpRepository.serverIp() } returns MutableStateFlow("192.168.1.1")
            every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(null)
            coEvery { versionRepository.fetchVersion("192.168.1.1") } returns Result.failure(RuntimeException("error"))
            val viewModel = createViewModel()
            val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }

            dispatcher.scheduler.runCurrent()

            assertNull(viewModel.uiState.first().serverVersion)
            verify(exactly = 1) { serverIpRepository.serverIp() }
            verify(exactly = 1) { simulatorRepository.selectedSimulator() }
            coVerify(exactly = 1) { versionRepository.fetchVersion("192.168.1.1") }
            confirmVerified(serverIpRepository, versionRepository, simulatorRepository)
            collectionJob.cancelAndJoin()
        }

    @Test
    fun `バージョン不一致時にボトムシートを表示する`() =
        runTest {
            every { serverIpRepository.serverIp() } returns MutableStateFlow("192.168.1.1")
            every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(null)
            coEvery { versionRepository.fetchVersion("192.168.1.1") } returns Result.success("2.0.0")
            val viewModel = createViewModel(appVersion = "1.0.0")
            val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }

            dispatcher.scheduler.runCurrent()

            assertTrue(viewModel.uiState.first().showVersionMismatchBottomSheet)
            verify(exactly = 1) { serverIpRepository.serverIp() }
            verify(exactly = 1) { simulatorRepository.selectedSimulator() }
            coVerify(exactly = 1) { versionRepository.fetchVersion("192.168.1.1") }
            confirmVerified(serverIpRepository, versionRepository, simulatorRepository)
            collectionJob.cancelAndJoin()
        }

    @Test
    fun `バージョン一致時はボトムシートを表示しない`() =
        runTest {
            every { serverIpRepository.serverIp() } returns MutableStateFlow("192.168.1.1")
            every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(null)
            coEvery { versionRepository.fetchVersion("192.168.1.1") } returns Result.success("1.0.0")
            val viewModel = createViewModel(appVersion = "1.0.0")
            val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }

            dispatcher.scheduler.runCurrent()

            assertFalse(viewModel.uiState.first().showVersionMismatchBottomSheet)
            verify(exactly = 1) { serverIpRepository.serverIp() }
            verify(exactly = 1) { simulatorRepository.selectedSimulator() }
            coVerify(exactly = 1) { versionRepository.fetchVersion("192.168.1.1") }
            confirmVerified(serverIpRepository, versionRepository, simulatorRepository)
            collectionJob.cancelAndJoin()
        }

    @Test
    fun `ボトムシートをdismissするとshowVersionMismatchBottomSheetがfalseになる`() =
        runTest {
            every { serverIpRepository.serverIp() } returns MutableStateFlow("192.168.1.1")
            every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(null)
            coEvery { versionRepository.fetchVersion("192.168.1.1") } returns Result.success("2.0.0")
            val viewModel = createViewModel(appVersion = "1.0.0")
            val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }
            dispatcher.scheduler.runCurrent()
            assertTrue(viewModel.uiState.first().showVersionMismatchBottomSheet)

            viewModel.dismissVersionMismatchBottomSheet()
            dispatcher.scheduler.runCurrent()

            assertFalse(viewModel.uiState.first().showVersionMismatchBottomSheet)
            verify(exactly = 1) { serverIpRepository.serverIp() }
            verify(exactly = 1) { simulatorRepository.selectedSimulator() }
            coVerify(exactly = 1) { versionRepository.fetchVersion("192.168.1.1") }
            confirmVerified(serverIpRepository, versionRepository, simulatorRepository)
            collectionJob.cancelAndJoin()
        }

    @Test
    fun `バージョン不一致のボトムシートはdismiss後に再ポーリングで再表示されない`() =
        runTest {
            every { serverIpRepository.serverIp() } returns MutableStateFlow("192.168.1.1")
            every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(null)
            coEvery { versionRepository.fetchVersion("192.168.1.1") } returns Result.success("2.0.0")
            val viewModel = createViewModel(appVersion = "1.0.0")
            val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }
            dispatcher.scheduler.runCurrent()
            viewModel.dismissVersionMismatchBottomSheet()
            dispatcher.scheduler.runCurrent()

            dispatcher.scheduler.advanceTimeBy(1_000L)
            dispatcher.scheduler.runCurrent()

            assertFalse(viewModel.uiState.first().showVersionMismatchBottomSheet)
            verify(exactly = 1) { serverIpRepository.serverIp() }
            verify(exactly = 1) { simulatorRepository.selectedSimulator() }
            coVerify(exactly = 2) { versionRepository.fetchVersion("192.168.1.1") }
            confirmVerified(serverIpRepository, versionRepository, simulatorRepository)
            collectionJob.cancelAndJoin()
        }

    @Test
    fun `バージョン不一致のボトムシートはdismiss後に再購読しても再表示されない`() =
        runTest {
            every { serverIpRepository.serverIp() } returns MutableStateFlow("192.168.1.1")
            every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(null)
            coEvery { versionRepository.fetchVersion("192.168.1.1") } returns Result.success("2.0.0")
            val viewModel = createViewModel(appVersion = "1.0.0")
            var collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }
            dispatcher.scheduler.runCurrent()
            viewModel.dismissVersionMismatchBottomSheet()
            dispatcher.scheduler.runCurrent()
            collectionJob.cancelAndJoin()

            collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }
            dispatcher.scheduler.runCurrent()

            assertFalse(viewModel.uiState.first().showVersionMismatchBottomSheet)
            verify(exactly = 1) { serverIpRepository.serverIp() }
            verify(exactly = 1) { simulatorRepository.selectedSimulator() }
            coVerify(exactly = 2) { versionRepository.fetchVersion("192.168.1.1") }
            confirmVerified(serverIpRepository, versionRepository, simulatorRepository)
            collectionJob.cancelAndJoin()
        }
}
