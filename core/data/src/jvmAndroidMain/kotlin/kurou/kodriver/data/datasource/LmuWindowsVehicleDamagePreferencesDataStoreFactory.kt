package kurou.kodriver.data.datasource

import androidx.datastore.core.DataStore
import kurou.kodriver.data.model.LmuWindowsVehicleDamagePreferences

internal fun createLmuWindowsVehicleDamagePreferencesDataStore(
    directory: String,
): DataStore<LmuWindowsVehicleDamagePreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "lmu_windows_vehicle_damage_preferences.pb",
        serializer = LmuWindowsVehicleDamagePreferencesSerializer,
    )
