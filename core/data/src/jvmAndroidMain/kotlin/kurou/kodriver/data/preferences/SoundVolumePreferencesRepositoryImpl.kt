package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.SoundVolumePreferencesRepository

internal class SoundVolumePreferencesRepositoryImpl(
    private val dataStore: DataStore<SoundVolumePreferences>,
) : SoundVolumePreferencesRepository {
    override fun volume(): Flow<Int> = dataStore.observeProperty { it.volume }

    override suspend fun saveVolume(volume: Int) {
        dataStore.saveProperty(volume) { prefs, value -> prefs.copy(volume = value) }
    }
}
