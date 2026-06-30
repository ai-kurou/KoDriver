package kurou.kodriver.feature.telemetrylogdetail

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class TelemetryLogDetailViewModel : ViewModel() {
    private val mutableUiState = MutableStateFlow(TelemetryLogDetailUiState())

    val uiState: StateFlow<TelemetryLogDetailUiState> = mutableUiState.asStateFlow()
}
