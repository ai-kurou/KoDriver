package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore

internal fun createReadoutStartSoundPreferencesDataStore(directory: String): DataStore<ReadoutStartSoundPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "readout_start_sound_preferences.pb",
        serializer = ReadoutStartSoundPreferencesSerializer,
    )
