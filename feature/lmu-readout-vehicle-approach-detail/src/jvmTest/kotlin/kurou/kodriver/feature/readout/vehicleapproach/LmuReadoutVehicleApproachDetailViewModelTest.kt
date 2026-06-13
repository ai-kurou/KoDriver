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
import kurou.kodriver.domain.usecase.ObserveSkipFirstLapUseCase
import kurou.kodriver.domain.usecase.SaveLateralThresholdUseCase
import kurou.kodriver.domain.usecase.SaveLongitudinalThresholdUseCase
import kurou.kodriver.domain.usecase.SaveSkipFirstLapUseCase
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuReadoutVehicleApproachDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var thresholdsRepository: FakeProximityThresholdsRepository
    private lateinit var vehicleApproachPreferencesRepository: FakeVehicleApproachPreferencesRepository
    private lateinit var viewModel: LmuReadoutVehicleApproachDetailViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        thresholdsRepository = FakeProximityThresholdsRepository()
        vehicleApproachPreferencesRepository = FakeVehicleApproachPreferencesRepository()
        viewModel = LmuReadoutVehicleApproachDetailViewModel(
            observeLateralThreshold = ObserveLateralThresholdUseCase(thresholdsRepository),
            observeLongitudinalThreshold = ObserveLongitudinalThresholdUseCase(thresholdsRepository),
            observeSkipFirstLap = ObserveSkipFirstLapUseCase(vehicleApproachPreferencesRepository),
            saveLateralThreshold = SaveLateralThresholdUseCase(thresholdsRepository),
            saveLongitudinalThreshold = SaveLongitudinalThresholdUseCase(thresholdsRepository),
            saveSkipFirstLap = SaveSkipFirstLapUseCase(vehicleApproachPreferencesRepository),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `初期状態はリポジトリのデフォルト値を反映した UiState を返す`() = runTest {
        assertEquals(
            LmuReadoutVehicleApproachDetailUiState(
                lateralThresholdMeters = 5.0,
                longitudinalThresholdMeters = 1.0,
                skipFirstLap = true,
            ),
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

    @Test
    fun `onSkipFirstLapChanged を呼ぶと UiState の skipFirstLap が更新される`() = runTest {
        viewModel.onSkipFirstLapChanged(true)
        assertEquals(true, viewModel.uiState.first().skipFirstLap)
    }
}
