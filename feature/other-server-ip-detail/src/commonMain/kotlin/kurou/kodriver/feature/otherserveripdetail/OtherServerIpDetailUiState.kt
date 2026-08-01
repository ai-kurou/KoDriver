package kurou.kodriver.feature.otherserveripdetail

/**
 * OtherServerIpDetail 画面の表示状態。
 */
data class OtherServerIpDetailUiState(
    val inputIp: String = "",
    val isInputValid: Boolean = true,
    val saveFailed: Boolean = false,
    val isCheckingConnectivity: Boolean = false,
    val connectivityWarning: Boolean = false,
    val isSaved: Boolean = false,
    val discoveredServers: List<DiscoveredServer> = emptyList(),
    val isDiscoveryDialogVisible: Boolean = false,
    val selectedDiscoveredServer: DiscoveredServer? = null,
)
