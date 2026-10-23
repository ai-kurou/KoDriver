package kurou.kodriver.core.lmuwindowsdata.preferences

import androidx.datastore.core.DataStore

internal fun createLmuWindowsFlagReadoutTextPreferencesDataStore(
    directory: String,
): DataStore<FlagReadoutTextPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "lmu_windows_auto_flag_readout_text_preferences.pb",
        serializer = FlagReadoutTextPreferencesSerializer,
    )
