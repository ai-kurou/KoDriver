package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore

internal fun createAceWindowsTyreTemperaturePreferencesDataStore(
    directory: String,
): DataStore<AceWindowsTyreTemperaturePreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "ace_windows_tyre_temperature_preferences.pb",
        serializer = AceWindowsTyreTemperaturePreferencesSerializer,
    )
