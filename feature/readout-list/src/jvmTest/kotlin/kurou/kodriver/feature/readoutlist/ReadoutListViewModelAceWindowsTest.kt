package kurou.kodriver.feature.readoutlist

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.QueuePreferencesRepository
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.usecase.ObserveQueueEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.ResolveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.SaveQueueEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.SaveSelectedSimulatorUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ReadoutListViewModelAceWindowsTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @MockK
    private lateinit var simulatorRepository: SimulatorPreferencesRepository

    @MockK
    private lateinit var readoutRepository: ReadoutPreferencesRepository

    @MockK
    private lateinit var queueRepository: QueuePreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `ace_windowsを選択するとlistPaneにフラッグと燃料残量アイテムが表示される`() =
        runTest {
        val simulatorFlow = MutableStateFlow<Simulator?>(null)
        every { simulatorRepository.selectedSimulator() } returns simulatorFlow
        coEvery { simulatorRepository.saveSelectedSimulator(Simulator.AceWindows) } answers {
            simulatorFlow.update { Simulator.AceWindows }
        }
        every { readoutRepository.observeReadoutEnabledStates("ace_windows") } returns MutableStateFlow(emptyMap())
        every { readoutRepository.observeReadoutOrder("ace_windows") } returns MutableStateFlow(emptyList())
        every { queueRepository.observeQueueEnabledStates() } returns MutableStateFlow(emptyMap())
        val viewModel =
            ReadoutListViewModel(
            observeSelectedSimulator = ObserveSelectedSimulatorUseCase(simulatorRepository),
            saveSelectedSimulator = SaveSelectedSimulatorUseCase(simulatorRepository),
            observeReadoutEnabledStates = ObserveReadoutEnabledStatesUseCase(readoutRepository),
            saveReadoutEnabledState = SaveReadoutEnabledStateUseCase(readoutRepository),
            observeReadoutOrder = ObserveReadoutOrderUseCase(readoutRepository),
            resolveReadoutOrder = ResolveReadoutOrderUseCase(),
            saveReadoutOrder = SaveReadoutOrderUseCase(readoutRepository),
            observeQueueEnabledStates = ObserveQueueEnabledStatesUseCase(queueRepository),
            saveQueueEnabledState = SaveQueueEnabledStateUseCase(queueRepository),
        )

        assertEquals(
            listOf(Simulator.LmuWindows, Simulator.Gt7Ps5, Simulator.AceWindows),
            viewModel.uiState.first().simulators,
        )

        viewModel.onSimulatorSelected(Simulator.AceWindows)

        val state = viewModel.uiState.first()
        assertEquals(Simulator.AceWindows, state.selectedSimulator)
        assertEquals(
            listOf(ReadoutItemKey.AceWindows.Flag.Root, ReadoutItemKey.AceWindows.RemainingFuel.Root),
            state.items,
        )
        assertEquals(true, state.readoutEnabledStates[ReadoutItemKey.AceWindows.Flag.Root])
        assertEquals(true, state.readoutEnabledStates[ReadoutItemKey.AceWindows.RemainingFuel.Root])
        assertEquals(true, state.queueEnabledStates[ReadoutItemKey.AceWindows.Flag.Root])
        assertEquals(true, state.queueEnabledStates[ReadoutItemKey.AceWindows.RemainingFuel.Root])
        verify(exactly = 1) { simulatorRepository.selectedSimulator() }
        coVerify(exactly = 1) { simulatorRepository.saveSelectedSimulator(Simulator.AceWindows) }
        verify(exactly = 1) { readoutRepository.observeReadoutEnabledStates("ace_windows") }
        verify(exactly = 1) { readoutRepository.observeReadoutOrder("ace_windows") }
        verify(exactly = 1) { queueRepository.observeQueueEnabledStates() }
        confirmVerified(simulatorRepository, readoutRepository, queueRepository)
    }
}
