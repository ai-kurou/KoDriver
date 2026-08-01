package kurou.kodriver.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import kurou.kodriver.data.model.LmuWindowsTyreTemperaturePreferences
import java.io.File

internal fun createLmuWindowsTyreTemperaturePreferencesDataStore(directory: String): DataStore<LmuWindowsTyreTemperaturePreferences> =
    DataStoreFactory.create(
        serializer = LmuWindowsTyreTemperaturePreferencesSerializer,
        produceFile = { File("$directory/lmu_windows_tyre_temperature_preferences.pb") },
    )
