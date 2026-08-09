package kurou.kodriver.data.datasource

import androidx.datastore.core.Serializer
import kurou.kodriver.data.model.RedFlagPreferences

internal val RedFlagPreferencesSerializer: Serializer<RedFlagPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = RedFlagPreferences(),
        kSerializer = RedFlagPreferences.serializer(),
        typeName = "RedFlagPreferences",
    )
