package kurou.kodriver.feature.readout

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveSelectedSimulatorUseCase
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class ReadoutViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var simulatorRepository: FakeSimulatorPreferencesRepository
    private lateinit var readoutRepository: FakeReadoutPreferencesRepository
    private lateinit var viewModel: ReadoutViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        simulatorRepository = FakeSimulatorPreferencesRepository()
        readoutRepository = FakeReadoutPreferencesRepository()
        viewModel = ReadoutViewModel(
            observeSelectedSimulator = ObserveSelectedSimulatorUseCase(simulatorRepository),
            saveSelectedSimulator = SaveSelectedSimulatorUseCase(simulatorRepository),
            observeReadoutEnabledStates = ObserveReadoutEnabledStatesUseCase(readoutRepository),
            saveReadoutEnabledState = SaveReadoutEnabledStateUseCase(readoutRepository),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `初期状態はシミュレータ未選択でアイテムが空`() = runTest {
        val state = viewModel.uiState.first()

        assertNull(state.selectedSimulator)
        assertEquals(emptyList(), state.items)
    }

    @Test
    fun `シミュレータを選択するとアイテムが読み込まれる`() = runTest {
        viewModel.onSimulatorSelected("lmu")

        val state = viewModel.uiState.first()
        assertEquals("lmu", state.selectedSimulator)
        assertEquals(listOf("車両接近", "残りラップ数"), state.items)
    }

    @Test
    fun `moveItemUpで先頭以外のアイテムを上に移動できる`() = runTest {
        viewModel.onSimulatorSelected("lmu")
        viewModel.moveItemUp(1)

        assertEquals(listOf("残りラップ数", "車両接近"), viewModel.uiState.first().items)
    }

    @Test
    fun `moveItemDownで末尾以外のアイテムを下に移動できる`() = runTest {
        viewModel.onSimulatorSelected("lmu")
        viewModel.moveItemDown(0)

        assertEquals(listOf("残りラップ数", "車両接近"), viewModel.uiState.first().items)
    }

    @Test
    fun `onReadoutEnabledChangedでON_OFF状態がRepositoryに保存される`() = runTest {
        viewModel.onSimulatorSelected("lmu")
        viewModel.onReadoutEnabledChanged("車両接近", false)

        assertEquals(false, viewModel.uiState.first().readoutEnabledStates["車両接近"])
    }

    @Test
    fun `シミュレータを選択するとRepositoryから永続化済みのON_OFF状態が読み込まれる`() = runTest {
        readoutRepository.saveReadoutEnabledState("lmu", "残りラップ数", false)

        viewModel.onSimulatorSelected("lmu")

        assertEquals(false, viewModel.uiState.first().readoutEnabledStates["残りラップ数"])
    }
}
