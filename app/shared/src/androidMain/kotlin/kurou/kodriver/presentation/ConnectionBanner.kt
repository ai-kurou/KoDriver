package kurou.kodriver.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kodriver.app.shared.generated.resources.Res
import kodriver.app.shared.generated.resources.banner_server_connected
import kodriver.app.shared.generated.resources.banner_server_disconnected
import kodriver.app.shared.generated.resources.banner_server_ip_not_configured
import kurou.kodriver.feature.serverconnection.ServerConnectionStatus
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
    val status = when (uiState.connectionStatus) {
        ServerConnectionStatus.CONNECTED -> ConnectionBannerStatus.CONNECTED
        ServerConnectionStatus.NOT_CONFIGURED, ServerConnectionStatus.DISCONNECTED ->
            ConnectionBannerStatus.DISCONNECTED
        ServerConnectionStatus.CHECKING -> ConnectionBannerStatus.UNCHECKED
    }
    val message = when (uiState.connectionStatus) {
        ServerConnectionStatus.NOT_CONFIGURED -> ipNotConfiguredMessage
        ServerConnectionStatus.CONNECTED -> connectedMessage
        ServerConnectionStatus.CHECKING, ServerConnectionStatus.DISCONNECTED ->
            disconnectedMessage
    }
    return ConnectionBannerUiState(
        status = status,
        message = message,
        iconType = ConnectionBannerIconType.NETWORK,
    )
}
