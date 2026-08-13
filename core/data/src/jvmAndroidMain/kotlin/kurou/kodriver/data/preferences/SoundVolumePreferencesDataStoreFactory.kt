package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore

internal fun createSoundVolumePreferencesDataStore(directory: String): DataStore<SoundVolumePreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "sound_volume_preferences.pb",
        serializer = SoundVolumePreferencesSerializer,
    )
