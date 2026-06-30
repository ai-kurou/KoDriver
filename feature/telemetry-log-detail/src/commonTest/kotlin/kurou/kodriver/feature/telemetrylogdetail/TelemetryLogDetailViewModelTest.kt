package kurou.kodriver.feature.telemetrylogdetail

import kotlin.test.Test
import kotlin.test.assertEquals

class TelemetryLogDetailViewModelTest {
    @Test
    fun `uiStateの初期値は空の項目を持つ`() {
        val viewModel = TelemetryLogDetailViewModel()

        assertEquals(TelemetryLogDetailUiState(), viewModel.uiState.value)
    }
}
