package kurou.kodriver.feature.readoutlist

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.ReadoutItemKey
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

        viewModel.onSimulatorSelected("lmu_windows")

        val state = viewModel.uiState.first()
        assertEquals("lmu_windows", state.selectedSimulator)
        assertEquals(
            listOf(ReadoutItemKey.FLAG, ReadoutItemKey.VEHICLE_APPROACH, ReadoutItemKey.VEHICLE_DAMAGE),
            state.items,
        )
    }

    @Test
    fun `moveItemでアイテムの順序を変更できる`() = runTest {
        viewModel.onSimulatorSelected("lmu_windows")
        viewModel.moveItem(0, 1)

        assertEquals(
            listOf(ReadoutItemKey.VEHICLE_APPROACH, ReadoutItemKey.FLAG, ReadoutItemKey.VEHICLE_DAMAGE),
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
        viewModel.onSimulatorSelected("lmu_windows")
        viewModel.onReadoutEnabledChanged(ReadoutItemKey.VEHICLE_APPROACH, false)

        assertEquals(false, viewModel.uiState.first().readoutEnabledStates[ReadoutItemKey.VEHICLE_APPROACH])
    }

    @Test
    fun `シミュレータ未選択時はON_OFF状態を保存しない`() = runTest {
        viewModel.onReadoutEnabledChanged(ReadoutItemKey.VEHICLE_APPROACH, false)

        assertEquals(emptyMap(), readoutRepository.observeReadoutEnabledStates("lmu_windows").first())
    }

    @Test
    fun `シミュレータを選択するとRepositoryから永続化済みのON_OFF状態が読み込まれる`() = runTest {
        readoutRepository.saveReadoutEnabledState("lmu_windows", ReadoutItemKey.FLAG, false)

        viewModel.onSimulatorSelected("lmu_windows")

        assertEquals(false, viewModel.uiState.first().readoutEnabledStates[ReadoutItemKey.FLAG])
    }

    @Test
    fun `シミュレータを選択するとRepositoryから永続化済みの順序が読み込まれる`() = runTest {
        readoutRepository.saveReadoutOrder("lmu_windows", listOf(ReadoutItemKey.FLAG, ReadoutItemKey.VEHICLE_APPROACH))

        viewModel.onSimulatorSelected("lmu_windows")

        assertEquals(
            listOf(ReadoutItemKey.FLAG, ReadoutItemKey.VEHICLE_APPROACH, ReadoutItemKey.VEHICLE_DAMAGE),
            viewModel.uiState.first().items,
        )
    }

    @Test
    fun `moveItemで変更した順序がRepositoryに保存される`() = runTest {
        viewModel.onSimulatorSelected("lmu_windows")
        viewModel.moveItem(0, 1)

        assertEquals(
            listOf(ReadoutItemKey.VEHICLE_APPROACH, ReadoutItemKey.FLAG, ReadoutItemKey.VEHICLE_DAMAGE),
            readoutRepository.observeReadoutOrder("lmu_windows").first(),
        )
    }

    @Test
    fun `連続moveItemではRepository更新より最後のmoveItem結果を優先して表示する`() = runTest {
        viewModel.onSimulatorSelected("lmu_windows")
        viewModel.moveItem(0, 1) // [vehicle_approach, flag, vehicle_damage]
        viewModel.moveItem(0, 1) // [flag, vehicle_approach, vehicle_damage]（初期順序に戻る）

        assertEquals(
            listOf(ReadoutItemKey.FLAG, ReadoutItemKey.VEHICLE_APPROACH, ReadoutItemKey.VEHICLE_DAMAGE),
            viewModel.uiState.first().items,
        )
    }

    @Test
    fun `onItemSelectedでアイテムが選択される`() = runTest {
        viewModel.onItemSelected(ReadoutItemKey.VEHICLE_APPROACH)

        assertEquals(ReadoutListItemType.VehicleApproach, viewModel.uiState.first().selectedItem)
    }

    @Test
    fun `存在しないアイテムを選択しても選択状態は変わらない`() = runTest {
        viewModel.onItemSelected(ReadoutItemKey.BLUE_FLAG)

        assertNull(viewModel.uiState.first().selectedItem)
    }

    @Test
    fun `同じアイテムを再度選択すると選択解除される`() = runTest {
        viewModel.onItemSelected(ReadoutItemKey.VEHICLE_APPROACH)
        viewModel.onItemSelected(ReadoutItemKey.VEHICLE_APPROACH)

        assertNull(viewModel.uiState.first().selectedItem)
    }

    @Test
    fun `clearSelectedItemで選択状態が解除される`() = runTest {
        viewModel.onItemSelected(ReadoutItemKey.VEHICLE_APPROACH)
        viewModel.clearSelectedItem()

        assertNull(viewModel.uiState.first().selectedItem)
    }

    @Test
    fun `gt7_ps5を選択するとベストラップアイテムが表示される`() = runTest {
        viewModel.onSimulatorSelected("gt7_ps5")

        val state = viewModel.uiState.first()
        assertEquals("gt7_ps5", state.selectedSimulator)
        assertEquals(listOf(ReadoutItemKey.BEST_LAP), state.items)
    }
}
