package kurou.kodriver.feature.readout.vehicleapproach

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.usecase.ObserveLateralThresholdUseCase
import kurou.kodriver.domain.usecase.ObserveLongitudinalThresholdUseCase
import kurou.kodriver.domain.usecase.SaveLateralThresholdUseCase
import kurou.kodriver.domain.usecase.SaveLongitudinalThresholdUseCase
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class VehicleApproachViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var thresholdsRepository: FakeProximityThresholdsRepository
    private lateinit var viewModel: VehicleApproachViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        thresholdsRepository = FakeProximityThresholdsRepository()
        viewModel = VehicleApproachViewModel(
            observeLateralThreshold = ObserveLateralThresholdUseCase(thresholdsRepository),
            observeLongitudinalThreshold = ObserveLongitudinalThresholdUseCase(thresholdsRepository),
            saveLateralThreshold = SaveLateralThresholdUseCase(thresholdsRepository),
            saveLongitudinalThreshold = SaveLongitudinalThresholdUseCase(thresholdsRepository),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `初期状態はリポジトリのデフォルト値を反映した UiState を返す`() = runTest {
        assertEquals(
            VehicleApproachUiState(lateralThresholdMeters = 5.0, longitudinalThresholdMeters = 1.0),
            viewModel.uiState.first(),
        )
    }

    @Test
    fun `onLateralThresholdChanged を呼ぶと UiState の lateralThresholdMeters が更新される`() = runTest {
        viewModel.onLateralThresholdChanged(3.5)
        assertEquals(3.5, viewModel.uiState.first().lateralThresholdMeters)
    }

    @Test
    fun `onLongitudinalThresholdChanged を呼ぶと UiState の longitudinalThresholdMeters が更新される`() = runTest {
        viewModel.onLongitudinalThresholdChanged(15.0)
        assertEquals(15.0, viewModel.uiState.first().longitudinalThresholdMeters)
    }
}
