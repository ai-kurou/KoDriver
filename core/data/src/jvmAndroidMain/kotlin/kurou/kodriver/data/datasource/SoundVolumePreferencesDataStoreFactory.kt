package kurou.kodriver.data.datasource

import androidx.datastore.core.DataStore
import kurou.kodriver.data.model.SoundVolumePreferences

internal fun createSoundVolumePreferencesDataStore(directory: String): DataStore<SoundVolumePreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "sound_volume_preferences.pb",
        serializer = SoundVolumePreferencesSerializer,
    )
