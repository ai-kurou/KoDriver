package kurou.kodriver.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import kurou.kodriver.data.model.LmuWindowsVehicleApproachPreferences
import java.io.File

internal fun createLmuWindowsVehicleApproachPreferencesDataStore(directory: String): DataStore<LmuWindowsVehicleApproachPreferences> =
    DataStoreFactory.create(
        serializer = LmuWindowsVehicleApproachPreferencesSerializer,
        produceFile = { File("$directory/lmu_windows_vehicle_approach_preferences.pb") },
    )
