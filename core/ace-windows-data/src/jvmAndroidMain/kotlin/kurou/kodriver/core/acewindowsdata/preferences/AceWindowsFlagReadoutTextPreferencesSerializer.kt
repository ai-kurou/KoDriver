package kurou.kodriver.core.acewindowsdata.preferences

import androidx.datastore.core.Serializer

internal val AceWindowsFlagReadoutTextPreferencesSerializer: Serializer<AceWindowsFlagReadoutTextPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = AceWindowsFlagReadoutTextPreferences(),
        kSerializer = AceWindowsFlagReadoutTextPreferences.serializer(),
    )
