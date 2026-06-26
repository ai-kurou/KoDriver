package kurou.kodriver.feature.serverconnection

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.repository.ServerIpRepository
import kurou.kodriver.domain.repository.ServerVersionRepository
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.usecase.FetchServerVersionUseCase
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

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `IP設定済みで接続成功時に接続済み状態を返す`() = runTest {
        val serverIpRepo = FakeServerIpRepository(initial = "192.168.1.1")
        val viewModel = createViewModel(serverIpRepo, FakeServerVersionRepository(Result.success("1.0.0")))
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }

        dispatcher.scheduler.runCurrent()

        val state = viewModel.uiState.first()
        assertEquals(ServerConnectionStatus.CONNECTED, state.connectionStatus)
        collectionJob.cancelAndJoin()
    }

    @Test
    fun `IP未設定時はNOT_CONFIGUREDを返す`() = runTest {
        val serverIpRepo = FakeServerIpRepository(initial = null)
        val viewModel = createViewModel(serverIpRepo)
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }

        dispatcher.scheduler.runCurrent()

        val state = viewModel.uiState.first()
        assertEquals(ServerConnectionStatus.NOT_CONFIGURED, state.connectionStatus)
        collectionJob.cancelAndJoin()
    }

    @Test
    fun `選択シミュレータがuiStateに反映される`() = runTest {
        val serverIpRepo = FakeServerIpRepository(initial = "192.168.1.1")
        val simulatorRepo = FakeSimulatorPreferencesRepository(initial = Simulator.LmuWindows)
        val viewModel = createViewModel(serverIpRepo, simulatorRepository = simulatorRepo)
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }

        dispatcher.scheduler.runCurrent()

        assertEquals(Simulator.LmuWindows, viewModel.uiState.first().selectedSimulator)
        collectionJob.cancelAndJoin()
    }

    @Test
    fun `IP設定後に接続確認を開始する`() = runTest {
        val serverIpRepo = FakeServerIpRepository(initial = null)
        val viewModel = createViewModel(serverIpRepo, FakeServerVersionRepository(Result.success("1.0.0")))
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }
        dispatcher.scheduler.runCurrent()
        assertFalse(viewModel.uiState.first().isConnectionChecked)

        serverIpRepo.saveServerIp("192.168.1.100")
        dispatcher.scheduler.runCurrent()

        val state = viewModel.uiState.first()
        assertEquals(ServerConnectionStatus.CONNECTED, state.connectionStatus)
        collectionJob.cancelAndJoin()
    }

    @Test
    fun `一定間隔で接続状態を更新する`() = runTest {
        val serverIpRepo = FakeServerIpRepository(initial = "192.168.1.1")
        val versionRepo = FakeServerVersionRepository(Result.failure(RuntimeException("down")))
        val viewModel = createViewModel(serverIpRepo, versionRepo)
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }
        dispatcher.scheduler.runCurrent()
        assertEquals(ServerConnectionStatus.DISCONNECTED, viewModel.uiState.first().connectionStatus)

        versionRepo.result = Result.success("1.0.0")
        dispatcher.scheduler.advanceTimeBy(1_000L)
        dispatcher.scheduler.runCurrent()

        assertEquals(ServerConnectionStatus.CONNECTED, viewModel.uiState.first().connectionStatus)
        collectionJob.cancelAndJoin()
    }

    @Test
    fun `接続確認で例外が発生しても未接続として監視を継続する`() = runTest {
        val serverIpRepo = FakeServerIpRepository(initial = "192.168.1.1")
        val versionRepo = FakeServerVersionRepository(Result.failure(RuntimeException("error")), failureCount = 1)
        val viewModel = createViewModel(serverIpRepo, versionRepo)
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }
        dispatcher.scheduler.runCurrent()
        assertEquals(ServerConnectionStatus.DISCONNECTED, viewModel.uiState.first().connectionStatus)

        versionRepo.result = Result.success("1.0.0")
        dispatcher.scheduler.advanceTimeBy(1_000L)
        dispatcher.scheduler.runCurrent()

        assertEquals(ServerConnectionStatus.CONNECTED, viewModel.uiState.first().connectionStatus)
        collectionJob.cancelAndJoin()
    }

    @Test
    fun `LMU選択時はrequiresKoDriverServerがtrueになる`() = runTest {
        val serverIpRepo = FakeServerIpRepository(initial = "192.168.1.1")
        val simulatorRepo = FakeSimulatorPreferencesRepository(initial = Simulator.LmuWindows)
        val viewModel = createViewModel(serverIpRepo, simulatorRepository = simulatorRepo)
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }

        dispatcher.scheduler.runCurrent()

        assertTrue(viewModel.uiState.first().requiresKoDriverServer)
        collectionJob.cancelAndJoin()
    }

    @Test
    fun `シミュレータ未選択時はrequiresKoDriverServerがfalseになる`() = runTest {
        val serverIpRepo = FakeServerIpRepository(initial = null)
        val viewModel = createViewModel(serverIpRepo)
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }

        dispatcher.scheduler.runCurrent()

        assertFalse(viewModel.uiState.first().requiresKoDriverServer)
        collectionJob.cancelAndJoin()
    }

    @Test
    fun `シミュレータ未選択時はnullを返す`() = runTest {
        val serverIpRepo = FakeServerIpRepository(initial = null)
        val viewModel = createViewModel(serverIpRepo)
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }

        dispatcher.scheduler.runCurrent()

        assertNull(viewModel.uiState.first().selectedSimulator)
        collectionJob.cancelAndJoin()
    }

    @Test
    fun `接続成功時にサーバーバージョンがuiStateに反映される`() = runTest {
        val serverIpRepo = FakeServerIpRepository(initial = "192.168.1.1")
        val versionRepo = FakeServerVersionRepository(Result.success("1.0.0"))
        val viewModel = createViewModel(serverIpRepo, versionRepo)
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }

        dispatcher.scheduler.runCurrent()

        assertEquals("1.0.0", viewModel.uiState.first().serverVersion)
        collectionJob.cancelAndJoin()
    }

    @Test
    fun `未接続時はサーバーバージョンがnullになる`() = runTest {
        val serverIpRepo = FakeServerIpRepository(initial = "192.168.1.1")
        val versionRepo = FakeServerVersionRepository(Result.failure(RuntimeException("error")))
        val viewModel = createViewModel(serverIpRepo, versionRepo)
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }

        dispatcher.scheduler.runCurrent()

        assertNull(viewModel.uiState.first().serverVersion)
        collectionJob.cancelAndJoin()
    }

    @Test
    fun `バージョン不一致時にボトムシートを表示する`() = runTest {
        val serverIpRepo = FakeServerIpRepository(initial = "192.168.1.1")
        val versionRepo = FakeServerVersionRepository(Result.success("2.0.0"))
        val viewModel = createViewModel(serverIpRepo, versionRepo, appVersion = "1.0.0")
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }

        dispatcher.scheduler.runCurrent()

        assertTrue(viewModel.uiState.first().showVersionMismatchBottomSheet)
        collectionJob.cancelAndJoin()
    }

    @Test
    fun `バージョン一致時はボトムシートを表示しない`() = runTest {
        val serverIpRepo = FakeServerIpRepository(initial = "192.168.1.1")
        val versionRepo = FakeServerVersionRepository(Result.success("1.0.0"))
        val viewModel = createViewModel(serverIpRepo, versionRepo, appVersion = "1.0.0")
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }

        dispatcher.scheduler.runCurrent()

        assertFalse(viewModel.uiState.first().showVersionMismatchBottomSheet)
        collectionJob.cancelAndJoin()
    }

    @Test
    fun `ボトムシートをdismissするとshowVersionMismatchBottomSheetがfalseになる`() = runTest {
        val serverIpRepo = FakeServerIpRepository(initial = "192.168.1.1")
        val versionRepo = FakeServerVersionRepository(Result.success("2.0.0"))
        val viewModel = createViewModel(serverIpRepo, versionRepo, appVersion = "1.0.0")
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }
        dispatcher.scheduler.runCurrent()
        assertTrue(viewModel.uiState.first().showVersionMismatchBottomSheet)

        viewModel.dismissVersionMismatchBottomSheet()
        dispatcher.scheduler.runCurrent()

        assertFalse(viewModel.uiState.first().showVersionMismatchBottomSheet)
        collectionJob.cancelAndJoin()
    }

    @Test
    fun `バージョン不一致のボトムシートはdismiss後に再ポーリングで再表示されない`() = runTest {
        val serverIpRepo = FakeServerIpRepository(initial = "192.168.1.1")
        val versionRepo = FakeServerVersionRepository(Result.success("2.0.0"))
        val viewModel = createViewModel(serverIpRepo, versionRepo, appVersion = "1.0.0")
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }
        dispatcher.scheduler.runCurrent()
        viewModel.dismissVersionMismatchBottomSheet()
        dispatcher.scheduler.runCurrent()

        dispatcher.scheduler.advanceTimeBy(1_000L)
        dispatcher.scheduler.runCurrent()

        assertFalse(viewModel.uiState.first().showVersionMismatchBottomSheet)
        collectionJob.cancelAndJoin()
    }

    private fun createViewModel(
        serverIpRepository: ServerIpRepository = FakeServerIpRepository(),
        versionRepository: ServerVersionRepository = FakeServerVersionRepository(),
        simulatorRepository: SimulatorPreferencesRepository = FakeSimulatorPreferencesRepository(),
        appVersion: String = "1.0.0",
    ) = ServerConnectionViewModel(
        fetchServerVersion = FetchServerVersionUseCase(versionRepository),
        observeServerIp = ObserveServerIpUseCase(serverIpRepository),
        observeSelectedSimulator = ObserveSelectedSimulatorUseCase(simulatorRepository),
        appVersion = appVersion,
    )
}

private class FakeServerIpRepository(
    initial: String? = null,
) : ServerIpRepository {
    private val flow = MutableStateFlow(initial)

    override fun serverIp(): Flow<String?> = flow
    override suspend fun saveServerIp(ip: String) { flow.update { ip } }
}

private class FakeSimulatorPreferencesRepository(
    initial: Simulator? = null,
) : SimulatorPreferencesRepository {
    val flow = MutableStateFlow(initial)

    override fun selectedSimulator(): Flow<Simulator?> = flow
    override suspend fun saveSelectedSimulator(simulator: Simulator) { flow.update { simulator } }
}

private class FakeServerVersionRepository(
    var result: Result<String> = Result.success("0.0.0"),
    var failureCount: Int = 0,
) : ServerVersionRepository {
    override suspend fun fetchVersion(ip: String): Result<String> {
        if (failureCount > 0) {
            failureCount--
            return Result.failure(RuntimeException("temporary failure"))
        }
        return result
    }
}
