package kurou.kodriver.data.repository

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.data.model.ReadoutStartSoundPreferences
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kurou.kodriver.domain.repository.ReadoutStartSoundPreferencesRepository

internal class ReadoutStartSoundPreferencesRepositoryImpl(
    private val dataStore: DataStore<ReadoutStartSoundPreferences>,
) : ReadoutStartSoundPreferencesRepository {

    override fun observeType(): Flow<ReadoutStartSoundType> =
        dataStore.data.map { ReadoutStartSoundType.fromId(it.type) }

    override suspend fun saveType(type: ReadoutStartSoundType) {
        dataStore.updateData { it.copy(type = type.id) }
    }
}
