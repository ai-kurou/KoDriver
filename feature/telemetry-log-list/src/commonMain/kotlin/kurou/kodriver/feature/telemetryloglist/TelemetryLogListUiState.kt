package kurou.kodriver.feature.telemetryloglist

import kurou.kodriver.domain.model.TelemetryLog

data class TelemetryLogListUiState(
    val logs: List<TelemetryLog> = emptyList(),
    val selectedLogId: Long? = null,
)
