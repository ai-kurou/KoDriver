package kurou.kodriver.core.lmuwindowsdata.preferences

import androidx.datastore.core.DataStore

internal fun createLmuWindowsVehicleDamagePreferencesDataStore(
    directory: String,
): DataStore<LmuWindowsVehicleDamagePreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "lmu_windows_auto_vehicle_damage_preferences.pb",
        serializer = LmuWindowsVehicleDamagePreferencesSerializer,
    )
