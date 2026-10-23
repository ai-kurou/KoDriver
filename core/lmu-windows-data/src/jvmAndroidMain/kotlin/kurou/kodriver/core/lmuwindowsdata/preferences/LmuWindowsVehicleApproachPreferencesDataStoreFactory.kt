package kurou.kodriver.core.lmuwindowsdata.preferences

import androidx.datastore.core.DataStore

internal fun createLmuWindowsVehicleApproachPreferencesDataStore(
    directory: String,
): DataStore<LmuWindowsVehicleApproachPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "lmu_windows_auto_vehicle_approach_preferences.pb",
        serializer = LmuWindowsVehicleApproachPreferencesSerializer,
    )
