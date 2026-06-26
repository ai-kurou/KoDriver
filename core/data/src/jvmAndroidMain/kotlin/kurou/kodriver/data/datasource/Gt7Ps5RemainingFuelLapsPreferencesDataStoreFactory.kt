package kurou.kodriver.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import kurou.kodriver.data.model.Gt7Ps5RemainingFuelLapsPreferences
import java.io.File

internal fun createGt7Ps5RemainingFuelLapsPreferencesDataStore(
    directory: String,
): DataStore<Gt7Ps5RemainingFuelLapsPreferences> =
    DataStoreFactory.create(
        serializer = Gt7Ps5RemainingFuelLapsPreferencesSerializer,
        produceFile = { File("$directory/gt7_ps5_remaining_fuel_laps_preferences.pb") },
    )
