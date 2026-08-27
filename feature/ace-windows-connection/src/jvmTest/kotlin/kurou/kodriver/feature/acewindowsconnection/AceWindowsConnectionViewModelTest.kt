package kurou.kodriver.feature.acewindowsconnection

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
import kurou.kodriver.domain.model.AceWindowsFuelData
import kurou.kodriver.domain.model.FuelPercent
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.AceWindowsFuelRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.usecase.CheckAceWindowsConnectionUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsConnectionUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsFuelUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class AceWindowsConnectionViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @MockK
    private lateinit var connectionRepository: AceWindowsFuelRepository

    @MockK
    private lateinit var simulatorRepository: SimulatorPreferencesRepository

    private val defaultFuel = AceWindowsFuelData(remainingPercent = FuelPercent(0.0))

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        AceWindowsConnectionViewModel(
            observeAceWindowsConnection =
                ObserveAceWindowsConnectionUseCase(
                    checkAceWindowsConnection = CheckAceWindowsConnectionUseCase(connectionRepository),
                    observeAceWindowsFuel = ObserveAceWindowsFuelUseCase(connectionRepository),
                ),
            observeSelectedSimulator = ObserveSelectedSimulatorUseCase(simulatorRepository),
        )

    @Test
    fun `ACE選択時に接続確認結果を反映する`() =
        runTest {
            every { connectionRepository.fuelStream() } returns MutableStateFlow(defaultFuel)
            coEvery { connectionRepository.isConnected() } returns true
            every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(Simulator.AceWindows)
            val viewModel = createViewModel()
            val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }

            dispatcher.scheduler.runCurrent()

            assertEquals(AceWindowsConnectionStatus.CONNECTED, viewModel.uiState.first().connectionStatus)
            verify(exactly = 1) { simulatorRepository.selectedSimulator() }
            verify(exactly = 1) { connectionRepository.fuelStream() }
            coVerify(exactly = 1) { connectionRepository.isConnected() }
            confirmVerified(connectionRepository, simulatorRepository)
            collectionJob.cancelAndJoin()
        }

    @Test
    fun `ACE選択時に燃料残量を保持する`() =
        runTest {
            val fuelFlow = MutableStateFlow(AceWindowsFuelData(remainingPercent = FuelPercent(32.5)))
            every { connectionRepository.fuelStream() } returns fuelFlow
            coEvery { connectionRepository.isConnected() } returns true
            every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(Simulator.AceWindows)
            val viewModel = createViewModel()
            val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }

            dispatcher.scheduler.runCurrent()

            assertEquals(32.5, viewModel.uiState.first().fuelRemainingPercent)

            fuelFlow.update { AceWindowsFuelData(remainingPercent = FuelPercent(28.0)) }
            dispatcher.scheduler.runCurrent()

            assertEquals(28.0, viewModel.uiState.first().fuelRemainingPercent)
            verify(exactly = 1) { simulatorRepository.selectedSimulator() }
            verify(exactly = 1) { connectionRepository.fuelStream() }
            coVerify(exactly = 1) { connectionRepository.isConnected() }
            confirmVerified(connectionRepository, simulatorRepository)
            collectionJob.cancelAndJoin()
        }

    @Test
    fun `ACE非選択時は未確認状態を返す`() =
        runTest {
            every { connectionRepository.fuelStream() } returns MutableStateFlow(defaultFuel)
            coEvery { connectionRepository.isConnected() } returns true
            every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(Simulator.LmuWindows)
            val viewModel = createViewModel()
            val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }

            dispatcher.scheduler.runCurrent()

            val state = viewModel.uiState.first()
            assertEquals(AceWindowsConnectionStatus.UNCHECKED, state.connectionStatus)
            assertNull(state.fuelRemainingPercent)
            verify(exactly = 1) { simulatorRepository.selectedSimulator() }
            verify(exactly = 0) { connectionRepository.fuelStream() }
            coVerify(exactly = 0) { connectionRepository.isConnected() }
            confirmVerified(connectionRepository, simulatorRepository)
            collectionJob.cancelAndJoin()
        }

    @Test
    fun `ACEから別シミュレータへ切り替えると未確認にリセットされる`() =
        runTest {
            val simulatorFlow = MutableStateFlow<Simulator>(Simulator.AceWindows)
            every { connectionRepository.fuelStream() } returns MutableStateFlow(defaultFuel)
            coEvery { connectionRepository.isConnected() } returns true
            every { simulatorRepository.selectedSimulator() } returns simulatorFlow
            val viewModel = createViewModel()
            val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }
            dispatcher.scheduler.runCurrent()
            assertEquals(AceWindowsConnectionStatus.CONNECTED, viewModel.uiState.first().connectionStatus)

            simulatorFlow.update { Simulator.LmuWindows }
            dispatcher.scheduler.runCurrent()

            val state = viewModel.uiState.first()
            assertEquals(AceWindowsConnectionStatus.UNCHECKED, state.connectionStatus)
            assertNull(state.fuelRemainingPercent)
            verify(exactly = 1) { simulatorRepository.selectedSimulator() }
            verify(exactly = 1) { connectionRepository.fuelStream() }
            coVerify(exactly = 1) { connectionRepository.isConnected() }
            confirmVerified(connectionRepository, simulatorRepository)
            collectionJob.cancelAndJoin()
        }

    @Test
    fun `ACE選択時に一定間隔で接続状態を更新する`() =
        runTest {
            val connectedFlow = MutableStateFlow(false)
            every { connectionRepository.fuelStream() } returns MutableStateFlow(defaultFuel)
            coEvery { connectionRepository.isConnected() } answers { connectedFlow.value }
            every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(Simulator.AceWindows)
            val viewModel = createViewModel()
            val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }
            dispatcher.scheduler.runCurrent()
            assertEquals(AceWindowsConnectionStatus.DISCONNECTED, viewModel.uiState.first().connectionStatus)

            connectedFlow.update { true }
            dispatcher.scheduler.advanceTimeBy(1_000L)
            dispatcher.scheduler.runCurrent()

            assertEquals(AceWindowsConnectionStatus.CONNECTED, viewModel.uiState.first().connectionStatus)
            verify(exactly = 1) { simulatorRepository.selectedSimulator() }
            verify(exactly = 1) { connectionRepository.fuelStream() }
            coVerify(exactly = 2) { connectionRepository.isConnected() }
            confirmVerified(connectionRepository, simulatorRepository)
            collectionJob.cancelAndJoin()
        }

    @Test
    fun `接続確認で例外が発生しても未接続として監視を継続する`() =
        runTest {
            val connectedFlow = MutableStateFlow(false)
            var remainingFailures = 1
            every { connectionRepository.fuelStream() } returns MutableStateFlow(defaultFuel)
            coEvery { connectionRepository.isConnected() } answers {
                if (remainingFailures > 0) {
                    remainingFailures--
                    error("connection check failed")
                }
                connectedFlow.value
            }
            every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(Simulator.AceWindows)
            val viewModel = createViewModel()
            val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }
            dispatcher.scheduler.runCurrent()
            assertEquals(AceWindowsConnectionStatus.DISCONNECTED, viewModel.uiState.first().connectionStatus)

            connectedFlow.update { true }
            dispatcher.scheduler.advanceTimeBy(1_000L)
            dispatcher.scheduler.runCurrent()

            assertEquals(AceWindowsConnectionStatus.CONNECTED, viewModel.uiState.first().connectionStatus)
            verify(exactly = 1) { simulatorRepository.selectedSimulator() }
            verify(exactly = 1) { connectionRepository.fuelStream() }
            coVerify(exactly = 2) { connectionRepository.isConnected() }
            confirmVerified(connectionRepository, simulatorRepository)
            collectionJob.cancelAndJoin()
        }
}
