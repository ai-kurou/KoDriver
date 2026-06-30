package kurou.kodriver.feature.telemetryloglist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kurou.kodriver.domain.model.TelemetryLog
import kurou.kodriver.domain.usecase.ObserveTelemetryLogsUseCase

class TelemetryLogListViewModel(
    observeTelemetryLogs: ObserveTelemetryLogsUseCase,
) : ViewModel() {
    val uiState: StateFlow<TelemetryLogListUiState> = observeTelemetryLogs()
        .map { logs ->
            TelemetryLogListUiState(
                logs = logs.sortedWith(
                    compareByDescending<TelemetryLog> { it.createdAt }
                        .thenByDescending { it.id },
                ),
            )
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            TelemetryLogListUiState(),
        )
}
