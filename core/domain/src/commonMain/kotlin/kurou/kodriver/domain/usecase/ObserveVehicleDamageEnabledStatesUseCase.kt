package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.VehicleDamagePreferencesRepository

class ObserveVehicleDamageEnabledStatesUseCase(private val repository: VehicleDamagePreferencesRepository) {
    operator fun invoke(): Flow<Map<String, Boolean>> = repository.observeEnabledStates()
}
