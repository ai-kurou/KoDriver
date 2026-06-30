package kurou.kodriver.feature.telemetrylogdetail

import kotlin.test.Test
import kotlin.test.assertEquals

class TelemetryLogDetailViewModelTest {
    @Test
    fun `uiStateの初期値は空の項目を持つ`() {
        val viewModel = TelemetryLogDetailViewModel()

        assertEquals(TelemetryLogDetailUiState(), viewModel.uiState.value)
    }

    @Test
    fun `setLogIdでログIDを保持する`() {
        val viewModel = TelemetryLogDetailViewModel()

        viewModel.setLogId(10)

        assertEquals(TelemetryLogDetailUiState(logId = 10), viewModel.uiState.value)
    }
}
