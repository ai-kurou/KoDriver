package kurou.kodriver.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import kurou.kodriver.data.model.ReadoutStartSoundPreferences
import java.io.File

internal fun createReadoutStartSoundPreferencesDataStore(directory: String): DataStore<ReadoutStartSoundPreferences> =
    DataStoreFactory.create(
        serializer = ReadoutStartSoundPreferencesSerializer,
        produceFile = { File("$directory/readout_start_sound_preferences.pb") },
    )
