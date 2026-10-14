package kurou.kodriver.feature.telemetrylogdetail

internal data class TelemetryLogDetailUiState(
    val logId: Long? = null,
    val items: List<TelemetryLogDetailItemUiState> = emptyList(),
)

internal data class TelemetryLogDetailItemUiState(
    val kind: TelemetryLogDetailItemKind,
    val telemetryJson: String,
)

internal enum class TelemetryLogDetailItemKind {
    SELECTED,
    PREVIOUS,
}
