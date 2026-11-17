package kurou.kodriver.domain.lmuwindows.usecase

import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleApproachPreferencesRepository
import kurou.kodriver.domain.model.ReadoutItemKey

public class SaveLmuWindowsVehicleApproachEnabledStateUseCase(
    private val repository: LmuWindowsVehicleApproachPreferencesRepository,
) {
    public suspend operator fun invoke(
        key: ReadoutItemKey,
        enabled: Boolean,
    ): Unit = repository.saveEnabledState(key, enabled)
}
