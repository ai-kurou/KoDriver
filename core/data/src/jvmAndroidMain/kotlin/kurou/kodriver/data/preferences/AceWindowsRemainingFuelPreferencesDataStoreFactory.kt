package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore

internal fun createAceWindowsRemainingFuelPreferencesDataStore(
    directory: String,
): DataStore<AceWindowsRemainingFuelPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "ace_windows_remaining_fuel_preferences.pb",
        serializer = AceWindowsRemainingFuelPreferencesSerializer,
    )
