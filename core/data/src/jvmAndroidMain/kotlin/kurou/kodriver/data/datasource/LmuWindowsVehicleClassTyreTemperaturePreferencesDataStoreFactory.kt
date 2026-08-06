package kurou.kodriver.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import kurou.kodriver.data.model.LmuWindowsVehicleClassTyreTemperaturePreferences
import java.io.File

internal fun createLmuWindowsVehicleClassTyreTemperaturePreferencesDataStore(
    directory: String,
): DataStore<LmuWindowsVehicleClassTyreTemperaturePreferences> =
    DataStoreFactory.create(
        serializer = LmuWindowsVehicleClassTyreTemperaturePreferencesSerializer,
        produceFile = { File("$directory/lmu_windows_vehicle_class_tyre_temperature_preferences.pb") },
    )
