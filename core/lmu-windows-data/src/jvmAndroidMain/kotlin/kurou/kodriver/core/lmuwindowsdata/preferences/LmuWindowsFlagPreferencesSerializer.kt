package kurou.kodriver.core.lmuwindowsdata.preferences

import androidx.datastore.core.Serializer

internal val LmuWindowsFlagPreferencesSerializer: Serializer<LmuWindowsFlagPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = LmuWindowsFlagPreferences(),
        kSerializer = LmuWindowsFlagPreferences.serializer(),
    )
