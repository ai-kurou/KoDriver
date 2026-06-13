package kurou.kodriver.feature.readoutlist

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
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

        viewModel.onSimulatorSelected("lmu")

        val state = viewModel.uiState.first()
        assertEquals("lmu", state.selectedSimulator)
        assertEquals(listOf("flag", "vehicle_approach", "vehicle_damage"), state.items)
    }

    @Test
    fun `moveItemでアイテムの順序を変更できる`() = runTest {
        viewModel.onSimulatorSelected("lmu")
        viewModel.moveItem(0, 1)

        assertEquals(listOf("vehicle_approach", "flag", "vehicle_damage"), viewModel.uiState.first().items)
    }

    @Test
    fun `シミュレータ未選択時はmoveItemで順序を保存しない`() = runTest {
        viewModel.moveItem(0, 1)

        assertEquals(emptyList(), readoutRepository.observeReadoutOrder("lmu").first())
    }

    @Test
    fun `onReadoutEnabledChangedでON_OFF状態がRepositoryに保存される`() = runTest {
        viewModel.onSimulatorSelected("lmu")
        viewModel.onReadoutEnabledChanged("vehicle_approach", false)

        assertEquals(false, viewModel.uiState.first().readoutEnabledStates["vehicle_approach"])
    }

    @Test
    fun `シミュレータ未選択時はON_OFF状態を保存しない`() = runTest {
        viewModel.onReadoutEnabledChanged("vehicle_approach", false)

        assertEquals(emptyMap(), readoutRepository.observeReadoutEnabledStates("lmu").first())
    }

    @Test
    fun `シミュレータを選択するとRepositoryから永続化済みのON_OFF状態が読み込まれる`() = runTest {
        readoutRepository.saveReadoutEnabledState("lmu", "flag", false)

        viewModel.onSimulatorSelected("lmu")

        assertEquals(false, viewModel.uiState.first().readoutEnabledStates["flag"])
    }

    @Test
    fun `シミュレータを選択するとRepositoryから永続化済みの順序が読み込まれる`() = runTest {
        readoutRepository.saveReadoutOrder("lmu", listOf("flag", "vehicle_approach"))

        viewModel.onSimulatorSelected("lmu")

        assertEquals(listOf("flag", "vehicle_approach", "vehicle_damage"), viewModel.uiState.first().items)
    }

    @Test
    fun `moveItemで変更した順序がRepositoryに保存される`() = runTest {
        viewModel.onSimulatorSelected("lmu")
        viewModel.moveItem(0, 1)

        assertEquals(
            listOf("vehicle_approach", "flag", "vehicle_damage"),
            readoutRepository.observeReadoutOrder("lmu").first(),
        )
    }

    @Test
    fun `連続moveItemではRepository更新より最後のmoveItem結果を優先して表示する`() = runTest {
        viewModel.onSimulatorSelected("lmu")
        viewModel.moveItem(0, 1) // [vehicle_approach, flag, vehicle_damage]
        viewModel.moveItem(0, 1) // [flag, vehicle_approach, vehicle_damage]（初期順序に戻る）

        assertEquals(
            listOf("flag", "vehicle_approach", "vehicle_damage"),
            viewModel.uiState.first().items,
        )
    }

    @Test
    fun `onItemSelectedでアイテムが選択される`() = runTest {
        viewModel.onItemSelected("vehicle_approach")

        assertEquals(ReadoutListItemType.VehicleApproach, viewModel.uiState.first().selectedItem)
    }

    @Test
    fun `存在しないアイテムを選択しても選択状態は変わらない`() = runTest {
        viewModel.onItemSelected("unknown")

        assertNull(viewModel.uiState.first().selectedItem)
    }

    @Test
    fun `同じアイテムを再度選択すると選択解除される`() = runTest {
        viewModel.onItemSelected("vehicle_approach")
        viewModel.onItemSelected("vehicle_approach")

        assertNull(viewModel.uiState.first().selectedItem)
    }

    @Test
    fun `clearSelectedItemで選択状態が解除される`() = runTest {
        viewModel.onItemSelected("vehicle_approach")
        viewModel.clearSelectedItem()

        assertNull(viewModel.uiState.first().selectedItem)
    }
}
