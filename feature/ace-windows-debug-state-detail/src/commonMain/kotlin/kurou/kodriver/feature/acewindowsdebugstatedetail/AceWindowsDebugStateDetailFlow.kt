package kurou.kodriver.feature.acewindowsdebugstatedetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kurou.kodriver.domain.model.ReadoutItemKey

/** UI 状態の合成だけを担う cold Flow。各段階の共有・初期値は ViewModel が管理する。 */
internal class AceWindowsDebugStateDetailFlow(
    cardOrder: Flow<List<AceWindowsDebugStateCardKey>>,
    receivedCardKeys: Flow<Set<AceWindowsDebugStateCardKey>>,
    private val aceWindows: Flow<AceWindowsDebugState>,
    private val selectedReadoutItemKey: Flow<ReadoutItemKey>,
    private val debugReadoutText: Flow<String>,
) {
    val header: Flow<AceWindowsDebugStateDetailUiState> =
        combine(cardOrder, receivedCardKeys) { order, received ->
            AceWindowsDebugStateDetailUiState(
                enabledCardKeys = received intersect aceWindowsSupportedCardKeys,
                cardOrder = order,
            )
        }

    fun uiState(header: Flow<AceWindowsDebugStateDetailUiState>): Flow<AceWindowsDebugStateDetailUiState> =
        combine(header, aceWindows, selectedReadoutItemKey, debugReadoutText) { state, ace, key, text ->
            state.copy(aceWindows = ace, selectedReadoutItemKey = key, debugReadoutText = text)
        }
}
