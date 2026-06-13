package kurou.kodriver.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import kurou.kodriver.data.model.SoundVolumePreferences
import java.io.File

internal fun createSoundVolumePreferencesDataStore(directory: String): DataStore<SoundVolumePreferences> =
    DataStoreFactory.create(
        serializer = SoundVolumePreferencesSerializer,
        produceFile = { File("$directory/sound_volume_preferences.pb") },
    )
