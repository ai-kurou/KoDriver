package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kurou.kodriver.domain.repository.ReadoutStartSoundPreferencesRepository

public class ObserveReadoutStartSoundTypeUseCase(
    private val repository: ReadoutStartSoundPreferencesRepository,
) {
    public operator fun invoke(): Flow<ReadoutStartSoundType> = repository.observeType()
}
