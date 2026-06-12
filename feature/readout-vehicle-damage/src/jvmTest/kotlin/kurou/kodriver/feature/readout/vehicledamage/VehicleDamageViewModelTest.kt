@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.readout.vehicledamage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.usecase.ObserveVehicleDamageEnabledStatesUseCase
import kurou.kodriver.domain.usecase.SaveVehicleDamageEnabledStateUseCase
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class VehicleDamageViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: FakeVehicleDamagePreferencesRepository
    private lateinit var viewModel: VehicleDamageViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeVehicleDamagePreferencesRepository()
        viewModel = VehicleDamageViewModel(
            observeEnabledStates = ObserveVehicleDamageEnabledStatesUseCase(repository),
            saveEnabledState = SaveVehicleDamageEnabledStateUseCase(repository),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `初期状態はリポジトリが空のとき overheatEnabled がデフォルト値 true の UiState を返す`() = runTest {
        assertEquals(VehicleDamageUiState(overheatEnabled = true), viewModel.uiState.first())
    }

    @Test
    fun `リポジトリに overheat=false が保存済みのとき overheatEnabled が false の UiState を返す`() = runTest {
        val repo = FakeVehicleDamagePreferencesRepository(
            initialStates = mapOf(ReadoutItemKey.OVERHEAT to false),
        )
        val vm = VehicleDamageViewModel(
            observeEnabledStates = ObserveVehicleDamageEnabledStatesUseCase(repo),
            saveEnabledState = SaveVehicleDamageEnabledStateUseCase(repo),
        )

        assertEquals(VehicleDamageUiState(overheatEnabled = false), vm.uiState.first())
    }

    @Test
    fun `onOverheatEnabledChanged を呼ぶと UiState の overheatEnabled が更新される`() = runTest {
        viewModel.onOverheatEnabledChanged(false)
        assertEquals(false, viewModel.uiState.first().overheatEnabled)

        viewModel.onOverheatEnabledChanged(true)
        assertEquals(true, viewModel.uiState.first().overheatEnabled)
    }
}
