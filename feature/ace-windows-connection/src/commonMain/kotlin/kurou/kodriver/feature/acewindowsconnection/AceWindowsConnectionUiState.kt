package kurou.kodriver.feature.acewindowsconnection

internal data class AceWindowsConnectionUiState(
    val connectionStatus: AceWindowsConnectionStatus = AceWindowsConnectionStatus.UNCHECKED,
    val fuelRemainingPercent: Double? = null,
) {
    val isConnected: Boolean get() = connectionStatus == AceWindowsConnectionStatus.CONNECTED
    val isConnectionChecked: Boolean get() = connectionStatus != AceWindowsConnectionStatus.UNCHECKED
}

/**
 * AceWindowsConnection の接続状態を表す表示用ステータス。
 */
enum class AceWindowsConnectionStatus {
    UNCHECKED,
    CONNECTED,
    DISCONNECTED,
}
