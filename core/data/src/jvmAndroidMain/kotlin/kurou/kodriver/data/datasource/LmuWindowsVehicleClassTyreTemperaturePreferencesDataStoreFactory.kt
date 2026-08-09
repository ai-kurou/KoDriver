package kurou.kodriver.data.datasource

import androidx.datastore.core.DataStore
import kurou.kodriver.data.model.LmuWindowsVehicleClassTyreTemperaturePreferences

internal fun createLmuWindowsVehicleClassTyreTemperaturePreferencesDataStore(
    directory: String,
): DataStore<LmuWindowsVehicleClassTyreTemperaturePreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "lmu_windows_vehicle_class_tyre_temperature_preferences.pb",
        serializer = LmuWindowsVehicleClassTyreTemperaturePreferencesSerializer,
    )
