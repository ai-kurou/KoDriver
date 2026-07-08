package kurou.kodriver.feature.telemetryloglist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kurou.kodriver.domain.model.TelemetryLog
import kurou.kodriver.domain.usecase.ObserveTelemetryLogsUseCase
import kurou.kodriver.domain.usecase.ResetTelemetryLogDatabaseUseCase

class TelemetryLogListViewModel(
    observeTelemetryLogs: ObserveTelemetryLogsUseCase,
    private val resetTelemetryLogDatabase: ResetTelemetryLogDatabaseUseCase,
) : ViewModel() {
    private val selectedLogId = MutableStateFlow<Long?>(null)
    private val resetState = MutableStateFlow(ResetState())
    private val showResetConfirmDialog = MutableStateFlow(false)

    val uiState: StateFlow<TelemetryLogListUiState> = observeTelemetryLogs()
        .map { logs ->
            logs.sortedWith(
                compareByDescending<TelemetryLog> { it.createdAt }
                    .thenByDescending { it.id },
            )
        }
        .combine(selectedLogId) { logs, selectedLogId ->
            logs to selectedLogId?.takeIf { selectedId -> logs.any { it.id == selectedId } }
        }
        .combine(resetState) { (logs, selectedLogId), resetState ->
            Triple(logs, selectedLogId, resetState)
        }
        .combine(showResetConfirmDialog) { (logs, selectedLogId, resetState), showResetConfirmDialog ->
            TelemetryLogListUiState(
                logs = logs,
                selectedLogId = selectedLogId,
                isResetting = resetState.isResetting,
                resetSucceeded = resetState.resetSucceeded,
                showResetConfirmDialog = showResetConfirmDialog,
            )
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            TelemetryLogListUiState(),
        )

    fun selectLog(id: Long) {
        selectedLogId.update { current -> if (current == id) null else id }
    }

    fun clearSelectedLog() {
        selectedLogId.update { null }
    }

    fun resetDatabase() {
        viewModelScope.launch {
            resetState.update { it.copy(isResetting = true, resetSucceeded = null) }
            val succeeded = try {
                resetTelemetryLogDatabase()
                true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                false
            }
            resetState.update { it.copy(isResetting = false, resetSucceeded = succeeded) }
        }
    }

    fun onResetClick() {
        showResetConfirmDialog.update { true }
    }

    fun onResetDismiss() {
        showResetConfirmDialog.update { false }
    }

    fun onResetConfirm() {
        showResetConfirmDialog.update { false }
        resetDatabase()
    }

    fun consumeResetResult() {
        resetState.update { it.copy(resetSucceeded = null) }
    }

    private data class ResetState(
        val isResetting: Boolean = false,
        val resetSucceeded: Boolean? = null,
    )
}
