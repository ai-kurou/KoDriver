package kurou.kodriver.feature.gt7ps5debugstatedetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kurou.kodriver.domain.usecase.ResolveDebugStateCardOrderUseCase

/** ローカル順序を優先して保存済み順序を合成する cold Flow。共有と初期値は ViewModel が管理する。 */
internal class Gt7Ps5DebugStateCardOrderFlow(
    persistedOrder: Flow<List<Gt7Ps5DebugStateCardKey>>,
    localOrder: Flow<List<Gt7Ps5DebugStateCardKey>?>,
    resolveCardOrder: ResolveDebugStateCardOrderUseCase,
) {
    val order: Flow<List<Gt7Ps5DebugStateCardKey>> =
        combine(persistedOrder, localOrder) { persisted, local ->
            local ?: resolveCardOrder(persistedOrder = persisted, defaultOrder = defaultDebugStateCardOrder)
        }
}
