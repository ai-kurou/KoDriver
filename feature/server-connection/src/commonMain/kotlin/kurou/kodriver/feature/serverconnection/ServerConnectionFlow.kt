package kurou.kodriver.feature.serverconnection

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kurou.kodriver.domain.usecase.KoDriverServerConnectionState
import kurou.kodriver.domain.usecase.KoDriverServerConnectionStatus

/** 接続状態とボトムシートの表示フラグから表示状態を合成する冷たい Flow。 */
internal class ServerConnectionFlow(
    connectionState: Flow<KoDriverServerConnectionState>,
    showVersionMismatchBottomSheet: Flow<Boolean>,
    appVersion: String,
) {
    val uiState: Flow<ServerConnectionUiState> =
        combine(connectionState, showVersionMismatchBottomSheet) { state, showBottomSheet ->
            ServerConnectionUiState(
                connectionStatus = state.connectionStatus.toUiStatus(),
                requiresKoDriverServer = state.requiresKoDriverServer,
                selectedSimulator = state.selectedSimulator,
                serverVersion = state.serverVersion,
                showVersionMismatchBottomSheet = showBottomSheet,
                appVersion = appVersion,
            )
        }
}

private fun KoDriverServerConnectionStatus.toUiStatus(): ServerConnectionStatus =
    when (this) {
        KoDriverServerConnectionStatus.NOT_CONFIGURED -> ServerConnectionStatus.NOT_CONFIGURED
        KoDriverServerConnectionStatus.CHECKING -> ServerConnectionStatus.CHECKING
        KoDriverServerConnectionStatus.CONNECTED -> ServerConnectionStatus.CONNECTED
        KoDriverServerConnectionStatus.DISCONNECTED -> ServerConnectionStatus.DISCONNECTED
    }
