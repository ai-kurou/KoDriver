package kurou.kodriver.feature.lmuwindowsreadout.raindetail

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsReadoutRainDetailViewModelTest {
    @Test
    fun `初期状態はstartReadoutEnabledがtrueのUiStateを返す`() =
        runTest {
            val viewModel = LmuWindowsReadoutRainDetailViewModel()

            assertEquals(
                LmuWindowsReadoutRainDetailUiState(startReadoutEnabled = true),
                viewModel.uiState.first(),
            )
        }

    @Test
    fun `onStartReadoutEnabledChangedを呼ぶとuiStateのstartReadoutEnabledが更新される`() =
        runTest {
            val viewModel = LmuWindowsReadoutRainDetailViewModel()

            viewModel.onStartReadoutEnabledChanged(false)

            assertEquals(false, viewModel.uiState.first().startReadoutEnabled)
        }
}
