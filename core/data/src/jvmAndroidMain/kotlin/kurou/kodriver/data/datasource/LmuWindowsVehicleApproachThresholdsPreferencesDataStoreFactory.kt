package kurou.kodriver.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import kurou.kodriver.data.model.LmuWindowsVehicleApproachThresholdsPreferences
import java.io.File

internal fun createLmuWindowsVehicleApproachThresholdsPreferencesDataStore(
    directory: String,
): DataStore<LmuWindowsVehicleApproachThresholdsPreferences> =
    DataStoreFactory.create(
        serializer = LmuWindowsVehicleApproachThresholdsPreferencesSerializer,
        produceFile = { File("$directory/lmu_windows_vehicle_approach_thresholds_preferences.pb") },
    )
