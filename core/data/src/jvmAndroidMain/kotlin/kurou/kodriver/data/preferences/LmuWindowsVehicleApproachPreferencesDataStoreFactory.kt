package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore

internal fun createLmuWindowsVehicleApproachPreferencesDataStore(
    directory: String,
): DataStore<LmuWindowsVehicleApproachPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "lmu_windows_vehicle_approach_preferences.pb",
        serializer = LmuWindowsVehicleApproachPreferencesSerializer,
    )
