package kurou.kodriver.presentation

import androidx.compose.runtime.Composable

enum class ConnectionBannerIconType { SIMULATOR, NETWORK }

data class ConnectionBannerUiState(
    val isConnected: Boolean = false,
    val isConnectionChecked: Boolean = false,
    val message: String = "",
    val iconType: ConnectionBannerIconType = ConnectionBannerIconType.SIMULATOR,
)

@Composable
expect fun rememberConnectionBannerUiState(): ConnectionBannerUiState
