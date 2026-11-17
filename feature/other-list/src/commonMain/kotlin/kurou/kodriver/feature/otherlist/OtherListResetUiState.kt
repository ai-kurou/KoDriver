package kurou.kodriver.feature.otherlist

/** ログ削除フローの表示状態。 */
public data class OtherListResetUiState(
    val showResetConfirmDialog: Boolean = false,
    val isResetting: Boolean = false,
    val resetSucceeded: Boolean? = null,
)
