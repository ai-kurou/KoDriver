package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.exportimport.Exportable
import kurou.kodriver.domain.model.SOUND_VOLUME_MAX
import kurou.kodriver.domain.model.SOUND_VOLUME_MIN
import kurou.kodriver.domain.repository.SoundVolumePreferencesRepository

internal class SoundVolumePreferencesRepositoryImpl(
    private val dataStore: DataStore<SoundVolumePreferences>,
) : SoundVolumePreferencesRepository,
    Exportable by DataStoreExportable(
        key = "sound_volume",
        dataStore = dataStore,
        serializer = SoundVolumePreferences.serializer(),
        sanitize = { it.copy(volume = it.volume.coerceIn(SOUND_VOLUME_MIN, SOUND_VOLUME_MAX)) },
    ) {
    override fun volume(): Flow<Int> = dataStore.observeProperty { it.volume }

    override suspend fun saveVolume(volume: Int) {
        dataStore.saveProperty(volume) { prefs, value -> prefs.copy(volume = value) }
    }
}
