package kurou.kodriver.domain.autoreadout.usecase

import kurou.kodriver.domain.autoreadout.repository.AutoReadoutPreferencesRepository
import kurou.kodriver.domain.model.ReadoutItemKey

public class SaveAutoReadoutEnabledStateUseCase(
    private val repository: AutoReadoutPreferencesRepository,
) {
    public suspend operator fun invoke(
        simulator: String,
        key: ReadoutItemKey,
        enabled: Boolean,
    ): Unit = repository.saveAutoReadoutEnabledState(simulator, key, enabled)
}
