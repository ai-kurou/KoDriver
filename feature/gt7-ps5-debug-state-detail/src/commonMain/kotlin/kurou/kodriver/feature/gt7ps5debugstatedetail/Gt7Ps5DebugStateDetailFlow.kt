package kurou.kodriver.feature.gt7ps5debugstatedetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kurou.kodriver.domain.model.ReadoutItemKey

/** UI 状態の合成だけを担う cold Flow。各段階の共有・初期値は ViewModel が管理する。 */
internal class Gt7Ps5DebugStateDetailFlow(
    cardOrder: Flow<List<Gt7Ps5DebugStateCardKey>>,
    receivedCardKeys: Flow<Set<Gt7Ps5DebugStateCardKey>>,
    private val gt7Ps5: Flow<Gt7Ps5DebugState>,
    private val selectedReadoutItemKey: Flow<ReadoutItemKey>,
    private val debugReadoutText: Flow<String>,
) {
    val header: Flow<Gt7Ps5DebugStateDetailUiState> =
        combine(cardOrder, receivedCardKeys) { order, received ->
            Gt7Ps5DebugStateDetailUiState(
                enabledCardKeys = received intersect gt7Ps5SupportedCardKeys,
                cardOrder = order,
            )
        }

    fun detail(header: Flow<Gt7Ps5DebugStateDetailUiState>): Flow<Gt7Ps5DebugStateDetailUiState> =
        combine(header, gt7Ps5, selectedReadoutItemKey, debugReadoutText) { state, gt7, key, text ->
            state.copy(gt7Ps5 = gt7, selectedReadoutItemKey = key, debugReadoutText = text)
        }
}
