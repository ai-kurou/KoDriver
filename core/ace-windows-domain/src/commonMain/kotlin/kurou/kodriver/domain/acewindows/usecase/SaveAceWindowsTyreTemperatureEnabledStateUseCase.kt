package kurou.kodriver.domain.acewindows.usecase

import kurou.kodriver.domain.acewindows.repository.AceWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.model.ReadoutItemKey

public class SaveAceWindowsTyreTemperatureEnabledStateUseCase(
    private val repository: AceWindowsTyreTemperaturePreferencesRepository,
) {
    public suspend operator fun invoke(
        key: ReadoutItemKey,
        enabled: Boolean,
    ): Unit = repository.saveEnabledState(key, enabled)
}
