package kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsReadoutTyreTemperatureDetailViewModelTest {

    private val viewModel = LmuWindowsReadoutTyreTemperatureDetailViewModel()

    @Test
    fun `初期状態は高温閾値90°CのUiStateを返す`() = runTest {
        val expected = LmuWindowsReadoutTyreTemperatureDetailUiState(highThresholdCelsius = 90)
        assertEquals(expected, viewModel.uiState.first())
    }

    @Test
    fun `高温閾値を変更するとuiStateに反映される`() = runTest {
        viewModel.onHighThresholdChanged(100)
        assertEquals(100, viewModel.uiState.first().highThresholdCelsius)
    }

    @Test
    fun `高温閾値をリセットするとデフォルト値90°Cに戻る`() = runTest {
        viewModel.onHighThresholdChanged(100)
        viewModel.onHighThresholdReset()
        assertEquals(90, viewModel.uiState.first().highThresholdCelsius)
    }
}
