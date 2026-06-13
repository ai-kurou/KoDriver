package kurou.kodriver.data.repository

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.data.model.SoundVolumePreferences
import kurou.kodriver.domain.repository.SoundVolumeRepository

internal class SoundVolumeRepositoryImpl(
    private val dataStore: DataStore<SoundVolumePreferences>,
) : SoundVolumeRepository {

    override fun volume(): Flow<Int> = dataStore.data.map { it.volume }

    override suspend fun saveVolume(volume: Int) {
        dataStore.updateData { it.copy(volume = volume) }
    }
}
