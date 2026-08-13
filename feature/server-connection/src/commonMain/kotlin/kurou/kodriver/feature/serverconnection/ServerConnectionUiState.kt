package kurou.kodriver.feature.serverconnection

import kurou.kodriver.domain.model.Simulator

/**
 * ServerConnection の接続状態を表す表示用ステータス。
 */
enum class ServerConnectionStatus { NOT_CONFIGURED, CHECKING, CONNECTED, DISCONNECTED }

/**
 * ServerConnection 画面の表示状態。
 */
data class ServerConnectionUiState(
    val connectionStatus: ServerConnectionStatus = ServerConnectionStatus.NOT_CONFIGURED,
    val requiresKoDriverServer: Boolean = false,
    val selectedSimulator: Simulator? = null,
    val serverVersion: String? = null,
    val showVersionMismatchBottomSheet: Boolean = false,
    val appVersion: String = "",
) {
    val isConnected: Boolean get() = connectionStatus == ServerConnectionStatus.CONNECTED
    val isConnectionChecked: Boolean
        get() =
            connectionStatus != ServerConnectionStatus.NOT_CONFIGURED &&
                connectionStatus != ServerConnectionStatus.CHECKING
    val isIpConfigured: Boolean get() = connectionStatus != ServerConnectionStatus.NOT_CONFIGURED
}
