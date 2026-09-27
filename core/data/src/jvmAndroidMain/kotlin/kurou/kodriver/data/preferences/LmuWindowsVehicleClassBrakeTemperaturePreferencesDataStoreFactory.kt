package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore

internal fun createLmuWindowsVehicleClassBrakeTemperaturePreferencesDataStore(
    directory: String,
): DataStore<LmuWindowsVehicleClassBrakeTemperaturePreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "lmu_windows_vehicle_class_brake_temperature_preferences.pb",
        serializer = LmuWindowsVehicleClassBrakeTemperaturePreferencesSerializer,
    )
