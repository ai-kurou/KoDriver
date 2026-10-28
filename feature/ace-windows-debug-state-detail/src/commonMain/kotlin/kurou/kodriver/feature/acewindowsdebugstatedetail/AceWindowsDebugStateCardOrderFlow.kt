package kurou.kodriver.feature.acewindowsdebugstatedetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kurou.kodriver.domain.usecase.ResolveDebugStateCardOrderUseCase

/** 保存済み順序とドラッグ中のローカル順序を合成する。共有・初期値は ViewModel が管理する。 */
internal class AceWindowsDebugStateCardOrderFlow(
    persistedOrder: Flow<List<AceWindowsDebugStateCardKey>>,
    localOrder: Flow<List<AceWindowsDebugStateCardKey>?>,
    resolveCardOrder: ResolveDebugStateCardOrderUseCase,
) {
    val cardOrder: Flow<List<AceWindowsDebugStateCardKey>> =
        combine(persistedOrder, localOrder) { persisted, local ->
            local ?: resolveCardOrder(persistedOrder = persisted, defaultOrder = defaultDebugStateCardOrder)
        }
}
