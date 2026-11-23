package kurou.kodriver.feature.otherlist

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.runCapturingErrorsOrNull

/**
 * 確認ダイアログ付きの初期化・削除操作の確認・実行・結果通知を管理する。
 * 実際の処理は [reset] として渡され、用途ごとに別インスタンスを生成して使う。実行のライフサイクルは呼び出し元に従う。
 */
public class OtherListResetFlow(
    private val reset: suspend () -> Unit,
) {
    public val uiState: StateFlow<OtherListResetUiState>
        field = MutableStateFlow(OtherListResetUiState())

    public fun onResetClick() {
        if (uiState.value.isResetting) return
        uiState.update { it.copy(showResetConfirmDialog = true) }
    }

    public fun onResetDismiss() {
        uiState.update { it.copy(showResetConfirmDialog = false) }
    }

    public suspend fun onResetConfirm() {
        if (!uiState.value.showResetConfirmDialog || uiState.value.isResetting) return
        uiState.update { it.copy(showResetConfirmDialog = false, isResetting = true, resetSucceeded = null) }
        try {
            val succeeded =
                runCapturingErrorsOrNull {
                    reset()
                    true
                } != null
            uiState.update { it.copy(resetSucceeded = succeeded) }
        } finally {
            uiState.update { it.copy(isResetting = false) }
        }
    }

    public fun consumeResetResult() {
        uiState.update { it.copy(resetSucceeded = null) }
    }
}
