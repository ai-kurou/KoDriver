package kurou.kodriver.feature.otherlist

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.usecase.ResetManualReadoutSettingsUseCase

/** 手動タブの設定初期化の確認・実行・結果通知を管理する。実行のライフサイクルは呼び出し元に従う。 */
public class OtherListManualReadoutSettingsResetFlow(
    private val resetManualReadoutSettings: ResetManualReadoutSettingsUseCase,
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
                try {
                    resetManualReadoutSettings()
                    true
                } catch (e: CancellationException) {
                    throw e
                } catch (
                    @Suppress("TooGenericExceptionCaught") e: Exception,
                ) {
                    false
                }
            _uiState.update { it.copy(resetSucceeded = succeeded) }
        } finally {
            _uiState.update { it.copy(isResetting = false) }
        }
    }

    public fun consumeResetResult() {
        _uiState.update { it.copy(resetSucceeded = null) }
    }
}
