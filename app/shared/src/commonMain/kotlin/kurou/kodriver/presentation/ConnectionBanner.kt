package kurou.kodriver.presentation

import androidx.compose.runtime.Composable

enum class ConnectionBannerIconType { SIMULATOR, NETWORK }

enum class ConnectionBannerStatus { UNCHECKED, CONNECTED, DISCONNECTED }

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

sealed interface ConnectionBannerNavigationTarget {
    data object ConsoleIp : ConnectionBannerNavigationTarget
    data object ServerIp : ConnectionBannerNavigationTarget
}

@Composable
expect fun rememberConnectionBannerUiState(): ConnectionBannerUiState
