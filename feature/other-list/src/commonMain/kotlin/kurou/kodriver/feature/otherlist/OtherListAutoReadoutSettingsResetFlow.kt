package kurou.kodriver.feature.otherlist

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.usecase.ResetAutoReadoutSettingsUseCase

/** 自動タブの設定初期化の確認・実行・結果通知を管理する。実行のライフサイクルは呼び出し元に従う。 */
class OtherListAutoReadoutSettingsResetFlow(
    private val resetAutoReadoutSettings: ResetAutoReadoutSettingsUseCase,
) {
    private val _uiState = MutableStateFlow(OtherListResetUiState())
    val uiState: StateFlow<OtherListResetUiState> = _uiState.asStateFlow()

    fun onResetClick() {
        if (_uiState.value.isResetting) return
        _uiState.update { it.copy(showResetConfirmDialog = true) }
    }

    fun onResetDismiss() {
        _uiState.update { it.copy(showResetConfirmDialog = false) }
    }

    suspend fun onResetConfirm() {
        if (!_uiState.value.showResetConfirmDialog || _uiState.value.isResetting) return
        _uiState.update { it.copy(showResetConfirmDialog = false, isResetting = true, resetSucceeded = null) }
        try {
            val succeeded =
                try {
                    resetAutoReadoutSettings()
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

    fun consumeResetResult() {
        _uiState.update { it.copy(resetSucceeded = null) }
    }
}
