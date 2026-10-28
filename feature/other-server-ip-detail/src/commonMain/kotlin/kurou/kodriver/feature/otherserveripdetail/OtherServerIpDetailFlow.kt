package kurou.kodriver.feature.otherserveripdetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** 保存済みIP、操作状態、検出結果から画面状態を合成する冷たい Flow。 */
internal class OtherServerIpDetailFlow(
    savedIp: Flow<String>,
    mutableState: Flow<OtherServerIpDetailMutableState>,
    discoveredServers: Flow<List<DiscoveredServer>>,
    validateServerIpAddress: ValidateServerIpAddressUseCase,
) {
    val initialUiState = OtherServerIpDetailUiState()

    val uiState: Flow<OtherServerIpDetailUiState> =
        combine(
            savedIp,
            mutableState,
            discoveredServers,
        ) { saved, m, discovered ->
            val current = m.userInput ?: saved
            OtherServerIpDetailUiState(
                inputIp = current,
                isInputValid = current.isEmpty() || validateServerIpAddress(current),
                saveFailed = m.saveFailed,
                isCheckingConnectivity = m.isCheckingConnectivity,
                connectivityWarning = m.connectivityWarning,
                isSaved = m.isSaved,
                discoveredServers = discovered,
                isDiscoveryDialogVisible = !m.isSaved && discovered.isNotEmpty() && !m.isDiscoveryDialogDismissed,
                selectedDiscoveredServer = m.selectedDiscoveredServer ?: discovered.firstOrNull(),
            )
        }
}
