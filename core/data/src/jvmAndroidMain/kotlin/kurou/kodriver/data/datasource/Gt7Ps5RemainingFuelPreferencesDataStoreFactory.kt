package kurou.kodriver.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import kurou.kodriver.data.model.Gt7Ps5RemainingFuelPreferences
import java.io.File

internal fun createGt7Ps5RemainingFuelPreferencesDataStore(
    directory: String,
): DataStore<Gt7Ps5RemainingFuelPreferences> =
    DataStoreFactory.create(
        serializer = Gt7Ps5RemainingFuelPreferencesSerializer,
        produceFile = { File("$directory/gt7_ps5_remaining_fuel_preferences.pb") },
    )
