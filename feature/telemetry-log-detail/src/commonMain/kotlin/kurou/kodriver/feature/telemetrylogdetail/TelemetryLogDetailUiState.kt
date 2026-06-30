package kurou.kodriver.feature.telemetrylogdetail

import kurou.kodriver.domain.model.TelemetryLogDetail

internal data class TelemetryLogDetailUiState(
    val logId: Long? = null,
    val items: List<TelemetryLogDetailItemUiState> = emptyList(),
)

internal data class TelemetryLogDetailItemUiState(
    val title: String,
    val telemetryJson: String,
)

internal fun TelemetryLogDetail?.toItems(): List<TelemetryLogDetailItemUiState> {
    if (this == null) return emptyList()
    return buildList {
        add(
            TelemetryLogDetailItemUiState(
                title = "選択したログ",
                telemetryJson = current.telemetryJson,
            ),
        )
        previous?.let { log ->
            add(
                TelemetryLogDetailItemUiState(
                    title = "一つ前のログ",
                    telemetryJson = log.telemetryJson,
                ),
            )
        }
    }
}
