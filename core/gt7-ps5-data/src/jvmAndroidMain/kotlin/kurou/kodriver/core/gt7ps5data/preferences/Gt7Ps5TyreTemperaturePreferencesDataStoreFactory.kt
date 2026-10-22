package kurou.kodriver.core.gt7ps5data.preferences

import androidx.datastore.core.DataStore

internal fun createGt7Ps5TyreTemperaturePreferencesDataStore(
    directory: String,
): DataStore<Gt7Ps5TyreTemperaturePreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "gt7_ps5_auto_tyre_temperature_preferences.pb",
        serializer = Gt7Ps5TyreTemperaturePreferencesSerializer,
    )
