package kurou.kodriver.presentation

import androidx.compose.runtime.Composable

/**
 * ConnectionBanner のアイコン種別。
 */
enum class ConnectionBannerIconType { SIMULATOR, NETWORK }

/**
 * ConnectionBanner の接続状態を表す表示用ステータス。
 */
enum class ConnectionBannerStatus { UNCHECKED, CONNECTED, DISCONNECTED }

/**
 * ConnectionBanner 画面の表示状態。
 */
data class ConnectionBannerUiState(
    val status: ConnectionBannerStatus = ConnectionBannerStatus.UNCHECKED,
    val message: String = "",
    val iconType: ConnectionBannerIconType = ConnectionBannerIconType.SIMULATOR,
    val snackbarConnectedMessage: String = "",
    val snackbarDisconnectedMessage: String = "",
    val isVisible: Boolean = true,
    val isTappable: Boolean = false,
    val tapNavigationTarget: ConnectionBannerNavigationTarget? = null,
) {
    val isConnected: Boolean get() = status == ConnectionBannerStatus.CONNECTED
    val isConnectionChecked: Boolean get() = status != ConnectionBannerStatus.UNCHECKED
}

/**
 * ConnectionBanner の遷移先を表す。
 */
sealed interface ConnectionBannerNavigationTarget {
    data object ConsoleIp : ConnectionBannerNavigationTarget

    data object ServerIp : ConnectionBannerNavigationTarget
}

/**
 * rememberConnectionBannerUiState のプラットフォーム別実装を要求する expect 宣言。
 */
@Composable
expect fun rememberConnectionBannerUiState(): ConnectionBannerUiState
