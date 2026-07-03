package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.LmuWindowsFlagPreferencesRepository

class ObserveLmuWindowsFlagEnabledStatesUseCase(private val repository: LmuWindowsFlagPreferencesRepository) {
    operator fun invoke(): Flow<Map<ReadoutItemKey, Boolean>> = repository.observeFlagEnabledStates()
}
