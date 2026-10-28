package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kurou.kodriver.domain.usecase.ResolveDebugStateCardOrderUseCase

/** ローカル順序を優先し、未指定なら保存済み順序を補完する cold Flow。共有・初期値は ViewModel が管理する。 */
internal class LmuWindowsDebugStateCardOrderFlow(
    persistedOrder: Flow<List<LmuWindowsDebugStateCardKey>>,
    localOrder: Flow<List<LmuWindowsDebugStateCardKey>?>,
    resolveCardOrder: ResolveDebugStateCardOrderUseCase,
) {
    val order: Flow<List<LmuWindowsDebugStateCardKey>> =
        combine(persistedOrder, localOrder) { persisted, local ->
            local ?: resolveCardOrder(persistedOrder = persisted, defaultOrder = defaultDebugStateCardOrder)
        }
}
