package kurou.kodriver.core.lmuwindowsdata.preferences

import androidx.datastore.core.DataStore

internal fun createLmuWindowsRemainingFuelPreferencesDataStore(
    directory: String,
): DataStore<LmuWindowsRemainingFuelPreferences> =
    preferencesDataStore(
        directory,
        "lmu_windows_auto_remaining_fuel_preferences.pb",
        LmuWindowsRemainingFuelPreferencesSerializer,
    )
