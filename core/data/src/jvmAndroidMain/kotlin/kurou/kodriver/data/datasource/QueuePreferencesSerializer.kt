package kurou.kodriver.data.datasource

import androidx.datastore.core.Serializer
import kurou.kodriver.data.model.QueuePreferences

internal val QueuePreferencesSerializer: Serializer<QueuePreferences> =
    protoBufPreferencesSerializer(
        defaultValue = QueuePreferences(),
        kSerializer = QueuePreferences.serializer(),
    )
