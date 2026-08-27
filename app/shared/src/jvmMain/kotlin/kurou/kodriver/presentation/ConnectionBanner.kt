package kurou.kodriver.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kurou.kodriver.app.shared.generated.resources.Res
import kurou.kodriver.app.shared.generated.resources.banner_console_ip_not_configured
import kurou.kodriver.app.shared.generated.resources.banner_simulator_connected
import kurou.kodriver.app.shared.generated.resources.banner_simulator_disconnected
import kurou.kodriver.feature.main.ConnectionBannerViewModel
import kurou.kodriver.feature.main.ConnectionBannerVmStatus
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * rememberConnectionBannerUiState のこのプラットフォーム向け実装。
 */
@Composable
actual fun rememberConnectionBannerUiState(): ConnectionBannerUiState {
    val viewModel: ConnectionBannerViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()

    val isGt7 = uiState.isGt7Ps5
    val isAceWindows = uiState.isAceWindows
    val connectedMessage = stringResource(Res.string.banner_simulator_connected)
    val disconnectedMessage = stringResource(Res.string.banner_simulator_disconnected)
    val consoleIpNotConfiguredMessage = stringResource(Res.string.banner_console_ip_not_configured)
    val snackbarConnectedMessage =
        stringResource(
            connectionBannerSnackbarConnectedMessageRes(isGt7, isAceWindows),
        )
    val snackbarDisconnectedMessage =
        stringResource(
            connectionBannerSnackbarDisconnectedMessageRes(isGt7, isAceWindows),
        )
    val status =
        when (uiState.connectionStatus) {
            ConnectionBannerVmStatus.CONNECTED -> ConnectionBannerStatus.CONNECTED

            ConnectionBannerVmStatus.DISCONNECTED -> ConnectionBannerStatus.DISCONNECTED

            ConnectionBannerVmStatus.UNCHECKED,
            ConnectionBannerVmStatus.IP_NOT_CONFIGURED,
            -> ConnectionBannerStatus.UNCHECKED
        }
    val message =
        when (uiState.connectionStatus) {
            ConnectionBannerVmStatus.CONNECTED -> connectedMessage

            ConnectionBannerVmStatus.IP_NOT_CONFIGURED -> consoleIpNotConfiguredMessage

            ConnectionBannerVmStatus.DISCONNECTED,
            ConnectionBannerVmStatus.UNCHECKED,
            -> disconnectedMessage
        }
    val iconType = if (isGt7) ConnectionBannerIconType.NETWORK else ConnectionBannerIconType.SIMULATOR
    val tapNavigationTarget =
        connectionBannerNavigationTarget(
            isGt7 = isGt7,
            supportsLmuServerIpNavigation = false,
        )
    return ConnectionBannerUiState(
        status = status,
        message = message,
        iconType = iconType,
        snackbarConnectedMessage = snackbarConnectedMessage,
        snackbarDisconnectedMessage = snackbarDisconnectedMessage,
        isTappable = tapNavigationTarget != null,
        tapNavigationTarget = tapNavigationTarget,
    )
}
