package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.AceWindowsVehicleApproachPreferencesRepository

class SaveAceWindowsVehicleApproachEnabledStateUseCase(
    private val repository: AceWindowsVehicleApproachPreferencesRepository,
) {
    suspend operator fun invoke(
        key: ReadoutItemKey,
        enabled: Boolean,
    ) = repository.saveEnabledState(key, enabled)
}
