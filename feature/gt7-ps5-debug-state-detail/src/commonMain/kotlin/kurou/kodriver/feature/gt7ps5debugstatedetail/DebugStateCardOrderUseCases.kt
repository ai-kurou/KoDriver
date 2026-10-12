package kurou.kodriver.feature.gt7ps5debugstatedetail

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
    fun observeKeys(): Flow<List<Gt7Ps5DebugStateCardKey>> =
        observeCardOrder().map { order ->
            order.mapNotNull { name -> Gt7Ps5DebugStateCardKey.entries.find { it.name == name } }
        }

    suspend fun saveKeys(order: List<Gt7Ps5DebugStateCardKey>) = saveCardOrder(order.map { it.name })
}
