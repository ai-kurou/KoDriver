package kurou.kodriver.domain.gt7ps5.usecase

import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5TyreTemperaturePreferencesRepository
import kurou.kodriver.domain.model.ReadoutItemKey

class SaveGt7Ps5TyreTemperatureEnabledStateUseCase(
    private val repository: Gt7Ps5TyreTemperaturePreferencesRepository,
) {
    suspend operator fun invoke(
        key: ReadoutItemKey,
        enabled: Boolean,
    ) = repository.saveEnabledState(key, enabled)
}
