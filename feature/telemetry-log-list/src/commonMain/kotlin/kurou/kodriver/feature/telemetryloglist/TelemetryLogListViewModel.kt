package kurou.kodriver.feature.telemetryloglist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.model.TelemetryLog
import kurou.kodriver.domain.usecase.ObserveTelemetryLogsUseCase

class TelemetryLogListViewModel(
    observeTelemetryLogs: ObserveTelemetryLogsUseCase,
) : ViewModel() {
    private val selectedLogId = MutableStateFlow<Long?>(null)

    val uiState: StateFlow<TelemetryLogListUiState> = observeTelemetryLogs()
        .map { logs ->
            logs.sortedWith(
                compareByDescending<TelemetryLog> { it.createdAt }
                    .thenByDescending { it.id },
            )
        }
        .combine(selectedLogId) { logs, selectedLogId ->
            TelemetryLogListUiState(
                logs = logs,
                selectedLogId = selectedLogId?.takeIf { selectedId -> logs.any { it.id == selectedId } },
            )
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            TelemetryLogListUiState(),
        )

    fun selectLog(id: Long) {
        selectedLogId.update { id }
    }

    fun clearSelectedLog() {
        selectedLogId.update { null }
    }
}
