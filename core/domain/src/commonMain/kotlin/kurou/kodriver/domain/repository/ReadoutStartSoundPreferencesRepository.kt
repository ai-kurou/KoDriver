package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ReadoutStartSoundType

public interface ReadoutStartSoundPreferencesRepository {
    public fun observeType(): Flow<ReadoutStartSoundType>

    public suspend fun saveType(type: ReadoutStartSoundType)
}
