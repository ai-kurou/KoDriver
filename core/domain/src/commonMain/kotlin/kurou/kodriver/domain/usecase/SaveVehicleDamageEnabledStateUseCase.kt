package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.VehicleDamagePreferencesRepository

class SaveVehicleDamageEnabledStateUseCase(private val repository: VehicleDamagePreferencesRepository) {
    suspend operator fun invoke(key: String, enabled: Boolean) =
        repository.saveEnabledState(key, enabled)
}
