package kurou.kodriver.feature.readoutlist

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelLapsEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5RemainingFuelLapsEnabledUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.SaveSelectedSimulatorUseCase
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class ReadoutListViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var simulatorRepository: FakeSimulatorPreferencesRepository
    private lateinit var readoutRepository: FakeReadoutPreferencesRepository
    private lateinit var gt7Ps5RemainingFuelLapsRepository: FakeGt7Ps5RemainingFuelLapsEnabledRepository
    private lateinit var viewModel: ReadoutListViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        simulatorRepository = FakeSimulatorPreferencesRepository()
        readoutRepository = FakeReadoutPreferencesRepository()
        gt7Ps5RemainingFuelLapsRepository = FakeGt7Ps5RemainingFuelLapsEnabledRepository()
        viewModel = ReadoutListViewModel(
            observeSelectedSimulator = ObserveSelectedSimulatorUseCase(simulatorRepository),
            saveSelectedSimulator = SaveSelectedSimulatorUseCase(simulatorRepository),
            observeReadoutEnabledStates = ObserveReadoutEnabledStatesUseCase(readoutRepository),
            saveReadoutEnabledState = SaveReadoutEnabledStateUseCase(readoutRepository),
            observeGt7Ps5RemainingFuelLapsEnabled = ObserveGt7Ps5RemainingFuelLapsEnabledUseCase(
                gt7Ps5RemainingFuelLapsRepository,
            ),
            saveGt7Ps5RemainingFuelLapsEnabled = SaveGt7Ps5RemainingFuelLapsEnabledUseCase(
                gt7Ps5RemainingFuelLapsRepository,
            ),
            observeReadoutOrder = ObserveReadoutOrderUseCase(readoutRepository),
            saveReadoutOrder = SaveReadoutOrderUseCase(readoutRepository),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `シミュレータ未選択時はアイテムが空で選択後に読み込まれる`() = runTest {
        assertNull(viewModel.uiState.first().selectedSimulator)
        assertEquals(emptyList(), viewModel.uiState.first().items)

        viewModel.onSimulatorSelected(Simulator.LmuWindows)

        val state = viewModel.uiState.first()
        assertEquals(Simulator.LmuWindows, state.selectedSimulator)
        assertEquals(
            listOf(ReadoutItemKey.Flag, ReadoutItemKey.VehicleApproach, ReadoutItemKey.VehicleDamage),
            state.items,
        )
    }

    @Test
    fun `moveItemでアイテムの順序を変更できる`() = runTest {
        viewModel.onSimulatorSelected(Simulator.LmuWindows)
        viewModel.moveItem(0, 1)

        assertEquals(
            listOf(ReadoutItemKey.VehicleApproach, ReadoutItemKey.Flag, ReadoutItemKey.VehicleDamage),
            viewModel.uiState.first().items,
        )
    }

    @Test
    fun `シミュレータ未選択時はmoveItemで順序を保存しない`() = runTest {
        viewModel.moveItem(0, 1)

        assertEquals(emptyList(), readoutRepository.observeReadoutOrder("lmu_windows").first())
    }

    @Test
    fun `onReadoutEnabledChangedでON_OFF状態がRepositoryに保存される`() = runTest {
        viewModel.onSimulatorSelected(Simulator.LmuWindows)
        viewModel.onReadoutEnabledChanged(ReadoutItemKey.VehicleApproach, false)

        assertEquals(false, viewModel.uiState.first().readoutEnabledStates[ReadoutItemKey.VehicleApproach])
    }

    @Test
    fun `シミュレータ未選択時はON_OFF状態を保存しない`() = runTest {
        viewModel.onReadoutEnabledChanged(ReadoutItemKey.VehicleApproach, false)

        assertEquals(emptyMap(), readoutRepository.observeReadoutEnabledStates("lmu_windows").first())
    }

    @Test
    fun `シミュレータを選択するとRepositoryから永続化済みのON_OFF状態が読み込まれる`() = runTest {
        readoutRepository.saveReadoutEnabledState("lmu_windows", ReadoutItemKey.Flag, false)

        viewModel.onSimulatorSelected(Simulator.LmuWindows)

        assertEquals(false, viewModel.uiState.first().readoutEnabledStates[ReadoutItemKey.Flag])
    }

    @Test
    fun `シミュレータを選択するとRepositoryから永続化済みの順序が読み込まれる`() = runTest {
        readoutRepository.saveReadoutOrder("lmu_windows", listOf(ReadoutItemKey.Flag, ReadoutItemKey.VehicleApproach))

        viewModel.onSimulatorSelected(Simulator.LmuWindows)

        assertEquals(
            listOf(ReadoutItemKey.Flag, ReadoutItemKey.VehicleApproach, ReadoutItemKey.VehicleDamage),
            viewModel.uiState.first().items,
        )
    }

    @Test
    fun `moveItemで変更した順序がRepositoryに保存される`() = runTest {
        viewModel.onSimulatorSelected(Simulator.LmuWindows)
        viewModel.moveItem(0, 1)

        assertEquals(
            listOf(ReadoutItemKey.VehicleApproach, ReadoutItemKey.Flag, ReadoutItemKey.VehicleDamage),
            readoutRepository.observeReadoutOrder("lmu_windows").first(),
        )
    }

    @Test
    fun `連続moveItemではRepository更新より最後のmoveItem結果を優先して表示する`() = runTest {
        viewModel.onSimulatorSelected(Simulator.LmuWindows)
        viewModel.moveItem(0, 1) // [vehicle_approach, flag, vehicle_damage]
        viewModel.moveItem(0, 1) // [flag, vehicle_approach, vehicle_damage]（初期順序に戻る）

        assertEquals(
            listOf(ReadoutItemKey.Flag, ReadoutItemKey.VehicleApproach, ReadoutItemKey.VehicleDamage),
            viewModel.uiState.first().items,
        )
    }

    @Test
    fun `onItemSelectedでアイテムが選択される`() = runTest {
        viewModel.onSimulatorSelected(Simulator.LmuWindows)
        viewModel.onItemSelected(ReadoutItemKey.VehicleApproach)

        assertEquals(ReadoutListItemType.LmuWindows.VehicleApproach, viewModel.uiState.first().selectedItem)
    }

    @Test
    fun `シミュレータ未選択時はonItemSelectedで選択状態は変わらない`() = runTest {
        viewModel.onItemSelected(ReadoutItemKey.VehicleApproach)

        assertNull(viewModel.uiState.first().selectedItem)
    }

    @Test
    fun `シミュレータに属さないアイテムを選択しても選択状態は変わらない`() = runTest {
        viewModel.onSimulatorSelected(Simulator.LmuWindows)
        viewModel.onItemSelected(ReadoutItemKey.MyBestLap)

        assertNull(viewModel.uiState.first().selectedItem)
    }

    @Test
    fun `同じアイテムを再度選択すると選択解除される`() = runTest {
        viewModel.onSimulatorSelected(Simulator.LmuWindows)
        viewModel.onItemSelected(ReadoutItemKey.VehicleApproach)
        viewModel.onItemSelected(ReadoutItemKey.VehicleApproach)

        assertNull(viewModel.uiState.first().selectedItem)
    }

    @Test
    fun `clearSelectedItemで選択状態が解除される`() = runTest {
        viewModel.onItemSelected(ReadoutItemKey.VehicleApproach)
        viewModel.clearSelectedItem()

        assertNull(viewModel.uiState.first().selectedItem)
    }

    @Test
    fun `gt7_ps5を選択するとGT7用の読み上げアイテムが表示される`() = runTest {
        viewModel.onSimulatorSelected(Simulator.Gt7Ps5)

        val state = viewModel.uiState.first()
        assertEquals(Simulator.Gt7Ps5, state.selectedSimulator)
        assertEquals(
            listOf(ReadoutItemKey.RemainingFuelLaps, ReadoutItemKey.MyBestLap),
            state.items,
        )
    }

    @Test
    fun `gt7_ps5を選択すると燃料残り周回数の保存済みON_OFF状態が表示される`() = runTest {
        gt7Ps5RemainingFuelLapsRepository.saveEnabled(false)

        viewModel.onSimulatorSelected(Simulator.Gt7Ps5)

        assertEquals(false, viewModel.uiState.first().readoutEnabledStates[ReadoutItemKey.RemainingFuelLaps])
    }

    @Test
    fun `gt7_ps5の燃料残り周回数のON_OFF状態を変更すると専用設定に保存される`() = runTest {
        viewModel.onSimulatorSelected(Simulator.Gt7Ps5)

        viewModel.onReadoutEnabledChanged(ReadoutItemKey.RemainingFuelLaps, false)

        assertEquals(false, gt7Ps5RemainingFuelLapsRepository.observeEnabled().first())
    }
}
