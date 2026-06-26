package kurou.kodriver.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kodriver.app.shared.generated.resources.Res
import kodriver.app.shared.generated.resources.banner_console_ip_not_configured
import kodriver.app.shared.generated.resources.banner_simulator_connected
import kodriver.app.shared.generated.resources.banner_simulator_disconnected
import kodriver.app.shared.generated.resources.gt7_connected
import kodriver.app.shared.generated.resources.gt7_disconnected
import kodriver.app.shared.generated.resources.lmu_connected
import kodriver.app.shared.generated.resources.lmu_disconnected
import kurou.kodriver.feature.main.ConnectionBannerViewModel
import kurou.kodriver.feature.main.ConnectionBannerVmStatus
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
actual fun rememberConnectionBannerUiState(): ConnectionBannerUiState {
    val viewModel: ConnectionBannerViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    if (!uiState.isSimulatorSelected) return ConnectionBannerUiState(isVisible = false)

    val isGt7 = uiState.isGt7Ps5
    val connectedMessage = stringResource(Res.string.banner_simulator_connected)
    val disconnectedMessage = stringResource(Res.string.banner_simulator_disconnected)
    val consoleIpNotConfiguredMessage = stringResource(Res.string.banner_console_ip_not_configured)
    val snackbarConnectedMessage = stringResource(
        if (isGt7) Res.string.gt7_connected else Res.string.lmu_connected,
    )
    val snackbarDisconnectedMessage = stringResource(
        if (isGt7) Res.string.gt7_disconnected else Res.string.lmu_disconnected,
    )
    val status = when (uiState.connectionStatus) {
        ConnectionBannerVmStatus.CONNECTED -> ConnectionBannerStatus.CONNECTED
        ConnectionBannerVmStatus.DISCONNECTED -> ConnectionBannerStatus.DISCONNECTED
        ConnectionBannerVmStatus.UNCHECKED,
        ConnectionBannerVmStatus.IP_NOT_CONFIGURED,
        -> ConnectionBannerStatus.UNCHECKED
    }
    val message = when (uiState.connectionStatus) {
        ConnectionBannerVmStatus.CONNECTED -> connectedMessage
        ConnectionBannerVmStatus.IP_NOT_CONFIGURED -> consoleIpNotConfiguredMessage
        ConnectionBannerVmStatus.DISCONNECTED,
        ConnectionBannerVmStatus.UNCHECKED,
        -> disconnectedMessage
    }
    val iconType = if (isGt7) ConnectionBannerIconType.NETWORK else ConnectionBannerIconType.SIMULATOR
    val isTappable = uiState.connectionStatus == ConnectionBannerVmStatus.IP_NOT_CONFIGURED
    val tapNavigationItemId = if (isTappable) "console_ip" else null
    return ConnectionBannerUiState(
        status = status,
        message = message,
        iconType = iconType,
        snackbarConnectedMessage = snackbarConnectedMessage,
        snackbarDisconnectedMessage = snackbarDisconnectedMessage,
        isTappable = isTappable,
        tapNavigationItemId = tapNavigationItemId,
    )
}
