package kurou.kodriver.presentation

import kurou.kodriver.domain.model.TelemetryData

sealed class TelemetryUiState {
    data object Connecting : TelemetryUiState()
    data class Connected(val data: TelemetryData) : TelemetryUiState()
    data class Error(val message: String) : TelemetryUiState()
}
