package kurou.kodriver.feature.lmuwindowsconnection

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
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.repository.LmuWindowsRepository
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.usecase.CheckLmuWindowsConnectionUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsConnectionViewModelTest {

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
    fun `LMU選択時に接続確認結果を反映する`() = runTest {
        val connectionRepository = FakeConnectionRepository(isConnected = true)
        val simulatorRepository = FakeSimulatorPreferencesRepository(initial = Simulator.LmuWindows)
        val viewModel = createViewModel(connectionRepository, simulatorRepository)
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }

        dispatcher.scheduler.runCurrent()

        assertEquals(LmuWindowsConnectionStatus.CONNECTED, viewModel.uiState.first().connectionStatus)
        collectionJob.cancelAndJoin()
    }

    @Test
    fun `LMU非選択時は未接続・未確認状態を返す`() = runTest {
        val connectionRepository = FakeConnectionRepository(isConnected = true)
        val simulatorRepository = FakeSimulatorPreferencesRepository(initial = null)
        val viewModel = createViewModel(connectionRepository, simulatorRepository)
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }

        dispatcher.scheduler.runCurrent()

        assertEquals(LmuWindowsConnectionStatus.UNCHECKED, viewModel.uiState.first().connectionStatus)
        collectionJob.cancelAndJoin()
    }

    @Test
    fun `LMU選択前は未確認状態とする`() = runTest {
        val connectionRepository = FakeConnectionRepository(isConnected = false)
        val simulatorRepository = FakeSimulatorPreferencesRepository(initial = null)
        val viewModel = createViewModel(connectionRepository, simulatorRepository)

        assertEquals(LmuWindowsConnectionStatus.UNCHECKED, viewModel.uiState.first().connectionStatus)
    }

    @Test
    fun `LMU選択に切り替えると接続確認を開始する`() = runTest {
        val connectionRepository = FakeConnectionRepository(isConnected = true)
        val simulatorRepository = FakeSimulatorPreferencesRepository(initial = null)
        val viewModel = createViewModel(connectionRepository, simulatorRepository)
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }
        dispatcher.scheduler.runCurrent()
        assertEquals(LmuWindowsConnectionStatus.UNCHECKED, viewModel.uiState.first().connectionStatus)

        simulatorRepository.saveSelectedSimulator(Simulator.LmuWindows)
        dispatcher.scheduler.runCurrent()

        assertEquals(LmuWindowsConnectionStatus.CONNECTED, viewModel.uiState.first().connectionStatus)
        collectionJob.cancelAndJoin()
    }

    @Test
    fun `LMUから別シミュレータへ切り替えると未接続にリセットされる`() = runTest {
        val connectionRepository = FakeConnectionRepository(isConnected = true)
        val simulatorRepository = FakeSimulatorPreferencesRepository(initial = Simulator.LmuWindows)
        val viewModel = createViewModel(connectionRepository, simulatorRepository)
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }
        dispatcher.scheduler.runCurrent()
        assertEquals(LmuWindowsConnectionStatus.CONNECTED, viewModel.uiState.first().connectionStatus)

        simulatorRepository.flow.value = null
        dispatcher.scheduler.runCurrent()

        assertEquals(LmuWindowsConnectionStatus.UNCHECKED, viewModel.uiState.first().connectionStatus)
        collectionJob.cancelAndJoin()
    }

    @Test
    fun `LMU選択時に一定間隔で接続状態を更新する`() = runTest {
        val connectionRepository = FakeConnectionRepository(isConnected = false)
        val simulatorRepository = FakeSimulatorPreferencesRepository(initial = Simulator.LmuWindows)
        val viewModel = createViewModel(connectionRepository, simulatorRepository)
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }
        dispatcher.scheduler.runCurrent()
        assertEquals(LmuWindowsConnectionStatus.DISCONNECTED, viewModel.uiState.first().connectionStatus)

        connectionRepository.isConnected = true
        dispatcher.scheduler.advanceTimeBy(1_000L)
        dispatcher.scheduler.runCurrent()

        assertEquals(LmuWindowsConnectionStatus.CONNECTED, viewModel.uiState.first().connectionStatus)
        collectionJob.cancelAndJoin()
    }

    @Test
    fun `接続確認で例外が発生しても未接続として監視を継続する`() = runTest {
        val connectionRepository = FakeConnectionRepository(isConnected = false, failureCount = 1)
        val simulatorRepository = FakeSimulatorPreferencesRepository(initial = Simulator.LmuWindows)
        val viewModel = createViewModel(connectionRepository, simulatorRepository)
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }
        dispatcher.scheduler.runCurrent()
        assertEquals(LmuWindowsConnectionStatus.DISCONNECTED, viewModel.uiState.first().connectionStatus)

        connectionRepository.isConnected = true
        dispatcher.scheduler.advanceTimeBy(1_000L)
        dispatcher.scheduler.runCurrent()

        assertEquals(LmuWindowsConnectionStatus.CONNECTED, viewModel.uiState.first().connectionStatus)
        collectionJob.cancelAndJoin()
    }

    private fun createViewModel(
        connectionRepository: LmuWindowsRepository,
        simulatorRepository: SimulatorPreferencesRepository,
    ) = LmuWindowsConnectionViewModel(
        checkLmuWindowsConnection = CheckLmuWindowsConnectionUseCase(connectionRepository),
        observeSelectedSimulator = ObserveSelectedSimulatorUseCase(simulatorRepository),
    )
}

private class FakeConnectionRepository(
    var isConnected: Boolean,
    var failureCount: Int = 0,
) : LmuWindowsRepository {
    override fun telemetryStream(): Flow<LmuWindowsTelemetryData> = emptyFlow()

    override suspend fun isConnected(): Boolean {
        if (failureCount > 0) {
            failureCount--
            error("connection check failed")
        }
        return isConnected
    }

    override suspend fun disconnect() = Unit
}

private class FakeSimulatorPreferencesRepository(
    initial: Simulator? = null,
) : SimulatorPreferencesRepository {
    val flow = MutableStateFlow(initial)

    override fun selectedSimulator(): Flow<Simulator?> = flow
    override suspend fun saveSelectedSimulator(simulator: Simulator) { flow.value = simulator }
}
