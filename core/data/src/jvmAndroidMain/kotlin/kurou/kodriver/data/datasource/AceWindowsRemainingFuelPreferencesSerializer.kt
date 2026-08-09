package kurou.kodriver.data.datasource

import androidx.datastore.core.Serializer
import kurou.kodriver.data.model.AceWindowsRemainingFuelPreferences

internal val AceWindowsRemainingFuelPreferencesSerializer: Serializer<AceWindowsRemainingFuelPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = AceWindowsRemainingFuelPreferences(),
        kSerializer = AceWindowsRemainingFuelPreferences.serializer(),
    )
