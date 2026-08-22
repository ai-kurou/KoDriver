package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore

internal fun createAceWindowsVehicleApproachPreferencesDataStore(
    directory: String,
): DataStore<AceWindowsVehicleApproachPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "ace_windows_vehicle_approach_preferences.pb",
        serializer = AceWindowsVehicleApproachPreferencesSerializer,
    )
