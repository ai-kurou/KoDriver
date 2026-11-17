package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.DebugStateCardOrderPreferencesRepository

public class SaveDebugStateCardOrderUseCase(
    private val repository: DebugStateCardOrderPreferencesRepository,
) {
    public suspend operator fun invoke(order: List<String>): Unit = repository.saveCardOrder(order)
}
