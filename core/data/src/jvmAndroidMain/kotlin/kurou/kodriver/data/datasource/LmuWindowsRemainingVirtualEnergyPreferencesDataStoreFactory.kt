package kurou.kodriver.data.datasource

import androidx.datastore.core.DataStore
import kurou.kodriver.data.model.LmuWindowsRemainingVirtualEnergyPreferences

internal fun createLmuWindowsRemainingVirtualEnergyPreferencesDataStore(
    directory: String,
): DataStore<LmuWindowsRemainingVirtualEnergyPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "lmu_windows_remaining_virtual_energy_preferences.pb",
        serializer = LmuWindowsRemainingVirtualEnergyPreferencesSerializer,
    )
