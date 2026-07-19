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
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

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
class ReadoutListViewModelTest {

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
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `シミュレータ未選択時はアイテムが空で選択後に読み込まれる`() = runTest {
        val simulatorFlow = MutableStateFlow<Simulator?>(null)
        val enabledStatesFlow = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
        val orderFlow = MutableStateFlow<List<ReadoutItemKey>>(emptyList())
        every { simulatorRepository.selectedSimulator() } returns simulatorFlow
        coEvery { simulatorRepository.saveSelectedSimulator(Simulator.LmuWindows) } answers {
            simulatorFlow.update { Simulator.LmuWindows }
        }
        every { readoutRepository.observeReadoutEnabledStates("lmu_windows") } returns enabledStatesFlow
        every { readoutRepository.observeReadoutOrder("lmu_windows") } returns orderFlow
        every { queueRepository.observeQueueEnabledStates() } returns MutableStateFlow(emptyMap())
        val viewModel = createViewModel(simulatorRepository, readoutRepository, queueRepository)

        assertNull(viewModel.uiState.first().selectedSimulator)
        assertEquals(emptyList(), viewModel.uiState.first().items)

        viewModel.onSimulatorSelected(Simulator.LmuWindows)

        val state = viewModel.uiState.first()
        assertEquals(Simulator.LmuWindows, state.selectedSimulator)
        assertEquals(
            listOf(
                ReadoutItemKey.LmuWindows.Flag.Root,
                ReadoutItemKey.LmuWindows.TyreTemperature.Root,
                ReadoutItemKey.LmuWindows.VehicleApproach.Root,
                ReadoutItemKey.LmuWindows.RemainingVirtualEnergyLaps.Root,
                ReadoutItemKey.LmuWindows.VehicleDamage.Root,
                ReadoutItemKey.LmuWindows.MyBestLap.Root,
            ),
            state.items,
        )
        assertEquals(false, state.readoutEnabledStates[ReadoutItemKey.LmuWindows.VehicleDamage.Root])
        assertEquals(false, state.readoutEnabledStates[ReadoutItemKey.LmuWindows.MyBestLap.Root])
        verify(exactly = 1) { simulatorRepository.selectedSimulator() }
        coVerify(exactly = 1) { simulatorRepository.saveSelectedSimulator(Simulator.LmuWindows) }
        confirmVerified(simulatorRepository)
    }

    @Test
    fun `moveItemでアイテムの順序を変更できる`() = runTest {
        val simulatorFlow = MutableStateFlow<Simulator?>(null)
        every { simulatorRepository.selectedSimulator() } returns simulatorFlow
        coEvery { simulatorRepository.saveSelectedSimulator(Simulator.LmuWindows) } answers {
            simulatorFlow.update { Simulator.LmuWindows }
        }
        every { readoutRepository.observeReadoutEnabledStates("lmu_windows") } returns
            MutableStateFlow(emptyMap())
        every { readoutRepository.observeReadoutOrder("lmu_windows") } returns MutableStateFlow(emptyList())
        coEvery { readoutRepository.saveReadoutOrder("lmu_windows", any()) } returns Unit
        every { queueRepository.observeQueueEnabledStates() } returns MutableStateFlow(emptyMap())
        val viewModel = createViewModel(simulatorRepository, readoutRepository, queueRepository)

        viewModel.onSimulatorSelected(Simulator.LmuWindows)
        viewModel.moveItem(0, 1)

        assertEquals(
            listOf(
                ReadoutItemKey.LmuWindows.TyreTemperature.Root,
                ReadoutItemKey.LmuWindows.Flag.Root,
                ReadoutItemKey.LmuWindows.VehicleApproach.Root,
                ReadoutItemKey.LmuWindows.RemainingVirtualEnergyLaps.Root,
                ReadoutItemKey.LmuWindows.VehicleDamage.Root,
                ReadoutItemKey.LmuWindows.MyBestLap.Root,
            ),
            viewModel.uiState.first().items,
        )
        coVerify(exactly = 1) {
            readoutRepository.saveReadoutOrder(
                "lmu_windows",
                listOf(
                    ReadoutItemKey.LmuWindows.TyreTemperature.Root,
                    ReadoutItemKey.LmuWindows.Flag.Root,
                    ReadoutItemKey.LmuWindows.VehicleApproach.Root,
                    ReadoutItemKey.LmuWindows.RemainingVirtualEnergyLaps.Root,
                    ReadoutItemKey.LmuWindows.VehicleDamage.Root,
                    ReadoutItemKey.LmuWindows.MyBestLap.Root,
                ),
            )
        }
    }

    @Test
    fun `シミュレータ未選択時はmoveItemで順序を保存しない`() = runTest {
        every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(null)
        every { queueRepository.observeQueueEnabledStates() } returns MutableStateFlow(emptyMap())
        val viewModel = createViewModel(simulatorRepository, readoutRepository, queueRepository)

        viewModel.moveItem(0, 1)

        assertEquals(emptyList(), viewModel.uiState.first().items)
        confirmVerified(readoutRepository)
    }

    @Test
    fun `onReadoutEnabledChangedでON_OFF状態がRepositoryに保存される`() = runTest {
        val simulatorFlow = MutableStateFlow<Simulator?>(null)
        val enabledStatesFlow = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
        every { simulatorRepository.selectedSimulator() } returns simulatorFlow
        coEvery { simulatorRepository.saveSelectedSimulator(Simulator.LmuWindows) } answers {
            simulatorFlow.update { Simulator.LmuWindows }
        }
        every { readoutRepository.observeReadoutEnabledStates("lmu_windows") } returns enabledStatesFlow
        every { readoutRepository.observeReadoutOrder("lmu_windows") } returns MutableStateFlow(emptyList())
        coEvery {
            readoutRepository.saveReadoutEnabledState(
                "lmu_windows",
                ReadoutItemKey.LmuWindows.VehicleApproach.Root,
                false,
            )
        } answers {
            enabledStatesFlow.update { it + (ReadoutItemKey.LmuWindows.VehicleApproach.Root to false) }
        }
        every { queueRepository.observeQueueEnabledStates() } returns MutableStateFlow(emptyMap())
        val viewModel = createViewModel(simulatorRepository, readoutRepository, queueRepository)

        viewModel.onSimulatorSelected(Simulator.LmuWindows)
        viewModel.onReadoutEnabledChanged(ReadoutItemKey.LmuWindows.VehicleApproach.Root, false)

        assertEquals(
            false,
            viewModel.uiState.first().readoutEnabledStates[ReadoutItemKey.LmuWindows.VehicleApproach.Root],
        )
        coVerify(exactly = 1) {
            readoutRepository.saveReadoutEnabledState(
                "lmu_windows",
                ReadoutItemKey.LmuWindows.VehicleApproach.Root,
                false,
            )
        }
    }

    @Test
    fun `シミュレータ未選択時はON_OFF状態を保存しない`() = runTest {
        every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(null)
        every { queueRepository.observeQueueEnabledStates() } returns MutableStateFlow(emptyMap())
        val viewModel = createViewModel(simulatorRepository, readoutRepository, queueRepository)

        viewModel.onReadoutEnabledChanged(ReadoutItemKey.LmuWindows.VehicleApproach.Root, false)

        assertEquals(emptyMap(), viewModel.uiState.first().readoutEnabledStates)
        confirmVerified(readoutRepository)
    }

    @Test
    fun `シミュレータを選択するとRepositoryから永続化済みのON_OFF状態が読み込まれる`() = runTest {
        val simulatorFlow = MutableStateFlow<Simulator?>(null)
        every { simulatorRepository.selectedSimulator() } returns simulatorFlow
        coEvery { simulatorRepository.saveSelectedSimulator(Simulator.LmuWindows) } answers {
            simulatorFlow.update { Simulator.LmuWindows }
        }
        every { readoutRepository.observeReadoutEnabledStates("lmu_windows") } returns
            MutableStateFlow(mapOf(ReadoutItemKey.LmuWindows.Flag.Root to false))
        every { readoutRepository.observeReadoutOrder("lmu_windows") } returns MutableStateFlow(emptyList())
        every { queueRepository.observeQueueEnabledStates() } returns MutableStateFlow(emptyMap())
        val viewModel = createViewModel(simulatorRepository, readoutRepository, queueRepository)

        viewModel.onSimulatorSelected(Simulator.LmuWindows)

        assertEquals(false, viewModel.uiState.first().readoutEnabledStates[ReadoutItemKey.LmuWindows.Flag.Root])
    }

    @Test
    fun `シミュレータを選択するとRepositoryから永続化済みの順序が読み込まれる`() = runTest {
        val simulatorFlow = MutableStateFlow<Simulator?>(null)
        every { simulatorRepository.selectedSimulator() } returns simulatorFlow
        coEvery { simulatorRepository.saveSelectedSimulator(Simulator.LmuWindows) } answers {
            simulatorFlow.update { Simulator.LmuWindows }
        }
        every { readoutRepository.observeReadoutEnabledStates("lmu_windows") } returns
            MutableStateFlow(emptyMap())
        every { readoutRepository.observeReadoutOrder("lmu_windows") } returns MutableStateFlow(
            listOf(ReadoutItemKey.LmuWindows.Flag.Root, ReadoutItemKey.LmuWindows.VehicleApproach.Root),
        )
        every { queueRepository.observeQueueEnabledStates() } returns MutableStateFlow(emptyMap())
        val viewModel = createViewModel(simulatorRepository, readoutRepository, queueRepository)

        viewModel.onSimulatorSelected(Simulator.LmuWindows)

        assertEquals(
            listOf(
                ReadoutItemKey.LmuWindows.Flag.Root,
                ReadoutItemKey.LmuWindows.VehicleApproach.Root,
                ReadoutItemKey.LmuWindows.TyreTemperature.Root,
                ReadoutItemKey.LmuWindows.RemainingVirtualEnergyLaps.Root,
                ReadoutItemKey.LmuWindows.VehicleDamage.Root,
                ReadoutItemKey.LmuWindows.MyBestLap.Root,
            ),
            viewModel.uiState.first().items,
        )
    }

    @Test
    fun `moveItemで変更した順序がRepositoryに保存される`() = runTest {
        val simulatorFlow = MutableStateFlow<Simulator?>(null)
        val orderFlow = MutableStateFlow<List<ReadoutItemKey>>(emptyList())
        every { simulatorRepository.selectedSimulator() } returns simulatorFlow
        coEvery { simulatorRepository.saveSelectedSimulator(Simulator.LmuWindows) } answers {
            simulatorFlow.update { Simulator.LmuWindows }
        }
        every { readoutRepository.observeReadoutEnabledStates("lmu_windows") } returns
            MutableStateFlow(emptyMap())
        every { readoutRepository.observeReadoutOrder("lmu_windows") } returns orderFlow
        coEvery { readoutRepository.saveReadoutOrder("lmu_windows", any()) } answers {
            orderFlow.update { secondArg() }
        }
        every { queueRepository.observeQueueEnabledStates() } returns MutableStateFlow(emptyMap())
        val viewModel = createViewModel(simulatorRepository, readoutRepository, queueRepository)

        viewModel.onSimulatorSelected(Simulator.LmuWindows)
        viewModel.moveItem(0, 1)

        assertEquals(
            listOf(
                ReadoutItemKey.LmuWindows.TyreTemperature.Root,
                ReadoutItemKey.LmuWindows.Flag.Root,
                ReadoutItemKey.LmuWindows.VehicleApproach.Root,
                ReadoutItemKey.LmuWindows.RemainingVirtualEnergyLaps.Root,
                ReadoutItemKey.LmuWindows.VehicleDamage.Root,
                ReadoutItemKey.LmuWindows.MyBestLap.Root,
            ),
            orderFlow.value,
        )
    }

    @Test
    fun `連続moveItemではRepository更新より最後のmoveItem結果を優先して表示する`() = runTest {
        val simulatorFlow = MutableStateFlow<Simulator?>(null)
        every { simulatorRepository.selectedSimulator() } returns simulatorFlow
        coEvery { simulatorRepository.saveSelectedSimulator(Simulator.LmuWindows) } answers {
            simulatorFlow.update { Simulator.LmuWindows }
        }
        every { readoutRepository.observeReadoutEnabledStates("lmu_windows") } returns
            MutableStateFlow(emptyMap())
        every { readoutRepository.observeReadoutOrder("lmu_windows") } returns MutableStateFlow(emptyList())
        coEvery { readoutRepository.saveReadoutOrder("lmu_windows", any()) } returns Unit
        every { queueRepository.observeQueueEnabledStates() } returns MutableStateFlow(emptyMap())
        val viewModel = createViewModel(simulatorRepository, readoutRepository, queueRepository)

        viewModel.onSimulatorSelected(Simulator.LmuWindows)
        viewModel.moveItem(0, 1) // [tyre_temperature, flag, vehicle_approach, vehicle_damage, my_best_lap]
        viewModel.moveItem(0, 1) // [flag, tyre_temperature, vehicle_approach, vehicle_damage, my_best_lap]（初期順序に戻る）

        assertEquals(
            listOf(
                ReadoutItemKey.LmuWindows.Flag.Root,
                ReadoutItemKey.LmuWindows.TyreTemperature.Root,
                ReadoutItemKey.LmuWindows.VehicleApproach.Root,
                ReadoutItemKey.LmuWindows.RemainingVirtualEnergyLaps.Root,
                ReadoutItemKey.LmuWindows.VehicleDamage.Root,
                ReadoutItemKey.LmuWindows.MyBestLap.Root,
            ),
            viewModel.uiState.first().items,
        )
    }

    @Test
    fun `onItemSelectedでアイテムが選択される`() = runTest {
        val simulatorFlow = MutableStateFlow<Simulator?>(null)
        every { simulatorRepository.selectedSimulator() } returns simulatorFlow
        coEvery { simulatorRepository.saveSelectedSimulator(Simulator.LmuWindows) } answers {
            simulatorFlow.update { Simulator.LmuWindows }
        }
        every { readoutRepository.observeReadoutEnabledStates("lmu_windows") } returns
            MutableStateFlow(emptyMap())
        every { readoutRepository.observeReadoutOrder("lmu_windows") } returns MutableStateFlow(emptyList())
        every { queueRepository.observeQueueEnabledStates() } returns MutableStateFlow(emptyMap())
        val viewModel = createViewModel(simulatorRepository, readoutRepository, queueRepository)

        viewModel.onSimulatorSelected(Simulator.LmuWindows)
        viewModel.onItemSelected(ReadoutItemKey.LmuWindows.VehicleApproach.Root)

        assertEquals(ReadoutListItemType.LmuWindows.VehicleApproach, viewModel.uiState.first().selectedItem)
    }

    @Test
    fun `シミュレータ未選択時はonItemSelectedで選択状態は変わらない`() = runTest {
        every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(null)
        every { queueRepository.observeQueueEnabledStates() } returns MutableStateFlow(emptyMap())
        val viewModel = createViewModel(simulatorRepository, readoutRepository, queueRepository)

        viewModel.onItemSelected(ReadoutItemKey.LmuWindows.VehicleApproach.Root)

        assertNull(viewModel.uiState.first().selectedItem)
    }

    @Test
    fun `シミュレータに属さないアイテムを選択しても選択状態は変わらない`() = runTest {
        val simulatorFlow = MutableStateFlow<Simulator?>(null)
        every { simulatorRepository.selectedSimulator() } returns simulatorFlow
        coEvery { simulatorRepository.saveSelectedSimulator(Simulator.LmuWindows) } answers {
            simulatorFlow.update { Simulator.LmuWindows }
        }
        every { readoutRepository.observeReadoutEnabledStates("lmu_windows") } returns
            MutableStateFlow(emptyMap())
        every { readoutRepository.observeReadoutOrder("lmu_windows") } returns MutableStateFlow(emptyList())
        every { queueRepository.observeQueueEnabledStates() } returns MutableStateFlow(emptyMap())
        val viewModel = createViewModel(simulatorRepository, readoutRepository, queueRepository)

        viewModel.onSimulatorSelected(Simulator.LmuWindows)
        viewModel.onItemSelected(ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root)

        assertNull(viewModel.uiState.first().selectedItem)
    }

    @Test
    fun `同じアイテムを再度選択すると選択解除される`() = runTest {
        val simulatorFlow = MutableStateFlow<Simulator?>(null)
        every { simulatorRepository.selectedSimulator() } returns simulatorFlow
        coEvery { simulatorRepository.saveSelectedSimulator(Simulator.LmuWindows) } answers {
            simulatorFlow.update { Simulator.LmuWindows }
        }
        every { readoutRepository.observeReadoutEnabledStates("lmu_windows") } returns
            MutableStateFlow(emptyMap())
        every { readoutRepository.observeReadoutOrder("lmu_windows") } returns MutableStateFlow(emptyList())
        every { queueRepository.observeQueueEnabledStates() } returns MutableStateFlow(emptyMap())
        val viewModel = createViewModel(simulatorRepository, readoutRepository, queueRepository)

        viewModel.onSimulatorSelected(Simulator.LmuWindows)
        viewModel.onItemSelected(ReadoutItemKey.LmuWindows.VehicleApproach.Root)
        viewModel.onItemSelected(ReadoutItemKey.LmuWindows.VehicleApproach.Root)

        assertNull(viewModel.uiState.first().selectedItem)
    }

    @Test
    fun `clearSelectedItemで選択状態が解除される`() = runTest {
        every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(null)
        every { queueRepository.observeQueueEnabledStates() } returns MutableStateFlow(emptyMap())
        val viewModel = createViewModel(simulatorRepository, readoutRepository, queueRepository)

        viewModel.onItemSelected(ReadoutItemKey.LmuWindows.VehicleApproach.Root)
        viewModel.clearSelectedItem()

        assertNull(viewModel.uiState.first().selectedItem)
    }

    @Test
    fun `gt7_ps5を選択するとGT7用の読み上げアイテムが表示される`() = runTest {
        val simulatorFlow = MutableStateFlow<Simulator?>(null)
        every { simulatorRepository.selectedSimulator() } returns simulatorFlow
        coEvery { simulatorRepository.saveSelectedSimulator(Simulator.Gt7Ps5) } answers {
            simulatorFlow.update { Simulator.Gt7Ps5 }
        }
        every { readoutRepository.observeReadoutEnabledStates("gt7_ps5") } returns MutableStateFlow(emptyMap())
        every { readoutRepository.observeReadoutOrder("gt7_ps5") } returns MutableStateFlow(emptyList())
        every { queueRepository.observeQueueEnabledStates() } returns MutableStateFlow(emptyMap())
        val viewModel = createViewModel(simulatorRepository, readoutRepository, queueRepository)

        viewModel.onSimulatorSelected(Simulator.Gt7Ps5)

        val state = viewModel.uiState.first()
        assertEquals(Simulator.Gt7Ps5, state.selectedSimulator)
        assertEquals(
            listOf(ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root, ReadoutItemKey.Gt7Ps5.MyBestLap.Root),
            state.items,
        )
    }

    @Test
    fun `gt7_ps5を選択すると共通Repositoryから燃料残り周回数の保存済みON_OFF状態が表示される`() = runTest {
        val simulatorFlow = MutableStateFlow<Simulator?>(null)
        every { simulatorRepository.selectedSimulator() } returns simulatorFlow
        coEvery { simulatorRepository.saveSelectedSimulator(Simulator.Gt7Ps5) } answers {
            simulatorFlow.update { Simulator.Gt7Ps5 }
        }
        every { readoutRepository.observeReadoutEnabledStates("gt7_ps5") } returns
            MutableStateFlow(mapOf(ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root to false))
        every { readoutRepository.observeReadoutOrder("gt7_ps5") } returns MutableStateFlow(emptyList())
        every { queueRepository.observeQueueEnabledStates() } returns MutableStateFlow(emptyMap())
        val viewModel = createViewModel(simulatorRepository, readoutRepository, queueRepository)

        viewModel.onSimulatorSelected(Simulator.Gt7Ps5)

        assertEquals(
            false,
            viewModel.uiState.first().readoutEnabledStates[ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root],
        )
    }

    @Test
    fun `ON_OFF状態はシミュレータと項目ごとに共通Repositoryへ保存される`() = runTest {
        val simulatorFlow = MutableStateFlow<Simulator?>(null)
        val gt7EnabledFlow = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
        val lmuEnabledFlow = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
        every { simulatorRepository.selectedSimulator() } returns simulatorFlow
        coEvery { simulatorRepository.saveSelectedSimulator(any()) } answers {
            simulatorFlow.update { firstArg() }
        }
        every { readoutRepository.observeReadoutEnabledStates("gt7_ps5") } returns gt7EnabledFlow
        every { readoutRepository.observeReadoutEnabledStates("lmu_windows") } returns lmuEnabledFlow
        every { readoutRepository.observeReadoutOrder("gt7_ps5") } returns MutableStateFlow(emptyList())
        every { readoutRepository.observeReadoutOrder("lmu_windows") } returns MutableStateFlow(emptyList())
        coEvery {
            readoutRepository.saveReadoutEnabledState("gt7_ps5", ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root, false)
        } answers {
            gt7EnabledFlow.update { it + (ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root to false) }
        }
        coEvery {
            readoutRepository.saveReadoutEnabledState("lmu_windows", ReadoutItemKey.LmuWindows.MyBestLap.Root, true)
        } answers {
            lmuEnabledFlow.update { it + (ReadoutItemKey.LmuWindows.MyBestLap.Root to true) }
        }
        every { queueRepository.observeQueueEnabledStates() } returns MutableStateFlow(emptyMap())
        val viewModel = createViewModel(simulatorRepository, readoutRepository, queueRepository)

        viewModel.onSimulatorSelected(Simulator.Gt7Ps5)
        viewModel.onReadoutEnabledChanged(ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root, false)

        assertEquals(false, gt7EnabledFlow.value[ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root])

        viewModel.onSimulatorSelected(Simulator.LmuWindows)
        viewModel.onReadoutEnabledChanged(ReadoutItemKey.LmuWindows.MyBestLap.Root, true)

        assertEquals(true, lmuEnabledFlow.value[ReadoutItemKey.LmuWindows.MyBestLap.Root])
    }
}
