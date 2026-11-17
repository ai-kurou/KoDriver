package kurou.kodriver.domain.lmuwindows.usecase

import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleDamagePreferencesRepository
import kurou.kodriver.domain.model.ReadoutItemKey

public class SaveLmuWindowsVehicleDamageEnabledStateUseCase(
    private val repository: LmuWindowsVehicleDamagePreferencesRepository,
) {
    public suspend operator fun invoke(
        key: ReadoutItemKey,
        enabled: Boolean,
    ): Unit = repository.saveEnabledState(key, enabled)
}
