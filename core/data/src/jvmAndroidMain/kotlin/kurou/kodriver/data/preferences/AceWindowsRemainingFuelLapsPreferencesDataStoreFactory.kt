package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore

internal fun createAceWindowsRemainingFuelLapsPreferencesDataStore(
    directory: String,
): DataStore<AceWindowsRemainingFuelLapsPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "ace_windows_remaining_fuel_laps_preferences.pb",
        serializer = AceWindowsRemainingFuelLapsPreferencesSerializer,
    )
