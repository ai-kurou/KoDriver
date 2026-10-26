package kurou.kodriver.feature.telemetryloglist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.DeleteTelemetryLogUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase

/**
 * TelemetryLogList 画面の状態管理とユーザー操作を扱う ViewModel。
 */
class TelemetryLogListViewModel internal constructor(
    observeSortedTelemetryLogs: ObserveSortedTelemetryLogsUseCase,
    observeSelectedSimulator: ObserveSelectedSimulatorUseCase,
    private val deleteTelemetryLog: DeleteTelemetryLogUseCase,
) : ViewModel() {
    private val _selectedLogId = MutableStateFlow<Long?>(null)
    private val _deleteState = MutableStateFlow(TelemetryLogListDeleteState())
    private val _pendingDeleteLogId = MutableStateFlow<Long?>(null)

    private var previousSimulator: Simulator? = null
    private val selectedSimulator =
        observeSelectedSimulator().distinctUntilChanged().onEach { simulator ->
            if (previousSimulator != null && previousSimulator != simulator) {
                _selectedLogId.update { null }
            }
            previousSimulator = simulator
        }

    val uiState: StateFlow<TelemetryLogListUiState> =
        TelemetryLogListFlow(
            logs = observeSortedTelemetryLogs(),
            selectedLogId = _selectedLogId,
            deleteState = _deleteState,
            pendingDeleteLogId = _pendingDeleteLogId,
            selectedSimulator = selectedSimulator,
        ).uiState.stateIn(
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

    fun onDeleteClick(id: Long) {
        _pendingDeleteLogId.update { id }
    }

    fun onDeleteDismiss() {
        _pendingDeleteLogId.update { null }
    }

    fun onDeleteConfirm() {
        val id = _pendingDeleteLogId.value ?: return
        _pendingDeleteLogId.update { null }
        deleteLog(id)
    }

    fun consumeDeleteResult() {
        _deleteState.update { it.copy(deleteSucceeded = null) }
    }

    private fun deleteLog(id: Long) {
        viewModelScope.launch {
            _deleteState.update { it.copy(isDeleting = true, deleteSucceeded = null) }
            val succeeded =
                try {
                    deleteTelemetryLog(id)
                    true
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    captureTelemetryLogListError(e)
                    false
                }
            _deleteState.update { it.copy(isDeleting = false, deleteSucceeded = succeeded) }
        }
    }
}
