package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore

internal fun createLmuWindowsFlagReadoutTextPreferencesDataStore(
    directory: String,
): DataStore<FlagReadoutTextPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "lmu_windows_flag_readout_text_preferences.pb",
        serializer = FlagReadoutTextPreferencesSerializer,
    )
