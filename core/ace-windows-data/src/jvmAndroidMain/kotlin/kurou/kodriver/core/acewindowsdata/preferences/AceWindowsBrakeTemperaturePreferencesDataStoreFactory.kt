package kurou.kodriver.core.acewindowsdata.preferences

import androidx.datastore.core.DataStore

internal fun createAceWindowsBrakeTemperaturePreferencesDataStore(
    directory: String,
): DataStore<AceWindowsBrakeTemperaturePreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "ace_windows_auto_brake_temperature_preferences.pb",
        serializer = AceWindowsBrakeTemperaturePreferencesSerializer,
    )
