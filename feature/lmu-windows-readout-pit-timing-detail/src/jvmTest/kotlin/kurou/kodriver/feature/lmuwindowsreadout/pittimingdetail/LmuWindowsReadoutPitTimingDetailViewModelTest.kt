package kurou.kodriver.feature.lmuwindowsreadout.pittimingdetail

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsReadoutPitTimingDetailViewModelTest {

    @Test
    fun `初期状態はスイッチがONのUiStateを返す`() = runTest {
        val viewModel = LmuWindowsReadoutPitTimingDetailViewModel()

        assertEquals(LmuWindowsReadoutPitTimingDetailUiState(), viewModel.uiState.value)
    }

    @Test
    fun `onVirtualEnergyEnabledChangedを呼ぶとvirtualEnergyEnabledが更新される`() = runTest {
        val viewModel = LmuWindowsReadoutPitTimingDetailViewModel()

        viewModel.onVirtualEnergyEnabledChanged(false)

        assertEquals(false, viewModel.uiState.value.virtualEnergyEnabled)
    }
}
