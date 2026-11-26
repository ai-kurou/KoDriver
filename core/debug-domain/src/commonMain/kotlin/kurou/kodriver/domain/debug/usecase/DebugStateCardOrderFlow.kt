package kurou.kodriver.domain.debug.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** ローカル順序を優先して保存済み順序を合成する cold Flow。共有と初期値は ViewModel が管理する。 */
public class DebugStateCardOrderFlow<K>(
    persistedOrder: Flow<List<K>>,
    localOrder: Flow<List<K>?>,
    defaultOrder: List<K>,
    resolveCardOrder: ResolveDebugStateCardOrderUseCase,
) {
    public val order: Flow<List<K>> =
        combine(persistedOrder, localOrder) { persisted, local ->
            local ?: resolveCardOrder(persistedOrder = persisted, defaultOrder = defaultOrder)
        }
}
