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
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
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
    private lateinit var viewModel: ReadoutListViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        simulatorRepository = FakeSimulatorPreferencesRepository()
        readoutRepository = FakeReadoutPreferencesRepository()
        viewModel = ReadoutListViewModel(
            observeSelectedSimulator = ObserveSelectedSimulatorUseCase(simulatorRepository),
            saveSelectedSimulator = SaveSelectedSimulatorUseCase(simulatorRepository),
            observeReadoutEnabledStates = ObserveReadoutEnabledStatesUseCase(readoutRepository),
            saveReadoutEnabledState = SaveReadoutEnabledStateUseCase(readoutRepository),
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
            listOf(
                ReadoutItemKey.LmuWindows.Flag.Root,
                ReadoutItemKey.LmuWindows.VehicleApproach,
                ReadoutItemKey.LmuWindows.VehicleDamage.Root,
                ReadoutItemKey.LmuWindows.TyreTemperature,
                ReadoutItemKey.LmuWindows.MyBestLap,
            ),
            state.items,
        )
        assertEquals(false, state.readoutEnabledStates[ReadoutItemKey.LmuWindows.TyreTemperature])
        assertEquals(false, state.readoutEnabledStates[ReadoutItemKey.LmuWindows.MyBestLap])
    }

    @Test
    fun `moveItemでアイテムの順序を変更できる`() = runTest {
        viewModel.onSimulatorSelected(Simulator.LmuWindows)
        viewModel.moveItem(0, 1)

        assertEquals(
            listOf(
                ReadoutItemKey.LmuWindows.VehicleApproach,
                ReadoutItemKey.LmuWindows.Flag.Root,
                ReadoutItemKey.LmuWindows.VehicleDamage.Root,
                ReadoutItemKey.LmuWindows.TyreTemperature,
                ReadoutItemKey.LmuWindows.MyBestLap,
            ),
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
        viewModel.onReadoutEnabledChanged(ReadoutItemKey.LmuWindows.VehicleApproach, false)

        assertEquals(false, viewModel.uiState.first().readoutEnabledStates[ReadoutItemKey.LmuWindows.VehicleApproach])
    }

    @Test
    fun `シミュレータ未選択時はON_OFF状態を保存しない`() = runTest {
        viewModel.onReadoutEnabledChanged(ReadoutItemKey.LmuWindows.VehicleApproach, false)

        assertEquals(emptyMap(), readoutRepository.observeReadoutEnabledStates("lmu_windows").first())
    }

    @Test
    fun `シミュレータを選択するとRepositoryから永続化済みのON_OFF状態が読み込まれる`() = runTest {
        readoutRepository.saveReadoutEnabledState("lmu_windows", ReadoutItemKey.LmuWindows.Flag.Root, false)

        viewModel.onSimulatorSelected(Simulator.LmuWindows)

        assertEquals(false, viewModel.uiState.first().readoutEnabledStates[ReadoutItemKey.LmuWindows.Flag.Root])
    }

    @Test
    fun `シミュレータを選択するとRepositoryから永続化済みの順序が読み込まれる`() = runTest {
        readoutRepository.saveReadoutOrder(
            "lmu_windows",
            listOf(ReadoutItemKey.LmuWindows.Flag.Root, ReadoutItemKey.LmuWindows.VehicleApproach),
        )

        viewModel.onSimulatorSelected(Simulator.LmuWindows)

        assertEquals(
            listOf(
                ReadoutItemKey.LmuWindows.Flag.Root,
                ReadoutItemKey.LmuWindows.VehicleApproach,
                ReadoutItemKey.LmuWindows.VehicleDamage.Root,
                ReadoutItemKey.LmuWindows.TyreTemperature,
                ReadoutItemKey.LmuWindows.MyBestLap,
            ),
            viewModel.uiState.first().items,
        )
    }

    @Test
    fun `moveItemで変更した順序がRepositoryに保存される`() = runTest {
        viewModel.onSimulatorSelected(Simulator.LmuWindows)
        viewModel.moveItem(0, 1)

        assertEquals(
            listOf(
                ReadoutItemKey.LmuWindows.VehicleApproach,
                ReadoutItemKey.LmuWindows.Flag.Root,
                ReadoutItemKey.LmuWindows.VehicleDamage.Root,
                ReadoutItemKey.LmuWindows.TyreTemperature,
                ReadoutItemKey.LmuWindows.MyBestLap,
            ),
            readoutRepository.observeReadoutOrder("lmu_windows").first(),
        )
    }

    @Test
    fun `連続moveItemではRepository更新より最後のmoveItem結果を優先して表示する`() = runTest {
        viewModel.onSimulatorSelected(Simulator.LmuWindows)
        viewModel.moveItem(0, 1) // [vehicle_approach, flag, vehicle_damage]
        viewModel.moveItem(0, 1) // [flag, vehicle_approach, vehicle_damage]（初期順序に戻る）

        assertEquals(
            listOf(
                ReadoutItemKey.LmuWindows.Flag.Root,
                ReadoutItemKey.LmuWindows.VehicleApproach,
                ReadoutItemKey.LmuWindows.VehicleDamage.Root,
                ReadoutItemKey.LmuWindows.TyreTemperature,
                ReadoutItemKey.LmuWindows.MyBestLap,
            ),
            viewModel.uiState.first().items,
        )
    }

    @Test
    fun `onItemSelectedでアイテムが選択される`() = runTest {
        viewModel.onSimulatorSelected(Simulator.LmuWindows)
        viewModel.onItemSelected(ReadoutItemKey.LmuWindows.VehicleApproach)

        assertEquals(ReadoutListItemType.LmuWindows.VehicleApproach, viewModel.uiState.first().selectedItem)
    }

    @Test
    fun `シミュレータ未選択時はonItemSelectedで選択状態は変わらない`() = runTest {
        viewModel.onItemSelected(ReadoutItemKey.LmuWindows.VehicleApproach)

        assertNull(viewModel.uiState.first().selectedItem)
    }

    @Test
    fun `シミュレータに属さないアイテムを選択しても選択状態は変わらない`() = runTest {
        viewModel.onSimulatorSelected(Simulator.LmuWindows)
        viewModel.onItemSelected(ReadoutItemKey.Gt7Ps5.RemainingFuelLaps)

        assertNull(viewModel.uiState.first().selectedItem)
    }

    @Test
    fun `同じアイテムを再度選択すると選択解除される`() = runTest {
        viewModel.onSimulatorSelected(Simulator.LmuWindows)
        viewModel.onItemSelected(ReadoutItemKey.LmuWindows.VehicleApproach)
        viewModel.onItemSelected(ReadoutItemKey.LmuWindows.VehicleApproach)

        assertNull(viewModel.uiState.first().selectedItem)
    }

    @Test
    fun `clearSelectedItemで選択状態が解除される`() = runTest {
        viewModel.onItemSelected(ReadoutItemKey.LmuWindows.VehicleApproach)
        viewModel.clearSelectedItem()

        assertNull(viewModel.uiState.first().selectedItem)
    }

    @Test
    fun `gt7_ps5を選択するとGT7用の読み上げアイテムが表示される`() = runTest {
        viewModel.onSimulatorSelected(Simulator.Gt7Ps5)

        val state = viewModel.uiState.first()
        assertEquals(Simulator.Gt7Ps5, state.selectedSimulator)
        assertEquals(
            listOf(ReadoutItemKey.Gt7Ps5.RemainingFuelLaps, ReadoutItemKey.Gt7Ps5.MyBestLap),
            state.items,
        )
    }

    @Test
    fun `gt7_ps5を選択すると共通Repositoryから燃料残り周回数の保存済みON_OFF状態が表示される`() = runTest {
        readoutRepository.saveReadoutEnabledState("gt7_ps5", ReadoutItemKey.Gt7Ps5.RemainingFuelLaps, false)

        viewModel.onSimulatorSelected(Simulator.Gt7Ps5)

        assertEquals(false, viewModel.uiState.first().readoutEnabledStates[ReadoutItemKey.Gt7Ps5.RemainingFuelLaps])
    }

    @Test
    fun `ON_OFF状態はシミュレータと項目ごとに共通Repositoryへ保存される`() = runTest {
        viewModel.onSimulatorSelected(Simulator.Gt7Ps5)

        viewModel.onReadoutEnabledChanged(ReadoutItemKey.Gt7Ps5.RemainingFuelLaps, false)

        assertEquals(
            false,
            readoutRepository.observeReadoutEnabledStates("gt7_ps5").first()[ReadoutItemKey.Gt7Ps5.RemainingFuelLaps],
        )

        viewModel.onSimulatorSelected(Simulator.LmuWindows)

        viewModel.onReadoutEnabledChanged(ReadoutItemKey.LmuWindows.MyBestLap, true)

        assertEquals(
            true,
            readoutRepository.observeReadoutEnabledStates("lmu_windows").first()[ReadoutItemKey.LmuWindows.MyBestLap],
        )
    }
}
