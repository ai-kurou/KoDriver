package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository

class ObserveReadoutEnabledStatesUseCase(private val repository: ReadoutPreferencesRepository) {
    operator fun invoke(simulator: String): Flow<Map<String, Boolean>> =
        repository.observeReadoutEnabledStates(simulator)
}
