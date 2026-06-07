package kurou.kodriver.feature.readout.vehicleapproach

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.ProximityData
import kurou.kodriver.domain.usecase.ObserveProximityUseCase
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class VehicleApproachViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: FakeProximityRepository
    private lateinit var viewModel: VehicleApproachViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeProximityRepository()
        viewModel = VehicleApproachViewModel(ObserveProximityUseCase(repository))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `初期状態はデフォルトの UiState を返す`() = runTest {
        assertEquals(VehicleApproachUiState(), viewModel.uiState.first())
    }

    @Test
    fun `ProximityData を受信しても UiState は変化しない`() = runTest {
        val before = viewModel.uiState.first()
        repository.emit(
            ProximityData(
                sideBySideLeftVehicleIds = setOf(1),
                sideBySideRightVehicleIds = setOf(2),
                lateralDistanceLeftMeters = 1.5,
                lateralDistanceRightMeters = 2.0,
            ),
        )
        assertEquals(before, viewModel.uiState.first())
    }
}
