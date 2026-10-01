package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.defaultReadoutOrder

/**
 * 保存済みの読み上げ順序をデフォルト順序と突き合わせた、実効的な読み上げ順序を監視する。
 * 一覧画面の表示順と Narrator の優先度判定が同じ順序を使うための共通入口。
 */
class ObserveResolvedReadoutOrderUseCase(
    private val observeReadoutOrder: ObserveReadoutOrderUseCase,
    private val resolveReadoutOrder: ResolveReadoutOrderUseCase,
) {
    operator fun invoke(simulator: Simulator): Flow<List<ReadoutItemKey>> {
        val defaultOrder = defaultReadoutOrder(simulator)
        return observeReadoutOrder(simulator.id).map { persisted ->
            resolveReadoutOrder(persistedOrder = persisted, defaultOrder = defaultOrder)
        }
    }
}
