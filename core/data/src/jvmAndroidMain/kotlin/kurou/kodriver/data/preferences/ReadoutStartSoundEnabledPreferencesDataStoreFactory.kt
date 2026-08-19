package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore

internal fun createReadoutStartSoundEnabledPreferencesDataStore(
    directory: String,
): DataStore<ReadoutStartSoundEnabledPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "readout_start_sound_enabled_preferences.pb",
        serializer = ReadoutStartSoundEnabledPreferencesSerializer,
    )
