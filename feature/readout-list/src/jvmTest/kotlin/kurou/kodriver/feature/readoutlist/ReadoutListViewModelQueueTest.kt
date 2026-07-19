package kurou.kodriver.feature.readoutlist

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
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
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

private fun createViewModel(
    simulatorRepository: SimulatorPreferencesRepository,
    readoutRepository: ReadoutPreferencesRepository,
    queueRepository: QueuePreferencesRepository,
) = ReadoutListViewModel(
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

@OptIn(ExperimentalCoroutinesApi::class)
class ReadoutListViewModelQueueTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @MockK
    private lateinit var simulatorRepository: SimulatorPreferencesRepository

    @MockK
    private lateinit var readoutRepository: ReadoutPreferencesRepository

    @MockK
    private lateinit var queueRepository: QueuePreferencesRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        every { simulatorRepository.selectedSimulator() } returns MutableStateFlow<Simulator?>(null)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `キューのデフォルト値にRepositoryの永続化済み状態がマージされて表示される`() = runTest {
        every { queueRepository.observeQueueEnabledStates() } returns
            MutableStateFlow(mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.LmuWindows.Flag.Root to true))
        val viewModel = createViewModel(simulatorRepository, readoutRepository, queueRepository)

        val state = viewModel.uiState.first()
        assertEquals(true, state.queueEnabledStates[ReadoutItemKey.LmuWindows.Flag.Root])
        assertEquals(true, state.queueEnabledStates[ReadoutItemKey.LmuWindows.RemainingVirtualEnergyLaps.Root])
    }

    @Test
    fun `onQueueEnabledChangedでキューのON_OFF状態がRepositoryに保存される`() = runTest {
        val queueEnabledFlow = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
        every { queueRepository.observeQueueEnabledStates() } returns queueEnabledFlow
        coEvery {
            queueRepository.saveQueueEnabledState(ReadoutItemKey.LmuWindows.Flag.Root, true)
        } answers {
            queueEnabledFlow.update { it + (ReadoutItemKey.LmuWindows.Flag.Root to true) }
        }
        val viewModel = createViewModel(simulatorRepository, readoutRepository, queueRepository)

        viewModel.onQueueEnabledChanged(ReadoutItemKey.LmuWindows.Flag.Root, true)

        assertEquals(true, viewModel.uiState.first().queueEnabledStates[ReadoutItemKey.LmuWindows.Flag.Root])
        coVerify(exactly = 1) {
            queueRepository.saveQueueEnabledState(ReadoutItemKey.LmuWindows.Flag.Root, true)
        }
    }
}
