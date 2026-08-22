package kurou.kodriver.data.preferences

import androidx.datastore.core.Serializer

internal val AceWindowsVehicleApproachPreferencesSerializer: Serializer<AceWindowsVehicleApproachPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = AceWindowsVehicleApproachPreferences(),
        kSerializer = AceWindowsVehicleApproachPreferences.serializer(),
    )
