package kurou.kodriver.data.preferences

import androidx.datastore.core.Serializer

internal val AceWindowsFlagReadoutTextPreferencesSerializer: Serializer<AceWindowsFlagReadoutTextPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = AceWindowsFlagReadoutTextPreferences(),
        kSerializer = AceWindowsFlagReadoutTextPreferences.serializer(),
    )
