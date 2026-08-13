package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore

internal fun createLmuWindowsTyreTemperaturePreferencesDataStore(
    directory: String,
): DataStore<LmuWindowsTyreTemperaturePreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "lmu_windows_tyre_temperature_preferences.pb",
        serializer = LmuWindowsTyreTemperaturePreferencesSerializer,
    )
