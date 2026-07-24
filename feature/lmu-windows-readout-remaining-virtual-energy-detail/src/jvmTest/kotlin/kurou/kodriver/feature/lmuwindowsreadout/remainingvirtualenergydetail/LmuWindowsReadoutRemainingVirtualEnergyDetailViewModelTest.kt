package kurou.kodriver.feature.lmuwindowsreadout.remainingvirtualenergydetail

import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsReadoutRemainingVirtualEnergyDetailViewModelTest {

    @Test
    fun `uiStateは空の状態を返す`() {
        val viewModel = LmuWindowsReadoutRemainingVirtualEnergyDetailViewModel()

        assertEquals(
            LmuWindowsReadoutRemainingVirtualEnergyDetailUiState,
            viewModel.uiState.value,
        )
    }
}
