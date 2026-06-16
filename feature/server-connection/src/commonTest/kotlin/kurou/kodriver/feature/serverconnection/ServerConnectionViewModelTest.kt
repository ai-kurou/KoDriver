package kurou.kodriver.feature.serverconnection

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.repository.ServerConnectionRepository
import kurou.kodriver.domain.repository.ServerIpRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.usecase.CheckServerConnectionUseCase
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
        val connectionRepo = FakeServerConnectionRepository(connected = true)
        val viewModel = createViewModel(serverIpRepo, connectionRepo)
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }

        dispatcher.scheduler.runCurrent()

        val state = viewModel.uiState.first()
        assertTrue(state.isConnected)
        assertTrue(state.isConnectionChecked)
        collectionJob.cancelAndJoin()
    }

    @Test
    fun `IP未設定時は未確認状態を返す`() = runTest {
        val serverIpRepo = FakeServerIpRepository(initial = null)
        val connectionRepo = FakeServerConnectionRepository(connected = true)
        val viewModel = createViewModel(serverIpRepo, connectionRepo)
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }

        dispatcher.scheduler.runCurrent()

        val state = viewModel.uiState.first()
        assertFalse(state.isConnected)
        assertFalse(state.isConnectionChecked)
        collectionJob.cancelAndJoin()
    }

    @Test
    fun `選択シミュレータがuiStateに反映される`() = runTest {
        val serverIpRepo = FakeServerIpRepository(initial = "192.168.1.1")
        val connectionRepo = FakeServerConnectionRepository(connected = true)
        val simulatorRepo = FakeSimulatorPreferencesRepository(initial = "lmu")
        val viewModel = createViewModel(serverIpRepo, connectionRepo, simulatorRepo)
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }

        dispatcher.scheduler.runCurrent()

        assertEquals("lmu", viewModel.uiState.first().selectedSimulator)
        collectionJob.cancelAndJoin()
    }

    @Test
    fun `IP設定後に接続確認を開始する`() = runTest {
        val serverIpRepo = FakeServerIpRepository(initial = null)
        val connectionRepo = FakeServerConnectionRepository(connected = true)
        val viewModel = createViewModel(serverIpRepo, connectionRepo)
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }
        dispatcher.scheduler.runCurrent()
        assertFalse(viewModel.uiState.first().isConnectionChecked)

        serverIpRepo.saveServerIp("192.168.1.100")
        dispatcher.scheduler.runCurrent()

        val state = viewModel.uiState.first()
        assertTrue(state.isConnected)
        assertTrue(state.isConnectionChecked)
        collectionJob.cancelAndJoin()
    }

    @Test
    fun `一定間隔で接続状態を更新する`() = runTest {
        val serverIpRepo = FakeServerIpRepository(initial = "192.168.1.1")
        val connectionRepo = FakeServerConnectionRepository(connected = false)
        val viewModel = createViewModel(serverIpRepo, connectionRepo)
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }
        dispatcher.scheduler.runCurrent()
        assertFalse(viewModel.uiState.first().isConnected)

        connectionRepo.connected = true
        dispatcher.scheduler.advanceTimeBy(1_000L)
        dispatcher.scheduler.runCurrent()

        assertTrue(viewModel.uiState.first().isConnected)
        collectionJob.cancelAndJoin()
    }

    @Test
    fun `接続確認で例外が発生しても未接続として監視を継続する`() = runTest {
        val serverIpRepo = FakeServerIpRepository(initial = "192.168.1.1")
        val connectionRepo = FakeServerConnectionRepository(connected = false, failureCount = 1)
        val viewModel = createViewModel(serverIpRepo, connectionRepo)
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }
        dispatcher.scheduler.runCurrent()
        assertFalse(viewModel.uiState.first().isConnected)

        connectionRepo.connected = true
        dispatcher.scheduler.advanceTimeBy(1_000L)
        dispatcher.scheduler.runCurrent()

        assertTrue(viewModel.uiState.first().isConnected)
        collectionJob.cancelAndJoin()
    }

    @Test
    fun `シミュレータ未選択時はnullを返す`() = runTest {
        val serverIpRepo = FakeServerIpRepository(initial = null)
        val connectionRepo = FakeServerConnectionRepository(connected = false)
        val viewModel = createViewModel(serverIpRepo, connectionRepo)
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }

        dispatcher.scheduler.runCurrent()

        assertNull(viewModel.uiState.first().selectedSimulator)
        collectionJob.cancelAndJoin()
    }

    private fun createViewModel(
        serverIpRepository: ServerIpRepository = FakeServerIpRepository(),
        connectionRepository: ServerConnectionRepository = FakeServerConnectionRepository(),
        simulatorRepository: SimulatorPreferencesRepository = FakeSimulatorPreferencesRepository(),
    ) = ServerConnectionViewModel(
        checkServerConnection = CheckServerConnectionUseCase(connectionRepository),
        observeServerIp = ObserveServerIpUseCase(serverIpRepository),
        observeSelectedSimulator = ObserveSelectedSimulatorUseCase(simulatorRepository),
    )
}

private class FakeServerConnectionRepository(
    var connected: Boolean = true,
    var failureCount: Int = 0,
) : ServerConnectionRepository {
    override suspend fun isConnected(ip: String): Boolean {
        if (failureCount > 0) {
            failureCount--
            error("connection check failed")
        }
        return connected
    }
}

private class FakeServerIpRepository(
    initial: String? = null,
) : ServerIpRepository {
    private val flow = MutableStateFlow(initial)

    override fun serverIp(): Flow<String?> = flow
    override suspend fun saveServerIp(ip: String) { flow.update { ip } }
}

private class FakeSimulatorPreferencesRepository(
    initial: String? = null,
) : SimulatorPreferencesRepository {
    private val flow = MutableStateFlow(initial)

    override fun selectedSimulator(): Flow<String?> = flow
    override suspend fun saveSelectedSimulator(simulator: String) { flow.update { simulator } }
}
