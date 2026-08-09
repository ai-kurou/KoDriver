package kurou.kodriver.data.datasource

import androidx.datastore.core.DataStore
import kurou.kodriver.data.model.LmuWindowsPitTimingPreferences

internal fun createLmuWindowsPitTimingPreferencesDataStore(
    directory: String,
): DataStore<LmuWindowsPitTimingPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "lmu_windows_pit_timing_preferences.pb",
        serializer = LmuWindowsPitTimingPreferencesSerializer,
    )
