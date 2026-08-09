package kurou.kodriver.data.datasource

import androidx.datastore.core.DataStore
import kurou.kodriver.data.model.LmuWindowsTyreTemperaturePreferences

internal fun createLmuWindowsTyreTemperaturePreferencesDataStore(
    directory: String,
): DataStore<LmuWindowsTyreTemperaturePreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "lmu_windows_tyre_temperature_preferences.pb",
        serializer = LmuWindowsTyreTemperaturePreferencesSerializer,
    )
