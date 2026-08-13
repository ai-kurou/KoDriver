package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore

internal fun createLmuWindowsVehicleClassTyreTemperaturePreferencesDataStore(
    directory: String,
): DataStore<LmuWindowsVehicleClassTyreTemperaturePreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "lmu_windows_vehicle_class_tyre_temperature_preferences.pb",
        serializer = LmuWindowsVehicleClassTyreTemperaturePreferencesSerializer,
    )
