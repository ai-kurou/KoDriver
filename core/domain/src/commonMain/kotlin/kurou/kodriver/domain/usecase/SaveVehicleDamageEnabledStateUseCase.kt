package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.VehicleDamagePreferencesRepository

class SaveVehicleDamageEnabledStateUseCase(private val repository: VehicleDamagePreferencesRepository) {
    suspend operator fun invoke(key: ReadoutItemKey, enabled: Boolean) =
        repository.saveEnabledState(key, enabled)
}
