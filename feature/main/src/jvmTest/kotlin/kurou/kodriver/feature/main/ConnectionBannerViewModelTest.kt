package kurou.kodriver.feature.main

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.repository.ConsoleAddressRepository
import kurou.kodriver.domain.repository.Gt7Ps5Repository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.usecase.CheckGt7Ps5ConnectionUseCase
import kurou.kodriver.domain.usecase.ObserveConsoleAddressUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ConnectionBannerViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `LMU選択時に接続確認結果を反映する`() = runTest {
        val lmuChecker = FakeLmuBannerConnectionChecker(isConnected = true)
        val simulatorRepository = FakeSimulatorPreferencesRepository(initial = "lmu_windows")
        val viewModel = createViewModel(lmuChecker = lmuChecker, simulatorRepository = simulatorRepository)
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }

        dispatcher.scheduler.runCurrent()

        assertEquals(ConnectionBannerVmStatus.CONNECTED, viewModel.uiState.first().connectionStatus)
        collectionJob.cancelAndJoin()
    }

    @Test
    fun `GT7選択時に接続確認結果を反映する`() = runTest {
        val gt7Repository = FakeGt7Ps5Repository(isConnected = true)
        val simulatorRepository = FakeSimulatorPreferencesRepository(initial = "gt7_ps5")
        val viewModel = createViewModel(gt7Repository = gt7Repository, simulatorRepository = simulatorRepository)
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }

        dispatcher.scheduler.runCurrent()

        assertEquals(ConnectionBannerVmStatus.CONNECTED, viewModel.uiState.first().connectionStatus)
        collectionJob.cancelAndJoin()
    }

    @Test
    fun `シミュレータ未選択時は未確認状態とする`() = runTest {
        val simulatorRepository = FakeSimulatorPreferencesRepository(initial = null)
        val viewModel = createViewModel(simulatorRepository = simulatorRepository)

        assertEquals(ConnectionBannerVmStatus.UNCHECKED, viewModel.uiState.first().connectionStatus)
    }

    @Test
    fun `LMU選択に切り替えると接続確認を開始する`() = runTest {
        val lmuChecker = FakeLmuBannerConnectionChecker(isConnected = true)
        val simulatorRepository = FakeSimulatorPreferencesRepository(initial = "gt7_ps5")
        val viewModel = createViewModel(lmuChecker = lmuChecker, simulatorRepository = simulatorRepository)
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }
        dispatcher.scheduler.runCurrent()

        simulatorRepository.saveSelectedSimulator("lmu_windows")
        dispatcher.scheduler.runCurrent()

        assertEquals(ConnectionBannerVmStatus.CONNECTED, viewModel.uiState.first().connectionStatus)
        collectionJob.cancelAndJoin()
    }

    @Test
    fun `GT7選択に切り替えると接続確認を開始する`() = runTest {
        val gt7Repository = FakeGt7Ps5Repository(isConnected = true)
        val simulatorRepository = FakeSimulatorPreferencesRepository(initial = "lmu_windows")
        val viewModel = createViewModel(gt7Repository = gt7Repository, simulatorRepository = simulatorRepository)
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }
        dispatcher.scheduler.runCurrent()

        simulatorRepository.saveSelectedSimulator("gt7_ps5")
        dispatcher.scheduler.runCurrent()

        assertEquals(ConnectionBannerVmStatus.CONNECTED, viewModel.uiState.first().connectionStatus)
        collectionJob.cancelAndJoin()
    }

    @Test
    fun `LMUから別シミュレータへ切り替えると未確認にリセットされる`() = runTest {
        val lmuChecker = FakeLmuBannerConnectionChecker(isConnected = true)
        val simulatorRepository = FakeSimulatorPreferencesRepository(initial = "lmu_windows")
        val viewModel = createViewModel(lmuChecker = lmuChecker, simulatorRepository = simulatorRepository)
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }
        dispatcher.scheduler.runCurrent()
        assertEquals(ConnectionBannerVmStatus.CONNECTED, viewModel.uiState.first().connectionStatus)

        simulatorRepository.saveSelectedSimulator("other")
        dispatcher.scheduler.runCurrent()

        assertEquals(ConnectionBannerVmStatus.UNCHECKED, viewModel.uiState.first().connectionStatus)
        collectionJob.cancelAndJoin()
    }

    @Test
    fun `LMU接続確認で例外が発生しても未接続として監視を継続する`() = runTest {
        val lmuChecker = FakeLmuBannerConnectionChecker(isConnected = false, throwCount = 1)
        val simulatorRepository = FakeSimulatorPreferencesRepository(initial = "lmu_windows")
        val viewModel = createViewModel(lmuChecker = lmuChecker, simulatorRepository = simulatorRepository)
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }
        dispatcher.scheduler.runCurrent()
        assertEquals(ConnectionBannerVmStatus.DISCONNECTED, viewModel.uiState.first().connectionStatus)

        lmuChecker.isConnected = true
        dispatcher.scheduler.advanceTimeBy(1_000L)
        dispatcher.scheduler.runCurrent()

        assertEquals(ConnectionBannerVmStatus.CONNECTED, viewModel.uiState.first().connectionStatus)
        collectionJob.cancelAndJoin()
    }

    @Test
    fun `GT7接続確認で例外が発生しても未接続として監視を継続する`() = runTest {
        val gt7Repository = FakeGt7Ps5Repository(isConnected = false, failureCount = 1)
        val simulatorRepository = FakeSimulatorPreferencesRepository(initial = "gt7_ps5")
        val viewModel = createViewModel(gt7Repository = gt7Repository, simulatorRepository = simulatorRepository)
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }
        dispatcher.scheduler.runCurrent()
        assertEquals(ConnectionBannerVmStatus.DISCONNECTED, viewModel.uiState.first().connectionStatus)

        gt7Repository.isConnected = true
        dispatcher.scheduler.advanceTimeBy(1_000L)
        dispatcher.scheduler.runCurrent()

        assertEquals(ConnectionBannerVmStatus.CONNECTED, viewModel.uiState.first().connectionStatus)
        collectionJob.cancelAndJoin()
    }

    @Test
    fun `GT7選択時にコンソールIP未設定であればIP_NOT_CONFIGUREDを返す`() = runTest {
        val simulatorRepository = FakeSimulatorPreferencesRepository(initial = "gt7_ps5")
        val viewModel = createViewModel(
            simulatorRepository = simulatorRepository,
            consoleAddressRepository = FakeConsoleAddressRepository(initial = null),
        )
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }
        dispatcher.scheduler.runCurrent()

        assertEquals(ConnectionBannerVmStatus.IP_NOT_CONFIGURED, viewModel.uiState.first().connectionStatus)
        collectionJob.cancelAndJoin()
    }

    @Test
    fun `GT7選択時にコンソールIPが設定されると接続確認を開始する`() = runTest {
        val gt7Repository = FakeGt7Ps5Repository(isConnected = true)
        val simulatorRepository = FakeSimulatorPreferencesRepository(initial = "gt7_ps5")
        val consoleAddressRepository = FakeConsoleAddressRepository(initial = null)
        val viewModel = createViewModel(
            gt7Repository = gt7Repository,
            simulatorRepository = simulatorRepository,
            consoleAddressRepository = consoleAddressRepository,
        )
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }
        dispatcher.scheduler.runCurrent()
        assertEquals(ConnectionBannerVmStatus.IP_NOT_CONFIGURED, viewModel.uiState.first().connectionStatus)

        consoleAddressRepository.save("192.168.1.100")
        dispatcher.scheduler.runCurrent()

        assertEquals(ConnectionBannerVmStatus.CONNECTED, viewModel.uiState.first().connectionStatus)
        collectionJob.cancelAndJoin()
    }

    private fun createViewModel(
        lmuChecker: LmuBannerConnectionChecker = FakeLmuBannerConnectionChecker(isConnected = false),
        gt7Repository: Gt7Ps5Repository = FakeGt7Ps5Repository(isConnected = false),
        simulatorRepository: SimulatorPreferencesRepository = FakeSimulatorPreferencesRepository(),
        consoleAddressRepository: ConsoleAddressRepository = FakeConsoleAddressRepository(initial = "192.168.1.1"),
    ) = ConnectionBannerViewModel(
        checkLmuConnection = lmuChecker,
        checkGt7Ps5Connection = CheckGt7Ps5ConnectionUseCase(gt7Repository),
        observeConsoleAddress = ObserveConsoleAddressUseCase(consoleAddressRepository),
        observeSelectedSimulator = ObserveSelectedSimulatorUseCase(simulatorRepository),
    )
}

private class FakeLmuBannerConnectionChecker(
    var isConnected: Boolean,
    var throwCount: Int = 0,
) : LmuBannerConnectionChecker {
    override fun statusFlow() = flow {
        while (true) {
            val status = if (throwCount > 0) {
                throwCount--
                ConnectionBannerVmStatus.DISCONNECTED
            } else if (isConnected) {
                ConnectionBannerVmStatus.CONNECTED
            } else {
                ConnectionBannerVmStatus.DISCONNECTED
            }
            emit(status)
            delay(1_000L)
        }
    }
}

private class FakeConsoleAddressRepository(initial: String?) : ConsoleAddressRepository {
    private val flow = MutableStateFlow(initial)
    override fun consoleAddress(): Flow<String?> = flow
    override suspend fun saveConsoleAddress(address: String) { flow.value = address }
    fun save(address: String?) { flow.value = address }
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
    initial: String? = null,
) : SimulatorPreferencesRepository {
    private val flow = MutableStateFlow(initial)

    override fun selectedSimulator(): Flow<String?> = flow
    override suspend fun saveSelectedSimulator(simulator: String) { flow.value = simulator }
}
