package kurou.kodriver.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kodriver.app.shared.generated.resources.Res
import kodriver.app.shared.generated.resources.banner_console_ip_not_configured
import kodriver.app.shared.generated.resources.banner_server_connected
import kodriver.app.shared.generated.resources.banner_server_disconnected
import kodriver.app.shared.generated.resources.banner_server_ip_not_configured
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

private fun vmStatusToBannerStatus(vmStatus: ConnectionBannerVmStatus): ConnectionBannerStatus =
    when (vmStatus) {
        ConnectionBannerVmStatus.CONNECTED -> ConnectionBannerStatus.CONNECTED
        ConnectionBannerVmStatus.DISCONNECTED -> ConnectionBannerStatus.DISCONNECTED
        ConnectionBannerVmStatus.UNCHECKED,
        ConnectionBannerVmStatus.IP_NOT_CONFIGURED,
        -> ConnectionBannerStatus.UNCHECKED
    }

@Composable
actual fun rememberConnectionBannerUiState(): ConnectionBannerUiState {
    val viewModel: ConnectionBannerViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    if (!uiState.isSimulatorSelected) return ConnectionBannerUiState(isVisible = false)

    val isGt7 = uiState.isGt7Ps5

    val connectedMessage = stringResource(
        if (isGt7) Res.string.banner_simulator_connected else Res.string.banner_server_connected,
    )
    val disconnectedMessage = stringResource(
        if (isGt7) Res.string.banner_simulator_disconnected else Res.string.banner_server_disconnected,
    )
    val ipNotConfiguredMessage = stringResource(
        if (isGt7) Res.string.banner_console_ip_not_configured else Res.string.banner_server_ip_not_configured,
    )
    val snackbarConnectedMessage = stringResource(
        if (isGt7) Res.string.gt7_connected else Res.string.lmu_connected,
    )
    val snackbarDisconnectedMessage = stringResource(
        if (isGt7) Res.string.gt7_disconnected else Res.string.lmu_disconnected,
    )

    val vmStatus = uiState.connectionStatus
    val tapNavigationTarget = connectionBannerNavigationTarget(
        isGt7 = isGt7,
        supportsLmuServerIpNavigation = true,
    )
    val message = when (vmStatus) {
        ConnectionBannerVmStatus.CONNECTED -> connectedMessage
        ConnectionBannerVmStatus.IP_NOT_CONFIGURED -> ipNotConfiguredMessage
        ConnectionBannerVmStatus.DISCONNECTED,
        ConnectionBannerVmStatus.UNCHECKED,
        -> disconnectedMessage
    }
    return ConnectionBannerUiState(
        status = vmStatusToBannerStatus(vmStatus),
        message = message,
        iconType = ConnectionBannerIconType.NETWORK,
        snackbarConnectedMessage = snackbarConnectedMessage,
        snackbarDisconnectedMessage = snackbarDisconnectedMessage,
        isTappable = tapNavigationTarget != null,
        tapNavigationTarget = tapNavigationTarget,
    )
}
