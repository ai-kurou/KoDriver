package kurou.kodriver.data.preferences

import androidx.datastore.core.Serializer

internal val AceWindowsFlagPreferencesSerializer: Serializer<AceWindowsFlagPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = AceWindowsFlagPreferences(),
        kSerializer = AceWindowsFlagPreferences.serializer(),
    )
