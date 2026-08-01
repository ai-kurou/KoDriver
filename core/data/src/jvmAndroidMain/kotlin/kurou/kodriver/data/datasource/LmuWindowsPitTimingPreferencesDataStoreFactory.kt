package kurou.kodriver.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import kurou.kodriver.data.model.LmuWindowsPitTimingPreferences
import java.io.File

internal fun createLmuWindowsPitTimingPreferencesDataStore(directory: String): DataStore<LmuWindowsPitTimingPreferences> =
    DataStoreFactory.create(
        serializer = LmuWindowsPitTimingPreferencesSerializer,
        produceFile = { File("$directory/lmu_windows_pit_timing_preferences.pb") },
    )
