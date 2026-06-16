package kurou.kodriver.presentation

import androidx.compose.runtime.Composable

data class ConnectionBannerUiState(
    val isConnected: Boolean = false,
    val isConnectionChecked: Boolean = false,
)

@Composable
expect fun rememberConnectionBannerUiState(): ConnectionBannerUiState
