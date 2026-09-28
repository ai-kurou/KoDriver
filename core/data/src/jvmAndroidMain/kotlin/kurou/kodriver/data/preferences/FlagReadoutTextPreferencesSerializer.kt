package kurou.kodriver.data.preferences

import androidx.datastore.core.Serializer

internal val FlagReadoutTextPreferencesSerializer: Serializer<FlagReadoutTextPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = FlagReadoutTextPreferences(),
        kSerializer = FlagReadoutTextPreferences.serializer(),
    )
