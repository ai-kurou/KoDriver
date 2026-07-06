package kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsTyreTemperatureHighThresholdUseCase
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsReadoutTyreTemperatureDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: FakeLmuWindowsTyreTemperaturePreferencesRepository
    private lateinit var viewModel: LmuWindowsReadoutTyreTemperatureDetailViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeLmuWindowsTyreTemperaturePreferencesRepository()
        viewModel = LmuWindowsReadoutTyreTemperatureDetailViewModel(
            observeHighThreshold = ObserveLmuWindowsTyreTemperatureHighThresholdUseCase(repository),
            saveHighThreshold = SaveLmuWindowsTyreTemperatureHighThresholdUseCase(repository),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `初期状態はリポジトリのデフォルト値を反映したUiStateを返す`() = runTest {
        assertEquals(
            LmuWindowsReadoutTyreTemperatureDetailUiState(highThresholdCelsius = 90),
            viewModel.uiState.first(),
        )
    }

    @Test
    fun `onHighThresholdChangedを呼ぶとuiStateのhighThresholdCelsiusが更新される`() = runTest {
        viewModel.onHighThresholdChanged(100)
        assertEquals(100, viewModel.uiState.first().highThresholdCelsius)
    }

    @Test
    fun `onHighThresholdResetを呼ぶとhighThresholdCelsiusがデフォルト値90に戻る`() = runTest {
        viewModel.onHighThresholdChanged(100)
        viewModel.onHighThresholdReset()
        assertEquals(90, viewModel.uiState.first().highThresholdCelsius)
    }
}
