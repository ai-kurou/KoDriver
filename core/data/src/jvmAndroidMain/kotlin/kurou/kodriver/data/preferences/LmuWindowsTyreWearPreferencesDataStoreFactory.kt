package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore

internal fun createLmuWindowsTyreWearPreferencesDataStore(directory: String): DataStore<LmuWindowsTyreWearPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "lmu_windows_tyre_wear_preferences.pb",
        serializer = LmuWindowsTyreWearPreferencesSerializer,
    )
