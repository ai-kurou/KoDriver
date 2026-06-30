package kurou.kodriver.feature.telemetrylogdetail

internal data class TelemetryLogDetailUiState(
    val logId: Long? = null,
    val items: List<Unit> = emptyList(),
)
