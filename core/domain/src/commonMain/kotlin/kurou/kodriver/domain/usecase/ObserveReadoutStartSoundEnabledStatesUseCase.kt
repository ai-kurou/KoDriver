package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.model.READOUT_START_SOUND_ENABLED_STATE_DEFAULT
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.ReadoutStartSoundEnabledPreferencesRepository

class ObserveReadoutStartSoundEnabledStatesUseCase(
    private val repository: ReadoutStartSoundEnabledPreferencesRepository,
) {
    operator fun invoke(): Flow<Map<ReadoutItemKey, Boolean>> =
        repository.observeStartSoundEnabledStates().map { persisted ->
            READOUT_START_SOUND_ENABLED_STATE_DEFAULT + persisted
        }
}
