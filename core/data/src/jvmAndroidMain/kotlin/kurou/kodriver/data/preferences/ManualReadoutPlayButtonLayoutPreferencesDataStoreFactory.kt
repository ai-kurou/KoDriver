package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore

internal fun createManualReadoutPlayButtonLayoutPreferencesDataStore(
    directory: String,
): DataStore<ManualReadoutPlayButtonLayoutPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "manual_readout_play_button_layout_preferences.pb",
        serializer = ManualReadoutPlayButtonLayoutPreferencesSerializer,
    )
