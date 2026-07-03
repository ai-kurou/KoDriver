package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.LmuWindowsVehicleDamagePreferencesRepository

class SaveLmuWindowsVehicleDamageEnabledStateUseCase(
    private val repository: LmuWindowsVehicleDamagePreferencesRepository,
) {
    suspend operator fun invoke(key: ReadoutItemKey, enabled: Boolean) =
        repository.saveEnabledState(key, enabled)
}
