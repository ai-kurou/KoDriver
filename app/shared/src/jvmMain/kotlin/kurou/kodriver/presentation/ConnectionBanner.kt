package kurou.kodriver.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kodriver.app.shared.generated.resources.Res
import kodriver.app.shared.generated.resources.banner_lmu_connected
import kodriver.app.shared.generated.resources.banner_lmu_disconnected
import kurou.kodriver.feature.lmuconnection.LmuConnectionViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
actual fun rememberConnectionBannerUiState(): ConnectionBannerUiState {
    val viewModel: LmuConnectionViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val connectedMessage = stringResource(Res.string.banner_lmu_connected)
    val disconnectedMessage = stringResource(Res.string.banner_lmu_disconnected)
    return ConnectionBannerUiState(
        isConnected = uiState.isConnected,
        isConnectionChecked = uiState.isConnectionChecked,
        message = if (uiState.isConnected) connectedMessage else disconnectedMessage,
    )
}
