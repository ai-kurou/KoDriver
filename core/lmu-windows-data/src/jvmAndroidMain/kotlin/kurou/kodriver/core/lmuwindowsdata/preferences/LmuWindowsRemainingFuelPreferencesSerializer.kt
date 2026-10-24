package kurou.kodriver.core.lmuwindowsdata.preferences

import androidx.datastore.core.Serializer

internal val LmuWindowsRemainingFuelPreferencesSerializer: Serializer<LmuWindowsRemainingFuelPreferences> =
    protoBufPreferencesSerializer(LmuWindowsRemainingFuelPreferences(), LmuWindowsRemainingFuelPreferences.serializer())
