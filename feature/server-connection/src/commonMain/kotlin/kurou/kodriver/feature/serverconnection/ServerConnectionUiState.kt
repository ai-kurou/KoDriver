package kurou.kodriver.feature.serverconnection

import kurou.kodriver.domain.model.SELECTED_SIMULATOR_DEFAULT
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.KoDriverServerConnectionStatus

/**
 * ServerConnection 画面の表示状態。
 */
data class ServerConnectionUiState(
    val connectionStatus: KoDriverServerConnectionStatus = KoDriverServerConnectionStatus.NOT_CONFIGURED,
    val requiresKoDriverServer: Boolean = SELECTED_SIMULATOR_DEFAULT.requiresKoDriverServer,
    val selectedSimulator: Simulator = SELECTED_SIMULATOR_DEFAULT,
    val serverVersion: String? = null,
    val showVersionMismatchBottomSheet: Boolean = false,
    val appVersion: String = "",
) {
    val isConnected: Boolean get() = connectionStatus == KoDriverServerConnectionStatus.CONNECTED
    val isConnectionChecked: Boolean
        get() =
            connectionStatus != KoDriverServerConnectionStatus.NOT_CONFIGURED &&
                connectionStatus != KoDriverServerConnectionStatus.CHECKING
}
