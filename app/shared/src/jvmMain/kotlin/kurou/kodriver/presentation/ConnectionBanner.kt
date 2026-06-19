package kurou.kodriver.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kodriver.app.shared.generated.resources.Res
import kodriver.app.shared.generated.resources.banner_simulator_connected
import kodriver.app.shared.generated.resources.banner_simulator_disconnected
import kurou.kodriver.feature.lmuwindowsconnection.LmuWindowsConnectionStatus
import kurou.kodriver.feature.lmuwindowsconnection.LmuWindowsConnectionViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
actual fun rememberConnectionBannerUiState(): ConnectionBannerUiState {
    val viewModel: LmuWindowsConnectionViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val connectedMessage = stringResource(Res.string.banner_simulator_connected)
    val disconnectedMessage = stringResource(Res.string.banner_simulator_disconnected)
    val status = when (uiState.connectionStatus) {
        LmuWindowsConnectionStatus.CONNECTED -> ConnectionBannerStatus.CONNECTED
        LmuWindowsConnectionStatus.DISCONNECTED -> ConnectionBannerStatus.DISCONNECTED
        LmuWindowsConnectionStatus.UNCHECKED -> ConnectionBannerStatus.UNCHECKED
    }
    val message = when (uiState.connectionStatus) {
        LmuWindowsConnectionStatus.CONNECTED -> connectedMessage
        LmuWindowsConnectionStatus.DISCONNECTED,
        LmuWindowsConnectionStatus.UNCHECKED,
        -> disconnectedMessage
    }
    return ConnectionBannerUiState(
        status = status,
        message = message,
        iconType = ConnectionBannerIconType.SIMULATOR,
    )
}
