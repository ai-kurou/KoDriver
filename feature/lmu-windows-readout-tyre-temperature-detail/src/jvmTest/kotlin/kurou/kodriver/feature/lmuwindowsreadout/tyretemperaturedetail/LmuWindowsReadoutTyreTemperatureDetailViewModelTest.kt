package kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsReadoutTyreTemperatureDetailViewModelTest {

    private val viewModel = LmuWindowsReadoutTyreTemperatureDetailViewModel()

    @Test
    fun `初期状態はデフォルトの UiState を返す`() = runTest {
        assertEquals(LmuWindowsReadoutTyreTemperatureDetailUiState(), viewModel.uiState.first())
    }
}
