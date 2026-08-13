package kurou.kodriver.data.preferences

import androidx.datastore.core.Serializer

internal val LmuWindowsFlagPreferencesSerializer: Serializer<LmuWindowsFlagPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = LmuWindowsFlagPreferences(),
        kSerializer = LmuWindowsFlagPreferences.serializer(),
    )
