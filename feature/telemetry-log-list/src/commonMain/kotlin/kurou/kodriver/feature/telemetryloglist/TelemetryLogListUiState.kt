package kurou.kodriver.feature.telemetryloglist

import kurou.kodriver.core.model.TelemetryLog

/**
 * TelemetryLogList 画面の表示状態。
 */
data class TelemetryLogListUiState(
    val logs: List<TelemetryLog> = emptyList(),
    val selectedLogId: Long? = null,
    val isResetting: Boolean = false,
    val resetSucceeded: Boolean? = null,
    val showResetConfirmDialog: Boolean = false,
    val pendingDeleteLogId: Long? = null,
    val isDeleting: Boolean = false,
    val deleteSucceeded: Boolean? = null,
)
