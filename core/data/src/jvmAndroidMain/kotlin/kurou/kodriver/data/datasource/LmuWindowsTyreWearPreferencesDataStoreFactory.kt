package kurou.kodriver.data.datasource

import androidx.datastore.core.DataStore
import kurou.kodriver.data.model.LmuWindowsTyreWearPreferences

internal fun createLmuWindowsTyreWearPreferencesDataStore(directory: String): DataStore<LmuWindowsTyreWearPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "lmu_windows_tyre_wear_preferences.pb",
        serializer = LmuWindowsTyreWearPreferencesSerializer,
    )
