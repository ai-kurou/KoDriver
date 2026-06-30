package kurou.kodriver.feature.telemetrylogdetail

internal data class TelemetryLogDetailUiState(
    val logId: Long? = null,
    val items: List<TelemetryLogDetailItemUiState> = emptyList(),
)

internal data class TelemetryLogDetailItemUiState(
    val title: String,
    val telemetryJson: String,
)
