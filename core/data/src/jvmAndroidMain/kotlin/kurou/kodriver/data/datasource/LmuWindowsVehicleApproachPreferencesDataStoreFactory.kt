package kurou.kodriver.data.datasource

import androidx.datastore.core.DataStore
import kurou.kodriver.data.model.LmuWindowsVehicleApproachPreferences

internal fun createLmuWindowsVehicleApproachPreferencesDataStore(
    directory: String,
): DataStore<LmuWindowsVehicleApproachPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "lmu_windows_vehicle_approach_preferences.pb",
        serializer = LmuWindowsVehicleApproachPreferencesSerializer,
    )
