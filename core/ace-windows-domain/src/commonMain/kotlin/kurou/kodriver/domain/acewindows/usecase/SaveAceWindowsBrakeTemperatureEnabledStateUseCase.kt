package kurou.kodriver.domain.acewindows.usecase

import kurou.kodriver.domain.acewindows.repository.AceWindowsBrakeTemperaturePreferencesRepository
import kurou.kodriver.domain.model.ReadoutItemKey

public class SaveAceWindowsBrakeTemperatureEnabledStateUseCase(
    private val repository: AceWindowsBrakeTemperaturePreferencesRepository,
) {
    public suspend operator fun invoke(
        key: ReadoutItemKey,
        enabled: Boolean,
    ): Unit = repository.saveEnabledState(key, enabled)
}
