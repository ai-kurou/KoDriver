package kurou.kodriver.domain.usecase

import kurou.kodriver.core.model.ReadoutItemKey
import kurou.kodriver.domain.repository.QueuePreferencesRepository

class SaveQueueEnabledStateUseCase(
    private val repository: QueuePreferencesRepository,
) {
    suspend operator fun invoke(
        key: ReadoutItemKey,
        enabled: Boolean,
    ) = repository.saveQueueEnabledState(key, enabled)
}
