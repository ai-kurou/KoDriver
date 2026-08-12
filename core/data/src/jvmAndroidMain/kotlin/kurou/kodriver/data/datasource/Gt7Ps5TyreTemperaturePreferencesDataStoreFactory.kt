package kurou.kodriver.data.datasource

import androidx.datastore.core.DataStore
import kurou.kodriver.data.model.Gt7Ps5TyreTemperaturePreferences

internal fun createGt7Ps5TyreTemperaturePreferencesDataStore(
    directory: String,
): DataStore<Gt7Ps5TyreTemperaturePreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "gt7_ps5_tyre_temperature_preferences.pb",
        serializer = Gt7Ps5TyreTemperaturePreferencesSerializer,
    )
