package kurou.kodriver.data.datasource

import androidx.datastore.core.DataStore
import kurou.kodriver.data.model.LmuWindowsVehicleApproachThresholdsPreferences

internal fun createLmuWindowsVehicleApproachThresholdsPreferencesDataStore(
    directory: String,
): DataStore<LmuWindowsVehicleApproachThresholdsPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "lmu_windows_vehicle_approach_thresholds_preferences.pb",
        serializer = LmuWindowsVehicleApproachThresholdsPreferencesSerializer,
    )
