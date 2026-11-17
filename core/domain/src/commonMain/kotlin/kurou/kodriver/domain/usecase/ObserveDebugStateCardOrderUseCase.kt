package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.DebugStateCardOrderPreferencesRepository

public class ObserveDebugStateCardOrderUseCase(
    private val repository: DebugStateCardOrderPreferencesRepository,
) {
    public operator fun invoke(): Flow<List<String>> = repository.observeCardOrder()
}
