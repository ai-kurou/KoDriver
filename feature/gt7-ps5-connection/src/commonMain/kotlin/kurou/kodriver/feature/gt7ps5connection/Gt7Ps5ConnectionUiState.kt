package kurou.kodriver.feature.gt7ps5connection

internal data class Gt7Ps5ConnectionUiState(
    val connectionStatus: Gt7Ps5ConnectionStatus = Gt7Ps5ConnectionStatus.UNCHECKED,
    val fuelLevel: Float? = null,
    val fuelCapacity: Float? = null,
    val currentLap: Int? = null,
    val totalLaps: Int? = null,
) {
    val isConnected: Boolean get() = connectionStatus == Gt7Ps5ConnectionStatus.CONNECTED
    val isConnectionChecked: Boolean get() = connectionStatus != Gt7Ps5ConnectionStatus.UNCHECKED
}

/**
 * Gt7Ps5Connection の接続状態を表す表示用ステータス。
 */
enum class Gt7Ps5ConnectionStatus {
    UNCHECKED,
    CONNECTED,
    DISCONNECTED,
}
