package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.Gt7Ps5TyreTemperaturePreferencesRepository

class SaveGt7Ps5TyreTemperatureEnabledStateUseCase(
    private val repository: Gt7Ps5TyreTemperaturePreferencesRepository,
) {
    suspend operator fun invoke(
        key: ReadoutItemKey,
        enabled: Boolean,
    ) = repository.saveEnabledState(key, enabled)
}
