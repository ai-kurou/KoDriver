package kurou.kodriver.feature.otherlist

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.runCapturingErrorsOrNull
import kurou.kodriver.domain.usecase.ResetTelemetryLogDatabaseUseCase

/** ログ削除の確認・実行・結果通知を管理する。実行のライフサイクルは呼び出し元に従う。 */
public class OtherListResetFlow(
    private val resetTelemetryLogDatabase: ResetTelemetryLogDatabaseUseCase,
) {
    private val _uiState = MutableStateFlow(OtherListResetUiState())
    public val uiState: StateFlow<OtherListResetUiState> = _uiState.asStateFlow()

    public fun onResetClick() {
        if (_uiState.value.isResetting) return
        _uiState.update { it.copy(showResetConfirmDialog = true) }
    }

    public fun onResetDismiss() {
        _uiState.update { it.copy(showResetConfirmDialog = false) }
    }

    public suspend fun onResetConfirm() {
        if (!_uiState.value.showResetConfirmDialog || _uiState.value.isResetting) return
        _uiState.update { it.copy(showResetConfirmDialog = false, isResetting = true, resetSucceeded = null) }
        try {
            val succeeded =
                runCapturingErrorsOrNull {
                    resetTelemetryLogDatabase()
                    true
                } != null
            _uiState.update { it.copy(resetSucceeded = succeeded) }
        } finally {
            _uiState.update { it.copy(isResetting = false) }
        }
    }

    public fun consumeResetResult() {
        _uiState.update { it.copy(resetSucceeded = null) }
    }
}
