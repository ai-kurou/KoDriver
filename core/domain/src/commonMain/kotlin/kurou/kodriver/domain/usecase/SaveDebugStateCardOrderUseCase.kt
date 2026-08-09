package kurou.kodriver.domain.usecase

import kurou.kodriver.core.model.DebugStateCardKey
import kurou.kodriver.domain.repository.DebugStateCardOrderPreferencesRepository

class SaveDebugStateCardOrderUseCase(
    private val repository: DebugStateCardOrderPreferencesRepository,
) {
    suspend operator fun invoke(order: List<DebugStateCardKey>) = repository.saveCardOrder(order)
}
