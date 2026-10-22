package kurou.kodriver.core.acewindowsdata.preferences

import androidx.datastore.core.Serializer

internal val AceWindowsRemainingFuelLapsPreferencesSerializer: Serializer<AceWindowsRemainingFuelLapsPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = AceWindowsRemainingFuelLapsPreferences(),
        kSerializer = AceWindowsRemainingFuelLapsPreferences.serializer(),
    )
