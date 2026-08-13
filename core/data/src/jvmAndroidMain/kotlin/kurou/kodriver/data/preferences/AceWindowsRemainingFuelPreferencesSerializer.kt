package kurou.kodriver.data.preferences

import androidx.datastore.core.Serializer

internal val AceWindowsRemainingFuelPreferencesSerializer: Serializer<AceWindowsRemainingFuelPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = AceWindowsRemainingFuelPreferences(),
        kSerializer = AceWindowsRemainingFuelPreferences.serializer(),
    )
