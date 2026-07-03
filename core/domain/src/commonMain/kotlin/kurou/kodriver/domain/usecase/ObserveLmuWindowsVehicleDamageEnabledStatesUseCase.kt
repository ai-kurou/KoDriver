package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.LmuWindowsVehicleDamagePreferencesRepository

class ObserveLmuWindowsVehicleDamageEnabledStatesUseCase(
    private val repository: LmuWindowsVehicleDamagePreferencesRepository,
) {
    operator fun invoke(): Flow<Map<ReadoutItemKey, Boolean>> = repository.observeEnabledStates()
}
