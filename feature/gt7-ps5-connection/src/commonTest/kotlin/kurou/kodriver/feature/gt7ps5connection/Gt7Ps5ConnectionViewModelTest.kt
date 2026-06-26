package kurou.kodriver.feature.gt7ps5connection

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.repository.Gt7Ps5Repository
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.usecase.CheckGt7Ps5ConnectionUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class Gt7Ps5ConnectionViewModelTest {

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
    fun `GT7選択時に接続確認結果を反映する`() = runTest {
        val connectionRepository = FakeGt7Ps5Repository(isConnected = true)
        val simulatorRepository = FakeSimulatorPreferencesRepository(initial = Simulator.Gt7Ps5)
        val viewModel = createViewModel(connectionRepository, simulatorRepository)
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }

        dispatcher.scheduler.runCurrent()

        assertEquals(Gt7Ps5ConnectionStatus.CONNECTED, viewModel.uiState.first().connectionStatus)
        collectionJob.cancelAndJoin()
    }

    @Test
    fun `GT7非選択時は未確認状態を返す`() = runTest {
        val connectionRepository = FakeGt7Ps5Repository(isConnected = true)
        val simulatorRepository = FakeSimulatorPreferencesRepository(initial = Simulator.LmuWindows)
        val viewModel = createViewModel(connectionRepository, simulatorRepository)
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }

        dispatcher.scheduler.runCurrent()

        assertEquals(Gt7Ps5ConnectionStatus.UNCHECKED, viewModel.uiState.first().connectionStatus)
        collectionJob.cancelAndJoin()
    }

    @Test
    fun `GT7選択前は未確認状態とする`() = runTest {
        val connectionRepository = FakeGt7Ps5Repository(isConnected = false)
        val simulatorRepository = FakeSimulatorPreferencesRepository(initial = null)
        val viewModel = createViewModel(connectionRepository, simulatorRepository)

        assertEquals(Gt7Ps5ConnectionStatus.UNCHECKED, viewModel.uiState.first().connectionStatus)
    }

    @Test
    fun `GT7選択に切り替えると接続確認を開始する`() = runTest {
        val connectionRepository = FakeGt7Ps5Repository(isConnected = true)
        val simulatorRepository = FakeSimulatorPreferencesRepository(initial = Simulator.LmuWindows)
        val viewModel = createViewModel(connectionRepository, simulatorRepository)
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }
        dispatcher.scheduler.runCurrent()
        assertEquals(Gt7Ps5ConnectionStatus.UNCHECKED, viewModel.uiState.first().connectionStatus)

        simulatorRepository.saveSelectedSimulator(Simulator.Gt7Ps5)
        dispatcher.scheduler.runCurrent()

        assertEquals(Gt7Ps5ConnectionStatus.CONNECTED, viewModel.uiState.first().connectionStatus)
        collectionJob.cancelAndJoin()
    }

    @Test
    fun `GT7から別シミュレータへ切り替えると未確認にリセットされる`() = runTest {
        val connectionRepository = FakeGt7Ps5Repository(isConnected = true)
        val simulatorRepository = FakeSimulatorPreferencesRepository(initial = Simulator.Gt7Ps5)
        val viewModel = createViewModel(connectionRepository, simulatorRepository)
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }
        dispatcher.scheduler.runCurrent()
        assertEquals(Gt7Ps5ConnectionStatus.CONNECTED, viewModel.uiState.first().connectionStatus)

        simulatorRepository.flow.value = Simulator.LmuWindows
        dispatcher.scheduler.runCurrent()

        assertEquals(Gt7Ps5ConnectionStatus.UNCHECKED, viewModel.uiState.first().connectionStatus)
        collectionJob.cancelAndJoin()
    }

    @Test
    fun `GT7選択時に一定間隔で接続状態を更新する`() = runTest {
        val connectionRepository = FakeGt7Ps5Repository(isConnected = false)
        val simulatorRepository = FakeSimulatorPreferencesRepository(initial = Simulator.Gt7Ps5)
        val viewModel = createViewModel(connectionRepository, simulatorRepository)
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }
        dispatcher.scheduler.runCurrent()
        assertEquals(Gt7Ps5ConnectionStatus.DISCONNECTED, viewModel.uiState.first().connectionStatus)

        connectionRepository.isConnected = true
        dispatcher.scheduler.advanceTimeBy(1_000L)
        dispatcher.scheduler.runCurrent()

        assertEquals(Gt7Ps5ConnectionStatus.CONNECTED, viewModel.uiState.first().connectionStatus)
        collectionJob.cancelAndJoin()
    }

    @Test
    fun `接続確認で例外が発生しても未接続として監視を継続する`() = runTest {
        val connectionRepository = FakeGt7Ps5Repository(isConnected = false, failureCount = 1)
        val simulatorRepository = FakeSimulatorPreferencesRepository(initial = Simulator.Gt7Ps5)
        val viewModel = createViewModel(connectionRepository, simulatorRepository)
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }
        dispatcher.scheduler.runCurrent()
        assertEquals(Gt7Ps5ConnectionStatus.DISCONNECTED, viewModel.uiState.first().connectionStatus)

        connectionRepository.isConnected = true
        dispatcher.scheduler.advanceTimeBy(1_000L)
        dispatcher.scheduler.runCurrent()

        assertEquals(Gt7Ps5ConnectionStatus.CONNECTED, viewModel.uiState.first().connectionStatus)
        collectionJob.cancelAndJoin()
    }

    private fun createViewModel(
        connectionRepository: Gt7Ps5Repository,
        simulatorRepository: SimulatorPreferencesRepository,
    ) = Gt7Ps5ConnectionViewModel(
        checkGt7Ps5Connection = CheckGt7Ps5ConnectionUseCase(connectionRepository),
        observeSelectedSimulator = ObserveSelectedSimulatorUseCase(simulatorRepository),
    )
}

private class FakeGt7Ps5Repository(
    var isConnected: Boolean,
    var failureCount: Int = 0,
) : Gt7Ps5Repository {
    override fun telemetryStream(): Flow<Gt7Ps5TelemetryData> = emptyFlow()

    override suspend fun isConnected(): Boolean {
        if (failureCount > 0) {
            failureCount--
            error("connection check failed")
        }
        return isConnected
    }
}

private class FakeSimulatorPreferencesRepository(
    initial: Simulator? = null,
) : SimulatorPreferencesRepository {
    val flow = MutableStateFlow(initial)

    override fun selectedSimulator(): Flow<Simulator?> = flow
    override suspend fun saveSelectedSimulator(simulator: Simulator) { flow.value = simulator }
}
