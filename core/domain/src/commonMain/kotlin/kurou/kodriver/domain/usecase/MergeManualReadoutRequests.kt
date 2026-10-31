package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.merge
import kurou.kodriver.domain.model.ManualReadoutRequest

/**
 * 手動読み上げの依頼（再生ボタン）とホットキー押下を1本のFlowに合流する。
 * 両経路の重複や他シミュレーター宛ても除外しない。宛先の判定は呼び出し側で行う。
 */
fun mergeManualReadoutRequests(
    requests: Flow<ManualReadoutRequest>,
    hotkeyPresses: Flow<ManualReadoutRequest>,
): Flow<ManualReadoutRequest> = merge(requests, hotkeyPresses)
