package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kurou.kodriver.domain.model.ReadoutItemKey

/** UI 状態の合成だけを担う cold Flow。各段階の共有・初期値は ViewModel が管理する。 */
internal class LmuWindowsDebugStateDetailFlow(
    cardOrder: Flow<List<LmuWindowsDebugStateCardKey>>,
    receivedCardKeys: Flow<Set<LmuWindowsDebugStateCardKey>>,
    private val lmuWindows: Flow<LmuWindowsDebugState>,
    private val selectedReadoutItemKey: Flow<ReadoutItemKey>,
    private val debugReadoutText: Flow<String>,
    private val hotkey: Flow<ReadoutHotkeyUiState>,
) {
    val header: Flow<LmuWindowsDebugStateDetailUiState> =
        combine(cardOrder, receivedCardKeys) { order, received ->
            LmuWindowsDebugStateDetailUiState(
                enabledCardKeys = received intersect lmuWindowsSupportedCardKeys,
                cardOrder = order,
            )
        }

    fun detail(header: Flow<LmuWindowsDebugStateDetailUiState>): Flow<LmuWindowsDebugStateDetailUiState> =
        combine(header, lmuWindows, selectedReadoutItemKey, debugReadoutText) { state, lmu, key, text ->
            state.copy(lmuWindows = lmu, selectedReadoutItemKey = key, debugReadoutText = text)
        }

    fun uiState(detail: Flow<LmuWindowsDebugStateDetailUiState>): Flow<LmuWindowsDebugStateDetailUiState> =
        combine(detail, hotkey) { state, hotkeyState ->
            state.copy(readoutHotkey = hotkeyState)
        }
}
