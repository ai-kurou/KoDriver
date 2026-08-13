package kurou.kodriver.data.preferences

import androidx.datastore.core.Serializer

internal val RedFlagPreferencesSerializer: Serializer<RedFlagPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = RedFlagPreferences(),
        kSerializer = RedFlagPreferences.serializer(),
    )
