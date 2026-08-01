package kurou.kodriver.feature.telemetryloglist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kurou.kodriver.domain.usecase.ResetTelemetryLogDatabaseUseCase

/**
 * TelemetryLogList 画面の状態管理とユーザー操作を扱う ViewModel。
 */
class TelemetryLogListViewModel internal constructor(
    observeSortedTelemetryLogs: ObserveSortedTelemetryLogsUseCase,
    private val resetTelemetryLogDatabase: ResetTelemetryLogDatabaseUseCase,
) : ViewModel() {
    private val _selectedLogId = MutableStateFlow<Long?>(null)
    private val _resetState = MutableStateFlow(ResetState())
    private val _showResetConfirmDialog = MutableStateFlow(false)

    val uiState: StateFlow<TelemetryLogListUiState> = combine(
        observeSortedTelemetryLogs(),
        _selectedLogId,
        _resetState,
        _showResetConfirmDialog,
    ) { logs, selectedLogId, resetState, showResetConfirmDialog ->
        TelemetryLogListUiState(
            logs = logs,
            selectedLogId = selectedLogId?.takeIf { selectedId -> logs.any { it.id == selectedId } },
            isResetting = resetState.isResetting,
            resetSucceeded = resetState.resetSucceeded,
            showResetConfirmDialog = showResetConfirmDialog,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        TelemetryLogListUiState(),
    )

    fun selectLog(id: Long) {
        _selectedLogId.update { current -> if (current == id) null else id }
    }

    fun clearSelectedLog() {
        _selectedLogId.update { null }
    }

    fun resetDatabase() {
        viewModelScope.launch {
            _resetState.update { it.copy(isResetting = true, resetSucceeded = null) }
            val succeeded = try {
                resetTelemetryLogDatabase()
                true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                false
            }
            _resetState.update { it.copy(isResetting = false, resetSucceeded = succeeded) }
        }
    }

    fun onResetClick() {
        _showResetConfirmDialog.update { true }
    }

    fun onResetDismiss() {
        _showResetConfirmDialog.update { false }
    }

    fun onResetConfirm() {
        _showResetConfirmDialog.update { false }
        resetDatabase()
    }

    fun consumeResetResult() {
        _resetState.update { it.copy(resetSucceeded = null) }
    }

    private data class ResetState(
        val isResetting: Boolean = false,
        val resetSucceeded: Boolean? = null,
    )
}
