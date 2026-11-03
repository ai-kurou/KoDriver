package kurou.kodriver.feature.acewindowsdebugstatedetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kurou.kodriver.domain.usecase.ResolveDebugStateCardOrderUseCase

/** ローカル順序を優先して保存済み順序を合成する cold Flow。共有と初期値は ViewModel が管理する。 */
internal class AceWindowsDebugStateCardOrderFlow(
    persistedOrder: Flow<List<AceWindowsDebugStateCardKey>>,
    localOrder: Flow<List<AceWindowsDebugStateCardKey>?>,
    resolveCardOrder: ResolveDebugStateCardOrderUseCase,
) {
    val order: Flow<List<AceWindowsDebugStateCardKey>> =
        combine(persistedOrder, localOrder) { persisted, local ->
            local ?: resolveCardOrder(persistedOrder = persisted, defaultOrder = defaultDebugStateCardOrder)
        }
}
