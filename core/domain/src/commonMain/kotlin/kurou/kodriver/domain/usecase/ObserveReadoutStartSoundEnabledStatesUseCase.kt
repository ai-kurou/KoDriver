package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.model.ReadoutCatalog
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.ReadoutStartSoundEnabledPreferencesRepository

class ObserveReadoutStartSoundEnabledStatesUseCase(
    private val repository: ReadoutStartSoundEnabledPreferencesRepository,
    private val catalog: ReadoutCatalog,
) {
    operator fun invoke(): Flow<Map<ReadoutItemKey, Boolean>> =
        repository.observeStartSoundEnabledStates().map { persisted ->
            catalog.startSoundEnabledStateDefaults + persisted
        }
}
