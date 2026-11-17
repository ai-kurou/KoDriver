package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.AutoReadoutPreferencesRepository

public class SaveAutoReadoutEnabledStateUseCase(
    private val repository: AutoReadoutPreferencesRepository,
) {
    public suspend operator fun invoke(
        simulator: String,
        key: ReadoutItemKey,
        enabled: Boolean,
    ): Unit = repository.saveAutoReadoutEnabledState(simulator, key, enabled)
}
