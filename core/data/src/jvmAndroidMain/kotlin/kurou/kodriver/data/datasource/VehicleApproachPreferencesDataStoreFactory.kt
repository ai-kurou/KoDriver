package kurou.kodriver.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import kurou.kodriver.data.model.VehicleApproachPreferences
import java.io.File

internal fun createVehicleApproachPreferencesDataStore(directory: String): DataStore<VehicleApproachPreferences> =
    DataStoreFactory.create(
        serializer = VehicleApproachPreferencesSerializer,
        produceFile = { File("$directory/vehicle_approach_preferences.pb") },
    )
