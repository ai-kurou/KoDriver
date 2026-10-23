package kurou.kodriver.core.lmuwindowsdata.preferences

import androidx.datastore.core.Serializer

internal val FlagReadoutTextPreferencesSerializer: Serializer<FlagReadoutTextPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = FlagReadoutTextPreferences(),
        kSerializer = FlagReadoutTextPreferences.serializer(),
    )
