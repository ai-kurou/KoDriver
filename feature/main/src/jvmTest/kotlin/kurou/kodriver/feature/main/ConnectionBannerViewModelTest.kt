package kurou.kodriver.feature.main

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.repository.LmuWindowsRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.usecase.CheckLmuWindowsConnectionUseCase
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
        val lmuRepository = FakeLmuRepository(isConnected = true)
        val simulatorRepository = FakeSimulatorPreferencesRepository(initial = "lmu_windows")
        val viewModel = createViewModel(lmuRepository, simulatorRepository)
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }

        dispatcher.scheduler.runCurrent()

        assertEquals(ConnectionBannerVmStatus.CONNECTED, viewModel.uiState.value.connectionStatus)
        collectionJob.cancelAndJoin()
    }

    @Test
    fun `GT7選択時は未確認状態を返す`() = runTest {
        val lmuRepository = FakeLmuRepository(isConnected = true)
        val simulatorRepository = FakeSimulatorPreferencesRepository(initial = "gt7_ps5")
        val viewModel = createViewModel(lmuRepository, simulatorRepository)
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }

        dispatcher.scheduler.runCurrent()

        assertEquals(ConnectionBannerVmStatus.UNCHECKED, viewModel.uiState.value.connectionStatus)
        collectionJob.cancelAndJoin()
    }

    @Test
    fun `シミュレータ未選択時は未確認状態とする`() = runTest {
        val lmuRepository = FakeLmuRepository(isConnected = false)
        val simulatorRepository = FakeSimulatorPreferencesRepository(initial = null)
        val viewModel = createViewModel(lmuRepository, simulatorRepository)

        assertEquals(ConnectionBannerVmStatus.UNCHECKED, viewModel.uiState.value.connectionStatus)
    }

    @Test
    fun `LMU選択に切り替えると接続確認を開始する`() = runTest {
        val lmuRepository = FakeLmuRepository(isConnected = true)
        val simulatorRepository = FakeSimulatorPreferencesRepository(initial = "gt7_ps5")
        val viewModel = createViewModel(lmuRepository, simulatorRepository)
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }
        dispatcher.scheduler.runCurrent()
        assertEquals(ConnectionBannerVmStatus.UNCHECKED, viewModel.uiState.value.connectionStatus)

        simulatorRepository.saveSelectedSimulator("lmu_windows")
        dispatcher.scheduler.runCurrent()

        assertEquals(ConnectionBannerVmStatus.CONNECTED, viewModel.uiState.value.connectionStatus)
        collectionJob.cancelAndJoin()
    }

    @Test
    fun `LMUから別シミュレータへ切り替えると未確認にリセットされる`() = runTest {
        val lmuRepository = FakeLmuRepository(isConnected = true)
        val simulatorRepository = FakeSimulatorPreferencesRepository(initial = "lmu_windows")
        val viewModel = createViewModel(lmuRepository, simulatorRepository)
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }
        dispatcher.scheduler.runCurrent()
        assertEquals(ConnectionBannerVmStatus.CONNECTED, viewModel.uiState.value.connectionStatus)

        simulatorRepository.saveSelectedSimulator("gt7_ps5")
        dispatcher.scheduler.runCurrent()

        assertEquals(ConnectionBannerVmStatus.UNCHECKED, viewModel.uiState.value.connectionStatus)
        collectionJob.cancelAndJoin()
    }

    @Test
    fun `LMU接続確認で例外が発生しても未接続として監視を継続する`() = runTest {
        val lmuRepository = FakeLmuRepository(isConnected = false, failureCount = 1)
        val simulatorRepository = FakeSimulatorPreferencesRepository(initial = "lmu_windows")
        val viewModel = createViewModel(lmuRepository, simulatorRepository)
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }
        dispatcher.scheduler.runCurrent()
        assertEquals(ConnectionBannerVmStatus.DISCONNECTED, viewModel.uiState.value.connectionStatus)

        lmuRepository.isConnected = true
        dispatcher.scheduler.advanceTimeBy(1_000L)
        dispatcher.scheduler.runCurrent()

        assertEquals(ConnectionBannerVmStatus.CONNECTED, viewModel.uiState.value.connectionStatus)
        collectionJob.cancelAndJoin()
    }

    private fun createViewModel(
        lmuRepository: LmuWindowsRepository,
        simulatorRepository: SimulatorPreferencesRepository,
    ) = ConnectionBannerViewModel(
        checkLmuWindowsConnection = CheckLmuWindowsConnectionUseCase(lmuRepository),
        observeSelectedSimulator = ObserveSelectedSimulatorUseCase(simulatorRepository),
    )
}

private class FakeLmuRepository(
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
    initial: String? = null,
) : SimulatorPreferencesRepository {
    private val flow = MutableStateFlow(initial)

    override fun selectedSimulator(): Flow<String?> = flow
    override suspend fun saveSelectedSimulator(simulator: String) { flow.value = simulator }
}
