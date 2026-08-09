package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.core.model.ReadoutStartSoundType

interface ReadoutStartSoundPreferencesRepository {
    fun observeType(): Flow<ReadoutStartSoundType>

    suspend fun saveType(type: ReadoutStartSoundType)
}
