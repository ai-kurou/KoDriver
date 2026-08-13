package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore

internal fun createLmuWindowsVehicleApproachThresholdsPreferencesDataStore(
    directory: String,
): DataStore<LmuWindowsVehicleApproachThresholdsPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "lmu_windows_vehicle_approach_thresholds_preferences.pb",
        serializer = LmuWindowsVehicleApproachThresholdsPreferencesSerializer,
    )
