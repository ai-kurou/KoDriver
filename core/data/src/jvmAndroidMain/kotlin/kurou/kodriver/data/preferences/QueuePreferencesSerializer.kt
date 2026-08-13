package kurou.kodriver.data.preferences

import androidx.datastore.core.Serializer

internal val QueuePreferencesSerializer: Serializer<QueuePreferences> =
    protoBufPreferencesSerializer(
        defaultValue = QueuePreferences(),
        kSerializer = QueuePreferences.serializer(),
    )
