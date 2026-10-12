package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.DebugStateCardOrderPreferencesRepository

class ObserveDebugStateCardOrderUseCase(
    private val repository: DebugStateCardOrderPreferencesRepository,
) {
    operator fun invoke(): Flow<List<String>> = repository.observeCardOrder()
}
