package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.FlagPreferencesRepository

class ObserveLmuWindowsFlagEnabledStatesUseCase(private val repository: FlagPreferencesRepository) {
    operator fun invoke(): Flow<Map<ReadoutItemKey, Boolean>> = repository.observeFlagEnabledStates()
}
