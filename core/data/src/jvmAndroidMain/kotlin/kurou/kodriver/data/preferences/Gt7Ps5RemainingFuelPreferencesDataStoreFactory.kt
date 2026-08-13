package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore

internal fun createGt7Ps5RemainingFuelPreferencesDataStore(
    directory: String,
): DataStore<Gt7Ps5RemainingFuelPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "gt7_ps5_remaining_fuel_preferences.pb",
        serializer = Gt7Ps5RemainingFuelPreferencesSerializer,
    )
