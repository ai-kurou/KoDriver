package kurou.kodriver.feature.lmuwindowsconnection

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
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.LmuWindowsRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.usecase.CheckLmuWindowsConnectionUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsConnectionUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsConnectionViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @MockK
    private lateinit var connectionRepository: LmuWindowsRepository

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

    private fun createViewModel() =
        LmuWindowsConnectionViewModel(
            observeLmuWindowsConnection =
                ObserveLmuWindowsConnectionUseCase(
                    checkLmuWindowsConnection = CheckLmuWindowsConnectionUseCase(connectionRepository),
                ),
            observeSelectedSimulator = ObserveSelectedSimulatorUseCase(simulatorRepository),
        )

    @Test
    fun `LMU選択時に接続確認結果を反映する`() =
        runTest {
            coEvery { connectionRepository.isConnected() } returns true
            every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(Simulator.LmuWindows)
            val viewModel = createViewModel()
            val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }

            dispatcher.scheduler.runCurrent()

            assertEquals(LmuWindowsConnectionStatus.CONNECTED, viewModel.uiState.first().connectionStatus)
            verify(exactly = 1) { simulatorRepository.selectedSimulator() }
            coVerify(exactly = 1) { connectionRepository.isConnected() }
            confirmVerified(connectionRepository, simulatorRepository)
            collectionJob.cancelAndJoin()
        }

    @Test
    fun `LMU非選択時は未接続・未確認状態を返す`() =
        runTest {
            coEvery { connectionRepository.isConnected() } returns true
            every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(null)
            val viewModel = createViewModel()
            val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }

            dispatcher.scheduler.runCurrent()

            assertEquals(LmuWindowsConnectionStatus.UNCHECKED, viewModel.uiState.first().connectionStatus)
            verify(exactly = 1) { simulatorRepository.selectedSimulator() }
            coVerify(exactly = 0) { connectionRepository.isConnected() }
            confirmVerified(connectionRepository, simulatorRepository)
            collectionJob.cancelAndJoin()
        }

    @Test
    fun `LMU選択前は未確認状態とする`() =
        runTest {
            coEvery { connectionRepository.isConnected() } returns false
            every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(null)
            val viewModel = createViewModel()

            assertEquals(LmuWindowsConnectionStatus.UNCHECKED, viewModel.uiState.first().connectionStatus)
            verify(exactly = 1) { simulatorRepository.selectedSimulator() }
            coVerify(exactly = 0) { connectionRepository.isConnected() }
            confirmVerified(connectionRepository, simulatorRepository)
        }

    @Test
    fun `LMU選択に切り替えると接続確認を開始する`() =
        runTest {
            val simulatorFlow = MutableStateFlow<Simulator?>(null)
            coEvery { connectionRepository.isConnected() } returns true
            every { simulatorRepository.selectedSimulator() } returns simulatorFlow
            coEvery { simulatorRepository.saveSelectedSimulator(Simulator.LmuWindows) } answers {
                simulatorFlow.update { Simulator.LmuWindows }
            }
            val viewModel = createViewModel()
            val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }
            dispatcher.scheduler.runCurrent()
            assertEquals(LmuWindowsConnectionStatus.UNCHECKED, viewModel.uiState.first().connectionStatus)

            simulatorRepository.saveSelectedSimulator(Simulator.LmuWindows)
            dispatcher.scheduler.runCurrent()

            assertEquals(LmuWindowsConnectionStatus.CONNECTED, viewModel.uiState.first().connectionStatus)
            verify(exactly = 1) { simulatorRepository.selectedSimulator() }
            coVerify(exactly = 1) { connectionRepository.isConnected() }
            coVerify(exactly = 1) { simulatorRepository.saveSelectedSimulator(Simulator.LmuWindows) }
            confirmVerified(connectionRepository, simulatorRepository)
            collectionJob.cancelAndJoin()
        }

    @Test
    fun `LMUから別シミュレータへ切り替えると未接続にリセットされる`() =
        runTest {
            val simulatorFlow = MutableStateFlow<Simulator?>(Simulator.LmuWindows)
            coEvery { connectionRepository.isConnected() } returns true
            every { simulatorRepository.selectedSimulator() } returns simulatorFlow
            val viewModel = createViewModel()
            val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }
            dispatcher.scheduler.runCurrent()
            assertEquals(LmuWindowsConnectionStatus.CONNECTED, viewModel.uiState.first().connectionStatus)

            simulatorFlow.update { null }
            dispatcher.scheduler.runCurrent()

            assertEquals(LmuWindowsConnectionStatus.UNCHECKED, viewModel.uiState.first().connectionStatus)
            verify(exactly = 1) { simulatorRepository.selectedSimulator() }
            coVerify(exactly = 1) { connectionRepository.isConnected() }
            confirmVerified(connectionRepository, simulatorRepository)
            collectionJob.cancelAndJoin()
        }

    @Test
    fun `LMU選択時に一定間隔で接続状態を更新する`() =
        runTest {
            val connectedFlow = MutableStateFlow(false)
            coEvery { connectionRepository.isConnected() } answers { connectedFlow.value }
            every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(Simulator.LmuWindows)
            val viewModel = createViewModel()
            val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }
            dispatcher.scheduler.runCurrent()
            assertEquals(LmuWindowsConnectionStatus.DISCONNECTED, viewModel.uiState.first().connectionStatus)

            connectedFlow.update { true }
            dispatcher.scheduler.advanceTimeBy(1_000L)
            dispatcher.scheduler.runCurrent()

            assertEquals(LmuWindowsConnectionStatus.CONNECTED, viewModel.uiState.first().connectionStatus)
            verify(exactly = 1) { simulatorRepository.selectedSimulator() }
            coVerify(exactly = 2) { connectionRepository.isConnected() }
            confirmVerified(connectionRepository, simulatorRepository)
            collectionJob.cancelAndJoin()
        }

    @Test
    fun `接続確認で例外が発生しても未接続として監視を継続する`() =
        runTest {
            val connectedFlow = MutableStateFlow(false)
            var remainingFailures = 1
            coEvery { connectionRepository.isConnected() } answers {
                if (remainingFailures > 0) {
                    remainingFailures--
                    error("connection check failed")
                }
                connectedFlow.value
            }
            every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(Simulator.LmuWindows)
            val viewModel = createViewModel()
            val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }
            dispatcher.scheduler.runCurrent()
            assertEquals(LmuWindowsConnectionStatus.DISCONNECTED, viewModel.uiState.first().connectionStatus)

            connectedFlow.update { true }
            dispatcher.scheduler.advanceTimeBy(1_000L)
            dispatcher.scheduler.runCurrent()

            assertEquals(LmuWindowsConnectionStatus.CONNECTED, viewModel.uiState.first().connectionStatus)
            verify(exactly = 1) { simulatorRepository.selectedSimulator() }
            coVerify(exactly = 2) { connectionRepository.isConnected() }
            confirmVerified(connectionRepository, simulatorRepository)
            collectionJob.cancelAndJoin()
        }
}
