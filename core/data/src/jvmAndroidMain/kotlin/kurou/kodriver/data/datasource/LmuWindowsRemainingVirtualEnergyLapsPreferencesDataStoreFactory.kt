package kurou.kodriver.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import kurou.kodriver.data.model.LmuWindowsRemainingVirtualEnergyLapsPreferences
import java.io.File

internal fun createLmuWindowsRemainingVirtualEnergyLapsPreferencesDataStore(
    directory: String,
): DataStore<LmuWindowsRemainingVirtualEnergyLapsPreferences> =
    DataStoreFactory.create(
        serializer = LmuWindowsRemainingVirtualEnergyLapsPreferencesSerializer,
        produceFile = { File("$directory/lmu_windows_remaining_virtual_energy_laps_preferences.pb") },
    )
