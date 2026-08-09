package kurou.kodriver.data.datasource

import androidx.datastore.core.DataStore
import kurou.kodriver.data.model.ReadoutStartSoundPreferences

internal fun createReadoutStartSoundPreferencesDataStore(directory: String): DataStore<ReadoutStartSoundPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "readout_start_sound_preferences.pb",
        serializer = ReadoutStartSoundPreferencesSerializer,
    )
