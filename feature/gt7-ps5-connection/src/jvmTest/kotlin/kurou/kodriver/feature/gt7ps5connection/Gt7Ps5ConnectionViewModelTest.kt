package kurou.kodriver.feature.gt7ps5connection

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
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.Gt7Ps5Repository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.usecase.CheckGt7Ps5ConnectionUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5ConnectionUseCase
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

    @MockK
    private lateinit var connectionRepository: Gt7Ps5Repository

    @MockK
    private lateinit var simulatorRepository: SimulatorPreferencesRepository

    private val defaultTelemetry = Gt7Ps5TelemetryData(
        lapCount = 0,
        lapsInRace = 0,
        bestLapTimeMs = 0,
        gasLevel = 0f,
        gasCapacity = 0f,
    )

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = Gt7Ps5ConnectionViewModel(
        observeGt7Ps5Connection = ObserveGt7Ps5ConnectionUseCase(
            checkGt7Ps5Connection = CheckGt7Ps5ConnectionUseCase(connectionRepository),
            observeGt7Ps5 = ObserveGt7Ps5UseCase(connectionRepository),
        ),
        observeSelectedSimulator = ObserveSelectedSimulatorUseCase(simulatorRepository),
    )

    @Test
    fun `GT7選択時に接続確認結果を反映する`() = runTest {
        every { connectionRepository.telemetryStream() } returns MutableStateFlow(defaultTelemetry)
        coEvery { connectionRepository.isConnected() } returns true
        every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(Simulator.Gt7Ps5)
        val viewModel = createViewModel()
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }

        dispatcher.scheduler.runCurrent()

        assertEquals(Gt7Ps5ConnectionStatus.CONNECTED, viewModel.uiState.first().connectionStatus)
        verify(exactly = 1) { simulatorRepository.selectedSimulator() }
        verify(exactly = 1) { connectionRepository.telemetryStream() }
        coVerify(exactly = 1) { connectionRepository.isConnected() }
        confirmVerified(connectionRepository, simulatorRepository)
        collectionJob.cancelAndJoin()
    }

    @Test
    fun `GT7選択時に燃料と周回数を保持する`() = runTest {
        val telemetryFlow = MutableStateFlow(
            Gt7Ps5TelemetryData(
                lapCount = 4,
                lapsInRace = 12,
                bestLapTimeMs = 0,
                gasLevel = 32.5f,
                gasCapacity = 50f,
            ),
        )
        every { connectionRepository.telemetryStream() } returns telemetryFlow
        coEvery { connectionRepository.isConnected() } returns true
        every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(Simulator.Gt7Ps5)
        val viewModel = createViewModel()
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }

        dispatcher.scheduler.runCurrent()

        val firstState = viewModel.uiState.first()
        assertEquals(32.5f, firstState.fuelLevel)
        assertEquals(50f, firstState.fuelCapacity)
        assertEquals(4, firstState.currentLap)
        assertEquals(12, firstState.totalLaps)

        telemetryFlow.update {
            Gt7Ps5TelemetryData(
                lapCount = 5,
                lapsInRace = 12,
                bestLapTimeMs = 0,
                gasLevel = 28f,
                gasCapacity = 50f,
            )
        }
        dispatcher.scheduler.runCurrent()

        val secondState = viewModel.uiState.first()
        assertEquals(28f, secondState.fuelLevel)
        assertEquals(5, secondState.currentLap)
        verify(exactly = 1) { simulatorRepository.selectedSimulator() }
        verify(exactly = 1) { connectionRepository.telemetryStream() }
        coVerify(exactly = 1) { connectionRepository.isConnected() }
        confirmVerified(connectionRepository, simulatorRepository)
        collectionJob.cancelAndJoin()
    }

    @Test
    fun `GT7非選択時は未確認状態を返す`() = runTest {
        every { connectionRepository.telemetryStream() } returns MutableStateFlow(defaultTelemetry)
        coEvery { connectionRepository.isConnected() } returns true
        every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(Simulator.LmuWindows)
        val viewModel = createViewModel()
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }

        dispatcher.scheduler.runCurrent()

        val state = viewModel.uiState.first()
        assertEquals(Gt7Ps5ConnectionStatus.UNCHECKED, state.connectionStatus)
        assertNull(state.fuelLevel)
        assertNull(state.fuelCapacity)
        assertNull(state.currentLap)
        assertNull(state.totalLaps)
        verify(exactly = 1) { simulatorRepository.selectedSimulator() }
        verify(exactly = 0) { connectionRepository.telemetryStream() }
        coVerify(exactly = 0) { connectionRepository.isConnected() }
        confirmVerified(connectionRepository, simulatorRepository)
        collectionJob.cancelAndJoin()
    }

    @Test
    fun `GT7選択前は未確認状態とする`() = runTest {
        every { connectionRepository.telemetryStream() } returns MutableStateFlow(defaultTelemetry)
        coEvery { connectionRepository.isConnected() } returns false
        every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(null)
        val viewModel = createViewModel()

        assertEquals(Gt7Ps5ConnectionStatus.UNCHECKED, viewModel.uiState.first().connectionStatus)
        verify(exactly = 1) { simulatorRepository.selectedSimulator() }
        verify(exactly = 0) { connectionRepository.telemetryStream() }
        coVerify(exactly = 0) { connectionRepository.isConnected() }
        confirmVerified(connectionRepository, simulatorRepository)
    }

    @Test
    fun `GT7選択に切り替えると接続確認を開始する`() = runTest {
        val simulatorFlow = MutableStateFlow<Simulator?>(Simulator.LmuWindows)
        every { connectionRepository.telemetryStream() } returns MutableStateFlow(defaultTelemetry)
        coEvery { connectionRepository.isConnected() } returns true
        every { simulatorRepository.selectedSimulator() } returns simulatorFlow
        coEvery { simulatorRepository.saveSelectedSimulator(Simulator.Gt7Ps5) } answers {
            simulatorFlow.update { Simulator.Gt7Ps5 }
        }
        val viewModel = createViewModel()
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }
        dispatcher.scheduler.runCurrent()
        assertEquals(Gt7Ps5ConnectionStatus.UNCHECKED, viewModel.uiState.first().connectionStatus)

        simulatorRepository.saveSelectedSimulator(Simulator.Gt7Ps5)
        dispatcher.scheduler.runCurrent()

        assertEquals(Gt7Ps5ConnectionStatus.CONNECTED, viewModel.uiState.first().connectionStatus)
        verify(exactly = 1) { simulatorRepository.selectedSimulator() }
        verify(exactly = 1) { connectionRepository.telemetryStream() }
        coVerify(exactly = 1) { connectionRepository.isConnected() }
        coVerify(exactly = 1) { simulatorRepository.saveSelectedSimulator(Simulator.Gt7Ps5) }
        confirmVerified(connectionRepository, simulatorRepository)
        collectionJob.cancelAndJoin()
    }

    @Test
    fun `GT7から別シミュレータへ切り替えると未確認にリセットされる`() = runTest {
        val simulatorFlow = MutableStateFlow<Simulator?>(Simulator.Gt7Ps5)
        every { connectionRepository.telemetryStream() } returns MutableStateFlow(defaultTelemetry)
        coEvery { connectionRepository.isConnected() } returns true
        every { simulatorRepository.selectedSimulator() } returns simulatorFlow
        val viewModel = createViewModel()
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }
        dispatcher.scheduler.runCurrent()
        assertEquals(Gt7Ps5ConnectionStatus.CONNECTED, viewModel.uiState.first().connectionStatus)

        simulatorFlow.update { Simulator.LmuWindows }
        dispatcher.scheduler.runCurrent()

        val state = viewModel.uiState.first()
        assertEquals(Gt7Ps5ConnectionStatus.UNCHECKED, state.connectionStatus)
        assertNull(state.fuelLevel)
        assertNull(state.fuelCapacity)
        assertNull(state.currentLap)
        assertNull(state.totalLaps)
        verify(exactly = 1) { simulatorRepository.selectedSimulator() }
        verify(exactly = 1) { connectionRepository.telemetryStream() }
        coVerify(exactly = 1) { connectionRepository.isConnected() }
        confirmVerified(connectionRepository, simulatorRepository)
        collectionJob.cancelAndJoin()
    }

    @Test
    fun `GT7選択時に一定間隔で接続状態を更新する`() = runTest {
        val connectedFlow = MutableStateFlow(false)
        every { connectionRepository.telemetryStream() } returns MutableStateFlow(defaultTelemetry)
        coEvery { connectionRepository.isConnected() } answers { connectedFlow.value }
        every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(Simulator.Gt7Ps5)
        val viewModel = createViewModel()
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }
        dispatcher.scheduler.runCurrent()
        assertEquals(Gt7Ps5ConnectionStatus.DISCONNECTED, viewModel.uiState.first().connectionStatus)

        connectedFlow.update { true }
        dispatcher.scheduler.advanceTimeBy(1_000L)
        dispatcher.scheduler.runCurrent()

        assertEquals(Gt7Ps5ConnectionStatus.CONNECTED, viewModel.uiState.first().connectionStatus)
        verify(exactly = 1) { simulatorRepository.selectedSimulator() }
        verify(exactly = 1) { connectionRepository.telemetryStream() }
        coVerify(exactly = 2) { connectionRepository.isConnected() }
        confirmVerified(connectionRepository, simulatorRepository)
        collectionJob.cancelAndJoin()
    }

    @Test
    fun `接続確認で例外が発生しても未接続として監視を継続する`() = runTest {
        val connectedFlow = MutableStateFlow(false)
        var remainingFailures = 1
        every { connectionRepository.telemetryStream() } returns MutableStateFlow(defaultTelemetry)
        coEvery { connectionRepository.isConnected() } answers {
            if (remainingFailures > 0) {
                remainingFailures--
                error("connection check failed")
            }
            connectedFlow.value
        }
        every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(Simulator.Gt7Ps5)
        val viewModel = createViewModel()
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }
        dispatcher.scheduler.runCurrent()
        assertEquals(Gt7Ps5ConnectionStatus.DISCONNECTED, viewModel.uiState.first().connectionStatus)

        connectedFlow.update { true }
        dispatcher.scheduler.advanceTimeBy(1_000L)
        dispatcher.scheduler.runCurrent()

        assertEquals(Gt7Ps5ConnectionStatus.CONNECTED, viewModel.uiState.first().connectionStatus)
        verify(exactly = 1) { simulatorRepository.selectedSimulator() }
        verify(exactly = 1) { connectionRepository.telemetryStream() }
        coVerify(exactly = 2) { connectionRepository.isConnected() }
        confirmVerified(connectionRepository, simulatorRepository)
        collectionJob.cancelAndJoin()
    }
}
