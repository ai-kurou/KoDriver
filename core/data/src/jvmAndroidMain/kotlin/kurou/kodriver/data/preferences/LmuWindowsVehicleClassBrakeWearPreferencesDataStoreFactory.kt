package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore

internal fun createLmuWindowsVehicleClassBrakeWearPreferencesDataStore(
    directory: String,
): DataStore<LmuWindowsVehicleClassBrakeWearPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "lmu_windows_vehicle_class_brake_wear_preferences.pb",
        serializer = LmuWindowsVehicleClassBrakeWearPreferencesSerializer,
    )
