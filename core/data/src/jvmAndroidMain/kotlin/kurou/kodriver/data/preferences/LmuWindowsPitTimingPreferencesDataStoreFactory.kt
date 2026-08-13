package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore

internal fun createLmuWindowsPitTimingPreferencesDataStore(
    directory: String,
): DataStore<LmuWindowsPitTimingPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "lmu_windows_pit_timing_preferences.pb",
        serializer = LmuWindowsPitTimingPreferencesSerializer,
    )
