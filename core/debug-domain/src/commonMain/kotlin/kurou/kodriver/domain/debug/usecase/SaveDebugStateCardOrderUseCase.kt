package kurou.kodriver.domain.debug.usecase

import kurou.kodriver.domain.debug.repository.DebugStateCardOrderPreferencesRepository

public class SaveDebugStateCardOrderUseCase(
    private val repository: DebugStateCardOrderPreferencesRepository,
) {
    public suspend operator fun invoke(order: List<String>): Unit = repository.saveCardOrder(order)
}
