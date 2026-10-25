package kurou.kodriver.feature.telemetryloglist

import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.TelemetryLog

/**
 * TelemetryLogList 画面の表示状態。
 */
data class TelemetryLogListUiState(
    val logs: List<TelemetryLog> = emptyList(),
    val selectedLogId: Long? = null,
    val selectedSimulator: Simulator? = null,
    val pendingDeleteLogId: Long? = null,
    val isDeleting: Boolean = false,
    val deleteSucceeded: Boolean? = null,
)
