package kurou.kodriver.data.datasource

import androidx.datastore.core.DataStore
import kurou.kodriver.data.model.Gt7Ps5RemainingFuelLapsPreferences

internal fun createGt7Ps5RemainingFuelLapsPreferencesDataStore(
    directory: String,
): DataStore<Gt7Ps5RemainingFuelLapsPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "gt7_ps5_remaining_fuel_laps_preferences.pb",
        serializer = Gt7Ps5RemainingFuelLapsPreferencesSerializer,
    )
