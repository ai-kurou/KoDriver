package kurou.kodriver.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import kurou.kodriver.data.model.LmuWindowsRemainingVirtualEnergyPreferences
import java.io.File

internal fun createLmuWindowsRemainingVirtualEnergyPreferencesDataStore(
    directory: String,
): DataStore<LmuWindowsRemainingVirtualEnergyPreferences> =
    DataStoreFactory.create(
        serializer = LmuWindowsRemainingVirtualEnergyPreferencesSerializer,
        produceFile = { File("$directory/lmu_windows_remaining_virtual_energy_preferences.pb") },
    )
