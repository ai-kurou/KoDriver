package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.FlagPreferencesRepository

class ObserveFlagEnabledStatesUseCase(private val repository: FlagPreferencesRepository) {
    operator fun invoke(): Flow<Map<String, Boolean>> = repository.observeFlagEnabledStates()
}
