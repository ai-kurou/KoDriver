package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore

internal fun createLmuWindowsRemainingVirtualEnergyPreferencesDataStore(
    directory: String,
): DataStore<LmuWindowsRemainingVirtualEnergyPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "lmu_windows_remaining_virtual_energy_preferences.pb",
        serializer = LmuWindowsRemainingVirtualEnergyPreferencesSerializer,
    )
