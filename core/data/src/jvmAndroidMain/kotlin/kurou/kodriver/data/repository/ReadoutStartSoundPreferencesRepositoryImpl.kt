package kurou.kodriver.data.repository

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.data.model.ReadoutStartSoundPreferences
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kurou.kodriver.domain.repository.ReadoutStartSoundPreferencesRepository

internal class ReadoutStartSoundPreferencesRepositoryImpl(
    private val dataStore: DataStore<ReadoutStartSoundPreferences>,
) : ReadoutStartSoundPreferencesRepository {
    override fun observeType(): Flow<ReadoutStartSoundType> =
        dataStore.observeProperty { ReadoutStartSoundType.fromId(it.type) }

    override suspend fun saveType(type: ReadoutStartSoundType) {
        dataStore.saveProperty(type.id) { prefs, value -> prefs.copy(type = value) }
    }
}
