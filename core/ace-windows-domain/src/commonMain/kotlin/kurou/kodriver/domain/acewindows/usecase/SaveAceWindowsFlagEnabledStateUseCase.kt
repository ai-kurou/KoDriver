package kurou.kodriver.domain.acewindows.usecase

import kurou.kodriver.domain.acewindows.repository.AceWindowsFlagPreferencesRepository
import kurou.kodriver.domain.model.ReadoutItemKey

public class SaveAceWindowsFlagEnabledStateUseCase(
    private val repository: AceWindowsFlagPreferencesRepository,
) {
    public suspend operator fun invoke(
        key: ReadoutItemKey,
        enabled: Boolean,
    ): Unit = repository.saveFlagEnabledState(key, enabled)
}
