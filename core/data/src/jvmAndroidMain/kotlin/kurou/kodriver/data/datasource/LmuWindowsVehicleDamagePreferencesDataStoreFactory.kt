package kurou.kodriver.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import kurou.kodriver.data.model.LmuWindowsVehicleDamagePreferences
import java.io.File

internal fun createLmuWindowsVehicleDamagePreferencesDataStore(directory: String): DataStore<LmuWindowsVehicleDamagePreferences> =
    DataStoreFactory.create(
        serializer = LmuWindowsVehicleDamagePreferencesSerializer,
        produceFile = { File("$directory/lmu_windows_vehicle_damage_preferences.pb") },
    )
