package kurou.kodriver.core.lmuwindowsdata.preferences

import androidx.datastore.core.Serializer

internal val LmuWindowsVehicleApproachPreferencesSerializer: Serializer<LmuWindowsVehicleApproachPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = LmuWindowsVehicleApproachPreferences(),
        kSerializer = LmuWindowsVehicleApproachPreferences.serializer(),
    )
