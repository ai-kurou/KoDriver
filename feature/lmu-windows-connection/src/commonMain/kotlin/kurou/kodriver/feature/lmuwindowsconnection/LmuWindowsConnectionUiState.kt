package kurou.kodriver.feature.lmuwindowsconnection

internal data class LmuWindowsConnectionUiState(
    val connectionStatus: LmuWindowsConnectionStatus = LmuWindowsConnectionStatus.UNCHECKED,
) {
    val isConnected: Boolean get() = connectionStatus == LmuWindowsConnectionStatus.CONNECTED
    val isConnectionChecked: Boolean get() = connectionStatus != LmuWindowsConnectionStatus.UNCHECKED
}

/**
 * LmuWindowsConnection の接続状態を表す表示用ステータス。
 */
enum class LmuWindowsConnectionStatus {
    UNCHECKED,
    CONNECTED,
    DISCONNECTED,
}
