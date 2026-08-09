package kurou.kodriver.data.datasource

import androidx.datastore.core.DataStore
import kurou.kodriver.data.model.AceWindowsRemainingFuelPreferences

internal fun createAceWindowsRemainingFuelPreferencesDataStore(
    directory: String,
): DataStore<AceWindowsRemainingFuelPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "ace_windows_remaining_fuel_preferences.pb",
        serializer = AceWindowsRemainingFuelPreferencesSerializer,
    )
