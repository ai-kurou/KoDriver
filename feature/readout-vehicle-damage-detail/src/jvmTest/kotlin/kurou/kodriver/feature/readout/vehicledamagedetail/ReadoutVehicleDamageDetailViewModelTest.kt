@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.readout.vehicledamagedetail

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
class ReadoutVehicleDamageDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: FakeReadoutVehicleDamageDetailPreferencesRepository
    private lateinit var viewModel: ReadoutVehicleDamageDetailViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeReadoutVehicleDamageDetailPreferencesRepository()
        viewModel = ReadoutVehicleDamageDetailViewModel(
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
        assertEquals(ReadoutVehicleDamageDetailUiState(overheatEnabled = true), viewModel.uiState.first())
    }

    @Test
    fun `リポジトリに overheat=false が保存済みのとき overheatEnabled が false の UiState を返す`() = runTest {
        val repo = FakeReadoutVehicleDamageDetailPreferencesRepository(
            initialStates = mapOf(ReadoutItemKey.OVERHEAT to false),
        )
        val vm = ReadoutVehicleDamageDetailViewModel(
            observeEnabledStates = ObserveVehicleDamageEnabledStatesUseCase(repo),
            saveEnabledState = SaveVehicleDamageEnabledStateUseCase(repo),
        )

        assertEquals(ReadoutVehicleDamageDetailUiState(overheatEnabled = false), vm.uiState.first())
    }

    @Test
    fun `onOverheatEnabledChanged を呼ぶと UiState の overheatEnabled が更新される`() = runTest {
        viewModel.onOverheatEnabledChanged(false)
        assertEquals(false, viewModel.uiState.first().overheatEnabled)

        viewModel.onOverheatEnabledChanged(true)
        assertEquals(true, viewModel.uiState.first().overheatEnabled)
    }
}
