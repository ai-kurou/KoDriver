package kurou.kodriver.feature.readoutlist

import io.mockk.MockKAnnotations
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
import kurou.kodriver.domain.repository.ReadoutStartSoundEnabledPreferencesRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class ReadoutListViewModelSelectionTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    @MockK
    private lateinit var simulatorRepository: SimulatorPreferencesRepository

    @MockK
    private lateinit var readoutRepository: ReadoutPreferencesRepository

    @MockK
    private lateinit var queueRepository: QueuePreferencesRepository

    @MockK
    private lateinit var startSoundRepository: ReadoutStartSoundEnabledPreferencesRepository

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
    fun `onItemSelectedでアイテムが選択される`() =
        runTest {
            val simulatorFlow = MutableStateFlow<Simulator?>(null)
            every { simulatorRepository.selectedSimulator() } returns simulatorFlow
            every { readoutRepository.observeReadoutEnabledStates("lmu_windows") } returns
                MutableStateFlow(emptyMap())
            every { readoutRepository.observeReadoutOrder("lmu_windows") } returns MutableStateFlow(emptyList())
            every { queueRepository.observeQueueEnabledStates() } returns MutableStateFlow(emptyMap())
            every { startSoundRepository.observeStartSoundEnabledStates() } returns MutableStateFlow(emptyMap())
            val viewModel =
                createViewModel(simulatorRepository, readoutRepository, queueRepository, startSoundRepository)

            simulatorFlow.update { Simulator.LmuWindows }
            viewModel.onItemSelected(ReadoutItemKey.LmuWindows.VehicleApproach.Root)

            assertEquals(ReadoutListItemType.LmuWindows.VehicleApproach, viewModel.uiState.first().selectedItem)
        }

    @Test
    fun `シミュレータ未選択時はonItemSelectedで選択状態は変わらない`() =
        runTest {
            every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(null)
            every { queueRepository.observeQueueEnabledStates() } returns MutableStateFlow(emptyMap())
            every { startSoundRepository.observeStartSoundEnabledStates() } returns MutableStateFlow(emptyMap())
            val viewModel =
                createViewModel(simulatorRepository, readoutRepository, queueRepository, startSoundRepository)

            viewModel.onItemSelected(ReadoutItemKey.LmuWindows.VehicleApproach.Root)

            assertNull(viewModel.uiState.first().selectedItem)
        }

    @Test
    fun `シミュレータに属さないアイテムを選択しても選択状態は変わらない`() =
        runTest {
            val simulatorFlow = MutableStateFlow<Simulator?>(null)
            every { simulatorRepository.selectedSimulator() } returns simulatorFlow
            every { readoutRepository.observeReadoutEnabledStates("lmu_windows") } returns
                MutableStateFlow(emptyMap())
            every { readoutRepository.observeReadoutOrder("lmu_windows") } returns MutableStateFlow(emptyList())
            every { queueRepository.observeQueueEnabledStates() } returns MutableStateFlow(emptyMap())
            every { startSoundRepository.observeStartSoundEnabledStates() } returns MutableStateFlow(emptyMap())
            val viewModel =
                createViewModel(simulatorRepository, readoutRepository, queueRepository, startSoundRepository)

            simulatorFlow.update { Simulator.LmuWindows }
            viewModel.onItemSelected(ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root)

            assertNull(viewModel.uiState.first().selectedItem)
        }

    @Test
    fun `同じアイテムを再度選択すると選択解除される`() =
        runTest {
            val simulatorFlow = MutableStateFlow<Simulator?>(null)
            every { simulatorRepository.selectedSimulator() } returns simulatorFlow
            every { readoutRepository.observeReadoutEnabledStates("lmu_windows") } returns
                MutableStateFlow(emptyMap())
            every { readoutRepository.observeReadoutOrder("lmu_windows") } returns MutableStateFlow(emptyList())
            every { queueRepository.observeQueueEnabledStates() } returns MutableStateFlow(emptyMap())
            every { startSoundRepository.observeStartSoundEnabledStates() } returns MutableStateFlow(emptyMap())
            val viewModel =
                createViewModel(simulatorRepository, readoutRepository, queueRepository, startSoundRepository)

            simulatorFlow.update { Simulator.LmuWindows }
            viewModel.onItemSelected(ReadoutItemKey.LmuWindows.VehicleApproach.Root)
            viewModel.onItemSelected(ReadoutItemKey.LmuWindows.VehicleApproach.Root)

            assertNull(viewModel.uiState.first().selectedItem)
        }

    @Test
    fun `アイテム選択後にシミュレータを切り替えると選択状態が解除される`() =
        runTest {
            val simulatorFlow = MutableStateFlow<Simulator?>(null)
            every { simulatorRepository.selectedSimulator() } returns simulatorFlow
            every { readoutRepository.observeReadoutEnabledStates("lmu_windows") } returns
                MutableStateFlow(emptyMap())
            every { readoutRepository.observeReadoutEnabledStates("gt7_ps5") } returns
                MutableStateFlow(emptyMap())
            every { readoutRepository.observeReadoutOrder("lmu_windows") } returns MutableStateFlow(emptyList())
            every { readoutRepository.observeReadoutOrder("gt7_ps5") } returns MutableStateFlow(emptyList())
            every { queueRepository.observeQueueEnabledStates() } returns MutableStateFlow(emptyMap())
            every { startSoundRepository.observeStartSoundEnabledStates() } returns MutableStateFlow(emptyMap())
            val viewModel =
                createViewModel(simulatorRepository, readoutRepository, queueRepository, startSoundRepository)

            simulatorFlow.update { Simulator.LmuWindows }
            viewModel.onItemSelected(ReadoutItemKey.LmuWindows.VehicleApproach.Root)
            assertEquals(ReadoutListItemType.LmuWindows.VehicleApproach, viewModel.uiState.first().selectedItem)

            simulatorFlow.update { Simulator.Gt7Ps5 }

            assertNull(viewModel.uiState.first().selectedItem)
            verify(exactly = 1) { simulatorRepository.selectedSimulator() }
            verify(exactly = 1) { readoutRepository.observeReadoutEnabledStates("lmu_windows") }
            verify(exactly = 1) { readoutRepository.observeReadoutEnabledStates("gt7_ps5") }
            verify(exactly = 1) { readoutRepository.observeReadoutOrder("lmu_windows") }
            verify(exactly = 1) { readoutRepository.observeReadoutOrder("gt7_ps5") }
            verify(exactly = 1) { queueRepository.observeQueueEnabledStates() }
            verify(exactly = 1) { startSoundRepository.observeStartSoundEnabledStates() }
            confirmVerified(simulatorRepository, readoutRepository, queueRepository, startSoundRepository)
        }

    @Test
    fun `clearSelectedItemで選択状態が解除される`() =
        runTest {
            every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(null)
            every { queueRepository.observeQueueEnabledStates() } returns MutableStateFlow(emptyMap())
            every { startSoundRepository.observeStartSoundEnabledStates() } returns MutableStateFlow(emptyMap())
            val viewModel =
                createViewModel(simulatorRepository, readoutRepository, queueRepository, startSoundRepository)

            viewModel.onItemSelected(ReadoutItemKey.LmuWindows.VehicleApproach.Root)
            viewModel.clearSelectedItem()

            assertNull(viewModel.uiState.first().selectedItem)
        }
}
