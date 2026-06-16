package kurou.kodriver.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kodriver.app.shared.generated.resources.Res
import kodriver.app.shared.generated.resources.banner_server_connected
import kodriver.app.shared.generated.resources.banner_server_disconnected
import kodriver.app.shared.generated.resources.banner_server_ip_not_configured
import kurou.kodriver.feature.serverconnection.ServerConnectionViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
actual fun rememberConnectionBannerUiState(): ConnectionBannerUiState {
    val viewModel: ServerConnectionViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val connectedMessage = stringResource(Res.string.banner_server_connected)
    val disconnectedMessage = stringResource(Res.string.banner_server_disconnected)
    val ipNotConfiguredMessage = stringResource(Res.string.banner_server_ip_not_configured)
    val ipNotConfigured = !uiState.isIpConfigured
    val isConnected = uiState.isConnectionChecked && uiState.isConnected
    val message = when {
        ipNotConfigured -> ipNotConfiguredMessage
        isConnected -> connectedMessage
        else -> disconnectedMessage
    }
    return ConnectionBannerUiState(
        isConnected = isConnected,
        isConnectionChecked = uiState.isConnectionChecked || ipNotConfigured,
        message = message,
        iconType = ConnectionBannerIconType.NETWORK,
    )
}
