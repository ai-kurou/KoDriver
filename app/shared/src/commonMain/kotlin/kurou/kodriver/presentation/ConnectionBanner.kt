package kurou.kodriver.presentation

import androidx.compose.runtime.Composable

enum class ConnectionBannerIconType { SIMULATOR, NETWORK }

enum class ConnectionBannerStatus { UNCHECKED, CONNECTED, DISCONNECTED }

data class ConnectionBannerUiState(
    val status: ConnectionBannerStatus = ConnectionBannerStatus.UNCHECKED,
    val message: String = "",
    val iconType: ConnectionBannerIconType = ConnectionBannerIconType.SIMULATOR,
) {
    val isConnected: Boolean get() = status == ConnectionBannerStatus.CONNECTED
    val isConnectionChecked: Boolean get() = status != ConnectionBannerStatus.UNCHECKED
}

@Composable
expect fun rememberConnectionBannerUiState(): ConnectionBannerUiState
