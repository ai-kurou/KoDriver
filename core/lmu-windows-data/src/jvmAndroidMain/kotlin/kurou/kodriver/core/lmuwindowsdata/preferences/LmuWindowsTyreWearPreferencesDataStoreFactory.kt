package kurou.kodriver.core.lmuwindowsdata.preferences

import androidx.datastore.core.DataStore

internal fun createLmuWindowsTyreWearPreferencesDataStore(directory: String): DataStore<LmuWindowsTyreWearPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "lmu_windows_auto_tyre_wear_preferences.pb",
        serializer = LmuWindowsTyreWearPreferencesSerializer,
    )
