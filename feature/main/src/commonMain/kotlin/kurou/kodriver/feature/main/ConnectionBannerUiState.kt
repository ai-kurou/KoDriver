package kurou.kodriver.feature.main

import kurou.kodriver.domain.model.Simulator

/**
 * ConnectionBannerVm 画面の表示状態。
 */
data class ConnectionBannerVmUiState(
    val connectionStatus: ConnectionBannerVmStatus = ConnectionBannerVmStatus.UNCHECKED,
    val selectedSimulator: Simulator? = null,
) {
    val isSimulatorSelected: Boolean get() = selectedSimulator != null
    val isGt7Ps5: Boolean get() = selectedSimulator is Simulator.Gt7Ps5
    val isAceWindows: Boolean get() = selectedSimulator is Simulator.AceWindows
}

/**
 * ConnectionBannerVm の接続状態を表す表示用ステータス。
 */
enum class ConnectionBannerVmStatus {
    UNCHECKED,
    CONNECTED,
    DISCONNECTED,
    IP_NOT_CONFIGURED,
}
