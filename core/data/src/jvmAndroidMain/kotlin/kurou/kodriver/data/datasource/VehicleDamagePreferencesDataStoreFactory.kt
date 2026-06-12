package kurou.kodriver.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import kurou.kodriver.data.model.VehicleDamagePreferences
import java.io.File

internal fun createVehicleDamagePreferencesDataStore(directory: String): DataStore<VehicleDamagePreferences> =
    DataStoreFactory.create(
        serializer = VehicleDamagePreferencesSerializer,
        produceFile = { File("$directory/vehicle_damage_preferences.pb") },
    )
