package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.usecase.ObserveDebugStateCardOrderUseCase
import kurou.kodriver.domain.usecase.ResolveDebugStateCardOrderUseCase
import kurou.kodriver.domain.usecase.SaveDebugStateCardOrderUseCase

internal data class DebugStateCardOrderUseCases(
    val observeCardOrder: ObserveDebugStateCardOrderUseCase,
    val resolveCardOrder: ResolveDebugStateCardOrderUseCase,
    val saveCardOrder: SaveDebugStateCardOrderUseCase,
) {
    fun observeKeys(): Flow<List<LmuWindowsDebugStateCardKey>> =
        observeCardOrder().map { order ->
            order.mapNotNull { name -> LmuWindowsDebugStateCardKey.entries.find { it.name == name } }
        }

    suspend fun saveKeys(order: List<LmuWindowsDebugStateCardKey>) = saveCardOrder(order.map { it.name })
}
