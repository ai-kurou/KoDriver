package kurou.kodriver.feature.telemetryloglist

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.TelemetryLog

internal data class TelemetryLogListDeleteState(
    val isDeleting: Boolean = false,
    val deleteSucceeded: Boolean? = null,
)

/** テレメトリログ一覧と画面内状態を合成する冷たい Flow。 */
internal class TelemetryLogListFlow(
    logs: Flow<List<TelemetryLog>>,
    selectedLogId: Flow<Long?>,
    deleteState: Flow<TelemetryLogListDeleteState>,
    pendingDeleteLogId: Flow<Long?>,
    selectedSimulator: Flow<Simulator>,
) {
    val uiState: Flow<TelemetryLogListUiState> =
        combine(
            logs,
            selectedLogId,
            deleteState,
            pendingDeleteLogId,
            selectedSimulator,
        ) { logs, selectedLogId, deleteState, pendingDeleteLogId, selectedSimulator ->
            TelemetryLogListUiState(
                logs = logs,
                selectedLogId = selectedLogId?.takeIf { selectedId -> logs.any { it.id == selectedId } },
                selectedSimulator = selectedSimulator,
                pendingDeleteLogId = pendingDeleteLogId,
                isDeleting = deleteState.isDeleting,
                deleteSucceeded = deleteState.deleteSucceeded,
            )
        }
}
