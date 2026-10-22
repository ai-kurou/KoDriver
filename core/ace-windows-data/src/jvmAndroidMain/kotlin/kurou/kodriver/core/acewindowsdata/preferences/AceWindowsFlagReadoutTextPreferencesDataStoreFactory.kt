package kurou.kodriver.core.acewindowsdata.preferences

import androidx.datastore.core.DataStore

internal fun createAceWindowsFlagReadoutTextPreferencesDataStore(
    directory: String,
): DataStore<AceWindowsFlagReadoutTextPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "ace_windows_auto_flag_readout_text_preferences.pb",
        serializer = AceWindowsFlagReadoutTextPreferencesSerializer,
    )
