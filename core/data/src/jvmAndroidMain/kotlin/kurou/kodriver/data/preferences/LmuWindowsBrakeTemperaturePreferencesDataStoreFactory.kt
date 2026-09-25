package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore

internal fun createLmuWindowsBrakeTemperaturePreferencesDataStore(
    directory: String,
): DataStore<LmuWindowsBrakeTemperaturePreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "lmu_windows_brake_temperature_preferences.pb",
        serializer = LmuWindowsBrakeTemperaturePreferencesSerializer,
    )
