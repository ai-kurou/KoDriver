package kurou.kodriver.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import kurou.kodriver.data.model.ProximityThresholdsPreferences
import java.io.File

internal fun createProximityThresholdsDataStore(directory: String): DataStore<ProximityThresholdsPreferences> =
    DataStoreFactory.create(
        serializer = ProximityThresholdsSerializer,
        produceFile = { File("$directory/proximity_thresholds.pb") },
    )
