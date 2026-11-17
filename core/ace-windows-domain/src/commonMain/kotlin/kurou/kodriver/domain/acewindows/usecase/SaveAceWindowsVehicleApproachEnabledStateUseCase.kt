package kurou.kodriver.domain.acewindows.usecase

import kurou.kodriver.domain.acewindows.repository.AceWindowsVehicleApproachPreferencesRepository
import kurou.kodriver.domain.model.ReadoutItemKey

public class SaveAceWindowsVehicleApproachEnabledStateUseCase(
    private val repository: AceWindowsVehicleApproachPreferencesRepository,
) {
    public suspend operator fun invoke(
        key: ReadoutItemKey,
        enabled: Boolean,
    ): Unit = repository.saveEnabledState(key, enabled)
}
