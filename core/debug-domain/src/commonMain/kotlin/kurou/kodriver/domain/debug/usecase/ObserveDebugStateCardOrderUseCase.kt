package kurou.kodriver.domain.debug.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.debug.repository.DebugStateCardOrderPreferencesRepository

public class ObserveDebugStateCardOrderUseCase(
    private val repository: DebugStateCardOrderPreferencesRepository,
) {
    public operator fun invoke(): Flow<List<String>> = repository.observeCardOrder()
}
