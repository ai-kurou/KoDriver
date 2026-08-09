package kurou.kodriver.data.datasource

import androidx.datastore.core.DataStore
import kurou.kodriver.data.model.Gt7Ps5RemainingFuelPreferences

internal fun createGt7Ps5RemainingFuelPreferencesDataStore(
    directory: String,
): DataStore<Gt7Ps5RemainingFuelPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "gt7_ps5_remaining_fuel_preferences.pb",
        serializer = Gt7Ps5RemainingFuelPreferencesSerializer,
    )
