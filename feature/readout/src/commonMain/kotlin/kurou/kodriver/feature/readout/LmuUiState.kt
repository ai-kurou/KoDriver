package kurou.kodriver.feature.readout

import kurou.kodriver.domain.model.LmuTelemetryData

sealed class LmuUiState {
    data object Connecting : LmuUiState()
    data class Connected(val data: LmuTelemetryData) : LmuUiState()
    data class Error(val message: String) : LmuUiState()
}
