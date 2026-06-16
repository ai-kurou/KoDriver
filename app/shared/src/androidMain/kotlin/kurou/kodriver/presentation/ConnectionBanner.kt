package kurou.kodriver.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kodriver.app.shared.generated.resources.Res
import kodriver.app.shared.generated.resources.banner_server_connected
import kodriver.app.shared.generated.resources.banner_server_disconnected
import kurou.kodriver.feature.serverconnection.ServerConnectionViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
actual fun rememberConnectionBannerUiState(): ConnectionBannerUiState {
    val viewModel: ServerConnectionViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val connectedMessage = stringResource(Res.string.banner_server_connected)
    val disconnectedMessage = stringResource(Res.string.banner_server_disconnected)
    return ConnectionBannerUiState(
        isConnected = uiState.isConnected,
        isConnectionChecked = uiState.isConnectionChecked,
        message = if (uiState.isConnected) connectedMessage else disconnectedMessage,
        iconType = ConnectionBannerIconType.NETWORK,
    )
}
