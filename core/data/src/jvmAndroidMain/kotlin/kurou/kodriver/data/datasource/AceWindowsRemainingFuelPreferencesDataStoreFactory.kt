package kurou.kodriver.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import kurou.kodriver.data.model.AceWindowsRemainingFuelPreferences
import java.io.File

internal fun createAceWindowsRemainingFuelPreferencesDataStore(directory: String): DataStore<AceWindowsRemainingFuelPreferences> =
    DataStoreFactory.create(
        serializer = AceWindowsRemainingFuelPreferencesSerializer,
        produceFile = { File("$directory/ace_windows_remaining_fuel_preferences.pb") },
    )
