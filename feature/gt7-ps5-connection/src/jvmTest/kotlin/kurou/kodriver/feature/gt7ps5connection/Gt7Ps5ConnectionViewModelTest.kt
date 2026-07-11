package kurou.kodriver.feature.gt7ps5connection

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.Gt7Ps5Repository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.usecase.CheckGt7Ps5ConnectionUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5UseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

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

    private fun createConnectionRepository(
        initialTelemetry: Gt7Ps5TelemetryData = Gt7Ps5TelemetryData(
            lapCount = 0,
            lapsInRace = 0,
            bestLapTimeMs = 0,
            gasLevel = 0f,
            gasCapacity = 0f,
        ),
        isConnected: Boolean = false,
        failureCount: Int = 0,
    ): Triple<Gt7Ps5Repository, MutableStateFlow<Gt7Ps5TelemetryData>, MutableStateFlow<Boolean>> {
        val telemetryFlow = MutableStateFlow(initialTelemetry)
        val connectedFlow = MutableStateFlow(isConnected)
        var remainingFailures = failureCount
        val repository = mockk<Gt7Ps5Repository>()
        every { repository.telemetryStream() } returns telemetryFlow
        coEvery { repository.isConnected() } answers {
            if (remainingFailures > 0) {
                remainingFailures--
                error("connection check failed")
            }
            connectedFlow.value
        }
        return Triple(repository, telemetryFlow, connectedFlow)
    }

    private fun createSimulatorRepository(
        initial: Simulator? = null,
    ): Pair<SimulatorPreferencesRepository, MutableStateFlow<Simulator?>> {
        val simulatorFlow = MutableStateFlow(initial)
        val repository = mockk<SimulatorPreferencesRepository>()
        every { repository.selectedSimulator() } returns simulatorFlow
        coEvery { repository.saveSelectedSimulator(any()) } answers { simulatorFlow.value = firstArg() }
        return repository to simulatorFlow
    }

    @Test
    fun `GT7選択時に接続確認結果を反映する`() = runTest {
        val (connectionRepository, _, _) = createConnectionRepository(isConnected = true)
        val (simulatorRepository, _) = createSimulatorRepository(initial = Simulator.Gt7Ps5)
        val viewModel = createViewModel(connectionRepository, simulatorRepository)
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }

        dispatcher.scheduler.runCurrent()

        assertEquals(Gt7Ps5ConnectionStatus.CONNECTED, viewModel.uiState.first().connectionStatus)
        collectionJob.cancelAndJoin()
    }

    @Test
    fun `GT7選択時に燃料と周回数を保持する`() = runTest {
        val (connectionRepository, telemetryFlow, _) = createConnectionRepository(
            isConnected = true,
            initialTelemetry = Gt7Ps5TelemetryData(
                lapCount = 4,
                lapsInRace = 12,
                bestLapTimeMs = 0,
                gasLevel = 32.5f,
                gasCapacity = 50f,
            ),
        )
        val (simulatorRepository, _) = createSimulatorRepository(initial = Simulator.Gt7Ps5)
        val viewModel = createViewModel(connectionRepository, simulatorRepository)
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }

        dispatcher.scheduler.runCurrent()

        assertEquals(32.5f, viewModel.uiState.value.fuelLevel)
        assertEquals(50f, viewModel.uiState.value.fuelCapacity)
        assertEquals(4, viewModel.uiState.value.currentLap)
        assertEquals(12, viewModel.uiState.value.totalLaps)

        telemetryFlow.value = Gt7Ps5TelemetryData(
            lapCount = 5,
            lapsInRace = 12,
            bestLapTimeMs = 0,
            gasLevel = 28f,
            gasCapacity = 50f,
        )
        dispatcher.scheduler.runCurrent()

        assertEquals(28f, viewModel.uiState.value.fuelLevel)
        assertEquals(5, viewModel.uiState.value.currentLap)
        collectionJob.cancelAndJoin()
    }

    @Test
    fun `GT7非選択時は未確認状態を返す`() = runTest {
        val (connectionRepository, _, _) = createConnectionRepository(isConnected = true)
        val (simulatorRepository, _) = createSimulatorRepository(initial = Simulator.LmuWindows)
        val viewModel = createViewModel(connectionRepository, simulatorRepository)
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }

        dispatcher.scheduler.runCurrent()

        assertEquals(Gt7Ps5ConnectionStatus.UNCHECKED, viewModel.uiState.first().connectionStatus)
        assertNull(viewModel.uiState.value.fuelLevel)
        assertNull(viewModel.uiState.value.fuelCapacity)
        assertNull(viewModel.uiState.value.currentLap)
        assertNull(viewModel.uiState.value.totalLaps)
        collectionJob.cancelAndJoin()
    }

    @Test
    fun `GT7選択前は未確認状態とする`() = runTest {
        val (connectionRepository, _, _) = createConnectionRepository(isConnected = false)
        val (simulatorRepository, _) = createSimulatorRepository(initial = null)
        val viewModel = createViewModel(connectionRepository, simulatorRepository)

        assertEquals(Gt7Ps5ConnectionStatus.UNCHECKED, viewModel.uiState.first().connectionStatus)
    }

    @Test
    fun `GT7選択に切り替えると接続確認を開始する`() = runTest {
        val (connectionRepository, _, _) = createConnectionRepository(isConnected = true)
        val (simulatorRepository, _) = createSimulatorRepository(initial = Simulator.LmuWindows)
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
        val (connectionRepository, _, _) = createConnectionRepository(isConnected = true)
        val (simulatorRepository, simulatorFlow) = createSimulatorRepository(initial = Simulator.Gt7Ps5)
        val viewModel = createViewModel(connectionRepository, simulatorRepository)
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }
        dispatcher.scheduler.runCurrent()
        assertEquals(Gt7Ps5ConnectionStatus.CONNECTED, viewModel.uiState.first().connectionStatus)

        simulatorFlow.value = Simulator.LmuWindows
        dispatcher.scheduler.runCurrent()

        assertEquals(Gt7Ps5ConnectionStatus.UNCHECKED, viewModel.uiState.first().connectionStatus)
        assertNull(viewModel.uiState.value.fuelLevel)
        assertNull(viewModel.uiState.value.fuelCapacity)
        assertNull(viewModel.uiState.value.currentLap)
        assertNull(viewModel.uiState.value.totalLaps)
        collectionJob.cancelAndJoin()
    }

    @Test
    fun `GT7選択時に一定間隔で接続状態を更新する`() = runTest {
        val (connectionRepository, _, connectedFlow) = createConnectionRepository(isConnected = false)
        val (simulatorRepository, _) = createSimulatorRepository(initial = Simulator.Gt7Ps5)
        val viewModel = createViewModel(connectionRepository, simulatorRepository)
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }
        dispatcher.scheduler.runCurrent()
        assertEquals(Gt7Ps5ConnectionStatus.DISCONNECTED, viewModel.uiState.first().connectionStatus)

        connectedFlow.value = true
        dispatcher.scheduler.advanceTimeBy(1_000L)
        dispatcher.scheduler.runCurrent()

        assertEquals(Gt7Ps5ConnectionStatus.CONNECTED, viewModel.uiState.first().connectionStatus)
        collectionJob.cancelAndJoin()
    }

    @Test
    fun `接続確認で例外が発生しても未接続として監視を継続する`() = runTest {
        val (connectionRepository, _, connectedFlow) = createConnectionRepository(isConnected = false, failureCount = 1)
        val (simulatorRepository, _) = createSimulatorRepository(initial = Simulator.Gt7Ps5)
        val viewModel = createViewModel(connectionRepository, simulatorRepository)
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }
        dispatcher.scheduler.runCurrent()
        assertEquals(Gt7Ps5ConnectionStatus.DISCONNECTED, viewModel.uiState.first().connectionStatus)

        connectedFlow.value = true
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
        observeGt7Ps5 = ObserveGt7Ps5UseCase(connectionRepository),
        observeSelectedSimulator = ObserveSelectedSimulatorUseCase(simulatorRepository),
    )
}
